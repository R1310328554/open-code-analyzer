"""Chinese annotation replacements for Sentinel 1.8.10 wave32a [0:15]."""

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

MF = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/log/jul/MessageFormatter.java"
FILE_REPLACEMENTS[MF] = [
    (
        "// contributors: lizongbo: proposed special treatment of array parameter values\n// Joern Huxhorn: pointed out double[] omission, suggested deep array copy",
        "// 贡献者：lizongbo 提出数组参数的特殊处理；Joern Huxhorn 指出 double[] 遗漏并建议深拷贝",
    ),
    (
        "/**\n * Formats messages according to very simple substitution rules. Substitutions\n * can be made 1, 2 or more arguments.\n *\n * <p>\n * For example,\n *\n * <pre>\n * MessageFormatter.format(&quot;Hi {}.&quot;, &quot;there&quot;)\n * </pre>\n *\n * will return the string \"Hi there.\".\n * <p>\n * The {} pair is called the <em>formatting anchor</em>. It serves to designate\n * the location where arguments need to be substituted within the message\n * pattern.\n * <p>\n * In case your message contains the '{' or the '}' character, you do not have\n * to do anything special unless the '}' character immediately follows '{'. For\n * example,\n *\n * <pre>\n * MessageFormatter.format(&quot;Set {1,2,3} is not equal to {}.&quot;, &quot;1,2&quot;);\n * </pre>\n *\n * will return the string \"Set {1,2,3} is not equal to 1,2.\".\n *\n * <p>\n * If for whatever reason you need to place the string \"{}\" in the message\n * without its <em>formatting anchor</em> meaning, then you need to escape the\n * '{' character with '\\', that is the backslash character. Only the '{'\n * character should be escaped. There is no need to escape the '}' character.\n * For example,\n *\n * <pre>\n * MessageFormatter.format(&quot;Set \\\\{} is not equal to {}.&quot;, &quot;1,2&quot;);\n * </pre>\n *\n * will return the string \"Set {} is not equal to 1,2.\".\n *\n * <p>\n * The escaping behavior just described can be overridden by escaping the escape\n * character '\\'. Calling\n *\n * <pre>\n * MessageFormatter.format(&quot;File name is C:\\\\\\\\{}.&quot;, &quot;file.zip&quot;);\n * </pre>\n *\n * will return the string \"File name is C:\\file.zip\".\n *\n * <p>\n * The formatting conventions are different than those of {@link MessageFormat}\n * which ships with the Java platform. This is justified by the fact that\n * SLF4J's implementation is 10 times faster than that of {@link MessageFormat}.\n * This local performance difference is both measurable and significant in the\n * larger context of the complete logging processing chain.\n *\n * <p>\n * See also {@link #format(String, Object)},\n * {@link #format(String, Object, Object)} and\n * {@link #arrayFormat(String, Object[])} methods for more details.\n *\n * @author Ceki G&uuml;lc&uuml;\n * @author Joern Huxhorn\n */",
        "/**\n * 按 SLF4J 风格用 {@code {}} 占位符格式化日志消息，支持 1 个、2 个或多个参数替换。\n * <p>\n * {@code {}} 称为<em>格式化锚点</em>，标记参数插入位置。\n * 消息中含字面量 {@code {} } 时可对 {@code {} } 前的 {@code \\} 转义；\n * 对 {@code \\} 本身再转义可恢复字面反斜杠语义。\n * </p>\n * <p>\n * 约定与 JDK {@link MessageFormat} 不同，但实现约快 10 倍，\n * 对整条日志处理链的性能影响显著。\n * </p>\n * <p>\n * 详见 {@link #format(String, Object)}、{@link #format(String, Object, Object)}\n * 与 {@link #arrayFormat(String, Object[])}。\n * </p>\n *\n * @author Ceki G&uuml;lc&uuml;\n * @author Joern Huxhorn\n */",
    ),
    (
        "     * Performs single argument substitution for the 'messagePattern' passed as\n     * parameter.\n     * <p>\n     * For example,\n     *\n     * <pre>\n     * MessageFormatter.format(&quot;Hi {}.&quot;, &quot;there&quot;);\n     * </pre>\n     *\n     * will return the string \"Hi there.\".\n     * <p>\n     *\n     * @param messagePattern\n     *          The message pattern which will be parsed and formatted\n     * @param arg\n     *          The argument to be substituted in place of the formatting anchor\n     * @return The formatted message",
        "     * 对消息模板做单参数占位符替换。\n     *\n     * @param messagePattern\n     *          The message pattern which will be parsed and formatted\n     * @param arg\n     *          The argument to be substituted in place of the formatting anchor\n     * @return The formatted message",
    ),
    (
        "     * Performs a two argument substitution for the 'messagePattern' passed as\n     * parameter.\n     * <p>\n     * For example,\n     *\n     * <pre>\n     * MessageFormatter.format(&quot;Hi {}. My name is {}.&quot;, &quot;Alice&quot;, &quot;Bob&quot;);\n     * </pre>\n     *\n     * will return the string \"Hi Alice. My name is Bob.\".\n     *\n     * @param messagePattern\n     *          The message pattern which will be parsed and formatted\n     * @param arg1\n     *          The argument to be substituted in place of the first formatting\n     *          anchor\n     * @param arg2\n     *          The argument to be substituted in place of the second formatting\n     *          anchor\n     * @return The formatted message",
        "     * 对消息模板做双参数占位符替换。\n     *\n     * @param messagePattern\n     *          The message pattern which will be parsed and formatted\n     * @param arg1\n     *          The argument to be substituted in place of the first formatting\n     *          anchor\n     * @param arg2\n     *          The argument to be substituted in place of the second formatting\n     *          anchor\n     * @return The formatted message",
    ),
    (
        "        // use string builder for better multicore performance",
        "        // 使用 StringBuilder 提升多核下拼接性能",
    ),
    (
        "                // no more variables",
        "                // 无更多占位符",
    ),
    (
        "                if (i == 0) { // this is a simple string",
        "                if (i == 0) { // 纯字符串，无占位符",
    ),
    (
        "                } else { // add the tail string which contains no variables and return\n                    // the result.",
        "                } else { // 追加尾部无占位符片段并返回",
    ),
    (
        "                        L--; // DELIM_START was escaped, thus should not be incremented",
        "                        L--; // 锚点被转义，参数索引不递增",
    ),
    (
        "                        // The escape character preceding the delimiter start is\n                        // itself escaped: \"abc x:\\\\{}\"\n                        // we have to consume one backward slash",
        "                        // 转义符本身也被转义（如 \"abc x:\\{}\"），需消费一个反斜杠",
    ),
    (
        "                    // normal case",
        "                    // 正常替换",
    ),
    (
        "        // append the characters following the last {} pair.",
        "        // 追加最后一个 {} 之后的尾部文本",
    ),
    (
        "    // special treatment of array values was suggested by 'lizongbo'",
        "    // 数组参数的深度展开逻辑由 lizongbo 建议",
    ),
    (
        "            // check for primitive array types because they\n            // unfortunately cannot be cast to Object[]",
        "            // 基本类型数组无法强转为 Object[]，需分别处理",
    ),
    (
        "            // allow repeats in siblings",
        "            // 兄弟节点允许重复引用",
    ),
]

MW = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/node/metric/MetricWriter.java"
FILE_REPLACEMENTS[MW] = [
    (
        "/**\n * This class is responsible for writing {@link MetricNode} to disk:\n * <ol>\n * <li>metric with the same second should write to the same file;</li>\n * <li>single file size must be controlled;</li>\n * <li>file name is like: {@code ${appName}-metrics.log.pid${pid}.yyyy-MM-dd.[number]}</li>\n * <li>metric of different day should in different file;</li>\n * <li>every metric file is accompanied with an index file, which file name is {@code ${metricFileName}.idx}</li>\n * </ol>\n *\n * @author Carpenter Lee\n */",
        "/**\n * 负责将 {@link MetricNode} 持久化到磁盘：\n * <ol>\n * <li>同一秒内的指标写入同一文件；</li>\n * <li>单文件大小受控；</li>\n * <li>文件名形如 {@code ${appName}-metrics.log.pid${pid}.yyyy-MM-dd.[number]}；</li>\n * <li>不同日期的指标分文件存储；</li>\n * <li>每个指标文件配有索引文件，名为 {@code ${metricFileName}.idx}。</li>\n * </ol>\n *\n * @author Carpenter Lee\n */",
    ),
    (
        "     * Note: {@link MetricFileNameComparator}'s implementation relies on the metric file name,\n     * so we should be careful when changing the metric file name.\n     *\n     * @see #formMetricFileName(String, int)\n     */",
        "     * 注意：{@link MetricFileNameComparator} 依赖指标文件名规则，修改命名格式时需同步调整比较器。\n     *\n     * @see #formMetricFileName(String, int)\n     */",
    ),
    (
        "     * file must exist when writing\n     */",
        "     * 写入时指标文件与索引文件必须已存在\n     */",
    ),
    (
        "        // first write, should create file",
        "        // 首次写入需创建文件",
    ),
    (
        "     * A comparator for metric file name. Metric file name is like: <br/>\n     * <pre>\n     * metrics.log.2018-03-06\n     * metrics.log.2018-03-07\n     * metrics.log.2018-03-07.10\n     * metrics.log.2018-03-06.100\n     * </pre>\n     * <p>\n     * File name with the early date is smaller, if date is same, the one with the small file number is smaller.\n     * Note that if the name is an absolute path, only the fileName({@link File#getName()}) part will be considered.\n     * So the above file names should be sorted as: <br/>\n     * <pre>\n     * metrics.log.2018-03-06\n     * metrics.log.2018-03-06.100\n     * metrics.log.2018-03-07\n     * metrics.log.2018-03-07.10\n     *\n     * </pre>\n     * </p>\n     */",
        "     * 指标文件名比较器。文件名示例：<br/>\n     * <pre>\n     * metrics.log.2018-03-06\n     * metrics.log.2018-03-07\n     * metrics.log.2018-03-07.10\n     * metrics.log.2018-03-06.100\n     * </pre>\n     * <p>\n     * 日期较早者更小；日期相同时序号较小者更小。\n     * 绝对路径仅比较 {@link File#getName()} 部分。\n     * 上述文件排序结果为：<br/>\n     * <pre>\n     * metrics.log.2018-03-06\n     * metrics.log.2018-03-06.100\n     * metrics.log.2018-03-07\n     * metrics.log.2018-03-07.10\n     * </pre>\n     * </p>\n     */",
    ),
    (
        "            // in case of file name contains pid, skip it, like Sentinel-Admin-metrics.log.pid22568.2018-12-24",
        "            // 文件名含 pid 时跳过该段，如 Sentinel-Admin-metrics.log.pid22568.2018-12-24",
    ),
    (
        "            // compare date first",
        "            // 先比较日期",
    ),
    (
        "            // same date, compare file number",
        "            // 日期相同再比较文件序号",
    ),
    (
        "     * Get all metric files' name in {@code baseDir}. The file name must like\n     * <pre>\n     * baseFileName + \".yyyy-MM-dd.number\"\n     * </pre>\n     * and not endsWith {@link #METRIC_FILE_INDEX_SUFFIX} or \".lck\".\n     *\n     * @param baseDir      the directory to search.\n     * @param baseFileName the file name pattern.\n     * @return the metric files' absolute path({@link File#getAbsolutePath()})\n     * @throws Exception",
        "     * 列出 {@code baseDir} 下符合 {@code baseFileName + \".yyyy-MM-dd.number\"} 的指标文件，\n     * 排除 {@link #METRIC_FILE_INDEX_SUFFIX} 与 \".lck\" 后缀。\n     *\n     * @param baseDir      the directory to search.\n     * @param baseFileName the file name pattern.\n     * @return the metric files' absolute path({@link File#getAbsolutePath()})\n     * @throws Exception",
    ),
    (
        "     * Test whether fileName matches baseFileName. fileName matches baseFileName when\n     * <pre>\n     * fileName = baseFileName + \".yyyy-MM-dd.number\"\n     * </pre>\n     *\n     * @param fileName     file name\n     * @param baseFileName base file name.\n     * @return if fileName matches baseFileName return true, else return false.",
        "     * 判断 {@code fileName} 是否匹配 {@code baseFileName + \".yyyy-MM-dd.number\"} 模式。\n     *\n     * @param fileName     file name\n     * @param baseFileName base file name.\n     * @return if fileName matches baseFileName return true, else return false.",
    ),
    (
        "            // part is like: \".yyyy-MM-dd.number\", eg. \".2018-12-24.11\"",
        "            // 后缀形如 \".yyyy-MM-dd.number\"，如 \".2018-12-24.11\"",
    ),
    (
        "     * Form metric file name use the specific appName and pid. Note that only\n     * form the file name, not include path.\n     *\n     * Note: {@link MetricFileNameComparator}'s implementation relays on the metric file name,\n     * we should be careful when changing the metric file name.\n     *\n     * @param appName\n     * @param pid\n     * @return metric file name.",
        "     * 根据应用名与 pid 生成指标文件名（不含路径）。\n     * 修改命名规则时需同步维护 {@link MetricFileNameComparator}。\n     *\n     * @param appName\n     * @param pid\n     * @return metric file name.",
    ),
    (
        "        // dot is special char that should be replaced.",
        "        // 应用名中的点号需替换为连字符",
    ),
    (
        "     * Form index file name of the {@code metricFileName}\n     *\n     * @param metricFileName\n     * @return the index file name of the metricFileName",
        "     * 生成指标文件 {@code metricFileName} 对应的索引文件名。\n     *\n     * @param metricFileName\n     * @return the index file name of the metricFileName",
    ),
]

LA = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/slots/statistic/base/LeapArray.java"
FILE_REPLACEMENTS[LA] = [
    (
        " * Basic data structure for statistic metrics in Sentinel.",
        " * Sentinel 统计指标的滑动窗口基础数据结构。",
    ),
    (
        " * Leap array use sliding window algorithm to count data. Each bucket cover {@code windowLengthInMs} time span,\n * and the total time span is {@link #intervalInMs}, so the total bucket amount is:\n * {@code sampleCount = intervalInMs / windowLengthInMs}.",
        " * 滑动窗口将 {@link #intervalInMs} 划分为 {@code sampleCount} 个桶，\n * 每桶跨度 {@code windowLengthInMs = intervalInMs / sampleCount}。",
    ),
    (
        "     * The conditional (predicate) update lock is used only when current bucket is deprecated.",
        "     * 仅在当前桶过期需重置时使用的条件更新锁。",
    ),
    (
        "     * The total bucket count is: {@code sampleCount = intervalInMs / windowLengthInMs}.\n     *\n     * @param sampleCount  bucket count of the sliding window\n     * @param intervalInMs the total time interval of this {@link LeapArray} in milliseconds",
        "     * 桶总数 {@code sampleCount = intervalInMs / windowLengthInMs}。\n     *\n     * @param sampleCount  bucket count of the sliding window\n     * @param intervalInMs the total time interval of this {@link LeapArray} in milliseconds",
    ),
    (
        "     * Get the bucket at current timestamp.\n     *\n     * @return the bucket at current timestamp",
        "     * 获取当前时间戳对应的窗口桶。\n     *\n     * @return the bucket at current timestamp",
    ),
    (
        "     * Create a new statistic value for bucket.\n     *\n     * @param timeMillis current time in milliseconds\n     * @return the new empty bucket",
        "     * 为窗口桶创建新的统计值实例。\n     *\n     * @param timeMillis current time in milliseconds\n     * @return the new empty bucket",
    ),
    (
        "     * Reset given bucket to provided start time and reset the value.\n     *\n     * @param startTime  the start time of the bucket in milliseconds\n     * @param windowWrap current bucket\n     * @return new clean bucket at given start time",
        "     * 将给定桶重置为指定起始时间并清空统计值。\n     *\n     * @param startTime  the start time of the bucket in milliseconds\n     * @param windowWrap current bucket\n     * @return new clean bucket at given start time",
    ),
    (
        "        // Calculate current index so we can map the timestamp to the leap array.",
        "        // 计算索引，将时间戳映射到环形数组",
    ),
    (
        "         * Get bucket item at given time from the array.\n         *\n         * (1) Bucket is absent, then just create a new bucket and CAS update to circular array.\n         * (2) Bucket is up-to-date, then just return the bucket.\n         * (3) Bucket is deprecated, then reset current bucket.",
        "         * 从数组获取对应时间的桶：\n         * (1) 桶不存在则 CAS 创建；\n         * (2) 桶仍有效则直接返回；\n         * (3) 桶已过期则重置。",
    ),
    (
        "                 *            bucket is empty, so create new and update",
        "                 *            桶为空，创建新桶并 CAS 更新",
    ),
    (
        "                 * If the old bucket is absent, then we create a new bucket at {@code windowStart},\n                 * then try to update circular array via a CAS operation. Only one thread can\n                 * succeed to update, while other threads yield its time slice.",
        "                 * 旧桶不存在时在 {@code windowStart} 创建新桶并通过 CAS 写入；\n                 * 仅一个线程成功，其余线程 yield 等待。",
    ),
    (
        "                    // Successfully updated, return the created bucket.",
        "                    // CAS 成功，返回新桶",
    ),
    (
        "                    // Contention failed, the thread will yield its time slice to wait for bucket available.",
        "                    // CAS 竞争失败，yield 等待桶可用",
    ),
    (
        "                 *            startTime of Bucket 3: 800, so it's up-to-date",
        "                 *            桶 3 起始 800，仍在当前窗口内",
    ),
    (
        "                 * If current {@code windowStart} is equal to the start timestamp of old bucket,\n                 * that means the time is within the bucket, so directly return the bucket.",
        "                 * {@code windowStart} 与旧桶起始时间相同，说明仍在该桶时间范围内，直接返回。",
    ),
    (
        "                 *          startTime of Bucket 2: 400, deprecated, should be reset",
        "                 *          桶 2 起始 400，已过期，需重置",
    ),
    (
        "                 * If the start timestamp of old bucket is behind provided time, that means\n                 * the bucket is deprecated. We have to reset the bucket to current {@code windowStart}.\n                 * Note that the reset and clean-up operations are hard to be atomic,\n                 * so we need a update lock to guarantee the correctness of bucket update.\n                 *\n                 * The update lock is conditional (tiny scope) and will take effect only when\n                 * bucket is deprecated, so in most cases it won't lead to performance loss.",
        "                 * 旧桶起始时间落后于当前 {@code windowStart} 表示桶已过期，需重置。\n                 * 重置与清理难以原子化，故用更新锁保证正确性；\n                 * 锁仅在桶过期时生效，通常不影响性能。",
    ),
    (
        "                        // Successfully get the update lock, now we reset the bucket.",
        "                        // 获取更新锁成功，重置桶",
    ),
    (
        "                // Should not go through here, as the provided time is already behind.",
        "                // 不应走到此分支：提供的时间早于旧桶起始时间",
    ),
    (
        "     * Get the previous bucket item before provided timestamp.\n     *\n     * @param timeMillis a valid timestamp in milliseconds\n     * @return the previous bucket item before provided timestamp",
        "     * 获取给定时间戳的前一个窗口桶。\n     *\n     * @param timeMillis a valid timestamp in milliseconds\n     * @return the previous bucket item before provided timestamp",
    ),
    (
        "     * Get the previous bucket item for current timestamp.\n     *\n     * @return the previous bucket item for current timestamp",
        "     * 获取当前时间的前一个窗口桶。\n     *\n     * @return the previous bucket item for current timestamp",
    ),
    (
        "     * Get statistic value from bucket for provided timestamp.\n     *\n     * @param timeMillis a valid timestamp in milliseconds\n     * @return the statistic value if bucket for provided timestamp is up-to-date; otherwise null",
        "     * 获取给定时间戳对应桶的统计值；桶无效时返回 null。\n     *\n     * @param timeMillis a valid timestamp in milliseconds\n     * @return the statistic value if bucket for provided timestamp is up-to-date; otherwise null",
    ),
    (
        "     * Check if a bucket is deprecated, which means that the bucket\n     * has been behind for at least an entire window time span.\n     *\n     * @param windowWrap a non-null bucket\n     * @return true if the bucket is deprecated; otherwise false",
        "     * 判断桶是否已过期（落后至少一整段 {@link #intervalInMs}）。\n     *\n     * @param windowWrap a non-null bucket\n     * @return true if the bucket is deprecated; otherwise false",
    ),
    (
        "     * Get valid bucket list for entire sliding window.\n     * The list will only contain \"valid\" buckets.\n     *\n     * @return valid bucket list for entire sliding window.",
        "     * 返回滑动窗口内全部有效桶列表。\n     *\n     * @return valid bucket list for entire sliding window.",
    ),
    (
        "     * Get all buckets for entire sliding window including deprecated buckets.\n     *\n     * @return all buckets for entire sliding window",
        "     * 返回滑动窗口内全部桶（含已过期桶）。\n     *\n     * @return all buckets for entire sliding window",
    ),
    (
        "     * Get aggregated value list for entire sliding window.\n     * The list will only contain value from \"valid\" buckets.\n     *\n     * @return aggregated value list for entire sliding window",
        "     * 返回滑动窗口内全部有效桶的统计值列表。\n     *\n     * @return aggregated value list for entire sliding window",
    ),
    (
        "     * Get the valid \"head\" bucket of the sliding window for provided timestamp.\n     * Package-private for test.\n     *\n     * @param timeMillis a valid timestamp in milliseconds\n     * @return the \"head\" bucket if it exists and is valid; otherwise null",
        "     * 获取给定时间对应的有效\"头\"桶（包内可见，供测试）。\n     *\n     * @param timeMillis a valid timestamp in milliseconds\n     * @return the \"head\" bucket if it exists and is valid; otherwise null",
    ),
    (
        "     * Get the valid \"head\" bucket of the sliding window at current timestamp.\n     *\n     * @return the \"head\" bucket if it exists and is valid; otherwise null",
        "     * 获取当前时间的有效\"头\"桶。\n     *\n     * @return the \"head\" bucket if it exists and is valid; otherwise null",
    ),
    (
        "     * Get sample count (total amount of buckets).\n     *\n     * @return sample count",
        "     * 获取采样桶数量。\n     *\n     * @return sample count",
    ),
    (
        "     * Get total interval length of the sliding window in milliseconds.\n     *\n     * @return interval in second",
        "     * 获取滑动窗口总时长（毫秒）。\n     *\n     * @return interval in second",
    ),
    (
        "     * Get total interval length of the sliding window.\n     *\n     * @return interval in second",
        "     * 获取滑动窗口总时长（秒）。\n     *\n     * @return interval in second",
    ),
    (
        "        // TODO: default method. Should remove this later.",
        "        // TODO：默认实现，后续应移除",
    ),
    (
        "        // Do nothing by default.",
        "        // 默认无实现",
    ),
    (
        "        // Calculate current bucket start time.",
        "        // 计算当前桶起始时间",
    ),
    (
        "        // Calculate index for expected head time.",
        "        // 计算预期头桶索引",
    ),
]

SL = "sentinel-core/src/main/java/com/alibaba/csp/sentinel/spi/SpiLoader.java"
FILE_REPLACEMENTS[SL] = [
    (
        " * A simple SPI loading facility (refactored since 1.8.1).\n *\n * <p>SPI is short for Service Provider Interface.</p>\n *\n * <p>\n * Service is represented by a single type, that is, a single interface or an abstract class.\n * Provider is implementations of Service, that is, some classes which implement the interface or extends the abstract class.\n * </p>\n *\n * <p>\n * For Service type:\n * Must interface or abstract class.\n * </p>\n *\n * <p>\n * For Provider class:\n * Must have a zero-argument constructor so that they can be instantiated during loading.\n * </p>\n *\n * <p>\n * For Provider configuration file:\n * 1. The file contains a list of fully-qualified binary names of concrete provider classes, one per line.\n * 2. Space and tab characters surrounding each name, as well as blank lines, are ignored.\n * 3. The comment line character is #, all characters following it are ignored.\n * </p>\n *\n *\n * <p>{@code SpiLoader} provide common functions, such as:</p>\n * <ul>\n * <li>Load all Provider instance unsorted/sorted list.</li>\n * <li>Load highest/lowest order priority instance.</li>\n * <li>Load first-found or default instance.</li>\n * <li>Load instance by alias name or provider class.</li>\n * </ul>",
        " * 轻量 SPI 加载器（1.8.1 起重构）。\n *\n * <p>SPI（Service Provider Interface）中 Service 为接口或抽象类，\n * Provider 为实现类，须有无参构造以便实例化。</p>\n *\n * <p>配置文件位于 {@code META-INF/services/<接口全名>}：\n * 每行一个实现类全名；空白行忽略；{@code #} 起为注释。</p>\n *\n * <p>{@code SpiLoader} 支持：</p>\n * <ul>\n * <li>加载全部 Provider（有序/无序）；</li>\n * <li>按 {@link Spi#order()} 取最高/最低优先级；</li>\n * <li>加载首个或默认实现；</li>\n * <li>按别名或类加载指定实例。</li>\n * </ul>",
    ),
    (
        "    // Default path for the folder of Provider configuration file",
        "    // Provider 配置文件默认目录前缀",
    ),
    (
        "    // Cache the SpiLoader instances, key: classname of Service, value: SpiLoader instance",
        "    // SpiLoader 实例缓存，键为 Service 类名",
    ),
    (
        "    // Cache the classes of Provider",
        "    // 已加载的 Provider 类列表",
    ),
    (
        "    // Cache the sorted classes of Provider",
        "    // 按 order 排序后的 Provider 类列表",
    ),
    (
        "     * Cache the classes of Provider, key: aliasName, value: class of Provider.\n     * Note: aliasName is the value of {@link Spi} when the Provider class has {@link Spi} annotation and value is not empty,\n     * otherwise use classname of the Provider.",
        "     * Provider 类缓存：键为别名（{@link Spi#value()} 非空时）或类全名。",
    ),
    (
        "    // Cache the singleton instance of Provider, key: classname of Provider, value: Provider instance",
        "    // Provider 单例实例缓存",
    ),
    (
        "    // Whether this SpiLoader has been loaded, that is, loaded the Provider configuration file",
        "    // 是否已解析 SPI 配置文件",
    ),
    (
        "    // Default provider class",
        "    // 标记 {@link Spi#isDefault()} 的默认 Provider 类",
    ),
    (
        "    // The Service class, must be interface or abstract class",
        "    // Service 接口或抽象类",
    ),
    (
        "     * Create SpiLoader instance via Service class\n     * Cached by className, and load from cache first\n     *\n     * @param service Service class\n     * @param <T>     Service type\n     * @return SpiLoader instance",
        "     * 按 Service 类获取 SpiLoader，按类名缓存。\n     *\n     * @param service Service class\n     * @param <T>     Service type\n     * @return SpiLoader instance",
    ),
    (
        "     * Reset and clear all SpiLoader instances.\n     * Package privilege, used only in test cases.",
        "     * 重置并清空全部 SpiLoader 缓存（包内可见，仅测试使用）。",
    ),
    (
        "    // Private access",
        "    // 私有构造",
    ),
    (
        "     * Load all Provider instances of the specified Service\n     *\n     * @return Provider instances list",
        "     * 加载全部 Provider 实例（配置文件顺序）。\n     *\n     * @return Provider instances list",
    ),
    (
        "     * Load all Provider instances of the specified Service, sorted by order value in class's {@link Spi} annotation\n     *\n     * @return Sorted Provider instances list",
        "     * 加载全部 Provider 实例，按 {@link Spi#order()} 排序。\n     *\n     * @return Sorted Provider instances list",
    ),
    (
        "     * Load highest order priority instance, order value is defined in class's {@link Spi} annotation\n     *\n     * @return Provider instance of highest order priority",
        "     * 加载 order 最小（优先级最高）的 Provider。\n     *\n     * @return Provider instance of highest order priority",
    ),
    (
        "     * Load lowest order priority instance, order value is defined in class's {@link Spi} annotation\n     *\n     * @return Provider instance of lowest order priority",
        "     * 加载 order 最大（优先级最低）的 Provider。\n     *\n     * @return Provider instance of lowest order priority",
    ),
    (
        "     * Load the first-found Provider instance\n     *\n     * @return Provider instance of first-found specific",
        "     * 加载配置文件中首个 Provider。\n     *\n     * @return Provider instance of first-found specific",
    ),
    (
        "     * Load the first-found Provider instance,if not found, return default Provider instance\n     *\n     * @return Provider instance",
        "     * 加载首个非默认 Provider；若无则返回默认实现。\n     *\n     * @return Provider instance",
    ),
    (
        "     * Load default Provider instance\n     * Provider class with @Spi(isDefault = true)\n     *\n     * @return default Provider instance",
        "     * 加载 {@code @Spi(isDefault=true)} 标记的默认 Provider。\n     *\n     * @return default Provider instance",
    ),
    (
        "     * Load instance by specific class type\n     *\n     * @param clazz class type\n     * @return Provider instance",
        "     * 按实现类加载 Provider 实例。\n     *\n     * @param clazz class type\n     * @return Provider instance",
    ),
    (
        "     * Load instance by aliasName of Provider class\n     *\n     * @param aliasName aliasName of Provider class\n     * @return Provider instance",
        "     * 按 {@link Spi#value()} 别名加载 Provider。\n     *\n     * @param aliasName aliasName of Provider class\n     * @return Provider instance",
    ),
    (
        "     * Reset and clear all fields of current SpiLoader instance and remove instance in SPI_LOADER_MAP",
        "     * 清空当前 SpiLoader 并从全局缓存移除",
    ),
    (
        "     * Load the Provider class from Provider configuration file",
        "     * 从 SPI 配置文件加载 Provider 类",
    ),
    (
        "                        // Skip blank line",
        "                        // 跳过空行",
    ),
    (
        "                        // Skip comment line",
        "                        // 跳过注释行",
    ),
    (
        "     * Create Provider instance list\n     *\n     * @param clazzList class types of Providers\n     * @return Provider instance list",
        "     * 批量创建 Provider 实例。\n     *\n     * @param clazzList class types of Providers\n     * @return Provider instance list",
    ),
    (
        "     * Create Provider instance\n     *\n     * @param clazz class type of Provider\n     * @return Provider class",
        "     * 创建 Provider 实例（根据 {@link Spi#isSingleton()} 决定是否单例）。\n     *\n     * @param clazz class type of Provider\n     * @return Provider class",
    ),
    (
        "     * Create Provider instance\n     *\n     * @param clazz     class type of Provider\n     * @param singleton if instance is singleton or prototype\n     * @return Provider instance",
        "     * 创建 Provider 实例。\n     *\n     * @param clazz     class type of Provider\n     * @param singleton if instance is singleton or prototype\n     * @return Provider instance",
    ),
    (
        "     * Close all resources\n     *\n     * @param closeables {@link Closeable} resources",
        "     * 关闭全部 {@link Closeable} 资源。\n     *\n     * @param closeables {@link Closeable} resources",
    ),
    (
        "     * Throw {@link SpiLoaderException} with message\n     *\n     * @param msg error message",
        "     * 抛出带消息的 {@link SpiLoaderException}。\n     *\n     * @param msg error message",
    ),
    (
        "     * Throw {@link SpiLoaderException} with message and Throwable\n     *\n     * @param msg error message",
        "     * 抛出带消息与原因的 {@link SpiLoaderException}。\n     *\n     * @param msg error message",
    ),
]

CFRM = "sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/rule/ClusterFlowRuleManager.java"
FILE_REPLACEMENTS[CFRM] = [
    (
        " * Manager for cluster flow rules.",
        " * 集群流控规则管理器：按 namespace 监听 {@link FlowRule} 动态属性并维护内存规则表。",
    ),
    (
        "     * The default cluster flow rule property supplier that creates a new dynamic property\n     * for a specific namespace to do rule management manually.",
        "     * 默认属性供应器：为 namespace 创建 {@link DynamicSentinelProperty} 以手动管理规则。",
    ),
    (
        "     * (flowId, clusterRule)",
        "     * (flowId, 集群流控规则)",
    ),
    (
        "     * (namespace, [flowId...])",
        "     * (namespace, flowId 集合)",
    ),
    (
        "     * <p>This map (flowId, namespace) is used for getting connected count\n     * when checking a specific rule in {@code ruleId}:</p>\n     *\n     * <pre>\n     * ruleId -> namespace -> connection group -> connected count\n     * </pre>",
        "     * <p>(flowId, namespace) 映射，校验规则 {@code ruleId} 时用于查询连接数：</p>\n     * <pre>\n     * ruleId -> namespace -> connection group -> connected count\n     * </pre>",
    ),
    (
        "     * (namespace, property-listener wrapper)",
        "     * (namespace, 属性与监听器包装)",
    ),
    (
        "     * Cluster flow rule property supplier for a specific namespace.",
        "     * 按 namespace 提供集群流控规则动态属性的供应器。",
    ),
    (
        "        // The server should always support default namespace,\n        // so register a default property for default namespace.",
        "        // 服务端始终支持 default namespace，预注册默认属性",
    ),
    (
        "     * Listen to the {@link SentinelProperty} for cluster {@link FlowRule}s.\n     * The property is the source of cluster {@link FlowRule}s for a specific namespace.\n     *\n     * @param namespace namespace to register",
        "     * 监听指定 namespace 的集群 {@link FlowRule} 动态属性。\n     *\n     * @param namespace namespace to register",
    ),
    (
        "     * Listen to the {@link SentinelProperty} for cluster {@link FlowRule}s if current property for namespace is absent.\n     * The property is the source of cluster {@link FlowRule}s for a specific namespace.\n     *\n     * @param namespace namespace to register",
        "     * namespace 尚无属性时注册集群 {@link FlowRule} 动态属性。\n     *\n     * @param namespace namespace to register",
    ),
    (
        "     * Remove cluster flow rule property for a specific namespace.\n     *\n     * @param namespace valid namespace",
        "     * 移除指定 namespace 的集群流控规则属性。\n     *\n     * @param namespace valid namespace",
    ),
    (
        "     * Get flow rule by rule ID.\n     *\n     * @param id rule ID\n     * @return flow rule",
        "     * 按 flowId 获取集群流控规则。\n     *\n     * @param id rule ID\n     * @return flow rule",
    ),
    (
        "     * Get all cluster flow rules within a specific namespace.\n     *\n     * @param namespace valid namespace\n     * @return cluster flow rules within the provided namespace",
        "     * 获取 namespace 下全部集群流控规则。\n     *\n     * @param namespace valid namespace\n     * @return cluster flow rules within the provided namespace",
    ),
    (
        "     * Load flow rules for a specific namespace. The former rules of the namespace will be replaced.\n     *\n     * @param namespace a valid namespace\n     * @param rules     rule list",
        "     * 加载 namespace 集群流控规则，覆盖原有规则。\n     *\n     * @param namespace a valid namespace\n     * @param rules     rule list",
    ),
    (
        "     * Clear all rules of the provided namespace and reset map.\n     *\n     * @param namespace valid namespace",
        "     * 清空 namespace 下全部规则并重置映射。\n     *\n     * @param namespace valid namespace",
    ),
    (
        "     * Get connected count for associated namespace of given {@code flowId}.\n     *\n     * @param flowId unique flow ID\n     * @return connected count",
        "     * 获取 flowId 对应 namespace 的客户端连接数。\n     *\n     * @param flowId unique flow ID\n     * @return connected count",
    ),
    (
        "            // Flow id should not be null after filtered.",
        "            // 过滤后 flowId 不应为空",
    ),
    (
        "            // Prepare cluster metric from valid flow ID.",
        "            // 为有效 flowId 初始化集群指标",
    ),
    (
        "        // Cleanup unused cluster metrics.",
        "        // 清理不再使用的集群指标",
    ),
]

CSCM = "sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/config/ClusterServerConfigManager.java"
FILE_REPLACEMENTS[CSCM] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群 Token 服务端全局配置管理器：监听传输、namespace 集合与流控统计配置。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "     * Server global transport and scope config.",
        "     * 服务端全局传输与 namespace 范围配置。",
    ),
    (
        "     * Server global flow config.",
        "     * 服务端全局流控统计配置。",
    ),
    (
        "     * Namespace-specific flow config for token server.\n     * Format: (namespace, config).",
        "     * 按 namespace 的流控配置，格式 (namespace, config)。",
    ),
    (
        "     * Property for cluster server global transport configuration.",
        "     * 集群服务端全局传输配置动态属性。",
    ),
    (
        "     * Property for cluster server namespace set.",
        "     * 集群服务端 namespace 集合动态属性。",
    ),
    (
        "     * Property for cluster server global flow control configuration.",
        "     * 集群服务端全局流控统计动态属性。",
    ),
    (
        "     * Register cluster server namespace set dynamic property.\n     *\n     * @param property server namespace set dynamic property",
        "     * 注册集群服务端 namespace 集合动态属性。\n     *\n     * @param property server namespace set dynamic property",
    ),
    (
        "     * Register cluster server transport configuration dynamic property.\n     *\n     * @param property server transport configuration dynamic property",
        "     * 注册集群服务端传输配置动态属性。\n     *\n     * @param property server transport configuration dynamic property",
    ),
    (
        "     * Register cluster server global statistic (flow) configuration dynamic property.\n     *\n     * @param property server flow configuration dynamic property",
        "     * 注册集群服务端全局流控统计配置动态属性。\n     *\n     * @param property server flow configuration dynamic property",
    ),
    (
        "     * Load provided server namespace set to property in memory.\n     *\n     * @param namespaceSet valid namespace set",
        "     * 将 namespace 集合加载到内存属性。\n     *\n     * @param namespaceSet valid namespace set",
    ),
    (
        "     * Load provided server transport configuration to property in memory.\n     *\n     * @param config valid cluster server transport configuration",
        "     * 将传输配置加载到内存属性。\n     *\n     * @param config valid cluster server transport configuration",
    ),
    (
        "     * Load provided server global statistic (flow) configuration to property in memory.\n     *\n     * @param config valid cluster server flow configuration for global",
        "     * 将全局流控统计配置加载到内存属性。\n     *\n     * @param config valid cluster server flow configuration for global",
    ),
    (
        "     * Load server flow config for a specific namespace.\n     *\n     * @param namespace a valid namespace\n     * @param config    valid flow config for the namespace",
        "     * 加载指定 namespace 的流控配置（当前仍写入全局属性）。\n     *\n     * @param namespace a valid namespace\n     * @param config    valid flow config for the namespace",
    ),
    (
        "        // TODO: Support namespace-scope server flow config.",
        "        // TODO：支持按 namespace 的服务端流控配置",
    ),
    (
        "     * Add a transport config observer. The observers will be called as soon as\n     * there are some changes in transport config (e.g. token server port).\n     *\n     * @param observer a valid transport config observer",
        "     * 添加传输配置观察者，端口等变更时回调。\n     *\n     * @param observer a valid transport config observer",
    ),
    (
        "            // TODO: should debounce?",
        "            // TODO：是否需要防抖",
    ),
    (
        "        // Always add the `default` namespace to the namespace set.",
        "        // 始终将 default namespace 加入集合",
    ),
    (
        "            // In embedded server mode, the server itself is also a part of service,\n            // so it should be added to namespace set.\n            // By default, the added namespace is the appName.",
        "            // 嵌入式模式下服务端自身也是客户端，将应用 namespace 加入集合",
    ),
    (
        "                // Remove the cluster rule property for deprecated namespace set.",
        "                // 移除已下线 namespace 的集群规则属性",
    ),
    (
        "            // Register the rule property if needed.",
        "            // 按需注册规则属性",
    ),
    (
        "            // Initialize the global QPS limiter for the namespace.",
        "            // 初始化 namespace 全局 QPS 限流器",
    ),
    (
        "                    // Reset all the metrics.",
        "                    // 窗口参数变更时重置全部指标",
    ),
    (
        "     * Get sample count of provided namespace.\n     *\n     * @param namespace valid namespace\n     * @return the sample count of namespace; if the namespace does not have customized value, use the global value",
        "     * 获取 namespace 采样桶数；无定制值时使用全局配置。\n     *\n     * @param namespace valid namespace\n     * @return the sample count of namespace; if the namespace does not have customized value, use the global value",
    ),
    (
        "     * <p>Set the embedded mode flag for the token server. </p>\n     * <p>\n     * NOTE: developers SHOULD NOT manually invoke this method.\n     * The embedded flag should be initialized by Sentinel when starting token server.\n     * </p>\n     *\n     * @param embedded whether the token server is currently running in embedded mode",
        "     * <p>设置 Token Server 嵌入式模式标志。</p>\n     * <p>注意：开发者不应手动调用；由 Sentinel 启动 Token Server 时初始化。</p>\n     *\n     * @param embedded whether the token server is currently running in embedded mode",
    ),
]

SAC = "sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/client/SentinelApiClient.java"
FILE_REPLACEMENTS[SAC] = [
    (
        " * Communicate with Sentinel client.",
        " * 与 Sentinel 客户端机器通信的 HTTP 异步客户端，封装规则/集群/网关等命令 API。",
    ),
    (
        "     * Check whether target instance (identified by tuple of app-ip:port)\n     * supports the form of \"xxxxx; xx=xx\" in \"Content-Type\" header.\n     * \n     * @param app target app name\n     * @param ip target node's address\n     * @param port target node's port",
        "     * 判断目标实例（app-ip:port）是否支持增强型 Content-Type 头格式。\n     * \n     * @param app target app name\n     * @param ip target node's address\n     * @param port target node's port",
    ),
    (
        "     * Build an `HttpUriRequest` in POST way.\n     * \n     * @param url\n     * @param params\n     * @param supportEnhancedContentType see {@link #isSupportEnhancedContentType(String, String, int)}\n     * @return",
        "     * 构造 POST 请求。\n     * \n     * @param url\n     * @param params\n     * @param supportEnhancedContentType see {@link #isSupportEnhancedContentType(String, String, int)}\n     * @return",
    ),
    (
        "     * With no param\n     * \n     * @param ip\n     * @param port\n     * @param api\n     * @return",
        "     * 无参数命令请求。\n     * \n     * @param ip\n     * @param port\n     * @param api\n     * @return",
    ),
    (
        "     * No app specified, force to GET\n     * \n     * @param ip\n     * @param port\n     * @param api\n     * @param params\n     * @return",
        "     * 未指定 app，强制 GET 请求。\n     * \n     * @param ip\n     * @param port\n     * @param api\n     * @param params\n     * @return",
    ),
    (
        "     * Prefer to execute request using POST\n     * \n     * @param app\n     * @param ip\n     * @param port\n     * @param api\n     * @param params\n     * @return",
        "     * 优先使用 POST 执行命令（旧版本回退 GET）。\n     * \n     * @param app\n     * @param ip\n     * @param port\n     * @param api\n     * @param params\n     * @return",
    ),
    (
        "            // Using GET in older versions, append parameters after url",
        "            // 旧版客户端使用 GET，参数拼在 URL 后",
    ),
    (
        "            // Using POST",
        "            // 新版使用 POST",
    ),
    (
        "     * Fetch cluster node.\n     *\n     * @param ip          ip to fetch\n     * @param port        port of the ip\n     * @param includeZero whether zero value should in the result list.\n     * @return",
        "     * 拉取集群节点指标。\n     *\n     * @param ip          ip to fetch\n     * @param port        port of the ip\n     * @param includeZero whether zero value should in the result list.\n     * @return",
    ),
    (
        "     * Fetch all parameter flow rules from provided machine.\n     *\n     * @param app  application name\n     * @param ip   machine client IP\n     * @param port machine client port\n     * @return all retrieved parameter flow rules\n     * @since 0.2.1",
        "     * 从指定机器拉取全部热点参数流控规则。\n     *\n     * @param app  application name\n     * @param ip   machine client IP\n     * @param port machine client port\n     * @return all retrieved parameter flow rules\n     * @since 0.2.1",
    ),
    (
        "     * Fetch all authority rules from provided machine.\n     *\n     * @param app  application name\n     * @param ip   machine client IP\n     * @param port machine client port\n     * @return all retrieved authority rules\n     * @since 0.2.1",
        "     * 从指定机器拉取全部授权规则。\n     *\n     * @param app  application name\n     * @param ip   machine client IP\n     * @param port machine client port\n     * @return all retrieved authority rules\n     * @since 0.2.1",
    ),
    (
        "     * set rules of the machine. rules == null will return immediately;\n     * rules.isEmpty() means setting the rules to empty.\n     *\n     * @param app\n     * @param ip\n     * @param port\n     * @param rules\n     * @return whether successfully set the rules.",
        "     * 向机器下发规则；{@code rules==null} 直接返回 true，空列表表示清空规则。\n     *\n     * @param app\n     * @param ip\n     * @param port\n     * @param rules\n     * @return whether successfully set the rules.",
    ),
    (
        "    // Cluster related",
        "    // 集群相关 API",
    ),
]

GFC = "sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/gateway/GatewayFlowRuleController.java"
FILE_REPLACEMENTS[GFC] = [
    (
        " * Gateway flow rule Controller for manage gateway flow rules.",
        " * 网关流控规则 REST 控制器：查询/增删改规则并通过 {@link SentinelApiClient} 推送到客户端。",
    ),
]

CAS = "sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/cluster_app_server_list.js"
FILE_REPLACEMENTS[CAS] = [
    (
        "        // tmp for dialog temporary data.",
        "        // 对话框临时数据",
    ),
    (
        "                        // Not belong to this app.",
        "                        // 不属于当前应用",
    ),
    (
        "                        // Belong to this app.",
        "                        // 属于当前应用",
    ),
    (
        "                    // Indicates that it's not belonging to current app.",
        "                    // 标记该 Server 不属于当前应用",
    ),
    (
        "            let belongToApp = serverData.serverType == 0; // don't modify here!",
        "            let belongToApp = serverData.serverType == 0; // 勿改：0 表示应用内 Server",
    ),
    (
        "        // Confirm function for confirm dialog.",
        "        // 确认对话框回调",
    ),
]

IDJS = "sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/identity.js"
FILE_REPLACEMENTS[IDJS] = [
    (
        "    // Fetch all machines by current app name.",
        "    // 按应用名拉取全部机器列表",
    ),
]

SE = "sentinel-transport/sentinel-transport-netty-http/src/main/java/com/alibaba/csp/sentinel/transport/command/codec/StringEncoder.java"
FILE_REPLACEMENTS[SE] = [
    (
        " * Encode a string to a byte array.",
        " * 将字符串按指定字符集编码为字节数组。",
    ),
]

HS = "sentinel-transport/sentinel-transport-netty-http/src/main/java/com/alibaba/csp/sentinel/transport/command/netty/HttpServer.java"
FILE_REPLACEMENTS[HS] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Netty HTTP 命令中心服务端：绑定端口、注册 {@link CommandHandler} 并分发请求。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "                // Will cause the application exit.",
        "                // 端口非法将导致应用退出",
    ),
    (
        "            // loop for an successful binding",
        "            // 循环尝试绑定直至成功",
    ),
    (
        "     * Increase port number every 3 tries.\n     * \n     * @param basePort base port to start\n     * @param retryCount retry count\n     * @return next calculated port",
        "     * 每失败 3 次将端口递增 1。\n     * \n     * @param basePort base port to start\n     * @param retryCount retry count\n     * @return next calculated port",
    ),
]

HSH = "sentinel-transport/sentinel-transport-netty-http/src/main/java/com/alibaba/csp/sentinel/transport/command/netty/HttpServerHandler.java"
FILE_REPLACEMENTS[HSH] = [
    (
        " * Netty-based HTTP server handler for command center.\n *\n * Note: HTTP chunked is not tested!",
        " * 命令中心 Netty HTTP 请求处理器：解析 GET/POST 参数并调用 {@link CommandHandler}。\n *\n * 注意：HTTP 分块传输未充分测试！",
    ),
    (
        "        // Find the matching command handler.",
        "        // 查找匹配的命令处理器",
    ),
    (
        "            // No matching command handler.",
        "            // 无匹配命令",
    ),
    (
        "        // Parse request parameters.",
        "        // 解析 URL 查询参数",
    ),
    (
        "        // Deal with post method, parameter in post has more privilege compared to that in querystring",
        "        // POST 参数优先级高于 QueryString",
    ),
    (
        "            // support multi-part and form-urlencoded",
        "            // 支持 multipart 与 form-urlencoded",
    ),
    (
        "                    data.retain(); // must retain each attr before destroy",
        "                    data.retain(); // destroy 前必须 retain",
    ),
    (
        "        // Parse command name.",
        "        // 解析命令名",
    ),
    (
        "        // Parse body.",
        "        // 解析请求体",
    ),
    (
        "        // Remove the / of the uri as the target(command name)\n        // Usually the uri is start with /",
        "        // 去掉 URI 前缀 / 得到命令名",
    ),
]

HSI = "sentinel-transport/sentinel-transport-netty-http/src/main/java/com/alibaba/csp/sentinel/transport/command/netty/HttpServerInitializer.java"
FILE_REPLACEMENTS[HSI] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Netty HTTP 命令通道初始化器：装配编解码器与 {@link HttpServerHandler}。\n *\n * @author Eric Zhao\n */",
    ),
]

REDIS_COMMON: list[tuple[str, str]] = [
    (" * This class provide a builder to build redis client connection config.", " * Redis 客户端连接配置及 Builder，支持单机/Sentinel/Cluster。"),
    (" * The default redisSentinel port.", " * Sentinel 默认端口。"),
    (" * The default redisCluster port.", " * Cluster 默认端口。"),
    (" * The default redis port.", " * Redis 单机默认端口。"),
    (" * Default timeout: 60 sec", " * 默认超时：60 秒"),
    (" * Default empty constructor.", " * 默认无参构造。"),
    (" * Constructor with host/port and timeout.", " * 指定主机、端口与超时构造。"),
    (" * Returns a new {@link RedisConnectionConfig.Builder} to construct a {@link RedisConnectionConfig}.", " * 返回用于构建 {@link RedisConnectionConfig} 的 {@link RedisConnectionConfig.Builder}。"),
    (" * Returns the host.", " * 返回主机地址。"),
    (" * Sets the Redis host.", " * 设置 Redis 主机。"),
    (" * Returns the Sentinel Master Id.", " * 返回 Sentinel Master ID。"),
    (" * Sets the Sentinel Master Id.", " * 设置 Sentinel Master ID。"),
    (" * Returns the Redis port.", " * 返回 Redis 端口。"),
    (" * Sets the Redis port. Defaults to {@link #DEFAULT_REDIS_PORT}.", " * 设置 Redis 端口，默认 {@link #DEFAULT_REDIS_PORT}。"),
    (" * Returns the password.", " * 返回密码。"),
    (" * Sets the password. Use empty string to skip authentication.", " * 设置密码；空字符串表示跳过认证。"),
    (" * Sets the password. Use empty char array to skip authentication.", " * 设置密码字符数组；空数组表示跳过认证。"),
    (" * Returns the command timeout for synchronous command execution.", " * 返回同步命令执行超时（毫秒）。"),
    (" * Sets the command timeout for synchronous command execution.", " * 设置同步命令执行超时。"),
    (" * Returns the Redis database number. Databases are only available for Redis Standalone and Redis Master/Slave.", " * 返回 Redis 库编号；仅单机/主从模式可用。"),
    (" * Sets the Redis database number. Databases are only available for Redis Standalone and Redis Master/Slave.", " * 设置 Redis 库编号。"),
    (" * Returns the client name.", " * 返回客户端名称。"),
    (" * Sets the client name to be applied on Redis connections.", " * 设置 Redis 连接上的客户端名称。"),
    (" * @return the list of {@link RedisConnectionConfig Redis Sentinel URIs}.", " * @return {@link RedisConnectionConfig Sentinel 节点}列表。"),
    (" * @return the list of {@link RedisConnectionConfig Redis Cluster URIs}.", " * @return {@link RedisConnectionConfig Cluster 节点}列表。"),
    (" * Builder for Redis RedisConnectionConfig.", " * {@link RedisConnectionConfig} 构建器。"),
    (" * Set Redis host. Creates a new builder.", " * 设置 Redis 主机并创建新 Builder。"),
    (" * Set Redis host and port. Creates a new builder", " * 设置 Redis 主机与端口并创建新 Builder。"),
    (" * Set Sentinel host. Creates a new builder.", " * 设置 Sentinel 主机并创建新 Builder。"),
    (" * Set Sentinel host and port. Creates a new builder.", " * 设置 Sentinel 主机与端口并创建新 Builder。"),
    (" * Set Sentinel host and master id. Creates a new builder.", " * 设置 Sentinel 主机与 Master ID 并创建新 Builder。"),
    (" * Set Sentinel host, port and master id. Creates a new builder.", " * 设置 Sentinel 主机、端口与 Master ID 并创建新 Builder。"),
    (" * Add a withRedisSentinel host to the existing builder.", " * 向 Builder 追加 Sentinel 节点。"),
    (" * Add a withRedisSentinel host/port to the existing builder.", " * 向 Builder 追加 Sentinel 主机与端口。"),
    (" * Set Cluster host. Creates a new builder.", " * 设置 Cluster 主机并创建新 Builder。"),
    (" * Set Cluster host and port. Creates a new builder.", " * 设置 Cluster 主机与端口并创建新 Builder。"),
    (" * Add a withRedisCluster host to the existing builder.", " * 向 Builder 追加 Cluster 节点。"),
    (" * Add a withRedisCluster host/port to the existing builder.", " * 向 Builder 追加 Cluster 主机与端口。"),
    (" * Adds host information to the builder. Does only affect Redis URI, cannot be used with Sentinel connections.", " * 设置主机（仅单机模式，不可与 Sentinel 混用）。"),
    (" * Adds port information to the builder. Does only affect Redis URI, cannot be used with Sentinel connections.", " * 设置端口（需先设置主机）。"),
    (" * Configures the database number.", " * 配置库编号。"),
    (" * Configures a client name.", " * 配置客户端名称。"),
    (" * Configures authentication.", " * 配置认证密码。"),
    (" * Configures a timeout.", " * 配置命令超时。"),
    (" * Configures a redisSentinel master Id.", " * 配置 Sentinel Master ID。"),
    (" * Sets the sslEnable.", " * 设置是否启用 SSL。"),
    (" * Sets the trustedCertificatesPath.", " * 设置受信任证书路径。"),
    (" * Sets the trustedCertificatesJksPassword.", " * 设置受信任证书 JKS 密码。"),
    (" * Sets the keyCertChainFilePath.", " * 设置密钥证书链文件路径。"),
    (" * Sets the keyFilePath.", " * 设置私钥文件路径。"),
    (" * Sets the keyFilePassword.", " * 设置私钥文件密码。"),
    (" * @return the RedisConnectionConfig.", " * @return 构建完成的 {@link RedisConnectionConfig}。"),
    (" * Return true for valid port numbers.", " * 判断端口是否在有效范围内。"),
    (" * Gets the value of trustedCertificatesPath.", " * 获取受信任证书路径。"),
    (" * Gets the value of trustedCertificatesJksPassword.", " * 获取受信任证书 JKS 密码。"),
    (" * Gets the value of keyCertChainFilePath.", " * 获取密钥证书链文件路径。"),
    (" * Gets the value of keyFilePath.", " * 获取私钥文件路径。"),
    (" * Gets the value of keyFilePassword.", " * 获取私钥文件密码。"),
    (" * Gets the value of sslEnable.", " * 获取是否启用 SSL。"),
    (" * @return New builder with Redis host/port.", " * @return 含 Redis 主机/端口的 Builder。"),
    (" * @return New builder with Sentinel host/port.", " * @return 含 Sentinel 主机/端口的 Builder。"),
    (" * @return New builder with Cluster host/port.", " * @return 含 Cluster 主机/端口的 Builder。"),
    (" * @return the builder", " * @return Builder 自身"),
    (" * @return the value of Builder", " * @return Builder 自身"),
    (" * @param host the port", " * @param host 主机名"),
    (" * @param port the port", " * @param port 端口"),
    (" * @param host the host name", " * @param host 主机名"),
    (" * @param timeout timeout value . unit is mill seconds", " * @param timeout 超时（毫秒）"),
    (" * @param host    the host", " * @param host 主机"),
    (" * @param port    the port", " * @param port 端口"),
    (" * @param password the password, must not be {@literal null}.", " * @param password 密码，不可为 {@literal null}"),
    (" * @param timeout the command timeout for synchronous command execution.", " * @param timeout 同步命令超时"),
    (" * @param database the Redis database number.", " * @param database Redis 库编号"),
    (" * @param clientName the client name.", " * @param clientName 客户端名称"),
    (" * @param password the password", " * @param password 密码"),
    (" * @param sentinelMasterId redisSentinel master id, must not be empty or {@literal null}", " * @param sentinelMasterId Sentinel Master ID，不可为空"),
    (" * @param masterId redisSentinel master id", " * @param masterId Sentinel Master ID"),
]

RCC = "sentinel-extension/sentinel-datasource-redis/src/main/java/com/alibaba/csp/sentinel/datasource/redis/config/RedisConnectionConfig.java"
FILE_REPLACEMENTS[RCC] = list(REDIS_COMMON)
