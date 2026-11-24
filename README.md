# Orion — Insights

A native Android app that renders a sleep-tracking **Insights** screen built from a Figma design in
Jetpack Compose. The screen is a scrollable summary of one night: a score, a horizontally scrollable
date strip, and cards for body movements, bed temperature, biological sleep age and sleep vitals.

Everything is driven by mock data behind a repository interface, so it runs with no backend.

## Highlights

- **Touch-first, not a static mock-up.** The date row scrolls horizontally and centres the selected day;
  the temperature and body-movement charts respond to tap and drag with a transient value pill and haptics;
  the whole page scrolls vertically without the charts stealing the gesture.
- **A colour-banded temperature chart.** The line is drawn in the colour of its value — light blue below
  26 °C, purple 26–28 °C, red above 28 °C — and blends smoothly across the thresholds.
- **Reusable, domain-agnostic components.** Everything in [`ui/components`](app/src/main/java/com/vibhuti/orion/ui/components)
  (cards, charts, the date strip, axis labels, range bar…) knows nothing about sleep; the screen feeds them plain inputs.

## Tech stack

Kotlin · Jetpack Compose (Material 3) · Coroutines/Flow · Navigation Compose · manual DI · JUnit4 + Turbine.
Single `:app` module. `minSdk 26`, `targetSdk 36`.

## Build & run

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew testDebugUnitTest      # run unit tests
./gradlew installDebug           # install on a connected device/emulator
```

The app opens straight onto the Insights screen; no sign-in or network is required.

## Architecture

MVVM with unidirectional data flow, per the project conventions in [CLAUDE.md](CLAUDE.md):

```
InsightsScreen (stateless)
   ← InsightsRoute (collects StateFlow)
   ← InsightsViewModel (StateFlow<InsightsUiState>)
   ← InsightsRepository (interface)
   ← MockInsightsRepository (data source)
```

- **UI state** is one `sealed interface InsightsUiState` (`Loading` / `Error` / `Success`). The selected
  night loads independently via a nested `NightState`, so switching days keeps the previous night on
  screen (dimmed) instead of flashing a spinner.
- **Repositories** return `Result<T>` and do their work on an injected `CoroutineDispatcher` (default
  `Dispatchers.IO`). Rapid day-switching cancels the in-flight request.
- **DI** is manual in [`di/AppContainer.kt`](app/src/main/java/com/vibhuti/orion/di/AppContainer.kt).
- **Composables** are stateless (state in, lambdas out), collect with `collectAsStateWithLifecycle`, and
  use stable keys in lazy lists.

### Package layout

| Package | What's in it |
|---|---|
| `data/repository` | Domain models, `InsightsRepository`, and `MockInsightsData` |
| `ui/insights` | The screen, route, ViewModel, formatters, and per-card `sections/` |
| `ui/components` | Reusable UI (cards, date strip, vital row, action row, axis labels…) |
| `ui/components/chart` | Reusable charts: banded line, event timeline, range bar, plus shared gesture/colour helpers |
| `ui/theme` | Dark colour palette, type scale, and the `OrionTheme` |

## Mock data

[`MockInsightsData`](app/src/main/java/com/vibhuti/orion/data/repository/MockInsightsData.kt) serves
deterministic, realistic data (with a small artificial latency):

- **Today's night** reproduces the Figma values exactly: score 72 "Excellent", restless time 3m 24s,
  rate 3.2/hr, sleep age 18–23, HRV 32 ms, heart rate 58 bpm, breath rate 14 brpm, and the
  purple → blue → red temperature curve.
- **Other nights** are seeded by their date, so the same day always shows the same (self-consistent)
  values — movements never overlap bed-exit periods, the restless rate tracks the score, and so on.

## Fonts

The design uses **Aeonik** (a commercial typeface). The repo bundles the CoType **trial** cuts
(Light / Regular / Bold) under [`res/font`](app/src/main/res/font). The trial is a subset: glyphs it
lacks (`/ : ? ° ·`) fall back to the system font, and there is no Medium weight. Drop the licensed
`.ttf`/`.otf` files in and the fallbacks disappear — see [`ui/theme/Type.kt`](app/src/main/java/com/vibhuti/orion/ui/theme/Type.kt).
The trial licence (`CoType-EULA-Trial.pdf`) is evaluation-only; swap in licensed fonts before release.

## Tests

```bash
./gradlew testDebugUnitTest
```

- `InsightsViewModelTest` — load, day selection, future-day guard, request cancellation, and error/retry,
  using a fake repository with Turbine and `StandardTestDispatcher`.
- `ThresholdColorScaleTest` — the temperature colour bands and the blended transitions.
- `InsightsFormatTest` — date/duration formatting and that the mock's design night matches the Figma figures.

## Known gaps / next steps

Tracked from a design & responsiveness review; not yet addressed:

- RTL locales mirror the chart axis/pill (they use `placeRelative`); large-font (2×) `DayChip` text can wrap.
- No tablet/foldable width cap; content stretches past ~600 dp.
- `selectedDate` isn't saved across process death, and timeline positions use `LocalDateTime` (DST-naïve).
- Accessibility polish pending: small-text contrast, full-date chip descriptions, `Role.Button` on the action row.
