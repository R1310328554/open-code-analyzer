"""Chinese annotation replacements for Nacos 3.2.3 wave77b [15:30] datasource dialect + derby mappers."""

R: dict[str, list[tuple[str, str]]] = {}

# --- AbstractDatabaseDialect ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/dialect/AbstractDatabaseDialect.java"] = [
    (
        "/**\n * Abstract DatabaseDialect.\n * Default limit for mysql,postgresql\n * @author Long Yu\n */",
        "/**\n * 数据库方言抽象基类，提供 MySQL/PostgreSQL 等数据库通用的分页与主键返回约定。\n *\n"
        " * <p>默认使用 {@code LIMIT offset, count} 语法拼接分页 SQL，子类可按具体数据库覆盖。</p>\n *\n"
        " * @author Long Yu\n */",
    ),
    (
        "    @Override\n    public int getPagePrevNum(int page, int pageSize) {",
        "    /** 计算分页起始偏移量：{@code (page - 1) * pageSize}。 */\n"
        "    @Override\n    public int getPagePrevNum(int page, int pageSize) {",
    ),
    (
        "    @Override\n    public int getPageLastNum(int page, int pageSize) {",
        "    /** 返回单页记录数（默认等于 pageSize）。 */\n"
        "    @Override\n    public int getPageLastNum(int page, int pageSize) {",
    ),
    (
        "    @Override\n    public String getLimitTopSqlWithMark(String sql) {",
        "    /** 在 SQL 末尾追加 {@code LIMIT ?} 占位符（取前 N 条）。 */\n"
        "    @Override\n    public String getLimitTopSqlWithMark(String sql) {",
    ),
    (
        "    @Override\n    public String getLimitPageSqlWithMark(String sql) {",
        "    /** 在 SQL 末尾追加 {@code LIMIT ?,?} 占位符（偏移量 + 页大小）。 */\n"
        "    @Override\n    public String getLimitPageSqlWithMark(String sql) {",
    ),
    (
        "    @Override\n    public String getLimitPageSql(String sql, int pageNo, int pageSize) {",
        "    /** 将分页参数直接拼入 SQL 的 LIMIT 子句。 */\n"
        "    @Override\n    public String getLimitPageSql(String sql, int pageNo, int pageSize) {",
    ),
    (
        "    @Override\n    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {",
        "    /** 按绝对偏移量拼接 LIMIT 分页 SQL。 */\n"
        "    @Override\n    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {",
    ),
    (
        "    @Override\n    public String[] getReturnPrimaryKeys() {",
        "    /** 返回插入后需回填的主键列名（小写形式）。 */\n"
        "    @Override\n    public String[] getReturnPrimaryKeys() {",
    ),
]

# --- TrustedPostgresqFunctionEnum ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-base/src/main/java/com/alibaba/nacos/plugin/datasource/impl/enums/TrustedPostgresqFunctionEnum.java"] = [
    (
        "/**\n * The TrustedSqlFunctionEnum enum class is used to enumerate and manage a list of trusted built-in SQL functions.\n"
        " * By using this enum, you can verify whether a given SQL function is part of the trusted functions list\n"
        " * to avoid potential SQL injection risks.\n *\n * @author blake.qiu\n"
        " * @deprecated Use {@link com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum} replaced.\n */",
        "/**\n * PostgreSQL 可信内置 SQL 函数枚举（已废弃）。\n *\n"
        " * <p>白名单校验函数名，防止 Mapper 动态 SQL 拼接时引入注入风险。"
        " 请改用 {@link com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum}。</p>\n *\n"
        " * @author blake.qiu\n"
        " * @deprecated Use {@link com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum} replaced.\n */",
    ),
    (
        "    /**\n     * NOW().\n     */",
        "    /** 当前时间函数 {@code NOW()}。 */\n",
    ),
    (
        "    private static final Map<String, TrustedPostgresqFunctionEnum> LOOKUP_MAP = new HashMap<>();",
        "    /** 函数名 → 枚举项的快速查找表。 */\n"
        "    private static final Map<String, TrustedPostgresqFunctionEnum> LOOKUP_MAP = new HashMap<>();",
    ),
    (
        "    static {\n        for (TrustedPostgresqFunctionEnum entry : TrustedPostgresqFunctionEnum.values()) {",
        "    // 启动时填充函数名索引\n    static {\n        for (TrustedPostgresqFunctionEnum entry : TrustedPostgresqFunctionEnum.values()) {",
    ),
    (
        "    /**\n     * Get the function name.\n     *\n     * @param functionName function name\n     * @return function\n     */",
        "    /**\n     * 按函数名返回对应 SQL 函数片段。\n     *\n     * @param functionName function name\n     * @return function\n     */",
    ),
    (
        "        throw new IllegalArgumentException(",
        "        // 非白名单函数名直接拒绝\n        throw new IllegalArgumentException(",
    ),
]

# --- AbstractMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/AbstractMapperByDerby.java"] = [
    (
        "/**\n * The abstract derby mapper contains CRUD methods.\n *\n * @author blake.qiu\n **/",
        "/**\n * Derby 数据源 Mapper 抽象基类。\n *\n"
        " * <p>继承 {@link com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper}，"
        " 通过 {@link com.alibaba.nacos.plugin.datasource.impl.enums.derby.TrustedDerbylFunctionEnum} "
        "解析 Derby 可信 SQL 函数。</p>\n *\n * @author blake.qiu\n **/",
    ),
    (
        "    @Override\n    public String getFunction(String functionName) {",
        "    /** 从 Derby 可信函数白名单解析 SQL 函数片段。 */\n"
        "    @Override\n    public String getFunction(String functionName) {",
    ),
]

# --- AiResourceMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/AiResourceMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of {@link AiResourceMapper}.\n *\n * @author nacos\n */",
        "/**\n * {@link AiResourceMapper} 的 Derby 实现。\n *\n"
        " * <p>使用 Derby {@code OFFSET … ROWS FETCH NEXT … ROWS ONLY} 语法分页查询 AI 资源表。</p>\n *\n"
        " * @author nacos\n */",
    ),
    (
        "    @Override\n    public MapperResult findAiResourceFetchRows(MapperContext context) {",
        "    /** 按命名空间与扩展条件分页查询 AI 资源列表。 */\n"
        "    @Override\n    public MapperResult findAiResourceFetchRows(MapperContext context) {",
    ),
    (
        "        appendExtraQueryCondition(where, context);",
        "        // 追加可选扩展查询条件\n        appendExtraQueryCondition(where, context);",
    ),
    (
        "    @Override\n    public void appendSingleAndCondition(WhereBuilder where, String field, Object value,\n        boolean likeMatch) {",
        "    /** 向 WHERE 追加单字段条件，支持等值、LIKE 及 IN 列表匹配。 */\n"
        "    @Override\n    public void appendSingleAndCondition(WhereBuilder where, String field, Object value,\n        boolean likeMatch) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- AiResourceVersionMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/AiResourceVersionMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of {@link AiResourceVersionMapper}.\n *\n * @author nacos\n */",
        "/**\n * {@link AiResourceVersionMapper} 的 Derby 实现。\n *\n"
        " * <p>按命名空间、资源名及可选 type/status/version 过滤，"
        " 按修改时间倒序分页查询 AI 资源版本。</p>\n *\n * @author nacos\n */",
    ),
    (
        "    @Override\n    public MapperResult findAiResourceVersionFetchRows(MapperContext context) {",
        "    /** 分页查询 AI 资源版本记录。 */\n"
        "    @Override\n    public MapperResult findAiResourceVersionFetchRows(MapperContext context) {",
    ),
    (
        "        Object type = context.getWhereParameter(FieldConstant.TYPE);",
        "        // 可选：按资源类型过滤\n        Object type = context.getWhereParameter(FieldConstant.TYPE);",
    ),
    (
        "        Object status = context.getWhereParameter(FieldConstant.STATUS);",
        "        // 可选：按发布状态过滤\n        Object status = context.getWhereParameter(FieldConstant.STATUS);",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- ConfigInfoBetaMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/ConfigInfoBetaMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of ConfigInfoBetaMapper.\n *\n * @author hyx\n **/",
        "/**\n * {@link ConfigInfoBetaMapper} 的 Derby 实现。\n *\n"
        " * <p>Beta 配置全量导出时使用子查询 + 关联方式分页，避免 Derby 大结果集一次性加载。</p>\n *\n * @author hyx\n **/",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoBetaForDumpAllFetchRows(MapperContext context) {",
        "    /** 分页拉取 Beta 配置用于全量 dump 导出。 */\n"
        "    @Override\n    public MapperResult findAllConfigInfoBetaForDumpAllFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- ConfigInfoGrayMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/ConfigInfoGrayMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of ConfigInfoGrayMapper.\n *\n * @author rong\n **/",
        "/**\n * {@link ConfigInfoGrayMapper} 的 Derby 实现。\n *\n"
        " * <p>灰度配置表的 dump 分页与增量变更拉取，使用 Derby 标准 OFFSET/FETCH 语法。</p>\n *\n * @author rong\n **/",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoGrayForDumpAllFetchRows(MapperContext context) {",
        "    /** 分页拉取灰度配置用于全量 dump。 */\n"
        "    @Override\n    public MapperResult findAllConfigInfoGrayForDumpAllFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findChangeConfig(MapperContext context) {",
        "    /** 按修改时间与上次最大 id 增量拉取灰度配置变更。 */\n"
        "    @Override\n    public MapperResult findChangeConfig(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- ConfigInfoMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/ConfigInfoMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of ConfigInfoMapper.\n *\n * @author hyx\n **/",
        "/**\n * {@link ConfigInfoMapper} 的 Derby 实现。\n *\n"
        " * <p>配置中心核心表的 Derby SQL 方言适配：LIKE 转义、OFFSET/FETCH 分页及模糊查询。</p>\n *\n * @author hyx\n **/",
    ),
    (
        "    private static final String SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE = \" ESCAPE '\\\\' \";",
        "    /** Derby LIKE 子句反斜杠转义后缀。 */\n"
        "    private static final String SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE = \" ESCAPE '\\\\' \";",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {",
        "    /** 按租户与应用名分页查询配置。 */\n"
        "    @Override\n    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult getTenantIdList(MapperContext context) {",
        "    /** 分页获取非默认命名空间的租户 id 列表。 */\n"
        "    @Override\n    public MapperResult getTenantIdList(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult getGroupIdList(MapperContext context) {",
        "    /** 分页获取默认命名空间下的 group_id 列表。 */\n"
        "    @Override\n    public MapperResult getGroupIdList(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigKey(MapperContext context) {",
        "    /** 分页拉取配置键（data_id/group_id/app_name）。 */\n"
        "    @Override\n    public MapperResult findAllConfigKey(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {",
        "    /** 分页拉取基础配置内容（含 md5）。 */\n"
        "    @Override\n    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoFragment(MapperContext context) {",
        "    /** 按 id 游标分页拉取配置片段，可按需包含 content。 */\n"
        "    @Override\n    public MapperResult findAllConfigInfoFragment(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findChangeConfigFetchRows(MapperContext context) {",
        "    /** 按多条件与时间范围分页查询变更配置。 */\n"
        "    @Override\n    public MapperResult findChangeConfigFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {",
        "    /** 分页列出配置的 group 键与 md5 摘要。 */\n"
        "    @Override\n    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {",
        "    /** 默认命名空间下按 dataId/group/content 模糊分页查询。 */\n"
        "    @Override\n    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
        "    /** 控制台分页精确查询配置详情。 */\n"
        "    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {",
        "    /** 按 group 与 tenant 分页查询基础配置。 */\n"
        "    @Override\n    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {",
        "    /** 模糊查询配置总数（供分页计数）。 */\n"
        "    @Override\n    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
        "    /** 模糊分页查询配置列表。 */\n"
        "    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {",
        "    /** 按租户模糊匹配分页拉取全部配置。 */\n"
        "    @Override\n    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findChangeConfig(MapperContext context) {",
        "    /** 增量拉取配置变更（按 gmt_modified 与 id 游标）。 */\n"
        "    @Override\n    public MapperResult findChangeConfig(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- ConfigInfoTagMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/ConfigInfoTagMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of ConfigInfoTagMapper.\n *\n * @author hyx\n **/",
        "/**\n * {@link ConfigInfoTagMapper} 的 Derby 实现。\n *\n"
        " * <p>配置标签表全量 dump 分页查询，采用子查询关联主表方式。</p>\n *\n * @author hyx\n **/",
    ),
    (
        "    @Override\n    public MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context) {",
        "    /** 分页拉取带标签配置用于全量 dump。 */\n"
        "    @Override\n    public MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- ConfigInfoTagsRelationMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/ConfigInfoTagsRelationMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of ConfigTagsRelationMapper.\n *\n * @author hyx\n **/",
        "/**\n * {@link ConfigTagsRelationMapper} 的 Derby 实现。\n *\n"
        " * <p>配置与标签关联查询；Derby 不支持 GROUP_CONCAT，SELECT 不含聚合标签列。</p>\n *\n * @author hyx\n **/",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
        "    /** 按标签 IN 条件精确分页查询配置。 */\n"
        "    @Override\n    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {",
        "    /** 标签模糊匹配下的配置总数统计。 */\n"
        "    @Override\n    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
        "    /** 标签模糊匹配分页查询配置列表。 */\n"
        "    @Override\n    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- ConfigMigrateMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/ConfigMigrateMapperByDerby.java"] = [
    (
        "/**\n * The type Config migrate mapper by derby.\n *\n * @author Sunrisea\n */",
        "/**\n * {@link ConfigMigrateMapper} 的 Derby 实现。\n *\n"
        " * <p>空 tenant 到 public 命名空间的配置/灰度配置迁移："
        " 识别待插入与待更新记录，并生成 INSERT … SELECT 语句。</p>\n *\n * @author Sunrisea\n */",
    ),
    (
        "    @Override\n    public MapperResult findConfigIdNeedInsertMigrate(MapperContext context) {",
        "    /** 分页查找需从空 tenant 插入 public 的配置 id。 */\n"
        "    @Override\n    public MapperResult findConfigIdNeedInsertMigrate(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigNeedUpdateMigrate(MapperContext context) {",
        "    /** 分页查找 public 侧需同步更新的配置（md5 不一致且更新更晚）。 */\n"
        "    @Override\n    public MapperResult findConfigNeedUpdateMigrate(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigGrayIdNeedInsertMigrate(MapperContext context) {",
        "    /** 分页查找需迁移插入的灰度配置 id。 */\n"
        "    @Override\n    public MapperResult findConfigGrayIdNeedInsertMigrate(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult findConfigGrayNeedUpdateMigrate(MapperContext context) {",
        "    /** 分页查找 public 侧需同步更新的灰度配置。 */\n"
        "    @Override\n    public MapperResult findConfigGrayNeedUpdateMigrate(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult migrateConfigInsertByIds(MapperContext context) {",
        "    /** 按 id 将源配置 INSERT SELECT 到 public tenant。 */\n"
        "    @Override\n    public MapperResult migrateConfigInsertByIds(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult migrateConfigGrayInsertByIds(MapperContext context) {",
        "    /** 按 id 将源灰度配置 INSERT SELECT 到 public tenant。 */\n"
        "    @Override\n    public MapperResult migrateConfigGrayInsertByIds(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]

# --- GroupCapacityMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/GroupCapacityMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of {@link GroupCapacityMapper}.\n *\n * @author lixiaoshuang\n */",
        "/**\n * {@link GroupCapacityMapper} 的 Derby 实现。\n *\n"
        " * <p>管理默认命名空间下 group 级配置容量：配额、用量增减及从 config_info 统计回填。</p>\n *\n"
        " * @author lixiaoshuang\n */",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
    (
        "    @Override\n    public MapperResult selectGroupInfoBySize(MapperContext context) {",
        "    /** 按 id 游标分页扫描 group 容量表。 */\n"
        "    @Override\n    public MapperResult selectGroupInfoBySize(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult select(MapperContext context) {",
        "    /** 按 group_id 查询容量配额与用量。 */\n"
        "    @Override\n    public MapperResult select(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult incrementUsageByWhere(MapperContext context) {",
        "    /** 无条件将指定 group 用量 +1。 */\n"
        "    @Override\n    public MapperResult incrementUsageByWhere(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult incrementUsageByWhereQuotaNotEqualZero(MapperContext context) {",
        "    /** 配额非零且未超限时将用量 +1。 */\n"
        "    @Override\n    public MapperResult incrementUsageByWhereQuotaNotEqualZero(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult insertIntoSelect(MapperContext context) {",
        "    /** 从 config_info 统计 count 并插入新 group 容量行。 */\n"
        "    @Override\n    public MapperResult insertIntoSelect(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult insertIntoSelectByWhere(MapperContext context) {",
        "    /** 按 group 与默认 tenant 统计后插入容量行。 */\n"
        "    @Override\n    public MapperResult insertIntoSelectByWhere(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult incrementUsageByWhereQuotaEqualZero(MapperContext context) {",
        "    /** 配额为零时按 max_size 上限递增用量。 */\n"
        "    @Override\n    public MapperResult incrementUsageByWhereQuotaEqualZero(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult decrementUsageByWhere(MapperContext context) {",
        "    /** 用量大于零时将指定 group 用量 -1。 */\n"
        "    @Override\n    public MapperResult decrementUsageByWhere(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult updateUsage(MapperContext context) {",
        "    /** 按全表 config_info 计数校正 group 用量。 */\n"
        "    @Override\n    public MapperResult updateUsage(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult updateUsageByWhere(MapperContext context) {",
        "    /** 按 group 与默认 tenant 统计校正用量。 */\n"
        "    @Override\n    public MapperResult updateUsageByWhere(MapperContext context) {",
    ),
]

# --- HistoryConfigInfoMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/HistoryConfigInfoMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of ConfigInfoMapper.\n *\n * @author hyx\n **/",
        "/**\n * {@link HistoryConfigInfoMapper} 的 Derby 实现。\n *\n"
        " * <p>配置历史表 his_config_info 的清理、分页查询与删除事件增量同步。</p>\n *\n * @author hyx\n **/",
    ),
    (
        "    @Override\n    public MapperResult removeConfigHistory(MapperContext context) {",
        "    /** 分批删除早于指定时间的过期历史记录。 */\n"
        "    @Override\n    public MapperResult removeConfigHistory(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult pageFindConfigHistoryFetchRows(MapperContext context) {",
        "    /** 按 dataId/group/tenant 倒序分页查询配置变更历史。 */\n"
        "    @Override\n    public MapperResult pageFindConfigHistoryFetchRows(MapperContext context) {",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
    (
        "    @Override\n    public MapperResult findDeletedConfig(MapperContext context) {",
        "    /** 增量拉取删除类型（op_type='D'）的历史配置。 */\n"
        "    @Override\n    public MapperResult findDeletedConfig(MapperContext context) {",
    ),
]

# --- TenantCapacityMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/TenantCapacityMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of TenantCapacityMapper.\n *\n * @author KiteSoar\n **/",
        "/**\n * {@link TenantCapacityMapper} 的 Derby 实现。\n *\n"
        " * <p>租户级配置容量配额管理：用量增减、校正及从 config_info 初始化容量行。</p>\n *\n * @author KiteSoar\n **/",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 数据源标识。 */\n    @Override\n    public String getDataSource() {",
    ),
    (
        "    @Override\n    public MapperResult getCapacityList4CorrectUsage(MapperContext context) {",
        "    /** 分页扫描租户容量表以批量校正用量。 */\n"
        "    @Override\n    public MapperResult getCapacityList4CorrectUsage(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult select(MapperContext context) {",
        "    /** 按 tenant_id 查询容量配额与当前用量。 */\n"
        "    @Override\n    public MapperResult select(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult incrementUsageWithDefaultQuotaLimit(MapperContext context) {",
        "    /** 配额为零时按 max_size 上限递增租户用量。 */\n"
        "    @Override\n    public MapperResult incrementUsageWithDefaultQuotaLimit(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult incrementUsageWithQuotaLimit(MapperContext context) {",
        "    /** 配额非零且未超限时将租户用量 +1。 */\n"
        "    @Override\n    public MapperResult incrementUsageWithQuotaLimit(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult incrementUsage(MapperContext context) {",
        "    /** 无条件将指定租户用量 +1。 */\n"
        "    @Override\n    public MapperResult incrementUsage(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult decrementUsage(MapperContext context) {",
        "    /** 用量大于零时将租户用量 -1。 */\n"
        "    @Override\n    public MapperResult decrementUsage(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult correctUsage(MapperContext context) {",
        "    /** 按 config_info 实际计数校正租户用量。 */\n"
        "    @Override\n    public MapperResult correctUsage(MapperContext context) {",
    ),
    (
        "    @Override\n    public MapperResult insertTenantCapacity(MapperContext context) {",
        "    /** 从 config_info 统计后 INSERT SELECT 初始化租户容量行。 */\n"
        "    @Override\n    public MapperResult insertTenantCapacity(MapperContext context) {",
    ),
]

# --- TenantInfoMapperByDerby ---

R["plugin-default-impl/nacos-default-datasource-plugin/nacos-datasource-plugin-derby/src/main/java/com/alibaba/nacos/plugin/datasource/impl/derby/TenantInfoMapperByDerby.java"] = [
    (
        "/**\n * The derby implementation of TenantInfoMapper.\n *\n * @author hyx\n **/",
        "/**\n * {@link TenantInfoMapper} 的 Derby 实现。\n *\n"
        " * <p>租户元数据 Mapper 的 Derby 绑定，CRUD SQL 由基类 {@link AbstractMapperByDerby} 提供。</p>\n *\n * @author hyx\n **/",
    ),
    (
        "    @Override\n    public String getDataSource() {",
        "    /** 返回 Derby 嵌入式数据源平台标识。 */\n    @Override\n    public String getDataSource() {",
    ),
]
