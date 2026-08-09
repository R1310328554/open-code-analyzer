"""Chinese JavaDoc replacements for springframework wave27a jdbc object classes."""

OBJECT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "BatchSqlUpdate.java": [
        (
            "/**\n * SqlUpdate subclass that performs batch update operations. Encapsulates\n * queuing up records to be updated, and adds them as a single batch once\n * {@code flush} is called or the given batch size has been met.\n *\n * <p>Note that this class is a <b>non-thread-safe object</b>, in contrast\n * to all other JDBC operations objects in this package. You need to create\n * a new instance of it for each use, or call {@code reset} before\n * reuse within the same thread.\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @since 1.1\n * @see #flush\n * @see #reset\n */",
            "/**\n * 执行批量更新操作的 SqlUpdate 子类。封装待更新记录的排队逻辑，\n * 在调用 {@code flush} 或达到给定批量大小时作为单个批次提交。\n *\n * <p>注意：与本包中其他 JDBC 操作对象不同，本类为<b>非线程安全对象</b>。\n * 每次使用需创建新实例，或在同一线程内复用前调用 {@code reset}。\n *\n * @author Keith Donald\n * @author Juergen Hoeller\n * @since 1.1\n * @see #flush\n * @see #reset\n */",
        ),
        (
            "\t/**\n\t * Default number of inserts to accumulate before committing a batch (5000).\n\t */",
            "\t/**\n\t * 提交批次前累积的默认插入条数（5000）。\n\t */",
        ),
        (
            "\t/**\n\t * Constructor to allow use as a JavaBean. DataSource and SQL\n\t * must be supplied before compilation and use.\n\t * @see #setDataSource\n\t * @see #setSql\n\t */",
            "\t/**\n\t * 允许作为 JavaBean 使用的构造函数。编译和使用前须设置 DataSource 与 SQL。\n\t * @see #setDataSource\n\t * @see #setSql\n\t */",
        ),
        (
            "\t/**\n\t * Construct an update object with a given DataSource and SQL.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL statement to execute\n\t */",
            "\t/**\n\t * 使用给定 DataSource 与 SQL 构造更新对象。\n\t * @param ds 用于获取连接的 DataSource\n\t * @param sql 要执行的 SQL 语句\n\t */",
        ),
        (
            "\t/**\n\t * Construct an update object with a given DataSource, SQL\n\t * and anonymous parameters.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL statement to execute\n\t * @param types the SQL types of the parameters, as defined in the\n\t * {@code java.sql.Types} class\n\t * @see java.sql.Types\n\t */",
            "\t/**\n\t * 使用给定 DataSource、SQL 与匿名参数构造更新对象。\n\t * @param ds 用于获取连接的 DataSource\n\t * @param sql 要执行的 SQL 语句\n\t * @param types 参数 SQL 类型，定义于 {@code java.sql.Types}\n\t * @see java.sql.Types\n\t */",
        ),
        (
            "\t/**\n\t * Construct an update object with a given DataSource, SQL,\n\t * anonymous parameters and specifying the maximum number of rows\n\t * that may be affected.\n\t * @param ds the DataSource to use to obtain connections\n\t * @param sql the SQL statement to execute\n\t * @param types the SQL types of the parameters, as defined in the\n\t * {@code java.sql.Types} class\n\t * @param batchSize the number of statements that will trigger\n\t * an automatic intermediate flush\n\t * @see java.sql.Types\n\t */",
            "\t/**\n\t * 使用给定 DataSource、SQL、匿名参数及批量大小构造更新对象。\n\t * @param ds 用于获取连接的 DataSource\n\t * @param sql 要执行的 SQL 语句\n\t * @param types 参数 SQL 类型，定义于 {@code java.sql.Types}\n\t * @param batchSize 触发自动中间 flush 的语句数量\n\t * @see java.sql.Types\n\t */",
        ),
        (
            "\t/**\n\t * Set the number of statements that will trigger an automatic intermediate\n\t * flush. {@code update} calls or the given statement parameters will\n\t * be queued until the batch size is met, at which point it will empty the\n\t * queue and execute the batch.\n\t * <p>You can also flush already queued statements with an explicit\n\t * {@code flush} call. Note that you need to this after queueing\n\t * all parameters to guarantee that all statements have been flushed.\n\t */",
            "\t/**\n\t * 设置触发自动中间 flush 的语句数量。{@code update} 调用或给定语句参数\n\t * 将排队直至达到批量大小，届时清空队列并执行批次。\n\t * <p>也可通过显式 {@code flush} 调用刷新已排队语句。\n\t * 注意：排队所有参数后须调用 flush，以确保所有语句均已刷新。\n\t */",
        ),
        (
            "\t/**\n\t * Set whether to track the rows affected by batch updates performed\n\t * by this operation object.\n\t * <p>Default is \"true\". Turn this off to save the memory needed for\n\t * the list of row counts.\n\t * @see #getRowsAffected()\n\t */",
            "\t/**\n\t * 设置是否跟踪本操作对象执行的批量更新所影响的行数。\n\t * <p>默认为 \"true\"。关闭可节省行数列表所需内存。\n\t * @see #getRowsAffected()\n\t */",
        ),
        (
            "\t/**\n\t * BatchSqlUpdate does not support BLOB or CLOB parameters.\n\t */",
            "\t/**\n\t * BatchSqlUpdate 不支持 BLOB 或 CLOB 参数。\n\t */",
        ),
        (
            "\t/**\n\t * Overridden version of {@code update} that adds the given statement\n\t * parameters to the queue rather than executing them immediately.\n\t * All other {@code update} methods of the SqlUpdate base class go\n\t * through this method and will thus behave similarly.\n\t * <p>You need to call {@code flush} to actually execute the batch.\n\t * If the specified batch size is reached, an implicit flush will happen;\n\t * you still need to finally call {@code flush} to flush all statements.\n\t * @param params array of parameter objects\n\t * @return the number of rows affected by the update (always -1,\n\t * meaning \"not applicable\", as the statement is not actually\n\t * executed by this method)\n\t * @see #flush\n\t */",
            "\t/**\n\t * 重写的 {@code update}，将给定语句参数加入队列而非立即执行。\n\t * SqlUpdate 基类的其他 {@code update} 方法均经此方法，行为类似。\n\t * <p>须调用 {@code flush} 才真正执行批次。\n\t * 达到指定批量大小时会隐式 flush；仍须最终调用 {@code flush} 刷新所有语句。\n\t * @param params 参数对象数组\n\t * @return 更新影响行数（恒为 -1，表示“不适用”，因本方法不实际执行语句）\n\t * @see #flush\n\t */",
        ),
        (
            "\t/**\n\t * Trigger any queued update operations to be added as a final batch.\n\t * @return an array of the number of rows affected by each statement\n\t */",
            "\t/**\n\t * 触发将已排队的更新操作作为最终批次提交。\n\t * @return 每条语句影响行数的数组\n\t */",
        ),
        (
            "\t/**\n\t * Return the current number of statements or statement parameters\n\t * in the queue.\n\t */",
            "\t/**\n\t * 返回队列中当前语句或语句参数的数量。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of already executed statements.\n\t */",
            "\t/**\n\t * 返回已执行语句的数量。\n\t */",
        ),
        (
            "\t/**\n\t * Return the number of affected rows for all already executed statements.\n\t * Accumulates all of {@code flush}'s return values until\n\t * {@code reset} is invoked.\n\t * @return an array of the number of rows affected by each statement\n\t * @see #reset\n\t */",
            "\t/**\n\t * 返回所有已执行语句的影响行数。\n\t * 累积 {@code flush} 的返回值直至调用 {@code reset}。\n\t * @return 每条语句影响行数的数组\n\t * @see #reset\n\t */",
        ),
        (
            "\t/**\n\t * Reset the statement parameter queue, the rows affected cache,\n\t * and the execution count.\n\t */",
            "\t/**\n\t * 重置语句参数队列、影响行数缓存与执行计数。\n\t */",
        ),
    ],
}
