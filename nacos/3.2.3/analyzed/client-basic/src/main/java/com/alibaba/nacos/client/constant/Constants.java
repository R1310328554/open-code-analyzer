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

package com.alibaba.nacos.client.constant;

import java.util.concurrent.TimeUnit;

/**
 * All the constants.
 * <p>Nacos 客户端内部常量：系统环境键、安全信息刷新间隔及地址列表 Provider 排序权重等。</p>
 *
 * @author onew
 */
public class Constants {
    
    /** 与 JVM 系统属性 / 环境变量相关的键名 */
    public static class SysEnv {
        
        /** 用户主目录系统属性键 */
        public static final String USER_HOME = "user.home";
        
        /** 应用/project 名称系统属性键 */
        public static final String PROJECT_NAME = "project.name";
        
        /** 客户端日志目录系统属性键 */
        public static final String JM_LOG_PATH = "JM.LOG.PATH";
        
        /** 客户端快照缓存目录系统属性键 */
        public static final String JM_SNAPSHOT_PATH = "JM.SNAPSHOT.PATH";
        
        /** 是否优先使用环境变量覆盖配置的开关键 */
        public static final String NACOS_ENV_FIRST = "nacos.env.first";
        
    }
    
    /** 安全与凭证刷新相关常量 */
    public static class Security {
        
        /** 安全信息（如 RAM 凭证）轮询刷新间隔（毫秒），默认 5 秒 */
        public static final long SECURITY_INFO_REFRESH_INTERVAL_MILLS =
            TimeUnit.SECONDS.toMillis(5);
        
    }
    
    /** Server 地址列表 Provider 优先级常量 */
    public static class Address {
        
        /** Endpoint 动态寻址 Provider 排序值（较高优先级） */
        public static final int ENDPOINT_SERVER_LIST_PROVIDER_ORDER = 500;
        
        /** 固定地址 ServerList Provider 排序值 */
        public static final int ADDRESS_SERVER_LIST_PROVIDER_ORDER = 499;
    }
    
}
