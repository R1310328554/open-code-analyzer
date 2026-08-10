package org.keycloak.common.util;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 字符串时长解析工具，支持秒、毫秒、ISO-8601 及简写单位（h/m/s/d）。
 */
public class DurationConverter {

    private static final String PERIOD = "P";
    private static final String PERIOD_OF_TIME = "PT";
    public static final Pattern DIGITS = Pattern.compile("^[-+]?\\d+$");
    private static final Pattern DIGITS_AND_UNIT = Pattern.compile("^(?:[-+]?\\d+(?:\\.\\d+)?(?i)[hms])+$");
    private static final Pattern DAYS = Pattern.compile("^[-+]?\\d+(?i)d$");
    private static final Pattern MILLIS = Pattern.compile("^[-+]?\\d+(?i)ms$");

    /**
     * 若 {@code value} 以数字开头，则：
     * <ul>
     * <li>纯数字视为秒数；</li>
     * <li>数字后接 {@code ms} 视为毫秒；</li>
     * <li>数字后接 {@code h}/{@code m}/{@code s} 时前缀 {@code PT} 后调用 {@link Duration#parse(CharSequence)}；</li>
     * <li>数字后接 {@code d} 时前缀 {@code P} 后调用 {@link Duration#parse(CharSequence)}。</li>
     * </ul>
     *
     * 否则直接调用 {@link Duration#parse(CharSequence)}。
     *
     * @param value a string duration
     * @return the parsed {@link Duration}
     * @throws IllegalArgumentException in case of parse failure
     */
    public static Duration parseDuration(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (DIGITS.asPredicate().test(value)) {
            return Duration.ofSeconds(Long.parseLong(value));
        } else if (MILLIS.asPredicate().test(value)) {
            return Duration.ofMillis(Long.parseLong(value.substring(0, value.length() - 2)));
        }

        try {
            if (DIGITS_AND_UNIT.asPredicate().test(value)) {
                return Duration.parse(PERIOD_OF_TIME + value);
            } else if (DAYS.asPredicate().test(value)) {
                return Duration.parse(PERIOD + value);
            }

            return Duration.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 判断给定字符串是否表示正时长。
     *
     * @param value a string duration following the same format as in {@link #parseDuration(String)}
     * @return true if the value represents a positive duration, false otherwise
     */
    public static boolean isPositiveDuration(String value) {
        Duration duration = parseDuration(value);
        return duration != null && !duration.isNegative() && !duration.isZero();
    }
}
