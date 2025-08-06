//
//  AI.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation

class AI {
    private let grid: Grid
    private let fleet: Fleet
    private var probabilityGrid: [[Double]]
    private var huntMode: Bool = false
    private var huntTargets: [(x: Int, y: Int)] = []
    private var lastHit: (x: Int, y: Int)?
    private var huntDirection: (dx: Int, dy: Int)?
    
    init(grid: Grid, fleet: Fleet) {
        self.grid = grid
        self.fleet = fleet
        self.probabilityGrid = Array(repeating: Array(repeating: 0.0, count: GameConstants.gridSize), count: GameConstants.gridSize)
        calculateInitialProbabilities()
    }
    
    // MARK: - AI Decision Making
    
    func makeMove() -> (x: Int, y: Int) {
        updateProbabilities()
        
        if huntMode && !huntTargets.isEmpty {
            // In hunt mode, target adjacent cells to known hits
            return selectHuntTarget()
        } else {
            // In search mode, use probability-based targeting
            return selectBestProbabilityTarget()
        }
    }
    
    func processResult(x: Int, y: Int, result: GameConstants.CellType, sunkShip: Ship?) {
        switch result {
        case .hit:
            handleHit(x: x, y: y)
        case .miss:
            handleMiss(x: x, y: y)
        case .sunk:
            handleSunk(x: x, y: y, ship: sunkShip)
        default:
            break
        }
        
        updateProbabilities()
    }
    
    // MARK: - Hunt Mode Logic
    
    private func handleHit(x: Int, y: Int) {
        lastHit = (x: x, y: y)
        huntMode = true
        
        // Add adjacent cells to hunt targets
        let adjacent = grid.getAdjacentCoordinates(x: x, y: y, includeDiagonals: false)
        for coord in adjacent {
            let cellType = grid.getCellType(x: coord.x, y: coord.y)
            if (cellType == .empty || cellType == .ship) && !huntTargets.contains(where: { $0.x == coord.x && $0.y == coord.y }) {
                huntTargets.append(coord)
            }
        }
        
        // If we have a previous hit in a line, prioritize that direction
        updateHuntDirection(newHit: (x: x, y: y))
    }
    
    private func handleMiss(x: Int, y: Int) {
        // Remove from hunt targets if it was there
        huntTargets.removeAll { $0.x == x && $0.y == y }
        
        // If we're in hunt mode and this was following a direction, reverse direction
        if let direction = huntDirection, let lastHit = lastHit {
            let oppositeDirection = (dx: -direction.dx, dy: -direction.dy)
            let oppositeTarget = (x: lastHit.x + oppositeDirection.dx, y: lastHit.y + oppositeDirection.dy)
            
            if grid.isValidCoordinate(x: oppositeTarget.x, y: oppositeTarget.y) {
                let cellType = grid.getCellType(x: oppositeTarget.x, y: oppositeTarget.y)
                if cellType == .empty || cellType == .ship {
                    huntTargets.insert(oppositeTarget, at: 0) // Priority target
                }
            }
        }
    }
    
    private func handleSunk(x: Int, y: Int, ship: Ship?) {
        // Clear hunt mode
        huntMode = false
        huntTargets.removeAll()
        lastHit = nil
        huntDirection = nil
        
        // Remove cells around the sunk ship from future consideration
        if let ship = ship {
            for cell in ship.getAllShipCells() {
                let adjacent = grid.getAdjacentCoordinates(x: cell.x, y: cell.y, includeDiagonals: true)
                for coord in adjacent {
                    let cellType = grid.getCellType(x: coord.x, y: coord.y)
                    if cellType == .empty || cellType == .ship {
                        // These cells can't contain ships (ships don't touch)
                        // We'll reduce their probability significantly
                    }
                }
            }
        }
    }
    
    private func updateHuntDirection(newHit: (x: Int, y: Int)) {
        guard let lastHit = lastHit else { return }
        
        let dx = newHit.x - lastHit.x
        let dy = newHit.y - lastHit.y
        
        if abs(dx) == 1 && dy == 0 {
            huntDirection = (dx: dx, dy: 0)
        } else if abs(dy) == 1 && dx == 0 {
            huntDirection = (dx: 0, dy: dy)
        }
    }
    
    private func selectHuntTarget() -> (x: Int, y: Int) {
        // Prioritize targets in the hunt direction if we have one
        if let direction = huntDirection, let lastHit = lastHit {
            let priorityTarget = (x: lastHit.x + direction.dx, y: lastHit.y + direction.dy)
            
            if grid.isValidCoordinate(x: priorityTarget.x, y: priorityTarget.y) {
                let cellType = grid.getCellType(x: priorityTarget.x, y: priorityTarget.y)
                if cellType == .empty || cellType == .ship {
                    huntTargets.removeAll { $0.x == priorityTarget.x && $0.y == priorityTarget.y }
                    return priorityTarget
                }
            }
        }
        
        // Otherwise, select the first available hunt target
        if let target = huntTargets.first {
            huntTargets.removeFirst()
            return target
        }
        
        // Fall back to probability-based selection
        huntMode = false
        return selectBestProbabilityTarget()
    }
    
    // MARK: - Probability Calculation
    
    private func calculateInitialProbabilities() {
        // Reset probability grid
        for x in 0..<GameConstants.gridSize {
            for y in 0..<GameConstants.gridSize {
                probabilityGrid[x][y] = 0.0
            }
        }
        
        // Calculate probabilities for each ship type
        for shipType in GameConstants.ShipType.allCases {
            calculateProbabilitiesForShip(shipType: shipType)
        }
    }
    
    private func updateProbabilities() {
        calculateInitialProbabilities()
        applyHeuristicModifiers()
    }
    
    private func calculateProbabilitiesForShip(shipType: GameConstants.ShipType) {
        let shipSize = shipType.size
        
        // Try placing the ship in every possible position and orientation
        for x in 0..<GameConstants.gridSize {
            for y in 0..<GameConstants.gridSize {
                for direction in [GameConstants.ShipDirection.horizontal, GameConstants.ShipDirection.vertical] {
                    if canPlaceVirtualShip(x: x, y: y, size: shipSize, direction: direction) {
                        // Add probability weight to all cells this ship would occupy
                        let coords = calculateShipCoordinates(x: x, y: y, size: shipSize, direction: direction)
                        for coord in coords {
                            probabilityGrid[coord.x][coord.y] += 1.0
                        }
                    }
                }
            }
        }
    }
    
    private func canPlaceVirtualShip(x: Int, y: Int, size: Int, direction: GameConstants.ShipDirection) -> Bool {
        let coords = calculateShipCoordinates(x: x, y: y, size: size, direction: direction)
        
        var hitCount = 0
        
        for coord in coords {
            // Check bounds
            if !grid.isValidCoordinate(x: coord.x, y: coord.y) {
                return false
            }
            
            let cellType = grid.getCellType(x: coord.x, y: coord.y)
            
            // Can't place on miss or sunk cells
            if cellType == .miss || cellType == .sunk {
                return false
            }
            
            // Count hits - ship must account for all hits in its path
            if cellType == .hit {
                hitCount += 1
            }
        }
        
        // For hunt mode, prioritize ships that contain known hits
        if huntMode && hitCount == 0 {
            return false
        }
        
        return true
    }
    
    private func calculateShipCoordinates(x: Int, y: Int, size: Int, direction: GameConstants.ShipDirection) -> [(x: Int, y: Int)] {
        var coords: [(x: Int, y: Int)] = []
        
        for i in 0..<size {
            if direction == .horizontal {
                coords.append((x: x + i, y: y))
            } else {
                coords.append((x: x, y: y + i))
            }
        }
        
        return coords
    }
    
    private func applyHeuristicModifiers() {
        for x in 0..<GameConstants.gridSize {
            for y in 0..<GameConstants.gridSize {
                // Don't target cells we've already shot
                let cellType = grid.getCellType(x: x, y: y)
                if cellType == .miss || cellType == .hit || cellType == .sunk {
                    probabilityGrid[x][y] = 0.0
                    continue
                }
                
                // Boost probability for cells adjacent to hits
                let adjacent = grid.getAdjacentCoordinates(x: x, y: y, includeDiagonals: false)
                for coord in adjacent {
                    if grid.isHit(x: coord.x, y: coord.y) {
                        probabilityGrid[x][y] *= 2.0
                    }
                }
                
                // Reduce probability near sunk ships (ships don't touch)
                let adjacentWithDiagonals = grid.getAdjacentCoordinates(x: x, y: y, includeDiagonals: true)
                for coord in adjacentWithDiagonals {
                    if grid.isSunk(x: coord.x, y: coord.y) {
                        probabilityGrid[x][y] *= 0.1
                    }
                }
                
                // Apply checkerboard pattern for parity (ships must be hit)
                // This gives slight preference to cells that follow ship placement patterns
                if (x + y) % 2 == 0 {
                    probabilityGrid[x][y] *= 1.1
                }
            }
        }
    }
    
    private func selectBestProbabilityTarget() -> (x: Int, y: Int) {
        var bestX = 0
        var bestY = 0
        var maxProbability = -1.0
        
        for x in 0..<GameConstants.gridSize {
            for y in 0..<GameConstants.gridSize {
                let cellType = grid.getCellType(x: x, y: y)
                if (cellType == .empty || cellType == .ship) && probabilityGrid[x][y] > maxProbability {
                    maxProbability = probabilityGrid[x][y]
                    bestX = x
                    bestY = y
                }
            }
        }
        
        return (x: bestX, y: bestY)
    }
    
    // MARK: - Debugging
    
    func printProbabilityGrid() {
        print("AI Probability Grid:")
        for y in 0..<GameConstants.gridSize {
            var row = ""
            for x in 0..<GameConstants.gridSize {
                let prob = probabilityGrid[x][y]
                let cellType = grid.getCellType(x: x, y: y)
                if cellType == .empty || cellType == .ship {
                    row += String(format: "%3.0f ", prob)
                } else {
                    row += "  X "
                }
            }
            print(row)
        }
        print("")
    }
    
    func getProbabilityAt(x: Int, y: Int) -> Double {
        guard grid.isValidCoordinate(x: x, y: y) else { return 0.0 }
        return probabilityGrid[x][y]
    }
} 