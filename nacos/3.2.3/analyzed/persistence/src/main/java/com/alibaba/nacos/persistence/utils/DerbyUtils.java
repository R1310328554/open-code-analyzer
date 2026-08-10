/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.persistence.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derby 嵌入式数据库 SQL 工具类。
 *
 * <p>Derby 表名默认大写，外部 MySQL 等库的 INSERT 语句导入 Derby 时需做大小写与反引号转换。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class DerbyUtils {
    
    /** 匹配 INSERT INTO ... VALUES 片段的正则表达式。 */
    private static final String INSERT_INTO_VALUES = "(INSERT INTO .+? VALUES)";
    
    /** 预编译的 INSERT 语句匹配模式。 */
    private static final Pattern INSERT_INTO_PATTERN = Pattern.compile(INSERT_INTO_VALUES);
    
    /**
     * 将外部库的 INSERT 语句转换为 Derby 兼容格式。
     *
     * <p>将 INSERT INTO 段转为大写并去除反引号，同时去掉末尾分号。</p>
     *
     * @param sql 外部数据库原始 INSERT SQL
     * @return 适配 Derby 的 INSERT SQL
     */
    /** 执行 INSERT 语句校正，无匹配时原样返回。 */
    public static String insertStatementCorrection(String sql) {
        Matcher matcher = INSERT_INTO_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        final String target = matcher.group(0);
        final String upperCase = target.toUpperCase().replace("`", "");
        return sql.replaceFirst(INSERT_INTO_VALUES, upperCase).replace(";", "");
    }
    
}
