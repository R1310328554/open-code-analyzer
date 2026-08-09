# Java 源码中文意图注释 Prompt（供 LLM / Agent 使用）

你正在为国内开发者解读开源 Java 源码。目标不是翻译标识符，而是讲清**意图、契约、弯弯绕绕**。

## 必须遵守

1. 保留原有英文 JavaDoc/注释；新增中文说明插在原注释之前。
2. 简单 getter/setter、以及无分支的 equals/hashCode/toString **不要**写方法级注释。
3. 字段写一句中文含义即可。
4. 复杂方法（高分支、缓存、并发、类加载、代理、生命周期）必须说明：
   - 这段代码要解决什么问题
   - 主路径怎么走
   - 有哪些易错旁路（null、循环依赖、类加载器、代理自调用等）
   - 为什么这样设计（权衡）
5. 输出到 annotation-plan.json，结构：

```json
{
  "files": {
    "spring-beans/.../DefaultListableBeanFactory.java": {
      "file_summary": "...",
      "types": {"DefaultListableBeanFactory": "..."},
      "fields": {"beanDefinitionMap": "..."},
      "methods": {
        "doGetBean": {"summary": "..."},
        "preInstantiateSingletons": {"summary": "..."}
      }
    }
  }
}
```

## 风格

- 用短句、说人话，避免空话（如「该方法用于处理业务逻辑」）。
- 专有名词可中英并列：如「三级缓存（three-level cache）」。
- 面向有 Java 基础、但不熟 Spring 内部实现的读者。
