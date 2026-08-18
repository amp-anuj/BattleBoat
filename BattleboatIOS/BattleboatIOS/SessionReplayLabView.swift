import SwiftUI
import UIKit
import AmplitudeSessionReplay

// ---------------------------------------------------------------------------
// Amplitude brand palette — mirrors res/values/colors.xml (Android) and the
// iOS GameConstants so this SwiftUI lab matches the rest of the app.
// ---------------------------------------------------------------------------
private extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0
        )
    }
    static let ampNavy = Color(hex: 0x0D1330)
    static let ampSurface = Color(hex: 0x1D2433)
    static let ampSurface2 = Color(hex: 0x252E45)
    static let ampSurface3 = Color(hex: 0x2F3A55)
    static let ampBlue = Color(hex: 0x1352CC)
    static let ampBlueLight = Color(hex: 0x3986F7)
    static let ampCoral = Color(hex: 0xE8410E)
    static let ampTeal = Color(hex: 0x00C2A8)
    static let ampLavender = Color(hex: 0x9164FA)
    static let ampTextPrimary = Color(hex: 0xE8EDF5)
    static let ampTextSecondary = Color(hex: 0x8B9DB5)
    static let ampBorder = Color(hex: 0x26314A)
    static let ampWater = Color(hex: 0x394B75)
}

/// SwiftUI Session Replay test bed — the iOS counterpart to the Android Compose
/// SR Lab. Exercises the SwiftUI surfaces that matter for Session Replay capture
/// (scrolling lists, sheets, alerts, text fields, blocked views, custom Canvas,
/// animation) themed around Battleboat. Presented from GameViewController via a
/// UIHostingController.
struct SessionReplayLabView: View {

    var onClose: () -> Void

    private let analytics = AnalyticsManager.shared

    @State private var email = "admiral@battleboat.io"
    @State private var passphrase = "secret-passphrase"
    @State private var passphraseVisible = false
    @State private var notes = "Enemy carrier last seen near G7."
    @State private var showHiddenRow = true
    @State private var shotCount = 0
    @State private var selectedOpponent = "Nelson"
    @State private var progress: Double = 0.2
    @State private var showSheet = false
    @State private var showDialog = false
    @State private var showFlushConfirm = false

    private let opponents = [
        "Nelson", "Yamamoto", "Nimitz", "Drake", "Halsey",
        "Rodney", "Togo", "Farragut", "Spruance", "Cunningham",
        "Doenitz", "Beatty", "Zheng He", "Barbarossa", "Themistocles"
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    identityCard
                    interactionsSection
                    textInputsSection
                    privacySection
                    sheetDialogSection
                    canvasSection
                    rosterSection
                    flushSection
                    Spacer(minLength: 24)
                }
                .padding(16)
            }
            .background(Color.ampNavy.ignoresSafeArea())
            .navigationTitle("Session Replay Lab")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onClose) {
                        Image(systemName: "xmark")
                    }
                    .tint(.ampTextPrimary)
                }
                ToolbarItem(placement: .principal) {
                    VStack(spacing: 2) {
                        Text("Session Replay Lab")
                            .font(.headline).foregroundColor(.ampTextPrimary)
                        Text("100% sample · remote config \(analytics.isSessionReplayRemoteConfigEnabled() ? "ON" : "off")")
                            .font(.caption2).foregroundColor(.ampTextSecondary)
                    }
                }
            }
            .toolbarBackground(Color.ampSurface, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
        }
        .task {
            // Animate the "enemy fleet sunk" ring so there are intermediate
            // frames to look for in the replay.
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_500_000_000)
                withAnimation(.easeInOut(duration: 0.6)) {
                    progress = progress >= 0.95 ? 0.15 : progress + 0.12
                }
            }
        }
        .sheet(isPresented: $showSheet) { deployFleetSheet }
        .alert("Replay check", isPresented: $showDialog) {
            Button("Got it", role: .cancel) { }
        } message: {
            Text("If this alert is missing or wrong in the replay, that matches known SR capture gaps.")
        }
        .alert("Flushed", isPresented: $showFlushConfirm) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("Flushed analytics + session replay.")
        }
    }

    // MARK: - Identity

    private var identityCard: some View {
        LabCard {
            HStack {
                Image(systemName: "star.fill").foregroundColor(.ampTeal)
                Text("Battleboat SwiftUI capture test")
                    .font(.subheadline).fontWeight(.semibold)
                    .foregroundColor(.ampTextPrimary)
            }
            Spacer().frame(height: 10)
            IdentityLine(label: "Recording", value: analytics.isSessionReplayActive() ? "YES" : "NO")
            IdentityLine(label: "Mask (local)", value: analytics.getSessionReplayLocalMaskLevel())
            IdentityLine(
                label: "Remote config",
                value: analytics.isSessionReplayRemoteConfigEnabled() ? "ON — overrides local" : "off"
            )
            IdentityLine(label: "Device ID", value: analytics.getDeviceId() ?? "(pending)")
            IdentityLine(label: "Session ID", value: String(analytics.getSessionId()))
            IdentityLine(label: "SDK", value: "AmplitudeSessionReplay-iOS")
            Text(analytics.getSessionReplayStatus())
                .font(.caption2).foregroundColor(.ampTextSecondary)
                .padding(.top, 8)
            if analytics.isSessionReplayRemoteConfigEnabled() {
                Text("⚠︎ Effective mask is set by the project's remote SR config and isn't reported by the SDK — check Session Replay settings in Amplitude. That's why text can appear unmasked despite the local 'conservative' request.")
                    .font(.caption2).foregroundColor(.ampCoral)
                    .padding(.top, 4)
            }
        }
    }

    // MARK: - Sections

    private var interactionsSection: some View {
        LabCard {
            SectionTitle("1. Interactions & animation")
            SectionBody("Fire shots and watch the enemy-fleet ring animate — check whether intermediate frames appear in the replay.")
            HStack(spacing: 12) {
                Button("Fire shot (\(shotCount))") { shotCount += 1 }
                    .buttonStyle(.borderedProminent)
                    .tint(.ampBlue)
                ProgressRing(progress: progress)
                    .frame(width: 40, height: 40)
                Text("\(Int(progress * 100))% sunk")
                    .font(.system(.body, design: .monospaced))
                    .foregroundColor(.ampTextPrimary)
            }
        }
    }

    private var textInputsSection: some View {
        LabCard {
            SectionTitle("2. Text & inputs")
            SectionBody("Whether these fields are masked depends on the effective mask level. With remote config ON, the project's remote SR config governs masking — the local value is not applied, so text you expect masked may be visible.")
            TextField("Admiral email", text: $email)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
            HStack {
                Group {
                    if passphraseVisible {
                        TextField("Fleet passphrase", text: $passphrase)
                    } else {
                        SecureField("Fleet passphrase", text: $passphrase)
                    }
                }
                .textFieldStyle(.roundedBorder)
                Button {
                    passphraseVisible.toggle()
                } label: {
                    Image(systemName: passphraseVisible ? "eye.slash" : "eye")
                }
                .tint(.ampTextSecondary)
            }
            TextField("Battle notes", text: $notes, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(2...4)
        }
    }

    private var privacySection: some View {
        LabCard {
            SectionTitle("3. Privacy — blocking")
            SectionBody("iOS SR captures the UIKit layer tree. A SwiftUI view is not a discrete UIView, so the SwiftUI .amp_setBlocked() modifier is NOT honored in this SR version — the first box still shows in the replay. The reliable path is a UIKit-backed view with amp_isBlocked = true (second box).")

            Text("① SwiftUI .amp_setBlocked() — NOT captured (known gap)")
                .font(.caption2).foregroundColor(.ampCoral)
            Text("Blocked enemy fleet layout — still visible in the replay")
                .frame(maxWidth: .infinity, minHeight: 44)
                .padding(12)
                .background(Color.ampCoral.opacity(0.16))
                .cornerRadius(8)
                .foregroundColor(.ampTextPrimary)
                .amp_setBlocked(true)

            Text("② UIKit amp_isBlocked via UIViewRepresentable — reliable")
                .font(.caption2).foregroundColor(.ampTeal)
                .padding(.top, 4)
            BlockedRegion(text: "Blocked enemy fleet layout — should be a placeholder")
                .frame(maxWidth: .infinity, minHeight: 44)

            Text("Appear / disappear capture test")
                .font(.caption2).foregroundColor(.ampTextSecondary)
                .padding(.top, 4)
            Button {
                withAnimation(.easeInOut) { showHiddenRow.toggle() }
            } label: {
                HStack {
                    Image(systemName: showHiddenRow ? "eye.slash" : "eye")
                    Text(showHiddenRow ? "Hide detail card" : "Show detail card")
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent).tint(.ampBlue)
            if showHiddenRow {
                HStack {
                    Image(systemName: "flag.fill").foregroundColor(.ampTeal)
                    Text("Flagship anchored at D4")
                        .foregroundColor(.ampTextPrimary)
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.ampSurface2)
                .cornerRadius(8)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }

    private var sheetDialogSection: some View {
        LabCard {
            SectionTitle("4. Modal sheet & alert")
            SectionBody("Sheets and alerts are overlay surfaces that can be missed or mis-captured in the replay.")
            HStack(spacing: 8) {
                Button("Open sheet") { showSheet = true }
                    .buttonStyle(.borderedProminent).tint(.ampBlue)
                Button("Open alert") { showDialog = true }
                    .buttonStyle(.bordered).tint(.ampTextSecondary)
            }
        }
    }

    private var canvasSection: some View {
        LabCard {
            SectionTitle("5. Custom Canvas / graphics")
            SectionBody("Custom-drawn content often needs bitmap capture — verify the battle grid appears in the replay.")
            BattleGrid()
                .frame(height: 140)
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private var rosterSection: some View {
        LabCard {
            SectionTitle("6. Scrollable opponent roster")
            SectionBody("Scroll the list, then flush — check whether scroll position / items are captured.")
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(opponents.prefix(4), id: \.self) { opponent in
                        Text(opponent)
                            .font(.footnote)
                            .padding(.horizontal, 12).padding(.vertical, 6)
                            .background(selectedOpponent == opponent ? Color.ampBlue.opacity(0.28) : Color.ampSurface2)
                            .foregroundColor(.ampTextPrimary)
                            .clipShape(Capsule())
                            .onTapGesture { selectedOpponent = opponent }
                    }
                }
            }
            ScrollView {
                VStack(spacing: 6) {
                    ForEach(opponents, id: \.self) { opponent in
                        OpponentRow(
                            opponent: opponent,
                            selected: opponent == selectedOpponent
                        )
                        .onTapGesture { selectedOpponent = opponent }
                    }
                }
            }
            .frame(height: 220)
            .overlay(
                RoundedRectangle(cornerRadius: 10).stroke(Color.ampBorder, lineWidth: 1)
            )
        }
    }

    private var flushSection: some View {
        LabCard {
            SectionTitle("7. Flush & verify")
            SectionBody("Interact above, then flush. Find this session in Amplitude with the device/session IDs in the header.")
            Button {
                analytics.flushEvents()
                analytics.flushSessionReplay()
                showFlushConfirm = true
            } label: {
                Text("Flush analytics + Session Replay")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent).tint(.ampBlue)
        }
    }

    private var deployFleetSheet: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Deploy Fleet").font(.title3).fontWeight(.bold)
            Text("Selected opponent: \(selectedOpponent)")
            Text("Wager: 500 gold")
            Button {
                showSheet = false
            } label: {
                Text("Confirm deployment").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent).tint(.ampBlue)
            .padding(.top, 8)
            Spacer()
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .foregroundColor(.ampTextPrimary)
        .background(Color.ampSurface.ignoresSafeArea())
        .presentationDetents([.medium])
    }
}

// MARK: - Reusable pieces

private struct LabCard<Content: View>: View {
    @ViewBuilder var content: () -> Content
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color.ampSurface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct SectionTitle: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text).font(.headline).foregroundColor(.ampTextPrimary)
    }
}

private struct SectionBody: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text).font(.footnote).foregroundColor(.ampTextSecondary)
    }
}

private struct IdentityLine: View {
    let label: String
    let value: String
    var body: some View {
        HStack(alignment: .top) {
            Text(label)
                .font(.caption).foregroundColor(.ampTextSecondary)
                .frame(width: 92, alignment: .leading)
            Text(value)
                .font(.system(.caption, design: .monospaced))
                .foregroundColor(.ampTextPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct OpponentRow: View {
    let opponent: String
    let selected: Bool
    var body: some View {
        HStack {
            ZStack {
                Circle()
                    .fill(selected ? Color.ampTeal : Color.ampSurface3)
                    .frame(width: 28, height: 28)
                Text(String(opponent.prefix(1)))
                    .font(.caption).fontWeight(.bold)
                    .foregroundColor(selected ? .ampNavy : .ampTextPrimary)
            }
            Text(opponent).foregroundColor(.ampTextPrimary)
            Spacer()
        }
        .padding(.horizontal, 12).padding(.vertical, 10)
        .background(selected ? Color.ampBlue.opacity(0.28) : Color.ampNavy)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

/// A UIKit-backed region marked with `amp_isBlocked = true`. Because this bridges
/// a real UIView into the SwiftUI hierarchy, iOS Session Replay's UIKit capture
/// can see it and replace it with a placeholder — unlike the SwiftUI-only
/// `.amp_setBlocked()` modifier, which has no discrete UIView to mark.
private struct BlockedRegion: UIViewRepresentable {
    let text: String

    func makeUIView(context: Context) -> UIView {
        let container = UIView()
        container.backgroundColor = UIColor(red: 0.91, green: 0.25, blue: 0.05, alpha: 0.16)
        container.layer.cornerRadius = 8
        container.amp_isBlocked = true

        let label = UILabel()
        label.text = text
        label.numberOfLines = 0
        label.textColor = UIColor(red: 0.91, green: 0.93, blue: 0.96, alpha: 1.0)
        label.font = .systemFont(ofSize: 15)
        label.translatesAutoresizingMaskIntoConstraints = false
        container.addSubview(label)
        NSLayoutConstraint.activate([
            label.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: 12),
            label.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -12),
            label.topAnchor.constraint(equalTo: container.topAnchor, constant: 12),
            label.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -12)
        ])
        return container
    }

    func updateUIView(_ uiView: UIView, context: Context) {}
}

private struct ProgressRing: View {
    let progress: Double
    var body: some View {
        ZStack {
            Circle().stroke(Color.ampSurface3, lineWidth: 6)
            Circle()
                .trim(from: 0, to: progress)
                .stroke(Color.ampCoral, style: StrokeStyle(lineWidth: 6, lineCap: .round))
                .rotationEffect(.degrees(-90))
        }
    }
}

/// A small Battleboat target grid drawn with SwiftUI Canvas — the custom-draw
/// surface for the Session Replay bitmap-capture test. 10x10 sea grid with a
/// few hits (coral) and misses (blue).
private struct BattleGrid: View {
    var body: some View {
        Canvas { context, size in
            let cells = 10
            let cell = min(size.width, size.height) / CGFloat(cells)
            let originX = (size.width - cell * CGFloat(cells)) / 2
            let originY = (size.height - cell * CGFloat(cells)) / 2

            var grid = Path()
            for i in 0...cells {
                let x = originX + CGFloat(i) * cell
                let y = originY + CGFloat(i) * cell
                grid.move(to: CGPoint(x: x, y: originY))
                grid.addLine(to: CGPoint(x: x, y: originY + cell * CGFloat(cells)))
                grid.move(to: CGPoint(x: originX, y: y))
                grid.addLine(to: CGPoint(x: originX + cell * CGFloat(cells), y: y))
            }
            context.stroke(grid, with: .color(.ampBorder), lineWidth: 1.5)

            func mark(_ col: Int, _ row: Int, _ color: Color, filled: Bool) {
                let c = CGPoint(
                    x: originX + (CGFloat(col) + 0.5) * cell,
                    y: originY + (CGFloat(row) + 0.5) * cell
                )
                let r = cell * (filled ? 0.28 : 0.24)
                let rect = CGRect(x: c.x - r, y: c.y - r, width: r * 2, height: r * 2)
                let circle = Path(ellipseIn: rect)
                if filled {
                    context.fill(circle, with: .color(color))
                } else {
                    context.stroke(circle, with: .color(color), lineWidth: 2)
                }
            }
            mark(2, 3, .ampCoral, filled: true)
            mark(3, 3, .ampCoral, filled: true)
            mark(6, 5, .ampCoral, filled: true)
            mark(5, 8, .ampBlueLight, filled: false)
            mark(8, 1, .ampBlueLight, filled: false)
        }
        .background(Color.ampWater)
    }
}
