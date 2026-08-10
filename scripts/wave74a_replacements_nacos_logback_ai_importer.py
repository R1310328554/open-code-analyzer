"""Chinese annotation replacements for Nacos 3.2.3 wave74a [0:15] logback12 + ai importer/pipeline."""

R: dict[str, list[tuple[str, str]]] = {}

# --- LogbackNacosLoggingAdapter ---

R["logger-adapter-impl/logback-adapter-12/src/main/java/com/alibaba/nacos/logger/adapter/logback12/LogbackNacosLoggingAdapter.java"] = [
    (
        "/**\n * Support for Logback version 1.0.8 to 1.2.X.\n *\n * @author <a href=\"mailto:huangxiaoyu1018@gmail.com\">hxy1991</a>\n * @author <a href=\"mailto:hujun3@xiaomi.com\">hujun</a>\n * @author xiweng.yy\n * @since 0.9.0\n */",
        "/**\n * Logback 1.0.8～1.2.x 版 Nacos 日志适配器。\n *\n * <p>实现 {@link com.alibaba.nacos.common.logging.NacosLoggingAdapter}，"
        " 加载 {@code nacos-logback12.xml} 并在 LoggerContext 重置时自动恢复 Nacos 配置；"
        " 通过检测 {@code ch.qos.logback.core.model.Model} 排除 Logback 1.3+。</p>\n *\n"
        " * @author <a href=\"mailto:huangxiaoyu1018@gmail.com\">hxy1991</a>\n"
        " * @author <a href=\"mailto:hujun3@xiaomi.com\">hujun</a>\n * @author xiweng.yy\n * @since 0.9.0\n */",
    ),
    (
        "    private static final String NACOS_LOGBACK_LOCATION = \"classpath:nacos-logback12.xml\";",
        "    /** Nacos 默认 Logback 1.2 配置文件 classpath 位置。 */\n"
        "    private static final String NACOS_LOGBACK_LOCATION = \"classpath:nacos-logback12.xml\";",
    ),
    (
        "    private static final String LOGBACK_CLASSES = \"ch.qos.logback.classic.Logger\";",
        "    /** Logback Classic Logger 实现类名，用于 classpath 探测。 */\n"
        "    private static final String LOGBACK_CLASSES = \"ch.qos.logback.classic.Logger\";",
    ),
    (
        "    private final NacosLogbackConfiguratorAdapterV1 configurator;",
        "    /** 自定义 Joran 配置器，支持 nacosClientProperty 且不污染用户 savepoint。 */\n"
        "    private final NacosLogbackConfiguratorAdapterV1 configurator;",
    ),
    (
        "    public LogbackNacosLoggingAdapter() {",
        "    /** 构造适配器并初始化 Logback 1.2 配置器。 */\n    public LogbackNacosLoggingAdapter() {",
    ),
    (
        "    @Override\n    public boolean isAdaptedLogger(Class<?> loggerClass) {",
        "    /** 判断 Logger 类是否为 Logback 1.2 Classic 且非 1.3+。 */\n"
        "    @Override\n    public boolean isAdaptedLogger(Class<?> loggerClass) {",
    ),
    (
        "    /**\n     * logback use 'ch.qos.logback.core.model.Model' since 1.3.0, set logback version during initialization.\n     */",
        "    /**\n     * Logback 自 1.3.0 起引入 {@code ch.qos.logback.core.model.Model}，"
        " 通过该类是否存在判断是否为 1.3 及以上版本。\n     */",
    ),
    (
        "    @Override\n    public boolean isNeedReloadConfiguration() {",
        "    /** Logback 1.2 适配器无需检测重载，固定返回 false。 */\n"
        "    @Override\n    public boolean isNeedReloadConfiguration() {",
    ),
    (
        "    @Override\n    public String getDefaultConfigLocation() {",
        "    /** 返回默认 nacos-logback12.xml 位置。 */\n    @Override\n    public String getDefaultConfigLocation() {",
    ),
    (
        "    @Override\n    public void loadConfiguration(NacosLoggingProperties loggingProperties) {",
        "    /** 加载指定位置 Logback 配置并注册 Context 重置监听器。 */\n"
        "    @Override\n    public void loadConfiguration(NacosLoggingProperties loggingProperties) {",
    ),
    (
        "    class NacosLoggerContextListener implements LoggerContextListener {",
        "    /** LoggerContext 监听器：在 reset 后重新加载 Nacos 日志配置。 */\n"
        "    class NacosLoggerContextListener implements LoggerContextListener {",
    ),
    (
        "        @Override\n        public boolean isResetResistant() {",
        "        /** 标记为 reset  resistant，避免被 Logback 自动移除。 */\n"
        "        @Override\n        public boolean isResetResistant() {",
    ),
    (
        "        @Override\n        public void onReset(LoggerContext context) {",
        "        /** Context 重置时按原 location 重新加载 Nacos 配置。 */\n"
        "        @Override\n        public void onReset(LoggerContext context) {",
    ),
]

# --- LogbackNacosLoggingAdapterBuilder ---

R["logger-adapter-impl/logback-adapter-12/src/main/java/com/alibaba/nacos/logger/adapter/logback12/LogbackNacosLoggingAdapterBuilder.java"] = [
    (
        "/**\n * Builder of {@link com.alibaba.nacos.common.logging.NacosLoggingAdapter} for logback 1.2.x and below.\n *\n * @author xiweng.yy\n */",
        "/**\n * Logback 1.2.x 日志适配器的 SPI 构建器。\n *\n * <p>通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册，"
        " 在 classpath 存在 Logback 1.2 且非 1.3+ 时由 {@link com.alibaba.nacos.common.logging.NacosLogging} 选用。</p>\n *\n"
        " * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public NacosLoggingAdapter build() {",
        "    /** 创建 {@link LogbackNacosLoggingAdapter} 实例。 */\n    @Override\n    public NacosLoggingAdapter build() {",
    ),
]

# --- NacosClientPropertyAction ---

R["logger-adapter-impl/logback-adapter-12/src/main/java/com/alibaba/nacos/logger/adapter/logback12/NacosClientPropertyAction.java"] = [
    (
        "/**\n * support logback read properties from NacosClientProperties. just like springProperty. for example:\n * <nacosClientProperty scope=\"context\" name=\"logPath\" source=\"system.log.path\" defaultValue=\"/root\" />\n *\n * @author onewe\n */",
        "/**\n * Logback Joran 自定义动作 {@code nacosClientProperty}，从 Nacos 客户端属性注入配置。\n *\n * <p>用法类似 Spring Boot 的 {@code springProperty}，例如："
        " {@code <nacosClientProperty scope=\"context\" name=\"logPath\" source=\"system.log.path\" defaultValue=\"/root\" />}。</p>\n *\n"
        " * @author onewe\n */",
    ),
    (
        "    private static final String DEFAULT_VALUE_ATTRIBUTE = \"defaultValue\";",
        "    /** XML 属性名：默认值。 */\n    private static final String DEFAULT_VALUE_ATTRIBUTE = \"defaultValue\";",
    ),
    (
        "    private static final String SOURCE_ATTRIBUTE = \"source\";",
        "    /** XML 属性名：Nacos 客户端属性源 key。 */\n    private static final String SOURCE_ATTRIBUTE = \"source\";",
    ),
    (
        "    private final NacosLoggingProperties loggingProperties;",
        "    /** 当前加载配置时绑定的 Nacos 日志属性。 */\n    private final NacosLoggingProperties loggingProperties;",
    ),
    (
        "    NacosClientPropertyAction(NacosLoggingProperties loggingProperties) {",
        "    /** 构造动作并注入属性查找源。 */\n    NacosClientPropertyAction(NacosLoggingProperties loggingProperties) {",
    ),
    (
        "    @Override\n    public void begin(InterpretationContext ic, String elementName, Attributes attributes)\n        throws ActionException {",
        "    /** 解析 {@code nacosClientProperty} 元素并将属性写入指定 scope。 */\n"
        "    @Override\n    public void begin(InterpretationContext ic, String elementName, Attributes attributes)\n        throws ActionException {",
    ),
]

# --- NacosLogbackConfiguratorAdapterV1 ---

R["logger-adapter-impl/logback-adapter-12/src/main/java/com/alibaba/nacos/logger/adapter/logback12/NacosLogbackConfiguratorAdapterV1.java"] = [
    (
        "/**\n * ensure that Nacos configuration does not affect user configuration savepoints and  scanning url.\n *\n * @author <a href=\"mailto:hujun3@xiaomi.com\">hujun</a>\n * @see <a href=\"https://github.com/alibaba/nacos/issues/6999\">#6999</a>\n */",
        "/**\n * Logback 1.2 专用 Joran 配置器，避免 Nacos 配置污染用户 savepoint 与扫描 URL。\n *\n"
        " * <p>禁用 {@link #registerSafeConfiguration}、注册 {@link NacosClientPropertyAction}，"
        " 并兼容 1.1.10 以下旧版 {@code doConfigure} API。</p>\n *\n"
        " * @author <a href=\"mailto:hujun3@xiaomi.com\">hujun</a>\n"
        " * @see <a href=\"https://github.com/alibaba/nacos/issues/6999\">#6999</a>\n */",
    ),
    (
        "    private NacosLoggingProperties loggingProperties;",
        "    /** 加载 XML 时使用的 Nacos 客户端属性。 */\n    private NacosLoggingProperties loggingProperties;",
    ),
    (
        "    public void setLoggingProperties(NacosLoggingProperties loggingProperties) {",
        "    /** 注入属性供 {@link NacosClientPropertyAction} 读取。 */\n"
        "    public void setLoggingProperties(NacosLoggingProperties loggingProperties) {",
    ),
    (
        "    /**\n     * ensure that Nacos configuration does not affect user configuration savepoints.\n     *\n     * @param eventList safe data\n     */",
        "    /**\n     * 空实现：阻止 Nacos 配置写入 Logback safe configuration savepoint。\n     *\n     * @param eventList safe data\n     */",
    ),
    (
        "    @Override\n    public void addInstanceRules(RuleStore rs) {",
        "    /** 在父类规则基础上注册 {@code nacosClientProperty} 解析规则。 */\n"
        "    @Override\n    public void addInstanceRules(RuleStore rs) {",
    ),
    (
        "    /**\n     * ensure that Nacos configuration does not affect user configuration scanning url.\n     *\n     * @param url config url\n     * @throws Exception e\n     */",
        "    /**\n     * 从 URL 加载 Logback 配置，禁用 URLConnection 缓存并兼容旧版 API。\n     *\n     * @param url config url\n     * @throws Exception e\n     */",
    ),
    (
        "                // adapter old version of logback below 1.1.10",
        "                // 兼容 Logback 1.1.10 以下仅支持 InputStream 的旧版 doConfigure API",
    ),
    (
        "    /**\n     * Since logback 1.1.10, Add new doConfigure API with sax systemId and use this API to do configure.\n     *\n     * @return {@code true} when logback is upper 1.1.10, otherwise {@code false}\n     */",
        "    /**\n     * 检测当前 Logback 是否提供带 systemId 的新版 {@code doConfigure(InputStream, String)} API。\n     *\n"
        "     * @return Logback 版本高于 1.1.10 时返回 {@code true}\n     */",
    ),
]

# --- DefaultAiResourceImportSourceProvider ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/DefaultAiResourceImportSourceProvider.java"] = [
    (
        "/**\n * Default AI resource import source presets.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * 内置 AI 资源导入源预设提供者。\n *\n * <p>根据配置属性组装官方 MCP Registry、Skill well-known 端点与 skills.sh 三类"
        " {@link com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportSource}，"
        " 供控制台与导入管线选用。</p>\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    public static final String PREFIX = \"nacos.plugin.ai.importer.\";",
        "    /** 导入插件配置键前缀。 */\n    public static final String PREFIX = \"nacos.plugin.ai.importer.\";",
    ),
    (
        "    public static final String MCP_OFFICIAL_PREFIX = PREFIX + \"mcp.official.\";",
        "    /** 官方 MCP Registry 导入源配置前缀。 */\n    public static final String MCP_OFFICIAL_PREFIX = PREFIX + \"mcp.official.\";",
    ),
    (
        "    public static final String SKILL_WELL_KNOWN_PREFIX = PREFIX + \"skills.well-known.\";",
        "    /** Skill well-known 发现端点配置前缀。 */\n    public static final String SKILL_WELL_KNOWN_PREFIX = PREFIX + \"skills.well-known.\";",
    ),
    (
        "    public static final String SKILLS_SH_PREFIX = PREFIX + \"skills.skills-sh.\";",
        "    /** skills.sh 平台导入源配置前缀。 */\n    public static final String SKILLS_SH_PREFIX = PREFIX + \"skills.skills-sh.\";",
    ),
    (
        "    @Override\n    public Collection<AiResourceImportSource> loadSources(Properties properties)\n        throws NacosException {",
        "    /** 按开关与属性加载全部已启用的内置导入源。 */\n"
        "    @Override\n    public Collection<AiResourceImportSource> loadSources(Properties properties)\n        throws NacosException {",
    ),
    (
        "    private void applyCommonSourceOptions(Properties properties, String prefix,\n        AiResourceImportSource source) {",
        "    /** 写入超时、分页上限、制品大小等通用导入选项。 */\n"
        "    private void applyCommonSourceOptions(Properties properties, String prefix,\n        AiResourceImportSource source) {",
    ),
    (
        "    private void applySecurityOptions(Properties properties, String prefix,\n        AiResourceImportSource source) {",
        "    /** 写入 allow-http、allow-private-network 等网络安全策略属性。 */\n"
        "    private void applySecurityOptions(Properties properties, String prefix,\n        AiResourceImportSource source) {",
    ),
]

# --- DefaultImportHttpClient ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/http/DefaultImportHttpClient.java"] = [
    (
        "/**\n * Shared HTTP client for built-in AI importers.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * 内置 AI 导入插件共享 HTTP 客户端。\n *\n * <p>基于 {@link java.net.http.HttpClient} 发起 GET 请求，"
        " 强制执行 HTTPS、禁止跟随重定向、限制响应体大小，"
        " 并可选允许 HTTP 或内网/本地地址（由导入源属性控制）。</p>\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    public static final String PROPERTY_ALLOW_HTTP = \"allow-http\";",
        "    /** 导入源属性：是否允许明文 HTTP（kebab-case）。 */\n    public static final String PROPERTY_ALLOW_HTTP = \"allow-http\";",
    ),
    (
        "    public static final String PROPERTY_ALLOW_PRIVATE_NETWORK = \"allow-private-network\";",
        "    /** 导入源属性：是否允许访问内网/本地地址（kebab-case）。 */\n"
        "    public static final String PROPERTY_ALLOW_PRIVATE_NETWORK = \"allow-private-network\";",
    ),
    (
        "    public DefaultImportHttpClient() {",
        "    /** 使用默认超时与不跟随重定向策略构造客户端。 */\n    public DefaultImportHttpClient() {",
    ),
    (
        "    /**\n     * Create a default importer HTTP client with custom DNS resolver.\n     *\n     * @param httpClient HTTP client\n     * @param dnsResolver DNS resolver\n     */",
        "    /**\n     * 使用自定义 {@link HttpClient} 与 DNS 解析器构造（便于测试）。\n     *\n     * @param httpClient HTTP client\n     * @param dnsResolver DNS resolver\n     */",
    ),
    (
        "    /**\n     * Send a GET request with the default read timeout.\n     *\n     * @param source import source\n     * @param url request URL\n     * @param accept optional Accept header\n     * @return HTTP response\n     * @throws Exception if validation or request fails\n     */",
        "    /**\n     * 以默认读超时发送 GET 请求。\n     *\n     * @param source import source\n     * @param url request URL\n     * @param accept optional Accept header\n     * @return HTTP response\n     * @throws Exception if validation or request fails\n     */",
    ),
    (
        "    /**\n     * Send a GET request after applying importer network policy.\n     *\n     * @param source import source\n     * @param url request URL\n     * @param readTimeoutSeconds request read timeout in seconds\n     * @param accept optional Accept header\n     * @return HTTP response\n     * @throws Exception if validation or request fails\n     */",
        "    /**\n     * 校验 URL 与网络安全策略后发送 GET 请求。\n     *\n     * @param source import source\n     * @param url request URL\n     * @param readTimeoutSeconds request read timeout in seconds\n     * @param accept optional Accept header\n     * @return HTTP response\n     * @throws Exception if validation or request fails\n     */",
    ),
    (
        "    public interface DnsResolver {",
        "    /** 可插拔 DNS 解析器，用于 SSRF 防护中的地址判定。 */\n    public interface DnsResolver {",
    ),
    (
        "    private static class LimitedByteArrayBodyHandler implements HttpResponse.BodyHandler<byte[]> {",
        "    /** 限制响应体最大字节数的 BodyHandler 工厂。 */\n"
        "    private static class LimitedByteArrayBodyHandler implements HttpResponse.BodyHandler<byte[]> {",
    ),
]

# --- ImportHttpResponse ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/http/ImportHttpResponse.java"] = [
    (
        "/**\n * HTTP response fetched by default AI importers.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * 内置 AI 导入 HTTP 客户端返回的响应封装。\n *\n * <p>包含最终 URL、状态码、响应头与 body 字节数组，"
        " 并提供 {@link #isSuccess()} 与 {@link #getContentType()} 便捷方法。</p>\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    private final String url;",
        "    /** 实际请求的 URL（重定向解析后）。 */\n    private final String url;",
    ),
    (
        "    private final int statusCode;",
        "    /** HTTP 状态码。 */\n    private final int statusCode;",
    ),
    (
        "    private final HttpHeaders headers;",
        "    /** 响应头集合。 */\n    private final HttpHeaders headers;",
    ),
    (
        "    private final byte[] body;",
        "    /** 响应体字节数组（永不为 null）。 */\n    private final byte[] body;",
    ),
    (
        "    public boolean isSuccess() {",
        "    /** 判断状态码是否为 2xx。 */\n    public boolean isSuccess() {",
    ),
    (
        "    public String getContentType() {",
        "    /** 返回 Content-Type 响应头，缺失时为空串。 */\n    public String getContentType() {",
    ),
]

# --- McpRegistryClient ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/mcp/McpRegistryClient.java"] = [
    (
        "/**\n * Minimal MCP official registry client used by the default importer plugin.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * 官方 MCP Registry HTTP 客户端（默认导入插件内部使用）。\n *\n * <p>分页拉取 Registry 列表、按 externalId 检索单个 Server，"
        " 并将 Registry JSON 适配为 {@link com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo}。</p>\n *\n"
        " * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    Page fetchOfficialRegistryPage(AiResourceImportSource source, String cursor, Integer limit,\n        String search) throws Exception {",
        "    /** 分页查询官方 Registry，支持 cursor、limit 与 search 参数。 */\n"
        "    Page fetchOfficialRegistryPage(AiResourceImportSource source, String cursor, Integer limit,\n        String search) throws Exception {",
    ),
    (
        "    McpServerDetailInfo fetchOfficialRegistryServer(AiResourceImportSource source,\n        String externalId, int limit) throws Exception {",
        "    /** 按 externalId（name 或 id）在 Registry 中定位单个 MCP Server 详情。 */\n"
        "    McpServerDetailInfo fetchOfficialRegistryServer(AiResourceImportSource source,\n        String externalId, int limit) throws Exception {",
    ),
    (
        "    static class Page {",
        "    /** Registry 分页结果：Server 列表与下一页 cursor。 */\n    static class Page {",
    ),
]

# --- McpRegistryImportService ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/mcp/McpRegistryImportService.java"] = [
    (
        "/**\n * Built-in importer for the official MCP registry API.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * 官方 MCP Registry API 内置导入服务。\n *\n * <p>实现 {@link com.alibaba.nacos.plugin.ai.importer.spi.AiResourceImportService}，"
        " 提供 MCP Server 搜索分页与详情拉取，制品 payload 为 {@code MCP_DETAIL} JSON。</p>\n *\n"
        " * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    public static final String RESOURCE_TYPE_MCP = AiResourceImportConstants.RESOURCE_TYPE_MCP;",
        "    /** 支持的资源类型：MCP Server。 */\n"
        "    public static final String RESOURCE_TYPE_MCP = AiResourceImportConstants.RESOURCE_TYPE_MCP;",
    ),
    (
        "    @Override\n    public AiResourceImportCandidatePage search(AiResourceImportContext context)\n        throws NacosException {",
        "    /** 调用 Registry 分页 API 搜索 MCP Server 候选列表。 */\n"
        "    @Override\n    public AiResourceImportCandidatePage search(AiResourceImportContext context)\n        throws NacosException {",
    ),
    (
        "    @Override\n    public AiResourceImportArtifact fetch(AiResourceImportContext context,\n        AiResourceImportItem item) throws NacosException {",
        "    /** 按 externalId 拉取单个 MCP Server 详情并封装为导入制品。 */\n"
        "    @Override\n    public AiResourceImportArtifact fetch(AiResourceImportContext context,\n        AiResourceImportItem item) throws NacosException {",
    ),
]

# --- McpRegistryImportServiceBuilder ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/mcp/McpRegistryImportServiceBuilder.java"] = [
    (
        "/**\n * Builder for the built-in official MCP registry import service.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * 官方 MCP Registry 导入服务的 SPI 构建器。\n *\n * <p>importer 类型标识为 {@code mcp-registry}，"
        " 通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册。</p>\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    public static final String IMPORTER_TYPE = \"mcp-registry\";",
        "    /** 导入器类型常量：{@code mcp-registry}。 */\n    public static final String IMPORTER_TYPE = \"mcp-registry\";",
    ),
    (
        "    @Override\n    public AiResourceImportService build(Properties properties) {",
        "    /** 创建 {@link McpRegistryImportService} 实例。 */\n"
        "    @Override\n    public AiResourceImportService build(Properties properties) {",
    ),
]

# --- SkillWellKnownImportService ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/skill/SkillWellKnownImportService.java"] = [
    (
        "/**\n * Importer for Skill well-known registry endpoints.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * Skill well-known 发现端点导入服务。\n *\n * <p>从 {@code /.well-known/agent-skills/index.json} 或"
        " {@code /.well-known/skills/index.json} 拉取索引，支持 0.1.0/0.2.0 schema，"
        " 将 Skill 文件或归档制品打包为 ZIP 导入。</p>\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    private static final String WELL_KNOWN_AGENT_SKILLS = \"/.well-known/agent-skills\";",
        "    /** Agent Skills well-known 路径前缀。 */\n"
        "    private static final String WELL_KNOWN_AGENT_SKILLS = \"/.well-known/agent-skills\";",
    ),
    (
        "    private static final String MARKDOWN_FILE = \"SKILL.md\";",
        "    /** Skill 包内必需的 Markdown 描述文件名。 */\n    private static final String MARKDOWN_FILE = \"SKILL.md\";",
    ),
    (
        "    @Override\n    public AiResourceImportCandidatePage search(AiResourceImportContext context)\n        throws NacosException {",
        "    /** 拉取 well-known 索引并按 query 过滤、分页返回 Skill 候选。 */\n"
        "    @Override\n    public AiResourceImportCandidatePage search(AiResourceImportContext context)\n        throws NacosException {",
    ),
    (
        "    @Override\n    public AiResourceImportArtifact fetch(AiResourceImportContext context,\n        AiResourceImportItem item) throws NacosException {",
        "    /** 下载指定 Skill 的文件或归档并打包为 SKILL_ZIP 制品。 */\n"
        "    @Override\n    public AiResourceImportArtifact fetch(AiResourceImportContext context,\n        AiResourceImportItem item) throws NacosException {",
    ),
]

# --- SkillWellKnownImportServiceBuilder ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/skill/SkillWellKnownImportServiceBuilder.java"] = [
    (
        "/**\n * Builder for the built-in well-known Skill import service.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * Skill well-known 导入服务的 SPI 构建器。\n *\n * <p>importer 类型标识为 {@code skills-well-known}。</p>\n *\n"
        " * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    public static final String IMPORTER_TYPE = \"skills-well-known\";",
        "    /** 导入器类型常量：{@code skills-well-known}。 */\n    public static final String IMPORTER_TYPE = \"skills-well-known\";",
    ),
    (
        "    @Override\n    public AiResourceImportService build(Properties properties) {",
        "    /** 创建 {@link SkillWellKnownImportService} 实例。 */\n"
        "    @Override\n    public AiResourceImportService build(Properties properties) {",
    ),
]

# --- SkillsShImportService ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/skill/SkillsShImportService.java"] = [
    (
        "/**\n * Importer for the skills.sh search and download APIs.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * skills.sh 平台搜索与下载 API 导入服务。\n *\n * <p>调用 {@code /api/search} 检索 Skill、"
        " {@code /api/download} 拉取文件快照并打包为含 {@code SKILL.md} 的 ZIP 制品。</p>\n *\n"
        " * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    private static final String API_SEARCH = \"/api/search\";",
        "    /** skills.sh 搜索 API 路径后缀。 */\n    private static final String API_SEARCH = \"/api/search\";",
    ),
    (
        "    private static final String API_DOWNLOAD = \"/api/download\";",
        "    /** skills.sh 下载 API 路径后缀。 */\n    private static final String API_DOWNLOAD = \"/api/download\";",
    ),
    (
        "    @Override\n    public AiResourceImportCandidatePage search(AiResourceImportContext context)\n        throws NacosException {",
        "    /** 调用 skills.sh 搜索 API 返回 Skill 候选列表。 */\n"
        "    @Override\n    public AiResourceImportCandidatePage search(AiResourceImportContext context)\n        throws NacosException {",
    ),
    (
        "    @Override\n    public AiResourceImportArtifact fetch(AiResourceImportContext context,\n        AiResourceImportItem item) throws NacosException {",
        "    /** 按仓库与 skillId 下载文件快照并打包为 SKILL_ZIP 制品。 */\n"
        "    @Override\n    public AiResourceImportArtifact fetch(AiResourceImportContext context,\n        AiResourceImportItem item) throws NacosException {",
    ),
]

# --- SkillsShImportServiceBuilder ---

R["plugin-default-impl/nacos-default-ai-importer-plugin/src/main/java/com/alibaba/nacos/plugin/ai/importer/defaultimpl/skill/SkillsShImportServiceBuilder.java"] = [
    (
        "/**\n * Builder for the built-in skills.sh Skill import service.\n *\n * @author xiweng.yy\n * @since 3.2.1\n */",
        "/**\n * skills.sh Skill 导入服务的 SPI 构建器。\n *\n * <p>importer 类型标识为 {@code skills-sh}。</p>\n *\n"
        " * @author xiweng.yy\n * @since 3.2.1\n */",
    ),
    (
        "    public static final String IMPORTER_TYPE = \"skills-sh\";",
        "    /** 导入器类型常量：{@code skills-sh}。 */\n    public static final String IMPORTER_TYPE = \"skills-sh\";",
    ),
    (
        "    @Override\n    public AiResourceImportService build(Properties properties) {",
        "    /** 创建 {@link SkillsShImportService} 实例。 */\n"
        "    @Override\n    public AiResourceImportService build(Properties properties) {",
    ),
]

# --- SkillScannerMarkdownFindingParser ---

R["plugin-default-impl/nacos-default-ai-pipeline-plugin/src/main/java/com/alibaba/nacos/plugin/ai/pipeline/spi/impl/SkillScannerMarkdownFindingParser.java"] = [
    (
        "/**\n * Extracts skill-scanner {@code --format markdown} finding titles from stdout for {@link Checkpoint} rows.\n *\n * <p>Looks for an {@code ## Findings} section and collects each {@code ### ...} heading line as one\n * finding title (e.g. {@code ### HIGH — Prompt injection} → {@code HIGH — Prompt injection}).</p>\n *\n * @author qiacheng.cxy\n * @since 3.2.0\n */",
        "/**\n * 从 skill-scanner {@code --format markdown} 输出中提取发现项标题，映射为 {@link Checkpoint} 行。\n *\n"
        " * <p>定位 {@code ## Findings} 小节，收集其下每条 {@code ### ...} 标题作为失败检查点"
        "（例如 {@code ### HIGH — Prompt injection}）。</p>\n *\n * @author qiacheng.cxy\n * @since 3.2.0\n */",
    ),
    (
        "    /**\n     * Builds reject checkpoints: one failed checkpoint per finding heading under {@code ## Findings}.\n     * If no headings are found, returns a single fallback checkpoint so callers still get a structured result.\n     */",
        "    /**\n     * 构建拒绝（失败）检查点：{@code ## Findings} 下每个三级标题对应一条失败记录。\n"
        "     * 若未解析到标题，返回一条 HIGH/CRITICAL 兜底检查点以保证结构化输出。\n     */",
    ),
    (
        "    /**\n     * Builds pass checkpoints when the scanner exits successfully: either a generic pass row, or\n     * derived from report text if needed later.\n     */",
        "    /**\n     * 扫描成功时构建通过检查点列表，按扫描选项包含 Prompt 注入、数据外泄等项；\n"
        "     * 启用 LLM/Meta 分析时追加对应检查行。\n     */",
    ),
    (
        "    /**\n     * Extracts heading text from each {@code ### } line inside the {@code ## Findings} section.\n     */",
        "    /** 从 {@code ## Findings} 小节内提取所有 {@code ### } 标题文本。 */\n",
    ),
]
