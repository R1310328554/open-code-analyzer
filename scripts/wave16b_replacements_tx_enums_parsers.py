"""Wave 16b [20:40] Chinese JavaDoc replacements — transaction enums and annotation parsers."""

TX_ENUMS_PARSERS_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Isolation.java": [
        (
            "/**\n * Enumeration that represents transaction isolation levels for use with the\n * {@link Transactional @Transactional} annotation, corresponding to the\n * {@link TransactionDefinition} interface.\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 1.2\n */",
            "/**\n * 表示与 {@link Transactional @Transactional} 注解配合使用的事务隔离级别的枚举，\n * 对应 {@link TransactionDefinition} 接口。\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 1.2\n */",
        ),
        (
            "\t/**\n\t * Use the default isolation level of the underlying data store.\n\t * <p>All other levels correspond to the JDBC isolation levels.\n\t * @see java.sql.Connection\n\t */",
            "\t/**\n\t * 使用底层数据存储的默认隔离级别。\n\t * <p>其他所有级别对应 JDBC 隔离级别。\n\t * @see java.sql.Connection\n\t */",
        ),
        (
            "\t/**\n\t * A constant indicating that dirty reads, non-repeatable reads, and phantom reads\n\t * can occur.\n\t * <p>This level allows a row changed by one transaction to be read by\n\t * another transaction before any changes in that row have been committed\n\t * (a \"dirty read\"). If any of the changes are rolled back, the second\n\t * transaction will have retrieved an invalid row.\n\t * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED\n\t */",
            "\t/**\n\t * 表示可能发生脏读、不可重复读和幻读的常量。\n\t * <p>此级别允许一个事务修改的行在变更提交前\n\t * 被另一事务读取（\"脏读\"）。若任一变更被回滚，\n\t * 第二事务将读到无效行。\n\t * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED\n\t */",
        ),
        (
            "\t/**\n\t * A constant indicating that dirty reads are prevented; non-repeatable reads\n\t * and phantom reads can occur.\n\t * <p>This level only prohibits a transaction from reading a row with uncommitted\n\t * changes in it.\n\t * @see java.sql.Connection#TRANSACTION_READ_COMMITTED\n\t */",
            "\t/**\n\t * 表示防止脏读；不可重复读和幻读仍可能发生。\n\t * <p>此级别仅禁止事务读取含未提交变更的行。\n\t * @see java.sql.Connection#TRANSACTION_READ_COMMITTED\n\t */",
        ),
        (
            "\t/**\n\t * A constant indicating that dirty reads and non-repeatable reads are\n\t * prevented; phantom reads can occur.\n\t * <p>This level prohibits a transaction from reading a row with uncommitted changes\n\t * in it, and it also prohibits the situation where one transaction reads a row,\n\t * a second transaction alters the row, and the first transaction re-reads the row,\n\t * getting different values the second time (a \"non-repeatable read\").\n\t * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ\n\t */",
            "\t/**\n\t * 表示防止脏读和不可重复读；幻读仍可能发生。\n\t * <p>此级别禁止读取含未提交变更的行，\n\t * 也禁止一事务读行、二事务改行、一事务再读却得到不同值\n\t *（\"不可重复读\"）的情况。\n\t * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ\n\t */",
        ),
        (
            "\t/**\n\t * A constant indicating that dirty reads, non-repeatable reads, and phantom\n\t * reads are prevented.\n\t * <p>This level includes the prohibitions in {@link #REPEATABLE_READ}\n\t * and further prohibits the situation where one transaction reads all rows that\n\t * satisfy a {@code WHERE} condition, a second transaction inserts a row\n\t * that satisfies that {@code WHERE} condition, and the first transaction\n\t * re-reads for the same condition, retrieving the additional \"phantom\" row\n\t * in the second read.\n\t * @see java.sql.Connection#TRANSACTION_SERIALIZABLE\n\t */",
            "\t/**\n\t * 表示防止脏读、不可重复读和幻读的常量。\n\t * <p>此级别包含 {@link #REPEATABLE_READ} 的所有限制，\n\t * 并进一步禁止：一事务读取满足 {@code WHERE} 条件的所有行，\n\t * 二事务插入满足该 {@code WHERE} 条件的行，\n\t * 一事务再次按相同条件读取时多出一行（\"幻读\"）。\n\t * @see java.sql.Connection#TRANSACTION_SERIALIZABLE\n\t */",
        ),
    ],
    "Propagation.java": [
        (
            "/**\n * Enumeration that represents transaction propagation behaviors for use\n * with the {@link Transactional} annotation, corresponding to the\n * {@link TransactionDefinition} interface.\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 1.2\n */",
            "/**\n * 表示与 {@link Transactional} 注解配合使用的事务传播行为的枚举，\n * 对应 {@link TransactionDefinition} 接口。\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 1.2\n */",
        ),
        (
            "\t/**\n\t * Support a current transaction, create a new one if none exists.\n\t * Analogous to EJB transaction attribute of the same name.\n\t * <p>This is the default setting of a transaction annotation.\n\t */",
            "\t/**\n\t * 支持当前事务，不存在则创建新事务。\n\t * 与同名 EJB 事务属性类似。\n\t * <p>这是事务注解的默认设置。\n\t */",
        ),
        (
            "\t/**\n\t * Support a current transaction, execute non-transactionally if none exists.\n\t * Analogous to EJB transaction attribute of the same name.\n\t * <p>Note: For transaction managers with transaction synchronization,\n\t * {@code SUPPORTS} is slightly different from no transaction at all,\n\t * as it defines a transaction scope that synchronization will apply for.\n\t * As a consequence, the same resources (JDBC Connection, Hibernate Session, etc)\n\t * will be shared for the entire specified scope. Note that this depends on\n\t * the actual synchronization configuration of the transaction manager.\n\t * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization\n\t */",
            "\t/**\n\t * 支持当前事务，不存在则以非事务方式执行。\n\t * 与同名 EJB 事务属性类似。\n\t * <p>注意：对支持事务同步的事务管理器，\n\t * {@code SUPPORTS} 与完全无事务略有不同，\n\t * 它定义了同步将适用的事务范围。\n\t * 因此相同资源（JDBC Connection、Hibernate Session 等）\n\t * 将在整个指定范围内共享。这取决于事务管理器的实际同步配置。\n\t * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization\n\t */",
        ),
        (
            "\t/**\n\t * Support a current transaction, throw an exception if none exists.\n\t * Analogous to EJB transaction attribute of the same name.\n\t */",
            "\t/**\n\t * 支持当前事务，不存在则抛出异常。\n\t * 与同名 EJB 事务属性类似。\n\t */",
        ),
        (
            "\t/**\n\t * Create a new transaction, and suspend the current transaction if one exists.\n\t * Analogous to the EJB transaction attribute of the same name.\n\t * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box\n\t * on all transaction managers. This in particular applies to\n\t * {@link org.springframework.transaction.jta.JtaTransactionManager},\n\t * which requires the {@code jakarta.transaction.TransactionManager} to be\n\t * made available to it (which is server-specific in standard Jakarta EE).\n\t * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager\n\t */",
            "\t/**\n\t * 创建新事务，若存在当前事务则挂起。\n\t * 与同名 EJB 事务属性类似。\n\t * <p><b>注意：</b>并非所有事务管理器都能开箱即用地挂起事务。\n\t * 尤其 {@link org.springframework.transaction.jta.JtaTransactionManager}\n\t * 需要向其提供 {@code jakarta.transaction.TransactionManager}\n\t *（在标准 Jakarta EE 中因服务器而异）。\n\t * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager\n\t */",
        ),
        (
            "\t/**\n\t * Execute non-transactionally, suspend the current transaction if one exists.\n\t * Analogous to EJB transaction attribute of the same name.\n\t * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box\n\t * on all transaction managers. This in particular applies to\n\t * {@link org.springframework.transaction.jta.JtaTransactionManager},\n\t * which requires the {@code jakarta.transaction.TransactionManager} to be\n\t * made available to it (which is server-specific in standard Jakarta EE).\n\t * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager\n\t */",
            "\t/**\n\t * 以非事务方式执行，若存在当前事务则挂起。\n\t * 与同名 EJB 事务属性类似。\n\t * <p><b>注意：</b>并非所有事务管理器都能开箱即用地挂起事务。\n\t * 尤其 {@link org.springframework.transaction.jta.JtaTransactionManager}\n\t * 需要向其提供 {@code jakarta.transaction.TransactionManager}\n\t *（在标准 Jakarta EE 中因服务器而异）。\n\t * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager\n\t */",
        ),
        (
            "\t/**\n\t * Execute non-transactionally, throw an exception if a transaction exists.\n\t * Analogous to EJB transaction attribute of the same name.\n\t */",
            "\t/**\n\t * 以非事务方式执行，若存在事务则抛出异常。\n\t * 与同名 EJB 事务属性类似。\n\t */",
        ),
        (
            "\t/**\n\t * Execute within a nested transaction if a current transaction exists,\n\t * behave like {@code REQUIRED} otherwise. There is no analogous feature in EJB.\n\t * <p>Note: Actual creation of a nested transaction will only work on specific\n\t * transaction managers. Out of the box, this only applies to the JDBC\n\t * DataSourceTransactionManager. Some JTA providers might support nested\n\t * transactions as well.\n\t * @see org.springframework.jdbc.datasource.DataSourceTransactionManager\n\t */",
            "\t/**\n\t * 若存在当前事务则在嵌套事务中执行，否则行为同 {@code REQUIRED}。\n\t * EJB 中无对应特性。\n\t * <p>注意：嵌套事务的实际创建仅适用于特定事务管理器。\n\t * 开箱即用仅适用于 JDBC DataSourceTransactionManager。\n\t * 部分 JTA 提供者也可能支持嵌套事务。\n\t * @see org.springframework.jdbc.datasource.DataSourceTransactionManager\n\t */",
        ),
    ],
    "RollbackOn.java": [
        (
            "/**\n * An enum for global rollback-on behavior.\n *\n * <p>Note that the default behavior matches the traditional behavior in\n * EJB CMT and JTA, with the latter having rollback rules similar to Spring.\n * A global switch to trigger a rollback on any exception affects Spring's\n * {@link Transactional} as well as {@link jakarta.transaction.Transactional}\n * but leaves the non-rule-based {@link jakarta.ejb.TransactionAttribute} as-is.\n *\n * @author Juergen Hoeller\n * @since 6.2\n * @see EnableTransactionManagement#rollbackOn()\n * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute\n */",
            "/**\n * 全局回滚行为的枚举。\n *\n * <p>默认行为与 EJB CMT 和 JTA 的传统行为一致，\n * 后者回滚规则与 Spring 类似。\n * 全局切换为任意异常触发回滚会影响 Spring 的\n * {@link Transactional} 以及 {@link jakarta.transaction.Transactional}，\n * 但不改变基于非规则的 {@link jakarta.ejb.TransactionAttribute}。\n *\n * @author Juergen Hoeller\n * @since 6.2\n * @see EnableTransactionManagement#rollbackOn()\n * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute\n */",
        ),
        (
            "\t/**\n\t * The default rollback-on behavior: rollback on\n\t * {@link RuntimeException RuntimeExceptions} as well as {@link Error Errors}.\n\t * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#ROLLBACK_ON_RUNTIME_EXCEPTIONS\n\t */",
            "\t/**\n\t * 默认回滚行为：对 {@link RuntimeException RuntimeExceptions}\n\t * 以及 {@link Error Errors} 回滚。\n\t * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#ROLLBACK_ON_RUNTIME_EXCEPTIONS\n\t */",
        ),
        (
            "\t/**\n\t * The alternative mode: rollback on all exceptions, including any checked\n\t * {@link Exception}.\n\t * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#ROLLBACK_ON_ALL_EXCEPTIONS\n\t */",
            "\t/**\n\t * 替代模式：对所有异常（包括任何受检 {@link Exception}）回滚。\n\t * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#ROLLBACK_ON_ALL_EXCEPTIONS\n\t */",
        ),
    ],
    "Ejb3TransactionAnnotationParser.java": [
        (
            "/**\n * Strategy implementation for parsing EJB3's {@link jakarta.ejb.TransactionAttribute} annotation.\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see SpringTransactionAnnotationParser\n * @see JtaTransactionAnnotationParser\n */",
            "/**\n * 解析 EJB3 {@link jakarta.ejb.TransactionAttribute} 注解的策略实现，\n * 支持 EJB3 基于注解异常的回滚规则。\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see SpringTransactionAnnotationParser\n * @see JtaTransactionAnnotationParser\n */",
        ),
        (
            "\t/**\n\t * EJB3-specific TransactionAttribute, implementing EJB3's rollback rules\n\t * which are based on annotated exceptions.\n\t */",
            "\t/**\n\t * EJB3 专用 TransactionAttribute，实现基于注解异常的 EJB3 回滚规则。\n\t */",
        ),
    ],
    "JtaTransactionAnnotationParser.java": [
        (
            "/**\n * Strategy implementation for parsing JTA 1.2's {@link jakarta.transaction.Transactional} annotation.\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see SpringTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n */",
            "/**\n * 解析 JTA 1.2 {@link jakarta.transaction.Transactional} 注解的策略实现，\n * 将 JTA 事务元数据转换为 Spring 事务属性。\n *\n * @author Juergen Hoeller\n * @since 4.0\n * @see SpringTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n */",
        ),
    ],
    "SpringTransactionAnnotationParser.java": [
        (
            "/**\n * Strategy implementation for parsing Spring's {@link Transactional} annotation.\n *\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 2.5\n * @see JtaTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n */",
            "/**\n * 解析 Spring {@link Transactional} 注解的策略实现，\n * 将注解属性映射为 Spring 规则型事务属性。\n *\n * @author Juergen Hoeller\n * @author Mark Paluch\n * @since 2.5\n * @see JtaTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n */",
        ),
    ],
    "TransactionAnnotationParser.java": [
        (
            "/**\n * Strategy interface for parsing known transaction annotation types.\n * {@link AnnotationTransactionAttributeSource} delegates to such\n * parsers for supporting specific annotation types such as Spring's own\n * {@link Transactional}, JTA 1.2's {@link jakarta.transaction.Transactional}\n * or EJB3's {@link jakarta.ejb.TransactionAttribute}.\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see AnnotationTransactionAttributeSource\n * @see SpringTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n * @see JtaTransactionAnnotationParser\n */",
            "/**\n * 解析已知事务注解类型的策略接口。\n * {@link AnnotationTransactionAttributeSource} 委托此类解析器\n * 以支持特定注解类型，如 Spring 自身的 {@link Transactional}、\n * JTA 1.2 的 {@link jakarta.transaction.Transactional}\n * 或 EJB3 的 {@link jakarta.ejb.TransactionAttribute}。\n *\n * @author Juergen Hoeller\n * @since 2.5\n * @see AnnotationTransactionAttributeSource\n * @see SpringTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n * @see JtaTransactionAnnotationParser\n */",
        ),
        (
            "\t/**\n\t * Determine whether the given class is a candidate for transaction attributes\n\t * in the annotation format of this {@code TransactionAnnotationParser}.\n\t * <p>If this method returns {@code false}, the methods on the given class\n\t * will not get traversed for {@code #parseTransactionAnnotation} introspection.\n\t * Returning {@code false} is therefore an optimization for non-affected\n\t * classes, whereas {@code true} simply means that the class needs to get\n\t * fully introspected for each method on the given class individually.\n\t * @param targetClass the class to introspect\n\t * @return {@code false} if the class is known to have no transaction\n\t * annotations at class or method level; {@code true} otherwise. The default\n\t * implementation returns {@code true}, leading to regular introspection.\n\t * @since 5.2\n\t */",
            "\t/**\n\t * 判断给定类是否为此 {@code TransactionAnnotationParser}\n\t * 注解格式下事务属性的候选类。\n\t * <p>若返回 {@code false}，则不会遍历该类方法进行\n\t * {@code #parseTransactionAnnotation} 内省。\n\t * 因此 {@code false} 是对不受影响类的优化，\n\t * 而 {@code true} 表示需对该类每个方法逐一完整内省。\n\t * @param targetClass 待内省的类\n\t * @return 若类在类或方法级别已知无事务注解则 {@code false}，\n\t * 否则 {@code true}。默认实现返回 {@code true}，进行常规内省。\n\t * @since 5.2\n\t */",
        ),
        (
            "\t/**\n\t * Parse the transaction attribute for the given method or class,\n\t * based on an annotation type understood by this parser.\n\t * <p>This essentially parses a known transaction annotation into Spring's metadata\n\t * attribute class. Returns {@code null} if the method/class is not transactional.\n\t * <p>The returned attribute will typically (but not necessarily) be of type\n\t * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute}.\n\t * @param element the annotated method or class\n\t * @return the configured transaction attribute, or {@code null} if none found\n\t * @see AnnotationTransactionAttributeSource#determineTransactionAttribute\n\t */",
            "\t/**\n\t * 基于本解析器理解的注解类型，解析给定方法或类的事务属性。\n\t * <p>本质上是将已知事务注解解析为 Spring 元数据属性类。\n\t * 若方法/类非事务性则返回 {@code null}。\n\t * <p>返回的属性通常（但不一定）为\n\t * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute} 类型。\n\t * @param element 带注解的方法或类\n\t * @return 配置的事务属性，未找到则 {@code null}\n\t * @see AnnotationTransactionAttributeSource#determineTransactionAttribute\n\t */",
        ),
    ],
    "AnnotationTransactionAttributeSource.java": [
        (
            "/**\n * Implementation of the\n * {@link org.springframework.transaction.interceptor.TransactionAttributeSource}\n * interface for working with transaction metadata from annotations.\n *\n * <p>This class reads Spring's {@link Transactional @Transactional} annotation and\n * exposes corresponding transaction attributes to Spring's transaction infrastructure.\n * Also supports JTA's {@link jakarta.transaction.Transactional} and EJB's\n * {@link jakarta.ejb.TransactionAttribute} annotation (if present).\n *\n * <p>This class may also serve as base class for a custom TransactionAttributeSource,\n * or get customized through {@link TransactionAnnotationParser} strategies.\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 1.2\n * @see Transactional\n * @see TransactionAnnotationParser\n * @see SpringTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n * @see org.springframework.transaction.interceptor.TransactionInterceptor#setTransactionAttributeSource\n * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean#setTransactionAttributeSource\n */",
            "/**\n * {@link org.springframework.transaction.interceptor.TransactionAttributeSource}\n * 接口的实现，用于处理注解来源的事务元数据。\n *\n * <p>本类读取 Spring {@link Transactional @Transactional} 注解，\n * 并向 Spring 事务基础设施暴露对应事务属性。\n * 也支持 JTA {@link jakarta.transaction.Transactional}\n * 和 EJB {@link jakarta.ejb.TransactionAttribute} 注解（若存在）。\n *\n * <p>本类也可作为自定义 TransactionAttributeSource 的基类，\n * 或通过 {@link TransactionAnnotationParser} 策略定制。\n *\n * @author Colin Sampaleanu\n * @author Juergen Hoeller\n * @since 1.2\n * @see Transactional\n * @see TransactionAnnotationParser\n * @see SpringTransactionAnnotationParser\n * @see Ejb3TransactionAnnotationParser\n * @see org.springframework.transaction.interceptor.TransactionInterceptor#setTransactionAttributeSource\n * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean#setTransactionAttributeSource\n */",
        ),
        (
            "\t/**\n\t * Create a default AnnotationTransactionAttributeSource, supporting\n\t * public methods that carry the {@code Transactional} annotation\n\t * or the EJB3 {@link jakarta.ejb.TransactionAttribute} annotation.\n\t */",
            "\t/**\n\t * 创建默认 AnnotationTransactionAttributeSource，\n\t * 支持带 {@code Transactional} 注解或\n\t * EJB3 {@link jakarta.ejb.TransactionAttribute} 注解的 public 方法。\n\t */",
        ),
        (
            "\t/**\n\t * Create a custom AnnotationTransactionAttributeSource, supporting\n\t * public methods that carry the {@code Transactional} annotation\n\t * or the EJB3 {@link jakarta.ejb.TransactionAttribute} annotation.\n\t * @param publicMethodsOnly whether to support public methods that carry\n\t * the {@code Transactional} annotation only (typically for use\n\t * with proxy-based AOP), or protected/private methods as well\n\t * (typically used with AspectJ class weaving)\n\t * @see #setPublicMethodsOnly\n\t */",
            "\t/**\n\t * 创建自定义 AnnotationTransactionAttributeSource，\n\t * 支持带 {@code Transactional} 或\n\t * EJB3 {@link jakarta.ejb.TransactionAttribute} 注解的方法。\n\t * @param publicMethodsOnly 是否仅支持带 {@code Transactional} 的 public 方法\n\t *（通常用于基于代理的 AOP），或也支持 protected/private 方法\n\t *（通常用于 AspectJ 类织入）\n\t * @see #setPublicMethodsOnly\n\t */",
        ),
        (
            "\t/**\n\t * Create a custom AnnotationTransactionAttributeSource.\n\t * @param annotationParser the TransactionAnnotationParser to use\n\t */",
            "\t/**\n\t * 创建自定义 AnnotationTransactionAttributeSource。\n\t * @param annotationParser 使用的 TransactionAnnotationParser\n\t */",
        ),
        (
            "\t/**\n\t * Create a custom AnnotationTransactionAttributeSource.\n\t * @param annotationParsers the TransactionAnnotationParsers to use\n\t */",
            "\t/**\n\t * 创建自定义 AnnotationTransactionAttributeSource。\n\t * @param annotationParsers 使用的 TransactionAnnotationParser 集合\n\t */",
        ),
        (
            "\t/**\n\t * Set whether transactional methods are expected to be public.\n\t * <p>The default is {@code true}.\n\t * @since 6.2\n\t * @see #AnnotationTransactionAttributeSource(boolean)\n\t */",
            "\t/**\n\t * 设置事务方法是否必须为 public。\n\t * <p>默认为 {@code true}。\n\t * @since 6.2\n\t * @see #AnnotationTransactionAttributeSource(boolean)\n\t */",
        ),
        (
            "\t/**\n\t * Add a default rollback rule, to be applied to all rule-based\n\t * transaction attributes returned by this source.\n\t * <p>By default, a rollback will be triggered on unchecked exceptions\n\t * but not on checked exceptions. A default rule may override this\n\t * while still respecting any custom rules in the transaction attribute.\n\t * @param rollbackRule a rollback rule overriding the default behavior,\n\t * for example, {@link RollbackRuleAttribute#ROLLBACK_ON_ALL_EXCEPTIONS}\n\t * @since 6.2\n\t * @see RuleBasedTransactionAttribute#getRollbackRules()\n\t * @see EnableTransactionManagement#rollbackOn()\n\t * @see Transactional#rollbackFor()\n\t * @see Transactional#noRollbackFor()\n\t */",
            "\t/**\n\t * 添加默认回滚规则，应用于本源返回的所有基于规则的事务属性。\n\t * <p>默认情况下，非受检异常触发回滚，受检异常不触发。\n\t * 默认规则可覆盖此行为，同时仍尊重事务属性中的自定义规则。\n\t * @param rollbackRule 覆盖默认行为的回滚规则，\n\t * 例如 {@link RollbackRuleAttribute#ROLLBACK_ON_ALL_EXCEPTIONS}\n\t * @since 6.2\n\t * @see RuleBasedTransactionAttribute#getRollbackRules()\n\t * @see EnableTransactionManagement#rollbackOn()\n\t * @see Transactional#rollbackFor()\n\t * @see Transactional#noRollbackFor()\n\t */",
        ),
        (
            "\t/**\n\t * Determine the transaction attribute for the given method or class.\n\t * <p>This implementation delegates to configured\n\t * {@link TransactionAnnotationParser TransactionAnnotationParsers}\n\t * for parsing known annotations into Spring's metadata attribute class.\n\t * Returns {@code null} if it's not transactional.\n\t * <p>Can be overridden to support custom annotations that carry transaction metadata.\n\t * @param element the annotated method or class\n\t * @return the configured transaction attribute, or {@code null} if none was found\n\t */",
            "\t/**\n\t * 确定给定方法或类的事务属性。\n\t * <p>本实现委托已配置的\n\t * {@link TransactionAnnotationParser TransactionAnnotationParsers}\n\t * 将已知注解解析为 Spring 元数据属性类。\n\t * 若非事务性则返回 {@code null}。\n\t * <p>可覆盖以支持携带事务元数据的自定义注解。\n\t * @param element 带注解的方法或类\n\t * @return 配置的事务属性，未找到则 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * By default, only public methods can be made transactional.\n\t * @see #setPublicMethodsOnly\n\t */",
            "\t/**\n\t * 默认情况下，仅 public 方法可声明为事务性。\n\t * @see #setPublicMethodsOnly\n\t */",
        ),
    ],
}
