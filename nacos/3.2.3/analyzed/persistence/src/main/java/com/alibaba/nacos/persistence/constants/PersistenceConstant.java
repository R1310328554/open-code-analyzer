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

package com.alibaba.nacos.persistence.constants;

/**
 * 持久化模块常量定义。
 *
 * <p>集中维护数据源平台属性名、Derby 目录、Raft 分组等持久层通用常量。</p>
 *
 * @author xiweng.yy
 */
public class PersistenceConstant {
    
    /** 默认字符编码 UTF-8。 */
    public static final String DEFAULT_ENCODE = "UTF-8";
    
    /** 旧版 Spring Boot 数据源平台属性名，升级后可能移除。 */
    public static final String DATASOURCE_PLATFORM_PROPERTY_OLD = "spring.datasource.platform";
    
    /** 新版 Spring Boot SQL 初始化平台属性名。 */
    public static final String DATASOURCE_PLATFORM_PROPERTY = "spring.sql.init.platform";
    
    /** MySQL 数据源平台标识。 */
    public static final String MYSQL = "mysql";
    
    /** Derby 嵌入式数据库平台标识。 */
    public static final String DERBY = "derby";
    
    public static final String EMPTY_DATASOURCE_PLATFORM = "";
    
    /** 嵌入式存储配置项键名。 */
    public static final String EMBEDDED_STORAGE = "embeddedStorage";
    
    /** Derby 数据文件根目录名。 */
    public static final String DERBY_BASE_DIR = "derby-data";
    
    /** Raft 读等待无超时时的占位标识字符串。 */
    public static final String EXTEND_NEED_READ_UNTIL_HAVE_DATA = "00--0-read-join-0--00";
    
    /** 配置模块 Raft 一致性分组名。 */
    public static final String CONFIG_MODEL_RAFT_GROUP = "nacos_config";
    
}
