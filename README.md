# 🗺️ AdventureLog — PWA to Native Android App

<p align="center">
  <strong>Transform your Web App / PWA into a high-performance Native Android Experience.</strong>
</p>

<!-- BADGES & PINS -->
<p align="center">
  <a href="https://github.com/Sir4y/adventurelogapp/releases">
    <img src="https://img.shields.io/github/v/release/Sir4y/adventurelogapp?color=00C853&label=Release&style=for-the-badge" alt="Latest Release" />
  </a>
  <a href="https://github.com/Sir4y/adventurelogapp/releases">
    <img src="https://img.shields.io/github/downloads/Sir4y/adventurelogapp/total?color=2196F3&label=Downloads&style=for-the-badge&logo=github" alt="Total Downloads" />
  </a>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Built for Android" />
  <img src="https://img.shields.io/badge/Java%20%2F%20Android%20SDK-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java / Android SDK" />
  <img src="https://img.shields.io/badge/PWA-Ready-4A90E2?style=for-the-badge&logo=pwa&logoColor=white" alt="PWA Ready" />
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License" />
  </a>
</p>


<!-- MAIN BANNER PLACEHOLDER -->
<p align="center">
  <img src="https://github.com/user-attachments/assets/cd7a37e3-a9ae-412e-9000-979658684141" alt="AdventureLog Banner" width="100%" />
</p>

</div>

<br/>

## 🌟 Highlights

**AdventureLog** turns any Progressive Web App (PWA) or self-hosted web service into a feature-packed, native-feeling Android application. Powered by an optimized Android WebView wrapper with deep system integrations.

<br/>

<div align="center">

| 📍 Native Geolocation | ⚡ Secret Settings Panel | 🛡️ Custom Security Controls | 🎨 OLED Themes & Icons |
| :---: | :---: | :---: | :---: |
| High-precision GPS & Network bridge working on HTTP & HTTPS | A hidden settings window that appears only when you hold four fingers on the screen for 4 seconds. | Configurable SSL bypass for local self-signed certs | Dynamic launcher icon switching & OLED dark mode |

</div>

<br/>

## 🎬 Preview & Screenshots

<div align="center">

<!-- CAROUSEL / DEMO GIFS PLACEHOLDER -->
<table>
  <tr>
    <td align="center">
      <strong>📱 Native App Experience</strong><br/><br/>
      <img src="https://github.com/user-attachments/assets/59f190cd-84f5-412b-b595-b4cd58a738e5" width="380px" alt="App Preview 1" />
    </td>
    <td align="center">
      <strong>⚡ Secret Settings Panel</strong><br/><br/>
      <img src="https://github.com/user-attachments/assets/a669bd65-8120-43e4-b82f-5b87b1fc29b0" width="380px" alt="App Preview 2" />
    </td>
  </tr>
</table>

</div>

<br/>

## ✨ Key Features

- **📍 Advanced Native Geolocation Bridge**: Seamlessly bridges HTML5 `navigator.geolocation` to Android's native `LocationManager` (GPS + Network), bypassing Chromium's HTTP secure origin restrictions for local servers.
- **🔔 Real-time UnifiedPush & WebPush**: Full support for WebPush notifications & UnifiedPush distributors.
- **🎨 Custom OLED Themes & Dynamic Icons**: Instant switcher for light, dark, and OLED themes + live app icon replacement.
- **📐 Immersive Edge-to-Edge Layout**: Automatic CSS safe area insets injection (`--safe-area-inset-*`).
- **⚡ Userscripts Support**: Inject custom JavaScript (Tampermonkey/Violentmonkey syntax) on page load.
- **🛡️ Custom Security Controls**: Configurable SSL bypass for local self-signed certs, cleartext HTTP support, & domain filtering.

<br/>

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO
```

### 2. Build & Run
```bash
# Generate signing key (first time only)
./make.sh keygen

# Build APK
./make.sh build

# Test on connected Android device
./make.sh test
```

### OR, just install apk
<br/>

## 🛠️ Configuration Options

| Option | Description | Default |
| :--- | :--- | :--- |
| `mainURL` | Target web app URL | `https://adventurelog.lan` |
| `geolocationEnabled` | Enables GPS/Network location bridge | `true` |
| `edgeToEdge` | Draws content behind Android system bars | `true` |
| `allowSubdomains` | Allows navigating subdomains inside app | `true` |
| `bypassSSL` | Allows trusting self-signed SSL certs | `false` |
| `openExternalLinksInBrowser` | Opens non-app links in default browser | `true` |

<br/>

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/Sir4y/adventurelogapp/issues).

<br/>

## 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for more information.

<div align="center">
  <sub>Built with ❤️ and Android Studio</sub>
</div>
