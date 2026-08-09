# Auto-generated replacements - verified against original
# Files: 15, total pairs: 176

R: dict[str, list[tuple[str, str]]] = {'store/src/main/java/org/apache/rocketmq/store/kv/CompactionService.java': [('public class CompactionService {',
                                                                              '/**\n'
                                                                              ' * 压缩（Compaction）服务：处理 COMPACTION 清理策略 '
                                                                              'Topic 的分发请求并写入 CompactionStore。\n'
                                                                              ' */\n'
                                                                              'public class CompactionService {'),
                                                                             ('    private final CompactionStore '
                                                                              'compactionStore;',
                                                                              '    /** 底层压缩存储。 */\n'
                                                                              '    private final CompactionStore '
                                                                              'compactionStore;'),
                                                                             ('    private final DefaultMessageStore '
                                                                              'defaultMessageStore;',
                                                                              '    /** 所属 MessageStore。 */\n'
                                                                              '    private final DefaultMessageStore '
                                                                              'defaultMessageStore;'),
                                                                             ('    private final CommitLog commitLog;',
                                                                              '    /** CommitLog，用于读取原始消息数据。 */\n'
                                                                              '    private final CommitLog commitLog;'),
                                                                             ('    public CompactionService(CommitLog '
                                                                              'commitLog, DefaultMessageStore '
                                                                              'messageStore, CompactionStore '
                                                                              'compactionStore) {',
                                                                              '    /** 构造压缩服务。 */\n'
                                                                              '    public CompactionService(CommitLog '
                                                                              'commitLog, DefaultMessageStore '
                                                                              'messageStore, CompactionStore '
                                                                              'compactionStore) {'),
                                                                             ('    public void '
                                                                              'putRequest(DispatchRequest request) {',
                                                                              '    /** 处理分发请求：COMPACTION 策略 Topic '
                                                                              '则写入压缩日志。 */\n'
                                                                              '    public void '
                                                                              'putRequest(DispatchRequest request) {'),
                                                                             ('    public boolean load(boolean exitOK) '
                                                                              '{',
                                                                              '    /** 加载 CompactionStore 数据。 */\n'
                                                                              '    public boolean load(boolean exitOK) '
                                                                              '{'),
                                                                             ('    public void shutdown() {',
                                                                              '    /** 关闭压缩存储。 */\n'
                                                                              '    public void shutdown() {'),
                                                                             ('    public void '
                                                                              'updateMasterAddress(String addr) {',
                                                                              '    /** 更新主节点地址。 */\n'
                                                                              '    public void '
                                                                              'updateMasterAddress(String addr) {')],
 'store/src/main/java/org/apache/rocketmq/store/kv/CompactionStore.java': [('public class CompactionStore {',
                                                                            '/**\n'
                                                                            ' * 压缩存储管理器：维护 CompactionLog 表、定时扫描 Topic '
                                                                            '配置并调度压缩任务。\n'
                                                                            ' */\n'
                                                                            'public class CompactionStore {'),
                                                                           ('    public static final String '
                                                                            'COMPACTION_DIR = "compaction";',
                                                                            '    /** 压缩根目录名。 */\n'
                                                                            '    public static final String '
                                                                            'COMPACTION_DIR = "compaction";'),
                                                                           ('    public static final String '
                                                                            'COMPACTION_LOG_DIR = "compactionLog";',
                                                                            '    /** 压缩日志子目录名。 */\n'
                                                                            '    public static final String '
                                                                            'COMPACTION_LOG_DIR = "compactionLog";'),
                                                                           ('    public static final String '
                                                                            'COMPACTION_CQ_DIR = "compactionCq";',
                                                                            '    /** 压缩消费队列子目录名。 */\n'
                                                                            '    public static final String '
                                                                            'COMPACTION_CQ_DIR = "compactionCq";'),
                                                                           ('    private final String compactionPath;',
                                                                            '    /** 压缩存储根路径。 */\n'
                                                                            '    private final String compactionPath;'),
                                                                           ('    private final String '
                                                                            'compactionLogPath;',
                                                                            '    /** 压缩日志路径。 */\n'
                                                                            '    private final String '
                                                                            'compactionLogPath;'),
                                                                           ('    private final String '
                                                                            'compactionCqPath;',
                                                                            '    /** 压缩消费队列路径。 */\n'
                                                                            '    private final String '
                                                                            'compactionCqPath;'),
                                                                           ('    private final DefaultMessageStore '
                                                                            'defaultMessageStore;',
                                                                            '    /** 所属 MessageStore。 */\n'
                                                                            '    private final DefaultMessageStore '
                                                                            'defaultMessageStore;'),
                                                                           ('    private final CompactionPositionMgr '
                                                                            'positionMgr;',
                                                                            '    /** 压缩位点管理器。 */\n'
                                                                            '    private final CompactionPositionMgr '
                                                                            'positionMgr;'),
                                                                           ('    private final '
                                                                            'ConcurrentHashMap<String, CompactionLog> '
                                                                            'compactionLogTable;',
                                                                            '    /** topic_queueId -> CompactionLog '
                                                                            '映射表。 */\n'
                                                                            '    private final '
                                                                            'ConcurrentHashMap<String, CompactionLog> '
                                                                            'compactionLogTable;'),
                                                                           ('    public '
                                                                            'CompactionStore(DefaultMessageStore '
                                                                            'defaultMessageStore) {',
                                                                            '    /** 构造压缩存储并初始化路径与调度线程池。 */\n'
                                                                            '    public '
                                                                            'CompactionStore(DefaultMessageStore '
                                                                            'defaultMessageStore) {'),
                                                                           ('    public void load(boolean exitOk) '
                                                                            'throws Exception {',
                                                                            '    /** 从磁盘加载已有 CompactionLog 并启动 Topic '
                                                                            '扫描任务。 */\n'
                                                                            '    public void load(boolean exitOk) '
                                                                            'throws Exception {'),
                                                                           ('    public void putMessage(String topic, '
                                                                            'int queueId, SelectMappedBufferResult '
                                                                            'smr) throws Exception {',
                                                                            '    /** 异步写入压缩消息。 */\n'
                                                                            '    public void putMessage(String topic, '
                                                                            'int queueId, SelectMappedBufferResult '
                                                                            'smr) throws Exception {'),
                                                                           ('    public void '
                                                                            'doDispatch(DispatchRequest '
                                                                            'dispatchRequest, SelectMappedBufferResult '
                                                                            'smr) throws Exception {',
                                                                            '    /** 分发路径写入压缩日志。 */\n'
                                                                            '    public void '
                                                                            'doDispatch(DispatchRequest '
                                                                            'dispatchRequest, SelectMappedBufferResult '
                                                                            'smr) throws Exception {'),
                                                                           ('    public GetMessageResult '
                                                                            'getMessage(final String group, final '
                                                                            'String topic, final int queueId, final '
                                                                            'long offset,\n'
                                                                            '        final int maxMsgNums, final int '
                                                                            'maxTotalMsgSize) {',
                                                                            '    /** 从压缩日志按偏移拉取消息。 */\n'
                                                                            '    public GetMessageResult '
                                                                            'getMessage(final String group, final '
                                                                            'String topic, final int queueId, final '
                                                                            'long offset,\n'
                                                                            '        final int maxMsgNums, final int '
                                                                            'maxTotalMsgSize) {'),
                                                                           ('    public void flush(int '
                                                                            'flushLeastPages) {',
                                                                            '    /** 刷盘所有 CompactionLog。 */\n'
                                                                            '    public void flush(int '
                                                                            'flushLeastPages) {'),
                                                                           ('    public void flushLog(int '
                                                                            'flushLeastPages) {',
                                                                            '    /** 刷盘压缩 CommitLog 部分。 */\n'
                                                                            '    public void flushLog(int '
                                                                            'flushLeastPages) {'),
                                                                           ('    public void flushCQ(int '
                                                                            'flushLeastPages) {',
                                                                            '    /** 刷盘压缩 ConsumeQueue 部分。 */\n'
                                                                            '    public void flushCQ(int '
                                                                            'flushLeastPages) {'),
                                                                           ('    public void '
                                                                            'updateMasterAddress(String addr) {',
                                                                            '    /** 更新主节点地址。 */\n'
                                                                            '    public void '
                                                                            'updateMasterAddress(String addr) {'),
                                                                           ('    public void shutdown() {',
                                                                            '    /** 关闭调度线程池并持久化位点。 */\n'
                                                                            '    public void shutdown() {'),
                                                                           ('    public ScheduledExecutorService '
                                                                            'getCompactionSchedule() {',
                                                                            '    /** 返回压缩调度线程池。 */\n'
                                                                            '    public ScheduledExecutorService '
                                                                            'getCompactionSchedule() {'),
                                                                           ('    public String getCompactionLogPath() '
                                                                            '{',
                                                                            '    /** 返回压缩日志路径。 */\n'
                                                                            '    public String getCompactionLogPath() '
                                                                            '{'),
                                                                           ('    public String getCompactionCqPath() {',
                                                                            '    /** 返回压缩 CQ 路径。 */\n'
                                                                            '    public String getCompactionCqPath() '
                                                                            '{'),
                                                                           ('    public CompactionPositionMgr '
                                                                            'getPositionMgr() {',
                                                                            '    /** 返回位点管理器。 */\n'
                                                                            '    public CompactionPositionMgr '
                                                                            'getPositionMgr() {'),
                                                                           ('    public int getOffsetMapSize() {',
                                                                            '    /** 返回偏移映射表大小。 */\n'
                                                                            '    public int getOffsetMapSize() {'),
                                                                           ('    public String getMasterAddr() {',
                                                                            '    /** 返回主节点地址。 */\n'
                                                                            '    public String getMasterAddr() {')],
 'store/src/main/java/org/apache/rocketmq/store/kv/MessageFetcher.java': [('public class MessageFetcher implements '
                                                                           'AutoCloseable {',
                                                                           '/**\n'
                                                                           ' * 消息拉取客户端：通过 Remoting 从主节点 LITE_PULL '
                                                                           '拉取压缩所需消息。\n'
                                                                           ' */\n'
                                                                           'public class MessageFetcher implements '
                                                                           'AutoCloseable {'),
                                                                          ('    private final RemotingClient client;',
                                                                           '    /** Netty Remoting 客户端。 */\n'
                                                                           '    private final RemotingClient client;'),
                                                                          ('    public MessageFetcher() {',
                                                                           '    /** 创建并启动 Remoting 客户端。 */\n'
                                                                           '    public MessageFetcher() {'),
                                                                          ('    @Override\n'
                                                                           '    public void close() throws IOException '
                                                                           '{',
                                                                           '    /** 关闭 Remoting 客户端。 */\n'
                                                                           '    @Override\n'
                                                                           '    public void close() throws IOException '
                                                                           '{'),
                                                                          ('    public void '
                                                                           'pullMessageFromMaster(String topic, int '
                                                                           'queueId, long endOffset, String '
                                                                           'masterAddr,\n'
                                                                           '                                      '
                                                                           'BiFunction<Long, RemotingCommand, Boolean> '
                                                                           'responseHandler) throws Exception {',
                                                                           '    /**\n'
                                                                           '     * 从主节点循环拉取消息直至 endOffset。\n'
                                                                           '     *\n'
                                                                           '     * @param responseHandler 处理每条拉取响应，返回 '
                                                                           'false 可提前终止\n'
                                                                           '     */\n'
                                                                           '    public void '
                                                                           'pullMessageFromMaster(String topic, int '
                                                                           'queueId, long endOffset, String '
                                                                           'masterAddr,\n'
                                                                           '                                      '
                                                                           'BiFunction<Long, RemotingCommand, Boolean> '
                                                                           'responseHandler) throws Exception {')],
 'store/src/main/java/org/apache/rocketmq/store/lock/AdaptiveBackOffSpinLock.java': [('public interface '
                                                                                      'AdaptiveBackOffSpinLock extends '
                                                                                      'PutMessageLock {',
                                                                                      '/**\n'
                                                                                      ' * 自适应退避自旋锁接口：支持配置更新与自旋/互斥锁切换。\n'
                                                                                      ' */\n'
                                                                                      'public interface '
                                                                                      'AdaptiveBackOffSpinLock extends '
                                                                                      'PutMessageLock {'),
                                                                                     ('    /**\n'
                                                                                      '     * Configuration update\n'
                                                                                      '     * @param '
                                                                                      'messageStoreConfig\n'
                                                                                      '     */',
                                                                                      '    /**\n'
                                                                                      '     * 根据存储配置更新锁参数。\n'
                                                                                      '     * @param '
                                                                                      'messageStoreConfig 存储配置\n'
                                                                                      '     */'),
                                                                                     ('    /**\n'
                                                                                      '     * Locking mechanism '
                                                                                      'switching\n'
                                                                                      '     */',
                                                                                      '    /** 在自旋锁与互斥锁之间切换。 */')],
 'store/src/main/java/org/apache/rocketmq/store/lock/AdaptiveBackOffSpinLockImpl.java': [('public class '
                                                                                          'AdaptiveBackOffSpinLockImpl '
                                                                                          'implements '
                                                                                          'AdaptiveBackOffSpinLock {',
                                                                                          '/**\n'
                                                                                          ' * 自适应退避自旋锁实现：按 TPS 与退让次数在 '
                                                                                          'SpinLock 与 ReentrantLock '
                                                                                          '间动态切换。\n'
                                                                                          ' */\n'
                                                                                          'public class '
                                                                                          'AdaptiveBackOffSpinLockImpl '
                                                                                          'implements '
                                                                                          'AdaptiveBackOffSpinLock {'),
                                                                                         ('    private '
                                                                                          'AdaptiveBackOffSpinLock '
                                                                                          'adaptiveLock;',
                                                                                          '    /** 当前生效的锁实现。 */\n'
                                                                                          '    private '
                                                                                          'AdaptiveBackOffSpinLock '
                                                                                          'adaptiveLock;'),
                                                                                         ('    private AtomicBoolean '
                                                                                          'state = new '
                                                                                          'AtomicBoolean(true);',
                                                                                          '    /** 切换过程中的全局状态标志。 */\n'
                                                                                          '    private AtomicBoolean '
                                                                                          'state = new '
                                                                                          'AtomicBoolean(true);'),
                                                                                         ('    private Map<String, '
                                                                                          'AdaptiveBackOffSpinLock> '
                                                                                          'locks;',
                                                                                          '    /** 可选锁实现映射（SpinLock / '
                                                                                          'ReentrantLock）。 */\n'
                                                                                          '    private Map<String, '
                                                                                          'AdaptiveBackOffSpinLock> '
                                                                                          'locks;'),
                                                                                         ('    private final '
                                                                                          'List<AtomicInteger> '
                                                                                          'tpsTable;',
                                                                                          '    /** 双槽 TPS 计数表。 */\n'
                                                                                          '    private final '
                                                                                          'List<AtomicInteger> '
                                                                                          'tpsTable;'),
                                                                                         ('    private final '
                                                                                          'List<Set<Thread>> '
                                                                                          'threadTable;',
                                                                                          '    /** 双槽竞争线程集合。 */\n'
                                                                                          '    private final '
                                                                                          'List<Set<Thread>> '
                                                                                          'threadTable;'),
                                                                                         ('    private int '
                                                                                          'swapCriticalPoint;',
                                                                                          '    /** 自旋锁切互斥锁的 TPS 临界点。 '
                                                                                          '*/\n'
                                                                                          '    private int '
                                                                                          'swapCriticalPoint;'),
                                                                                         ('    private AtomicInteger '
                                                                                          'currentThreadNum = new '
                                                                                          'AtomicInteger(0);',
                                                                                          '    /** 当前持锁线程数。 */\n'
                                                                                          '    private AtomicInteger '
                                                                                          'currentThreadNum = new '
                                                                                          'AtomicInteger(0);'),
                                                                                         ('    private AtomicBoolean '
                                                                                          'isOpen = new '
                                                                                          'AtomicBoolean(true);',
                                                                                          '    /** 是否启用自动切换。 */\n'
                                                                                          '    private AtomicBoolean '
                                                                                          'isOpen = new '
                                                                                          'AtomicBoolean(true);'),
                                                                                         ('    public '
                                                                                          'AdaptiveBackOffSpinLockImpl() '
                                                                                          '{',
                                                                                          '    /** 初始化两种锁实现与统计表。 */\n'
                                                                                          '    public '
                                                                                          'AdaptiveBackOffSpinLockImpl() '
                                                                                          '{'),
                                                                                         ('    @Override\n'
                                                                                          '    public void lock() {',
                                                                                          '    /** 加锁并记录 TPS/线程统计。 */\n'
                                                                                          '    @Override\n'
                                                                                          '    public void lock() {'),
                                                                                         ('    @Override\n'
                                                                                          '    public void unlock() {',
                                                                                          '    /** 解锁并在启用时尝试切换锁类型。 */\n'
                                                                                          '    @Override\n'
                                                                                          '    public void unlock() {'),
                                                                                         ('    @Override\n'
                                                                                          '    public void '
                                                                                          'update(MessageStoreConfig '
                                                                                          'messageStoreConfig) {',
                                                                                          '    /** 委托当前锁实现更新配置。 */\n'
                                                                                          '    @Override\n'
                                                                                          '    public void '
                                                                                          'update(MessageStoreConfig '
                                                                                          'messageStoreConfig) {'),
                                                                                         ('    @Override\n'
                                                                                          '    public void swap() {',
                                                                                          '    /** 根据退让次数与 TPS '
                                                                                          '决定是否切换锁类型或调整自旋次数。 */\n'
                                                                                          '    @Override\n'
                                                                                          '    public void swap() {'),
                                                                                         ('    public '
                                                                                          'Collection<AdaptiveBackOffSpinLock> '
                                                                                          'getLocks() {',
                                                                                          '    /** 返回所有锁实现。 */\n'
                                                                                          '    public '
                                                                                          'Collection<AdaptiveBackOffSpinLock> '
                                                                                          'getLocks() {'),
                                                                                         ('    public boolean '
                                                                                          'getState() {',
                                                                                          '    /** 返回切换状态标志。 */\n'
                                                                                          '    public boolean '
                                                                                          'getState() {'),
                                                                                         ('    public '
                                                                                          'AdaptiveBackOffSpinLock '
                                                                                          'getAdaptiveLock() {',
                                                                                          '    /** 返回当前锁实现。 */\n'
                                                                                          '    public '
                                                                                          'AdaptiveBackOffSpinLock '
                                                                                          'getAdaptiveLock() {'),
                                                                                         ('    public boolean isOpen() '
                                                                                          '{',
                                                                                          '    /** 是否启用自动切换。 */\n'
                                                                                          '    public boolean isOpen() '
                                                                                          '{')],
 'store/src/main/java/org/apache/rocketmq/store/lock/BackOffReentrantLock.java': [('public class BackOffReentrantLock '
                                                                                   'implements AdaptiveBackOffSpinLock '
                                                                                   '{',
                                                                                   '/**\n'
                                                                                   ' * 基于 ReentrantLock '
                                                                                   '的写消息锁：高竞争场景下的互斥锁备选方案。\n'
                                                                                   ' */\n'
                                                                                   'public class BackOffReentrantLock '
                                                                                   'implements AdaptiveBackOffSpinLock '
                                                                                   '{'),
                                                                                  ('    private ReentrantLock '
                                                                                   'putMessageNormalLock = new '
                                                                                   'ReentrantLock(); // NonfairSync',
                                                                                   '    /** 非公平 ReentrantLock。 */\n'
                                                                                   '    private ReentrantLock '
                                                                                   'putMessageNormalLock = new '
                                                                                   'ReentrantLock(); // NonfairSync'),
                                                                                  ('    @Override\n'
                                                                                   '    public void lock() {',
                                                                                   '    /** 获取互斥锁。 */\n'
                                                                                   '    @Override\n'
                                                                                   '    public void lock() {'),
                                                                                  ('    @Override\n'
                                                                                   '    public void unlock() {',
                                                                                   '    /** 释放互斥锁。 */\n'
                                                                                   '    @Override\n'
                                                                                   '    public void unlock() {')],
 'store/src/main/java/org/apache/rocketmq/store/lock/BackOffSpinLock.java': [('public class BackOffSpinLock implements '
                                                                              'AdaptiveBackOffSpinLock {',
                                                                              '/**\n'
                                                                              ' * 退避自旋锁：CAS 自旋失败后 Thread.sleep(0) '
                                                                              '退让，并自适应调整自旋次数。\n'
                                                                              ' */\n'
                                                                              'public class BackOffSpinLock implements '
                                                                              'AdaptiveBackOffSpinLock {'),
                                                                             ('    private AtomicBoolean '
                                                                              'putMessageSpinLock = new '
                                                                              'AtomicBoolean(true);',
                                                                              '    /** 自旋锁 CAS 标志。 */\n'
                                                                              '    private AtomicBoolean '
                                                                              'putMessageSpinLock = new '
                                                                              'AtomicBoolean(true);'),
                                                                             ('    private int optimalDegree;',
                                                                              '    /** 当前最优自旋次数 K。 */\n'
                                                                              '    private int optimalDegree;'),
                                                                             ('    private final List<AtomicInteger> '
                                                                              'numberOfRetreat;',
                                                                              '    /** 双槽退让次数统计。 */\n'
                                                                              '    private final List<AtomicInteger> '
                                                                              'numberOfRetreat;'),
                                                                             ('    public BackOffSpinLock() {',
                                                                              '    /** 初始化自旋次数与退让计数器。 */\n'
                                                                              '    public BackOffSpinLock() {'),
                                                                             ('    @Override\n    public void lock() {',
                                                                              '    /** CAS 自旋获取锁，失败则退让并计数。 */\n'
                                                                              '    @Override\n'
                                                                              '    public void lock() {'),
                                                                             ('    @Override\n'
                                                                              '    public void unlock() {',
                                                                              '    /** 释放自旋锁。 */\n'
                                                                              '    @Override\n'
                                                                              '    public void unlock() {'),
                                                                             ('    @Override\n'
                                                                              '    public void '
                                                                              'update(MessageStoreConfig '
                                                                              'messageStoreConfig) {',
                                                                              '    /** 从配置更新最优自旋次数。 */\n'
                                                                              '    @Override\n'
                                                                              '    public void '
                                                                              'update(MessageStoreConfig '
                                                                              'messageStoreConfig) {'),
                                                                             ('    public int getOptimalDegree() {',
                                                                              '    /** 返回当前自旋次数。 */\n'
                                                                              '    public int getOptimalDegree() {'),
                                                                             ('    public boolean isAdapt() {',
                                                                              '    /** 是否仍可继续增大自旋次数。 */\n'
                                                                              '    public boolean isAdapt() {'),
                                                                             ('    public synchronized void '
                                                                              'adapt(boolean isRise) {',
                                                                              '    /** 根据竞争情况增大或减小自旋次数。 */\n'
                                                                              '    public synchronized void '
                                                                              'adapt(boolean isRise) {'),
                                                                             ('    public int getNumberOfRetreat(int '
                                                                              'pos) {',
                                                                              '    /** 返回指定槽位的退让次数。 */\n'
                                                                              '    public int getNumberOfRetreat(int '
                                                                              'pos) {')],
 'store/src/main/java/org/apache/rocketmq/store/logfile/AbstractMappedFile.java': [('public abstract class '
                                                                                    'AbstractMappedFile extends '
                                                                                    'ReferenceResource implements '
                                                                                    'MappedFile {',
                                                                                    '/**\n'
                                                                                    ' * MappedFile 抽象基类：继承引用计数资源并实现 '
                                                                                    'MappedFile 接口。\n'
                                                                                    ' */\n'
                                                                                    'public abstract class '
                                                                                    'AbstractMappedFile extends '
                                                                                    'ReferenceResource implements '
                                                                                    'MappedFile {')],
 'store/src/main/java/org/apache/rocketmq/store/logfile/MappedFile.java': [('public interface MappedFile {',
                                                                            '/**\n'
                                                                            ' * 内存映射文件接口：CommitLog/ConsumeQueue '
                                                                            '等持久化文件的读写、刷盘与生命周期管理。\n'
                                                                            ' */\n'
                                                                            'public interface MappedFile {'),
                                                                           ('    /**\n'
                                                                            '     * Returns the file name of the '
                                                                            '{@code MappedFile}.\n'
                                                                            '     *\n'
                                                                            '     * @return the file name\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回 MappedFile 的文件名。\n'
                                                                            '     *\n'
                                                                            '     * @return 文件名\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Change the file name of the {@code '
                                                                            'MappedFile}.\n'
                                                                            '     *\n'
                                                                            '     * @param fileName the new file name\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 修改 MappedFile 的文件名。\n'
                                                                            '     *\n'
                                                                            '     * @param fileName 新文件名\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the file size of the '
                                                                            '{@code MappedFile}.\n'
                                                                            '     *\n'
                                                                            '     * @return the file size\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回 MappedFile 的文件大小。\n'
                                                                            '     *\n'
                                                                            '     * @return 文件大小（字节）\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the {@code FileChannel} '
                                                                            'behind the {@code MappedFile}.\n'
                                                                            '     *\n'
                                                                            '     * @return the file channel\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回 MappedFile 底层的 FileChannel。\n'
                                                                            '     *\n'
                                                                            '     * @return 文件通道\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns true if this {@code '
                                                                            'MappedFile} is full and no new messages '
                                                                            'can be added.\n'
                                                                            '     *\n'
                                                                            '     * @return true if the file is full\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 文件是否已满且无法再追加消息。\n'
                                                                            '     *\n'
                                                                            '     * @return 已满返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns true if this {@code '
                                                                            'MappedFile} is available.\n'
                                                                            '     * <p>\n'
                                                                            '     * The mapped file will be not '
                                                                            "available if it's shutdown or destroyed.\n"
                                                                            '     *\n'
                                                                            '     * @return true if the file is '
                                                                            'available\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 文件是否仍可用（未 shutdown 或 destroy）。\n'
                                                                            '     *\n'
                                                                            '     * @return 可用返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Appends a message object to the '
                                                                            'current {@code MappedFile} with a '
                                                                            'specific call back.\n'
                                                                            '     *\n'
                                                                            '     * @param message a message to '
                                                                            'append\n'
                                                                            '     * @param messageCallback the '
                                                                            'specific call back to execute the real '
                                                                            'append action\n'
                                                                            '     * @param putMessageContext\n'
                                                                            '     * @return the append result\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 通过回调将单条消息追加到当前 MappedFile。\n'
                                                                            '     *\n'
                                                                            '     * @param message 待追加消息\n'
                                                                            '     * @param messageCallback 执行实际写入的回调\n'
                                                                            '     * @param putMessageContext 写消息上下文\n'
                                                                            '     * @return 追加结果\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Appends a batch message object to '
                                                                            'the current {@code MappedFile} with a '
                                                                            'specific call back.\n'
                                                                            '     *\n'
                                                                            '     * @param message a message to '
                                                                            'append\n'
                                                                            '     * @param messageCallback the '
                                                                            'specific call back to execute the real '
                                                                            'append action\n'
                                                                            '     * @param putMessageContext\n'
                                                                            '     * @return the append result\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 通过回调将批量消息追加到当前 MappedFile。\n'
                                                                            '     *\n'
                                                                            '     * @param message 批量消息\n'
                                                                            '     * @param messageCallback 执行实际写入的回调\n'
                                                                            '     * @param putMessageContext 写消息上下文\n'
                                                                            '     * @return 追加结果\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Appends a raw message data '
                                                                            'represents by a byte array to the current '
                                                                            '{@code MappedFile}.\n'
                                                                            '     * Using mappedByteBuffer\n'
                                                                            '     *\n'
                                                                            '     * @param data the byte array to '
                                                                            'append\n'
                                                                            '     * @return true if success; false '
                                                                            'otherwise.\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 通过 MappedByteBuffer 追加字节数组原始消息数据。\n'
                                                                            '     *\n'
                                                                            '     * @param data 待追加字节数组\n'
                                                                            '     * @return 成功返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Appends a raw message data '
                                                                            'represents by a byte array to the current '
                                                                            '{@code MappedFile}.\n'
                                                                            '     * Using fileChannel\n'
                                                                            '     *\n'
                                                                            '     * @param data the byte array to '
                                                                            'append\n'
                                                                            '     * @return true if success; false '
                                                                            'otherwise.\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 通过 FileChannel 追加字节数组原始消息数据。\n'
                                                                            '     *\n'
                                                                            '     * @param data 待追加字节数组\n'
                                                                            '     * @return 成功返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Appends a raw message data '
                                                                            'represents by a byte array to the current '
                                                                            '{@code MappedFile}.\n'
                                                                            '     *\n'
                                                                            '     * @param data the byte buffer to '
                                                                            'append\n'
                                                                            '     * @return true if success; false '
                                                                            'otherwise.\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 追加 ByteBuffer 原始消息数据。\n'
                                                                            '     *\n'
                                                                            '     * @param data 待追加缓冲区\n'
                                                                            '     * @return 成功返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Appends a raw message data '
                                                                            'represents by a byte array to the current '
                                                                            '{@code MappedFile},\n'
                                                                            '     * starting at the given offset in '
                                                                            'the array.\n'
                                                                            '     *\n'
                                                                            '     * @param data the byte array to '
                                                                            'append\n'
                                                                            '     * @param offset the offset within '
                                                                            'the array of the first byte to be read\n'
                                                                            '     * @param length the number of bytes '
                                                                            'to be read from the given array\n'
                                                                            '     * @return true if success; false '
                                                                            'otherwise.\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 从字节数组指定偏移处追加一段原始消息数据。\n'
                                                                            '     *\n'
                                                                            '     * @param data 字节数组\n'
                                                                            '     * @param offset 起始偏移\n'
                                                                            '     * @param length 读取长度\n'
                                                                            '     * @return 成功返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the global offset of the '
                                                                            "current {code MappedFile}, it's a long "
                                                                            'value of the file name.\n'
                                                                            '     *\n'
                                                                            '     * @return the offset of this file\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回当前文件的全局起始偏移（通常由文件名解析）。\n'
                                                                            '     *\n'
                                                                            '     * @return 文件起始偏移\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Flushes the data in cache to disk '
                                                                            'immediately.\n'
                                                                            '     *\n'
                                                                            '     * @param flushLeastPages the least '
                                                                            'pages to flush\n'
                                                                            '     * @return the flushed position after '
                                                                            'the method call\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 将缓存数据刷入磁盘。\n'
                                                                            '     *\n'
                                                                            '     * @param flushLeastPages 最少刷盘页数\n'
                                                                            '     * @return 刷盘后的位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Flushes the data in the secondary '
                                                                            'cache to page cache or disk immediately.\n'
                                                                            '     *\n'
                                                                            '     * @param commitLeastPages the least '
                                                                            'pages to commit\n'
                                                                            '     * @return the committed position '
                                                                            'after the method call\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 将二级缓存提交到页缓存或磁盘（TransientStorePool '
                                                                            '场景）。\n'
                                                                            '     *\n'
                                                                            '     * @param commitLeastPages 最少提交页数\n'
                                                                            '     * @return 提交后的位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Selects a slice of the mapped byte '
                                                                            "buffer's sub-region behind the mapped "
                                                                            'file,\n'
                                                                            '     * starting at the given position.\n'
                                                                            '     *\n'
                                                                            '     * @param pos the given position\n'
                                                                            '     * @param size the size of the '
                                                                            'returned sub-region\n'
                                                                            '     * @return a {@code '
                                                                            'SelectMappedBufferResult} instance '
                                                                            'contains the selected slice\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 从指定位置选取 MappedByteBuffer 子区域。\n'
                                                                            '     *\n'
                                                                            '     * @param pos 起始位置\n'
                                                                            '     * @param size 子区域大小\n'
                                                                            '     * @return 包含选中切片的 '
                                                                            'SelectMappedBufferResult\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Selects a slice of the mapped byte '
                                                                            "buffer's sub-region behind the mapped "
                                                                            'file,\n'
                                                                            '     * starting at the given position.\n'
                                                                            '     *\n'
                                                                            '     * @param pos the given position\n'
                                                                            '     * @return a {@code '
                                                                            'SelectMappedBufferResult} instance '
                                                                            'contains the selected slice\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 从指定位置选取 MappedByteBuffer '
                                                                            '子区域至文件末尾。\n'
                                                                            '     *\n'
                                                                            '     * @param pos 起始位置\n'
                                                                            '     * @return 包含选中切片的 '
                                                                            'SelectMappedBufferResult\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the mapped byte buffer '
                                                                            'behind the mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @return the mapped byte buffer\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回底层 MappedByteBuffer。\n'
                                                                            '     *\n'
                                                                            '     * @return 映射缓冲区\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns a slice of the mapped byte '
                                                                            'buffer behind the mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @return the slice of the mapped '
                                                                            'byte buffer\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回 MappedByteBuffer 的 slice 视图。\n'
                                                                            '     *\n'
                                                                            '     * @return 缓冲区切片\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the store timestamp of the '
                                                                            'last message.\n'
                                                                            '     *\n'
                                                                            '     * @return the store timestamp\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回最后一条消息的存储时间戳。\n'
                                                                            '     *\n'
                                                                            '     * @return 存储时间戳\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the last modified '
                                                                            'timestamp of the file.\n'
                                                                            '     *\n'
                                                                            '     * @return the last modified '
                                                                            'timestamp\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回文件最后修改时间戳。\n'
                                                                            '     *\n'
                                                                            '     * @return 最后修改时间\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Get data from a certain pos offset '
                                                                            'with size byte\n'
                                                                            '     *\n'
                                                                            '     * @param pos a certain pos offset to '
                                                                            'get data\n'
                                                                            '     * @param size the size of data\n'
                                                                            '     * @param byteBuffer the data\n'
                                                                            '     * @return true if with data; false '
                                                                            'if no data;\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 从指定偏移读取指定长度数据到 ByteBuffer。\n'
                                                                            '     *\n'
                                                                            '     * @param pos 起始偏移\n'
                                                                            '     * @param size 数据长度\n'
                                                                            '     * @param byteBuffer 目标缓冲区\n'
                                                                            '     * @return 有数据返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Destroys the file and delete it '
                                                                            'from the file system.\n'
                                                                            '     *\n'
                                                                            '     * @param intervalForcibly The time '
                                                                            'interval in milliseconds after which any '
                                                                            'remaining references will be forcibly '
                                                                            'released during destroy\n'
                                                                            '     * @return true if success; false '
                                                                            'otherwise.\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 销毁文件并从文件系统删除。\n'
                                                                            '     *\n'
                                                                            '     * @param intervalForcibly '
                                                                            '强制释放剩余引用的等待毫秒数\n'
                                                                            '     * @return 成功返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Shutdowns the file and mark it '
                                                                            'unavailable.\n'
                                                                            '     *\n'
                                                                            '     * @param intervalForcibly The time '
                                                                            'interval in milliseconds after which any '
                                                                            'remaining references will be forcibly '
                                                                            'released during shutdown\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 关闭文件并标记为不可用。\n'
                                                                            '     *\n'
                                                                            '     * @param intervalForcibly '
                                                                            '强制释放剩余引用的等待毫秒数\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Decreases the reference count by '
                                                                            '{@code 1} and clean up the mapped file if '
                                                                            'the reference count reaches at\n'
                                                                            '     * {@code 0}.\n'
                                                                            '     */',
                                                                            '    /** 引用计数减 1，归零时清理 MappedFile。 */'),
                                                                           ('    /**\n'
                                                                            '     * Increases the reference count by '
                                                                            '{@code 1}.\n'
                                                                            '     *\n'
                                                                            '     * @return true if success; false '
                                                                            'otherwise.\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 引用计数加 1。\n'
                                                                            '     *\n'
                                                                            '     * @return 成功返回 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns true if the current file '
                                                                            'is first mapped file of some consume '
                                                                            'queue.\n'
                                                                            '     *\n'
                                                                            '     * @return true or false\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 当前文件是否为某消费队列的首个 MappedFile。\n'
                                                                            '     *\n'
                                                                            '     * @return 是则 true\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Sets the flag whether the current '
                                                                            'file is first mapped file of some consume '
                                                                            'queue.\n'
                                                                            '     *\n'
                                                                            '     * @param firstCreateInQueue true or '
                                                                            'false\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 设置是否为消费队列首个 MappedFile。\n'
                                                                            '     *\n'
                                                                            '     * @param firstCreateInQueue 标志值\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the flushed position of '
                                                                            'this mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @return the flushed posotion\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回已刷盘位置。\n'
                                                                            '     *\n'
                                                                            '     * @return 刷盘位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Sets the flushed position of this '
                                                                            'mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @param flushedPosition the '
                                                                            'specific flushed position\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 设置已刷盘位置。\n'
                                                                            '     *\n'
                                                                            '     * @param flushedPosition 刷盘位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the wrote position of this '
                                                                            'mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @return the wrote position\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回已写入位置。\n'
                                                                            '     *\n'
                                                                            '     * @return 写入位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Sets the wrote position of this '
                                                                            'mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @param wrotePosition the specific '
                                                                            'wrote position\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 设置已写入位置。\n'
                                                                            '     *\n'
                                                                            '     * @param wrotePosition 写入位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Returns the current max readable '
                                                                            'position of this mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @return the max readable position\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回当前最大可读位置。\n'
                                                                            '     *\n'
                                                                            '     * @return 可读位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Sets the committed position of '
                                                                            'this mapped file.\n'
                                                                            '     *\n'
                                                                            '     * @param committedPosition the '
                                                                            'specific committed position\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 设置已提交位置。\n'
                                                                            '     *\n'
                                                                            '     * @param committedPosition 提交位置\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Lock the mapped bytebuffer\n'
                                                                            '     */',
                                                                            '    /** 锁定 MappedByteBuffer（mlock）。 */'),
                                                                           ('    /**\n'
                                                                            '     * Unlock the mapped bytebuffer\n'
                                                                            '     */',
                                                                            '    /** 解锁 MappedByteBuffer（munlock）。 */'),
                                                                           ('    /**\n'
                                                                            '     * Warm up the mapped bytebuffer\n'
                                                                            '     * @param type\n'
                                                                            '     * @param pages\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 预热 MappedByteBuffer 页。\n'
                                                                            '     * @param type 刷盘类型\n'
                                                                            '     * @param pages 预热页数\n'
                                                                            '     */'),
                                                                           ('    /**\n     * Swap map\n     */',
                                                                            '    /** 交换内存映射（swapMap）。 */'),
                                                                           ('    /**\n     * Clean pageTable\n     */',
                                                                            '    /** 清理已换出的页表映射。 */'),
                                                                           ('    /**\n'
                                                                            '     * Get recent swap map time\n'
                                                                            '     */',
                                                                            '    /** 返回最近一次 swapMap 时间戳。 */'),
                                                                           ('    /**\n'
                                                                            '     * Get recent MappedByteBuffer access '
                                                                            'count since last swap\n'
                                                                            '     */',
                                                                            '    /** 返回自上次 swap 以来的 MappedByteBuffer '
                                                                            '访问次数。 */'),
                                                                           ('    /**\n'
                                                                            '     * Get the underlying file\n'
                                                                            '     * @return\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回底层 File 对象。\n'
                                                                            '     * @return 文件\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * rename file to add ".delete" '
                                                                            'suffix\n'
                                                                            '     */',
                                                                            '    /** 重命名文件并追加 .delete 后缀。 */'),
                                                                           ('    /**\n'
                                                                            '     * move the file to the parent '
                                                                            'directory\n'
                                                                            '     * @throws IOException\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 将文件移动到父目录。\n'
                                                                            '     * @throws IOException IO 异常\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Get the last flush time\n'
                                                                            '     * @return\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 返回最后一次刷盘时间。\n'
                                                                            '     * @return 刷盘时间\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Init mapped file\n'
                                                                            '     * @param fileName file name\n'
                                                                            '     * @param fileSize file size\n'
                                                                            '     * @param transientStorePool '
                                                                            'transient store pool\n'
                                                                            '     * @throws IOException\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 初始化 MappedFile。\n'
                                                                            '     * @param fileName 文件名\n'
                                                                            '     * @param fileSize 文件大小\n'
                                                                            '     * @param transientStorePool 瞬态存储池\n'
                                                                            '     * @throws IOException IO 异常\n'
                                                                            '     */'),
                                                                           ('    /**\n'
                                                                            '     * Check mapped file is loaded to '
                                                                            'memory with given position and size\n'
                                                                            '     * @param position start offset of '
                                                                            'data\n'
                                                                            '     * @param size data size\n'
                                                                            '     * @return data is resided in memory '
                                                                            'or not\n'
                                                                            '     */',
                                                                            '    /**\n'
                                                                            '     * 检查指定范围数据是否已加载到内存。\n'
                                                                            '     * @param position 起始偏移\n'
                                                                            '     * @param size 数据大小\n'
                                                                            '     * @return 在内存中返回 true\n'
                                                                            '     */')],
 'store/src/main/java/org/apache/rocketmq/store/logfile/SharedByteBufferManager.java': [('/**\n'
                                                                                         ' * Shared byte buffer '
                                                                                         'manager for managing some '
                                                                                         'shared ByteBuffers Buffer '
                                                                                         'size is set based on '
                                                                                         "MessageStoreConfig's\n"
                                                                                         ' * maxMessageSize\n'
                                                                                         ' */\n'
                                                                                         'public class '
                                                                                         'SharedByteBufferManager {',
                                                                                         '/**\n'
                                                                                         ' * 共享 ByteBuffer 管理器：按 '
                                                                                         'maxMessageSize 分配 '
                                                                                         'DirectByteBuffer 池供并发借用。\n'
                                                                                         ' */\n'
                                                                                         'public class '
                                                                                         'SharedByteBufferManager {'),
                                                                                        ('    private static volatile '
                                                                                         'SharedByteBufferManager '
                                                                                         'instance;',
                                                                                         '    /** 单例实例。 */\n'
                                                                                         '    private static volatile '
                                                                                         'SharedByteBufferManager '
                                                                                         'instance;'),
                                                                                        ('    private '
                                                                                         'SharedByteBuffer[] '
                                                                                         'sharedByteBuffers;',
                                                                                         '    /** 共享缓冲区数组。 */\n'
                                                                                         '    private '
                                                                                         'SharedByteBuffer[] '
                                                                                         'sharedByteBuffers;'),
                                                                                        ('    /**\n'
                                                                                         '     * Get singleton '
                                                                                         'instance\n'
                                                                                         '     */',
                                                                                         '    /** 获取单例实例。 */'),
                                                                                        ('    /**\n'
                                                                                         '     * Initialize shared '
                                                                                         'buffers with specified '
                                                                                         'messageSize size and shared '
                                                                                         'buffer number\n'
                                                                                         '     *\n'
                                                                                         '     * @param maxMessageSize '
                                                                                         'max messageSize size\n'
                                                                                         '     * @param '
                                                                                         'sharedBufferNum number of '
                                                                                         'shared buffers\n'
                                                                                         '     */',
                                                                                         '    /**\n'
                                                                                         '     * 初始化共享缓冲区池。\n'
                                                                                         '     *\n'
                                                                                         '     * @param maxMessageSize '
                                                                                         '最大消息体大小\n'
                                                                                         '     * @param '
                                                                                         'sharedBufferNum 共享缓冲区数量\n'
                                                                                         '     */'),
                                                                                        ('    /**\n'
                                                                                         '     * Borrow a shared '
                                                                                         'buffer\n'
                                                                                         '     *\n'
                                                                                         '     * @return Shared '
                                                                                         'buffer\n'
                                                                                         '     */',
                                                                                         '    /**\n'
                                                                                         '     * 随机借用一块共享缓冲区。\n'
                                                                                         '     *\n'
                                                                                         '     * @return 共享缓冲区包装\n'
                                                                                         '     */'),
                                                                                        ('    /**\n'
                                                                                         '     * Get current buffer '
                                                                                         'size\n'
                                                                                         '     *\n'
                                                                                         '     * @return Buffer size\n'
                                                                                         '     */',
                                                                                         '    /**\n'
                                                                                         '     * 返回缓冲区大小。\n'
                                                                                         '     *\n'
                                                                                         '     * @return 字节数\n'
                                                                                         '     */'),
                                                                                        ('    /**\n'
                                                                                         '     * Check if initialized\n'
                                                                                         '     *\n'
                                                                                         '     * @return Whether '
                                                                                         'initialized\n'
                                                                                         '     */',
                                                                                         '    /**\n'
                                                                                         '     * 是否已初始化。\n'
                                                                                         '     *\n'
                                                                                         '     * @return 已初始化返回 true\n'
                                                                                         '     */'),
                                                                                        ('    /**\n'
                                                                                         '     * Shared byte buffer '
                                                                                         'class\n'
                                                                                         '     */',
                                                                                         '    /** 带 ReentrantLock 的共享 '
                                                                                         'DirectByteBuffer 包装。 */'),
                                                                                        ('        public '
                                                                                         'SharedByteBuffer(int size) {',
                                                                                         '        /** 分配指定大小的 '
                                                                                         'DirectByteBuffer。 */\n'
                                                                                         '        public '
                                                                                         'SharedByteBuffer(int size) '
                                                                                         '{'),
                                                                                        ('        public void '
                                                                                         'release() {',
                                                                                         '        /** 释放锁。 */\n'
                                                                                         '        public void '
                                                                                         'release() {'),
                                                                                        ('        public ByteBuffer '
                                                                                         'acquire() {',
                                                                                         '        /** 获取锁并返回缓冲区。 */\n'
                                                                                         '        public ByteBuffer '
                                                                                         'acquire() {')],
 'store/src/main/java/org/apache/rocketmq/store/metrics/DefaultStoreMetricsConstant.java': [('public class '
                                                                                             'DefaultStoreMetricsConstant '
                                                                                             '{',
                                                                                             '/**\n'
                                                                                             ' * '
                                                                                             '默认存储指标常量：OpenTelemetry '
                                                                                             '指标名与标签键定义。\n'
                                                                                             ' */\n'
                                                                                             'public class '
                                                                                             'DefaultStoreMetricsConstant '
                                                                                             '{'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_SIZE = '
                                                                                             '"rocketmq_storage_size";',
                                                                                             '    /** 存储占用字节数 Gauge。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_SIZE = '
                                                                                             '"rocketmq_storage_size";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_FLUSH_BEHIND '
                                                                                             '= '
                                                                                             '"rocketmq_storage_flush_behind_bytes";',
                                                                                             '    /** 刷盘落后字节数 Gauge。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_FLUSH_BEHIND '
                                                                                             '= '
                                                                                             '"rocketmq_storage_flush_behind_bytes";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_DISPATCH_BEHIND '
                                                                                             '= '
                                                                                             '"rocketmq_storage_dispatch_behind_bytes";',
                                                                                             '    /** 分发落后字节数 Gauge。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_DISPATCH_BEHIND '
                                                                                             '= '
                                                                                             '"rocketmq_storage_dispatch_behind_bytes";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_MESSAGE_RESERVE_TIME '
                                                                                             '= '
                                                                                             '"rocketmq_storage_message_reserve_time";',
                                                                                             '    /** 最早消息保留时长 Gauge。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_STORAGE_MESSAGE_RESERVE_TIME '
                                                                                             '= '
                                                                                             '"rocketmq_storage_message_reserve_time";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_TIMER_ENQUEUE_LAG '
                                                                                             '= '
                                                                                             '"rocketmq_timer_enqueue_lag";',
                                                                                             '    /** 定时消息入队滞后条数。 */\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_TIMER_ENQUEUE_LAG '
                                                                                             '= '
                                                                                             '"rocketmq_timer_enqueue_lag";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'COUNTER_TIMER_ENQUEUE_TOTAL '
                                                                                             '= '
                                                                                             '"rocketmq_timer_enqueue_total";',
                                                                                             '    /** 定时消息入队累计计数。 */\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'COUNTER_TIMER_ENQUEUE_TOTAL '
                                                                                             '= '
                                                                                             '"rocketmq_timer_enqueue_total";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'COUNTER_TIMER_DEQUEUE_TOTAL '
                                                                                             '= '
                                                                                             '"rocketmq_timer_dequeue_total";',
                                                                                             '    /** 定时消息出队累计计数。 */\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'COUNTER_TIMER_DEQUEUE_TOTAL '
                                                                                             '= '
                                                                                             '"rocketmq_timer_dequeue_total";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'HISTOGRAM_DELAY_MSG_LATENCY '
                                                                                             '= '
                                                                                             '"rocketmq_delay_message_latency";',
                                                                                             '    /** 延迟消息时延直方图。 */\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'HISTOGRAM_DELAY_MSG_LATENCY '
                                                                                             '= '
                                                                                             '"rocketmq_delay_message_latency";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'LABEL_STORAGE_TYPE = '
                                                                                             '"storage_type";',
                                                                                             '    /** 存储类型标签键。 */\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'LABEL_STORAGE_TYPE = '
                                                                                             '"storage_type";'),
                                                                                            ('    public static final '
                                                                                             'String LABEL_TOPIC = '
                                                                                             '"topic";',
                                                                                             '    /** Topic 标签键。 */\n'
                                                                                             '    public static final '
                                                                                             'String LABEL_TOPIC = '
                                                                                             '"topic";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_BYTES_ROCKSDB_WRITTEN '
                                                                                             '= '
                                                                                             '"rocketmq_rocksdb_bytes_written";',
                                                                                             '    /** RocksDB 累计写入字节数。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_BYTES_ROCKSDB_WRITTEN '
                                                                                             '= '
                                                                                             '"rocketmq_rocksdb_bytes_written";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_BYTES_ROCKSDB_READ '
                                                                                             '= '
                                                                                             '"rocketmq_rocksdb_bytes_read";',
                                                                                             '    /** RocksDB 累计读取字节数。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_BYTES_ROCKSDB_READ '
                                                                                             '= '
                                                                                             '"rocketmq_rocksdb_bytes_read";'),
                                                                                            ('    public static final '
                                                                                             'String '
                                                                                             'GAUGE_RATE_ROCKSDB_CACHE_HIT '
                                                                                             '= '
                                                                                             '"rocketmq_rocksdb_rate_cache_hit";',
                                                                                             '    /** RocksDB 块缓存命中率。 '
                                                                                             '*/\n'
                                                                                             '    public static final '
                                                                                             'String '
                                                                                             'GAUGE_RATE_ROCKSDB_CACHE_HIT '
                                                                                             '= '
                                                                                             '"rocketmq_rocksdb_rate_cache_hit";')],
 'store/src/main/java/org/apache/rocketmq/store/metrics/DefaultStoreMetricsManager.java': [('public class '
                                                                                            'DefaultStoreMetricsManager '
                                                                                            'implements '
                                                                                            'StoreMetricsManager {',
                                                                                            '/**\n'
                                                                                            ' * 默认存储指标管理器：注册 '
                                                                                            'OpenTelemetry '
                                                                                            'Gauge/Counter/Histogram '
                                                                                            '并采集存储与时间轮指标。\n'
                                                                                            ' */\n'
                                                                                            'public class '
                                                                                            'DefaultStoreMetricsManager '
                                                                                            'implements '
                                                                                            'StoreMetricsManager {'),
                                                                                           ('    private '
                                                                                            'Supplier<AttributesBuilder> '
                                                                                            'attributesBuilderSupplier;',
                                                                                            '    /** 指标属性构建器供应者。 */\n'
                                                                                            '    private '
                                                                                            'Supplier<AttributesBuilder> '
                                                                                            'attributesBuilderSupplier;'),
                                                                                           ('    private '
                                                                                            'MessageStoreConfig '
                                                                                            'messageStoreConfig;',
                                                                                            '    /** 存储配置。 */\n'
                                                                                            '    private '
                                                                                            'MessageStoreConfig '
                                                                                            'messageStoreConfig;'),
                                                                                           ('    private '
                                                                                            'RocksDBStoreMetricsManager '
                                                                                            'rocksDBStoreMetricsManager;',
                                                                                            '    /** RocksDB 指标子管理器。 '
                                                                                            '*/\n'
                                                                                            '    private '
                                                                                            'RocksDBStoreMetricsManager '
                                                                                            'rocksDBStoreMetricsManager;'),
                                                                                           ('    public '
                                                                                            'DefaultStoreMetricsManager() '
                                                                                            '{',
                                                                                            '    /** 构造并初始化 RocksDB '
                                                                                            '指标管理器。 */\n'
                                                                                            '    public '
                                                                                            'DefaultStoreMetricsManager() '
                                                                                            '{'),
                                                                                           ('    public '
                                                                                            'List<Pair<InstrumentSelector, '
                                                                                            'ViewBuilder>> '
                                                                                            'getMetricsView() {',
                                                                                            '    /** 返回延迟消息时延直方图的 View '
                                                                                            '配置。 */\n'
                                                                                            '    public '
                                                                                            'List<Pair<InstrumentSelector, '
                                                                                            'ViewBuilder>> '
                                                                                            'getMetricsView() {'),
                                                                                           ('    public void '
                                                                                            'init(Meter meter, '
                                                                                            'Supplier<AttributesBuilder> '
                                                                                            'attributesBuilderSupplier,\n'
                                                                                            '        MessageStore '
                                                                                            'messageStore) {',
                                                                                            '    /** 注册存储、时间轮及 RocksDB '
                                                                                            '相关 OpenTelemetry 指标。 */\n'
                                                                                            '    public void '
                                                                                            'init(Meter meter, '
                                                                                            'Supplier<AttributesBuilder> '
                                                                                            'attributesBuilderSupplier,\n'
                                                                                            '        MessageStore '
                                                                                            'messageStore) {'),
                                                                                           ('    public void '
                                                                                            'incTimerDequeueCount(String '
                                                                                            'topic) {',
                                                                                            '    /** 定时消息出队计数加 1。 */\n'
                                                                                            '    public void '
                                                                                            'incTimerDequeueCount(String '
                                                                                            'topic) {'),
                                                                                           ('    public void '
                                                                                            'incTimerEnqueueCount(String '
                                                                                            'topic) {',
                                                                                            '    /** 定时消息入队计数加 1。 */\n'
                                                                                            '    public void '
                                                                                            'incTimerEnqueueCount(String '
                                                                                            'topic) {'),
                                                                                           ('    public '
                                                                                            'AttributesBuilder '
                                                                                            'newAttributesBuilder() {',
                                                                                            '    /** '
                                                                                            '创建带默认存储类型/介质标签的属性构建器。 */\n'
                                                                                            '    public '
                                                                                            'AttributesBuilder '
                                                                                            'newAttributesBuilder() {'),
                                                                                           ('    public '
                                                                                            'RocksDBStoreMetricsManager '
                                                                                            'getRocksDBStoreMetricsManager() '
                                                                                            '{',
                                                                                            '    /** 返回 RocksDB 指标管理器。 '
                                                                                            '*/\n'
                                                                                            '    public '
                                                                                            'RocksDBStoreMetricsManager '
                                                                                            'getRocksDBStoreMetricsManager() '
                                                                                            '{')],
 'store/src/main/java/org/apache/rocketmq/store/metrics/RocksDBStoreMetricsManager.java': [('public class '
                                                                                            'RocksDBStoreMetricsManager '
                                                                                            '{',
                                                                                            '/**\n'
                                                                                            ' * RocksDB '
                                                                                            '消费队列指标管理器：采集读写、压缩与缓存命中率等 '
                                                                                            'Ticker 统计。\n'
                                                                                            ' */\n'
                                                                                            'public class '
                                                                                            'RocksDBStoreMetricsManager '
                                                                                            '{'),
                                                                                           ('    // The cumulative '
                                                                                            'number of bytes read from '
                                                                                            'the database.\n'
                                                                                            '    private '
                                                                                            'ObservableLongGauge '
                                                                                            'bytesRocksdbRead = new '
                                                                                            'NopObservableLongGauge();',
                                                                                            '    /** 累计从数据库读取的字节数 '
                                                                                            'Gauge。 */\n'
                                                                                            '    private '
                                                                                            'ObservableLongGauge '
                                                                                            'bytesRocksdbRead = new '
                                                                                            'NopObservableLongGauge();'),
                                                                                           ('    // The cumulative '
                                                                                            'number of bytes written '
                                                                                            'to the database.\n'
                                                                                            '    private '
                                                                                            'ObservableLongGauge '
                                                                                            'bytesRocksdbWritten = new '
                                                                                            'NopObservableLongGauge();',
                                                                                            '    /** 累计写入数据库的字节数 '
                                                                                            'Gauge。 */\n'
                                                                                            '    private '
                                                                                            'ObservableLongGauge '
                                                                                            'bytesRocksdbWritten = new '
                                                                                            'NopObservableLongGauge();'),
                                                                                           ('    // The cumulative '
                                                                                            'number of read operations '
                                                                                            'performed.\n'
                                                                                            '    private '
                                                                                            'ObservableLongGauge '
                                                                                            'timesRocksdbRead = new '
                                                                                            'NopObservableLongGauge();',
                                                                                            '    /** 累计读操作次数 Gauge。 '
                                                                                            '*/\n'
                                                                                            '    private '
                                                                                            'ObservableLongGauge '
                                                                                            'timesRocksdbRead = new '
                                                                                            'NopObservableLongGauge();'),
                                                                                           ('    // The rate at which '
                                                                                            'cache lookups were served '
                                                                                            'from the cache rather '
                                                                                            'than needing to be '
                                                                                            'fetched from disk.\n'
                                                                                            '    private '
                                                                                            'ObservableDoubleGauge '
                                                                                            'rocksdbCacheHitRate = new '
                                                                                            'NopObservableDoubleGauge();',
                                                                                            '    /** 块缓存命中率 Gauge。 */\n'
                                                                                            '    private '
                                                                                            'ObservableDoubleGauge '
                                                                                            'rocksdbCacheHitRate = new '
                                                                                            'NopObservableDoubleGauge();'),
                                                                                           ('    public '
                                                                                            'List<Pair<InstrumentSelector, '
                                                                                            'ViewBuilder>> '
                                                                                            'getMetricsView() {',
                                                                                            '    /** 返回空 View '
                                                                                            '列表（RocksDB 指标使用默认聚合）。 */\n'
                                                                                            '    public '
                                                                                            'List<Pair<InstrumentSelector, '
                                                                                            'ViewBuilder>> '
                                                                                            'getMetricsView() {'),
                                                                                           ('    public void '
                                                                                            'init(Meter meter, '
                                                                                            'Supplier<AttributesBuilder> '
                                                                                            'attributesBuilderSupplier,\n'
                                                                                            '        '
                                                                                            'ConsumeQueueStoreInterface '
                                                                                            'consumeQueueStore) {',
                                                                                            '    /** 为 RocksDB 消费队列注册 '
                                                                                            'OpenTelemetry Gauge 回调。 '
                                                                                            '*/\n'
                                                                                            '    public void '
                                                                                            'init(Meter meter, '
                                                                                            'Supplier<AttributesBuilder> '
                                                                                            'attributesBuilderSupplier,\n'
                                                                                            '        '
                                                                                            'ConsumeQueueStoreInterface '
                                                                                            'consumeQueueStore) {'),
                                                                                           ('    public '
                                                                                            'AttributesBuilder '
                                                                                            'newAttributesBuilder() {',
                                                                                            '    /** 创建带默认存储标签的属性构建器。 '
                                                                                            '*/\n'
                                                                                            '    public '
                                                                                            'AttributesBuilder '
                                                                                            'newAttributesBuilder() '
                                                                                            '{')],
 'store/src/main/java/org/apache/rocketmq/store/metrics/StoreMetricsManager.java': [('/**\n'
                                                                                     ' * Store metrics manager '
                                                                                     'interface for different message '
                                                                                     'store implementations.\n'
                                                                                     ' * This interface provides a '
                                                                                     'unified way to access metrics '
                                                                                     'functionality\n'
                                                                                     ' * regardless of the underlying '
                                                                                     'message store type.\n'
                                                                                     ' */\n'
                                                                                     'public interface '
                                                                                     'StoreMetricsManager {',
                                                                                     '/**\n'
                                                                                     ' * 存储指标管理器接口：为不同 MessageStore '
                                                                                     '实现提供统一的 OpenTelemetry 指标接入。\n'
                                                                                     ' */\n'
                                                                                     'public interface '
                                                                                     'StoreMetricsManager {'),
                                                                                    ('    /**\n'
                                                                                     '     * Initialize metrics with '
                                                                                     'the given meter and attributes '
                                                                                     'builder supplier.\n'
                                                                                     '     *\n'
                                                                                     '     * @param '
                                                                                     'meter                     '
                                                                                     'OpenTelemetry meter\n'
                                                                                     '     * @param '
                                                                                     'attributesBuilderSupplier '
                                                                                     'Metrics attributes builder '
                                                                                     'supplier\n'
                                                                                     '     * @param '
                                                                                     'messageStore             The '
                                                                                     'message store instance\n'
                                                                                     '     */',
                                                                                     '    /**\n'
                                                                                     '     * 初始化指标采集。\n'
                                                                                     '     *\n'
                                                                                     '     * @param meter '
                                                                                     'OpenTelemetry Meter\n'
                                                                                     '     * @param '
                                                                                     'attributesBuilderSupplier '
                                                                                     '指标属性构建器供应者\n'
                                                                                     '     * @param messageStore '
                                                                                     'MessageStore 实例\n'
                                                                                     '     */'),
                                                                                    ('    /**\n'
                                                                                     '     * Get metrics view '
                                                                                     'configuration.\n'
                                                                                     '     *\n'
                                                                                     '     * @return List of '
                                                                                     'instrument selector and view '
                                                                                     'builder pairs\n'
                                                                                     '     */',
                                                                                     '    /**\n'
                                                                                     '     * 获取指标 View 配置。\n'
                                                                                     '     *\n'
                                                                                     '     * @return '
                                                                                     'InstrumentSelector 与 ViewBuilder '
                                                                                     '对列表\n'
                                                                                     '     */')],
 'store/src/main/java/org/apache/rocketmq/store/plugin/MessageStoreFactory.java': [('public final class '
                                                                                    'MessageStoreFactory {',
                                                                                    '/**\n'
                                                                                    ' * MessageStore 插件工厂：按 Broker '
                                                                                    '配置反射链式包装 '
                                                                                    'AbstractPluginMessageStore。\n'
                                                                                    ' */\n'
                                                                                    'public final class '
                                                                                    'MessageStoreFactory {'),
                                                                                   ('    public static MessageStore '
                                                                                    'build(MessageStorePluginContext '
                                                                                    'context,\n'
                                                                                    '        MessageStore '
                                                                                    'messageStore) throws IOException '
                                                                                    '{',
                                                                                    '    /**\n'
                                                                                    '     * 构建带插件链的 MessageStore。\n'
                                                                                    '     *\n'
                                                                                    '     * @param context 插件上下文\n'
                                                                                    '     * @param messageStore 原始 '
                                                                                    'MessageStore\n'
                                                                                    '     * @return 包装后的 MessageStore\n'
                                                                                    '     */\n'
                                                                                    '    public static MessageStore '
                                                                                    'build(MessageStorePluginContext '
                                                                                    'context,\n'
                                                                                    '        MessageStore '
                                                                                    'messageStore) throws IOException '
                                                                                    '{')]}
