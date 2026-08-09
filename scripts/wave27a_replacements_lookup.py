"""Chinese JavaDoc replacements for springframework wave27a datasource lookup classes."""

LOOKUP_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractRoutingDataSource.java": [
        (
            "/**\n * Abstract {@link javax.sql.DataSource} implementation that routes {@link #getConnection()}\n * calls to one of various target DataSources based on a lookup key. The latter is usually\n * (but not necessarily) determined through some thread-bound transaction context.\n *\n * @author Juergen Hoeller\n * @since 2.0.1\n * @see #setTargetDataSources\n * @see #setDefaultTargetDataSource\n * @see #determineCurrentLookupKey()\n */",
            "/**\n * 抽象 {@link javax.sql.DataSource} 实现，根据查找键将 {@link #getConnection()}\n * 调用路由到多个目标 DataSource 之一。查找键通常（但不一定）\n * 由线程绑定的事务上下文决定。\n *\n * @author Juergen Hoeller\n * @since 2.0.1\n * @see #setTargetDataSources\n * @see #setDefaultTargetDataSource\n * @see #determineCurrentLookupKey()\n */",
        ),
        (
            "\t/**\n\t * Specify the map of target DataSources, with the lookup key as key.\n\t * <p>The mapped value can either be a corresponding {@link javax.sql.DataSource}\n\t * instance or a data source name String (to be resolved via a\n\t * {@link #setDataSourceLookup DataSourceLookup}).\n\t * <p>The key can be of arbitrary type; this class implements the\n\t * generic lookup process only. The concrete key representation will\n\t * be handled by {@link #resolveSpecifiedLookupKey(Object)} and\n\t * {@link #determineCurrentLookupKey()}.\n\t */",
            "\t/**\n\t * 指定目标 DataSource 映射，键为查找键。\n\t * <p>映射值可以是 {@link javax.sql.DataSource} 实例，\n\t * 或待 {@link #setDataSourceLookup DataSourceLookup} 解析的数据源名称字符串。\n\t * <p>键可为任意类型；本类仅实现通用查找流程。\n\t * 具体键表示由 {@link #resolveSpecifiedLookupKey(Object)} 与\n\t * {@link #determineCurrentLookupKey()} 处理。\n\t */",
        ),
        (
            "\t/**\n\t * Specify the default target DataSource, if any.\n\t * <p>The mapped value can either be a corresponding {@link javax.sql.DataSource}\n\t * instance or a data source name String (to be resolved via a\n\t * {@link #setDataSourceLookup DataSourceLookup}).\n\t * <p>This DataSource will be used as target if none of the keyed\n\t * {@link #setTargetDataSources targetDataSources} match the\n\t * {@link #determineCurrentLookupKey()} current lookup key.\n\t */",
            "\t/**\n\t * 指定默认目标 DataSource（若有）。\n\t * <p>映射值可以是 {@link javax.sql.DataSource} 实例，\n\t * 或待 {@link #setDataSourceLookup DataSourceLookup} 解析的数据源名称字符串。\n\t * <p>若无键控 {@link #setTargetDataSources targetDataSources}\n\t * 匹配 {@link #determineCurrentLookupKey()} 当前查找键，则使用此 DataSource。\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to apply a lenient fallback to the default DataSource\n\t * if no specific DataSource could be found for the current lookup key.\n\t * <p>Default is \"true\", accepting lookup keys without a corresponding entry\n\t * in the target DataSource map - simply falling back to the default DataSource\n\t * in that case.\n\t * <p>Switch this flag to \"false\" if you would prefer the fallback to only apply\n\t * if the lookup key was {@code null}. Lookup keys without a DataSource\n\t * entry will then lead to an IllegalStateException.\n\t * @see #setTargetDataSources\n\t * @see #setDefaultTargetDataSource\n\t * @see #determineCurrentLookupKey()\n\t */",
            "\t/**\n\t * 指定当前查找键找不到对应 DataSource 时，是否宽松回退到默认 DataSource。\n\t * <p>默认为 \"true\"，接受目标 DataSource 映射中无对应条目的查找键，\n\t * 此时直接回退到默认 DataSource。\n\t * <p>若希望仅在查找键为 {@code null} 时回退，可将此标志设为 \"false\"。\n\t * 无 DataSource 条目的查找键将抛出 IllegalStateException。\n\t * @see #setTargetDataSources\n\t * @see #setDefaultTargetDataSource\n\t * @see #determineCurrentLookupKey()\n\t */",
        ),
        (
            "\t/**\n\t * Set the DataSourceLookup implementation to use for resolving data source\n\t * name Strings in the {@link #setTargetDataSources targetDataSources} map.\n\t * <p>Default is a {@link JndiDataSourceLookup}, allowing the JNDI names\n\t * of application server DataSources to be specified directly.\n\t */",
            "\t/**\n\t * 设置用于解析 {@link #setTargetDataSources targetDataSources} 映射中\n\t * 数据源名称字符串的 DataSourceLookup 实现。\n\t * <p>默认为 {@link JndiDataSourceLookup}，可直接指定应用服务器 DataSource 的 JNDI 名称。\n\t */",
        ),
        (
            "\t/**\n\t * Delegates to {@link #initialize()}.\n\t */",
            "\t/**\n\t * 委托 {@link #initialize()}。\n\t */",
        ),
        (
            "\t/**\n\t * Initialize the internal state of this {@code AbstractRoutingDataSource}\n\t * by resolving the configured target DataSources.\n\t * @throws IllegalArgumentException if the target DataSources have not been configured\n\t * @since 6.1\n\t * @see #setTargetDataSources(Map)\n\t * @see #setDefaultTargetDataSource(Object)\n\t * @see #getResolvedDataSources()\n\t * @see #getResolvedDefaultDataSource()\n\t */",
            "\t/**\n\t * 解析已配置的目标 DataSource，初始化本 {@code AbstractRoutingDataSource} 内部状态。\n\t * @throws IllegalArgumentException 未配置目标 DataSource 时\n\t * @since 6.1\n\t * @see #setTargetDataSources(Map)\n\t * @see #setDefaultTargetDataSource(Object)\n\t * @see #getResolvedDataSources()\n\t * @see #getResolvedDefaultDataSource()\n\t */",
        ),
        (
            "\t/**\n\t * Resolve the given lookup key object, as specified in the\n\t * {@link #setTargetDataSources targetDataSources} map, into\n\t * the actual lookup key to be used for matching with the\n\t * {@link #determineCurrentLookupKey() current lookup key}.\n\t * <p>The default implementation simply returns the given key as-is.\n\t * @param lookupKey the lookup key object as specified by the user\n\t * @return the lookup key as needed for matching\n\t */",
            "\t/**\n\t * 将 {@link #setTargetDataSources targetDataSources} 映射中指定的查找键对象\n\t * 解析为用于与 {@link #determineCurrentLookupKey()} 当前查找键匹配的\n\t * 实际查找键。\n\t * <p>默认实现直接返回给定键。\n\t * @param lookupKey 用户指定的查找键对象\n\t * @return 用于匹配的实际查找键\n\t */",
        ),
        (
            "\t/**\n\t * Resolve the specified data source object into a DataSource instance.\n\t * <p>The default implementation handles DataSource instances and data source\n\t * names (to be resolved via a {@link #setDataSourceLookup DataSourceLookup}).\n\t * @param dataSourceObject the data source value object as specified in the\n\t * {@link #setTargetDataSources targetDataSources} map\n\t * @return the resolved DataSource (never {@code null})\n\t * @throws IllegalArgumentException in case of an unsupported value type\n\t */",
            "\t/**\n\t * 将指定数据源对象解析为 DataSource 实例。\n\t * <p>默认实现处理 DataSource 实例与数据源名称\n\t * （通过 {@link #setDataSourceLookup DataSourceLookup} 解析）。\n\t * @param dataSourceObject {@link #setTargetDataSources targetDataSources}\n\t * 映射中指定的数据源值对象\n\t * @return 解析后的 DataSource（永不为 {@code null}）\n\t * @throws IllegalArgumentException 值类型不受支持时\n\t */",
        ),
        (
            "\t/**\n\t * Return the resolved target DataSources that this router manages.\n\t * @return an unmodifiable map of resolved lookup keys and DataSources\n\t * @throws IllegalStateException if the target DataSources are not resolved yet\n\t * @since 5.2.9\n\t * @see #setTargetDataSources\n\t */",
            "\t/**\n\t * 返回本路由器管理的已解析目标 DataSource。\n\t * @return 已解析查找键与 DataSource 的不可修改映射\n\t * @throws IllegalStateException 目标 DataSource 尚未解析时\n\t * @since 5.2.9\n\t * @see #setTargetDataSources\n\t */",
        ),
        (
            "\t/**\n\t * Return the resolved default target DataSource, if any.\n\t * @return the default DataSource, or {@code null} if none or not resolved yet\n\t * @since 5.2.9\n\t * @see #setDefaultTargetDataSource\n\t */",
            "\t/**\n\t * 返回已解析的默认目标 DataSource（若有）。\n\t * @return 默认 DataSource，若无或尚未解析则为 {@code null}\n\t * @since 5.2.9\n\t * @see #setDefaultTargetDataSource\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the current target DataSource. Determines the\n\t * {@link #determineCurrentLookupKey() current lookup key}, performs\n\t * a lookup in the {@link #setTargetDataSources targetDataSources} map,\n\t * falls back to the specified\n\t * {@link #setDefaultTargetDataSource default target DataSource} if necessary.\n\t * @see #determineCurrentLookupKey()\n\t */",
            "\t/**\n\t * 获取当前目标 DataSource。确定 {@link #determineCurrentLookupKey()} 当前查找键，\n\t * 在 {@link #setTargetDataSources targetDataSources} 映射中查找，\n\t * 必要时回退到 {@link #setDefaultTargetDataSource 默认目标 DataSource}。\n\t * @see #determineCurrentLookupKey()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the current lookup key. This will typically be\n\t * implemented to check a thread-bound transaction context.\n\t * <p>Allows for arbitrary keys. The returned key needs\n\t * to match the stored lookup key type, as resolved by the\n\t * {@link #resolveSpecifiedLookupKey} method.\n\t */",
            "\t/**\n\t * 确定当前查找键。通常实现为检查线程绑定的事务上下文。\n\t * <p>允许任意键。返回的键需与 {@link #resolveSpecifiedLookupKey} 方法\n\t * 解析后的存储查找键类型匹配。\n\t */",
        ),
    ],
    "BeanFactoryDataSourceLookup.java": [
        (
            "/**\n * {@link DataSourceLookup} implementation based on a Spring {@link BeanFactory}.\n *\n * <p>Will lookup Spring managed beans identified by bean name,\n * expecting them to be of type {@code javax.sql.DataSource}.\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n */",
            "/**\n * 基于 Spring {@link BeanFactory} 的 {@link DataSourceLookup} 实现。\n *\n * <p>按 Bean 名称查找 Spring 管理的 Bean，\n * 期望其类型为 {@code javax.sql.DataSource}。\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.beans.factory.BeanFactory\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link BeanFactoryDataSourceLookup} class.\n\t * <p>The BeanFactory to access must be set via {@code setBeanFactory}.\n\t * @see #setBeanFactory\n\t */",
            "\t/**\n\t * 创建 {@link BeanFactoryDataSourceLookup} 的新实例。\n\t * <p>须通过 {@code setBeanFactory} 设置要访问的 BeanFactory。\n\t * @see #setBeanFactory\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link BeanFactoryDataSourceLookup} class.\n\t * <p>Use of this constructor is redundant if this object is being created\n\t * by a Spring IoC container, as the supplied {@link BeanFactory} will be\n\t * replaced by the {@link BeanFactory} that creates it (c.f. the\n\t * {@link BeanFactoryAware} contract). So only use this constructor if you\n\t * are using this class outside the context of a Spring IoC container.\n\t * @param beanFactory the bean factory to be used to lookup {@link DataSource DataSources}\n\t */",
            "\t/**\n\t * 创建 {@link BeanFactoryDataSourceLookup} 的新实例。\n\t * <p>若由 Spring IoC 容器创建此对象，使用此构造函数是多余的，\n\t * 所供 {@link BeanFactory} 将被创建它的 {@link BeanFactory} 替换\n\t * （参见 {@link BeanFactoryAware} 契约）。\n\t * 仅在本类用于 Spring IoC 容器外部时使用此构造函数。\n\t * @param beanFactory 用于查找 {@link DataSource DataSources} 的 Bean 工厂\n\t */",
        ),
    ],
    "DataSourceLookup.java": [
        (
            "/**\n * Strategy interface for looking up DataSources by name.\n *\n * <p>Used, for example, to resolve data source names in JPA\n * {@code persistence.xml} files.\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager#setDataSourceLookup\n */",
            "/**\n * 按名称查找 DataSource 的策略接口。\n *\n * <p>例如用于解析 JPA {@code persistence.xml} 文件中的数据源名称。\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager#setDataSourceLookup\n */",
        ),
        (
            "\t/**\n\t * Retrieve the DataSource identified by the given name.\n\t * @param dataSourceName the name of the DataSource\n\t * @return the DataSource (never {@code null})\n\t * @throws DataSourceLookupFailureException if the lookup failed\n\t */",
            "\t/**\n\t * 获取给定名称对应的 DataSource。\n\t * @param dataSourceName DataSource 名称\n\t * @return DataSource（永不为 {@code null}）\n\t * @throws DataSourceLookupFailureException 查找失败时\n\t */",
        ),
    ],
    "DataSourceLookupFailureException.java": [
        (
            "/**\n * Exception to be thrown by a DataSourceLookup implementation,\n * indicating that the specified DataSource could not be obtained.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * DataSourceLookup 实现抛出的异常，表示无法获取指定的 DataSource。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Constructor for DataSourceLookupFailureException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * DataSourceLookupFailureException 构造函数。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for DataSourceLookupFailureException.\n\t * @param msg the detail message\n\t * @param cause the root cause (usually from using an underlying\n\t * lookup API such as JNDI)\n\t */",
            "\t/**\n\t * DataSourceLookupFailureException 构造函数。\n\t * @param msg 详细消息\n\t * @param cause 根本原因（通常来自 JNDI 等底层查找 API）\n\t */",
        ),
    ],
    "JndiDataSourceLookup.java": [
        (
            "/**\n * JNDI-based {@link DataSourceLookup} implementation.\n *\n * <p>For specific JNDI configuration, it is recommended to configure\n * the \"jndiEnvironment\"/\"jndiTemplate\" properties.\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setJndiEnvironment\n * @see #setJndiTemplate\n */",
            "/**\n * 基于 JNDI 的 {@link DataSourceLookup} 实现。\n *\n * <p>如需特定 JNDI 配置，建议配置 \"jndiEnvironment\"/\"jndiTemplate\" 属性。\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setJndiEnvironment\n * @see #setJndiTemplate\n */",
        ),
    ],
    "MapDataSourceLookup.java": [
        (
            "/**\n * Simple {@link DataSourceLookup} implementation that relies on a map for doing lookups.\n *\n * <p>Useful for testing environments or applications that need to match arbitrary\n * {@link String} names to target {@link DataSource} objects.\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @author Rick Evans\n * @since 2.0\n */",
            "/**\n * 依赖映射进行查找的简单 {@link DataSourceLookup} 实现。\n *\n * <p>适用于测试环境，或需将任意 {@link String} 名称\n * 映射到目标 {@link DataSource} 对象的应用。\n *\n * @author Costin Leau\n * @author Juergen Hoeller\n * @author Rick Evans\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link MapDataSourceLookup} class.\n\t */",
            "\t/**\n\t * 创建 {@link MapDataSourceLookup} 的新实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link MapDataSourceLookup} class.\n\t * @param dataSources the {@link Map} of {@link DataSource DataSources}; the keys\n\t * are {@link String Strings}, the values are actual {@link DataSource} instances.\n\t */",
            "\t/**\n\t * 创建 {@link MapDataSourceLookup} 的新实例。\n\t * @param dataSources {@link DataSource DataSources} 的 {@link Map}；\n\t * 键为 {@link String}，值为实际 {@link DataSource} 实例。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link MapDataSourceLookup} class.\n\t * @param dataSourceName the name under which the supplied {@link DataSource} is to be added\n\t * @param dataSource the {@link DataSource} to be added\n\t */",
            "\t/**\n\t * 创建 {@link MapDataSourceLookup} 的新实例。\n\t * @param dataSourceName 所供 {@link DataSource} 的注册名称\n\t * @param dataSource 要添加的 {@link DataSource}\n\t */",
        ),
        (
            "\t/**\n\t * Set the {@link Map} of {@link DataSource DataSources}; the keys\n\t * are {@link String Strings}, the values are actual {@link DataSource} instances.\n\t * <p>If the supplied {@link Map} is {@code null}, then this method\n\t * call effectively has no effect.\n\t * @param dataSources said {@link Map} of {@link DataSource DataSources}\n\t */",
            "\t/**\n\t * 设置 {@link DataSource DataSources} 的 {@link Map}；\n\t * 键为 {@link String}，值为实际 {@link DataSource} 实例。\n\t * <p>若所供 {@link Map} 为 {@code null}，此方法调用无实际效果。\n\t * @param dataSources 上述 {@link DataSource DataSources} 的 {@link Map}\n\t */",
        ),
        (
            "\t/**\n\t * Get the {@link Map} of {@link DataSource DataSources} maintained by this object.\n\t * <p>The returned {@link Map} is {@link Collections#unmodifiableMap(java.util.Map) unmodifiable}.\n\t * @return said {@link Map} of {@link DataSource DataSources} (never {@code null})\n\t */",
            "\t/**\n\t * 获取本对象维护的 {@link DataSource DataSources} 的 {@link Map}。\n\t * <p>返回的 {@link Map} 为 {@link Collections#unmodifiableMap(java.util.Map) 不可修改}。\n\t * @return 上述 {@link DataSource DataSources} 的 {@link Map}（永不为 {@code null}）\n\t */",
        ),
        (
            "\t/**\n\t * Add the supplied {@link DataSource} to the map of {@link DataSource DataSources}\n\t * maintained by this object.\n\t * @param dataSourceName the name under which the supplied {@link DataSource} is to be added\n\t * @param dataSource the {@link DataSource} to be so added\n\t */",
            "\t/**\n\t * 将所供 {@link DataSource} 添加到本对象维护的\n\t * {@link DataSource DataSources} 映射中。\n\t * @param dataSourceName 所供 {@link DataSource} 的注册名称\n\t * @param dataSource 要添加的 {@link DataSource}\n\t */",
        ),
    ],
    "SingleDataSourceLookup.java": [
        (
            "/**\n * An implementation of the DataSourceLookup that simply wraps a\n * single given DataSource, returned for any data source name.\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * DataSourceLookup 实现，简单包装单个给定 DataSource，\n * 对任意数据源名称均返回该 DataSource。\n *\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Create a new instance of the {@link SingleDataSourceLookup} class.\n\t * @param dataSource the single {@link DataSource} to wrap\n\t */",
            "\t/**\n\t * 创建 {@link SingleDataSourceLookup} 的新实例。\n\t * @param dataSource 要包装的单个 {@link DataSource}\n\t */",
        ),
    ],
}
