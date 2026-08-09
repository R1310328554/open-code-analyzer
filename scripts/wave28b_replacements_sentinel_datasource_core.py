"""Chinese JavaDoc replacements for Sentinel 1.8.10 wave28b datasource extension classes."""

EXTENSION_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractDataSource.java": [
        (
            "/**\n * The abstract readable data source provides basic functionality for loading and parsing config.\n *\n * @param <S> source data type\n * @param <T> target data type\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
            "/**\n * 可读数据源的抽象基类，封装配置加载与解析的通用逻辑。\n *\n * @param <S> 原始配置数据类型\n * @param <T> 解析后的目标类型\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        ),
        (
            "    public AbstractDataSource(Converter<S, T> parser) {",
            "    /** 使用给定 {@link Converter} 构造数据源，并初始化 {@link DynamicSentinelProperty}。 */\n    public AbstractDataSource(Converter<S, T> parser) {",
        ),
        (
            "    public T loadConfig(S conf) throws Exception {",
            "    /** 将原始配置 {@code conf} 经 parser 转换为目标类型。 */\n    public T loadConfig(S conf) throws Exception {",
        ),
    ],
    "ReadableDataSource.java": [
        (
            "/**\n * The readable data source is responsible for retrieving configs (read-only).\n *\n * @param <S> source data type\n * @param <T> target data type\n * @author leyou\n * @author Eric Zhao\n */",
            "/**\n * 只读数据源接口：从后端拉取配置并暴露为 {@link SentinelProperty}。\n *\n * @param <S> 原始数据类型\n * @param <T> 解析后的目标类型\n * @author leyou\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Load data data source as the target type.\n     *\n     * @return the target data.\n     * @throws Exception IO or other error occurs\n     */",
            "    /**\n     * 读取原始数据并解析为目标类型。\n     *\n     * @return 解析后的配置对象\n     * @throws Exception IO 或其他异常\n     */",
        ),
        (
            "    /**\n     * Read original data from the data source.\n     *\n     * @return the original data.\n     * @throws Exception IO or other error occurs\n     */",
            "    /**\n     * 从后端读取原始配置字符串或二进制数据。\n     *\n     * @return 原始配置\n     * @throws Exception IO 或其他异常\n     */",
        ),
        (
            "    /**\n     * Get {@link SentinelProperty} of the data source.\n     *\n     * @return the property.\n     */",
            "    /**\n     * 获取与此数据源绑定的 {@link SentinelProperty}，规则变更时通过其推送。\n     *\n     * @return 动态属性对象\n     */",
        ),
        (
            "    /**\n     * Close the data source.\n     *\n     * @throws Exception IO or other error occurs\n     */",
            "    /**\n     * 关闭数据源并释放后台连接或监听资源。\n     *\n     * @throws Exception IO 或其他异常\n     */",
        ),
    ],
    "WritableDataSource.java": [
        (
            "/**\n * Interface of writable data source support.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 可写数据源接口：将规则持久化到外部存储（如本地文件）。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "    /**\n     * Write the {@code value} to the data source.\n     *\n     * @param value value to write\n     * @throws Exception IO or other error occurs\n     */",
            "    /**\n     * 将 {@code value} 序列化后写入后端存储。\n     *\n     * @param value 待写入的配置对象\n     * @throws Exception IO 或其他异常\n     */",
        ),
        (
            "    /**\n     * Close the data source.\n     *\n     * @throws Exception IO or other error occurs\n     */",
            "    /**\n     * 关闭数据源并释放相关资源。\n     *\n     * @throws Exception IO 或其他异常\n     */",
        ),
    ],
    "AutoRefreshDataSource.java": [
        (
            "/**\n * A {@link ReadableDataSource} automatically fetches the backend data.\n *\n * @param <S> source data type\n * @param <T> target data type\n * @author Carpenter Lee\n */",
            "/**\n * 自动定时刷新的 {@link ReadableDataSource}：周期性检测变更并更新 {@link SentinelProperty}。\n *\n * @param <S> 原始数据类型\n * @param <T> 解析后的目标类型\n * @author Carpenter Lee\n */",
        ),
        (
            "    public AutoRefreshDataSource(Converter<S, T> configParser) {",
            "    /** 使用默认刷新间隔（3 秒）启动定时刷新任务。 */\n    public AutoRefreshDataSource(Converter<S, T> configParser) {",
        ),
        (
            "    public AutoRefreshDataSource(Converter<S, T> configParser, final long recommendRefreshMs) {",
            "    /** 指定刷新间隔（毫秒）并启动定时刷新任务。 */\n    public AutoRefreshDataSource(Converter<S, T> configParser, final long recommendRefreshMs) {",
        ),
        (
            "    protected boolean isModified() {",
            "    /** 子类可覆盖以判断后端数据是否变更；默认始终返回 true。 */\n    protected boolean isModified() {",
        ),
    ],
    "Converter.java": [
        (
            "/**\n * Convert an object from source type {@code S} to target type {@code T}.\n *\n * @author leyou\n * @author Eric Zhao\n */",
            "/**\n * 配置转换器：将原始类型 {@code S} 转换为目标类型 {@code T}（如 JSON 字符串 → 规则列表）。\n *\n * @author leyou\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Convert {@code source} to the target type.\n     *\n     * @param source the source object\n     * @return the target object\n     */",
            "    /**\n     * 将 {@code source} 转换为目标类型。\n     *\n     * @param source 原始配置对象\n     * @return 解析后的目标对象\n     */",
        ),
    ],
    "EmptyDataSource.java": [
        (
            "/**\n * A {@link ReadableDataSource} based on nothing. {@link EmptyDataSource#getProperty()} will always return the same cached\n * {@link SentinelProperty} that doing nothing.\n * <br/>\n * This class is used when we want to use default settings instead of configs from the {@link ReadableDataSource}.\n *\n * @author leyou\n */",
            "/**\n * 空数据源：不读取任何外部配置，{@link #getProperty()} 始终返回无操作的 {@link NoOpSentinelProperty}。\n * <br/>\n * 当希望使用 Sentinel 内置默认规则而非外部配置时使用。\n *\n * @author leyou\n */",
        ),
        (
            "    public static final ReadableDataSource<Object, Object> EMPTY_DATASOURCE = new EmptyDataSource();",
            "    /** 全局共享的空数据源单例。 */\n    public static final ReadableDataSource<Object, Object> EMPTY_DATASOURCE = new EmptyDataSource();",
        ),
    ],
    "FileInJarReadableDataSource.java": [
        (
            "/**\n * <p>\n * A {@link ReadableDataSource} based on jar file. This class can only read file initially when it loads file.\n * </p>\n * <p>\n * Limitations: Default read buffer size is 1 MB, while max allowed buffer size is 4MB.\n * File size should not exceed the buffer size, or exception will be thrown. Default charset is UTF-8.\n * </p>\n *\n * @author dingq\n * @author Eric Zhao\n * @since 1.6.0\n */",
            "/**\n * <p>\n * 基于 JAR 内文件的只读数据源，仅在初始化时加载一次，不支持热更新。\n * </p>\n * <p>\n * 限制：默认读缓冲 1 MB，最大 4 MB；文件不得超过缓冲大小，否则抛异常。默认字符集 UTF-8。\n * </p>\n *\n * @author dingq\n * @author Eric Zhao\n * @since 1.6.0\n */",
        ),
        (
            "    /**\n     * @param jarName       the jar to read\n     * @param fileInJarName the file in jar to read\n     * @param configParser  the config decoder (parser)\n     * @throws IOException if IO failure occurs\n     */",
            "    /**\n     * @param jarName       待读取的 JAR 路径\n     * @param fileInJarName JAR 内配置文件路径\n     * @param configParser  配置解析器\n     * @throws IOException IO 失败时抛出\n     */",
        ),
        (
            "            // Will throw FileNotFoundException later.",
            "            // 条目不存在，后续 read 将失败",
        ),
        (
            "    private void firstLoad() {",
            "    /** 构造完成后立即加载一次配置并更新 property。 */\n    private void firstLoad() {",
        ),
    ],
    "FileRefreshableDataSource.java": [
        (
            "/**\n * <p>\n * A {@link ReadableDataSource} based on file. This class will automatically\n * fetches the backend file every isModified period.\n * </p>\n * <p>\n * Limitations: Default read buffer size is 1 MB. If file size is greater than\n * buffer size, exceeding bytes will be ignored. Default charset is UTF-8.\n * </p>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
            "/**\n * <p>\n * 基于本地文件的自动刷新数据源：定时检测文件修改时间并重新加载。\n * </p>\n * <p>\n * 限制：默认读缓冲 1 MB；文件大于缓冲时超出部分被忽略。默认字符集 UTF-8。\n * </p>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        ),
        (
            "    /**\n     * Create a file based {@link ReadableDataSource} whose read buffer size is\n     * 1MB, charset is UTF8, and read interval is 3 seconds.\n     *\n     * @param file         the file to read\n     * @param configParser the config decoder (parser)\n     */",
            "    /**\n     * 创建文件数据源：缓冲 1 MB、UTF-8、刷新间隔 3 秒。\n     *\n     * @param file         配置文件\n     * @param configParser 配置解析器\n     */",
        ),
        (
            "        // If the file does not exist, the last modified will be 0.",
            "        // 文件不存在时 lastModified 为 0",
        ),
        (
            "    private void firstLoad() {",
            "    /** 构造完成后立即加载一次配置。 */\n    private void firstLoad() {",
        ),
        (
            "            // Will throw FileNotFoundException later.",
            "            // 文件不存在，后续 read 将抛 FileNotFoundException",
        ),
    ],
    "FileWritableDataSource.java": [
        (
            "/**\n * A {@link WritableDataSource} based on file.\n *\n * @param <T> data type\n * @author Eric Zhao\n * @since 0.2.0\n */",
            "/**\n * 基于本地文件的 {@link WritableDataSource}，将规则序列化后写入指定路径。\n *\n * @param <T> 配置数据类型\n * @author Eric Zhao\n * @since 0.2.0\n */",
        ),
        (
            "    @Override\n    public void write(T value) throws Exception {",
            "    /** 加锁后将 value 编码为字符串并覆盖写入文件。 */\n    @Override\n    public void write(T value) throws Exception {",
        ),
        (
            "                        // nothing",
            "                        // 忽略关闭异常",
        ),
        (
            "        // Nothing",
            "        // 文件数据源无需额外关闭逻辑",
        ),
    ],
}
