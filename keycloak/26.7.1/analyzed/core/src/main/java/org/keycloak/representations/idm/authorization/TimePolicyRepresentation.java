/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.representations.idm.authorization;

/**
 * 时间（time）类型授权策略的 REST 表示，按日期/时间窗口约束访问。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TimePolicyRepresentation extends AbstractPolicyRepresentation {

    /** 生效起始时间（not-before）。 */
    private String notBefore;
    /** 生效截止时间（not-on-or-after）。 */
    private String notOnOrAfter;
    /** 起始日（月内日期）。 */
    private String dayMonth;
    /** 结束日（月内日期）。 */
    private String dayMonthEnd;
    /** 起始月份。 */
    private String month;
    /** 结束月份。 */
    private String monthEnd;
    /** 起始年份。 */
    private String year;
    /** 结束年份。 */
    private String yearEnd;
    /** 起始小时。 */
    private String hour;
    /** 结束小时。 */
    private String hourEnd;
    /** 起始分钟。 */
    private String minute;
    /** 结束分钟。 */
    private String minuteEnd;

    /** @return 固定策略类型 {@code time} */
    @Override
    public String getType() {
        return "time";
    }

    /** @return 生效起始时间 */
    public String getNotBefore() {
        return notBefore;
    }

    /** @param notBefore 生效起始时间 */
    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    /** @return 生效截止时间 */
    public String getNotOnOrAfter() {
        return notOnOrAfter;
    }

    /** @param notOnOrAfter 生效截止时间 */
    public void setNotOnOrAfter(String notOnOrAfter) {
        this.notOnOrAfter = notOnOrAfter;
    }

    /** @return 起始日 */
    public String getDayMonth() {
        return dayMonth;
    }

    /** @param dayMonth 起始日 */
    public void setDayMonth(String dayMonth) {
        this.dayMonth = dayMonth;
    }

    /** @return 结束日 */
    public String getDayMonthEnd() {
        return dayMonthEnd;
    }

    /** @param dayMonthEnd 结束日 */
    public void setDayMonthEnd(String dayMonthEnd) {
        this.dayMonthEnd = dayMonthEnd;
    }

    /** @return 起始月份 */
    public String getMonth() {
        return month;
    }

    /** @param month 起始月份 */
    public void setMonth(String month) {
        this.month = month;
    }

    /** @return 结束月份 */
    public String getMonthEnd() {
        return monthEnd;
    }

    /** @param monthEnd 结束月份 */
    public void setMonthEnd(String monthEnd) {
        this.monthEnd = monthEnd;
    }

    /** @return 起始年份 */
    public String getYear() {
        return year;
    }

    /** @param year 起始年份 */
    public void setYear(String year) {
        this.year = year;
    }

    /** @return 结束年份 */
    public String getYearEnd() {
        return yearEnd;
    }

    /** @param yearEnd 结束年份 */
    public void setYearEnd(String yearEnd) {
        this.yearEnd = yearEnd;
    }

    /** @return 起始小时 */
    public String getHour() {
        return hour;
    }

    /** @param hour 起始小时 */
    public void setHour(String hour) {
        this.hour = hour;
    }

    /** @return 结束小时 */
    public String getHourEnd() {
        return hourEnd;
    }

    /** @param hourEnd 结束小时 */
    public void setHourEnd(String hourEnd) {
        this.hourEnd = hourEnd;
    }

    /** @return 起始分钟 */
    public String getMinute() {
        return minute;
    }

    /** @param minute 起始分钟 */
    public void setMinute(String minute) {
        this.minute = minute;
    }

    /** @return 结束分钟 */
    public String getMinuteEnd() {
        return minuteEnd;
    }

    /** @param minuteEnd 结束分钟 */
    public void setMinuteEnd(String minuteEnd) {
        this.minuteEnd = minuteEnd;
    }
}
