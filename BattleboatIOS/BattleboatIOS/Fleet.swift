//
//  Fleet.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation

class Fleet {
    private var ships: [Ship]
    let player: Int
    let grid: Grid
    
    init(player: Int, grid: Grid) {
        self.player = player
        self.grid = grid
        self.ships = []
        createFleet()
    }
    
    // MARK: - Fleet Creation
    
    private func createFleet() {
        ships.removeAll()
        
        for shipType in GameConstants.ShipType.allCases {
            let ship = Ship(type: shipType, player: player)
            ships.append(ship)
        }
    }
    
    // MARK: - Ship Management
    
    func getShips() -> [Ship] {
        return ships
    }
    
    func getShip(ofType type: GameConstants.ShipType) -> Ship? {
        return ships.first { $0.type == type }
    }
    
    func getShipAt(x: Int, y: Int) -> Ship? {
        return ships.first { ship in
            ship.containsCoordinate(x: x, y: y)
        }
    }
    
    // MARK: - Ship Placement
    
    func placeShip(type: GameConstants.ShipType, x: Int, y: Int, direction: GameConstants.ShipDirection) -> Bool {
        guard let ship = getShip(ofType: type) else { return false }
        guard !ship.isPlaced else { return false }
        
        return ship.placeAt(x: x, y: y, direction: direction, grid: grid)
    }
    
    func canPlaceShip(type: GameConstants.ShipType, x: Int, y: Int, direction: GameConstants.ShipDirection) -> Bool {
        guard let ship = getShip(ofType: type) else { return false }
        guard !ship.isPlaced else { return false }
        
        return ship.canPlaceAt(x: x, y: y, direction: direction, grid: grid)
    }
    
    func placeShipsRandomly() -> Bool {
        // Reset fleet
        resetFleet()
        
        // Try to place each ship randomly
        for ship in ships {
            if !ship.placeRandomly(grid: grid) {
                // If any ship can't be placed, reset and try again
                resetFleet()
                return false
            }
        }
        
        return true
    }
    
    func placeShipsRandomlyWithRetries(maxRetries: Int = 10) -> Bool {
        for _ in 0..<maxRetries {
            if placeShipsRandomly() {
                return true
            }
        }
        return false
    }
    
    // MARK: - Fleet Status
    
    func areAllShipsPlaced() -> Bool {
        return ships.allSatisfy { $0.isPlaced }
    }
    
    func allShipsSunk() -> Bool {
        return ships.allSatisfy { $0.isSunk() }
    }
    
    func getPlacedShipsCount() -> Int {
        return ships.filter { $0.isPlaced }.count
    }
    
    func getTotalShipsCount() -> Int {
        return ships.count
    }
    
    func getSunkShipsCount() -> Int {
        return ships.filter { $0.isSunk() }.count
    }
    
    func getHitShipsCount() -> Int {
        return ships.filter { $0.isHit() }.count
    }
    
    // MARK: - Damage Handling
    
    func processHit(x: Int, y: Int) -> Ship? {
        guard let ship = getShipAt(x: x, y: y) else { return nil }
        
        ship.incrementDamage()
        
        // If ship is sunk, mark all its cells as sunk
        if ship.isSunk() {
            grid.markAsSunk(coordinates: ship.getAllShipCells())
        }
        
        return ship
    }
    
    // MARK: - Fleet Analysis
    
    func getAllShipCoordinates() -> [(x: Int, y: Int)] {
        var allCoords: [(x: Int, y: Int)] = []
        
        for ship in ships {
            if ship.isPlaced {
                allCoords.append(contentsOf: ship.getAllShipCells())
            }
        }
        
        return allCoords
    }
    
    func getUnplacedShips() -> [Ship] {
        return ships.filter { !$0.isPlaced }
    }
    
    func getRemainingShipsToPlace() -> [GameConstants.ShipType] {
        return getUnplacedShips().map { $0.type }
    }
    
    // MARK: - Fleet Reset
    
    func resetFleet() {
        // Clear grid of ships
        for ship in ships {
            if ship.isPlaced {
                for cell in ship.getAllShipCells() {
                    if grid.isShip(x: cell.x, y: cell.y) || grid.isDamagedShip(x: cell.x, y: cell.y) {
                        grid.setCellType(x: cell.x, y: cell.y, type: .empty)
                    }
                }
            }
        }
        
        // Recreate fleet
        createFleet()
    }
    
    // MARK: - Validation
    
    func validateFleetPlacement() -> Bool {
        // Check that all ships are placed
        guard areAllShipsPlaced() else { return false }
        
        // Check that no ships overlap
        var occupiedCells: Set<String> = []
        
        for ship in ships {
            for cell in ship.getAllShipCells() {
                let cellKey = "\(cell.x),\(cell.y)"
                if occupiedCells.contains(cellKey) {
                    return false // Overlap detected
                }
                occupiedCells.insert(cellKey)
            }
        }
        
        return true
    }
    
    // MARK: - Debug
    
    func printFleetStatus() {
        print("Fleet for player \(player):")
        for ship in ships {
            print("  \(ship.description)")
        }
        print("Ships placed: \(getPlacedShipsCount())/\(getTotalShipsCount())")
        print("Ships sunk: \(getSunkShipsCount())/\(getTotalShipsCount())")
        print("")
    }
} 