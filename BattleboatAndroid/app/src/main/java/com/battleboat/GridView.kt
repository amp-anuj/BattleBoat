package com.battleboat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/**
 * Custom view that displays a 10x10 battleship grid
 */
class GridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var cellSize = 0f
    private var gridStartX = 0f
    private var gridStartY = 0f
    private var gridSize = 0f
    
    // Grid data
    private var grid: Grid? = null
    private var fleet: Fleet? = null
    private var isPlayerGrid = true
    private var showShips = true
    
    // Interaction
    var onCellClickListener: ((row: Int, col: Int) -> Unit)? = null
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Grid lines — Amplitude border color
        gridPaint.color = Color.parseColor("#26314A")
        gridPaint.strokeWidth = 1.5f
        gridPaint.style = Paint.Style.STROKE

        // Coordinate text — muted secondary
        textPaint.color = Color.parseColor("#5A6A85")
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
    }
    
    fun setGrid(grid: Grid, fleet: Fleet? = null, isPlayerGrid: Boolean = true, showShips: Boolean = true) {
        this.grid = grid
        this.fleet = fleet
        this.isPlayerGrid = isPlayerGrid
        this.showShips = showShips
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions()
    }
    
    private fun calculateDimensions() {
        // Reserve space for coordinates
        val coordinateSpace = 30f
        val availableWidth = width - coordinateSpace * 2
        val availableHeight = height - coordinateSpace * 2
        val maxSize = min(availableWidth, availableHeight)
        
        gridSize = maxSize
        cellSize = gridSize / GameConstants.GRID_SIZE
        
        // Center the grid in the available space
        gridStartX = coordinateSpace + (availableWidth - gridSize) / 2
        gridStartY = coordinateSpace + (availableHeight - gridSize) / 2
        
        // Ensure minimum cell size for usability
        if (cellSize < 15f) {
            cellSize = 15f
            gridSize = cellSize * GameConstants.GRID_SIZE
            gridStartX = coordinateSpace + (availableWidth - gridSize) / 2
            gridStartY = coordinateSpace + (availableHeight - gridSize) / 2
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val grid = this.grid ?: return
        
        // Draw cells
        for (row in 0 until GameConstants.GRID_SIZE) {
            for (col in 0 until GameConstants.GRID_SIZE) {
                drawCell(canvas, row, col, grid.getCellType(row, col))
            }
        }
        
        // Draw grid lines
        drawGridLines(canvas)
        
        // Draw coordinates
        drawCoordinates(canvas)
    }
    
    private fun drawCell(canvas: Canvas, row: Int, col: Int, cellType: CellType) {
        val left = gridStartX + col * cellSize
        val top = gridStartY + row * cellSize
        val right = left + cellSize
        val bottom = top + cellSize
        
        val rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        
        // Set cell color based on type
        paint.style = Paint.Style.FILL
        when (cellType) {
            CellType.EMPTY -> {
                // Amplitude deep ocean water (#394B75)
                paint.color = Color.parseColor("#394B75")
            }
            CellType.SHIP -> {
                if (showShips) {
                    // Amplitude Teal for placed ships
                    paint.color = Color.parseColor("#009D88")
                } else {
                    // Hide ships on enemy grid — same as water
                    paint.color = Color.parseColor("#394B75")
                }
            }
            CellType.HIT -> {
                // Amplitude Coral for hits
                paint.color = Color.parseColor("#E8410E")
            }
            CellType.MISS -> {
                // Dark surface for misses (muted)
                paint.color = Color.parseColor("#252E45")
            }
            CellType.SUNK -> {
                // Near-black with red tinge for sunk
                paint.color = Color.parseColor("#1A0A05")
            }
        }

        // Rounded rect cells
        val radius = cellSize * 0.12f
        val rectF = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectF, radius, radius, paint)

        // Draw hit/miss markers
        when (cellType) {
            CellType.HIT -> {
                // White glow dot
                paint.color = Color.parseColor("#FFFFFF")
                paint.style = Paint.Style.FILL
                canvas.drawCircle(
                    left + cellSize / 2,
                    top + cellSize / 2,
                    cellSize / 5,
                    paint
                )
            }
            CellType.MISS -> {
                // Muted blue ring
                paint.color = Color.parseColor("#3986F7")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = maxOf(2f, cellSize * 0.06f)
                canvas.drawCircle(
                    left + cellSize / 2,
                    top + cellSize / 2,
                    cellSize / 5,
                    paint
                )
            }
            CellType.SUNK -> {
                // Coral border on sunk cells
                paint.color = Color.parseColor("#E8410E")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = maxOf(2f, cellSize * 0.08f)
                canvas.drawRoundRect(rectF, radius, radius, paint)
            }
            else -> {}
        }
    }
    
    private fun drawGridLines(canvas: Canvas) {
        gridPaint.color = Color.parseColor("#26314A")
        gridPaint.strokeWidth = 1f

        // Vertical lines
        for (i in 0..GameConstants.GRID_SIZE) {
            val x = gridStartX + i * cellSize
            canvas.drawLine(x, gridStartY, x, gridStartY + gridSize, gridPaint)
        }
        
        // Horizontal lines
        for (i in 0..GameConstants.GRID_SIZE) {
            val y = gridStartY + i * cellSize
            canvas.drawLine(gridStartX, y, gridStartX + gridSize, y, gridPaint)
        }
    }
    
    private fun drawCoordinates(canvas: Canvas) {
        val textSize = maxOf(cellSize / 4, 12f) // Ensure readable text size
        textPaint.textSize = textSize
        
        // Column letters (A-J) - above the grid
        for (col in 0 until GameConstants.GRID_SIZE) {
            val letter = ('A' + col).toString()
            val x = gridStartX + col * cellSize + cellSize / 2
            val y = gridStartY - 8
            if (y > textSize) { // Only draw if there's space
                canvas.drawText(letter, x, y, textPaint)
            }
        }
        
        // Row numbers (1-10) - left of the grid
        for (row in 0 until GameConstants.GRID_SIZE) {
            val number = (row + 1).toString()
            val x = gridStartX - 16
            val y = gridStartY + row * cellSize + cellSize / 2 + textSize / 3
            if (x > 0) { // Only draw if there's space
                canvas.drawText(number, x, y, textPaint)
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            // Check if touch is within grid bounds
            if (x >= gridStartX && x <= gridStartX + gridSize &&
                y >= gridStartY && y <= gridStartY + gridSize) {
                
                val col = ((x - gridStartX) / cellSize).toInt()
                val row = ((y - gridStartY) / cellSize).toInt()
                
                if (row in 0 until GameConstants.GRID_SIZE && col in 0 until GameConstants.GRID_SIZE) {
                    onCellClickListener?.invoke(row, col)
                    return true
                }
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        // Calculate the desired size
        val desiredSize = when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                min(widthSize, heightSize)
            }
            widthMode == MeasureSpec.EXACTLY -> widthSize
            heightMode == MeasureSpec.EXACTLY -> heightSize
            else -> min(widthSize, heightSize)
        }
        
        setMeasuredDimension(desiredSize, desiredSize)
    }
} 