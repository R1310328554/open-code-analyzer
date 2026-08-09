"""Chinese JavaDoc replacements for springframework wave24a spring-jdbc config."""

CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DatabasePopulatorConfigUtils.java": [
        (
            "/**\n * Internal utility methods used with JDBC configuration.\n *\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.1\n */",
            "/**\n * JDBC 配置使用的内部工具方法。\n *\n * @author Juergen Hoeller\n * @author Stephane Nicoll\n * @since 3.1\n */",
        ),
    ],
    "EmbeddedDatabaseBeanDefinitionParser.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} that\n * parses an {@code embedded-database} element and creates a {@link BeanDefinition}\n * for an {@link EmbeddedDatabaseFactoryBean}.\n *\n * <p>Picks up nested {@code script} elements and configures a\n * {@link ResourceDatabasePopulator} for each of them.\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.0\n * @see DatabasePopulatorConfigUtils\n */",
            "/**\n * 解析 {@code embedded-database} 元素并为\n * {@link EmbeddedDatabaseFactoryBean} 创建 {@link BeanDefinition} 的\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser}。\n *\n * <p>收集嵌套 {@code script} 元素，并为每个配置\n * {@link ResourceDatabasePopulator}。\n *\n * @author Oliver Gierke\n * @author Juergen Hoeller\n * @author Sam Brannen\n * @since 3.0\n * @see DatabasePopulatorConfigUtils\n */",
        ),
        (
            "\t/**\n\t * Constant for the \"database-name\" attribute.\n\t */",
            "\t/**\n\t * {@code database-name} 属性常量。\n\t */",
        ),
        (
            "\t/**\n\t * Constant for the \"generate-name\" attribute.\n\t */",
            "\t/**\n\t * {@code generate-name} 属性常量。\n\t */",
        ),
    ],
    "InitializeDatabaseBeanDefinitionParser.java": [
        (
            "/**\n * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} that parses an {@code initialize-database}\n * element and creates a {@link BeanDefinition} of type {@link DataSourceInitializer}. Picks up nested\n * {@code script} elements and configures a {@link ResourceDatabasePopulator} for them.\n *\n * @author Dave Syer\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * 解析 {@code initialize-database} 元素并创建 {@link DataSourceInitializer} 类型\n * {@link BeanDefinition} 的 {@link org.springframework.beans.factory.xml.BeanDefinitionParser}。\n * 收集嵌套 {@code script} 元素并为其配置 {@link ResourceDatabasePopulator}。\n *\n * @author Dave Syer\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
    ],
    "JdbcNamespaceHandler.java": [
        (
            "/**\n * {@link NamespaceHandler} for JDBC configuration namespace.\n * @author Oliver Gierke\n * @author Dave Syer\n */",
            "/**\n * JDBC 配置命名空间的 {@link NamespaceHandler}。\n * 注册 embedded-database 与 initialize-database 等解析器。\n * @author Oliver Gierke\n * @author Dave Syer\n */",
        ),
    ],
    "SortedResourcesFactoryBean.java": [
        (
            "/**\n * {@link FactoryBean} implementation that takes a list of location Strings\n * and creates a sorted array of {@link Resource} instances.\n *\n * @author Dave Syer\n * @author Juergen Hoeller\n * @author Christian Dupuis\n * @since 3.0\n */",
            "/**\n * 接收位置字符串列表并创建已排序 {@link Resource} 数组的\n * {@link FactoryBean} 实现。\n *\n * @author Dave Syer\n * @author Juergen Hoeller\n * @author Christian Dupuis\n * @since 3.0\n */",
        ),
    ],
}
