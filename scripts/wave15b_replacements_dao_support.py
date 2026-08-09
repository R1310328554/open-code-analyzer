"""Wave 15b [20:40] Chinese JavaDoc replacements — dao annotation/support classes."""

DAO_SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "PersistenceExceptionTranslationAdvisor.java": [
        (
            "/**\n * Spring AOP exception translation aspect for use at Repository or DAO layer level.\n * Translates native persistence exceptions into Spring's DataAccessException hierarchy,\n * based on a given PersistenceExceptionTranslator.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.dao.DataAccessException\n * @see org.springframework.dao.support.PersistenceExceptionTranslator\n */",
            "/**\n * 用于 Repository 或 DAO 层的 Spring AOP 异常转换切面。\n * 基于给定 PersistenceExceptionTranslator，\n * 将原生持久化异常转换为 Spring 的 DataAccessException 层次结构。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see org.springframework.dao.DataAccessException\n * @see org.springframework.dao.support.PersistenceExceptionTranslator\n */",
        ),
        (
            "\t/**\n\t * Create a new PersistenceExceptionTranslationAdvisor.\n\t * @param persistenceExceptionTranslator the PersistenceExceptionTranslator to use\n\t * @param repositoryAnnotationType the annotation type to check for\n\t */",
            "\t/**\n\t * 创建新的 PersistenceExceptionTranslationAdvisor。\n\t * @param persistenceExceptionTranslator 要使用的 PersistenceExceptionTranslator\n\t * @param repositoryAnnotationType 要检查的注解类型\n\t */",
        ),
        (
            "\t/**\n\t * Create a new PersistenceExceptionTranslationAdvisor.\n\t * @param beanFactory the ListableBeanFactory to obtaining all\n\t * PersistenceExceptionTranslators from\n\t * @param repositoryAnnotationType the annotation type to check for\n\t */",
            "\t/**\n\t * 创建新的 PersistenceExceptionTranslationAdvisor。\n\t * @param beanFactory 用于获取所有 PersistenceExceptionTranslator 的 ListableBeanFactory\n\t * @param repositoryAnnotationType 要检查的注解类型\n\t */",
        ),
    ],
    "PersistenceExceptionTranslationPostProcessor.java": [
        (
            "/**\n * Bean post-processor that automatically applies persistence exception translation to any\n * bean marked with Spring's @{@link org.springframework.stereotype.Repository Repository}\n * annotation, adding a corresponding {@link PersistenceExceptionTranslationAdvisor} to\n * the exposed proxy (either an existing AOP proxy or a newly generated proxy that\n * implements all of the target's interfaces).\n *\n * <p>Translates native resource exceptions to Spring's\n * {@link org.springframework.dao.DataAccessException DataAccessException} hierarchy.\n * Autodetects beans that implement the\n * {@link org.springframework.dao.support.PersistenceExceptionTranslator\n * PersistenceExceptionTranslator} interface, which are subsequently asked to translate\n * candidate exceptions.\n *\n * <p>All of Spring's applicable resource factories (for example,\n * {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean})\n * implement the {@code PersistenceExceptionTranslator} interface out of the box.\n * As a consequence, all that is usually needed to enable automatic exception\n * translation is marking all affected beans (such as Repositories or DAOs)\n * with the {@code @Repository} annotation, along with defining this post-processor\n * as a bean in the application context.\n *\n * <p>{@code PersistenceExceptionTranslator} beans are sorted according to Spring's\n * dependency ordering rules: see {@link org.springframework.core.Ordered} and\n * {@link org.springframework.core.annotation.Order}. Note that such beans will\n * get retrieved from any scope, not just singleton scope.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see PersistenceExceptionTranslationAdvisor\n * @see org.springframework.stereotype.Repository\n * @see org.springframework.dao.DataAccessException\n * @see org.springframework.dao.support.PersistenceExceptionTranslator\n */",
            "/**\n * Bean 后处理器，自动对标记 Spring @{@link org.springframework.stereotype.Repository Repository}\n * 注解的 Bean 应用持久化异常转换，\n * 向暴露的代理（现有 AOP 代理或新生成、实现目标全部接口的代理）\n * 添加对应的 {@link PersistenceExceptionTranslationAdvisor}。\n *\n * <p>将原生资源异常转换为 Spring 的\n * {@link org.springframework.dao.DataAccessException DataAccessException} 层次结构。\n * 自动检测实现\n * {@link org.springframework.dao.support.PersistenceExceptionTranslator\n * PersistenceExceptionTranslator} 接口的 Bean，并委托其转换候选异常。\n *\n * <p>Spring 所有适用的资源工厂（例如\n * {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}）\n * 均开箱实现 {@code PersistenceExceptionTranslator} 接口。\n * 因此，启用自动异常转换通常只需为所有受影响的 Bean（如 Repository 或 DAO）\n * 标记 {@code @Repository} 注解，并在应用上下文中定义本后处理器。\n *\n * <p>{@code PersistenceExceptionTranslator} Bean 按 Spring 依赖排序规则排序：\n * 参见 {@link org.springframework.core.Ordered} 和\n * {@link org.springframework.core.annotation.Order}。注意，此类 Bean 可从任意作用域检索，\n * 不限于单例作用域。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see PersistenceExceptionTranslationAdvisor\n * @see org.springframework.stereotype.Repository\n * @see org.springframework.dao.DataAccessException\n * @see org.springframework.dao.support.PersistenceExceptionTranslator\n */",
        ),
        (
            "\t/**\n\t * Set the 'repository' annotation type.\n\t * The default repository annotation type is the {@link Repository} annotation.\n\t * <p>This setter property exists so that developers can provide their own\n\t * (non-Spring-specific) annotation type to indicate that a class has a\n\t * repository role.\n\t * @param repositoryAnnotationType the desired annotation type\n\t */",
            "\t/**\n\t * 设置“repository”注解类型。\n\t * 默认 repository 注解类型为 {@link Repository} 注解。\n\t * <p>提供此 setter 以便开发者使用自定义（非 Spring 专用）注解类型\n\t * 标识类具有 repository 角色。\n\t * @param repositoryAnnotationType 所需的注解类型\n\t */",
        ),
    ],
    "ChainedPersistenceExceptionTranslator.java": [
        (
            "/**\n * Implementation of {@link PersistenceExceptionTranslator} that supports chaining,\n * allowing the addition of PersistenceExceptionTranslator instances in order.\n * Returns {@code non-null} on the first (if any) match.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 支持链式组合的 {@link PersistenceExceptionTranslator} 实现，\n * 允许按顺序添加 PersistenceExceptionTranslator 实例。\n * 在首次（若有）匹配时返回 {@code non-null}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/** List of PersistenceExceptionTranslators. */",
            "\t/** PersistenceExceptionTranslator 列表。 */",
        ),
        (
            "\t/**\n\t * Add a PersistenceExceptionTranslator to the chained delegate list.\n\t */",
            "\t/**\n\t * 向链式委托列表添加 PersistenceExceptionTranslator。\n\t */",
        ),
        (
            "\t/**\n\t * Return all registered PersistenceExceptionTranslator delegates (as array).\n\t */",
            "\t/**\n\t * 返回所有已注册的 PersistenceExceptionTranslator 委托（数组形式）。\n\t */",
        ),
    ],
    "DaoSupport.java": [
        (
            "/**\n * Generic base class for DAOs, defining template methods for DAO initialization.\n *\n * <p>Extended by Spring's specific DAO support classes, such as:\n * JdbcDaoSupport, JdoDaoSupport, etc.\n *\n * @author Juergen Hoeller\n * @since 1.2.2\n * @see org.springframework.jdbc.core.support.JdbcDaoSupport\n * @deprecated as of 7.0, in favor of direct injection of client dependencies\n */",
            "/**\n * DAO 的通用基类，定义 DAO 初始化的模板方法。\n *\n * <p>由 Spring 特定 DAO 支持类扩展，例如 JdbcDaoSupport、JdoDaoSupport 等。\n *\n * @author Juergen Hoeller\n * @since 1.2.2\n * @see org.springframework.jdbc.core.support.JdbcDaoSupport\n * @deprecated as of 7.0, in favor of direct injection of client dependencies\n */",
        ),
        (
            "\t/** Logger available to subclasses. */",
            "\t/** 子类可用的 Logger。 */",
        ),
        (
            "\t/**\n\t * Abstract subclasses must override this to check their configuration.\n\t * <p>Implementors should be marked as {@code final} if concrete subclasses\n\t * are not supposed to override this template method themselves.\n\t * @throws IllegalArgumentException in case of illegal configuration\n\t */",
            "\t/**\n\t * 抽象子类必须重写此方法以检查其配置。\n\t * <p>若具体子类不应自行重写此模板方法，实现者应标记为 {@code final}。\n\t * @throws IllegalArgumentException 配置非法时\n\t */",
        ),
        (
            "\t/**\n\t * Concrete subclasses can override this for custom initialization behavior.\n\t * Gets called after population of this instance's bean properties.\n\t * @throws Exception if DAO initialization fails\n\t * (will be rethrown as a BeanInitializationException)\n\t * @see org.springframework.beans.factory.BeanInitializationException\n\t */",
            "\t/**\n\t * 具体子类可重写此方法以实现自定义初始化行为。\n\t * 在本实例 Bean 属性填充后调用。\n\t * @throws Exception DAO 初始化失败时\n\t *（将重新抛出为 BeanInitializationException）\n\t * @see org.springframework.beans.factory.BeanInitializationException\n\t */",
        ),
    ],
    "PersistenceExceptionTranslator.java": [
        (
            "/**\n * Interface implemented by Spring integrations with data access technologies\n * that throw runtime exceptions, such as JPA and Hibernate.\n *\n * <p>This allows consistent usage of combined exception translation functionality,\n * without forcing a single translator to understand every single possible type\n * of exception.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
            "/**\n * 由与抛出运行时异常的数据访问技术（如 JPA 和 Hibernate）\n * 集成的 Spring 组件实现的接口。\n *\n * <p>允许一致地使用组合异常转换功能，\n * 而无需单个转换器理解所有可能的异常类型。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n */",
        ),
        (
            "\t/**\n\t * Translate the given runtime exception thrown by a persistence framework to a\n\t * corresponding exception from Spring's generic\n\t * {@link org.springframework.dao.DataAccessException} hierarchy, if possible.\n\t * <p>Do not translate exceptions that are not understood by this translator:\n\t * for example, if coming from another persistence framework, or resulting\n\t * from user code or otherwise unrelated to persistence.\n\t * <p>Of particular importance is the correct translation to\n\t * DataIntegrityViolationException, for example on constraint violation.\n\t * Implementations may use Spring JDBC's sophisticated exception translation\n\t * to provide further information in the event of SQLException as a root cause.\n\t * @param ex a RuntimeException to translate\n\t * @return the corresponding DataAccessException (or {@code null} if the\n\t * exception could not be translated, as in this case it may result from\n\t * user code rather than from an actual persistence problem)\n\t * @see org.springframework.dao.DataIntegrityViolationException\n\t * @see org.springframework.jdbc.support.SQLExceptionTranslator\n\t */",
            "\t/**\n\t * 若可能，将持久化框架抛出的给定运行时异常转换为\n\t * Spring 通用 {@link org.springframework.dao.DataAccessException} 层次结构中\n\t * 对应的异常。\n\t * <p>不要转换本转换器无法理解的异常：\n\t * 例如来自其他持久化框架、用户代码或与持久化无关的异常。\n\t * <p>正确转换为 DataIntegrityViolationException（例如约束违例）尤为重要。\n\t * 实现可在根因为 SQLException 时利用 Spring JDBC 的精细异常转换\n\t * 提供更多信息。\n\t * @param ex 要转换的 RuntimeException\n\t * @return 对应的 DataAccessException（若无法转换则返回 {@code null}，\n\t * 此时异常可能来自用户代码而非实际持久化问题）\n\t * @see org.springframework.dao.DataIntegrityViolationException\n\t * @see org.springframework.jdbc.support.SQLExceptionTranslator\n\t */",
        ),
    ],
    "PersistenceExceptionTranslationInterceptor.java": [
        (
            "/**\n * AOP Alliance MethodInterceptor that provides persistence exception translation\n * based on a given PersistenceExceptionTranslator.\n *\n * <p>Delegates to the given {@link PersistenceExceptionTranslator} to translate\n * a RuntimeException thrown into Spring's DataAccessException hierarchy\n * (if appropriate). If the RuntimeException in question is declared on the\n * target method, it is always propagated as-is (with no translation applied).\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see PersistenceExceptionTranslator\n */",
            "/**\n * 基于给定 PersistenceExceptionTranslator 提供持久化异常转换的\n * AOP Alliance MethodInterceptor。\n *\n * <p>委托给定 {@link PersistenceExceptionTranslator} 将抛出的 RuntimeException\n * 转换为 Spring 的 DataAccessException 层次结构（若适用）。\n * 若所涉 RuntimeException 在目标方法上声明，则始终原样传播（不应用转换）。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @since 2.0\n * @see PersistenceExceptionTranslator\n */",
        ),
        (
            "\t/**\n\t * Create a new PersistenceExceptionTranslationInterceptor.\n\t * Needs to be configured with a PersistenceExceptionTranslator afterwards.\n\t * @see #setPersistenceExceptionTranslator\n\t */",
            "\t/**\n\t * 创建新的 PersistenceExceptionTranslationInterceptor。\n\t * 之后须配置 PersistenceExceptionTranslator。\n\t * @see #setPersistenceExceptionTranslator\n\t */",
        ),
        (
            "\t/**\n\t * Create a new PersistenceExceptionTranslationInterceptor\n\t * for the given PersistenceExceptionTranslator.\n\t * @param pet the PersistenceExceptionTranslator to use\n\t */",
            "\t/**\n\t * 为给定 PersistenceExceptionTranslator 创建新的\n\t * PersistenceExceptionTranslationInterceptor。\n\t * @param pet 要使用的 PersistenceExceptionTranslator\n\t */",
        ),
        (
            "\t/**\n\t * Create a new PersistenceExceptionTranslationInterceptor, autodetecting\n\t * PersistenceExceptionTranslators in the given BeanFactory.\n\t * @param beanFactory the ListableBeanFactory to obtaining all\n\t * PersistenceExceptionTranslators from\n\t */",
            "\t/**\n\t * 创建新的 PersistenceExceptionTranslationInterceptor，\n\t * 自动检测给定 BeanFactory 中的 PersistenceExceptionTranslator。\n\t * @param beanFactory 用于获取所有 PersistenceExceptionTranslator 的 ListableBeanFactory\n\t */",
        ),
        (
            "\t/**\n\t * Specify the PersistenceExceptionTranslator to use.\n\t * <p>Default is to autodetect all PersistenceExceptionTranslators\n\t * in the containing BeanFactory, using them in a chain.\n\t * @see #detectPersistenceExceptionTranslators\n\t */",
            "\t/**\n\t * 指定要使用的 PersistenceExceptionTranslator。\n\t * <p>默认为自动检测所在 BeanFactory 中所有 PersistenceExceptionTranslator，\n\t * 以链式方式使用。\n\t * @see #detectPersistenceExceptionTranslators\n\t */",
        ),
        (
            "\t/**\n\t * Specify whether to always translate the exception (\"true\"), or whether throw the\n\t * raw exception when declared, i.e. when the originating method signature's exception\n\t * declarations allow for the raw exception to be thrown (\"false\").\n\t * <p>Default is \"false\". Switch this flag to \"true\" in order to always translate\n\t * applicable exceptions, independent of the originating method signature.\n\t * <p>Note that the originating method does not have to declare the specific exception.\n\t * Any base class will do as well, even {@code throws Exception}: As long as the\n\t * originating method does explicitly declare compatible exceptions, the raw exception\n\t * will be rethrown. If you would like to avoid throwing raw exceptions in any case,\n\t * switch this flag to \"true\".\n\t */",
            "\t/**\n\t * 指定是否始终转换异常（\"true\"），或在已声明时抛出原始异常（\"false\"），\n\t * 即源方法签名的异常声明允许抛出原始异常时。\n\t * <p>默认为 \"false\"。设为 \"true\" 可始终转换适用异常，\n\t * 不受源方法签名影响。\n\t * <p>注意，源方法不必声明特定异常。\n\t * 任何基类均可，甚至 {@code throws Exception}：只要源方法显式声明兼容异常，\n\t * 原始异常将被重新抛出。若希望任何情况下都避免抛出原始异常，\n\t * 请将本标志设为 \"true\"。\n\t */",
        ),
        (
            "\t/**\n\t * Detect all PersistenceExceptionTranslators in the given BeanFactory.\n\t * @param bf the ListableBeanFactory to obtain PersistenceExceptionTranslators from\n\t * @return a chained PersistenceExceptionTranslator, combining all\n\t * PersistenceExceptionTranslators found in the given bean factory\n\t * @see ChainedPersistenceExceptionTranslator\n\t */",
            "\t/**\n\t * 检测给定 BeanFactory 中所有 PersistenceExceptionTranslator。\n\t * @param bf 用于获取 PersistenceExceptionTranslator 的 ListableBeanFactory\n\t * @return 链式 PersistenceExceptionTranslator，组合给定 Bean 工厂中找到的所有\n\t * PersistenceExceptionTranslator\n\t * @see ChainedPersistenceExceptionTranslator\n\t */",
        ),
    ],
}
