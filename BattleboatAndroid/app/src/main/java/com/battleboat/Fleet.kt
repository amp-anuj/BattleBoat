package com.battleboat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Manages a collection of ships for a player
 */
@Parcelize
data class Fleet(
    private val ships: MutableList<Ship> = mutableListOf()
) : Parcelable {
    
    init {
        if (ships.isEmpty()) {
            // Initialize with standard battleship fleet
            ships.addAll(ShipType.allShips().map { Ship(it) })
        }
    }
    
    /**
     * Get all ships in the fleet
     */
    fun getAllShips(): List<Ship> = ships.toList()
    
    /**
     * Get ships that are placed on the board
     */
    fun getPlacedShips(): List<Ship> = ships.filter { it.isPlaced }
    
    /**
     * Get ships that are not yet placed
     */
    fun getUnplacedShips(): List<Ship> = ships.filter { !it.isPlaced }
    
    /**
     * Get a ship by its type
     */
    fun getShip(type: ShipType): Ship? = ships.find { it.type == type }
    
    /**
     * Get ship at a specific position
     */
    fun getShipAt(row: Int, col: Int): Ship? {
        return ships.find { it.containsPosition(row, col) }
    }
    
    /**
     * Add a ship to the fleet
     */
    fun addShip(ship: Ship) {
        ships.add(ship)
    }
    
    /**
     * Remove a ship from the fleet
     */
    fun removeShip(ship: Ship) {
        ships.remove(ship)
    }
    
    /**
     * Check if all ships are placed
     */
    fun allShipsPlaced(): Boolean = ships.all { it.isPlaced }
    
    /**
     * Check if all ships are sunk
     */
    fun allShipsSunk(): Boolean = getPlacedShips().all { it.isSunk() }
    
    /**
     * Get the number of ships
     */
    fun shipCount(): Int = ships.size
    
    /**
     * Get the number of placed ships
     */
    fun placedShipCount(): Int = getPlacedShips().size
    
    /**
     * Get the number of sunk ships
     */
    fun sunkShipCount(): Int = ships.count { it.isSunk() }
    
    /**
     * Get the number of remaining (not sunk) ships
     */
    fun remainingShipCount(): Int = ships.count { it.isPlaced && !it.isSunk() }
    
    /**
     * Get all positions occupied by ships
     */
    fun getAllShipPositions(): Set<Pair<Int, Int>> {
        val positions = mutableSetOf<Pair<Int, Int>>()
        ships.forEach { ship ->
            positions.addAll(ship.getPositions())
        }
        return positions
    }
    
    /**
     * Get all positions that have been hit
     */
    fun getAllHitPositions(): Set<Pair<Int, Int>> {
        val positions = mutableSetOf<Pair<Int, Int>>()
        ships.forEach { ship ->
            positions.addAll(ship.getHitPositions())
        }
        return positions
    }
    
    /**
     * Check if a position can be used for ship placement
     */
    fun canPlaceShipAt(ship: Ship, row: Int, col: Int, orientation: Orientation): Boolean {
        if (!ship.canBePlacedAt(row, col, orientation)) {
            return false
        }
        
        // Create temporary ship to check positions
        val tempShip = ship.copy()
        tempShip.placeAt(row, col, orientation)
        
        // Check for overlaps with existing ships
        for (existingShip in getPlacedShips()) {
            if (existingShip.type != ship.type && tempShip.overlapsWith(existingShip)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Place a ship at the specified position
     */
    fun placeShip(ship: Ship, row: Int, col: Int, orientation: Orientation): Boolean {
        if (!canPlaceShipAt(ship, row, col, orientation)) {
            return false
        }
        
        return ship.placeAt(row, col, orientation)
    }
    
    /**
     * Remove a ship from the board
     */
    fun removeShipFromBoard(ship: Ship) {
        ship.removeFromBoard()
    }
    
    /**
     * Hit a ship at the specified position
     */
    fun hitShip(row: Int, col: Int): Ship? {
        val ship = getShipAt(row, col)
        ship?.hit(row, col)
        return ship
    }
    
    /**
     * Auto-place all ships randomly
     */
    fun autoPlaceShips() {
        // Remove all ships from board first
        ships.forEach { it.removeFromBoard() }
        
        val shuffledShips = ships.shuffled()
        
        for (ship in shuffledShips) {
            var attempts = 0
            var placed = false
            
            while (!placed && attempts < 100) {
                val row = (0 until GameConstants.GRID_SIZE).random()
                val col = (0 until GameConstants.GRID_SIZE).random()
                val orientation = Orientation.values().random()
                
                if (canPlaceShipAt(ship, row, col, orientation)) {
                    placeShip(ship, row, col, orientation)
                    placed = true
                }
                
                attempts++
            }
        }
    }
    
    /**
     * Clear all ship placements
     */
    fun clearPlacements() {
        ships.forEach { it.removeFromBoard() }
    }
    
    /**
     * Get fleet statistics
     */
    fun getStats(): FleetStats {
        val totalShips = ships.size
        val placedShips = placedShipCount()
        val sunkShips = sunkShipCount()
        val remainingShips = remainingShipCount()
        
        val totalHits = ships.sumOf { it.getHitCount() }
        val totalSize = ships.sumOf { it.size }
        
        return FleetStats(
            totalShips = totalShips,
            placedShips = placedShips,
            sunkShips = sunkShips,
            remainingShips = remainingShips,
            totalHits = totalHits,
            totalSize = totalSize,
            damagePercentage = if (totalSize > 0) totalHits.toFloat() / totalSize else 0f
        )
    }
    
    /**
     * Get the largest remaining ship size
     */
    fun getLargestRemainingShipSize(): Int {
        return ships.filter { it.isPlaced && !it.isSunk() }
            .maxOfOrNull { it.size } ?: 0
    }
    
    /**
     * Get ships by size (largest first)
     */
    fun getShipsBySize(): List<Ship> {
        return ships.sortedByDescending { it.size }
    }
    
    /**
     * Check if fleet is valid (all ships placed, no overlaps)
     */
    fun isValid(): Boolean {
        if (!allShipsPlaced()) return false
        
        // Check for overlaps
        for (i in ships.indices) {
            for (j in i + 1 until ships.size) {
                if (ships[i].overlapsWith(ships[j])) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * Get a random ship that's not sunk
     */
    fun getRandomAliveShip(): Ship? {
        val aliveShips = ships.filter { it.isPlaced && !it.isSunk() }
        return if (aliveShips.isNotEmpty()) aliveShips.random() else null
    }
    
    /**
     * Get ships that have been hit but not sunk
     */
    fun getDamagedShips(): List<Ship> {
        return ships.filter { it.isPlaced && it.getHitCount() > 0 && !it.isSunk() }
    }
    
    /**
     * Get ships that haven't been hit at all
     */
    fun getUntouchedShips(): List<Ship> {
        return ships.filter { it.isPlaced && it.getHitCount() == 0 }
    }
    
    /**
     * Create a deep copy of the fleet
     */
    fun copy(): Fleet {
        val newShips = ships.map { it.copy() }.toMutableList()
        return Fleet(newShips)
    }
    
    override fun toString(): String {
        val placed = placedShipCount()
        val sunk = sunkShipCount()
        return "Fleet: $placed/${shipCount()} placed, $sunk sunk"
    }
}

/**
 * Fleet statistics
 */
data class FleetStats(
    val totalShips: Int,
    val placedShips: Int,
    val sunkShips: Int,
    val remainingShips: Int,
    val totalHits: Int,
    val totalSize: Int,
    val damagePercentage: Float
) {
    val isDefeated: Boolean get() = sunkShips == totalShips
    val survivalRate: Float get() = if (totalShips > 0) remainingShips.toFloat() / totalShips else 0f
} 