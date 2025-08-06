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
        
        // Test analytics integration
        analyticsManager.testAnalytics()
        
        // Track main menu opened event
        analyticsManager.trackEvent("Main Menu Opened")
        
        setupButtons()
    }
    
    private fun setupButtons() {
        val playButton = findViewById<Button>(R.id.button_play)
        val tutorialButton = findViewById<Button>(R.id.button_tutorial)
        val statisticsButton = findViewById<Button>(R.id.button_statistics)
        val settingsButton = findViewById<Button>(R.id.button_settings)
        
        playButton.setOnClickListener {
            analyticsManager.trackEvent("Play Button Pressed")
            startGame()
        }
        
        tutorialButton.setOnClickListener {
            analyticsManager.trackEvent("Tutorial Button Pressed")
            startTutorial()
        }
        
        statisticsButton.setOnClickListener {
            analyticsManager.trackEvent("Statistics Button Pressed")
            showStatistics()
        }
        
        settingsButton.setOnClickListener {
            analyticsManager.trackEvent("Settings Button Pressed")
            showSettings()
        }
        
        // Long press on settings to show analytics debug
        settingsButton.setOnLongClickListener {
            showAnalyticsDebug()
            true
        }
    }
    
    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
    
    private fun startTutorial() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("tutorial_mode", true)
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
    
    override fun onResume() {
        super.onResume()
        analyticsManager.trackEvent("App Foregrounded")
    }
    
    override fun onPause() {
        super.onPause()
        analyticsManager.trackEvent("App Backgrounded")
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle Amplitude Guides and Surveys preview links
        // This will be handled by the AmplitudeEngagement instance
        // when preview links are opened from the Amplitude dashboard
    }
} 