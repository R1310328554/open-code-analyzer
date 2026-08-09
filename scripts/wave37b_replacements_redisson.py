"""Chinese annotation replacements for Redisson 4.7.0 wave-37b tomcat-7/8 [15:30]."""
from __future__ import annotations

_T7 = "redisson-tomcat/redisson-tomcat-7/src/main/java/org/redisson/tomcat/"
_T8 = "redisson-tomcat/redisson-tomcat-8/src/main/java/org/redisson/tomcat/"

W37B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- shared cluster message patterns (tomcat-7/8/10 identical sources) ---

_attr_msg = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 集群 HTTP Session 属性同步消息的基类（Apache Tomcat）。\n"
        " * <p>携带发起节点 ID 与目标 Session ID，并提供 Redisson 编解码辅助方法。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributeMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 发起变更的 Tomcat 节点标识\n"
        "     *  @param sessionId 目标 HTTP Session ID\n"
        "     */\n"
        "    public AttributeMessage(String nodeId, String sessionId) {",
    ),
    (
        "\tprotected byte[] toByteArray(Encoder encoder, Object value) throws IOException {",
        "    /** 使用 Redisson {@link Encoder} 将属性值序列化为字节数组。 */\n"
        "\tprotected byte[] toByteArray(Encoder encoder, Object value) throws IOException {",
    ),
    (
        "\tprotected Object toObject(Decoder<?> decoder, byte[] value) throws IOException, ClassNotFoundException {",
        "    /** 使用 Redisson {@link Decoder} 从字节数组反序列化属性值。 */\n"
        "\tprotected Object toObject(Decoder<?> decoder, byte[] value) throws IOException, ClassNotFoundException {",
    ),
]

W37B_REPLACEMENTS[f"{_T8}AttributeMessage.java"] = _attr_msg
W37B_REPLACEMENTS["AttributeMessage.java"] = _attr_msg

_attr_remove = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * 跨 Tomcat 节点广播：从指定 Session 移除一组属性名。\n *\n * @author Nikita Koksharov\n */",
    ),
    (
        "    public AttributeRemoveMessage(String nodeId, String sessionId, Set<String> names) {",
        "    /** @param names 待移除的属性名集合 */\n"
        "    public AttributeRemoveMessage(String nodeId, String sessionId, Set<String> names) {",
    ),
]
W37B_REPLACEMENTS[f"{_T8}AttributeRemoveMessage.java"] = _attr_remove
W37B_REPLACEMENTS["AttributeRemoveMessage.java"] = _attr_remove

_attr_update = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨 Tomcat 节点广播：更新 Session 中单个属性的值。\n"
        " * <p>构造时将值编码为 {@code byte[]} 以便 Redis 发布/订阅传输。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributeUpdateMessage(String nodeId, String sessionId, String name, Object value, Encoder encoder) throws IOException {",
        "    /** @param name 属性名\n"
        "     *  @param value 新属性值\n"
        "     *  @param encoder Redisson 编码器\n"
        "     */\n"
        "    public AttributeUpdateMessage(String nodeId, String sessionId, String name, Object value, Encoder encoder) throws IOException {",
    ),
    (
        "    public Object getValue(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
        "    /** 使用给定解码器还原属性值。 */\n"
        "    public Object getValue(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
    ),
]
W37B_REPLACEMENTS[f"{_T8}AttributeUpdateMessage.java"] = _attr_update
W37B_REPLACEMENTS["AttributeUpdateMessage.java"] = _attr_update

_attrs_clear = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n * 跨 Tomcat 节点广播：清空指定 Session 的全部属性。\n *\n * @author Nikita Koksharov\n */",
    ),
    (
        "    public AttributesClearMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 发起清空的节点\n     *  @param sessionId 目标 Session */\n"
        "    public AttributesClearMessage(String nodeId, String sessionId) {",
    ),
]
W37B_REPLACEMENTS[f"{_T8}AttributesClearMessage.java"] = _attrs_clear
W37B_REPLACEMENTS["AttributesClearMessage.java"] = _attrs_clear

_attrs_put_all = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨 Tomcat 节点广播：批量写入 Session 属性。\n"
        " * <p>构造时将每个值编码为字节数组以支持集群 Topic 消息传递。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public AttributesPutAllMessage(RedissonSessionManager redissonSessionManager, String sessionId, Map<String, Object> attrs, Encoder encoder) throws Exception {",
        "    /** @param redissonSessionManager 提供节点 ID 的 Session 管理器\n"
        "     *  @param sessionId 目标 Session\n"
        "     *  @param attrs 待写入的属性名→值映射\n"
        "     *  @param encoder Redisson 编码器\n"
        "     */\n"
        "    public AttributesPutAllMessage(RedissonSessionManager redissonSessionManager, String sessionId, Map<String, Object> attrs, Encoder encoder) throws Exception {",
    ),
    (
        "    public Map<String, Object> getAttrs(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
        "    /** 解码全部属性并返回名→值映射；原始 attrs 为 null 时返回 null。 */\n"
        "    public Map<String, Object> getAttrs(Decoder<?> decoder) throws IOException, ClassNotFoundException {",
    ),
]
W37B_REPLACEMENTS[f"{_T8}AttributesPutAllMessage.java"] = _attrs_put_all
W37B_REPLACEMENTS["AttributesPutAllMessage.java"] = _attrs_put_all

_jndi = [
    (
        "/**\n * Redisson Session Manager for Apache Tomcat. \n * Uses Redisson instance located in JNDI.\n * \n * @author Nikita Koksharov \n *\n */",
        "/**\n"
        " * 基于 JNDI 查找 {@link org.redisson.api.RedissonClient} 的 Tomcat Session 管理器。\n"
        " * <p>适用于应用服务器已托管 Redisson 实例、Tomcat 仅引用而不自行创建客户端的场景。\n"
        " * 不支持 {@code configPath} 配置文件方式。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    @Override\n    public void setConfigPath(String configPath) {",
        "    /** JNDI 模式下禁止使用配置文件路径。 */\n    @Override\n    public void setConfigPath(String configPath) {",
    ),
    (
        "    @Override\n    protected RedissonClient buildClient() throws LifecycleException {",
        "    /** 从 {@code java:comp/env} 按 {@link #jndiName} 查找 {@link org.redisson.api.RedissonClient}。 */\n"
        "    @Override\n    protected RedissonClient buildClient() throws LifecycleException {",
    ),
    (
        "    public String getJndiName() {",
        "    /** 返回 JNDI 环境条目名称。 */\n    public String getJndiName() {",
    ),
    (
        "    public void setJndiName(String jndiName) {",
        "    /** 设置 JNDI 环境条目名称（如 {@code redisson}）。 */\n    public void setJndiName(String jndiName) {",
    ),
    (
        "    @Override\n    protected void shutdownRedisson() {",
        "    /** JNDI 托管的 Redisson 由容器生命周期管理，此处不关闭。 */\n    @Override\n    protected void shutdownRedisson() {",
    ),
]
W37B_REPLACEMENTS[f"{_T7}JndiRedissonSessionManager.java"] = _jndi
W37B_REPLACEMENTS[f"{_T8}JndiRedissonSessionManager.java"] = _jndi
W37B_REPLACEMENTS["JndiRedissonSessionManager.java"] = _jndi

_sso = [
    (
        "/**\n * Extended implementation of Tomcat SSO valve to use Redis or Valkey as a storage.\n * This allows to cluster Tomcat without sticky sessions.\n */",
        "/**\n"
        " * 扩展 Tomcat {@link org.apache.catalina.authenticator.SingleSignOn} Valve，\n"
        " * 以 Redis/Valkey 持久化 SSO 条目，实现无粘性 Session 的 Tomcat 集群单点登录。\n"
        " * <p>本地 {@code cache} 与 Redis Map {@code redisson:tomcat_sso} 双向同步。\n"
        " */",
    ),
    (
        "  void setSessionManager(RedissonSessionManager manager) {",
        "  /** 注入 {@link RedissonSessionManager} 以访问 Redis Map。 */\n  void setSessionManager(RedissonSessionManager manager) {",
    ),
    (
        "  @Override\n  public void invoke(Request request, Response response) throws IOException, ServletException {",
        "  /** 请求前从 Redis 同步 SSO 条目，再委托父类 Valve 链。 */\n  @Override\n  public void invoke(Request request, Response response) throws IOException, ServletException {",
    ),
    (
        "  @Override\n  public void sessionDestroyed(String ssoId, Session session) {",
        "  /** Session 销毁时清理 Redis 中对应 SSO 条目。 */\n  @Override\n  public void sessionDestroyed(String ssoId, Session session) {",
    ),
    (
        "  @Override\n  protected boolean associate(String ssoId, Session session) {",
        "  /** 关联 Session 与 SSO ID 后，将条目写入 Redis。 */\n  @Override\n  protected boolean associate(String ssoId, Session session) {",
    ),
    (
        "  @Override\n  protected boolean reauthenticate(String ssoId, Realm realm, Request request) {",
        "  /** 重新认证前同步 Redis 中的 SSO 状态。 */\n  @Override\n  protected boolean reauthenticate(String ssoId, Realm realm, Request request) {",
    ),
    (
        "  @Override\n  protected void register(String ssoId, Principal principal, String authType, String username, String password) {",
        "  /** 注册新 SSO 条目并持久化到 Redis。 */\n  @Override\n  protected void register(String ssoId, Principal principal, String authType, String username, String password) {",
    ),
    (
        "  @Override\n  protected void deregister(String ssoId) {",
        "  /** 注销 SSO 并从 Redis 删除条目。 */\n  @Override\n  protected void deregister(String ssoId) {",
    ),
    (
        "  @Override\n  protected boolean update(String ssoId, Principal principal, String authType, String username, String password) {",
        "  /** 更新 SSO 凭证；成功时写回 Redis。 */\n  @Override\n  protected boolean update(String ssoId, Principal principal, String authType, String username, String password) {",
    ),
    (
        "  @Override\n  protected void removeSession(String ssoId, Session session) {",
        "  /** 移除 Session 关联；若无剩余 Session 则注销 SSO。 */\n  @Override\n  protected void removeSession(String ssoId, Session session) {",
    ),
    (
        "  /**\n   * Lookup {@code SingleSignOnEntry} for the given SSO ID and make sure local cache has the same value.\n   * That applies also to non existence.\n   *\n   * @param ssoSessionId SSO session id we are looking for\n   * @return matching {@code SingleSignOnEntry} instance or null when not found\n   */",
        "  /**\n"
        "   * 按 SSO ID 从 Redis 查找 {@link org.apache.catalina.authenticator.SingleSignOnEntry}，\n"
        "   * 并同步本地 cache（包括条目不存在时移除缓存项）。\n"
        "   *\n"
        "   * @param ssoSessionId 目标 SSO Session ID\n"
        "   * @return 匹配的条目，未找到时返回 {@code null}\n"
        "   */",
    ),
    (
        "  /**\n   * Retrieve SSO session ID from provided cookies in the request.\n   *\n   * @param request The request that has been sent to the server.\n   * @return SSO session ID provided with the request or null when none provided\n   */",
        "  /**\n"
        "   * 从请求 Cookie 中解析 SSO Session ID。\n"
        "   *\n"
        "   * @param request 入站请求\n"
        "   * @return Cookie 中的 SSO ID，未提供时返回 {@code null}\n"
        "   */",
    ),
]
W37B_REPLACEMENTS[f"{_T7}RedissonSingleSignOn.java"] = _sso
W37B_REPLACEMENTS[f"{_T8}RedissonSingleSignOn.java"] = _sso
W37B_REPLACEMENTS["RedissonSingleSignOn.java"] = _sso

_session_created = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨 Tomcat 节点广播：通知其他实例有新 Session 创建。\n"
        " * <p>继承 {@link AttributeMessage}，携带节点 ID 与 Session ID。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public SessionCreatedMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 创建 Session 的节点\n     *  @param sessionId 新 Session ID */\n"
        "    public SessionCreatedMessage(String nodeId, String sessionId) {",
    ),
]
W37B_REPLACEMENTS[f"{_T7}SessionCreatedMessage.java"] = _session_created
W37B_REPLACEMENTS[f"{_T8}SessionCreatedMessage.java"] = _session_created
W37B_REPLACEMENTS["SessionCreatedMessage.java"] = _session_created

_session_destroyed = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 跨 Tomcat 节点广播：通知其他实例 Session 已销毁。\n"
        " * <p>继承 {@link AttributeMessage}，携带节点 ID 与 Session ID。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public SessionDestroyedMessage(String nodeId, String sessionId) {",
        "    /** @param nodeId 销毁 Session 的节点\n     *  @param sessionId 已销毁 Session ID */\n"
        "    public SessionDestroyedMessage(String nodeId, String sessionId) {",
    ),
]
W37B_REPLACEMENTS[f"{_T7}SessionDestroyedMessage.java"] = _session_destroyed
W37B_REPLACEMENTS[f"{_T8}SessionDestroyedMessage.java"] = _session_destroyed
W37B_REPLACEMENTS["SessionDestroyedMessage.java"] = _session_destroyed

# --- tomcat-7 valves (javax.servlet; basename keys omitted to avoid tomcat-10/11 clash) ---

_update_valve_javax = [
    (
        "/**\n * Redisson Valve object for Apache Tomcat\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Tomcat {@link org.apache.catalina.valves.ValveBase}：请求结束后将 Session 持久化到 Redis。\n"
        " * <p>在 {@link org.apache.catalina.connector.Request} 处理完成后调用\n"
        " * {@link RedissonSessionManager#store(javax.servlet.http.HttpSession)} 写回变更。\n"
        " * <p>配合 {@link UsageValve} 跟踪 Session 使用计数，避免并发更新冲突。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public void incUsage() {",
        "    /** 递增 Valve 引用计数（{@link RedissonSessionManager} 生命周期管理）。 */\n    public void incUsage() {",
    ),
    (
        "    public int decUsage() {",
        "    /** 递减引用计数并返回当前值。 */\n    public int decUsage() {",
    ),
    (
        "        //check if we already filtered/processed this request",
        "        // 防止同一请求在 Valve 链中重复触发持久化",
    ),
    (
        "    @Override\n    public void invoke(Request request, Response response) throws IOException, ServletException {",
        "    /** 委托后续 Valve；finally 块中将 Session 写回 Redis。 */\n    @Override\n    public void invoke(Request request, Response response) throws IOException, ServletException {",
    ),
]
W37B_REPLACEMENTS[f"{_T7}UpdateValve.java"] = _update_valve_javax

_usage_valve_javax = [
    (
        "/**\n * Redisson Valve object for Apache Tomcat\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Tomcat Valve：在请求处理期间标记 {@link RedissonSession} 为“使用中”。\n"
        " * <p>通过 {@link RedissonSession#startUsage()} / {@link RedissonSession#endUsage()}\n"
        " * 防止后台线程在请求未完成时覆盖 Session 状态。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    public void incUsage() {",
        "    /** 递增 Valve 引用计数。 */\n    public void incUsage() {",
    ),
    (
        "    public int decUsage() {",
        "    /** 递减引用计数并返回当前值。 */\n    public int decUsage() {",
    ),
    (
        "        //check if we already filtered/processed this request",
        "        // 防止同一请求重复进入使用计数逻辑",
    ),
    (
        "    @Override\n    public void invoke(Request request, Response response) throws IOException, ServletException {",
        "    /** 请求前 {@code startUsage}，完成后 {@code endUsage}，再委托 Valve 链。 */\n    @Override\n    public void invoke(Request request, Response response) throws IOException, ServletException {",
    ),
]
W37B_REPLACEMENTS[f"{_T7}UsageValve.java"] = _usage_valve_javax
