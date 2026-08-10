"""Chinese annotation replacements for Nacos 3.2.3 wave76b [15:30] default auth roles/token/users."""

R: dict[str, list[tuple[str, str]]] = {}

# --- AbstractCachedRoleService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/AbstractCachedRoleService.java"] = [
    (
        "/**\n * Nacos abstract cached role service.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 角色服务抽象基类：定时从持久层加载角色与权限并维护内存缓存。\n *\n"
        " * <p>子类实现 {@link #getAllRoles()} 等数据访问；{@link #reload()} 每 15 秒刷新缓存。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    protected static final int DEFAULT_PAGE_NO = 1;",
        "    /** 分页查询默认页码。 */\n    protected static final int DEFAULT_PAGE_NO = 1;",
    ),
    (
        "    private volatile Set<String> roleSet = new ConcurrentHashSet<>();",
        "    /** 已加载的全部角色名集合。 */\n    private volatile Set<String> roleSet = new ConcurrentHashSet<>();",
    ),
    (
        "    private volatile Map<String, List<RoleInfo>> roleInfoMap = new ConcurrentHashMap<>();",
        "    /** 用户名 → 角色绑定列表。 */\n    private volatile Map<String, List<RoleInfo>> roleInfoMap = new ConcurrentHashMap<>();",
    ),
    (
        "    private volatile Map<String, List<PermissionInfo>> permissionInfoMap =\n        new ConcurrentHashMap<>();",
        "    /** 角色名 → 权限列表。 */\n    private volatile Map<String, List<PermissionInfo>> permissionInfoMap =\n        new ConcurrentHashMap<>();",
    ),
    (
        "    @Scheduled(initialDelay = 5000, fixedDelay = 15000)\n    protected void reload() {",
        "    /** 定时刷新角色与权限缓存（启动 5 秒后首次，之后每 15 秒）。 */\n"
        "    @Scheduled(initialDelay = 5000, fixedDelay = 15000)\n    protected void reload() {",
    ),
    (
        "            List<RoleInfo> roleInfoPage = getAllRoles();",
        "            // 拉取全部角色绑定并重建用户→角色映射\n            List<RoleInfo> roleInfoPage = getAllRoles();",
    ),
    (
        "            for (String role : tmpRoleSet) {",
        "            // 为每个角色加载全部权限\n            for (String role : tmpRoleSet) {",
    ),
]

# --- AbstractCheckedRoleService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/AbstractCheckedRoleService.java"] = [
    (
        "/**\n * Nacos abstract cached role service.\n *\n * @author xiweng.yy\n */",
        "/**\n * 带权限校验的角色服务抽象类：在缓存基类之上实现 {@link #hasPermission} 等鉴权逻辑。\n *\n"
        " * <p>支持 GLOBAL_ADMIN 放行、控制台资源限制及通配符权限匹配。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public boolean hasPermission(NacosUser nacosUser, Permission permission) {",
        "    /** 判断用户是否拥有指定资源权限（任一角色匹配即通过）。 */\n"
        "    @Override\n    public boolean hasPermission(NacosUser nacosUser, Permission permission) {",
    ),
    (
        "        // Global admin pass:",
        "        // 全局管理员直接放行并标记 globalAdmin",
    ),
    (
        "        // Old global admin can pass resource 'console/':",
        "        // 非全局管理员禁止访问 console/ 前缀资源",
    ),
    (
        "        // For other roles, use a pattern match to decide if pass or not.",
        "        // 其他角色：按资源通配符与 action 子串匹配",
    ),
    (
        "    /**\n     * Reject deletion or manual creation of system-reserved roles.\n     *\n     * @param role role name to check\n     */",
        "    /**\n     * 拒绝删除或手动创建系统保留角色（GLOBAL_ADMIN、ANONYMOUS）。\n     *\n     * @param role role name to check\n     */",
    ),
    (
        "    /**\n     * If API is update user password, don't do permission check, because there is permission check in API logic.\n     */",
        "    /** 修改密码 API 跳过权限校验（业务层已校验）。 */\n",
    ),
    (
        "    private String joinResource(Resource resource) {",
        "    /** 将 {@link Resource} 拼接为权限匹配用的 namespace:group:type/name 字符串。 */\n"
        "    private String joinResource(Resource resource) {",
    ),
]

# --- NacosRoleService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/NacosRoleService.java"] = [
    (
        "/**\n * Nacos auth plugin role service interface.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 默认鉴权插件角色服务接口。\n *\n"
        " * <p>涵盖角色 CRUD、权限绑定、分页/模糊查询及全局管理员判定。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Determine if the user has permission of the resource.\n     *\n"
        "     * <p>Note if the user has many roles, this method returns true if any one role of the user has the desired\n"
        "     * permission.\n     *\n     * @param nacosUser  user info\n     * @param permission permission to auth\n"
        "     * @return true if granted, false otherwise\n     */",
        "    /**\n     * 判断用户是否拥有指定资源权限。\n     *\n"
        "     * <p>用户多角色时任一角色匹配即返回 true。</p>\n     *\n     * @param nacosUser  user info\n"
        "     * @param permission permission to auth\n     * @return true if granted, false otherwise\n     */",
    ),
    (
        "    /**\n     * Add permission to tole.\n     *\n     * @param role     role name\n     * @param resource resource\n     * @param action   action\n     */",
        "    /**\n     * 为角色新增权限。\n     *\n     * @param role     role name\n     * @param resource resource\n     * @param action   action\n     */",
    ),
    (
        "    /**\n     * Delete permission from role.\n     *\n     * @param role     role name\n     * @param resource resource\n     * @param action   action\n     */",
        "    /**\n     * 删除角色的指定权限。\n     *\n     * @param role     role name\n     * @param resource resource\n     * @param action   action\n     */",
    ),
    (
        "    /**\n     * Get all permissions of the role.\n     *\n     * @param role role name\n     * @return List of {@link PermissionInfo} for the role\n     */",
        "    /**\n     * 获取角色的全部权限。\n     *\n     * @param role role name\n     * @return List of {@link PermissionInfo} for the role\n     */",
    ),
    (
        "    /**\n     * Accurate search permissions by role name pattern.\n     *\n     * @param role      role name pattern\n"
        "     * @param pageNo    page number\n     * @param pageSize  page size\n"
        "     * @return List of {@link RoleInfo} match role name pattern\n     */",
        "    /**\n     * 按角色名精确分页查询权限。\n     *\n     * @param role      role name pattern\n"
        "     * @param pageNo    page number\n     * @param pageSize  page size\n"
        "     * @return List of {@link RoleInfo} match role name pattern\n     */",
    ),
    (
        "    /**\n     * Blur search permissions by role name pattern.\n     *\n     * @param role      role name pattern\n"
        "     * @param pageNo    page number\n     * @param pageSize  page size\n"
        "     * @return List of {@link RoleInfo} match role name pattern\n     */",
        "    /**\n     * 按角色名模糊分页查询权限。\n     *\n     * @param role      role name pattern\n"
        "     * @param pageNo    page number\n     * @param pageSize  page size\n"
        "     * @return List of {@link RoleInfo} match role name pattern\n     */",
    ),
    (
        "    /**\n     * Judge whether the permission is duplicate.\n     *\n     * @param role role name\n"
        "     * @param resource resource\n     * @param action action\n     * @return true if duplicate, false otherwise\n     */",
        "    /**\n     * 判断权限是否重复（含 rw 通配动作）。\n     *\n     * @param role role name\n"
        "     * @param resource resource\n     * @param action action\n     * @return true if duplicate, false otherwise\n     */",
    ),
    (
        "    /**\n     * Get All roles for target user.\n     *\n     * @param username username of target user\n"
        "     * @return List of {@link RoleInfo} for target user\n     */",
        "    /**\n     * 获取目标用户的全部角色绑定。\n     *\n     * @param username username of target user\n"
        "     * @return List of {@link RoleInfo} for target user\n     */",
    ),
    (
        "    /**\n     * Add role to user.\n     *\n     * @param role     role name\n     * @param username user name\n     */",
        "    /**\n     * 为用户绑定角色。\n     *\n     * @param role     role name\n     * @param username user name\n     */",
    ),
    (
        "    /**\n     * Check if user has admin role.\n     *\n     * @param userName user name\n     * @return true if user has admin role.\n     */",
        "    /**\n     * 判断用户是否拥有 GLOBAL_ADMIN 角色。\n     *\n     * @param userName user name\n     * @return true if user has admin role.\n     */",
    ),
    (
        "    /**\n     * Check if all user has at least one admin role.\n     *\n     * @return true if all user has at least one admin role.\n     */",
        "    /**\n     * 判断系统中是否已存在全局管理员角色。\n     *\n     * @return true if all user has at least one admin role.\n     */",
    ),
]

# --- NacosRoleServiceDirectImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/NacosRoleServiceDirectImpl.java"] = [
    (
        "/**\n * Nacos builtin role service, implemented by directly access to database.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * Nacos 内置角色服务：直连数据库持久层实现。\n *\n"
        " * <p>启用缓存时优先读 {@link AbstractCachedRoleService} 内存映射，否则回源 "
        "{@link RolePersistService}/{@link PermissionPersistService}。</p>\n *\n"
        " * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    @Override\n    public List<RoleInfo> getRoles(String username) {",
        "    /** 获取用户角色列表，缓存未命中时查库并回填。 */\n"
        "    @Override\n    public List<RoleInfo> getRoles(String username) {",
    ),
    (
        "    @Override\n    public void addRole(String role, String username) {",
        "    /** 绑定角色：校验用户存在、禁止保留角色、防重复绑定。 */\n"
        "    @Override\n    public void addRole(String role, String username) {",
    ),
    (
        "    @Override\n    public void addAdminRole(String username) {",
        "    /** 创建首个 GLOBAL_ADMIN 并绑定指定用户。 */\n"
        "    @Override\n    public void addAdminRole(String username) {",
    ),
    (
        "    @Override\n    public void addPermission(String role, String resource, String action) {",
        "    /** 为已存在角色新增权限。 */\n"
        "    @Override\n    public void addPermission(String role, String resource, String action) {",
    ),
    (
        "    boolean isUserBoundToRole(String role, String username) {",
        "    /** 判断用户是否已绑定指定角色。 */\n    boolean isUserBoundToRole(String role, String username) {",
    ),
]

# --- NacosRoleServiceRemoteImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/roles/NacosRoleServiceRemoteImpl.java"] = [
    (
        "/**\n * Nacos builtin role service, implemented by remote request to nacos server.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 角色服务远程实现：通过 HTTP 调用服务端 V3 角色/权限 API。\n *\n"
        " * <p>适用于 Console 等独立进程，本地仅维护缓存与远程转发。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public void addPermission(String role, String resource, String action) {",
        "    /** 远程 POST 新增角色权限。 */\n    @Override\n    public void addPermission(String role, String resource, String action) {",
    ),
    (
        "    @Override\n    public List<PermissionInfo> getPermissions(String role) {",
        "    /** 优先读缓存，未命中则触发 reload 后返回。 */\n"
        "    @Override\n    public List<PermissionInfo> getPermissions(String role) {",
    ),
    (
        "    @Override\n    public void addAdminRole(String username) {",
        "    /** 远程场景下管理员角色由创建用户流程同步，此处仅更新本地缓存标记。 */\n"
        "    @Override\n    public void addAdminRole(String username) {",
    ),
    (
        "        // if has global admin role, means already synced admin role to console cached.",
        "        // 已有全局管理员说明 Console 缓存已同步",
    ),
    (
        "        // No need to call add admin role. In {@link NacosUserServiceRemoteImpl#createUser},",
        "        // 无需远程调用：{@link NacosUserServiceRemoteImpl#createUser} 会创建管理员",
    ),
    (
        "    private String buildRemotePermissionUrlPath(String apiPath) {",
        "    /** 拼接远程权限 API 完整 URL。 */\n    private String buildRemotePermissionUrlPath(String apiPath) {",
    ),
    (
        "    private Page<PermissionInfo> getPermissionInfoPageFromRemote(Query query) {",
        "    /** GET 远程权限列表并反序列化为分页结果。 */\n"
        "    private Page<PermissionInfo> getPermissionInfoPageFromRemote(Query query) {",
    ),
]

# --- TokenManager ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/TokenManager.java"] = [
    (
        "/**\n * Token Manager Interface.\n *\n * @author majorhe\n */",
        "/**\n * JWT 令牌管理器接口。\n *\n"
        " * <p>负责签发、校验、解析令牌及查询有效期/TTL。</p>\n *\n * @author majorhe\n */",
    ),
    (
        "    /**\n     * Create token.\n     *\n     * @param authentication auth info\n     * @return token\n     * @throws AccessException access exception\n     */",
        "    /**\n     * 根据 Spring Security 认证信息签发令牌。\n     *\n     * @param authentication auth info\n     * @return token\n     * @throws AccessException access exception\n     */",
    ),
    (
        "    /**\n     * Get auth Info.\n     *\n     * @param token token\n     * @return auth info\n     * @throws AccessException access exception\n     */",
        "    /**\n     * 从令牌还原 Spring Security {@link Authentication}。\n     *\n     * @param token token\n     * @return auth info\n     * @throws AccessException access exception\n     */",
    ),
    (
        "    /**\n     * validate token.\n     *\n     * @param token token\n     * @throws AccessException access exception\n     */",
        "    /**\n     * 校验令牌有效性。\n     *\n     * @param token token\n     * @throws AccessException access exception\n     */",
    ),
    (
        "    /**\n     * parse token.\n     *\n     * @param token token\n     * @return nacos user object\n     * @throws AccessException access exception\n     */",
        "    /**\n     * 解析令牌为 {@link NacosUser}。\n     *\n     * @param token token\n     * @return nacos user object\n     * @throws AccessException access exception\n     */",
    ),
    (
        "    /**\n     * validate token.\n     *\n     * @return  token validity in seconds\n     * @throws AccessException access exception\n     */",
        "    /**\n     * 获取配置的令牌默认有效期（秒）。\n     *\n     * @return  token validity in seconds\n     * @throws AccessException access exception\n     */",
    ),
    (
        "    /**\n     * validate token.\n     *\n     * @param token token\n     * @return token ttl in seconds\n     * @throws AccessException access exception\n     */",
        "    /**\n     * 获取指定令牌剩余 TTL（秒）。\n     *\n     * @param token token\n     * @return token ttl in seconds\n     * @throws AccessException access exception\n     */",
    ),
]

# --- TokenManagerDelegate ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/TokenManagerDelegate.java"] = [
    (
        "/**\n * token manager delegate.\n *\n * @author majorhe\n */",
        "/**\n * {@link TokenManager} 委托包装：统一转发至实际实现（如 {@link CachedJwtTokenManager}）。\n *\n"
        " * @author majorhe\n */",
    ),
    (
        "    public static final String NACOS_AUTH_TOKEN_CACHING_ENABLED =\n        \"nacos.core.auth.plugin.nacos.token.cache.enable\";",
        "    /** 是否启用 JWT 令牌本地缓存的配置项键名。 */\n"
        "    public static final String NACOS_AUTH_TOKEN_CACHING_ENABLED =\n        \"nacos.core.auth.plugin.nacos.token.cache.enable\";",
    ),
    (
        "    public TokenManagerDelegate(TokenManager delegate) {",
        "    /** 注入底层 TokenManager 实现。 */\n    public TokenManagerDelegate(TokenManager delegate) {",
    ),
    (
        "    private TokenManager getExecuteTokenManager() {",
        "    /** 返回实际执行的 TokenManager。 */\n    private TokenManager getExecuteTokenManager() {",
    ),
]

# --- CachedJwtTokenManager ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/impl/CachedJwtTokenManager.java"] = [
    (
        "/**\n * Cached JWT token manager.\n *\n * @author majorhe\n */",
        "/**\n * 带本地缓存的 JWT 令牌管理器。\n *\n"
        " * <p>按 token 与 username 双索引缓存解析结果，定时清理过期项；临近过期时自动刷新。</p>\n *\n"
        " * @author majorhe\n */",
    ),
    (
        "    /**\n     * key: token string, value: token entity.\n     */",
        "    /** token 字符串 → 缓存实体。 */\n",
    ),
    (
        "    /**\n     * key: username, value: token entity. cache token created by self.\n     */",
        "    /** 用户名 → 本机签发的 token 实体。 */\n",
    ),
    (
        "    @Scheduled(initialDelay = 30000, fixedDelay = 60000)\n    private void cleanExpiredToken() {",
        "    /** 每分钟扫描并移除过期 token/user 缓存。 */\n"
        "    @Scheduled(initialDelay = 30000, fixedDelay = 60000)\n    private void cleanExpiredToken() {",
    ),
    (
        "    public String createToken(String username) throws AccessException {",
        "    /** 签发或复用未临近过期的缓存 token。 */\n    public String createToken(String username) throws AccessException {",
    ),
    (
        "        // jwtTokenManager.validateToken(token) will throw runtime exception if token invalid",
        "        // 无效 token 时 jwtTokenManager 会抛异常",
    ),
    (
        "        // if token valid",
        "        // 校验通过后回填缓存",
    ),
    (
        "    private boolean needRefresh(long expiredTimeMills) {",
        "    /** 剩余有效期不足 1/10 时需刷新 token。 */\n    private boolean needRefresh(long expiredTimeMills) {",
    ),
    (
        "    static class TokenEntity {",
        "    /** 内存缓存的 token 元数据（认证信息、用户、过期时间）。 */\n    static class TokenEntity {",
    ),
]

# --- JwtTokenManager ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/token/impl/JwtTokenManager.java"] = [
    (
        "/**\n * JWT token manager.\n *\n * @author wfnuser\n * @author nkorange\n */",
        "/**\n * 基于 {@link NacosJwtParser} 的 JWT 令牌管理器。\n *\n"
        " * <p>监听 {@link ServerConfigChangeEvent} 热更新密钥与过期时间；鉴权关闭时返回占位 token。</p>\n *\n"
        " * @author wfnuser\n * @author nkorange\n */",
    ),
    (
        "    private static final String AUTH_DISABLED_TOKEN = \"AUTH_DISABLED\";",
        "    /** 鉴权关闭时的占位 token 字符串。 */\n    private static final String AUTH_DISABLED_TOKEN = \"AUTH_DISABLED\";",
    ),
    (
        "    /**\n     * Token validity time(seconds).\n     */",
        "    /** 令牌默认有效期（秒）。 */\n",
    ),
    (
        "    private void processProperties() {",
        "    /** 从环境变量加载过期时间与 Base64 密钥并初始化解析器。 */\n    private void processProperties() {",
    ),
    (
        "        // create a token when auth enabled or nacos.core.auth.plugin.nacos.token.secret.key is configured",
        "        // 鉴权开启或已配置密钥时才签发真实 JWT",
    ),
    (
        "            // check nacos.core.auth.plugin.nacos.token.secret.key only if auth enabled",
        "            // 鉴权开启时必须校验密钥已配置",
    ),
    (
        "    public NacosUser parseToken(String token) throws AccessException {",
        "    /** 解析 JWT 为 NacosUser，未配置密钥时抛错。 */\n    public NacosUser parseToken(String token) throws AccessException {",
    ),
    (
        "    private void checkJwtParser() {",
        "    /** 鉴权开启时校验 jwtParser 已初始化。 */\n    private void checkJwtParser() {",
    ),
]

# --- AbstractCachedUserService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/AbstractCachedUserService.java"] = [
    (
        "/**\n * Nacos abstract cached user service.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 用户服务抽象基类：定时加载用户列表至内存 Map。\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private volatile Map<String, User> userMap = new ConcurrentHashMap<>();",
        "    /** 用户名 → 用户实体缓存。 */\n    private volatile Map<String, User> userMap = new ConcurrentHashMap<>();",
    ),
    (
        "    @Scheduled(initialDelay = 5000, fixedDelay = 15000)\n    protected void reload() {",
        "    /** 每 15 秒全量刷新用户缓存。 */\n"
        "    @Scheduled(initialDelay = 5000, fixedDelay = 15000)\n    protected void reload() {",
    ),
    (
        "    /**\n     * Reject reserved system usernames from being created or deleted.\n     *\n     * @param username the username to check\n     */",
        "    /**\n     * 禁止创建或删除系统保留用户名（如 ANONYMOUS）。\n     *\n     * @param username the username to check\n     */",
    ),
    (
        "    /**\n     * [ISSUE #13625] check username and password is blank.\n     */",
        "    /** 校验用户名与密码非空，并拒绝保留用户名。 */\n",
    ),
]

# --- NacosUser ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUser.java"] = [
    (
        "/**\n * Nacos User.\n *\n * @author nkorange\n * @since 1.2.0\n */",
        "/**\n * 鉴权上下文中的 Nacos 用户模型。\n *\n"
        " * <p>扩展持久层 {@link User}，附加 JWT token 与是否全局管理员标记。</p>\n *\n"
        " * @author nkorange\n * @since 1.2.0\n */",
    ),
    (
        "    private String token;",
        "    /** 当前会话 JWT 令牌。 */\n    private String token;",
    ),
    (
        "    private boolean globalAdmin = false;",
        "    /** 是否为 GLOBAL_ADMIN 角色用户。 */\n    private boolean globalAdmin = false;",
    ),
    (
        "    public NacosUser(String userName, String token) {",
        "    /** 构造带用户名与 token 的用户对象。 */\n    public NacosUser(String userName, String token) {",
    ),
]

# --- NacosUserDetails ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserDetails.java"] = [
    (
        "/**\n * custom user.\n *\n * @author wfnuser\n */",
        "/**\n * Spring Security {@link UserDetails} 适配：包装持久层 {@link User}。\n *\n * @author wfnuser\n */",
    ),
    (
        "    private final User user;",
        "    /** 底层持久化用户实体。 */\n    private final User user;",
    ),
    (
        "        // TODO: get authorities",
        "        // TODO: 从角色服务加载 GrantedAuthority",
    ),
    (
        "    @Override\n    public boolean isAccountNonExpired() {",
        "    /** 账户永不过期。 */\n    @Override\n    public boolean isAccountNonExpired() {",
    ),
    (
        "    @Override\n    public boolean isEnabled() {",
        "    /** 账户始终启用。 */\n    @Override\n    public boolean isEnabled() {",
    ),
]

# --- NacosUserService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserService.java"] = [
    (
        "/**\n * Nacos auth plugin user service interface.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 默认鉴权插件用户服务接口。\n *\n"
        " * <p>扩展 {@link UserDetailsService}，提供用户 CRUD、分页与模糊查询。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    /**\n     * Update user password.\n     *\n     * @param username username to be updated password\n     * @param password new password\n     */",
        "    /**\n     * 更新用户密码。\n     *\n     * @param username username to be updated password\n     * @param password new password\n     */",
    ),
    (
        "    /**\n     * Get users by paged.\n     *\n     * @param pageNo       page number\n     * @param pageSize     page size\n     * @param username     username\n     * @return user list\n     */",
        "    /**\n     * 分页精确查询用户列表。\n     *\n     * @param pageNo       page number\n     * @param pageSize     page size\n     * @param username     username\n     * @return user list\n     */",
    ),
    (
        "    /**\n     * Get User info by username.\n     *\n     * @param username     username\n     * @return {@link User} information\n     */",
        "    /**\n     * 按用户名获取用户信息。\n     *\n     * @param username     username\n     * @return {@link User} information\n     */",
    ),
    (
        "    /**\n     * Create user.\n     *\n     * @param username     username\n     * @param password     password\n     * @param encode       {@code true} will encode password, {@code false} will not encode password\n     */",
        "    /**\n     * 创建用户。\n     *\n     * @param username     username\n     * @param password     password\n     * @param encode       {@code true} will encode password, {@code false} will not encode password\n     */",
    ),
    (
        "    /**\n     * Delete user.\n     *\n     * @param username     username\n     */",
        "    /**\n     * 删除用户。\n     *\n     * @param username     username\n     */",
    ),
]

# --- NacosUserServiceDirectImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserServiceDirectImpl.java"] = [
    (
        "/**\n * Custom user service, implemented by directly access to database.\n *\n * @author wfnuser\n * @author nkorange\n */",
        "/**\n * 用户服务直连实现：通过 {@link UserPersistService} 访问数据库。\n *\n * @author wfnuser\n * @author nkorange\n */",
    ),
    (
        "    @Override\n    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {",
        "    /** Spring Security 加载用户，优先读缓存。 */\n"
        "    @Override\n    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {",
    ),
    (
        "    @Override\n    public void createUser(String username, String password, boolean encode) {",
        "    /** 创建用户：校验凭证并按需 BCrypt 编码密码。 */\n"
        "    @Override\n    public void createUser(String username, String password, boolean encode) {",
    ),
    (
        "    @Override\n    public void deleteUser(String username) {",
        "    /** 删除用户，禁止删除系统保留用户名。 */\n    @Override\n    public void deleteUser(String username) {",
    ),
]

# --- NacosUserServiceRemoteImpl ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/users/NacosUserServiceRemoteImpl.java"] = [
    (
        "/**\n * Custom user service, implemented by remote request to nacos server.\n *\n * @author xiweng.yy\n */",
        "/**\n * 用户服务远程实现：HTTP 调用服务端用户管理 API。\n *\n"
        " * <p>Console 等独立部署场景使用，本地维护用户缓存。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {",
        "    /** 远程查询用户并包装为 NacosUserDetails。 */\n"
        "    @Override\n    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {",
    ),
    (
        "        // ignore encode = true, let nacos server do encode",
        "        // 密码编码由服务端处理，忽略 encode 参数",
    ),
    (
        "    private void doCreateAdminUser(String password) {",
        "    /** 调用 /admin 接口创建默认管理员用户。 */\n    private void doCreateAdminUser(String password) {",
    ),
    (
        "    private Page<User> getUserPageFromRemote(Query query) {",
        "    /** GET 远程用户列表并反序列化分页结果。 */\n    private Page<User> getUserPageFromRemote(Query query) {",
    ),
    (
        "    private String buildRemoteUserUrlPath(String apiPath) {",
        "    /** 拼接远程用户 API 完整 URL。 */\n    private String buildRemoteUserUrlPath(String apiPath) {",
    ),
]
