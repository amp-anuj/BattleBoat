//
//  AnalyticsManager.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import Foundation
import UIKit
import AmplitudeSwift
import AmplitudeEngagementSwift
import AmplitudeSwiftSessionReplayPlugin

class AnalyticsManager {
    static let shared = AnalyticsManager()
    
    private var amplitude: Amplitude
    //private var amplitudeEngagement: AmplitudeEngagement
    private var sessionReplayPlugin: AmplitudeSwiftSessionReplayPlugin? = nil
    
    // Configure your Amplitude API key here
    private let amplitudeAPIKey = "909b2239fab57efd4268eb75dbc28d30"
    
    private init() {
        let amplitudeEngagement = AmplitudeEngagement(amplitudeAPIKey)
        amplitude = Amplitude(configuration: Configuration(
            apiKey: amplitudeAPIKey,
            logLevel: LogLevelEnum.DEBUG,
            autocapture: [.networkTracking],
            networkTrackingOptions: .init(captureRules:
                                            [.init(hosts: ["*"], statusCodeRange: "400-599")])
        ))
        amplitude.add(plugin: amplitudeEngagement.getPlugin())
        
        // Initialize Session Replay with 100% sample rate
        sessionReplayPlugin = AmplitudeSwiftSessionReplayPlugin(sampleRate: 1.0)
        amplitude.add(plugin: sessionReplayPlugin!)
        
    }
    
    // MARK: - Initialization
    
    func configure() {
        // Set user properties similar to the original JS version
        let identify = Identify()
        identify.set(property: "Game", value: "BattleBoat iOS")
        amplitude.identify(identify: identify)
        
        print("Analytics ready for tracking")
        
    }
    
    // MARK: - Game Events (matching original JS events exactly)
    
    func trackSelectShip(shipType: GameConstants.ShipType) {
        var event = BaseEvent(
            eventType: "Select Ship",
            eventProperties: [
                "ship": shipType.rawValue,
            ]
        )
        amplitude.track(event: event)
        //amplitudeEngagement.forwardEvent(event)
    }
    
    func trackPlaceShip(shipType: GameConstants.ShipType, success: Bool, x: Int? = nil, y: Int? = nil) {
        var properties: [String: Any] = [
            "ship": shipType.rawValue,
            "success": success
        ]
        
        if let x = x, let y = y {
            properties["x"] = x
            properties["y"] = y
        }
        
        let event = BaseEvent(
            eventType: "Place Ship",
            eventProperties: properties
        )
        amplitude.track(event: event)
    }
    
    func trackRotateShip(shipType: GameConstants.ShipType) {
        let event = BaseEvent(
            eventType: "Rotate Ship",
            eventProperties: [
                "ship": shipType.rawValue
            ]
        )
        amplitude.track(event: event)
    }
    
    func trackStartGame() {
        amplitude.track(event: BaseEvent(eventType: "Start Game"))
    }
    
    func trackShootShip(x: Int, y: Int, hit: Bool, consecutiveHits: Int) {
        let event = BaseEvent(
            eventType: "Shoot Ship",
            eventProperties: [
                "x": x,
                "y": y,
                "hit": hit,
                "consecutiveHits": consecutiveHits
            ]
        )
        amplitude.track(event: event)
    }
    
    func trackGameOver(win: Bool, shotsTaken: Int) {
        let event = BaseEvent(
            eventType: "Game Over",
            eventProperties: [
                "win": win,
                "shotsTaken": shotsTaken
            ]
        )
        amplitude.track(event: event)
    }
    
    // MARK: - Additional iOS-specific events
    
    func trackTutorialStep(step: GameConstants.TutorialStep) {
        let event = BaseEvent(
            eventType: "Tutorial Step",
            eventProperties: [
                "step": step.rawValue,
                "stepName": tutorialStepName(step)
            ]
        )
        amplitude.track(event: event)
    }
    
    func trackTutorialSkipped() {
        amplitude.track(event: BaseEvent(eventType: "Tutorial Skipped"))
    }
    
    func trackTutorialCompleted() {
        amplitude.track(event: BaseEvent(eventType: "Tutorial Completed"))
    }
    
    func trackRandomPlacement() {
        amplitude.track(event: BaseEvent(eventType: "Random Ship Placement"))
    }
    
    func trackShowProbabilityHeatmap() {
        amplitude.track(event: BaseEvent(eventType: "Show Probability Heatmap"))
    }
    
    func trackHideProbabilityHeatmap() {
        amplitude.track(event: BaseEvent(eventType: "Hide Probability Heatmap"))
    }
    
    func trackStatsReset() {
        amplitude.track(event: BaseEvent(eventType: "Reset Statistics"))
    }
    
    // MARK: - User Properties
    
    func setUserProperties(gameStats: GameStats) {
        let identify = Identify()
        identify.set(property: "games_played", value: gameStats.gamesPlayed)
        identify.set(property: "games_won", value: gameStats.gamesWon)
        identify.set(property: "win_percentage", value: gameStats.winPercentage)
        identify.set(property: "overall_accuracy", value: gameStats.overallAccuracy)
        identify.set(property: "total_shots", value: gameStats.totalShots)
        identify.set(property: "total_hits", value: gameStats.totalHits)
        identify.set(property: "performance_level", value: gameStats.getPerformanceLevel())
        
        amplitude.identify(identify: identify)
    }
    
    func setDeviceInfo() {
        let identify = Identify()
        identify.set(property: "platform", value: "iOS")
        identify.set(property: "device_model", value: deviceModel())
        identify.set(property: "ios_version", value: iosVersion())
        identify.set(property: "app_version", value: appVersion())
        
        amplitude.identify(identify: identify)
    }
    
    // MARK: - Session Management
    
    // Sessions are handled automatically by Amplitude SDK
    
    // MARK: - Session Replay Management
    
    func getSessionReplayStatus() -> String {
        return "📦 Session Replay: Manual package installation required"
    }
    
    // MARK: - Custom Events for Ship Placement Analysis
    
    func trackHumanGridAnalysis(gameStats: GameStats, humanFleet: Fleet) {
        // Track ship placement patterns for AI improvement (similar to original JS)
        var gridData: [String: Any] = [:]
        
        for ship in humanFleet.getShips() {
            if ship.isPlaced {
                let shipKey = ship.type.rawValue
                gridData["\(shipKey)_x"] = ship.x
                gridData["\(shipKey)_y"] = ship.y
                gridData["\(shipKey)_direction"] = ship.direction.rawValue
            }
        }
        
        gridData["user_id"] = gameStats.userID
        gridData["game_performance"] = gameStats.getPerformanceLevel()
        
        let event = BaseEvent(
            eventType: "Human Grid Analysis",
            eventProperties: gridData
        )
        amplitude.track(event: event)
    }
    
    // MARK: - Helper Methods
    
    private func tutorialStepName(_ step: GameConstants.TutorialStep) -> String {
        switch step {
        case .selectShip: return "Select Ship"
        case .placeShip: return "Place Ship"
        case .startGame: return "Start Game"
        case .shootEnemy: return "Shoot Enemy"
        case .complete: return "Complete"
        }
    }
    
    private func deviceModel() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        return identifier
    }
    
    private func iosVersion() -> String {
        return UIDevice.current.systemVersion
    }
    
    private func appVersion() -> String {
        return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown"
    }
    
    // MARK: - Debug Mode
    
    func enableDebugMode() {
        // In debug mode, you might want to use a different API key or disable tracking
        #if DEBUG
        print("Analytics Debug Mode Enabled")
        // You could disable tracking in debug mode if desired
        // amplitude.configuration.optOut = true
        #endif
    }
    
    // MARK: - Privacy Compliance
    
    func optOut() {
        amplitude.configuration.optOut = true
        amplitude.track(event: BaseEvent(eventType: "Analytics Opt Out"))
    }
    
    func optIn() {
        amplitude.configuration.optOut = false
        amplitude.track(event: BaseEvent(eventType: "Analytics Opt In"))
    }
    
    func isOptedOut() -> Bool {
        return amplitude.configuration.optOut
    }
} 
