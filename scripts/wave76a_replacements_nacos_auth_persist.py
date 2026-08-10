"""Chinese annotation replacements for Nacos 3.2.3 wave76a [0:15] default auth persist/page handlers."""

R: dict[str, list[tuple[str, str]]] = {}

# --- ExternalRolePersistServiceImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/ExternalRolePersistServiceImpl.java"] = [
    (
        "/**\n * Implemetation of ExternalRolePersistServiceImpl.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 外部数据源（MySQL 等）角色持久化服务实现。\n *\n * <p>通过 {@link JdbcTemplate} 访问 {@code roles} 表，"
        " 支持分页查询、模糊搜索及角色增删；分页委托 {@link AuthExternalPaginationHelperImpl} 按数据源类型适配 SQL。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    private JdbcTemplate jt;",
        "    /** 动态数据源提供的 JDBC 模板。 */\n    private JdbcTemplate jt;",
    ),
    (
        "    private String dataSourceType = \"\";",
        "    /** 当前数据源类型，用于选择分页适配器。 */\n    private String dataSourceType = \"\";",
    ),
    (
        "    @PostConstruct\n    protected void init() {",
        "    /** 初始化 JDBC 模板与数据源类型。 */\n    @PostConstruct\n    protected void init() {",
    ),
    (
        "    @Override\n    public Page<RoleInfo> getRoles(int pageNo, int pageSize) {",
        "    /** 分页查询全部角色（按 role 去重计数）。 */\n    @Override\n    public Page<RoleInfo> getRoles(int pageNo, int pageSize) {",
    ),
    (
        "    @Override\n    public Page<RoleInfo> getRolesByUserNameAndRoleName(String username, String role, int pageNo,\n        int pageSize) {",
        "    /** 按用户名与角色名精确过滤后分页查询。 */\n    @Override\n    public Page<RoleInfo> getRolesByUserNameAndRoleName(String username, String role, int pageNo,\n        int pageSize) {",
    ),
    (
        "    /**\n     * Execute add role operation.\n     *\n     * @param role     role string value.\n     * @param userName username string value.\n     */",
        "    /**\n     * 向 {@code roles} 表插入用户-角色绑定记录。\n     *\n     * @param role     role string value.\n     * @param userName username string value.\n     */",
    ),
    (
        "    /**\n     * Execute delete role operation.\n     *\n     * @param role role string value.\n     */",
        "    /**\n     * 按角色名删除该角色的全部绑定记录。\n     *\n     * @param role role string value.\n     */",
    ),
    (
        "    /**\n     * Execute delete role operation.\n     *\n     * @param role     role string value.\n     * @param username username string value.\n     */",
        "    /**\n     * 删除指定用户与角色的单条绑定。\n     *\n     * @param role     role string value.\n     * @param username username string value.\n     */",
    ),
    (
        "    @Override\n    public List<String> findRolesLikeRoleName(String role) {",
        "    /** 按角色名模糊匹配，返回角色名列表。 */\n    @Override\n    public List<String> findRolesLikeRoleName(String role) {",
    ),
    (
        "    @Override\n    public String generateLikeArgument(String s) {",
        "    /** 将通配符 {@code *} 转为 SQL {@code %}，并转义下划线。 */\n    @Override\n    public String generateLikeArgument(String s) {",
    ),
    (
        "    @Override\n    public Page<RoleInfo> findRolesLike4Page(String username, String role, int pageNo,\n        int pageSize) {",
        "    /** 用户名与角色名模糊查询并分页。 */\n    @Override\n    public Page<RoleInfo> findRolesLike4Page(String username, String role, int pageNo,\n        int pageSize) {",
    ),
    (
        "    @Override\n    public <E> AuthPaginationHelper<E> createPaginationHelper() {",
        "    /** 创建外部数据源鉴权分页助手。 */\n    @Override\n    public <E> AuthPaginationHelper<E> createPaginationHelper() {",
    ),
]

# --- ExternalUserPersistServiceImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/ExternalUserPersistServiceImpl.java"] = [
    (
        "/**\n * Implemetation of ExternalUserPersistServiceImpl.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        "/**\n * 外部数据源用户持久化服务实现。\n *\n * <p>操作 {@code users} 表完成用户 CRUD、密码更新与分页/模糊查询；"
        " 默认新建用户 {@code enabled=true}，查询无结果时返回 {@code null}。</p>\n *\n"
        " * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
    ),
    (
        "    @PostConstruct\n    protected void init() {",
        "    /** 从动态数据源获取 JDBC 模板与类型。 */\n    @PostConstruct\n    protected void init() {",
    ),
    (
        "    /**\n     * Execute create user operation.\n     *\n     * @param username username string value.\n     * @param password password string value.\n     */",
        "    /**\n     * 创建用户并写入加密前的密码字段。\n     *\n     * @param username username string value.\n     * @param password password string value.\n     */",
    ),
    (
        "    /**\n     * Execute delete user operation.\n     *\n     * @param username username string value.\n     */",
        "    /**\n     * 按用户名删除用户记录。\n     *\n     * @param username username string value.\n     */",
    ),
    (
        "    /**\n     * Execute update user password operation.\n     *\n     * @param username username string value.\n     * @param password password string value.\n     */",
        "    /**\n     * 更新指定用户的密码。\n     *\n     * @param username username string value.\n     * @param password password string value.\n     */",
    ),
    (
        "    /**\n     * Execute find user by username operation.\n     *\n     * @param username username string value.\n     * @return User model.\n     */",
        "    /**\n     * 按用户名精确查询单个用户。\n     *\n     * @param username username string value.\n     * @return User model.\n     */",
    ),
    (
        "    @Override\n    public Page<User> getUsers(int pageNo, int pageSize, String username) {",
        "    /** 分页查询用户，可选按用户名精确过滤。 */\n    @Override\n    public Page<User> getUsers(int pageNo, int pageSize, String username) {",
    ),
    (
        "    @Override\n    public List<String> findUserLikeUsername(String username) {",
        "    /** 用户名模糊匹配，返回用户名列表。 */\n    @Override\n    public List<String> findUserLikeUsername(String username) {",
    ),
    (
        "    @Override\n    public Page<User> findUsersLike4Page(String username, int pageNo, int pageSize) {",
        "    /** 用户名模糊查询并分页返回用户实体。 */\n    @Override\n    public Page<User> findUsersLike4Page(String username, int pageNo, int pageSize) {",
    ),
    (
        "    @Override\n    public String generateLikeArgument(String s) {",
        "    /** 构造 SQL LIKE 参数字符串（通配符与转义处理）。 */\n    @Override\n    public String generateLikeArgument(String s) {",
    ),
    (
        "    @Override\n    public <E> AuthPaginationHelper<E> createPaginationHelper() {",
        "    /** 创建外部数据源分页助手实例。 */\n    @Override\n    public <E> AuthPaginationHelper<E> createPaginationHelper() {",
    ),
]

# --- PermissionInfo ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/PermissionInfo.java"] = [
    (
        "/**\n * PermissionInfo model.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * 权限信息模型：角色对资源的操作授权。\n *\n * <p>三元组 {@code role + resource + action} 对应 RBAC 中一条权限记录，"
        " 序列化后用于持久化层与 API 传输。</p>\n *\n * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    /**\n     * Role name.\n     */",
        "    /** 拥有该权限的角色名。 */",
    ),
    (
        "    /**\n     * Resource.\n     */",
        "    /** 受控资源标识（如命名空间、配置路径等）。 */",
    ),
    (
        "    /**\n     * Action on resource.\n     */",
        "    /** 对资源允许的操作（读、写、删除等）。 */",
    ),
    (
        "    public String getRole() {",
        "    /** 获取角色名。 */\n    public String getRole() {",
    ),
    (
        "    public void setRole(String role) {",
        "    /** 设置角色名。 */\n    public void setRole(String role) {",
    ),
    (
        "    public String getResource() {",
        "    /** 获取资源标识。 */\n    public String getResource() {",
    ),
    (
        "    public String getAction() {",
        "    /** 获取操作类型。 */\n    public String getAction() {",
    ),
]

# --- PermissionPersistService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/PermissionPersistService.java"] = [
    (
        "/**\n * Permission CRUD service.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * 权限持久化服务接口。\n *\n * <p>定义角色权限的分页查询、授予、撤销及模糊搜索；"
        " 实现类区分内嵌 Derby 与外部 MySQL 等数据源。</p>\n *\n * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    /**\n     * get the permissions of role by page.\n     *\n     * @param role role\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return permissions page info\n     */",
        "    /**\n     * 分页查询指定角色的权限列表。\n     *\n     * @param role role\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return permissions page info\n     */",
    ),
    (
        "    /**\n     * assign permission to role.\n     *\n     * @param role role\n     * @param resource resource\n     * @param action action\n     */",
        "    /**\n     * 为角色授予对资源的操作权限。\n     *\n     * @param role role\n     * @param resource resource\n     * @param action action\n     */",
    ),
    (
        "    /**\n     * delete the role's permission.\n     *\n     * @param role role\n     * @param resource resource\n     * @param action action\n     */",
        "    /**\n     * 撤销角色的指定资源操作权限。\n     *\n     * @param role role\n     * @param resource resource\n     * @param action action\n     */",
    ),
    (
        "    Page<PermissionInfo> findPermissionsLike4Page(String role, int pageNo, int pageSize);",
        "    /** 按角色名模糊匹配并分页返回权限。 */\n    Page<PermissionInfo> findPermissionsLike4Page(String role, int pageNo, int pageSize);",
    ),
    (
        "    String generateLikeArgument(String s);",
        "    /** 生成 SQL LIKE 模糊查询参数。 */\n    String generateLikeArgument(String s);",
    ),
    (
        "    /**\n     * create Pagination utils.\n     *\n     * @param <E> Generic object\n     * @return {@link AuthPaginationHelper}\n     */",
        "    /**\n     * 创建鉴权模块专用分页助手。\n     *\n     * @param <E> Generic object\n     * @return {@link AuthPaginationHelper}\n     */",
    ),
]

# --- RoleInfo ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/RoleInfo.java"] = [
    (
        "/**\n * Role Info.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * 角色信息模型：用户与角色的绑定关系。\n *\n * <p>{@code roles} 表一行对应一个 {@link RoleInfo}，"
        " 包含角色名与所属用户名，用于控制台展示与鉴权校验。</p>\n *\n * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    private String role;",
        "    /** 角色名称。 */\n    private String role;",
    ),
    (
        "    private String username;",
        "    /** 被分配该角色的用户名。 */\n    private String username;",
    ),
    (
        "    public String getRole() {",
        "    /** 获取角色名。 */\n    public String getRole() {",
    ),
    (
        "    public String getUsername() {",
        "    /** 获取用户名。 */\n    public String getUsername() {",
    ),
    (
        "    @Override\n    public String toString() {",
        "    /** 返回角色与用户的调试字符串。 */\n    @Override\n    public String toString() {",
    ),
]

# --- RolePersistService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/RolePersistService.java"] = [
    (
        "/**\n * Role CRUD service.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * 角色持久化服务接口。\n *\n * <p>涵盖角色分页查询、用户-角色绑定增删、模糊搜索及分页助手工厂方法；"
        " 由 Embedded/External 实现类分别对接内嵌与外部数据库。</p>\n *\n * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    /**\n     * get roles by page.\n     *\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return roles page info\n     */",
        "    /**\n     * 分页查询全部角色信息。\n     *\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return roles page info\n     */",
    ),
    (
        "    /**\n     * query the user's roles by username.\n     *\n     * @param username username\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return roles page info\n     */",
        "    /**\n     * 按用户名与角色名过滤后分页查询。\n     *\n     * @param username username\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return roles page info\n     */",
    ),
    (
        "    /**\n     * assign role to user.\n     *\n     * @param role role\n     * @param userName username\n     */",
        "    /**\n     * 为用户分配角色。\n     *\n     * @param role role\n     * @param userName username\n     */",
    ),
    (
        "    /**\n     * delete role.\n     *\n     * @param role role\n     */",
        "    /**\n     * 删除角色及其全部用户绑定。\n     *\n     * @param role role\n     */",
    ),
    (
        "    /**\n     * delete user's role.\n     *\n     * @param role role\n     * @param username username\n     */",
        "    /**\n     * 解除指定用户的角色绑定。\n     *\n     * @param role role\n     * @param username username\n     */",
    ),
    (
        "    /**\n     * fuzzy query roles by role name.\n     *\n     * @param role role\n     * @return roles\n     */",
        "    /**\n     * 按角色名模糊查询角色列表。\n     *\n     * @param role role\n     * @return roles\n     */",
    ),
    (
        "    /**\n     * Generate fuzzy search Sql.\n     *\n     * @param s origin string\n     * @return fuzzy search Sql\n     */",
        "    /**\n     * 将用户输入转为 SQL LIKE 参数。\n     *\n     * @param s origin string\n     * @return fuzzy search Sql\n     */",
    ),
    (
        "    /**.\n     * fuzzy query role information based on roleName and username\n     *\n     * @param username username of user\n     * @param pageNo page number\n     * @param pageSize page size\n     * @return {@link Page} with {@link RoleInfo} generation\n     */",
        "    /**\n     * 用户名与角色名模糊查询并分页。\n     *\n     * @param username username of user\n     * @param pageNo page number\n     * @param pageSize page size\n     * @return {@link Page} with {@link RoleInfo} generation\n     */",
    ),
]

# --- User ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/User.java"] = [
    (
        "/**\n * User.\n *\n * @author wfnuser\n */",
        "/**\n * Nacos 控制台用户实体。\n *\n * <p>映射 {@code users} 表，包含用户名与密码字段；"
        " 密码在持久化层通常已加密，供 Spring Security 认证加载。</p>\n *\n * @author wfnuser\n */",
    ),
    (
        "    private String username;",
        "    /** 登录用户名，全局唯一。 */\n    private String username;",
    ),
    (
        "    private String password;",
        "    /** 用户密码（存储格式取决于加密策略）。 */\n    private String password;",
    ),
    (
        "    public String getPassword() {",
        "    /** 获取密码字段。 */\n    public String getPassword() {",
    ),
    (
        "    public void setPassword(String password) {",
        "    /** 设置密码字段。 */\n    public void setPassword(String password) {",
    ),
    (
        "    public String getUsername() {",
        "    /** 获取用户名。 */\n    public String getUsername() {",
    ),
    (
        "    public void setUsername(String username) {",
        "    /** 设置用户名。 */\n    public void setUsername(String username) {",
    ),
]

# --- UserPersistService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/UserPersistService.java"] = [
    (
        "/**\n * User CRUD service.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * 用户持久化服务接口。\n *\n * <p>定义用户创建、删除、改密、单查与分页/模糊查询；"
        " 实现类根据部署模式选择内嵌或外部 JDBC 访问。</p>\n *\n * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    /**\n     * create user.\n     *\n     * @param username username\n     * @param password password\n     */",
        "    /**\n     * 创建新用户。\n     *\n     * @param username username\n     * @param password password\n     */",
    ),
    (
        "    /**\n     * delete user by username.\n     *\n     * @param username username\n     */",
        "    /**\n     * 按用户名删除用户。\n     *\n     * @param username username\n     */",
    ),
    (
        "    /**\n     * update password of the user.\n     *\n     * @param username username\n     * @param password password\n     */",
        "    /**\n     * 更新用户密码。\n     *\n     * @param username username\n     * @param password password\n     */",
    ),
    (
        "    /**\n     * query user by username.\n     *\n     * @param username username\n     * @return user\n     */",
        "    /**\n     * 按用户名精确查询用户。\n     *\n     * @param username username\n     * @return user\n     */",
    ),
    (
        "    /**\n     * get users by page.\n     *\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return user page info\n     */",
        "    /**\n     * 分页查询用户列表。\n     *\n     * @param pageNo pageNo\n     * @param pageSize pageSize\n     * @return user page info\n     */",
    ),
    (
        "    /**\n     * fuzzy query user by username.\n     *\n     * @param username username\n     * @return usernames\n     */",
        "    /**\n     * 用户名模糊匹配，返回用户名集合。\n     *\n     * @param username username\n     * @return usernames\n     */",
    ),
    (
        "    Page<User> findUsersLike4Page(String username, int pageNo, int pageSize);",
        "    /** 用户名模糊查询并分页返回用户实体。 */\n    Page<User> findUsersLike4Page(String username, int pageNo, int pageSize);",
    ),
]

# --- AuthEmbeddedPaginationHelperImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/embedded/AuthEmbeddedPaginationHelperImpl.java"] = [
    (
        "/**\n * Auth plugin Pagination Utils For Apache Derby.\n *\n * @param <E> Generic class\n * @author huangKeMing\n */",
        "/**\n * 内嵌 Derby 数据源鉴权分页助手。\n *\n * <p>通过 {@link DatabaseOperate} 执行计数与数据查询，"
        " 分页 SQL 由 {@link DerbyPageHandlerAdapter} 追加 OFFSET/FETCH 子句。</p>\n *\n"
        " * @param <E> Generic class\n * @author huangKeMing\n */",
    ),
    (
        "    private final DatabaseOperate databaseOperate;",
        "    /** 内嵌存储数据库操作门面。 */\n    private final DatabaseOperate databaseOperate;",
    ),
    (
        "    public AuthEmbeddedPaginationHelperImpl(DatabaseOperate databaseOperate) {",
        "    /** 注入内嵌数据库操作实例。 */\n    public AuthEmbeddedPaginationHelperImpl(DatabaseOperate databaseOperate) {",
    ),
    (
        "    /**\n     * Take paging.\n     *\n     * @param sqlCountRows Query total SQL\n     * @param sqlFetchRows Query data sql\n     * @param args         query args\n     * @param pageNo       page number\n     * @param pageSize     page size\n     * @param rowMapper    Entity mapping\n     * @return Paging data\n     */",
        "    /**\n     * 标准分页查询：先计数再拉取当前页数据。\n     *\n     * @param sqlCountRows Query total SQL\n     * @param sqlFetchRows Query data sql\n     * @param args         query args\n     * @param pageNo       page number\n     * @param pageSize     page size\n     * @param rowMapper    Entity mapping\n     * @return Paging data\n     */",
    ),
    (
        "        // Query the total number of current records",
        "        // 查询符合条件的总记录数",
    ),
    (
        "        // Count pages",
        "        // 计算总页数",
    ),
    (
        "        // Create Page object",
        "        // 构造分页结果对象",
    ),
    (
        "        // fill the sql Page args",
        "        // 追加 OFFSET/FETCH 分页参数",
    ),
    (
        "    @Override\n    public void updateLimit(final String sql, final Object[] args) {",
        "    /** 在内嵌存储上下文中执行带限流的更新 SQL。 */\n    @Override\n    public void updateLimit(final String sql, final Object[] args) {",
    ),
    (
        "    private OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
        "    /** 委托 Derby 适配器生成分页 SQL 与新参数数组。 */\n    private OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
    ),
]

# --- AuthExternalPaginationHelperImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/extrnal/AuthExternalPaginationHelperImpl.java"] = [
    (
        "/**\n * Auth plugin Pagination Utils For Apache External.\n *\n * @param <E> Generic class\n * @author huangKeMing\n */",
        "/**\n * 外部数据源鉴权分页助手。\n *\n * <p>基于 {@link JdbcTemplate} 执行分页；按 {@code dataSourceType} "
        "选择 {@link MysqlPageHandlerAdapter} 或 {@link DefaultPageHandlerAdapter} 拼接 LIMIT/OFFSET。</p>\n *\n"
        " * @param <E> Generic class\n * @author huangKeMing\n */",
    ),
    (
        "    private final JdbcTemplate jdbcTemplate;",
        "    /** Spring JDBC 模板，访问外部数据库。 */\n    private final JdbcTemplate jdbcTemplate;",
    ),
    (
        "    private volatile String dataSourceType;",
        "    /** 数据源类型标识（mysql、derby 等）。 */\n    private volatile String dataSourceType;",
    ),
    (
        "    /**\n     * Take paging.\n     *\n     * @param sqlCountRows query total SQL\n     * @param sqlFetchRows query data sql\n     * @param args         query parameters\n     * @param pageNo       page number\n     * @param pageSize     page size\n     * @param rowMapper    {@link RowMapper}\n     * @return Paginated data {@code <E>}\n     */",
        "    /**\n     * 分页查询：计数 SQL 与数据 SQL 分离，支持游标 lastMaxId 重载。\n     *\n     * @param sqlCountRows query total SQL\n     * @param sqlFetchRows query data sql\n     * @param args         query parameters\n     * @param pageNo       page number\n     * @param pageSize     page size\n     * @param rowMapper    {@link RowMapper}\n     * @return Paginated data {@code <E>}\n     */",
    ),
    (
        "        // Query the total number of current records.",
        "        // 统计当前查询条件下的总记录数",
    ),
    (
        "        // Compute pages count",
        "        // 根据总数与页大小计算页数",
    ),
    (
        "    /**\n     * Update limit with response.\n     *\n     * @param sql  sql\n     * @param args args\n     * @return update row count\n     */",
        "    /**\n     * 执行更新并返回受影响行数。\n     *\n     * @param sql  sql\n     * @param args args\n     * @return update row count\n     */",
    ),
    (
        "    /**\n     * Get handler adapter.\n     *\n     * @param dataSourceType data source type.\n     * @return\n     */",
        "    /**\n     * 按数据源类型选取分页适配器，无匹配时用默认实现。\n     *\n     * @param dataSourceType data source type.\n     * @return\n     */",
    ),
    (
        "    private OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
        "    /** 调用适配器追加数据库方言分页子句。 */\n    private OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
    ),
]

# --- PageHandlerAdapter ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/handler/PageHandlerAdapter.java"] = [
    (
        "/**\n * Auth plugin page handler adapter.\n *\n * @author huangKeMing\n */",
        "/**\n * 鉴权模块分页 SQL 适配器接口。\n *\n * <p>不同数据库方言的分页语法（MySQL LIMIT、Derby OFFSET/FETCH）"
        " 由实现类统一封装为 {@link OffsetFetchResult}。</p>\n *\n * @author huangKeMing\n */",
    ),
    (
        "    /**\n     * Determine whether the current data source supports paging.\n     *\n     * @param dataSourceType data source type\n     * @return true if the current data source supports paging\n     */",
        "    /**\n     * 判断适配器是否支持给定数据源类型。\n     *\n     * @param dataSourceType data source type\n     * @return true if the current data source supports paging\n     */",
    ),
    (
        "    /**\n     * Add offset and fetch next.\n     *\n     * @param fetchSql fetch sql.\n     * @param arg      arguments.\n     * @param pageNo   page number.\n     * @param pageSize page size.\n     * @return\n     */",
        "    /**\n     * 为查询 SQL 追加分页子句并扩展参数数组。\n     *\n     * @param fetchSql fetch sql.\n     * @param arg      arguments.\n     * @param pageNo   page number.\n     * @param pageSize page size.\n     * @return\n     */",
    ),
]

# --- PageHandlerAdapterFactory ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/handler/PageHandlerAdapterFactory.java"] = [
    (
        "/**\n * pagination factory.\n *\n * @author huangKeMing\n */",
        "/**\n * 分页适配器工厂（单例）。\n *\n * <p>启动时注册 MySQL、Derby 与默认三种 {@link PageHandlerAdapter}，"
        " 供内嵌与外部分页助手按类名或 {@link PageHandlerAdapter#supports} 选取。</p>\n *\n * @author huangKeMing\n */",
    ),
    (
        "    private final List<PageHandlerAdapter> handlerAdapters;",
        "    /** 已注册的适配器有序列表。 */\n    private final List<PageHandlerAdapter> handlerAdapters;",
    ),
    (
        "    private final Map<String, PageHandlerAdapter> handlerAdapterMap;",
        "    /** 类全名到适配器实例的映射。 */\n    private final Map<String, PageHandlerAdapter> handlerAdapterMap;",
    ),
    (
        "    public List<PageHandlerAdapter> getHandlerAdapters() {",
        "    /** 返回全部适配器列表（不可变）。 */\n    public List<PageHandlerAdapter> getHandlerAdapters() {",
    ),
    (
        "    public Map<String, PageHandlerAdapter> getHandlerAdapterMap() {",
        "    /** 返回类名到适配器的映射（不可变）。 */\n    public Map<String, PageHandlerAdapter> getHandlerAdapterMap() {",
    ),
    (
        "        // MysqlPageHandlerAdapter",
        "        // 注册 MySQL LIMIT 分页适配器",
    ),
    (
        "        // DerbyPageHandlerAdapter",
        "        // 注册 Derby OFFSET/FETCH 适配器",
    ),
    (
        "        // DefaultPageHandlerAdapter",
        "        // 注册默认（无分页改写）适配器",
    ),
    (
        "    public static PageHandlerAdapterFactory getInstance() {",
        "    /** 获取工厂单例。 */\n    public static PageHandlerAdapterFactory getInstance() {",
    ),
]

# --- DefaultPageHandlerAdapter ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/handler/support/DefaultPageHandlerAdapter.java"] = [
    (
        "/**\n * Default page handler adapter.\n *\n * @author huangKeMing\n */",
        "/**\n * 默认分页适配器（不改写 SQL）。\n *\n * <p>{@link #supports} 恒为 false，仅作为外部数据源无匹配方言时的兜底，"
        " 原样返回 SQL 与参数。</p>\n *\n * @author huangKeMing\n */",
    ),
    (
        "    @Override\n    public boolean supports(String dataSourceType) {",
        "    /** 默认适配器不匹配任何数据源类型。 */\n    @Override\n    public boolean supports(String dataSourceType) {",
    ),
    (
        "    @Override\n    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
        "    /** 不追加方言分页子句，直接封装原 SQL。 */\n    @Override\n    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
    ),
]

# --- DerbyPageHandlerAdapter ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/handler/support/DerbyPageHandlerAdapter.java"] = [
    (
        "/**\n * derby page handler adapter.\n *\n * @author huangKeMing\n */",
        "/**\n * Apache Derby 分页适配器。\n *\n * <p>在 SQL 末尾追加 {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY}，"
        " 参数为 {@code (pageNo-1)*pageSize} 与 {@code pageSize}；已含 OFFSET 时不再改写。</p>\n *\n * @author huangKeMing\n */",
    ),
    (
        "    @Override\n    public boolean supports(String dataSourceType) {",
        "    /** 仅支持 Derby 内嵌数据源。 */\n    @Override\n    public boolean supports(String dataSourceType) {",
    ),
    (
        "    @Override\n    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
        "    /** 追加 Derby 标准 OFFSET/FETCH 分页语法及占位参数。 */\n    @Override\n    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
    ),
]

# --- MysqlPageHandlerAdapter ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/persistence/handler/support/MysqlPageHandlerAdapter.java"] = [
    (
        "/**\n * mysql page handler adapter.\n *\n * @author huangKeMing\n */",
        "/**\n * MySQL 分页适配器。\n *\n * <p>在 SQL 末尾追加 {@code LIMIT ?, ?}，参数为偏移量与页大小；"
        " 若 SQL 已含 LIMIT 关键字则保持原样。</p>\n *\n * @author huangKeMing\n */",
    ),
    (
        "    @Override\n    public boolean supports(String dataSourceType) {",
        "    /** 仅支持 MySQL 外部数据源。 */\n    @Override\n    public boolean supports(String dataSourceType) {",
    ),
    (
        "    @Override\n    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
        "    /** 追加 MySQL LIMIT 子句及偏移、条数占位参数。 */\n    @Override\n    public OffsetFetchResult addOffsetAndFetchNext(String fetchSql, Object[] arg, int pageNo,\n        int pageSize) {",
    ),
]
