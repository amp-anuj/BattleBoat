//
//  GridView.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import UIKit

protocol GridViewDelegate: AnyObject {
    func gridView(_ gridView: GridView, didTapCellAt x: Int, y: Int)
    func gridView(_ gridView: GridView, didLongPressCellAt x: Int, y: Int)
}

class GridView: UIView {
    weak var delegate: GridViewDelegate?
    
    private let grid: Grid
    private let isPlayerGrid: Bool
    private var cellButtons: [[UIButton]] = []
    private let gridSize: Int
    private var isGridSetup = false
    
    // Visual properties (will be calculated based on container size)
    private var cellSize: CGFloat = 30.0
    private var spacing: CGFloat = 2.0
    
    init(grid: Grid, isPlayerGrid: Bool = false) {
        self.grid = grid
        self.isPlayerGrid = isPlayerGrid
        self.gridSize = grid.size
        
        // Start with a default size, will be resized when added to container
        super.init(frame: CGRect(x: 0, y: 0, width: 300, height: 300))
        
        // Grid setup will happen in layoutSubviews once we know the actual size
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        // Only setup grid once we have a proper size
        guard bounds.width > 0 && bounds.height > 0 else { return }
        
        // Calculate sizing based on actual container size
        let containerSize = min(bounds.width, bounds.height)
        let newSpacing = max(1.0, containerSize * 0.01) // 1% of container size for spacing
        let newCellSize = (containerSize - (CGFloat(gridSize - 1) * newSpacing)) / CGFloat(gridSize)
        
        // Only setup if size changed significantly or not yet setup
        if !isGridSetup || abs(cellSize - newCellSize) > 1.0 {
            spacing = newSpacing
            cellSize = newCellSize
            setupGrid()
            updateDisplay()
            isGridSetup = true
        }
    }
    
    // MARK: - Grid Setup
    
    private func setupGrid() {
        // Clear existing buttons
        cellButtons.flatMap { $0 }.forEach { $0.removeFromSuperview() }
        
        backgroundColor = GameConstants.Colors.gridLineColor
        cellButtons = Array(repeating: Array(repeating: UIButton(), count: gridSize), count: gridSize)
        
        for x in 0..<gridSize {
            for y in 0..<gridSize {
                let button = createCellButton(x: x, y: y)
                cellButtons[x][y] = button
                addSubview(button)
            }
        }
    }
    
    private func createCellButton(x: Int, y: Int) -> UIButton {
        let button = UIButton(type: .custom)
        
        let xPos = CGFloat(x) * (cellSize + spacing)
        let yPos = CGFloat(y) * (cellSize + spacing)
        button.frame = CGRect(x: xPos, y: yPos, width: cellSize, height: cellSize)
        
        button.backgroundColor = GameConstants.Colors.waterColor
        button.layer.borderWidth = max(0.5, cellSize * 0.03)
        button.layer.borderColor = GameConstants.Colors.gridLineColor.cgColor
        button.layer.cornerRadius = max(2.0, cellSize * 0.1)
        
        button.tag = x * gridSize + y
        button.addTarget(self, action: #selector(cellTapped(_:)), for: .touchUpInside)
        
        // Add long press gesture for ship placement
        let longPress = UILongPressGestureRecognizer(target: self, action: #selector(cellLongPressed(_:)))
        longPress.minimumPressDuration = 0.5
        button.addGestureRecognizer(longPress)
        
        return button
    }
    
    // MARK: - User Interaction
    
    @objc private func cellTapped(_ sender: UIButton) {
        let x = sender.tag / gridSize
        let y = sender.tag % gridSize
        delegate?.gridView(self, didTapCellAt: x, y: y)
    }
    
    @objc private func cellLongPressed(_ gesture: UILongPressGestureRecognizer) {
        guard gesture.state == .began else { return }
        
        if let button = gesture.view as? UIButton {
            let x = button.tag / gridSize
            let y = button.tag % gridSize
            delegate?.gridView(self, didLongPressCellAt: x, y: y)
        }
    }
    
    // MARK: - Display Updates
    
    func updateDisplay() {
        for x in 0..<gridSize {
            for y in 0..<gridSize {
                updateCell(x: x, y: y)
            }
        }
    }
    
    func updateCell(x: Int, y: Int) {
        guard x >= 0 && x < gridSize && y >= 0 && y < gridSize else { return }
        
        let button = cellButtons[x][y]
        let cellType = grid.getCellType(x: x, y: y)
        
        configureCellAppearance(button: button, cellType: cellType, x: x, y: y)
    }
    
    private func configureCellAppearance(button: UIButton, cellType: GameConstants.CellType, x: Int, y: Int) {
        button.setTitle("", for: .normal)
        button.backgroundColor = getCellColor(for: cellType)
        
        // Add visual indicators
        switch cellType {
        case .empty:
            button.setTitle("", for: .normal)
            
        case .ship:
            if isPlayerGrid {
                // Show ships on player's grid
                button.backgroundColor = GameConstants.Colors.shipColor
                button.setTitle("●", for: .normal)
                button.setTitleColor(.white, for: .normal)
            } else {
                // Hide ships on enemy grid
                button.backgroundColor = GameConstants.Colors.waterColor
                button.setTitle("", for: .normal)
            }
            
        case .miss:
            button.setTitle("○", for: .normal)
            button.setTitleColor(.white, for: .normal)
            button.titleLabel?.font = UIFont.boldSystemFont(ofSize: cellSize * 0.6)
            
        case .hit:
            button.setTitle("✗", for: .normal)
            button.setTitleColor(.white, for: .normal)
            button.titleLabel?.font = UIFont.boldSystemFont(ofSize: cellSize * 0.6)
            
        case .sunk:
            button.setTitle("✗", for: .normal)
            button.setTitleColor(.white, for: .normal)
            button.titleLabel?.font = UIFont.boldSystemFont(ofSize: cellSize * 0.6)
            button.layer.borderWidth = max(1.0, cellSize * 0.1)
            button.layer.borderColor = UIColor.red.cgColor
        }
        
        // Accessibility
        button.accessibilityLabel = getAccessibilityLabel(x: x, y: y, cellType: cellType)
    }
    
    private func getCellColor(for cellType: GameConstants.CellType) -> UIColor {
        switch cellType {
        case .empty:
            return GameConstants.Colors.waterColor
        case .ship:
            return isPlayerGrid ? GameConstants.Colors.shipColor : GameConstants.Colors.waterColor
        case .miss:
            return GameConstants.Colors.missColor
        case .hit:
            return GameConstants.Colors.hitColor
        case .sunk:
            return GameConstants.Colors.sunkColor
        }
    }
    
    private func getAccessibilityLabel(x: Int, y: Int, cellType: GameConstants.CellType) -> String {
        let position = "Row \(y + 1), Column \(x + 1)"
        
        switch cellType {
        case .empty:
            return "\(position), Empty water"
        case .ship:
            return isPlayerGrid ? "\(position), Your ship" : "\(position), Unknown"
        case .miss:
            return "\(position), Miss"
        case .hit:
            return "\(position), Hit"
        case .sunk:
            return "\(position), Sunk ship"
        }
    }
    
    // MARK: - Ship Placement Preview
    
    func showShipPreview(at x: Int, y: Int, size: Int, direction: GameConstants.ShipDirection, isValid: Bool) {
        clearShipPreview()
        
        let coords = calculateShipCoordinates(x: x, y: y, size: size, direction: direction)
        let previewColor = isValid ? GameConstants.Colors.selectedColor : UIColor.red.withAlphaComponent(0.5)
        
        for coord in coords {
            if coord.x >= 0 && coord.x < gridSize && coord.y >= 0 && coord.y < gridSize {
                let button = cellButtons[coord.x][coord.y]
                button.backgroundColor = previewColor
            }
        }
    }
    
    func clearShipPreview() {
        for x in 0..<gridSize {
            for y in 0..<gridSize {
                if grid.getCellType(x: x, y: y) == .empty {
                    cellButtons[x][y].backgroundColor = GameConstants.Colors.waterColor
                }
            }
        }
    }
    
    private func calculateShipCoordinates(x: Int, y: Int, size: Int, direction: GameConstants.ShipDirection) -> [(x: Int, y: Int)] {
        var coords: [(x: Int, y: Int)] = []
        
        for i in 0..<size {
            if direction == .horizontal {
                coords.append((x: x + i, y: y))
            } else {
                coords.append((x: x, y: y + i))
            }
        }
        
        return coords
    }
    
    // MARK: - Animation
    
    func animateCell(x: Int, y: Int, completion: (() -> Void)? = nil) {
        guard x >= 0 && x < gridSize && y >= 0 && y < gridSize else {
            completion?()
            return
        }
        
        let button = cellButtons[x][y]
        
        UIView.animate(withDuration: 0.2, delay: 0, options: [.curveEaseInOut], animations: {
            button.transform = CGAffineTransform(scaleX: 1.2, y: 1.2)
        }) { _ in
            UIView.animate(withDuration: 0.2, delay: 0, options: [.curveEaseInOut], animations: {
                button.transform = .identity
            }) { _ in
                completion?()
            }
        }
    }
    
    func highlightCell(x: Int, y: Int, highlight: Bool) {
        guard x >= 0 && x < gridSize && y >= 0 && y < gridSize else { return }
        
        let button = cellButtons[x][y]
        
        if highlight {
            button.layer.borderWidth = 3.0
            button.layer.borderColor = UIColor.yellow.cgColor
        } else {
            button.layer.borderWidth = 1.0
            button.layer.borderColor = GameConstants.Colors.gridLineColor.cgColor
        }
    }
    
    // MARK: - Probability Heatmap (Debug Feature)
    
    func showProbabilityHeatmap(probabilities: [[Double]]) {
        guard probabilities.count == gridSize && probabilities[0].count == gridSize else { return }
        
        // Find max probability for normalization
        var maxProb: Double = 0
        for row in probabilities {
            for prob in row {
                maxProb = max(maxProb, prob)
            }
        }
        
        guard maxProb > 0 else { return }
        
        for x in 0..<gridSize {
            for y in 0..<gridSize {
                if grid.getCellType(x: x, y: y) == .empty {
                    let button = cellButtons[x][y]
                    let normalizedProb = probabilities[x][y] / maxProb
                    let alpha = CGFloat(normalizedProb * 0.7 + 0.1) // 0.1 to 0.8 alpha
                    button.backgroundColor = UIColor.red.withAlphaComponent(alpha)
                    
                    // Show probability as text for debugging
                    button.setTitle(String(format: "%.0f", probabilities[x][y]), for: .normal)
                    button.setTitleColor(.white, for: .normal)
                    button.titleLabel?.font = UIFont.systemFont(ofSize: max(8, cellSize * 0.25))
                }
            }
        }
    }
    
    func hideProbabilityHeatmap() {
        updateDisplay()
    }
    
    // MARK: - Convenience
    
    override var intrinsicContentSize: CGSize {
        // Return the actual calculated size, or a default if not yet calculated
        if cellSize > 0 {
            let totalSize = CGFloat(gridSize) * cellSize + CGFloat(gridSize - 1) * spacing
            return CGSize(width: totalSize, height: totalSize)
        } else {
            // Default size before layout
            return CGSize(width: 300, height: 300)
        }
    }
} 