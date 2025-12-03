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
import AmplitudeSessionReplay
import Experiment

class AnalyticsManager {
    static let shared = AnalyticsManager()
    
    private var amplitude: Amplitude
    private var amplitudeEngagement: AmplitudeEngagement
    private var sessionReplayPlugin: SessionReplayPlugin? = nil
    private var experiment: ExperimentClient?
    private var isEngagementReady = false
    
    // MARK: - Game Model Reference for Callbacks
    weak var gameModel: GameModel?
    
    // Configure your Amplitude API key here
    private let amplitudeAPIKey = "909b2239fab57efd4268eb75dbc28d30"
    
    private init() {
        // Initialize AmplitudeEngagement first and store it
        print("🚀 Initializing Amplitude Guides & Surveys SDK...")
        amplitudeEngagement = AmplitudeEngagement(amplitudeAPIKey)
        print("✅ Amplitude Guides & Surveys SDK initialized")
        
        amplitude = Amplitude(configuration: Configuration(
            apiKey: amplitudeAPIKey,
            logLevel: LogLevelEnum.DEBUG,
            autocapture: .frustrationInteractions
        ))
        print("🔌 Adding Guides & Surveys plugin to Amplitude...")
        amplitude.add(plugin: amplitudeEngagement.getPlugin())
        print("✅ Guides & Surveys plugin added to Amplitude")
        
        
        
        // Initialize Session Replay with 100% sample rate
        sessionReplayPlugin = SessionReplayPlugin(
            sampleRate: 1.0,
            maskLevel: .conservative,
            enableRemoteConfig: true,
            webviewMappings: [:],
            autoStart: true,
            captureWebViews: true
        )
        amplitude.add(plugin: sessionReplayPlugin!)
        
        print("📹 Session Replay: Recording 100% of sessions")
        print("Analytics ready for tracking")
        
        let experiment = Experiment.initializeWithAmplitudeAnalytics(
            apiKey: amplitudeAPIKey,
            config: ExperimentConfigBuilder()
                .automaticExposureTracking(false)
                .build()
        )
        
        // Store experiment instance for manual exposure tracking
        self.experiment = experiment
        
    }
    
    // MARK: - Configuration
    
    func configure() {
        print("⚙️ Configuring AnalyticsManager...")
        
        // Set user properties similar to the original JS version
        let identify = Identify()
        identify.set(property: "Game", value: "BattleBoat iOS")
        amplitude.identify(identify: identify)
        print("✅ User identified")
        
        // Boot AmplitudeEngagement with device and user IDs
        // Wait a moment for Amplitude to generate device ID
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            let deviceId = self.amplitude.getDeviceId()
            let userId = self.amplitude.getUserId()
            
            print("🔍 Device ID: \(deviceId ?? "nil"), User ID: \(userId ?? "nil")")
            
            if let deviceId = deviceId {
                // iOS boot() API with AmplitudeBootOptions and integrations
                let bootOptions = AmplitudeBootOptions(
                    user_id: userId ?? "",
                    device_id: deviceId,
                    integrations: [
                        { event, eventProperties in
                            // Forward Guides & Surveys events to Amplitude
                            print("📊 Guides & Surveys event: \(event)")
                            
                            // Create BaseEvent from the forwarded event
                            let baseEvent = BaseEvent(
                                eventType: event,
                                eventProperties: eventProperties
                            )
                            self.amplitude.track(event: baseEvent)
                        }
                    ]
                )
                self.amplitudeEngagement.boot(options: bootOptions)
                print("✅ AmplitudeEngagement booted with Device ID: \(deviceId)")
                print("✅ Event forwarding enabled for Guides & Surveys")
            } else {
                print("⚠️ Device ID not available yet, retrying boot...")
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    if let deviceId = self.amplitude.getDeviceId() {
                        let bootOptions = AmplitudeBootOptions(
                            user_id: self.amplitude.getUserId() ?? "",
                            device_id: deviceId,
                            integrations: [
                                { event, eventProperties in
                                    print("📊 Guides & Surveys event: \(event)")
                                    let baseEvent = BaseEvent(
                                        eventType: event,
                                        eventProperties: eventProperties
                                    )
                                    self.amplitude.track(event: baseEvent)
                                }
                            ]
                        )
                        self.amplitudeEngagement.boot(options: bootOptions)
                        print("✅ AmplitudeEngagement booted (retry) with Device ID: \(deviceId)")
                        print("✅ Event forwarding enabled for Guides & Surveys")
                    } else {
                        print("❌ Device ID still not available - Guides may not work")
                    }
                }
            }
        }
        
        // Start experiment client to fetch flag configurations and variants
        startExperimentClient()
        
        // Set up Guides and Surveys callbacks after Amplitude is initialized
        setupGuidesAndSurveysCallbacks()
        
        print("✅ AnalyticsManager configured - Guides & Surveys should now be active")
    
    }
    
    // MARK: - Guides and Surveys Callbacks
    
    private func setupGuidesAndSurveysCallbacks() {
        print("🎯 Setting up Guides and Surveys callbacks...")
        
        // Wait a moment for the engagement plugin to fully initialize
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            do {
                self.amplitudeEngagement.addCallback("place_ship") {
                    print("🎯 Amplitude Guide Callback: Placing carrier ship horizontally at second position")
                    
                    guard let gameModel = self.gameModel else {
                        print("❌ GameModel not available for callback")
                        return
                    }
                    
                    // Check if we're in the ship placement phase
                    guard gameModel.gameState == .placingShips else {
                        print("❌ Not in ship placement phase - current state: \(gameModel.gameState)")
                        return
                    }
                    
                    // Select the carrier ship
                    gameModel.selectShip(type: .carrier)
                    
                    // Set direction to horizontal (should already be default)
                    if gameModel.selectedShipDirection != .horizontal {
                        gameModel.rotateSelectedShip()
                    }
                    
                    // Place the ship at second position (1,1) - horizontal placement
                    let x = 1  // Second column (0-indexed)
                    let y = 1  // Second row (0-indexed)
                    let success = gameModel.placeShip(at: x, y: y)
                    
                    if success {
                        print("✅ Carrier successfully placed horizontally at position (\(x), \(y)) via Amplitude Guide!")
                        
                        // Track the callback action
                        self.trackEvent(name: "Ship Placed", properties: [
                            "Ship": "carrier",
                            "X": x,
                            "Y": y,
                            "Success": true,
                            "Source": "amplitude_guides_surveys"
                        ])
                    } else {
                        print("❌ Failed to place carrier at position (\(x), \(y)) - position might be occupied or invalid")
                    }
                }
                
                self.isEngagementReady = true
                print("✅ Guides and Surveys callbacks registered and ready")
            } catch {
                print("❌ Failed to register Guides and Surveys callbacks: \(error)")
            }
        }
    }
    
    /// Start the experiment client with the same user context as analytics
    private func startExperimentClient() {
        guard let experiment = experiment else {
            print("❌ Experiment client not initialized")
            return
        }
        
        // Start experiment client (this fetches flag configs and variants)
        experiment.start(nil) { error in
            if let error = error {
                print("❌ Failed to start experiment client: \(error)")
            } else {
                print("✅ Experiment client started successfully - flags and variants loaded")
            }
        }
    }
    
    // MARK: - Game Events (matching original JS events exactly)
    
    /// Generic event tracking method
    func trackEvent(name: String, properties: [String: Any] = [:]) {
        let event = BaseEvent(
            eventType: name,
            eventProperties: properties
        )
        amplitude.track(event: event)
    }
    
    /// Track screen views for Guides and Surveys targeting
    func trackScreen(name: String) {
        // If engagement isn't ready yet, retry after a delay
        guard isEngagementReady else {
            print("⏳ Engagement not ready yet, retrying screen tracking for '\(name)'...")
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                self.trackScreen(name: name)
            }
            return
        }
        
        do {
            print("📱 Tracking screen: \(name)...")
            print("🔍 Current Device ID: \(amplitude.getDeviceId() ?? "nil")")
            print("🔍 Current User ID: \(amplitude.getUserId() ?? "nil")")
            amplitudeEngagement.screen(name)
            print("✅ Screen tracked successfully: \(name)")
            print("💡 If you have a guide targeting '\(name)', it should appear now")
            print("💡 Check Amplitude dashboard: Guides & Surveys → your guide → targeting should match '\(name)'")
        } catch {
            print("❌ Failed to track screen '\(name)': \(error)")
        }
    }
    
    
    func trackSelectShip(shipType: GameConstants.ShipType) {
        // Get exposure properties and track exposure (this ensures experiment_key is in the $exposure event)
        let exposureProps = getExposurePropertiesWithTracking(flagKey: "battleboat-test")
        
        // Combine ship selection properties with exposure properties
        var properties: [String: Any] = [
            "Ship": shipType.rawValue
        ]
        
        // Add exposure properties to the event (flag_key and variant)
        properties.merge(exposureProps) { (current, _) in current }
        
        // Track the ship selection event with exposure context
        let event = BaseEvent(
            eventType: "Ship Selected",
            eventProperties: properties
        )
        amplitude.track(event: event)
        
        // Note: Exposure event is already tracked by getExposurePropertiesWithTracking()
    }
    
    func trackPlaceShip(shipType: GameConstants.ShipType, success: Bool, x: Int? = nil, y: Int? = nil) {
        var properties: [String: Any] = [
            "Ship": shipType.rawValue,
            "Success": success
        ]
        
        if let x = x, let y = y {
            properties["X"] = x
            properties["Y"] = y
        }
        
        let event = BaseEvent(
            eventType: "Ship Placed",
            eventProperties: properties
        )
        amplitude.track(event: event)
    }
    
    func trackRotateShip(shipType: GameConstants.ShipType) {
        let event = BaseEvent(
            eventType: "Ship Rotated",
            eventProperties: [
                "Ship": shipType.rawValue,
            ]
        )
        amplitude.track(event: event)
    }
    
    func trackStartGame() {
        amplitude.track(event: BaseEvent(eventType: "Game Started"))
    }
    
    func trackPlayerShoot(x: Int, y: Int, hit: Bool, consecutiveHits: Int = 0) {
        let event = BaseEvent(
            eventType: "Shot Fired",
            eventProperties: [
                "X": x,
                "Y": y,
                "Hit": hit,
                "Consecutive Hits": consecutiveHits,
                "Player": "human"
            ]
        )
        amplitude.track(event: event)
    }
    
    func trackGameEnd(playerWon: Bool, shotsTaken: Int) {
        let event = BaseEvent(
            eventType: "Game Ended",
            eventProperties: [
                "Win": playerWon,
                "Shots Taken": shotsTaken
            ]
        )
        amplitude.track(event: event)
    }
    
    func trackComputerShoot(x: Int, y: Int, hit: Bool) {
        let event = BaseEvent(
            eventType: "Shot Fired",
            eventProperties: [
                "X": x,
                "Y": y,
                "Hit": hit,
                "Player": "computer"
            ]
        )
        amplitude.track(event: event)
    }
    
    // MARK: - Experiment & Exposure Tracking
    
    /// Get a variant for a flag and manually track exposure
    func getVariant(flagKey: String, fallback: Variant? = nil) -> Variant {
        guard let experiment = experiment else {
            print("❌ Experiment client not initialized")
            return fallback ?? Variant("control")
        }
        
        let variant = experiment.variant(flagKey, fallback: fallback)
        
        // Manually track exposure using SDK's built-in method
        experiment.exposure(key: flagKey)
        
        print("📊 Tracked exposure: flag_key=\(flagKey), variant=\(variant.value ?? "control")")
        
        return variant
    }
    
    /// Get a variant without tracking exposure
    func getVariantWithoutExposure(flagKey: String, fallback: Variant? = nil) -> Variant {
        guard let experiment = experiment else {
            print("❌ Experiment client not initialized")
            return fallback ?? Variant("control")
        }
        
        return experiment.variant(flagKey, fallback: fallback)
    }
    
    /// Manually track exposure for a specific flag
    func trackManualExposure(flagKey: String) {
        guard let experiment = experiment else {
            print("❌ Experiment client not initialized")
            return
        }
        
        // Track exposure with correct properties (flag_key, variant, experiment_key)
        experiment.exposure(key: flagKey)
        
        let variant = experiment.variant(flagKey)
        print("📊 Tracked manual exposure: flag_key=\(flagKey), variant=\(variant.value ?? "control")")
    }
    
    /// Get exposure properties for a flag to add to other events
    /// Note: This doesn't include experiment_key as it's only available through SDK's internal exposure tracking
    func getExposureProperties(flagKey: String) -> [String: Any] {
        guard let experiment = experiment else {
            print("❌ Experiment client not initialized")
            return [:]
        }
        
        let variant = experiment.variant(flagKey)
        
        let properties: [String: Any] = [
            "flag_key": flagKey,
            "variant": variant.value ?? "control"
        ]
        
        return properties
    }
    
    /// Get exposure properties WITH experiment_key by triggering exposure tracking
    /// This will track an exposure event and return the properties
    func getExposurePropertiesWithTracking(flagKey: String) -> [String: Any] {
        guard let experiment = experiment else {
            print("❌ Experiment client not initialized")
            return [:]
        }
        
        let variant = experiment.variant(flagKey)
        
        // Track exposure to ensure experiment_key is captured in the exposure event
        experiment.exposure(key: flagKey)
        
        let properties: [String: Any] = [
            "flag_key": flagKey,
            "variant": variant.value ?? "control"
            // experiment_key is only available in the actual $exposure event tracked by the SDK
        ]
        
        return properties
    }
    
    // MARK: - Additional iOS-specific events
    
    func trackTutorialStep(step: GameConstants.TutorialStep) {
        let event = BaseEvent(
            eventType: "Tutorial Step",
            eventProperties: [
                "Step": step.rawValue,
                "Step Name": tutorialStepName(step)
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
    
    func trackRandomShipPlacement() {
        amplitude.track(event: BaseEvent(eventType: "Ships Placed Randomly"))
    }
    
    func trackShowProbabilityHeatmap() {
        amplitude.track(event: BaseEvent(eventType: "Probability Heatmap Shown"))
    }
    
    func trackHideProbabilityHeatmap() {
        amplitude.track(event: BaseEvent(eventType: "Probability Heatmap Hidden"))
    }
    
    func trackResetStatistics() {
        amplitude.track(event: BaseEvent(eventType: "Statistics Reset"))
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
        if sessionReplayPlugin != nil {
            return "✅ Session Replay: Active (100% capture rate)"
        } else {
            return "❌ Session Replay: Not initialized"
        }
    }
    
    // MARK: - Custom Events for Ship Placement Analysis
    
    func trackHumanGridAnalysis(gameStats: GameStats, humanFleet: Fleet) {
        // Track ship placement patterns for AI improvement (similar to original JS)
        var gridData: [String: Any] = [:]
        
        for ship in humanFleet.getShips() {
            if ship.isPlaced {
                let shipKey = ship.type.rawValue
                gridData["\(shipKey) X"] = ship.x
                gridData["\(shipKey) Y"] = ship.y
                gridData["\(shipKey) Direction"] = ship.direction.rawValue
            }
        }
        
        gridData["User ID"] = gameStats.userID
        gridData["Game Performance"] = gameStats.getPerformanceLevel()
        
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
    
    // MARK: - URL Handling for Guides and Surveys Preview
    
    func handleAmplitudeURL(_ url: URL) -> Bool {
        return amplitudeEngagement.handleUrl(url)
    }
} 
