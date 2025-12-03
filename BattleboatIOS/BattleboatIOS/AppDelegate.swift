//
//  AppDelegate.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        
        // Initialize analytics
        setupAnalytics()
        
        // Set up global appearance
        setupAppearance()
        
        return true
    }
    
    private func setupAnalytics() {
        AnalyticsManager.shared.configure()
        AnalyticsManager.shared.setDeviceInfo()
        
        #if DEBUG
        AnalyticsManager.shared.enableDebugMode()
        #endif
    }
    
    private func setupAppearance() {
        // Configure navigation bar appearance
        let navigationBarAppearance = UINavigationBarAppearance()
        navigationBarAppearance.configureWithOpaqueBackground()
        navigationBarAppearance.backgroundColor = UIColor.systemBlue
        navigationBarAppearance.titleTextAttributes = [.foregroundColor: UIColor.white]
        navigationBarAppearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
        
        UINavigationBar.appearance().standardAppearance = navigationBarAppearance
        UINavigationBar.appearance().compactAppearance = navigationBarAppearance
        UINavigationBar.appearance().scrollEdgeAppearance = navigationBarAppearance
        
        // Set tint color for navigation items
        UINavigationBar.appearance().tintColor = UIColor.white
        
        // Configure tab bar appearance (if needed in future)
        UITabBar.appearance().backgroundColor = UIColor.systemBackground
        UITabBar.appearance().tintColor = UIColor.systemBlue
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
    
    // MARK: - URL Handling for Amplitude Guides and Surveys Preview
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        // Handle Amplitude Guides and Surveys preview URLs
        if AnalyticsManager.shared.handleAmplitudeURL(url) {
            return true
        }
        
        // Handle other URL schemes here if needed
        return false
    }
} 
