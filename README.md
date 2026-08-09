# open-code-analyzer

各类著名开源软件项目、框架的**架构分析**、关键技术详解、逐行解读。

本仓库提供一套**命令行分析框架（无 UI）**：你给出项目地址与版本，框架按固定步骤完成拉取、静态分析、中文意图注释与架构文档生成。

也兼容/可衔接外部 AI 工具与开源项目，例如：

- [graphify](https://github.com/safishamsi/graphify) —— 代码知识图谱
- [Understand-Anything](https://github.com/Egonex-AI/Understand-Anything) —— 多代理理解管线与图谱

---

## 目录约定

每个开源项目一个顶层目录；其下按**版本号**组织：

```text
<project>/
  <version>/
    original/     # 对应版本原始源码（只读，不做改动）
    analyzed/     # 与 original 同结构；在复杂类/方法上叠加中文意图解析
    架构说明/
      项目模块说明/
      意图架构.md
      核心调用链.md
      ...
    _reports/     # 扫描/复杂度/注释计划等静态分析产物
    META.json     # 仓库、版本、git ref 元数据
```

示例：分析 Spring Framework 最新 Release（当前为 **7.0.8**）后：

```text
springframework/
  7.0.8/
    original/
    analyzed/
    架构说明/
      项目模块说明/
```

### 注释策略（重要）——直接改 `analyzed` 源码

本项目的**主目标**是让国内开发者直接阅读中文注释版源码，因此 `analyzed/` 不是只写架构文档，而是**改文件内容**：

1. **已有英文 JavaDoc / `//` 注释 → 翻译成中文**（保留 `{@link}` / `@param` 等标签结构）
2. **每个字段**补充中文注释（说明含义与意图）
3. **每个方法**补充中文注释；复杂方法在方法体内关键步骤加中文行内注释
4. 版权 License 头保持英文不动
5. `original/` 永远只读对照；所有改动只发生在 `analyzed/`

```bash
# 推荐：从 original 重建 analyzed 并做中文翻译/补注释
./bin/localize-zh.sh springframework 7.0.8 --force-sync
# 或只处理某些模块
./bin/localize-zh.sh springframework 7.0.8 --modules spring-jdbc,spring-beans
```

---

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
# 可选：复杂度分析（强烈建议）
pip install lizard
```

### 2. 一键分析某个项目

```bash
# 最新 Release
./bin/analyze-project.sh springframework latest

# 指定版本
./bin/analyze-project.sh https://github.com/spring-projects/spring-framework 7.0.8

# org/repo 形式
./bin/analyze-project.sh spring-projects/spring-boot latest

# 限制注释文件数（大仓可先小后大）
./bin/analyze-project.sh springframework latest --max-files 400
```

等价于：

```bash
./bin/oca analyze <project|url|org/repo> [version|latest]
```

### 3. 分步命令

```bash
./bin/oca resolve springframework latest   # 只解析，不落盘
./bin/oca fetch   springframework 7.0.8    # depth=1 浅克隆到 original/
./bin/oca scan    springframework 7.0.8    # 模块/语言扫描
./bin/oca complexity springframework 7.0.8 # 复杂度热点 + 意图候选
./bin/oca arch    springframework 7.0.8    # 生成 架构说明/
./bin/oca annotate springframework 7.0.8 --max-files 400
```

### 4. 可选外部工具

```bash
./bin/integrate-external-tools.sh springframework 7.0.8
```

未安装时会写入安装说明到 `_reports/external-tools/`。

---

## 模糊目标时的确认规则

| 你说的话 | 框架行为 |
| --- | --- |
| 分析 springframework 最新源码 | 解析为 [spring-projects/spring-framework](https://github.com/spring-projects/spring-framework) 最新 **Release** |
| 分析 spring 最新源码 | **模糊**：提示确认具体仓库（framework / boot / cloud ...） |
| 分析 java 最新源码 | **模糊**：提示确认 OpenJDK / Log4j / 其它具体项目 |
| 给出明确 GitHub 链接 | 分析其最新 Release；若无 Release，则分析默认分支 tip（`depth=1`） |

---

## 分析流水线（逻辑）

```text
resolve  →  确认仓库 + 版本（Release / tip / 显式）
   ↓
fetch    →  git clone --depth=1，写入 original/（去掉 .git）
   ↓
scan     →  模块识别、语言分布
   ↓
complexity → lizard/启发式复杂度，过滤 trivial 方法
   ↓
arch     →  架构说明/ + 项目模块说明/
   ↓
annotate →  original 同步到 analyzed，按计划+热点写入中文解析
```

配置见 [`config/defaults.yaml`](config/defaults.yaml)。  
注释计划（可手写/LLM 生成）见各版本 `_reports/annotation-plan.json`，Prompt 模板见 [`prompts/annotation_java.md`](prompts/annotation_java.md)。

---

## 已分析样例：Spring Framework 7.0.8

- 仓库：https://github.com/spring-projects/spring-framework
- 版本：`v7.0.8`（分析时最新 Release）
- 规模：约 1.1 万文件 / 9k+ Java 等代码文件
- 产物：
  - [`springframework/7.0.8/架构说明/`](springframework/7.0.8/架构说明/)
  - [`springframework/7.0.8/架构说明/项目模块说明/`](springframework/7.0.8/架构说明/项目模块说明/)
  - [`springframework/7.0.8/架构说明/国内开发者避坑指南.md`](springframework/7.0.8/架构说明/国内开发者避坑指南.md)
  - [`springframework/7.0.8/_reports/complexity.md`](springframework/7.0.8/_reports/complexity.md)
  - `analyzed/` 中 400+ 高价值文件已加中文意图注释；核心调用链含行内导读

推荐阅读顺序：

1. `架构说明/意图架构.md`
2. `架构说明/核心调用链.md`
3. `架构说明/国内开发者避坑指南.md`
4. `analyzed/.../AbstractApplicationContext.java` → `doGetBean` → 三级缓存 → `DispatcherServlet`

---

## 仓库结构（框架本身）

```text
bin/                      # CLI 脚本
framework/oca/            # Python 分析框架
config/defaults.yaml
prompts/
scripts/                  # 针对特定项目的增强脚本（如 Spring 导读注入）
requirements.txt
springframework/          # 分析产物示例
```

---

## 设计原则

1. **静态结果可复查**：报告与注释落盘，不依赖线上 UI
2. **只读 original**：永远可对照上游
3. **复杂处说人话**：面向国内开发者，讲清弯弯绕绕与设计权衡
4. **可扩展**：annotation-plan / LLM / graphify / Understand-Anything 都是增强层，不是硬依赖
