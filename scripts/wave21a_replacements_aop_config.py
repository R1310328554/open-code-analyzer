"""Chinese JavaDoc replacements for springframework wave21a AOP config [1:4]."""

AOP_CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PointcutComponentDefinition.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.parsing.ComponentDefinition}\n * implementation that holds a pointcut definition.\n *\n * @author Rob Harrop\n * @since 2.0\n */",
            "/**\n * 持有切入点定义的 {@link org.springframework.beans.factory.parsing.ComponentDefinition}\n * 实现。\n *\n * @author Rob Harrop\n * @since 2.0\n */",
        ),
    ],
    "PointcutEntry.java": [
        (
            "/**\n * {@link ParseState} entry representing a pointcut.\n *\n * @author Mark Fisher\n * @since 2.0\n */",
            "/**\n * 表示切入点的 {@link ParseState} 条目。\n *\n * @author Mark Fisher\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new {@code PointcutEntry} instance.\n\t * @param name the bean name of the pointcut\n\t */",
            "\t/**\n\t * 创建新的 {@code PointcutEntry} 实例。\n\t * @param name 切入点的 Bean 名称\n\t */",
        ),
    ],
    "ScopedProxyBeanDefinitionDecorator.java": [
        (
            "/**\n * {@link BeanDefinitionDecorator} responsible for parsing the\n * {@code <aop:scoped-proxy/>} tag.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n */",
            "/**\n * 负责解析 {@code <aop:scoped-proxy/>} 标签的\n * {@link BeanDefinitionDecorator}。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n */",
        ),
        (
            "\t\t// Register the original bean definition as it will be referenced by the scoped proxy\n\t\t// and is relevant for tooling (validation, navigation).",
            "\t\t// 注册原始 Bean 定义，作用域代理会引用它，且对工具（校验、导航）有意义。",
        ),
    ],
    "SimpleBeanFactoryAwareAspectInstanceFactory.java": [
        (
            "/**\n * Implementation of {@link AspectInstanceFactory} that locates the aspect from the\n * {@link org.springframework.beans.factory.BeanFactory} using a configured bean name.\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * {@link AspectInstanceFactory} 的实现，通过配置的 Bean 名称\n * 从 {@link org.springframework.beans.factory.BeanFactory} 定位切面。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Set the name of the aspect bean. This is the bean that is returned when calling\n\t * {@link #getAspectInstance()}.\n\t */",
            "\t/**\n\t * 设置切面 Bean 的名称。调用 {@link #getAspectInstance()} 时返回该 Bean。\n\t */",
        ),
        (
            "\t/**\n\t * Look up the aspect bean from the {@link BeanFactory} and return it.\n\t * @see #setAspectBeanName\n\t */",
            "\t/**\n\t * 从 {@link BeanFactory} 查找切面 Bean 并返回。\n\t * @see #setAspectBeanName\n\t */",
        ),
    ],
    "SpringConfiguredBeanDefinitionParser.java": [
        (
            "/**\n * {@link BeanDefinitionParser} responsible for parsing the\n * {@code <aop:spring-configured/>} tag.\n *\n * <p><b>NOTE:</b> This is essentially a duplicate of Spring 2.5's\n * {@link org.springframework.context.config.SpringConfiguredBeanDefinitionParser}\n * for the {@code <context:spring-configured/>} tag, mirrored here for compatibility with\n * Spring 2.0's {@code <aop:spring-configured/>} tag (avoiding a direct dependency on the\n * context package).\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 负责解析 {@code <aop:spring-configured/>} 标签的\n * {@link BeanDefinitionParser}。\n *\n * <p><b>注意：</b> 这本质上是 Spring 2.5 中\n * {@link org.springframework.context.config.SpringConfiguredBeanDefinitionParser}\n * 针对 {@code <context:spring-configured/>} 标签的副本，在此镜像以兼容\n * Spring 2.0 的 {@code <aop:spring-configured/>} 标签（避免直接依赖 context 包）。\n *\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * The bean name of the internally managed bean configurer aspect.\n\t */",
            "\t/**\n\t * 内部管理的 Bean 配置器切面的 Bean 名称。\n\t */",
        ),
    ],
}
