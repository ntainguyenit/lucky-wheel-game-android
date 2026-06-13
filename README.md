# Mix Spin Wheel 🎡

[English Below]

Một trò chơi vòng quay may mắn (chọn con vật) tương tác, phong cách HTML5 dành cho Android, được xây dựng hoàn toàn theo các tiêu chuẩn phát triển Android hiện đại. Dự án này thể hiện sự hiểu biết sâu sắc về **Clean Architecture**, **Reactive Programming**, và **Declarative UI**.

## 🚀 Công nghệ & Thư viện (Tech Stack)

- **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3) để xây dựng giao diện hoàn toàn declarative và reactive.
- **Kiến trúc:** **MVVM (Model-View-ViewModel)** với luồng dữ liệu một chiều (UDF).
- **Xử lý bất đồng bộ:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [StateFlow](https://developer.android.com/kotlin/flow) để quản lý state và xử lý logic nền.
- **Lưu trữ nội bộ:** [Room Database](https://developer.android.com/training/data-storage/room) ánh xạ SQLite thành các object Kotlin với KSP (Kotlin Symbol Processing).
- **Quản lý Dependency:** Gradle Version Catalogs (`libs.versions.toml`) giúp theo dõi và quản lý thư viện tập trung.
- **Mạng/API:** Cấu hình sẵn Retrofit2, OkHttp3 (kèm Logging Interceptor), và Moshi để parse JSON.
- **Cấu hình Testing:** Thiết lập sẵn JUnit4, Espresso, Robolectric, và Roborazzi (Snapshot testing).

## 🏗 Kiến trúc & Design Patterns

Cấu trúc dự án được phân chia module chặt chẽ theo feature và layer, đảm bảo tính dễ bảo trì, dễ test và mở rộng:

* **Presentation Layer (`ui/`)**: Chứa các `GameScreens` xây dựng bằng Jetpack Compose. Logic giao diện được tách biệt hoàn toàn khỏi logic nghiệp vụ (business logic).
* **Domain & State Management (`GameViewModel.kt`)**: Triển khai ViewModel chứa `StateFlow` để cung cấp state phản hồi cho UI. Xử lý các logic tính toán vật lý khi quay, số dư ví ảo và kiểm tra đặt cược.
* **Data Layer (`data/`)**: 
  * `GameRepository.kt` đóng vai trò là nguồn dữ liệu duy nhất (single source of truth) của app.
  * `GameDatabase.kt` xử lý lưu trữ dữ liệu bền vững (ví ảo, đăng nhập hằng ngày, điểm giới thiệu) qua Room.

## 💡 Tính năng Kỹ thuật Nổi bật

1. **Custom Canvas Animations**: Vòng quay và hiệu ứng giảm tốc mượt mà của kim chỉ được lập trình hoàn toàn bằng toán học thông qua `animateFloatAsState` và Coroutines của Compose, thể hiện hiệu năng render UI phức tạp.
2. **State-Driven UI**: Toàn bộ state của app (Số dư, Kết quả quay, Cài đặt Admin) luân chuyển một chiều một cách chặt chẽ từ ViewModel xuống, đảm bảo không bao giờ xảy ra lỗi bất đồng bộ UI.
3. **Admin RTP Controller**: Bảng điều khiển cấu hình động cho phép điều chỉnh Tỉ lệ Thắng (RTP - Return to Player) theo thời gian thực, minh chứng cho việc áp dụng data-binding chuyên sâu.

## 🛠 Yêu cầu & Cài đặt

1. Clone repository:
   ```bash
   git clone https://github.com/ntainguyenit/lucky-wheel-game-android.git
   ```
2. Mở dự án bằng **Android Studio** (Khuyến nghị bản Flamingo trở lên).
3. Kết nối thiết bị Android của bạn hoặc bật máy ảo (emulator).
4. Nhấn **Run 'app'** để build và chạy ứng dụng.

---

# Mix Spin Wheel 🎡 (English Version)

An interactive, HTML5-style animal selection betting wheel game for Android, built entirely with modern Android development standards. This project demonstrates a strong understanding of **Clean Architecture principles**, **Reactive Programming**, and **Declarative UI**.

## 🚀 Tech Stack & Libraries

- **Language:** [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3) for fully declarative and reactive UI components.
- **Architecture:** **MVVM (Model-View-ViewModel)** with unidirectional data flow (UDF).
- **Concurrency & Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [StateFlow](https://developer.android.com/kotlin/flow) for state management and background processing.
- **Local Persistence:** [Room Database](https://developer.android.com/training/data-storage/room) mapping local SQLite to Kotlin objects with KSP (Kotlin Symbol Processing).
- **Dependency Management:** Gradle Version Catalogs (`libs.versions.toml`) for centralized dependency tracking.
- **Networking/API (Configured):** Retrofit2, OkHttp3 (with Logging Interceptor), and Moshi for JSON parsing.
- **Testing Setup:** Configured with JUnit4, Espresso, Robolectric, and Roborazzi (Snapshot testing).

## 🏗 Architecture & Design Patterns

The project structure is strictly modularized by features and layers, ensuring maintainability, testability, and scalability:

* **Presentation Layer (`ui/`)**: Contains the `GameScreens` built with Jetpack Compose. UI logic is decoupled from business logic.
* **Domain & State Management (`GameViewModel.kt`)**: Implements the ViewModel holding `StateFlow` to provide a reactive state to the UI. Handles the spinning physics, wallet balance calculations, and betting validation.
* **Data Layer (`data/`)**: 
  * `GameRepository.kt` acts as the single source of truth for the application's data.
  * `GameDatabase.kt` handles persistent storage (simulated wallet, daily logins, referral points) via Room.

## 💡 Key Technical Features

1. **Custom Canvas Animations**: The spinning wheel and smooth pointer deceleration are implemented purely mathematically using Compose's `animateFloatAsState` and Coroutines, showcasing complex UI rendering performance.
2. **State-Driven UI**: Entire application state (Balance, Spin Result, Admin Settings) flows strictly downwards from the ViewModel, ensuring zero UI inconsistencies.
3. **Admin RTP Controller**: Features a dynamic configuration panel allowing real-time adjustment of the Win Ratio (RTP - Return to Player), demonstrating deep data-binding concepts.

## 🛠 Prerequisites & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ntainguyenit/lucky-wheel-game-android.git
   ```
2. Open the project in **Android Studio** (Flamingo or newer recommended).
3. Connect your Android device or start an emulator.
4. Click **Run 'app'** to build and deploy the application.
