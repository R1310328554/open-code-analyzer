#!/usr/bin/env python3
"""为 Spring Framework 核心文件生成 annotation-plan.json（中文意图）。"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "springframework" / "7.0.8" / "_reports" / "annotation-plan.json"

files: dict[str, dict] = {}

files[
    "spring-beans/src/main/java/org/springframework/beans/factory/support/AbstractBeanFactory.java"
] = {
    "file_summary": (
        "Bean 工厂抽象骨架：把「按名字要一个 Bean」拆成缓存查找、父工厂委托、"
        "作用域创建、FactoryBean 解包等步骤。读懂 doGetBean，就读懂了 IoC 主路径。"
    ),
    "types": {
        "AbstractBeanFactory": (
            "BeanFactory 的核心抽象实现。对外 getBean，对内 doGetBean；"
            "同时管理 BeanDefinition 合并（getMergedLocalBeanDefinition）、"
            "作用域注册、以及 BeanPostProcessor 缓存分类。"
        )
    },
    "fields": {
        "parentBeanFactory": "父容器：当前找不到定义时可委派，实现容器层次。",
        "mergedBeanDefinitions": "合并后的 RootBeanDefinition 缓存（含 parent 合并结果）。",
        "factoryBeanObjectCache": "FactoryBean 产出的对象缓存，避免重复 getObject。",
        "beanPostProcessors": "Bean 生命周期钩子列表（实例化前/后、初始化前/后）。",
    },
    "methods": {
        "doGetBean": {
            "summary": (
                "获取 Bean 的真正入口。主路径：转换别名 → 单例缓存（含三级缓存早期引用）→ "
                "若无则 createBean → 按 scope 处理 → 如需则 FactoryBean 解包 → 类型转换。"
                "旁路重点：1) 原型创建中检测循环依赖直接失败；2) 依赖 dependsOn 先实例化；"
                "3) 父工厂委托。阅读时顺着「缓存命中 / 需要创建」两条路走，不要被异常分支带偏。"
            )
        },
        "getObjectForBeanInstance": {
            "summary": (
                "处理 FactoryBean 语义：若 name 以 & 开头返回工厂本身，否则调用 getObject 拿产品。"
                "这是 Spring 里「工厂 Bean」与「普通 Bean」最容易混淆的分界点。"
            )
        },
        "getMergedLocalBeanDefinition": {
            "summary": (
                "把 child BeanDefinition 与 parent 合并成 RootBeanDefinition。"
                "XML 继承、注解覆盖等最终都汇聚到这里；合并结果会被缓存。"
            )
        },
    },
}

files[
    "spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java"
] = {
    "file_summary": (
        "单例注册表：所谓「三级缓存」就在这里。"
        "它解决的问题是：单例创建到一半时，如何安全地把早期引用暴露给循环依赖的另一方。"
    ),
    "types": {
        "DefaultSingletonBeanRegistry": (
            "管理 singletonObjects / earlySingletonObjects / singletonFactories 三级结构，"
            "以及依赖边（dependentBeanMap）用于销毁顺序。"
        )
    },
    "fields": {
        "singletonObjects": "一级缓存：完全初始化好的单例。",
        "earlySingletonObjects": "二级缓存：已实例化但尚未完成填充/初始化的早期暴露对象（可能是半成品或代理）。",
        "singletonFactories": "三级缓存：对象工厂；第一次获取早期引用时调用，常用于生成 AOP 代理。",
        "singletonsCurrentlyInCreation": "正在创建中的单例名字集合，用于循环依赖检测。",
        "dependentBeanMap": "依赖我的 Bean 集合，销毁时要先销毁依赖方。",
    },
    "methods": {
        "getSingleton": {
            "summary": (
                "按名取单例。允许早期引用时：一级没有 → 二级没有 → 从三级工厂创建并升到二级。"
                "很多人对「为何需要第三级」困惑：因为若直接把原始对象放二级，"
                "AOP 需要的「早期代理」可能来不及生成；三级用工厂把「是否代理」推迟到第一次被依赖时。"
            )
        },
        "addSingletonFactory": {
            "summary": "注册三级缓存工厂，通常在 createBeanInstance 之后、populateBean 之前调用。"
        },
        "destroySingletons": {
            "summary": "按依赖关系逆序销毁单例，避免先杀被依赖者导致依赖方看到残骸。"
        },
    },
}

files[
    "spring-beans/src/main/java/org/springframework/beans/factory/support/AbstractAutowireCapableBeanFactory.java"
] = {
    "file_summary": (
        "真正「造 Bean」的车间：实例化 → 属性填充 → 初始化。"
        "循环依赖的早期暴露、@Autowired 注入、init-method、BeanPostProcessor 都在此串联。"
    ),
    "types": {
        "AbstractAutowireCapableBeanFactory": (
            "AutowireCapableBeanFactory 的抽象实现，承载 createBean/doCreateBean 主流程。"
        )
    },
    "fields": {
        "instantiationStrategy": "实例化策略：反射构造 / CGLIB 子类等。",
        "allowCircularReferences": "是否允许单例循环依赖（默认 true）。",
    },
    "methods": {
        "doCreateBean": {
            "summary": (
                "创建 Bean 核心四步：1) createBeanInstance；2) 单例则 addSingletonFactory 早期暴露；"
                "3) populateBean 注入属性；4) initializeBean 调 Aware/PostProcessor/init。"
                "若允许循环依赖，早期暴露必须发生在属性填充前，否则对方 getBean 仍看不到自己。"
            )
        },
        "populateBean": {
            "summary": (
                "属性填充与自动装配。注解注入（AutowiredAnnotationBeanPostProcessor）"
                "通常以 InstantiationAwareBeanPostProcessor 形式介入。"
                "注意：注入点类型匹配失败、多个候选、主候选 @Primary/@Priority 都在这一层附近决出胜负。"
            )
        },
        "initializeBean": {
            "summary": (
                "初始化阶段：invokeAwareMethods → before/after BeanPostProcessor → "
                "InitializingBean/init-method。AOP 代理常常在后置处理器 after 阶段生成。"
            )
        },
        "resolveDependency": {
            "summary": (
                "把依赖描述（字段/参数）解析成实际对象或 ObjectFactory/Optional 等包装。"
                "这是 @Autowired 背后的匹配引擎入口之一。"
            )
        },
    },
}

files[
    "spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultListableBeanFactory.java"
] = {
    "file_summary": (
        "最常用的可列表 BeanFactory：持有 beanDefinitionMap，支持按类型查找、"
        "注解候选解析、以及预实例化单例。几乎所有 ApplicationContext 底层都是它。"
    ),
    "types": {
        "DefaultListableBeanFactory": (
            "ListableBeanFactory + BeanDefinitionRegistry 的默认实现；"
            "负责注册定义、按类型解析、主候选决策、预实例化。"
        )
    },
    "fields": {
        "beanDefinitionMap": "Bean 名 → BeanDefinition 的主表。",
        "allBeanNamesByType": "按类型缓存的 Bean 名列表，加速 getBeansOfType。",
        "resolvableDependencies": "预注册的可解析依赖（如 BeanFactory 自身）。",
        "autowireCandidateResolver": "决定某定义是否可作为注入候选（含 @Qualifier 等）。",
    },
    "methods": {
        "preInstantiateSingletons": {
            "summary": (
                "容器启动尾声：把所有非懒加载单例都 getBean 一遍。"
                "FactoryBean 要区分 smart 初始化与普通 getObject；失败会让整个 refresh 失败。"
            )
        },
        "doResolveDependency": {
            "summary": (
                "依赖解析主逻辑：处理 @Value、集合/数组/Map 注入、Optional、ObjectProvider，"
                "以及多个候选时的 primary/priority/名称回落。这里分支极多，建议按「注入点类型」分类阅读。"
            )
        },
        "findAutowireCandidates": {
            "summary": "找出某类型的所有候选 Bean 名与实例（或解析后的值），供自动装配决策。"
        },
        "registerBeanDefinition": {
            "summary": (
                "注册/覆盖 BeanDefinition。注意 allowBeanDefinitionOverriding 与同名冲突策略；"
                "启动阶段大量后置处理器会改写已有定义。"
            )
        },
    },
}

files[
    "spring-context/src/main/java/org/springframework/context/support/AbstractApplicationContext.java"
] = {
    "file_summary": (
        "ApplicationContext 抽象：refresh() 是整容器启动剧本。"
        "BeanFactory 只是其中一幕；事件、国际化、后置处理器注册都在此编排。"
    ),
    "types": {
        "AbstractApplicationContext": (
            "实现 ConfigurableApplicationContext，定义 refresh/close 模板流程。"
        )
    },
    "fields": {
        "beanFactoryPostProcessors": "用户手动添加的工厂后置处理器（早于扫描出来的那些）。",
        "applicationEventMulticaster": "事件广播器；早期事件可能先排队后补发。",
        "startupShutdownMonitor": "启动/关闭互斥监视器。",
    },
    "methods": {
        "refresh": {
            "summary": (
                "容器启动总编排。顺序强相关，乱序会破坏语义："
                "准备 → 获得 BeanFactory → 工厂后置处理（解析 @Configuration）→ "
                "注册 Bean 后置处理 → 消息源/事件 → onRefresh → 监听器 → "
                "预实例化单例 → finishRefresh。读源码时对照每一步的「输入/输出不变量」。"
            )
        },
        "invokeBeanFactoryPostProcessors": {
            "summary": (
                "执行 BeanFactoryPostProcessor；ConfigurationClassPostProcessor 在此阶段"
                "把配置类变成一批 BeanDefinition。优先级与排序非常关键。"
            )
        },
        "registerBeanPostProcessors": {
            "summary": "实例化并注册 BeanPostProcessor，影响后续所有 Bean 的创建。"
        },
        "finishBeanFactoryInitialization": {
            "summary": "冻结定义并预实例化非懒加载单例；应用「真正开始干活」往往从这里开始。"
        },
        "close": {
            "summary": "发布关闭事件、销毁单例、取消注册 JVM shutdown hook；与 refresh 对称。"
        },
    },
}

files[
    "spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassPostProcessor.java"
] = {
    "file_summary": (
        "@Configuration / @ComponentScan / @Import / @Bean 的总加工厂。"
        "没有它，注解配置世界转不起来。弯绕点在于：配置类可能被 CGLIB 增强以支持 @Bean 拦截。"
    ),
    "types": {
        "ConfigurationClassPostProcessor": (
            "BeanDefinitionRegistryPostProcessor：解析配置类并注册衍生 BeanDefinition；"
            "也会增强 FULL 模式配置类。"
        )
    },
    "methods": {
        "postProcessBeanDefinitionRegistry": {
            "summary": (
                "扫描并解析配置类，把 @Bean 方法等变成 BeanDefinition 注册进 registry。"
                "可能多轮迭代，因为 @Import 会引入新的配置类。"
            )
        },
        "enhanceConfigurationClasses": {
            "summary": (
                "对 FULL @Configuration 做 CGLIB 增强，使 @Bean 方法调用也能走容器语义"
                "（同配置类内部 @Bean 互调返回的是容器管理实例，而非每次 new）。"
            )
        },
    },
}

files[
    "spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassParser.java"
] = {
    "file_summary": "配置类语法分析器：递归处理 @ComponentScan/@Import/@ImportResource/@PropertySource/@Bean。",
    "types": {
        "ConfigurationClassParser": "把一个配置类展开成 ConfigurationClass 图（含导入边）。"
    },
    "methods": {
        "parse": {
            "summary": "入口：从一组候选配置类开始解析，直到导入图闭合。"
        },
        "processImports": {
            "summary": (
                "@Import 支持普通类、ImportSelector、ImportBeanDefinitionRegistrar。"
                "选择器还可 Deferred，导致处理时机推迟——这是条件装配里常见「坑」来源。"
            )
        },
        "doProcessConfigurationClass": {
            "summary": "处理单个配置类的各个注解成员；内部再递归成员类与超类。"
        },
    },
}

files[
    "spring-aop/src/main/java/org/springframework/aop/framework/JdkDynamicAopProxy.java"
] = {
    "file_summary": "基于 JDK 动态代理的 AOP 实现：目标必须实现接口；拦截逻辑在 invoke。",
    "types": {
        "JdkDynamicAopProxy": "InvocationHandler 实现；把方法调用路由到拦截器链或目标对象。"
    },
    "methods": {
        "invoke": {
            "summary": (
                "代理调用入口：处理 equals/hashCode、expose-proxy、以及 ReflectiveMethodInvocation 链式 proceed。"
                "若拦截器链为空则直接反射目标方法。自调用失效：因为目标内部 this 不是这个 proxy。"
            )
        }
    },
}

files[
    "spring-aop/src/main/java/org/springframework/aop/framework/CglibAopProxy.java"
] = {
    "file_summary": "CGLIB 子类代理：目标无接口或强制类代理时使用；通过生成子类覆盖方法插入拦截。",
    "types": {
        "CglibAopProxy": "创建 CGLIB Enhancer 子类代理，并配置一组 Callback（拦截器/调度器）。"
    },
    "methods": {
        "getProxy": {
            "summary": "构建 Enhancer、设置 callback 过滤（哪些方法被拦截），创建代理实例。"
        }
    },
}

files[
    "spring-webmvc/src/main/java/org/springframework/web/servlet/DispatcherServlet.java"
] = {
    "file_summary": (
        "Spring MVC 前端控制器：所有请求的交通枢纽。"
        "它不自己处理业务，而是找 HandlerMapping → HandlerAdapter → ViewResolver/MessageConverter。"
    ),
    "types": {
        "DispatcherServlet": (
            "继承 FrameworkServlet；在 doService/doDispatch 中编排 MVC 组件。"
        )
    },
    "fields": {
        "handlerMappings": "有序 HandlerMapping 列表；谁先能映射就用谁。",
        "handlerAdapters": "能执行某种 Handler 的适配器列表。",
        "viewResolvers": "逻辑视图名 → View。",
        "handlerExceptionResolvers": "处理器异常 → 错误视图/响应。",
    },
    "methods": {
        "doDispatch": {
            "summary": (
                "请求主链：获取 HandlerExecutionChain → 找适配器 → "
                "（可选）multipart 解析 → 调用 handler → 处理结果/异常 → 渲染。"
                "拦截器 preHandle/postHandle/afterCompletion 嵌在链里；preHandle 返回 false 会短路。"
            )
        },
        "initStrategies": {
            "summary": "从 ApplicationContext 探测并初始化 MVC 九大组件；缺省则用默认策略。"
        },
        "processDispatchResult": {
            "summary": "把 ModelAndView 或异常解析结果落到响应（渲染视图或已处理完）。"
        },
    },
}

# 额外核心类型（偏短但关键）
files[
    "spring-core/src/main/java/org/springframework/core/ResolvableType.java"
] = {
    "file_summary": "在泛型擦除世界里「还原」类型信息的核心工具；注入匹配、转换服务重度依赖它。",
    "types": {
        "ResolvableType": "可嵌套描述 Type/Class/泛型参数，并支持 assignability 判断。"
    },
    "methods": {
        "forType": {"summary": "从 Type/Class 等构造 ResolvableType 的工厂入口。"},
        "isAssignableFrom": {
            "summary": "考虑泛型参数的可赋值判断；比 raw Class.isAssignableFrom 更严格也更接近真实注入语义。"
        },
    },
}

files[
    "spring-beans/src/main/java/org/springframework/beans/factory/support/ConstructorResolver.java"
] = {
    "file_summary": "构造器自动装配与工厂方法解析：如何在一堆构造器里选出「唯一」可满足的那一个。",
    "types": {
        "ConstructorResolver": "负责构造器/工厂方法参数解析与歧义消解。"
    },
    "methods": {
        "autowireConstructor": {
            "summary": (
                "根据参数类型与候选 Bean 给构造器打分，选出最优。"
                "歧义时抛 UnsatisfiedDependencyException——这也是构造器循环依赖难解的根源之一。"
            )
        }
    },
}

files[
    "spring-tx/src/main/java/org/springframework/transaction/interceptor/TransactionInterceptor.java"
] = {
    "file_summary": "事务拦截器：把 @Transactional 变成环绕通知，委托 TransactionAspectSupport 管边界。",
    "types": {
        "TransactionInterceptor": "AOP MethodInterceptor，执行事务性 invoke。"
    },
    "methods": {
        "invoke": {
            "summary": "开启/加入事务 → 调目标方法 → 按异常回滚或提交。传播行为差异全在获取事务那一步体现。"
        }
    },
}

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps({"files": files}, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"wrote {OUT} files={len(files)}")
