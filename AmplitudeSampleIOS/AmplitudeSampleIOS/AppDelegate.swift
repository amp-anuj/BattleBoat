//
//  AppDelegate.swift
//  AmplitudeSampleIOS
//
//  A minimal sample app demonstrating Amplitude Analytics and Guides & Surveys installation.
//

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Initialize Amplitude Analytics and Guides & Surveys
        AmplitudeManager.shared.initialize()
        
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
    }
    
    // MARK: - URL Handling for Amplitude Guides and Surveys Preview
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        // Handle Amplitude Guides and Surveys preview URLs
        if AmplitudeManager.shared.handleUrl(url) {
            return true
        }
        return false
    }
}

