//
//  Grid.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation

class Grid {
    private var cells: [[GameConstants.CellType]]
    let size: Int
    let player: Int
    
    init(size: Int = GameConstants.gridSize, player: Int) {
        self.size = size
        self.player = player
        self.cells = Array(repeating: Array(repeating: .empty, count: size), count: size)
    }
    
    // MARK: - Cell Management
    
    func getCellType(x: Int, y: Int) -> GameConstants.CellType {
        guard isValidCoordinate(x: x, y: y) else { return .empty }
        return cells[x][y]
    }
    
    func setCellType(x: Int, y: Int, type: GameConstants.CellType) {
        guard isValidCoordinate(x: x, y: y) else { return }
        cells[x][y] = type
    }
    
    func isValidCoordinate(x: Int, y: Int) -> Bool {
        return x >= 0 && x < size && y >= 0 && y < size
    }
    
    // MARK: - Cell Type Checks
    
    func isEmpty(x: Int, y: Int) -> Bool {
        return getCellType(x: x, y: y) == .empty
    }
    
    func isShip(x: Int, y: Int) -> Bool {
        return getCellType(x: x, y: y) == .ship
    }
    
    func isHit(x: Int, y: Int) -> Bool {
        return getCellType(x: x, y: y) == .hit
    }
    
    func isMiss(x: Int, y: Int) -> Bool {
        return getCellType(x: x, y: y) == .miss
    }
    
    func isSunk(x: Int, y: Int) -> Bool {
        return getCellType(x: x, y: y) == .sunk
    }
    
    func isUndamagedShip(x: Int, y: Int) -> Bool {
        return getCellType(x: x, y: y) == .ship
    }
    
    func isDamagedShip(x: Int, y: Int) -> Bool {
        let cellType = getCellType(x: x, y: y)
        return cellType == .hit || cellType == .sunk
    }
    
    func isWater(x: Int, y: Int) -> Bool {
        let cellType = getCellType(x: x, y: y)
        return cellType == .empty || cellType == .miss
    }
    
    // MARK: - Game Actions
    
    func shoot(x: Int, y: Int) -> GameConstants.CellType {
        guard isValidCoordinate(x: x, y: y) else { return .empty }
        
        let currentType = getCellType(x: x, y: y)
        
        switch currentType {
        case .empty:
            setCellType(x: x, y: y, type: .miss)
            return .miss
        case .ship:
            setCellType(x: x, y: y, type: .hit)
            return .hit
        case .miss, .hit, .sunk:
            // Already shot here
            return currentType
        }
    }
    
    func markAsSunk(coordinates: [(x: Int, y: Int)]) {
        for coord in coordinates {
            if isValidCoordinate(x: coord.x, y: coord.y) {
                setCellType(x: coord.x, y: coord.y, type: .sunk)
            }
        }
    }
    
    // MARK: - Grid Analysis
    
    func getAllShipCoordinates() -> [(x: Int, y: Int)] {
        var shipCoords: [(x: Int, y: Int)] = []
        
        for x in 0..<size {
            for y in 0..<size {
                if isShip(x: x, y: y) || isDamagedShip(x: x, y: y) {
                    shipCoords.append((x: x, y: y))
                }
            }
        }
        
        return shipCoords
    }
    
    func getAllHitCoordinates() -> [(x: Int, y: Int)] {
        var hitCoords: [(x: Int, y: Int)] = []
        
        for x in 0..<size {
            for y in 0..<size {
                if isHit(x: x, y: y) {
                    hitCoords.append((x: x, y: y))
                }
            }
        }
        
        return hitCoords
    }
    
    func getAllMissCoordinates() -> [(x: Int, y: Int)] {
        var missCoords: [(x: Int, y: Int)] = []
        
        for x in 0..<size {
            for y in 0..<size {
                if isMiss(x: x, y: y) {
                    missCoords.append((x: x, y: y))
                }
            }
        }
        
        return missCoords
    }
    
    func getAdjacentCoordinates(x: Int, y: Int, includeDiagonals: Bool = false) -> [(x: Int, y: Int)] {
        var adjacent: [(x: Int, y: Int)] = []
        
        let directions: [(Int, Int)] = includeDiagonals ?
            [(-1, -1), (-1, 0), (-1, 1), (0, -1), (0, 1), (1, -1), (1, 0), (1, 1)] :
            [(-1, 0), (0, -1), (0, 1), (1, 0)]
        
        for (dx, dy) in directions {
            let newX = x + dx
            let newY = y + dy
            
            if isValidCoordinate(x: newX, y: newY) {
                adjacent.append((x: newX, y: newY))
            }
        }
        
        return adjacent
    }
    
    // MARK: - Grid State
    
    func reset() {
        for x in 0..<size {
            for y in 0..<size {
                cells[x][y] = .empty
            }
        }
    }
    
    func copy() -> Grid {
        let newGrid = Grid(size: size, player: player)
        for x in 0..<size {
            for y in 0..<size {
                newGrid.setCellType(x: x, y: y, type: getCellType(x: x, y: y))
            }
        }
        return newGrid
    }
    
    // MARK: - Debug
    
    func printGrid() {
        print("Grid for player \(player):")
        for y in 0..<size {
            var row = ""
            for x in 0..<size {
                let cellType = getCellType(x: x, y: y)
                switch cellType {
                case .empty: row += "."
                case .ship: row += "S"
                case .miss: row += "O"
                case .hit: row += "X"
                case .sunk: row += "#"
                }
                row += " "
            }
            print(row)
        }
        print("")
    }
} 