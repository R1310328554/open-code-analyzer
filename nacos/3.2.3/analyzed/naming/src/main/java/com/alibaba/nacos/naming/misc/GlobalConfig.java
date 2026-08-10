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

package com.alibaba.nacos.naming.misc;

import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Component;

import static com.alibaba.nacos.naming.constants.Constants.DATA_WARMUP;
import static com.alibaba.nacos.naming.constants.Constants.EMPTY_SERVICE_CLEAN_INTERVAL;
import static com.alibaba.nacos.naming.constants.Constants.EMPTY_SERVICE_EXPIRED_TIME;
import static com.alibaba.nacos.naming.constants.Constants.EXPIRED_METADATA_CLEAN_INTERVAL;
import static com.alibaba.nacos.naming.constants.Constants.EXPIRED_METADATA_EXPIRED_TIME;
import static com.alibaba.nacos.naming.constants.Constants.EXPIRE_INSTANCE;

/**
 * Naming 全局配置读取器（Distro 协议及相关清理策略）。
 *
 * <p>从 {@link EnvUtil} 读取数据预热、实例过期、空服务清理、过期元数据清理及模糊订阅上限等运行时参数。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
@Component
public class GlobalConfig {
    
    /** 是否启用数据预热。 */
    public boolean isDataWarmup() {
        return EnvUtil.getProperty(DATA_WARMUP, Boolean.class, false);
    }
    
    /** 是否启用实例过期清理。 */
    public boolean isExpireInstance() {
        return EnvUtil.getProperty(EXPIRE_INSTANCE, Boolean.class, true);
    }
    
    /** 空服务清理任务执行间隔（毫秒）。 */
    public static Long getEmptyServiceCleanInterval() {
        return EnvUtil.getProperty(EMPTY_SERVICE_CLEAN_INTERVAL, Long.class, 60000L);
    }
    
    /** 空服务判定过期时间（毫秒）。 */
    public static Long getEmptyServiceExpiredTime() {
        return EnvUtil.getProperty(EMPTY_SERVICE_EXPIRED_TIME, Long.class, 60000L);
    }
    
    /** 过期元数据清理任务间隔（毫秒）。 */
    public static Long getExpiredMetadataCleanInterval() {
        return EnvUtil.getProperty(EXPIRED_METADATA_CLEAN_INTERVAL, Long.class, 5000L);
    }
    
    /** 元数据过期判定时间（毫秒）。 */
    public static Long getExpiredMetadataExpiredTime() {
        return EnvUtil.getProperty(EXPIRED_METADATA_EXPIRED_TIME, Long.class, 60000L);
    }
    
    /** 模糊订阅最大 pattern 数量上限。 */
    public static int getMaxPatternCount() {
        return EnvUtil.getProperty("nacos.naming.fuzzy.watch.max.pattern.count", Integer.class, 20);
    }
    
    /** 单个模糊 pattern 最大匹配服务数上限。 */
    public static int getMaxMatchedServiceCount() {
        return EnvUtil.getProperty("nacos.naming.fuzzy.watch.max.pattern.match.service.count",
            Integer.class, 500);
        
    }
    
}
