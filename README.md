# MoonLight Toolbox

A Java Swing desktop toolbox for organizing local tools, documents, and web links.

## What changed

- Tool entries now support two launcher modes:
  - `System`: open with the system default behavior.
  - `Custom`: start the entry with a specific executable, such as a different `java.exe`, `python.exe`, `node.exe`, or any custom binary.
- Custom launchers support:
  - launcher executable path
  - launch arguments
  - working directory
  - placeholders in arguments: `{target}`, `{name}`, `{launcher}`
- Every config save now creates an automatic backup.

## Default tools root

- Default built-in tools root: `G:\MoonLight\tools`
- The entry editor now provides a `Create Template` button.
- It creates a standard directory under the tools root and writes a `README.txt` with launcher metadata.

Example buckets created automatically:

```text
tools\
  java\
  python\
  node\
  native\
  custom\
  document\
```

## Config and backups

- Main config file: `data/toolbox-config.xml`
- Backup directory: `data/backup`
- Latest backup marker: `data/backup/LATEST_BACKUP.txt`

Each save writes a timestamped backup like:

```text
data/backup/toolbox-config-20260630-201530-123.xml.bak
```

## Launcher examples

Use custom launcher mode when you need different runtimes.

### Java 8

- Target: `G:\tools\my-app.jar`
- Launcher: `E:\java\jdk1.8.0_202\bin\java.exe`
- Arguments: `-jar {target}`

### Java 17

- Target: `G:\tools\my-app.jar`
- Launcher: `E:\java\jdk-17\bin\java.exe`
- Arguments: `-jar {target}`

### Python

- Target: `G:\tools\scan.py`
- Launcher: `C:\Python311\python.exe`
- Arguments: `{target} --debug`

### Node.js

- Target: `G:\tools\server.js`
- Launcher: `C:\Program Files\nodejs\node.exe`
- Arguments: `{target}`

## Template generation

When adding or editing a local tool/document entry:

1. Fill in the entry name and type.
2. Optionally set a custom launcher.
3. Click `Create Template`.

The toolbox will create a directory such as:

```text
G:\MoonLight\tools\java\my-tool\
```

And generate:

- `README.txt`
- `bin\` and `data\` for tool entries

## Build

```powershell
$out = Join-Path $PWD "out"
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = Get-ChildItem -Path (Join-Path $PWD "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $out $sources
```

## Run

```powershell
java -jar .\foxshelf-toolbox.jar
```

Or package first with the existing build flow if your environment allows script execution.
