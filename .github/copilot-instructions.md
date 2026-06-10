# Pillow Notes App - Development Guide

## Project Overview

Pillow is a full-featured Android notes application built with modern technologies:
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room ORM
- **DI Framework**: Hilt

## Key Directories

- **`data/`** - Database layer (entities, DAOs, repositories)
- **`domain/`** - Business logic (models, use cases)
- **`presentation/`** - ViewModels managing UI state
- **`ui/`** - Jetpack Compose screens and themes
- **`biometric/`** - Fingerprint authentication
- **`di/`** - Hilt dependency injection modules

## Development Tips

### Working with ViewModels
ViewModels manage UI state using Flow/StateFlow for reactive updates. Always collect state in Composables using `.collectAsState()`.

### Database Queries
Use Coroutine Flows for reactive queries. Repositories abstract the database layer - avoid direct DAO calls outside repositories.

### Composable Structure
Keep Composables focused and reusable. Extract complex UI logic into separate functions. Use state holders (ViewModels) for screen-level state.

### Common Tasks

**Add a new feature:**
1. Create entity in `data/db/entity/`
2. Create DAO in `data/db/dao/`
3. Create repository in `data/repository/`
4. Create ViewModel in `presentation/viewmodel/`
5. Create UI screen in `ui/screen/`

**Add a new screen:**
1. Create Composable in `ui/screen/`
2. Add route in `ui/navigation/PillowNavGraph.kt`
3. Create ViewModel if needed
4. Update navigation to link new screen

**Database migrations:**
1. Increment Room `@Database` version
2. Create migration in DAOs if schema changes
3. Test with existing data

## Build & Run

```bash
./gradlew build              # Build project
./gradlew installDebug       # Install on device
./gradlew test               # Run tests
./gradlew connectedAndroidTest  # Run UI tests
```

## Testing Strategy

- Unit tests for repositories and ViewModels
- Instrumentation tests for database operations
- Compose integration tests for UI flows
- Test coverage should be >70%

## Code Style

- Follow Kotlin conventions
- Use meaningful variable names
- Document complex logic with comments
- Keep functions small and focused (~20 lines ideal)

## Performance Notes

- Database queries are optimized with proper indexing
- Use `LazyColumn` for scrollable lists in Compose
- Avoid recomposition by using `remember` and `derivedStateOf`
- Use coroutines for all blocking operations

## Common Pitfalls to Avoid

1. ❌ Don't use `.value` instead of `.collectAsState()` in Compose
2. ❌ Don't make database calls on main thread
3. ❌ Don't forget to close resources in `onCleared()`
4. ❌ Don't create ViewModels outside of viewModel() composition
5. ❌ Don't use mutable state directly; use ViewModel for state management

## Resources

- [README.md](../README.md) - Project overview
- [SETUP.md](../SETUP.md) - Setup instructions
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Room Database: https://developer.android.com/training/data-storage/room
- Hilt: https://developer.android.com/training/dependency-injection/hilt-android

## Useful Links in IDE

- Run tests: `Shift+Ctrl+F10` (Windows/Linux) or `Shift+Cmd+R` (Mac)
- Build: `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (Mac)
- Format code: `Ctrl+Alt+L` (Windows/Linux) or `Cmd+Option+L` (Mac)
- Rename symbol: `Shift+F6`

---

**For detailed setup, see SETUP.md**
