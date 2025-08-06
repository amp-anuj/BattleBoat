//
//  GameViewController.swift
//  BattleboatIOS
//
//  Created by Battleboat on 2024/01/01.
//  Copyright © 2024 Battleboat. All rights reserved.
//

import UIKit

class GameViewController: UIViewController {
    
    // MARK: - Game Components
    private var gameModel: GameModel!
    private var playerGridView: GridView!
    private var enemyGridView: GridView!
    
    // MARK: - UI Components
    private let scrollView = UIScrollView()
    private let mainStackView = UIStackView()
    private let titleLabel = UILabel()
    private let messageLabel = UILabel()
    private let playerGridLabel = UILabel()
    private let playerGridContainer = UIView()
    private let enemyGridLabel = UILabel()
    private let enemyGridContainer = UIView()
    private let shipSelectionStackView = UIStackView()
    private let controlsStackView = UIStackView()
    private let statsStackView = UIStackView()
    
    // Ship selection buttons
    private let carrierButton = UIButton(type: .system)
    private let battleshipButton = UIButton(type: .system)
    private let destroyerButton = UIButton(type: .system)
    private let submarineButton = UIButton(type: .system)
    private let patrolboatButton = UIButton(type: .system)
    
    // Control buttons
    private let rotateButton = UIButton(type: .system)
    private let randomPlaceButton = UIButton(type: .system)
    private let startGameButton = UIButton(type: .system)
    private let restartButton = UIButton(type: .system)
    private let showHeatmapButton = UIButton(type: .system)
    
    // Stats labels
    private let gamesWonLabel = UILabel()
    private let accuracyLabel = UILabel()
    private let currentShotsLabel = UILabel()
    private let resetStatsButton = UIButton(type: .system)
    
    // Tutorial overlay
    private let tutorialView = UIView()
    private let tutorialLabel = UILabel()
    private let skipTutorialButton = UIButton(type: .system)
    
    private var shipButtons: [GameConstants.ShipType: UIButton] = [:]
    private var isShowingHeatmap = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        initializeGame()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        setupGridViews()
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        view.backgroundColor = UIColor.white
        
        // Setup scroll view for guaranteed scrolling
        scrollView.showsVerticalScrollIndicator = true
        scrollView.alwaysBounceVertical = true
        scrollView.isScrollEnabled = true
        scrollView.contentInsetAdjustmentBehavior = .never
        scrollView.bounces = true
        scrollView.scrollIndicatorInsets = .zero
        scrollView.contentInset = .zero
        
        // Setup title
        titleLabel.text = "Battleboat iOS"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 24)
        titleLabel.textColor = .systemBlue
        titleLabel.textAlignment = .center
        
        // Setup message label
        messageLabel.text = "Welcome to Battleboat! Place your ships to begin."
        messageLabel.numberOfLines = 0
        messageLabel.textAlignment = .center
        messageLabel.font = UIFont.systemFont(ofSize: 16)
        
        // Setup grid labels
        playerGridLabel.text = "Your Fleet"
        playerGridLabel.textAlignment = .center
        playerGridLabel.font = UIFont.boldSystemFont(ofSize: 16)
        
        enemyGridLabel.text = "Enemy Fleet"
        enemyGridLabel.textAlignment = .center
        enemyGridLabel.font = UIFont.boldSystemFont(ofSize: 16)
        
        // Setup containers
        playerGridContainer.backgroundColor = UIColor.clear
        playerGridContainer.layer.cornerRadius = 6
        playerGridContainer.layer.borderWidth = 1
        playerGridContainer.layer.borderColor = UIColor.systemGray4.cgColor
        
        enemyGridContainer.backgroundColor = UIColor.clear
        enemyGridContainer.layer.cornerRadius = 6
        enemyGridContainer.layer.borderWidth = 1
        enemyGridContainer.layer.borderColor = UIColor.systemGray4.cgColor
        
        // Map ship buttons
        shipButtons = [
            .carrier: carrierButton,
            .battleship: battleshipButton,
            .destroyer: destroyerButton,
            .submarine: submarineButton,
            .patrolboat: patrolboatButton
        ]
        
        // Setup components
        setupShipButtons()
        setupControlButtons()
        setupStatsLabels()
        setupTutorial()
        setupLayoutConstraints()
        
        // Initial UI state
        updateUIForGameState(.placingShips)
    }
    
    private func setupShipButtons() {
        shipSelectionStackView.axis = .horizontal
        shipSelectionStackView.spacing = 8
        shipSelectionStackView.distribution = .fillEqually
        
        for (shipType, button) in shipButtons {
            // Use compact names for horizontal layout
            let buttonTitle: String
            switch shipType {
            case .carrier: buttonTitle = "Carrier\n(5)"
            case .battleship: buttonTitle = "Battle\n(4)"
            case .destroyer: buttonTitle = "Destroy\n(3)"
            case .submarine: buttonTitle = "Sub\n(3)"
            case .patrolboat: buttonTitle = "Patrol\n(2)"
            }
            
            button.setTitle(buttonTitle, for: .normal)
            button.backgroundColor = UIColor.systemBlue
            button.setTitleColor(.white, for: .normal)
            button.layer.cornerRadius = 6
            button.titleLabel?.numberOfLines = 2
            button.titleLabel?.textAlignment = .center
            button.titleLabel?.font = UIFont.systemFont(ofSize: 12, weight: .medium)
            button.titleLabel?.adjustsFontSizeToFitWidth = true
            button.titleLabel?.minimumScaleFactor = 0.7
            button.addTarget(self, action: #selector(shipButtonTapped(_:)), for: .touchUpInside)
            shipSelectionStackView.addArrangedSubview(button)
        }
    }
    
    private func setupControlButtons() {
        controlsStackView.axis = .horizontal
        controlsStackView.spacing = 8
        controlsStackView.distribution = .fillEqually
        
        let buttons = [rotateButton, randomPlaceButton, startGameButton, restartButton, showHeatmapButton]
        
        for button in buttons {
            button.backgroundColor = UIColor.systemGray
            button.setTitleColor(.white, for: .normal)
            button.layer.cornerRadius = 8
            button.titleLabel?.numberOfLines = 2
            button.titleLabel?.textAlignment = .center
            button.titleLabel?.font = UIFont.systemFont(ofSize: 10, weight: .medium)
            button.titleLabel?.adjustsFontSizeToFitWidth = true
            button.titleLabel?.minimumScaleFactor = 0.7
            controlsStackView.addArrangedSubview(button)
        }
        
        // Set button titles
        rotateButton.setTitle("Rotate\nShip", for: .normal)
        randomPlaceButton.setTitle("Random\nPlace", for: .normal)
        startGameButton.setTitle("Start\nGame", for: .normal)
        restartButton.setTitle("New\nGame", for: .normal)
        showHeatmapButton.setTitle("Show\nHeatmap", for: .normal)
        
        // Add targets
        rotateButton.addTarget(self, action: #selector(rotateButtonTapped), for: .touchUpInside)
        randomPlaceButton.addTarget(self, action: #selector(randomPlaceButtonTapped), for: .touchUpInside)
        startGameButton.addTarget(self, action: #selector(startGameButtonTapped), for: .touchUpInside)
        restartButton.addTarget(self, action: #selector(restartButtonTapped), for: .touchUpInside)
        showHeatmapButton.addTarget(self, action: #selector(showHeatmapButtonTapped), for: .touchUpInside)
    }
    
    private func setupStatsLabels() {
        statsStackView.axis = .horizontal
        statsStackView.spacing = 8
        statsStackView.distribution = .fillEqually
        
        let labels = [gamesWonLabel, accuracyLabel, currentShotsLabel]
        
        for label in labels {
            label.font = UIFont.systemFont(ofSize: 14, weight: .medium)
            label.textAlignment = .center
            label.adjustsFontSizeToFitWidth = true
            label.minimumScaleFactor = 0.8
            label.numberOfLines = 1
            statsStackView.addArrangedSubview(label)
        }
        
        resetStatsButton.setTitle("Reset", for: .normal)
        resetStatsButton.backgroundColor = UIColor.systemRed
        resetStatsButton.setTitleColor(.white, for: .normal)
        resetStatsButton.layer.cornerRadius = 8
        resetStatsButton.titleLabel?.font = UIFont.systemFont(ofSize: 14, weight: .medium)
        resetStatsButton.addTarget(self, action: #selector(resetStatsButtonTapped), for: .touchUpInside)
        statsStackView.addArrangedSubview(resetStatsButton)
        
        // Initial stats with shorter text
        gamesWonLabel.text = "Won: 0/0"
        accuracyLabel.text = "Acc: 0%"
        currentShotsLabel.text = "Shots: 0"
    }
    
    private func setupTutorial() {
        tutorialView.backgroundColor = UIColor.black.withAlphaComponent(0.8)
        tutorialView.layer.cornerRadius = 12
        tutorialView.isHidden = true
        
        tutorialLabel.textColor = .white
        tutorialLabel.numberOfLines = 0
        tutorialLabel.textAlignment = .center
        tutorialLabel.font = UIFont.systemFont(ofSize: 16)
        
        skipTutorialButton.setTitle("Skip Tutorial", for: .normal)
        skipTutorialButton.backgroundColor = UIColor.systemRed
        skipTutorialButton.setTitleColor(.white, for: .normal)
        skipTutorialButton.layer.cornerRadius = 8
        skipTutorialButton.addTarget(self, action: #selector(skipTutorialTapped), for: .touchUpInside)
    }
    
    private func setupLayoutConstraints() {
        // Setup scroll view and main stack view
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        mainStackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(scrollView)
        scrollView.addSubview(mainStackView)
        
        // Configure main stack view with generous spacing for natural layout
        mainStackView.axis = .vertical
        mainStackView.spacing = 24
        mainStackView.alignment = .center
        mainStackView.distribution = .fill
        
        // Calculate grid size - same size for both grids
        let screenWidth = UIScreen.main.bounds.width
        let margin: CGFloat = 20
        let availableWidth = screenWidth - (margin * 2)
        let gridSize = min(availableWidth * 0.8, 250) // 80% of available width, max 250 points
        
        // Set fixed sizes for grid containers
        playerGridContainer.widthAnchor.constraint(equalToConstant: gridSize).isActive = true
        playerGridContainer.heightAnchor.constraint(equalToConstant: gridSize).isActive = true
        enemyGridContainer.widthAnchor.constraint(equalToConstant: gridSize).isActive = true
        enemyGridContainer.heightAnchor.constraint(equalToConstant: gridSize).isActive = true
        
        // Add spacer views for better separation between sections
        let topSpacer = UIView()
        topSpacer.heightAnchor.constraint(equalToConstant: 8).isActive = true
        
        let playerSectionSpacer = UIView()
        playerSectionSpacer.heightAnchor.constraint(equalToConstant: 12).isActive = true
        
        let enemySectionSpacer = UIView()
        enemySectionSpacer.heightAnchor.constraint(equalToConstant: 12).isActive = true
        
        let bottomSpacer = UIView()
        bottomSpacer.heightAnchor.constraint(equalToConstant: 20).isActive = true
        
        // Add all elements to stack view in order with explicit spacing
        let arrangedSubviews = [
            topSpacer,
            titleLabel,
            messageLabel,
            playerSectionSpacer,
            playerGridLabel,
            playerGridContainer,
            shipSelectionStackView,
            enemySectionSpacer,
            enemyGridLabel,
            enemyGridContainer,
            controlsStackView,
            statsStackView,
            bottomSpacer
        ]
        
        for subview in arrangedSubviews {
            subview.translatesAutoresizingMaskIntoConstraints = false
            mainStackView.addArrangedSubview(subview)
        }
        
        // Set specific widths for horizontal elements
        let buttonWidth = screenWidth - (margin * 2)
        shipSelectionStackView.widthAnchor.constraint(equalToConstant: buttonWidth).isActive = true
        controlsStackView.widthAnchor.constraint(equalToConstant: buttonWidth).isActive = true
        statsStackView.widthAnchor.constraint(equalToConstant: buttonWidth).isActive = true
        messageLabel.widthAnchor.constraint(equalToConstant: buttonWidth).isActive = true
        
        // Set heights for button rows - extra height to prevent text overlap
        shipSelectionStackView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        controlsStackView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        statsStackView.heightAnchor.constraint(equalToConstant: 50).isActive = true
        
        // Tutorial overlay (on main view, not scrollable)
        tutorialView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(tutorialView)
        tutorialView.addSubview(tutorialLabel)
        tutorialView.addSubview(skipTutorialButton)
        tutorialLabel.translatesAutoresizingMaskIntoConstraints = false
        skipTutorialButton.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            // Scroll view constraints - full screen
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            
            // Main stack view constraints - proper scroll setup with padding
            mainStackView.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor, constant: margin),
            mainStackView.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor, constant: margin),
            mainStackView.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor, constant: -margin),
            mainStackView.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor, constant: -margin),
            mainStackView.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor, constant: -margin * 2),
            
            // Tutorial overlay (on main view, not scrollable)
            tutorialView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            tutorialView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            tutorialView.widthAnchor.constraint(equalToConstant: 280),
            tutorialView.heightAnchor.constraint(equalToConstant: 180),
            
            tutorialLabel.topAnchor.constraint(equalTo: tutorialView.topAnchor, constant: 16),
            tutorialLabel.leadingAnchor.constraint(equalTo: tutorialView.leadingAnchor, constant: 16),
            tutorialLabel.trailingAnchor.constraint(equalTo: tutorialView.trailingAnchor, constant: -16),
            
            skipTutorialButton.bottomAnchor.constraint(equalTo: tutorialView.bottomAnchor, constant: -16),
            skipTutorialButton.centerXAnchor.constraint(equalTo: tutorialView.centerXAnchor),
            skipTutorialButton.widthAnchor.constraint(equalToConstant: 100),
            skipTutorialButton.heightAnchor.constraint(equalToConstant: 36)
        ])
    }
    
    private func initializeGame() {
        gameModel = GameModel()
        gameModel.delegate = self
    }
    
    private func setupGridViews() {
        // Remove existing grid views
        playerGridView?.removeFromSuperview()
        enemyGridView?.removeFromSuperview()
        
        // Create new grid views
        playerGridView = GridView(grid: gameModel.humanGrid, isPlayerGrid: true)
        enemyGridView = GridView(grid: gameModel.computerGrid, isPlayerGrid: false)
        
        playerGridView.delegate = self
        enemyGridView.delegate = self
        
        // Add to containers
        playerGridContainer.addSubview(playerGridView)
        enemyGridContainer.addSubview(enemyGridView)
        
        // Center the grids
        playerGridView.translatesAutoresizingMaskIntoConstraints = false
        enemyGridView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            // Player grid view - centered with padding
            playerGridView.centerXAnchor.constraint(equalTo: playerGridContainer.centerXAnchor),
            playerGridView.centerYAnchor.constraint(equalTo: playerGridContainer.centerYAnchor),
            playerGridView.leadingAnchor.constraint(greaterThanOrEqualTo: playerGridContainer.leadingAnchor, constant: 8),
            playerGridView.trailingAnchor.constraint(lessThanOrEqualTo: playerGridContainer.trailingAnchor, constant: -8),
            playerGridView.topAnchor.constraint(greaterThanOrEqualTo: playerGridContainer.topAnchor, constant: 8),
            playerGridView.bottomAnchor.constraint(lessThanOrEqualTo: playerGridContainer.bottomAnchor, constant: -8),
            
            // Enemy grid view - centered with padding
            enemyGridView.centerXAnchor.constraint(equalTo: enemyGridContainer.centerXAnchor),
            enemyGridView.centerYAnchor.constraint(equalTo: enemyGridContainer.centerYAnchor),
            enemyGridView.leadingAnchor.constraint(greaterThanOrEqualTo: enemyGridContainer.leadingAnchor, constant: 8),
            enemyGridView.trailingAnchor.constraint(lessThanOrEqualTo: enemyGridContainer.trailingAnchor, constant: -8),
            enemyGridView.topAnchor.constraint(greaterThanOrEqualTo: enemyGridContainer.topAnchor, constant: 8),
            enemyGridView.bottomAnchor.constraint(lessThanOrEqualTo: enemyGridContainer.bottomAnchor, constant: -8)
        ])
        
        // Grid labels are now handled in setupLayoutConstraints()
    }
    
    // MARK: - Button Actions
    
    @objc private func shipButtonTapped(_ sender: UIButton) {
        guard let shipType = shipButtons.first(where: { $0.value == sender })?.key else { return }
        gameModel.selectShip(type: shipType)
        updateShipButtonSelection(selected: shipType)
    }
    
    @objc private func rotateButtonTapped() {
        gameModel.rotateSelectedShip()
    }
    
    @objc private func randomPlaceButtonTapped() {
        gameModel.placeShipsRandomly()
    }
    
    @objc private func startGameButtonTapped() {
        gameModel.startGame()
    }
    
    @objc private func restartButtonTapped() {
        let alert = UIAlertController(title: "New Game", message: "Are you sure you want to start a new game?", preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "New Game", style: .destructive) { _ in
            self.gameModel.restartGame()
            self.setupGridViews()
            self.isShowingHeatmap = false
            self.updateHeatmapButton()
        })
        
        present(alert, animated: true)
    }
    
    @objc private func showHeatmapButtonTapped() {
        isShowingHeatmap.toggle()
        updateHeatmapButton()
        
        if isShowingHeatmap {
            let probabilities = gameModel.getAIProbabilities()
            enemyGridView.showProbabilityHeatmap(probabilities: probabilities)
            AnalyticsManager.shared.trackShowProbabilityHeatmap()
        } else {
            enemyGridView.hideProbabilityHeatmap()
            AnalyticsManager.shared.trackHideProbabilityHeatmap()
        }
    }
    
    @objc private func resetStatsButtonTapped() {
        let alert = UIAlertController(title: "Reset Statistics", message: "Are you sure you want to reset all game statistics?", preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alert.addAction(UIAlertAction(title: "Reset", style: .destructive) { _ in
            self.gameModel.resetAllStats()
        })
        
        present(alert, animated: true)
    }
    
    @objc private func skipTutorialTapped() {
        gameModel.skipTutorial()
        hideTutorial()
    }
    
    // MARK: - UI Updates
    
    private func updateUIForGameState(_ state: GameConstants.GameState) {
        DispatchQueue.main.async {
            switch state {
            case .placingShips:
                self.shipSelectionStackView.isHidden = false
                self.rotateButton.isHidden = false
                self.randomPlaceButton.isHidden = false
                self.startGameButton.isHidden = true
                self.restartButton.isHidden = true
                self.showHeatmapButton.isHidden = true
                
            case .readyToPlay:
                self.shipSelectionStackView.isHidden = true
                self.rotateButton.isHidden = true
                self.randomPlaceButton.isHidden = true
                self.startGameButton.isHidden = false
                self.restartButton.isHidden = false
                self.showHeatmapButton.isHidden = true
                
            case .playerTurn, .computerTurn:
                self.shipSelectionStackView.isHidden = true
                self.rotateButton.isHidden = true
                self.randomPlaceButton.isHidden = true
                self.startGameButton.isHidden = true
                self.restartButton.isHidden = false
                self.showHeatmapButton.isHidden = false
                
            case .gameOver:
                self.restartButton.isHidden = false
                self.showHeatmapButton.isHidden = false
            }
            
            self.enemyGridContainer.alpha = (state == .playerTurn || state == .gameOver) ? 1.0 : 0.6
        }
    }
    
    private func updateShipButtonSelection(selected: GameConstants.ShipType) {
        for (shipType, button) in shipButtons {
            if shipType == selected {
                button.backgroundColor = UIColor.systemGreen
            } else if gameModel.isShipPlaced(shipType) {
                button.backgroundColor = UIColor.systemGray
                button.isEnabled = false
            } else {
                button.backgroundColor = UIColor.systemBlue
                button.isEnabled = true
            }
        }
    }
    
    private func updateShipPlacementStatus(_ shipType: GameConstants.ShipType, isPlaced: Bool) {
        guard let button = shipButtons[shipType] else { return }
        
        DispatchQueue.main.async {
            if isPlaced {
                button.backgroundColor = UIColor.systemGray
                button.isEnabled = false
                button.setTitle("✓", for: .normal)
            } else {
                button.backgroundColor = UIColor.systemBlue
                button.isEnabled = true
                // Restore original button text
                let buttonTitle: String
                switch shipType {
                case .carrier: buttonTitle = "Carrier\n(5)"
                case .battleship: buttonTitle = "Battle\n(4)"
                case .destroyer: buttonTitle = "Destroy\n(3)"
                case .submarine: buttonTitle = "Sub\n(3)"
                case .patrolboat: buttonTitle = "Patrol\n(2)"
                }
                button.setTitle(buttonTitle, for: .normal)
            }
        }
    }
    
    private func updateStatsDisplay(_ stats: GameStats) {
        DispatchQueue.main.async {
            self.gamesWonLabel.text = "Won: \(stats.winLossRecord)"
            self.accuracyLabel.text = "Acc: \(String(format: "%.0f", stats.overallAccuracy))%"
            self.currentShotsLabel.text = "\(stats.shotsTaken)/\(stats.shotsHit)"
        }
    }
    
    private func updateHeatmapButton() {
        DispatchQueue.main.async {
            self.showHeatmapButton.setTitle(self.isShowingHeatmap ? "Hide\nHeatmap" : "Show\nHeatmap", for: .normal)
            self.showHeatmapButton.backgroundColor = self.isShowingHeatmap ? UIColor.systemRed : UIColor.systemGray
        }
    }
    
    private func updateMessage(_ message: String) {
        DispatchQueue.main.async {
            self.messageLabel.text = message
            
            // Animate message update
            UIView.transition(with: self.messageLabel, duration: 0.3, options: .transitionCrossDissolve, animations: {
                self.messageLabel.alpha = 1.0
            }, completion: nil)
        }
    }
    
    private func showTutorial(message: String) {
        DispatchQueue.main.async {
            self.tutorialLabel.text = message
            self.tutorialView.isHidden = false
            
            UIView.animate(withDuration: 0.3) {
                self.tutorialView.alpha = 1.0
            }
        }
    }
    
    private func hideTutorial() {
        DispatchQueue.main.async {
            UIView.animate(withDuration: 0.3, animations: {
                self.tutorialView.alpha = 0.0
            }) { _ in
                self.tutorialView.isHidden = true
            }
        }
    }
    
    private func showGameEndAlert(playerWon: Bool) {
        let title = playerWon ? "Victory!" : "Defeat"
        let message = playerWon ? "Congratulations! You defeated the AI!" : "The AI has defeated you. Better luck next time!"
        
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        if gameModel.stats.shouldShowTip() {
            let tip = gameModel.stats.getRandomTip()
            alert.message = "\(message)\n\nTip: \(tip)"
        }
        
        alert.addAction(UIAlertAction(title: "New Game", style: .default) { _ in
            self.gameModel.restartGame()
            self.setupGridViews()
        })
        
        alert.addAction(UIAlertAction(title: "View Stats", style: .default) { _ in
            self.showStatsDetail()
        })
        
        present(alert, animated: true)
    }
    
    private func showStatsDetail() {
        let stats = gameModel.stats
        let performance = stats.getPerformanceLevel()
        
        let message = """
        Performance Level: \(performance)
        
        Current Game:
        • Shots Taken: \(stats.shotsTaken)
        • Hits: \(stats.shotsHit)
        • Accuracy: \(String(format: "%.1f", stats.currentAccuracy))%
        
        Overall Statistics:
        • Games Played: \(stats.gamesPlayed)
        • Games Won: \(stats.gamesWon)
        • Win Rate: \(String(format: "%.1f", stats.winPercentage))%
        • Total Shots: \(stats.totalShots)
        • Total Hits: \(stats.totalHits)
        • Overall Accuracy: \(String(format: "%.1f", stats.overallAccuracy))%
        """
        
        let alert = UIAlertController(title: "Detailed Statistics", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

// MARK: - GridViewDelegate

extension GameViewController: GridViewDelegate {
    func gridView(_ gridView: GridView, didTapCellAt x: Int, y: Int) {
        if gridView == playerGridView {
            // Player grid - ship placement
            if gameModel.gameState == .placingShips {
                let success = gameModel.placeShip(at: x, y: y)
                if success {
                    playerGridView.updateDisplay()
                }
            }
        } else if gridView == enemyGridView {
            // Enemy grid - shooting
            if gameModel.gameState == .playerTurn {
                let success = gameModel.playerShoot(at: x, y: y)
                if success {
                    enemyGridView.animateCell(x: x, y: y) {
                        self.enemyGridView.updateDisplay()
                        
                        // Update heatmap if showing
                        if self.isShowingHeatmap {
                            let probabilities = self.gameModel.getAIProbabilities()
                            self.enemyGridView.showProbabilityHeatmap(probabilities: probabilities)
                        }
                    }
                }
            }
        }
    }
    
    func gridView(_ gridView: GridView, didLongPressCellAt x: Int, y: Int) {
        // Long press for ship placement preview
        if gridView == playerGridView && gameModel.gameState == .placingShips {
            if let selectedType = gameModel.selectedShipType {
                // Show preview for ship placement
                let canPlace = gameModel.canPlaceShip(type: selectedType, at: x, y: y, direction: gameModel.selectedShipDirection)
                playerGridView.showShipPreview(at: x, y: y, size: selectedType.size, direction: gameModel.selectedShipDirection, isValid: canPlace)
                
                // Clear preview after delay
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                    self.playerGridView.clearShipPreview()
                    self.playerGridView.updateDisplay()
                }
            }
        }
    }
}

// MARK: - GameModelDelegate

extension GameViewController: GameModelDelegate {
    func gameModel(_ gameModel: GameModel, didUpdateState state: GameConstants.GameState) {
        updateUIForGameState(state)
        
        // Update grid displays
        DispatchQueue.main.async {
            self.playerGridView.updateDisplay()
            self.enemyGridView.updateDisplay()
        }
    }
    
    func gameModel(_ gameModel: GameModel, didUpdateStats stats: GameStats) {
        updateStatsDisplay(stats)
    }
    
    func gameModel(_ gameModel: GameModel, didReceiveMessage message: String) {
        updateMessage(message)
        
        // Show tutorial if needed
        if let tutorialMessage = gameModel.getCurrentTutorialMessage() {
            showTutorial(message: tutorialMessage)
        } else {
            hideTutorial()
        }
    }
    
    func gameModel(_ gameModel: GameModel, gameDidEnd playerWon: Bool) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.showGameEndAlert(playerWon: playerWon)
        }
    }
    
    func gameModel(_ gameModel: GameModel, didUpdateShipPlacement ship: GameConstants.ShipType, isPlaced: Bool) {
        updateShipPlacementStatus(ship, isPlaced: isPlaced)
    }
} 