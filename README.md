# FinLit 💰

**FinLit** is a financial literacy Android app that helps users assess their financial knowledge and build it up through personalized learning — plus a built-in AI scam checker to help users navigate real-world financial risks.

## Features

- **Financial Literacy Quiz** — assesses a user's current financial literacy level
- **Personalized Course Recommendations** — suggests learning content based on quiz results and skill gaps
- **Scam Scanner** — paste any suspicious message (e.g. a text, email, or DM) and get an AI-generated risk analysis, including:
  - A risk persona classification (e.g. "Vulnerable")
  - A written risk analysis explaining *why* the message may be a scam
  - A recommended action for the user to take

## Tech Stack

- **Kotlin** + **Jetpack Compose** — UI toolkit
- **Material3** — design system
- **Orbit MVI** — unidirectional state management for the presentation layer
- **Koin** — dependency injection
- **Ktorfit** — type-safe networking client
- **kotlinx.serialization** — request/response (de)serialization
- **Gradle (Kotlin DSL)** — build system

The Scam Scanner feature follows a clean architecture split:

```
Data layer        -> ScamApi, ScamDto, ScamMapper, ScamRepositoryImpl
Domain layer       -> ScamAnalysis, ScamRepository
Presentation layer  -> ScamScannerContract, ScamScannerViewModel, ScamScannerScreen
```

It calls a dedicated backend endpoint that runs the AI-powered risk analysis and returns a structured response (persona, risk analysis, and recommended action), which the app then renders in a Material3 result dialog.

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17+
- An Android emulator or physical device

### Build & Run

```bash
# Clone the repo
git clone https://github.com/Jaybycakes/FinLit.git
cd FinLit

# Build the project
./gradlew build

# Or open the project in Android Studio and run directly on an emulator/device
```

## Project Structure

```
FinLit/
├── app/                                 # Main Android application module
├── gradle/                              # Gradle wrapper files
├── .kiro/skills/beginner-pattern-skill/ # Architecture pattern reference used for feature scaffolding
├── SCAM_SCANNER_IMPLEMENTATION.md       # Detailed write-up of the Scam Scanner feature
└── build.gradle.kts / settings.gradle.kts
```

## Contributing

Issues and pull requests are welcome — see the [Issues](https://github.com/Jaybycakes/FinLit/issues) tab.
