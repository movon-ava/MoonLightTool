# MoonLightTool

MoonLightTool 是一个基于 Java Swing 的桌面工具箱，用于按分类管理本地工具、文档和网址入口。

## 功能概览

- 分类管理：左侧分类树管理工具、网址、文档等内容。
- 内容详情：中间列表展示当前分类内容，右侧查看目标、位置、标签、说明和安装配置。
- 启动方式：支持系统默认打开、CMD、PowerShell、资源管理器定位。
- 自定义启动：可为工具指定独立的启动程序、参数和工作目录。
- 中英文切换：主界面右上角支持 `EN` / `中` 快速切换。
- 自动备份：配置保存时会自动备份历史配置。
- 模板生成：新增工具时可快速生成标准目录结构。

## 启动方式

直接运行已构建的 jar：

```powershell
java -jar .\foxshelf-toolbox.jar
```

如果 PowerShell 禁止执行脚本，可以先调整当前进程策略后再构建：

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

## 构建

项目已提供构建脚本：

```powershell
.\build.ps1
```

构建成功后会生成：

```text
foxshelf-toolbox.jar
```

也可以手动编译：

```powershell
$out = Join-Path $PWD "out"
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = Get-ChildItem -Path (Join-Path $PWD "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $out $sources
```

## 项目结构

```text
src/
  toolbox/
    Main.java
    model/       数据模型
    service/     配置保存、启动服务、模板服务
    ui/          Swing 界面
build.ps1        构建脚本
run.ps1          运行脚本
pom.xml          Maven 配置
```

## 配置和备份

默认配置文件：

```text
data/toolbox-config.xml
```

配置备份目录：

```text
data/backup/
```

每次保存配置时会生成类似下面的备份文件：

```text
data/backup/toolbox-config-20260701-201530-123.xml.bak
```

最新备份标记：

```text
data/backup/LATEST_BACKUP.txt
```

## 工具目录

默认工具根目录：

```text
G:\MoonLight\tools
```

> 注意：`tools/` 目录包含 JDK、反编译器、分析器等大体积第三方工具，部分单文件超过 GitHub 普通仓库 100MB 限制，因此默认不提交到 Git。  
> 从 GitHub 拉取源码后，需要自行准备 `tools/` 目录，或从单独的 Release/网盘归档下载后解压到 `G:\MoonLight\tools`。

模板生成会按类型创建目录，例如：

```text
tools\
  java\
  python\
  node\
  native\
  custom\
  document\
```

## 自定义启动示例

### Java

```text
目标: G:\tools\my-app.jar
启动程序: E:\java\jdk1.8.0_202\bin\java.exe
参数: -jar {target}
```

### Python

```text
目标: G:\tools\scan.py
启动程序: C:\Python311\python.exe
参数: {target} --debug
```

### Node.js

```text
目标: G:\tools\server.js
启动程序: C:\Program Files\nodejs\node.exe
参数: {target}
```

## 参数占位符

自定义启动参数支持：

- `{target}`：当前内容目标路径或 URL
- `{name}`：当前内容名称
- `{launcher}`：启动程序路径

## Git 提交说明

仓库只提交源码、构建脚本和文档。

已提交的运行配置：

- `data/toolbox-config.xml`

以下内容默认不推送：

- `AI_HANDOVER_REPORT.md`
- `tools/`
- `data/backup/`
- `.backup/`
- `.m2/`
- `bug/`
- `*.jar`
