# AGENTS.md

## Project identity

This repository contains Minecraft server plugins for noasaba.

- The website is `https://noasaba.com`.
- Use `noasaba / nanosize` as the project signature in descriptions, README files, startup logs, and credits.
- Do not use `noasaba / nanosize` or `noasaba by nanosize` in author fields.

### Original noasaba plugins

These rules apply to plugins originally created for noasaba by nanosize.

- Plugin author fields must be exactly `nanosize`.
- Use `nanosize` only for the author field.
- Use `noasaba / nanosize` for descriptions, README files, startup logs, and credits.

### Third-party plugin modification policy

These rules apply when updating, debugging, patching, forking, modifying, or maintaining plugins originally created by someone else.

- Do not remove or overwrite the original author's name.
- Keep the original author first and add `nanosize` after the original author.
- Use this format where possible:
  - `<OriginalAuthor>, nanosize`
- If the metadata supports multiple authors, list the original author first and `nanosize` second.
- If the metadata supports only a single author string, append `nanosize` after the original author.
- README files, credits, changelogs, plugin descriptions, and documentation must preserve the original author's credit.
- `nanosize` should be described as the modifier, updater, debugger, maintainer, or noasaba-side maintainer when appropriate.
- Do not present modified third-party plugins as originally authored only by `nanosize`.
- The `Plugin author fields must be exactly nanosize` rule applies only to plugins originally created for noasaba by nanosize.

Example for third-party plugin metadata:

```yml
author: OriginalAuthor, nanosize
```

Example when multiple authors are supported:

```yml
authors:
  - OriginalAuthor
  - nanosize
```

Example README credit:

```md
## Credits

Original author: OriginalAuthor  
Modified and maintained for noasaba by nanosize.
```

## Java and package rules

- Java plugins must use Gradle.
- Do not migrate Java plugin projects to Maven unless explicitly requested.
- Root package must be `com.noasaba.<plugin>`.
- Public API must be placed under `com.noasaba.<plugin>.api`.
- Classes outside the `api` package are internal implementation details unless explicitly documented otherwise.
- Other plugins must not depend on internal implementation packages.

Recommended package structure:

```text
com.noasaba.<plugin>
com.noasaba.<plugin>.api
com.noasaba.<plugin>.command
com.noasaba.<plugin>.listener
com.noasaba.<plugin>.service
com.noasaba.<plugin>.storage
com.noasaba.<plugin>.config
com.noasaba.<plugin>.internal
```

## Platform compatibility

- noasaba plugins must remain compatible with Spigot.
- Target the currently requested server version: `26.2`.
- Use `plugin.yml` for plugin metadata.
- Do not require `paper-plugin.yml` unless explicitly requested for a Paper-only plugin.
- Do not depend on Paper-only APIs in core plugin code.
- Optional Paper-specific integrations must not prevent the plugin from loading on Spigot.
- If Paper-specific behavior is useful, isolate it so the plugin can still compile and run on Spigot.

## Plugin and command naming

- Plugins must be independent.
- Commands must be independent and feature-based.
- Do not create a shared `/noasaba` parent command by default.
- Users should not need to type `noasaba` to use normal plugin features.
- Use short, meaningful commands such as `/land`, `/money`, `/vote`, `/chat`, or plugin-specific equivalents.
- Administrative commands should belong to the relevant plugin command, such as `/land reload` or `/money reload`.
- The `noasaba` name should be used for package names, permissions, metadata, descriptions, and signatures, not as a default command prefix.

## Permission rules

- Permission nodes must use lowercase dot-separated names.
- Use `noasaba.<plugin>.<scope>.<action>` where practical.
- Command permissions should use `noasaba.<plugin>.command.<command>`.
- Admin permission should be `noasaba.<plugin>.admin`.
- Reload permission should be `noasaba.<plugin>.reload`.
- Debug permission should be `noasaba.<plugin>.debug`.
- Bypass permission should be `noasaba.<plugin>.bypass`.
- Permission changes that break existing setups require a major version bump.

Examples:

```text
noasaba.land.command.claim
noasaba.land.command.unclaim
noasaba.land.command.info
noasaba.land.reload
noasaba.land.debug
noasaba.land.admin
noasaba.land.bypass
```

## API and hook rules

- Consider external plugin hooks for every feature that may need integration.
- Expose integrations through API classes, Bukkit services, custom events, commands, or documented hooks.
- Public API must be placed under `com.noasaba.<plugin>.api`.
- Do not expose implementation classes as API.
- Do not require other plugins to depend on internal packages.
- Breaking public API changes require a major version bump.
- Backward-compatible public API additions require a minor version bump.

Public API should document:

- nullability
- thread-safety
- whether the method must be called on the main server thread
- expected failure behavior

## Versioning rules

- Use Semantic Versioning: `MAJOR.MINOR.PATCH`.
- Any code change must include a version bump.
- Runtime behavior changes require a version bump.
- Patch version for fixes and internal compatible changes.
- Minor version for backward-compatible feature additions or public API additions.
- Major version for breaking API, config, database, command, or permission changes.
- If unsure whether a change requires a version bump, bump the patch version.

Examples:

```text
1.0.0 -> 1.0.1
1.0.0 -> 1.1.0
1.0.0 -> 2.0.0
```

## Command documentation policy

- Each plugin must keep command and permission documentation in `command.md`.
- If README mentions chat commands, the corresponding permissions must also be documented in `command.md`.
- `command.md` must separate general user commands from administrator commands.
- Use `## 一般向けコマンド` for normal player-facing commands.
- Use `## 管理者向けコマンド・権限` for admin, reload, debug, bypass, and maintenance commands.
- Each section must use a Markdown table with these columns:
  - 権限名
  - チャットコマンド
  - 使い方の例
  - 具体的な解説
- If a permission has no direct chat command, leave the チャットコマンド field blank.
- If a command does not require a permission, write `なし` in the 権限名 field.
- When adding, changing, or removing commands or permissions, update `command.md`.
- Command and permission changes require a version bump.
- Breaking command or permission changes require a major version bump.

Recommended `command.md` structure:

```md
# Commands and Permissions

noasaba / nanosize

## 一般向けコマンド

| 権限名 | チャットコマンド | 使い方の例 | 具体的な解説 |
|---|---|---|---|
| `noasaba.land.command.claim` | `/land claim` | `/land claim` | 現在いる場所の土地を保護します。 |

## 管理者向けコマンド・権限

| 権限名 | チャットコマンド | 使い方の例 | 具体的な解説 |
|---|---|---|---|
| `noasaba.land.reload` | `/land reload` | `/land reload` | 設定ファイルを再読み込みします。 |
| `noasaba.land.admin` |  |  | 管理者向けの包括権限です。通常プレイヤーには付与しません。 |
```

## README policy

- README should describe the plugin purpose, setup, and basic usage.
- If README lists chat commands, it must link to `command.md`.
- Detailed command and permission documentation should be kept in `command.md`.
- README must preserve original author credit when the plugin is based on third-party work.

Recommended README command section:

```md
## Commands

主要なチャットコマンド、権限、使用例、詳細説明は [`command.md`](./command.md) を参照してください。
```

## Configuration policy

- Use YAML for default plugin configuration unless another format is explicitly required.
- Config keys should use lowercase kebab-case.
- Do not rename or remove existing config keys without migration logic.
- Default config values must be safe for production.
- Breaking config changes require a major version bump.

Example:

```yml
settings:
  debug: false
  language: ja_jp

messages:
  prefix: "&b[noasaba]&r "

storage:
  type: sqlite
  sqlite-file: data.db
```

## Logging policy

- Use the plugin logger.
- Do not use `System.out.println`.
- Startup logs should include plugin name, version, and `noasaba / nanosize`.
- Do not log secrets, tokens, database passwords, or personal information.
- Error logs must include enough context to identify the failed operation.

Example startup log:

```text
[NoasabaLand] Enabled v1.0.0 - noasaba / nanosize
```

## Storage policy

- Do not store persistent data only in memory.
- Use SQLite as the default local persistent storage unless another storage backend is explicitly required.
- Use MySQL or MariaDB only when cross-server or external storage is required.
- Database schema changes require migration logic.
- Migrations must be idempotent.
- Database work must not run on the main server thread.
- Breaking database schema changes require a major version bump.

## Dependency policy

- Do not add new dependencies without justification.
- Prefer Spigot/Bukkit APIs when sufficient.
- Avoid large libraries for small utility features.
- Runtime dependencies must be documented.
- Use soft dependencies when the plugin can operate without the dependency.
- Use hard dependencies only when the plugin cannot start without them.
- Do not shade Spigot, Bukkit, Paper, or Minecraft server APIs into the plugin jar.

## Completion report

Final responses must include:

- Changed files
- Version bump status
- API/hook impact
- Config/database migration impact
- command.md update status
- Spigot compatibility status
- Author/credit attribution status
- Commands/checks run
