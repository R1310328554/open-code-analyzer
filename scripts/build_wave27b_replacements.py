#!/usr/bin/env python3
"""Generate wave27b replacement modules from embedded translation tables."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "springframework/7.0.8/original"
ANALYZED = ROOT / "springframework/7.0.8/analyzed"
SCRIPTS = ROOT / "scripts"

BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/sf_w27b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

# (filename -> list of (english, chinese) javadoc/comment replacements)
OBJECT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}
SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def add_object(name: str, pairs: list[tuple[str, str]]) -> None:
    OBJECT_REPLACEMENTS[name] = pairs


def add_support(name: str, pairs: list[tuple[str, str]]) -> None:
    SUPPORT_REPLACEMENTS[name] = pairs


# ── GenericSqlQuery.java ──────────────────────────────────────────────────────
add_object("GenericSqlQuery.java", [
    (
        "/**\n * A concrete variant of {@link SqlQuery} which can be configured\n * with a {@link RowMapper}.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 3.0\n * @param <T> the result type\n * @see #setRowMapper\n * @see #setRowMapperClass\n */",
        "/**\n * {@link SqlQuery} 的具体实现，可通过 {@link RowMapper} 配置结果映射。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 3.0\n * @param <T> 结果类型\n * @see #setRowMapper\n * @see #setRowMapperClass\n */",
    ),
    (
        "\t/**\n\t * Set a specific {@link RowMapper} instance to use for this query.\n\t * @since 4.3.2\n\t */",
        "\t/**\n\t * 设置用于本查询的 {@link RowMapper} 实例。\n\t * @since 4.3.2\n\t */",
    ),
    (
        "\t/**\n\t * Set a {@link RowMapper} class for this query, creating a fresh\n\t * {@link RowMapper} instance per execution.\n\t */",
        "\t/**\n\t * 设置本查询使用的 {@link RowMapper} 类，每次执行时创建新的 {@link RowMapper} 实例。\n\t */",
    ),
])

# ── GenericStoredProcedure.java ───────────────────────────────────────────────
add_object("GenericStoredProcedure.java", [
    (
        "/**\n * Concrete implementation making it possible to define the RDBMS stored procedures\n * in an application context without writing a custom Java implementation class.\n * <p>\n * This implementation does not provide a typed method for invocation so executions\n * must use one of the generic {@link StoredProcedure#execute(java.util.Map)} or\n * {@link StoredProcedure#execute(org.springframework.jdbc.core.ParameterMapper)} methods.\n *\n * @author Thomas Risberg\n * @see org.springframework.jdbc.object.StoredProcedure\n */",
        "/**\n * 具体实现，允许在应用上下文中定义 RDBMS 存储过程，\n * 而无需编写自定义 Java 实现类。\n * <p>\n * 本实现不提供类型化的调用方法，执行时必须使用\n * 通用的 {@link StoredProcedure#execute(java.util.Map)} 或\n * {@link StoredProcedure#execute(org.springframework.jdbc.core.ParameterMapper)} 方法。\n *\n * @author Thomas Risberg\n * @see org.springframework.jdbc.object.StoredProcedure\n */",
    ),
])

# ── MappingSqlQuery.java ──────────────────────────────────────────────────────
add_object("MappingSqlQuery.java", [
    (
        "/**\n * Reusable query in which concrete subclasses must implement the abstract\n * mapRow(ResultSet, int) method to convert each row of the JDBC ResultSet\n * into an object.\n *\n * <p>Simplifies MappingSqlQueryWithParameters API by dropping parameters and\n * context. Most subclasses won't care about parameters. If you don't use\n * contextual information, subclass this instead of MappingSqlQueryWithParameters.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Jean-Pierre Pawlak\n * @param <T> the result type\n * @see MappingSqlQueryWithParameters\n */",
        "/**\n * 可复用查询，具体子类必须实现抽象方法 mapRow(ResultSet, int)，\n * 将 JDBC ResultSet 的每一行转换为对象。\n *\n * <p>通过省略参数和上下文简化 MappingSqlQueryWithParameters API。\n * 大多数子类不关心参数；若不需要上下文信息，应继承本类而非 MappingSqlQueryWithParameters。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Jean-Pierre Pawlak\n * @param <T> 结果类型\n * @see MappingSqlQueryWithParameters\n */",
    ),
    (
        "\t/**\n\t * Constructor that allows use as a JavaBean.\n\t */",
        "\t/**\n\t * 允许作为 JavaBean 使用的构造器。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient constructor with DataSource and SQL string.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL to run\n\t */",
        "\t/**\n\t * 便捷构造器，接收 DataSource 和 SQL 字符串。\n\t * @param ds 用于获取连接的 DataSource\n\t * @param sql 要执行的 SQL\n\t */",
    ),
    (
        "\t/**\n\t * This method is implemented to invoke the simpler mapRow\n\t * template method, ignoring parameters.\n\t * @see #mapRow(ResultSet, int)\n\t */",
        "\t/**\n\t * 本方法调用更简单的 mapRow 模板方法，忽略参数。\n\t * @see #mapRow(ResultSet, int)\n\t */",
    ),
    (
        "\t/**\n\t * Subclasses must implement this method to convert each row of the\n\t * ResultSet into an object of the result type.\n\t * <p>Subclasses of this class, as opposed to direct subclasses of\n\t * MappingSqlQueryWithParameters, don't need to concern themselves\n\t * with the parameters to the execute method of the query object.\n\t * @param rs the ResultSet we're working through\n\t * @param rowNum row number (from 0) we're up to\n\t * @return an object of the result type\n\t * @throws SQLException if there's an error extracting data.\n\t * Subclasses can simply not catch SQLExceptions, relying on the\n\t * framework to clean up.\n\t */",
        "\t/**\n\t * 子类必须实现本方法，将 ResultSet 的每一行转换为结果类型的对象。\n\t * <p>与本类子类不同，MappingSqlQueryWithParameters 的直接子类\n\t * 无需关心查询对象 execute 方法的参数。\n\t * @param rs 正在遍历的 ResultSet\n\t * @param rowNum 当前行号（从 0 开始）\n\t * @return 结果类型的对象\n\t * @throws SQLException 提取数据出错时抛出。\n\t * 子类通常无需捕获 SQLException，由框架负责清理。\n\t */",
    ),
])

# ── MappingSqlQueryWithParameters.java ────────────────────────────────────────
add_object("MappingSqlQueryWithParameters.java", [
    (
        "/**\n * Reusable RDBMS query in which concrete subclasses must implement\n * the abstract mapRow(ResultSet, int) method to map each row of\n * the JDBC ResultSet into an object.\n *\n * <p>Such manual mapping is usually preferable to \"automatic\"\n * mapping using reflection, which can become complex in non-trivial\n * cases. For example, the present class allows different objects\n * to be used for different rows (for example, if a subclass is indicated).\n * It allows computed fields to be set. And there's no need for\n * ResultSet columns to have the same names as bean properties.\n * The Pareto Principle in action: going the extra mile to automate\n * the extraction process makes the framework much more complex\n * and delivers little real benefit.\n *\n * <p>Subclasses can be constructed providing SQL, parameter types\n * and a DataSource. SQL will often vary between subclasses.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Jean-Pierre Pawlak\n * @param <T> the result type\n * @see org.springframework.jdbc.object.MappingSqlQuery\n * @see org.springframework.jdbc.object.SqlQuery\n */",
        "/**\n * 可复用的 RDBMS 查询，具体子类必须实现抽象方法 mapRow(ResultSet, int)，\n * 将 JDBC ResultSet 的每一行映射为对象。\n *\n * <p>这种手动映射通常优于基于反射的\"自动\"映射——后者在非平凡场景下会变得复杂。\n * 例如，本类允许不同行使用不同对象（例如根据指示选择子类）、设置计算字段，\n * 且 ResultSet 列名无需与 Bean 属性同名。\n * 帕累托原则在此体现：为自动化提取过程额外投入会使框架复杂得多，收益却有限。\n *\n * <p>子类构造时可提供 SQL、参数类型和 DataSource，SQL 通常因子类而异。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Jean-Pierre Pawlak\n * @param <T> 结果类型\n * @see org.springframework.jdbc.object.MappingSqlQuery\n * @see org.springframework.jdbc.object.SqlQuery\n */",
    ),
    (
        "\t/**\n\t * Constructor to allow use as a JavaBean.\n\t */",
        "\t/**\n\t * 允许作为 JavaBean 使用的构造器。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient constructor with DataSource and SQL string.\n\t * @param ds the DataSource to use to get connections\n\t * @param sql the SQL to run\n\t */",
        "\t/**\n\t * 便捷构造器，接收 DataSource 和 SQL 字符串。\n\t * @param ds 用于获取连接的 DataSource\n\t * @param sql 要执行的 SQL\n\t */",
    ),
    (
        "\t/**\n\t * Implementation of protected abstract method. This invokes the subclass's\n\t * implementation of the mapRow() method.\n\t */",
        "\t/**\n\t * 受保护抽象方法的实现，调用子类的 mapRow() 方法。\n\t */",
    ),
    (
        "\t/**\n\t * Subclasses must implement this method to convert each row\n\t * of the ResultSet into an object of the result type.\n\t * @param rs the ResultSet we're working through\n\t * @param rowNum row number (from 0) we're up to\n\t * @param parameters to the query (passed to the execute() method).\n\t * Subclasses are rarely interested in these.\n\t * It can be {@code null} if there are no parameters.\n\t * @param context passed to the execute() method.\n\t * It can be {@code null} if no contextual information is need.\n\t * @return an object of the result type\n\t * @throws SQLException if there's an error extracting data.\n\t * Subclasses can simply not catch SQLExceptions, relying on the\n\t * framework to clean up.\n\t */",
        "\t/**\n\t * 子类必须实现本方法，将 ResultSet 的每一行转换为结果类型的对象。\n\t * @param rs 正在遍历的 ResultSet\n\t * @param rowNum 当前行号（从 0 开始）\n\t * @param parameters 查询参数（传入 execute() 方法），子类通常不关心，无参数时可 {@code null}\n\t * @param context 传入 execute() 方法的上下文，无上下文信息时可 {@code null}\n\t * @return 结果类型的对象\n\t * @throws SQLException 提取数据出错时抛出。\n\t * 子类通常无需捕获 SQLException，由框架负责清理。\n\t */",
    ),
    (
        "\t/**\n\t * Implementation of RowMapper that calls the enclosing\n\t * class's {@code mapRow} method for each row.\n\t */",
        "\t/**\n\t * RowMapper 实现，对每一行调用外部类的 {@code mapRow} 方法。\n\t */",
    ),
    (
        "\t\t/**\n\t\t * Use an array results. More efficient if we know how many results to expect.\n\t\t */",
        "\t\t/**\n\t\t * 构造 RowMapper 实现，若已知结果数量则更高效。\n\t\t */",
    ),
])

# ── SqlOperation.java ─────────────────────────────────────────────────────────
add_object("SqlOperation.java", [
    (
        "/**\n * Operation object representing an SQL-based operation such as a query or update,\n * as opposed to a stored procedure.\n *\n * <p>Configures a {@link org.springframework.jdbc.core.PreparedStatementCreatorFactory}\n * based on the declared parameters.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
        "/**\n * 表示基于 SQL 的操作（如查询或更新）的操作对象，区别于存储过程。\n *\n * <p>根据声明的参数配置 {@link org.springframework.jdbc.core.PreparedStatementCreatorFactory}。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n */",
    ),
    (
        "\t/**\n\t * Object enabling us to create PreparedStatementCreators efficiently,\n\t * based on this class's declared parameters.\n\t */",
        "\t/**\n\t * 基于本类声明的参数高效创建 PreparedStatementCreator 的工厂对象。\n\t */",
    ),
    (
        "\t/** Parsed representation of the SQL statement. */",
        "\t/** SQL 语句的解析表示。 */",
    ),
    (
        "\t/** Monitor for locking the cached representation of the parsed SQL statement. */",
        "\t/** 用于锁定已解析 SQL 语句缓存表示的监视器。 */",
    ),
    (
        "\t/**\n\t * Overridden method to configure the PreparedStatementCreatorFactory\n\t * based on our declared parameters.\n\t */",
        "\t/**\n\t * 重写方法，根据声明的参数配置 PreparedStatementCreatorFactory。\n\t */",
    ),
    (
        "\t/**\n\t * Hook method that subclasses may override to post-process compilation.\n\t * This implementation does nothing.\n\t * @see #compileInternal\n\t */",
        "\t/**\n\t * 子类可覆盖的后处理编译钩子方法，本实现为空操作。\n\t * @see #compileInternal\n\t */",
    ),
    (
        "\t/**\n\t * Obtain a parsed representation of this operation's SQL statement.\n\t * <p>Typically used for named parameter parsing.\n\t */",
        "\t/**\n\t * 获取本操作 SQL 语句的解析表示。\n\t * <p>通常用于命名参数解析。\n\t */",
    ),
    (
        "\t/**\n\t * Return a PreparedStatementSetter to perform an operation\n\t * with the given parameters.\n\t * @param params the parameter array (may be {@code null})\n\t */",
        "\t/**\n\t * 返回 PreparedStatementSetter，以给定参数执行操作。\n\t * @param params 参数数组（可为 {@code null}）\n\t */",
    ),
    (
        "\t/**\n\t * Return a PreparedStatementCreator to perform an operation\n\t * with the given parameters.\n\t * @param params the parameter array (may be {@code null})\n\t */",
        "\t/**\n\t * 返回 PreparedStatementCreator，以给定参数执行操作。\n\t * @param params 参数数组（可为 {@code null}）\n\t */",
    ),
    (
        "\t/**\n\t * Return a PreparedStatementCreator to perform an operation\n\t * with the given parameters.\n\t * @param sqlToUse the actual SQL statement to use (if different from\n\t * the factory's, for example because of named parameter expanding)\n\t * @param params the parameter array (may be {@code null})\n\t */",
        "\t/**\n\t * 返回 PreparedStatementCreator，以给定参数执行操作。\n\t * @param sqlToUse 实际使用的 SQL 语句（若与工厂不同，例如因命名参数展开）\n\t * @param params 参数数组（可为 {@code null}）\n\t */",
    ),
])

# ── JdbcUtilsRuntimeHints.java ────────────────────────────────────────────────
add_support("JdbcUtilsRuntimeHints.java", [
    (
        "/**\n * {@link RuntimeHintsRegistrar} implementation that registers runtime hints for\n * {@link JdbcUtils}.\n *\n * @author Brian Clozel\n * @since 6.2.13\n */",
        "/**\n * {@link RuntimeHintsRegistrar} 实现，为 {@link JdbcUtils} 注册运行时提示。\n *\n * @author Brian Clozel\n * @since 6.2.13\n */",
    ),
])

# ── SqlCall.java ──────────────────────────────────────────────────────────────
add_object("SqlCall.java", [
    (
        "/**\n * RdbmsOperation using a JdbcTemplate and representing an SQL-based\n * call such as a stored procedure or a stored function.\n *\n * <p>Configures a CallableStatementCreatorFactory based on the declared\n * parameters.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @see CallableStatementCreatorFactory\n */",
        "/**\n * 使用 JdbcTemplate 的 RdbmsOperation，表示基于 SQL 的调用，\n * 如存储过程或存储函数。\n *\n * <p>根据声明的参数配置 CallableStatementCreatorFactory。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @see CallableStatementCreatorFactory\n */",
    ),
    (
        "\t/**\n\t * Flag used to indicate that this call is for a function and to\n\t * use the {? = call get_invoice_count(?)} syntax.\n\t */",
        "\t/**\n\t * 标志本调用是否为函数，并使用 {? = call get_invoice_count(?)} 语法。\n\t */",
    ),
    (
        "\t/**\n\t * Flag used to indicate that the sql for this call should be used exactly as\n\t * it is defined. No need to add the escape syntax and parameter placeholders.\n\t */",
        "\t/**\n\t * 标志本调用的 SQL 是否应原样使用，无需添加转义语法和参数占位符。\n\t */",
    ),
    (
        "\t/**\n\t * Call string as defined in java.sql.CallableStatement.\n\t * String of form {call add_invoice(?, ?, ?)} or {? = call get_invoice_count(?)}\n\t * if isFunction is set to true. Updated after each parameter is added.\n\t */",
        "\t/**\n\t * java.sql.CallableStatement 定义的调用字符串。\n\t * 形式为 {call add_invoice(?, ?, ?)} 或 {? = call get_invoice_count(?)}（isFunction 为 true 时）。\n\t * 每添加一个参数后更新。\n\t */",
    ),
    (
        "\t/**\n\t * Object enabling us to create CallableStatementCreators\n\t * efficiently, based on this class's declared parameters.\n\t */",
        "\t/**\n\t * 基于本类声明的参数高效创建 CallableStatementCreator 的工厂对象。\n\t */",
    ),
    (
        "\t/**\n\t * Constructor to allow use as a JavaBean.\n\t * A DataSource, SQL and any parameters must be supplied before\n\t * invoking the {@code compile} method and using this object.\n\t * @see #setDataSource\n\t * @see #setSql\n\t * @see #compile\n\t */",
        "\t/**\n\t * 允许作为 JavaBean 使用的构造器。\n\t * 调用 {@code compile} 方法并使用本对象前，必须提供 DataSource、SQL 及参数。\n\t * @see #setDataSource\n\t * @see #setSql\n\t * @see #compile\n\t */",
    ),
    (
        "\t/**\n\t * Create a new SqlCall object with SQL, but without parameters.\n\t * Must add parameters or settle with none.\n\t * @param ds the DataSource to obtain connections from\n\t * @param sql the SQL to execute\n\t */",
        "\t/**\n\t * 创建带 SQL 但无参数的 SqlCall 对象，需添加参数或确认无参数。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL\n\t */",
    ),
    (
        "\t/**\n\t * Set whether this call is for a function.\n\t */",
        "\t/**\n\t * 设置本调用是否为函数。\n\t */",
    ),
    (
        "\t/**\n\t * Return whether this call is for a function.\n\t */",
        "\t/**\n\t * 返回本调用是否为函数。\n\t */",
    ),
    (
        "\t/**\n\t * Set whether the SQL can be used as is.\n\t */",
        "\t/**\n\t * 设置 SQL 是否可直接使用。\n\t */",
    ),
    (
        "\t/**\n\t * Return whether the SQL can be used as is.\n\t */",
        "\t/**\n\t * 返回 SQL 是否可直接使用。\n\t */",
    ),
    (
        "\t/**\n\t * Overridden method to configure the CallableStatementCreatorFactory\n\t * based on our declared parameters.\n\t * @see RdbmsOperation#compileInternal()\n\t */",
        "\t/**\n\t * 重写方法，根据声明的参数配置 CallableStatementCreatorFactory。\n\t * @see RdbmsOperation#compileInternal()\n\t */",
    ),
    (
        "\t/**\n\t * Hook method that subclasses may override to react to compilation.\n\t * This implementation does nothing.\n\t */",
        "\t/**\n\t * 子类可覆盖以响应编译的钩子方法，本实现为空操作。\n\t */",
    ),
    (
        "\t/**\n\t * Get the call string.\n\t */",
        "\t/**\n\t * 获取调用字符串。\n\t */",
    ),
    (
        "\t/**\n\t * Return a CallableStatementCreator to perform an operation\n\t * with these parameters.\n\t * @param inParams parameters. May be {@code null}.\n\t */",
        "\t/**\n\t * 返回 CallableStatementCreator，以这些参数执行操作。\n\t * @param inParams 参数，可为 {@code null}\n\t */",
    ),
    (
        "\t/**\n\t * Return a CallableStatementCreator to perform an operation\n\t * with the parameters returned from this ParameterMapper.\n\t * @param inParamMapper parametermapper. May not be {@code null}.\n\t */",
        "\t/**\n\t * 返回 CallableStatementCreator，使用 ParameterMapper 返回的参数执行操作。\n\t * @param inParamMapper 参数映射器，不可为 {@code null}\n\t */",
    ),
])

# ── SqlFunction.java ──────────────────────────────────────────────────────────
add_object("SqlFunction.java", [
    (
        "/**\n * SQL \"function\" wrapper for a query that returns a single row of results.\n * The default behavior is to return an int, but that can be overridden by\n * using the constructor with an extra return type parameter.\n *\n * <p>Intended to use to call SQL functions that return a single result using a\n * query like \"select user()\" or \"select sysdate from dual\". It is not intended\n * for calling more complex stored functions or for using a CallableStatement to\n * invoke a stored procedure or stored function. Use StoredProcedure or SqlCall\n * for this type of processing.\n *\n * <p>This is a concrete class, which there is often no need to subclass.\n * Code using this package can create an object of this type, declaring SQL\n * and parameters, and then invoke the appropriate {@code run} method\n * repeatedly to execute the function. Subclasses are only supposed to add\n * specialized {@code run} methods for specific parameter and return types.\n *\n * <p>Like all RdbmsOperation objects, SqlFunction objects are thread-safe.\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Jean-Pierre Pawlak\n * @param <T> the result type\n * @see StoredProcedure\n */",
        "/**\n * SQL\"函数\"包装器，用于返回单行结果的查询。\n * 默认返回 int，可通过带返回类型参数的构造器覆盖。\n *\n * <p>用于调用返回单个结果的 SQL 函数，如 \"select user()\" 或 \"select sysdate from dual\"。\n * 不适用于调用复杂存储函数，也不适用于通过 CallableStatement 调用存储过程或存储函数；\n * 此类场景请使用 StoredProcedure 或 SqlCall。\n *\n * <p>本类为具体类，通常无需子类化。\n * 使用本包的代码可创建本类对象、声明 SQL 和参数，然后反复调用 {@code run} 方法执行函数。\n * 子类仅用于添加针对特定参数和返回类型的 {@code run} 方法。\n *\n * <p>与所有 RdbmsOperation 对象一样，SqlFunction 对象线程安全。\n *\n * @author Rod Johnson\n * @author Juergen Hoeller\n * @author Jean-Pierre Pawlak\n * @param <T> 结果类型\n * @see StoredProcedure\n */",
    ),
    (
        "\t/**\n\t * Constructor to allow use as a JavaBean.\n\t * A DataSource, SQL and any parameters must be supplied before\n\t * invoking the {@code compile} method and using this object.\n\t * @see #setDataSource\n\t * @see #setSql\n\t * @see #compile\n\t */",
        "\t/**\n\t * 允许作为 JavaBean 使用的构造器。\n\t * 调用 {@code compile} 并使用本对象前，必须提供 DataSource、SQL 及参数。\n\t * @see #setDataSource\n\t * @see #setSql\n\t * @see #compile\n\t */",
    ),
    (
        "\t/**\n\t * Create a new SqlFunction object with SQL, but without parameters.\n\t * Must add parameters or settle with none.\n\t * @param ds the DataSource to obtain connections from\n\t * @param sql the SQL to execute\n\t */",
        "\t/**\n\t * 创建带 SQL 但无参数的 SqlFunction 对象，需添加参数或确认无参数。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL\n\t */",
    ),
    (
        "\t/**\n\t * Create a new SqlFunction object with SQL and parameters.\n\t * @param ds the DataSource to obtain connections from\n\t * @param sql the SQL to execute\n\t * @param types the SQL types of the parameters, as defined in the\n\t * {@code java.sql.Types} class\n\t * @see java.sql.Types\n\t */",
        "\t/**\n\t * 创建带 SQL 和参数的 SqlFunction 对象。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL\n\t * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}\n\t * @see java.sql.Types\n\t */",
    ),
    (
        "\t/**\n\t * Create a new SqlFunction object with SQL, parameters and a result type.\n\t * @param ds the DataSource to obtain connections from\n\t * @param sql the SQL to execute\n\t * @param types the SQL types of the parameters, as defined in the\n\t * {@code java.sql.Types} class\n\t * @param resultType the type that the result object is required to match\n\t * @see #setResultType(Class)\n\t * @see java.sql.Types\n\t */",
        "\t/**\n\t * 创建带 SQL、参数和结果类型的 SqlFunction 对象。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL\n\t * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}\n\t * @param resultType 结果对象必须匹配的类型\n\t * @see #setResultType(Class)\n\t * @see java.sql.Types\n\t */",
    ),
    (
        "\t/**\n\t * Specify the type that the result object is required to match.\n\t * <p>If not specified, the result value will be exposed as\n\t * returned by the JDBC driver.\n\t */",
        "\t/**\n\t * 指定结果对象必须匹配的类型。\n\t * <p>未指定时，结果值将按 JDBC 驱动返回的原样暴露。\n\t */",
    ),
    (
        "\t/**\n\t * This implementation of this method extracts a single value from the\n\t * single row returned by the function. If there are a different number\n\t * of rows returned, this is treated as an error.\n\t */",
        "\t/**\n\t * 本方法从函数返回的单行中提取单个值；若返回行数不符则视为错误。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to run the function without arguments.\n\t * @return the value of the function\n\t */",
        "\t/**\n\t * 无参数执行函数的便捷方法。\n\t * @return 函数返回值\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to run the function with a single int argument.\n\t * @param parameter single int parameter\n\t * @return the value of the function\n\t */",
        "\t/**\n\t * 以单个 int 参数执行函数的便捷方法。\n\t * @param parameter 单个 int 参数\n\t * @return 函数返回值\n\t */",
    ),
    (
        "\t/**\n\t * Analogous to the SqlQuery.execute([]) method. This is a\n\t * generic method to execute a query, taken a number of arguments.\n\t * @param parameters array of parameters. These will be objects or\n\t * object wrapper types for primitives.\n\t * @return the value of the function\n\t */",
        "\t/**\n\t * 类似 SqlQuery.execute([]) 方法，以可变参数执行查询。\n\t * @param parameters 参数数组，为基本类型的对象或包装类型\n\t * @return 函数返回值\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to run the function without arguments,\n\t * returning the value as an object.\n\t * @return the value of the function\n\t */",
        "\t/**\n\t * 无参数执行函数并以 Object 返回值的便捷方法。\n\t * @return 函数返回值\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to run the function with a single int argument.\n\t * @param parameter single int parameter\n\t * @return the value of the function as an Object\n\t */",
        "\t/**\n\t * 以单个 int 参数执行函数并以 Object 返回值的便捷方法。\n\t * @param parameter 单个 int 参数\n\t * @return 函数返回值（Object 形式）\n\t */",
    ),
    (
        "\t/**\n\t * Analogous to the {@code SqlQuery.findObject(Object[])} method.\n\t * This is a generic method to execute a query, taken a number of arguments.\n\t * @param parameters array of parameters. These will be objects or\n\t * object wrapper types for primitives.\n\t * @return the value of the function, as an Object\n\t * @see #execute(Object[])\n\t */",
        "\t/**\n\t * 类似 {@code SqlQuery.findObject(Object[])} 方法，以参数数组执行查询。\n\t * @param parameters 参数数组，为基本类型的对象或包装类型\n\t * @return 函数返回值（Object 形式）\n\t * @see #execute(Object[])\n\t */",
    ),
])


def _format_replacements_dict(name: str, data: dict[str, list[tuple[str, str]]]) -> str:
    lines = [f'"""Chinese JavaDoc replacements for springframework wave27b {name} classes."""', ""]
    var = f"{name.upper()}_REPLACEMENTS"
    lines.append(f"{var}: dict[str, list[tuple[str, str]]] = {{")
    for fname, pairs in sorted(data.items()):
        lines.append(f'    "{fname}": [')
        for old, new in pairs:
            lines.append(f"        ({old!r},\n         {new!r}),")
        lines.append("    ],")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def _load_parts() -> None:
    g: dict = {"add_object": add_object, "add_support": add_support}
    for fname in ("wave27b_replacements_part2.py", "wave27b_replacements_part3.py"):
        exec((SCRIPTS / fname).read_text(encoding="utf-8"), g)


def generate_modules() -> None:
    _load_parts()

    (SCRIPTS / "wave27b_replacements_object.py").write_text(
        _format_replacements_dict("object", OBJECT_REPLACEMENTS), encoding="utf-8"
    )
    support = {**SUPPORT_REPLACEMENTS}
    (SCRIPTS / "wave27b_replacements_support.py").write_text(
        _format_replacements_dict("support", support), encoding="utf-8"
    )
    print(f"Generated object: {len(OBJECT_REPLACEMENTS)} files, support: {len(support)} files")


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def validate_all() -> tuple[int, list[str]]:
    _load_parts()
    all_reps = {**OBJECT_REPLACEMENTS, **SUPPORT_REPLACEMENTS}
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        reps = all_reps.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = apply_replacements(src.read_text(encoding="utf-8"), reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            if cn < 10:
                failures.append(f"LOW_CN={cn}: {rel}")
                continue
            if "Licensed under the Apache License" not in text:
                failures.append(f"NO_LICENSE: {rel}")
                continue
            dst = ANALYZED / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
    return ok, failures


if __name__ == "__main__":
    generate_modules()
    ok, failures = validate_all()
    if failures or ok != len(BATCH_FILES):
        print("FAILURES:", failures)
        raise SystemExit(1)
    print(f"All {ok} files validated.")
