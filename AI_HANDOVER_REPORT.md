# AI Handover Report

## Project summary

- Project type: Java Swing desktop toolbox
- Main entry: `src/toolbox/Main.java`
- Core config file: `data/toolbox-config.xml`
- Persistence: `java.beans.XMLEncoder` / `XMLDecoder`

## Current architecture

- `toolbox.model`
  - `ToolboxConfig`: root config object
  - `ToolboxNode`: tree node for folder/tool/web/document entries
  - `NodeType`: node type enum
  - `AppSettings`: app-level settings
  - `LauncherType`: newly added launcher type enum
- `toolbox.service`
  - `ConfigService`: load/save config and now auto-backup before each save
  - `LauncherService`: open entries with system mode or custom launcher mode
- `toolbox.ui`
  - `MainFrame`: main UI
  - `EntryEditorDialog`: create/edit entries, now supports custom launcher fields
  - `SettingsDialog`: app settings

## Changes made in this round

### 0. Localized add-entry dialog and moved build temp output

Reason for the issue:

- The `新增内容` / `新增分类` editor dialog was still using hard-coded English labels, buttons, prompts, and chooser titles inside `EntryEditorDialog`.
- The build script was writing Java compile intermediates to a fixed `out` directory under the project root, which caused temporary compile directories to accumulate in the repo workspace.

What changed:

- Localized the add/edit entry dialog UI strings in `EntryEditorDialog` to Chinese:
  - field labels
  - action buttons
  - validation messages
  - template creation messages
  - file chooser titles
  - launcher mode display names
- Updated `LauncherType` display names:
  - `SYSTEM_OPEN` -> `系统默认`
  - `CUSTOM_COMMAND` -> `自定义命令`
- Changed `build.ps1` so compile intermediates are now created under the system temp directory:
  - temp root: `%TEMP%\\MoonLightBuild`
  - unique per-run subdirectory via GUID
  - automatic cleanup in `finally`

Behavioral boundary:

- No launch logic, config logic, template generation logic, or data model behavior was changed.
- Final output jar path remains `G:\MoonLight\foxshelf-toolbox.jar`.
- Existing historical `out*` directories in the project root were left untouched on purpose.

### 1. Per-entry custom launcher support

Added to `ToolboxNode`:

- `launcherType`
- `launcherPath`
- `launchArguments`
- `workingDirectory`
- `usesCustomLauncher()`

Added enum:

- `LauncherType.SYSTEM_OPEN`
- `LauncherType.CUSTOM_COMMAND`

Behavior:

- Web entries still use system browser behavior.
- Tool/document entries can now use:
  - system open
  - custom executable + argument template + working directory

Supported placeholder substitution in launch arguments:

- `{target}`
- `{name}`
- `{workingDir}`
- `{launcher}`

### 2. Automatic backups on every save

Implemented in `ConfigService.save(...)`:

- Before overwriting `data/toolbox-config.xml`, create a backup copy if the file already exists.
- Backup location: `data/backup`
- Naming format: `toolbox-config-yyyyMMdd-HHmmss-SSS.xml.bak`
- Also writes `data/backup/LATEST_BACKUP.txt`

### 3. Entry editor updated

`EntryEditorDialog` now includes:

- launcher type selector
- launcher executable path
- launch arguments
- working directory
- template directory creation button

### 4. Default tools root and template generation

Default root:

- `G:\MoonLight\tools`

Implemented through:

- legacy constructor fallback in `EntryEditorDialog`
- `ToolTemplateService`

Behavior:

- local tool/document entries can create a template directory directly from the editor
- directory bucket is inferred from launcher/runtime:
  - `java`
  - `python`
  - `node`
  - `native`
  - `custom`
  - `document`
- each template gets a `README.txt`
- tool entries also get `bin/` and `data/`

Validation added:

- custom launcher mode requires non-empty launcher path
- target remains required for non-folder entries

## Files changed

- `src/toolbox/model/LauncherType.java`
- `src/toolbox/model/ToolboxNode.java`
- `src/toolbox/service/ConfigService.java`
- `src/toolbox/service/LauncherService.java`
- `src/toolbox/service/ToolTemplateService.java`
- `src/toolbox/ui/EntryEditorDialog.java`
- `src/toolbox/ui/MainFrame.java`
- `README.md`

## Backups created before modification

Directory:

- `.backup/20260630-pre-launcher`
- `.backup/20260701-104333-pre-dialog-temp-fix`
- `.backup/20260701-104351-pre-launcher-type-fix`

Contains source backups for:

- `ToolboxNode.java`
- `LauncherService.java`
- `ConfigService.java`
- `EntryEditorDialog.java`
- `MainFrame.java`
- `README.md`
- `build.ps1`
- `AI_HANDOVER_REPORT.md`
- `LauncherType.java`

## Verification completed

- UI localization spot checks completed with UTF-8 file reads for:
  - `EntryEditorDialog.java`
  - `LauncherType.java`

- Build-script behavior review completed:
  - compile output path is no longer project-root `out`
  - temp build folder is now system temp + GUID
  - cleanup is handled in `finally`

- Direct execution of `build.ps1` could not be completed in the current environment because local PowerShell execution policy blocked script startup before the script body ran.

Expected compile command remains:

```powershell
javac -encoding UTF-8 -d <temp-out> <all-java-sources>
```

## Additional fixes on 2026-07-01

### Custom launcher troubleshooting

- Confirmed the reported custom-launch failure was reproducible with the screenshot configuration.
- Root cause in the sample config:
  - launcher executable: `java.exe`
  - arguments: `java -jar java-chains.jar`
  - this makes `java` become the first runtime argument and fails with:
    - `Error: Could not find or load main class java`
- Verified the correct argument form for that case is:
  - `-jar java-chains.jar`

### Custom launcher runtime behavior

Updated `LauncherService` so custom launch entries now:

- validate launcher path existence before start
- keep `CMD` / `PowerShell` windows open for troubleshooting
- write default custom-launch output/error logs to:
  - `data/launcher-logs`
- if a custom launcher exits quickly with a non-zero exit code in default mode, surface:
  - command
  - working directory
  - exit code
  - log file path
  - recent log excerpt

### Script target working-directory fix

Updated file-target launch behavior for `CMD` / `PowerShell` modes:

- when the target is a script or file path, launch now first switches into the target file's parent directory
- this prevents relative paths inside batch files such as:
  - `java -jar java-chains.jar`
- from failing just because the toolbox process was started from a different current directory

### Default-open script handling

Updated default open behavior for script targets:

- `.bat`, `.cmd`, `.ps1` targets no longer rely on `Desktop.open(...)`
- they are now launched explicitly through `cmd` / `powershell`
- launch always switches into the script file's parent directory first
- this fixes default-open failures for tools that depend on relative paths inside their startup scripts

### Default-open executable working directory

Updated default open behavior for `.exe` targets:

- executable targets now start through `ProcessBuilder`
- working directory is explicitly set to the executable file's parent directory
- this fixes wrapper executables such as `start.exe` that internally depend on sibling `.bat` files or other relative paths

### Build reliability and old-jar prevention

Updated `build.ps1` so it now:

- deletes legacy project-root compile temp directories before rebuild:
  - `out`
  - `out8`
  - `out-rebuild`
  - `target-check`
  - `out-check*`
- compiles only to system temp
- builds jar to a temp file first, then moves it into place
- treats jar replacement failure as a hard build failure
- prints a clear message if `foxshelf-toolbox.jar` is locked by a running process

Important note:

- A prior false-positive build result was caused by `foxshelf-toolbox.jar` being held open by a running `javaw.exe` process while PowerShell continued after a non-terminating file replacement error.
- This has now been corrected by forcing terminating behavior in the build script.

## Additional backups created

- `.backup/20260701-112602-pre-launch-fix`
- `.backup/20260701-113517-pre-build-lock-fix`

## Known limitations

### MainFrame UI integration is partial

Behavioral support is complete enough for editing, saving, loading, and launching custom runtime entries.

But `MainFrame` was only minimally touched because the file contains pre-existing encoding-corrupted UI strings, which makes broad safe patching expensive and error-prone.

Current status:

- search already includes custom launcher fields
- clone/edit already preserves custom launcher fields
- no dedicated detail-row display for launcher metadata yet
- no dedicated backup-folder button yet

### Existing encoding issues

Several legacy source files contain mojibake/garbled UI text. This is pre-existing technical debt. It does not block compilation, but it makes future surgical patching harder.

Recommended next step:

1. Normalize all source files to UTF-8 with reviewed string literals.
2. Then finish `MainFrame` UI enhancements:
   - show launcher mode/path/args in detail pane
   - add "Open backup folder" action
   - optionally add restore-backup UI

## Suggested next tasks for another AI

1. Complete `MainFrame` detail panel support for launcher metadata.
2. Add a backup restore workflow:
   - list backups
   - preview selected backup path/time
   - restore with confirmation
3. Add launcher presets:
   - Java `-jar {target}`
   - Python `{target}`
   - Node `{target}`
4. Add import/export support for config snapshots.
5. Normalize UI text language and encoding across the repo.

## Important assumptions

- The user wants automatic backup on each config mutation.
- Different Java versions should be handled by per-entry custom executable paths, not a global runtime manager.
- Cross-language support is intentionally generic through `CUSTOM_COMMAND`, not hard-coded by language.

## 2026-07-01 global language switch and label update

User request:

- Change `内容类型` to `类型`.
- Change `种类` to `分类`.
- Add global Chinese/English switching.

Implemented:

- Added `src/toolbox/ui/I18n.java` for lightweight UI text switching.
- Added `language` to `AppSettings`, persisted through the existing XML config flow.
- Added a language selector to `SettingsDialog`.
- Routed main window, detail pane, context menus, editor dialog labels, validation prompts, chooser titles, and settings dialog labels through `I18n`.
- Chinese mode now displays detail labels as `类型` and `分类`.
- English mode switches the app chrome and dialogs to English while preserving user-created category and entry names.

Backup created:

- `.backup/20260701-194147-pre-i18n-labels`

Verification:

- `javac -encoding UTF-8` compilation check passed after the changes.

## 2026-07-01 visible language switch and text rendering update

User feedback:

- The UI looked unchanged because language switching was only inside Settings.
- Text looked slightly blurry at lower resolutions.

Implemented:

- Added a visible `EN` / `中` language toggle button to the hero action bar.
- Existing configs that still held the default English title/welcome text now auto-normalize to the selected language.
- Added Java2D/Swing text antialiasing startup properties in `Main`.

Backup created:

- `.backup/20260701-200128-pre-visible-language-aa`

Verification:

- `javac -encoding UTF-8` compilation check passed.

## 2026-07-01 backup pruning and jar rebuild

User request:

- Delete old backups and keep only three.
- Rebuild the jar package.

Implemented:

- Created a pre-operation report backup at `.backup/20260701-201940-pre-prune-rebuild`.
- Pruned `.backup` to the latest three backup directories:
  - `.backup/20260701-201940-pre-prune-rebuild`
  - `.backup/20260701-200750-pre-add-delay-layout`
  - `.backup/20260701-200128-pre-visible-language-aa`
- Rebuilt `foxshelf-toolbox.jar` successfully.

## 2026-07-01 Git installation and GitHub push preparation

User request:

- Install Git to `D:\git`.
- Push this project to `https://github.com/movon-ava/MoonLightTool.git`.

Implemented:

- Installed Git for Windows `2.55.0.windows.1` to `D:\git`.
- Initialized the project as a Git repository on branch `main`.
- Added `.gitignore` to exclude local runtime assets and large/generated files:
  - `tools/`
  - `data/`
  - `.backup/`
  - `.m2/`
  - `bug/`
  - `*.jar`
- Configured local repository identity as:
  - `user.name=movon-ava`
  - `user.email=movon-ava@users.noreply.github.com`
- Added remote:
  - `origin=https://github.com/movon-ava/MoonLightTool.git`

Current push status:

- Local commit was created.
- Push to GitHub is blocked by HTTPS/TLS connectivity issues from Git:
  - Schannel reported `server closed abruptly (missing close_notify)`.
  - OpenSSL backend reported inability to connect to `github.com:443`.

## 2026-07-01 add-entry refresh and layout adjustments

User request:

- New entries appeared only after a short delay.
- Remove the top-right category count.
- Move the language switch button to the top-right.
- Move the right detail pane Edit/Delete buttons to the bottom.

Implemented:

- `saveAndRefresh` now refreshes the UI immediately and persists config asynchronously on a daemon save thread.
- Config saves are serialized with a `saveLock` to avoid overlapping XML writes.
- Removed the top-right category count card.
- Moved the `EN` / `中` language switch into the top-right hero area.
- Moved detail-pane Edit/Delete actions to the bottom of the right panel.

Backup created:

- `.backup/20260701-200750-pre-add-delay-layout`

Verification:

- `javac -encoding UTF-8` compilation check passed.
