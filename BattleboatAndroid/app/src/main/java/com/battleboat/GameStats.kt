package com.battleboat

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages game statistics with persistent storage
 */
class GameStats private constructor(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "battleboat_stats"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_GAMES_WON = "games_won"
        private const val KEY_TOTAL_SHOTS = "total_shots"
        private const val KEY_TOTAL_HITS = "total_hits"
        private const val KEY_SHIPS_SUNK = "ships_sunk"
        private const val KEY_CURRENT_WIN_STREAK = "current_win_streak"
        private const val KEY_LONGEST_WIN_STREAK = "longest_win_streak"
        private const val KEY_TOTAL_GAME_TIME = "total_game_time"
        private const val KEY_FASTEST_WIN = "fastest_win"
        private const val KEY_FIRST_GAME_DATE = "first_game_date"
        private const val KEY_LAST_GAME_DATE = "last_game_date"
        
        @Volatile
        private var INSTANCE: GameStats? = null
        
        fun getInstance(context: Context): GameStats {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameStats(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Current game tracking
    private var currentGameStartTime: Long = 0
    private var currentGameShots: Int = 0
    private var currentGameHits: Int = 0
    
    /**
     * Start tracking a new game
     */
    fun startGame() {
        currentGameStartTime = System.currentTimeMillis()
        currentGameShots = 0
        currentGameHits = 0
        
        // Update first game date if not set
        if (getFirstGameDate() == 0L) {
            prefs.edit().putLong(KEY_FIRST_GAME_DATE, currentGameStartTime).apply()
        }
    }
    
    /**
     * Record a shot (hit or miss)
     */
    fun recordShot(isHit: Boolean) {
        currentGameShots++
        if (isHit) {
            currentGameHits++
        }
    }
    
    /**
     * Record game end with result
     */
    fun endGame(playerWon: Boolean, shipsSunk: Int) {
        val gameTime = if (currentGameStartTime > 0) {
            System.currentTimeMillis() - currentGameStartTime
        } else 0L
        
        val editor = prefs.edit()
        
        // Update basic stats
        editor.putInt(KEY_GAMES_PLAYED, getGamesPlayed() + 1)
        editor.putInt(KEY_TOTAL_SHOTS, getTotalShots() + currentGameShots)
        editor.putInt(KEY_TOTAL_HITS, getTotalHits() + currentGameHits)
        editor.putInt(KEY_SHIPS_SUNK, getShipsSunk() + shipsSunk)
        editor.putLong(KEY_TOTAL_GAME_TIME, getTotalGameTime() + gameTime)
        editor.putLong(KEY_LAST_GAME_DATE, System.currentTimeMillis())
        
        if (playerWon) {
            // Update win stats
            editor.putInt(KEY_GAMES_WON, getGamesWon() + 1)
            
            // Update win streak
            val newWinStreak = getCurrentWinStreak() + 1
            editor.putInt(KEY_CURRENT_WIN_STREAK, newWinStreak)
            
            if (newWinStreak > getLongestWinStreak()) {
                editor.putInt(KEY_LONGEST_WIN_STREAK, newWinStreak)
            }
            
            // Update fastest win
            if (gameTime > 0 && (getFastestWin() == 0L || gameTime < getFastestWin())) {
                editor.putLong(KEY_FASTEST_WIN, gameTime)
            }
        } else {
            // Reset win streak on loss
            editor.putInt(KEY_CURRENT_WIN_STREAK, 0)
        }
        
        editor.apply()
        
        // Reset current game tracking
        currentGameStartTime = 0
        currentGameShots = 0
        currentGameHits = 0
    }
    
    /**
     * Reset all statistics
     */
    fun resetStats() {
        prefs.edit().clear().apply()
        currentGameStartTime = 0
        currentGameShots = 0
        currentGameHits = 0
    }
    
    // Getters for statistics
    fun getGamesPlayed(): Int = prefs.getInt(KEY_GAMES_PLAYED, 0)
    
    fun getGamesWon(): Int = prefs.getInt(KEY_GAMES_WON, 0)
    
    fun getGamesLost(): Int = getGamesPlayed() - getGamesWon()
    
    fun getWinPercentage(): Float {
        val played = getGamesPlayed()
        return if (played > 0) (getGamesWon().toFloat() / played) * 100f else 0f
    }
    
    fun getTotalShots(): Int = prefs.getInt(KEY_TOTAL_SHOTS, 0)
    
    fun getTotalHits(): Int = prefs.getInt(KEY_TOTAL_HITS, 0)
    
    fun getHitPercentage(): Float {
        val shots = getTotalShots()
        return if (shots > 0) (getTotalHits().toFloat() / shots) * 100f else 0f
    }
    
    fun getShipsSunk(): Int = prefs.getInt(KEY_SHIPS_SUNK, 0)
    
    fun getCurrentWinStreak(): Int = prefs.getInt(KEY_CURRENT_WIN_STREAK, 0)
    
    fun getLongestWinStreak(): Int = prefs.getInt(KEY_LONGEST_WIN_STREAK, 0)
    
    fun getTotalGameTime(): Long = prefs.getLong(KEY_TOTAL_GAME_TIME, 0L)
    
    fun getAverageGameTime(): Long {
        val played = getGamesPlayed()
        return if (played > 0) getTotalGameTime() / played else 0L
    }
    
    fun getFastestWin(): Long = prefs.getLong(KEY_FASTEST_WIN, 0L)
    
    fun getFirstGameDate(): Long = prefs.getLong(KEY_FIRST_GAME_DATE, 0L)
    
    fun getLastGameDate(): Long = prefs.getLong(KEY_LAST_GAME_DATE, 0L)
    
    /**
     * Get current game statistics
     */
    fun getCurrentGameStats(): CurrentGameStats {
        val gameTime = if (currentGameStartTime > 0) {
            System.currentTimeMillis() - currentGameStartTime
        } else 0L
        
        return CurrentGameStats(
            shots = currentGameShots,
            hits = currentGameHits,
            gameTime = gameTime,
            hitPercentage = if (currentGameShots > 0) {
                (currentGameHits.toFloat() / currentGameShots) * 100f
            } else 0f
        )
    }
    
    /**
     * Get overall statistics summary
     */
    fun getStatsSummary(): StatsSummary {
        return StatsSummary(
            gamesPlayed = getGamesPlayed(),
            gamesWon = getGamesWon(),
            gamesLost = getGamesLost(),
            winPercentage = getWinPercentage(),
            totalShots = getTotalShots(),
            totalHits = getTotalHits(),
            hitPercentage = getHitPercentage(),
            shipsSunk = getShipsSunk(),
            currentWinStreak = getCurrentWinStreak(),
            longestWinStreak = getLongestWinStreak(),
            totalGameTime = getTotalGameTime(),
            averageGameTime = getAverageGameTime(),
            fastestWin = getFastestWin(),
            firstGameDate = getFirstGameDate(),
            lastGameDate = getLastGameDate()
        )
    }
    
    /**
     * Get achievement status
     */
    fun getAchievements(): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        // First game
        if (getGamesPlayed() >= 1) {
            achievements.add(Achievement("First Battle", "Play your first game", true))
        }
        
        // Win games
        if (getGamesWon() >= 1) {
            achievements.add(Achievement("First Victory", "Win your first game", true))
        }
        if (getGamesWon() >= 10) {
            achievements.add(Achievement("Veteran", "Win 10 games", true))
        }
        if (getGamesWon() >= 50) {
            achievements.add(Achievement("Admiral", "Win 50 games", true))
        }
        
        // Win streaks
        if (getLongestWinStreak() >= 3) {
            achievements.add(Achievement("Hat Trick", "Win 3 games in a row", true))
        }
        if (getLongestWinStreak() >= 10) {
            achievements.add(Achievement("Unstoppable", "Win 10 games in a row", true))
        }
        
        // Accuracy
        if (getGamesPlayed() >= 5 && getHitPercentage() >= 50f) {
            achievements.add(Achievement("Sharpshooter", "Achieve 50%+ hit rate", true))
        }
        if (getGamesPlayed() >= 10 && getHitPercentage() >= 75f) {
            achievements.add(Achievement("Expert Marksman", "Achieve 75%+ hit rate", true))
        }
        
        return achievements
    }
    
    /**
     * Export statistics as a map
     */
    fun exportStats(): Map<String, Any> {
        return mapOf(
            "gamesPlayed" to getGamesPlayed(),
            "gamesWon" to getGamesWon(),
            "winPercentage" to getWinPercentage(),
            "totalShots" to getTotalShots(),
            "totalHits" to getTotalHits(),
            "hitPercentage" to getHitPercentage(),
            "shipsSunk" to getShipsSunk(),
            "currentWinStreak" to getCurrentWinStreak(),
            "longestWinStreak" to getLongestWinStreak(),
            "totalGameTime" to getTotalGameTime(),
            "averageGameTime" to getAverageGameTime(),
            "fastestWin" to getFastestWin(),
            "firstGameDate" to getFirstGameDate(),
            "lastGameDate" to getLastGameDate()
        )
    }
}

/**
 * Current game statistics
 */
data class CurrentGameStats(
    val shots: Int,
    val hits: Int,
    val gameTime: Long,
    val hitPercentage: Float
)

/**
 * Overall statistics summary
 */
data class StatsSummary(
    val gamesPlayed: Int,
    val gamesWon: Int,
    val gamesLost: Int,
    val winPercentage: Float,
    val totalShots: Int,
    val totalHits: Int,
    val hitPercentage: Float,
    val shipsSunk: Int,
    val currentWinStreak: Int,
    val longestWinStreak: Int,
    val totalGameTime: Long,
    val averageGameTime: Long,
    val fastestWin: Long,
    val firstGameDate: Long,
    val lastGameDate: Long
)

/**
 * Achievement data
 */
data class Achievement(
    val title: String,
    val description: String,
    val unlocked: Boolean
) 