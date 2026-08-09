/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.redisson.executor;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * Quartz 风格 Cron 表达式解析与下次触发时间计算。
 * <p>
 * 支持秒/分/时/日/月/周/年七段字段，以及 L、W、# 等 Quartz 扩展。
 * 用于 {@link org.redisson.RedissonExecutorService} 的 Cron 调度。
 *
 * @author Sharada Jambula, James House
 * @author Contributions from Mads Henderson
 * @author Refactoring from CronTrigger to CronExpression by Aaron Craven
 */
@SuppressWarnings({"EmptyStatement", "InnerAssignment", "ConstantName",
        "BooleanExpressionComplexity", "NestedIfDepth", "ParenPad",
        "MethodLength", "WhitespaceAfter", "NoClone", "UnnecessaryParentheses", "AvoidInlineConditionals",
        "EqualsAvoidNull", "OneStatementPerLine"})
public final class CronExpression implements Serializable, Cloneable {

    private static final long serialVersionUID = 12423409423L;

    /** 字段索引：秒。 */
    protected static final int SECOND = 0;
    /** 字段索引：分。 */
    protected static final int MINUTE = 1;
    /** 字段索引：时。 */
    protected static final int HOUR = 2;
    /** 字段索引：日（月内）。 */
    protected static final int DAY_OF_MONTH = 3;
    /** 字段索引：月。 */
    protected static final int MONTH = 4;
    /** 字段索引：星期几。 */
    protected static final int DAY_OF_WEEK = 5;
    /** 字段索引：年。 */
    protected static final int YEAR = 6;
    /** 通配符 {@code *} 的内部整型标记。 */
    protected static final int ALL_SPEC_INT = 99; // '*'
    /** 未指定 {@code ?} 的内部整型标记（仅日/周字段）。 */
    protected static final int NO_SPEC_INT = 98; // '?'
    protected static final Integer ALL_SPEC = ALL_SPEC_INT;
    protected static final Integer NO_SPEC = NO_SPEC_INT;

    /** 月份英文缩写 → Calendar 月份索引（0 基）。 */
    protected static final Map<String, Integer> monthMap = new HashMap<String, Integer>(20);
    /** 星期英文缩写 → 1-7（SUN=1）。 */
    protected static final Map<String, Integer> dayMap = new HashMap<String, Integer>(60);
    static {
        monthMap.put("JAN", 0);
        monthMap.put("FEB", 1);
        monthMap.put("MAR", 2);
        monthMap.put("APR", 3);
        monthMap.put("MAY", 4);
        monthMap.put("JUN", 5);
        monthMap.put("JUL", 6);
        monthMap.put("AUG", 7);
        monthMap.put("SEP", 8);
        monthMap.put("OCT", 9);
        monthMap.put("NOV", 10);
        monthMap.put("DEC", 11);

        dayMap.put("SUN", 1);
        dayMap.put("MON", 2);
        dayMap.put("TUE", 3);
        dayMap.put("WED", 4);
        dayMap.put("THU", 5);
        dayMap.put("FRI", 6);
        dayMap.put("SAT", 7);
    }

    /** 原始 Cron 表达式字符串（大写）。 */
    private final String cronExpression;
    /** 解析与计算使用的时区，null 时取系统默认。 */
    private TimeZone timeZone = null;
    /** 允许的秒值集合。 */
    protected transient TreeSet<Integer> seconds;
    /** 允许的分值集合。 */
    protected transient TreeSet<Integer> minutes;
    /** 允许的时值集合。 */
    protected transient TreeSet<Integer> hours;
    /** 允许的日（月内）值集合。 */
    protected transient TreeSet<Integer> daysOfMonth;
    /** 允许的月值集合。 */
    protected transient TreeSet<Integer> months;
    /** 允许的星期值集合。 */
    protected transient TreeSet<Integer> daysOfWeek;
    /** 允许的年值集合。 */
    protected transient TreeSet<Integer> years;

    /** 是否为“最后一个指定星期几”（L 修饰）。 */
    protected transient boolean lastdayOfWeek = false;
    /** 第 N 个指定星期几（# 修饰，1-5）。 */
    protected transient int nthdayOfWeek = 0;
    /** 是否为“当月最后一天”（L 修饰）。 */
    protected transient boolean lastdayOfMonth = false;
    /** 是否使用最近工作日（W 修饰）。 */
    protected transient boolean nearestWeekday = false;
    /** 相对月末最后一天的偏移（L-n）。 */
    protected transient int lastdayOffset = 0;
    /** 表达式是否已解析为各字段集合。 */
    protected transient boolean expressionParsed = false;

    /** 允许的最大年份（当前年 + 100）。 */
    public static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 100;

    /**
     * 根据 Cron 表达式字符串构造实例并立即解析。
     *
     * @param cronExpression 新对象所代表的 cron 表达式字符串
     */
    public CronExpression(String cronExpression) {
        if (cronExpression == null) {
            throw new IllegalArgumentException("cronExpression cannot be null");
        }

        this.cronExpression = cronExpression.toUpperCase(Locale.US);

        try {
            buildExpression(this.cronExpression);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 复制已有 {@code CronExpression} 实例（含时区）。
     *
     * @param expression 待复制的已有 cron 表达式实例
     */
    public CronExpression(CronExpression expression) {
        /*
         * We don't call the other constructor here since we need to swallow the
         * ParseException. We also elide some of the sanity checking as it is
         * not logically trippable.
         */
        this.cronExpression = expression.getCronExpression();
        try {
            buildExpression(cronExpression);
        } catch (ParseException ex) {
            throw new AssertionError();
        }
        if (expression.getTimeZone() != null) {
            setTimeZone((TimeZone) expression.getTimeZone().clone());
        }
    }

    /**
     * 判断给定时间是否满足 cron 表达式（毫秒被忽略）。
     *
     * @param date 待评估的日期时间
     * @return 若给定时间恰好为有效触发点则返回 true
     */
    public boolean isSatisfiedBy(Date date) {
        Calendar testDateCal = Calendar.getInstance(getTimeZone());
        testDateCal.setTime(date);
        testDateCal.set(Calendar.MILLISECOND, 0);
        Date originalDate = testDateCal.getTime();

        testDateCal.add(Calendar.SECOND, -1);

        Date timeAfter = getTimeAfter(testDateCal.getTime());

        return ((timeAfter != null) && (timeAfter.equals(originalDate)));
    }

    /**
     * 返回给定时间之后第一个满足表达式的触发时间。
     *
     * @param date 搜索起点
     * @return 下一个有效触发时间
     */
    public Date getNextValidTimeAfter(Date date) {
        return getTimeAfter(date);
    }

    /**
     * 返回给定时间之后第一个不满足表达式的时间点（性能较差，逐秒推进）。
     *
     * @param date 搜索起点
     * @return 下一个无效时间点的前一秒之后
     */
    public Date getNextInvalidTimeAfter(Date date) {
        long difference = 1000;

        // 对齐到整秒，保证相邻触发点间隔计算准确
        Calendar adjustCal = Calendar.getInstance(getTimeZone());
        adjustCal.setTime(date);
        adjustCal.set(Calendar.MILLISECOND, 0);
        Date lastDate = adjustCal.getTime();

        Date newDate;

        // TODO(QUARTZ-481)：当前实现性能较差，依赖逐次 getTimeAfter 推进

        // 循环取下一个满足点，直到与上次相差超过 1 秒
        // apart. At that point, lastDate is the last valid fire time. We return
        // the second immediately following it.
        while (difference == 1000) {
            newDate = getTimeAfter(lastDate);
            if(newDate == null)
                break;

            difference = newDate.getTime() - lastDate.getTime();

            if (difference == 1000) {
                lastDate = newDate;
            }
        }

        return new Date(lastDate.getTime() + 1000);
    }

    /**
     * 返回表达式解析与计算使用的时区。
     *
     * @return time zone
     */
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    /**
     * 设置表达式解析与计算使用的时区。
     *
     * @param timeZone 时区对象
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * 返回 cron 表达式的字符串形式。
     *
     * @return cron 表达式字符串
     */
    @Override
    public String toString() {
        return cronExpression;
    }

    /**
     * 判断给定字符串能否解析为合法 cron 表达式。
     *
     * @param cronExpression 待校验的表达式
     * @return 合法返回 true
     */
    public static boolean isValidExpression(String cronExpression) {

        try {
            new CronExpression(cronExpression);
        } catch (IllegalArgumentException pe) {
            return false;
        }

        return true;
    }

    /** 校验表达式，不合法时抛出 {@link ParseException}。 */
    public static void validateExpression(String cronExpression) throws ParseException {

        new CronExpression(cronExpression);
    }


    ////////////////////////////////////////////////////////////////////////////
    //
    // 表达式解析
    //
    ////////////////////////////////////////////////////////////////////////////

    /** 将 cron 字符串解析为各字段 TreeSet 及 L/W/# 等修饰标志。 */
    protected void buildExpression(String expression) throws ParseException {
        expressionParsed = true;

        try {

            if (seconds == null) {
                seconds = new TreeSet<Integer>();
            }
            if (minutes == null) {
                minutes = new TreeSet<Integer>();
            }
            if (hours == null) {
                hours = new TreeSet<Integer>();
            }
            if (daysOfMonth == null) {
                daysOfMonth = new TreeSet<Integer>();
            }
            if (months == null) {
                months = new TreeSet<Integer>();
            }
            if (daysOfWeek == null) {
                daysOfWeek = new TreeSet<Integer>();
            }
            if (years == null) {
                years = new TreeSet<Integer>();
            }

            int exprOn = SECOND;

            StringTokenizer exprsTok = new StringTokenizer(expression, " \t",
                    false);

            while (exprsTok.hasMoreTokens() && exprOn <= YEAR) {
                String expr = exprsTok.nextToken().trim();

                // L 不能与逗号分隔的多值日字段混用
                if(exprOn == DAY_OF_MONTH && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(",")) {
                    throw new ParseException("Support for specifying 'L' and 'LW' with other days of the month is not implemented", -1);
                }
                // L 不能与逗号分隔的多值周字段混用
                if(exprOn == DAY_OF_WEEK && expr.indexOf('L') != -1 && expr.length() > 1  && expr.contains(",")) {
                    throw new ParseException("Support for specifying 'L' with other days of the week is not implemented", -1);
                }
                if(exprOn == DAY_OF_WEEK && expr.indexOf('#') != -1 && expr.indexOf('#', expr.indexOf('#') +1) != -1) {
                    throw new ParseException("Support for specifying multiple \"nth\" days is not implemented.", -1);
                }

                StringTokenizer vTok = new StringTokenizer(expr, ",");
                while (vTok.hasMoreTokens()) {
                    String v = vTok.nextToken();
                    storeExpressionVals(0, v, exprOn);
                }

                exprOn++;
            }

            if (exprOn <= DAY_OF_WEEK) {
                throw new ParseException("Unexpected end of expression.",
                        expression.length());
            }

            if (exprOn <= YEAR) {
                storeExpressionVals(0, "*", YEAR);
            }

            TreeSet<Integer> dow = getSet(DAY_OF_WEEK);
            TreeSet<Integer> dom = getSet(DAY_OF_MONTH);

            // 日字段与周字段不能同时指定具体值（须其一为 ?）
            boolean dayOfMSpec = !dom.contains(NO_SPEC);
            boolean dayOfWSpec = !dow.contains(NO_SPEC);

            if (!dayOfMSpec || dayOfWSpec) {
                if (!dayOfWSpec || dayOfMSpec) {
                    throw new ParseException(
                            "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.", 0);
                }
            }
        } catch (ParseException pe) {
            throw pe;
        } catch (Exception e) {
            throw new ParseException("Illegal cron expression format ("
                    + e.toString() + ")", 0);
        }
    }

    /** 解析单个字段片段（*、?、范围、步进、L/W/# 等）并写入对应集合。 */
    protected int storeExpressionVals(int pos, String s, int type)
            throws ParseException {

        int incr = 0;
        int i = skipWhiteSpace(pos, s);
        if (i >= s.length()) {
            return i;
        }
        char c = s.charAt(i);
        if ((c >= 'A') && (c <= 'Z') && (!s.equals("L")) && (!s.equals("LW")) && (!s.matches("^L-[0-9]*[W]?"))) {
            String sub = s.substring(i, i + 3);
            int sval = -1;
            int eval = -1;
            if (type == MONTH) {
                sval = getMonthNumber(sub) + 1;
                if (sval <= 0) {
                    throw new ParseException("Invalid Month value: '" + sub + "'", i);
                }
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getMonthNumber(sub) + 1;
                        if (eval <= 0) {
                            throw new ParseException("Invalid Month value: '" + sub + "'", i);
                        }
                    }
                }
            } else if (type == DAY_OF_WEEK) {
                sval = getDayOfWeekNumber(sub);
                if (sval < 0) {
                    throw new ParseException("Invalid Day-of-Week value: '"
                            + sub + "'", i);
                }
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getDayOfWeekNumber(sub);
                        if (eval < 0) {
                            throw new ParseException(
                                    "Invalid Day-of-Week value: '" + sub
                                            + "'", i);
                        }
                    } else if (c == '#') {
                        try {
                            i += 4;
                            nthdayOfWeek = Integer.parseInt(s.substring(i));
                            if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            throw new ParseException(
                                    "A numeric value between 1 and 5 must follow the '#' option",
                                    i);
                        }
                    } else if (c == 'L') {
                        lastdayOfWeek = true;
                        i++;
                    }
                }

            } else {
                throw new ParseException(
                        "Illegal characters for this position: '" + sub + "'",
                        i);
            }
            if (eval != -1) {
                incr = 1;
            }
            addToSet(sval, eval, incr, type);
            return (i + 3);
        }

        if (c == '?') {
            i++;
            if ((i + 1) < s.length()
                    && (s.charAt(i) != ' ' && s.charAt(i + 1) != '\t')) {
                throw new ParseException("Illegal character after '?': "
                        + s.charAt(i), i);
            }
            if (type != DAY_OF_WEEK && type != DAY_OF_MONTH) {
                throw new ParseException(
                        "'?' can only be specfied for Day-of-Month or Day-of-Week.",
                        i);
            }
            if (type == DAY_OF_WEEK && !lastdayOfMonth) {
                int val = daysOfMonth.last();
                if (val == NO_SPEC_INT) {
                    throw new ParseException(
                            "'?' can only be specfied for Day-of-Month -OR- Day-of-Week.",
                            i);
                }
            }

            addToSet(NO_SPEC_INT, -1, 0, type);
            return i;
        }

        if (c == '*' || c == '/') {
            if (c == '*' && (i + 1) >= s.length()) {
                addToSet(ALL_SPEC_INT, -1, incr, type);
                return i + 1;
            } else if (c == '/'
                    && ((i + 1) >= s.length() || s.charAt(i + 1) == ' ' || s
                    .charAt(i + 1) == '\t')) {
                throw new ParseException("'/' must be followed by an integer.", i);
            } else if (c == '*') {
                i++;
            }
            c = s.charAt(i);
            if (c == '/') { // 解析步进增量 /n
                i++;
                if (i >= s.length()) {
                    throw new ParseException("Unexpected end of string.", i);
                }

                incr = getNumericValue(s, i);

                i++;
                if (incr > 10) {
                    i++;
                }
                if (incr > 59 && (type == SECOND || type == MINUTE)) {
                    throw new ParseException("Increment > 60 : " + incr, i);
                } else if (incr > 23 && (type == HOUR)) {
                    throw new ParseException("Increment > 24 : " + incr, i);
                } else if (incr > 31 && (type == DAY_OF_MONTH)) {
                    throw new ParseException("Increment > 31 : " + incr, i);
                } else if (incr > 7 && (type == DAY_OF_WEEK)) {
                    throw new ParseException("Increment > 7 : " + incr, i);
                } else if (incr > 12 && (type == MONTH)) {
                    throw new ParseException("Increment > 12 : " + incr, i);
                }
            } else {
                incr = 1;
            }

            addToSet(ALL_SPEC_INT, -1, incr, type);
            return i;
        } else if (c == 'L') {
            i++;
            if (type == DAY_OF_MONTH) {
                lastdayOfMonth = true;
            }
            if (type == DAY_OF_WEEK) {
                addToSet(7, 7, 0, type);
            }
            if(type == DAY_OF_MONTH && s.length() > i) {
                c = s.charAt(i);
                if(c == '-') {
                    ValueSet vs = getValue(0, s, i+1);
                    lastdayOffset = vs.value;
                    if(lastdayOffset > 30)
                        throw new ParseException("Offset from last day must be <= 30", i+1);
                    i = vs.pos;
                }
                if(s.length() > i) {
                    c = s.charAt(i);
                    if(c == 'W') {
                        nearestWeekday = true;
                        i++;
                    }
                }
            }
            return i;
        } else if (c >= '0' && c <= '9') {
            int val = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                addToSet(val, -1, -1, type);
            } else {
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(val, s, i);
                    val = vs.value;
                    i = vs.pos;
                }
                i = checkNext(i, s, val, type);
                return i;
            }
        } else {
            throw new ParseException("Unexpected character: " + c, i);
        }

        return i;
    }

    /** 处理数字后的 L、W、#、-、/ 等后缀修饰。 */
    protected int checkNext(int pos, String s, int val, int type)
            throws ParseException {

        int end = -1;
        int i = pos;

        if (i >= s.length()) {
            addToSet(val, end, -1, type);
            return i;
        }

        char c = s.charAt(pos);

        if (c == 'L') {
            if (type == DAY_OF_WEEK) {
                if(val < 1 || val > 7)
                    throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
                lastdayOfWeek = true;
            } else {
                throw new ParseException("'L' option is not valid here. (pos=" + i + ")", i);
            }
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == 'W') {
            if (type == DAY_OF_MONTH) {
                nearestWeekday = true;
            } else {
                throw new ParseException("'W' option is not valid here. (pos=" + i + ")", i);
            }
            if(val > 31)
                throw new ParseException("The 'W' option does not make sense with values larger than 31 (max number of days in a month)", i);
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == '#') {
            if (type != DAY_OF_WEEK) {
                throw new ParseException("'#' option is not valid here. (pos=" + i + ")", i);
            }
            i++;
            try {
                nthdayOfWeek = Integer.parseInt(s.substring(i));
                if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new ParseException(
                        "A numeric value between 1 and 5 must follow the '#' option",
                        i);
            }

            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == '-') {
            i++;
            c = s.charAt(i);
            int v = Integer.parseInt(String.valueOf(c));
            end = v;
            i++;
            if (i >= s.length()) {
                addToSet(val, end, 1, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v, s, i);
                end = vs.value;
                i = vs.pos;
            }
            if (i < s.length() && ((c = s.charAt(i)) == '/')) {
                i++;
                c = s.charAt(i);
                int v2 = Integer.parseInt(String.valueOf(c));
                i++;
                if (i >= s.length()) {
                    addToSet(val, end, v2, type);
                    return i;
                }
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(v2, s, i);
                    int v3 = vs.value;
                    addToSet(val, end, v3, type);
                    i = vs.pos;
                    return i;
                } else {
                    addToSet(val, end, v2, type);
                    return i;
                }
            } else {
                addToSet(val, end, 1, type);
                return i;
            }
        }

        if (c == '/') {
            i++;
            c = s.charAt(i);
            int v2 = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                addToSet(val, end, v2, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v2, s, i);
                int v3 = vs.value;
                addToSet(val, end, v3, type);
                i = vs.pos;
                return i;
            } else {
                throw new ParseException("Unexpected character '" + c + "' after '/'", i);
            }
        }

        addToSet(val, end, 0, type);
        i++;
        return i;
    }

    /** 返回原始 cron 表达式。 */
    public String getCronExpression() {
        return cronExpression;
    }

    /** 返回各字段解析结果的调试摘要（多行文本）。 */
    public String getExpressionSummary() {
        StringBuilder buf = new StringBuilder();

        buf.append("seconds: ");
        buf.append(getExpressionSetSummary(seconds));
        buf.append("\n");
        buf.append("minutes: ");
        buf.append(getExpressionSetSummary(minutes));
        buf.append("\n");
        buf.append("hours: ");
        buf.append(getExpressionSetSummary(hours));
        buf.append("\n");
        buf.append("daysOfMonth: ");
        buf.append(getExpressionSetSummary(daysOfMonth));
        buf.append("\n");
        buf.append("months: ");
        buf.append(getExpressionSetSummary(months));
        buf.append("\n");
        buf.append("daysOfWeek: ");
        buf.append(getExpressionSetSummary(daysOfWeek));
        buf.append("\n");
        buf.append("lastdayOfWeek: ");
        buf.append(lastdayOfWeek);
        buf.append("\n");
        buf.append("nearestWeekday: ");
        buf.append(nearestWeekday);
        buf.append("\n");
        buf.append("NthDayOfWeek: ");
        buf.append(nthdayOfWeek);
        buf.append("\n");
        buf.append("lastdayOfMonth: ");
        buf.append(lastdayOfMonth);
        buf.append("\n");
        buf.append("years: ");
        buf.append(getExpressionSetSummary(years));
        buf.append("\n");

        return buf.toString();
    }

    /** 将 TreeSet 字段值格式化为 *、? 或逗号分隔列表。 */
    protected String getExpressionSetSummary(java.util.Set<Integer> set) {

        if (set.contains(NO_SPEC)) {
            return "?";
        }
        if (set.contains(ALL_SPEC)) {
            return "*";
        }

        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = set.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.ArrayList<Integer> list) {

        if (list.contains(NO_SPEC)) {
            return "?";
        }
        if (list.contains(ALL_SPEC)) {
            return "*";
        }

        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = list.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    /** 跳过空白字符。 */
    protected int skipWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t'); i++) {
            ;
        }

        return i;
    }

    /** 查找下一个空白位置（用于解析数字）。 */
    protected int findNextWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) != ' ' || s.charAt(i) != '\t'); i++) {
            ;
        }

        return i;
    }

    /** 向指定字段集合添加单值、范围或步进序列。 */
    protected void addToSet(int val, int end, int incr, int type)
            throws ParseException {

        TreeSet<Integer> set = getSet(type);

        if (type == SECOND || type == MINUTE) {
            if ((val < 0 || val > 59 || end > 59) && (val != ALL_SPEC_INT)) {
                throw new ParseException(
                        "Minute and Second values must be between 0 and 59",
                        -1);
            }
        } else if (type == HOUR) {
            if ((val < 0 || val > 23 || end > 23) && (val != ALL_SPEC_INT)) {
                throw new ParseException(
                        "Hour values must be between 0 and 23", -1);
            }
        } else if (type == DAY_OF_MONTH) {
            if ((val < 1 || val > 31 || end > 31) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT)) {
                throw new ParseException(
                        "Day of month values must be between 1 and 31", -1);
            }
        } else if (type == MONTH) {
            if ((val < 1 || val > 12 || end > 12) && (val != ALL_SPEC_INT)) {
                throw new ParseException(
                        "Month values must be between 1 and 12", -1);
            }
        } else if (type == DAY_OF_WEEK) {
            if ((val == 0 || val > 7 || end > 7) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT)) {
                throw new ParseException(
                        "Day-of-Week values must be between 1 and 7", -1);
            }
        }

        if ((incr == 0 || incr == -1) && val != ALL_SPEC_INT) {
            if (val != -1) {
                set.add(val);
            } else {
                set.add(NO_SPEC);
            }

            return;
        }

        int startAt = val;
        int stopAt = end;

        if (val == ALL_SPEC_INT && incr <= 0) {
            incr = 1;
            set.add(ALL_SPEC); // put in a marker, but also fill values
        }

        if (type == SECOND || type == MINUTE) {
            if (stopAt == -1) {
                stopAt = 59;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 0;
            }
        } else if (type == HOUR) {
            if (stopAt == -1) {
                stopAt = 23;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 0;
            }
        } else if (type == DAY_OF_MONTH) {
            if (stopAt == -1) {
                stopAt = 31;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == MONTH) {
            if (stopAt == -1) {
                stopAt = 12;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == DAY_OF_WEEK) {
            if (stopAt == -1) {
                stopAt = 7;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == YEAR) {
            if (stopAt == -1) {
                stopAt = MAX_YEAR;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1970;
            }
        }

        // 范围 end < start 时需跨周期取模（如跨月、跨周）
        // the next day, month etc. This is done by adding the maximum amount for that
        // type, and using modulus max to determine the value being added.
        int max = -1;
        if (stopAt < startAt) {
            switch (type) {
                case       SECOND : max = 60; break;
                case       MINUTE : max = 60; break;
                case         HOUR : max = 24; break;
                case        MONTH : max = 12; break;
                case  DAY_OF_WEEK : max = 7;  break;
                case DAY_OF_MONTH : max = 31; break;
                case         YEAR : throw new IllegalArgumentException("Start year must be less than stop year");
                default           : throw new IllegalArgumentException("Unexpected type encountered");
            }
            stopAt += max;
        }

        for (int i = startAt; i <= stopAt; i += incr) {
            if (max == -1) {
                // ie: there's no max to overflow over
                set.add(i);
            } else {
                // take the modulus to get the real value
                int i2 = i % max;

                // 1-indexed ranges should not include 0, and should include their max
                if (i2 == 0 && (type == MONTH || type == DAY_OF_WEEK || type == DAY_OF_MONTH) ) {
                    i2 = max;
                }

                set.add(i2);
            }
        }
    }

    /** 按字段类型返回对应 TreeSet。 */
    TreeSet<Integer> getSet(int type) {
        switch (type) {
            case SECOND:
                return seconds;
            case MINUTE:
                return minutes;
            case HOUR:
                return hours;
            case DAY_OF_MONTH:
                return daysOfMonth;
            case MONTH:
                return months;
            case DAY_OF_WEEK:
                return daysOfWeek;
            case YEAR:
                return years;
            default:
                return null;
        }
    }

    /** 从字符串当前位置解析连续数字。 */
    protected ValueSet getValue(int v, String s, int i) {
        char c = s.charAt(i);
        StringBuilder s1 = new StringBuilder(String.valueOf(v));
        while (c >= '0' && c <= '9') {
            s1.append(c);
            i++;
            if (i >= s.length()) {
                break;
            }
            c = s.charAt(i);
        }
        ValueSet val = new ValueSet();

        val.pos = (i < s.length()) ? i : i + 1;
        val.value = Integer.parseInt(s1.toString());
        return val;
    }

    /** 解析从 i 开始的整数字面值。 */
    protected int getNumericValue(String s, int i) {
        int endOfVal = findNextWhiteSpace(i, s);
        String val = s.substring(i, endOfVal);
        return Integer.parseInt(val);
    }

    /** 将三字母月份缩写转为 Calendar 月份索引。 */
    protected int getMonthNumber(String s) {
        Integer integer = monthMap.get(s);

        if (integer == null) {
            return -1;
        }

        return integer;
    }

    /** 将三字母星期缩写转为 1-7 数值。 */
    protected int getDayOfWeekNumber(String s) {
        Integer integer = dayMap.get(s);

        if (integer == null) {
            return -1;
        }

        return integer;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // 下次触发时间计算
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * 计算 strictly after {@code afterTime} 的第一个满足表达式的时间点。
     * <p>
     * 自秒→分→时→日→月→年逐级推进，处理 L/W/# 与闰年等边界。
     */
    public Date getTimeAfter(Date afterTime) {

        // 仅按公历GregorianCalendar 计算
        Calendar cl = new java.util.GregorianCalendar(getTimeZone());

        // 起点加 1 秒，因为求的是严格“之后”的触发点
        // given time
        afterTime = new Date(afterTime.getTime() + 1000);
        // 毫秒归零
        cl.setTime(afterTime);
        cl.set(Calendar.MILLISECOND, 0);

        boolean gotOne = false;
        // 循环推进各字段直到找到合法组合或超出 MAX_YEAR
        while (!gotOne) {

            //if (endTime != null && cl.getTime().after(endTime)) return null;
            if(cl.get(Calendar.YEAR) > 2999) { // 防止无限循环
                return null;
            }

            SortedSet<Integer> st = null;
            int t = 0;

            int sec = cl.get(Calendar.SECOND);
            int min = cl.get(Calendar.MINUTE);

            // 匹配秒字段.
            st = seconds.tailSet(sec);
            if (st != null && st.size() != 0) {
                sec = st.first();
            } else {
                sec = seconds.first();
                min++;
                cl.set(Calendar.MINUTE, min);
            }
            cl.set(Calendar.SECOND, sec);

            min = cl.get(Calendar.MINUTE);
            int hr = cl.get(Calendar.HOUR_OF_DAY);
            t = -1;

            // 匹配分字段.
            st = minutes.tailSet(min);
            if (st != null && st.size() != 0) {
                t = min;
                min = st.first();
            } else {
                min = minutes.first();
                hr++;
            }
            if (min != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, min);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.MINUTE, min);

            hr = cl.get(Calendar.HOUR_OF_DAY);
            int day = cl.get(Calendar.DAY_OF_MONTH);
            t = -1;

            // 匹配时字段.
            st = hours.tailSet(hr);
            if (st != null && st.size() != 0) {
                t = hr;
                hr = st.first();
            } else {
                hr = hours.first();
                day++;
            }
            if (hr != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.DAY_OF_MONTH, day);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.HOUR_OF_DAY, hr);

            day = cl.get(Calendar.DAY_OF_MONTH);
            int mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            t = -1;
            int tmon = mon;

            // 匹配日/周字段（二者互斥，仅一种规则生效）.
            boolean dayOfMSpec = !daysOfMonth.contains(NO_SPEC);
            boolean dayOfWSpec = !daysOfWeek.contains(NO_SPEC);
            if (dayOfMSpec && !dayOfWSpec) { // 按“月内日”规则
                st = daysOfMonth.tailSet(day);
                if (lastdayOfMonth) {
                    if(!nearestWeekday) {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        day -= lastdayOffset;
                        if(t > day) {
                            mon++;
                            if(mon > 12) {
                                mon = 1;
                                tmon = 3333; // ensure test of mon != tmon further below fails
                                cl.add(Calendar.YEAR, 1);
                            }
                            day = 1;
                        }
                    } else {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        day -= lastdayOffset;

                        java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
                        tcal.set(Calendar.SECOND, 0);
                        tcal.set(Calendar.MINUTE, 0);
                        tcal.set(Calendar.HOUR_OF_DAY, 0);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

                        int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        int dow = tcal.get(Calendar.DAY_OF_WEEK);

                        if(dow == Calendar.SATURDAY && day == 1) {
                            day += 2;
                        } else if(dow == Calendar.SATURDAY) {
                            day -= 1;
                        } else if(dow == Calendar.SUNDAY && day == ldom) {
                            day -= 2;
                        } else if(dow == Calendar.SUNDAY) {
                            day += 1;
                        }

                        tcal.set(Calendar.SECOND, sec);
                        tcal.set(Calendar.MINUTE, min);
                        tcal.set(Calendar.HOUR_OF_DAY, hr);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        Date nTime = tcal.getTime();
                        if(nTime.before(afterTime)) {
                            day = 1;
                            mon++;
                        }
                    }
                } else if(nearestWeekday) {
                    t = day;
                    day = daysOfMonth.first();

                    java.util.Calendar tcal = java.util.Calendar.getInstance(getTimeZone());
                    tcal.set(Calendar.SECOND, 0);
                    tcal.set(Calendar.MINUTE, 0);
                    tcal.set(Calendar.HOUR_OF_DAY, 0);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

                    int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    int dow = tcal.get(Calendar.DAY_OF_WEEK);

                    if(dow == Calendar.SATURDAY && day == 1) {
                        day += 2;
                    } else if(dow == Calendar.SATURDAY) {
                        day -= 1;
                    } else if(dow == Calendar.SUNDAY && day == ldom) {
                        day -= 2;
                    } else if(dow == Calendar.SUNDAY) {
                        day += 1;
                    }


                    tcal.set(Calendar.SECOND, sec);
                    tcal.set(Calendar.MINUTE, min);
                    tcal.set(Calendar.HOUR_OF_DAY, hr);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    Date nTime = tcal.getTime();
                    if(nTime.before(afterTime)) {
                        day = daysOfMonth.first();
                        mon++;
                    }
                } else if (st != null && st.size() != 0) {
                    t = day;
                    day = st.first();
                    // make sure we don't over-run a short month, such as february
                    int lastDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    if (day > lastDay) {
                        day = daysOfMonth.first();
                        mon++;
                    }
                } else {
                    day = daysOfMonth.first();
                    mon++;
                }

                if (day != t || mon != tmon) {
                    cl.set(Calendar.SECOND, 0);
                    cl.set(Calendar.MINUTE, 0);
                    cl.set(Calendar.HOUR_OF_DAY, 0);
                    cl.set(Calendar.DAY_OF_MONTH, day);
                    cl.set(Calendar.MONTH, mon - 1);
                    // '- 1' because calendar is 0-based for this field, and we
                    // are 1-based
                    continue;
                }
            } else if (dayOfWSpec && !dayOfMSpec) { // 按“星期几”规则
                if (lastdayOfWeek) { // 当月最后一个指定星期几
                    // the month?
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // did we already miss the
                        // last one?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    }

                    // find date of last occurrence of this day in this month...
                    while ((day + daysToAdd + 7) <= lDay) {
                        daysToAdd += 7;
                    }

                    day += daysToAdd;

                    if (daysToAdd > 0) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are not promoting the month
                        continue;
                    }

                } else if (nthdayOfWeek != 0) { // 第 N 个指定星期几
                    // are we looking for the Nth XXX day in the month?
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    } else if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    boolean dayShifted = false;
                    if (daysToAdd > 0) {
                        dayShifted = true;
                    }

                    day += daysToAdd;
                    int weekOfMonth = day / 7;
                    if (day % 7 > 0) {
                        weekOfMonth++;
                    }

                    daysToAdd = (nthdayOfWeek - weekOfMonth) * 7;
                    day += daysToAdd;
                    if (daysToAdd < 0
                            || day > getLastDayOfMonth(mon, cl
                            .get(Calendar.YEAR))) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0 || dayShifted) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are NOT promoting the month
                        continue;
                    }
                } else {
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    st = daysOfWeek.tailSet(cDow);
                    if (st != null && st.size() > 0) {
                        dow = st.first();
                    }

                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // will we pass the end of
                        // the month?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0) { // are we swithing days?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day + daysToAdd);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' because calendar is 0-based for this field,
                        // and we are 1-based
                        continue;
                    }
                }
            } else { // dayOfWSpec && !dayOfMSpec
                throw new UnsupportedOperationException(
                        "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
            }
            cl.set(Calendar.DAY_OF_MONTH, day);

            mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            int year = cl.get(Calendar.YEAR);
            t = -1;

            // 超出 MAX_YEAR 则无法产生更多触发点
            // but keep looping...
            if (year > MAX_YEAR) {
                return null;
            }

            // 匹配月字段.
            st = months.tailSet(mon);
            if (st != null && st.size() != 0) {
                t = mon;
                mon = st.first();
            } else {
                mon = months.first();
                year++;
            }
            if (mon != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, mon - 1);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.MONTH, mon - 1);
            // '- 1' because calendar is 0-based for this field, and we are
            // 1-based

            year = cl.get(Calendar.YEAR);
            t = -1;

            // 匹配年字段.
            st = years.tailSet(year);
            if (st != null && st.size() != 0) {
                t = year;
                year = st.first();
            } else {
                return null; // ran out of years...
            }

            if (year != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, 0);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.YEAR, year);

            gotOne = true;
        } // while( !gotOne )

        return cl.getTime();
    }

    /**
     * 设置小时并处理夏令时跳变（若设置失败则尝试 hour+1）。
     *
     * @param cal 待操作的 Calendar
     * @param hour 目标小时
     */
    protected void setCalendarHour(Calendar cal, int hour) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        if (cal.get(java.util.Calendar.HOUR_OF_DAY) != hour && hour != 24) {
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour + 1);
        }
    }

    /** 返回 endTime 之前最后一个触发点（未实现，恒返回 null）。 */
    public Date getTimeBefore(Date endTime) {
        // FUTURE_TODO: implement QUARTZ-423
        return null;
    }

    /** 返回最后一次触发时间（未实现，恒返回 null）。 */
    public Date getFinalFireTime() {
        // FUTURE_TODO: implement QUARTZ-423
        return null;
    }

    /** 判断公历闰年。 */
    protected boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0));
    }

    /** 返回指定年月的最后一天（考虑闰年 2 月）。 */
    protected int getLastDayOfMonth(int monthNum, int year) {

        switch (monthNum) {
            case 1:
                return 31;
            case 2:
                return (isLeapYear(year)) ? 29 : 28;
            case 3:
                return 31;
            case 4:
                return 30;
            case 5:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case 8:
                return 31;
            case 9:
                return 30;
            case 10:
                return 31;
            case 11:
                return 30;
            case 12:
                return 31;
            default:
                throw new IllegalArgumentException("Illegal month number: "
                        + monthNum);
        }
    }


    /** 反序列化后重新解析 cron 表达式（transient 字段不持久化）。 */
    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException, ClassNotFoundException {

        stream.defaultReadObject();
        try {
            buildExpression(cronExpression);
        } catch (Exception ignore) {
        } // never happens
    }

    /** 克隆为新 CronExpression 实例。 */
    @Override
    @Deprecated
    public Object clone() {
        return new CronExpression(this);
    }
}

@SuppressWarnings("VisibilityModifier")
/** 解析数字时的临时结果：数值与下一个解析位置。 */
class ValueSet {
    /** 解析得到的整数值。 */
    public int value;

    /** 下一个待解析字符索引。 */
    public int pos;
}
