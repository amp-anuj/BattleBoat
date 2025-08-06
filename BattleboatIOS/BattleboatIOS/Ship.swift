//
//  Ship.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation

class Ship {
    let type: GameConstants.ShipType
    let size: Int
    var direction: GameConstants.ShipDirection
    var x: Int
    var y: Int
    var damage: Int
    var isPlaced: Bool
    var cells: [(x: Int, y: Int)]
    let player: Int
    
    init(type: GameConstants.ShipType, player: Int) {
        self.type = type
        self.size = type.size
        self.direction = .horizontal
        self.x = -1
        self.y = -1
        self.damage = 0
        self.isPlaced = false
        self.cells = []
        self.player = player
    }
    
    // MARK: - Ship Placement
    
    func canPlaceAt(x: Int, y: Int, direction: GameConstants.ShipDirection, grid: Grid) -> Bool {
        let coords = calculateCoordinates(x: x, y: y, direction: direction)
        
        // Check if all coordinates are within bounds
        for coord in coords {
            if coord.x < 0 || coord.x >= GameConstants.gridSize ||
               coord.y < 0 || coord.y >= GameConstants.gridSize {
                return false
            }
            
            // Check if cell is already occupied
            if grid.getCellType(x: coord.x, y: coord.y) != .empty {
                return false
            }
        }
        
        // Check for adjacent ships (ships can't touch)
        for coord in coords {
            for dx in -1...1 {
                for dy in -1...1 {
                    if dx == 0 && dy == 0 { continue }
                    
                    let checkX = coord.x + dx
                    let checkY = coord.y + dy
                    
                    if checkX >= 0 && checkX < GameConstants.gridSize &&
                       checkY >= 0 && checkY < GameConstants.gridSize {
                        if grid.getCellType(x: checkX, y: checkY) == .ship {
                            return false
                        }
                    }
                }
            }
        }
        
        return true
    }
    
    func placeAt(x: Int, y: Int, direction: GameConstants.ShipDirection, grid: Grid) -> Bool {
        if !canPlaceAt(x: x, y: y, direction: direction, grid: grid) {
            return false
        }
        
        self.x = x
        self.y = y
        self.direction = direction
        self.cells = calculateCoordinates(x: x, y: y, direction: direction)
        self.isPlaced = true
        
        // Update grid
        for cell in cells {
            grid.setCellType(x: cell.x, y: cell.y, type: .ship)
        }
        
        return true
    }
    
    private func calculateCoordinates(x: Int, y: Int, direction: GameConstants.ShipDirection) -> [(x: Int, y: Int)] {
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
    
    // MARK: - Ship Status
    
    func incrementDamage() {
        damage += 1
    }
    
    func isSunk() -> Bool {
        return damage >= size
    }
    
    func isHit() -> Bool {
        return damage > 0
    }
    
    func containsCoordinate(x: Int, y: Int) -> Bool {
        return cells.contains { $0.x == x && $0.y == y }
    }
    
    func getAllShipCells() -> [(x: Int, y: Int)] {
        return cells
    }
    
    // MARK: - Random Placement
    
    func placeRandomly(grid: Grid) -> Bool {
        let maxAttempts = 100
        var attempts = 0
        
        while attempts < maxAttempts {
            let randomX = Int.random(in: 0..<GameConstants.gridSize)
            let randomY = Int.random(in: 0..<GameConstants.gridSize)
            let randomDirection: GameConstants.ShipDirection = Bool.random() ? .horizontal : .vertical
            
            if placeAt(x: randomX, y: randomY, direction: randomDirection, grid: grid) {
                return true
            }
            
            attempts += 1
        }
        
        return false
    }
    
    // MARK: - Debug Description
    
    var description: String {
        return "\(type.displayName) (\(size) cells) - Damage: \(damage)/\(size) - Sunk: \(isSunk())"
    }
} 