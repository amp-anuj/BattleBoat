package com.battleboat

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Advanced AI opponent for Battleboat game
 * Implements probability-based targeting with hunt mode
 */
class AI {
    private val probabilityGrid = Array(GameConstants.GRID_SIZE) { 
        Array(GameConstants.GRID_SIZE) { 0 } 
    }
    
    private val targetStack = mutableListOf<Pair<Int, Int>>()
    private var huntMode = false
    private var lastHit: Pair<Int, Int>? = null
    private var huntDirection: Pair<Int, Int>? = null
    private var huntOrigin: Pair<Int, Int>? = null
    
    private var difficulty = AIDifficulty.MEDIUM
    private var shotCount = 0
    private var hitCount = 0
    
    /**
     * Set AI difficulty level
     */
    fun setDifficulty(difficulty: AIDifficulty) {
        this.difficulty = difficulty
    }
    
    /**
     * Reset AI state for new game
     */
    fun reset() {
        clearProbabilityGrid()
        targetStack.clear()
        huntMode = false
        lastHit = null
        huntDirection = null
        huntOrigin = null
        shotCount = 0
        hitCount = 0
    }
    
    /**
     * Get the next target position for the AI
     */
    fun getNextTarget(playerGrid: Grid, aiFleet: Fleet): Pair<Int, Int>? {
        shotCount++
        
        // Update probability grid based on current state
        updateProbabilityGrid(playerGrid, aiFleet)
        
        // If in hunt mode, continue hunting
        if (huntMode && targetStack.isNotEmpty()) {
            return getNextHuntTarget()
        }
        
        // Get best target based on probability
        return getBestTarget(playerGrid)
    }
    
    /**
     * Process the result of a shot
     */
    fun processShotResult(target: Pair<Int, Int>, result: ShotResult, sunkShip: Ship?) {
        val (row, col) = target
        
        when (result) {
            ShotResult.HIT -> {
                hitCount++
                onHit(row, col)
                
                // If ship was sunk, exit hunt mode
                if (sunkShip != null) {
                    onShipSunk(sunkShip)
                }
            }
            ShotResult.MISS -> {
                onMiss(row, col)
            }
            else -> {}
        }
    }
    
    /**
     * Handle successful hit
     */
    private fun onHit(row: Int, col: Int) {
        if (!huntMode) {
            // Enter hunt mode
            huntMode = true
            huntOrigin = row to col
            huntDirection = null
            addAdjacentTargets(row, col)
        } else {
            // Continue hunt mode
            lastHit?.let { (lastRow, lastCol) ->
                if (huntDirection == null) {
                    // Determine direction
                    val deltaRow = row - lastRow
                    val deltaCol = col - lastCol
                    huntDirection = deltaRow to deltaCol
                    
                    // Add targets in this direction
                    addDirectionalTargets(row, col, huntDirection!!)
                    
                    // Also add targets in opposite direction from origin
                    huntOrigin?.let { (originRow, originCol) ->
                        val oppositeDelta = -deltaRow to -deltaCol
                        addDirectionalTargets(originRow, originCol, oppositeDelta)
                    }
                } else {
                    // Continue in same direction
                    addDirectionalTargets(row, col, huntDirection!!)
                }
            }
        }
        
        lastHit = row to col
    }
    
    /**
     * Handle missed shot
     */
    private fun onMiss(row: Int, col: Int) {
        // If we were hunting in a direction, try the opposite direction
        if (huntMode && huntDirection != null) {
            huntOrigin?.let { (originRow, originCol) ->
                val (deltaRow, deltaCol) = huntDirection!!
                val oppositeDelta = -deltaRow to -deltaCol
                addDirectionalTargets(originRow, originCol, oppositeDelta)
            }
            huntDirection = null
        }
    }
    
    /**
     * Handle ship sunk
     */
    private fun onShipSunk(ship: Ship) {
        huntMode = false
        targetStack.clear()
        lastHit = null
        huntDirection = null
        huntOrigin = null
        
        // Mark area around sunk ship as low priority
        markSunkShipArea(ship)
    }
    
    /**
     * Add adjacent targets to hunt stack
     */
    private fun addAdjacentTargets(row: Int, col: Int) {
        val adjacentPositions = GameConstants.getAdjacentPositions(row, col)
        for (pos in adjacentPositions) {
            if (!targetStack.contains(pos)) {
                targetStack.add(0, pos) // Add to front for priority
            }
        }
    }
    
    /**
     * Add directional targets to hunt stack
     */
    private fun addDirectionalTargets(row: Int, col: Int, direction: Pair<Int, Int>) {
        val (deltaRow, deltaCol) = direction
        var currentRow = row + deltaRow
        var currentCol = col + deltaCol
        
        // Add up to 4 targets in this direction
        for (i in 0 until 4) {
            if (GameConstants.isValidPosition(currentRow, currentCol)) {
                val target = currentRow to currentCol
                if (!targetStack.contains(target)) {
                    targetStack.add(0, target)
                }
                currentRow += deltaRow
                currentCol += deltaCol
            } else {
                break
            }
        }
    }
    
    /**
     * Get next target from hunt stack
     */
    private fun getNextHuntTarget(): Pair<Int, Int>? {
        while (targetStack.isNotEmpty()) {
            val target = targetStack.removeAt(0)
            // Remove targets that have already been shot
            // This will be checked by the game logic
            return target
        }
        huntMode = false
        return null
    }
    
    /**
     * Get best target based on probability
     */
    private fun getBestTarget(playerGrid: Grid): Pair<Int, Int>? {
        val availableTargets = playerGrid.getAvailableTargets()
        if (availableTargets.isEmpty()) return null
        
        // Apply difficulty-based randomization
        val candidates = when (difficulty) {
            AIDifficulty.EASY -> {
                // Random selection with slight bias toward high probability
                val shuffled = availableTargets.shuffled()
                shuffled.take(min(shuffled.size, 10))
            }
            AIDifficulty.MEDIUM -> {
                // Select from top 30% of targets
                val sorted = availableTargets.sortedByDescending { (row, col) ->
                    probabilityGrid[row][col]
                }
                val topCount = max(1, (sorted.size * 0.3).toInt())
                sorted.take(topCount)
            }
            AIDifficulty.HARD -> {
                // Always select highest probability targets
                val maxProbability = availableTargets.maxOfOrNull { (row, col) ->
                    probabilityGrid[row][col]
                } ?: 0
                
                availableTargets.filter { (row, col) ->
                    probabilityGrid[row][col] == maxProbability
                }
            }
        }
        
        return candidates.randomOrNull()
    }
    
    /**
     * Update probability grid based on current game state
     */
    private fun updateProbabilityGrid(playerGrid: Grid, aiFleet: Fleet) {
        clearProbabilityGrid()
        
        // Get remaining ship sizes
        val remainingShipSizes = getRemainingShipSizes(aiFleet)
        
        // Calculate probability for each cell
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (!playerGrid.hasBeenShot(row, col)) {
                    probabilityGrid[row][col] = calculateCellProbability(
                        row, col, playerGrid, remainingShipSizes
                    )
                }
            }
        }
        
        // Apply hunt mode bonuses
        if (huntMode) {
            applyHuntModeBonuses(playerGrid)
        }
        
        // Apply strategic bonuses
        applyStrategicBonuses(playerGrid)
    }
    
    /**
     * Calculate probability for a specific cell
     */
    private fun calculateCellProbability(
        row: Int, 
        col: Int, 
        playerGrid: Grid, 
        shipSizes: List<Int>
    ): Int {
        var probability = 0
        
        for (shipSize in shipSizes) {
            // Check horizontal placement
            if (canPlaceShipHorizontally(row, col, shipSize, playerGrid)) {
                probability += shipSize
            }
            
            // Check vertical placement
            if (canPlaceShipVertically(row, col, shipSize, playerGrid)) {
                probability += shipSize
            }
        }
        
        return probability
    }
    
    /**
     * Check if a ship can be placed horizontally starting at the given position
     */
    private fun canPlaceShipHorizontally(
        row: Int, 
        col: Int, 
        shipSize: Int, 
        playerGrid: Grid
    ): Boolean {
        if (col + shipSize > GameConstants.GRID_SIZE) return false
        
        for (i in 0 until shipSize) {
            val checkCol = col + i
            if (playerGrid.isMiss(row, checkCol)) {
                return false
            }
        }
        return true
    }
    
    /**
     * Check if a ship can be placed vertically starting at the given position
     */
    private fun canPlaceShipVertically(
        row: Int, 
        col: Int, 
        shipSize: Int, 
        playerGrid: Grid
    ): Boolean {
        if (row + shipSize > GameConstants.GRID_SIZE) return false
        
        for (i in 0 until shipSize) {
            val checkRow = row + i
            if (playerGrid.isMiss(checkRow, col)) {
                return false
            }
        }
        return true
    }
    
    /**
     * Get remaining ship sizes that haven't been sunk
     */
    private fun getRemainingShipSizes(aiFleet: Fleet): List<Int> {
        // In a real game, we'd track which ships have been sunk
        // For now, assume all ship sizes are still possible
        return ShipType.allShips().map { it.size }
    }
    
    /**
     * Apply hunt mode bonuses to probability grid
     */
    private fun applyHuntModeBonuses(playerGrid: Grid) {
        // Increase probability for cells adjacent to hits
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (playerGrid.isHit(row, col) && !playerGrid.isSunk(row, col)) {
                    val adjacentPositions = GameConstants.getAdjacentPositions(row, col)
                    for ((adjRow, adjCol) in adjacentPositions) {
                        if (!playerGrid.hasBeenShot(adjRow, adjCol)) {
                            probabilityGrid[adjRow][adjCol] += 50
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Apply strategic bonuses to probability grid
     */
    private fun applyStrategicBonuses(playerGrid: Grid) {
        // Center bias - cells in the center are more likely to be part of ships
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (!playerGrid.hasBeenShot(row, col)) {
                    val centerDistance = kotlin.math.abs(row - 4.5) + kotlin.math.abs(col - 4.5)
                    val centerBonus = max(0, (10 - centerDistance).toInt())
                    probabilityGrid[row][col] += centerBonus
                }
            }
        }
        
        // Checkerboard pattern bonus for early game
        if (shotCount < 20) {
            for (row in 0 until GameConstants.GRID_SIZE) {
                for (col in 0 until GameConstants.GRID_SIZE) {
                    if (!playerGrid.hasBeenShot(row, col) && (row + col) % 2 == 0) {
                        probabilityGrid[row][col] += 5
                    }
                }
            }
        }
    }
    
    /**
     * Mark area around sunk ship as low priority
     */
    private fun markSunkShipArea(ship: Ship) {
        for ((row, col) in ship.getPositions()) {
            val adjacentPositions = GameConstants.getAdjacentPositions(row, col)
            for ((adjRow, adjCol) in adjacentPositions) {
                probabilityGrid[adjRow][adjCol] = max(0, probabilityGrid[adjRow][adjCol] - 25)
            }
        }
    }
    
    /**
     * Clear the probability grid
     */
    private fun clearProbabilityGrid() {
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                probabilityGrid[row][col] = 0
            }
        }
    }
    
    /**
     * Get AI statistics
     */
    fun getStats(): AIStats {
        val accuracy = if (shotCount > 0) hitCount.toFloat() / shotCount else 0f
        return AIStats(
            shotCount = shotCount,
            hitCount = hitCount,
            accuracy = accuracy,
            difficulty = difficulty,
            huntMode = huntMode
        )
    }
    
    /**
     * Get current probability grid (for debugging/visualization)
     */
    fun getProbabilityGrid(): Array<Array<Int>> {
        return probabilityGrid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Auto-place ships for AI with strategic positioning
     */
    fun autoPlaceShips(fleet: Fleet) {
        fleet.clearPlacements()
        val ships = fleet.getAllShips().sortedByDescending { it.size }
        
        for (ship in ships) {
            var attempts = 0
            var placed = false
            
            while (!placed && attempts < 1000) {
                val row = Random.nextInt(GameConstants.GRID_SIZE)
                val col = Random.nextInt(GameConstants.GRID_SIZE)
                val orientation = if (Random.nextBoolean()) Orientation.HORIZONTAL else Orientation.VERTICAL
                
                if (fleet.canPlaceShipAt(ship, row, col, orientation)) {
                    fleet.placeShip(ship, row, col, orientation)
                    placed = true
                }
                attempts++
            }
        }
    }
}

/**
 * AI statistics
 */
data class AIStats(
    val shotCount: Int,
    val hitCount: Int,
    val accuracy: Float,
    val difficulty: AIDifficulty,
    val huntMode: Boolean
) 