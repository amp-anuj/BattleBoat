package com.battleboat

/**
 * Ship types with their sizes
 */
enum class ShipType(val size: Int, val displayName: String) {
    CARRIER(5, "Carrier"),
    BATTLESHIP(4, "Battleship"), 
    CRUISER(3, "Cruiser"),
    SUBMARINE(3, "Submarine"),
    DESTROYER(2, "Destroyer");
    
    companion object {
        fun allShips() = values().toList()
    }
}

/**
 * Cell types for the game grid
 */
enum class CellType {
    EMPTY,      // Water, no ship
    SHIP,       // Ship part
    HIT,        // Ship part that was hit
    MISS,       // Water that was shot
    SUNK        // Ship part that belongs to a sunk ship
}

/**
 * Ship orientation
 */
enum class Orientation {
    HORIZONTAL,
    VERTICAL;
    
    fun opposite(): Orientation {
        return if (this == HORIZONTAL) VERTICAL else HORIZONTAL
    }
}

/**
 * Game states
 */
enum class GameState {
    SETUP,          // Placing ships
    PLAYER_TURN,    // Player's turn to shoot
    AI_TURN,        // AI's turn to shoot
    GAME_OVER       // Game finished
}

/**
 * Game result
 */
enum class GameResult {
    PLAYER_WIN,
    AI_WIN,
    DRAW
}

/**
 * AI difficulty levels
 */
enum class AIDifficulty {
    EASY,
    MEDIUM, 
    HARD
}

/**
 * Game constants
 */
object GameConstants {
    const val GRID_SIZE = 10
    const val TOTAL_SHIPS = 5
    const val MAX_SHIP_SIZE = 5
    
    // AI constants
    const val AI_TURN_DELAY = 1000L // milliseconds
    const val HUNT_MODE_THRESHOLD = 3
    const val MAX_PROBABILITY = 100
    
    // Animation durations
    const val FADE_DURATION = 300L
    const val SLIDE_DURATION = 500L
    const val EXPLOSION_DURATION = 600L
    
    // Grid bounds
    fun isValidPosition(row: Int, col: Int): Boolean {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE
    }
    
    fun getAdjacentPositions(row: Int, col: Int): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        
        for ((dRow, dCol) in directions) {
            val newRow = row + dRow
            val newCol = col + dCol
            if (isValidPosition(newRow, newCol)) {
                positions.add(newRow to newCol)
            }
        }
        return positions
    }
    
    fun getAllPositions(): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                positions.add(row to col)
            }
        }
        return positions
    }
} 