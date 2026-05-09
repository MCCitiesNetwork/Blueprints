# Compatibility Contracts

This document is part of the **Phase 0 behavior lockdown** for the Blueprints
plugin refactor (see `refactor-plan.md`). It is the source of truth for the
externally-visible surface that the rename + refactor MUST preserve, and for
the migration shims that bridge the old `Blu3Print` / `blu3print` identity to
the new `Blueprints` / `blueprints` identity.

The characterization tests under `src/test/java/...` enforce these contracts.
A change to anything below is a behavior change and requires an isolated PR
with explicit approval — never bundle it with a refactor PR.

---

## 1. Plugin descriptor (`plugin.yml`)

Currently locked:

| Key      | Value                                                     |
|----------|-----------------------------------------------------------|
| `name`   | `Blu3PrintPlugin`                                         |
| `prefix` | `Blu3Print`                                               |
| `main`   | `io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin`       |

After Phase 1 these become `Blueprints`, `Blueprints`, and a new package path.
The old `name`/`prefix` are not preserved — Bukkit only loads one plugin per
descriptor — but command and permission aliases ARE preserved (see below).

## 2. Commands

The plugin currently registers 14 commands. Each has a `blu3.<sub>` alias.

| Command (current)              | Alias (current) | Permission (current)         |
|--------------------------------|------------------|------------------------------|
| `blu3print`                    | `blu3`           | `blu3print`                  |
| `blu3print.duplicate`          | `blu3.duplicate` | `blu3print.duplicate`        |
| `blu3print.rotate`             | `blu3.rotate`    | `blu3print.rotate`           |
| `blu3print.turn`               | `blu3.turn`      | `blu3print.turn`             |
| `blu3print.face`               | `blu3.face`      | `blu3print.face`             |
| `blu3print.import`             | `blu3.import`    | `blu3print.import`           |
| `blu3print.export`             | `blu3.export`    | `blu3print.export`           |
| `blu3print.scale`              | `blu3.scale`     | `blu3print.scale`            |
| `blu3print.name`               | `blu3.name`      | `blu3print.name`             |
| `blu3print.give`               | `blu3.give`      | `blu3print.give`             |
| `blu3print.help`               | `blu3.help`      | `blu3print.help`             |
| `blu3print.config`             | `blu3.config`    | `blu3print.config`           |
| `blu3print.player-config`      | `blu3.player`    | `blu3print.player-config`    |
| `blu3print.global-config`      | `blu3.global`    | `blu3print.global-config`    |

**Phase 1 migration rule:** every new `blueprints.<sub>` command MUST keep all
the legacy names in its `aliases:` list for at least one release. The
`blu3` short alias also stays. Tab completers move to the new command but must
still resolve under the old names.

## 3. Permissions

The umbrella permissions `blu3print.*` and `blu3print.basics` (and their
children listed in `plugin.yml`) are the long-standing surface. Defaults
that are not the implicit `op`:

| Permission                               | Default  |
|------------------------------------------|----------|
| `blu3print.basics`                       | `not op` |
| `blu3print.update-available-message`     | `op`     |
| `blu3print.no-block-cost`                | `false`  |
| `blu3print.force-place-discount`         | `false`  |

**Phase 1 migration rule:** new `blueprints.*` permissions are added; old
`blu3print.*` permissions are kept and mapped to the new ones via permission
children (Bukkit allows a parent permission to grant another permission as
a child) so existing per-permission grants in operator configs keep working.
No permission default may flip during Phase 1.

## 4. Configuration keys

Currently rooted at `blu3print.*` in `config.yml`. Full path list is locked
in `GConfigTest`.

**Phase 1 migration rule:** the `ConfigService` reads `blueprints.*` first,
falling back to `blu3print.*` if absent. On first save, the new key is
written; old keys are left untouched so a downgrade still works.

Per-blueprint config (`Config` enum) and player config (`PlayerConfig`) keys
keep the same enum names; only the YAML root changes.

## 5. Persistence: `blu3prints.json`

The plugin persists its blueprint cache to a JSON file in the plugin's data
folder. The file path moves with the plugin name (Bukkit derives it from
`plugin.yml`'s `name` field).

**Phase 1 migration rule:** on `onEnable`, if the new data folder
(`Blueprints/`) does not contain `blueprints.json` and the legacy
`Blu3PrintPlugin/blu3prints.json` exists, copy it across. The legacy file is
not deleted.

## 6. Encoded blueprint string format

The on-the-wire format produced by `EncodingUtils` is the user-facing import
/ export contract. Locked by `EncodingUtilsTest`:

```
<INGREDIENTS> '|' <X>:<Y>:<Z> '|' <ORIENT>-<ROT>-<SCALE> '~' <BODY>
```

with delimiters `~ | - = : ! .` and a 52-symbol alphabet `A..Z a..z`. **No
refactor PR may change any of these.** A future format change requires its
own versioned format and a parser shim — never silently.

## 7. Behavior-change rule

Refactor PRs must not modify any externally-observable behavior. If a change
is needed (e.g. a bug fix), it lives in its own PR and updates the
characterization tests + this doc explicitly.
