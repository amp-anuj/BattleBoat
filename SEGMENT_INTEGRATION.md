# Segment + Amplitude Integration - Web Project (_segment)

This document describes the Segment analytics, Amplitude Session Replay, and Amplitude Guides & Surveys integration for the `_segment` version of the Battleboat web project.

## Overview

The `_segment` version of the web project uses:
- **Segment** for analytics tracking (events, user identification, page views)
- **Amplitude Session Replay Standalone SDK** for session recording
- **Amplitude Guides & Surveys SDK** for in-product messaging and surveys

This hybrid approach allows you to:
- Send analytics data to multiple destinations through Segment
- Capture session replays in Amplitude with the `[Amplitude] Session Replay ID` property
- Display guides and surveys triggered by Segment events
- Personalize guides and surveys with user properties from Segment

## Files Modified

### 1. `index_segment.html`
- **Segment Analytics**: Added Segment snippet for event tracking
  - Write Key: `tHUX88CH9RI7gvquAoaMXjK1MSvZC0AY`
  - Includes `analytics.page()` call for automatic page tracking
- **Amplitude Session Replay Standalone SDK**: Added for session recording
  - Version: `1.29.2`
  - CDN: `https://cdn.amplitude.com/libs/session-replay-browser-1.29.2-min.js.gz`
  - API Key: `49a3ecaa7ea5305f3bdd5b2014480f8b`
  - Sample Rate: `1` (100% capture)
  - **Device ID**: Retrieved from Segment's `analytics.user().anonymousId()`
  - **Session ID**: Retrieved from `analytics_session_id` cookie (30-minute timeout)
- **Amplitude Guides & Surveys SDK**: Added for in-product messaging
  - CDN: `https://cdn.amplitude.com/script/49a3ecaa7ea5305f3bdd5b2014480f8b.engagement.js`
  - API Key: `49a3ecaa7ea5305f3bdd5b2014480f8b` (same as Session Replay)
  - **Event Forwarding**: Segment events automatically forwarded to Guides & Surveys
  - **User Properties**: Shared between Segment and Guides & Surveys
- **User Identification**: Combined identify call with properties:
  - `Game: 'BattleBoat'`
  - `Sportsbook Personalization` array

### 2. `js/battleboat_segment.js`
- Replaced all `amplitude.track()` calls with `analytics.track()`
- **Added Session Replay properties to all events** using `window.getSessionReplayProps()`
- Events now automatically include `[Amplitude] Session Replay ID` property
- Updated device ID retrieval to use `analytics.user().anonymousId()`

### 3. `css/styles_segment.css`
- Unchanged (identical copy of original styles)

## Segment Configuration

### Write Key
```
tHUX88CH9RI7gvquAoaMXjK1MSvZC0AY
```

### Initialization
The Segment snippet is loaded in the `<head>` section of `index_segment.html` and automatically:
- Loads the Segment Analytics.js library
- Calls `analytics.page()` on initial page load
- Makes the `analytics` object available globally

## Events Tracked

All events use Segment's `track` call following the format:
```javascript
analytics.track('Event Name', {
  property1: 'value1',
  property2: 'value2'
});
```

### Event List

| Event Name | Properties | When Triggered |
|------------|------------|----------------|
| **Game Initialized** | None | When game object is created |
| **Game Ended** | `Win` (boolean), `Shots Taken` (number) | When game ends (win or lose) |
| **Shot Fired** | `X` (number), `Y` (number), `Hit` (boolean), `Consecutive Hits` (number) | When player shoots |
| **Ship Selected** | `Ship` (string), `Ship Type` (string) | When player selects a ship to place |
| **Ship Placed** | `Ship` (string), `Success` (boolean), `X` (number), `Y` (number) | When player places a ship |
| **Ship Rotated** | `Ship` (string) | When player rotates a ship |
| **Game Started** | None | When player clicks "Start Game" |

## User Identification

User identification is called once on page load with the following traits:

```javascript
analytics.identify({
  Game: 'BattleBoat',
  'Sportsbook Personalization': [
    {
      "Is Personalized": "false",
      "Module": "popular"
    },
    {
      "Is Personalized": "true",
      "Module": "page_tabs"
    }
  ]
});
```

## Page Tracking

Automatic page view tracking is enabled via `analytics.page()` called in the Segment snippet.

## ID Management

### Device ID
The device ID is **always retrieved from Segment** using:
```javascript
analytics.user().anonymousId()
```

This ensures consistency between Segment events and Amplitude Session Replay. The anonymous ID is also displayed in the tagline element after a 500ms delay.

### Session ID
The session ID is **retrieved from the `analytics_session_id` cookie**:
- If the cookie exists, it's used as the session ID
- If it doesn't exist, a new timestamp-based ID is created and stored
- Session timeout: 30 minutes
- Format: Unix timestamp in milliseconds (e.g., `1698765432123`)

This approach ensures:
✅ Session continuity across page refreshes  
✅ Proper session boundaries (30-min timeout)  
✅ Consistency between analytics tracking and session replay  
✅ No creation of duplicate session IDs

## Amplitude Session Replay Integration

### How It Works

This integration uses the **Amplitude Session Replay Standalone SDK**, which is designed for 3rd party analytics providers like Segment. Here's how it works:

1. **Segment** handles all event tracking and user identification
2. **Amplitude Session Replay** independently captures session recordings
3. Each event sent through Segment includes `[Amplitude] Session Replay ID` property
4. Amplitude links the replay to events using this ID

### Session Replay Properties

Every tracked event now includes:
```javascript
{
  "[Amplitude] Session Replay ID": "6eb24f81-a106-45b0-879c-65248d7b8911/1710374872575"
}
```

This property is automatically added by the `window.getSessionReplayProps()` helper function.

### Implementation Details

```javascript
// Helper function to get session ID from cookie
function getStoredSessionId() {
  var sessionId = getCookie("analytics_session_id");
  
  if (!sessionId) {
    // Create new session ID (timestamp in milliseconds)
    sessionId = Date.now();
    // Store for 30 minutes
    setCookie("analytics_session_id", sessionId, 30 / 1440);
  } else {
    sessionId = parseInt(sessionId, 10);
  }
  
  return sessionId;
}

// Initialize Session Replay after Segment is ready
analytics.ready(function() {
  // Get device ID from Segment
  var deviceId = analytics.user().anonymousId();
  
  // Get session ID from cookie
  var sessionId = getStoredSessionId();
  
  // Initialize Amplitude Session Replay
  window.sessionReplay.init(AMPLITUDE_API_KEY, {
    deviceId: deviceId,
    sessionId: sessionId,
    sampleRate: 1
  });
});

// Track events with replay properties
var replayProps = window.getSessionReplayProps();
analytics.track('Event Name', Object.assign({
  // Your event properties
}, replayProps));
```

**Key Points:**
- Device ID is retrieved from Segment (not created)
- Session ID is retrieved from `analytics_session_id` cookie (not created on-the-fly)
- Session Replay initializes only after Segment is ready
- 30-minute session timeout aligns with typical analytics practices

### Viewing Replays

1. Log in to [Amplitude Dashboard](https://analytics.amplitude.com/)
2. Navigate to Session Replay section
3. Replays are linked to events via `[Amplitude] Session Replay ID`
4. You can view replays even though events come through Segment

## Amplitude Guides & Surveys Integration

### How It Works

The [Guides & Surveys SDK works with third-party analytics providers](https://amplitude.com/docs/guides-and-surveys/sdk#other-amplitude-sdks-and-third-party-analytics-providers) like Segment through manual initialization:

1. **Segment** tracks all events and user properties
2. **Guides & Surveys** listens for events via `forwardEvent()`
3. Events tracked through Segment can trigger guides and surveys
4. User properties from Segment are available for personalization

### Implementation Details

```javascript
// Initialize Guides and Surveys
engagement.init(AMPLITUDE_API_KEY, {
  serverZone: "US",
  logLevel: 2
});

// Boot after Segment is ready
analytics.ready(function() {
  var deviceId = analytics.user().anonymousId();
  var sessionId = getStoredSessionId();
  
  // Boot with Segment device/session IDs
  engagement.boot({
    userId: null,
    deviceId: deviceId,
    sessionId: sessionId,
    userProperties: {
      Game: 'BattleBoat'
    }
  });
  
  // Forward Segment events to Guides & Surveys
  analytics.on('track', function(event, properties) {
    engagement.forwardEvent({
      event_type: event,
      event_properties: properties || {}
    });
  });
});

// Set user properties for personalization
engagement._setUserProperties(userProperties);
```

### Event Forwarding

All Segment events are automatically forwarded to Guides & Surveys:
- `Game Initialized` → Can trigger guides
- `Shot Fired` → Can trigger surveys
- `Game Ended` → Can trigger feedback surveys
- And all other custom events

### User Properties for Personalization

User properties are shared with Guides & Surveys for content personalization:
```javascript
var userProperties = {
  Game: 'BattleBoat',
  'Sportsbook Personalization': [...]
};

// Set in both Segment and Guides & Surveys
analytics.identify(userProperties);
engagement._setUserProperties(userProperties);
```

You can use these in guides/surveys with `{{ properties.propertyName }}` syntax.

### Viewing Guides & Surveys

1. Log in to [Amplitude Dashboard](https://analytics.amplitude.com/)
2. Navigate to Guides & Surveys section
3. Create guides and surveys with event-based triggers
4. Preview and publish to your users

## Differences from Amplitude Version

### Maintained Features
- ✅ **Session Replay** (via Standalone SDK)
- ✅ **Guides & Surveys** (via manual init/boot with event forwarding)
- ✅ All game events tracked with same names and properties
- ✅ User identification with traits
- ✅ Page view tracking
- ✅ Same game logic and UI

### Changed Implementation
- 🔄 Event tracking via Segment instead of Amplitude SDK
- 🔄 Session Replay via Standalone SDK instead of Browser Plugin
- 🔄 Guides & Surveys via manual `init()` and `boot()` instead of `amplitude.add()`
- 🔄 Event forwarding via `analytics.on('track')` to Guides & Surveys
- 🔄 Device ID retrieved from Segment's `analytics.user().anonymousId()`
- 🔄 Session ID retrieved from `analytics_session_id` cookie (30-min timeout)

### Removed Features
The following Amplitude-specific features are **NOT** available in the Segment version:
- ❌ Amplitude Experiments
- ❌ Automatic event forwarding (requires manual setup)

## Destinations

With Segment, you can route this data to multiple destinations:
- Amplitude (via Segment destination)
- Google Analytics
- Mixpanel
- Customer.io
- And 300+ other integrations

Configure destinations in the Segment dashboard at:
https://app.segment.com/

## Testing

To test the integration:

1. **Open Browser Console**
   - Navigate to `index_segment.html`
   - Open browser developer tools (F12)

2. **Check Analytics Objects**
   ```javascript
   // Check Segment Device ID
   console.log('Device ID:', analytics.user().anonymousId())
   
   // Check Session ID from cookie
   console.log('Session ID Cookie:', document.cookie)
   
   // Check Session Replay
   console.log('Session Replay:', window.sessionReplay)
   console.log('Session Replay Props:', window.getSessionReplayProps())
   
   // Check Guides & Surveys
   console.log('Guides & Surveys:', window.engagement)
   console.log('G&S Debug Status:', window.engagement._debugStatus())
   ```
   
   **Verify:**
   - Device ID matches across Segment, Session Replay, and Guides & Surveys
   - `analytics_session_id` cookie exists in document.cookie
   - Session Replay Properties include `[Amplitude] Session Replay ID`
   - `engagement._debugStatus()` shows:
     - `stateInitialized: true`
     - `decideSuccessful: true`
     - `user` object is present with correct deviceId

3. **Play the Game**
   - Select and place ships
   - Fire shots
   - Complete a game

4. **Verify in Segment Debugger**
   - Go to https://app.segment.com/
   - Navigate to Connections → Sources → JavaScript
   - Check the Debugger tab to see real-time events
   - **Look for `[Amplitude] Session Replay ID` property in events**

5. **Verify Session Replay in Amplitude**
   - Go to https://analytics.amplitude.com/
   - Navigate to Session Replay
   - Find your session (may take a few minutes to process)
   - Verify the replay is captured and linked to events

6. **Verify Guides & Surveys**
   - Go to https://analytics.amplitude.com/
   - Navigate to Guides & Surveys
   - Create a test guide or survey with an event trigger (e.g., "Game Initialized")
   - Refresh the game page to trigger the guide/survey
   - **Note**: Guides & Surveys must be enabled in project settings

## Migration Notes

### From Amplitude to Segment

If you want to migrate data from the Amplitude version to Segment:

1. **Event Names**: All event names are identical between versions
2. **Property Names**: All property names are identical between versions
3. **Historical Data**: Use Segment's Amplitude destination to send data to Amplitude
4. **User IDs**: Map Segment's anonymous ID to Amplitude's device ID

### Back to Amplitude

To revert to the Amplitude version:
- Use `index.html` instead of `index_segment.html`
- Original Amplitude integration remains unchanged in the non-`_segment` files

## File Structure

```
battleboat-gh-pages/
├── index.html                    # Original (Amplitude)
├── index_segment.html            # Segment version
├── js/
│   ├── battleboat.js            # Original (Amplitude)
│   └── battleboat_segment.js    # Segment version
├── css/
│   ├── styles.css               # Original
│   └── styles_segment.css       # Copy for Segment
└── img/                         # Shared images
```

## Documentation References

### Segment
- [Segment JavaScript SDK Documentation](https://segment.com/docs/connections/sources/catalog/libraries/website/javascript/)
- [Segment Track API](https://segment.com/docs/connections/sources/catalog/libraries/website/javascript/#track)
- [Segment Identify API](https://segment.com/docs/connections/sources/catalog/libraries/website/javascript/#identify)
- [Segment Page API](https://segment.com/docs/connections/sources/catalog/libraries/website/javascript/#page)

### Amplitude Session Replay
- [Session Replay Standalone SDK](https://amplitude.com/docs/session-replay/session-replay-standalone-sdk)
- [Session Replay with 3rd Party Analytics](https://amplitude.com/docs/session-replay/session-replay-standalone-sdk#quickstart)
- [Session Replay Integration with Segment](https://amplitude.com/docs/session-replay/session-replay-integration-with-segment)

### Amplitude Guides & Surveys
- [Guides & Surveys Web SDK](https://amplitude.com/docs/guides-and-surveys/sdk)
- [Guides & Surveys with Third-Party Analytics](https://amplitude.com/docs/guides-and-surveys/sdk#other-amplitude-sdks-and-third-party-analytics-providers)
- [Forward Events to Guides & Surveys](https://amplitude.com/docs/guides-and-surveys/sdk#forward-event)
- [Set User Properties](https://amplitude.com/docs/guides-and-surveys/sdk#set-user-properties)

## Support

For issues or questions:
- Segment Support: https://segment.com/help/
- Segment Community: https://community.segment.com/

---

**Last Updated**: October 28, 2025

