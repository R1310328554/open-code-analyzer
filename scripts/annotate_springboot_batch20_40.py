#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 batch files [20:40]."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][20:40]

# Per-file: list of (old, new) replacements — longest/most specific first
FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "OnPropertyListCondition.java": [
        (
            "/**\n * {@link Condition} that checks if a property whose value is a list is defined in the\n * environment.\n *\n * @author Eneias Silva\n * @author Stephane Nicoll\n * @since 2.0.5\n */",
            "/**\n * 检查环境中是否定义了值为列表的属性。\n * <p>\n * 通过 {@link Binder} 绑定指定属性名，若成功绑定为 {@code List<String>} 则条件匹配。\n *\n * @author Eneias Silva\n * @author Stephane Nicoll\n * @since 2.0.5\n */",
        ),
        (
            "/**\n\t * Create a new instance with the property to check and the message builder to use.\n\t * @param propertyName the name of the property\n\t * @param messageBuilder a message builder supplier that should provide a fresh\n\t * instance on each call\n\t */",
            "/**\n\t * 创建新实例。\n\t *\n\t * @param propertyName 要检查的属性名\n\t * @param messageBuilder 消息构建器供应者，每次调用应提供新实例\n\t */",
        ),
    ],
    "OnResourceCondition.java": [
        (
            "/**\n * {@link Condition} that checks for specific resources.\n *\n * @author Dave Syer\n * @see ConditionalOnResource\n */",
            "/**\n * 检查指定资源是否存在的 {@link Condition}。\n * <p>\n * 解析 {@link ConditionalOnResource} 注解中的资源位置，\n * 通过 {@link ResourceLoader} 验证每个资源是否存在。\n *\n * @author Dave Syer\n * @see ConditionalOnResource\n */",
        ),
    ],
    "OnThreadingCondition.java": [
        (
            "/**\n * {@link Condition} that checks for a required {@link Threading}.\n *\n * @author Moritz Halbritter\n * @see ConditionalOnThreading\n */",
            "/**\n * 检查所需 {@link Threading} 是否处于活动状态的 {@link Condition}。\n * <p>\n * 读取 {@link ConditionalOnThreading} 注解指定的线程模型，\n * 通过 {@link Threading#isActive(Environment)} 判断是否匹配。\n *\n * @author Moritz Halbritter\n * @see ConditionalOnThreading\n */",
        ),
    ],
    "OnWarDeploymentCondition.java": [
        (
            "/**\n * {@link Condition} that checks if the application is running as a traditional war\n * deployment.\n *\n * @author Madhura Bhave\n * @see ConditionalOnWarDeployment\n * @see ConditionalOnNotWarDeployment\n */",
            "/**\n * 检查应用是否以传统 WAR 方式部署的 {@link Condition}。\n * <p>\n * 若 {@link ResourceLoader} 为 {@link WebApplicationContext} 且\n * {@link ServletContext} 非空，则视为 WAR 部署。\n *\n * @author Madhura Bhave\n * @see ConditionalOnWarDeployment\n * @see ConditionalOnNotWarDeployment\n */",
        ),
    ],
    "OnWebApplicationCondition.java": [
        (
            "/**\n * {@link Condition} that checks for the presence or absence of\n * {@link WebApplicationContext}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @see ConditionalOnWebApplication\n * @see ConditionalOnNotWebApplication\n */",
            "/**\n * 检查是否存在或不存在 {@link WebApplicationContext} 的 {@link Condition}。\n * <p>\n * 支持 Servlet 与 Reactive 两种 Web 应用类型，\n * 也可作为 {@link AutoConfigurationImportFilter} 批量过滤自动配置类。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @see ConditionalOnWebApplication\n * @see ConditionalOnNotWebApplication\n */",
        ),
    ],
    "ResourceCondition.java": [
        (
            "/**\n * {@link SpringBootCondition} used to check if a resource can be found using a\n * configurable property and optional default location(s).\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.3.0\n */",
            "/**\n * 通过可配置属性及可选默认位置检查资源是否存在的 {@link SpringBootCondition}。\n * <p>\n * 若环境中已定义配置属性则直接匹配；否则依次检查默认资源位置。\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 1.3.0\n */",
        ),
        (
            "/**\n\t * Create a new condition.\n\t * @param name the name of the component\n\t * @param property the configuration property\n\t * @param resourceLocations default location(s) where the configuration file can be\n\t * found if the configuration key is not specified\n\t * @since 2.0.0\n\t */",
            "/**\n\t * 创建新条件。\n\t *\n\t * @param name 组件名称（用于条件消息）\n\t * @param property 配置属性键\n\t * @param resourceLocations 未指定配置键时的默认资源位置\n\t * @since 2.0.0\n\t */",
        ),
        (
            "/**\n\t * Check if one of the default resource locations actually exists.\n\t * @param context the condition context\n\t * @param metadata the annotation metadata\n\t * @return the condition outcome\n\t */",
            "/**\n\t * 检查默认资源位置中是否至少有一个实际存在。\n\t *\n\t * @param context 条件上下文\n\t * @param metadata 注解元数据\n\t * @return 条件判定结果\n\t */",
        ),
    ],
    "SearchStrategy.java": [
        (
            "/**\n * Some named search strategies for beans in the bean factory hierarchy.\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
            "/**\n * Bean 工厂层次结构中搜索 Bean 的命名策略。\n * <p>\n * 用于 {@link ConditionalOnBean}、{@link ConditionalOnMissingBean} 等条件注解，\n * 控制是否在父上下文或整个层次结构中查找 Bean。\n *\n * @author Dave Syer\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * Search only the current context.\n\t */",
            "/**\n\t * 仅在当前上下文中搜索。\n\t */",
        ),
        (
            "/**\n\t * Search all ancestors, but not the current context.\n\t */",
            "/**\n\t * 搜索所有祖先上下文，但不包括当前上下文。\n\t */",
        ),
        (
            "/**\n\t * Search the entire hierarchy.\n\t */",
            "/**\n\t * 搜索整个层次结构。\n\t */",
        ),
    ],
    "SpringBootCondition.java": [
        (
            "/**\n * Base of all {@link Condition} implementations used with Spring Boot. Provides sensible\n * logging to help the user diagnose what classes are loaded.\n *\n * @author Phillip Webb\n * @author Greg Turnquist\n * @since 1.0.0\n */",
            "/**\n * Spring Boot 中所有 {@link Condition} 实现的基类。\n * <p>\n * 提供合理的日志输出与条件评估报告记录，帮助用户诊断类加载与条件匹配情况。\n *\n * @author Phillip Webb\n * @author Greg Turnquist\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * Determine the outcome of the match along with suitable log output.\n\t * @param context the condition context\n\t * @param metadata the annotation metadata\n\t * @return the condition outcome\n\t */",
            "/**\n\t * 判定条件是否匹配并生成相应的日志输出。\n\t *\n\t * @param context 条件上下文\n\t * @param metadata 注解元数据\n\t * @return 条件判定结果\n\t */",
        ),
        (
            "/**\n\t * Return true if any of the specified conditions match.\n\t * @param context the context\n\t * @param metadata the annotation meta-data\n\t * @param conditions conditions to test\n\t * @return {@code true} if any condition matches.\n\t */",
            "/**\n\t * 若指定条件中有任一匹配则返回 {@code true}。\n\t *\n\t * @param context 条件上下文\n\t * @param metadata 注解元数据\n\t * @param conditions 要测试的条件\n\t * @return 任一条件匹配时为 {@code true}\n\t */",
        ),
        (
            "/**\n\t * Return true if any of the specified condition matches.\n\t * @param context the context\n\t * @param metadata the annotation meta-data\n\t * @param condition condition to test\n\t * @return {@code true} if the condition matches.\n\t */",
            "/**\n\t * 若指定条件匹配则返回 {@code true}。\n\t *\n\t * @param context 条件上下文\n\t * @param metadata 注解元数据\n\t * @param condition 要测试的条件\n\t * @return 条件匹配时为 {@code true}\n\t */",
        ),
    ],
    "ContainerImageMetadata.java": [
        (
            "/**\n * Metadata about a container image that can be added to an {@link AttributeAccessor}.\n * Primarily designed to be attached to {@link BeanDefinition BeanDefinitions} created in\n * support of Testcontainers or Docker Compose.\n *\n * @param imageName the container image name or {@code null} if the image name is not yet\n * known\n * @author Phillip Webb\n * @since 3.4.0\n */",
            "/**\n * 可附加到 {@link AttributeAccessor} 的容器镜像元数据。\n * <p>\n * 主要用于标记 Testcontainers 或 Docker Compose 支持场景下创建的\n * {@link BeanDefinition BeanDefinitions}。\n *\n * @param imageName 容器镜像名称；若尚未确定则为 {@code null}\n * @author Phillip Webb\n * @since 3.4.0\n */",
        ),
        (
            "/**\n\t * Add this container image metadata to the given attributes.\n\t * @param attributes the attributes to add the metadata to\n\t */",
            "/**\n\t * 将此容器镜像元数据添加到给定属性访问器。\n\t *\n\t * @param attributes 要添加元数据的属性访问器\n\t */",
        ),
        (
            "/**\n\t * Return {@code true} if {@link ContainerImageMetadata} has been added to the given\n\t * attributes.\n\t * @param attributes the attributes to check\n\t * @return if metadata is present\n\t */",
            "/**\n\t * 若给定属性访问器中已添加 {@link ContainerImageMetadata} 则返回 {@code true}。\n\t *\n\t * @param attributes 要检查的属性访问器\n\t * @return 元数据是否存在\n\t */",
        ),
        (
            "/**\n\t * Return {@link ContainerImageMetadata} from the given attributes or {@code null} if\n\t * no metadata has been added.\n\t * @param attributes the attributes\n\t * @return the metadata or {@code null}\n\t */",
            "/**\n\t * 从给定属性访问器获取 {@link ContainerImageMetadata}；\n\t * 若未添加元数据则返回 {@code null}。\n\t *\n\t * @param attributes 属性访问器\n\t * @return 元数据或 {@code null}\n\t */",
        ),
    ],
    "ConfigurationPropertiesAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for\n * {@link ConfigurationProperties @ConfigurationProperties} beans. Automatically binds and\n * validates any bean annotated with {@code @ConfigurationProperties}.\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n * @see EnableConfigurationProperties\n * @see ConfigurationProperties\n */",
            "/**\n * {@link ConfigurationProperties @ConfigurationProperties} Bean 的\n * {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 自动绑定并校验所有标注 {@code @ConfigurationProperties} 的 Bean。\n *\n * @author Stephane Nicoll\n * @since 1.3.0\n * @see EnableConfigurationProperties\n * @see ConfigurationProperties\n */",
        ),
    ],
    "LifecycleAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} relating to the application\n * context's lifecycle.\n *\n * @author Andy Wilkinson\n * @since 2.3.0\n */",
            "/**\n * 与应用上下文生命周期相关的 {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 注册默认的 {@link DefaultLifecycleProcessor}，\n * 并根据 {@link LifecycleProperties} 配置各关闭阶段的超时时间。\n *\n * @author Andy Wilkinson\n * @since 2.3.0\n */",
        ),
    ],
    "LifecycleProperties.java": [
        (
            "/**\n * Configuration properties for lifecycle processing.\n *\n * @author Andy Wilkinson\n * @since 2.3.0\n */",
            "/**\n * 生命周期处理的配置属性。\n *\n * @author Andy Wilkinson\n * @since 2.3.0\n */",
        ),
        (
            "/**\n\t * Timeout for the shutdown of any phase (group of SmartLifecycle beans with the same\n\t * 'phase' value).\n\t */",
            "/**\n\t * 任意阶段（具有相同 {@code phase} 值的 {@code SmartLifecycle} Bean 组）关闭的超时时间。\n\t */",
        ),
    ],
    "MessageSourceAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for {@link MessageSource}.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Eddú Meléndez\n * @author Marc Becker\n * @author Misagh Moayyed\n * @since 1.5.0\n */",
            "/**\n * {@link MessageSource} 的 {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 当类路径上存在资源包文件且未定义自定义 {@code MessageSource} Bean 时，\n * 自动配置 {@link ResourceBundleMessageSource}。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Eddú Meléndez\n * @author Marc Becker\n * @author Misagh Moayyed\n * @since 1.5.0\n */",
        ),
    ],
    "MessageSourceProperties.java": [
        (
            "/**\n * Configuration properties for Message Source.\n *\n * @author Stephane Nicoll\n * @author Kedar Joshi\n * @author Misagh Moayyed\n * @since 2.0.0\n */",
            "/**\n * 消息源（Message Source）的配置属性。\n *\n * @author Stephane Nicoll\n * @author Kedar Joshi\n * @author Misagh Moayyed\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * List of basenames (essentially a fully-qualified classpath location), each\n\t * following the ResourceBundle convention with relaxed support for slash based\n\t * locations. If it doesn't contain a package qualifier (such as \"org.mypackage\"), it\n\t * will be resolved from the classpath root.\n\t */",
            "/**\n\t * 基名列表（本质上是完全限定的类路径位置），\n\t * 遵循 ResourceBundle 约定，并放宽对斜杠分隔位置的支持。\n\t * 若不包含包限定符（如 {@code org.mypackage}），则从类路径根解析。\n\t */",
        ),
        (
            "/**\n\t * List of locale-independent property file resources containing common messages.\n\t */",
            "/**\n\t * 包含通用消息、与区域设置无关的属性文件资源列表。\n\t */",
        ),
        (
            "/**\n\t * Message bundles encoding.\n\t */",
            "/**\n\t * 消息资源包的字符编码。\n\t */",
        ),
        (
            "/**\n\t * Loaded resource bundle files cache duration. When not set, bundles are cached\n\t * forever. If a duration suffix is not specified, seconds will be used.\n\t */",
            "/**\n\t * 已加载资源包文件的缓存时长。未设置时永久缓存。\n\t * 若未指定时长后缀，默认以秒为单位。\n\t */",
        ),
        (
            "/**\n\t * Whether to fall back to the system Locale if no files for a specific Locale have\n\t * been found. if this is turned off, the only fallback will be the default file (e.g.\n\t * \"messages.properties\" for basename \"messages\").\n\t */",
            "/**\n\t * 未找到特定区域设置的文件时，是否回退到系统 {@code Locale}。\n\t * 关闭后，唯一回退为默认文件（如基名为 {@code messages} 时的 {@code messages.properties}）。\n\t */",
        ),
        (
            "/**\n\t * Whether to always apply the MessageFormat rules, parsing even messages without\n\t * arguments.\n\t */",
            "/**\n\t * 是否始终应用 MessageFormat 规则，即使消息不含参数也进行解析。\n\t */",
        ),
        (
            "/**\n\t * Whether to use the message code as the default message instead of throwing a\n\t * \"NoSuchMessageException\". Recommended during development only.\n\t */",
            "/**\n\t * 是否将消息代码作为默认消息，而非抛出 {@code NoSuchMessageException}。\n\t * 建议仅在开发阶段启用。\n\t */",
        ),
    ],
    "PropertyPlaceholderAutoConfiguration.java": [
        (
            "/**\n * {@link EnableAutoConfiguration Auto-configuration} for\n * {@link PropertySourcesPlaceholderConfigurer}.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @since 1.5.0\n */",
            "/**\n * {@link PropertySourcesPlaceholderConfigurer} 的\n * {@link EnableAutoConfiguration 自动配置}。\n * <p>\n * 在当前上下文中未定义同名 Bean 时，注册默认的占位符解析器，\n * 支持 {@code ${...}} 属性占位符替换。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @since 1.5.0\n */",
        ),
    ],
    "AbstractRepositoryConfigurationSourceSupport.java": [
        (
            "/**\n * Base {@link ImportBeanDefinitionRegistrar} used to auto-configure Spring Data\n * Repositories.\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Oliver Gierke\n * @since 1.0.0\n */",
            "/**\n * 用于自动配置 Spring Data Repository 的\n * {@link ImportBeanDefinitionRegistrar} 基类。\n * <p>\n * 通过 {@link RepositoryConfigurationDelegate} 扫描并注册 Repository 接口，\n * 基包默认取自 {@link AutoConfigurationPackages}。\n *\n * @author Phillip Webb\n * @author Dave Syer\n * @author Oliver Gierke\n * @since 1.0.0\n */",
        ),
        (
            "/**\n\t * The Spring Data annotation used to enable the particular repository support.\n\t * @return the annotation class\n\t */",
            "/**\n\t * 启用特定 Repository 支持的 Spring Data 注解类型。\n\t *\n\t * @return 注解类\n\t */",
        ),
        (
            "/**\n\t * The configuration class that will be used by Spring Boot as a template.\n\t * @return the configuration class\n\t */",
            "/**\n\t * Spring Boot 用作模板的配置类。\n\t *\n\t * @return 配置类\n\t */",
        ),
        (
            "/**\n\t * The {@link RepositoryConfigurationExtension} for the particular repository support.\n\t * @return the repository configuration extension\n\t */",
            "/**\n\t * 特定 Repository 支持的 {@link RepositoryConfigurationExtension}。\n\t *\n\t * @return Repository 配置扩展\n\t */",
        ),
        (
            "/**\n\t * The {@link BootstrapMode} for the particular repository support. Defaults to\n\t * {@link BootstrapMode#DEFAULT}.\n\t * @return the bootstrap mode\n\t */",
            "/**\n\t * 特定 Repository 支持的 {@link BootstrapMode}，默认为 {@link BootstrapMode#DEFAULT}。\n\t *\n\t * @return 引导模式\n\t */",
        ),
        (
            "/**\n\t * An auto-configured {@link AnnotationRepositoryConfigurationSource}.\n\t */",
            "/**\n\t * 自动配置的 {@link AnnotationRepositoryConfigurationSource}。\n\t */",
        ),
    ],
    "ConditionalOnRepositoryType.java": [
        (
            "/**\n * {@link Conditional @Conditional} that only matches when a particular type of Spring\n * Data repository has been enabled.\n *\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
            "/**\n * 仅当已启用特定类型的 Spring Data Repository 时才匹配的\n * {@link Conditional @Conditional}。\n * <p>\n * 通过 {@code spring.data.<store>.repositories.type} 属性\n * 与注解指定的 {@link RepositoryType} 进行比较。\n *\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * The name of the store that backs the repositories.\n\t * @return the store\n\t */",
            "/**\n\t * 支撑 Repository 的存储名称（如 {@code mongodb}、{@code redis}）。\n\t *\n\t * @return 存储名称\n\t */",
        ),
        (
            "/**\n\t * The required repository type.\n\t * @return the required repository type\n\t */",
            "/**\n\t * 所需的 Repository 类型。\n\t *\n\t * @return 所需的 Repository 类型\n\t */",
        ),
    ],
    "OnRepositoryTypeCondition.java": [
        (
            "/**\n * {@link SpringBootCondition} for controlling what type of Spring Data repositories are\n * auto-configured.\n *\n * @author Andy Wilkinson\n */",
            "/**\n * 控制自动配置哪种类型的 Spring Data Repository 的 {@link SpringBootCondition}。\n * <p>\n * 读取 {@code spring.data.<store>.repositories.type} 配置，\n * 与 {@link ConditionalOnRepositoryType} 要求的类型进行匹配。\n *\n * @author Andy Wilkinson\n */",
        ),
    ],
    "RepositoryType.java": [
        (
            "/**\n * Type of Spring Data repositories to enable.\n *\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
            "/**\n * 要启用的 Spring Data Repository 类型。\n *\n * @author Andy Wilkinson\n * @since 2.0.0\n */",
        ),
        (
            "/**\n\t * Enables all repository types automatically based on their availability.\n\t */",
            "/**\n\t * 根据可用性自动启用所有 Repository 类型。\n\t */",
        ),
        (
            "/**\n\t * Enables imperative repositories.\n\t */",
            "/**\n\t * 启用命令式（imperative）Repository。\n\t */",
        ),
        (
            "/**\n\t * Enables no repositories.\n\t */",
            "/**\n\t * 不启用任何 Repository。\n\t */",
        ),
        (
            "/**\n\t * Enables reactive repositories.\n\t */",
            "/**\n\t * 启用响应式（reactive）Repository。\n\t */",
        ),
    ],
    "NoSuchBeanDefinitionFailureAnalyzer.java": [
        (
            "/**\n * An {@link AbstractInjectionFailureAnalyzer} that performs analysis of failures caused\n * by a {@link NoSuchBeanDefinitionException}.\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Scott Frederick\n */",
            "/**\n * 分析由 {@link NoSuchBeanDefinitionException} 引起的注入失败的\n * {@link AbstractInjectionFailureAnalyzer}。\n * <p>\n * 结合 {@link ConditionEvaluationReport} 报告相关自动配置条件未匹配的原因，\n * 并列出用户配置中可能冲突的 Bean 候选。\n *\n * @author Stephane Nicoll\n * @author Phillip Webb\n * @author Scott Frederick\n */",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    import json
    raise SystemExit(main())
