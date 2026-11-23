# Orion Interview project: conventions for the AI assistant

Native Android, Kotlin, Jetpack Compose. Single `:app` module.

## Architecture (keep to it)
- MVVM + unidirectional data flow: `Screen (stateless) <- Route (collects state) <- ViewModel (StateFlow<UiState>) <- Repository (interface) <- data source (Retrofit / BLE / Room)`.
- UI state = one `sealed interface XUiState` per screen (Loading / Success / Error ...). No mutable state exposed from ViewModels.
- Repositories return `Result<T>` or `Flow<T>`; do IO on an injected `CoroutineDispatcher` (default `Dispatchers.IO`).
- DI is manual in `di/AppContainer.kt`. Add new dependencies there.
- Packages: `data/remote`, `data/repository`, `data/ble` (if needed), `ui/<feature>`, `ui/theme`, `di`.

## Rules
- Small, focused changes. Explain the plan before writing code.
- Every ViewModel gets a unit test with a fake repository (see `HomeViewModelTest`, uses Turbine + StandardTestDispatcher).
- Compose: stateless composables take state + lambdas; use `collectAsStateWithLifecycle`; stable `key` in lazy lists.
- BLE: request `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` at runtime on API 31+, `ACCESS_FINE_LOCATION` on API <= 30. Stop scans on a timeout and in `onCleared`. Close `BluetoothGatt` on disconnect.
- No hardcoded secrets. No blocking calls on the main thread.
- Build check: `./gradlew assembleDebug testDebugUnitTest`.
