"""Large-file JavaDoc replacements for wave-12 batch [20:40]."""

LARGE_FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CronExpression.java": [
        (
            "/**\n * Representation of a\n * <a href=\"https://www.manpagez.com/man/5/crontab/\">crontab expression</a>\n * that can calculate the next time it matches.\n *\n * <p>{@code CronExpression} instances are created through {@link #parse(String)};\n * the next match is determined with {@link #next(Temporal)}.\n *\n * <p>Supports a Quartz day-of-month/week field with an L/# expression. Follows\n * common cron conventions in every other respect, including 0-6 for SUN-SAT\n * (plus 7 for SUN as well). Note that Quartz deviates from the day-of-week\n * convention in cron through 1-7 for SUN-SAT whereas Spring strictly follows\n * cron even in combination with the optional Quartz-specific L/# expressions.\n *\n * @author Arjen Poutsma\n * @since 5.3\n * @see CronTrigger\n */",
            "/**\n * 可计算下次匹配时间的\n * <a href=\"https://www.manpagez.com/man/5/crontab/\">crontab 表达式</a>表示。\n *\n * <p>{@code CronExpression} 实例通过 {@link #parse(String)} 创建；\n * 下次匹配时间由 {@link #next(Temporal)} 确定。\n *\n * <p>支持带 L/# 表达式的 Quartz 日/周字段。其余方面遵循常见 cron 约定，\n * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。\n * 注意 Quartz 在 cron 中对星期几使用 1-7 表示 SUN-SAT，\n * 而 Spring 即使结合可选 Quartz 专有 L/# 表达式也严格遵循 cron 约定。\n *\n * @author Arjen Poutsma\n * @since 5.3\n * @see CronTrigger\n */",
        ),
        (
            "\t/**\n\t * Parse the given\n\t * <a href=\"https://www.manpagez.com/man/5/crontab/\">crontab expression</a>\n\t * string into a {@code CronExpression}.\n\t * The string has six single space-separated time and date fields:",
            "\t/**\n\t * 将给定\n\t * <a href=\"https://www.manpagez.com/man/5/crontab/\">crontab 表达式</a>\n\t * 字符串解析为 {@code CronExpression}。\n\t * 字符串包含六个以单个空格分隔的日期时间字段：",
        ),
        (
            "\t * <p>The following rules apply:",
            "\t * <p>适用以下规则：",
        ),
        (
            "\t * <li>\n\t * A field may be an asterisk ({@code *}), which always stands for\n\t * \"first-last\". For the \"day of the month\" or \"day of the week\" fields, a\n\t * question mark ({@code ?}) may be used instead of an asterisk.\n\t * </li>",
            "\t * <li>\n\t * 字段可以是星号 ({@code *})，始终表示“首尾范围”。\n\t * 对于“日”或“星期几”字段，可用问号 ({@code ?}) 代替星号。\n\t * </li>",
        ),
        (
            "\t * <li>\n\t * Ranges of numbers are expressed by two numbers separated with a hyphen\n\t * ({@code -}). The specified range is inclusive.\n\t * </li>",
            "\t * <li>\n\t * 数字范围由连字符 ({@code -}) 分隔的两个数字表示，范围包含两端。\n\t * </li>",
        ),
        (
            "\t * <li>Following a range (or {@code *}) with {@code /n} specifies\n\t * the interval of the number's value through the range.\n\t * </li>",
            "\t * <li>在范围（或 {@code *}）后接 {@code /n} 表示该范围内数值的步进间隔。\n\t * </li>",
        ),
        (
            "\t * <li>\n\t * English names can also be used for the \"month\" and \"day of week\" fields.\n\t * Use the first three letters of the particular day or month (case does not\n\t * matter).\n\t * </li>",
            "\t * <li>\n\t * “月”和“星期几”字段也可使用英文名称。\n\t * 使用对应日或月的前三个字母（大小写不敏感）。\n\t * </li>",
        ),
        (
            "\t * <li>\n\t * The \"day of month\" and \"day of week\" fields can contain a\n\t * {@code L}-character, which stands for \"last\", and has a different meaning\n\t * in each field:",
            "\t * <li>\n\t * “日”和“星期几”字段可包含 {@code L} 字符，表示“最后”，\n\t * 在各字段中含义不同：",
        ),
        (
            "\t * <li>\n\t * In the \"day of month\" field, {@code L} stands for \"the last day of the\n\t * month\". If followed by an negative offset (i.e. {@code L-n}), it means\n\t * \"{@code n}th-to-last day of the month\". If followed by {@code W} (i.e.\n\t * {@code LW}), it means \"the last weekday of the month\".\n\t * </li>",
            "\t * <li>\n\t * 在“日”字段中，{@code L} 表示“当月最后一天”。\n\t * 若后跟负偏移（即 {@code L-n}），表示“当月倒数第 {@code n} 天”。\n\t * 若后跟 {@code W}（即 {@code LW}），表示“当月最后一个工作日”。\n\t * </li>",
        ),
        (
            "\t * <li>\n\t * In the \"day of week\" field, {@code dL} or {@code DDDL} stands for\n\t * \"the last day of week {@code d} (or {@code DDD}) in the month\".\n\t * </li>",
            "\t * <li>\n\t * 在“星期几”字段中，{@code dL} 或 {@code DDDL} 表示\n\t * “当月最后一个星期 {@code d}（或 {@code DDD}）”。\n\t * </li>",
        ),
        (
            "\t * <li>\n\t * The \"day of month\" field can be {@code nW}, which stands for \"the nearest\n\t * weekday to day of the month {@code n}\".\n\t * If {@code n} falls on Saturday, this yields the Friday before it.\n\t * If {@code n} falls on Sunday, this yields the Monday after,\n\t * which also happens if {@code n} is {@code 1} and falls on a Saturday\n\t * (i.e. {@code 1W} stands for \"the first weekday of the month\").\n\t * </li>",
            "\t * <li>\n\t * “日”字段可为 {@code nW}，表示“距当月第 {@code n} 日最近的工作日”。\n\t * 若 {@code n} 为周六，则取前一个周五。\n\t * 若 {@code n} 为周日，则取后一个周一；\n\t * 若 {@code n} 为 {@code 1} 且落在周六，同样取后一个周一\n\t * （即 {@code 1W} 表示“当月第一个工作日”）。\n\t * </li>",
        ),
        (
            "\t * <li>\n\t * The \"day of week\" field can be {@code d#n} (or {@code DDD#n}), which\n\t * stands for \"the {@code n}-th day of week {@code d} (or {@code DDD}) in\n\t * the month\".\n\t * </li>",
            "\t * <li>\n\t * “星期几”字段可为 {@code d#n}（或 {@code DDD#n}），\n\t * 表示“当月第 {@code n} 个星期 {@code d}（或 {@code DDD}）”。\n\t * </li>",
        ),
        (
            "\t * <p>Example expressions:",
            "\t * <p>示例表达式：",
        ),
        (
            "\t * <li>{@code \"0 0 * * * *\"} = the top of every hour of every day</li>",
            "\t * <li>{@code \"0 0 * * * *\"} = 每天每小时的整点</li>",
        ),
        (
            "\t * <li><code>\"*&#47;10 * * * * *\"</code> = every ten seconds</li>",
            "\t * <li><code>\"*&#47;10 * * * * *\"</code> = 每十秒</li>",
        ),
        (
            "\t * <li>{@code \"0 0 8-10 * * *\"} = 8, 9 and 10 o'clock of every day</li>",
            "\t * <li>{@code \"0 0 8-10 * * *\"} = 每天 8、9、10 点</li>",
        ),
        (
            "\t * <li>{@code \"0 0 6,19 * * *\"} = 6:00 AM and 7:00 PM every day</li>",
            "\t * <li>{@code \"0 0 6,19 * * *\"} = 每天 6:00 和 19:00</li>",
        ),
        (
            "\t * <li>{@code \"0 0/30 8-10 * * *\"} = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day</li>",
            "\t * <li>{@code \"0 0/30 8-10 * * *\"} = 每天 8:00、8:30、9:00、9:30、10:00 和 10:30</li>",
        ),
        (
            "\t * <li>{@code \"0 0 9-17 * * MON-FRI\"} = on the hour nine-to-five weekdays</li>",
            "\t * <li>{@code \"0 0 9-17 * * MON-FRI\"} = 工作日上午 9 点至下午 5 点整点</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 25 12 ?\"} = every Christmas Day at midnight</li>",
            "\t * <li>{@code \"0 0 0 25 12 ?\"} = 每年圣诞节午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 L * *\"} = last day of the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 L * *\"} = 每月最后一天午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 L-3 * *\"} = third-to-last day of the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 L-3 * *\"} = 每月倒数第三天午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 1W * *\"} = first weekday of the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 1W * *\"} = 每月第一个工作日午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 LW * *\"} = last weekday of the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 LW * *\"} = 每月最后一个工作日午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 * * 5L\"} = last Friday of the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 * * 5L\"} = 每月最后一个周五午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 * * THUL\"} = last Thursday of the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 * * THUL\"} = 每月最后一个周四午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 ? * 5#2\"} = the second Friday in the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 ? * 5#2\"} = 每月第二个周五午夜</li>",
        ),
        (
            "\t * <li>{@code \"0 0 0 ? * MON#1\"} = the first Monday in the month at midnight</li>",
            "\t * <li>{@code \"0 0 0 ? * MON#1\"} = 每月第一个周一午夜</li>",
        ),
        (
            "\t * <p>The following macros are also supported.",
            "\t * <p>还支持以下宏。",
        ),
        (
            "\t * <li>{@code \"@yearly\"} (or {@code \"@annually\"}) to run un once a year, i.e. {@code \"0 0 0 1 1 *\"}</li>",
            "\t * <li>{@code \"@yearly\"}（或 {@code \"@annually\"}）每年运行一次，即 {@code \"0 0 0 1 1 *\"}</li>",
        ),
        (
            "\t * <li>{@code \"@monthly\"} to run once a month, i.e. {@code \"0 0 0 1 * *\"}</li>",
            "\t * <li>{@code \"@monthly\"} 每月运行一次，即 {@code \"0 0 0 1 * *\"}</li>",
        ),
        (
            "\t * <li>{@code \"@weekly\"} to run once a week, i.e. {@code \"0 0 0 * * 0\"}</li>",
            "\t * <li>{@code \"@weekly\"} 每周运行一次，即 {@code \"0 0 0 * * 0\"}</li>",
        ),
        (
            "\t * <li>{@code \"@daily\"} (or {@code \"@midnight\"}) to run once a day, i.e. {@code \"0 0 0 * * *\"}</li>",
            "\t * <li>{@code \"@daily\"}（或 {@code \"@midnight\"}）每天运行一次，即 {@code \"0 0 0 * * *\"}</li>",
        ),
        (
            "\t * <li>{@code \"@hourly\"} to run once an hour, i.e. {@code \"0 0 * * * *\"}</li>",
            "\t * <li>{@code \"@hourly\"} 每小时运行一次，即 {@code \"0 0 * * * *\"}</li>",
        ),
        (
            "\t * @param expression the expression string to parse\n\t * @return the parsed {@code CronExpression} object\n\t * @throws IllegalArgumentException in the expression does not conform to\n\t * the cron format\n\t */",
            "\t * @param expression 待解析的表达式字符串\n\t * @return 解析后的 {@code CronExpression} 对象\n\t * @throws IllegalArgumentException 表达式不符合 cron 格式时\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given string represents a valid cron expression.\n\t * @param expression the expression to evaluate\n\t * @return {@code true} if the given expression is a valid cron expression\n\t * @since 5.3.8\n\t */",
            "\t/**\n\t * 判断给定字符串是否为有效的 cron 表达式。\n\t * @param expression 待评估的表达式\n\t * @return 若给定表达式有效则返回 {@code true}\n\t * @since 5.3.8\n\t */",
        ),
        (
            "\t/**\n\t * Calculate the next {@link Temporal} that matches this expression.\n\t * @param temporal the seed value\n\t * @param <T> the type of temporal\n\t * @return the next temporal that matches this expression, or {@code null}\n\t * if no such temporal can be found\n\t */",
            "\t/**\n\t * 计算匹配本表达式的下一个 {@link Temporal}。\n\t * @param temporal 种子值\n\t * @param <T> 时间类型\n\t * @return 匹配本表达式的下一个 temporal，找不到则返回 {@code null}\n\t */",
        ),
        (
            "\t/**\n\t * Return the expression string used to create this {@code CronExpression}.\n\t */",
            "\t/**\n\t * 返回用于创建本 {@code CronExpression} 的表达式字符串。\n\t */",
        ),
    ],
    "CronField.java": [
        (
            "/**\n * Single field in a cron pattern. Created using the {@code parse*} methods,\n * the main and only entry point is {@link #nextOrSame(Temporal)}.\n *\n * <p>Supports a Quartz day-of-month/week field with an L/# expression. Follows\n * common cron conventions in every other respect, including 0-6 for SUN-SAT\n * (plus 7 for SUN as well). Note that Quartz deviates from the day-of-week\n * convention in cron through 1-7 for SUN-SAT whereas Spring strictly follows\n * cron even in combination with the optional Quartz-specific L/# expressions.\n *\n * @author Arjen Poutsma\n * @since 5.3\n */",
            "/**\n * cron 模式中的单个字段。通过 {@code parse*} 方法创建，\n * 主要且唯一的入口为 {@link #nextOrSame(Temporal)}。\n *\n * <p>支持带 L/# 表达式的 Quartz 日/周字段。其余方面遵循常见 cron 约定，\n * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。\n * 注意 Quartz 在 cron 中对星期几使用 1-7 表示 SUN-SAT，\n * 而 Spring 即使结合可选 Quartz 专有 L/# 表达式也严格遵循 cron 约定。\n *\n * @author Arjen Poutsma\n * @since 5.3\n */",
        ),
        (
            "\t/**\n\t * Return a {@code CronField} enabled for 0 nanoseconds.\n\t */",
            "\t/**\n\t * 返回启用 0 纳秒的 {@code CronField}。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a seconds {@code CronField}, the first entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为秒 {@code CronField}，即 cron 表达式的第一项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a minutes {@code CronField}, the second entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为分 {@code CronField}，即 cron 表达式的第二项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into an hours {@code CronField}, the third entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为时 {@code CronField}，即 cron 表达式的第三项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a days of months {@code CronField}, the fourth entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为日 {@code CronField}，即 cron 表达式的第四项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a month {@code CronField}, the fifth entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为月 {@code CronField}，即 cron 表达式的第五项。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a days of week {@code CronField}, the sixth entry of a cron expression.\n\t */",
            "\t/**\n\t * 将给定值解析为星期几 {@code CronField}，即 cron 表达式的第六项。\n\t */",
        ),
        (
            "\t/**\n\t * Get the next or same {@link Temporal} in the sequence matching this\n\t * cron field.\n\t * @param temporal the seed value\n\t * @return the next or same temporal matching the pattern\n\t */",
            "\t/**\n\t * 获取序列中匹配本 cron 字段的下一个或相同的 {@link Temporal}。\n\t * @param temporal 种子值\n\t * @return 匹配模式的下一个或相同 temporal\n\t */",
        ),
        (
            "\t/**\n\t * Represents the type of cron field, i.e. seconds, minutes, hours,\n\t * day-of-month, month, day-of-week.\n\t */",
            "\t/**\n\t * 表示 cron 字段类型，即秒、分、时、日、月、星期几。\n\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the value of this type for the given temporal.\n\t\t * @return the value of this type\n\t\t */",
            "\t\t/**\n\t\t * 返回给定 temporal 上本类型的值。\n\t\t * @return 本类型的值\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Return the general range of this type. For instance, this method\n\t\t * will return 0-31 for {@link #MONTH}.\n\t\t * @return the range of this field\n\t\t */",
            "\t\t/**\n\t\t * 返回本类型的通用范围。例如，{@link #MONTH} 将返回 0-31。\n\t\t * @return 本字段的范围\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Check whether the given value is valid, i.e. whether it falls in\n\t\t * {@linkplain #range() range}.\n\t\t * @param value the value to check\n\t\t * @return the value that was passed in\n\t\t * @throws IllegalArgumentException if the given value is invalid\n\t\t */",
            "\t\t/**\n\t\t * 检查给定值是否有效，即是否落在 {@linkplain #range() 范围}内。\n\t\t * @param value 待检查的值\n\t\t * @return 传入的值\n\t\t * @throws IllegalArgumentException 给定值无效时\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Elapse the given temporal for the difference between the current\n\t\t * value of this field and the goal value. Typically, the returned\n\t\t * temporal will have the given goal as the current value for this type,\n\t\t * but this is not the case for {@link #DAY_OF_MONTH}.\n\t\t * @param temporal the temporal to elapse\n\t\t * @param goal the goal value\n\t\t * @param <T> the type of temporal\n\t\t * @return the elapsed temporal, typically with {@code goal} as value\n\t\t * for this type.\n\t\t */",
            "\t\t/**\n\t\t * 将给定 temporal 推进本字段当前值与目标值之差。\n\t\t * 通常返回的 temporal 在本类型上具有给定目标值，\n\t\t * 但 {@link #DAY_OF_MONTH} 例外。\n\t\t * @param temporal 待推进的 temporal\n\t\t * @param goal 目标值\n\t\t * @param <T> 时间类型\n\t\t * @return 推进后的 temporal，通常本类型值为 {@code goal}\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Roll forward the give temporal until it reaches the next higher\n\t\t * order field. Calling this method is equivalent to calling\n\t\t * {@link #elapseUntil(Temporal, int)} with goal set to the\n\t\t * minimum value of this field's range.\n\t\t * @param temporal the temporal to roll forward\n\t\t * @param <T> the type of temporal\n\t\t * @return the rolled forward temporal\n\t\t */",
            "\t\t/**\n\t\t * 将给定 temporal 向前滚动至下一更高阶字段。\n\t\t * 调用本方法等价于以本字段范围最小值为 goal 调用\n\t\t * {@link #elapseUntil(Temporal, int)}。\n\t\t * @param temporal 待滚动的 temporal\n\t\t * @param <T> 时间类型\n\t\t * @return 滚动后的 temporal\n\t\t */",
        ),
        (
            "\t\t/**\n\t\t * Reset this and all lower order fields of the given temporal to their\n\t\t * minimum value. For instance for {@link #MINUTE}, this method\n\t\t * resets nanos, seconds, <strong>and</strong> minutes to 0.\n\t\t * @param temporal the temporal to reset\n\t\t * @param <T> the type of temporal\n\t\t * @return the reset temporal\n\t\t */",
            "\t\t/**\n\t\t * 将给定 temporal 的本字段及所有低阶字段重置为最小值。\n\t\t * 例如 {@link #MINUTE} 会将纳秒、秒<strong>和</strong>分重置为 0。\n\t\t * @param temporal 待重置的 temporal\n\t\t * @param <T> 时间类型\n\t\t * @return 重置后的 temporal\n\t\t */",
        ),
    ],
}
