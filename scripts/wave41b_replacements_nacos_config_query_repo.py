"""Chinese annotation replacements for Nacos 3.2.3 wave41b [15:30] config query/repo."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/DefaultConfigQueryHandlerChainBuilder.java": [
        (
            "/**\n * DefaultConfigQueryHandlerChainBuilder.\n *\n * @author Nacos\n */",
            "/**\n * 默认配置查询责任链构建器：按固定顺序组装入口、内容类型、灰度匹配、"
            "特殊 Tag 与正式配置处理器。\n"
            " * DefaultConfigQueryHandlerChainBuilder.\n *\n * @author Nacos\n */",
        ),
        (
            "    @Override\n    public ConfigQueryHandlerChain build() {",
            "    /** 构建 Nacos 默认查询链：Entry → ContentType → Gray → SpecialTag → Formal */\n"
            "    @Override\n    public ConfigQueryHandlerChain build() {",
        ),
        (
            "    @Override\n    public String getName() {",
            "    /** 返回构建器标识 {@code nacos} */\n    @Override\n    public String getName() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/enums/ResponseCode.java": [
        (
            "/**\n * ResponseCode.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询链 HTTP 风格响应码枚举，供 {@link com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse} 封装错误信息。\n"
            " * ResponseCode.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Request success.\n     */",
            "    /**\n     * 请求成功（200）。\n     * Request success.\n     */",
        ),
        (
            "    /**\n     * Request failed.\n     */",
            "    /**\n     * 请求失败（500）。\n     * Request failed.\n     */",
        ),
        (
            "    int code;",
            "    /** HTTP 状态码数值 */\n    int code;",
        ),
        (
            "    String desc;",
            "    /** 响应描述文案 */\n    String desc;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/AbstractConfigQueryHandler.java": [
        (
            "/**\n * AbstractConfigQueryHandler. This abstract class provides a base implementation for configuration query handlers. It\n"
            " * implements the {@link ConfigQueryHandler} interface and handles the chaining of handlers.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询处理器抽象基类：实现 {@link ConfigQueryHandler} 并维护责任链中的 nextHandler 指针。\n"
            " * AbstractConfigQueryHandler. This abstract class provides a base implementation for configuration query handlers. It\n"
            " * implements the {@link ConfigQueryHandler} interface and handles the chaining of handlers.\n *\n * @author Nacos\n */",
        ),
        (
            "    public ConfigQueryHandler nextHandler;",
            "    /** 责任链中下一个处理器 */\n    public ConfigQueryHandler nextHandler;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/ConfigChainEntryHandler.java": [
        (
            "/**\n * ConfigChainEntryHandler.\n"
            " * The entry point handler for the responsibility chain, responsible for initializing the chain and handling configuration query requests.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询责任链入口处理器：规范化 tenant、加读锁加载 {@link CacheItem}，"
            "并将请求传递给后续处理器。\n"
            " * ConfigChainEntryHandler.\n"
            " * The entry point handler for the responsibility chain, responsible for initializing the chain and handling configuration query requests.\n *\n * @author Nacos\n */",
        ),
        (
            "    private static final ThreadLocal<CacheItem> CACHE_ITEM_THREAD_LOCAL = new ThreadLocal<>();",
            "    /** 当前线程持有的缓存项，供链内后续 Handler 读取 */\n"
            "    private static final ThreadLocal<CacheItem> CACHE_ITEM_THREAD_LOCAL = new ThreadLocal<>();",
        ),
        (
            "        request.setTenant(NamespaceUtil.processNamespaceParameter(request.getTenant()));",
            "        // 规范化 namespace/tenant 参数\n        request.setTenant(NamespaceUtil.processNamespaceParameter(request.getTenant()));",
        ),
        (
            "        if (lockResult > 0 && cacheItem != null) {",
            "        // 读锁成功且缓存存在：设置 ThreadLocal 并委托下一处理器\n        if (lockResult > 0 && cacheItem != null) {",
        ),
        (
            "        } else if (lockResult == 0 || cacheItem == null) {",
            "        // 未命中缓存或锁竞争失败：返回 CONFIG_NOT_FOUND\n        } else if (lockResult == 0 || cacheItem == null) {",
        ),
        (
            "        } else {",
            "        // 读锁冲突：返回 CONFIG_QUERY_CONFLICT\n        } else {",
        ),
        (
            "    public static CacheItem getThreadLocalCacheItem() {",
            "    /** 供链内 Handler 获取入口阶段缓存的 {@link CacheItem} */\n"
            "    public static CacheItem getThreadLocalCacheItem() {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/ConfigContentTypeHandler.java": [
        (
            "/**\n * The type Config content type handler.\n * @author Sunrisea\n */",
            "/**\n * 配置内容类型处理器：在下游返回有效配置后，根据扩展名或已有 contentType "
            "解析 {@link com.alibaba.nacos.config.server.enums.FileTypeEnum} 并设置 HTTP Content-Type 头。\n"
            " * The type Config content type handler.\n * @author Sunrisea\n */",
        ),
        (
            "        ConfigQueryChainResponse response = getNextHandler().handle(request);",
            "        // 先委托后续处理器获取配置内容\n        ConfigQueryChainResponse response = getNextHandler().handle(request);",
        ),
        (
            "        if (response.getStatus() == ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND",
            "        // 未找到或特殊 Tag 未命中时直接透传\n        if (response.getStatus() == ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/ConfigQueryHandler.java": [
        (
            "/**\n * Configuration Query Handler Interface.\n"
            " * This interface defines the standard methods for handling configuration query requests.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询责任链处理器接口：定义名称、处理逻辑及链式传递方法。\n"
            " * Configuration Query Handler Interface.\n"
            " * This interface defines the standard methods for handling configuration query requests.\n *\n * @author Nacos\n */",
        ),
        (
            "    /**\n     * Gets the name of the handler.\n     * @return The name of the handler.\n     */",
            "    /**\n     * 获取处理器唯一名称，用于日志与链路追踪。\n     * Gets the name of the handler.\n     * @return The name of the handler.\n     */",
        ),
        (
            "    /**\n     * Handles the configuration query request.\n"
            "     * If the current handler cannot process the request, it should throw an IOException.\n"
            "     * @param request The configuration query request.\n"
            "     * @return The response to the configuration query.\n"
            "     * @throws IOException If an I/O error occurs.\n     */",
            "    /**\n     * 处理配置查询请求；无法处理时应委托 {@link #getNextHandler()} 或返回对应状态。\n"
            "     * Handles the configuration query request.\n"
            "     * If the current handler cannot process the request, it should throw an IOException.\n"
            "     * @param request The configuration query request.\n"
            "     * @return The response to the configuration query.\n"
            "     * @throws IOException If an I/O error occurs.\n     */",
        ),
        (
            "    /**\n     * Sets the next handler in the chain.\n"
            "     * @param nextHandler The next handler to which the request can be passed if the current handler cannot process it.\n     */",
            "    /**\n     * 设置责任链中的下一个处理器。\n"
            "     * Sets the next handler in the chain.\n"
            "     * @param nextHandler The next handler to which the request can be passed if the current handler cannot process it.\n     */",
        ),
        (
            "    /**\n     * Gets the next handler in the chain.\n     * @return The next handler.\n     */",
            "    /**\n     * 获取责任链中的下一个处理器。\n"
            "     * Gets the next handler in the chain.\n     * @return The next handler.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/FormalHandler.java": [
        (
            "/**\n * Formal Handler. This class represents a formal handler in the configuration query processing chain. If the request\n"
            " * has not been processed by previous handlers, it will be handled by this handler.\n *\n * @author Nacos\n */",
            "/**\n * 正式配置查询处理器（责任链末端）：从磁盘读取正式配置内容，"
            "填充 MD5、加密密钥与最后修改时间。\n"
            " * Formal Handler. This class represents a formal handler in the configuration query processing chain. If the request\n"
            " * has not been processed by previous handlers, it will be handled by this handler.\n *\n * @author Nacos\n */",
        ),
        (
            "        CacheItem cacheItem = ConfigChainEntryHandler.getThreadLocalCacheItem();",
            "        // 从入口 Handler 的 ThreadLocal 获取缓存元数据\n        CacheItem cacheItem = ConfigChainEntryHandler.getThreadLocalCacheItem();",
        ),
        (
            "        if (StringUtils.isBlank(content)) {",
            "        // 磁盘内容为空视为配置不存在\n        if (StringUtils.isBlank(content)) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/GrayRuleMatchHandler.java": [
        (
            "/**\n * GrayRuleMatchHandler. This class represents a gray rule handler in the configuration query processing chain. It\n"
            " * checks if the request matches any gray rules and processes the request accordingly.\n *\n * @author Nacos\n */",
            "/**\n * 灰度规则匹配处理器：遍历 {@link CacheItem} 中排序后的灰度规则，"
            "按客户端 appLabels 匹配并返回灰度配置内容。\n"
            " * GrayRuleMatchHandler. This class represents a gray rule handler in the configuration query processing chain. It\n"
            " * checks if the request matches any gray rules and processes the request accordingly.\n *\n * @author Nacos\n */",
        ),
        (
            "        // Check if the request matches any gray rules",
            "        // 遍历缓存中的灰度规则，按 appLabels 匹配首个命中项",
        ),
        (
            "        if (matchedGray != null) {",
            "        // 命中灰度：从磁盘读取灰度内容并返回 CONFIG_FOUND_GRAY\n        if (matchedGray != null) {",
        ),
        (
            "        } else {",
            "        // 未命中灰度：继续传递给下一处理器\n        } else {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/handler/SpecialTagNotFoundHandler.java": [
        (
            "/**\n * SpecialTagNotFound Handler.\n"
            " * This class represents special tag not found handler in the configuration query processing chain.\n *\n * @author Nacos\n */",
            "/**\n * 特殊 Tag 未找到处理器：当请求携带 tag 参数时直接返回 "
            "{@link com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse.ConfigQueryStatus#SPECIAL_TAG_CONFIG_NOT_FOUND}。\n"
            " * SpecialTagNotFound Handler.\n"
            " * This class represents special tag not found handler in the configuration query processing chain.\n *\n * @author Nacos\n */",
        ),
        (
            "        if (StringUtils.isNotBlank(request.getTag())) {",
            "        // 请求含 tag 时不再走正式配置，直接标记特殊 Tag 未找到\n        if (StringUtils.isNotBlank(request.getTag())) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/model/ConfigQueryChainRequest.java": [
        (
            "/**\n * ConfigQueryChainRequest.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询责任链请求模型：封装 dataId、group、tenant、tag 及客户端 appLabels。\n"
            " * ConfigQueryChainRequest.\n *\n * @author Nacos\n */",
        ),
        (
            "    private String dataId;",
            "    /** 配置 dataId */\n    private String dataId;",
        ),
        (
            "    private String group;",
            "    /** 配置 group */\n    private String group;",
        ),
        (
            "    private String tenant;",
            "    /** 命名空间/tenant */\n    private String tenant;",
        ),
        (
            "    private String tag;",
            "    /** 特殊 Tag（Beta/Tag 灰度场景） */\n    private String tag;",
        ),
        (
            "    private Map<String, String> appLabels;",
            "    /** 客户端应用标签，用于灰度规则匹配 */\n    private Map<String, String> appLabels;",
        ),
        (
            "    /**\n     * buildConfigQueryChainRequest.\n",
            "    /**\n     * 便捷构建仅含 dataId/group/tenant 的查询请求。\n     * buildConfigQueryChainRequest.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/query/model/ConfigQueryChainResponse.java": [
        (
            "/**\n * ConfigQueryChainResponse.\n *\n * @author Nacos\n */",
            "/**\n * 配置查询责任链响应模型：包含配置内容、MD5、Content-Type、"
            "灰度匹配结果及 {@link ConfigQueryStatus} 状态。\n"
            " * ConfigQueryChainResponse.\n *\n * @author Nacos\n */",
        ),
        (
            "    public enum ConfigQueryStatus {",
            "    /** 配置查询结果状态枚举 */\n    public enum ConfigQueryStatus {",
        ),
        (
            "        /**\n         * Indicates that the configuration was found and is formal.\n         */",
            "        /**\n         * 命中正式配置。\n         * Indicates that the configuration was found and is formal.\n         */",
        ),
        (
            "        /**\n         * Indicates that the configuration was found and is gray.\n         */",
            "        /**\n         * 命中灰度配置。\n         * Indicates that the configuration was found and is gray.\n         */",
        ),
        (
            "        /**\n         * Indicates that the configuration special tag was not found.\n         */",
            "        /**\n         * 特殊 Tag 配置未找到。\n         * Indicates that the configuration special tag was not found.\n         */",
        ),
        (
            "        /**\n         * Indicates that the configuration was not found.\n         */",
            "        /**\n         * 配置不存在。\n         * Indicates that the configuration was not found.\n         */",
        ),
        (
            "        /**\n         * Indicates a conflict in the configuration query.\n         */",
            "        /**\n         * 配置查询冲突（如读锁竞争）。\n         * Indicates a conflict in the configuration query.\n         */",
        ),
        (
            "    /**\n     * Build fail response.\n",
            "    /**\n     * 构建带错误码与消息的失败响应。\n     * Build fail response.\n",
        ),
        (
            "    public void setErrorInfo(int errorCode, String errorMsg) {",
            "    /** 设置失败响应的错误码与消息（使用 {@link ResponseCode#FAIL}） */\n"
            "    public void setErrorInfo(int errorCode, String errorMsg) {",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/repository/ConfigInfoBetaPersistService.java": [
        (
            "/**\n * Database service, providing access to config_info_beta in the database.\n"
            " * Deprecated since 2.5.0，only support on compatibility,replaced with ConfigInfoGray model, will be  soon removed on further version.\n"
            " * @author lixiaoshuang\n */",
            "/**\n * Beta 配置持久化服务接口：访问数据库 {@code config_info_beta} 表（2.5.0 起已废弃，"
            "由 {@link ConfigInfoGrayPersistService} 替代，仅保留兼容）。\n"
            " * Database service, providing access to config_info_beta in the database.\n"
            " * Deprecated since 2.5.0，only support on compatibility,replaced with ConfigInfoGray model, will be  soon removed on further version.\n"
            " * @author lixiaoshuang\n */",
        ),
        (
            "    //------------------------------------------insert---------------------------------------------//",
            "    //------------------------------------------insert 插入---------------------------------------------//",
        ),
        (
            "    //------------------------------------------delete---------------------------------------------//",
            "    //------------------------------------------delete 删除---------------------------------------------//",
        ),
        (
            "    //------------------------------------------update---------------------------------------------//",
            "    //------------------------------------------update 更新---------------------------------------------//",
        ),
        (
            "    //------------------------------------------select---------------------------------------------//",
            "    //------------------------------------------select 查询---------------------------------------------//",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/repository/ConfigInfoGrayPersistService.java": [
        (
            "/**\n * Database service, providing access to config_info_gray in the database.\n *\n * @author rong\n */",
            "/**\n * 灰度配置持久化服务接口：访问数据库 {@code config_info_gray} 表，"
            "提供灰度配置的增删改查与 dump 分页查询。\n"
            " * Database service, providing access to config_info_gray in the database.\n *\n * @author rong\n */",
        ),
        (
            "    //------------------------------------------insert---------------------------------------------//",
            "    //------------------------------------------insert 插入---------------------------------------------//",
        ),
        (
            "    //------------------------------------------delete---------------------------------------------//",
            "    //------------------------------------------delete 删除---------------------------------------------//",
        ),
        (
            "    //------------------------------------------update---------------------------------------------//",
            "    //------------------------------------------update 更新---------------------------------------------//",
        ),
        (
            "    //------------------------------------------select---------------------------------------------//",
            "    //------------------------------------------select 查询---------------------------------------------//",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/repository/ConfigInfoPersistService.java": [
        (
            "/**\n * Database service, providing access to config_info in the database.\n *\n * @author lixiaoshuang\n */",
            "/**\n * 正式配置持久化服务接口：访问数据库 {@code config_info} 主表，"
            "提供配置的 CRUD、批量导入、分页查询与变更追踪。\n"
            " * Database service, providing access to config_info in the database.\n *\n * @author lixiaoshuang\n */",
        ),
        (
            "    String PATTERN_STR = \"*\";",
            "    /** 模糊查询通配符 */\n    String PATTERN_STR = \"*\";",
        ),
        (
            "    Object[] EMPTY_ARRAY = new Object[] {};",
            "    /** 空参数数组常量 */\n    Object[] EMPTY_ARRAY = new Object[] {};",
        ),
        (
            "    //------------------------------------------insert---------------------------------------------//",
            "    //------------------------------------------insert 插入---------------------------------------------//",
        ),
        (
            "    //------------------------------------------delete---------------------------------------------//",
            "    //------------------------------------------delete 删除---------------------------------------------//",
        ),
        (
            "    //------------------------------------------update---------------------------------------------//",
            "    //------------------------------------------update 更新---------------------------------------------//",
        ),
        (
            "    //------------------------------------------select---------------------------------------------//",
            "    //------------------------------------------select 查询---------------------------------------------//",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/repository/ConfigInfoTagPersistService.java": [
        (
            "/**\n * Database service, providing access to config_info_tag in the database.\n"
            " * Deprecated since 2.5.0，only support on compatibility,replaced with ConfigInfoGray model, will be  soon removed on further version.\n"
            " * @author lixiaoshuang\n */",
            "/**\n * Tag 配置持久化服务接口：访问数据库 {@code config_info_tag} 表（2.5.0 起已废弃，"
            "由灰度模型替代，仅保留兼容）。\n"
            " * Database service, providing access to config_info_tag in the database.\n"
            " * Deprecated since 2.5.0，only support on compatibility,replaced with ConfigInfoGray model, will be  soon removed on further version.\n"
            " * @author lixiaoshuang\n */",
        ),
        (
            "    //------------------------------------------insert---------------------------------------------//",
            "    //------------------------------------------insert 插入---------------------------------------------//",
        ),
        (
            "    //------------------------------------------delete---------------------------------------------//",
            "    //------------------------------------------delete 删除---------------------------------------------//",
        ),
        (
            "    //------------------------------------------update---------------------------------------------//",
            "    //------------------------------------------update 更新---------------------------------------------//",
        ),
        (
            "    //------------------------------------------select---------------------------------------------//",
            "    //------------------------------------------select 查询---------------------------------------------//",
        ),
    ],
}
