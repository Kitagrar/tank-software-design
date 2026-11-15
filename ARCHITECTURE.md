# Architecture

## Layers

Dependencies should point from infrastructure to application to domain.
Ports belong to the inner side; adapters implement them. Packages currently
group features and do not correspond one-to-one to architectural layers.
Constructor injection supplies policies and adapters; no DI container is needed.

The inventory below covers all production types, including `TankAction.Kind`.
Names are relative to `ru.mipt.bit.platformer`.

| Layer | Types | Responsibility |
| --- | --- | --- |
| Domain | `model.Tank` | Entity and State context: health, facing, movement and shooting |
| Domain | `model.Bullet`, `model.Tree` | Projectile and stationary obstacle entities |
| Domain | `model.GameLevel` | Aggregate ownership, logical tick, collisions and entity lifecycle |
| Domain | `model.Direction` | Direction value; also carries a rendering angle (debt) |
| Domain | `model.Movable`, `model.Obstacle` | Movement and occupancy capabilities |
| Domain | `model.CompositeObstacle`, `model.MapBorder` | Composite occupancy and map-boundary rules |
| Domain | `model.LevelEntity` | Marker for observable entity membership |
| Domain/core port | `model.LevelObserver` | Lifecycle output contract |
| Domain | `model.state.TankState` | State policy contract |
| Domain | `model.state.LightTankState`, `model.state.MediumTankState`, `model.state.HeavyTankState` | Immutable movement/shooting policies |
| Domain | `level.LevelValidator`, `level.RandomTankPositionGenerator` | Layout validity and initial spawn selection |
| Domain/application | `level.RandomLevelCreationStrategy` | In-process layout generation and aggregate construction; not an external adapter |
| Application | `level.LevelCreationStrategy` | Initial-level factory/strategy seam |
| Application | `command.Command`, `command.MoveCommand`, `command.ShootCommand`, `command.ToggleHealthBarsCommand` | Requests and their execution against receivers |
| Application | `control.TankAction`, `control.TankAction.Kind` | MOVE/SHOOT intention value and discriminant |
| Application | `control.TankControlStrategy`, `control.KeyboardControlStrategy`, `control.RandomAiControlStrategy` | Decision contract and keyboard/random policies |
| Application | `control.TankController`, `control.DisplayController` | Deferred command construction |
| Application, legacy | `input.InputHandler` | Older direct input-to-movement flow, unused by the launcher |
| Application ports | `port.InputSource`, `port.HealthBarDisplay` | Input queries and display control |
| Infrastructure | `input.LibGdxInputSource` | LibGDX keyboard adapter |
| Infrastructure | `graphics.GraphicalLevel` | Observer-maintained scene and health-bar display adapter |
| Infrastructure | `graphics.EntityRenderer` | Internal renderer component contract |
| Infrastructure | `graphics.TankRenderer`, `graphics.TreeRenderer`, `graphics.BulletRenderer`, `graphics.HealthBarRendererDecorator` | Graphical adapter components; decorator owns its wrapped renderer |
| Infrastructure | `util.GdxGameUtils`, `util.TileRectangleInterpolator` | Map/drawing helpers and visual interpolation |
| Infrastructure/application, mixed | `level.FileLevelCreationStrategy` | Classpath loading, parsing, validation and level construction |
| Infrastructure/composition root | `GameDesktopLauncher` | LibGDX driving adapter, object wiring and four-phase cycle |

LibGDX calls the launcher's lifecycle methods: this is framework IoC. The launcher
collects commands, executes them, ticks the logical level and renders the scene.

## State

`Tank` owns a private `TankState` and delegates duration and shooting permission.
The three immutable State implementations have no framework dependencies.

| Living health / maxHealth | State | Cell duration | Shooting permission |
| --- | --- | --- | --- |
| Greater than 70% | LIGHT | 0.4 s | Yes |
| 15% through 70%, inclusive | MEDIUM | 0.8 s | Yes |
| Greater than 0%, below 15% | HEAVY | 1.2 s | No |

Damage immediately selects the living State using cross-multiplication in `long`.
An ongoing move retains its progress and endpoints; subsequent updates use the
new duration. At zero health, existing death/removal rules apply. No fourth State
is introduced; any retained policy is irrelevant to a dead tank's capabilities.

`Tank.canShoot()` combines life, stationary status and State permission.
`Tank.shoot()` enforces it, and `ShootCommand` checks it again on execution.
Heavy AI still selects from the same five actions; SHOOT becomes a safe no-op,
without reroll. Default damage 25 skips living HEAVY (100, 75, 50, 25, 0), so
HEAVY is covered by tests using other damage values. No booster/cooldown is added.

## Ports

| Port | Clients | Implementations |
| --- | --- | --- |
| `port.InputSource` | Keyboard strategy, display controller, legacy input handler | `LibGdxInputSource`, test fakes |
| `model.LevelObserver` | `GameLevel` | `GraphicalLevel`, test observers |
| `port.HealthBarDisplay` | Toggle command and display controller | `GraphicalLevel`, test displays |

`InputSource` is an application input-query boundary: application code polls the
adapter, so its call direction differs from an inbound use-case endpoint.
`LevelObserver` publishes initial replay and runtime membership events.
Health bars read live model references; no health-change events or level polling
are needed. Moving the input/display ports inward changes ownership, not behaviour.

## Adapters

| Adapter | Boundary and external mechanism |
| --- | --- |
| `LibGdxInputSource` | Implements input queries using LibGDX keyboard APIs |
| `GraphicalLevel` | Implements lifecycle/display ports using a LibGDX scene and renderers |
| `GameDesktopLauncher` | Driving adapter from LibGDX callbacks to application/domain; composition root |
| `FileLevelCreationStrategy` | Classpath resource integration through the level factory seam; **mixed/incomplete boundary**, with parsing and construction still embedded |

`LevelCreationStrategy` is currently a factory seam called by the composition root,
not a dedicated resource-reading output port. Random generation is internal policy,
whereas file creation crosses an external I/O boundary. They are not equivalent
external adapters.

## Non-ports

- `Movable` and `Obstacle`: domain capabilities/policies, not external boundaries.
- `Command`: request-pattern abstraction, not automatically a hexagonal port.
- `TankControlStrategy`: internal action-selection policy.
- `EntityRenderer`: infrastructure-internal component used by Decorator.
- `TankState` and `LevelEntity`: domain policy contract and entity marker.

## Known architectural debt

- Model APIs depend on LibGDX `GridPoint2`; `Tank` also uses `MathUtils.isEqual`.
  Strict framework independence requires a domain coordinate value object and
  conversions at adapter boundaries. This touches model, collision, generation,
  rendering bridges and many tests, and is intentionally deferred.
- `Direction` combines game direction and rendering angle.
- `FileLevelCreationStrategy` combines classpath I/O, Reader parsing, validation
  and `GameLevel` construction. A later source port, classpath adapter and inward
  parser could separate these; HW8 does not introduce them.
- Legacy `InputHandler` bypasses Command and remains only for compatibility.
- `input` and `level` still contain types with different architectural roles.
  Their remaining packages are not rewritten merely for uniformity.

Concrete adapter wiring in the composition root and renderer composition inside
infrastructure are intentional. This is an incremental boundary improvement,
not a claim that the project now fully implements Clean Architecture.
