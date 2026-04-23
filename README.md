# OpenClaw Android Companion

Een onofficiële Android companion app voor OpenClaw met dezelfde functionaliteit als de iOS app.

## Functies

- **WebSocket Verbinding** — Real-time communicatie met je OpenClaw gateway
- **Chat Interface** — Jetpack Compose UI met berichtengeschiedenis
- **Wake Word Detectie** — "Hey OpenClaw" met Porcupine
- **QR Code Scanning** — Snel koppelen met je gateway
- **Voice Input** — Spraak-naar-tekst voor hands-free chat
- **Camera Sharing** — Foto's delen in gesprekken
- **Push Notificaties** — Firebase Cloud Messaging voor berichten op de achtergrond
- **Biometrische Authenticatie** — Vingerafdruk/gezichtsherkenning
- **Donker/Licht Thema** — Automatisch meeschakelen
- **Lokale Berichten** — Room database voor offline geschiedenis

## Tech Stack

| Component | Library |
|-----------|---------|
| UI | Jetpack Compose |
| WebSocket | Ktor Client |
| Database | Room |
| QR Scanner | ML Kit |
| Wake Word | Porcupine |
| Push | Firebase Cloud Messaging |
| Camera | CameraX |
| Dependency Injection | Hilt |
| Async | Kotlin Coroutines / Flow |

## Installatie

1. Clone deze repo
2. Open in Android Studio
3. Voeg je `google-services.json` toe (Firebase)
4. Build en run

## Configuratie

De app zoekt automatisch naar OpenClaw gateways op het lokale netwerk. Je kunt ook handmatig verbinden via:
- QR code scannen (via OpenClaw web interface)
- Handmatig IP/port invoeren
- Tailscale netwerk

## Licentie

MIT — Dit is een onofficiële community project, geen onderdeel van OpenClaw zelf.
