"""Chinese annotation replacements for Redisson 4.7.0 wave-61a executor/fanout/geo [0:15]."""
from __future__ import annotations

_EX = "redisson/src/main/java/org/redisson/api/executor/"
_FO = "redisson/src/main/java/org/redisson/api/fanout/"
_GE = "redisson/src/main/java/org/redisson/api/geo/"

W61A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_EX}TaskFinishedListener.java": [
        (
            "/**\n * Task listener invoked when task was finished\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务执行完成时触发的监听器。\n * <p>\n * 无论任务成功或失败，只要执行结束都会回调 {@link #onFinished(String)}。\n * 可与 {@link TaskStartedListener}、{@link TaskSuccessListener} 等组合使用以跟踪任务全生命周期。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when task finished\n     *\n     * @param taskId - id of task\n     */",
            "    /**\n     * 任务执行结束时调用。\n     *\n     * @param taskId 任务唯一标识\n     */",
        ),
    ],
    "TaskFinishedListener.java": [
        (
            "/**\n * Task listener invoked when task was finished\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务执行完成时触发的监听器。\n * <p>\n * 无论任务成功或失败，只要执行结束都会回调 {@link #onFinished(String)}。\n * 可与 {@link TaskStartedListener}、{@link TaskSuccessListener} 等组合使用以跟踪任务全生命周期。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when task finished\n     *\n     * @param taskId - id of task\n     */",
            "    /**\n     * 任务执行结束时调用。\n     *\n     * @param taskId 任务唯一标识\n     */",
        ),
    ],
    f"{_EX}TaskListener.java": [
        (
            "/**\n * Base task listener interface\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务监听器的根接口，继承 {@link java.util.EventListener}。\n * <p>\n * 具体监听器如 {@link TaskStartedListener}、{@link TaskSuccessListener}、\n * {@link TaskFinishedListener} 均扩展本接口，便于统一注册与类型约束。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    "TaskListener.java": [
        (
            "/**\n * Base task listener interface\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务监听器的根接口，继承 {@link java.util.EventListener}。\n * <p>\n * 具体监听器如 {@link TaskStartedListener}、{@link TaskSuccessListener}、\n * {@link TaskFinishedListener} 均扩展本接口，便于统一注册与类型约束。\n *\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_EX}TaskStartedListener.java": [
        (
            "/**\n * Task listener invoked when task was started\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务开始执行时触发的监听器。\n * <p>\n * 在远程执行器节点真正开始运行任务逻辑前回调，可用于记录启动时间或初始化监控上下文。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when task was started\n     *\n     * @param taskId - id of task\n     */",
            "    /**\n     * 任务开始执行时调用。\n     *\n     * @param taskId 任务唯一标识\n     */",
        ),
    ],
    "TaskStartedListener.java": [
        (
            "/**\n * Task listener invoked when task was started\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务开始执行时触发的监听器。\n * <p>\n * 在远程执行器节点真正开始运行任务逻辑前回调，可用于记录启动时间或初始化监控上下文。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when task was started\n     *\n     * @param taskId - id of task\n     */",
            "    /**\n     * 任务开始执行时调用。\n     *\n     * @param taskId 任务唯一标识\n     */",
        ),
    ],
    f"{_EX}TaskSuccessListener.java": [
        (
            "/**\n * Task listener invoked when task was succeeded\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务成功完成时触发的监听器。\n * <p>\n * 仅在任务正常返回结果时回调；若任务抛异常或超时，应通过其他失败监听器处理。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when task was succeeded\n     *\n     * @param taskId - id of task\n     * @param result - result of task\n     */",
            "    /**\n     * 任务成功完成时调用。\n     *\n     * @param taskId 任务唯一标识\n     * @param result 任务执行返回的结果，类型由提交时的泛型决定\n     */",
        ),
    ],
    "TaskSuccessListener.java": [
        (
            "/**\n * Task listener invoked when task was succeeded\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 分布式任务成功完成时触发的监听器。\n * <p>\n * 仅在任务正常返回结果时回调；若任务抛异常或超时，应通过其他失败监听器处理。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Invoked when task was succeeded\n     *\n     * @param taskId - id of task\n     * @param result - result of task\n     */",
            "    /**\n     * 任务成功完成时调用。\n     *\n     * @param taskId 任务唯一标识\n     * @param result 任务执行返回的结果，类型由提交时的泛型决定\n     */",
        ),
    ],
    f"{_FO}FanoutPublishArgs.java": [
        (
            "/**\n * Interface defining parameters for queue addition operations.\n *\n * @param <V> type\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 可靠扇出（Reliable Fanout）发布操作的参数构建接口。\n * <p>\n * 通过 {@link #messages(MessageArgs[])} 静态工厂创建实例，可链式设置消息头编解码器。\n *\n * @param <V> 消息体类型\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Sets the codec to be used for encoding and decoding message headers.\n     *\n     * @param codec the codec\n     * @return arguments object\n     */",
            "    /**\n     * 设置消息头编解码所用的 {@link org.redisson.client.codec.Codec}。\n     *\n     * @param codec 编解码器\n     * @return 当前参数对象，支持链式调用\n     */",
        ),
        (
            "    /**\n     * Defines messages to be added.\n     *\n     * @param msgs The message arguments to be added to the queue\n     * @return arguments object\n     */",
            "    /**\n     * 定义待发布的一条或多条消息。\n     *\n     * @param msgs 要加入扇出队列的消息参数\n     * @return 参数构建对象\n     */",
        ),
    ],
    "FanoutPublishArgs.java": [
        (
            "/**\n * Interface defining parameters for queue addition operations.\n *\n * @param <V> type\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 可靠扇出（Reliable Fanout）发布操作的参数构建接口。\n * <p>\n * 通过 {@link #messages(MessageArgs[])} 静态工厂创建实例，可链式设置消息头编解码器。\n *\n * @param <V> 消息体类型\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Sets the codec to be used for encoding and decoding message headers.\n     *\n     * @param codec the codec\n     * @return arguments object\n     */",
            "    /**\n     * 设置消息头编解码所用的 {@link org.redisson.client.codec.Codec}。\n     *\n     * @param codec 编解码器\n     * @return 当前参数对象，支持链式调用\n     */",
        ),
        (
            "    /**\n     * Defines messages to be added.\n     *\n     * @param msgs The message arguments to be added to the queue\n     * @return arguments object\n     */",
            "    /**\n     * 定义待发布的一条或多条消息。\n     *\n     * @param msgs 要加入扇出队列的消息参数\n     * @return 参数构建对象\n     */",
        ),
    ],
    f"{_FO}FanoutPublishParams.java": [
        (
            "public final class FanoutPublishParams<V> extends BaseSyncParams<FanoutPublishArgs<V>> implements FanoutPublishArgs<V> {",
            "/**\n * {@link FanoutPublishArgs} 的默认实现，持有待发布消息及可选的消息头编解码器。\n *\n * @param <V> 消息体类型\n * @author Nikita Koksharov\n */\npublic final class FanoutPublishParams<V> extends BaseSyncParams<FanoutPublishArgs<V>> implements FanoutPublishArgs<V> {",
        ),
        (
            "    private final MessageArgs<V>[] msgs;\n    private Codec headersCodec;",
            "    /** 待发布的消息参数数组。 */\n    private final MessageArgs<V>[] msgs;\n    /** 消息头编解码器，可为 null 表示使用默认编解码。 */\n    private Codec headersCodec;",
        ),
        (
            "    public FanoutPublishParams(MessageArgs<V>[] msgs) {",
            "    /** 以给定消息列表创建发布参数。 */\n    public FanoutPublishParams(MessageArgs<V>[] msgs) {",
        ),
        (
            "    public MessageArgs<V>[] getMsgs() {",
            "    /** 返回待发布的消息参数数组。 */\n    public MessageArgs<V>[] getMsgs() {",
        ),
        (
            "    public Codec getHeadersCodec() {",
            "    /** 返回消息头编解码器。 */\n    public Codec getHeadersCodec() {",
        ),
    ],
    "FanoutPublishParams.java": [
        (
            "public final class FanoutPublishParams<V> extends BaseSyncParams<FanoutPublishArgs<V>> implements FanoutPublishArgs<V> {",
            "/**\n * {@link FanoutPublishArgs} 的默认实现，持有待发布消息及可选的消息头编解码器。\n *\n * @param <V> 消息体类型\n * @author Nikita Koksharov\n */\npublic final class FanoutPublishParams<V> extends BaseSyncParams<FanoutPublishArgs<V>> implements FanoutPublishArgs<V> {",
        ),
        (
            "    private final MessageArgs<V>[] msgs;\n    private Codec headersCodec;",
            "    /** 待发布的消息参数数组。 */\n    private final MessageArgs<V>[] msgs;\n    /** 消息头编解码器，可为 null 表示使用默认编解码。 */\n    private Codec headersCodec;",
        ),
        (
            "    public FanoutPublishParams(MessageArgs<V>[] msgs) {",
            "    /** 以给定消息列表创建发布参数。 */\n    public FanoutPublishParams(MessageArgs<V>[] msgs) {",
        ),
        (
            "    public MessageArgs<V>[] getMsgs() {",
            "    /** 返回待发布的消息参数数组。 */\n    public MessageArgs<V>[] getMsgs() {",
        ),
        (
            "    public Codec getHeadersCodec() {",
            "    /** 返回消息头编解码器。 */\n    public Codec getHeadersCodec() {",
        ),
    ],
    f"{_FO}MessageFilter.java": [
        (
            "/**\n * Interface for filtering messages in a ReliableFanout object.\n * <p>\n * Implementing this interface allows selective message delivery to subscribers\n * based on custom logic. The filter evaluates messages and determines if they\n * should be delivered to specific subscribers.\n * <p>\n * As a serializable BiPredicate, instances of this interface can be:\n * - Transmitted across network boundaries\n * - Replicated among all ReliableFanout objects\n * - Applied on each node during message publishing process\n * <p>\n * When implemented, the test method should return true if the message should be\n * delivered, or false to filter it out.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 可靠扇出（ReliableFanout）消息过滤接口。\n * <p>\n * 实现本接口可基于自定义逻辑选择性投递消息：{@link java.util.function.BiPredicate#test}\n * 返回 {@code true} 表示应投递，{@code false} 表示过滤掉。\n * <p>\n * 作为可序列化的 {@link java.util.function.BiPredicate}，实例可：\n * <ul>\n *   <li>跨网络传输到各节点</li>\n *   <li>在所有 ReliableFanout 副本间复制</li>\n *   <li>在发布流程的每个节点上执行过滤</li>\n * </ul>\n * 第二个参数为消息头键值对，可用于基于元数据的过滤决策。\n *\n * @param <V> 消息体类型\n * @author Nikita Koksharov\n */",
        ),
    ],
    "MessageFilter.java": [
        (
            "/**\n * Interface for filtering messages in a ReliableFanout object.\n * <p>\n * Implementing this interface allows selective message delivery to subscribers\n * based on custom logic. The filter evaluates messages and determines if they\n * should be delivered to specific subscribers.\n * <p>\n * As a serializable BiPredicate, instances of this interface can be:\n * - Transmitted across network boundaries\n * - Replicated among all ReliableFanout objects\n * - Applied on each node during message publishing process\n * <p>\n * When implemented, the test method should return true if the message should be\n * delivered, or false to filter it out.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 可靠扇出（ReliableFanout）消息过滤接口。\n * <p>\n * 实现本接口可基于自定义逻辑选择性投递消息：{@link java.util.function.BiPredicate#test}\n * 返回 {@code true} 表示应投递，{@code false} 表示过滤掉。\n * <p>\n * 作为可序列化的 {@link java.util.function.BiPredicate}，实例可：\n * <ul>\n *   <li>跨网络传输到各节点</li>\n *   <li>在所有 ReliableFanout 副本间复制</li>\n *   <li>在发布流程的每个节点上执行过滤</li>\n * </ul>\n * 第二个参数为消息头键值对，可用于基于元数据的过滤决策。\n *\n * @param <V> 消息体类型\n * @author Nikita Koksharov\n */",
        ),
    ],
    f"{_GE}GeoEntry.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 地理空间集合中的单条成员及其经纬度坐标。\n * <p>\n * 用于 {@link org.redisson.api.RGeo#add(GeoEntry...)} 批量写入 Redis GEO 数据结构。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    private final double longitude;\n    private final double latitude;\n    private final Object member;",
            "    /** 经度（longitude）。 */\n    private final double longitude;\n    /** 纬度（latitude）。 */\n    private final double latitude;\n    /** 集合成员标识，可为任意对象。 */\n    private final Object member;",
        ),
        (
            "    public GeoEntry(double longitude, double latitude, Object member) {",
            "    /**\n     * 创建地理条目。\n     *\n     * @param longitude 经度\n     * @param latitude 纬度\n     * @param member 成员标识\n     */\n    public GeoEntry(double longitude, double latitude, Object member) {",
        ),
        (
            "    public double getLatitude() {",
            "    /** 返回纬度。 */\n    public double getLatitude() {",
        ),
        (
            "    public double getLongitude() {",
            "    /** 返回经度。 */\n    public double getLongitude() {",
        ),
        (
            "    public Object getMember() {",
            "    /** 返回成员标识。 */\n    public Object getMember() {",
        ),
    ],
    "GeoEntry.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 地理空间集合中的单条成员及其经纬度坐标。\n * <p>\n * 用于 {@link org.redisson.api.RGeo#add(GeoEntry...)} 批量写入 Redis GEO 数据结构。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    private final double longitude;\n    private final double latitude;\n    private final Object member;",
            "    /** 经度（longitude）。 */\n    private final double longitude;\n    /** 纬度（latitude）。 */\n    private final double latitude;\n    /** 集合成员标识，可为任意对象。 */\n    private final Object member;",
        ),
        (
            "    public GeoEntry(double longitude, double latitude, Object member) {",
            "    /**\n     * 创建地理条目。\n     *\n     * @param longitude 经度\n     * @param latitude 纬度\n     * @param member 成员标识\n     */\n    public GeoEntry(double longitude, double latitude, Object member) {",
        ),
        (
            "    public double getLatitude() {",
            "    /** 返回纬度。 */\n    public double getLatitude() {",
        ),
        (
            "    public double getLongitude() {",
            "    /** 返回经度。 */\n    public double getLongitude() {",
        ),
        (
            "    public Object getMember() {",
            "    /** 返回成员标识。 */\n    public Object getMember() {",
        ),
    ],
    f"{_GE}GeoOrder.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 地理空间搜索结果的距离排序方式。\n * <p>\n * 配合 {@link OptionalGeoSearch#order(GeoOrder)} 使用，\n * {@link #ASC} 表示按距离升序（近到远），{@link #DESC} 表示降序。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    ASC, DESC",
            "    /** 按距离升序排列（由近到远）。 */\n    ASC,\n    /** 按距离降序排列（由远到近）。 */\n    DESC",
        ),
    ],
    "GeoOrder.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 地理空间搜索结果的距离排序方式。\n * <p>\n * 配合 {@link OptionalGeoSearch#order(GeoOrder)} 使用，\n * {@link #ASC} 表示按距离升序（近到远），{@link #DESC} 表示降序。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    ASC, DESC",
            "    /** 按距离升序排列（由近到远）。 */\n    ASC,\n    /** 按距离降序排列（由远到近）。 */\n    DESC",
        ),
    ],
    f"{_GE}GeoPosition.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 不可变的经纬度坐标对，表示地理空间中的一个点。\n * <p>\n * 实现 {@link #equals} 与 {@link #hashCode}，可在集合中作为键或值使用。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    private final double longitude;\n    private final double latitude;",
            "    /** 经度。 */\n    private final double longitude;\n    /** 纬度。 */\n    private final double latitude;",
        ),
        (
            "    public GeoPosition(double longitude, double latitude) {",
            "    /**\n     * 以给定经纬度创建坐标。\n     *\n     * @param longitude 经度\n     * @param latitude 纬度\n     */\n    public GeoPosition(double longitude, double latitude) {",
        ),
        (
            "    public double getLatitude() {",
            "    /** 返回纬度。 */\n    public double getLatitude() {",
        ),
        (
            "    public double getLongitude() {",
            "    /** 返回经度。 */\n    public double getLongitude() {",
        ),
    ],
    "GeoPosition.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 不可变的经纬度坐标对，表示地理空间中的一个点。\n * <p>\n * 实现 {@link #equals} 与 {@link #hashCode}，可在集合中作为键或值使用。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    private final double longitude;\n    private final double latitude;",
            "    /** 经度。 */\n    private final double longitude;\n    /** 纬度。 */\n    private final double latitude;",
        ),
        (
            "    public GeoPosition(double longitude, double latitude) {",
            "    /**\n     * 以给定经纬度创建坐标。\n     *\n     * @param longitude 经度\n     * @param latitude 纬度\n     */\n    public GeoPosition(double longitude, double latitude) {",
        ),
        (
            "    public double getLatitude() {",
            "    /** 返回纬度。 */\n    public double getLatitude() {",
        ),
        (
            "    public double getLongitude() {",
            "    /** 返回经度。 */\n    public double getLongitude() {",
        ),
    ],
    f"{_GE}GeoSearchArgs.java": [
        (
            "/**\n * Arguments object for RGeo search method.\n * <p>\n * {@link org.redisson.api.RGeo#search(GeoSearchArgs)}\n * {@link org.redisson.api.RGeoAsync#searchAsync(GeoSearchArgs)}\n * {@link org.redisson.api.RGeoRx#search(GeoSearchArgs)}\n * {@link org.redisson.api.RGeoReactive#search(GeoSearchArgs)}\n *\n * @author Nikita Koksharov\n */",
            "/**\n * {@link org.redisson.api.RGeo#search(GeoSearchArgs)} 等地理搜索方法的参数入口。\n * <p>\n * 通过静态工厂 {@link #from(Object)} 或 {@link #from(double, double)} 指定搜索中心，\n * 再链式调用 {@link ShapeGeoSearch}、{@link OptionalGeoSearch} 方法完善条件。\n * <p>\n * 同步/异步/Reactive 变体均接受本类型：\n * {@link org.redisson.api.RGeo#search(GeoSearchArgs)}、\n * {@link org.redisson.api.RGeoAsync#searchAsync(GeoSearchArgs)}、\n * {@link org.redisson.api.RGeoRx#search(GeoSearchArgs)}、\n * {@link org.redisson.api.RGeoReactive#search(GeoSearchArgs)}\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Defines search from defined member\n     *\n     * @param member - object\n     * @return search conditions object\n     */",
            "    /**\n     * 以集合中已有成员的位置作为搜索中心。\n     *\n     * @param member 成员对象\n     * @return 形状搜索条件构建器\n     */",
        ),
        (
            "    /**\n     * Defines search from defined longitude and latitude coordinates\n     *\n     * @param longitude - longitude of object\n     * @param latitude - latitude of object\n     * @return search conditions object\n     */",
            "    /**\n     * 以给定经纬度坐标作为搜索中心。\n     *\n     * @param longitude 经度\n     * @param latitude 纬度\n     * @return 形状搜索条件构建器\n     */",
        ),
    ],
    "GeoSearchArgs.java": [
        (
            "/**\n * Arguments object for RGeo search method.\n * <p>\n * {@link org.redisson.api.RGeo#search(GeoSearchArgs)}\n * {@link org.redisson.api.RGeoAsync#searchAsync(GeoSearchArgs)}\n * {@link org.redisson.api.RGeoRx#search(GeoSearchArgs)}\n * {@link org.redisson.api.RGeoReactive#search(GeoSearchArgs)}\n *\n * @author Nikita Koksharov\n */",
            "/**\n * {@link org.redisson.api.RGeo#search(GeoSearchArgs)} 等地理搜索方法的参数入口。\n * <p>\n * 通过静态工厂 {@link #from(Object)} 或 {@link #from(double, double)} 指定搜索中心，\n * 再链式调用 {@link ShapeGeoSearch}、{@link OptionalGeoSearch} 方法完善条件。\n * <p>\n * 同步/异步/Reactive 变体均接受本类型：\n * {@link org.redisson.api.RGeo#search(GeoSearchArgs)}、\n * {@link org.redisson.api.RGeoAsync#searchAsync(GeoSearchArgs)}、\n * {@link org.redisson.api.RGeoRx#search(GeoSearchArgs)}、\n * {@link org.redisson.api.RGeoReactive#search(GeoSearchArgs)}\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Defines search from defined member\n     *\n     * @param member - object\n     * @return search conditions object\n     */",
            "    /**\n     * 以集合中已有成员的位置作为搜索中心。\n     *\n     * @param member 成员对象\n     * @return 形状搜索条件构建器\n     */",
        ),
        (
            "    /**\n     * Defines search from defined longitude and latitude coordinates\n     *\n     * @param longitude - longitude of object\n     * @param latitude - latitude of object\n     * @return search conditions object\n     */",
            "    /**\n     * 以给定经纬度坐标作为搜索中心。\n     *\n     * @param longitude 经度\n     * @param latitude 纬度\n     * @return 形状搜索条件构建器\n     */",
        ),
    ],
    f"{_GE}GeoSearchParams.java": [
        (
            "/**\n * @author Nikita Koksharov\n */",
            "/**\n * {@link GeoSearchArgs}、{@link ShapeGeoSearch}、{@link OptionalGeoSearch} 的可变参数实现。\n * <p>\n * 封装搜索中心、形状（矩形/圆形）、结果数量限制及排序等条件，供 {@link org.redisson.api.RGeo} 内部使用。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    private Object member;\n    private Double longitude;\n    private Double latitude;\n    private Double width;\n    private Double height;\n    private Double radius;\n    private GeoUnit unit;\n    private Integer count;\n    private boolean countAny;\n    private GeoOrder order;",
            "    /** 以成员为搜索中心时的成员对象。 */\n    private Object member;\n    /** 以坐标为搜索中心时的经度。 */\n    private Double longitude;\n    /** 以坐标为搜索中心时的纬度。 */\n    private Double latitude;\n    /** 矩形搜索区域的宽度。 */\n    private Double width;\n    /** 矩形搜索区域的高度。 */\n    private Double height;\n    /** 圆形搜索半径。 */\n    private Double radius;\n    /** 距离单位。 */\n    private GeoUnit unit;\n    /** 返回结果数量上限。 */\n    private Integer count;\n    /** 为 true 时使用 ANY 语义（找到足够数量即返回，可能非最近）。 */\n    private boolean countAny;\n    /** 结果距离排序方式。 */\n    private GeoOrder order;",
        ),
        (
            "    GeoSearchParams(Object member) {",
            "    /** 以成员位置为搜索中心创建参数。 */\n    GeoSearchParams(Object member) {",
        ),
        (
            "    GeoSearchParams(double longitude, double latitude) {",
            "    /** 以经纬度坐标为搜索中心创建参数。 */\n    GeoSearchParams(double longitude, double latitude) {",
        ),
        (
            "    public Object getMember() {",
            "    /** 返回搜索中心成员。 */\n    public Object getMember() {",
        ),
        (
            "    public Double getLongitude() {",
            "    /** 返回搜索中心经度。 */\n    public Double getLongitude() {",
        ),
        (
            "    public Double getLatitude() {",
            "    /** 返回搜索中心纬度。 */\n    public Double getLatitude() {",
        ),
        (
            "    public Double getWidth() {",
            "    /** 返回矩形宽度。 */\n    public Double getWidth() {",
        ),
        (
            "    public Double getHeight() {",
            "    /** 返回矩形高度。 */\n    public Double getHeight() {",
        ),
        (
            "    public Double getRadius() {",
            "    /** 返回圆形搜索半径。 */\n    public Double getRadius() {",
        ),
        (
            "    public GeoUnit getUnit() {",
            "    /** 返回距离单位。 */\n    public GeoUnit getUnit() {",
        ),
        (
            "    public Integer getCount() {",
            "    /** 返回结果数量上限。 */\n    public Integer getCount() {",
        ),
        (
            "    public boolean isCountAny() {",
            "    /** 是否使用 ANY 计数语义。 */\n    public boolean isCountAny() {",
        ),
        (
            "    public GeoOrder getOrder() {",
            "    /** 返回结果排序方式。 */\n    public GeoOrder getOrder() {",
        ),
    ],
    "GeoSearchParams.java": [
        (
            "/**\n * @author Nikita Koksharov\n */",
            "/**\n * {@link GeoSearchArgs}、{@link ShapeGeoSearch}、{@link OptionalGeoSearch} 的可变参数实现。\n * <p>\n * 封装搜索中心、形状（矩形/圆形）、结果数量限制及排序等条件，供 {@link org.redisson.api.RGeo} 内部使用。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    private Object member;\n    private Double longitude;\n    private Double latitude;\n    private Double width;\n    private Double height;\n    private Double radius;\n    private GeoUnit unit;\n    private Integer count;\n    private boolean countAny;\n    private GeoOrder order;",
            "    /** 以成员为搜索中心时的成员对象。 */\n    private Object member;\n    /** 以坐标为搜索中心时的经度。 */\n    private Double longitude;\n    /** 以坐标为搜索中心时的纬度。 */\n    private Double latitude;\n    /** 矩形搜索区域的宽度。 */\n    private Double width;\n    /** 矩形搜索区域的高度。 */\n    private Double height;\n    /** 圆形搜索半径。 */\n    private Double radius;\n    /** 距离单位。 */\n    private GeoUnit unit;\n    /** 返回结果数量上限。 */\n    private Integer count;\n    /** 为 true 时使用 ANY 语义（找到足够数量即返回，可能非最近）。 */\n    private boolean countAny;\n    /** 结果距离排序方式。 */\n    private GeoOrder order;",
        ),
        (
            "    GeoSearchParams(Object member) {",
            "    /** 以成员位置为搜索中心创建参数。 */\n    GeoSearchParams(Object member) {",
        ),
        (
            "    GeoSearchParams(double longitude, double latitude) {",
            "    /** 以经纬度坐标为搜索中心创建参数。 */\n    GeoSearchParams(double longitude, double latitude) {",
        ),
        (
            "    public Object getMember() {",
            "    /** 返回搜索中心成员。 */\n    public Object getMember() {",
        ),
        (
            "    public Double getLongitude() {",
            "    /** 返回搜索中心经度。 */\n    public Double getLongitude() {",
        ),
        (
            "    public Double getLatitude() {",
            "    /** 返回搜索中心纬度。 */\n    public Double getLatitude() {",
        ),
        (
            "    public Double getWidth() {",
            "    /** 返回矩形宽度。 */\n    public Double getWidth() {",
        ),
        (
            "    public Double getHeight() {",
            "    /** 返回矩形高度。 */\n    public Double getHeight() {",
        ),
        (
            "    public Double getRadius() {",
            "    /** 返回圆形搜索半径。 */\n    public Double getRadius() {",
        ),
        (
            "    public GeoUnit getUnit() {",
            "    /** 返回距离单位。 */\n    public GeoUnit getUnit() {",
        ),
        (
            "    public Integer getCount() {",
            "    /** 返回结果数量上限。 */\n    public Integer getCount() {",
        ),
        (
            "    public boolean isCountAny() {",
            "    /** 是否使用 ANY 计数语义。 */\n    public boolean isCountAny() {",
        ),
        (
            "    public GeoOrder getOrder() {",
            "    /** 返回结果排序方式。 */\n    public GeoOrder getOrder() {",
        ),
    ],
    f"{_GE}GeoUnit.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 地理空间距离度量单位，对应 Redis GEO 命令中的单位字符串。\n * <p>\n * 各枚举常量的 {@link #toString()} 返回 Redis 接受的缩写（如 {@code m}、{@code km}）。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    METERS {",
            "    /** 米（Redis 单位 {@code m}）。 */\n    METERS {",
        ),
        (
            "    KILOMETERS {",
            "    /** 千米（Redis 单位 {@code km}）。 */\n    KILOMETERS {",
        ),
        (
            "    MILES {",
            "    /** 英里（Redis 单位 {@code mi}）。 */\n    MILES {",
        ),
        (
            "    FEET {",
            "    /** 英尺（Redis 单位 {@code ft}）。 */\n    FEET {",
        ),
    ],
    "GeoUnit.java": [
        (
            "/**\n * \n * @author Nikita Koksharov\n *\n */",
            "/**\n * 地理空间距离度量单位，对应 Redis GEO 命令中的单位字符串。\n * <p>\n * 各枚举常量的 {@link #toString()} 返回 Redis 接受的缩写（如 {@code m}、{@code km}）。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    METERS {",
            "    /** 米（Redis 单位 {@code m}）。 */\n    METERS {",
        ),
        (
            "    KILOMETERS {",
            "    /** 千米（Redis 单位 {@code km}）。 */\n    KILOMETERS {",
        ),
        (
            "    MILES {",
            "    /** 英里（Redis 单位 {@code mi}）。 */\n    MILES {",
        ),
        (
            "    FEET {",
            "    /** 英尺（Redis 单位 {@code ft}）。 */\n    FEET {",
        ),
    ],
    f"{_GE}OptionalGeoSearch.java": [
        (
            "/**\n * Arguments object for RGeo search method.\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 地理搜索的可选参数阶段：在确定搜索形状后可设置结果数量与排序。\n * <p>\n * 由 {@link ShapeGeoSearch#box} 或 {@link ShapeGeoSearch#radius} 返回，\n * 链式调用完成后作为 {@link GeoSearchArgs} 传给 {@link org.redisson.api.RGeo#search}。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Defines limit of search result\n     *\n     * @param value - result limit\n     * @return search conditions object\n     */",
            "    /**\n     * 限制返回结果数量（按距离排序后取前 N 条）。\n     *\n     * @param value 结果数量上限\n     * @return 当前搜索条件对象\n     */",
        ),
        (
            "    /**\n     * Defines limit of search result.\n     * Returns as soon as enough matches are found.\n     * Result size might be not closest to defined limit,\n     * but works faster.\n     *\n     * @param value - result limit\n     * @return search conditions object\n     */",
            "    /**\n     * 限制返回结果数量，采用 ANY 语义：找到足够匹配即返回，可能非距离最近但更快。\n     *\n     * @param value 结果数量上限\n     * @return 当前搜索条件对象\n     */",
        ),
        (
            "    /**\n     * Defines order of search result\n     *\n     * @param geoOrder - result order\n     * @return search conditions object\n     */",
            "    /**\n     * 指定结果按距离排序的方式。\n     *\n     * @param geoOrder 排序枚举（升序或降序）\n     * @return 当前搜索条件对象\n     */",
        ),
    ],
    "OptionalGeoSearch.java": [
        (
            "/**\n * Arguments object for RGeo search method.\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 地理搜索的可选参数阶段：在确定搜索形状后可设置结果数量与排序。\n * <p>\n * 由 {@link ShapeGeoSearch#box} 或 {@link ShapeGeoSearch#radius} 返回，\n * 链式调用完成后作为 {@link GeoSearchArgs} 传给 {@link org.redisson.api.RGeo#search}。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Defines limit of search result\n     *\n     * @param value - result limit\n     * @return search conditions object\n     */",
            "    /**\n     * 限制返回结果数量（按距离排序后取前 N 条）。\n     *\n     * @param value 结果数量上限\n     * @return 当前搜索条件对象\n     */",
        ),
        (
            "    /**\n     * Defines limit of search result.\n     * Returns as soon as enough matches are found.\n     * Result size might be not closest to defined limit,\n     * but works faster.\n     *\n     * @param value - result limit\n     * @return search conditions object\n     */",
            "    /**\n     * 限制返回结果数量，采用 ANY 语义：找到足够匹配即返回，可能非距离最近但更快。\n     *\n     * @param value 结果数量上限\n     * @return 当前搜索条件对象\n     */",
        ),
        (
            "    /**\n     * Defines order of search result\n     *\n     * @param geoOrder - result order\n     * @return search conditions object\n     */",
            "    /**\n     * 指定结果按距离排序的方式。\n     *\n     * @param geoOrder 排序枚举（升序或降序）\n     * @return 当前搜索条件对象\n     */",
        ),
    ],
    f"{_GE}ShapeGeoSearch.java": [
        (
            "/**\n * Arguments object for RGeo search method.\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 地理搜索的形状定义阶段：在确定搜索中心后选择矩形或圆形区域。\n * <p>\n * 由 {@link GeoSearchArgs#from} 返回；调用 {@link #box} 或 {@link #radius} 后进入 {@link OptionalGeoSearch} 阶段。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Defines search within box\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @param width - box width\n     * @param height - box height\n     * @param geoUnit - geo unit\n     * @return search conditions object\n     */",
            "    /**\n     * 在以搜索中心为基准的矩形区域内搜索。\n     * <p>\n     * 需要 <b>Redis 6.2.0 及以上版本。</b>\n     *\n     * @param width 矩形宽度\n     * @param height 矩形高度\n     * @param geoUnit 距离单位\n     * @return 可选参数构建器\n     */",
        ),
        (
            "    /**\n     * Defines search within radius\n     *\n     * @param radius - radius in geo units\n     * @param geoUnit - geo unit\n     * @return search conditions object\n     */",
            "    /**\n     * 在指定半径的圆形区域内搜索。\n     *\n     * @param radius 半径（以 geoUnit 为单位）\n     * @param geoUnit 距离单位\n     * @return 可选参数构建器\n     */",
        ),
    ],
    "ShapeGeoSearch.java": [
        (
            "/**\n * Arguments object for RGeo search method.\n *\n * @author Nikita Koksharov\n */",
            "/**\n * 地理搜索的形状定义阶段：在确定搜索中心后选择矩形或圆形区域。\n * <p>\n * 由 {@link GeoSearchArgs#from} 返回；调用 {@link #box} 或 {@link #radius} 后进入 {@link OptionalGeoSearch} 阶段。\n *\n * @author Nikita Koksharov\n */",
        ),
        (
            "    /**\n     * Defines search within box\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @param width - box width\n     * @param height - box height\n     * @param geoUnit - geo unit\n     * @return search conditions object\n     */",
            "    /**\n     * 在以搜索中心为基准的矩形区域内搜索。\n     * <p>\n     * 需要 <b>Redis 6.2.0 及以上版本。</b>\n     *\n     * @param width 矩形宽度\n     * @param height 矩形高度\n     * @param geoUnit 距离单位\n     * @return 可选参数构建器\n     */",
        ),
        (
            "    /**\n     * Defines search within radius\n     *\n     * @param radius - radius in geo units\n     * @param geoUnit - geo unit\n     * @return search conditions object\n     */",
            "    /**\n     * 在指定半径的圆形区域内搜索。\n     *\n     * @param radius 半径（以 geoUnit 为单位）\n     * @param geoUnit 距离单位\n     * @return 可选参数构建器\n     */",
        ),
    ],
}
