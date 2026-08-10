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

package com.alibaba.nacos.persistence.configuration;

import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.persistence.utils.DatasourcePlatformUtil;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 持久化数据源配置初始化器。
 *
 * <p>在 Spring 上下文启动前根据 platform 与 standalone 模式决定使用外置 DB 还是内嵌存储，并设置 {@link #useExternalDb} 与 {@link #embeddedStorage} 静态标志。</p>
 *
 * @author xiweng.yy
 */
public class DatasourceConfiguration
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    /** 是否使用外置数据库（集群模式默认为 true）。 */
    public static boolean useExternalDb = false;
    
    /** 是否启用内嵌存储，初始值取自 standalone 配置。 */
    public static boolean embeddedStorage = EnvUtil.getStandaloneMode();
    
    public static boolean isUseExternalDb() {
        return useExternalDb;
    }
    
    public static void setUseExternalDb(boolean useExternalDb) {
        DatasourceConfiguration.useExternalDb = useExternalDb;
    }
    
    public static boolean isEmbeddedStorage() {
        return embeddedStorage;
    }
    
    public static void setEmbeddedStorage(boolean embeddedStorage) {
        DatasourceConfiguration.embeddedStorage = embeddedStorage;
    }
    
    private void loadDatasourceConfiguration() {
        // 集群模式默认走外置数据源；platform 非空且非 derby 即视为外置
        String platform = DatasourcePlatformUtil.getDatasourcePlatform("");
        boolean useExternalStorage =
            !PersistenceConstant.EMPTY_DATASOURCE_PLATFORM.equalsIgnoreCase(platform)
                && !PersistenceConstant.DERBY
                    .equalsIgnoreCase(platform);
        setUseExternalDb(useExternalStorage);
        
        // 须在 setUseExternalDb 之后设置 embeddedStorage
        // 单机通常为 true，集群为 false；集群强制 true 则开启分布式内嵌引擎
        // If this value is set to true in cluster mode, nacos's distributed storage engine is turned on
        // default value is depend on ${nacos.standalone}
        
        if (isUseExternalDb()) {
            setEmbeddedStorage(false);
        } else {
            boolean embeddedStorage =
                isEmbeddedStorage() || Boolean.getBoolean(PersistenceConstant.EMBEDDED_STORAGE);
            setEmbeddedStorage(embeddedStorage);
            
            // 未开启内嵌存储时自动升级到外置 DB，与历史行为一致
            // upgraded to the external data source storage, as before
            if (!embeddedStorage) {
                setUseExternalDb(true);
            }
        }
    }
    
    /** ApplicationContextInitializer 入口：加载并固化数据源类型配置。 */
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        loadDatasourceConfiguration();
    }
}
