package com.battleboat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Main menu activity for Battleboat game
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var gameStats: GameStats
    private lateinit var analyticsManager: AnalyticsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize managers
        gameStats = GameStats.getInstance(this)
        analyticsManager = AnalyticsManager.getInstance(this)
        
        // Initialize analytics
        analyticsManager.initialize()
        
        // Check if initialization was successful
        if (analyticsManager.isAmplitudeInitialized()) {
            android.util.Log.d("MainActivity", "✅ Analytics initialization successful")
        } else {
            android.util.Log.e("MainActivity", "❌ Analytics initialization failed")
        }
        
        // Test analytics integration
        analyticsManager.testAnalytics()
        
        // Track main menu opened
        analyticsManager.trackEvent("Main Menu Opened")
        
        // Track screen view for Amplitude Guides and Surveys
        analyticsManager.trackScreen("MainMenuScreen")

        // Hide the action bar if it exists
        supportActionBar?.hide()
        
        setupButtons()
    }
    
    private fun setupButtons() {
        val playButton = findViewById<Button>(R.id.button_play)
        val tutorialButton = findViewById<Button>(R.id.button_tutorial)
        val statisticsButton = findViewById<Button>(R.id.button_statistics)
        val settingsButton = findViewById<Button>(R.id.button_settings)
        val webViewButton = findViewById<Button>(R.id.button_webview)
        
        playButton.setOnClickListener {
            navigateToGame()
        }
        
        tutorialButton.setOnClickListener {
            navigateToTutorial()
        }
        
        statisticsButton.setOnClickListener {
            navigateToStatistics()
        }
        
        settingsButton.setOnClickListener {
            navigateToSettings()
        }
        
        webViewButton.setOnClickListener {
            navigateToWebView()
        }
        
        // Long press on settings to show analytics debug
        settingsButton.setOnLongClickListener {
            showAnalyticsDebug()
            true
        }
    }
    
    private fun navigateToGame() {
        analyticsManager.trackEvent("Play Button Pressed")
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToTutorial() {
        analyticsManager.trackEvent("Tutorial Button Pressed")
        // TODO: Implement tutorial
    }

    private fun navigateToStatistics() {
        analyticsManager.trackEvent("Statistics Button Pressed")
        // TODO: Implement statistics
    }

    private fun navigateToSettings() {
        analyticsManager.trackEvent("Settings Button Pressed")
        // TODO: Implement settings
    }
    
    private fun navigateToWebView() {
        analyticsManager.trackEvent("WebView Button Pressed")
        
        // Get native identity parameters for WebView user linking
        val deviceId = analyticsManager.getDeviceId()
        val userId = analyticsManager.getUserId()
        val sessionId = analyticsManager.getSessionId()
        
        android.util.Log.d("MainActivity", "🔗 Opening WebView with native identity:")
        android.util.Log.d("MainActivity", "   Device ID: $deviceId")
        android.util.Log.d("MainActivity", "   User ID: $userId")
        android.util.Log.d("MainActivity", "   Session ID: $sessionId")
        
        val intent = Intent(this, WebViewActivity::class.java)
        // Note: 10.0.2.2 is the Android emulator's alias for host machine's localhost
        intent.putExtra(WebViewActivity.EXTRA_URL, "http://10.0.2.2:5503/index.html")
        
        // Pass native identity for user linking
        deviceId?.let { intent.putExtra(WebViewActivity.EXTRA_DEVICE_ID, it) }
        userId?.let { intent.putExtra(WebViewActivity.EXTRA_USER_ID, it) }
        sessionId?.let { intent.putExtra(WebViewActivity.EXTRA_SESSION_ID, it) }
        
        startActivity(intent)
    }
    
    private fun showStatistics() {
        // For now, we'll show a simple dialog or toast
        // In a full implementation, this would open a statistics activity
        val stats = gameStats.getStatsSummary()
        val message = """
            Games Played: ${stats.gamesPlayed}
            Games Won: ${stats.gamesWon}
            Win Rate: ${String.format("%.1f", stats.winPercentage)}%
            Hit Rate: ${String.format("%.1f", stats.hitPercentage)}%
            Win Streak: ${stats.longestWinStreak}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Statistics")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSettings() {
        // For now, we'll show a simple settings dialog
        // In a full implementation, this would open a settings activity
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setMessage("Settings screen coming soon!\n\n💡 Tip: Long press this button for analytics debug")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showAnalyticsDebug() {
        // Test analytics and show detailed status
        analyticsManager.testAnalytics()
        
        val status = analyticsManager.getAnalyticsStatus()
        
        AlertDialog.Builder(this)
            .setTitle("🔍 Analytics Debug")
            .setMessage(status)
            .setPositiveButton("Run Test") { _, _ ->
                analyticsManager.testAnalytics()
                Toast.makeText(this, "Test events sent! Check Android Studio logs and Amplitude dashboard", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle Amplitude Guides and Surveys preview links
        // This will be handled by the AmplitudeEngagement instance
        // when preview links are opened from the Amplitude dashboard
        analyticsManager.handlePreviewLink(intent)
    }
} 