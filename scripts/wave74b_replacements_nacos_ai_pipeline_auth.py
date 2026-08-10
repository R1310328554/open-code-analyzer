"""Chinese annotation replacements for Nacos 3.2.3 wave74b [15:30] ai pipeline/trace + default auth."""

R: dict[str, list[tuple[str, str]]] = {}

# --- SkillScannerPipelineService ---

R["plugin-default-impl/nacos-default-ai-pipeline-plugin/src/main/java/com/alibaba/nacos/plugin/ai/pipeline/spi/impl/SkillScannerPipelineService.java"] = [
    (
        "/**\n * Publish pipeline service that integrates Cisco AI Defense skill-scanner for security scanning\n"
        " * of AI Agent Skills before publishing.\n *\n * <p>Uses <a href=\"https://github.com/cisco-ai-defense/skill-scanner\">skill-scanner</a> to detect prompt\n"
        " * injection, data exfiltration, and malicious code patterns. Optional LLM semantic analysis via\n"
        " * node property {@code useLlm=true} and {@code llmApiKey}/{@code llmModel} (mapped to\n"
        " * {@code SKILL_SCANNER_LLM_*} in the subprocess environment). Rejects publishing if HIGH/CRITICAL\n"
        " * findings are detected.</p>\n *\n * <p>CLI uses {@code --format markdown --detailed} so stdout matches Cisco skill-scanner report\n"
        " * formats documented in the upstream project.</p>\n *\n * @author qiacheng.cxy\n */",
        "/**\n * 集成 Cisco AI Defense skill-scanner 的 AI 资源发布流水线服务。\n *\n"
        " * <p>在发布前对 Agent Skill 等 AI 资源做安全扫描，调用"
        " <a href=\"https://github.com/cisco-ai-defense/skill-scanner\">skill-scanner</a>"
        " 检测提示词注入、数据外泄与恶意代码模式。可通过节点属性 {@code useLlm=true} 及"
        " {@code llmApiKey}/{@code llmModel} 启用 LLM 语义分析（映射为子进程环境变量"
        " {@code SKILL_SCANNER_LLM_*}）。若发现 HIGH/CRITICAL 级别风险则拒绝发布。</p>\n *\n"
        " * <p>CLI 使用 {@code --format markdown --detailed}，stdout 格式与上游 skill-scanner 报告一致。</p>\n *\n"
        " * @author qiacheng.cxy\n */",
    ),
    (
        "    /**\n     * skill-scanner CLI command name.\n     */",
        "    /** skill-scanner 可执行命令默认名称。 */\n",
    ),
    (
        "    /**\n     * Report format for subprocess stdout ({@code skill-scanner --format ...}).\n     *\n"
        "     * @see <a href=\"https://github.com/cisco-ai-defense/skill-scanner\">skill-scanner</a> CLI {@code --format}\n"
        "     */",
        "    /**\n     * 子进程 stdout 报告格式（{@code skill-scanner --format ...}）。\n     *\n"
        "     * @see <a href=\"https://github.com/cisco-ai-defense/skill-scanner\">skill-scanner</a> CLI {@code --format}\n"
        "     */",
    ),
    (
        "    /**\n     * Installation hint when skill-scanner is not found.\n     */",
        "    /** skill-scanner 未安装时的安装指引文案。 */\n",
    ),
    (
        "    public SkillScannerPipelineService(boolean installed) {",
        "    /** 按是否已安装构造服务（未安装时 scannerCommand 为 null）。 */\n"
        "    public SkillScannerPipelineService(boolean installed) {",
    ),
    (
        "    public SkillScannerPipelineService(String scannerCommand) {",
        "    /** 指定 skill-scanner 可执行路径或命令名。 */\n"
        "    public SkillScannerPipelineService(String scannerCommand) {",
    ),
    (
        "    @Override\n    public String pipelineId() {",
        "    /** 流水线标识：skill-scanner。 */\n    @Override\n    public String pipelineId() {",
    ),
    (
        "    List<String> buildScanCommand(Path tempDir) {",
        "    /** 组装 skill-scanner scan 子进程命令行参数。 */\n    List<String> buildScanCommand(Path tempDir) {",
    ),
    (
        "    int waitForProcess(Process process) throws InterruptedException {",
        "    /** 等待扫描子进程结束并返回退出码（便于单测覆写）。 */\n"
        "    int waitForProcess(Process process) throws InterruptedException {",
    ),
    (
        "    private void writeResourceFiles(Path baseDir, List<ResourceFileContent> files)",
        "    /** 将待扫描资源文件写入临时目录，并校验路径不越界。 */\n"
        "    private void writeResourceFiles(Path baseDir, List<ResourceFileContent> files)",
    ),
    (
        "    private List<ResourceFileContent> normalizeFilesForScanner(PublishPipelineContext context,",
        "    /** 为 AgentSpec/Prompt 等资源合成 SKILL.md，以兼容 skill-scanner 输入格式。 */\n"
        "    private List<ResourceFileContent> normalizeFilesForScanner(PublishPipelineContext context,",
    ),
    (
        "    private boolean containsSkillMarkdown(List<ResourceFileContent> files) {",
        "    /** 判断文件列表是否已包含 SKILL.md。 */\n"
        "    private boolean containsSkillMarkdown(List<ResourceFileContent> files) {",
    ),
    (
        "    private void deleteRecursively(File file) {",
        "    /** 递归删除扫描临时目录。 */\n    private void deleteRecursively(File file) {",
    ),
    (
        "    @Override\n    public int getPreferOrder() {",
        "    /** 流水线执行优先级（数值越小越靠前）。 */\n    @Override\n    public int getPreferOrder() {",
    ),
    (
        "    @Override\n    public PublishPipelineResourceType[] pipelineResourceTypes() {",
        "    /** 适用的 AI 资源类型：Skill、AgentSpec、Prompt。 */\n"
        "    @Override\n    public PublishPipelineResourceType[] pipelineResourceTypes() {",
    ),
]

# --- SkillScannerPipelineServiceBuilder ---

R["plugin-default-impl/nacos-default-ai-pipeline-plugin/src/main/java/com/alibaba/nacos/plugin/ai/pipeline/spi/impl/SkillScannerPipelineServiceBuilder.java"] = [
    (
        "/**\n * Builder for {@link SkillScannerPipelineService}. Checks if skill-scanner is installed\n"
        " * during initialization and logs installation instructions if not found.\n *\n"
        " * <p>Optional node properties (via {@code nacos.plugin.ai-pipeline.skill-scanner.*}):</p>\n"
        " * <ul>\n"
        " *   <li>{@code useLlm} — {@code true} to pass {@code --use-llm} (semantic analysis; requires API key in properties or parent env)</li>\n"
        " *   <li>{@code llmApiKey} — sets subprocess {@code SKILL_SCANNER_LLM_API_KEY}</li>\n"
        " *   <li>{@code llmModel} — sets subprocess {@code SKILL_SCANNER_LLM_MODEL}</li>\n"
        " *   <li>{@code llmProvider} — {@code anthropic} or {@code openai} for {@code --llm-provider}</li>\n"
        " *   <li>{@code enableMeta} — {@code true} to pass {@code --enable-meta}</li>\n"
        " * </ul>\n *\n * @author qiacheng.cxy\n */",
        "/**\n * {@link SkillScannerPipelineService} 的 SPI 构建器。\n *\n"
        " * <p>初始化时检测 skill-scanner 是否可用，未安装则记录安装指引；"
        " 支持通过 {@code nacos.plugin.ai-pipeline.skill-scanner.*} 配置扫描选项：</p>\n"
        " * <ul>\n"
        " *   <li>{@code useLlm} — 为 {@code true} 时传递 {@code --use-llm}（语义分析，需 API Key）</li>\n"
        " *   <li>{@code llmApiKey} — 写入子进程 {@code SKILL_SCANNER_LLM_API_KEY}</li>\n"
        " *   <li>{@code llmModel} — 写入子进程 {@code SKILL_SCANNER_LLM_MODEL}</li>\n"
        " *   <li>{@code llmProvider} — {@code anthropic} 或 {@code openai}，对应 {@code --llm-provider}</li>\n"
        " *   <li>{@code enableMeta} — 为 {@code true} 时传递 {@code --enable-meta}</li>\n"
        " * </ul>\n *\n * @author qiacheng.cxy\n */",
    ),
    (
        "    /**\n     * Property key to override the scanner executable path or command.\n     */",
        "    /** 覆盖 scanner 可执行路径或命令名的配置键。 */\n",
    ),
    (
        "    /**\n     * Legacy alias for scanner executable path.\n     */\n    private static final String PROPERTY_EXECUTABLE = \"executable\";",
        "    /** 可执行路径的兼容配置键（executable）。 */\n"
        "    private static final String PROPERTY_EXECUTABLE = \"executable\";",
    ),
    (
        "    /**\n     * Legacy alias for scanner executable path.\n     */\n    private static final String PROPERTY_PATH = \"path\";",
        "    /** 可执行路径的兼容配置键（path）。 */\n    private static final String PROPERTY_PATH = \"path\";",
    ),
    (
        "    @Override\n    public String pipelineId() {",
        "    /** 返回流水线标识 skill-scanner。 */\n    @Override\n    public String pipelineId() {",
    ),
    (
        "    @Override\n    public PublishPipelineService build(Properties properties) {",
        "    /** 解析配置并构建 {@link SkillScannerPipelineService} 实例。 */\n"
        "    @Override\n    public PublishPipelineService build(Properties properties) {",
    ),
    (
        "    /**\n     * Resolve skill-scanner executable path from properties or PATH.\n     *\n"
        "     * @param properties pipeline node properties\n     * @return resolved command path, or {@code null} if not found\n"
        "     */",
        "    /**\n     * 从节点属性或系统 PATH 解析 skill-scanner 可执行路径。\n     *\n"
        "     * @param properties 流水线节点属性\n     * @return 可执行路径，未找到时返回 {@code null}\n"
        "     */",
    ),
    (
        "    private List<String> getConfiguredCandidates(Properties properties) {",
        "    /** 收集 command/executable/path 等配置项作为候选命令。 */\n"
        "    private List<String> getConfiguredCandidates(Properties properties) {",
    ),
    (
        "    private String resolveCandidate(String candidate) {",
        "    /** 解析单个候选：绝对路径校验或 PATH 查找。 */\n"
        "    private String resolveCandidate(String candidate) {",
    ),
    (
        "    private String findExecutableInPath(String command) {",
        "    /** 在 PATH 及 ~/.local/bin 中查找可执行文件。 */\n"
        "    private String findExecutableInPath(String command) {",
    ),
    (
        "    String getPathEnv() {",
        "    /** 读取 PATH 环境变量（便于单测覆写）。 */\n    String getPathEnv() {",
    ),
]

# --- SkillScannerScanOptions ---

R["plugin-default-impl/nacos-default-ai-pipeline-plugin/src/main/java/com/alibaba/nacos/plugin/ai/pipeline/spi/impl/SkillScannerScanOptions.java"] = [
    (
        "/**\n * Skill-scanner CLI options derived from pipeline node {@link Properties}.\n *\n"
        " * <p>Configure via {@code nacos.plugin.ai-pipeline.type=skill-scanner}\n"
        " * and matching {@code nacos.plugin.ai-pipeline.skill-scanner.&lt;key&gt;} entries\n"
        " * (see {@link com.alibaba.nacos.ai.pipeline.config.FilePipelineConfigProvider}).</p>\n *\n"
        " * <p>Environment variables for the LLM match\n"
        " * <a href=\"https://github.com/cisco-ai-defense/skill-scanner\">skill-scanner</a> documentation.</p>\n *\n"
        " * @author qiacheng.cxy\n */",
        "/**\n * 从流水线节点 {@link Properties} 解析的 skill-scanner CLI 扫描选项。\n *\n"
        " * <p>通过 {@code nacos.plugin.ai-pipeline.type=skill-scanner} 及"
        " {@code nacos.plugin.ai-pipeline.skill-scanner.&lt;key&gt;} 配置（参见"
        " {@link com.alibaba.nacos.ai.pipeline.config.FilePipelineConfigProvider}）。</p>\n *\n"
        " * <p>LLM 相关环境变量命名与"
        " <a href=\"https://github.com/cisco-ai-defense/skill-scanner\">skill-scanner</a> 官方文档一致。</p>\n *\n"
        " * @author qiacheng.cxy\n */",
    ),
    (
        "    static final String PROP_USE_LLM = \"useLlm\";",
        "    /** 是否启用 LLM 语义分析（--use-llm）。 */\n    static final String PROP_USE_LLM = \"useLlm\";",
    ),
    (
        "    static final String PROP_LLM_API_KEY = \"llmApiKey\";",
        "    /** LLM API Key 配置键。 */\n    static final String PROP_LLM_API_KEY = \"llmApiKey\";",
    ),
    (
        "    static final String PROP_LLM_MODEL = \"llmModel\";",
        "    /** LLM 模型名配置键。 */\n    static final String PROP_LLM_MODEL = \"llmModel\";",
    ),
    (
        "    static final String PROP_LLM_PROVIDER = \"llmProvider\";",
        "    /** LLM 提供商配置键（anthropic/openai）。 */\n    static final String PROP_LLM_PROVIDER = \"llmProvider\";",
    ),
    (
        "    static final String PROP_ENABLE_META = \"enableMeta\";",
        "    /** 是否启用元数据扫描（--enable-meta）。 */\n    static final String PROP_ENABLE_META = \"enableMeta\";",
    ),
    (
        "    static SkillScannerScanOptions none() {",
        "    /** 返回全部选项关闭的默认实例。 */\n    static SkillScannerScanOptions none() {",
    ),
    (
        "    static SkillScannerScanOptions fromProperties(Properties properties) {",
        "    /** 从节点属性解析扫描选项，空属性时返回 {@link #none()}。 */\n"
        "    static SkillScannerScanOptions fromProperties(Properties properties) {",
    ),
    (
        "    /**\n     * Applies LLM-related variables to the subprocess environment when configured.\n"
        "     * Keys match skill-scanner CLI expectations ({@value #ENV_LLM_API_KEY}, {@value #ENV_LLM_MODEL}).\n"
        "     */",
        "    /**\n     * 将已配置的 LLM 变量写入子进程环境。\n"
        "     * 键名符合 skill-scanner CLI 约定（{@value #ENV_LLM_API_KEY}、{@value #ENV_LLM_MODEL}）。\n"
        "     */",
    ),
]

# --- AiResourceTraceLogSubscriber ---

R["plugin-default-impl/nacos-default-ai-trace-plugin/src/main/java/com/alibaba/nacos/plugin/trace/ai/AiResourceTraceLogSubscriber.java"] = [
    (
        "/**\n * Default AI resource trace subscriber that keeps the existing file log output.\n *\n * @author nacos\n */",
        "/**\n * 默认 AI 资源追踪订阅者：将 {@link AiResourceTraceEvent} 以 JSON 写入专用 trace 日志。\n *\n"
        " * <p>订阅者名称为 {@value #NAME}，输出 logger 为"
        " {@code com.alibaba.nacos.ai.resource.trace}，保持与既有文件日志格式兼容。</p>\n *\n * @author nacos\n */",
    ),
    (
        "    public static final String NAME = \"ai-resource-trace-log\";",
        "    /** 追踪订阅者在 SPI 中的唯一名称。 */\n    public static final String NAME = \"ai-resource-trace-log\";",
    ),
    (
        "    @Override\n    public String getName() {",
        "    /** 返回订阅者名称 {@link #NAME}。 */\n    @Override\n    public String getName() {",
    ),
    (
        "    @Override\n    public void onEvent(TraceEvent event) {",
        "    /** 收到 AI 资源追踪事件时序列化为 JSON 并写入 info 日志。 */\n"
        "    @Override\n    public void onEvent(TraceEvent event) {",
    ),
    (
        "    @Override\n    public List<Class<? extends TraceEvent>> subscribeTypes() {",
        "    /** 仅订阅 {@link AiResourceTraceEvent} 类型。 */\n"
        "    @Override\n    public List<Class<? extends TraceEvent>> subscribeTypes() {",
    ),
    (
        "    static Map<String, Object> buildLogEntry(AiResourceTraceEvent event) {",
        "    /** 将追踪事件字段组装为结构化日志 Map（便于 JSON 输出）。 */\n"
        "    static Map<String, Object> buildLogEntry(AiResourceTraceEvent event) {",
    ),
]

# --- AnonymousAccessInitializer ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/AnonymousAccessInitializer.java"] = [
    (
        "/**\n * Initializes the system-reserved anonymous user, role and default permission when AI anonymous access is enabled.\n"
        " * Follows the same pattern as admin user initialization in Nacos.\n *\n * @author nacos\n */",
        "/**\n * AI 匿名访问启用时，初始化系统预留的匿名用户、角色与默认权限。\n *\n"
        " * <p>流程与 Nacos 管理员用户初始化一致：{@link PostConstruct} 阶段写入用户表、角色绑定及"
        " {@code public:*:ai/*} 只读权限。</p>\n *\n * @author nacos\n */",
    ),
    (
        "    private static final String DEFAULT_ANONYMOUS_PERMISSION_RESOURCE = \"public:*:ai/*\";",
        "    /** 匿名角色默认 AI 资源权限表达式。 */\n"
        "    private static final String DEFAULT_ANONYMOUS_PERMISSION_RESOURCE = \"public:*:ai/*\";",
    ),
    (
        "    private static final String DEFAULT_ANONYMOUS_PERMISSION_ACTION = \"r\";",
        "    /** 匿名默认权限动作：只读（r）。 */\n"
        "    private static final String DEFAULT_ANONYMOUS_PERMISSION_ACTION = \"r\";",
    ),
    (
        "    /**\n     * Initialize anonymous user, role and default permission if AI anonymous access is enabled.\n     */",
        "    /** 若开启 AI 匿名访问，则确保匿名用户、角色与默认权限存在。 */\n",
    ),
    (
        "    private void ensureAnonymousUser() {",
        "    /** 创建匿名用户（密码随机 BCrypt，不可用于登录）。 */\n    private void ensureAnonymousUser() {",
    ),
    (
        "    private void ensureAnonymousRole() {",
        "    /** 为匿名用户绑定 {@link AuthConstants#ANONYMOUS_ROLE} 角色。 */\n"
        "    private void ensureAnonymousRole() {",
    ),
    (
        "    private void ensureDefaultPermission() {",
        "    /** 为匿名角色授予 public AI 资源只读权限。 */\n    private void ensureDefaultPermission() {",
    ),
]

# --- DefaultAiVisibilityService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/DefaultAiVisibilityService.java"] = [
    (
        "/**\n * Default AI visibility service implementation for nacos auth plugin.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 默认鉴权插件的 AI 可见性服务实现。\n *\n"
        " * <p>校验资源可见性、为列表查询提供 {@link QueryAdvisor} 谓词，"
        " 并与 {@link AuthPluginService} 联动做细粒度权限校验。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private static final String RESOURCE_PREFIX = \"@@visibility\";",
        "    /** 可见性权限资源 ID 前缀。 */\n    private static final String RESOURCE_PREFIX = \"@@visibility\";",
    ),
    (
        "    private static final String ANONYMOUS_IDENTITY = AuthConstants.ANONYMOUS_USER;",
        "    /** 匿名身份标识，用于区分公开资源查询范围。 */\n"
        "    private static final String ANONYMOUS_IDENTITY = AuthConstants.ANONYMOUS_USER;",
    ),
    (
        "    @Override\n    public ValidationResult validateVisibility(String identity, String action, String apiType,",
        "    /** 校验当前身份对指定 AI 资源是否具有读/写可见性。 */\n"
        "    @Override\n    public ValidationResult validateVisibility(String identity, String action, String apiType,",
    ),
    (
        "    @Override\n    public QueryAdvisor adviseQuery(String identity, String action, String apiType,",
        "    /** 为列表查询推荐基础谓词与授权资源过滤条件。 */\n"
        "    @Override\n    public QueryAdvisor adviseQuery(String identity, String action, String apiType,",
    ),
    (
        "        // TODO: populate explicit authorized resources from auth plugin once query advisor integration is complete.",
        "        // TODO: 查询顾问与鉴权插件深度集成后，填充显式授权资源列表",
    ),
    (
        "    @Override\n    public String getVisibilityServiceName() {",
        "    /** 返回可见性服务名称（与鉴权插件类型一致）。 */\n"
        "    @Override\n    public String getVisibilityServiceName() {",
    ),
    (
        "    private boolean isPermitted(String currentUser, boolean isRead, VisibilityResource candidate) {",
        "    /** 综合所有者、公开范围与 RBAC 权限判断是否允许访问。 */\n"
        "    private boolean isPermitted(String currentUser, boolean isRead, VisibilityResource candidate) {",
    ),
    (
        "    private String buildResourceIdentifier(VisibilityResource res) {",
        "    /** 构造 @@visibility 命名空间下的权限资源标识。 */\n"
        "    private String buildResourceIdentifier(VisibilityResource res) {",
    ),
    (
        "    private boolean checkResourcePermission(VisibilityResource res, String action) {",
        "    /** 委托鉴权插件校验指定资源的读/写权限。 */\n"
        "    private boolean checkResourcePermission(VisibilityResource res, String action) {",
    ),
    (
        "    private boolean isAuthDisabled(String apiType) {",
        "    /** 判断当前 API 作用域是否关闭鉴权。 */\n    private boolean isAuthDisabled(String apiType) {",
    ),
    (
        "    private boolean isAnonymousIdentity(String identity) {",
        "    /** 是否为匿名用户身份。 */\n    private boolean isAnonymousIdentity(String identity) {",
    ),
    (
        "    private boolean isCurrentIdentityGlobalAdmin(String identity) {",
        "    /** 当前请求上下文中的用户是否为全局管理员。 */\n"
        "    private boolean isCurrentIdentityGlobalAdmin(String identity) {",
    ),
]

# --- JwtAuthenticationEntryPoint ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/JwtAuthenticationEntryPoint.java"] = [
    (
        "/**\n * jwt auth fail point.\n *\n * @author wfnuser\n */",
        "/**\n * JWT 认证失败时的 Spring Security 入口点（已废弃）。\n *\n"
        " * <p>认证异常时记录错误日志并向客户端返回 HTTP 401 Unauthorized。</p>\n *\n * @author wfnuser\n */",
    ),
    (
        "    @Override\n    public void commence(HttpServletRequest request, HttpServletResponse response,",
        "    /** 处理未认证请求：写日志并发送 401 响应。 */\n"
        "    @Override\n    public void commence(HttpServletRequest request, HttpServletResponse response,",
    ),
]

# --- NacosAuthPluginService ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/NacosAuthPluginService.java"] = [
    (
        "/**\n * Nacos default auth plugin service implementation.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 内置鉴权插件 {@link AuthPluginService} 实现。\n *\n"
        " * <p>负责身份识别（JWT/用户名密码）、权限校验，并在资源标记允许时降级为 AI 匿名访问。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public Collection<String> identityNames() {",
        "    /** 返回请求中可携带的身份凭证字段名列表。 */\n"
        "    @Override\n    public Collection<String> identityNames() {",
    ),
    (
        "    @Override\n    public boolean enableAuth(ActionTypes action, String type) {",
        "    /** 对所有 action 与 type 均启用鉴权。 */\n"
        "    @Override\n    public boolean enableAuth(ActionTypes action, String type) {",
    ),
    (
        "        // enable all of action and type",
        "        // 不区分 action/type，一律开启鉴权",
    ),
    (
        "    @Override\n    public AuthResult validateIdentity(IdentityContext identityContext, Resource resource) {",
        "    /** 校验用户身份；失败且资源允许匿名时降级为匿名用户。 */\n"
        "    @Override\n    public AuthResult validateIdentity(IdentityContext identityContext, Resource resource) {",
    ),
    (
        "    private boolean isAnonymousAllowed(Resource resource) {",
        "    /** 判断资源是否标记允许 AI 匿名访问且全局开关已开启。 */\n"
        "    private boolean isAnonymousAllowed(Resource resource) {",
    ),
    (
        "    private NacosUser validateUser(IdentityContext identityContext) throws AccessException {",
        "    /** 优先 JWT，其次用户名密码，成功后将用户写入 IdentityContext。 */\n"
        "    private NacosUser validateUser(IdentityContext identityContext) throws AccessException {",
    ),
    (
        "    private String resolveToken(IdentityContext identityContext) {",
        "    /** 从 Authorization Bearer 或 accessToken 参数解析 JWT。 */\n"
        "    private String resolveToken(IdentityContext identityContext) {",
    ),
    (
        "    @Override\n    public AuthResult validateAuthority(IdentityContext identityContext, Permission permission) {",
        "    /** 校验已认证用户对指定资源的操作权限。 */\n"
        "    @Override\n    public AuthResult validateAuthority(IdentityContext identityContext, Permission permission) {",
    ),
    (
        "    @Override\n    public String getAuthServiceName() {",
        "    /** 返回鉴权插件类型名 nacos。 */\n    @Override\n    public String getAuthServiceName() {",
    ),
    (
        "    @Override\n    public boolean isLoginEnabled() {",
        "    /** 控制台 API 是否启用登录鉴权。 */\n    @Override\n    public boolean isLoginEnabled() {",
    ),
    (
        "    /**\n     * Only auth enabled and not global admin role existed.\n     *\n"
        "     * @return {@code true} when auth enabled and not global admin role existed, otherwise {@code false}\n"
        "     */",
        "    /**\n     * 鉴权已开启且尚未配置全局管理员角色时为 true（需引导初始化管理员）。\n     *\n"
        "     * @return {@code true} when auth enabled and not global admin role existed, otherwise {@code false}\n"
        "     */",
    ),
    (
        "    protected void checkNacosAuthManager() {",
        "    /** 懒加载 {@link IAuthenticationManager} Bean。 */\n    protected void checkNacosAuthManager() {",
    ),
]

# --- SafeBcryptPasswordEncoder ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/SafeBcryptPasswordEncoder.java"] = [
    (
        "/**\n * BCrypt encoder that fixes the password length vulnerability.\n *\n"
        " * <p>Problem solved: When password length exceeds 72 characters, the original {@link BCryptPasswordEncoder}\n"
        " * only matches the first 72 characters, which could lead to different passwords being\n"
        " * validated as matching (e.g., passwords {@code \"A\".repeat(73)} and {@code \"A\".repeat(80)}\n"
        " * would be considered identical).\n *\n"
        " * <p>Fix logic: Adds length validation in {@link #matches(CharSequence, String)},\n"
        " * returning false directly if the password length exceeds 72.\n *\n"
        " * <p><strong>Recommendation:</strong> It is advised to add password length validation\n"
        " * during user registration/password modification to prevent login failures caused\n"
        " * by historical data issues.\n *\n"
        " * @see <a href=\"https://github.com/advisories/GHSA-mg83-c7gq-rv5c\">Spring Security Password Length Vulnerability Advisory</a>\n"
        " * @author linwumignshi\n */",
        "/**\n * 修复 BCrypt 密码长度漏洞的安全密码编码器。\n *\n"
        " * <p>问题：原 {@link BCryptPasswordEncoder} 仅比较前 72 字符，超长密码可能被误判为相同。</p>\n *\n"
        " * <p>修复：在 {@link #matches(CharSequence, String)} 中若明文长度超过"
        " {@link AuthConstants#MAX_PASSWORD_LENGTH} 则直接返回 false。</p>\n *\n"
        " * <p><strong>建议：</strong>注册与改密流程也应限制密码长度，避免历史脏数据导致无法登录。</p>\n *\n"
        " * @see <a href=\"https://github.com/advisories/GHSA-mg83-c7gq-rv5c\">Spring Security Password Length Vulnerability Advisory</a>\n"
        " * @author linwumignshi\n */",
    ),
    (
        "        // Reject excessively long passwords immediately",
        "        // 超长密码直接拒绝，避免 BCrypt 截断比较",
    ),
]

# --- AbstractAuthenticationManager ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/authenticate/AbstractAuthenticationManager.java"] = [
    (
        "/**\n * AbstractAuthenticationManager.\n *\n * @author Weizhan▪Yun\n * @date 2023/1/13 12:48\n */",
        "/**\n * 认证管理器抽象实现：封装用户名密码、JWT 与 HTTP 请求三种认证入口及 RBAC 授权。\n *\n * @author Weizhan▪Yun\n * @date 2023/1/13 12:48\n */",
    ),
    (
        "    @Override\n    public NacosUser authenticate(String username, String rawPassword) throws AccessException {",
        "    /** 用户名密码认证，成功后签发 JWT 并返回 {@link NacosUser}。 */\n"
        "    @Override\n    public NacosUser authenticate(String username, String rawPassword) throws AccessException {",
    ),
    (
        "    @Override\n    public NacosUser authenticate(String token) throws AccessException {",
        "    /** 解析并校验 JWT，返回对应 {@link NacosUser}。 */\n"
        "    @Override\n    public NacosUser authenticate(String token) throws AccessException {",
    ),
    (
        "    @Override\n    public NacosUser authenticate(HttpServletRequest httpServletRequest) throws AccessException {",
        "    /** 从 HTTP 请求提取 token 或表单凭证并完成认证。 */\n"
        "    @Override\n    public NacosUser authenticate(HttpServletRequest httpServletRequest) throws AccessException {",
    ),
    (
        "    @Override\n    public void authorize(Permission permission, NacosUser nacosUser) throws AccessException {",
        "    /** 全局管理员或具备 RBAC 权限时放行，否则抛出 {@link AccessException}。 */\n"
        "    @Override\n    public void authorize(Permission permission, NacosUser nacosUser) throws AccessException {",
    ),
    (
        "    private String resolveToken(HttpServletRequest request) {",
        "    /** 从 Authorization 头或 accessToken 参数解析 Bearer JWT。 */\n"
        "    private String resolveToken(HttpServletRequest request) {",
    ),
    (
        "    @Override\n    public boolean hasGlobalAdminRole(String username) {",
        "    /** 指定用户名是否拥有全局管理员角色。 */\n"
        "    @Override\n    public boolean hasGlobalAdminRole(String username) {",
    ),
    (
        "    @Override\n    public boolean hasGlobalAdminRole() {",
        "    /** 系统中是否存在全局管理员角色。 */\n    @Override\n    public boolean hasGlobalAdminRole() {",
    ),
    (
        "    @Override\n    public boolean hasGlobalAdminRole(NacosUser nacosUser) {",
        "    /** 判断用户是否为全局管理员并回写 {@link NacosUser#setGlobalAdmin}。 */\n"
        "    @Override\n    public boolean hasGlobalAdminRole(NacosUser nacosUser) {",
    ),
]

# --- DefaultAuthenticationManager ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/authenticate/DefaultAuthenticationManager.java"] = [
    (
        "/**\n * DefaultAuthenticationManager.\n *\n * @author Weizhan▪Yun\n * @date 2023/1/17 13:27\n */",
        "/**\n * {@link IAuthenticationManager} 默认实现，直接继承 {@link AbstractAuthenticationManager} 逻辑。\n *\n * @author Weizhan▪Yun\n * @date 2023/1/17 13:27\n */",
    ),
    (
        "    public DefaultAuthenticationManager(NacosUserService userDetailsService,",
        "    /** 注入用户、JWT 与角色服务并委托父类完成认证授权。 */\n"
        "    public DefaultAuthenticationManager(NacosUserService userDetailsService,",
    ),
]

# --- IAuthenticationManager ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/authenticate/IAuthenticationManager.java"] = [
    (
        "/**\n * Authentication interface.\n *\n * @author Weizhan▪Yun\n * @date 2023/1/12 23:31\n */",
        "/**\n * Nacos 鉴权认证管理器接口：统一用户名密码、JWT 与 HTTP 请求认证及 RBAC 授权。\n *\n * @author Weizhan▪Yun\n * @date 2023/1/12 23:31\n */",
    ),
    (
        "    /**\n     * Authentication of user with password.\n     *\n     * @param username    username\n"
        "     * @param rawPassword raw password\n     * @return user related to this request, null if no user info is found.\n"
        "     * @throws AccessException if authentication is failed\n     */",
        "    /**\n     * 用户名密码认证。\n     *\n     * @param username    username\n     * @param rawPassword raw password\n"
        "     * @return 认证成功后的 {@link NacosUser}\n     * @throws AccessException if authentication is failed\n     */",
    ),
    (
        "    /**\n     * Authentication with jwt.\n     *\n     * @param jwtToken json web token\n     * @return nacos user\n"
        "     * @throws AccessException if authentication is failed\n     */",
        "    /**\n     * JWT Token 认证。\n     *\n     * @param jwtToken json web token\n     * @return nacos user\n"
        "     * @throws AccessException if authentication is failed\n     */",
    ),
    (
        "    /**\n     * Authentication of request, identify the user who request the resource.\n     *\n"
        "     * @param httpServletRequest http servlet request\n     * @return nacos user\n     * @throws AccessException if authentication is failed\n"
        "     */",
        "    /**\n     * 从 HTTP 请求识别并认证访问用户。\n     *\n     * @param httpServletRequest http servlet request\n"
        "     * @return nacos user\n     * @throws AccessException if authentication is failed\n     */",
    ),
    (
        "    /**\n     * Authorize if the nacosUser has the specified permission.\n     *\n     * @param permission permission to auth\n"
        "     * @param nacosUser  nacosUser who wants to access the resource.\n     * @throws AccessException if authorization is failed\n"
        "     */",
        "    /**\n     * 校验 {@link NacosUser} 是否具备指定 {@link Permission}。\n     *\n     * @param permission permission to auth\n"
        "     * @param nacosUser  nacosUser who wants to access the resource.\n     * @throws AccessException if authorization is failed\n     */",
    ),
    (
        "    /**\n     * Whether the user has the administrator role.\n     *\n     * @param username nacos user name\n"
        "     * @return if the user has the administrator role.\n     */",
        "    /**\n     * 指定用户是否拥有全局管理员角色。\n     *\n     * @param username nacos user name\n"
        "     * @return if the user has the administrator role.\n     */",
    ),
    (
        "    /**\n     * Whether the user exist the administrator role.\n     *\n     * @return if the user exist the administrator role.\n"
        "     */",
        "    /**\n     * 系统中是否已存在全局管理员角色。\n     *\n     * @return if the user exist the administrator role.\n     */",
    ),
    (
        "    /**\n     * Whether the user has the administrator role.\n     *\n     * @param nacosUser nacos user name\n"
        "     * @return if the user has the administrator role.\n     */",
        "    /**\n     * 给定 {@link NacosUser} 是否为全局管理员。\n     *\n     * @param nacosUser nacos user name\n"
        "     * @return if the user has the administrator role.\n     */",
    ),
]

# --- ConditionOnInnerDatasource ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/condition/ConditionOnInnerDatasource.java"] = [
    (
        "/**\n * When nacos deployment type is `merged` or `server`.\n *\n * @author xiweng.yy\n */",
        "/**\n * Spring 条件：Nacos 部署类型为 merged 或 server（非独立 Console）。\n *\n"
        " * <p>用于仅在 Server 进程加载依赖内嵌数据源的用户/权限持久化 Bean。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public boolean matches(ConditionContext conditionContext,",
        "    /** 部署类型不是 console 时匹配（即 merged/server 模式）。 */\n"
        "    @Override\n    public boolean matches(ConditionContext conditionContext,",
    ),
]

# --- ConditionOnNacosAuth ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/condition/ConditionOnNacosAuth.java"] = [
    (
        "/**\n * when nacos.core.auth.system.type=nacos\n *\n * @author karsonto\n */",
        "/**\n * Spring 条件：{@code nacos.core.auth.system.type} 为 nacos 内置鉴权。\n *\n"
        " * <p>仅在该配置下注册默认鉴权插件相关 Bean。</p>\n *\n * @author karsonto\n */",
    ),
    (
        "    @Override\n    public boolean matches(ConditionContext conditionContext,",
        "    /** 读取环境属性并判断是否为 Nacos 内置鉴权类型。 */\n"
        "    @Override\n    public boolean matches(ConditionContext conditionContext,",
    ),
]

# --- ConditionOnRemoteDatasource ---

R["plugin-default-impl/nacos-default-auth-plugin/src/main/java/com/alibaba/nacos/plugin/auth/impl/condition/ConditionOnRemoteDatasource.java"] = [
    (
        "/**\n * When nacos deployment type is `console`.\n *\n * @author xiweng.yy\n */",
        "/**\n * Spring 条件：Nacos 部署类型为独立 Console（console）。\n *\n"
        " * <p>Console 使用远程数据源访问 Server 侧用户权限数据时加载对应 Bean。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    @Override\n    public boolean matches(ConditionContext conditionContext,",
        "    /** 部署类型为 console 时匹配。 */\n"
        "    @Override\n    public boolean matches(ConditionContext conditionContext,",
    ),
]
