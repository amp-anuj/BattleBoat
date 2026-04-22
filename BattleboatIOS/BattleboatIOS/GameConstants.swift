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
    
    // MARK: - Amplitude Brand Colors
    // Navy #0D1330 | Surface #1D2433 | Blue #1352CC | Blue Light #3986F7
    // Coral #E8410E | Teal #00C2A8 | Lavender #9164FA
    struct Colors {
        // Grid cell colors
        static let waterColor     = UIColor(red: 0.224, green: 0.302, blue: 0.459, alpha: 1.0)  // #394B75 — deep ocean
        static let waterHover     = UIColor(red: 0.220, green: 0.525, blue: 0.969, alpha: 0.35) // blue tint hover
        static let shipColor      = UIColor(red: 0.000, green: 0.761, blue: 0.659, alpha: 1.0)  // #00C2A8 — Amp Teal
        static let shipDarkColor  = UIColor(red: 0.000, green: 0.616, blue: 0.533, alpha: 1.0)  // #009D88 — placed ship
        static let hitColor       = UIColor(red: 0.910, green: 0.255, blue: 0.055, alpha: 1.0)  // #E8410E — Amp Coral
        static let missColor      = UIColor(red: 0.145, green: 0.180, blue: 0.271, alpha: 1.0)  // #252E45 — muted surface
        static let sunkColor      = UIColor(red: 0.102, green: 0.039, blue: 0.020, alpha: 1.0)  // #1A0A05 — near black-red
        static let selectedColor  = UIColor(red: 0.000, green: 0.761, blue: 0.659, alpha: 0.45) // teal preview
        static let gridLineColor  = UIColor(red: 0.114, green: 0.141, blue: 0.196, alpha: 1.0)  // #1D2433 — surface

        // UI chrome colors
        static let navyBackground = UIColor(red: 0.051, green: 0.075, blue: 0.188, alpha: 1.0)  // #0D1330
        static let surfaceDark    = UIColor(red: 0.114, green: 0.141, blue: 0.196, alpha: 1.0)  // #1D2433
        static let surfaceMedium  = UIColor(red: 0.145, green: 0.180, blue: 0.271, alpha: 1.0)  // #252E45
        static let surfaceLight   = UIColor(red: 0.184, green: 0.227, blue: 0.333, alpha: 1.0)  // #2F3A55
        static let ampBlue        = UIColor(red: 0.075, green: 0.322, blue: 0.800, alpha: 1.0)  // #1352CC
        static let ampBlueLt      = UIColor(red: 0.224, green: 0.525, blue: 0.969, alpha: 1.0)  // #3986F7
        static let ampCoral       = UIColor(red: 0.910, green: 0.255, blue: 0.055, alpha: 1.0)  // #E8410E
        static let ampTeal        = UIColor(red: 0.000, green: 0.761, blue: 0.659, alpha: 1.0)  // #00C2A8
        static let ampLavender    = UIColor(red: 0.569, green: 0.392, blue: 0.980, alpha: 1.0)  // #9164FA
        static let textPrimary    = UIColor(red: 0.910, green: 0.929, blue: 0.961, alpha: 1.0)  // #E8EDF5
        static let textSecondary  = UIColor(red: 0.545, green: 0.616, blue: 0.714, alpha: 1.0)  // #8B9DB5
        static let borderColor    = UIColor(red: 0.545, green: 0.616, blue: 0.714, alpha: 0.18)
    }
    
    // MARK: - Inter Font Helpers
    // Fonts are registered via Info.plist UIAppFonts.
    // In Xcode, add the Fonts/ folder to the target under Build Phases > Copy Bundle Resources.
    struct Fonts {
        static func regular(_ size: CGFloat)  -> UIFont { UIFont(name: "Inter-Regular",  size: size) ?? .systemFont(ofSize: size, weight: .regular) }
        static func medium(_ size: CGFloat)   -> UIFont { UIFont(name: "Inter-Medium",   size: size) ?? .systemFont(ofSize: size, weight: .medium) }
        static func semiBold(_ size: CGFloat) -> UIFont { UIFont(name: "Inter-SemiBold", size: size) ?? .systemFont(ofSize: size, weight: .semibold) }
        static func bold(_ size: CGFloat)     -> UIFont { UIFont(name: "Inter-Bold",     size: size) ?? .systemFont(ofSize: size, weight: .bold) }
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