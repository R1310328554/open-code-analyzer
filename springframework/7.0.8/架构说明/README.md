# springframework 7.0.8 架构说明

## 基本信息

- 仓库: [spring-projects/spring-framework](https://github.com/spring-projects/spring-framework)
- 分析版本: **7.0.8**（git ref: `v7.0.8`, 来源: release）
- 原始源码: `/workspace/springframework/7.0.8/original`（只读）
- 注释分析: `/workspace/springframework/7.0.8/analyzed`

## 分析目标

本目录用于帮助国内开发者快速建立「意图架构」心智模型：
不是罗列 API，而是讲清楚模块边界、核心调用链、以及复杂实现为何如此设计。

## 规模速览

- 文件总数: 11286
- 代码文件: 9573
- 语言: java=9154, xml=691, asciidoc=470, kotlin=388, properties=89, sql=80, gradle=34, yaml=24, groovy=17, javascript=10, markdown=8, ruby=2

## 模块依赖心智图（核心容器）

```text
spring-core
    ↑
spring-beans   ← IoC/DI 真正落地
    ↑
spring-context ← ApplicationContext / 注解配置 / 事件
    ↑
应用与生态（MVC / WebFlux / TX / JDBC / AOP ...）
```

## 推荐阅读顺序（意图优先）

1. `spring-core` 资源与类型工具（知其底座）
2. `spring-beans` DefaultListableBeanFactory 生命周期
3. `spring-context` refresh() 与配置类处理
4. `spring-aop` 代理拦截链
5. `spring-web` + `spring-webmvc` 请求主链路
6. 按需：`spring-tx` / `spring-jdbc` / `spring-webflux`

## 子文档

- [意图架构.md](./意图架构.md) —— Spring 到底在解决什么
- [核心调用链.md](./核心调用链.md) —— refresh / doGetBean / doDispatch
- [国内开发者避坑指南.md](./国内开发者避坑指南.md) —— 循环依赖、事务自调用等
- [项目模块说明/](./项目模块说明/) —— 各模块说明
- 静态扫描报告见 `../_reports/`
