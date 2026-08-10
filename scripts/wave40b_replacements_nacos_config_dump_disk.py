"""Chinese annotation replacements for Nacos 3.2.3 wave40b [15:30] config dump disk."""

R: dict[str, list[tuple[str, str]]] = {
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/DumpService.java": [
        (
            "/**\n * Dump data service.\n *\n * @author Nacos\n */",
            "/**\n * 配置 Dump 核心抽象服务：将持久层配置同步至内存缓存与本地磁盘。\n"
            " * <p>负责监听 {@link ConfigDataChangeEvent} 触发增量 dump、调度全量 dump、"
            "灰度全量 dump 及历史配置清理；嵌入式与外部存储由子类实现启动与执行权限控制。</p>\n"
            " * Dump data service.\n *\n * @author Nacos\n */",
        ),
        (
            "    protected DumpProcessor processor;",
            "    /** 增量 dump 任务处理器（正式/灰度单条配置） */\n    protected DumpProcessor processor;",
        ),
        (
            "    protected DumpAllProcessor dumpAllProcessor;",
            "    /** 全量正式配置 dump 处理器 */\n    protected DumpAllProcessor dumpAllProcessor;",
        ),
        (
            "    protected DumpAllGrayProcessor dumpAllGrayProcessor;",
            "    /** 全量灰度配置 dump 处理器 */\n    protected DumpAllGrayProcessor dumpAllGrayProcessor;",
        ),
        (
            "    /**\n     * full dump interval.\n     */",
            "    /**\n     * 全量 dump 定时任务间隔（分钟）。\n     * full dump interval.\n     */",
        ),
        (
            "    /**\n     * full dump delay.\n     */",
            "    /**\n     * 全量 dump 首次调度随机延迟上限（分钟）。\n     * full dump delay.\n     */",
        ),
        (
            "    private TaskManager dumpTaskMgr;",
            "    /** 增量 dump 任务队列管理器 */\n    private TaskManager dumpTaskMgr;",
        ),
        (
            "    private TaskManager dumpAllTaskMgr;",
            "    /** 全量 dump 任务队列管理器（含正式与灰度处理器路由） */\n    private TaskManager dumpAllTaskMgr;",
        ),
        (
            "     * Here you inject the dependent objects constructively, ensuring that some of the dependent functionality is\n"
            "     * initialized ahead of time.\n",
            "     * 构造注入持久化、迁移、集群等依赖，并提前初始化 dump 处理器与事件订阅。\n"
            "     * Here you inject the dependent objects constructively, ensuring that some of the dependent functionality is\n"
            "     * initialized ahead of time.\n",
        ),
        (
            "        // Generate ConfigDataChangeEvent concurrently",
            "        // 并发场景下收到配置变更事件，构造 DumpRequest 并入队",
        ),
        (
            "    /**\n     * initialize.\n",
            "    /**\n     * 子类实现的 dump 启动入口（嵌入式需等待 Raft 选主，外部存储直接 dump）。\n     * initialize.\n",
        ),
        (
            "    /**\n     * config history clear.\n     */\n    class ConfigHistoryClear implements Runnable {",
            "    /**\n     * 历史配置清理定时任务：按 {@link #canExecute()} 权限调用 {@link HistoryConfigCleaner}。\n     * config history clear.\n     */\n    class ConfigHistoryClear implements Runnable {",
        ),
        (
            "    /**\n     * config history clear.\n     */\n    class DumpAllProcessorRunner implements Runnable {",
            "    /**\n     * 全量正式配置 dump 调度 Runner：向 dumpAllTaskMgr 提交 {@link DumpAllTask}。\n     * config history clear.\n     */\n    class DumpAllProcessorRunner implements Runnable {",
        ),
        (
            "    /**\n     * dump all gray processor runner.\n     */",
            "    /**\n     * 全量灰度配置 dump 调度 Runner：向 dumpAllTaskMgr 提交 {@link DumpAllGrayTask}。\n     * dump all gray processor runner.\n     */",
        ),
        (
            "            LogUtil.DEFAULT_LOG.warn(\"DumpService start\");",
            "            // dump 主流程开始：启动时全量 + 集群模式下定时任务注册\n            LogUtil.DEFAULT_LOG.warn(\"DumpService start\");",
        ),
        (
            "            if (!EnvUtil.getStandaloneMode()) {",
            "            // 非单机模式：注册全量 dump、变更 worker 与历史清理定时任务\n            if (!EnvUtil.getStandaloneMode()) {",
        ),
        (
            "            LogUtil.DEFAULT_LOG.info(\"start clear all config-info.\");",
            "            // 启动时清空磁盘缓存后执行全量 dump\n            LogUtil.DEFAULT_LOG.info(\"start clear all config-info.\");",
        ),
        (
            "            LogUtil.DEFAULT_LOG.info(\"start to clear all gray-config-info on startup.\");",
            "            // 启动时清空灰度磁盘缓存后执行灰度全量 dump\n            LogUtil.DEFAULT_LOG.info(\"start to clear all gray-config-info on startup.\");",
        ),
        (
            "     * dump operation.\n",
            "     * 对外 dump 入口：按 grayName 分流至正式或灰度增量任务。\n     * dump operation.\n",
        ),
        (
            "     * dump formal config.\n",
            "     * 增量 dump 正式配置：以 groupKey 为 taskKey 入队 {@link DumpTask}。\n     * dump formal config.\n",
        ),
        (
            "     * dump gray.\n",
            "     * 增量 dump 灰度配置：taskKey 附加 grayName 后缀以区分并发任务。\n     * dump gray.\n",
        ),
        (
            "     * Used to determine whether the aggregation task, configuration history cleanup task can be performed.\n",
            "     * 判断当前节点是否可执行全量 dump 与历史清理（嵌入式仅 Leader，外部存储仅首 IP）。\n"
            "     * Used to determine whether the aggregation task, configuration history cleanup task can be performed.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/EmbeddedDumpService.java": [
        (
            "/**\n * Embedded dump service.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
            "/**\n * 嵌入式存储（Derby + Raft）场景下的 Dump 服务实现。\n"
            " * <p>启动时订阅配置 Raft 组 Leader 元数据，选主成功后执行 {@link #dumpOperate()}；"
            "仅 Leader 节点执行 dump 与历史清理。</p>\n"
            " * Embedded dump service.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        ),
        (
            "    /**\n     * If it's just a normal reading failure, it can be resolved by retrying.\n     */",
            "    /**\n     * 可重试的读失败错误信息（一致性协议暂时不可读等）。\n"
            "     * If it's just a normal reading failure, it can be resolved by retrying.\n     */",
        ),
        (
            "    /**\n     * If the read failed due to an internal problem in the Raft state machine, it cannot be remedied by retrying.\n     */",
            "    /**\n     * 不可重试的 Raft 状态机内部错误（FSM 过载、STATE_ERROR 等）。\n"
            "     * If the read failed due to an internal problem in the Raft state machine, it cannot be remedied by retrying.\n     */",
        ),
        (
            "        // watch path => /nacos_config/leader/ has value ?",
            "        // 订阅 /nacos_config/leader/ 元数据，Leader 就绪后触发 dump",
        ),
        (
            "                    // must make sure that there is a value here to perform the correct operation that follows",
            "                    // Leader 元数据非空才继续 dump，避免空值误触发",
        ),
        (
            "                    // Identify without a timeout mechanism",
            "                    // 标记需持续读直到有数据（嵌入式存储扩展上下文）",
        ),
        (
            "                    // Remove your own listening to avoid task accumulation",
            "                    // dump 成功后取消订阅，避免 Observer 任务堆积",
        ),
        (
            "        // We must wait for the dump task to complete the callback operation before",
            "        // 必须等待 dump 回调完成再继续节点初始化，否则启动顺序错乱",
        ),
        (
            "        // If an exception occurs during the execution of the dump task, the exception",
            "        // dump 失败则向上抛出，触发节点启动失败流程",
        ),
        (
            "        // if is derby + raft mode, only leader can execute",
            "        // Derby + Raft 模式下仅 Leader 可执行 dump 与清理",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/ExternalDumpService.java": [
        (
            "/**\n * External dump service.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
            "/**\n * 外部存储（MySQL 等）场景下的 Dump 服务实现。\n"
            " * <p>启动后直接 {@link #dumpOperate()}；仅集群首个 IP 节点执行 dump 与历史清理，"
            "避免多节点重复全量任务。</p>\n"
            " * External dump service.\n *\n * @author <a href=\"mailto:liaochuntao@live.com\">liaochuntao</a>\n */",
        ),
        (
            "     * Here you inject the dependent objects constructively, ensuring that some of the dependent functionality is\n"
            "     * initialized ahead of time.\n",
            "     * 构造注入依赖并委托父类完成处理器与事件订阅初始化。\n"
            "     * Here you inject the dependent objects constructively, ensuring that some of the dependent functionality is\n"
            "     * initialized ahead of time.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/HistoryConfigCleaner.java": [
        (
            "/**\n * The interface History config cleaner.\n * @author Sunrisea\n */",
            "/**\n * 历史配置清理 SPI 接口：由 {@link HistoryConfigCleanerManager} 按名称加载实现。\n"
            " * The interface History config cleaner.\n * @author Sunrisea\n */",
        ),
        (
            "    /**\n     * Clean history config.\n     */",
            "    /**\n     * 执行历史配置清理逻辑（过期记录删除等）。\n     * Clean history config.\n     */",
        ),
        (
            "    /**\n     * Gets name.\n",
            "    /**\n     * 返回清理器唯一名称，用于配置项 {@code nacos.config.history.clear.name} 匹配。\n     * Gets name.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/HistoryConfigCleanerConfig.java": [
        (
            "/**\n * The type History config cleaner config.\n * @author Sunrisea\n */",
            "/**\n * 历史配置清理器动态配置：从环境变量读取当前启用的清理器名称。\n"
            " * The type History config cleaner config.\n * @author Sunrisea\n */",
        ),
        (
            "    private String activeHistoryConfigCleaner = \"nacos\";",
            "    /** 当前激活的历史清理器名称，默认 nacos 内置实现 */\n    private String activeHistoryConfigCleaner = \"nacos\";",
        ),
        (
            "    /**\n     * Gets instance.\n",
            "    /**\n     * 获取单例配置实例。\n     * Gets instance.\n",
        ),
        (
            "    /**\n     * Gets active history config cleaner.\n",
            "    /**\n     * 获取当前启用的历史清理器名称。\n     * Gets active history config cleaner.\n",
        ),
        (
            "    /**\n     * Sets active history config cleaner.\n",
            "    /**\n     * 动态设置历史清理器名称（运行时配置变更）。\n     * Sets active history config cleaner.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/HistoryConfigCleanerManager.java": [
        (
            "/**\n * The type History config cleaner manager.\n *\n * @author Sunrisea\n */",
            "/**\n * 历史配置清理器管理器：通过 SPI 加载实现并注册默认 {@code nacos} 清理器。\n"
            " * The type History config cleaner manager.\n *\n * @author Sunrisea\n */",
        ),
        (
            "    private static HashMap<String, HistoryConfigCleaner> historyConfigCleanerMap =",
            "    /** 清理器名称 → 实例映射表 */\n    private static HashMap<String, HistoryConfigCleaner> historyConfigCleanerMap =",
        ),
        (
            "    /**\n     * Gets history config cleaner.\n",
            "    /**\n     * 按名称获取清理器，未命中时回退至默认 nacos 实现。\n     * Gets history config cleaner.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/disk/ConfigDiskService.java": [
        (
            "/**\n * config disk service.\n *\n * @author zunfei.lzf\n */",
            "/**\n * 配置本地磁盘持久化 SPI：抽象正式/灰度配置的读写删与全量清空。\n"
            " * config disk service.\n *\n * @author zunfei.lzf\n */",
        ),
        (
            "     * Save configuration information to disk.\n",
            "     * 将正式配置内容写入本地磁盘。\n     * Save configuration information to disk.\n",
        ),
        (
            "     * Save gray information to disk.\n",
            "     * 将灰度配置内容写入本地磁盘。\n     * Save gray information to disk.\n",
        ),
        (
            "     * Deletes gray configuration files on disk.\n",
            "     * 删除磁盘上的灰度配置文件。\n     * Deletes gray configuration files on disk.\n",
        ),
        (
            "     * Returns the content of the gray cache file in server.\n",
            "     * 读取服务端灰度缓存文件内容，不存在时返回 null。\n     * Returns the content of the gray cache file in server.\n",
        ),
        (
            "     * Deletes configuration files on disk.\n",
            "     * 删除磁盘上的正式配置文件。\n     * Deletes configuration files on disk.\n",
        ),
        (
            "     * Returns the content of the  cache file in server.\n",
            "     * 读取服务端正式配置缓存文件内容，不存在时返回 null。\n     * Returns the content of the  cache file in server.\n",
        ),
        (
            "     * Clear all config file.\n",
            "     * 清空全部正式配置磁盘文件（启动全量 dump 前调用）。\n     * Clear all config file.\n",
        ),
        (
            "     * Clear all gray config file.\n",
            "     * 清空全部灰度配置磁盘文件。\n     * Clear all gray config file.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/disk/ConfigDiskServiceFactory.java": [
        (
            "/**\n * config disk serve factory.\n *\n * @author zunfei.lzf\n */",
            "/**\n * 配置磁盘服务工厂：按系统属性 {@code config_disk_type} 选择 Raw 文件或 RocksDB 实现。\n"
            " * config disk serve factory.\n *\n * @author zunfei.lzf\n */",
        ),
        (
            "    static ConfigDiskService configDiskService;",
            "    /** 单例磁盘服务实例（双重检查锁懒加载） */\n    static ConfigDiskService configDiskService;",
        ),
        (
            "    private static final String TYPE_RAW_DISK = \"rawdisk\";",
            "    /** 原始文件目录存储类型标识 */\n    private static final String TYPE_RAW_DISK = \"rawdisk\";",
        ),
        (
            "    private static final String TYPE_ROCKSDB = \"rocksdb\";",
            "    /** RocksDB KV 存储类型标识 */\n    private static final String TYPE_ROCKSDB = \"rocksdb\";",
        ),
        (
            "     * get disk service.\n",
            "     * 获取磁盘服务单例，默认 rawdisk，可配置为 rocksdb。\n     * get disk service.\n",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/disk/ConfigRawDiskService.java": [
        (
            "/**\n * config raw disk service.\n *\n * @author zunfei.lzf\n */",
            "/**\n * 基于目录结构的配置磁盘实现：按 dataId/group/tenant/grayName 映射文件路径。\n"
            " * <p>路径经 {@link PathEncoderManager} 编码，防止特殊字符导致目录遍历问题。</p>\n"
            " * config raw disk service.\n *\n * @author zunfei.lzf\n */",
        ),
        (
            "    private static final String BASE_DIR = File.separator + \"data\" + File.separator + \"config-data\";",
            "    /** 默认命名空间正式配置根目录（相对 Nacos Home） */\n    private static final String BASE_DIR = File.separator + \"data\" + File.separator + \"config-data\";",
        ),
        (
            "    private static final String TENANT_BASE_DIR =",
            "    /** 多租户正式配置根目录 */\n    private static final String TENANT_BASE_DIR =",
        ),
        (
            "    private static final String GRAY_DIR = File.separator + \"data\" + File.separator + \"gray-data\";",
            "    /** 默认命名空间灰度配置根目录 */\n    private static final String GRAY_DIR = File.separator + \"data\" + File.separator + \"gray-data\";",
        ),
        (
            "     * Save configuration information to disk.\n     */",
            "     * 将正式配置写入 targetFile 对应路径。\n     * Save configuration information to disk.\n     */",
        ),
        (
            "     * Returns the path of the server cache file.\n     */",
            "     * 计算正式配置磁盘文件路径（含参数校验与路径编码）。\n     * Returns the path of the server cache file.\n     */",
        ),
        (
            "        // fix https://github.com/alibaba/nacos/issues/10067",
            "        // 对 dataId/group/tenant 做路径编码，修复特殊字符目录问题",
        ),
        (
            "     * Returns the path of the gray cache file in server.\n     */",
            "     * 计算灰度配置磁盘文件路径（含 grayName 子目录）。\n     * Returns the path of the gray cache file in server.\n     */",
        ),
        (
            "     * Clear all config file.\n     */",
            "     * 删除 config-data 与 tenant-config-data 目录下全部正式配置。\n     * Clear all config file.\n     */",
        ),
        (
            "     * Clear all gray config file.\n     */",
            "     * 删除 gray-data 与 tenant-gray-data 目录下全部灰度配置。\n     * Clear all gray config file.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/disk/ConfigRocksDbDiskService.java": [
        (
            "/**\n * config rocks db disk service.\n *\n * @author shiyiyue\n */",
            "/**\n * 基于 RocksDB 的配置磁盘实现：以编码后的复合键存储正式/灰度配置内容。\n"
            " * <p>按目录分库（config-data / gray-data），针对正式库动态调整 WriteBuffer 与 compaction 策略。</p>\n"
            " * config rocks db disk service.\n *\n * @author shiyiyue\n */",
        ),
        (
            "    Map<String, RocksDB> rocksDbMap = new HashMap<>();",
            "    /** 目录路径 → RocksDB 实例缓存（懒加载打开） */\n    Map<String, RocksDB> rocksDbMap = new HashMap<>();",
        ),
        (
            "     * + -> %2B % -> %25.\n     */",
            "     * URL 风格键编码：{@code +} → {@code %2B}，{@code %} → {@code %25}。\n     * + -> %2B % -> %25.\n     */",
        ),
        (
            "     * save config to disk.\n     */",
            "     * 内部写入：按 type 选择 DB 并以复合键 put 配置内容。\n     * save config to disk.\n     */",
        ),
        (
            "     * Save tag information to disk.\n     */",
            "     * 将灰度配置写入 GRAY_DIR 对应 RocksDB。\n     * Save tag information to disk.\n     */",
        ),
        (
            "        //set more write buffer size to formal config-data, reduce flush to sst file frequency.",
            "        // 正式库增大 WriteBuffer，降低 flush 频率",
        ),
        (
            "        //once a stt file is flushed, compact it immediately to avoid too many sst file which will result in read latency.",
            "        // Level0 文件数达 1 即触发 compaction，避免 SST 过多导致读延迟",
        ),
        (
            "     * get suit formal buffer size.\n",
            "     * 按 JVM 堆大小为正式库选择合适的 WriteBuffer（MB）。\n     * get suit formal buffer size.\n",
        ),
        (
            "     * Clear all config file.\n     */",
            "     * 关闭并销毁正式配置 RocksDB，删除 rocksdata/config-data 目录。\n     * Clear all config file.\n     */",
        ),
        (
            "     * Clear all gray config file.\n     */",
            "     * 关闭并销毁灰度 RocksDB，删除 rocksdata/gray-data 目录。\n     * Clear all gray config file.\n     */",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/processor/DumpAllGrayProcessor.java": [
        (
            "/**\n * Dump all gray processor.\n *\n * @author Nacos\n * @datete 2024/02/20\n */",
            "/**\n * 全量灰度配置 dump 处理器：分页扫描持久层灰度配置并调用 {@link ConfigCacheService#dumpGray}。\n"
            " * <p>跳过 tenant 为空的记录；用于启动时与定时全量灰度同步。</p>\n"
            " * Dump all gray processor.\n *\n * @author Nacos\n * @datete 2024/02/20\n */",
        ),
        (
            "    static final int PAGE_SIZE = getAllDumpPageSize();",
            "    /** 全量 dump 分页大小（来自 {@link PropertyUtil#getAllDumpPageSize}） */\n    static final int PAGE_SIZE = getAllDumpPageSize();",
        ),
        (
            "    final ConfigInfoGrayPersistService configInfoGrayPersistService;",
            "    /** 灰度配置持久化服务 */\n    final ConfigInfoGrayPersistService configInfoGrayPersistService;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/processor/DumpAllProcessor.java": [
        (
            "/**\n * Dump all processor.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
            "/**\n * 全量正式配置 dump 处理器：按 ID 片段分页扫描 DB，对比 MD5/时间戳后异步写入缓存与磁盘。\n"
            " * <p>启动模式使用多线程池加速；非启动模式单线程且按需拉取变更内容。"
            "特殊 dataId（白名单、开关）会同步加载到内存。</p>\n"
            " * Dump all processor.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
        ),
        (
            "                //if not start up, page query will not return content, check md5 and lastModified first ,if changed ,get single content info to dump.",
            "                // 非启动全量：分页不含 content，先比对 MD5/时间戳，有变更再单条查询内容",
        ),
        (
            "                    //check md5 & update local disk cache.",
            "                    // 校验 MD5 与 lastModified，决定是否重新 dump 到本地磁盘缓存",
        ),
        (
            "        //wait all task are finished and then shutdown executor.",
            "        // 等待线程池中 dump 任务全部完成后关闭 executor",
        ),
        (
            "    final ConfigInfoPersistService configInfoPersistService;",
            "    /** 正式配置持久化服务 */\n    final ConfigInfoPersistService configInfoPersistService;",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/processor/DumpProcessor.java": [
        (
            "/**\n * dump processor.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
            "/**\n * 增量 dump 处理器：解析 {@link DumpTask} 的 groupKey，从 DB 拉取最新配置并委托 {@link DumpConfigHandler} 落盘。\n"
            " * <p>支持正式与灰度两条路径；DB 无记录时标记 remove。</p>\n"
            " * dump processor.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
        ),
        (
            "    final ConfigInfoPersistService configInfoPersistService;",
            "    /** 正式配置持久化服务 */\n    final ConfigInfoPersistService configInfoPersistService;",
        ),
        (
            "    final ConfigInfoGrayPersistService configInfoGrayPersistService;",
            "    /** 灰度配置持久化服务 */\n    final ConfigInfoGrayPersistService configInfoGrayPersistService;",
        ),
        (
            "        String type = \"formal\";",
            "        // 日志类型标识：灰度任务使用 grayName，否则为 formal\n        String type = \"formal\";",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/task/DumpAllBetaTask.java": [
        (
            "/**\n * Dump all beta task.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
            "/**\n * 全量 Beta 配置 dump 延迟任务占位类（TASK_ID 供任务管理器路由）。\n"
            " * <p>merge 为空实现，Beta 全量逻辑已迁移至 Gray 体系。</p>\n"
            " * Dump all beta task.\n *\n * @author Nacos\n * @date 2020/7/5 12:19 PM\n */",
        ),
        (
            "    public static final String TASK_ID = \"dumpAllBetaConfigTask\";",
            "    /** 全量 Beta dump 任务唯一标识 */\n    public static final String TASK_ID = \"dumpAllBetaConfigTask\";",
        ),
    ],
    "config/src/main/java/com/alibaba/nacos/config/server/service/dump/task/DumpAllGrayTask.java": [
        (
            "/**\n * Dump all gray task.\n *\n * @author Nacos\n * @date 2024/3/5\n */",
            "/**\n * 全量灰度配置 dump 延迟任务，由 {@link DumpAllGrayProcessor} 处理。\n"
            " * Dump all gray task.\n *\n * @author Nacos\n * @date 2024/3/5\n */",
        ),
        (
            "    @Override\n    public void merge(AbstractDelayTask task) {\n    }",
            "    /** 全量灰度任务合并策略：空实现，不做任务合并 */\n    @Override\n    public void merge(AbstractDelayTask task) {\n    }",
        ),
    ],
}
