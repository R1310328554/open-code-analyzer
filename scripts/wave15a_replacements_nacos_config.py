"""Chinese annotation replacements for Nacos 3.2.3 wave15a [0:15] config model/remote request."""

R: dict[str, list[tuple[str, str]]] = {}

# --- ConfigDetailInfo ---

R["api/src/main/java/com/alibaba/nacos/api/config/model/ConfigDetailInfo.java"] = [
    (
        "/**\n * Nacos configuration detail information.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 配置详情信息，在 {@link ConfigBasicInfo} 基础上包含完整内容与创建元数据。\n *\n * <p>控制台查询、导出等场景返回本类型。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private String content;\n    \n    private String encryptedDataKey;\n    \n    private String createUser;\n    \n    private String createIp;",
        "    /** 配置内容正文。 */\n    private String content;\n    \n    /** 加密数据密钥（启用加密时非空）。 */\n    private String encryptedDataKey;\n    \n    /** 创建该配置的用户名。 */\n    private String createUser;\n    \n    /** 创建该配置的客户端 IP。 */\n    private String createIp;",
    ),
    (
        "    public String getContent() {\n        return content;\n    }\n    \n    public void setContent(String content) {\n        this.content = content;\n    }\n    \n    public String getEncryptedDataKey() {\n        return encryptedDataKey;\n    }\n    \n    public void setEncryptedDataKey(String encryptedDataKey) {\n        this.encryptedDataKey = encryptedDataKey;\n    }\n    \n    public String getCreateUser() {\n        return createUser;\n    }\n    \n    public void setCreateUser(String createUser) {\n        this.createUser = createUser;\n    }\n    \n    public String getCreateIp() {\n        return createIp;\n    }\n    \n    public void setCreateIp(String createIp) {\n        this.createIp = createIp;\n    }",
        "    /** 获取配置内容。 */\n    public String getContent() {\n        return content;\n    }\n    \n    /** 设置配置内容。 */\n    public void setContent(String content) {\n        this.content = content;\n    }\n    \n    /** 获取加密数据密钥。 */\n    public String getEncryptedDataKey() {\n        return encryptedDataKey;\n    }\n    \n    /** 设置加密数据密钥。 */\n    public void setEncryptedDataKey(String encryptedDataKey) {\n        this.encryptedDataKey = encryptedDataKey;\n    }\n    \n    /** 获取创建用户。 */\n    public String getCreateUser() {\n        return createUser;\n    }\n    \n    /** 设置创建用户。 */\n    public void setCreateUser(String createUser) {\n        this.createUser = createUser;\n    }\n    \n    /** 获取创建 IP。 */\n    public String getCreateIp() {\n        return createIp;\n    }\n    \n    /** 设置创建 IP。 */\n    public void setCreateIp(String createIp) {\n        this.createIp = createIp;\n    }",
    ),
]

# --- ConfigGrayInfo ---

R["api/src/main/java/com/alibaba/nacos/api/config/model/ConfigGrayInfo.java"] = [
    (
        "/**\n * Nacos configuration gray information.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 配置灰度信息，在 {@link ConfigDetailInfo} 基础上附加灰度名称与规则。\n *\n * <p>用于灰度发布场景下区分不同灰度版本。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private String grayName;\n    \n    private String grayRule;",
        "    /** 灰度版本名称。 */\n    private String grayName;\n    \n    /** 灰度匹配规则表达式。 */\n    private String grayRule;",
    ),
    (
        "    public String getGrayName() {\n        return grayName;\n    }\n    \n    public void setGrayName(String grayName) {\n        this.grayName = grayName;\n    }\n    \n    public String getGrayRule() {\n        return grayRule;\n    }\n    \n    public void setGrayRule(String grayRule) {\n        this.grayRule = grayRule;\n    }",
        "    /** 获取灰度名称。 */\n    public String getGrayName() {\n        return grayName;\n    }\n    \n    /** 设置灰度名称。 */\n    public void setGrayName(String grayName) {\n        this.grayName = grayName;\n    }\n    \n    /** 获取灰度规则。 */\n    public String getGrayRule() {\n        return grayRule;\n    }\n    \n    /** 设置灰度规则。 */\n    public void setGrayRule(String grayRule) {\n        this.grayRule = grayRule;\n    }",
    ),
]

# --- ConfigHistoryBasicInfo ---

R["api/src/main/java/com/alibaba/nacos/api/config/model/ConfigHistoryBasicInfo.java"] = [
    (
        "/**\n * Nacos configuration history basic information.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 配置历史记录概要信息。\n *\n * <p>包含操作来源、操作类型及发布方式等元数据，不含完整配置内容。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private String srcIp;\n    \n    private String srcUser;\n    \n    /**\n     * Operation type, include inserting, updating and deleting.\n     */\n    private String opType;\n    \n    private String publishType;",
        "    /** 执行操作的来源 IP。 */\n    private String srcIp;\n    \n    /** 执行操作的用户名。 */\n    private String srcUser;\n    \n    /** 操作类型，如新增、更新或删除。 */\n    private String opType;\n    \n    /** 发布方式（如正式或灰度）。 */\n    private String publishType;",
    ),
    (
        "    public String getSrcIp() {\n        return srcIp;\n    }\n    \n    public void setSrcIp(String srcIp) {\n        this.srcIp = srcIp;\n    }\n    \n    public String getSrcUser() {\n        return srcUser;\n    }\n    \n    public void setSrcUser(String srcUser) {\n        this.srcUser = srcUser;\n    }\n    \n    public String getOpType() {\n        return opType;\n    }\n    \n    public void setOpType(String opType) {\n        this.opType = opType;\n    }\n    \n    public String getPublishType() {\n        return publishType;\n    }\n    \n    public void setPublishType(String publishType) {\n        this.publishType = publishType;\n    }",
        "    /** 获取操作来源 IP。 */\n    public String getSrcIp() {\n        return srcIp;\n    }\n    \n    /** 设置操作来源 IP。 */\n    public void setSrcIp(String srcIp) {\n        this.srcIp = srcIp;\n    }\n    \n    /** 获取操作用户。 */\n    public String getSrcUser() {\n        return srcUser;\n    }\n    \n    /** 设置操作用户。 */\n    public void setSrcUser(String srcUser) {\n        this.srcUser = srcUser;\n    }\n    \n    /** 获取操作类型。 */\n    public String getOpType() {\n        return opType;\n    }\n    \n    /** 设置操作类型。 */\n    public void setOpType(String opType) {\n        this.opType = opType;\n    }\n    \n    /** 获取发布方式。 */\n    public String getPublishType() {\n        return publishType;\n    }\n    \n    /** 设置发布方式。 */\n    public void setPublishType(String publishType) {\n        this.publishType = publishType;\n    }",
    ),
]

# --- ConfigHistoryDetailInfo ---

R["api/src/main/java/com/alibaba/nacos/api/config/model/ConfigHistoryDetailInfo.java"] = [
    (
        "/**\n * Nacos configuration history detail information.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 配置历史详情，包含某次变更的完整内容与扩展信息。\n *\n * <p>继承 {@link ConfigHistoryBasicInfo} 的操作元数据。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    private String content;\n    \n    private String encryptedDataKey;\n    \n    private String grayName;\n    \n    private String extInfo;",
        "    /** 该历史版本对应的配置内容。 */\n    private String content;\n    \n    /** 加密数据密钥。 */\n    private String encryptedDataKey;\n    \n    /** 关联的灰度名称（灰度发布时）。 */\n    private String grayName;\n    \n    /** 扩展信息 JSON 字符串。 */\n    private String extInfo;",
    ),
    (
        "    public String getContent() {\n        return content;\n    }\n    \n    public void setContent(String content) {\n        this.content = content;\n    }\n    \n    public String getEncryptedDataKey() {\n        return encryptedDataKey;\n    }\n    \n    public void setEncryptedDataKey(String encryptedDataKey) {\n        this.encryptedDataKey = encryptedDataKey;\n    }\n    \n    public String getGrayName() {\n        return grayName;\n    }\n    \n    public void setGrayName(String grayName) {\n        this.grayName = grayName;\n    }\n    \n    public String getExtInfo() {\n        return extInfo;\n    }\n    \n    public void setExtInfo(String extInfo) {\n        this.extInfo = extInfo;\n    }",
        "    /** 获取历史配置内容。 */\n    public String getContent() {\n        return content;\n    }\n    \n    /** 设置历史配置内容。 */\n    public void setContent(String content) {\n        this.content = content;\n    }\n    \n    /** 获取加密数据密钥。 */\n    public String getEncryptedDataKey() {\n        return encryptedDataKey;\n    }\n    \n    /** 设置加密数据密钥。 */\n    public void setEncryptedDataKey(String encryptedDataKey) {\n        this.encryptedDataKey = encryptedDataKey;\n    }\n    \n    /** 获取灰度名称。 */\n    public String getGrayName() {\n        return grayName;\n    }\n    \n    /** 设置灰度名称。 */\n    public void setGrayName(String grayName) {\n        this.grayName = grayName;\n    }\n    \n    /** 获取扩展信息。 */\n    public String getExtInfo() {\n        return extInfo;\n    }\n    \n    /** 设置扩展信息。 */\n    public void setExtInfo(String extInfo) {\n        this.extInfo = extInfo;\n    }",
    ),
]

# --- ConfigListenerInfo ---

R["api/src/main/java/com/alibaba/nacos/api/config/model/ConfigListenerInfo.java"] = [
    (
        "/**\n * Nacos configuration listeners information.\n *\n * @author xiweng.yy\n */",
        "/**\n * Nacos 配置监听器查询结果。\n *\n * <p>可按配置维度或 IP 维度查询当前监听该配置的客户端列表及状态。</p>\n *\n * @author xiweng.yy\n */",
    ),
    (
        "    public static final String QUERY_TYPE_CONFIG = \"config\";\n    \n    public static final String QUERY_TYPE_IP = \"ip\";\n    \n    private String queryType;\n    \n    private Map<String, String> listenersStatus;",
        "    /** 按配置（dataId/group）查询监听器。 */\n    public static final String QUERY_TYPE_CONFIG = \"config\";\n    \n    /** 按客户端 IP 查询监听器。 */\n    public static final String QUERY_TYPE_IP = \"ip\";\n    \n    /** 查询类型，见 {@link #QUERY_TYPE_CONFIG} 与 {@link #QUERY_TYPE_IP}。 */\n    private String queryType;\n    \n    /** 监听器标识到状态的映射。 */\n    private Map<String, String> listenersStatus;",
    ),
    (
        "    public String getQueryType() {\n        return queryType;\n    }\n    \n    public void setQueryType(String queryType) {\n        this.queryType = queryType;\n    }\n    \n    public Map<String, String> getListenersStatus() {\n        return listenersStatus;\n    }\n    \n    public void setListenersStatus(Map<String, String> listenersStatus) {\n        this.listenersStatus = listenersStatus;\n    }",
        "    /** 获取查询类型。 */\n    public String getQueryType() {\n        return queryType;\n    }\n    \n    /** 设置查询类型。 */\n    public void setQueryType(String queryType) {\n        this.queryType = queryType;\n    }\n    \n    /** 获取监听器状态映射。 */\n    public Map<String, String> getListenersStatus() {\n        return listenersStatus;\n    }\n    \n    /** 设置监听器状态映射。 */\n    public void setListenersStatus(Map<String, String> listenersStatus) {\n        this.listenersStatus = listenersStatus;\n    }",
    ),
]

# --- SameConfigPolicy ---

R["api/src/main/java/com/alibaba/nacos/api/config/model/SameConfigPolicy.java"] = [
    (
        "/**\n * SameConfigPolicy.\n *\n * @author klw\n */",
        "/**\n * 导入配置时遇到同名配置的处理策略。\n *\n * <p>用于批量导入或迁移场景，决定冲突时中止、跳过或覆盖。</p>\n *\n * @author klw\n */",
    ),
    (
        "    /**\n     * Abort import  on duplicate.\n     */\n    ABORT,\n    \n    /**\n     * Skipping on duplicate.\n     */\n    SKIP,\n    \n    /**\n     * Overwrite on duplicate.\n     */\n    OVERWRITE",
        "    /** 发现重复配置时中止整个导入。 */\n    ABORT,\n    \n    /** 发现重复配置时跳过该项。 */\n    SKIP,\n    \n    /** 发现重复配置时覆盖已有配置。 */\n    OVERWRITE",
    ),
]

# --- AbstractConfigRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/AbstractConfigRequest.java"] = [
    (
        "/**\n * abstract request of config module request,all config module request should extends this class.\n *\n * @author liuzunfei\n * @version $Id: ConfigCommonRequest.java, v 0.1 2020年07月13日 9:05 PM liuzunfei Exp $\n */",
        "/**\n * 配置模块远程请求的抽象基类，所有配置相关客户端请求应继承本类。\n *\n * <p>统一携带 dataId、group、tenant 三元组及模块标识。</p>\n *\n * @author liuzunfei\n * @version $Id: ConfigCommonRequest.java, v 0.1 2020年07月13日 9:05 PM liuzunfei Exp $\n */",
    ),
    (
        "    private String dataId;\n    \n    private String group;\n    \n    private String tenant;",
        "    /** 配置 Data ID。 */\n    private String dataId;\n    \n    /** 配置分组。 */\n    private String group;\n    \n    /** 命名空间（租户）ID。 */\n    private String tenant;",
    ),
    (
        "    public String getDataId() {\n        return dataId;\n    }\n    \n    public void setDataId(String dataId) {\n        this.dataId = dataId;\n    }\n    \n    public String getGroup() {\n        return group;\n    }\n    \n    public void setGroup(String group) {\n        this.group = group;\n    }\n    \n    public String getTenant() {\n        return tenant;\n    }\n    \n    public void setTenant(String tenant) {\n        this.tenant = tenant;\n    }\n    \n    @Override\n    public String getModule() {\n        return Constants.Config.CONFIG_MODULE;\n    }",
        "    /** 获取配置 Data ID。 */\n    public String getDataId() {\n        return dataId;\n    }\n    \n    /** 设置配置 Data ID。 */\n    public void setDataId(String dataId) {\n        this.dataId = dataId;\n    }\n    \n    /** 获取配置分组。 */\n    public String getGroup() {\n        return group;\n    }\n    \n    /** 设置配置分组。 */\n    public void setGroup(String group) {\n        this.group = group;\n    }\n    \n    /** 获取命名空间 ID。 */\n    public String getTenant() {\n        return tenant;\n    }\n    \n    /** 设置命名空间 ID。 */\n    public void setTenant(String tenant) {\n        this.tenant = tenant;\n    }\n    \n    /** 返回配置模块标识。 */\n    @Override\n    public String getModule() {\n        return Constants.Config.CONFIG_MODULE;\n    }",
    ),
]

# --- AbstractFuzzyWatchNotifyRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/AbstractFuzzyWatchNotifyRequest.java"] = [
    (
        "/**\n * AbstractFuzzyListenNotifyRequest.\n *\n * @author stone-98\n * @date 2024/3/14\n */",
        "/**\n * 模糊监听通知类服务端请求的抽象基类。\n *\n * <p>服务端向客户端推送模糊订阅变更或同步时使用。</p>\n *\n * @author stone-98\n * @date 2024/3/14\n */",
    ),
    (
        "    public AbstractFuzzyWatchNotifyRequest() {\n    }\n    \n    @Override\n    public String getModule() {\n        return CONFIG_MODULE;\n    }",
        "    /** 无参构造，供序列化框架使用。 */\n    public AbstractFuzzyWatchNotifyRequest() {\n    }\n    \n    /** 返回配置模块标识。 */\n    @Override\n    public String getModule() {\n        return CONFIG_MODULE;\n    }",
    ),
]

# --- ClientConfigMetricRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ClientConfigMetricRequest.java"] = [
    (
        "/**\n * request of config module metrics.\n *\n * @author liuzunfei\n * @version $Id: ClientConfigMetricRequest.java, v 0.1 2020年12月30日 9:05 PM liuzunfei Exp $\n */",
        "/**\n * 配置模块客户端指标采集请求，由服务端发起拉取客户端缓存等指标。\n *\n * @author liuzunfei\n * @version $Id: ClientConfigMetricRequest.java, v 0.1 2020年12月30日 9:05 PM liuzunfei Exp $\n */",
    ),
    (
        "    private List<MetricsKey> metricsKeys = new ArrayList<>();\n    \n    @Override\n    public String getModule() {\n        return Constants.Config.CONFIG_MODULE;\n    }\n    \n    public List<MetricsKey> getMetricsKeys() {\n        return metricsKeys;\n    }\n    \n    public void setMetricsKeys(List<MetricsKey> metricsKeys) {\n        this.metricsKeys = metricsKeys;\n    }",
        "    /** 待采集的指标键列表。 */\n    private List<MetricsKey> metricsKeys = new ArrayList<>();\n    \n    /** 返回配置模块标识。 */\n    @Override\n    public String getModule() {\n        return Constants.Config.CONFIG_MODULE;\n    }\n    \n    /** 获取指标键列表。 */\n    public List<MetricsKey> getMetricsKeys() {\n        return metricsKeys;\n    }\n    \n    /** 设置指标键列表。 */\n    public void setMetricsKeys(List<MetricsKey> metricsKeys) {\n        this.metricsKeys = metricsKeys;\n    }",
    ),
    (
        "    public static class MetricsKey implements Serializable {\n        \n        private static final long serialVersionUID = -2731160029960311757L;\n        \n        String type;\n        \n        String key;\n        \n        public static final String CACHE_DATA = \"cacheData\";\n        \n        public static final String SNAPSHOT_DATA = \"snapshotData\";",
        "    /** 指标键，由类型与键名组成。 */\n    public static class MetricsKey implements Serializable {\n        \n        private static final long serialVersionUID = -2731160029960311757L;\n        \n        /** 指标类型。 */\n        String type;\n        \n        /** 指标键名。 */\n        String key;\n        \n        /** 客户端内存缓存数据指标。 */\n        public static final String CACHE_DATA = \"cacheData\";\n        \n        /** 本地快照数据指标。 */\n        public static final String SNAPSHOT_DATA = \"snapshotData\";",
    ),
    (
        "        /**\n         * build metrics key.\n         *\n         * @param type type.\n         * @param key  key.\n         * @return metric key.\n         */",
        "        /**\n         * 构造指标键实例。\n         *\n         * @param type 指标类型\n         * @param key  指标键名\n         * @return 指标键对象\n         */",
    ),
    (
        "        public String getType() {\n            return type;\n        }\n        \n        public void setType(String type) {\n            this.type = type;\n        }\n        \n        public String getKey() {\n            return key;\n        }\n        \n        public void setKey(String key) {\n            this.key = key;\n        }",
        "        /** 获取指标类型。 */\n        public String getType() {\n            return type;\n        }\n        \n        /** 设置指标类型。 */\n        public void setType(String type) {\n            this.type = type;\n        }\n        \n        /** 获取指标键名。 */\n        public String getKey() {\n            return key;\n        }\n        \n        /** 设置指标键名。 */\n        public void setKey(String key) {\n            this.key = key;\n        }",
    ),
]

# --- ConfigBatchListenRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ConfigBatchListenRequest.java"] = [
    (
        "/**\n * request of listening a batch of configs.\n *\n * @author liuzunfei\n * @version $Id: ConfigBatchListenRequest.java, v 0.1 2020年07月27日 7:46 PM liuzunfei Exp $\n */",
        "/**\n * 批量监听或取消监听配置的远程请求。\n *\n * <p>客户端长轮询时一次性提交多个 dataId/group/tenant 及本地 MD5。</p>\n *\n * @author liuzunfei\n * @version $Id: ConfigBatchListenRequest.java, v 0.1 2020年07月27日 7:46 PM liuzunfei Exp $\n */",
    ),
    (
        "    /**\n     * listen or remove listen.\n     */\n    private boolean listen = true;\n    \n    private List<ConfigListenContext> configListenContexts = new ArrayList<>();",
        "    /** {@code true} 表示注册监听，{@code false} 表示取消监听。 */\n    private boolean listen = true;\n    \n    /** 待监听或取消的配置上下文列表。 */\n    private List<ConfigListenContext> configListenContexts = new ArrayList<>();",
    ),
    (
        "    /**\n     * add listen config.\n     *\n     * @param group  group.\n     * @param dataId dataId.\n     * @param tenant tenant.\n     * @param md5    md5.\n     */",
        "    /**\n     * 追加一条配置监听上下文。\n     *\n     * @param group  配置分组\n     * @param dataId 配置 Data ID\n     * @param tenant 命名空间 ID\n     * @param md5    客户端当前内容 MD5\n     */",
    ),
    (
        "    /**\n     * Getter method for property <tt>configListenContexts</tt>.\n     *\n     * @return property value of configListenContexts\n     */",
        "    /**\n     * 获取配置监听上下文列表。\n     *\n     * @return 监听上下文列表\n     */",
    ),
    (
        "    /**\n     * Setter method for property <tt>configListenContexts</tt>.\n     *\n     * @param configListenContexts value to be assigned to property configListenContexts\n     */",
        "    /**\n     * 设置配置监听上下文列表。\n     *\n     * @param configListenContexts 监听上下文列表\n     */",
    ),
    (
        "    /**\n     * Getter method for property <tt>listen</tt>.\n     *\n     * @return property value of listen\n     */",
        "    /**\n     * 是否为注册监听（而非取消）。\n     *\n     * @return 注册监听返回 {@code true}\n     */",
    ),
    (
        "    /**\n     * Setter method for property <tt>listen</tt>.\n     *\n     * @param listen value to be assigned to property listen\n     */",
        "    /**\n     * 设置监听或取消标志。\n     *\n     * @param listen {@code true} 注册监听，{@code false} 取消\n     */",
    ),
    (
        "    public static class ConfigListenContext {\n        \n        String group;\n        \n        String md5;\n        \n        String dataId;\n        \n        String tenant;\n        \n        public ConfigListenContext() {\n            \n        }",
        "    /** 单条配置的监听上下文。 */\n    public static class ConfigListenContext {\n        \n        /** 配置分组。 */\n        String group;\n        \n        /** 客户端缓存的内容 MD5。 */\n        String md5;\n        \n        /** 配置 Data ID。 */\n        String dataId;\n        \n        /** 命名空间 ID。 */\n        String tenant;\n        \n        /** 无参构造。 */\n        public ConfigListenContext() {\n            \n        }",
    ),
    (
        "        /**\n         * Getter method for property <tt>group</tt>.\n         *\n         * @return property value of group\n         */",
        "        /**\n         * 获取配置分组。\n         *\n         * @return 分组名\n         */",
    ),
    (
        "        /**\n         * Setter method for property <tt>groupId</tt>.\n         *\n         * @param group value to be assigned to property groupId\n         */",
        "        /**\n         * 设置配置分组。\n         *\n         * @param group 分组名\n         */",
    ),
    (
        "        /**\n         * Getter method for property <tt>md5</tt>.\n         *\n         * @return property value of md5\n         */",
        "        /**\n         * 获取内容 MD5。\n         *\n         * @return MD5 摘要\n         */",
    ),
    (
        "        /**\n         * Setter method for property <tt>md5</tt>.\n         *\n         * @param md5 value to be assigned to property md5\n         */",
        "        /**\n         * 设置内容 MD5。\n         *\n         * @param md5 MD5 摘要\n         */",
    ),
    (
        "        /**\n         * Getter method for property <tt>dataId</tt>.\n         *\n         * @return property value of dataId\n         */",
        "        /**\n         * 获取 Data ID。\n         *\n         * @return Data ID\n         */",
    ),
    (
        "        /**\n         * Setter method for property <tt>dataId</tt>.\n         *\n         * @param dataId value to be assigned to property dataId\n         */",
        "        /**\n         * 设置 Data ID。\n         *\n         * @param dataId Data ID\n         */",
    ),
    (
        "        /**\n         * Getter method for property <tt>tenant</tt>.\n         *\n         * @return property value of tenant\n         */",
        "        /**\n         * 获取命名空间 ID。\n         *\n         * @return 租户 ID\n         */",
    ),
    (
        "        /**\n         * Setter method for property <tt>tenant</tt>.\n         *\n         * @param tenant value to be assigned to property tenant\n         */",
        "        /**\n         * 设置命名空间 ID。\n         *\n         * @param tenant 租户 ID\n         */",
    ),
]

# --- ConfigChangeNotifyRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ConfigChangeNotifyRequest.java"] = [
    (
        "/**\n * ConfigChangeNotifyRequest.\n *\n * @author liuzunfei\n * @version $Id: ConfigChangeNotifyRequest.java, v 0.1 2020年07月14日 3:20 PM liuzunfei Exp $\n */",
        "/**\n * 配置变更通知请求，由服务端主动推送给客户端。\n *\n * <p>告知指定 dataId/group/tenant 的配置已发生变更，客户端应拉取最新内容。</p>\n *\n * @author liuzunfei\n * @version $Id: ConfigChangeNotifyRequest.java, v 0.1 2020年07月14日 3:20 PM liuzunfei Exp $\n */",
    ),
    (
        "    String dataId;\n    \n    String group;\n    \n    String tenant;",
        "    /** 发生变更的配置 Data ID。 */\n    String dataId;\n    \n    /** 配置分组。 */\n    String group;\n    \n    /** 命名空间 ID。 */\n    String tenant;",
    ),
    (
        "    public String getDataId() {\n        return dataId;\n    }\n    \n    public void setDataId(String dataId) {\n        this.dataId = dataId;\n    }\n    \n    public String getGroup() {\n        return group;\n    }\n    \n    public void setGroup(String group) {\n        this.group = group;\n    }\n    \n    public String getTenant() {\n        return tenant;\n    }\n    \n    public void setTenant(String tenant) {\n        this.tenant = tenant;\n    }",
        "    /** 获取 Data ID。 */\n    public String getDataId() {\n        return dataId;\n    }\n    \n    /** 设置 Data ID。 */\n    public void setDataId(String dataId) {\n        this.dataId = dataId;\n    }\n    \n    /** 获取配置分组。 */\n    public String getGroup() {\n        return group;\n    }\n    \n    /** 设置配置分组。 */\n    public void setGroup(String group) {\n        this.group = group;\n    }\n    \n    /** 获取命名空间 ID。 */\n    public String getTenant() {\n        return tenant;\n    }\n    \n    /** 设置命名空间 ID。 */\n    public void setTenant(String tenant) {\n        this.tenant = tenant;\n    }",
    ),
    (
        "    /**\n     * build success response.\n     *\n     * @param dataId dataId\n     * @param group  group\n     * @param tenant tenant\n     * @return ConfigChangeNotifyResponse\n     */",
        "    /**\n     * 构造配置变更通知请求。\n     *\n     * @param dataId 配置 Data ID\n     * @param group  配置分组\n     * @param tenant 命名空间 ID\n     * @return 填充完毕的通知请求\n     */",
    ),
    (
        "    @Override\n    public String getModule() {\n        return Constants.Config.CONFIG_MODULE;\n    }",
        "    /** 返回配置模块标识。 */\n    @Override\n    public String getModule() {\n        return Constants.Config.CONFIG_MODULE;\n    }",
    ),
]

# --- ConfigFuzzyWatchChangeNotifyRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ConfigFuzzyWatchChangeNotifyRequest.java"] = [
    (
        "/**\n * Represents a request to notify changes when a fuzzy watched configuration changed.\n *\n * <p>This request is used to notify clients about changes in configurations that match fuzzy listening patterns.\n *\n * @author stone-98\n * @date 2024/3/13\n */",
        "/**\n * 模糊监听配置变更通知请求。\n *\n * <p>当匹配模糊订阅模式的配置发生变更时，服务端向客户端推送本请求。</p>\n *\n * @author stone-98\n * @date 2024/3/13\n */",
    ),
    (
        "    /**\n     * The groupKey of the configuration that has changed.\n     */\n    private String groupKey;\n    \n    /**\n     * Indicates whether the configuration exists or not.\n     */\n    private String changeType;",
        "    /** 发生变更的配置 groupKey（tenant@@group@@dataId）。 */\n    private String groupKey;\n    \n    /** 变更类型，如新增或删除。 */\n    private String changeType;",
    ),
    (
        "    /**\n     * Constructs an empty FuzzyListenNotifyChangeRequest.\n     */",
        "    /** 无参构造，供序列化使用。 */",
    ),
    (
        "    /**\n     * Constructs a FuzzyListenNotifyChangeRequest with the specified parameters.\n     *\n     * @param groupKey   The group of the configuration that has changed\n     * @param changeType Indicates whether the configuration exists or not\n     */",
        "    /**\n     * 构造模糊监听变更通知。\n     *\n     * @param groupKey   变更配置的 groupKey\n     * @param changeType 变更类型\n     */",
    ),
    (
        "    public String getGroupKey() {\n        return groupKey;\n    }\n    \n    public void setGroupKey(String groupKey) {\n        this.groupKey = groupKey;\n    }\n    \n    public String getChangeType() {\n        return changeType;\n    }\n    \n    public void setChangeType(String changeType) {\n        this.changeType = changeType;\n    }",
        "    /** 获取变更配置的 groupKey。 */\n    public String getGroupKey() {\n        return groupKey;\n    }\n    \n    /** 设置变更配置的 groupKey。 */\n    public void setGroupKey(String groupKey) {\n        this.groupKey = groupKey;\n    }\n    \n    /** 获取变更类型。 */\n    public String getChangeType() {\n        return changeType;\n    }\n    \n    /** 设置变更类型。 */\n    public void setChangeType(String changeType) {\n        this.changeType = changeType;\n    }",
    ),
    (
        "    /**\n     * Returns a string representation of the FuzzyListenNotifyChangeRequest.\n     *\n     * @return A string representation of the request\n     */",
        "    /**\n     * 返回请求的字符串表示。\n     *\n     * @return 调试字符串\n     */",
    ),
]

# --- ConfigFuzzyWatchRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ConfigFuzzyWatchRequest.java"] = [
    (
        "/**\n * Represents a request for batch fuzzy listening configurations.\n *\n * <p>This request is used to request batch fuzzy listening configurations from the server. It contains a set of\n * contexts, each representing a fuzzy listening context.\n *\n * @author stone-98\n * @date 2024/3/4\n */",
        "/**\n * 批量模糊监听配置的客户端请求。\n *\n * <p>携带 groupKey 模式、已接收键集合及监听类型，向服务端注册或同步模糊订阅。</p>\n *\n * @author stone-98\n * @date 2024/3/4\n */",
    ),
    (
        "    /**\n     * The namespace or tenant associated with the configurations.\n     */\n    private String groupKeyPattern;\n    \n    private Set<String> receivedGroupKeys;\n    \n    /**\n     * Flag indicating whether to listen for changes.\n     */\n    private String watchType;\n    \n    /**\n     * Flag indicating whether the client is initializing.\n     */\n    private boolean isInitializing;",
        "    /** 模糊匹配的 groupKey 模式。 */\n    private String groupKeyPattern;\n    \n    /** 客户端已知的 groupKey 集合，用于差异同步。 */\n    private Set<String> receivedGroupKeys;\n    \n    /** 监听类型（注册、取消等）。 */\n    private String watchType;\n    \n    /** 客户端是否处于模糊监听初始化阶段。 */\n    private boolean isInitializing;",
    ),
    (
        "    /**\n     * Constructs an empty ConfigBatchFuzzyListenRequest.\n     */",
        "    /** 无参构造。 */",
    ),
    (
        "    public String getGroupKeyPattern() {\n        return groupKeyPattern;\n    }\n    \n    public void setGroupKeyPattern(String groupKeyPattern) {\n        this.groupKeyPattern = groupKeyPattern;\n    }\n    \n    public Set<String> getReceivedGroupKeys() {\n        return receivedGroupKeys;\n    }\n    \n    public void setReceivedGroupKeys(Set<String> receivedGroupKeys) {\n        this.receivedGroupKeys = receivedGroupKeys;\n    }\n    \n    public String getWatchType() {\n        return watchType;\n    }\n    \n    public void setWatchType(String watchType) {\n        this.watchType = watchType;\n    }\n    \n    public boolean isInitializing() {\n        return isInitializing;\n    }\n    \n    public void setInitializing(boolean initializing) {\n        isInitializing = initializing;\n    }",
        "    /** 获取 groupKey 匹配模式。 */\n    public String getGroupKeyPattern() {\n        return groupKeyPattern;\n    }\n    \n    /** 设置 groupKey 匹配模式。 */\n    public void setGroupKeyPattern(String groupKeyPattern) {\n        this.groupKeyPattern = groupKeyPattern;\n    }\n    \n    /** 获取已接收的 groupKey 集合。 */\n    public Set<String> getReceivedGroupKeys() {\n        return receivedGroupKeys;\n    }\n    \n    /** 设置已接收的 groupKey 集合。 */\n    public void setReceivedGroupKeys(Set<String> receivedGroupKeys) {\n        this.receivedGroupKeys = receivedGroupKeys;\n    }\n    \n    /** 获取监听类型。 */\n    public String getWatchType() {\n        return watchType;\n    }\n    \n    /** 设置监听类型。 */\n    public void setWatchType(String watchType) {\n        this.watchType = watchType;\n    }\n    \n    /** 是否处于初始化阶段。 */\n    public boolean isInitializing() {\n        return isInitializing;\n    }\n    \n    /** 设置初始化标志。 */\n    public void setInitializing(boolean initializing) {\n        isInitializing = initializing;\n    }",
    ),
    (
        "    /**\n     * Get the module name for this request.\n     *\n     * @return The module name\n     */",
        "    /**\n     * 返回所属模块名。\n     *\n     * @return 配置模块标识\n     */",
    ),
]

# --- ConfigFuzzyWatchSyncRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ConfigFuzzyWatchSyncRequest.java"] = [
    (
        "/**\n * Represents a request to notify the difference between client and server side.\n *\n * <p>This request is used to notify clients about the difference in configurations that match fuzzy listening\n * patterns.\n *\n * @author stone-98\n * @date 2024/3/6\n */",
        "/**\n * 模糊监听差异同步请求，服务端向客户端推送与本地不一致的配置集合。\n *\n * <p>用于模糊订阅初始化及增量对齐，支持分批传输。</p>\n *\n * @author stone-98\n * @date 2024/3/6\n */",
    ),
    (
        "    /**\n     * The pattern used to match group keys for the configurations.\n     */\n    private String groupKeyPattern;\n    \n    /**\n     * The set of contexts containing information about the configurations.\n     */\n    private Set<Context> contexts;\n    \n    /**\n     * see FUZZY_WATCH_INIT_NOTIFY,FINISH_FUZZY_WATCH_INIT_NOTIFY,FUZZY_WATCH_DIFF_SYNC_NOTIFY.\n     */\n    private String syncType;\n    \n    private int totalBatch;\n    \n    private int currentBatch;",
        "    /** 模糊匹配的 groupKey 模式。 */\n    private String groupKeyPattern;\n    \n    /** 差异配置上下文集合。 */\n    private Set<Context> contexts;\n    \n    /** 同步类型，参见 FUZZY_WATCH_INIT_NOTIFY 等常量。 */\n    private String syncType;\n    \n    /** 分批同步的总批次数。 */\n    private int totalBatch;\n    \n    /** 当前批次序号（从 1 起）。 */\n    private int currentBatch;",
    ),
    (
        "    public String getSyncType() {\n        return syncType;\n    }\n    \n    public void setSyncType(String syncType) {\n        this.syncType = syncType;\n    }\n    \n    public int getTotalBatch() {\n        return totalBatch;\n    }\n    \n    public void setTotalBatch(int totalBatch) {\n        this.totalBatch = totalBatch;\n    }\n    \n    public int getCurrentBatch() {\n        return currentBatch;\n    }\n    \n    public void setCurrentBatch(int currentBatch) {\n        this.currentBatch = currentBatch;\n    }",
        "    /** 获取同步类型。 */\n    public String getSyncType() {\n        return syncType;\n    }\n    \n    /** 设置同步类型。 */\n    public void setSyncType(String syncType) {\n        this.syncType = syncType;\n    }\n    \n    /** 获取总批次数。 */\n    public int getTotalBatch() {\n        return totalBatch;\n    }\n    \n    /** 设置总批次数。 */\n    public void setTotalBatch(int totalBatch) {\n        this.totalBatch = totalBatch;\n    }\n    \n    /** 获取当前批次。 */\n    public int getCurrentBatch() {\n        return currentBatch;\n    }\n    \n    /** 设置当前批次。 */\n    public void setCurrentBatch(int currentBatch) {\n        this.currentBatch = currentBatch;\n    }",
    ),
    (
        "    /**\n     * Constructs an empty FuzzyListenNotifyDiffRequest.\n     */",
        "    /** 无参构造。 */",
    ),
    (
        "    /**\n     * Constructs a FuzzyListenNotifyDiffRequest with the specified parameters.\n     *\n     * @param groupKeyPattern The pattern used to match group keys for the configurations\n     * @param contexts        The set of contexts containing information about the configurations\n     */",
        "    /**\n     * 私有构造，通过静态工厂方法创建实例。\n     *\n     * @param groupKeyPattern groupKey 匹配模式\n     * @param contexts        差异配置上下文集合\n     */",
    ),
    (
        "    /**\n     * Builds an initial FuzzyListenNotifyDiffRequest with the specified set of contexts and group key pattern.\n     *\n     * @param contexts        The set of contexts containing information about the configurations\n     * @param groupKeyPattern The pattern used to match group keys for the configurations\n     * @return An initial FuzzyListenNotifyDiffRequest\n     */",
        "    /**\n     * 构建分批差异同步请求。\n     *\n     * @param contexts        差异配置上下文\n     * @param groupKeyPattern groupKey 匹配模式\n     * @return 同步请求实例\n     */",
    ),
    (
        "    /**\n     * Builds fuzzy watch init finish request.\n     *\n     * @param groupKeyPattern The pattern used to match group keys for the configurations\n     * @return A final FuzzyListenNotifyDiffRequest\n     */",
        "    /**\n     * 构建模糊监听初始化完成通知。\n     *\n     * @param groupKeyPattern groupKey 匹配模式\n     * @return 初始化完成同步请求\n     */",
    ),
    (
        "    public String getGroupKeyPattern() {\n        return groupKeyPattern;\n    }\n    \n    public void setGroupKeyPattern(String groupKeyPattern) {\n        this.groupKeyPattern = groupKeyPattern;\n    }\n    \n    public Set<Context> getContexts() {\n        return contexts;\n    }\n    \n    public void setContexts(Set<Context> contexts) {\n        this.contexts = contexts;\n    }",
        "    /** 获取 groupKey 匹配模式。 */\n    public String getGroupKeyPattern() {\n        return groupKeyPattern;\n    }\n    \n    /** 设置 groupKey 匹配模式。 */\n    public void setGroupKeyPattern(String groupKeyPattern) {\n        this.groupKeyPattern = groupKeyPattern;\n    }\n    \n    /** 获取差异上下文集合。 */\n    public Set<Context> getContexts() {\n        return contexts;\n    }\n    \n    /** 设置差异上下文集合。 */\n    public void setContexts(Set<Context> contexts) {\n        this.contexts = contexts;\n    }",
    ),
    (
        "    /**\n     * Represents context information about a configuration.\n     */\n    public static class Context {\n        \n        String groupKey;\n        \n        /**\n         * see {@link com.alibaba.nacos.api.common.Constants.ConfigChangedType ADD_CONFIG&} ADD_CONFIG: a new config\n         * should be added for  clientside . DELETE_CONFIG: a  config should be removed for  clientside .\n         */\n        private String changedType;",
        "    /** 单条模糊监听差异的配置上下文。 */\n    public static class Context {\n        \n        /** 配置的 groupKey。 */\n        String groupKey;\n        \n        /** 变更类型，参见 {@link com.alibaba.nacos.api.common.Constants.ConfigChangedType}：ADD_CONFIG 表示客户端应新增，DELETE_CONFIG 表示应移除。 */\n        private String changedType;",
    ),
    (
        "        /**\n         * Constructs an empty Context object.\n         */",
        "        /** 无参构造。 */",
    ),
    (
        "        /**\n         * Builds a new context object with the provided parameters.\n         *\n         * @param groupKey    The groupKey associated of the configuration.\n         * @param changedType The type of the configuration change event.\n         * @return A new context object initialized with the provided parameters.\n         */",
        "        /**\n         * 构造差异上下文。\n         *\n         * @param groupKey    配置 groupKey\n         * @param changedType 变更类型\n         * @return 上下文实例\n         */",
    ),
    (
        "        public String getGroupKey() {\n            return groupKey;\n        }\n        \n        public void setGroupKey(String groupKey) {\n            this.groupKey = groupKey;\n        }\n        \n        public String getChangedType() {\n            return changedType;\n        }\n        \n        public void setChangedType(String changedType) {\n            this.changedType = changedType;\n        }",
        "        /** 获取 groupKey。 */\n        public String getGroupKey() {\n            return groupKey;\n        }\n        \n        /** 设置 groupKey。 */\n        public void setGroupKey(String groupKey) {\n            this.groupKey = groupKey;\n        }\n        \n        /** 获取变更类型。 */\n        public String getChangedType() {\n            return changedType;\n        }\n        \n        /** 设置变更类型。 */\n        public void setChangedType(String changedType) {\n            this.changedType = changedType;\n        }",
    ),
]

# --- ConfigPublishRequest ---

R["api/src/main/java/com/alibaba/nacos/api/config/remote/request/ConfigPublishRequest.java"] = [
    (
        "/**\n * request to publish a config.\n *\n * @author liuzunfei\n * @version $Id: ConfigPublishRequest.java, v 0.1 2020年07月16日 4:30 PM liuzunfei Exp $\n */",
        "/**\n * 发布配置的远程请求。\n *\n * <p>携带配置内容、CAS MD5 及可选扩展参数，由客户端发往服务端。</p>\n *\n * @author liuzunfei\n * @version $Id: ConfigPublishRequest.java, v 0.1 2020年07月16日 4:30 PM liuzunfei Exp $\n */",
    ),
    (
        "    String content;\n    \n    String casMd5;\n    \n    private Map<String, String> additionMap;\n    \n    public ConfigPublishRequest() {\n        \n    }\n    \n    public ConfigPublishRequest(String dataId, String group, String tenant, String content) {\n        this.content = content;\n        super.setGroup(group);\n        super.setTenant(tenant);\n        super.setDataId(dataId);\n    }",
        "    /** 待发布的配置内容。 */\n    String content;\n    \n    /** CAS 发布时期望的当前内容 MD5。 */\n    String casMd5;\n    \n    /** 附加参数字典（如配置类型、加密密钥等）。 */\n    private Map<String, String> additionMap;\n    \n    /** 无参构造。 */\n    public ConfigPublishRequest() {\n        \n    }\n    \n    /**\n     * 构造发布请求。\n     *\n     * @param dataId  配置 Data ID\n     * @param group   配置分组\n     * @param tenant  命名空间 ID\n     * @param content 配置内容\n     */\n    public ConfigPublishRequest(String dataId, String group, String tenant, String content) {\n        this.content = content;\n        super.setGroup(group);\n        super.setTenant(tenant);\n        super.setDataId(dataId);\n    }",
    ),
    (
        "    /**\n     * get additional param.\n     *\n     * @param key key of param.\n     * @return value of param ,return null if not exist.\n     */",
        "    /**\n     * 获取附加参数值。\n     *\n     * @param key 参数键\n     * @return 参数值，不存在时返回 {@code null}\n     */",
    ),
    (
        "    /**\n     * put additional param value. will override if exist.\n     *\n     * @param key   key of param.\n     * @param value value of param.\n     */",
        "    /**\n     * 写入附加参数，已存在则覆盖。\n     *\n     * @param key   参数键\n     * @param value 参数值\n     */",
    ),
    (
        "    /**\n     * Getter method for property <tt>content</tt>.\n     *\n     * @return property value of content\n     */",
        "    /**\n     * 获取配置内容。\n     *\n     * @return 配置正文\n     */",
    ),
    (
        "    /**\n     * Setter method for property <tt>content</tt>.\n     *\n     * @param content value to be assigned to property content\n     */",
        "    /**\n     * 设置配置内容。\n     *\n     * @param content 配置正文\n     */",
    ),
    (
        "    /**\n     * Getter method for property <tt>casMd5</tt>.\n     *\n     * @return property value of casMd5\n     */",
        "    /**\n     * 获取 CAS MD5。\n     *\n     * @return 期望的当前内容 MD5\n     */",
    ),
    (
        "    /**\n     * Setter method for property <tt>casMd5</tt>.\n     *\n     * @param casMd5 value to be assigned to property content\n     */",
        "    /**\n     * 设置 CAS MD5。\n     *\n     * @param casMd5 期望的当前内容 MD5\n     */",
    ),
    (
        "    /**\n     * Getter method for property <tt>casMd5</tt>.\n     *\n     * @return property value of casMd5\n     */\n    public Map<String, String> getAdditionMap() {",
        "    /**\n     * 获取附加参数字典。\n     *\n     * @return 附加参数映射\n     */\n    public Map<String, String> getAdditionMap() {",
    ),
    (
        "    /**\n     * Setter method for property <tt>additionMap</tt>.\n     *\n     * @param additionMap value to be assigned to property additionMap\n     */",
        "    /**\n     * 设置附加参数字典。\n     *\n     * @param additionMap 附加参数映射\n     */",
    ),
]
