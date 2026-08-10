"""Chinese annotation replacements for Nacos 3.2.3 wave37a [0:15] config model cache/info."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigAdvanceInfo.java": [
        (
            "/**\n * Config advance info.\n *\n * @author Nacos\n */",
            "/**\n * 配置扩展元信息：记录创建/修改时间、操作者、用途、生效范围、类型、Schema 及标签等，\n"
            " * 供控制台展示与审计，与 {@link ConfigInfo} 主表字段互补。\n"
            " * Config advance info.\n *\n * @author Nacos\n */",
        ),
        (
            "    private long createTime;",
            "    /** 配置首次创建时间戳（毫秒） */\n"
            "    private long createTime;",
        ),
        (
            "    private long modifyTime;",
            "    /** 配置最近一次修改时间戳（毫秒） */\n"
            "    private long modifyTime;",
        ),
        (
            "    private String createUser;",
            "    /** 创建或最后修改操作的用户名 */\n"
            "    private String createUser;",
        ),
        (
            "    private String createIp;",
            "    /** 创建或最后修改操作的来源 IP */\n"
            "    private String createIp;",
        ),
        (
            "    private String desc;",
            "    /** 配置描述说明 */\n"
            "    private String desc;",
        ),
        (
            "    private String use;",
            "    /** 配置用途说明（业务场景） */\n"
            "    private String use;",
        ),
        (
            "    private String effect;",
            "    /** 配置生效范围或影响说明 */\n"
            "    private String effect;",
        ),
        (
            "    private String type;",
            "    /** 配置内容类型（如 text、json、yaml） */\n"
            "    private String type;",
        ),
        (
            "    private String schema;",
            "    /** 配置内容对应的 Schema 定义 */\n"
            "    private String schema;",
        ),
        (
            "    private String configTags;",
            "    /** 配置标签，逗号分隔，用于检索与分类 */\n"
            "    private String configTags;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigAllInfo.java": [
        (
            "/**\n * ConfigAllInfo.\n *\n * @author Nacos\n */",
            "/**\n * 配置完整信息视图：继承 {@link ConfigInfo} 并附加创建/修改时间、操作者、\n"
            " * 用途、生效说明与 Schema 等扩展字段，供控制台详情页一次性返回。\n"
            " * ConfigAllInfo.\n *\n * @author Nacos\n */",
        ),
        (
            "    private long createTime;",
            "    /** 配置创建时间戳（毫秒） */\n"
            "    private long createTime;",
        ),
        (
            "    private long modifyTime;",
            "    /** 配置最近修改时间戳（毫秒） */\n"
            "    private long modifyTime;",
        ),
        (
            "    private String createUser;",
            "    /** 创建或修改操作的用户名 */\n"
            "    private String createUser;",
        ),
        (
            "    private String createIp;",
            "    /** 创建或修改操作的来源 IP */\n"
            "    private String createIp;",
        ),
        (
            "    private String use;",
            "    /** 配置用途说明 */\n"
            "    private String use;",
        ),
        (
            "    private String effect;",
            "    /** 配置生效范围说明 */\n"
            "    private String effect;",
        ),
        (
            "    private String schema;",
            "    /** 配置内容 Schema 定义 */\n"
            "    private String schema;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigCache.java": [
        (
            "/**\n * config cache .\n *\n * @author shiyiyue1102\n */",
            "/**\n * 内存配置缓存条目：保存 MD5 摘要、加密数据密钥与最后修改时间戳，\n"
            " * 供 {@link com.alibaba.nacos.config.server.service.ConfigCacheService} 快速比对客户端版本。\n"
            " * config cache .\n *\n * @author shiyiyue1102\n */",
        ),
        (
            "    volatile String md5 = Constants.NULL;",
            "    /** 配置内容 MD5 摘要，{@link Constants#NULL} 表示尚未加载 */\n"
            "    volatile String md5 = Constants.NULL;",
        ),
        (
            "    volatile String encryptedDataKey;",
            "    /** 加密配置的数据密钥标识 */\n"
            "    volatile String encryptedDataKey;",
        ),
        (
            "    volatile long lastModifiedTs;",
            "    /** 配置最后修改时间戳（毫秒），-1 表示未设置 */\n"
            "    volatile long lastModifiedTs;",
        ),
        (
            "    /**\n     * clear cache.\n     */",
            "    /**\n     * 清空缓存条目，重置 MD5、加密密钥与修改时间。\n"
            "     * clear cache.\n     */",
        ),
        (
            "    public ConfigCache(String md5, long lastModifiedTs) {",
            "    /** 以 MD5 与修改时间构造缓存条目，MD5 经 {@link com.alibaba.nacos.core.utils.StringPool} 驻留 */\n"
            "    public ConfigCache(String md5, long lastModifiedTs) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigCacheFactory.java": [
        (
            "/**\n * The interface Config cache factory.\n *\n * @author Sunrisea\n */",
            "/**\n * 配置缓存工厂 SPI 接口：按实现名称创建 {@link ConfigCache} 与 {@link ConfigCacheGray} 实例，\n"
            " * 由 {@link ConfigCacheFactoryDelegate} 根据 {@code nacos.config.cache.type} 选择具体实现。\n"
            " * The interface Config cache factory.\n *\n * @author Sunrisea\n */",
        ),
        (
            "    /**\n     * Create config cache config cache.\n     *\n     * @return the config cache\n     */",
            "    /**\n     * 创建标准配置缓存实例。\n"
            "     * Create config cache config cache.\n     *\n     * @return the config cache\n     */",
        ),
        (
            "    /**\n     * Create config cache gray config cache gray.\n     *\n     * @return the config cache gray\n     */",
            "    /**\n     * 创建灰度配置缓存实例。\n"
            "     * Create config cache gray config cache gray.\n     *\n     * @return the config cache gray\n     */",
        ),
        (
            "    /**\n     * Gets config cache factroy name.\n     *\n     * @return the config cache factory name\n     */",
            "    /**\n     * 返回工厂实现名称，用于与配置项 {@code nacos.config.cache.type} 匹配。\n"
            "     * Gets config cache factroy name.\n     *\n     * @return the config cache factory name\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigCacheFactoryDelegate.java": [
        (
            "/**\n * The type Config cache factory delegate.\n *\n * @author Sunrisea\n */",
            "/**\n * 配置缓存工厂委托单例：通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 加载\n"
            " * {@link ConfigCacheFactory} 实现，按 {@code nacos.config.cache.type} 选择或回退默认工厂。\n"
            " * The type Config cache factory delegate.\n *\n * @author Sunrisea\n */",
        ),
        (
            "    private String configCacheFactoryType = EnvUtil.getProperty(\"nacos.config.cache.type\", \"nacos\");",
            "    /** 当前选中的缓存工厂类型，默认 {@code nacos} */\n"
            "    private String configCacheFactoryType = EnvUtil.getProperty(\"nacos.config.cache.type\", \"nacos\");",
        ),
        (
            "    private ConfigCacheFactory configCacheFactory = null;",
            "    /** 已匹配或默认的缓存工厂实例 */\n"
            "    private ConfigCacheFactory configCacheFactory = null;",
        ),
        (
            "    /**\n     * Gets instance.\n     *\n     * @return the instance\n     */",
            "    /**\n     * 获取工厂委托单例。\n"
            "     * Gets instance.\n     *\n     * @return the instance\n     */",
        ),
        (
            "    /**\n     * Create config cache config cache.\n     *\n     * @return the config cache\n     */",
            "    /**\n     * 委托当前工厂创建标准 {@link ConfigCache}。\n"
            "     * Create config cache config cache.\n     *\n     * @return the config cache\n     */",
        ),
        (
            "    /**\n     * Create config cache config cache.\n     *\n     * @param md5            the md 5\n     * @param lastModifiedTs the last modified ts\n     * @return the config cache\n     */",
            "    /**\n     * 创建并初始化带 MD5 与修改时间的 {@link ConfigCache}。\n"
            "     * Create config cache config cache.\n     *\n     * @param md5            the md 5\n     * @param lastModifiedTs the last modified ts\n     * @return the config cache\n     */",
        ),
        (
            "    /**\n     * Create config cache gray config cache gray.\n     *\n     * @return the config cache gray\n     */",
            "    /**\n     * 委托当前工厂创建 {@link ConfigCacheGray}。\n"
            "     * Create config cache gray config cache gray.\n     *\n     * @return the config cache gray\n     */",
        ),
        (
            "    /**\n     * Create config cache gray config cache gray.\n     *\n     * @param grayName the gray name\n     * @return the config cache gray\n     */",
            "    /**\n     * 创建并设置灰度名称的 {@link ConfigCacheGray}。\n"
            "     * Create config cache gray config cache gray.\n     *\n     * @param grayName the gray name\n     * @return the config cache gray\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigCacheGray.java": [
        (
            "/**\n * extensible config cache.\n *\n * @author rong\n */",
            "/**\n * 灰度配置缓存：在 {@link ConfigCache} 基础上附加灰度名称与 {@link com.alibaba.nacos.config.server.model.gray.GrayRule}，\n"
            " * 支持按客户端标签匹配灰度规则并参与推送路由。\n"
            " * extensible config cache.\n *\n * @author rong\n */",
        ),
        (
            "    private String grayName;",
            "    /** 灰度配置名称，唯一标识一条灰度发布 */\n"
            "    private String grayName;",
        ),
        (
            "    private GrayRule grayRule;",
            "    /** 解析后的灰度匹配规则对象 */\n"
            "    private GrayRule grayRule;",
        ),
        (
            "    /**\n     * clear cache.\n     */",
            "    /**\n     * 清空灰度缓存，委托父类重置 MD5 等基础字段。\n"
            "     * clear cache.\n     */",
        ),
        (
            "    /**\n     * get raw gray rule from db.\n     *\n     * @return raw gray rule from db.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 获取数据库中存储的原始灰度规则表达式。\n"
            "     * get raw gray rule from db.\n     *\n     * @return raw gray rule from db.\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * reset gray rule.\n     *\n     * @param grayRule raw gray rule from db.\n     * @throws RuntimeException if gray rule is invalid.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 从数据库原始规则字符串重新解析并设置灰度规则，无效时抛出异常。\n"
            "     * reset gray rule.\n     *\n     * @param grayRule raw gray rule from db.\n     * @throws RuntimeException if gray rule is invalid.\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * judge whether match gray rule.\n     *\n     * @param tags conn tags.\n     * @return true if match, false otherwise.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 判断客户端连接标签是否匹配当前灰度规则。\n"
            "     * judge whether match gray rule.\n     *\n     * @param tags conn tags.\n     * @return true if match, false otherwise.\n     * @date 2024/3/14\n     */",
        ),
        (
            "    /**\n     * if gray rule is valid.\n     *\n     * @return true if valid, false otherwise.\n     * @date 2024/3/14\n     */",
            "    /**\n     * 判断灰度规则是否已加载且合法。\n"
            "     * if gray rule is valid.\n     *\n     * @return true if valid, false otherwise.\n     * @date 2024/3/14\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigCachePostProcessor.java": [
        (
            "/**\n * The interface Config cache md5 post processor.\n *\n * @author Sunrisea\n */",
            "/**\n * 配置缓存 MD5 后处理器 SPI：在写入 {@link ConfigCache} 前对内容做额外处理\n"
            " * （如自定义摘要算法），由 {@link ConfigCachePostProcessorDelegate} 按类型加载。\n"
            " * The interface Config cache md5 post processor.\n *\n * @author Sunrisea\n */",
        ),
        (
            "    /**\n     * Gets post processor name.\n     *\n     * @return the post processor name\n     */",
            "    /**\n     * 返回后处理器名称，与 {@code nacos.config.cache.type} 匹配。\n"
            "     * Gets post processor name.\n     *\n     * @return the post processor name\n     */",
        ),
        (
            "    /**\n     * Post process.\n     *\n     * @param configCache the config cache\n     * @param content     the content\n     */",
            "    /**\n     * 对配置内容执行后处理并更新 {@link ConfigCache}（如重算 MD5）。\n"
            "     * Post process.\n     *\n     * @param configCache the config cache\n     * @param content     the content\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigCachePostProcessorDelegate.java": [
        (
            "/**\n * The type Config cache md5 post processor delegate.\n *\n * @author Sunrisea\n */",
            "/**\n * 配置缓存 MD5 后处理器委托单例：SPI 加载 {@link ConfigCachePostProcessor} 实现，\n"
            " * 按 {@code nacos.config.cache.type} 选择匹配项或回退 {@link NacosConfigCachePostProcessor}。\n"
            " * The type Config cache md5 post processor delegate.\n *\n * @author Sunrisea\n */",
        ),
        (
            "    private String configCacheMd5PostProcessorType =\n        EnvUtil.getProperty(\"nacos.config.cache.type\", \"nacos\");",
            "    /** 当前选中的后处理器类型，默认 {@code nacos} */\n"
            "    private String configCacheMd5PostProcessorType =\n        EnvUtil.getProperty(\"nacos.config.cache.type\", \"nacos\");",
        ),
        (
            "    private ConfigCachePostProcessor configCachePostProcessor;",
            "    /** 已匹配或默认的后处理器实例 */\n"
            "    private ConfigCachePostProcessor configCachePostProcessor;",
        ),
        (
            "    public static ConfigCachePostProcessorDelegate getInstance() {",
            "    /** 获取后处理器委托单例 */\n"
            "    public static ConfigCachePostProcessorDelegate getInstance() {",
        ),
        (
            "    public void postProcess(ConfigCache configCache, String content) {",
            "    /** 委托当前后处理器更新 {@link ConfigCache} 的 MD5 等字段 */\n"
            "    public void postProcess(ConfigCache configCache, String content) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigHistoryInfo.java": [
        (
            "/**\n * ConfigHistoryInfo.\n *\n * @author Nacos\n */",
            "/**\n * 配置变更历史记录：保存 dataId/group/tenant、操作类型、发布类型、灰度名称、\n"
            " * 内容快照及操作者 IP/用户等，供历史查询与审计回溯。\n"
            " * ConfigHistoryInfo.\n *\n * @author Nacos\n */",
        ),
        (
            "    private long id;",
            "    /** 历史记录主键 ID */\n"
            "    private long id;",
        ),
        (
            "    private long lastId = -1;",
            "    /** 上一条历史记录 ID，-1 表示无前置版本 */\n"
            "    private long lastId = -1;",
        ),
        (
            "    private String dataId;",
            "    /** 配置 dataId */\n"
            "    private String dataId;",
        ),
        (
            "    private String group;",
            "    /** 配置 group */\n"
            "    private String group;",
        ),
        (
            "    private String tenant;",
            "    /** 命名空间 tenant（namespace） */\n"
            "    private String tenant;",
        ),
        (
            "    /**\n     * Operation type, include inserting, updating and deleting.\n     */",
            "    /**\n     * 操作类型：插入、更新或删除。\n"
            "     * Operation type, include inserting, updating and deleting.\n     */",
        ),
        (
            "    private String publishType;",
            "    /** 发布类型（正式/灰度/Beta 等） */\n"
            "    private String publishType;",
        ),
        (
            "    private String grayName;",
            "    /** 关联的灰度配置名称，非灰度发布时为 null */\n"
            "    private String grayName;",
        ),
        (
            "    private String encryptedDataKey;",
            "    /** 加密配置的数据密钥标识 */\n"
            "    private String encryptedDataKey;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigHistoryInfoDetail.java": [
        (
            "/**\n * ConfigHistoryInfoPair.\n *\n * @author dirtybit\n */",
            "/**\n * 配置变更历史详情：对比变更前后的 MD5、内容、加密密钥与扩展信息，\n"
            " * 供控制台展示 diff 及回滚决策。\n"
            " * ConfigHistoryInfoPair.\n *\n * @author dirtybit\n */",
        ),
        (
            "    private long id;",
            "    /** 历史记录主键 ID */\n"
            "    private long id;",
        ),
        (
            "    /**\n     * Operation type, include inserting, updating and deleting.\n     */",
            "    /**\n     * 操作类型：插入、更新或删除。\n"
            "     * Operation type, include inserting, updating and deleting.\n     */",
        ),
        (
            "    private String originalMd5;",
            "    /** 变更前配置内容 MD5 */\n"
            "    private String originalMd5;",
        ),
        (
            "    private String originalContent;",
            "    /** 变更前配置内容正文 */\n"
            "    private String originalContent;",
        ),
        (
            "    private String updatedMd5;",
            "    /** 变更后配置内容 MD5 */\n"
            "    private String updatedMd5;",
        ),
        (
            "    private String updatedContent;",
            "    /** 变更后配置内容正文 */\n"
            "    private String updatedContent;",
        ),
        (
            "    private String updateExtInfo;",
            "    /** 变更后的扩展信息 JSON */\n"
            "    private String updateExtInfo;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigInfo.java": [
        (
            "/**\n * ConfigInfo.\n *\n * @author boyan\n * @date 2010-5-4\n */",
            "/**\n * 配置主实体：在 {@link ConfigInfoBase} 基础上扩展 tenant、appName、类型、\n"
            " * 描述、标签与修改时间，是 Config 模块最核心的持久化与 API 模型。\n"
            " * ConfigInfo.\n *\n * @author boyan\n * @date 2010-5-4\n */",
        ),
        (
            "    private String tenant;",
            "    /** 命名空间 ID（tenant） */\n"
            "    private String tenant;",
        ),
        (
            "    private String appName;",
            "    /** 关联应用名，用于归属与检索 */\n"
            "    private String appName;",
        ),
        (
            "    private String type;",
            "    /** 配置内容类型（text/json/yaml 等） */\n"
            "    private String type;",
        ),
        (
            "    private String desc;",
            "    /** 配置描述 */\n"
            "    private String desc;",
        ),
        (
            "    private String configTags;",
            "    /** 配置标签，逗号分隔 */\n"
            "    private String configTags;",
        ),
        (
            "    private Long gmtModified;",
            "    /** 最后修改时间（毫秒时间戳） */\n"
            "    private Long gmtModified;",
        ),
        (
            "    public ConfigInfo(String dataId, String group, String tenant, String appName, String content) {",
            "    /** 以 dataId、group、tenant、appName 与 content 构造完整配置实体 */\n"
            "    public ConfigInfo(String dataId, String group, String tenant, String appName, String content) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigInfo4Beta.java": [
        (
            "/**\n * ConfigInfo4Beta.\n *\n * @author Nacos\n */",
            "/**\n * Beta 灰度配置实体：继承 {@link ConfigInfo} 并附加 {@code betaIps} 字段，\n"
            " * 限定仅指定 IP 列表的客户端可拉取该 Beta 版本配置。\n"
            " * ConfigInfo4Beta.\n *\n * @author Nacos\n */",
        ),
        (
            "    private String betaIps;",
            "    /** 允许拉取 Beta 配置的客户端 IP 列表，逗号分隔 */\n"
            "    private String betaIps;",
        ),
        (
            "    public ConfigInfo4Beta(String dataId, String group, String appName, String content,\n        String betaIps) {",
            "    /** 构造带 Beta IP 白名单的配置实体 */\n"
            "    public ConfigInfo4Beta(String dataId, String group, String appName, String content,\n        String betaIps) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigInfo4Tag.java": [
        (
            "/**\n * ConfigInfo4Tag.\n *\n * @author Nacos\n */",
            "/**\n * 标签维度配置实体：继承 {@link ConfigInfo} 并附加 {@code tag} 字段，\n"
            " * 支持同一 dataId/group 下按标签隔离不同配置版本。\n"
            " * ConfigInfo4Tag.\n *\n * @author Nacos\n */",
        ),
        (
            "    private String tag;",
            "    /** 配置标签名，与 dataId/group 共同构成唯一键 */\n"
            "    private String tag;",
        ),
        (
            "    public ConfigInfo4Tag(String dataId, String group, String tag, String appName, String content) {",
            "    /** 以 dataId、group、tag、appName 与 content 构造标签配置实体 */\n"
            "    public ConfigInfo4Tag(String dataId, String group, String tag, String appName, String content) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigInfoBase.java": [
        (
            "/**\n * ConfigInfoBase.\n * And can't add field, to compatible with old interface(If adding a field, then it will occur compatibility problems).\n *\n * @author Nacos\n */",
            "/**\n * 配置基础实体：包含 id、dataId、group、content、md5 与加密密钥，\n"
            " * 字段集合固定以兼容旧版 Open API，不可随意增删字段。\n"
            " * ConfigInfoBase.\n * And can't add field, to compatible with old interface(If adding a field, then it will occur compatibility problems).\n *\n * @author Nacos\n */",
        ),
        (
            "    private long id;",
            "    /** 数据库主键 ID */\n"
            "    private long id;",
        ),
        (
            "    private String dataId;",
            "    /** 配置 dataId，业务唯一标识之一 */\n"
            "    private String dataId;",
        ),
        (
            "    private String group;",
            "    /** 配置 group，默认 {@link com.alibaba.nacos.config.server.constant.Constants#DEFAULT_GROUP} */\n"
            "    private String group;",
        ),
        (
            "    private String content;",
            "    /** 配置内容正文 */\n"
            "    private String content;",
        ),
        (
            "    private String md5;",
            "    /** 配置内容 MD5 摘要，用于客户端变更检测 */\n"
            "    private String md5;",
        ),
        (
            "    private String encryptedDataKey;",
            "    /** 加密配置的数据密钥标识 */\n"
            "    private String encryptedDataKey;",
        ),
        (
            "    public ConfigInfoBase(String dataId, String group, String content) {",
            "    /** 构造基础配置并自动按 {@link Constants#PERSIST_ENCODE} 计算 MD5 */\n"
            "    public ConfigInfoBase(String dataId, String group, String content) {",
        ),
        (
            "    public void dump(PrintWriter writer) {",
            "    /** 将配置内容写入 {@link PrintWriter}，供导出或流式响应 */\n"
            "    public void dump(PrintWriter writer) {",
        ),
        (
            "    @Override\n    public int compareTo(ConfigInfoBase o) {",
            "    /** 按 dataId、group、content 字典序比较，用于排序与去重 */\n"
            "    @Override\n    public int compareTo(ConfigInfoBase o) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/model/ConfigInfoBaseEx.java": [
        (
            "/**\n * ConfigInfoBaseEx.\n * And can't add field, to compatible with old interface(If adding a field, then it will occur compatibility problems).\n *\n * @author Nacos\n */",
            "/**\n * 带状态码的配置基础扩展：在 {@link ConfigInfoBase} 上附加单条批量操作的结果\n"
            " * status 与 message，字段集合固定以兼容旧版批量 API。\n"
            " * ConfigInfoBaseEx.\n * And can't add field, to compatible with old interface(If adding a field, then it will occur compatibility problems).\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Single message status code, when querying for batch.\n     * And details of message status code, you can see Constants.java.\n     */",
            "    /**\n     * 单条批量操作结果状态码，详见 {@link com.alibaba.nacos.config.server.constant.Constants}。\n"
            "     * Single message status code, when querying for batch.\n     * And details of message status code, you can see Constants.java.\n     */",
        ),
        (
            "    /**\n     * Single message information, when querying for batch.\n     */",
            "    /**\n     * 单条批量操作的说明信息（成功或失败原因）。\n"
            "     * Single message information, when querying for batch.\n     */",
        ),
        (
            "    public ConfigInfoBaseEx(String dataId, String group, String content, int status,\n        String message) {",
            "    /** 构造带状态码与消息的扩展配置实体 */\n"
            "    public ConfigInfoBaseEx(String dataId, String group, String content, int status,\n        String message) {",
        ),
    ],
}
