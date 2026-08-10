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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.common.utils.DateFormatUtils;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 配置服务端时间工具：提供当前时间的 {@link Timestamp} 与格式化字符串。
 * Time util.
 *
 * @author Nacos
 */
public class TimeUtils {
    
    /** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
    private static final String YYYYMMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    
    /** 获取当前系统时间的 {@link Timestamp} 实例 */
    public static Timestamp getCurrentTime() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    /** 获取当前时间的格式化字符串（yyyy-MM-dd HH:mm:ss） */
    public static String getCurrentTimeStr() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return DateFormatUtils.format(c.getTime(), YYYYMMMDDHHMMSS);
    }
}
