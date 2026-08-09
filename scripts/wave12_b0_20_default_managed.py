DEFAULT_MANAGED_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "DefaultManagedTaskExecutor.java": [
        (
            "/**\n * JNDI-based variant of {@link ConcurrentTaskExecutor}, performing a default lookup for\n * JSR-236's \"java:comp/DefaultManagedExecutorService\" in a Jakarta EE/8 environment.\n *\n * <p>Note: This class is not strictly JSR-236 based; it can work with any regular\n * {@link java.util.concurrent.Executor} that can be found in JNDI.\n * The actual adapting to {@link jakarta.enterprise.concurrent.ManagedExecutorService}\n * happens in the base class {@link ConcurrentTaskExecutor} itself.\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see jakarta.enterprise.concurrent.ManagedExecutorService\n */",
            "/**\n * 基于 JNDI 的 {@link ConcurrentTaskExecutor} 变体，\n * 在 Jakarta EE/8 环境中默认查找 JSR-236 的 \"java:comp/DefaultManagedExecutorService\"。\n *\n * <p>注意：本类并非严格基于 JSR-236；可与 JNDI 中找到的任意常规\n * {@link java.util.concurrent.Executor} 配合工作。\n * 实际适配 {@link jakarta.enterprise.concurrent.ManagedExecutorService}\n * 在基类 {@link ConcurrentTaskExecutor} 自身中完成。\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see jakarta.enterprise.concurrent.ManagedExecutorService\n */",
        ),
        (
            "\t/**\n\t * Set the JNDI template to use for JNDI lookups.\n\t * @see org.springframework.jndi.JndiAccessor#setJndiTemplate\n\t */",
            "\t/**\n\t * 设置用于 JNDI 查找的 JNDI 模板。\n\t * @see org.springframework.jndi.JndiAccessor#setJndiTemplate\n\t */",
        ),
        (
            "\t/**\n\t * Set the JNDI environment to use for JNDI lookups.\n\t * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment\n\t */",
            "\t/**\n\t * 设置用于 JNDI 查找的 JNDI 环境。\n\t * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment\n\t */",
        ),
        (
            "\t/**\n\t * Set whether the lookup occurs in a Jakarta EE container, i.e. if the prefix\n\t * \"java:comp/env/\" needs to be added if the JNDI name doesn't already\n\t * contain it. PersistenceAnnotationBeanPostProcessor's default is \"true\".\n\t * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef\n\t */",
            "\t/**\n\t * 设置查找是否发生在 Jakarta EE 容器中，即若 JNDI 名称尚未包含前缀\n\t * \"java:comp/env/\" 是否需要添加。PersistenceAnnotationBeanPostProcessor 默认为 \"true\"。\n\t * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef\n\t */",
        ),
        (
            "\t/**\n\t * Specify a JNDI name of the {@link java.util.concurrent.Executor} to delegate to,\n\t * replacing the default JNDI name \"java:comp/DefaultManagedExecutorService\".\n\t * <p>This can either be a fully qualified JNDI name, or the JNDI name relative\n\t * to the current environment naming context if \"resourceRef\" is set to \"true\".\n\t * @see #setConcurrentExecutor\n\t * @see #setResourceRef\n\t */",
            "\t/**\n\t * 指定要委托的 {@link java.util.concurrent.Executor} 的 JNDI 名称，\n\t * 替换默认 JNDI 名称 \"java:comp/DefaultManagedExecutorService\"。\n\t * <p>可以是完全限定 JNDI 名称，或在 \"resourceRef\" 为 \"true\" 时\n\t * 相对于当前环境命名上下文的 JNDI 名称。\n\t * @see #setConcurrentExecutor\n\t * @see #setResourceRef\n\t */",
        ),
    ],
}
