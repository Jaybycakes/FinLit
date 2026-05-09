# Scam Scanner Implementation

## Overview
A complete AI-powered scam detection feature following the android-beginner-stack pattern with Jetpack Compose, Orbit MVI, Koin, and Ktorfit.

## Architecture

### Data Layer
- **ScamApi.kt**: Ktorfit interface for the scam analysis API
- **ScamDto.kt**: Request/Response DTOs with kotlinx.serialization
- **ScamMapper.kt**: Maps DTOs to domain models
- **ScamRepositoryImpl.kt**: Repository implementation with error handling

### Domain Layer
- **ScamAnalysis.kt**: Domain model
- **ScamRepository.kt**: Repository interface

### Presentation Layer
- **ScamScannerContract.kt**: State, Intent, and SideEffect definitions
- **ScamScannerViewModel.kt**: Orbit MVI ViewModel with business logic
- **ScamScannerScreen.kt**: Composable UI with Material3 design

## Features

### UI Components
✅ Clean Material3 design matching the provided mockup
✅ Header with icon and descriptive text
✅ Multi-line text input with character counter
✅ Paste from clipboard functionality
✅ Clear button for input
✅ Privacy notice card
✅ Analyze button with loading state
✅ Result dialog with persona badge and detailed analysis
✅ Fully responsive and accessible

### Functionality
✅ Real-time character count (0/1000)
✅ Input validation
✅ Clipboard paste integration
✅ Loading states during API calls
✅ Error handling with user-friendly messages
✅ Result display in modal dialog
✅ Persona-based color coding (Vulnerable = error color)

### Network Layer
✅ Separate Ktorfit instance for scam API
✅ Base URL: `https://7iffdm.buildship.run/`
✅ Endpoint: `POST /sentinv2-evaluate`
✅ Request body: `{"scenario": "user message"}`
✅ Response parsing with kotlinx.serialization
✅ Proper error handling and mapping

### State Management
✅ Orbit MVI 6.1.0 with simple syntax
✅ Unidirectional data flow
✅ Side effects for one-time events (snackbars, clipboard)
✅ Proper lifecycle handling

### Dependency Injection
✅ Koin manual DSL (bypassing KSP for now)
✅ Named qualifiers for multiple Ktorfit instances
✅ Shared HttpClient for both APIs
✅ ViewModel injection with `koinViewModel()`

## API Integration

### Request Format
```json
{
  "scenario": "I was asked to transfer through qris"
}
```

### Response Format
```json
{
  "persona": "Vulnerable",
  "risk_analysis": "The user's scenario...",
  "recommended_action": "Exercise extreme caution..."
}
```

## Usage

The app launches directly to the Scam Scanner screen. Users can:
1. Type or paste a suspicious message
2. Click "Paste from clipboard" for quick input
3. Click "Analyze with AI" to scan the message
4. View results in a modal dialog
5. Dismiss and analyze another message

## Preview Functions

The implementation includes 4 preview functions:
- Empty state
- With text input
- Analyzing state (loading)
- Result dialog

## Code Quality

✅ Follows android-beginner-stack conventions
✅ Clean architecture with clear separation of concerns
✅ Proper error handling throughout
✅ Type-safe navigation with sealed interfaces
✅ Composable previews for all states
✅ Material3 design system
✅ Accessibility considerations
✅ Lifecycle-aware state collection

## Next Steps

To test the implementation:
1. Sync Gradle to download Orbit MVI 6.1.0
2. Build the project
3. Run on device/emulator
4. Test with sample scenarios like:
   - "I was asked to transfer through qris"
   - "Click this link to claim your prize"
   - "Your account will be suspended unless you verify"
