# Mix Spin Wheel

An interactive, HTML5-style animal selection betting wheel game for Android, built entirely with modern Android development standards. This project demonstrates a strong understanding of **Clean Architecture principles**, **Reactive Programming**, and **Declarative UI**.

## Tech Stack & Libraries

- **Language:** [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3) for fully declarative and reactive UI components.
- **Architecture:** **MVVM (Model-View-ViewModel)** with unidirectional data flow (UDF).
- **Concurrency & Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [StateFlow](https://developer.android.com/kotlin/flow) for state management and background processing.
- **Local Persistence:** [Room Database](https://developer.android.com/training/data-storage/room) mapping local SQLite to Kotlin objects with KSP (Kotlin Symbol Processing).
- **Dependency Management:** Gradle Version Catalogs (`libs.versions.toml`) for centralized dependency tracking.
- **Networking/API (Configured):** Retrofit2, OkHttp3 (with Logging Interceptor), and Moshi for JSON parsing.
- **Testing Setup:** Configured with JUnit4, Espresso, Robolectric, and Roborazzi (Snapshot testing).

## Architecture & Design Patterns

The project structure is strictly modularized by features and layers, ensuring maintainability, testability, and scalability:

* **Presentation Layer (`ui/`)**: Contains the `GameScreens` built with Jetpack Compose. UI logic is decoupled from business logic.
* **Domain & State Management (`GameViewModel.kt`)**: Implements the ViewModel holding `StateFlow` to provide a reactive state to the UI. Handles the spinning physics, wallet balance calculations, and betting validation.
* **Data Layer (`data/`)**: 
  * `GameRepository.kt` acts as the single source of truth for the application's data.
  * `GameDatabase.kt` handles persistent storage (simulated wallet, daily logins, referral points) via Room.

## Key Technical Features

1. **Custom Canvas Animations**: The spinning wheel and smooth pointer deceleration are implemented purely mathematically using Compose's `animateFloatAsState` and Coroutines, showcasing complex UI rendering performance.
2. **State-Driven UI**: Entire application state (Balance, Spin Result, Admin Settings) flows strictly downwards from the ViewModel, ensuring zero UI inconsistencies.
3. **Admin RTP Controller**: Features a dynamic configuration panel allowing real-time adjustment of the Win Ratio (RTP - Return to Player), demonstrating deep data-binding concepts.

## Prerequisites & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ntainguyenit/lucky-wheel-game-android.git
   ```
2. Open the project in **Android Studio** (Flamingo or newer recommended).
3. Connect your Android device or start an emulator.
4. Click **Run 'app'** to build and deploy the application.
