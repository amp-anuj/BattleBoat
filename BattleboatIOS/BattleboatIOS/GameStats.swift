//
//  GameStats.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation

class GameStats {
    // Current game stats
    private(set) var shotsTaken: Int = 0
    private(set) var shotsHit: Int = 0
    
    // Persistent stats
    private(set) var totalShots: Int = 0
    private(set) var totalHits: Int = 0
    private(set) var gamesPlayed: Int = 0
    private(set) var gamesWon: Int = 0
    private(set) var consecutiveHits: Int = 0
    
    // User ID for analytics
    private(set) var userID: String = ""
    
    // UserDefaults keys
    private struct Keys {
        static let totalShots = "totalShots"
        static let totalHits = "totalHits"
        static let gamesPlayed = "gamesPlayed"
        static let gamesWon = "gamesWon"
        static let userID = "userID"
    }
    
    init() {
        self.userID = loadOrCreateUserID()
        loadStats()
    }
    
    // MARK: - Game Actions
    
    func incrementShots() {
        shotsTaken += 1
        consecutiveHits = 0
    }
    
    func recordHit() {
        shotsHit += 1
        consecutiveHits += 1
    }
    
    func recordMiss() {
        consecutiveHits = 0
    }
    
    func wonGame() {
        gamesPlayed += 1
        gamesWon += 1
        syncStats()
    }
    
    func lostGame() {
        gamesPlayed += 1
        syncStats()
    }
    
    // MARK: - Statistics Calculations
    
    var currentAccuracy: Double {
        guard shotsTaken > 0 else { return 0.0 }
        return Double(shotsHit) / Double(shotsTaken) * 100.0
    }
    
    var overallAccuracy: Double {
        guard totalShots > 0 else { return 0.0 }
        return Double(totalHits) / Double(totalShots) * 100.0
    }
    
    var winPercentage: Double {
        guard gamesPlayed > 0 else { return 0.0 }
        return Double(gamesWon) / Double(gamesPlayed) * 100.0
    }
    
    var winLossRecord: String {
        return "\(gamesWon) of \(gamesPlayed)"
    }
    
    // MARK: - Persistence
    
    private func loadStats() {
        let defaults = UserDefaults.standard
        totalShots = defaults.integer(forKey: Keys.totalShots)
        totalHits = defaults.integer(forKey: Keys.totalHits)
        gamesPlayed = defaults.integer(forKey: Keys.gamesPlayed)
        gamesWon = defaults.integer(forKey: Keys.gamesWon)
    }
    
    private func syncStats() {
        // Update totals
        totalShots += shotsTaken
        totalHits += shotsHit
        
        // Save to UserDefaults
        let defaults = UserDefaults.standard
        defaults.set(totalShots, forKey: Keys.totalShots)
        defaults.set(totalHits, forKey: Keys.totalHits)
        defaults.set(gamesPlayed, forKey: Keys.gamesPlayed)
        defaults.set(gamesWon, forKey: Keys.gamesWon)
        defaults.synchronize()
    }
    
    private func loadOrCreateUserID() -> String {
        let defaults = UserDefaults.standard
        
        if let existingID = defaults.string(forKey: Keys.userID) {
            return existingID
        } else {
            let newID = generateUUID()
            defaults.set(newID, forKey: Keys.userID)
            defaults.synchronize()
            return newID
        }
    }
    
    private func generateUUID() -> String {
        return UUID().uuidString
    }
    
    // MARK: - Game Reset
    
    func resetCurrentGame() {
        shotsTaken = 0
        shotsHit = 0
        consecutiveHits = 0
    }
    
    func resetAllStats() {
        // Reset current game
        resetCurrentGame()
        
        // Reset persistent stats
        totalShots = 0
        totalHits = 0
        gamesPlayed = 0
        gamesWon = 0
        
        // Save reset values
        let defaults = UserDefaults.standard
        defaults.set(0, forKey: Keys.totalShots)
        defaults.set(0, forKey: Keys.totalHits)
        defaults.set(0, forKey: Keys.gamesPlayed)
        defaults.set(0, forKey: Keys.gamesWon)
        defaults.synchronize()
    }
    
    // MARK: - Debug Information
    
    func printStats() {
        print("=== Game Statistics ===")
        print("Current Game:")
        print("  Shots taken: \(shotsTaken)")
        print("  Shots hit: \(shotsHit)")
        print("  Current accuracy: \(String(format: "%.1f", currentAccuracy))%")
        print("  Consecutive hits: \(consecutiveHits)")
        print("")
        print("Overall:")
        print("  Games played: \(gamesPlayed)")
        print("  Games won: \(gamesWon)")
        print("  Win percentage: \(String(format: "%.1f", winPercentage))%")
        print("  Total shots: \(totalShots)")
        print("  Total hits: \(totalHits)")
        print("  Overall accuracy: \(String(format: "%.1f", overallAccuracy))%")
        print("  User ID: \(userID)")
        print("=======================")
    }
    
    // MARK: - Analytics Data
    
    func getAnalyticsData() -> [String: Any] {
        return [
            "userID": userID,
            "shotsTaken": shotsTaken,
            "shotsHit": shotsHit,
            "currentAccuracy": currentAccuracy,
            "consecutiveHits": consecutiveHits,
            "gamesPlayed": gamesPlayed,
            "gamesWon": gamesWon,
            "winPercentage": winPercentage,
            "totalShots": totalShots,
            "totalHits": totalHits,
            "overallAccuracy": overallAccuracy
        ]
    }
    
    // MARK: - Game Performance Analysis
    
    func getPerformanceLevel() -> String {
        if gamesPlayed < 3 {
            return "Beginner"
        } else if winPercentage >= 70 {
            return "Expert"
        } else if winPercentage >= 50 {
            return "Advanced"
        } else if winPercentage >= 30 {
            return "Intermediate"
        } else {
            return "Novice"
        }
    }
    
    func shouldShowTip() -> Bool {
        // Show tips for new players or those struggling
        return gamesPlayed < 5 || winPercentage < 30
    }
    
    func getRandomTip() -> String {
        let tips = [
            "Try spacing your shots in a checkerboard pattern to find ships faster.",
            "When you hit a ship, target the adjacent cells to find the rest of it.",
            "Ships can't touch each other, so avoid cells next to sunk ships.",
            "The AI is smart - it learns from your ship placement patterns!",
            "Larger ships are easier to hit but harder to find initially.",
            "Corner cells are statistically less likely to contain ships."
        ]
        
        return tips.randomElement() ?? "Keep practicing to improve your strategy!"
    }
}