"""Chinese OCA + JavaDoc replacements for Spring Framework 7.0.8 wave31b mega batch [10:20]."""

from __future__ import annotations

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# ---------------------------------------------------------------------------
# AbstractAutoProxyCreator.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["AbstractAutoProxyCreator.java"] = [
    (
        "package org.springframework.aop.framework.autoproxy;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "Spring AOP 自动代理创建器抽象基类：作为 BeanPostProcessor 在容器初始化阶段为符合条件的 Bean 包装 AOP 代理，"
        "协调通用/特定拦截器、TargetSourceCreator 与 Advisor 适配注册表。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.aop.framework.autoproxy;",
    ),
    (
        "/**\n"
        " * {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现使用 AOP 代理包装每个符合条件的\n"
        " * bean，在调用 bean 本身之前委托给指定的拦截器。\n"
        " * <p>该类区分“通用”拦截器：为其创建的所有代理共享，以及“特定”拦截器：每个 bean 实例唯一。不需要有任何通用的拦截器。如果有，则使用 InterceptorNames \n"
        " * 属性来设置它们。与 {@link org.springframework.aop.framework.ProxyFactoryBean} 一样，使用当前工厂中的拦截器名称而不是\n"
        " *  bean 引用来允许正确处理原型顾问程序和拦截器：例如，支持有状态 mixins。 {@link #setInterceptorNames \"interceptorNames\n"
        " * \"} 条目支持任何建议类型。\n"
        " * <p>如果有大量的bean需要用类似的代理包装，即委托给相同的拦截器，那么这种自动代理特别有用。您可以向 bean 工厂注册一个这样的后处理器来实现相同的效果，而不是为 x 个\n"
        " * 目标 bean 进行 x 个重复的代理定义。\n"
        " * <p>子类可以应用任何策略来决定是否要代理 bean，例如按类型、按名称、按定义详细信息等。它们还可以返回应仅应用于特定 bean 实例的附加拦截器。一个简单的具体实现是 {@\n"
        " * link BeanNameAutoProxyCreator}，通过给定名称标识要代理的 bean。\n"
        " * <p> 任意数量的 {@link TargetSourceCreator} 实现都可用于创建自定义目标源：例如，池原型对象。只要 TargetSourceCreator\n"
        " * 指定自定义 {@link org.springframework.aop.TargetSource}，即使没有建议，自动代理也会发生。如果没有设置\n"
        " * TargetSourceCreators，或者没有匹配，则默认情况下将使用 {@link\n"
        " * org.springframework.aop.target.SingletonTargetSource} 来包装目标 bean 实例。\n"
        " * @author Juergen Hoeller\n"
        " * @author Rod Johnson\n"
        " * @author Rob Harrop\n"
        " * @author Sam Brannen\n"
        " * @since 13.10.2003\n"
        " * @see #setInterceptorNames\n"
        " * @see #getAdvicesAndAdvisorsForBean\n"
        " * @see BeanNameAutoProxyCreator\n"
        " * @see DefaultAdvisorAutoProxyCreator\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class AbstractAutoProxyCreator — 意图说明\n\n"
        "AOP 自动代理的核心抽象：在 Bean 实例化/属性填充阶段决定是否创建代理、装配 Advisor 与 TargetSource，"
        "是 DefaultAdvisorAutoProxyCreator、BeanNameAutoProxyCreator 等的共同父类。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现，"
        "用 AOP 代理包装每个符合条件的 Bean，在调用目标 Bean 之前先经过指定拦截器链。\n"
        " * <p>区分「通用」拦截器（所有代理共享）与「特定」拦截器（每个 Bean 实例独有）。"
        "通用拦截器通过 {@link #setInterceptorNames interceptorNames} 配置；"
        "与 {@link org.springframework.aop.framework.ProxyFactoryBean} 一样使用工厂内 Bean 名称而非引用，"
        "以正确处理原型 Advisor/Interceptor（如 stateful mixin）。\n"
        " * <p>当大量 Bean 需委托相同拦截器时，注册单个后处理器即可替代重复的代理定义。\n"
        " * <p>子类决定代理策略（按类型、名称、定义细节等），并可返回仅作用于特定 Bean 的附加拦截器。"
        "典型实现如 {@link BeanNameAutoProxyCreator}。\n"
        " * <p>可配置 {@link TargetSourceCreator} 创建自定义 TargetSource（如池化原型）；"
        "即使无 Advice，只要指定了自定义 {@link org.springframework.aop.TargetSource} 也会代理。"
        "未匹配时默认使用 {@link org.springframework.aop.target.SingletonTargetSource}。\n"
        " * @author Juergen Hoeller\n"
        " * @author Rod Johnson\n"
        " * @author Rob Harrop\n"
        " * @author Sam Brannen\n"
        " * @since 13.10.2003\n"
        " * @see #setInterceptorNames\n"
        " * @see #getAdvicesAndAdvisorsForBean\n"
        " * @see BeanNameAutoProxyCreator\n"
        " * @see DefaultAdvisorAutoProxyCreator\n"
        " */",
    ),
    (
        "\t/**\n"
        "\t * 子类的方便常量：“不代理”的返回值。\n"
        "\t * @see #getAdvicesAndAdvisorsForBean\n"
        "\t */",
        "\t/**\n"
        "\t * 子类便捷常量：表示「不创建代理」的返回值。\n"
        "\t * @see #getAdvicesAndAdvisorsForBean\n"
        "\t */",
    ),
    (
        "\t/**\n"
        "\t * 指定要使用的 {@link AdvisorAdapterRegistry}。 <p>Ddefault 是全局 {@link AdvisorAdapterRegistry}。\n"
        "\t * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry\n"
        "\t */",
        "\t/**\n"
        "\t * 指定要使用的 {@link AdvisorAdapterRegistry}。\n"
        "\t * <p>默认为全局 {@link AdvisorAdapterRegistry}。\n"
        "\t * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry\n"
        "\t */",
    ),
]

# ---------------------------------------------------------------------------
# CustomizableTraceInterceptor.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["CustomizableTraceInterceptor.java"] = [
    (
        "package org.springframework.aop.interceptor;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "可定制占位符的方法级跟踪拦截器：在方法入口/出口/异常时输出日志，"
        "支持 $[methodName]、$[arguments] 等运行时占位符替换。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.aop.interceptor;",
    ),
    (
        "/**\n"
        " * {@code MethodInterceptor} 实现允许使用占位符进行高度可定制的方法级跟踪。\n"
        " * <p>Trace 消息写入方法入口处，如果方法调用成功则写入方法出口处。如果调用导致异常，则会写入异常消息。这些跟踪消息的内容是完全可定制的，并且可以使用特殊的占位符来允许您在\n"
        " * 日志消息中包含运行时信息。可用的占位符有：\n"
        " * <p><ul> <li>{@code $[methodName]} - 替换为正在调用的方法的名称</li> <li>{@code $[targetClassName]} -\n"
        " * 替换为调用目标的类的名称</li> <li>{@code $[targetClassShortName]} - 替换为调用目标的类的短名称incall</li>\n"
        " * <li>{@code $[returnValue]} - 替换为调用 </li> 返回的值 <li>{@code $[argumentTypes]} -\n"
        " * 替换为方法参数的短类名的逗号分隔列表</li> <li>{@code $[arguments]} - 替换为方法参数的 {@code String}\n"
        " * 表示形式的逗号分隔列表</li> <li>{@code $[exception]} - 替换为调用期间引发的任何 {@code Throwable} 的 {@code\n"
        " * String} 表示形式</li> <li>{@code $[invocationTime]} - 替换为所采取的时间（以毫秒为单位）通过方法调用</li> </ul>\n"
        " * <p> 对于哪些消息中可以使用哪些占位符存在限制：有关有效占位符的详细信息，请参阅各个消息属性。\n"
        " * @author Rob Harrop\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 1.2\n"
        " * @see #setEnterMessage\n"
        " * @see #setExitMessage\n"
        " * @see #setExceptionMessage\n"
        " * @see SimpleTraceInterceptor\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class CustomizableTraceInterceptor — 意图说明\n\n"
        "基于占位符模板的方法跟踪拦截器：继承 AbstractTraceInterceptor，"
        "在 enter/exit/exception 三类消息中替换运行时占位符生成诊断日志。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 支持占位符的高度可定制 {@code MethodInterceptor}，用于方法级跟踪日志。\n"
        " * <p>方法入口写 enter 消息，正常返回写 exit 消息，抛出异常写 exception 消息。"
        "消息模板可完全自定义，可用占位符包括：\n"
        " * <ul>\n"
        " * <li>{@code $[methodName]} — 被调用方法名</li>\n"
        " * <li>{@code $[targetClassName]} — 目标类全限定名</li>\n"
        " * <li>{@code $[targetClassShortName]} — 目标类短名</li>\n"
        " * <li>{@code $[returnValue]} — 返回值字符串</li>\n"
        " * <li>{@code $[argumentTypes]} — 参数类型短名列表</li>\n"
        " * <li>{@code $[arguments]} — 参数值字符串列表</li>\n"
        " * <li>{@code $[exception]} — 异常字符串</li>\n"
        " * <li>{@code $[invocationTime]} — 调用耗时（毫秒）</li>\n"
        " * </ul>\n"
        " * <p>不同消息类型允许的占位符集合不同，详见各 setXxxMessage 属性。\n"
        " * @author Rob Harrop\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 1.2\n"
        " * @see #setEnterMessage\n"
        " * @see #setExitMessage\n"
        " * @see #setExceptionMessage\n"
        " * @see SimpleTraceInterceptor\n"
        " */",
    ),
]

# ---------------------------------------------------------------------------
# BeanPropertyRowMapper.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["BeanPropertyRowMapper.java"] = [
    (
        "package org.springframework.jdbc.core;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "基于 JavaBean 属性名的 RowMapper：按列名（含下划线转驼峰）匹配 setter，"
        "将 ResultSet 行映射为目标类型实例；便利优先，非高性能场景首选。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core;",
    ),
    (
        "/**\n"
        " * {@link RowMapper} 实现将行转换为指定映射目标类的新实例。映射的目标类必须是顶级类或 {@code static} 嵌套类，并且它必须具有默认或无参数构造函数。\n"
        " * <p>Column 值根据列名（从结果集元数据获取）与目标类中相应属性的公共设置器的匹配进行映射。名称可以直接匹配，也可以通过使用“驼峰”大小写将用下划线分隔的部分的名称转换为\n"
        " * 相同的名称来匹配。\n"
        " * <p>Mapping 是为许多常见类型的目标类中的属性提供的\n"
        " * –例如：String、boolean、Boolean、byte、Byte、short、Short、int、Integer、long、Long、float、Float、double、Double、BigDecimal、{@code\n"
        " * java.util.Date} 等。\n"
        " * <p>为了促进没有匹配名称的列和属性之间的映射，请尝试在 SQL 语句中使用下划线分隔的列别名，例如 {@code \"select fname as first_name fr\n"
        " * om customer\"}，其中 {@code first_name} 可以映射到目标类中的 {@code setFirstName(String)} 方法。\n"
        " * <p> 对于从数据库读取的 {@code NULL} 值，将尝试使用 {@code null} 调用相应的 setter 方法，但对于 Java 原语，默认情况下这将导致\n"
        " * {@link TypeMismatchException}。要忽略目标类中所有基元属性的 {@code NULL} 数据库值，请将 {@code\n"
        " * primitivesDefaultedForNullValue} 标志设置为 {@code true}。有关详细信息，请参阅 {@link\n"
        " * #setPrimitivesDefaultedForNullValue(boolean)}。\n"
        " * <p>如果需要映射到具有 <em> 数据类的目标类 </em> 构造函数 –例如，Java {@code record} 或 Kotlin {@code data} 类\n"
        " * –请改用 {@link DataClassRowMapper}。\n"
        " * <p>请注意，此类旨在提供便利而不是高性能。为了获得最佳性能，请考虑使用自定义 {@code RowMapper} 实现。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 2.5\n"
        " * @param <T> 结果类型\n"
        " * @see DataClassRowMapper\n"
        " * @see SimplePropertyRowMapper\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class BeanPropertyRowMapper — 意图说明\n\n"
        "按列名与 JavaBean setter 匹配的行映射器：支持下划线列名转驼峰属性、"
        "ConversionService 类型转换及 NULL 基元处理策略。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * {@link RowMapper} 实现，将每行映射为指定目标类的新实例。"
        "目标类须为顶级或 {@code static} 嵌套类，且有无参构造器。\n"
        " * <p>列值按 ResultSet 元数据列名与目标类 public setter 匹配；"
        "支持直接匹配或下划线列名转驼峰属性名。\n"
        " * <p>内置 String、数值、Boolean、BigDecimal、{@code java.util.Date} 等常见类型转换。\n"
        " * <p>列名与属性不一致时可在 SQL 中使用下划线别名，如 {@code first_name} 映射 {@code setFirstName}。\n"
        " * <p>数据库 NULL 默认调用 setter(null)；基元属性会触发 {@link TypeMismatchException}，"
        "可设 {@link #setPrimitivesDefaultedForNullValue(boolean)} 忽略。\n"
        " * <p>映射 record/data class 请用 {@link DataClassRowMapper}。\n"
        " * <p>本类侧重便利而非性能，高性能场景请用定制 {@code RowMapper}。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 2.5\n"
        " * @param <T> 结果类型\n"
        " * @see DataClassRowMapper\n"
        " * @see SimplePropertyRowMapper\n"
        " */",
    ),
    (
        "\t/**\n"
        "\t * 创建一个新的 {@code BeanPropertyRowMapper} 用于 bean 样式配置。\n"
        "\t * @see #setMappedClass\n"
        "\t * @see #setCheckFullyPopulated\n"
        "\t */",
        "\t/**\n"
        "\t * 创建 {@code BeanPropertyRowMapper}，供后续通过 setter 配置 mappedClass 等属性。\n"
        "\t * @see #setMappedClass\n"
        "\t * @see #setCheckFullyPopulated\n"
        "\t */",
    ),
]

# ---------------------------------------------------------------------------
# JdbcOperations.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["JdbcOperations.java"] = [
    (
        "package org.springframework.jdbc.core;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "Spring JDBC 核心操作接口：定义 Connection/Statement/PreparedStatement/CallableStatement "
        "上的查询、更新、批处理与存储过程调用契约；由 JdbcTemplate 实现，便于测试 mock。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core;",
    ),
    (
        "/**\n"
        " * 指定一组基本 JDBC 操作的接口。\n"
        " * <p>由{@link JdbcTemplate}实现。通常不直接使用，但它是增强可测试性的有用选项，因为它可以轻松地被模拟或存根。\n"
        " * <p>或者，可以模拟标准 JDBC 基础结构。然而，模拟这个接口会显着减少工作量。作为测试数据访问代码的模拟对象方法的替代方法，请考虑通过 <em>Spring TestCon\n"
        " * text Framework</em>（在 {@code spring-test} 工件中）提供的强大集成测试支持。\n"
        " * <p><b>NOTE：从 6.1 开始，有一个统一的 JDBC 访问外观，以 {@link\n"
        " * org.springframework.jdbc.core.simple.JdbcClient} 的形式提供。</b> {@code JdbcClient} 为常见的 JDBC\n"
        " * 查询/更新提供了流畅的 API 风格，可以灵活地使用索引或命名参数。它委托给 {@code JdbcOperations}/{@code\n"
        " * NamedParameterJdbcOperations} 来实际执行。\n"
        " * @author Rod Johnson\n"
        " * @author Juergen Hoeller\n"
        " * @see JdbcTemplate\n"
        " * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "interface JdbcOperations — 意图说明\n\n"
        "JDBC 模板层对外契约：封装连接获取、语句执行、异常翻译与回调风格 API，"
        "是 JdbcTemplate/NamedParameterJdbcTemplate 的共同抽象。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 定义 Spring JDBC 基本操作的接口。\n"
        " * <p>由 {@link JdbcTemplate} 实现。通常注入接口类型以增强可测试性（便于 mock/stub）。\n"
        " * <p>相比 mock 整个 JDBC 栈，mock 本接口更轻量；"
        "也可考虑 {@code spring-test} 中的集成测试支持。\n"
        " * <p><b>注意：自 6.1 起推荐使用 {@link org.springframework.jdbc.core.simple.JdbcClient} "
        "作为统一 JDBC 访问外观。</b> {@code JdbcClient} 提供流式 API，"
        "底层仍委托 {@code JdbcOperations}/{@code NamedParameterJdbcOperations}。\n"
        " * @author Rod Johnson\n"
        " * @author Juergen Hoeller\n"
        " * @see JdbcTemplate\n"
        " * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations\n"
        " */",
    ),
    (
        "\t/**\n"
        "\t * 执行 JDBC 数据访问操作，作为在 JDBC 连接上工作的回调操作来实现。这允许在 Spring 的托管 JDBC 环境中实现任意数据访问操作：即参与 Spring 管理的事\n"
        "\t * 务并将 JDBC SQLException 转换为 Spring 的 DataAccessException 层次结构。 <p>回调操作可以返回结果对象，例如域对象或域对象的集\n"
        "\t * 合。\n"
        "\t * @param action 指定操作的回调对象\n"
        "\t * @return 操作返回的结果对象，如果没有则返回 {@code null}\n"
        "\t * @throws DataAccessException 如果有任何问题\n"
        "\t */",
        "\t/**\n"
        "\t * 在 Spring 托管的 {@link Connection} 上执行 {@link ConnectionCallback}。\n"
        "\t * <p>参与 Spring 事务并将 {@link SQLException} 翻译为 {@link DataAccessException}。\n"
        "\t * @param action 指定操作的回调\n"
        "\t * @return 回调返回的结果，无则 {@code null}\n"
        "\t * @throws DataAccessException 数据访问失败时\n"
        "\t */",
    ),
]

# ---------------------------------------------------------------------------
# JdbcTemplate.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["JdbcTemplate.java"] = [
    (
        "package org.springframework.jdbc.core;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "Spring JDBC 中央模板类：实现 JdbcOperations，管理 DataSource 连接、"
        "PreparedStatement/CallableStatement 生命周期、异常翻译与各类 query/update 回调。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core;",
    ),
    (
        "/**\n"
        " * <b>这是 JDBC 核心包中的中央委托。 </b> 它可以直接用于许多数据访问目的，支持任何类型的 JDBC 操作。在此之上，为了获得更集中、更方便的外观，请考虑从 6.1 \n"
        " * 开始使用 {@link org.springframework.jdbc.core.simple.JdbcClient}。\n"
        " * <p>该类简化了 JDBC 的使用并有助于避免常见错误。它执行核心 JDBC 工作流程，让应用程序代码提供 SQL 并提取结果。此类执行 SQL 查询或更新、启动 Result\n"
        " * Set 迭代并捕获 JDBC 异常并将它们转换为常见的 {@code org.springframework.dao} 异常层次结构。\n"
        " * 使用此类的 <p>Code 只需实现回调接口，给它们一个明确定义的契约。 {@link PreparedStatementCreator}\n"
        " * 回调接口在给定连接的情况下创建准备好的语句，提供 SQL 和任何必要的参数。 {@link ResultSetExtractor} 接口从 ResultSet\n"
        " * 中提取值。另请参阅 {@link PreparedStatementSetter} 和 {@link RowMapper} 了解两个流行的替代回调接口。\n"
        " * <p>一旦配置，该模板类的实例就是线程安全的。可以通过使用 DataSource 引用直接实例化在服务实现中使用，或者在应用程序上下文中做好准备并作为 bean 引用提供给服务\n"
        " * 。注意：数据源应始终在应用程序上下文中配置为 bean，在第一种情况下直接提供给服务，在第二种情况下提供给准备好的模板。\n"
        " * <p> 因为此类可通过回调接口和 {@link org.springframework.jdbc.support.SQLExceptionTranslator}\n"
        " * 接口进行参数化，所以不需要对其进行子类化。\n"
        " * <p>A 此类执行的所有 SQL 操作都在调试级别记录，使用“org.springframework.jdbc.core.JdbcTemplate”作为日志类别。\n"
        " * <p><b>NOTE：从 6.1 开始，有一个统一的 JDBC 访问外观，以 {@link\n"
        " * org.springframework.jdbc.core.simple.JdbcClient} 的形式提供。</b> {@code JdbcClient} 为常见的 JDBC\n"
        " * 查询/更新提供了流畅的 API 风格，可以灵活地使用索引或命名参数。它委托给 {@code JdbcTemplate}/{@code\n"
        " * NamedParameterJdbcTemplate} 来实际执行。\n"
        " * @author Rod Johnson\n"
        " * @author Juergen Hoeller\n"
        " * @author Thomas Risberg\n"
        " * @author Yanming Zhou\n"
        " * @since May 3, 2001\n"
        " * @see JdbcOperations\n"
        " * @see PreparedStatementCreator\n"
        " * @see PreparedStatementSetter\n"
        " * @see CallableStatementCreator\n"
        " * @see PreparedStatementCallback\n"
        " * @see CallableStatementCallback\n"
        " * @see ResultSetExtractor\n"
        " * @see RowCallbackHandler\n"
        " * @see RowMapper\n"
        " * @see org.springframework.jdbc.support.SQLExceptionTranslator\n"
        " * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class JdbcTemplate — 意图说明\n\n"
        "JDBC 核心模板：封装连接/语句资源管理、SQL 执行与 SQLException 翻译，"
        "通过 ConnectionCallback/PreparedStatementCallback 等回调让应用代码专注 SQL 与结果映射。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * <b>JDBC 核心包中的中央委托类。</b> 可直接完成各类 JDBC 操作；"
        "自 6.1 起也可考虑更简洁的 {@link org.springframework.jdbc.core.simple.JdbcClient}。\n"
        " * <p>简化 JDBC 使用、避免常见错误：执行查询/更新、遍历 ResultSet，"
        "并将 {@link SQLException} 翻译为 {@code org.springframework.dao} 异常层次。\n"
        " * <p>应用代码实现 {@link PreparedStatementCreator}、{@link ResultSetExtractor}、"
        "{@link RowMapper} 等回调即可，无需子类化本模板。\n"
        " * <p>配置完成后实例线程安全；DataSource 应作为 Bean 注入。\n"
        " * <p>所有 SQL 操作在 debug 级别记录，日志类别为 {@code org.springframework.jdbc.core.JdbcTemplate}。\n"
        " * <p><b>注意：6.1+ 提供 {@link org.springframework.jdbc.core.simple.JdbcClient} 流式 API，"
        "底层仍委托本类与 {@code NamedParameterJdbcTemplate}。</b>\n"
        " * @author Rod Johnson\n"
        " * @author Juergen Hoeller\n"
        " * @author Thomas Risberg\n"
        " * @author Yanming Zhou\n"
        " * @since May 3, 2001\n"
        " * @see JdbcOperations\n"
        " * @see PreparedStatementCreator\n"
        " * @see PreparedStatementSetter\n"
        " * @see CallableStatementCreator\n"
        " * @see PreparedStatementCallback\n"
        " * @see CallableStatementCallback\n"
        " * @see ResultSetExtractor\n"
        " * @see RowCallbackHandler\n"
        " * @see RowMapper\n"
        " * @see org.springframework.jdbc.support.SQLExceptionTranslator\n"
        " * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate\n"
        " */",
    ),
]

# ---------------------------------------------------------------------------
# StatementCreatorUtils.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["StatementCreatorUtils.java"] = [
    (
        "package org.springframework.jdbc.core;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "PreparedStatement/CallableStatement 参数绑定工具：Java 类型到 JDBC 类型的映射、"
        "NULL/LOB 值设置及 ParameterMetaData 兼容性处理。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core;",
    ),
    (
        "/**\n"
        " * 用于PreparedStatementSetter/Creator 和CallableStatementCreator 实现的实用方法，提供复杂的参数管理（包括对LOB 值的支\n"
        " * 持）。\n"
        " * <p> 由PreparedStatementCreatorFactory 和CallableStatementCreatorFactory\n"
        " * 使用，但也可直接在自定义setter/creator 实现中使用。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @since 1.1\n"
        " * @see PreparedStatementSetter\n"
        " * @see PreparedStatementCreator\n"
        " * @see CallableStatementCreator\n"
        " * @see PreparedStatementCreatorFactory\n"
        " * @see CallableStatementCreatorFactory\n"
        " * @see SqlParameter\n"
        " * @see SqlTypeValue\n"
        " * @see org.springframework.jdbc.core.support.SqlLobValue\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class StatementCreatorUtils — 意图说明\n\n"
        "语句创建辅助类：为 PreparedStatementSetter/Creator 提供 setParameterValue、"
        "setNull 等静态方法，统一 Java 类型与 SQL 类型的绑定逻辑。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * {@link PreparedStatementSetter}/{@link PreparedStatementCreator} 与\n"
        " * {@link CallableStatementCreator} 的参数绑定工具类，支持 LOB 等复杂类型。\n"
        " * <p>由 {@link PreparedStatementCreatorFactory}、{@link CallableStatementCreatorFactory} 使用，"
        "也可在自定义 creator/setter 中直接调用。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @since 1.1\n"
        " * @see PreparedStatementSetter\n"
        " * @see PreparedStatementCreator\n"
        " * @see CallableStatementCreator\n"
        " * @see PreparedStatementCreatorFactory\n"
        " * @see CallableStatementCreatorFactory\n"
        " * @see SqlParameter\n"
        " * @see SqlTypeValue\n"
        " * @see org.springframework.jdbc.core.support.SqlLobValue\n"
        " */",
    ),
]

# ---------------------------------------------------------------------------
# CallMetaDataContext.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["CallMetaDataContext.java"] = [
    (
        "package org.springframework.jdbc.core.metadata;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "存储过程调用上下文：持有过程名、catalog/schema、SqlParameter 列表与 CallMetaDataProvider，"
        "负责 IN/OUT 参数映射及 CallableStatement 创建前的元数据解析。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core.metadata;",
    ),
    (
        "/**\n"
        " * 用于管理用于存储过程调用的配置和执行的上下文元数据的类。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Kiril Nugmanov\n"
        " * @since 2.5\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class CallMetaDataContext — 意图说明\n\n"
        "存储过程调用的运行时上下文：结合 CallMetaDataProvider 解析参数顺序与 JDBC 类型，"
        "生成 CallableStatement 所需的参数声明与返回值映射。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 管理存储过程调用配置与执行所需的上下文元数据。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Kiril Nugmanov\n"
        " * @since 2.5\n"
        " */",
    ),
]

# ---------------------------------------------------------------------------
# GenericCallMetaDataProvider.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["GenericCallMetaDataProvider.java"] = [
    (
        "package org.springframework.jdbc.core.metadata;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "CallMetaDataProvider 通用实现：通过 DatabaseMetaData.getProcedureColumns "
        "读取存储过程参数元数据，可被数据库特定子类扩展。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core.metadata;",
    ),
    (
        "/**\n"
        " * {@link CallMetaDataProvider} 接口的通用实现。\n"
        " * <p> 这个类可以扩展以提供数据库特定的行为。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @author Stephane Nicoll\n"
        " * @since 2.5\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class GenericCallMetaDataProvider — 意图说明\n\n"
        "基于 JDBC DatabaseMetaData 的过程参数元数据提供者：解析 IN/OUT/INOUT 参数名、"
        "序号与 SQL 类型，供 SimpleJdbcCall 等组件使用。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * {@link CallMetaDataProvider} 的通用实现。\n"
        " * <p>可被子类扩展以适配特定数据库的行为差异。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @author Stephane Nicoll\n"
        " * @since 2.5\n"
        " */",
    ),
    (
        "\t/**\n"
        "\t * 用于使用提供的数据库元数据进行初始化的构造函数。\n"
        "\t * @param databaseMetaData 要使用的元数据\n"
        "\t */",
        "\t/**\n"
        "\t * 使用给定 {@link DatabaseMetaData} 初始化提供者。\n"
        "\t * @param databaseMetaData JDBC 数据库元数据\n"
        "\t */",
    ),
]

# ---------------------------------------------------------------------------
# GenericTableMetaDataProvider.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["GenericTableMetaDataProvider.java"] = [
    (
        "package org.springframework.jdbc.core.metadata;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "TableMetaDataProvider 通用实现：读取表列元数据、generated keys 能力及标识符大小写规则，"
        "支撑 SimpleJdbcInsert 等自动生成 INSERT 语句。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core.metadata;",
    ),
    (
        "/**\n"
        " * {@link TableMetaDataProvider} 接口的通用实现，应该为所有支持的数据库提供足够的功能。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 2.5\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class GenericTableMetaDataProvider — 意图说明\n\n"
        "表结构元数据通用提供者：通过 DatabaseMetaData 获取列名、类型、"
        "是否支持 getGeneratedKeys 等信息，供 TableMetaDataContext 构建 INSERT。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * {@link TableMetaDataProvider} 的通用实现，适用于大多数受支持的数据库。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 2.5\n"
        " */",
    ),
]

# ---------------------------------------------------------------------------
# TableMetaDataContext.java
# ---------------------------------------------------------------------------
FILE_REPLACEMENTS["TableMetaDataContext.java"] = [
    (
        "package org.springframework.jdbc.core.metadata;",
        "/* ===== [OCA 中文解析] =====\n"
        "文件意图总览\n\n"
        "表操作元数据上下文：持有表名、catalog/schema、列元数据与 TableMetaDataProvider，"
        "为 SimpleJdbcInsert 生成参数化 INSERT 及主键回填逻辑。\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "package org.springframework.jdbc.core.metadata;",
    ),
    (
        "/**\n"
        " * 管理用于配置和执行数据库表操作的上下文元数据的类。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 2.5\n"
        " */",
        "/* ===== [OCA 中文解析] =====\n"
        "class TableMetaDataContext — 意图说明\n\n"
        "INSERT/UPDATE 等表操作的运行时上下文：解析参与列、引号规则与 generated keys，"
        "与 GenericTableMetaDataProvider 协作完成元数据驱动的 SQL 组装。\n\n"
        "（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        "===== [OCA 中文解析结束] ===== */\n"
        "/**\n"
        " * 管理数据库表操作（如 INSERT）的配置与执行上下文元数据。\n"
        " * @author Thomas Risberg\n"
        " * @author Juergen Hoeller\n"
        " * @author Sam Brannen\n"
        " * @since 2.5\n"
        " */",
    ),
]
