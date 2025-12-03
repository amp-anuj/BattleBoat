//
//  AmplitudeManager.swift
//  AmplitudeSampleIOS
//
//  A clean, minimal implementation of Amplitude Analytics and Guides & Surveys.
//  This file demonstrates the complete installation process.
//
//  Documentation:
//  - Analytics: https://amplitude.com/docs/sdks/analytics/ios/ios-swift-sdk
//  - Guides & Surveys: https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-ios-sdk
//

import Foundation
import AmplitudeSwift
import AmplitudeEngagementSwift

/// Singleton class managing Amplitude Analytics and Guides & Surveys
class AmplitudeManager {
    
    // MARK: - Singleton
    
    static let shared = AmplitudeManager()
    
    // MARK: - Properties
    
    /// Your Amplitude API Key
    private let apiKey = "909b2239fab57efd4268eb75dbc28d30"
    
    /// Main Amplitude Analytics instance
    private var amplitude: Amplitude!
    
    /// Amplitude Guides & Surveys instance
    private var amplitudeEngagement: AmplitudeEngagement!
    
    // MARK: - Initialization
    
    private init() {}
    
    /// Initialize Amplitude Analytics and Guides & Surveys
    /// Call this method once in AppDelegate.didFinishLaunchingWithOptions
    func initialize() {
        print("🚀 Initializing Amplitude...")
        
        // Step 1: Initialize Amplitude Guides & Surveys
        amplitudeEngagement = AmplitudeEngagement(apiKey)
        print("✅ Amplitude Guides & Surveys SDK initialized")
        
        // Step 2: Initialize Amplitude Analytics
        amplitude = Amplitude(configuration: Configuration(
            apiKey: apiKey,
            logLevel: LogLevelEnum.DEBUG
        ))
        print("✅ Amplitude Analytics SDK initialized")
        
        // Step 3: Add Guides & Surveys plugin to Amplitude
        amplitude.add(plugin: amplitudeEngagement.getPlugin())
        print("✅ Guides & Surveys plugin added to Amplitude")
        
        // Step 4: Boot Guides & Surveys with device ID (required for targeting)
        bootGuidesAndSurveys()
        
        print("🎉 Amplitude initialization complete!")
    }
    
    // MARK: - Boot Guides & Surveys
    
    private func bootGuidesAndSurveys() {
        // Wait for device ID to be generated
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            guard let deviceId = self.amplitude.getDeviceId() else {
                print("⚠️ Device ID not available, retrying...")
                self.retryBoot()
                return
            }
            
            let userId = self.amplitude.getUserId()
            
            // Boot with AmplitudeBootOptions for event forwarding
            let bootOptions = AmplitudeBootOptions(
                user_id: userId ?? "",
                device_id: deviceId,
                integrations: [
                    { event, eventProperties in
                        // Forward Guides & Surveys events to Amplitude
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
            print("✅ Guides & Surveys booted with Device ID: \(deviceId)")
        }
    }
    
    private func retryBoot() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            guard let deviceId = self.amplitude.getDeviceId() else {
                print("❌ Device ID still not available")
                return
            }
            
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
            print("✅ Guides & Surveys booted (retry) with Device ID: \(deviceId)")
        }
    }
    
    // MARK: - Screen Tracking
    
    /// Track screen views for Guides and Surveys targeting
    /// - Parameter name: The screen name (e.g., "HomeScreen", "SettingsScreen")
    func trackScreen(name: String) {
        amplitudeEngagement.screen(name)
        print("📱 Screen tracked: \(name)")
    }
    
    // MARK: - URL Handling
    
    /// Handle Amplitude preview URLs for testing guides and surveys
    /// - Parameter url: The URL to handle
    /// - Returns: true if the URL was handled by Amplitude
    func handleUrl(_ url: URL) -> Bool {
        return amplitudeEngagement.handleUrl(url)
    }
}

