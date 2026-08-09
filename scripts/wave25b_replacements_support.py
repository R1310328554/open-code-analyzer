"""Chinese JavaDoc replacements for springframework wave25b support classes."""

SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractInterruptibleBatchPreparedStatementSetter.java": [
        (
            "/**\n * Abstract implementation of the {@link InterruptibleBatchPreparedStatementSetter}\n * interface, combining the check for available values and setting of those\n * into a single callback method {@link #setValuesIfAvailable}.\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setValuesIfAvailable\n */",
            "/**\n * {@link InterruptibleBatchPreparedStatementSetter} 接口的抽象实现，\n * 将可用值检查和值设置合并为单一回调方法 {@link #setValuesIfAvailable}。\n *\n * @author Juergen Hoeller\n * @since 2.0\n * @see #setValuesIfAvailable\n */",
        ),
        (
            "\t/**\n\t * This implementation calls {@link #setValuesIfAvailable}\n\t * and sets this instance's exhaustion flag accordingly.\n\t */",
            "\t/**\n\t * 本实现调用 {@link #setValuesIfAvailable}\n\t * 并据此设置本实例的耗尽标志。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation return this instance's current exhaustion flag.\n\t */",
            "\t/**\n\t * 本实现返回本实例当前的耗尽标志。\n\t */",
        ),
        (
            "\t/**\n\t * This implementation returns {@code Integer.MAX_VALUE}.\n\t * Can be overridden in subclasses to lower the maximum batch size.\n\t */",
            "\t/**\n\t * 本实现返回 {@code Integer.MAX_VALUE}。\n\t * 子类可覆盖以降低最大批大小。\n\t */",
        ),
        (
            "\t/**\n\t * Check for available values and set them on the given PreparedStatement.\n\t * If no values are available anymore, return {@code false}.\n\t * @param ps the PreparedStatement we'll invoke setter methods on\n\t * @param i index of the statement we're issuing in the batch, starting from 0\n\t * @return whether there were values to apply (that is, whether the applied\n\t * parameters should be added to the batch and this method should be called\n\t * for a further iteration)\n\t * @throws SQLException if an SQLException is encountered\n\t * (i.e. there is no need to catch SQLException)\n\t */",
            "\t/**\n\t * 检查可用值并将其设置到给定 PreparedStatement 上。\n\t * 若无更多可用值，返回 {@code false}。\n\t * @param ps 将调用 setter 方法的 PreparedStatement\n\t * @param i 批中当前语句的索引，从 0 开始\n\t * @return 是否有值可应用（即应用的参数是否应加入批中，\n\t * 以及是否应再次调用本方法进行下一轮迭代）\n\t * @throws SQLException 遇到 SQLException 时抛出（无需捕获）\n\t */",
        ),
    ],
    "AbstractLobCreatingPreparedStatementCallback.java": [
        (
            "/**\n * Abstract {@link PreparedStatementCallback} implementation that manages a {@link LobCreator}.\n * Typically used as inner class, with access to surrounding method arguments.\n *\n * <p>Delegates to the {@code setValues} template method for setting values\n * on the PreparedStatement, using a given LobCreator for BLOB/CLOB arguments.\n *\n * <p>A usage example with {@link org.springframework.jdbc.core.JdbcTemplate}:\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object\n * LobHandler lobHandler = new DefaultLobHandler();  // reusable object\n *\n * jdbcTemplate.execute(\n *     \"INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)\",\n *     new AbstractLobCreatingPreparedStatementCallback(lobHandler) {\n *       protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {\n *         ps.setString(1, name);\n *         lobCreator.setBlobAsBinaryStream(ps, 2, contentStream, contentLength);\n *         lobCreator.setClobAsString(ps, 3, description);\n *       }\n *     });\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @see org.springframework.jdbc.support.lob.LobCreator\n * @deprecated as of 6.2, in favor of {@link SqlBinaryValue} and {@link SqlCharacterValue}\n */",
            "/**\n * 管理 {@link LobCreator} 的抽象 {@link PreparedStatementCallback} 实现。\n * 通常作为内部类使用，可访问外围方法参数。\n *\n * <p>委托 {@code setValues} 模板方法在 PreparedStatement 上设置值，\n * 使用给定 LobCreator 处理 BLOB/CLOB 参数。\n *\n * <p>与 {@link org.springframework.jdbc.core.JdbcTemplate} 配合使用的示例：\n *\n * <pre class=\"code\">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可复用对象\n * LobHandler lobHandler = new DefaultLobHandler();  // 可复用对象\n *\n * jdbcTemplate.execute(\n *     \"INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)\",\n *     new AbstractLobCreatingPreparedStatementCallback(lobHandler) {\n *       protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {\n *         ps.setString(1, name);\n *         lobCreator.setBlobAsBinaryStream(ps, 2, contentStream, contentLength);\n *         lobCreator.setClobAsString(ps, 3, description);\n *       }\n *     });\n * </pre>\n *\n * @author Juergen Hoeller\n * @since 1.0.2\n * @see org.springframework.jdbc.support.lob.LobCreator\n * @deprecated 自 6.2 起弃用，建议使用 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}\n */",
        ),
        (
            "\t/**\n\t * Create a new AbstractLobCreatingPreparedStatementCallback for the\n\t * given LobHandler.\n\t * @param lobHandler the LobHandler to create LobCreators with\n\t */",
            "\t/**\n\t * 为给定 LobHandler 创建新的 AbstractLobCreatingPreparedStatementCallback。\n\t * @param lobHandler 用于创建 LobCreator 的 LobHandler\n\t */",
        ),
        (
            "\t/**\n\t * Set values on the given PreparedStatement, using the given\n\t * LobCreator for BLOB/CLOB arguments.\n\t * @param ps the PreparedStatement to use\n\t * @param lobCreator the LobCreator to use\n\t * @throws SQLException if thrown by JDBC methods\n\t * @throws DataAccessException in case of custom exceptions\n\t */",
            "\t/**\n\t * 在给定 PreparedStatement 上设置值，\n\t * 使用给定 LobCreator 处理 BLOB/CLOB 参数。\n\t * @param ps 要使用的 PreparedStatement\n\t * @param lobCreator 要使用的 LobCreator\n\t * @throws SQLException JDBC 方法抛出时\n\t * @throws DataAccessException 自定义异常时\n\t */",
        ),
    ],
}
