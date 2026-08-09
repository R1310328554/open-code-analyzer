from __future__ import annotations

from pathlib import Path

from ..resolve import ResolvedTarget
from ..static.scanner import ModuleInfo, ScanResult
from ..util.fs import write_text
from ..util.paths import ProjectLayout


SPRING_MODULE_ZH = {
    "spring-core": {
        "title": "核心工具与底层抽象",
        "summary": (
            "整个框架的地基：资源抽象（Resource）、类型转换、反射/MethodHandle 工具、"
            "编解码、环境属性占位符、任务调度基础等。几乎所有模块都依赖它。"
        ),
        "key_packages": [
            "org.springframework.core",
            "org.springframework.core.io",
            "org.springframework.core.env",
            "org.springframework.core.convert",
            "org.springframework.util",
        ],
        "tricky": [
            "GenericTypeResolver / ResolvableType：泛型擦除下的类型解析弯弯绕绕最多",
            "SimpleAliasRegistry / ConcurrentReferenceHashMap：别名与缓存并发细节",
            "SpringProperties / NativeDetector：运行时能力探测与特性开关",
        ],
    },
    "spring-beans": {
        "title": "IoC 容器（BeanFactory）",
        "summary": (
            "Bean 定义、作用域、依赖注入、FactoryBean、属性编辑器等。"
            "DefaultListableBeanFactory 是经典 IoC 实现，理解 Spring 必读。"
        ),
        "key_packages": [
            "org.springframework.beans",
            "org.springframework.beans.factory",
            "org.springframework.beans.factory.support",
            "org.springframework.beans.factory.config",
            "org.springframework.beans.factory.xml",
        ],
        "tricky": [
            "doGetBean / createBean / populateBean / initializeBean：完整生命周期",
            "依赖解析中的循环依赖三级缓存（singletonFactories / earlySingletonObjects）",
            "AutowireCandidateResolver 与 @Autowired/@Qualifier 的候选筛选",
        ],
    },
    "spring-context": {
        "title": "应用上下文与企业级特性",
        "summary": (
            "ApplicationContext 在 BeanFactory 之上增加：国际化、事件、资源模式解析、"
            "注解配置（@Configuration/@ComponentScan）、Environment、JMX 等。"
        ),
        "key_packages": [
            "org.springframework.context",
            "org.springframework.context.annotation",
            "org.springframework.context.support",
            "org.springframework.context.event",
        ],
        "tricky": [
            "ConfigurationClassPostProcessor：@Configuration 的解析/增强/导入",
            "AnnotationConfigApplicationContext 刷新流程 refresh()",
            "事件广播与 @EventListener 适配",
        ],
    },
    "spring-expression": {
        "title": "Spring 表达式语言（SpEL）",
        "summary": "在注解与配置中嵌入可求值表达式；解析器、AST、求值上下文是核心。",
        "key_packages": ["org.springframework.expression", "org.springframework.expression.spel"],
        "tricky": ["SpelExpressionParser 与编译型表达式", "EvaluationContext 变量/根对象/类型定位"],
    },
    "spring-aop": {
        "title": "面向切面编程",
        "summary": "Proxy / Advisor / Advice / Pointcut 抽象，以及 JDK/CGLIB 代理基础设施。",
        "key_packages": [
            "org.springframework.aop",
            "org.springframework.aop.framework",
            "org.springframework.aop.support",
        ],
        "tricky": [
            "JdkDynamicAopProxy vs CglibAopProxy 选择与拦截链",
            "ExposeInvocationInterceptor 与自调用（self-invocation）失效问题",
        ],
    },
    "spring-aspects": {
        "title": "AspectJ 编译期/加载期织入支持",
        "summary": "与 AspectJ 集成的注解切面与织入相关支持。",
        "key_packages": ["org.springframework.beans.factory.aspectj", "org.springframework.cache.aspectj"],
        "tricky": ["与 spring-aop 运行时代理模型的差异：织入时机不同"],
    },
    "spring-web": {
        "title": "Web 通用抽象",
        "summary": "HttpMessageConverter、编解码、客户端、CORS、绑定等，MVC 与 WebFlux 共用大量概念。",
        "key_packages": ["org.springframework.http", "org.springframework.web", "org.springframework.web.bind"],
        "tricky": ["内容协商 ContentNegotiation", "HandlerMethod 参数解析体系"],
    },
    "spring-webmvc": {
        "title": "Servlet 体系 Spring MVC",
        "summary": "DispatcherServlet 前端控制器模型：映射、适配、视图解析、异常处理。",
        "key_packages": ["org.springframework.web.servlet"],
        "tricky": [
            "DispatcherServlet#doDispatch 主流程",
            "RequestMappingHandlerMapping 条件组合（path/method/params/headers/consumes/produces）",
        ],
    },
    "spring-webflux": {
        "title": "响应式 Web 栈",
        "summary": "基于 Reactive Streams 的非阻塞 Web 框架，可运行在 Netty 等服务器。",
        "key_packages": ["org.springframework.web.reactive"],
        "tricky": ["HandlerMapping/HandlerAdapter 的 reactive 变体", "与 MVC API 形似神异之处"],
    },
    "spring-websocket": {
        "title": "WebSocket 支持",
        "summary": "WebSocket 握手、会话、STOMP 消息代理集成等。",
        "key_packages": ["org.springframework.web.socket"],
        "tricky": ["SockJS 降级与协议协商"],
    },
    "spring-tx": {
        "title": "事务抽象",
        "summary": "PlatformTransactionManager / ReactiveTransactionManager、传播行为、同步回调。",
        "key_packages": ["org.springframework.transaction"],
        "tricky": ["传播行为 REQUIRED/REQUIRES_NEW/NESTED 的真实含义", "事务同步 TransactionSynchronization"],
    },
    "spring-jdbc": {
        "title": "JDBC 支持",
        "summary": "JdbcTemplate、异常转译、DataSource 工具，降低 JDBC 样板代码。",
        "key_packages": ["org.springframework.jdbc"],
        "tricky": ["异常转译 SQLErrorCodeSQLExceptionTranslator", "回调风格与资源释放"],
    },
    "spring-orm": {
        "title": "ORM 集成",
        "summary": "与 JPA 等 ORM 的集成层（EntityManager 管理、异常转译等）。",
        "key_packages": ["org.springframework.orm"],
        "tricky": ["共享 EntityManager 与事务同步"],
    },
    "spring-r2dbc": {
        "title": "R2DBC 支持",
        "summary": "响应式关系数据库访问抽象。",
        "key_packages": ["org.springframework.r2dbc"],
        "tricky": ["与 JDBC 编程模型差异：Publisher 链与连接生命周期"],
    },
    "spring-oxm": {
        "title": "对象/XML 映射",
        "summary": "OXM 抽象及常见实现适配。",
        "key_packages": ["org.springframework.oxm"],
        "tricky": ["Marshaller/Unmarshaller 与命名空间处理"],
    },
    "spring-jms": {
        "title": "JMS 集成",
        "summary": "消息发送/监听容器与事务性会话支持。",
        "key_packages": ["org.springframework.jms"],
        "tricky": ["监听容器并发与确认模式"],
    },
    "spring-messaging": {
        "title": "消息抽象",
        "summary": "Message、MessageChannel、注解驱动消息处理等通用消息模型。",
        "key_packages": ["org.springframework.messaging"],
        "tricky": ["消息转换与 HandlerMethod 参数解析"],
    },
    "spring-test": {
        "title": "测试支持",
        "summary": "Spring TestContext Framework、MockMvc、WebTestClient 等。",
        "key_packages": ["org.springframework.test"],
        "tricky": ["上下文缓存与 @DirtiesContext", "Servlet/Reactive 测试工具差异"],
    },
    "spring-core-test": {
        "title": "核心测试工具",
        "summary": "供框架自身与扩展使用的核心测试夹具/断言支持。",
        "key_packages": ["org.springframework.core.test"],
        "tricky": [],
    },
    "spring-context-support": {
        "title": "上下文扩展支持",
        "summary": "邮件、调度、缓存等额外集成支持。",
        "key_packages": ["org.springframework.mail", "org.springframework.cache", "org.springframework.scheduling"],
        "tricky": ["CacheAbstraction 与具体缓存实现桥接"],
    },
    "spring-context-indexer": {
        "title": "组件索引器",
        "summary": "编译期生成组件索引，加速大型应用的组件扫描。",
        "key_packages": ["org.springframework.context.index"],
        "tricky": ["注解处理器与 META-INF/spring.components"],
    },
    "spring-instrument": {
        "title": "字节码 instrumentation 代理",
        "summary": "Load-time weaving 等所需的 Java agent 支持。",
        "key_packages": ["org.springframework.instrument"],
        "tricky": ["agent 附着时机与类加载器边界"],
    },
}


def _module_doc(mod: ModuleInfo) -> str:
    meta = SPRING_MODULE_ZH.get(mod.name, None)
    lines = [f"# {mod.name}", ""]
    if meta:
        lines += [f"> {meta['title']}", "", "## 模块职责", "", meta["summary"], ""]
        if meta.get("key_packages"):
            lines += ["## 关键包", ""]
            for p in meta["key_packages"]:
                lines.append(f"- `{p}`")
            lines.append("")
        if meta.get("tricky"):
            lines += ["## 弯弯绕绕（建议精读）", ""]
            for t in meta["tricky"]:
                lines.append(f"- {t}")
            lines.append("")
    elif mod.description:
        lines += ["## 模块职责", "", mod.description, ""]
    else:
        lines += ["## 模块职责", "", "（自动识别模块，详见源码与构建脚本。）", ""]

    lines += [
        "## 规模",
        "",
        f"- 路径: `{mod.path}`",
        f"- 文件数: {mod.file_count}",
        f"- 代码文件数: {mod.code_files}",
        f"- 语言分布: {', '.join(f'{k}={v}' for k, v in mod.languages.items()) or 'n/a'}",
        "",
    ]
    return "\n".join(lines)


def generate_architecture_docs(
    layout: ProjectLayout,
    target: ResolvedTarget,
    scan: ScanResult,
    *,
    extra_overview: str | None = None,
) -> None:
    layout.ensure()

    # 总览
    overview = [
        f"# {layout.project} {layout.version} 架构说明",
        "",
        "## 基本信息",
        "",
        f"- 仓库: [{target.owner}/{target.repo}]({target.repo_url})",
        f"- 分析版本: **{layout.version}**（git ref: `{target.git_ref}`, 来源: {target.source}）",
        f"- 原始源码: `{layout.original}`（只读）",
        f"- 注释分析: `{layout.analyzed}`",
        "",
        "## 分析目标",
        "",
        "本目录用于帮助国内开发者快速建立「意图架构」心智模型：",
        "不是罗列 API，而是讲清楚模块边界、核心调用链、以及复杂实现为何如此设计。",
        "",
        "## 规模速览",
        "",
        f"- 文件总数: {scan.total_files}",
        f"- 代码文件: {scan.code_files}",
        f"- 语言: {', '.join(f'{k}={v}' for k, v in list(scan.languages.items())[:12])}",
        "",
        "## 模块依赖心智图（核心容器）",
        "",
        "```text",
        "spring-core",
        "    ↑",
        "spring-beans   ← IoC/DI 真正落地",
        "    ↑",
        "spring-context ← ApplicationContext / 注解配置 / 事件",
        "    ↑",
        "应用与生态（MVC / WebFlux / TX / JDBC / AOP ...）",
        "```",
        "",
        "## 推荐阅读顺序（意图优先）",
        "",
        "1. `spring-core` 资源与类型工具（知其底座）",
        "2. `spring-beans` DefaultListableBeanFactory 生命周期",
        "3. `spring-context` refresh() 与配置类处理",
        "4. `spring-aop` 代理拦截链",
        "5. `spring-web` + `spring-webmvc` 请求主链路",
        "6. 按需：`spring-tx` / `spring-jdbc` / `spring-webflux`",
        "",
        "## 子文档",
        "",
        f"- [{layout.module_docs_dirname}/](./{layout.module_docs_dirname}/) —— 各模块说明",
        f"- 静态扫描报告见 `../{layout.reports_dirname}/`",
        "",
    ]
    if extra_overview:
        overview += ["## 补充解析", "", extra_overview.strip(), ""]

    write_text(layout.arch / "README.md", "\n".join(overview))

    # 模块说明索引 + 每模块文档
    index = [
        f"# {layout.project} {layout.version} 项目模块说明",
        "",
        "按代码规模排序。每个模块文档包含：职责、关键包、弯弯绕绕精读点。",
        "",
    ]
    for mod in sorted(scan.modules, key=lambda m: (-m.code_files, m.name)):
        doc_name = f"{mod.name}.md"
        write_text(layout.module_docs / doc_name, _module_doc(mod))
        index.append(f"- [{mod.name}](./{doc_name}) — 代码文件 {mod.code_files}")
    write_text(layout.module_docs / "README.md", "\n".join(index) + "\n")

    # 意图架构专篇
    intent = f"""# 意图架构：Spring Framework 到底在解决什么？

## 一句话

Spring Framework 的核心意图不是「提供一堆注解」，而是：

> **把对象的创建、依赖关系、横切关注点、运行时环境，从业务代码中拆出去，变成可配置、可扩展、可测试的基础设施。**

## 三层意图

1. **对象生命周期意图（IoC）**  
   谁创建对象？何时初始化？销毁顺序？作用域如何隔离？  
   → `spring-beans` / `spring-context`

2. **协作关系意图（DI + 事件 + 资源）**  
   对象之间如何发现彼此？如何替换实现？如何解耦通知？  
   → 依赖注入、`ApplicationEvent`、`Environment`/`Resource`

3. **横切与集成意图（AOP / TX / Web / Data）**  
   日志、安全、事务、Web 协议、数据访问如何以一致方式挂到业务方法上？  
   → `spring-aop`、`spring-tx`、`spring-web*`、`spring-jdbc`/`orm`/`r2dbc`

## 最容易云里雾里的点（中文解释）

### 1. 「注解」不是魔法，是「元数据 + 后置处理器」

`@Autowired`、`@Configuration`、`@RequestMapping` 本身不会跑逻辑。  
真正干活的是容器启动时的 `BeanFactoryPostProcessor` / `BeanPostProcessor`：  
扫描注解 → 生成/改写 `BeanDefinition` → 创建代理 → 注入依赖。

### 2. 循环依赖为何有时能过、有时不能过

单例 + setter/字段注入时，Spring 用三级缓存提前暴露「早期引用」以打断循环。  
构造器循环依赖无法用同一招安全解决，因而常常失败——这不是 bug，是生命周期约束。

### 3. AOP 为啥「同类自调用」失效

事务/鉴权往往靠代理拦截。`this.xxx()` 走的是真实对象而非代理，所以拦截器不触发。  
这是代理模型的结构结果，不是事务注解写错这么简单。

### 4. MVC 与 WebFlux 为什么长得像却不能混用线程模型

MVC 假设 Servlet 线程；WebFlux 假设事件循环/异步。API 相似是为了迁移成本，  
但阻塞调用放进 WebFlux 会直接打穿性能模型。

## 结合本仓库的阅读方式

- `original/`：对照权威源码（勿改）
- `analyzed/`：在复杂类型/方法上叠加中文意图注释
- `架构说明/`：先建立地图，再按复杂度热点下钻

版本：{layout.version}  
仓库：{target.repo_url}
"""
    write_text(layout.arch / "意图架构.md", intent)

    # 核心调用链
    chains = """# 核心调用链（精读地图）

## Bean 获取主链

```text
ApplicationContext.getBean(name)
  -> AbstractApplicationContext / BeanFactory.getBean
    -> AbstractBeanFactory.doGetBean
      -> getSingleton (三级缓存)
      -> createBean
         -> AbstractAutowireCapableBeanFactory.createBeanInstance
         -> populateBean          # 依赖注入
         -> initializeBean        # Aware / BeanPostProcessor / init-method
```

## 容器启动主链

```text
AbstractApplicationContext.refresh()
  -> prepareRefresh
  -> obtainFreshBeanFactory
  -> prepareBeanFactory
  -> postProcessBeanFactory
  -> invokeBeanFactoryPostProcessors   # 含 ConfigurationClassPostProcessor
  -> registerBeanPostProcessors
  -> initMessageSource / initApplicationEventMulticaster
  -> onRefresh
  -> registerListeners
  -> finishBeanFactoryInitialization   # 非懒加载单例实例化
  -> finishRefresh
```

## MVC 请求主链

```text
DispatcherServlet.doDispatch
  -> HandlerMapping.getHandler
  -> HandlerAdapter.handle
  -> processDispatchResult (ViewResolver / MessageConverter)
```

建议对照 `analyzed/` 中对应类的中文注释阅读。
"""
    write_text(layout.arch / "核心调用链.md", chains)
