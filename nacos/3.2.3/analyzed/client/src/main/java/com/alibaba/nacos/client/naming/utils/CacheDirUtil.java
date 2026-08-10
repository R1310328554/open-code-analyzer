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

package com.alibaba.nacos.client.naming.utils;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.File;

import com.alibaba.nacos.client.env.NacosClientProperties;

/**
 * 命名客户端磁盘缓存目录工具。
 *
 * <p>根据 JVM 属性、客户端配置与 namespace 解析本地快照目录路径，供 {@link com.alibaba.nacos.client.naming.cache.DiskCache} 使用。</p>
 *
 * @author zongkang.guo
 */
public class CacheDirUtil {
    
    /** 全局缓存目录（init 后有效）。 */
    private static String cacheDir;
    
    /** JVM 快照根路径属性键。 */
    private static final String JM_SNAPSHOT_PATH_PROPERTY = "JM.SNAPSHOT.PATH";
    
    /** 缓存路径中的 nacos 子目录名。 */
    private static final String FILE_PATH_NACOS = "nacos";
    
    /** 缓存路径中的 naming 子目录名。 */
    private static final String FILE_PATH_NAMING = "naming";
    
    /** 用户主目录 JVM 属性键。 */
    private static final String USER_HOME_PROPERTY = "user.home";
    
    /**
     * 初始化并返回命名磁盘缓存目录。
     *
     * <p>优先使用 {@code JM.SNAPSHOT.PATH}，否则回退至 {@code user.home}/nacos/naming/{namespace}。</p>
     *
     * @param namespace 命名空间 ID
     * @param properties 客户端配置
     * @return 缓存目录绝对路径
     */
    public static String initCacheDir(String namespace, NacosClientProperties properties) {
        
        String jmSnapshotPath = properties.getProperty(JM_SNAPSHOT_PATH_PROPERTY);
        
        String namingCacheRegistryDir = "";
        if (properties.getProperty(PropertyKeyConst.NAMING_CACHE_REGISTRY_DIR) != null) {
            namingCacheRegistryDir =
                File.separator
                    + properties.getProperty(PropertyKeyConst.NAMING_CACHE_REGISTRY_DIR);
        }
        
        if (!StringUtils.isBlank(jmSnapshotPath)) {
            cacheDir = jmSnapshotPath + File.separator + FILE_PATH_NACOS + namingCacheRegistryDir
                + File.separator
                + FILE_PATH_NAMING + File.separator + namespace;
        } else {
            cacheDir =
                properties.getProperty(USER_HOME_PROPERTY) + File.separator + FILE_PATH_NACOS
                    + namingCacheRegistryDir
                    + File.separator + FILE_PATH_NAMING + File.separator + namespace;
        }
        
        return cacheDir;
    }
    
    /** 返回最近一次 init 设置的缓存目录。 */
    public static String getCacheDir() {
        return cacheDir;
    }
}
