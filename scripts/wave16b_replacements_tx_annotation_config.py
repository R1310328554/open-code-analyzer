"""Wave 16b [20:40] Chinese JavaDoc replacements — transaction annotation configuration."""

TX_ANNOTATION_CONFIG_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractTransactionManagementConfiguration.java": [
        (
            "/**\n * Abstract base {@code @Configuration} class providing common structure for enabling\n * Spring's annotation-driven transaction management capability.\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableTransactionManagement\n */",
            "/**\n * 启用 Spring 注解驱动事务管理能力的抽象 {@code @Configuration} 基类，\n * 提供通用结构。\n *\n * @author Chris Beams\n * @author Stephane Nicoll\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableTransactionManagement\n */",
        ),
        (
            "\t/**\n\t * Default transaction manager, as configured through a {@link TransactionManagementConfigurer}.\n\t */",
            "\t/**\n\t * 通过 {@link TransactionManagementConfigurer} 配置的默认事务管理器。\n\t */",
        ),
    ],
    "ProxyTransactionManagementConfiguration.java": [
        (
            "/**\n * {@code @Configuration} class that registers the Spring infrastructure beans\n * necessary to enable proxy-based annotation-driven transaction management.\n *\n * @author Chris Beams\n * @author Sebastien Deleuze\n * @since 3.1\n * @see EnableTransactionManagement\n * @see TransactionManagementConfigurationSelector\n */",
            "/**\n * 注册启用基于代理的注解驱动事务管理所需 Spring 基础设施 Bean 的\n * {@code @Configuration} 类。\n *\n * @author Chris Beams\n * @author Sebastien Deleuze\n * @since 3.1\n * @see EnableTransactionManagement\n * @see TransactionManagementConfigurationSelector\n */",
        ),
    ],
    "RestrictedTransactionalEventListenerFactory.java": [
        (
            "/**\n * Extension of {@link TransactionalEventListenerFactory},\n * detecting invalid transaction configuration for transactional event listeners:\n * {@link Transactional} only supported with {@link Propagation#REQUIRES_NEW}\n * and {@link Propagation#NOT_SUPPORTED}.\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see org.springframework.transaction.event.TransactionalEventListener\n * @see Transactional\n */",
            "/**\n * {@link TransactionalEventListenerFactory} 的扩展，\n * 检测事务事件监听器上的无效事务配置：\n * {@link Transactional} 仅在与 {@link Propagation#REQUIRES_NEW}\n * 和 {@link Propagation#NOT_SUPPORTED} 组合时受支持。\n *\n * @author Juergen Hoeller\n * @since 6.1\n * @see org.springframework.transaction.event.TransactionalEventListener\n * @see Transactional\n */",
        ),
    ],
    "TransactionManagementConfigurationSelector.java": [
        (
            "/**\n * Selects which implementation of {@link AbstractTransactionManagementConfiguration}\n * should be used based on the value of {@link EnableTransactionManagement#mode} on the\n * importing {@code @Configuration} class.\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableTransactionManagement\n * @see ProxyTransactionManagementConfiguration\n * @see TransactionManagementConfigUtils#TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME\n * @see TransactionManagementConfigUtils#JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME\n */",
            "/**\n * 根据导入 {@code @Configuration} 类上\n * {@link EnableTransactionManagement#mode} 的值，\n * 选择应使用的 {@link AbstractTransactionManagementConfiguration} 实现。\n *\n * @author Chris Beams\n * @author Juergen Hoeller\n * @since 3.1\n * @see EnableTransactionManagement\n * @see ProxyTransactionManagementConfiguration\n * @see TransactionManagementConfigUtils#TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME\n * @see TransactionManagementConfigUtils#JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME\n */",
        ),
        (
            "\t/**\n\t * Returns {@link ProxyTransactionManagementConfiguration} or\n\t * {@code AspectJ(Jta)TransactionManagementConfiguration} for {@code PROXY}\n\t * and {@code ASPECTJ} values of {@link EnableTransactionManagement#mode()},\n\t * respectively.\n\t */",
            "\t/**\n\t * 分别针对 {@link EnableTransactionManagement#mode()} 的\n\t * {@code PROXY} 和 {@code ASPECTJ} 值，\n\t * 返回 {@link ProxyTransactionManagementConfiguration} 或\n\t * {@code AspectJ(Jta)TransactionManagementConfiguration}。\n\t */",
        ),
    ],
    "TransactionBeanRegistrationAotProcessor.java": [
        (
            "/**\n * AOT {@code BeanRegistrationAotProcessor} that detects the presence of\n * {@link Transactional @Transactional} on annotated elements and creates\n * the required reflection hints.\n *\n * @author Sebastien Deleuze\n * @since 6.0\n * @see TransactionRuntimeHints\n */",
            "/**\n * AOT {@code BeanRegistrationAotProcessor}，检测注解元素上\n * {@link Transactional @Transactional} 的存在并创建所需反射提示。\n *\n * @author Sebastien Deleuze\n * @since 6.0\n * @see TransactionRuntimeHints\n */",
        ),
    ],
}
