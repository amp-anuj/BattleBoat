package com.battleboat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity for testing surveys in WebView
 * Passes native device ID to WebView for unified user tracking
 */
class WebViewActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "WebViewActivity"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_DEVICE_ID = "extra_device_id"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val DEFAULT_URL = "http://10.0.2.2:5503/index.html"
    }
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var urlText: TextView
    private lateinit var analyticsManager: AnalyticsManager
    private var currentUrl: String = DEFAULT_URL
    
    // Native identity parameters for user linking
    private var nativeDeviceId: String? = null
    private var nativeUserId: String? = null
    private var nativeSessionId: Long? = null
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        
        analyticsManager = AnalyticsManager.getInstance(this)
        analyticsManager.trackScreen("WebViewScreen")
        
        // Initialize views
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        urlText = findViewById(R.id.text_url)
        
        // Setup toolbar buttons
        setupToolbar()
        
        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        
        // Set WebView clients
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page loaded: $url")
                progressBar.visibility = android.view.View.GONE
                urlText.text = url ?: "WebView"
                
                analyticsManager.trackEvent("WebView Page Loaded", mapOf(
                    "url" to (url ?: "unknown")
                ))
            }
            
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebView error: $errorCode - $description for $failingUrl")
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = android.view.View.GONE
                } else {
                    progressBar.visibility = android.view.View.VISIBLE
                }
            }
            
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                Log.d(TAG, "WebView Console: ${consoleMessage?.message()} -- Line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                return true
            }
        }
        
        // Enable WebView debugging for Chrome DevTools
        WebView.setWebContentsDebuggingEnabled(true)
        
        // Get URL from intent or use default
        // Note: 10.0.2.2 is the Android emulator's alias for localhost
        currentUrl = intent.getStringExtra(EXTRA_URL) ?: DEFAULT_URL
        
        // Get native identity parameters for user linking
        nativeDeviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
        nativeUserId = intent.getStringExtra(EXTRA_USER_ID)
        nativeSessionId = intent.getLongExtra(EXTRA_SESSION_ID, 0L).takeIf { it > 0 }
        
        Log.d(TAG, "🔗 Native identity for WebView:")
        Log.d(TAG, "   Device ID: $nativeDeviceId")
        Log.d(TAG, "   User ID: $nativeUserId")
        Log.d(TAG, "   Session ID: $nativeSessionId")
        
        // Build URL with native identity parameters
        val finalUrl = buildUrlWithIdentity(currentUrl)
        
        Log.d(TAG, "Loading URL: $finalUrl")
        loadUrl(finalUrl)
        
        analyticsManager.trackEvent("WebView Opened", mapOf(
            "url" to currentUrl
        ))
    }
    
    private fun setupToolbar() {
        val closeButton = findViewById<ImageButton>(R.id.button_close)
        val reloadButton = findViewById<ImageButton>(R.id.button_reload)
        
        closeButton.setOnClickListener {
            analyticsManager.trackEvent("WebView Closed")
            finish()
        }
        
        reloadButton.setOnClickListener {
            analyticsManager.trackEvent("WebView Reloaded", mapOf(
                "url" to (webView.url ?: currentUrl)
            ))
            webView.reload()
        }
    }
    
    private fun loadUrl(url: String) {
        urlText.text = url
        webView.loadUrl(url)
    }
    
    /**
     * Build URL with native identity parameters for Amplitude user linking
     */
    private fun buildUrlWithIdentity(baseUrl: String): String {
        val queryParams = mutableListOf<String>()
        
        // Add device ID parameter for Amplitude user linking
        nativeDeviceId?.let { deviceId ->
            if (deviceId.isNotEmpty()) {
                queryParams.add("amp_device_id=$deviceId")
                Log.d(TAG, "🔗 Passing native device ID to WebView: $deviceId")
            }
        }
        
        // Add user ID if available
        nativeUserId?.let { userId ->
            if (userId.isNotEmpty()) {
                queryParams.add("amp_user_id=${java.net.URLEncoder.encode(userId, "UTF-8")}")
                Log.d(TAG, "🔗 Passing native user ID to WebView: $userId")
            }
        }
        
        // Add session ID if available
        nativeSessionId?.let { sessionId ->
            if (sessionId > 0) {
                queryParams.add("amp_session_id=$sessionId")
                Log.d(TAG, "🔗 Passing native session ID to WebView: $sessionId")
            }
        }
        
        // Return original URL if no parameters to add
        if (queryParams.isEmpty()) {
            return baseUrl
        }
        
        // Append query parameters to URL
        val separator = if (baseUrl.contains("?")) "&" else "?"
        val finalUrl = baseUrl + separator + queryParams.joinToString("&")
        
        Log.d(TAG, "✅ Native identity will be passed to web SDK for unified tracking")
        return finalUrl
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}

