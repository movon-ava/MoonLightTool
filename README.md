# MoonLightTool 项目文档

MoonLightTool 是一个基于 Java Swing 的桌面工具箱，用于集中管理本地安全工具、文档资料和网址入口。项目重点解决三件事：配置可迁移、工具包可独立更新、运行入口统一管理。

## 主要功能

- 分类管理：左侧树形分类，中间显示当前分类内容，右侧显示详情。
- 多类型入口：支持工具、网址、文档、子分类。
- 启动方式：支持默认打开、CMD、PowerShell、资源管理器定位。
- 自定义启动：支持指定启动程序、参数、工作目录。
- 相对路径：`tools/` 下的工具保存为相对路径，换机器或换目录后仍可识别。
- 工具检测：主界面左下角提供“检测工具”，读取 `tools-manifest.xml` 检查工具缺失状态。
- 工具下载入口：工具清单可配置网盘链接或直链；直链可由程序下载，网盘链接可直接打开。
- 中英文切换：主界面右上角支持 `EN` / `中` 切换。
- 自动备份：配置保存时自动备份到 `data/backup/`。

## 快速开始

从 GitHub 拉取后，建议目录结构如下：

```text
MoonLightTool/
  foxshelf-toolbox.jar
  data/
    toolbox-config.xml
  tools/
  tools-manifest.xml
```

运行：

```powershell
java -jar .\foxshelf-toolbox.jar
```

如果 `tools/` 不存在或不完整，打开程序后点击左下角“检测工具”，根据提示从网盘下载并解压工具包。

## 构建

推荐使用项目自带脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

构建产物：

```text
foxshelf-toolbox.jar
```

手动编译：

```powershell
$out = Join-Path $PWD "out"
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = Get-ChildItem -Path (Join-Path $PWD "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $out $sources
```

## 路径规则

项目不再依赖固定的 `G:\MoonLight\tools`。

默认工具目录：

```text
tools
```

路径解析规则：

- `tools/java/demo/demo.jar` 会解析为项目根目录下的 `tools/java/demo/demo.jar`
- `${APP_HOME}/data/file.txt` 会解析为项目根目录下的文件
- `${TOOLS}/java/demo/demo.jar` 会解析为当前工具根目录下的文件
- 外部绝对路径仍会保留，例如 `C:\Windows\System32\notepad.exe`
- URL 不会做路径转换

编辑内容时，如果选择的文件位于工具目录内，程序会自动保存为相对路径，便于提交和迁移。

## 工具包管理

`tools/` 目录包含 JDK、反编译器、分析器等大体积第三方工具，部分单文件超过 GitHub 普通仓库 100MB 限制，因此不直接提交到 Git。

推荐做法：

1. 将完整 `tools/` 压缩为工具包，上传到网盘。
2. 在 `tools-manifest.xml` 中填写网盘链接或直链。
3. GitHub 仓库只提交源码、配置、jar、工具清单。
4. 新环境拉取项目后，通过“检测工具”打开下载链接并解压。

### tools-manifest.xml

工具清单使用 Java XML 编码格式，示例：

```xml
<object class="toolbox.model.ToolManifestEntry">
  <void property="id">
    <string>tools-archive</string>
  </void>
  <void property="name">
    <string>完整 tools 工具包</string>
  </void>
  <void property="version">
    <string>2026-07-02</string>
  </void>
  <void property="path">
    <string>tools</string>
  </void>
  <void property="url">
    <string>https://example.com/replace-with-your-cloud-drive-link</string>
  </void>
</object>
```

字段说明：

- `id`：工具唯一标识
- `name`：显示名称
- `version`：版本号
- `path`：工具文件或目录路径，建议使用相对路径
- `url`：网盘链接或直链
- `sha256`：可选，直链下载后用于校验
- `size`：可选，文件大小校验
- `description`：说明

## 自定义启动

自定义启动适合需要指定运行时的工具，例如不同 Java、Python、Node.js。

### Java 示例

```text
目标: tools/java/my-tool/my-tool.jar
启动程序: tools/language/java/jdk8/bin/java.exe
参数: -jar {target}
```

### Python 示例

```text
目标: tools/python/demo/scan.py
启动程序: C:\Python311\python.exe
参数: {target} --debug
```

### Node.js 示例

```text
目标: tools/node/server/server.js
启动程序: C:\Program Files\nodejs\node.exe
参数: {target}
```

占位符：

- `{target}`：目标的实际可执行路径或 URL
- `{targetPath}`：同 `{target}`，保留给兼容配置使用
- `{name}`：当前内容名称
- `{launcher}`：启动程序实际路径
- `{workingDir}`：工作目录实际路径

## 配置和备份

主配置：

```text
data/toolbox-config.xml
```

自动备份：

```text
data/backup/
```

最新备份标记：

```text
data/backup/LATEST_BACKUP.txt
```

## Git 提交策略

会提交：

- `src/`
- `README.md`
- `pom.xml`
- `build.ps1`
- `run.ps1`
- `foxshelf-toolbox.jar`
- `data/toolbox-config.xml`
- `tools-manifest.xml`

不会提交：

- `AI_HANDOVER_REPORT.md`
- `tools/`
- `tools*.7z`
- `data/backup/`
- `data/launcher-logs/`
- `.backup/`
- `.m2/`
- `bug/`

## 推荐更新流程

更新源码：

```powershell
git pull
```

更新工具包：

1. 从网盘下载最新 `tools` 工具包。
2. 解压覆盖项目根目录下的 `tools/`。
3. 打开程序，点击“检测工具”确认缺失状态。

更新配置：

1. 在程序中新增或编辑内容。
2. 确认工具路径保存为 `tools/...` 相对路径。
3. 提交 `data/toolbox-config.xml`。
