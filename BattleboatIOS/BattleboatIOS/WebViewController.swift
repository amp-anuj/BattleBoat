//
//  WebViewController.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import UIKit
import WebKit

/// View controller for testing Guides & Surveys in WebView
/// Passes native device ID to WebView for unified user tracking
class WebViewController: UIViewController {
    
    // MARK: - Properties
    
    /// Default URL - localhost for iOS simulator
    static let defaultURL = "http://localhost:5503/index.html"
    
    var urlString: String
    
    /// Native device ID to pass to WebView for user identity linking
    var nativeDeviceId: String?
    
    /// Native user ID to pass to WebView
    var nativeUserId: String?
    
    /// Native session ID to pass to WebView
    var nativeSessionId: Int64?
    
    private var webView: WKWebView!
    private let progressView = UIProgressView(progressViewStyle: .default)
    private let toolbar = UIToolbar()
    private let urlLabel = UILabel()
    private var progressObservation: NSKeyValueObservation?
    
    // MARK: - Initialization
    
    /// Initialize with URL and optional native identity parameters
    /// - Parameters:
    ///   - url: The URL to load in the WebView
    ///   - deviceId: Native Amplitude device ID for user linking
    ///   - userId: Native Amplitude user ID
    ///   - sessionId: Native Amplitude session ID
    init(url: String = WebViewController.defaultURL, deviceId: String? = nil, userId: String? = nil, sessionId: Int64? = nil) {
        self.urlString = url
        self.nativeDeviceId = deviceId
        self.nativeUserId = userId
        self.nativeSessionId = sessionId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        self.urlString = WebViewController.defaultURL
        super.init(coder: coder)
    }
    
    // MARK: - Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupWebView()
        loadURL()
        
        // Track screen view
        AnalyticsManager.shared.trackScreen(name: "WebViewScreen")
        AnalyticsManager.shared.trackEvent(name: "WebView Opened", properties: [
            "url": urlString
        ])
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        progressObservation = nil
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // Setup toolbar
        toolbar.translatesAutoresizingMaskIntoConstraints = false
        toolbar.barTintColor = .systemBackground
        toolbar.isTranslucent = false
        
        // Create toolbar items
        let closeButton = UIBarButtonItem(
            image: UIImage(systemName: "xmark"),
            style: .plain,
            target: self,
            action: #selector(closeButtonTapped)
        )
        closeButton.tintColor = .systemRed
        
        let reloadButton = UIBarButtonItem(
            image: UIImage(systemName: "arrow.clockwise"),
            style: .plain,
            target: self,
            action: #selector(reloadButtonTapped)
        )
        reloadButton.tintColor = .systemBlue
        
        let backButton = UIBarButtonItem(
            image: UIImage(systemName: "chevron.left"),
            style: .plain,
            target: self,
            action: #selector(goBackTapped)
        )
        backButton.tintColor = .systemBlue
        
        let forwardButton = UIBarButtonItem(
            image: UIImage(systemName: "chevron.right"),
            style: .plain,
            target: self,
            action: #selector(goForwardTapped)
        )
        forwardButton.tintColor = .systemBlue
        
        let flexSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
        let fixedSpace = UIBarButtonItem(barButtonSystemItem: .fixedSpace, target: nil, action: nil)
        fixedSpace.width = 16
        
        toolbar.items = [closeButton, fixedSpace, backButton, forwardButton, flexSpace, reloadButton]
        
        // Setup URL label
        urlLabel.translatesAutoresizingMaskIntoConstraints = false
        urlLabel.font = .systemFont(ofSize: 12)
        urlLabel.textColor = .secondaryLabel
        urlLabel.textAlignment = .center
        urlLabel.numberOfLines = 1
        urlLabel.lineBreakMode = .byTruncatingMiddle
        urlLabel.text = urlString
        
        // Setup progress view
        progressView.translatesAutoresizingMaskIntoConstraints = false
        progressView.progressTintColor = .systemBlue
        progressView.trackTintColor = .systemGray5
        
        // Add subviews
        view.addSubview(toolbar)
        view.addSubview(urlLabel)
        view.addSubview(progressView)
        
        // Setup constraints
        NSLayoutConstraint.activate([
            toolbar.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            toolbar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            toolbar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            toolbar.heightAnchor.constraint(equalToConstant: 44),
            
            urlLabel.topAnchor.constraint(equalTo: toolbar.bottomAnchor, constant: 4),
            urlLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            urlLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            
            progressView.topAnchor.constraint(equalTo: urlLabel.bottomAnchor, constant: 4),
            progressView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            progressView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            progressView.heightAnchor.constraint(equalToConstant: 2)
        ])
    }
    
    private func setupWebView() {
        // Configure WKWebView with settings optimized for Guides & Surveys
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        
        // Enable JavaScript
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true
        configuration.defaultWebpagePreferences = preferences
        
        // Create WebView
        webView = WKWebView(frame: .zero, configuration: configuration)
        webView.translatesAutoresizingMaskIntoConstraints = false
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.allowsBackForwardNavigationGestures = true
        webView.scrollView.bounces = true
        
        // Enable WebView debugging (for Safari Web Inspector)
        if #available(iOS 16.4, *) {
            webView.isInspectable = true
        }
        
        view.addSubview(webView)
        
        NSLayoutConstraint.activate([
            webView.topAnchor.constraint(equalTo: progressView.bottomAnchor),
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        // Observe loading progress
        progressObservation = webView.observe(\.estimatedProgress, options: [.new]) { [weak self] webView, _ in
            let progress = Float(webView.estimatedProgress)
            self?.progressView.setProgress(progress, animated: true)
            self?.progressView.isHidden = progress >= 1.0
        }
    }
    
    private func loadURL() {
        // Build URL with native identity parameters for user linking
        var finalURLString = urlString
        var queryParams: [String] = []
        
        // Add device ID parameter for Amplitude user linking
        if let deviceId = nativeDeviceId, !deviceId.isEmpty {
            queryParams.append("amp_device_id=\(deviceId)")
            print("🔗 Passing native device ID to WebView: \(deviceId)")
        }
        
        // Add user ID if available
        if let userId = nativeUserId, !userId.isEmpty {
            queryParams.append("amp_user_id=\(userId.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? userId)")
            print("🔗 Passing native user ID to WebView: \(userId)")
        }
        
        // Add session ID if available
        if let sessionId = nativeSessionId, sessionId > 0 {
            queryParams.append("amp_session_id=\(sessionId)")
            print("🔗 Passing native session ID to WebView: \(sessionId)")
        }
        
        // Append query parameters to URL
        if !queryParams.isEmpty {
            let separator = finalURLString.contains("?") ? "&" : "?"
            finalURLString += separator + queryParams.joined(separator: "&")
        }
        
        guard let url = URL(string: finalURLString) else {
            showError("Invalid URL: \(finalURLString)")
            return
        }
        
        let request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 30)
        webView.load(request)
        
        print("📱 WebView loading URL: \(finalURLString)")
        if nativeDeviceId != nil {
            print("✅ Native identity will be passed to web SDK for unified tracking")
        }
    }
    
    // MARK: - Actions
    
    @objc private func closeButtonTapped() {
        AnalyticsManager.shared.trackEvent(name: "WebView Closed", properties: [:])
        dismiss(animated: true)
    }
    
    @objc private func reloadButtonTapped() {
        AnalyticsManager.shared.trackEvent(name: "WebView Reloaded", properties: [
            "url": webView.url?.absoluteString ?? urlString
        ])
        webView.reload()
    }
    
    @objc private func goBackTapped() {
        if webView.canGoBack {
            webView.goBack()
        }
    }
    
    @objc private func goForwardTapped() {
        if webView.canGoForward {
            webView.goForward()
        }
    }
    
    // MARK: - Helpers
    
    private func showError(_ message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

// MARK: - WKNavigationDelegate

extension WebViewController: WKNavigationDelegate {
    
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        progressView.isHidden = false
        progressView.setProgress(0.1, animated: false)
        print("🌐 WebView started loading...")
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        progressView.isHidden = true
        urlLabel.text = webView.url?.absoluteString ?? urlString
        
        let loadedURL = webView.url?.absoluteString ?? "unknown"
        print("✅ WebView finished loading: \(loadedURL)")
        
        AnalyticsManager.shared.trackEvent(name: "WebView Page Loaded", properties: [
            "url": loadedURL
        ])
    }
    
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        progressView.isHidden = true
        print("❌ WebView navigation failed: \(error.localizedDescription)")
        
        // Don't show error for cancelled requests (e.g., user navigated away)
        if (error as NSError).code != NSURLErrorCancelled {
            showError("Failed to load page: \(error.localizedDescription)")
        }
    }
    
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        progressView.isHidden = true
        print("❌ WebView provisional navigation failed: \(error.localizedDescription)")
        
        // Show helpful error for connection refused (server not running)
        if (error as NSError).code == NSURLErrorCannotConnectToHost {
            showError("Cannot connect to server.\n\nMake sure your local development server is running on port 5503.\n\nRun: npx serve -p 5503")
        } else if (error as NSError).code != NSURLErrorCancelled {
            showError("Failed to load page: \(error.localizedDescription)")
        }
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        // Log navigation actions for debugging
        if let url = navigationAction.request.url {
            print("🔗 WebView navigating to: \(url.absoluteString)")
        }
        decisionHandler(.allow)
    }
}

// MARK: - WKUIDelegate

extension WebViewController: WKUIDelegate {
    
    // Handle JavaScript alerts
    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default) { _ in
            completionHandler()
        })
        present(alert, animated: true)
    }
    
    // Handle JavaScript confirms
    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { _ in
            completionHandler(false)
        })
        alert.addAction(UIAlertAction(title: "OK", style: .default) { _ in
            completionHandler(true)
        })
        present(alert, animated: true)
    }
    
    // Handle JavaScript prompts
    func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String, defaultText: String?, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (String?) -> Void) {
        let alert = UIAlertController(title: nil, message: prompt, preferredStyle: .alert)
        alert.addTextField { textField in
            textField.text = defaultText
        }
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { _ in
            completionHandler(nil)
        })
        alert.addAction(UIAlertAction(title: "OK", style: .default) { _ in
            completionHandler(alert.textFields?.first?.text)
        })
        present(alert, animated: true)
    }
    
    // Handle window.open()
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        // Open links that would open in new window/tab in the same webview
        if navigationAction.targetFrame == nil {
            webView.load(navigationAction.request)
        }
        return nil
    }
}

