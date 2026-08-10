"""Chinese annotation replacements for Nacos 3.2.3 wave38b [15:30] config gray/monitor."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/AbstractGrayRule.java": [
        (
            "/**\n * Gray rule. type with version determined parse logic.\n *\n * @author shiyiyue\n */",
            "/**\n * 灰度规则抽象基类：由 type 与 version 决定具体解析与匹配逻辑。\n"
            " * 构造时解析原始表达式，解析失败则标记为无效；子类实现 {@link #parse} 与 {@link #match}。\n"
            " * Gray rule. type with version determined parse logic.\n *\n * @author shiyiyue\n */",
        ),
        (
            "    protected String rawGrayRuleExp;",
            "    /** 原始灰度规则表达式字符串 */\n    protected String rawGrayRuleExp;",
        ),
        (
            "    protected int priority;",
            "    /** 规则优先级，数值越大越优先匹配 */\n    protected int priority;",
        ),
        (
            "    protected volatile boolean valid = true;",
            "    /** 规则是否有效（解析成功且语义合法） */\n    protected volatile boolean valid = true;",
        ),
        (
            "    public AbstractGrayRule() {",
            "    /** 无参构造，供 SPI 与反射实例化使用 */\n    public AbstractGrayRule() {",
        ),
        (
            "    public AbstractGrayRule(String rawGrayRuleExp, int priority) {",
            "    /**\n     * 根据原始表达式与优先级构造灰度规则。\n     *\n     * @param rawGrayRuleExp 原始灰度表达式\n     * @param priority       匹配优先级\n     */\n"
            "    public AbstractGrayRule(String rawGrayRuleExp, int priority) {",
        ),
        (
            "    /**\n     * parse gray rule.\n     *\n     * @param rawGrayRule raw gray rule.\n     * @throws NacosException if parse failed.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 解析原始灰度规则表达式为内部结构。\n     *\n     * @param rawGrayRule 原始灰度规则字符串\n     * @throws NacosException 解析失败时抛出\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * match gray rule.\n     *\n     * @param labels conn labels.\n     * @return true if match.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 判断客户端连接标签是否命中本灰度规则。\n     *\n     * @param labels 连接侧标签 Map（如 ClientIp、VipserverTag 等）\n     * @return 命中返回 true\n     * @date 2024/3/14\n     */",
        ),
        (
            "    public boolean isValid() {",
            "    /** 规则是否在构造/解析后仍有效 */\n    public boolean isValid() {",
        ),
        (
            "    /**\n     * get type.\n     *\n     * @return gray rule type.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 获取灰度规则类型标识（如 beta、tag、tagv2）。\n     *\n     * @return 规则 type\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * get version.\n     *\n     * @return gray rule version.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 获取灰度规则版本号，与 type 共同唯一定位实现类。\n     *\n     * @return 规则 version\n     * @date 2024/3/14\n     */",
        ),
        (
            "    public String getRawGrayRuleExp() {",
            "    /** 获取持久化/展示用的原始表达式 */\n    public String getRawGrayRuleExp() {",
        ),
        (
            "    public int getPriority() {",
            "    /** 获取规则优先级 */\n    public int getPriority() {",
        ),
        (
            "    public void setPriority(int priority) {",
            "    /** 设置规则优先级 */\n    public void setPriority(int priority) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/AbstractTagMatchGrayRule.java": [
        (
            "/**\n * description.\n *\n * @author rong\n * @date 2024-03-13 14:31\n */",
            "/**\n * 基于标签键值匹配的灰度规则抽象基类：定义表达式正则常量与格式校验。\n"
            " * 子类实现单标签或多标签组合匹配；相等性比较包含表达式、优先级、type 与 version。\n"
            " * description.\n *\n * @author rong\n * @date 2024-03-13 14:31\n */",
        ),
        (
            "    protected static final String EQUAL_PATTERN = \"=\";",
            "    /** 键值分隔符模式（等号） */\n    protected static final String EQUAL_PATTERN = \"=\";",
        ),
        (
            "    protected static final String KEY_PATTERN = \"[a-zA-Z0-9-_:\\\\.]+\";",
            "    /** 标签键合法字符模式 */\n    protected static final String KEY_PATTERN = \"[a-zA-Z0-9-_:\\.]+\";",
        ),
        (
            "    protected static final String VALUE_SPLITER_PATTERN = \",\";",
            "    /** 多值分隔符模式（逗号） */\n    protected static final String VALUE_SPLITER_PATTERN = \",\";",
        ),
        (
            "    protected static final String VALUE_PATTERN =",
            "    /** 标签值列表的正则模式（逗号分隔多个值） */\n    protected static final String VALUE_PATTERN =",
        ),
        (
            "    public AbstractTagMatchGrayRule() {",
            "    /** 无参构造 */\n    public AbstractTagMatchGrayRule() {",
        ),
        (
            "    public AbstractTagMatchGrayRule(String rawGrayRuleExp, int priority) {",
            "    /**\n     * 构造标签匹配灰度规则。\n     *\n     * @param rawGrayRuleExp 原始表达式\n     * @param priority       优先级\n     */\n"
            "    public AbstractTagMatchGrayRule(String rawGrayRuleExp, int priority) {",
        ),
        (
            "    protected void isPatternMatch(String rawString, String pattern) throws NacosException {",
            "    /**\n     * 校验字符串是否符合指定正则，不匹配则抛出参数异常。\n     *\n     * @param rawString 待校验字符串\n     * @param pattern   正则模式\n     * @throws NacosException 格式不合法\n     */\n"
            "    protected void isPatternMatch(String rawString, String pattern) throws NacosException {",
        ),
        (
            "    /**\n     * Check whether another tag match gray rule has the same expression, priority, type and version.\n     *\n     * @param obj another object.\n     * @return true if equals.\n     */",
            "    /**\n     * 判断与另一标签灰度规则是否在表达式、优先级、type 与 version 上完全一致。\n     *\n     * @param obj 待比较对象\n     * @return 等价返回 true\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/BetaGrayRule.java": [
        (
            "/**\n * beta gray rule for beta ips.\n * @author shiyiyue1102\n */",
            "/**\n * Beta 灰度规则：按客户端 IP 白名单决定是否下发 Beta 配置。\n"
            " * 表达式为逗号分隔的 IP 列表，匹配标签 {@link #CLIENT_IP_LABEL}。\n"
            " * beta gray rule for beta ips.\n * @author shiyiyue1102\n */",
        ),
        (
            "    Set<String> betaIps;",
            "    /** 允许命中 Beta 灰度的客户端 IP 集合 */\n    Set<String> betaIps;",
        ),
        (
            "    public static final String CLIENT_IP_LABEL = \"ClientIp\";",
            "    /** 连接标签中客户端 IP 的键名 */\n    public static final String CLIENT_IP_LABEL = \"ClientIp\";",
        ),
        (
            "    public static final String TYPE_BETA = \"beta\";",
            "    /** Beta 灰度规则类型标识 */\n    public static final String TYPE_BETA = \"beta\";",
        ),
        (
            "    public static final String VERSION = \"1.0.0\";",
            "    /** Beta 规则版本号 */\n    public static final String VERSION = \"1.0.0\";",
        ),
        (
            "    public static final int PRIORITY = Integer.MAX_VALUE;",
            "    /** Beta 规则默认最高优先级 */\n    public static final int PRIORITY = Integer.MAX_VALUE;",
        ),
        (
            "    public BetaGrayRule() {",
            "    /** 无参构造，供 SPI 加载 */\n    public BetaGrayRule() {",
        ),
        (
            "    public BetaGrayRule(String betaIps, int priority) {",
            "    /**\n     * 根据 IP 列表字符串与优先级构造 Beta 规则。\n     *\n     * @param betaIps  逗号分隔 IP 表达式\n     * @param priority 优先级\n     */\n"
            "    public BetaGrayRule(String betaIps, int priority) {",
        ),
        (
            "    /**\n     * parse beta gray rule.\n     * @param rawGrayRule raw gray rule.\n     * @throws NacosException exception.\n     */",
            "    /**\n     * 解析逗号分隔的 IP 列表为 {@link #betaIps} 集合。\n     *\n     * @param rawGrayRule 原始 IP 表达式\n     * @throws NacosException 解析异常\n     */",
        ),
        (
            "    @Override\n    \n    public boolean match(Map<String, String> labels) {",
            "    /** 客户端 IP 标签存在且落在白名单内则命中 */\n    @Override\n    public boolean match(Map<String, String> labels) {",
        ),
        (
            "    @Override\n    public String getType() {",
            "    /** 返回 {@link #TYPE_BETA} */\n    @Override\n    public String getType() {",
        ),
        (
            "    @Override\n    public String getVersion() {",
            "    /** 返回 {@link #VERSION} */\n    @Override\n    public String getVersion() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/ConfigGrayPersistInfo.java": [
        (
            "/**\n * description.\n *\n * @author rong\n * @date 2024-03-14 10:57\n */",
            "/**\n * 灰度规则持久化 DTO：以 JSON 形式存入数据库，含 type、version、表达式与优先级。\n"
            " * 与 {@link GrayRuleManager} 配合完成序列化/反序列化及实例构造。\n"
            " * description.\n *\n * @author rong\n * @date 2024-03-14 10:57\n */",
        ),
        (
            "    private String type;",
            "    /** 灰度规则类型（如 beta、tag、tagv2） */\n    private String type;",
        ),
        (
            "    private String version;",
            "    /** 规则版本号，与 type 联合定位实现类 */\n    private String version;",
        ),
        (
            "    private String expr;",
            "    /** 原始灰度表达式字符串 */\n    private String expr;",
        ),
        (
            "    private int priority;",
            "    /** 规则匹配优先级 */\n    private int priority;",
        ),
        (
            "    public ConfigGrayPersistInfo(String type, String version, String expr, int priority) {",
            "    /**\n     * 构造持久化信息。\n     *\n     * @param type     规则类型\n     * @param version  规则版本\n     * @param expr     原始表达式\n     * @param priority 优先级\n     */\n"
            "    public ConfigGrayPersistInfo(String type, String version, String expr, int priority) {",
        ),
        (
            "    public String getType() {",
            "    /** 获取规则类型 */\n    public String getType() {",
        ),
        (
            "    public void setType(String type) {",
            "    /** 设置规则类型 */\n    public void setType(String type) {",
        ),
        (
            "    public String getVersion() {",
            "    /** 获取规则版本 */\n    public String getVersion() {",
        ),
        (
            "    public void setVersion(String version) {",
            "    /** 设置规则版本 */\n    public void setVersion(String version) {",
        ),
        (
            "    public String getExpr() {",
            "    /** 获取原始表达式 */\n    public String getExpr() {",
        ),
        (
            "    public void setExpr(String expr) {",
            "    /** 设置原始表达式 */\n    public void setExpr(String expr) {",
        ),
        (
            "    public int getPriority() {",
            "    /** 获取优先级 */\n    public int getPriority() {",
        ),
        (
            "    public void setPriority(int priority) {",
            "    /** 设置优先级 */\n    public void setPriority(int priority) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/GrayRule.java": [
        (
            "/**\n * gray rule.\n *\n * @author rong\n */",
            "/**\n * 配置灰度规则 SPI 接口：定义匹配、有效性、类型版本及优先级等契约。\n"
            " * 各实现通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册至 {@link GrayRuleManager}。\n"
            " * gray rule.\n *\n * @author rong\n */",
        ),
        (
            "    /**\n    * gray rule match labels or not.\n    *\n    * @date 2024/3/14\n    * @param labels conn labels.\n    * @return true if match, false otherwise.\n    */",
            "    /**\n    * 判断连接标签是否命中灰度规则。\n    *\n    * @date 2024/3/14\n    * @param labels 连接侧标签 Map\n    * @return 命中返回 true，否则 false\n    */",
        ),
        (
            "    /**\n    * if the gray rule is valid.\n    *\n    * @date 2024/3/14\n    * @return true if valid, false otherwise.\n    */",
            "    /**\n    * 规则是否有效（表达式可解析且语义合法）。\n    *\n    * @date 2024/3/14\n    * @return 有效返回 true\n    */",
        ),
        (
            "    /**\n    * get gray rule type.\n    *\n    * @date 2024/3/14\n    * @return the gray rule type.\n    */",
            "    /**\n    * 获取规则类型标识。\n    *\n    * @date 2024/3/14\n    * @return 灰度规则 type\n    */",
        ),
        (
            "    /**\n    * get gray rule version.\n    *\n    * @date 2024/3/14\n    * @return the gray rule version.\n    */",
            "    /**\n    * 获取规则版本号。\n    *\n    * @date 2024/3/14\n    * @return 灰度规则 version\n    */",
        ),
        (
            "    /**\n    * get gray rule priority.\n    *\n    * @date 2024/3/14\n    * @return the gray rule priority.\n    */",
            "    /**\n    * 获取规则匹配优先级。\n    *\n    * @date 2024/3/14\n    * @return 优先级数值\n    */",
        ),
        (
            "    /**\n    * get raw String of gray rule.\n    *\n    * @date 2024/3/14\n    * @return the raw String of gray rule.\n    */",
            "    /**\n    * 获取原始灰度表达式字符串（持久化用）。\n    *\n    * @date 2024/3/14\n    * @return 原始表达式\n    */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/GrayRuleManager.java": [
        (
            "/**\n * GrayRuleManager.\n *\n * @author zunfei.lzf\n */",
            "/**\n * 灰度规则管理器：通过 SPI 收集 type+version 到实现类的映射，负责构造、序列化与反序列化。\n"
            " * 持久化形态为 {@link ConfigGrayPersistInfo} JSON，运行时还原为 {@link GrayRule} 实例。\n"
            " * GrayRuleManager.\n *\n * @author zunfei.lzf\n */",
        ),
        (
            "    private static final Map<String, Class<?>> GRAY_RULE_MAP = new ConcurrentHashMap<>(8);",
            "    /** type_version 键到 GrayRule 实现类的并发映射表 */\n    private static final Map<String, Class<?>> GRAY_RULE_MAP = new ConcurrentHashMap<>(8);",
        ),
        (
            "    public static final String SPLIT = \"_\";",
            "    /** type 与 version 拼接分隔符 */\n    public static final String SPLIT = \"_\";",
        ),
        (
            "    /**\n     * get class by type and version.\n     *\n     * @param type    type.\n     * @param version version.\n     * @return class.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 按 type 与 version 查找已注册的 GrayRule 实现类。\n     *\n     * @param type    规则类型\n     * @param version 规则版本\n     * @return 实现 Class，未注册则 null\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * construct gray rule.\n     *\n     * @param configGrayPersistInfo config gray persist info.\n     * @return gray rule.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 由持久化 DTO 反射构造 GrayRule 实例（String, int 构造器）。\n     *\n     * @param configGrayPersistInfo 持久化信息\n     * @return 灰度规则实例，类型未注册时 null\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * construct config gray persist info.\n     *\n     * @param grayRule gray rule.\n     * @return config gray persist info.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 将运行时 GrayRule 转为可持久化的 DTO。\n     *\n     * @param grayRule 灰度规则实例\n     * @return ConfigGrayPersistInfo\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * deserialize config gray persist info.\n     *\n     * @param grayRuleRawStringFromDb gray rule raw string from db.\n     * @return config gray persist info.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 从数据库 JSON 字符串反序列化为持久化 DTO。\n     *\n     * @param grayRuleRawStringFromDb 数据库中的 JSON\n     * @return ConfigGrayPersistInfo\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * serialize config gray persist info.\n     *\n     * @param configGrayPersistInfo config gray persist info.\n     * @return serialized string.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 将持久化 DTO 序列化为 JSON 字符串写入数据库。\n     *\n     * @param configGrayPersistInfo 持久化对象\n     * @return JSON 字符串\n     * @date 2024/3/14\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/TagGrayRule.java": [
        (
            "/**\n * Tag gray rule.\n *\n * @author shiyiyue\n */",
            "/**\n * 传统 Tag 灰度规则：按 VipserverTag 标签值精确匹配，用于旧版 Tag 配置下发。\n"
            " * 表达式即为目标 tag 值；优先级仅次于 Beta 规则。\n"
            " * Tag gray rule.\n *\n * @author shiyiyue\n */",
        ),
        (
            "    String tagValue;",
            "    /** 期望匹配的 VipserverTag 值 */\n    String tagValue;",
        ),
        (
            "    public static final String VIP_SERVER_TAG_LABEL = VIPSERVER_TAG;",
            "    /** 连接标签中 VipserverTag 的键名 */\n    public static final String VIP_SERVER_TAG_LABEL = VIPSERVER_TAG;",
        ),
        (
            "    public static final String TYPE_TAG = \"tag\";",
            "    /** Tag 灰度规则类型标识 */\n    public static final String TYPE_TAG = \"tag\";",
        ),
        (
            "    public static final String VERSION = \"1.0.0\";",
            "    /** Tag 规则版本号 */\n    public static final String VERSION = \"1.0.0\";",
        ),
        (
            "    public static final int PRIORITY = Integer.MAX_VALUE - 1;",
            "    /** Tag 规则默认优先级（略低于 Beta） */\n    public static final int PRIORITY = Integer.MAX_VALUE - 1;",
        ),
        (
            "    public TagGrayRule() {",
            "    /** 无参构造，供 SPI 加载 */\n    public TagGrayRule() {",
        ),
        (
            "    public TagGrayRule(String rawGrayRuleExp, int priority) {",
            "    /**\n     * 构造 Tag 灰度规则。\n     *\n     * @param rawGrayRuleExp 目标 tag 值表达式\n     * @param priority       优先级\n     */\n"
            "    public TagGrayRule(String rawGrayRuleExp, int priority) {",
        ),
        (
            "    @Override\n    protected void parse(String rawGrayRule) throws NacosException {",
            "    /** 将原始表达式直接作为 tagValue；空串则跳过 */\n    @Override\n    protected void parse(String rawGrayRule) throws NacosException {",
        ),
        (
            "    @Override\n    public boolean match(Map<String, String> labels) {",
            "    /** 标签存在且值与 tagValue 相等则命中 */\n    @Override\n    public boolean match(Map<String, String> labels) {",
        ),
        (
            "    @Override\n    public String getType() {",
            "    /** 返回 {@link #TYPE_TAG} */\n    @Override\n    public String getType() {",
        ),
        (
            "    @Override\n    public String getVersion() {",
            "    /** 返回 {@link #VERSION} */\n    @Override\n    public String getVersion() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/multitag/MultiTagMatchGrayRule.java": [
        (
            "/**\n * tag v2 gray rule.\n *\n * @author rong\n */",
            "/**\n * TagV2 多标签组合灰度规则：支持 key=value 与 ||、&& 逻辑组合，版本 1.1.0。\n"
            " * 表达式形如 a=v1,v2&&b=v3||c=v4；匹配时对标签 Map 做 OR 子句内 AND 合取判定。\n"
            " * tag v2 gray rule.\n *\n * @author rong\n */",
        ),
        (
            "    private List<TagV2GrayRuleItem> ruleItems;",
            "    /** 解析后的规则项列表（含连接符 OR/AND 语义） */\n    private List<TagV2GrayRuleItem> ruleItems;",
        ),
        (
            "    public static final String TYPE_TAGV2 = TAG_V2;",
            "    /** TagV2 规则类型常量 */\n    public static final String TYPE_TAGV2 = TAG_V2;",
        ),
        (
            "    public static final String VERSION_1_1_0 = \"1.1.0\";",
            "    /** 多标签规则版本 1.1.0 */\n    public static final String VERSION_1_1_0 = \"1.1.0\";",
        ),
        (
            "    public MultiTagMatchGrayRule() {",
            "    /** 无参构造 */\n    public MultiTagMatchGrayRule() {",
        ),
        (
            "    public MultiTagMatchGrayRule(String rawGrayRuleExp, int priority) {",
            "    /**\n     * 构造多标签匹配规则。\n     *\n     * @param rawGrayRuleExp 含 ||、&& 的复合表达式\n     * @param priority       优先级\n     */\n"
            "    public MultiTagMatchGrayRule(String rawGrayRuleExp, int priority) {",
        ),
        (
            "            //each subExpression is jointed by one or multi \"&&\"",
            "            // 每个 OR 子句内由一条或多条 && 连接的键值项组成",
        ),
        (
            "    /**\n     * this rule will match labelsMap.\n     *\n     * @param labelsMap labels map.\n     * @return true if match. false if not match.\n     * @date 2024/2/6\n     */",
            "    /**\n     * 按 OR 子句划分规则项，子句内 AND 合取匹配 labelsMap。\n     *\n     * @param labelsMap 客户端标签 Map\n     * @return 任一 OR 子句全部 AND 项命中则 true\n     * @date 2024/2/6\n     */",
        ),
        (
            "                //if AND, will consider the current ruleItem belong to this subRule.",
            "                // AND：当前项属于同一 OR 子句，需与前项同时满足",
        ),
        (
            "                //if one of the items in the subRule is not match, will continue to next subRule.",
            "                // 子句内已有项未命中则跳过该子句后续项",
        ),
        (
            "                //if the key has already existed in this subRule,",
            "                // 同一 OR 子句内重复 key 视为语法错误，子句不匹配",
        ),
        (
            "                //check current item",
            "                // 校验当前键值项是否命中 labelsMap",
        ),
        (
            "                //if OR, will consider the current ruleItem belong to the next subRule,",
            "                // OR：结束当前子句，开启下一子句（首项 joint 改为 AND）",
        ),
        (
            "                //only when subRuleMatchFlag is true, update result.",
            "                // 仅当上一子句匹配成功时更新已匹配项计数",
        ),
        (
            "    /**\n     * check this TagV2GrayRule is valid or not.\n     *\n     * @return true if valid. false if not valid.\n     * @date 2024/2/7\n     */",
            "    /**\n     * 校验规则项非空、各项 key 合法且同一 OR 子句内无重复 key。\n     *\n     * @return 语义合法返回 true\n     * @date 2024/2/7\n     */",
        ),
        (
            "    @Override\n    public String getType() {",
            "    /** 返回 {@link #TYPE_TAGV2} */\n    @Override\n    public String getType() {",
        ),
        (
            "    @Override\n    public String getVersion() {",
            "    /** 返回 {@link #VERSION_1_1_0} */\n    @Override\n    public String getVersion() {",
        ),
        (
            "    /**\n     * tag v2 gray rule item.\n     *\n     * @author rong\n     */",
            "    /**\n     * TagV2 单条规则项：键、运算符、允许值集合及与子句内前项的连接关系。\n     *\n     * @author rong\n     */",
        ),
        (
            "        public String key;",
            "        /** 标签键名 */\n        public String key;",
        ),
        (
            "        public TagV2GrayRuleOperator operator = TagV2GrayRuleOperator.IN;",
            "        /** 匹配运算符，默认 IN（值在集合内） */\n        public TagV2GrayRuleOperator operator = TagV2GrayRuleOperator.IN;",
        ),
        (
            "        public final Set<String> values = new HashSet<>();",
            "        /** 参与 IN/NOT_IN 判定的值集合 */\n        public final Set<String> values = new HashSet<>();",
        ),
        (
            "        public TagV2GrayRuleJoint joint = TagV2GrayRuleJoint.AND;",
            "        /** 与前一项的逻辑连接（OR 子句边界或 AND 合取） */\n        public TagV2GrayRuleJoint joint = TagV2GrayRuleJoint.AND;",
        ),
        (
            "        public TagV2GrayRuleItem(String key) {",
            "        /** 仅指定键的构造 */\n        public TagV2GrayRuleItem(String key) {",
        ),
        (
            "        public TagV2GrayRuleItem(String key, Set<String> values) {",
            "        /**\n         * 指定键与允许值集合。\n         *\n         * @param key    标签键\n         * @param values 允许值集合\n         */\n"
            "        public TagV2GrayRuleItem(String key, Set<String> values) {",
        ),
        (
            "        /**\n         * judge if value is match the rule.\n         *\n         * @param value value\n         * @return boolean true if match, false otherwise.\n         * @date 2024/2/8\n         */",
            "        /**\n         * 按 operator 判断单个标签值是否满足本项。\n         *\n         * @param value 标签实际值\n         * @return 匹配返回 true\n         * @date 2024/2/8\n         */",
        ),
        (
            "        /**\n         * judge if rule is valid.\n         *\n         * @return boolean true if valid, false otherwise.\n         * @throws NacosException if invalid.\n         * @date 2024/2/8\n         */",
            "        /**\n         * 规则项是否有效（当前实现要求 key 非空）。\n         *\n         * @return 有效返回 true\n         * @date 2024/2/8\n         */",
        ),
        (
            "        public static TagV2GrayRuleItemBuilder builder() {",
            "        /** 获取规则项建造者 */\n        public static TagV2GrayRuleItemBuilder builder() {",
        ),
        (
            "        @Override\n        public TagV2GrayRuleItem clone() {",
            "        /** 深拷贝规则项（含 values 集合） */\n        @Override\n        public TagV2GrayRuleItem clone() {",
        ),
        (
            "        public static final class TagV2GrayRuleItemBuilder {",
            "        /** TagV2 规则项流式构造器 */\n        public static final class TagV2GrayRuleItemBuilder {",
        ),
        (
            "            public TagV2GrayRuleItemBuilder key(String key) {",
            "            /** 设置标签键并返回 builder */\n            public TagV2GrayRuleItemBuilder key(String key) {",
        ),
        (
            "            public TagV2GrayRuleItem build() {",
            "            /** 构建规则项实例 */\n            public TagV2GrayRuleItem build() {",
        ),
        (
            "    /**\n     * tag v2 gray rule joint.\n     *\n     * @author rong\n     */",
            "    /**\n     * TagV2 逻辑连接符：表达式中的 ||、&& 及对应 split 正则。\n     *\n     * @author rong\n     */",
        ),
        (
            "        /**\n         * and.\n         */\n        AND(\"&&\", \"AND\"),",
            "        /** 逻辑与（&&） */\n        AND(\"&&\", \"AND\"),",
        ),
        (
            "        /**\n         * or.\n         */\n        OR(\"||\", \"OR\"),",
            "        /** 逻辑或（||），亦标记 OR 子句起始 */\n        OR(\"||\", \"OR\"),",
        ),
        (
            "        /**\n         * and regexp.\n         */\n        AND_REGEXP(\"&&\", \"AND_REGEXP\"),",
            "        /** 用于 split 的 AND 正则 */\n        AND_REGEXP(\"&&\", \"AND_REGEXP\"),",
        ),
        (
            "        /**\n         * or regexp.\n         */\n        OR_REGEXP(\"\\\\|\\\\|\", \"OR_REGEXP\");",
            "        /** 用于 split 的 OR 正则（转义 ||） */\n        OR_REGEXP(\"\\\\|\\\\|\", \"OR_REGEXP\");",
        ),
        (
            "        public final String expression;",
            "        /** 连接符或正则字面量 */\n        public final String expression;",
        ),
        (
            "        public final String name;",
            "        /** 枚举名称标识 */\n        public final String name;",
        ),
        (
            "    /**\n     * tag v2 gray rule operator.\n     *\n     * @author rong\n     */",
            "    /**\n     * TagV2 值匹配运算符：IN、NOT_IN、EXIST、NOT_EXIST。\n     *\n     * @author rong\n     */",
        ),
        (
            "        /**\n         * in.\n         */\n        IN(\"in\", \"IN\"),",
            "        /** 值在允许集合内 */\n        IN(\"in\", \"IN\"),",
        ),
        (
            "        /**\n         * not in.\n         */\n        NOT_IN(\"not in\", \"NOT_IN\"),",
            "        /** 值不在允许集合内 */\n        NOT_IN(\"not in\", \"NOT_IN\"),",
        ),
        (
            "        /**\n         * exist.\n         */\n        EXIST(\"exist\", \"EXIST\"),",
            "        /** 标签值存在（非 null） */\n        EXIST(\"exist\", \"EXIST\"),",
        ),
        (
            "        /**\n         * not exist.\n         */\n        NOT_EXIST(\"not exist\", \"NOT_EXIST\");",
            "        /** 标签值不存在（为 null） */\n        NOT_EXIST(\"not exist\", \"NOT_EXIST\");",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/gray/singletag/SingleTagMatchGrayRule.java": [
        (
            "/**\n * Single tag match gray rule.单标签匹配规则.\n *\n * @author shiyiyue\n */",
            "/**\n * TagV2 单标签匹配灰度规则：表达式格式为 key=value1,value2，命中任一值即匹配。\n"
            " * type 为 tagv2，version 1.0.0；继承 {@link AbstractTagMatchGrayRule} 的正则校验。\n"
            " * Single tag match gray rule.单标签匹配规则.\n *\n * @author shiyiyue\n */",
        ),
        (
            "    private String tagKey;",
            "    /** 待匹配的标签键名 */\n    private String tagKey;",
        ),
        (
            "    private Set<String> tagValueSet;",
            "    /** 允许命中的标签值集合 */\n    private Set<String> tagValueSet;",
        ),
        (
            "    private static final int KEY_VALUE_ARRAY_LENGTH = 2;",
            "    /** 键值对 split 后期望长度为 2 */\n    private static final int KEY_VALUE_ARRAY_LENGTH = 2;",
        ),
        (
            "    public SingleTagMatchGrayRule() {",
            "    /** 无参构造 */\n    public SingleTagMatchGrayRule() {",
        ),
        (
            "    public SingleTagMatchGrayRule(String rawGrayRuleExp, int priority) {",
            "    /**\n     * 构造单标签匹配规则。\n     *\n     * @param rawGrayRuleExp key=value1,value2 表达式\n     * @param priority       优先级\n     */\n"
            "    public SingleTagMatchGrayRule(String rawGrayRuleExp, int priority) {",
        ),
        (
            "    /**\n     * parse rule, accept key=value1,value2,value3.\n     *\n     * @param rawRule raw rule\n     */",
            "    /**\n     * 解析 key=value1,value2 格式表达式，校验键值正则并填充 tagKey/tagValueSet。\n     *\n     * @param rawRule 原始规则字符串\n     */",
        ),
        (
            "    @Override\n    public boolean match(Map<String, String> labels) {",
            "    /** 标签键存在且值落在 tagValueSet 内则命中 */\n    @Override\n    public boolean match(Map<String, String> labels) {",
        ),
        (
            "    public static final String TYPE_TAGV2 = TAG_V2;",
            "    /** TagV2 规则类型常量 */\n    public static final String TYPE_TAGV2 = TAG_V2;",
        ),
        (
            "    public static final String VERSION_1_0_0 = \"1.0.0\";",
            "    /** 单标签规则版本 1.0.0 */\n    public static final String VERSION_1_0_0 = \"1.0.0\";",
        ),
        (
            "    @Override\n    public String getType() {",
            "    /** 返回 {@link #TYPE_TAGV2} */\n    @Override\n    public String getType() {",
        ),
        (
            "    @Override\n    public String getVersion() {",
            "    /** 返回 {@link #VERSION_1_0_0} */\n    @Override\n    public String getVersion() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/monitor/MetricsMonitor.java": [
        (
            "/**\n * Metrics Monitor.\n *\n * @author Nacos\n */",
            "/**\n * 配置模块核心指标注册中心：维护拉配置、发布、长轮询、通知、dump 等 gauge 与 timer/counter。\n"
            " * 静态块向 {@link NacosMeterRegistryCenter#CONFIG_STABLE_REGISTRY} 注册 Micrometer 指标供运维采集。\n"
            " * Metrics Monitor.\n *\n * @author Nacos\n */",
        ),
        (
            "    private static final String METER_REGISTRY = NacosMeterRegistryCenter.CONFIG_STABLE_REGISTRY;",
            "    /** 配置稳定指标注册表名称 */\n    private static final String METER_REGISTRY = NacosMeterRegistryCenter.CONFIG_STABLE_REGISTRY;",
        ),
        (
            "    private static AtomicInteger getConfig = new AtomicInteger();",
            "    /** 拉配置并发/进行中的任务计数 */\n    private static AtomicInteger getConfig = new AtomicInteger();",
        ),
        (
            "    private static AtomicInteger publish = new AtomicInteger();",
            "    /** 发布配置任务计数 */\n    private static AtomicInteger publish = new AtomicInteger();",
        ),
        (
            "    /**\n     * task for notify config change to sub client of http long polling.\n     */\n    private static AtomicInteger longPolling = new AtomicInteger();",
            "    /** HTTP 长轮询订阅客户端的配置变更通知任务计数 */\n    private static AtomicInteger longPolling = new AtomicInteger();",
        ),
        (
            "    private static AtomicInteger configCount = new AtomicInteger();",
            "    /** 当前配置 group 总数（由 PrintMemoryTask 刷新） */\n    private static AtomicInteger configCount = new AtomicInteger();",
        ),
        (
            "    /**\n     * task for notify config change to cluster server.\n     */\n    private static AtomicInteger notifyTask = new AtomicInteger();",
            "    /** 向集群其他节点同步配置变更的通知任务计数 */\n    private static AtomicInteger notifyTask = new AtomicInteger();",
        ),
        (
            "    /**\n     * task for notify config change to sub client of long connection.\n     */\n    private static AtomicInteger notifyClientTask = new AtomicInteger();",
            "    /** 长连接订阅客户端的配置变更通知任务计数 */\n    private static AtomicInteger notifyClientTask = new AtomicInteger();",
        ),
        (
            "    private static AtomicInteger dumpTask = new AtomicInteger();",
            "    /** 配置 dump 到磁盘任务计数 */\n    private static AtomicInteger dumpTask = new AtomicInteger();",
        ),
        (
            "    /**\n     * config fuzzy search count.\n     */\n    private static AtomicInteger fuzzySearch = new AtomicInteger();",
            "    /** 配置模糊搜索请求计数 */\n    private static AtomicInteger fuzzySearch = new AtomicInteger();",
        ),
        (
            "    /**\n     * version -> client config subscriber count.\n     */\n    private static ConcurrentHashMap<String, AtomicInteger> configSubscriber =",
            "    /** 按协议版本（v1/v2）统计的配置订阅客户端数 */\n    private static ConcurrentHashMap<String, AtomicInteger> configSubscriber =",
        ),
        (
            "    /**\n     * config change count.\n     */\n    private static StringTopNCounter configChangeCount = new StringTopNCounter();",
            "    /** 按 dataId@group@tenant 维度的配置变更 TopN 计数器 */\n    private static StringTopNCounter configChangeCount = new StringTopNCounter();",
        ),
        (
            "    public static AtomicInteger getConfigMonitor() {",
            "    /** 获取拉配置任务 gauge 引用 */\n    public static AtomicInteger getConfigMonitor() {",
        ),
        (
            "    public static AtomicInteger getPublishMonitor() {",
            "    /** 获取发布任务 gauge 引用 */\n    public static AtomicInteger getPublishMonitor() {",
        ),
        (
            "    public static AtomicInteger getLongPollingMonitor() {",
            "    /** 获取 HTTP 长轮询通知任务 gauge 引用 */\n    public static AtomicInteger getLongPollingMonitor() {",
        ),
        (
            "    public static AtomicInteger getConfigCountMonitor() {",
            "    /** 获取配置 group 总数 gauge 引用 */\n    public static AtomicInteger getConfigCountMonitor() {",
        ),
        (
            "    public static AtomicInteger getNotifyTaskMonitor() {",
            "    /** 获取集群通知任务 gauge 引用 */\n    public static AtomicInteger getNotifyTaskMonitor() {",
        ),
        (
            "    public static AtomicInteger getNotifyClientTaskMonitor() {",
            "    /** 获取长连接客户端通知任务 gauge 引用 */\n    public static AtomicInteger getNotifyClientTaskMonitor() {",
        ),
        (
            "    public static AtomicInteger getDumpTaskMonitor() {",
            "    /** 获取 dump 任务 gauge 引用 */\n    public static AtomicInteger getDumpTaskMonitor() {",
        ),
        (
            "    public static AtomicInteger getFuzzySearchMonitor() {",
            "    /** 获取模糊搜索计数 gauge 引用 */\n    public static AtomicInteger getFuzzySearchMonitor() {",
        ),
        (
            "    public static AtomicInteger getConfigSubscriberMonitor(String version) {",
            "    /** 按版本获取订阅客户端数 gauge 引用 */\n    public static AtomicInteger getConfigSubscriberMonitor(String version) {",
        ),
        (
            "    public static StringTopNCounter getConfigChangeCount() {",
            "    /** 获取配置变更 TopN 计数器 */\n    public static StringTopNCounter getConfigChangeCount() {",
        ),
        (
            "    public static Timer getReadConfigRtTimer() {",
            "    /** 读配置耗时 Timer */\n    public static Timer getReadConfigRtTimer() {",
        ),
        (
            "    public static Timer getWriteConfigRtTimer() {",
            "    /** 写配置耗时 Timer */\n    public static Timer getWriteConfigRtTimer() {",
        ),
        (
            "    public static Timer getNotifyRtTimer() {",
            "    /** 配置变更通知耗时 Timer */\n    public static Timer getNotifyRtTimer() {",
        ),
        (
            "    public static Timer getDumpRtTimer() {",
            "    /** 配置 dump 耗时 Timer */\n    public static Timer getDumpRtTimer() {",
        ),
        (
            "    public static Counter getIllegalArgumentException() {",
            "    /** 非法参数异常 Counter */\n    public static Counter getIllegalArgumentException() {",
        ),
        (
            "    public static Counter getNacosException() {",
            "    /** Nacos 业务异常 Counter */\n    public static Counter getNacosException() {",
        ),
        (
            "    public static Counter getConfigNotifyException() {",
            "    /** 配置通知失败异常 Counter */\n    public static Counter getConfigNotifyException() {",
        ),
        (
            "    public static Counter getUnhealthException() {",
            "    /** 不健康状态异常 Counter */\n    public static Counter getUnhealthException() {",
        ),
        (
            "    public static void incrementConfigChangeCount(String tenant, String group, String dataId) {",
            "    /**\n     * 递增指定 tenant@group@dataId 的配置变更计数。\n     *\n     * @param tenant 命名空间\n     * @param group  配置 group\n     * @param dataId 配置 dataId\n     */\n"
            "    public static void incrementConfigChangeCount(String tenant, String group, String dataId) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/monitor/ConfigDynamicMeterRefreshService.java": [
        (
            "/**\n * dynamic meter refresh service.\n *\n * @author <a href=\"mailto:liuyixiao0821@gmail.com\">liuyixiao</a>\n */",
            "/**\n * 配置模块动态 Micrometer 指标刷新服务：定时刷新配置变更 TopN 并周期性清零计数。\n"
            " * 与 {@link MetricsMonitor} 及 {@link NacosMeterRegistryCenter} 配合暴露运维指标。\n"
            " * dynamic meter refresh service.\n *\n * @author <a href=\"mailto:liuyixiao0821@gmail.com\">liuyixiao</a>\n */",
        ),
        (
            "    private static final String TOPN_CONFIG_CHANGE_REGISTRY =",
            "    /** TopN 配置变更计数注册表名称 */\n    private static final String TOPN_CONFIG_CHANGE_REGISTRY =",
        ),
        (
            "    private static final int CONFIG_CHANGE_N = 10;",
            "    /** 保留的配置变更 TopN 条数 */\n    private static final int CONFIG_CHANGE_N = 10;",
        ),
        (
            "    /**\n     * refresh config change count top n per 30s.\n     */",
            "    /**\n     * 每 30 秒刷新配置变更次数 TopN 到 Micrometer gauge。\n     */",
        ),
        (
            "    /**\n     * reset config change count to 0 every week.\n     */",
            "    /**\n     * 每周一零点重置配置变更累计计数，避免长期膨胀。\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/monitor/MemoryMonitor.java": [
        (
            "/**\n * Memory monitor.\n *\n * @author Nacos\n */",
            "/**\n * 配置服务内存与响应监控入口：启动周期性打印内存、拉配置响应分布及异步通知队列任务。\n"
            " * 并每日清零部分 {@link MetricsMonitor} 计数器。\n"
            " * Memory monitor.\n *\n * @author Nacos\n */",
        ),
        (
            "    @Autowired\n    public MemoryMonitor(AsyncNotifyService notifySingleService) {",
            "    /**\n     * 注入后注册三类定时监控任务（间隔 {@link #DELAY_SECONDS} 秒）。\n     *\n     * @param notifySingleService 异步通知服务，供队列监控使用\n     */\n"
            "    @Autowired\n    public MemoryMonitor(AsyncNotifyService notifySingleService) {",
        ),
        (
            "    private static final long DELAY_SECONDS = 10;",
            "    /** 监控任务初始延迟与执行间隔（秒） */\n    private static final long DELAY_SECONDS = 10;",
        ),
        (
            "    /**\n     * reset some metrics to 0 every day.\n     */",
            "    /**\n     * 每日零点重置拉配置、发布与模糊搜索等日级计数器。\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/monitor/PrintGetConfigResponeTask.java": [
        (
            "/**\n * PrintGetConfigResponeTask.\n *\n * @author zongtanghu\n */",
            "/**\n * 定时打印拉配置响应耗时分布的任务：调用 {@link ResponseMonitor#getStringForPrint()} 写入 MEMORY_LOG。\n"
            " * 类名保留历史拼写 Respone。\n"
            " * PrintGetConfigResponeTask.\n *\n * @author zongtanghu\n */",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 将响应耗时百分比分段统计写入内存监控日志 */\n    @Override\n    public void run() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/monitor/PrintMemoryTask.java": [
        (
            "/**\n * Print memory task.\n *\n * @author zongtanghu\n */",
            "/**\n * 定时打印配置缓存与订阅统计：group 数、订阅客户端数、订阅条目数，并更新 configCount 指标。\n"
            " * Print memory task.\n *\n * @author zongtanghu\n */",
        ),
        (
            "    @Override\n    public void run() {",
            "    /** 采集缓存 group 数与订阅规模，写入 MEMORY_LOG 并刷新 MetricsMonitor */\n    @Override\n    public void run() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/monitor/ResponseMonitor.java": [
        (
            "/**\n * Response Monitory.\n *\n * @author Nacos\n */",
            "/**\n * 拉配置响应耗时分布监控：按毫秒区间累计请求占比，供 {@link PrintGetConfigResponeTask} 周期性输出。\n"
            " * 类名保留历史拼写 Monitory。\n"
            " * Response Monitory.\n *\n * @author Nacos\n */",
        ),
        (
            "    private static AtomicLong[] getConfigCountDetail = new AtomicLong[8];",
            "    /** 各耗时区间（0-50ms … 3000ms+）的请求计数 */\n    private static AtomicLong[] getConfigCountDetail = new AtomicLong[8];",
        ),
        (
            "    private static AtomicLong getConfigCount = new AtomicLong();",
            "    /** 拉配置请求总计数（用于计算百分比） */\n    private static AtomicLong getConfigCount = new AtomicLong();",
        ),
        (
            "    private static final int MS_50 = 50;",
            "    /** 耗时区间上界：50ms */\n    private static final int MS_50 = 50;",
        ),
        (
            "    private static final int MS_100 = 100;",
            "    /** 耗时区间上界：100ms */\n    private static final int MS_100 = 100;",
        ),
        (
            "    private static final int MS_200 = 200;",
            "    /** 耗时区间上界：200ms */\n    private static final int MS_200 = 200;",
        ),
        (
            "    private static final int MS_500 = 500;",
            "    /** 耗时区间上界：500ms */\n    private static final int MS_500 = 500;",
        ),
        (
            "    private static final int MS_1000 = 1000;",
            "    /** 耗时区间上界：1000ms */\n    private static final int MS_1000 = 1000;",
        ),
        (
            "    private static final int MS_2000 = 2000;",
            "    /** 耗时区间上界：2000ms */\n    private static final int MS_2000 = 2000;",
        ),
        (
            "    private static final int MS_3000 = 3000;",
            "    /** 耗时区间上界：3000ms */\n    private static final int MS_3000 = 3000;",
        ),
        (
            "    /**\n     * Refresh for getting configCountDetail.\n     */",
            "    /**\n     * 初始化或重置各耗时区间计数器数组。\n     */",
        ),
        (
            "    /**\n     * AddConfigTime.\n     *\n     * @param time config time which is added.\n     */",
            "    /**\n     * 记录一次拉配置耗时并累加到对应毫秒区间。\n     *\n     * @param time 本次请求耗时（毫秒）\n     */",
        ),
        (
            "    public static String getStringForPrint() {",
            "    /** 生成各耗时区间占比字符串并清零区间计数（总计一并清零） */\n    public static String getStringForPrint() {",
        ),
    ],
}
