/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期时间格式化工具：封装 {@link SimpleDateFormat} 常用 pattern 常量及
 * {@link #format(Date, String)} 安全格式化（null 参数抛 NPE）。
 * Date and time formatting utilities.
 * @author zzq
 */
public class DateFormatUtils {
    
    private DateFormatUtils() {
    }
    
    /** 日期格式：yyyyMMdd */
    public static final String YYYYMMDD = "yyyyMMdd";
    
    public static final String YYMMDD = "yyMMdd";
    
    public static final String HHMMSS = "HHmmss";
    
    public static final String YYYYMM = "yyyyMM";
    
    /** 日期时间格式：yyyyMMddHHmmss */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    
    public static final String YYYY = "yyyy";
    
    public static final String MM = "MM";
    
    public static final String DD = "dd";
    
    public static final String YYYYMMDDSLASH = "yyyy/MM/dd";
    
    public static final String YYYYMMDDHHMMSSNOMARK = "yyyyMMddHHmmss";
    
    /**
     * Formats a date/time into a specific pattern.
     *
     * @param date  the date to format, not null
     * @param pattern  the pattern to use to format the date, not null
     * @return the formatted date
      * <p>日期格式化常量与方法；详见类级说明。</p>
     */
    public static String format(final Date date, final String pattern) {
        if (date == null) {
            throw new NullPointerException("date must not be null");
        }
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
}
