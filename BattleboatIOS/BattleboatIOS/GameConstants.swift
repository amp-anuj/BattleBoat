//
//  GameConstants.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation
import UIKit

struct GameConstants {
    // Grid Constants
    static let gridSize = 10
    static let cellSize: CGFloat = 30.0
    static let gridSpacing: CGFloat = 2.0
    
    // Player Constants
    static let humanPlayer = 0
    static let computerPlayer = 1
    static let virtualPlayer = 2
    
    // Cell Types
    enum CellType: Int {
        case empty = 0      // Water (empty)
        case ship = 1       // Undamaged ship
        case miss = 2       // Water with missed shot
        case hit = 3        // Damaged ship (hit)
        case sunk = 4       // Sunk ship
    }
    
    // Ship Types and Sizes
    enum ShipType: String, CaseIterable {
        case carrier = "carrier"
        case battleship = "battleship"
        case destroyer = "destroyer"
        case submarine = "submarine"
        case patrolboat = "patrolboat"
        
        var size: Int {
            switch self {
            case .carrier: return 5
            case .battleship: return 4
            case .destroyer: return 3
            case .submarine: return 3
            case .patrolboat: return 2
            }
        }
        
        var displayName: String {
            switch self {
            case .carrier: return "Aircraft Carrier"
            case .battleship: return "Battleship"
            case .destroyer: return "Destroyer"
            case .submarine: return "Submarine"
            case .patrolboat: return "Patrol Boat"
            }
        }
    }
    
    // Ship Direction
    enum ShipDirection: Int {
        case vertical = 0
        case horizontal = 1
    }
    
    // Colors
    struct Colors {
        static let waterColor = UIColor(red: 0.4, green: 0.7, blue: 0.9, alpha: 1.0)
        static let shipColor = UIColor(red: 0.5, green: 0.5, blue: 0.5, alpha: 1.0)
        static let hitColor = UIColor(red: 0.9, green: 0.3, blue: 0.3, alpha: 1.0)
        static let missColor = UIColor(red: 0.8, green: 0.8, blue: 0.8, alpha: 1.0)
        static let sunkColor = UIColor(red: 0.6, green: 0.2, blue: 0.2, alpha: 1.0)
        static let selectedColor = UIColor(red: 0.2, green: 0.8, blue: 0.2, alpha: 0.5)
        static let gridLineColor = UIColor(red: 0.3, green: 0.3, blue: 0.3, alpha: 1.0)
    }
    
    // Game States
    enum GameState {
        case placingShips
        case readyToPlay
        case playerTurn
        case computerTurn
        case gameOver
    }
    
    // Tutorial Steps
    enum TutorialStep: Int {
        case selectShip = 0
        case placeShip = 1
        case startGame = 2
        case shootEnemy = 3
        case complete = 4
    }
} 