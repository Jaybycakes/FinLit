---
name: android-beginner-stack
description: |
  Expert guide for building Android apps with the "Easy Bundle" stack: Jetpack Compose (Material3 UI), Orbit MVI (state management), Koin + Koin Annotations (dependency injection), and Ktorfit + Ktor (networking).
  Use when: scaffolding a new Android project, writing Composables with previews, setting up Koin modules, defining Ktorfit API interfaces, wiring Orbit MVI ViewModels, handling network errors/SSL/timeouts, building navigation with Nav3, or teaching beginners clean Android architecture.
  Covers: gradle setup, folder structure, certificate pinning, error handling, UDF data flow, side effects, collectAsStateWithLifecycle, koinViewModel, Serializable routes, and full working code examples with Preview annotations.
---

# Android Beginner Stack

Compose + Orbit MVI + Koin Annotations + Ktorfit. Single-module, clean architecture, beginner-friendly.

## Folder Structure

```
app/src/main/
├── data/
│   ├── remote/          # Ktorfit interfaces + DTOs
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Domain models
│   └── repository/      # Repository interfaces
├── presentation/
│   └── <feature>/
│       ├── <Feature>Screen.kt       # Composable
│       ├── <Feature>ViewModel.kt    # Orbit ContainerHost
│       └── <Feature>Contract.kt    # State + Intent + SideEffect
└── di/
    └── AppModule.kt     # Koin @Module
```

## Reference Files

| Topic | File |
|-------|------|
| Gradle setup (libs.versions.toml + build.gradle.kts) | [references/gradle-setup.md](references/gradle-setup.md) |
| Ktorfit: interfaces, SSL, timeouts, error handling | [references/ktorfit-network.md](references/ktorfit-network.md) |
| Orbit MVI: ViewModel, State, Intent, SideEffect | [references/orbit-mvi.md](references/orbit-mvi.md) |
| Koin Annotations: modules, scopes, ViewModel injection | [references/koin-di.md](references/koin-di.md) |
| Compose: screens, previews, state collection | [references/compose-ui.md](references/compose-ui.md) |
| Navigation (Nav3 + Koin) | [references/navigation.md](references/navigation.md) |
| End-to-end wiring example (Users feature) | [references/full-example.md](references/full-example.md) |

## Core Rules

1. **Every Composable has `@Preview`** — no exceptions, even loading/error states.
2. **State flows down, events flow up** — Composables emit `Intent`, never mutate state directly.
3. **Side effects ≠ state** — navigation and one-time toasts go through `postSideEffect()`.
4. **Repository is the single source of truth** — ViewModel calls repository, never Ktorfit directly.
5. **Error handling is explicit** — `Result<T>` with `.onSuccess`/`.onFailure` always present.
6. **`collectAsStateWithLifecycle()`** — always, never bare `collectAsState()`.
7. **Pass only IDs between screens** — fetch data in destination ViewModel via `SavedStateHandle`.

## Quick Decision Guide

| Need | Solution |
|------|----------|
| HTTP GET/POST | Ktorfit `@GET`/`@POST` interface |
| SSL on debug builds | `UnsafeOkHttpClient` (dev only) — see ktorfit-network.md |
| Certificate pinning (prod) | OkHttp `CertificatePinner` — see ktorfit-network.md |
| Inject ViewModel in Compose | `koinViewModel()` |
| One-time navigation event | `postSideEffect(SideEffect.NavigateTo(...))` |
| Show Snackbar once | `postSideEffect(SideEffect.ShowMessage(...))` |
| Loading / Success / Error UI | Sealed `UiState` — see orbit-mvi.md |
| Screen-scoped DI | `@KoinViewModel` annotation |
| App-wide singleton | `@Single` annotation |

## Workflow for a New Feature

1. **Contract** — define `State`, `Intent`, sealed `SideEffect` in `<Feature>Contract.kt`
2. **ViewModel** — implement `ContainerHost<State, SideEffect>`, wire intents to repository
3. **Repository** — interface in domain, impl in data; wrap Ktorfit calls in `runCatching`
4. **Screen** — stateless Composable receiving `state` + `onIntent` lambda + side effect handler
5. **DI** — annotate with `@Single`/`@KoinViewModel`, register module in `startKoin`
6. **Navigation** — add `@Serializable` route data class, register in `NavHost`

Read the relevant reference file(s) for each step above.
