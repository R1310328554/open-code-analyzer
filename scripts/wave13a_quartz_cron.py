"""Chinese JavaDoc replacements for springframework wave13a QuartzCronField."""

QUARTZ_CRON_FIELD_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "QuartzCronField.java": [
        (
            "/**\n * Extension of {@link CronField} for\n * <a href=\"https://www.quartz-scheduler.org\">Quartz</a>-specific fields.\n * Created using the {@code parse*} methods, uses a {@link TemporalAdjuster}\n * internally.\n *\n * <p>Supports a Quartz day-of-month/week field with an L/# expression. Follows\n * common cron conventions in every other respect, including 0-6 for SUN-SAT\n * (plus 7 for SUN as well). Note that Quartz deviates from the day-of-week\n * convention in cron through 1-7 for SUN-SAT whereas Spring strictly follows\n * cron even in combination with the optional Quartz-specific L/# expressions.\n *\n * @author Arjen Poutsma\n * @since 5.3\n */",
            "/**\n * 面向 <a href=\"https://www.quartz-scheduler.org\">Quartz</a> 特定字段的\n * {@link CronField} 扩展。通过 {@code parse*} 方法创建，内部使用\n * {@link TemporalAdjuster}。\n *\n * <p>支持带 L/# 表达式的 Quartz 日/周字段；其余方面遵循常见 cron 约定，\n * 包括 SUN-SAT 使用 0-6（SUN 也可用 7）。\n * 注意：Quartz 对星期使用 1-7 表示 SUN-SAT，与 cron 不同；\n * Spring 即使结合可选的 Quartz 专用 L/# 表达式也严格遵循 cron 约定。\n *\n * @author Arjen Poutsma\n * @since 5.3\n */",
        ),
        (
            "\t/**\n\t * Constructor for fields that need to roll forward over a different type\n\t * than the type this field represents. See {@link #parseDaysOfWeek(String)}.\n\t */",
            "\t/**\n\t * 用于需跨越与本字段类型不同的类型向前滚动的字段的构造函数。\n\t * 参见 {@link #parseDaysOfWeek(String)}。\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given value is a Quartz day-of-month field.\n\t */",
            "\t/**\n\t * 判断给定值是否为 Quartz 的“日”字段。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a days of months {@code QuartzCronField},\n\t * the fourth entry of a cron expression.\n\t * <p>Expects a \"L\" or \"W\" in the given value.\n\t */",
            "\t/**\n\t * 将给定值解析为 cron 表达式第四项——“日”的 {@code QuartzCronField}。\n\t * <p>要求值中包含 \"L\" 或 \"W\"。\n\t */",
        ),
        (
            "\t/**\n\t * Determine whether the given value is a Quartz day-of-week field.\n\t */",
            "\t/**\n\t * 判断给定值是否为 Quartz 的“星期”字段。\n\t */",
        ),
        (
            "\t/**\n\t * Parse the given value into a days of week {@code QuartzCronField},\n\t * the sixth entry of a cron expression.\n\t * <p>Expects a \"L\" or \"#\" in the given value.\n\t */",
            "\t/**\n\t * 将给定值解析为 cron 表达式第六项——“星期”的 {@code QuartzCronField}。\n\t * <p>要求值中包含 \"L\" 或 \"#\"。\n\t */",
        ),
        (
            "\t/**\n\t * Returns an adjuster that resets to midnight.\n\t */",
            "\t/**\n\t * 返回重置到午夜的调整器。\n\t */",
        ),
        (
            "\t/**\n\t * Returns an adjuster that returns a new temporal set to the last\n\t * day of the current month at midnight.\n\t */",
            "\t/**\n\t * 返回将时间调整到当月最后一天午夜的调整器。\n\t */",
        ),
        (
            "\t/**\n\t * Returns an adjuster that returns the last weekday of the month.\n\t */",
            "\t/**\n\t * 返回当月最后一个工作日的调整器。\n\t */",
        ),
        (
            "\t/**\n\t * Returns a temporal adjuster that finds the nth-to-last day of the month.\n\t * @param offset the negative offset, i.e. -3 means third-to-last\n\t * @return a nth-to-last day-of-month adjuster\n\t */",
            "\t/**\n\t * 返回查找当月倒数第 n 天的 TemporalAdjuster。\n\t * @param offset 负偏移，例如 -3 表示倒数第三天\n\t * @return 倒数第 n 天的“日”调整器\n\t */",
        ),
        (
            "\t/**\n\t * Returns a temporal adjuster that finds the weekday nearest to the given\n\t * day-of-month. If {@code dayOfMonth} falls on a Saturday, the date is\n\t * moved back to Friday; if it falls on a Sunday (or if {@code dayOfMonth}\n\t * is 1 and it falls on a Saturday), it is moved forward to Monday.\n\t * @param dayOfMonth the goal day-of-month\n\t * @return the weekday-nearest-to adjuster\n\t */",
            "\t/**\n\t * 返回查找最接近给定“日”的工作日的 TemporalAdjuster。\n\t * 若 {@code dayOfMonth} 落在周六则回退到周五；\n\t * 若落在周日（或 {@code dayOfMonth} 为 1 且落在周六）则前进到周一。\n\t * @param dayOfMonth 目标“日”\n\t * @return 最近工作日的调整器\n\t */",
        ),
        (
            "\t/**\n\t * Returns a temporal adjuster that finds the last of the given day-of-week\n\t * in a month.\n\t */",
            "\t/**\n\t * 返回查找某月内给定星期最后出现的 TemporalAdjuster。\n\t */",
        ),
        (
            "\t/**\n\t * Returns a temporal adjuster that finds {@code ordinal}-th occurrence of\n\t * the given day-of-week in a month.\n\t */",
            "\t/**\n\t * 返回查找某月内给定星期第 {@code ordinal} 次出现的 TemporalAdjuster。\n\t */",
        ),
        (
            "\t/**\n\t * Rolls back the given {@code result} to midnight. When\n\t * {@code current} has the same day of month as {@code result}, the former\n\t * is returned, to make sure that we don't end up before where we started.\n\t */",
            "\t/**\n\t * 将给定 {@code result} 回退到午夜。\n\t * 当 {@code current} 与 {@code result} 的“日”相同时返回前者，\n\t * 以确保不会回退到起点之前。\n\t */",
        ),
    ],
}
