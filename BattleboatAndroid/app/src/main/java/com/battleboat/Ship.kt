package com.battleboat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a ship in the game
 */
@Parcelize
data class Ship(
    val type: ShipType,
    var startRow: Int = -1,
    var startCol: Int = -1,
    var orientation: Orientation = Orientation.HORIZONTAL,
    var isPlaced: Boolean = false,
    private val hitPositions: MutableSet<Pair<Int, Int>> = mutableSetOf()
) : Parcelable {
    
    val size: Int get() = type.size
    val displayName: String get() = type.displayName
    
    /**
     * Get all positions occupied by this ship
     */
    fun getPositions(): List<Pair<Int, Int>> {
        if (!isPlaced) return emptyList()
        
        val positions = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until size) {
            when (orientation) {
                Orientation.HORIZONTAL -> positions.add(startRow to startCol + i)
                Orientation.VERTICAL -> positions.add(startRow + i to startCol)
            }
        }
        return positions
    }
    
    /**
     * Check if the ship can be placed at the given position
     */
    fun canBePlacedAt(row: Int, col: Int, orientation: Orientation): Boolean {
        for (i in 0 until size) {
            val (checkRow, checkCol) = when (orientation) {
                Orientation.HORIZONTAL -> row to col + i
                Orientation.VERTICAL -> row + i to col
            }
            
            if (!GameConstants.isValidPosition(checkRow, checkCol)) {
                return false
            }
        }
        return true
    }
    
    /**
     * Place the ship at the given position
     */
    fun placeAt(row: Int, col: Int, orientation: Orientation): Boolean {
        if (!canBePlacedAt(row, col, orientation)) {
            return false
        }
        
        this.startRow = row
        this.startCol = col
        this.orientation = orientation
        this.isPlaced = true
        return true
    }
    
    /**
     * Remove the ship from the board
     */
    fun removeFromBoard() {
        isPlaced = false
        startRow = -1
        startCol = -1
        hitPositions.clear()
    }
    
    /**
     * Rotate the ship
     */
    fun rotate(): Orientation {
        orientation = orientation.opposite()
        return orientation
    }
    
    /**
     * Check if position is part of this ship
     */
    fun containsPosition(row: Int, col: Int): Boolean {
        return getPositions().contains(row to col)
    }
    
    /**
     * Hit the ship at a specific position
     */
    fun hit(row: Int, col: Int): Boolean {
        if (!containsPosition(row, col)) {
            return false
        }
        
        hitPositions.add(row to col)
        return true
    }
    
    /**
     * Check if the ship is sunk (all positions hit)
     */
    fun isSunk(): Boolean {
        if (!isPlaced) return false
        return hitPositions.size == size
    }
    
    /**
     * Check if a specific position on this ship has been hit
     */
    fun isHitAt(row: Int, col: Int): Boolean {
        return hitPositions.contains(row to col)
    }
    
    /**
     * Get all hit positions on this ship
     */
    fun getHitPositions(): Set<Pair<Int, Int>> {
        return hitPositions.toSet()
    }
    
    /**
     * Get the number of hits on this ship
     */
    fun getHitCount(): Int {
        return hitPositions.size
    }
    
    /**
     * Get the damage percentage (0.0 to 1.0)
     */
    fun getDamagePercentage(): Float {
        if (!isPlaced) return 0f
        return hitPositions.size.toFloat() / size.toFloat()
    }
    
    /**
     * Check if ship overlaps with another ship
     */
    fun overlapsWith(other: Ship): Boolean {
        if (!isPlaced || !other.isPlaced) return false
        
        val myPositions = getPositions().toSet()
        val otherPositions = other.getPositions().toSet()
        
        return myPositions.intersect(otherPositions).isNotEmpty()
    }
    
    /**
     * Check if ship is adjacent to another ship (not allowed in battleship)
     */
    fun isAdjacentTo(other: Ship): Boolean {
        if (!isPlaced || !other.isPlaced) return false
        
        val myPositions = getPositions().toSet()
        val otherPositions = other.getPositions().toSet()
        
        for ((row, col) in myPositions) {
            val adjacentPositions = GameConstants.getAdjacentPositions(row, col)
            for (pos in adjacentPositions) {
                if (otherPositions.contains(pos)) {
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * Get ship bounds (min/max row/col)
     */
    fun getBounds(): ShipBounds? {
        if (!isPlaced) return null
        
        val positions = getPositions()
        val rows = positions.map { it.first }
        val cols = positions.map { it.second }
        
        return ShipBounds(
            minRow = rows.minOrNull() ?: 0,
            maxRow = rows.maxOrNull() ?: 0,
            minCol = cols.minOrNull() ?: 0,
            maxCol = cols.maxOrNull() ?: 0
        )
    }
    
    override fun toString(): String {
        return "$displayName at ($startRow, $startCol) ${orientation.name.lowercase()} - ${getHitCount()}/$size hits"
    }
}

/**
 * Represents the bounds of a ship
 */
data class ShipBounds(
    val minRow: Int,
    val maxRow: Int, 
    val minCol: Int,
    val maxCol: Int
) 