# Copilot instructions

## Build, test, and run

- Build and run the test suite with `mvn test`; build the artifact with `mvn package`.
- Run one test class with `mvn -Dtest=SnakeGameTest test` or `mvn -Dtest=TronGameTest test`. Run one method with, for example, `mvn -Dtest=SnakeGameTest#wrapsAtBoardEdges test`.
- No lint command or linter is configured in `pom.xml`.
- `mvn javafx:run` uses the `mainClass` configured in `pom.xml`, currently `pl.mikolaj.games.copilot.tron.TronApplication`. The README's Snake run instructions do not match that setting. Other application entry points are `pl.mikolaj.games.copilot.snake.SnakeApplication` and `pl.mikolaj.games.tictactoe.Main`.

## Architecture

This is a Java 21 Maven project with multiple JavaFX games. Each Copilot game separates its rules from its JavaFX application: `SnakeGame` and `TronGame` hold the board state and advance it through `tick()`, while their corresponding `*Application` classes own JavaFX input, animation timers, and canvas rendering. Keep game rules independent of JavaFX so they remain directly testable.

Tic-Tac-Toe uses a different, shared-board design: `AbstractBoard` supplies the 4-by-4 tile and winning-combination logic; `RealBoard` adapts it to JavaFX, and `VirtualBoard` copies the real board state to choose a computer move. `Main` launches that UI.

## Conventions specific to this codebase

- Game state uses nested enums and records for concepts such as directions, players, and cells. Trails and snakes are stored head-first; public collection getters return snapshots rather than the mutable internal collections.
- The game models reject immediate reverse turns and determine outcomes during a tick. Preserve this division between rules/state changes in the model and presentation/input in the JavaFX application.
- `SnakeGame` accepts a `Random` through a package-private constructor so package tests can make food placement predictable. Keep test-only control at package scope rather than exposing it in the public API.
- Tests use JUnit Jupiter under `src/test/java`, in the same package as their corresponding game model.
