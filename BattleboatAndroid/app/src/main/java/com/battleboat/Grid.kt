package com.battleboat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a game grid (10x10 board)
 */
@Parcelize
data class Grid(
    private val cells: Array<Array<CellType>> = Array(GameConstants.GRID_SIZE) { 
        Array(GameConstants.GRID_SIZE) { CellType.EMPTY } 
    }
) : Parcelable {
    
    /**
     * Get the cell type at a specific position
     */
    fun getCellType(row: Int, col: Int): CellType {
        if (!GameConstants.isValidPosition(row, col)) {
            return CellType.EMPTY
        }
        return cells[row][col]
    }
    
    /**
     * Set the cell type at a specific position
     */
    fun setCellType(row: Int, col: Int, cellType: CellType) {
        if (GameConstants.isValidPosition(row, col)) {
            cells[row][col] = cellType
        }
    }
    
    /**
     * Check if a position has been shot at
     */
    fun hasBeenShot(row: Int, col: Int): Boolean {
        val cellType = getCellType(row, col)
        return cellType == CellType.HIT || cellType == CellType.MISS || cellType == CellType.SUNK
    }
    
    /**
     * Check if a ship is already placed on the grid
     */
    fun hasShip(ship: Ship): Boolean {
        for (position in ship.getPositions()) {
            if (getCellType(position.first, position.second) == CellType.SHIP) {
                return true
            }
        }
        return false
    }
    
    /**
     * Check if a position contains a ship
     */
    fun hasShip(row: Int, col: Int): Boolean {
        val cellType = getCellType(row, col)
        return cellType == CellType.SHIP || cellType == CellType.HIT || cellType == CellType.SUNK
    }
    
    /**
     * Check if a position is empty water
     */
    fun isEmpty(row: Int, col: Int): Boolean {
        return getCellType(row, col) == CellType.EMPTY
    }
    
    /**
     * Check if a position was hit
     */
    fun isHit(row: Int, col: Int): Boolean {
        val cellType = getCellType(row, col)
        return cellType == CellType.HIT || cellType == CellType.SUNK
    }
    
    /**
     * Check if a position was missed
     */
    fun isMiss(row: Int, col: Int): Boolean {
        return getCellType(row, col) == CellType.MISS
    }
    
    /**
     * Check if a position contains a sunk ship part
     */
    fun isSunk(row: Int, col: Int): Boolean {
        return getCellType(row, col) == CellType.SUNK
    }
    
    /**
     * Place a ship on the grid
     */
    fun placeShip(ship: Ship) {
        if (!ship.isPlaced) return
        
        for ((row, col) in ship.getPositions()) {
            setCellType(row, col, CellType.SHIP)
        }
    }
    
    /**
     * Remove a ship from the grid
     */
    fun removeShip(ship: Ship) {
        if (!ship.isPlaced) return
        
        for ((row, col) in ship.getPositions()) {
            if (getCellType(row, col) == CellType.SHIP) {
                setCellType(row, col, CellType.EMPTY)
            }
        }
    }
    
    /**
     * Shoot at a position and return the result
     */
    fun shoot(row: Int, col: Int): ShotResult {
        if (!GameConstants.isValidPosition(row, col)) {
            return ShotResult.INVALID
        }
        
        if (hasBeenShot(row, col)) {
            return ShotResult.ALREADY_SHOT
        }
        
        val cellType = getCellType(row, col)
        return when (cellType) {
            CellType.SHIP -> {
                setCellType(row, col, CellType.HIT)
                ShotResult.HIT
            }
            CellType.EMPTY -> {
                setCellType(row, col, CellType.MISS)
                ShotResult.MISS
            }
            else -> ShotResult.INVALID
        }
    }
    
    /**
     * Mark ship parts as sunk
     */
    fun markShipAsSunk(ship: Ship) {
        if (!ship.isPlaced || !ship.isSunk()) return
        
        for ((row, col) in ship.getPositions()) {
            if (getCellType(row, col) == CellType.HIT) {
                setCellType(row, col, CellType.SUNK)
            }
        }
    }
    
    /**
     * Get all positions that have been shot
     */
    fun getShotPositions(): List<Pair<Int, Int>> {
        val shotPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (hasBeenShot(row, col)) {
                    shotPositions.add(row to col)
                }
            }
        }
        return shotPositions
    }
    
    /**
     * Get all positions that contain ships
     */
    fun getShipPositions(): List<Pair<Int, Int>> {
        val shipPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (hasShip(row, col)) {
                    shipPositions.add(row to col)
                }
            }
        }
        return shipPositions
    }
    
    /**
     * Get all positions that were hit
     */
    fun getHitPositions(): List<Pair<Int, Int>> {
        val hitPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (isHit(row, col)) {
                    hitPositions.add(row to col)
                }
            }
        }
        return hitPositions
    }
    
    /**
     * Get all positions that were missed
     */
    fun getMissPositions(): List<Pair<Int, Int>> {
        val missPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (isMiss(row, col)) {
                    missPositions.add(row to col)
                }
            }
        }
        return missPositions
    }
    
    /**
     * Get all empty positions (not shot, no ship)
     */
    fun getEmptyPositions(): List<Pair<Int, Int>> {
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (isEmpty(row, col)) {
                    emptyPositions.add(row to col)
                }
            }
        }
        return emptyPositions
    }
    
    /**
     * Get all positions available for shooting (not yet shot)
     */
    fun getAvailableTargets(): List<Pair<Int, Int>> {
        val availableTargets = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                if (!hasBeenShot(row, col)) {
                    availableTargets.add(row to col)
                }
            }
        }
        return availableTargets
    }
    
    /**
     * Check if a rectangular area is clear (no ships)
     */
    fun isAreaClear(startRow: Int, startCol: Int, endRow: Int, endCol: Int): Boolean {
        for (row in startRow..endRow) {
            for (col in startCol..endCol) {
                if (!GameConstants.isValidPosition(row, col)) continue
                if (hasShip(row, col)) {
                    return false
                }
            }
        }
        return true
    }
    
    /**
     * Check if a ship can be placed without overlapping
     */
    fun canPlaceShip(ship: Ship, row: Int, col: Int, orientation: Orientation): Boolean {
        if (!ship.canBePlacedAt(row, col, orientation)) {
            return false
        }
        
        // Create temporary ship to check positions
        val tempShip = ship.copy()
        tempShip.placeAt(row, col, orientation)
        
        // Check if any position is already occupied
        for ((shipRow, shipCol) in tempShip.getPositions()) {
            if (hasShip(shipRow, shipCol)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Clear the entire grid
     */
    fun clear() {
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                cells[row][col] = CellType.EMPTY
            }
        }
    }
    
    /**
     * Create a copy of this grid
     */
    fun copy(): Grid {
        val newGrid = Grid()
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                newGrid.cells[row][col] = this.cells[row][col]
            }
        }
        return newGrid
    }
    
    /**
     * Get grid statistics
     */
    fun getStats(): GridStats {
        var ships = 0
        var hits = 0
        var misses = 0
        var sunk = 0
        
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                when (getCellType(row, col)) {
                    CellType.SHIP -> ships++
                    CellType.HIT -> hits++
                    CellType.MISS -> misses++
                    CellType.SUNK -> sunk++
                    else -> {}
                }
            }
        }
        
        return GridStats(ships, hits, misses, sunk)
    }
    
    // Required for Parcelable
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Grid
        
        if (!cells.contentDeepEquals(other.cells)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        return cells.contentDeepHashCode()
    }
}

/**
 * Result of shooting at a position
 */
enum class ShotResult {
    HIT,
    MISS,
    ALREADY_SHOT,
    INVALID
}

/**
 * Grid statistics
 */
data class GridStats(
    val ships: Int,
    val hits: Int,
    val misses: Int,
    val sunk: Int
) {
    val totalShots: Int get() = hits + misses + sunk
    val hitRate: Float get() = if (totalShots > 0) (hits + sunk).toFloat() / totalShots else 0f
} 