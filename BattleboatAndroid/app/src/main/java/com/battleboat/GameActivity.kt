package com.battleboat

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

/**
 * Main game activity where the battleship game is played
 */
class GameActivity : AppCompatActivity() {
    
    private lateinit var gameStats: GameStats
    private lateinit var analyticsManager: AnalyticsManager
    
    // Game components
    private lateinit var playerFleet: Fleet
    private lateinit var aiFleet: Fleet
    private lateinit var playerGrid: Grid
    private lateinit var aiGrid: Grid
    private lateinit var ai: AI
    
    // Game state
    private var gameState = GameState.SETUP
    private var isTutorialMode = false
    private var gameStartTime = 0L
    
    // Ship placement state
    private var currentShipType: ShipType? = null
    private var isHorizontal = true
    private var placingShips = true
    
    // UI components
    private lateinit var statusText: TextView
    private lateinit var actionButton: Button
    private lateinit var playerGridView: GridView
    private lateinit var enemyGridView: GridView
    
    // Stats UI components
    private lateinit var statsWins: TextView
    private lateinit var statsWinPercentage: TextView
    private lateinit var statsAccuracy: TextView
    private lateinit var statsShots: TextView
    private lateinit var statsWinStreak: TextView
    private lateinit var statsShipsSunk: TextView
    private lateinit var resetStatsButton: Button
    private lateinit var backToMenuButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            setContentView(R.layout.activity_game)
            
            // Check if tutorial mode
            isTutorialMode = intent.getBooleanExtra("tutorial_mode", false)
            
                    // Initialize managers
            gameStats = GameStats.getInstance(this)
            analyticsManager = AnalyticsManager.getInstance(this)
            
            // Set this activity reference for Amplitude callbacks
            analyticsManager.gameActivity = this
            
            // Track screen view for Amplitude Guides and Surveys
            analyticsManager.trackScreen("GameScreen")
            
            setupUI()
            initializeGame()
        } catch (e: Exception) {
            e.printStackTrace()
            // Show error and go back to main menu
            Toast.makeText(this, "Error starting game: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun initializeGame() {
        // Initialize game components
        playerFleet = Fleet()
        aiFleet = Fleet()
        playerGrid = Grid()
        aiGrid = Grid()
        ai = AI()
        
        // Setup AI ships
        ai.autoPlaceShips(aiFleet)
        for (ship in aiFleet.getPlacedShips()) {
            aiGrid.placeShip(ship)
        }
        
        // Start statistics tracking
        gameStats.startGame()
        gameStartTime = System.currentTimeMillis()
        
        if (isTutorialMode) {
            showTutorialStep(1)
        } else {
            startShipPlacement()
        }
    }
    
    private fun setupUI() {
        Log.d("GameActivity", "Setting up UI...")
        
        statusText = findViewById(R.id.status_text)
        actionButton = findViewById(R.id.action_button)
        playerGridView = findViewById(R.id.player_grid)
        enemyGridView = findViewById(R.id.enemy_grid)
        
        // Stats UI components
        statsWins = findViewById(R.id.stats_wins)
        statsWinPercentage = findViewById(R.id.stats_win_percentage)
        statsAccuracy = findViewById(R.id.stats_accuracy)
        statsShots = findViewById(R.id.stats_shots)
        statsWinStreak = findViewById(R.id.stats_win_streak)
        statsShipsSunk = findViewById(R.id.stats_ships_sunk)
        resetStatsButton = findViewById(R.id.reset_stats_button)
        backToMenuButton = findViewById(R.id.back_to_menu_button)
        
        Log.d("GameActivity", "UI components found - statusText: ${statusText != null}, actionButton: ${actionButton != null}")
        Log.d("GameActivity", "Grid views found - player: ${playerGridView != null}, enemy: ${enemyGridView != null}")
        
        if (statusText == null || actionButton == null || playerGridView == null || enemyGridView == null) {
            throw RuntimeException("Could not find required UI components")
        }
        
        actionButton.setOnClickListener {
            Log.d("GameActivity", "Action button clicked!")
            handleActionButton()
        }
        
        // Set up enemy grid click listener for shooting
        enemyGridView.onCellClickListener = { row, col ->
            if (gameState == GameState.PLAYER_TURN) {
                Log.d("GameActivity", "Player shooting at $row, $col")
                playerShoot(row, col)
            }
        }
        
        // Set up reset stats button
        resetStatsButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Statistics")
                .setMessage("Are you sure you want to reset all game statistics? This action cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    gameStats.resetStats()
                    updateStatsDisplay()
                    Toast.makeText(this, "Statistics reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        // Set up back to menu button
        backToMenuButton.setOnClickListener {
            analyticsManager.trackEvent("Back to Menu Button Pressed")
            finish() // Close GameActivity and return to MainActivity
        }
        
        Log.d("GameActivity", "UI setup complete, calling updateUI")
        updateUI()
        updateStatsDisplay()
    }
    
    private fun startShipPlacement() {
        gameState = GameState.SETUP
        placingShips = true
        currentShipType = getNextShipToPlace()
        
        // Track ship placement screen
        analyticsManager.trackScreen("ShipPlacementScreen")
        
        updateShipPlacementUI()
        
        // Set up player grid click listener for ship placement
        playerGridView.onCellClickListener = { row, col ->
            if (placingShips && currentShipType != null) {
                placeShipAt(row, col)
            }
        }
        
        // Set up grid views
        playerGridView.setGrid(playerGrid, playerFleet, isPlayerGrid = true, showShips = true)
        enemyGridView.setGrid(aiGrid, aiFleet, isPlayerGrid = false, showShips = false)
    }
    
    private fun updateShipPlacementUI() {
        val currentShip = currentShipType
        if (currentShip != null) {
            statusText.text = "Place your ${currentShip.displayName} (${currentShip.size} cells)"
            actionButton.text = if (isHorizontal) "Rotate ↻" else "Rotate ↻"
            // Track ship selection
            analyticsManager.trackSelectShip(currentShip.name.lowercase())
        } else if (playerFleet.allShipsPlaced()) {
            statusText.text = "All ships placed! Ready to start?"
            actionButton.text = "Start Game"
        } else {
            statusText.text = "Auto place remaining ships"
            actionButton.text = "Auto Place"
        }
    }
    
    private fun getNextShipToPlace(): ShipType? {
        val placedTypes = playerFleet.getPlacedShips().map { it.type }
        return ShipType.values().find { !placedTypes.contains(it) }
    }
    
    private fun placeShipAt(row: Int, col: Int) {
        val shipType = currentShipType ?: return
        val ship = playerFleet.getShip(shipType) ?: return
        
        val orientation = if (isHorizontal) Orientation.HORIZONTAL else Orientation.VERTICAL
        val success = playerFleet.placeShip(ship, row, col, orientation)
        analyticsManager.trackPlaceShip(shipType.name.lowercase(), success, col, row)
        
        if (success) {
            // Update grid
            for (ship in playerFleet.getPlacedShips()) {
                if (ship.type == shipType && !playerGrid.hasShip(ship)) {
                    playerGrid.placeShip(ship)
                    break
                }
            }
            
            // Move to next ship
            currentShipType = getNextShipToPlace()
            updateShipPlacementUI()
            
            // Update grid view
            playerGridView.setGrid(playerGrid, playerFleet, isPlayerGrid = true, showShips = true)
            
            if (currentShipType == null) {
                placingShips = false
                // Switch back to enemy grid for shooting
                enemyGridView.onCellClickListener = { row, col ->
                    if (gameState == GameState.PLAYER_TURN) {
                        playerShoot(row, col)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Cannot place ship here!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startGame() {
        gameState = GameState.PLAYER_TURN
        updateUI()
        
        // Track player turn screen
        analyticsManager.trackScreen("PlayerTurnScreen")
        analyticsManager.trackStartGame()
    }
    
    private fun handleActionButton() {
        when (gameState) {
            GameState.SETUP -> {
                if (playerFleet.allShipsPlaced()) {
                    startGame()
                } else if (placingShips && currentShipType != null) {
                    // Rotate ship
                    isHorizontal = !isHorizontal
                    analyticsManager.trackRotateShip(currentShipType!!.name.lowercase())
                    updateShipPlacementUI()
                } else {
                    // Auto-place remaining ships
                    playerFleet.autoPlaceShips()
                    for (ship in playerFleet.getPlacedShips()) {
                        playerGrid.placeShip(ship)
                    }
                    placingShips = false
                    currentShipType = null
                    updateShipPlacementUI()
                    playerGridView.setGrid(playerGrid, playerFleet, isPlayerGrid = true, showShips = true)
                    
                    // Set up enemy grid for shooting
                    enemyGridView.onCellClickListener = { row, col ->
                        if (gameState == GameState.PLAYER_TURN) {
                            playerShoot(row, col)
                        }
                    }
                }
            }
            GameState.PLAYER_TURN -> {
                // Player shoots by clicking enemy grid - no action button needed
                Toast.makeText(this, "Tap the enemy grid to shoot!", Toast.LENGTH_SHORT).show()
            }
            GameState.AI_TURN -> {
                // AI turn is handled automatically
            }
            GameState.GAME_OVER -> {
                // Restart game
                finish()
            }
        }
    }
    
    private fun playerShoot(row: Int, col: Int) {
        // Check if already shot here
        if (aiGrid.hasBeenShot(row, col)) {
            Toast.makeText(this, "Already shot here!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val result = aiGrid.shoot(row, col)
        val hitShip = if (result == ShotResult.HIT) aiFleet.hitShip(row, col) else null
        
        // Track shot
        gameStats.recordShot(result == ShotResult.HIT)
        analyticsManager.trackPlayerShoot(col, row, result == ShotResult.HIT)
        
        var message = when (result) {
            ShotResult.HIT -> {
                if (hitShip?.isSunk() == true) {
                    aiGrid.markShipAsSunk(hitShip)
                    analyticsManager.trackEvent("Ship Sunk", mapOf("Ship Type" to hitShip.type.name))
                    "Hit and Sunk ${hitShip.displayName}!"
                } else {
                    "Hit!"
                }
            }
            ShotResult.MISS -> "Miss!"
            else -> "Invalid shot"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        
        // Update grids
        enemyGridView.setGrid(aiGrid, aiFleet, isPlayerGrid = false, showShips = false)
        
        // Check win condition
        if (aiFleet.allShipsSunk()) {
            endGame(GameResult.PLAYER_WIN)
            return
        }
        
        // Switch to AI turn
        gameState = GameState.AI_TURN
        analyticsManager.trackScreen("AITurnScreen")
        updateUI()
        
        // AI turn after delay
        actionButton.postDelayed({
            if (gameState == GameState.AI_TURN) {
                handleAITurn()
            }
        }, 1500)
    }
    
    private fun simulatePlayerShot() {
        // For demo purposes, pick a random available target
        val availableTargets = aiGrid.getAvailableTargets()
        if (availableTargets.isEmpty()) {
            endGame(GameResult.DRAW)
            return
        }
        
        val target = availableTargets.random()
        val (row, col) = target
        
        val result = aiGrid.shoot(row, col)
        val hitShip = if (result == ShotResult.HIT) aiFleet.hitShip(row, col) else null
        
        // Track shot
        gameStats.recordShot(result == ShotResult.HIT)
        analyticsManager.trackPlayerShoot(col, row, result == ShotResult.HIT)
        
        var message = when (result) {
            ShotResult.HIT -> {
                if (hitShip?.isSunk() == true) {
                    aiGrid.markShipAsSunk(hitShip)
                    analyticsManager.trackEvent("Ship Sunk", mapOf("Ship Type" to hitShip.type.name))
                    "Hit and Sunk ${hitShip.displayName}!"
                } else {
                    "Hit!"
                }
            }
            ShotResult.MISS -> "Miss!"
            else -> "Invalid shot"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        
        // Check win condition
        if (aiFleet.allShipsSunk()) {
            endGame(GameResult.PLAYER_WIN)
            return
        }
        
        // Switch to AI turn
        gameState = GameState.AI_TURN
        analyticsManager.trackScreen("AITurnScreen")
        updateUI()
        
        // AI turn after delay
        actionButton.postDelayed({
            if (gameState == GameState.AI_TURN) {
                handleAITurn()
            }
        }, 1500)
    }
    
    private fun handleAITurn() {
        val target = ai.getNextTarget(playerGrid, playerFleet)
        if (target == null) {
            endGame(GameResult.DRAW)
            return
        }
        
        val (row, col) = target
        val result = playerGrid.shoot(row, col)
        val hitShip = if (result == ShotResult.HIT) playerFleet.hitShip(row, col) else null
        
        ai.processShotResult(target, result, if (hitShip?.isSunk() == true) hitShip else null)
        
        var message = "AI: "
        message += when (result) {
            ShotResult.HIT -> {
                if (hitShip?.isSunk() == true) {
                    playerGrid.markShipAsSunk(hitShip)
                    "Hit and Sunk your ${hitShip.displayName}!"
                } else {
                    "Hit!"
                }
            }
            ShotResult.MISS -> "Miss!"
            else -> "Invalid shot"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        
        // Update grids
        playerGridView.setGrid(playerGrid, playerFleet, isPlayerGrid = true, showShips = true)
        
        // Check lose condition
        if (playerFleet.allShipsSunk()) {
            endGame(GameResult.AI_WIN)
            return
        }
        
        // Switch back to player turn
        gameState = GameState.PLAYER_TURN
        analyticsManager.trackScreen("PlayerTurnScreen")
        updateUI()
    }
    
    private fun endGame(result: GameResult) {
        gameState = GameState.GAME_OVER
        analyticsManager.trackScreen("GameOverScreen")
        
        val gameTime = System.currentTimeMillis() - gameStartTime
        val playerWon = result == GameResult.PLAYER_WIN
        val currentStats = gameStats.getCurrentGameStats()
        
        // Record game end
        gameStats.endGame(playerWon, if (playerWon) aiFleet.sunkShipCount() else 0)
        analyticsManager.trackGameEnd(playerWon, currentStats.shots)
        
        val message = when (result) {
            GameResult.PLAYER_WIN -> "Victory! You sunk all enemy ships!"
            GameResult.AI_WIN -> "Defeat! All your ships were sunk!"
            GameResult.DRAW -> "Draw!"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("$message\n\nTime: ${formatTime(gameTime)}\nShots: ${currentStats.shots}\nHits: ${currentStats.hits}")
            .setPositiveButton("New Game") { _, _ ->
                recreate()
            }
            .setNegativeButton("Main Menu") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
        
        updateUI()
        updateStatsDisplay()
    }
    
    private fun updateStatsDisplay() {
        try {
            // Check if stats UI components are initialized
            if (!::statsWins.isInitialized) {
                return
            }
            
            val summary = gameStats.getStatsSummary()
            
            // Win stats
            statsWins.text = "${summary.gamesWon} of ${summary.gamesPlayed}"
            statsWinPercentage.text = "${summary.winPercentage.toInt()}%"
            
            // Accuracy stats
            statsAccuracy.text = "${summary.hitPercentage.toInt()}%"
            statsShots.text = "${summary.totalHits} hits / ${summary.totalShots} shots"
            
            // Win streak
            statsWinStreak.text = summary.currentWinStreak.toString()
            
            // Ships sunk
            statsShipsSunk.text = summary.shipsSunk.toString()
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Don't crash the app, just log the error
        }
    }
    
    private fun showTutorialStep(step: Int) {
        analyticsManager.trackEvent("Tutorial Step", mapOf("Step" to step))
        
        val messages = arrayOf(
            "Welcome to Battleboat! This tutorial will teach you the basics.",
            "First, you need to place your 5 ships on the grid.",
            "Tap the enemy grid to fire and try to sink all their ships!",
            "The AI will get smarter as the game progresses. Good luck!"
        )
        
        if (step <= messages.size) {
            AlertDialog.Builder(this)
                .setTitle("Tutorial - Step $step")
                .setMessage(messages[step - 1])
                .setPositiveButton(if (step < messages.size) "Next" else "Start Game") { _, _ ->
                    if (step < messages.size) {
                        showTutorialStep(step + 1)
                    } else {
                        analyticsManager.trackEvent("Tutorial Completed")
                        startShipPlacement()
                    }
                }
                .setNegativeButton("Skip") { _, _ ->
                    analyticsManager.trackEvent("Tutorial Skipped", mapOf("Step" to step))
                    startShipPlacement()
                }
                .show()
        }
    }
    
    private fun updateUI() {
        try {
            // Check if UI components are initialized
            if (!::statusText.isInitialized || !::actionButton.isInitialized) {
                return
            }
            
            val statusMessage = when (gameState) {
                GameState.SETUP -> "Place Your Ships"
                GameState.PLAYER_TURN -> "Your Turn - Tap Enemy Grid to Shoot"
                GameState.AI_TURN -> "Enemy Turn"
                GameState.GAME_OVER -> "Game Over"
            }
            
            statusText.text = statusMessage
            
            val buttonText = when (gameState) {
                GameState.SETUP -> if (playerFleet.allShipsPlaced()) "Start Game" else "Auto Place Ships"
                GameState.PLAYER_TURN -> "Tap Enemy Grid to Shoot"
                GameState.AI_TURN -> "AI Thinking..."
                GameState.GAME_OVER -> "New Game"
            }
            
            actionButton.text = buttonText
            actionButton.isEnabled = gameState != GameState.AI_TURN
        } catch (e: Exception) {
            e.printStackTrace()
            // Don't crash the app, just log the error
        }
    }
    
    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Quit Game")
            .setMessage("Are you sure you want to quit the current game?")
            .setPositiveButton("Yes") { _, _ ->
                analyticsManager.trackEvent("Game Quit")
                @Suppress("DEPRECATION")
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    // MARK: - Public methods for Amplitude Guides and Surveys callbacks
    
    /**
     * Public access to game state for callbacks
     */
    fun getCurrentGameState(): GameState = gameState
    
    /**
     * Public access to player fleet for callbacks
     */
    fun getPlayerFleet(): Fleet = playerFleet
    
    /**
     * Public access to player grid for callbacks
     */
    fun getPlayerGrid(): Grid = playerGrid
    
    /**
     * Public access to current ship type for callbacks
     */
    fun getCurrentShipType(): ShipType? = currentShipType
    
    /**
     * Public method to set current ship type for callbacks
     */
    fun setCurrentShipType(shipType: ShipType?) {
        currentShipType = shipType
    }
    
    /**
     * Public method to refresh UI for callbacks
     */
    fun refreshUI() {
        runOnUiThread {
            playerGridView.setGrid(playerGrid, playerFleet, isPlayerGrid = true, showShips = true)
            updateShipPlacementUI()
        }
    }
    
    /**
     * Move to the next unplaced ship in the sequence
     * Used by callbacks to properly advance the game state
     */
    fun moveToNextUnplacedShip() {
        currentShipType = getNextShipToPlace()
        
        // If all ships are placed, update game state
        if (currentShipType == null && playerFleet.allShipsPlaced()) {
            placingShips = false
            
            // Update UI to show game is ready to start
            runOnUiThread {
                updateShipPlacementUI()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear the activity reference to avoid memory leaks
        analyticsManager.gameActivity = null
    }
} 