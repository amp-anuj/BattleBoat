//
//  GameModel.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation

protocol GameModelDelegate: AnyObject {
    func gameModel(_ gameModel: GameModel, didUpdateState state: GameConstants.GameState)
    func gameModel(_ gameModel: GameModel, didUpdateStats stats: GameStats)
    func gameModel(_ gameModel: GameModel, didReceiveMessage message: String)
    func gameModel(_ gameModel: GameModel, gameDidEnd playerWon: Bool)
    func gameModel(_ gameModel: GameModel, didUpdateShipPlacement ship: GameConstants.ShipType, isPlaced: Bool)
}

class GameModel: ObservableObject {
    weak var delegate: GameModelDelegate?
    
    // Game components
    private(set) var humanGrid: Grid
    private(set) var computerGrid: Grid
    private(set) var humanFleet: Fleet
    private(set) var computerFleet: Fleet
    private(set) var ai: AI
    private(set) var stats: GameStats
    
    // Game state
    private(set) var gameState: GameConstants.GameState = .placingShips
    private(set) var currentTutorialStep: GameConstants.TutorialStep = .selectShip
    private(set) var showTutorial: Bool = true
    
    // Ship placement state
    private(set) var selectedShipType: GameConstants.ShipType?
    private(set) var selectedShipDirection: GameConstants.ShipDirection = .horizontal
    
    // Fleet configuration
    private let fleetConfiguration: [GameConstants.ShipType: Int] = [
        .carrier: 1,
        .battleship: 1,  
        .destroyer: 1,
        .submarine: 1,
        .patrolboat: 1
    ]
    
    init() {
        // Initialize grids
        humanGrid = Grid(player: GameConstants.humanPlayer)
        computerGrid = Grid(player: GameConstants.computerPlayer)
        
        // Initialize fleets
        humanFleet = Fleet(player: GameConstants.humanPlayer, grid: humanGrid)
        computerFleet = Fleet(player: GameConstants.computerPlayer, grid: computerGrid)
        
        // Initialize AI
        ai = AI(grid: humanGrid, fleet: humanFleet)
        
        // Initialize stats
        stats = GameStats()
        
        // Setup computer ships
        setupComputerFleet()
        
        // Check if tutorial should be shown
        showTutorial = shouldShowTutorial()
        
        // Set initial user properties
        AnalyticsManager.shared.setUserProperties(gameStats: stats)
    }
    
    // MARK: - Game Setup
    
    private func setupComputerFleet() {
        // Place computer ships randomly
        if !computerFleet.placeShipsRandomlyWithRetries() {
            // Fallback: try again with fresh grid
            computerGrid.reset()
            computerFleet = Fleet(player: GameConstants.computerPlayer, grid: computerGrid)
            _ = computerFleet.placeShipsRandomlyWithRetries(maxRetries: 20)
        }
    }
    
    private func shouldShowTutorial() -> Bool {
        return UserDefaults.standard.bool(forKey: "showTutorial") || stats.gamesPlayed < 1
    }
    
    // MARK: - Ship Placement
    
    func selectShip(type: GameConstants.ShipType) {
        guard gameState == .placingShips else { return }
        guard humanFleet.getShip(ofType: type)?.isPlaced == false else { return }
        
        selectedShipType = type
        
        if showTutorial && currentTutorialStep == .selectShip {
            advanceTutorial()
        }
        
        // Track ship selection
        AnalyticsManager.shared.trackSelectShip(shipType: type)
        
        delegate?.gameModel(self, didReceiveMessage: "Selected \(type.displayName). Tap on your grid to place it.")
    }
    
    func rotateSelectedShip() {
        guard let shipType = selectedShipType else { return }
        
        selectedShipDirection = selectedShipDirection == .horizontal ? .vertical : .horizontal
        
        // Track ship rotation
        AnalyticsManager.shared.trackRotateShip(shipType: shipType)
        
        delegate?.gameModel(self, didReceiveMessage: "Ship rotated to \(selectedShipDirection == .horizontal ? "horizontal" : "vertical") orientation.")
    }
    
    func placeShip(at x: Int, y: Int) -> Bool {
        guard let shipType = selectedShipType else { return false }
        guard gameState == .placingShips else { return false }
        
        let success = humanFleet.placeShip(type: shipType, x: x, y: y, direction: selectedShipDirection)
        
        // Track ship placement attempt
        AnalyticsManager.shared.trackPlaceShip(shipType: shipType, success: success, x: x, y: y)
        
        if success {
            delegate?.gameModel(self, didUpdateShipPlacement: shipType, isPlaced: true)
            delegate?.gameModel(self, didReceiveMessage: "\(shipType.displayName) placed successfully!")
            
            selectedShipType = nil
            
            if showTutorial && currentTutorialStep == .placeShip {
                advanceTutorial()
            }
            
            // Check if all ships are placed
            if humanFleet.areAllShipsPlaced() {
                transitionToReady()
            }
        } else {
            delegate?.gameModel(self, didReceiveMessage: "Cannot place \(shipType.displayName) here. Try a different location.")
        }
        
        return success
    }
    
    func canPlaceShip(type: GameConstants.ShipType, at x: Int, y: Int, direction: GameConstants.ShipDirection) -> Bool {
        return humanFleet.canPlaceShip(type: type, x: x, y: y, direction: direction)
    }
    
    func placeShipsRandomly() {
        guard gameState == .placingShips else { return }
        
        // Track random placement
        AnalyticsManager.shared.trackRandomPlacement()
        
        if humanFleet.placeShipsRandomlyWithRetries() {
            for shipType in GameConstants.ShipType.allCases {
                delegate?.gameModel(self, didUpdateShipPlacement: shipType, isPlaced: true)
            }
            
            delegate?.gameModel(self, didReceiveMessage: "Ships placed randomly!")
            transitionToReady()
        } else {
            delegate?.gameModel(self, didReceiveMessage: "Failed to place ships randomly. Please place them manually.")
        }
    }
    
    private func transitionToReady() {
        gameState = .readyToPlay
        delegate?.gameModel(self, didUpdateState: gameState)
        
        if showTutorial && currentTutorialStep == .placeShip {
            advanceTutorial()
        }
    }
    
    // MARK: - Game Play
    
    func startGame() {
        guard gameState == .readyToPlay else { return }
        guard humanFleet.areAllShipsPlaced() else { return }
        
        gameState = .playerTurn
        delegate?.gameModel(self, didUpdateState: gameState)
        delegate?.gameModel(self, didReceiveMessage: "Game started! Attack the enemy fleet.")
        
        // Track game start
        AnalyticsManager.shared.trackStartGame()
        
        if showTutorial && currentTutorialStep == .startGame {
            advanceTutorial()
        }
    }
    
    func playerShoot(at x: Int, y: Int) -> Bool {
        guard gameState == .playerTurn else { return false }
        // Allow shooting at cells that haven't been shot at yet (empty or ship)
        let cellType = computerGrid.getCellType(x: x, y: y)
        guard cellType == .empty || cellType == .ship else { return false }
        
        stats.incrementShots()
        
        let result = computerGrid.shoot(x: x, y: y)
        var sunkShip: Ship? = nil
        let isHit = result == .hit
        
        // Track shooting
        AnalyticsManager.shared.trackShootShip(x: x, y: y, hit: isHit, consecutiveHits: stats.consecutiveHits)
        
        switch result {
        case .hit:
            stats.recordHit()
            sunkShip = computerFleet.processHit(x: x, y: y)
            
            if let ship = sunkShip, ship.isSunk() {
                delegate?.gameModel(self, didReceiveMessage: "You sunk the enemy's \(ship.type.displayName)!")
                
                // Check for game end
                if computerFleet.allShipsSunk() {
                    endGame(playerWon: true)
                    return true
                }
            } else {
                delegate?.gameModel(self, didReceiveMessage: "Hit!")
            }
            
        case .miss:
            stats.recordMiss()
            delegate?.gameModel(self, didReceiveMessage: "Miss!")
            
        default:
            return false
        }
        
        delegate?.gameModel(self, didUpdateStats: stats)
        
        if showTutorial && currentTutorialStep == .shootEnemy {
            advanceTutorial()
        }
        
        // Computer's turn
        if gameState != .gameOver {
            gameState = .computerTurn
            delegate?.gameModel(self, didUpdateState: gameState)
            
            // Delay computer move for better UX
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                self.computerTurn()
            }
        }
        
        return true
    }
    
    private func computerTurn() {
        guard gameState == .computerTurn else { return }
        
        let (x, y) = ai.makeMove()
        let result = humanGrid.shoot(x: x, y: y)
        var sunkShip: Ship? = nil
        
        switch result {
        case .hit:
            sunkShip = humanFleet.processHit(x: x, y: y)
            
            if let ship = sunkShip, ship.isSunk() {
                delegate?.gameModel(self, didReceiveMessage: "Computer sunk your \(ship.type.displayName)!")
                
                // Check for game end
                if humanFleet.allShipsSunk() {
                    endGame(playerWon: false)
                    return
                }
            } else {
                delegate?.gameModel(self, didReceiveMessage: "Computer hit your ship!")
            }
            
        case .miss:
            delegate?.gameModel(self, didReceiveMessage: "Computer missed!")
            
        default:
            break
        }
        
        // Update AI with result
        ai.processResult(x: x, y: y, result: result, sunkShip: sunkShip)
        
        // Back to player's turn
        gameState = .playerTurn
        delegate?.gameModel(self, didUpdateState: gameState)
    }
    
    // MARK: - Game End
    
    private func endGame(playerWon: Bool) {
        gameState = .gameOver
        delegate?.gameModel(self, didUpdateState: gameState)
        
        // Track game over
        AnalyticsManager.shared.trackGameOver(win: playerWon, shotsTaken: stats.shotsTaken)
        
        if playerWon {
            stats.wonGame()
            delegate?.gameModel(self, didReceiveMessage: "Congratulations! You defeated the computer!")
        } else {
            stats.lostGame()
            delegate?.gameModel(self, didReceiveMessage: "Game Over! The computer sunk all your ships.")
        }
        
        // Update user properties with latest stats
        AnalyticsManager.shared.setUserProperties(gameStats: stats)
        
        // Track human grid analysis for AI improvement
        AnalyticsManager.shared.trackHumanGridAnalysis(gameStats: stats, humanFleet: humanFleet)
        
        delegate?.gameModel(self, didUpdateStats: stats)
        delegate?.gameModel(self, gameDidEnd: playerWon)
        
        if showTutorial {
            completeTutorial()
        }
    }
    
    func restartGame() {
        // Reset grids
        humanGrid.reset()
        computerGrid.reset()
        
        // Reset fleets
        humanFleet = Fleet(player: GameConstants.humanPlayer, grid: humanGrid)
        computerFleet = Fleet(player: GameConstants.computerPlayer, grid: computerGrid)
        
        // Reset AI
        ai = AI(grid: humanGrid, fleet: humanFleet)
        
        // Reset game state
        gameState = .placingShips
        selectedShipType = nil
        selectedShipDirection = .horizontal
        
        // Reset stats for new game
        stats.resetCurrentGame()
        
        // Setup computer ships
        setupComputerFleet()
        
        // Notify delegate
        delegate?.gameModel(self, didUpdateState: gameState)
        delegate?.gameModel(self, didUpdateStats: stats)
        delegate?.gameModel(self, didReceiveMessage: "New game started! Place your ships.")
        
        // Reset ship placement notifications
        for shipType in GameConstants.ShipType.allCases {
            delegate?.gameModel(self, didUpdateShipPlacement: shipType, isPlaced: false)
        }
    }
    
    // MARK: - Tutorial
    
    private func advanceTutorial() {
        guard showTutorial else { return }
        
        // Track current tutorial step before advancing
        AnalyticsManager.shared.trackTutorialStep(step: currentTutorialStep)
        
        switch currentTutorialStep {
        case .selectShip:
            currentTutorialStep = .placeShip
        case .placeShip:
            currentTutorialStep = .startGame
        case .startGame:
            currentTutorialStep = .shootEnemy
        case .shootEnemy:
            currentTutorialStep = .complete
        case .complete:
            break
        }
    }
    
    private func completeTutorial() {
        showTutorial = false
        currentTutorialStep = .complete
        UserDefaults.standard.set(false, forKey: "showTutorial")
        
        // Track tutorial completion
        AnalyticsManager.shared.trackTutorialCompleted()
    }
    
    func skipTutorial() {
        // Track tutorial skip
        AnalyticsManager.shared.trackTutorialSkipped()
        completeTutorial()
    }
    
    // MARK: - Statistics
    
    func resetAllStats() {
        // Track stats reset
        AnalyticsManager.shared.trackStatsReset()
        
        stats.resetAllStats()
        delegate?.gameModel(self, didUpdateStats: stats)
        delegate?.gameModel(self, didReceiveMessage: "All statistics have been reset.")
        
        // Update user properties after reset
        AnalyticsManager.shared.setUserProperties(gameStats: stats)
    }
    
    // MARK: - Debug Features
    
    func enableDebugMode() {
        // Enable debug features like probability heatmap
        UserDefaults.standard.set(true, forKey: "debugMode")
    }
    
    func getAIProbabilities() -> [[Double]] {
        var probabilities: [[Double]] = Array(repeating: Array(repeating: 0.0, count: GameConstants.gridSize), count: GameConstants.gridSize)
        
        for x in 0..<GameConstants.gridSize {
            for y in 0..<GameConstants.gridSize {
                probabilities[x][y] = ai.getProbabilityAt(x: x, y: y)
            }
        }
        
        return probabilities
    }
    
    // MARK: - Game State Queries
    
    func canSelectShip(_ type: GameConstants.ShipType) -> Bool {
        guard gameState == .placingShips else { return false }
        return humanFleet.getShip(ofType: type)?.isPlaced == false
    }
    
    func isShipPlaced(_ type: GameConstants.ShipType) -> Bool {
        return humanFleet.getShip(ofType: type)?.isPlaced == true
    }
    
    func getUnplacedShips() -> [GameConstants.ShipType] {
        return humanFleet.getRemainingShipsToPlace()
    }
    
    func getCurrentTutorialMessage() -> String? {
        guard showTutorial else { return nil }
        
        switch currentTutorialStep {
        case .selectShip:
            return "Select a ship from the list to place it on your grid."
        case .placeShip:
            return "Tap on your grid to place the selected ship. Use the rotate button to change orientation."
        case .startGame:
            return "All ships placed! Tap 'Start Game' to begin the battle."
        case .shootEnemy:
            return "Tap on the enemy grid to attack. Try to find and sink all enemy ships!"
        case .complete:
            return nil
        }
    }
} 