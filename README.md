#  3AM
> the kinda app you asked for to listen to music at 3AM. A submission from FiniteCode for HackClub's 3AM program.


## ⚙️ Features
- AmpEngine: A custom engine built from the ground up that utilizes `Media3` alongside `Exoplayer` and `MediaSessionService` to provide seamless playback.
- AmpEqualizer: In-House audio DSP effects with genre based presets, Bass Boost and more to come.
- HeartEngine: A local Mediastore based scanner that automatically scans and categorizes music with a efficient local database and automatic updates.
- Aesthetics: While not a technical feature, the app features slick glassmorphism/realtime-blurs and a dark/moody OLED vibe.

## 📱 Screenshots
> Coming Soon

## 🏗️ the Stack
- UI: Jetpack Compose, Material 3 Expressive, Coil, Haze from chrisbanes (a shoutout to his great library!) 
- Audio: Jetpack Media3.

## 🛠️ Building

### Prerequisites:
- Android Studio Ladybug or newer.
- JDK 11 / 17.
- Android device or emulator running API 33+. (Android 13+)

### Spinning it Up:
```Shell
git clone https://github.com/Finite-Code/3AM.git 3AM
cd 3AM
./gradlew assembleDebug
```

## 🧑‍⚖️ License
This project utilizes GPL-3.0

Checkout LICENSE