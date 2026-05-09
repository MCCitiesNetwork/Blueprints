# Compatibility Contracts

This document is the source of truth for the externally-visible surface that
the Blueprints plugin must preserve through the in-flight refactor (see
`refactor-plan.md`). It also codifies the migration shims introduced in
**Phase 1**, which renamed the plugin from `Blu3Print` / `blu3print` to
`Blueprints` / `blueprints`.

The characterization tests under `src/test/java/...` enforce these contracts.
A change to anything below is a behavior change and requires an isolated PR
with explicit approval — never bundle it with a refactor PR.

---

## 1. Plugin descriptor (`plugin.yml`)

After Phase 1 the canonical identity is:

| Key      | Value                                              |
|----------|----------------------------------------------------|
| `name`   | `Blueprints`                                       |
| `prefix` | `Blueprints`                                       |
| `main`   | `io.github.bl3rune.blueprints.Blueprints`          |

The old `name` / `prefix` are not preserved — Bukkit only loads one plugin
per descriptor — but command and permission aliases ARE preserved (see below).

## 2. Commands

The plugin registers 14 commands. Each is canonical under the
`blueprints.<sub>` namespace and exposes both the legacy `blu3print.<sub>`
form and a short `blu3.<sub>` (or shortened `blu3.player` / `blu3.global`)
form as Bukkit aliases.

| Command (canonical)            | Legacy alias               | Short alias      | Permission                  |
|--------------------------------|----------------------------|------------------|-----------------------------|
| `blueprints`                   | `blu3print`                | `blu3`           | `blueprints`                |
| `blueprints.duplicate`         | `blu3print.duplicate`      | `blu3.duplicate` | `blueprints.duplicate`      |
| `blueprints.rotate`            | `blu3print.rotate`         | `blu3.rotate`    | `blueprints.rotate`         |
| `blueprints.turn`              | `blu3print.turn`           | `blu3.turn`      | `blueprints.turn`           |
| `blueprints.face`              | `blu3print.face`           | `blu3.face`      | `blueprints.face`           |
| `blueprints.import`            | `blu3print.import`         | `blu3.import`    | `blueprints.import`         |
| `blueprints.export`            | `blu3print.export`         | `blu3.export`    | `blueprints.export`         |
| `blueprints.scale`             | `blu3print.scale`          | `blu3.scale`     | `blueprints.scale`          |
| `blueprints.name`              | `blu3print.name`           | `blu3.name`      | `blueprints.name`           |
| `blueprints.give`              | `blu3print.give`           | `blu3.give`      | `blueprints.give`           |
| `blueprints.help`              | `blu3print.help`           | `blu3.help`      | `blueprints.help`           |
| `blueprints.config`            | `blu3print.config`         | `blu3.config`    | `blueprints.config`         |
| `blueprints.player-config`     | `blu3print.player-config`  | `blu3.player`    | `blueprints.player-config`  |
| `blueprints.global-config`     | `blu3print.global-config`  | `blu3.global`    | `blueprints.global-config`  |

**Migration rule:** the legacy aliases stay registered for at least one
release. Removing any of them is a behavior change.

## 3. Permissions

The new umbrella `blueprints.*` and `blueprints.basics` are canonical. Every
legacy `blu3print.<sub>` permission is still declared and lists the
corresponding `blueprints.<sub>` as a child, so any operator who already
granted a legacy permission keeps working without re-grants.

Defaults that are not the implicit `op`:

| Permission                                | Default  |
|-------------------------------------------|----------|
| `blueprints.basics`                       | `not op` |
| `blueprints.update-available-message`     | `op`     |
| `blueprints.no-block-cost`                | `false`  |
| `blueprints.force-place-discount`         | `false`  |

The same defaults are mirrored on the legacy `blu3print.*` aliases.
**No permission default may flip** during the migration window.

## 4. Configuration keys

The canonical root in `config.yml` is now `blueprints.*`. The runtime reader
(`GlobalConfig.refreshConfiguration` via `resolveKey`) reads `blueprints.<key>`
first and falls back to `blu3print.<key>` if the new key is absent. This lets
existing operator configs keep loading. New writes use the new root only.

`GConfig.getConfigPath()` returns the canonical path; `getLegacyConfigPath()`
returns the legacy `blu3print.*` form for the same suffix.

Per-blueprint config (`Config` enum) and player config (`PlayerConfig`) keys
keep the same enum names; only the YAML root changes.

## 5. Persistence: `blueprints.json` (was `blu3prints.json`)

The plugin persists its blueprint cache to a JSON file in its data folder.
With the rename, the data folder moves from `plugins/Blu3PrintPlugin/` to
`plugins/Blueprints/` and the file is renamed `blu3prints.json` → `blueprints.json`.

`Blueprints.migrateLegacyDataFolderIfNeeded()` runs on `onEnable` and:
- copies `plugins/Blu3PrintPlugin/blu3prints.json` → `plugins/Blueprints/blueprints.json`
  if the new file does not yet exist;
- copies `plugins/Blu3PrintPlugin/config.yml` → `plugins/Blueprints/config.yml`
  if the new config has not yet been written.

The legacy folder is left intact as a safety net.

For the migration window, `loadSavedBlueprintsToCache` also still reads a
legacy `blu3prints.json` from the new folder if found, in case a server
operator manually moved the old file across.

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
