"""Chinese annotation replacements for Nacos 3.2.3 wave39b [15:30] config service core."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/remote/ConfigRemoveRequestHandler.java": [
        (
            "/**\n * handler to remove config.\n *\n * @author liuzunfei\n * @version $Id: ConfiRemoveRequestHandler.java, v 0.1 2020年07月16日 5:49 PM liuzunfei Exp $\n */",
            "/**\n * 配置删除 RPC 请求处理器：校验 namespace/dataId/group/tag 后调用 {@link ConfigOperationService#deleteConfig}。\n"
            " * 集成 TPS 限流、鉴权、参数提取与 namespace 校验。\n"
            " * handler to remove config.\n *\n * @author liuzunfei\n * @version $Id: ConfiRemoveRequestHandler.java, v 0.1 2020年07月16日 5:49 PM liuzunfei Exp $\n */",
        ),
        (
            "    private final ConfigInfoPersistService configInfoPersistService;",
            "    /** 正式配置持久化服务（构造注入，供扩展使用） */\n    private final ConfigInfoPersistService configInfoPersistService;",
        ),
        (
            "    private final ConfigInfoGrayPersistService configInfoGrayPersistService;",
            "    /** 灰度配置持久化服务 */\n    private final ConfigInfoGrayPersistService configInfoGrayPersistService;",
        ),
        (
            "    private final ConfigOperationService configOperationService;",
            "    /** 配置发布/删除统一操作入口 */\n    private final ConfigOperationService configOperationService;",
        ),
        (
            "        // check tenant",
            "        // 规范化并校验 tenant（namespace）",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/remote/FuzzyWatchChangeNotifyTask.java": [
        (
            "/**\n * Represents a task for pushing notification to remote clients.\n */",
            "/**\n * 模糊监听配置变更推送任务：向指定 RPC 连接异步推送 {@link ConfigFuzzyWatchChangeNotifyRequest}。\n"
            " * 支持 TPS 限流、超时回调与指数退避重试；超过最大重试次数则注销连接。\n"
            " * Represents a task for pushing notification to remote clients.\n */",
        ),
        (
            "    ConfigFuzzyWatchChangeNotifyRequest notifyRequest;",
            "    /** 待推送的模糊监听变更通知请求体 */\n    ConfigFuzzyWatchChangeNotifyRequest notifyRequest;",
        ),
        (
            "    int tryTimes = 0;",
            "    /** 当前已尝试推送次数（用于退避调度） */\n    int tryTimes = 0;",
        ),
        (
            "     * Constructs a RpcPushTask with the specified parameters.",
            "     * 构造模糊监听变更推送任务。\n     *\n     * Constructs a RpcPushTask with the specified parameters.",
        ),
        (
            "            // Client is already offline, ignore the task.",
            "            // 客户端已离线，忽略本次推送任务",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/remote/FuzzyWatchSyncNotifyCallback.java": [
        (
            "/**\n * Represents a callback for handling the result of an RPC push operation.\n *\n * @author stone-98\n */",
            "/**\n * 模糊监听同步推送结果回调：处理批次计数、初始化完成通知与失败重试。\n"
            " * 成功时若整批同步完成且为 INIT 类型，会追加推送 init-finish 请求。\n"
            " * Represents a callback for handling the result of an RPC push operation.\n *\n * @author stone-98\n */",
        ),
        (
            "     * The RpcPushTask associated with the callback.",
            "     * 关联的模糊监听同步推送任务实例。",
        ),
        (
            "        // Check TPS limits",
            "        // 记录 TPS 成功/失败计量点",
        ),
        (
            "                // Create RPC push task and push the request to the client",
            "                // 构造 finish 推送任务并调度到客户端",
        ),
        (
            "        // Log the failure and retry the task",
            "        // 记录失败日志并重新调度推送任务",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/remote/FuzzyWatchSyncNotifyTask.java": [
        (
            "/**\n * Represents a task for pushing FuzzyListenNotifyDiffRequest to clients.\n *\n * @author stone-98\n */",
            "/**\n * 模糊监听同步推送任务：向客户端分批推送 {@link ConfigFuzzyWatchSyncRequest}。\n"
            " * 配合 {@link FuzzyWatchSyncNotifyCallback} 完成批次计数与 init-finish 收尾。\n"
            " * Represents a task for pushing FuzzyListenNotifyDiffRequest to clients.\n *\n * @author stone-98\n */",
        ),
        (
            "     * The FuzzyListenNotifyDiffRequest to be pushed.",
            "     * 待推送的模糊监听同步请求。",
        ),
        (
            "     * The maximum number of times to retry pushing the request.",
            "     * 推送失败时的最大重试次数。",
        ),
        (
            "     * The current number of attempts made to push the request.",
            "     * 当前已执行的推送尝试次数。",
        ),
        (
            "     * The ID of the connection associated with the client.",
            "     * 目标客户端 RPC 连接 ID。",
        ),
        (
            "            // If over the maximum retry times, log a warning and unregister the client connection",
            "            // 超过最大重试次数：记录告警并注销客户端连接",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/remote/RpcConfigChangeNotifier.java": [
        (
            "/**\n * ConfigChangeNotifier.\n *\n * @author liuzunfei\n * @version $Id: ConfigChangeNotifier.java, v 0.1 2020年07月20日 3:00 PM liuzunfei Exp $\n */",
            "/**\n * 基于 RPC 的配置变更推送器：订阅 {@link LocalDataChangeEvent}，向监听该 groupKey 的客户端推送变更通知。\n"
            " * 内含 {@link RpcPushTask} 与 {@link RpcPushCallback}，支持 TPS 限流与退避重试。\n"
            " * ConfigChangeNotifier.\n *\n * @author liuzunfei\n * @version $Id: ConfigChangeNotifier.java, v 0.1 2020年07月20日 3:00 PM liuzunfei Exp $\n */",
        ),
        (
            "     * adaptor to config module ,when server side config change ,invoke this method.",
            "     * 配置模块适配入口：服务端配置变更时，向所有监听该 groupKey 的客户端发起 RPC 推送。\n     *\n     * adaptor to config module ,when server side config change ,invoke this method.",
        ),
        (
            "            // first time:delay 0s; second time:delay 2s; third time:delay 4s",
            "            // 退避策略：第 n 次重试延迟 n*2 秒",
        ),
        (
            "            // client is already offline, ignore task.",
            "            // 客户端已离线，忽略剩余重试",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/result/code/ResultCodeEnum.java": [
        (
            "/**\n * ResultCodeEnum.\n *\n * @author klw\n * @ClassName: ResultCodeEnum\n * @Description: result code enum\n * @date 2019/6/28 14:43\n */",
            "/**\n * 配置模块历史结果码枚举（已废弃，建议使用 {@link com.alibaba.nacos.api.model.v2.ErrorCode}）。\n"
            " * 保留通用成功/错误码及配置导入相关错误码，消息字段已为中文。\n"
            " * ResultCodeEnum.\n *\n * @author klw\n * @ClassName: ResultCodeEnum\n * @Description: result code enum\n * @date 2019/6/28 14:43\n */",
        ),
        (
            "    /**\n     * Common code.\n     **/",
            "    /** 通用结果码（200 成功 / 500 服务器错误） */",
        ),
        (
            "    /**\n     * Config use 100001 ~ 100999.\n     **/",
            "    /** 配置业务专用码段：100001 ~ 100999 */",
        ),
        (
            "    private int code;",
            "    /** HTTP 风格业务码 */\n    private int code;",
        ),
        (
            "    private String msg;",
            "    /** 面向用户的中文/英文提示信息 */\n    private String msg;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ClientIpWhiteList.java": [
        (
            "/**\n * Client ip whitelist.\n *\n * @author Nacos\n */",
            "/**\n * 客户端 IP 白名单服务：从元数据加载 ACL 配置，控制是否启用及合法 IP 列表。\n"
            " * 用于限制仅白名单内客户端可执行特定配置操作。\n"
            " * Client ip whitelist.\n *\n * @author Nacos\n */",
        ),
        (
            "     * Judge whether specified client ip includes in the whitelist.",
            "     * 判断指定客户端 IP 是否在白名单中。\n     *\n     * Judge whether specified client ip includes in the whitelist.",
        ),
        (
            "     * Whether start client ip whitelist.",
            "     * 白名单功能是否已启用。\n     *\n     * Whether start client ip whitelist.",
        ),
        (
            "     * Load white lists based content parameter value.",
            "     * 从 JSON 内容加载白名单配置（开关与 IP 列表）。\n     *\n     * Load white lists based content parameter value.",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ClientRecord.java": [
        (
            "/**\n * ClientRecord saves records which fetch from client-side.\n *\n * @author zongtanghu\n */",
            "/**\n * 单客户端订阅与轮询快照：记录 IP、最近活跃时间、各 groupKey 的 MD5 与轮询时间戳。\n"
            " * 由 {@link ClientTrackService} 维护生命周期。\n"
            " * ClientRecord saves records which fetch from client-side.\n *\n * @author zongtanghu\n */",
        ),
        (
            "    private final String ip;",
            "    /** 客户端 IP 地址（构造后不可变） */\n    private final String ip;",
        ),
        (
            "    private volatile long lastTime;",
            "    /** 最近一次 track 更新的毫秒时间戳 */\n    private volatile long lastTime;",
        ),
        (
            "    private final ConcurrentMap<String, String> groupKey2md5Map;",
            "    /** groupKey → 客户端上报的配置 MD5 */\n    private final ConcurrentMap<String, String> groupKey2md5Map;",
        ),
        (
            "    private final ConcurrentMap<String, Long> groupKey2pollingTsMap;",
            "    /** groupKey → 最近一次长轮询/监听时间戳 */\n    private final ConcurrentMap<String, Long> groupKey2pollingTsMap;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ClientTrackService.java": [
        (
            "/**\n * ClientTrackService which tracks client's md5 service and delete expired ip's records.\n *\n * @author Nacos\n */",
            "/**\n * 客户端 MD5 跟踪服务：记录各 IP 订阅的 groupKey 与 MD5，供控制台查询订阅状态与是否最新。\n"
            " * 与 {@link ConfigCacheService#isUptodate} 配合判断客户端配置是否落后。\n"
            " * ClientTrackService which tracks client's md5 service and delete expired ip's records.\n *\n * @author Nacos\n */",
        ),
        (
            "     * Put the specified value(ip/groupKey/clientMd5) into clientRecords Map.",
            "     * 记录客户端对某 groupKey 上报的 MD5，并刷新活跃时间与轮询时间戳。\n     *\n     * Put the specified value(ip/groupKey/clientMd5) into clientRecords Map.",
        ),
        (
            "     * Groupkey ->  SubscriberStatus.",
            "     * 查询指定 IP 下各 groupKey 的订阅状态（是否最新、MD5、最后轮询时间）。\n     *\n     * Groupkey ->  SubscriberStatus.",
        ),
        (
            "        // record here is non-null",
            "        // getClientRecord 保证返回非空记录",
        ),
        (
            "     * Specify subscriber's ip and look up whether data is latest.\n     * groupKey -> isUptodate.",
            "     * 查询指定订阅者 IP 下各 groupKey 配置是否与服务器缓存一致。\n     * groupKey -> isUptodate.",
        ),
        (
            "     * All of client records, adding or deleting.",
            "     * 全局客户端记录表（IP → {@link ClientRecord}），支持并发读写与整表刷新。",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigCacheService.java": [
        (
            "/**\n * Config service.\n *\n * @author Nacos\n */",
            "/**\n * 配置本地缓存核心服务：维护 groupKey → {@link CacheItem} 的 JVM 缓存，并同步磁盘 dump。\n"
            " * 提供正式/灰度配置的 dump、删除、MD5 查询、读写锁与客户端版本比对（isUptodate）。\n"
            " * 变更后通过 {@link LocalDataChangeEvent} 触发 RPC 推送。\n"
            " * Config service.\n *\n * @author Nacos\n */",
        ),
        (
            "     * groupKey -> cacheItem.",
            "     * 全局配置缓存：groupKey（dataId+group+tenant）→ 缓存项。",
        ),
        (
            "     * Save config file and update md5 value in cache.",
            "     * 将配置内容写入磁盘并更新本地缓存 MD5（可携带已知 MD5 跳过重复计算）。\n     *\n     * Save config file and update md5 value in cache.",
        ),
        (
            "            //check timestamp",
            "            // 校验持久化时间戳，忽略过期 dump",
        ),
        (
            "            //check md5 & update local disk cache.",
            "            // MD5 变化时落盘并更新磁盘缓存",
        ),
        (
            "            //check  md5 and timestamp & update local jvm cache.",
            "            // 同步更新 JVM 缓存中的 MD5 与时间戳",
        ),
        (
            "                    // Protect from disk full.",
            "                    // 磁盘满等 IO 异常时记录致命日志",
        ),
        (
            "     * Save gray config file and update md5 value in cache.",
            "     * 灰度配置 dump：比较 MD5、灰度规则与时间戳后更新缓存与磁盘。\n     *\n     * Save gray config file and update md5 value in cache.",
        ),
        (
            "     * Delete gray config file, and delete cache.",
            "     * 删除灰度配置：清理磁盘、JVM 灰度缓存并发布本地变更事件。\n     *\n     * Delete gray config file, and delete cache.",
        ),
        (
            "     * Delete config file, and delete cache.",
            "     * 删除正式配置：清理磁盘与 CACHE 条目并通知监听方。\n     *\n     * Delete config file, and delete cache.",
        ),
        (
            "        // If data is non-existent.",
            "        // 缓存中不存在该 groupKey，视为删除成功",
        ),
        (
            "        // try to lock failed",
            "        // 写锁获取失败",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigChangePublisher.java": [
        (
            "/**\n * ConfigChangePublisher.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
            "/**\n * 配置变更事件发布器：在集群非嵌入式存储模式下，通过 {@link NotifyCenter} 广播 {@link ConfigDataChangeEvent}。\n"
            " * 嵌入式集群模式下单节点不重复发布，避免多副本重复通知。\n"
            " * ConfigChangePublisher.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        ),
        (
            "     * Notify ConfigChange.",
            "     * 发布配置数据变更事件（嵌入式集群且非 standalone 时直接返回）。\n     *\n     * Notify ConfigChange.",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigDetailService.java": [
        (
            "/**\n * config detail service.\n *\n * @author 985492783@qq.com\n * @date 2023/2/9 5:25\n */",
            "/**\n * 配置分页检索服务：通过有界队列 + 工作线程池异步执行 DB 查询，调用方阻塞等待结果。\n"
            " * 支持精确/模糊搜索，队列满或超时时抛出 503 限流异常。\n"
            " * config detail service.\n *\n * @author 985492783@qq.com\n * @date 2023/2/9 5:25\n */",
        ),
        (
            "     * the max_capacity of eventLinkedBlockingQueue may be controlled by the properties {@link PropertiesConstant#SEARCH_MAX_CAPACITY}.",
            "     * 搜索任务队列最大容量，可通过 {@link PropertiesConstant#SEARCH_MAX_CAPACITY} 配置。",
        ),
        (
            "     * the wait_timeout of search config business may be controlled by the properties {@link PropertiesConstant#SEARCH_WAIT_TIMEOUT}.",
            "     * 调用方等待搜索结果的超时毫秒数，可通过 {@link PropertiesConstant#SEARCH_WAIT_TIMEOUT} 配置。",
        ),
        (
            "     * the max_thread of clientEventExecutor may be controlled by the properties {@link PropertiesConstant#SEARCH_MAX_THREAD}.",
            "     * 搜索工作线程数上限，可通过 {@link PropertiesConstant#SEARCH_MAX_THREAD} 配置。",
        ),
        (
            "     * init worker thread.",
            "     * 初始化有界队列与固定数量搜索工作线程（长期循环 take 任务）。",
        ),
        (
            "     * block thread and use workerThread to search config.",
            "     * 阻塞当前线程，将搜索任务入队并由 worker 执行 DB 分页查询后唤醒返回。",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigFuzzyWatchContextService.java": [
        (
            "/**\n * fuzzy watch context for config.\n *\n * @author shiyiyue\n */",
            "/**\n * 配置模糊监听上下文：维护 groupKeyPattern → 订阅客户端集合，以及 pattern → 已匹配 groupKey 集合。\n"
            " * 配置增删时同步更新匹配集合并决定是否推送；定期 trim 清理无效上下文。\n"
            " * fuzzy watch context for config.\n *\n * @author shiyiyue\n */",
        ),
        (
            "     * groupKeyPattern -> watched client id set.",
            "     * 模糊 pattern → 正在监听的 RPC 连接 ID 集合。",
        ),
        (
            "     * groupKeyPattern -> matched groupKeys set.",
            "     * 模糊 pattern → 当前已匹配到的 groupKey 集合（受上限保护）。",
        ),
        (
            "     * trim  fuzzy watch context. <br/> 1.remove watchedClients if watched client is empty. 2.remove matchedServiceKeys",
            "     * 定期裁剪模糊监听上下文：无订阅客户端时移除 matchedGroupKeys；订阅集合为空时移除 watchedClients。\n"
            "     * trim  fuzzy watch context. <br/> 1.remove watchedClients if watched client is empty. 2.remove matchedServiceKeys",
        ),
        (
            "     * sync group key change to fuzzy context.",
            "     * 将单条 groupKey 的增删变更同步到各 pattern 的 matchedGroupKeys，返回是否需要通知客户端。\n     *\n     * sync group key change to fuzzy context.",
        ),
        (
            "     * make matched group key when deleted configs on loa protection model.",
            "     * 在负载保护模型下，删除配置导致匹配数下降时补全 matchedGroupKeys。\n     *\n     * make matched group key when deleted configs on loa protection model.",
        ),
        (
            "        // Add the connection ID to the set associated with the key pattern in keyPatternContext",
            "        // 将连接 ID 加入该 pattern 的订阅集合",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigMigrateService.java": [
        (
            "/**\n * migrate beta and tag to gray model. should only invoked from config sync notify.\n *\n * @author shiyiyue\n */",
            "/**\n * 配置模型迁移服务：将历史 Beta/Tag 表数据双写或迁移至灰度（Gray）模型。\n"
            " * 通常在配置同步通知链路中调用；支持命名空间迁移与旧表版本兼容。\n"
            " * migrate beta and tag to gray model. should only invoked from config sync notify.\n *\n * @author shiyiyue\n */",
        ),
        (
            "     * The Config info beta persist service.",
            "     * 历史 Beta 配置持久化服务。",
        ),
        (
            "     * The Config info tag persist service.",
            "     * 历史 Tag 配置持久化服务。",
        ),
        (
            "     * The Config info gray persist service.",
            "     * 灰度配置持久化服务（新模型）。",
        ),
        (
            "     * The Config info persist service.",
            "     * 正式配置持久化服务。",
        ),
        (
            "     * The Config migrate persist service.",
            "     * 迁移进度与双写状态持久化服务。",
        ),
        (
            "     * The Namespace persist service.",
            "     * 命名空间持久化服务（迁移 namespace 时使用）。",
        ),
        (
            "     * The Old table version.",
            "     * 是否为旧表结构版本（影响迁移分支逻辑）。",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/ConfigOperationService.java": [
        (
            "/**\n * ConfigService.\n *\n * @author dongyafei\n * @date 2022/8/11\n */",
            "/**\n * 配置写操作统一入口：发布/删除正式与灰度配置，协调持久化、迁移双写与变更事件。\n"
            " * 支持 Beta/Tag 灰度发布、CAS 乐观锁、Istio 标签联动与操作审计追踪。\n"
            " * ConfigService.\n *\n * @author dongyafei\n * @date 2022/8/11\n */",
        ),
        (
            "     * Adds or updates non-aggregated data.",
            "     * 发布或更新单条配置（含 Beta/Tag 灰度分支与正式 CAS/insert 逻辑）。\n     *\n     * Adds or updates non-aggregated data.",
        ),
        (
            "        //beta publish",
            "        // Beta 灰度发布：双写旧表并迁移至 Gray 模型",
        ),
        (
            "        // tag publish",
            "        // Tag 灰度发布：双写 Tag 表并同步 Gray",
        ),
        (
            "        //formal publish",
            "        // 正式配置发布：CAS 或 insert/update",
        ),
        (
            "     * publish gray config tag v2.",
            "     * 发布 TagV2/Beta 等灰度配置：校验规则格式与版本数量上限后持久化。\n     *\n     * publish gray config tag v2.",
        ),
        (
            "        //version count check.",
            "        // 校验同一 dataId 下灰度版本数是否超限",
        ),
        (
            "        // set old md5",
            "        // CAS 发布时携带客户端已知 MD5",
        ),
        (
            "     * Synchronously delete all pre-aggregation data under a dataId.",
            "     * 同步删除配置：grayName 为空删正式配置，否则删指定灰度版本；并发布变更事件与审计日志。\n     *\n     * Synchronously delete all pre-aggregation data under a dataId.",
        ),
    ],
}
