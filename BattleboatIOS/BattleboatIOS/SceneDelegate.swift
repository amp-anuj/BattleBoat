//
//  SceneDelegate.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
        
        guard let windowScene = (scene as? UIWindowScene) else { return }
        
        // Create the main window
        window = UIWindow(windowScene: windowScene)
        
        // Create and set the root view controller
        let gameViewController = GameViewController()
        let navigationController = UINavigationController(rootViewController: gameViewController)
        
        // Configure navigation controller
        navigationController.navigationBar.prefersLargeTitles = false
        gameViewController.navigationItem.title = "Battleboat"
        
        // Add menu button (if needed)
        setupNavigationItems(for: gameViewController)
        
        window?.rootViewController = navigationController
        window?.makeKeyAndVisible()
    }
    
    private func setupNavigationItems(for viewController: GameViewController) {
        // Add info button to navigation bar
        let infoButton = UIBarButtonItem(
            image: UIImage(systemName: "info.circle"),
            style: .plain,
            target: self,
            action: #selector(showInfo)
        )
        
        // Add settings button
        let settingsButton = UIBarButtonItem(
            image: UIImage(systemName: "gear"),
            style: .plain,
            target: self,
            action: #selector(showSettings)
        )
        
        viewController.navigationItem.rightBarButtonItems = [settingsButton, infoButton]
    }
    
    @objc private func showInfo() {
        guard let topViewController = window?.rootViewController?.topMostViewController() else { return }
        
        let alert = UIAlertController(
            title: "About Battleboat iOS",
            message: """
            A faithful iOS recreation of the Battleboat.js web game.
            
            Challenge an intelligent AI in the classic game of Battleship!
            
            Features:
            • Advanced AI opponent with probability-based targeting
            • Tutorial system for new players
            • Comprehensive statistics tracking
            • Probability heatmap visualization
            • Modern iOS interface
            
            Original web game by Bill Mei
            iOS version by Battleboat Team
            """,
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        topViewController.present(alert, animated: true)
    }
    
    @objc private func showSettings() {
        guard let topViewController = window?.rootViewController?.topMostViewController() else { return }
        
        let alert = UIAlertController(title: "Settings", message: "Game settings and preferences", preferredStyle: .actionSheet)
        
        // Tutorial toggle
        let tutorialText = UserDefaults.standard.bool(forKey: "showTutorial") ? "Disable Tutorial" : "Enable Tutorial"
        alert.addAction(UIAlertAction(title: tutorialText, style: .default) { _ in
            let currentState = UserDefaults.standard.bool(forKey: "showTutorial")
            UserDefaults.standard.set(!currentState, forKey: "showTutorial")
        })
        
        // Debug mode toggle
        let debugText = UserDefaults.standard.bool(forKey: "debugMode") ? "Disable Debug Mode" : "Enable Debug Mode"
        alert.addAction(UIAlertAction(title: debugText, style: .default) { _ in
            let currentState = UserDefaults.standard.bool(forKey: "debugMode")
            UserDefaults.standard.set(!currentState, forKey: "debugMode")
        })
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        
        // For iPad
        if let popover = alert.popoverPresentationController {
            popover.barButtonItem = topViewController.navigationItem.rightBarButtonItems?.first
        }
        
        topViewController.present(alert, animated: true)
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
        // Session tracking handled automatically by Amplitude
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
        // Session tracking handled automatically by Amplitude
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
        // Foreground tracking handled automatically by Amplitude
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
        // Background tracking handled automatically by Amplitude
    }
    
    // MARK: - URL Handling for Amplitude Guides and Surveys Preview
    
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        guard let url = URLContexts.first?.url else { return }
        
        // Handle Amplitude Guides and Surveys preview URLs
        if AnalyticsManager.shared.handleAmplitudeURL(url) {
            return
        }
        
        // Handle other URL schemes here if needed
    }
}

// MARK: - UIViewController Extension

extension UIViewController {
    func topMostViewController() -> UIViewController {
        if let presentedViewController = self.presentedViewController {
            return presentedViewController.topMostViewController()
        }
        
        if let navigationController = self as? UINavigationController {
            return navigationController.visibleViewController?.topMostViewController() ?? self
        }
        
        if let tabBarController = self as? UITabBarController {
            return tabBarController.selectedViewController?.topMostViewController() ?? self
        }
        
        return self
    }
} 