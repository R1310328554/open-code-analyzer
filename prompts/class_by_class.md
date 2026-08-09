# 逐类精读中文化（给 Agent）

## 目标

每次只处理 **一个** Java 类。先读懂，再改 `analyzed/` 注释。

## 步骤

1. 读取 `_reports/class-queue/CURRENT.json` 与 `work/packet.json`
2. 打开 `original/` 下对应源码，**通读**类注释、字段、方法
3. 用自己的话概括：
   - 这个类解决什么问题
   - 关键字段各自含义
   - 复杂方法的主路径 / 旁路
4. 改写 `analyzed/` 同路径文件：
   - **文件顶部版权 / 法律声明（Apache License 等）原样保留英文，不要翻译、不要改动**
   - `package-info.java` 若仅有版权声明则不动；有包说明 JavaDoc 再译中文
   - 英文 JavaDoc → 通顺中文（保留 `{@link}`、`@param` 名、`@author`、`@since`）
   - 每个字段一句中文
   - 每个方法有中文说明；复杂方法体内关键步骤加 `//` 中文
   - 简单 getter/setter 可极简，但不能不写（若原本有英文则译中文）
5. 执行 `./bin/class-mark-done.sh <project> <version>`

## 禁止

- 翻译或改写版权 / 法律声明头
- 调用批量机翻 / 正则查找替换英文注释
- 不看代码只译字面
- 一次处理多个类
