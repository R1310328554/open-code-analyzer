"""Part 2 of wave27b replacements - SqlUpdate, StoredProcedure, UpdatableSqlQuery."""

# add_object / add_support injected by build_wave27b_replacements.py via exec()
# ── SqlUpdate.java ────────────────────────────────────────────────────────────
add_object("SqlUpdate.java", [
    (
        "/**\n * Reusable operation object representing an SQL update.\n *\n * <p>This class provides a number of {@code update} methods,\n * analogous to the {@code execute} methods of query objects.\n *\n * <p>This class is concrete. Although it can be subclassed (for example\n * to add a custom update method) it can easily be parameterized by setting\n * SQL and declaring parameters.\n *\n * <p>Like all {@code RdbmsOperation} classes that ship with the Spring\n * Framework, {@code SqlQuery} instances are thread-safe after their\n * initialization is complete. That is, after they are constructed and configured\n * via their setter methods, they can be used safely from multiple threads.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see SqlQuery\n */",
        "/**\n * 可复用的 SQL 更新操作对象。\n *\n * <p>本类提供多个 {@code update} 方法，类似查询对象的 {@code execute} 方法。\n *\n * <p>本类为具体类，可子类化（例如添加自定义 update 方法），\n * 也可通过设置 SQL 和声明参数轻松配置。\n *\n * <p>与 Spring Framework 中所有 {@code RdbmsOperation} 类一样，\n * {@code SqlUpdate} 实例在初始化完成后线程安全——\n * 即构造并通过 setter 配置后，可安全地在多线程中使用。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @see SqlQuery\n */",
    ),
    (
        "\t/**\n\t * Maximum number of rows the update may affect. If more are\n\t * affected, an exception will be thrown. Ignored if 0.\n\t */",
        "\t/**\n\t * 更新可影响的最大行数，超出则抛异常；为 0 时忽略。\n\t */",
    ),
    (
        "\t/**\n\t * An exact number of rows that must be affected.\n\t * Ignored if 0.\n\t */",
        "\t/**\n\t * 必须影响的确切行数；为 0 时忽略。\n\t */",
    ),
    (
        "\t/**\n\t * Constructor to allow use as a JavaBean. DataSource and SQL\n\t * must be supplied before compilation and use.\n\t * @see #setDataSource\n\t * @see #setSql\n\t */",
        "\t/**\n\t * 允许作为 JavaBean 使用的构造器，编译和使用前必须提供 DataSource 和 SQL。\n\t * @see #setDataSource\n\t * @see #setSql\n\t */",
    ),
    (
        "\t/**\n\t * Constructs an update object with a given DataSource and SQL.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL statement to execute\n\t */",
        "\t/**\n\t * 使用给定 DataSource 和 SQL 构造更新对象。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL 语句\n\t */",
    ),
    (
        "\t/**\n\t * Construct an update object with a given DataSource, SQL\n\t * and anonymous parameters.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL statement to execute\n\t * @param types the SQL types of the parameters, as defined in the\n\t * {@code java.sql.Types} class\n\t * @see java.sql.Types\n\t */",
        "\t/**\n\t * 使用给定 DataSource、SQL 和匿名参数构造更新对象。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL 语句\n\t * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}\n\t * @see java.sql.Types\n\t */",
    ),
    (
        "\t/**\n\t * Construct an update object with a given DataSource, SQL,\n\t * anonymous parameters and specifying the maximum number of rows\n\t * that may be affected.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL statement to execute\n\t * @param types the SQL types of the parameters, as defined in the\n\t * {@code java.sql.Types} class\n\t * @param maxRowsAffected the maximum number of rows that may\n\t * be affected by the update\n\t * @see java.sql.Types\n\t */",
        "\t/**\n\t * 使用给定 DataSource、SQL、匿名参数及最大影响行数构造更新对象。\n\t * @param ds 获取连接的 DataSource\n\t * @param sql 要执行的 SQL 语句\n\t * @param types 参数的 SQL 类型，定义于 {@code java.sql.Types}\n\t * @param maxRowsAffected 更新可影响的最大行数\n\t * @see java.sql.Types\n\t */",
    ),
    (
        "\t/**\n\t * Set the maximum number of rows that may be affected by this update.\n\t * The default value is 0, which does not limit the number of rows affected.\n\t * @param maxRowsAffected the maximum number of rows that can be affected by\n\t * this update without this class's update method considering it an error\n\t */",
        "\t/**\n\t * 设置本更新可影响的最大行数，默认 0 表示不限制。\n\t * @param maxRowsAffected 超出此值时 update 方法视为错误\n\t */",
    ),
    (
        "\t/**\n\t * Set the <i>exact</i> number of rows that must be affected by this update.\n\t * The default value is 0, which allows any number of rows to be affected.\n\t * <p>This is an alternative to setting the <i>maximum</i> number of rows\n\t * that may be affected.\n\t * @param requiredRowsAffected the exact number of rows that must be affected\n\t * by this update without this class's update method considering it an error\n\t */",
        "\t/**\n\t * 设置本更新必须影响的<i>确切</i>行数，默认 0 表示任意行数均可。\n\t * <p>这是设置最大影响行数的替代方案。\n\t * @param requiredRowsAffected 行数不符时 update 方法视为错误\n\t */",
    ),
    (
        "\t/**\n\t * Check the given number of affected rows against the\n\t * specified maximum number or required number.\n\t * @param rowsAffected the number of affected rows\n\t * @throws JdbcUpdateAffectedIncorrectNumberOfRowsException\n\t * if the actually affected rows are out of bounds\n\t * @see #setMaxRowsAffected\n\t * @see #setRequiredRowsAffected\n\t */",
        "\t/**\n\t * 检查实际影响行数是否符合最大或必需行数限制。\n\t * @param rowsAffected 实际影响行数\n\t * @throws JdbcUpdateAffectedIncorrectNumberOfRowsException 行数超出限制时\n\t * @see #setMaxRowsAffected\n\t * @see #setRequiredRowsAffected\n\t */",
    ),
    (
        "\t/**\n\t * Generic method to execute the update given parameters.\n\t * All other update methods invoke this method.\n\t * @param params array of parameters objects\n\t * @return the number of rows affected by the update\n\t */",
        "\t/**\n\t * 以给定参数执行更新的通用方法，其他 update 方法均调用本方法。\n\t * @param params 参数对象数组\n\t * @return 更新影响的行数\n\t */",
    ),
    (
        "\t/**\n\t * Method to execute the update given arguments and\n\t * retrieve the generated keys using a KeyHolder.\n\t * @param params array of parameter objects\n\t * @param generatedKeyHolder the KeyHolder that will hold the generated keys\n\t * @return the number of rows affected by the update\n\t */",
        "\t/**\n\t * 以给定参数执行更新，并通过 KeyHolder 获取生成的主键。\n\t * @param params 参数对象数组\n\t * @param generatedKeyHolder 存放生成主键的 KeyHolder\n\t * @return 更新影响的行数\n\t */",
    ),
    (
        "\t/**\n\t * Convenience method to execute an update with no parameters.\n\t */",
        "\t/**\n\t * 无参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to execute an update given one int arg.\n\t */",
        "\t/**\n\t * 以单个 int 参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to execute an update given two int args.\n\t */",
        "\t/**\n\t * 以两个 int 参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to execute an update given one long arg.\n\t */",
        "\t/**\n\t * 以单个 long 参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to execute an update given two long args.\n\t */",
        "\t/**\n\t * 以两个 long 参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to execute an update given one String arg.\n\t */",
        "\t/**\n\t * 以单个 String 参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Convenient method to execute an update given two String args.\n\t */",
        "\t/**\n\t * 以两个 String 参数执行更新的便捷方法。\n\t */",
    ),
    (
        "\t/**\n\t * Generic method to execute the update given named parameters.\n\t * All other update methods invoke this method.\n\t * @param paramMap a Map of parameter name to parameter object,\n\t * matching named parameters specified in the SQL statement\n\t * @return the number of rows affected by the update\n\t */",
        "\t/**\n\t * 以命名参数执行更新的通用方法。\n\t * @param paramMap 参数名到参数对象的映射，与 SQL 中的命名参数对应\n\t * @return 更新影响的行数\n\t */",
    ),
    (
        "\t/**\n\t * Method to execute the update given arguments and\n\t * retrieve the generated keys using a KeyHolder.\n\t * @param paramMap a Map of parameter name to parameter object,\n\t * matching named parameters specified in the SQL statement\n\t * @param generatedKeyHolder the KeyHolder that will hold the generated keys\n\t * @return the number of rows affected by the update\n\t */",
        "\t/**\n\t * 以命名参数执行更新，并通过 KeyHolder 获取生成的主键。\n\t * @param paramMap 参数名到参数对象的映射，与 SQL 中的命名参数对应\n\t * @param generatedKeyHolder 存放生成主键的 KeyHolder\n\t * @return 更新影响的行数\n\t */",
    ),
])

# ── StoredProcedure.java ──────────────────────────────────────────────────────
add_object("StoredProcedure.java", [
    (
        "/**\n * Superclass for object abstractions of RDBMS stored procedures.\n * This class is abstract, and it is intended that subclasses will provide a typed\n * method for invocation that delegates to the supplied {@link #execute} method.\n *\n * <p>The inherited {@link #setSql sql} property is the name of the stored procedure\n * in the RDBMS.\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n */",
        "/**\n * RDBMS 存储过程对象抽象的父类。\n * 本类为抽象类，子类应提供类型化调用方法，委托给 {@link #execute} 方法。\n *\n * <p>继承的 {@link #setSql sql} 属性为 RDBMS 中存储过程的名称。\n *\n * @author Rod Johnson\n * @author Thomas Risberg\n */",
    ),
    (
        "\t/**\n\t * Allow use as a bean.\n\t */",
        "\t/**\n\t * 允许作为 Bean 使用。\n\t */",
    ),
    (
        "\t/**\n\t * Create a new object wrapper for a stored procedure.\n\t * @param ds the DataSource to use throughout the lifetime\n\t * of this object to obtain connections\n\t * @param name the name of the stored procedure in the database\n\t */",
        "\t/**\n\t * 为存储过程创建新的对象包装器。\n\t * @param ds 本对象生命周期内用于获取连接的 DataSource\n\t * @param name 数据库中存储过程的名称\n\t */",
    ),
    (
        "\t/**\n\t * Create a new object wrapper for a stored procedure.\n\t * @param jdbcTemplate the JdbcTemplate which wraps DataSource\n\t * @param name the name of the stored procedure in the database\n\t */",
        "\t/**\n\t * 为存储过程创建新的对象包装器。\n\t * @param jdbcTemplate 包装 DataSource 的 JdbcTemplate\n\t * @param name 数据库中存储过程的名称\n\t */",
    ),
    (
        "\t/**\n\t * StoredProcedure parameter Maps are by default allowed to contain\n\t * additional entries that are not actually used as parameters.\n\t */",
        "\t/**\n\t * StoredProcedure 的参数 Map 默认允许包含未实际用作参数的额外条目。\n\t */",
    ),
    (
        "\t/**\n\t * Declare a parameter.\n\t * <p>Parameters declared as {@code SqlParameter} and {@code SqlInOutParameter}\n\t * will always be used to provide input values. In addition to this, any parameter declared\n\t * as {@code SqlOutParameter} where a non-null input value is provided will also be used\n\t * as an input parameter.\n\t * <b>Note: Calls to declareParameter must be made in the same order as\n\t * they appear in the database's stored procedure parameter list.</b>\n\t * <p>Names are purely used to help mapping.\n\t * @param param the parameter object\n\t * @throws InvalidDataAccessApiUsageException if the parameter has no name, or if the\n\t * operation is already compiled, and hence cannot be configured further\n\t */",
        "\t/**\n\t * 声明参数。\n\t * <p>声明为 {@code SqlParameter} 和 {@code SqlInOutParameter} 的参数始终用于提供输入值。\n\t * 此外，声明为 {@code SqlOutParameter} 且提供了非空输入值的参数也会作为输入参数。\n\t * <b>注意：declareParameter 调用顺序必须与数据库存储过程参数列表一致。</b>\n\t * <p>名称仅用于辅助映射。\n\t * @param param 参数对象\n\t * @throws InvalidDataAccessApiUsageException 参数无名称或操作已编译无法继续配置时\n\t */",
    ),
    (
        "\t/**\n\t * Execute the stored procedure with the provided parameter values. This is\n\t * a convenience method where the order of the passed in parameter values\n\t * must match the order that the parameters where declared in.\n\t * @param inParams variable number of input parameters. Output parameters should\n\t * not be included in this map. It is legal for values to be {@code null}, and this\n\t * will produce the correct behavior using a NULL argument to the stored procedure.\n\t * @return map of output params, keyed by name as in parameter declarations.\n\t * Output parameters will appear here, with their values after the stored procedure\n\t * has been called.\n\t */",
        "\t/**\n\t * 以提供的参数值执行存储过程。\n\t * 便捷方法，传入参数值的顺序必须与声明顺序一致。\n\t * @param inParams 可变数量的输入参数，输出参数不应包含在内；值为 {@code null} 合法，\n\t * 将以 NULL 参数调用存储过程。\n\t * @return 输出参数映射，键为参数声明中的名称，\n\t * 存储过程调用后输出参数的值将出现在此映射中。\n\t */",
    ),
    (
        "\t/**\n\t * Execute the stored procedure. Subclasses should define a strongly typed\n\t * execute method (with a meaningful name) that invokes this method, populating\n\t * the input map and extracting typed values from the output map. Subclass\n\t * execute methods will often take domain objects as arguments and return values.\n\t * Alternatively, they can return void.\n\t * @param inParams map of input parameters, keyed by name as in parameter\n\t * declarations. Output parameters need not (but can) be included in this map.\n\t * It is legal for map entries to be {@code null}, and this will produce the\n\t * correct behavior using a NULL argument to the stored procedure.\n\t * @return map of output params, keyed by name as in parameter declarations.\n\t * Output parameters will appear here, with their values after the\n\t * stored procedure has been called.\n\t */",
        "\t/**\n\t * 执行存储过程。子类应定义强类型 execute 方法（有意义的名称）调用本方法，\n\t * 填充输入映射并从输出映射提取类型化值；子类 execute 方法通常接收领域对象并返回值，也可返回 void。\n\t * @param inParams 输入参数映射，键为参数声明中的名称；\n\t * 输出参数可不（但可）包含在内；值为 {@code null} 合法。\n\t * @return 输出参数映射，存储过程调用后输出参数的值将出现在此映射中。\n\t */",
    ),
    (
        "\t/**\n\t * Execute the stored procedure. Subclasses should define a strongly typed\n\t * execute method (with a meaningful name) that invokes this method, passing in\n\t * a ParameterMapper that will populate the input map.  This allows mapping database\n\t * specific features since the ParameterMapper has access to the Connection object.\n\t * The execute method is also responsible for extracting typed values from the output map.\n\t * Subclass execute methods will often take domain objects as arguments and return values.\n\t * Alternatively, they can return void.\n\t * @param inParamMapper map of input parameters, keyed by name as in parameter\n\t * declarations. Output parameters need not (but can) be included in this map.\n\t * It is legal for map entries to be {@code null}, and this will produce the correct\n\t * behavior using a NULL argument to the stored procedure.\n\t * @return map of output params, keyed by name as in parameter declarations.\n\t * Output parameters will appear here, with their values after the\n\t * stored procedure has been called.\n\t */",
        "\t/**\n\t * 执行存储过程。子类应定义强类型 execute 方法调用本方法，\n\t * 传入 ParameterMapper 填充输入映射；ParameterMapper 可访问 Connection，便于映射数据库特定特性。\n\t * execute 方法还负责从输出映射提取类型化值。\n\t * @param inParamMapper 参数映射器，填充输入参数映射\n\t * @return 输出参数映射，存储过程调用后输出参数的值将出现在此映射中。\n\t */",
    ),
])

# ── UpdatableSqlQuery.java ────────────────────────────────────────────────────
add_object("UpdatableSqlQuery.java", [
    (
        "/**\n * Reusable RDBMS query in which concrete subclasses must implement\n * the abstract updateRow(ResultSet, int, context) method to update each\n * row of the JDBC ResultSet and optionally map contents into an object.\n *\n * <p>Subclasses can be constructed providing SQL, parameter types\n * and a DataSource. SQL will often vary between subclasses.\n *\n * @author Thomas Risberg\n * @param <T> the result type\n * @see org.springframework.jdbc.object.SqlQuery\n */",
        "/**\n * 可复用的 RDBMS 查询，具体子类必须实现抽象方法 updateRow(ResultSet, int, context)，\n * 更新 JDBC ResultSet 的每一行，并可选地将内容映射为对象。\n *\n * <p>子类构造时可提供 SQL、参数类型和 DataSource，SQL 通常因子类而异。\n *\n * @author Thomas Risberg\n * @param <T> 结果类型\n * @see org.springframework.jdbc.object.SqlQuery\n */",
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
        "\t/**\n\t * Implementation of the superclass template method. This invokes the subclass's\n\t * implementation of the {@code updateRow()} method.\n\t */",
        "\t/**\n\t * 父类模板方法的实现，调用子类的 {@code updateRow()} 方法。\n\t */",
    ),
    (
        "\t/**\n\t * Subclasses must implement this method to update each row of the\n\t * ResultSet and optionally create object of the result type.\n\t * @param rs the ResultSet we're working through\n\t * @param rowNum row number (from 0) we're up to\n\t * @param context passed to the {@code execute()} method.\n\t * It can be {@code null} if no contextual information is need.  If you\n\t * need to pass in data for each row, you can pass in a HashMap with\n\t * the primary key of the row being the key for the HashMap.  That way\n\t * it is easy to locate the updates for each row\n\t * @return an object of the result type\n\t * @throws SQLException if there's an error updating data.\n\t * Subclasses can simply not catch SQLExceptions, relying on the\n\t * framework to clean up.\n\t */",
        "\t/**\n\t * 子类必须实现本方法，更新 ResultSet 的每一行，并可选地创建结果类型对象。\n\t * @param rs 正在遍历的 ResultSet\n\t * @param rowNum 当前行号（从 0 开始）\n\t * @param context 传入 {@code execute()} 方法的上下文，无上下文信息时可 {@code null}；\n\t * 若需为每行传入数据，可使用以行主键为键的 HashMap，便于定位每行的更新\n\t * @return 结果类型的对象\n\t * @throws SQLException 更新数据出错时抛出。\n\t * 子类通常无需捕获 SQLException，由框架负责清理。\n\t */",
    ),
    (
        "\t/**\n\t * Implementation of RowMapper that calls the enclosing\n\t * class's {@code updateRow()} method for each row.\n\t */",
        "\t/**\n\t * RowMapper 实现，对每一行调用外部类的 {@code updateRow()} 方法。\n\t */",
    ),
])
