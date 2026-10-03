#  3AM
> *the kinda app you asked for to listen to music at 3AM.* A submission from FiniteCode for HackClub's 3AM program.


## ⚙️ Features
- **AmpEngine**: A custom engine built from the ground up that utilizes `Media3` alongside `Exoplayer` and `MediaSessionService` to provide seamless playback.
- **AmpEqualizer**: In-House audio DSP effects with genre based presets, Bass Boost and more to come.
- **HeartEngine**: A local Mediastore based scanner that automatically scans and categorizes music with a efficient local database and automatic updates.
- **Aesthetics**: While not a technical feature, the app features slick glassmorphism/realtime-blurs and a dark/moody OLED vibe.

## 📱 Screenshots
<img width="1728" height="1117" alt="MacBook Pro 16_ - 1(1)" src="https://github.com/user-attachments/assets/fba780c7-d035-457c-8660-47b4e774a23b" />


## 🏗️ the Stack
- **UI**: Jetpack Compose, Material 3 Expressive, Coil, [Haze from chrisbanes](https://chrisbanes.github.io/haze/latest/)
- **Audio**: Jetpack Media3.

## 🛠️ Building

### Prerequisites:
- Android Studio Ladybug or newer.
- JDK 11 / 17.
- Android device or emulator running API 33+. (**Android 13+**)

### Spinning it Up:
```Shell
git clone https://github.com/Finite-Code/3AM.git 3AM
cd 3AM
./gradlew assembleDebug
```

## External Help
AI/LLMs were used for the following things:
- Antigravity: to generate a color scheme for the app logo. 
- Android Studio's native tab to autocomplete (idk if it uses AI)
- To setup regex matching [`3ae56a`](https://github.com/Finite-Code/3AM/commit/3ae56adf39d562150be1c68298d3a0579f4cecf8)

> Not AI but: used online components to generate a boilerplate-UI for the web

## 🧑‍⚖️ License
**This project utilizes GPL-3.0 LICENSE**

*Checkout [LICENSE](https://github.com/Finite-Code/3AM/blob/main/LICENSE)*
