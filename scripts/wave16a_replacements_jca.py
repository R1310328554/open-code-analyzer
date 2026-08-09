"""Chinese JavaDoc replacements for springframework wave16a JCA [0:1]."""

JCA_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SimpleBootstrapContext.java": [
        (
            "/**\n * Simple implementation of the JCA 1.7 {@link jakarta.resource.spi.BootstrapContext}\n * interface, used for bootstrapping a JCA ResourceAdapter in a local environment.\n *\n * <p>Delegates to the given WorkManager and XATerminator, if any. Creates simple\n * local instances of {@code java.util.Timer}.\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see jakarta.resource.spi.ResourceAdapter#start(jakarta.resource.spi.BootstrapContext)\n * @see ResourceAdapterFactoryBean\n */",
            "/**\n * JCA 1.7 {@link jakarta.resource.spi.BootstrapContext} 接口的简单实现，\n * 用于在本地环境中引导 JCA ResourceAdapter。\n *\n * <p>委托给给定的 WorkManager 和 XATerminator（若有）。\n * 创建 {@code java.util.Timer} 的简单本地实例。\n *\n * @author Juergen Hoeller\n * @since 2.0.3\n * @see jakarta.resource.spi.ResourceAdapter#start(jakarta.resource.spi.BootstrapContext)\n * @see ResourceAdapterFactoryBean\n */",
        ),
        (
            "\t/**\n\t * Create a new SimpleBootstrapContext for the given WorkManager,\n\t * with no XATerminator available.\n\t * @param workManager the JCA WorkManager to use (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定 WorkManager 创建 SimpleBootstrapContext，\n\t * 不提供 XATerminator。\n\t * @param workManager 要使用的 JCA WorkManager（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SimpleBootstrapContext for the given WorkManager and XATerminator.\n\t * @param workManager the JCA WorkManager to use (may be {@code null})\n\t * @param xaTerminator the JCA XATerminator to use (may be {@code null})\n\t */",
            "\t/**\n\t * 为给定 WorkManager 和 XATerminator 创建 SimpleBootstrapContext。\n\t * @param workManager 要使用的 JCA WorkManager（可为 {@code null}）\n\t * @param xaTerminator 要使用的 JCA XATerminator（可为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Create a new SimpleBootstrapContext for the given WorkManager, XATerminator\n\t * and TransactionSynchronizationRegistry.\n\t * @param workManager the JCA WorkManager to use (may be {@code null})\n\t * @param xaTerminator the JCA XATerminator to use (may be {@code null})\n\t * @param transactionSynchronizationRegistry the TransactionSynchronizationRegistry\n\t * to use (may be {@code null})\n\t * @since 5.0\n\t */",
            "\t/**\n\t * 为给定 WorkManager、XATerminator 和 TransactionSynchronizationRegistry\n\t * 创建 SimpleBootstrapContext。\n\t * @param workManager 要使用的 JCA WorkManager（可为 {@code null}）\n\t * @param xaTerminator 要使用的 JCA XATerminator（可为 {@code null}）\n\t * @param transactionSynchronizationRegistry 要使用的 TransactionSynchronizationRegistry\n\t * （可为 {@code null}）\n\t * @since 5.0\n\t */",
        ),
    ],
}
