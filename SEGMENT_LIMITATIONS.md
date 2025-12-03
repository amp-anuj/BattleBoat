# Segment Integration Limitations

## Current Setup Status

### ✅ What's Working
- **Segment Analytics**: All events tracking through Segment
- **Amplitude Session Replay**: Recording sessions with `[Amplitude] Session Replay ID` in events
- **Event Forwarding**: Segment events include Session Replay IDs
- **Multi-Destination**: Can route Segment data to multiple analytics platforms

### ❌ What's NOT Working
- **Amplitude Guides & Surveys**: Cannot be integrated with Segment using CDN-only approach

## Why Guides & Surveys Doesn't Work

The Amplitude Guides & Surveys SDK has incompatible initialization requirements when used with third-party analytics providers like Segment:

### Issue 1: API-Key Script Auto-Boots
The API-key specific script (`https://cdn.amplitude.com/script/API_KEY.engagement.js`):
- Automatically initializes AND boots the SDK
- Boots before Segment provides device ID/session ID
- Results in error: `User must specify at least a user_id or device_id`

### Issue 2: Generic Bundle Doesn't Expose Global
The npm package (`@amplitude/engagement-browser`):
- Doesn't expose `window.engagement` when loaded via CDN (unpkg, jsdelivr, esm.sh)
- Requires a module bundler (webpack, vite, rollup) to use properly
- Cannot be manually initialized without a build process

###Issue 3: Documentation Gap
Amplitude's documentation for [third-party analytics integration](https://amplitude.com/docs/guides-and-surveys/sdk#other-amplitude-sdks-and-third-party-analytics-providers) assumes npm installation with a build process, not CDN-only usage.

## Solutions to Add Guides & Surveys

If you need Guides & Surveys, here are your options:

### Option 1: Use Amplitude Browser SDK (Recommended)
**Replace Segment with Amplitude for analytics**

Pros:
- ✅ Guides & Surveys works out-of-the-box
- ✅ Session Replay works perfectly  
- ✅ All features integrated
- ✅ CDN-only, no build process needed

Cons:
- ❌ Lose Segment's multi-destination routing
- ❌ Need to migrate tracking code from Segment to Amplitude

### Option 2: Set Up Build Process
**Keep Segment, add npm + bundler**

Pros:
- ✅ Keep Segment for analytics
- ✅ Can manually control Guides & Surveys initialization

Cons:
- ❌ Requires npm, bundler (webpack/vite), build step
- ❌ More complex development workflow
- ❌ Need to deploy built JavaScript

Steps:
```bash
npm install @amplitude/engagement-browser
```

```javascript
import { init, boot } from '@amplitude/engagement-browser';

// Initialize
init(API_KEY, { serverZone: 'US' });

// Boot with Segment IDs after Segment is ready
analytics.ready(() => {
  boot({
    deviceId: analytics.user().anonymousId(),
    sessionId: getStoredSessionId(),
    userProperties: { Game: 'BattleBoat' }
  });
  
  // Forward Segment events
  analytics.on('track', (event, properties) => {
    forwardEvent({ event_type: event, event_properties: properties });
  });
});
```

### Option 3: Hybrid Approach
**Use Amplitude SDK in Segment as a destination**

1. Keep Segment for client-side tracking
2. Add Amplitude as a Segment destination
3. Use Amplitude Browser SDK alongside Segment
4. Let Segment forward events to Amplitude
5. Guides & Surveys uses Amplitude SDK's identity

Pros:
- ✅ Keep Segment's multi-destination routing
- ✅ Guides & Surveys works
- ✅ CDN-only

Cons:
- ❌ Running two analytics SDKs (heavier page load)
- ❌ Need to sync user identity between both

## Current File Status

- `index.html` - Original with Amplitude SDK (has Guides & Surveys)
- `index_segment.html` - Segment version (no Guides & Surveys)
- `js/battleboat.js` - Original with Amplitude tracking
- `js/battleboat_segment.js` - Segment tracking + Session Replay IDs
- `css/styles_segment.css` - Copy of styles

## Recommendation

**If you need Guides & Surveys**: Use `index.html` (the original Amplitude version) instead of `index_segment.html`.

**If you need multi-destination routing**: Stick with `index_segment.html` and accept that Guides & Surveys isn't available.

**If you need both**: Implement Option 2 (build process) or Option 3 (hybrid approach).

