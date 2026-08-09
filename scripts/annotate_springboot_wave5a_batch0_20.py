#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-5 slice [0:20]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:20]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FileEncodingApplicationListener.java": [
        (
            "/**\n * An {@link ApplicationListener} that halts application startup if the system file\n * encoding does not match an expected value set in the environment. By default has no\n * effect, but if you set {@code spring.mandatory_file_encoding} (or some camelCase or\n * UPPERCASE variant of that) to the name of a character encoding (e.g. \"UTF-8\") then this\n * initializer throws an exception when the {@code file.encoding} System property does not\n * equal it.\n *\n * <p>\n * The System property {@code file.encoding} is normally set by the JVM in response to the\n * {@code LANG} or {@code LC_ALL} environment variables. It is used (along with other\n * platform-dependent variables keyed off those environment variables) to encode JVM\n * arguments as well as file names and paths. In most cases you can override the file\n * encoding System property on the command line (with standard JVM features), but also\n * consider setting the {@code LANG} environment variable to an explicit\n * character-encoding value (e.g. \"en_GB.UTF-8\").\n *\n * @author Dave Syer\n * @author Madhura Bhave\n * @since 1.0.0\n */",
            "/**\n * 当系统文件编码与环境中的期望值不一致时中止应用启动的 {@link ApplicationListener}。\n * 默认无效果；若将 {@code spring.mandatory_file_encoding}（或其 camelCase/UPPERCASE 变体）\n * 设为字符编码名（如 \"UTF-8\"），则当 {@code file.encoding} 系统属性与其不一致时抛出异常。\n * <p>\n * {@code file.encoding} 通常由 JVM 根据 {@code LANG} 或 {@code LC_ALL} 环境变量设置，\n * 用于编码 JVM 参数及文件名/路径。多数情况下可在命令行覆盖该属性，\n * 也可将 {@code LANG} 设为明确的字符编码区域（如 \"en_GB.UTF-8\"）。\n *\n * @author Dave Syer\n * @author Madhura Bhave\n * @since 1.0.0\n */",
        ),
    ],
    "TypeExcludeFilter.java": [
        (
            "/**\n * Provides exclusion {@link TypeFilter TypeFilters} that are loaded from the\n * {@link BeanFactory} and automatically applied to {@code SpringBootApplication}\n * scanning. Can also be used directly with {@code @ComponentScan} as follows:\n * <pre class=\"code\">\n * &#064;ComponentScan(excludeFilters = @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))\n * </pre>\n * <p>\n * Implementations should provide a subclass registered with {@link BeanFactory} and\n * override the {@link #match(MetadataReader, MetadataReaderFactory)} method. They should\n * also implement a valid {@link #hashCode() hashCode} and {@link #equals(Object) equals}\n * methods so that they can be used as part of Spring test's application context caches.\n * <p>\n * Note that {@code TypeExcludeFilters} are initialized very early in the application\n * lifecycle, they should generally not have dependencies on any other beans. They are\n * primarily used internally to support {@code spring-boot-test}.\n *\n * @author Phillip Webb\n * @since 1.4.0\n */",
            "/**\n * 提供从 {@link BeanFactory} 加载并自动应用于 {@code SpringBootApplication}\n * 扫描的排除 {@link TypeFilter TypeFilters}。也可直接配合 {@code @ComponentScan} 使用：\n * <pre class=\"code\">\n * &#064;ComponentScan(excludeFilters = @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))\n * </pre>\n * <p>\n * 实现类应提供注册到 {@link BeanFactory} 的子类并重写\n * {@link #match(MetadataReader, MetadataReaderFactory)}，同时实现有效的\n * {@link #hashCode() hashCode} 与 {@link #equals(Object) equals}，以便用于 Spring 测试的应用上下文缓存。\n * <p>\n * {@code TypeExcludeFilter} 在应用生命周期极早期初始化，通常不应依赖其他 Bean，\n * 主要用于内部支持 {@code spring-boot-test}。\n *\n * @author Phillip Webb\n * @since 1.4.0\n */",
        ),
    ],
    "Configurations.java": [
        (
            "/**\n * A set of {@link Configuration @Configuration} classes that can be registered in\n * {@link ApplicationContext}. Classes can be returned from one or more\n * {@link Configurations} instances by using {@link #getClasses(Configurations[])}. The\n * resulting array follows the ordering rules usually applied by the\n * {@link ApplicationContext} and/or custom {@link ImportSelector} implementations.\n * <p>\n * This class is primarily intended for use with tests that need to specify configuration\n * classes but can't use {@link org.springframework.test.context.junit4.SpringRunner}.\n * <p>\n * Implementations of this class should be annotated with {@link Order @Order} or\n * implement {@link Ordered}.\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see UserConfigurations\n */",
            "/**\n * 可在 {@link ApplicationContext} 中注册的 {@link Configuration @Configuration} 类集合。\n * 可通过 {@link #getClasses(Configurations[])} 从一个或多个 {@link Configurations} 实例返回类数组，\n * 排序遵循 {@link ApplicationContext} 及自定义 {@link ImportSelector} 的常规规则。\n * <p>\n * 主要用于需要指定配置类但无法使用\n * {@link org.springframework.test.context.junit4.SpringRunner} 的测试场景。\n * <p>\n * 实现类应标注 {@link Order @Order} 或实现 {@link Ordered}。\n *\n * @author Phillip Webb\n * @since 2.0.0\n * @see UserConfigurations\n */",
        ),
        (
            "/**\n\t * Create a new {@link Configurations} instance.\n\t * @param classes the configuration classes\n\t */",
            "/**\n\t * 创建新的 {@link Configurations} 实例。\n\t *\n\t * @param classes 配置类\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link Configurations} instance.\n\t * @param sorter a {@link UnaryOperator} used to sort the configurations\n\t * @param classes the configuration classes\n\t * @param beanNameGenerator an optional function used to generate the bean name\n\t * @since 3.4.0\n\t */",
            "/**\n\t * 创建新的 {@link Configurations} 实例。\n\t *\n\t * @param sorter 用于排序配置的 {@link UnaryOperator}\n\t * @param classes 配置类\n\t * @param beanNameGenerator 可选的 Bean 名称生成函数\n\t * @since 3.4.0\n\t */",
        ),
        (
            "/**\n\t * Merge configurations from another source of the same type.\n\t * @param other the other {@link Configurations} (must be of the same type as this\n\t * instance)\n\t * @return a new configurations instance (must be of the same type as this instance)\n\t */",
            "/**\n\t * 合并同类型来源的配置。\n\t *\n\t * @param other 另一个 {@link Configurations}（须与本实例同类型）\n\t * @return 新的配置实例（须与本实例同类型）\n\t */",
        ),
        (
            "/**\n\t * Merge configurations.\n\t * @param mergedClasses the merged classes\n\t * @return a new configurations instance (must be of the same type as this instance)\n\t */",
            "/**\n\t * 合并配置类。\n\t *\n\t * @param mergedClasses 合并后的类集合\n\t * @return 新的配置实例（须与本实例同类型）\n\t */",
        ),
        (
            "/**\n\t * Return the bean name that should be used for the given configuration class or\n\t * {@code null} to use the default name.\n\t * @param beanClass the bean class\n\t * @return the bean name\n\t * @since 3.4.0\n\t */",
            "/**\n\t * 返回给定配置类应使用的 Bean 名称，或 {@code null} 使用默认名称。\n\t *\n\t * @param beanClass Bean 类\n\t * @return Bean 名称\n\t * @since 3.4.0\n\t */",
        ),
        (
            "/**\n\t * Return the classes from all the specified configurations in the order that they\n\t * would be registered.\n\t * @param configurations the source configuration\n\t * @return configuration classes in registration order\n\t */",
            "/**\n\t * 按注册顺序返回所有指定配置中的类。\n\t *\n\t * @param configurations 源配置\n\t * @return 按注册顺序排列的配置类\n\t */",
        ),
        (
            "/**\n\t * Collate the given configuration by sorting and merging them.\n\t * @param configurations the source configuration\n\t * @return the collated configurations\n\t * @since 3.4.0\n\t */",
            "/**\n\t * 对给定配置排序并合并。\n\t *\n\t * @param configurations 源配置\n\t * @return 整理后的配置\n\t * @since 3.4.0\n\t */",
        ),
    ],
    "DeterminableImports.java": [
        (
            "/**\n * Interface that can be implemented by {@link ImportSelector} and\n * {@link ImportBeanDefinitionRegistrar} implementations when they can determine imports\n * early. The {@link ImportSelector} and {@link ImportBeanDefinitionRegistrar} interfaces\n * are quite flexible which can make it hard to tell exactly what bean definitions they\n * will add. This interface should be used when an implementation consistently results in\n * the same imports, given the same source.\n * <p>\n * Using {@link DeterminableImports} is particularly useful when working with Spring's\n * testing support. It allows for better generation of {@link ApplicationContext} cache\n * keys.\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.5.0\n */",
            "/**\n * 当 {@link ImportSelector} 与 {@link ImportBeanDefinitionRegistrar} 实现可提前确定导入内容时可实现的接口。\n * 上述接口较灵活，难以精确预知将添加的 Bean 定义；若给定相同源时导入结果一致，应使用本接口。\n * <p>\n * 配合 Spring 测试支持时尤其有用，可更好地生成 {@link ApplicationContext} 缓存键。\n *\n * @author Phillip Webb\n * @author Andy Wilkinson\n * @since 1.5.0\n */",
        ),
        (
            "/**\n\t * Return a set of objects that represent the imports. Objects within the returned\n\t * {@code Set} must implement a valid {@link Object#hashCode() hashCode} and\n\t * {@link Object#equals(Object) equals}.\n\t * <p>\n\t * Imports from multiple {@link DeterminableImports} instances may be combined by the\n\t * caller to create a complete set.\n\t * <p>\n\t * Unlike {@link ImportSelector} and {@link ImportBeanDefinitionRegistrar} any\n\t * {@link Aware} callbacks will not be invoked before this method is called.\n\t * @param metadata the source meta-data\n\t * @return a key representing the annotations that actually drive the import\n\t */",
            "/**\n\t * 返回表示导入内容的对象集合；集合内对象须实现有效的\n\t * {@link Object#hashCode() hashCode} 与 {@link Object#equals(Object) equals}。\n\t * <p>\n\t * 调用方可合并多个 {@link DeterminableImports} 实例的导入以构成完整集合。\n\t * <p>\n\t * 与 {@link ImportSelector}、{@link ImportBeanDefinitionRegistrar} 不同，\n\t * 调用本方法前不会触发任何 {@link Aware} 回调。\n\t *\n\t * @param metadata 源元数据\n\t * @return 代表实际驱动导入的注解的键\n\t */",
        ),
    ],
    "ImportCandidates.java": [
        (
            "/**\n * Contains {@code @Configuration} import candidates, usually auto-configurations.\n *\n * The {@link #load(Class, ClassLoader)} method can be used to discover the import\n * candidates.\n *\n * @author Moritz Halbritter\n * @author Scott Frederick\n * @since 2.7.0\n */",
            "/**\n * 包含 {@code @Configuration} 导入候选（通常为自动配置类）。\n * <p>\n * 可通过 {@link #load(Class, ClassLoader)} 发现导入候选。\n *\n * @author Moritz Halbritter\n * @author Scott Frederick\n * @since 2.7.0\n */",
        ),
        (
            "/**\n\t * Returns the list of loaded import candidates.\n\t * @return the list of import candidates\n\t */",
            "/**\n\t * 返回已加载的导入候选列表。\n\t *\n\t * @return 导入候选列表\n\t */",
        ),
        (
            "/**\n\t * Loads the names of import candidates from the classpath. The names of the import\n\t * candidates are stored in files named\n\t * {@code META-INF/spring/full-qualified-annotation-name.imports} on the classpath.\n\t * Every line contains the full qualified name of the candidate class. Comments are\n\t * supported using the # character.\n\t * @param annotation annotation to load\n\t * @param classLoader class loader to use for loading\n\t * @return list of names of annotated classes\n\t */",
            "/**\n\t * 从类路径加载导入候选类名。候选名存储于\n\t * {@code META-INF/spring/全限定注解名.imports} 文件中，每行一个候选类全限定名，\n\t * 支持以 {@code #} 开头的注释。\n\t *\n\t * @param annotation 要加载的注解\n\t * @param classLoader 用于加载的类加载器\n\t * @return 候选类名列表\n\t */",
        ),
    ],
    "UserConfigurations.java": [
        (
            "/**\n * {@link Configurations} representing user-defined {@code @Configuration} classes (i.e.\n * those defined in classes usually written by the user).\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
            "/**\n * 表示用户定义的 {@code @Configuration} 类（即用户编写的类）的 {@link Configurations}。\n *\n * @author Phillip Webb\n * @since 2.0.0\n */",
        ),
    ],
    "ConfigData.java": [
        (
            "/**\n * Configuration data that has been loaded from a {@link ConfigDataResource} and may\n * ultimately contribute {@link PropertySource property sources} to Spring's\n * {@link Environment}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n * @see ConfigDataLocationResolver\n * @see ConfigDataLoader\n */",
            "/**\n * 从 {@link ConfigDataResource} 加载的配置数据，最终可向 Spring {@link Environment}\n * 贡献 {@link PropertySource 属性源}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n * @see ConfigDataLocationResolver\n * @see ConfigDataLoader\n */",
        ),
        (
            "/**\n\t * A {@link ConfigData} instance that contains no data.\n\t */",
            "/**\n\t * 不含任何数据的 {@link ConfigData} 实例。\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigData} instance with the same options applied to each\n\t * source.\n\t * @param propertySources the config data property sources in ascending priority\n\t * order.\n\t * @param options the config data options applied to each source\n\t * @see #ConfigData(Collection, PropertySourceOptions)\n\t */",
            "/**\n\t * 创建新的 {@link ConfigData} 实例，对每个源应用相同选项。\n\t *\n\t * @param propertySources 按优先级升序排列的配置数据属性源\n\t * @param options 应用于每个源的配置数据选项\n\t * @see #ConfigData(Collection, PropertySourceOptions)\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigData} instance with specific property source options.\n\t * @param propertySources the config data property sources in ascending priority\n\t * order.\n\t * @param propertySourceOptions the property source options\n\t * @since 2.4.5\n\t */",
            "/**\n\t * 创建新的 {@link ConfigData} 实例，指定属性源选项。\n\t *\n\t * @param propertySources 按优先级升序排列的配置数据属性源\n\t * @param propertySourceOptions 属性源选项\n\t * @since 2.4.5\n\t */",
        ),
        (
            "/**\n\t * Return the configuration data property sources in ascending priority order. If the\n\t * same key is contained in more than one of the sources, then the later source will\n\t * win.\n\t * @return the config data property sources\n\t */",
            "/**\n\t * 按优先级升序返回配置数据属性源；若多个源含相同键，后出现的源优先。\n\t *\n\t * @return 配置数据属性源\n\t */",
        ),
        (
            "/**\n\t * Return the {@link Options config data options} that apply to the given source.\n\t * @param propertySource the property source to check\n\t * @return the options that apply\n\t * @since 2.4.5\n\t */",
            "/**\n\t * 返回应用于给定源的 {@link Options 配置数据选项}。\n\t *\n\t * @param propertySource 要检查的属性源\n\t * @return 适用的选项\n\t * @since 2.4.5\n\t */",
        ),
        (
            "/**\n\t * Strategy interface used to supply {@link Options} for a given\n\t * {@link PropertySource}.\n\t *\n\t * @since 2.4.5\n\t */",
            "/**\n\t * 为给定 {@link PropertySource} 提供 {@link Options} 的策略接口。\n\t *\n\t * @since 2.4.5\n\t */",
        ),
        (
            "/**\n\t\t * {@link PropertySourceOptions} instance that always returns\n\t\t * {@link Options#NONE}.\n\t\t * @since 2.4.6\n\t\t */",
            "/**\n\t\t * 始终返回 {@link Options#NONE} 的 {@link PropertySourceOptions} 实例。\n\t\t * @since 2.4.6\n\t\t */",
        ),
        (
            "/**\n\t\t * Return the options that should apply for the given property source.\n\t\t * @param propertySource the property source\n\t\t * @return the options to apply\n\t\t */",
            "/**\n\t\t * 返回应用于给定属性源的选项。\n\t\t *\n\t\t * @param propertySource 属性源\n\t\t * @return 要应用的选项\n\t\t */",
        ),
        (
            "/**\n\t\t * Create a new {@link PropertySourceOptions} instance that always returns the\n\t\t * same options regardless of the property source.\n\t\t * @param options the options to return\n\t\t * @return a new {@link PropertySourceOptions} instance\n\t\t */",
            "/**\n\t\t * 创建始终返回相同选项（与属性源无关）的 {@link PropertySourceOptions} 实例。\n\t\t *\n\t\t * @param options 要返回的选项\n\t\t * @return 新的 {@link PropertySourceOptions} 实例\n\t\t */",
        ),
        (
            "/**\n\t * {@link PropertySourceOptions} that always returns the same result.\n\t */",
            "/**\n\t * 始终返回相同结果的 {@link PropertySourceOptions}。\n\t */",
        ),
        (
            "/**\n\t * A set of {@link Option} flags.\n\t *\n\t * @since 2.4.5\n\t */",
            "/**\n\t * {@link Option} 标志集合。\n\t *\n\t * @since 2.4.5\n\t */",
        ),
        (
            "/**\n\t\t * No options.\n\t\t */",
            "/**\n\t\t * 无选项。\n\t\t */",
        ),
        (
            "/**\n\t\t * Returns if the given option is contained in this set.\n\t\t * @param option the option to check\n\t\t * @return {@code true} of the option is present\n\t\t */",
            "/**\n\t\t * 判断本集合是否包含给定选项。\n\t\t *\n\t\t * @param option 要检查的选项\n\t\t * @return 选项存在时为 {@code true}\n\t\t */",
        ),
        (
            "/**\n\t\t * Create a new {@link Options} instance that contains the options in this set\n\t\t * excluding the given option.\n\t\t * @param option the option to exclude\n\t\t * @return a new {@link Options} instance\n\t\t */",
            "/**\n\t\t * 创建新 {@link Options} 实例，包含本集合中除给定选项外的所有选项。\n\t\t *\n\t\t * @param option 要排除的选项\n\t\t * @return 新的 {@link Options} 实例\n\t\t */",
        ),
        (
            "/**\n\t\t * Create a new {@link Options} instance that contains the options in this set\n\t\t * including the given option.\n\t\t * @param option the option to include\n\t\t * @return a new {@link Options} instance\n\t\t */",
            "/**\n\t\t * 创建新 {@link Options} 实例，包含本集合及给定选项。\n\t\t *\n\t\t * @param option 要包含的选项\n\t\t * @return 新的 {@link Options} 实例\n\t\t */",
        ),
        (
            "/**\n\t\t * Create a new instance with the given {@link Option} values.\n\t\t * @param options the options to include\n\t\t * @return a new {@link Options} instance\n\t\t */",
            "/**\n\t\t * 使用给定 {@link Option} 值创建新实例。\n\t\t *\n\t\t * @param options 要包含的选项\n\t\t * @return 新的 {@link Options} 实例\n\t\t */",
        ),
        (
            "/**\n\t * Option flags that can be applied.\n\t */",
            "/**\n\t * 可应用的选项标志。\n\t */",
        ),
        (
            "/**\n\t\t * Ignore all imports properties from the source.\n\t\t */",
            "/**\n\t\t * 忽略源中所有 import 相关属性。\n\t\t */",
        ),
        (
            "/**\n\t\t * Ignore all profile activation and include properties.\n\t\t * @since 2.4.3\n\t\t */",
            "/**\n\t\t * 忽略所有 profile 激活与 include 相关属性。\n\t\t * @since 2.4.3\n\t\t */",
        ),
        (
            "/**\n\t\t * Indicates that the source is \"profile specific\" and should be included after\n\t\t * profile specific sibling imports.\n\t\t * @since 2.4.5\n\t\t */",
            "/**\n\t\t * 表示源为「profile 特定」，应在 profile 特定同级 import 之后包含。\n\t\t * @since 2.4.5\n\t\t */",
        ),
    ],
    "ConfigDataActivationContext.java": [
        (
            "/**\n * Context information used when determining when to activate\n * {@link ConfigDataEnvironmentContributor contributed} {@link ConfigData}.\n *\n * @author Phillip Webb\n */",
            "/**\n * 判定何时激活 {@link ConfigDataEnvironmentContributor 贡献的} {@link ConfigData} 时使用的上下文信息。\n *\n * @author Phillip Webb\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataActivationContext} instance before any profiles have\n\t * been activated.\n\t * @param environment the source environment\n\t * @param binder a binder providing access to relevant config data contributions\n\t */",
            "/**\n\t * 在尚未激活任何 profile 时创建新的 {@link ConfigDataActivationContext} 实例。\n\t *\n\t * @param environment 源环境\n\t * @param binder 提供相关配置数据贡献访问的绑定器\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataActivationContext} instance with the given\n\t * {@link CloudPlatform} and {@link Profiles}.\n\t * @param cloudPlatform the cloud platform\n\t * @param profiles the profiles\n\t */",
            "/**\n\t * 使用给定 {@link CloudPlatform} 与 {@link Profiles} 创建新的 {@link ConfigDataActivationContext} 实例。\n\t *\n\t * @param cloudPlatform 云平台\n\t * @param profiles profile 信息\n\t */",
        ),
        (
            "/**\n\t * Return a new {@link ConfigDataActivationContext} with specific profiles.\n\t * @param profiles the profiles\n\t * @return a new {@link ConfigDataActivationContext} with specific profiles\n\t */",
            "/**\n\t * 返回包含指定 profile 的新 {@link ConfigDataActivationContext}。\n\t *\n\t * @param profiles profile 信息\n\t * @return 包含指定 profile 的新实例\n\t */",
        ),
        (
            "/**\n\t * Return the active {@link CloudPlatform} or {@code null}.\n\t * @return the active cloud platform\n\t */",
            "/**\n\t * 返回活动的 {@link CloudPlatform}，或 {@code null}。\n\t *\n\t * @return 活动云平台\n\t */",
        ),
        (
            "/**\n\t * Return profile information if it is available.\n\t * @return profile information or {@code null}\n\t */",
            "/**\n\t * 若可用则返回 profile 信息。\n\t *\n\t * @return profile 信息，或 {@code null}\n\t */",
        ),
    ],
}


PART2 = {
    "ConfigDataEnvironment.java": [
        (
            "/**\n * Wrapper around a {@link ConfigurableEnvironment} that can be used to import and apply\n * {@link ConfigData}. Configures the initial set of\n * {@link ConfigDataEnvironmentContributors} by wrapping property sources from the Spring\n * {@link Environment} and adding the initial set of locations.\n * <p>\n * The initial locations can be influenced through the {@link #LOCATION_PROPERTY},\n * {@value #ADDITIONAL_LOCATION_PROPERTY} and {@value #IMPORT_PROPERTY} properties. If no\n * explicit properties are set, the {@link #DEFAULT_SEARCH_LOCATIONS} will be used.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Nan Chiu\n */",
            "/**\n * 包装 {@link ConfigurableEnvironment}，用于导入并应用 {@link ConfigData}。\n * 通过包装 Spring {@link Environment} 中的属性源并添加初始位置集合来配置\n * {@link ConfigDataEnvironmentContributors} 初始集。\n * <p>\n * 初始位置可通过 {@link #LOCATION_PROPERTY}、{@value #ADDITIONAL_LOCATION_PROPERTY}\n * 与 {@value #IMPORT_PROPERTY} 属性影响；未显式设置时使用 {@link #DEFAULT_SEARCH_LOCATIONS}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Nan Chiu\n */",
        ),
        (
            "/**\n\t * Property used override the imported locations.\n\t */",
            "/**\n\t * 用于覆盖导入位置的属性。\n\t */",
        ),
        (
            "/**\n\t * Property used to provide additional locations to import.\n\t */",
            "/**\n\t * 用于提供额外导入位置的属性。\n\t */",
        ),
        (
            "/**\n\t * Property used to determine what action to take when a\n\t * {@code ConfigDataNotFoundAction} is thrown.\n\t * @see ConfigDataNotFoundAction\n\t */",
            "/**\n\t * 抛出 {@code ConfigDataNotFoundAction} 时决定采取何种动作的属性。\n\t * @see ConfigDataNotFoundAction\n\t */",
        ),
        (
            "/**\n\t * Default search locations used if not {@link #LOCATION_PROPERTY} is found.\n\t */",
            "/**\n\t * 未找到 {@link #LOCATION_PROPERTY} 时使用的默认搜索位置。\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataEnvironment} instance.\n\t * @param logFactory the deferred log factory\n\t * @param bootstrapContext the bootstrap context\n\t * @param environment the Spring {@link Environment}.\n\t * @param resourceLoader {@link ResourceLoader} to load resource locations\n\t * @param additionalProfiles any additional profiles to activate\n\t * @param environmentUpdateListener optional\n\t * {@link ConfigDataEnvironmentUpdateListener} that can be used to track\n\t * {@link Environment} updates.\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataEnvironment} 实例。\n\t *\n\t * @param logFactory 延迟日志工厂\n\t * @param bootstrapContext 引导上下文\n\t * @param environment Spring {@link Environment}\n\t * @param resourceLoader 加载资源位置的 {@link ResourceLoader}\n\t * @param additionalProfiles 要额外激活的 profile\n\t * @param environmentUpdateListener 可选的 {@link ConfigDataEnvironmentUpdateListener}，用于跟踪 {@link Environment} 更新\n\t */",
        ),
        (
            "/**\n\t * Process all contributions and apply any newly imported property sources to the\n\t * {@link Environment}.\n\t */",
            "/**\n\t * 处理所有贡献并将新导入的属性源应用到 {@link Environment}。\n\t */",
        ),
    ],
    "ConfigDataEnvironmentContributorPlaceholdersResolver.java": [
        (
            "/**\n * {@link PlaceholdersResolver} backed by one or more\n * {@link ConfigDataEnvironmentContributor} instances.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Moritz Halbritter\n */",
            "/**\n * 由一个或多个 {@link ConfigDataEnvironmentContributor} 实例支持的 {@link PlaceholdersResolver}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Moritz Halbritter\n */",
        ),
    ],
    "ConfigDataEnvironmentContributors.java": [
        (
            "/**\n * An immutable tree structure of {@link ConfigDataEnvironmentContributors} used to\n * process imports.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 用于处理 import 的 {@link ConfigDataEnvironmentContributor} 不可变树结构。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataEnvironmentContributors} instance.\n\t * @param logFactory the log factory\n\t * @param bootstrapContext the bootstrap context\n\t * @param contributors the initial set of contributors\n\t * @param conversionService the conversion service to use\n\t * @param environmentUpdateListener the environment update listener\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataEnvironmentContributors} 实例。\n\t *\n\t * @param logFactory 日志工厂\n\t * @param bootstrapContext 引导上下文\n\t * @param contributors 初始贡献者集合\n\t * @param conversionService 要使用的转换服务\n\t * @param environmentUpdateListener 环境更新监听器\n\t */",
        ),
        (
            "/**\n\t * Processes imports from all active contributors and return a new\n\t * {@link ConfigDataEnvironmentContributors} instance.\n\t * @param importer the importer used to import {@link ConfigData}\n\t * @param activationContext the current activation context or {@code null} if the\n\t * context has not yet been created\n\t * @return a {@link ConfigDataEnvironmentContributors} instance with all relevant\n\t * imports have been processed\n\t */",
            "/**\n\t * 处理所有活动贡献者的 import 并返回新的 {@link ConfigDataEnvironmentContributors} 实例。\n\t *\n\t * @param importer 用于导入 {@link ConfigData} 的导入器\n\t * @param activationContext 当前激活上下文；尚未创建时为 {@code null}\n\t * @return 已处理所有相关 import 的 {@link ConfigDataEnvironmentContributors} 实例\n\t */",
        ),
        (
            "/**\n\t * Returns the root contributor.\n\t * @return the root contributor.\n\t */",
            "/**\n\t * 返回根贡献者。\n\t *\n\t * @return 根贡献者\n\t */",
        ),
        (
            "/**\n\t * Return a {@link Binder} backed by the contributors.\n\t * @param activationContext the activation context\n\t * @param options binder options to apply\n\t * @return a binder instance\n\t */",
            "/**\n\t * 返回由贡献者支持的 {@link Binder}。\n\t *\n\t * @param activationContext 激活上下文\n\t * @param options 要应用的绑定器选项\n\t * @return 绑定器实例\n\t */",
        ),
        (
            "/**\n\t * Return a {@link Binder} backed by the contributors.\n\t * @param activationContext the activation context\n\t * @param filter a filter used to limit the contributors\n\t * @param options binder options to apply\n\t * @return a binder instance\n\t */",
            "/**\n\t * 返回由贡献者支持的 {@link Binder}。\n\t *\n\t * @param activationContext 激活上下文\n\t * @param filter 用于限制贡献者的过滤器\n\t * @param options 要应用的绑定器选项\n\t * @return 绑定器实例\n\t */",
        ),
        (
            "private static class ContributorDataLoaderContext implements ConfigDataLoaderContext {\n\n\t\tprivate final ConfigDataEnvironmentContributors contributors;\n\n\t\tContributorDataLoaderContext(ConfigDataEnvironmentContributors contributors) {",
            "/**\n\t * 贡献者对应的 {@link ConfigDataLoaderContext}。\n\t */\n\tprivate static class ContributorDataLoaderContext implements ConfigDataLoaderContext {\n\n\t\tprivate final ConfigDataEnvironmentContributors contributors;\n\n\t\tContributorDataLoaderContext(ConfigDataEnvironmentContributors contributors) {",
        ),
        (
            "private static class ContributorConfigDataLocationResolverContext implements ConfigDataLocationResolverContext {\n\n\t\tprivate final ConfigDataEnvironmentContributors contributors;",
            "/**\n\t * 贡献者对应的 {@link ConfigDataLocationResolverContext}。\n\t */\n\tprivate static class ContributorConfigDataLocationResolverContext implements ConfigDataLocationResolverContext {\n\n\t\tprivate final ConfigDataEnvironmentContributors contributors;",
        ),
        (
            "/**\n\t * Binder options that can be used with\n\t * {@link ConfigDataEnvironmentContributors#getBinder(ConfigDataActivationContext, BinderOption...)}.\n\t */",
            "/**\n\t * 可与 {@link ConfigDataEnvironmentContributors#getBinder(ConfigDataActivationContext, BinderOption...)} 配合使用的绑定器选项。\n\t */",
        ),
        (
            "/**\n\t\t * Throw an exception if an inactive contributor contains a bound value.\n\t\t */",
            "/**\n\t\t * 非活动贡献者包含绑定值时抛出异常。\n\t\t */",
        ),
    ],
    "ConfigDataEnvironmentPostProcessor.java": [
        (
            "/**\n * {@link EnvironmentPostProcessor} that loads and applies {@link ConfigData} to Spring's\n * {@link Environment}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Nguyen Bao Sach\n * @since 2.4.0\n */",
            "/**\n * 加载并将 {@link ConfigData} 应用到 Spring {@link Environment} 的 {@link EnvironmentPostProcessor}。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Nguyen Bao Sach\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * The default order for the processor.\n\t */",
            "/**\n\t * 处理器的默认顺序。\n\t */",
        ),
        (
            "/**\n\t * Property used to determine what action to take when a\n\t * {@code ConfigDataLocationNotFoundException} is thrown.\n\t * @see ConfigDataNotFoundAction\n\t */",
            "/**\n\t * 抛出 {@code ConfigDataLocationNotFoundException} 时决定采取何种动作的属性。\n\t * @see ConfigDataNotFoundAction\n\t */",
        ),
        (
            "/**\n\t * Apply {@link ConfigData} post-processing to an existing {@link Environment}. This\n\t * method can be useful when working with an {@link Environment} that has been created\n\t * directly and not necessarily as part of a {@link SpringApplication}.\n\t * @param environment the environment to apply {@link ConfigData} to\n\t */",
            "/**\n\t * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。\n\t * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。\n\t *\n\t * @param environment 要应用 {@link ConfigData} 的环境\n\t */",
        ),
        (
            "/**\n\t * Apply {@link ConfigData} post-processing to an existing {@link Environment}. This\n\t * method can be useful when working with an {@link Environment} that has been created\n\t * directly and not necessarily as part of a {@link SpringApplication}.\n\t * @param environment the environment to apply {@link ConfigData} to\n\t * @param resourceLoader the resource loader to use\n\t * @param bootstrapContext the bootstrap context to use or {@code null} to use a\n\t * throw-away context\n\t * @param additionalProfiles any additional profiles that should be applied\n\t */",
            "/**\n\t * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。\n\t * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。\n\t *\n\t * @param environment 要应用 {@link ConfigData} 的环境\n\t * @param resourceLoader 要使用的资源加载器\n\t * @param bootstrapContext 引导上下文；{@code null} 时使用临时上下文\n\t * @param additionalProfiles 要额外应用的 profile\n\t */",
        ),
        (
            "/**\n\t * Apply {@link ConfigData} post-processing to an existing {@link Environment}. This\n\t * method can be useful when working with an {@link Environment} that has been created\n\t * directly and not necessarily as part of a {@link SpringApplication}.\n\t * @param environment the environment to apply {@link ConfigData} to\n\t * @param resourceLoader the resource loader to use\n\t * @param bootstrapContext the bootstrap context to use or {@code null} to use a\n\t * throw-away context\n\t * @param additionalProfiles any additional profiles that should be applied\n\t * @param environmentUpdateListener optional\n\t * {@link ConfigDataEnvironmentUpdateListener} that can be used to track\n\t * {@link Environment} updates.\n\t */",
            "/**\n\t * 对现有 {@link Environment} 应用 {@link ConfigData} 后处理。\n\t * 适用于直接创建、未必通过 {@link SpringApplication} 创建的环境。\n\t *\n\t * @param environment 要应用 {@link ConfigData} 的环境\n\t * @param resourceLoader 要使用的资源加载器\n\t * @param bootstrapContext 引导上下文；{@code null} 时使用临时上下文\n\t * @param additionalProfiles 要额外应用的 profile\n\t * @param environmentUpdateListener 可选的 {@link ConfigDataEnvironmentUpdateListener}，用于跟踪 {@link Environment} 更新\n\t */",
        ),
    ],
    "ConfigDataEnvironmentUpdateListener.java": [
        (
            "/**\n * {@link EventListener} to listen to {@link Environment} updates triggered by the\n * {@link ConfigDataEnvironmentPostProcessor}.\n *\n * @author Phillip Webb\n * @since 2.4.2\n */",
            "/**\n * 监听 {@link ConfigDataEnvironmentPostProcessor} 触发的 {@link Environment} 更新的 {@link EventListener}。\n *\n * @author Phillip Webb\n * @since 2.4.2\n */",
        ),
        (
            "/**\n\t * A {@link ConfigDataEnvironmentUpdateListener} that does nothing.\n\t */",
            "/**\n\t * 空操作的 {@link ConfigDataEnvironmentUpdateListener}。\n\t */",
        ),
        (
            "/**\n\t * Called when a new {@link PropertySource} is added to the {@link Environment}.\n\t * @param propertySource the {@link PropertySource} that was added\n\t * @param location the original {@link ConfigDataLocation} of the source.\n\t * @param resource the {@link ConfigDataResource} of the source.\n\t */",
            "/**\n\t * 向 {@link Environment} 添加新 {@link PropertySource} 时调用。\n\t *\n\t * @param propertySource 已添加的 {@link PropertySource}\n\t * @param location 源的原始 {@link ConfigDataLocation}\n\t * @param resource 源的 {@link ConfigDataResource}\n\t */",
        ),
        (
            "/**\n\t * Called when {@link Environment} profiles are set.\n\t * @param profiles the profiles being set\n\t */",
            "/**\n\t * 设置 {@link Environment} profile 时调用。\n\t *\n\t * @param profiles 正在设置的 profile\n\t */",
        ),
        (
            "/**\n\t * Called when config data options are obtained for a particular property source.\n\t * @param configData the config data\n\t * @param propertySource the property source\n\t * @param options the options as provided by\n\t * {@link ConfigData#getOptions(PropertySource)}\n\t * @return the actual options that should be used\n\t * @since 3.5.1\n\t */",
            "/**\n\t * 获取特定属性源的配置数据选项时调用。\n\t *\n\t * @param configData 配置数据\n\t * @param propertySource 属性源\n\t * @param options {@link ConfigData#getOptions(PropertySource)} 提供的选项\n\t * @return 实际应使用的选项\n\t * @since 3.5.1\n\t */",
        ),
    ],
    "ConfigDataException.java": [
        (
            "/**\n * Abstract base class for configuration data exceptions.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 配置数据异常的抽象基类。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataException} instance.\n\t * @param message the exception message\n\t * @param cause the exception cause\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataException} 实例。\n\t *\n\t * @param message 异常消息\n\t * @param cause 异常原因\n\t */",
        ),
    ],
    "ConfigDataImporter.java": [
        (
            "/**\n * Imports {@link ConfigData} by {@link ConfigDataLocationResolver resolving} and\n * {@link ConfigDataLoader loading} locations. {@link ConfigDataResource resources} are\n * tracked to ensure that they are not imported multiple times.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 通过 {@link ConfigDataLocationResolver 解析} 并 {@link ConfigDataLoader 加载} 位置来导入 {@link ConfigData}。\n * 跟踪 {@link ConfigDataResource 资源} 以避免重复导入。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataImporter} instance.\n\t * @param logFactory the log factory\n\t * @param notFoundAction the action to take when a location cannot be found\n\t * @param resolvers the config data location resolvers\n\t * @param loaders the config data loaders\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataImporter} 实例。\n\t *\n\t * @param logFactory 日志工厂\n\t * @param notFoundAction 找不到位置时的处理动作\n\t * @param resolvers 配置数据位置解析器\n\t * @param loaders 配置数据加载器\n\t */",
        ),
        (
            "/**\n\t * Resolve and load the given list of locations, filtering any that have been\n\t * previously loaded.\n\t * @param activationContext the activation context\n\t * @param locationResolverContext the location resolver context\n\t * @param loaderContext the loader context\n\t * @param locations the locations to resolve\n\t * @return a map of the loaded locations and data\n\t */",
            "/**\n\t * 解析并加载给定位置列表，过滤已加载项。\n\t *\n\t * @param activationContext 激活上下文\n\t * @param locationResolverContext 位置解析器上下文\n\t * @param loaderContext 加载器上下文\n\t * @param locations 要解析的位置\n\t * @return 已加载位置与数据的映射\n\t */",
        ),
    ],
    "ConfigDataLoader.java": [
        (
            "/**\n * Strategy class that can be used to load {@link ConfigData} for a given\n * {@link ConfigDataResource}. Implementations should be added as {@code spring.factories}\n * entries. The following constructor parameter types are supported:\n * <ul>\n * <li>{@link DeferredLogFactory} - if the loader needs deferred logging</li>\n * <li>{@link ConfigurableBootstrapContext} - A bootstrap context that can be used to\n * store objects that may be expensive to create, or need to be shared\n * ({@link BootstrapContext} or {@link BootstrapRegistry} may also be used).</li>\n * </ul>\n * <p>\n * Multiple loaders cannot claim the same resource.\n *\n * @param <R> the resource type\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
            "/**\n * 为给定 {@link ConfigDataResource} 加载 {@link ConfigData} 的策略类。\n * 实现应作为 {@code spring.factories} 条目注册，支持以下构造器参数类型：\n * <ul>\n * <li>{@link DeferredLogFactory} — 需要延迟日志时</li>\n * <li>{@link ConfigurableBootstrapContext} — 可存储创建成本高或需共享的对象\n * （也可使用 {@link BootstrapContext} 或 {@link BootstrapRegistry}）</li>\n * </ul>\n * <p>\n * 多个加载器不能声明同一资源。\n *\n * @param <R> 资源类型\n * @author Phillip Webb\n * @author Madhura Bhave\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Returns if the specified resource can be loaded by this instance.\n\t * @param context the loader context\n\t * @param resource the resource to check.\n\t * @return if the resource is supported by this loader\n\t */",
            "/**\n\t * 判断本实例是否可加载指定资源。\n\t *\n\t * @param context 加载器上下文\n\t * @param resource 要检查的资源\n\t * @return 本加载器是否支持该资源\n\t */",
        ),
        (
            "/**\n\t * Load {@link ConfigData} for the given resource.\n\t * @param context the loader context\n\t * @param resource the resource to load\n\t * @return the loaded config data or {@code null} if the location should be skipped\n\t * @throws IOException on IO error\n\t * @throws ConfigDataResourceNotFoundException if the resource cannot be found\n\t */",
            "/**\n\t * 为给定资源加载 {@link ConfigData}。\n\t *\n\t * @param context 加载器上下文\n\t * @param resource 要加载的资源\n\t * @return 已加载的配置数据；应跳过该位置时为 {@code null}\n\t * @throws IOException IO 错误\n\t * @throws ConfigDataResourceNotFoundException 找不到资源\n\t */",
        ),
    ],
    "ConfigDataLoaderContext.java": [
        (
            "/**\n * Context provided to {@link ConfigDataLoader} methods.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 提供给 {@link ConfigDataLoader} 方法的上下文。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Provides access to the {@link ConfigurableBootstrapContext} shared across all\n\t * {@link EnvironmentPostProcessor EnvironmentPostProcessors}.\n\t * @return the bootstrap context\n\t */",
            "/**\n\t * 提供对所有 {@link EnvironmentPostProcessor EnvironmentPostProcessors} 共享的\n\t * {@link ConfigurableBootstrapContext} 的访问。\n\t *\n\t * @return 引导上下文\n\t */",
        ),
    ],
    "ConfigDataLoaders.java": [
        (
            "/**\n * A collection of {@link ConfigDataLoader} instances loaded through\n * {@code spring.factories}.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
            "/**\n * 通过 {@code spring.factories} 加载的 {@link ConfigDataLoader} 实例集合。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataLoaders} instance.\n\t * @param logFactory the deferred log factory\n\t * @param bootstrapContext the bootstrap context\n\t * @param springFactoriesLoader the loader to use\n\t */",
            "/**\n\t * 创建新的 {@link ConfigDataLoaders} 实例。\n\t *\n\t * @param logFactory 延迟日志工厂\n\t * @param bootstrapContext 引导上下文\n\t * @param springFactoriesLoader 要使用的加载器\n\t */",
        ),
        (
            "/**\n\t * Load {@link ConfigData} using the first appropriate {@link ConfigDataLoader}.\n\t * @param <R> the resource type\n\t * @param context the loader context\n\t * @param resource the resource to load\n\t * @return the loaded {@link ConfigData}\n\t * @throws IOException on IO error\n\t */",
            "/**\n\t * 使用第一个合适的 {@link ConfigDataLoader} 加载 {@link ConfigData}。\n\t *\n\t * @param <R> 资源类型\n\t * @param context 加载器上下文\n\t * @param resource 要加载的资源\n\t * @return 已加载的 {@link ConfigData}\n\t * @throws IOException IO 错误\n\t */",
        ),
    ],
    "ConfigDataLocation.java": [
        (
            "/**\n * A user specified location that can be {@link ConfigDataLocationResolver resolved} to\n * one or more {@link ConfigDataResource config data resources}. A\n * {@link ConfigDataLocation} is a simple wrapper around a {@link String} value. The exact\n * format of the value will depend on the underlying technology, but is usually a URL like\n * syntax consisting of a prefix and path. For example, {@code crypt:somehost/somepath}.\n * <p>\n * Locations can be mandatory or {@link #isOptional() optional}. Optional locations are\n * prefixed with {@code optional:}.\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
            "/**\n * 用户指定的位置，可 {@link ConfigDataLocationResolver 解析} 为一个或多个 {@link ConfigDataResource 配置数据资源}。\n * {@link ConfigDataLocation} 是对 {@link String} 值的简单包装；格式取决于底层技术，\n * 通常为前缀加路径的 URL 风格，例如 {@code crypt:somehost/somepath}。\n * <p>\n * 位置可为必选或 {@link #isOptional() 可选}；可选位置以 {@code optional:} 为前缀。\n *\n * @author Phillip Webb\n * @since 2.4.0\n */",
        ),
        (
            "/**\n\t * Prefix used to indicate that a {@link ConfigDataResource} is optional.\n\t */",
            "/**\n\t * 表示 {@link ConfigDataResource} 为可选的前缀。\n\t */",
        ),
        (
            "/**\n\t * Return if the location is optional and should ignore\n\t * {@link ConfigDataNotFoundException}.\n\t * @return if the location is optional\n\t */",
            "/**\n\t * 返回位置是否可选（应忽略 {@link ConfigDataNotFoundException}）。\n\t *\n\t * @return 位置是否可选\n\t */",
        ),
        (
            "/**\n\t * Return the value of the location (always excluding any user specified\n\t * {@code optional:} prefix).\n\t * @return the location value\n\t */",
            "/**\n\t * 返回位置值（始终不含用户指定的 {@code optional:} 前缀）。\n\t *\n\t * @return 位置值\n\t */",
        ),
        (
            "/**\n\t * Return if {@link #getValue()} has the specified prefix.\n\t * @param prefix the prefix to check\n\t * @return if the value has the prefix\n\t */",
            "/**\n\t * 判断 {@link #getValue()} 是否具有指定前缀。\n\t *\n\t * @param prefix 要检查的前缀\n\t * @return 值是否具有该前缀\n\t */",
        ),
        (
            "/**\n\t * Return {@link #getValue()} with the specified prefix removed. If the location does\n\t * not have the given prefix then the {@link #getValue()} is returned unchanged.\n\t * @param prefix the prefix to check\n\t * @return the value with the prefix removed\n\t */",
            "/**\n\t * 返回移除指定前缀后的 {@link #getValue()}；若无该前缀则原样返回。\n\t *\n\t * @param prefix 要检查的前缀\n\t * @return 移除前缀后的值\n\t */",
        ),
        (
            "/**\n\t * Return an array of {@link ConfigDataLocation} elements built by splitting this\n\t * {@link ConfigDataLocation} around a delimiter of {@code \";\"}.\n\t * @return the split locations\n\t * @since 2.4.7\n\t */",
            "/**\n\t * 以 {@code \";\"} 为分隔符拆分本 {@link ConfigDataLocation}，返回 {@link ConfigDataLocation} 数组。\n\t *\n\t * @return 拆分后的位置\n\t * @since 2.4.7\n\t */",
        ),
        (
            "/**\n\t * Return an array of {@link ConfigDataLocation} elements built by splitting this\n\t * {@link ConfigDataLocation} around the specified delimiter.\n\t * @param delimiter the delimiter to split on\n\t * @return the split locations\n\t * @since 2.4.7\n\t */",
            "/**\n\t * 以指定分隔符拆分本 {@link ConfigDataLocation}，返回 {@link ConfigDataLocation} 数组。\n\t *\n\t * @param delimiter 分隔符\n\t * @return 拆分后的位置\n\t * @since 2.4.7\n\t */",
        ),
        (
            "/**\n\t * Create a new {@link ConfigDataLocation} with a specific {@link Origin}.\n\t * @param origin the origin to set\n\t * @return a new {@link ConfigDataLocation} instance.\n\t */",
            "/**\n\t * 创建带有指定 {@link Origin} 的新 {@link ConfigDataLocation}。\n\t *\n\t * @param origin 要设置的来源\n\t * @return 新的 {@link ConfigDataLocation} 实例\n\t */",
        ),
        (
            "/**\n\t * Factory method to create a new {@link ConfigDataLocation} from a string.\n\t * @param location the location string\n\t * @return the {@link ConfigDataLocation} (which may be empty)\n\t */",
            "/**\n\t * 从字符串创建新 {@link ConfigDataLocation} 的工厂方法。\n\t *\n\t * @param location 位置字符串\n\t * @return {@link ConfigDataLocation}（可能为空）\n\t */",
        ),
    ],
}

contributor_replacements = [('/**\n * A single element that may directly or indirectly contribute configuration data to the\n * {@link Environment}. There are several {@link Kind kinds} of contributor, all are\n * immutable and will be replaced with new versions as imports are processed.\n * <p>\n * Contributors may provide a set of imports that should be processed and ultimately\n * turned into children. There are two distinct import phases:\n * <ul>\n * <li>{@link ImportPhase#BEFORE_PROFILE_ACTIVATION Before} profiles have been\n * activated.</li>\n * <li>{@link ImportPhase#AFTER_PROFILE_ACTIVATION After} profiles have been\n * activated.</li>\n * </ul>\n * In each phase <em>all</em> imports will be resolved before they are loaded.\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Nan Chiu\n */', '/**\n * 可直接或间接向 {@link Environment} 贡献配置数据的单个元素。\n * 存在多种 {@link Kind 类型} 的贡献者，均为不可变，import 处理过程中会被新版本替换。\n * <p>\n * 贡献者可提供待处理的 import 集合，最终转为子节点。import 分两个阶段：\n * <ul>\n * <li>{@link ImportPhase#BEFORE_PROFILE_ACTIVATION profile 激活前}</li>\n * <li>{@link ImportPhase#AFTER_PROFILE_ACTIVATION profile 激活后}</li>\n * </ul>\n * 每个阶段中，<em>所有</em> import 会先解析再加载。\n *\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Nan Chiu\n */'), ('/**\n\t * Create a new {@link ConfigDataEnvironmentContributor} instance.\n\t * @param kind the contributor kind\n\t * @param location the location of this contributor\n\t * @param resource the resource that contributed the data or {@code null}\n\t * @param fromProfileSpecificImport if the contributor is from a profile specific\n\t * import\n\t * @param propertySource the property source for the data or {@code null}\n\t * @param configurationPropertySource the configuration property source for the data\n\t * or {@code null}\n\t * @param properties the config data properties or {@code null}\n\t * @param configDataOptions any config data options that should apply\n\t * @param children the children of this contributor at each {@link ImportPhase}\n\t * @param conversionService the conversion service to use\n\t */', '/**\n\t * 创建新的 {@link ConfigDataEnvironmentContributor} 实例。\n\t *\n\t * @param kind 贡献者类型\n\t * @param location 本贡献者的位置\n\t * @param resource 贡献数据的资源，或 {@code null}\n\t * @param fromProfileSpecificImport 是否来自 profile 特定 import\n\t * @param propertySource 数据的属性源，或 {@code null}\n\t * @param configurationPropertySource 数据的配置属性源，或 {@code null}\n\t * @param properties 配置数据属性，或 {@code null}\n\t * @param configDataOptions 应应用的配置数据选项\n\t * @param children 各 {@link ImportPhase} 下的子贡献者\n\t * @param conversionService 要使用的转换服务\n\t */'), ('/**\n\t * Return the contributor kind.\n\t * @return the kind of contributor\n\t */', '/**\n\t * 返回贡献者类型。\n\t *\n\t * @return 贡献者类型\n\t */'), ('/**\n\t * Return if this contributor is currently active.\n\t * @param activationContext the activation context\n\t * @return if the contributor is active\n\t */', '/**\n\t * 返回本贡献者当前是否活动。\n\t *\n\t * @param activationContext 激活上下文\n\t * @return 贡献者是否活动\n\t */'), ('/**\n\t * Return the resource that contributed this instance.\n\t * @return the resource or {@code null}\n\t */', '/**\n\t * 返回贡献本实例的资源。\n\t *\n\t * @return 资源，或 {@code null}\n\t */'), ('/**\n\t * Return if the contributor is from a profile specific import.\n\t * @return if the contributor is profile specific\n\t */', '/**\n\t * 返回贡献者是否来自 profile 特定 import。\n\t *\n\t * @return 是否为 profile 特定\n\t */'), ('/**\n\t * Return the property source for this contributor.\n\t * @return the property source or {@code null}\n\t */', '/**\n\t * 返回本贡献者的属性源。\n\t *\n\t * @return 属性源，或 {@code null}\n\t */'), ('/**\n\t * Return the configuration property source for this contributor.\n\t * @return the configuration property source or {@code null}\n\t */', '/**\n\t * 返回本贡献者的配置属性源。\n\t *\n\t * @return 配置属性源，或 {@code null}\n\t */'), ('/**\n\t * Return if the contributor has a specific config data option.\n\t * @param option the option to check\n\t * @return {@code true} if the option is present\n\t */', '/**\n\t * 返回贡献者是否具有指定配置数据选项。\n\t *\n\t * @param option 要检查的选项\n\t * @return 选项存在时为 {@code true}\n\t */'), ('/**\n\t * Return any imports requested by this contributor.\n\t * @return the imports\n\t */', '/**\n\t * 返回本贡献者请求的所有 import。\n\t *\n\t * @return import 列表\n\t */'), ('/**\n\t * Return true if this contributor has imports that have not yet been processed in the\n\t * given phase.\n\t * @param importPhase the import phase\n\t * @return if there are unprocessed imports\n\t */', '/**\n\t * 若本贡献者在给定阶段仍有未处理的 import 则返回 {@code true}。\n\t *\n\t * @param importPhase import 阶段\n\t * @return 是否存在未处理的 import\n\t */'), ('/**\n\t * Return children of this contributor for the given phase.\n\t * @param importPhase the import phase\n\t * @return a list of children\n\t */', '/**\n\t * 返回给定阶段下本贡献者的子节点。\n\t *\n\t * @param importPhase import 阶段\n\t * @return 子节点列表\n\t */'), ('/**\n\t * Returns a {@link Stream} that traverses this contributor and all its children in\n\t * priority order.\n\t * @return the stream\n\t */', '/**\n\t * 按优先级顺序遍历本贡献者及其所有子节点的 {@link Stream}。\n\t *\n\t * @return 流\n\t */'), ('/**\n\t * Returns an {@link Iterator} that traverses this contributor and all its children in\n\t * priority order.\n\t * @return the iterator\n\t * @see java.lang.Iterable#iterator()\n\t */', '/**\n\t * 按优先级顺序遍历本贡献者及其所有子节点的 {@link Iterator}。\n\t *\n\t * @return 迭代器\n\t * @see java.lang.Iterable#iterator()\n\t */'), ('/**\n\t * Create a new {@link ConfigDataEnvironmentContributor} with bound\n\t * {@link ConfigDataProperties}.\n\t * @param contributors the contributors used for binding\n\t * @param activationContext the activation context\n\t * @return a new contributor instance\n\t */', '/**\n\t * 创建已绑定 {@link ConfigDataProperties} 的新 {@link ConfigDataEnvironmentContributor}。\n\t *\n\t * @param contributors 用于绑定的贡献者\n\t * @param activationContext 激活上下文\n\t * @return 新贡献者实例\n\t */'), ('/**\n\t * Create a new {@link ConfigDataEnvironmentContributor} instance with a new set of\n\t * children for the given phase.\n\t * @param importPhase the import phase\n\t * @param children the new children\n\t * @return a new contributor instance\n\t */', '/**\n\t * 为给定阶段创建带有新子节点集合的新 {@link ConfigDataEnvironmentContributor} 实例。\n\t *\n\t * @param importPhase import 阶段\n\t * @param children 新子节点\n\t * @return 新贡献者实例\n\t */'), ('/**\n\t * Create a new {@link ConfigDataEnvironmentContributor} instance where an existing\n\t * child is replaced.\n\t * @param existing the existing node that should be replaced\n\t * @param replacement the replacement node that should be used instead\n\t * @return a new {@link ConfigDataEnvironmentContributor} instance\n\t */', '/**\n\t * 创建替换现有子节点的新 {@link ConfigDataEnvironmentContributor} 实例。\n\t *\n\t * @param existing 应被替换的现有节点\n\t * @param replacement 替换节点\n\t * @return 新的 {@link ConfigDataEnvironmentContributor} 实例\n\t */'), ('/**\n\t * Factory method to create a {@link Kind#ROOT root} contributor.\n\t * @param contributors the immediate children of the root\n\t * @param conversionService the conversion service to use\n\t * @return a new {@link ConfigDataEnvironmentContributor} instance\n\t */', '/**\n\t * 创建 {@link Kind#ROOT 根} 贡献者的工厂方法。\n\t *\n\t * @param contributors 根的直系子节点\n\t * @param conversionService 要使用的转换服务\n\t * @return 新的 {@link ConfigDataEnvironmentContributor} 实例\n\t */'), ('/**\n\t * Factory method to create a {@link Kind#INITIAL_IMPORT initial import} contributor.\n\t * This contributor is used to trigger initial imports of additional contributors. It\n\t * does not contribute any properties itself.\n\t * @param initialImports the initial import locations (with placeholders resolved)\n\t * @param conversionService the conversion service to use\n\t * @return a new {@link ConfigDataEnvironmentContributor} instance\n\t */', '/**\n\t * 创建 {@link Kind#INITIAL_IMPORT 初始 import} 贡献者的工厂方法。\n\t * 用于触发额外贡献者的初始 import，本身不贡献任何属性。\n\t *\n\t * @param initialImports 初始 import 位置（占位符已解析）\n\t * @param conversionService 要使用的转换服务\n\t * @return 新的 {@link ConfigDataEnvironmentContributor} 实例\n\t */'), ("/**\n\t * Factory method to create a contributor that wraps an {@link Kind#EXISTING existing}\n\t * property source. The contributor provides access to existing properties, but\n\t * doesn't actively import any additional contributors.\n\t * @param propertySource the property source to wrap\n\t * @param conversionService the conversion service to use\n\t * @return a new {@link ConfigDataEnvironmentContributor} instance\n\t */", '/**\n\t * 创建包装 {@link Kind#EXISTING 现有} 属性源的贡献者的工厂方法。\n\t * 提供对现有属性的访问，但不主动 import 额外贡献者。\n\t *\n\t * @param propertySource 要包装的属性源\n\t * @param conversionService 要使用的转换服务\n\t * @return 新的 {@link ConfigDataEnvironmentContributor} 实例\n\t */'), ('/**\n\t * Factory method to create an {@link Kind#UNBOUND_IMPORT unbound import} contributor.\n\t * This contributor has been actively imported from another contributor and may itself\n\t * import further contributors later.\n\t * @param location the location of this contributor\n\t * @param resource the config data resource\n\t * @param profileSpecific if the contributor is from a profile specific import\n\t * @param configData the config data\n\t * @param propertySourceIndex the index of the property source that should be used\n\t * @param conversionService the conversion service to use\n\t * @param environmentUpdateListener the environment update listener\n\t * @return a new {@link ConfigDataEnvironmentContributor} instance\n\t */', '/**\n\t * 创建 {@link Kind#UNBOUND_IMPORT 未绑定 import} 贡献者的工厂方法。\n\t * 该贡献者由其他贡献者主动 import，后续可能继续 import 更多贡献者。\n\t *\n\t * @param location 本贡献者的位置\n\t * @param resource 配置数据资源\n\t * @param profileSpecific 是否来自 profile 特定 import\n\t * @param configData 配置数据\n\t * @param propertySourceIndex 应使用的属性源索引\n\t * @param conversionService 要使用的转换服务\n\t * @param environmentUpdateListener 环境更新监听器\n\t * @return 新的 {@link ConfigDataEnvironmentContributor} 实例\n\t */'), ('/**\n\t * Factory method to create an {@link Kind#EMPTY_LOCATION empty location} contributor.\n\t * @param location the location of this contributor\n\t * @param profileSpecific if the contributor is from a profile specific import\n\t * @param conversionService the conversion service to use\n\t * @return a new {@link ConfigDataEnvironmentContributor} instance\n\t */', '/**\n\t * 创建 {@link Kind#EMPTY_LOCATION 空位置} 贡献者的工厂方法。\n\t *\n\t * @param location 本贡献者的位置\n\t * @param profileSpecific 是否来自 profile 特定 import\n\t * @param conversionService 要使用的转换服务\n\t * @return 新的 {@link ConfigDataEnvironmentContributor} 实例\n\t */'), ('/**\n\t * The various kinds of contributor.\n\t */', '/**\n\t * 贡献者的各种类型。\n\t */'), ('/**\n\t\t * A root contributor used contain the initial set of children.\n\t\t */', '/**\n\t\t * 包含初始子节点集合的根贡献者。\n\t\t */'), ('/**\n\t\t * An initial import that needs to be processed.\n\t\t */', '/**\n\t\t * 需要处理的初始 import。\n\t\t */'), ('/**\n\t\t * An existing property source that contributes properties but no imports.\n\t\t */', '/**\n\t\t * 贡献属性但不贡献 import 的现有属性源。\n\t\t */'), ('/**\n\t\t * A contributor with {@link ConfigData} imported from another contributor but not\n\t\t * yet bound.\n\t\t */', '/**\n\t\t * 从其他贡献者 import 了 {@link ConfigData} 但尚未绑定的贡献者。\n\t\t */'), ('/**\n\t\t * A contributor with {@link ConfigData} imported from another contributor that\n\t\t * has been.\n\t\t */', '/**\n\t\t * 从其他贡献者 import 了 {@link ConfigData} 且已完成绑定的贡献者。\n\t\t */'), ('/**\n\t\t * A valid location that contained nothing to load.\n\t\t */', '/**\n\t\t * 有效但无可加载内容的位置。\n\t\t */'), ('/**\n\t * Import phases that can be used when obtaining imports.\n\t */', '/**\n\t * 获取 import 时可用的 import 阶段。\n\t */'), ('/**\n\t\t * The phase before profiles have been activated.\n\t\t */', '/**\n\t\t * profile 激活前的阶段。\n\t\t */'), ('/**\n\t\t * The phase after profiles have been activated.\n\t\t */', '/**\n\t\t * profile 激活后的阶段。\n\t\t */'), ('/**\n\t\t * Return the {@link ImportPhase} based on the given activation context.\n\t\t * @param activationContext the activation context\n\t\t * @return the import phase\n\t\t */', '/**\n\t\t * 根据给定激活上下文返回 {@link ImportPhase}。\n\t\t *\n\t\t * @param activationContext 激活上下文\n\t\t * @return import 阶段\n\t\t */'), ('/**\n\t * Iterator that traverses the contributor tree.\n\t */', '/**\n\t * 遍历贡献者树的迭代器。\n\t */')]
FILE_REPLACEMENTS["ConfigDataEnvironmentContributor.java"] = contributor_replacements
FILE_REPLACEMENTS.update(PART2)


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:160]}...")
        text = text.replace(old, new, 1)
    return text


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
        if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
            shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = dst.read_text(encoding="utf-8")
            if has_chinese(text):
                cn_lines = len(re.findall(r"[\u4e00-\u9fff]", text))
                if cn_lines > 20:
                    ok += 1
                    print(f"SKIP(already CN) {rel}")
                    continue
            text = apply_replacements(text, reps)
            if not has_chinese(text):
                failures.append(f"NO_CHINESE_AFTER: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print("Marked 20 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
