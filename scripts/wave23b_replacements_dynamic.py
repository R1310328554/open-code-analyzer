"""Chinese JavaDoc replacements for springframework wave23b target.dynamic package."""

DYNAMIC_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractRefreshableTargetSource.java": [
        (
            "/**\n * Abstract {@link org.springframework.aop.TargetSource} implementation that\n * wraps a refreshable target object. Subclasses can determine whether a\n * refresh is required, and need to provide fresh target objects.\n *\n * <p>Implements the {@link Refreshable} interface in order to allow for\n * explicit control over the refresh status.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see #requiresRefresh()\n * @see #freshTarget()\n */",
            "/**\n * 包装可刷新目标对象的 {@link org.springframework.aop.TargetSource} 抽象实现。\n * 子类可判定是否需要刷新，并须提供新目标对象。\n *\n * <p>实现 {@link Refreshable} 接口以允许显式控制刷新状态。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @author Juergen Hoeller\n * @since 2.0\n * @see #requiresRefresh()\n * @see #freshTarget()\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 供子类使用的日志记录器。 */",
        ),
        (
            "\t/**\n\t * Set the delay between refresh checks, in milliseconds.\n\t * Default is -1, indicating no refresh checks at all.\n\t * <p>Note that an actual refresh will only happen when\n\t * {@link #requiresRefresh()} returns {@code true}.\n\t */",
            "\t/**\n\t * 设置刷新检查间隔（毫秒）。\n\t * 默认为 -1，表示不进行任何刷新检查。\n\t * <p>注意：仅当 {@link #requiresRefresh()} 返回 {@code true} 时才会实际刷新。\n\t */",
        ),
        (
            "\t\tlogger.debug(\"Attempting to refresh target\");",
            "\t\tlogger.debug(\"正在尝试刷新目标\");",
        ),
        (
            "\t\tlogger.debug(\"Target refreshed successfully\");",
            "\t\tlogger.debug(\"目标刷新成功\");",
        ),
        (
            "\t\t\t// Going to perform a refresh check - update the timestamp.",
            "\t\t\t// 即将执行刷新检查——更新时间戳。",
        ),
        (
            "\t\t\tlogger.debug(\"Refresh check delay elapsed - checking whether refresh is required\");",
            "\t\t\tlogger.debug(\"刷新检查延迟已过——检查是否需要刷新\");",
        ),
        (
            "\t/**\n\t * Determine whether a refresh is required.\n\t * Invoked for each refresh check, after the refresh check delay has elapsed.\n\t * <p>The default implementation always returns {@code true}, triggering\n\t * a refresh every time the delay has elapsed. To be overridden by subclasses\n\t * with an appropriate check of the underlying target resource.\n\t * @return whether a refresh is required\n\t */",
            "\t/**\n\t * 判定是否需要刷新。\n\t * 每次刷新检查（延迟已过）时调用。\n\t * <p>默认实现始终返回 {@code true}，延迟一过即触发刷新。\n\t * 子类应覆盖以对底层目标资源做适当检查。\n\t * @return 是否需要刷新\n\t */",
        ),
        (
            "\t/**\n\t * Obtain a fresh target object.\n\t * <p>Only invoked if a refresh check has found that a refresh is required\n\t * (that is, {@link #requiresRefresh()} has returned {@code true}).\n\t * @return the fresh target object\n\t */",
            "\t/**\n\t * 获取新目标对象。\n\t * <p>仅当刷新检查发现需要刷新时调用\n\t * （即 {@link #requiresRefresh()} 返回 {@code true}）。\n\t * @return 新目标对象\n\t */",
        ),
    ],
    "BeanFactoryRefreshableTargetSource.java": [
        (
            "/**\n * Refreshable TargetSource that fetches fresh target beans from a BeanFactory.\n *\n * <p>Can be subclassed to override {@code requiresRefresh()} to suppress\n * unnecessary refreshes. By default, a refresh will be performed every time\n * the \"refreshCheckDelay\" has elapsed.\n *\n * @author Rob Harrop\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n * @see #requiresRefresh()\n * @see #setRefreshCheckDelay\n */",
            "/**\n * 从 BeanFactory 获取新目标 Bean 的可刷新 TargetSource。\n *\n * <p>可子类化并覆盖 {@code requiresRefresh()} 以抑制不必要的刷新。\n * 默认在 \"refreshCheckDelay\" 间隔过后每次都会刷新。\n *\n * @author Rob Harrop\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Mark Fisher\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n * @see #requiresRefresh()\n * @see #setRefreshCheckDelay\n */",
        ),
        (
            "\t/**\n\t * Create a new BeanFactoryRefreshableTargetSource for the given\n\t * bean factory and bean name.\n\t * <p>Note that the passed-in BeanFactory should have an appropriate\n\t * bean definition set up for the given bean name.\n\t * @param beanFactory the BeanFactory to fetch beans from\n\t * @param beanName the name of the target bean\n\t */",
            "\t/**\n\t * 为给定 BeanFactory 与 Bean 名称创建 BeanFactoryRefreshableTargetSource。\n\t * <p>注意：传入的 BeanFactory 须已为给定 Bean 名称配置相应 Bean 定义。\n\t * @param beanFactory 用于获取 Bean 的 BeanFactory\n\t * @param beanName 目标 Bean 名称\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve a fresh target object.\n\t */",
            "\t/**\n\t * 获取新目标对象。\n\t */",
        ),
        (
            "\t/**\n\t * A template method that subclasses may override to provide a\n\t * fresh target object for the given bean factory and bean name.\n\t * <p>This default implementation fetches a new target bean\n\t * instance from the bean factory.\n\t * @see org.springframework.beans.factory.BeanFactory#getBean\n\t */",
            "\t/**\n\t * 子类可覆盖的模板方法，为给定 BeanFactory 与 Bean 名称提供新目标对象。\n\t * <p>默认实现从 BeanFactory 获取新目标 Bean 实例。\n\t * @see org.springframework.beans.factory.BeanFactory#getBean\n\t */",
        ),
    ],
    "Refreshable.java": [
        (
            "/**\n * Interface to be implemented by dynamic target objects,\n * which support reloading and optionally polling for updates.\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @since 2.0\n */",
            "/**\n * 动态目标对象应实现的接口，\n * 支持重新加载并可选择轮询更新。\n *\n * @author Rod Johnson\n * @author Rob Harrop\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Refresh the underlying target object.\n\t */",
            "\t/**\n\t * 刷新底层目标对象。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of actual refreshes since startup.\n\t */",
            "\t/**\n\t * 返回启动以来的实际刷新次数。\n\t */",
        ),
        (
            "\t/**\n\t * Return the last time an actual refresh happened (as timestamp).\n\t */",
            "\t/**\n\t * 返回上次实际刷新的时间（时间戳）。\n\t */",
        ),
    ],
}
