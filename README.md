# Battleboat — Amplitude Instrumentation Test Bed

A multi-platform implementation of the classic Battleship game used as a test bed for [Amplitude](https://amplitude.com) analytics instrumentation. All platforms share the same Amplitude project and event schema, making it easy to compare SDK behavior, test new features, and validate instrumentation across Web, iOS, and Android.

## Purpose

This repo exists to test and demonstrate Amplitude's full suite of SDKs and features in a realistic app context. The Battleship game provides natural user interactions (ship placement, shooting, winning/losing) that produce meaningful analytics events without requiring a production app.

Use it to:
- Prototype and validate Amplitude instrumentation patterns
- Test new SDK features across platforms (Session Replay, Guides & Surveys, Experiment)
- Demonstrate cross-platform identity linking (native app → WebView)
- Reference working code examples for each Amplitude SDK

## Platforms

| Platform | Folder | Purpose |
|----------|--------|---------|
| **Web** | _(root)_ | Browser SDK baseline; also served as a WebView inside iOS and Android |
| **iOS (Full Game)** | `BattleboatIOS/` | Full-featured iOS game; most complete Amplitude instrumentation |
| **Android (Full Game)** | `BattleboatAndroid/` | Full-featured Android game; mirrors iOS instrumentation |
| **iOS (Sample App)** | `AmplitudeSampleIOS/` | Minimal clean-room iOS reference for SDK setup |

## Amplitude Features Tested

| Feature | Web | iOS Game | Android Game | iOS Sample |
|---------|-----|----------|--------------|------------|
| Analytics (event tracking) | ✅ | ✅ | ✅ | ✅ |
| Session Replay | ✅ | ✅ | ⚠️ disabled* | — |
| Guides & Surveys | ✅ | ✅ | ✅ | ✅ |
| Guides & Surveys Callbacks | — | ✅ | ✅ | — |
| Experiment (Feature Flags) | ✅ | ✅ | — | — |
| Autocapture (sessions, lifecycle) | ✅ | ✅ | ✅ | — |
| Frustration Interactions | ✅ | ✅ | ✅ | — |
| Network Tracking | — | ✅ | ✅ | — |
| WebView Identity Linking | ✅ | ✅ | ✅ | — |

_* Android Session Replay is disabled pending stable Jetpack Compose support from Amplitude._

## Shared Event Schema

All platforms fire the same core game events:

| Event | Properties |
|-------|-----------|
| `Game Started` | — |
| `Game Ended` | `Win` (bool), `Shots Taken` (int) |
| `Shot Fired` | `X`, `Y`, `Hit` (bool), `Player` (human/computer), `Consecutive Hits` |
| `Ship Selected` | `Ship` (carrier/battleship/destroyer/submarine/patrolboat) |
| `Ship Placed` | `Ship`, `X`, `Y`, `Success` (bool) |
| `Ship Rotated` | `Ship` |

## Amplitude Project

All platforms use the same Amplitude project (`909b2239fab57efd4268eb75dbc28d30`). The web app is the primary surface; iOS and Android pass their device/session IDs into the WebView so all activity is stitched to the same user.

## Web App

The web app lives at the repo root and is hosted on GitHub Pages. It is also loaded inside a WebView in both native apps for cross-platform identity testing.

**Key files:**
- `index.html` — Amplitude SDK initialization, identity linking from URL params
- `js/battleboat.js` — game logic + event tracking
- `css/styles.css` — game UI

**Running locally:**
```bash
# Any static file server works, e.g.:
npx serve .
# Then open http://localhost:3000
```

## Platform READMEs

- [BattleboatIOS](BattleboatIOS/README.md) — iOS full game with all Amplitude features
- [BattleboatAndroid](BattleboatAndroid/README.md) — Android full game
- [AmplitudeSampleIOS](AmplitudeSampleIOS/README.md) — minimal iOS SDK reference

## Original Game

This project is based on [Battleboat.js](https://github.com/billmei/battleboat) by Bill Mei — a JavaScript AI that beats humans at Battleship.
