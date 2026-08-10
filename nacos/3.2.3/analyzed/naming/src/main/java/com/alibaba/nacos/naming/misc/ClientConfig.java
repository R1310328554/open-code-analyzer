/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.naming.constants.ClientConstants;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * Naming 客户端连接配置（动态可热更新）。
 *
 * <p>继承 {@link AbstractDynamicConfig}，管理客户端过期时间等参数，支持从环境变量读取并运行时刷新。</p>
 *
 * @author xiweng.yy
 */
public class ClientConfig extends AbstractDynamicConfig {
    
    /** 动态配置模块标识名。 */
    private static final String NAMING_CLIENT = "NamingClient";
    
    /** 单例实例。 */
    private static final ClientConfig INSTANCE = new ClientConfig();
    
    /** 客户端连接过期时间（毫秒）。 */
    private long clientExpiredTime = ClientConstants.DEFAULT_CLIENT_EXPIRED_TIME;
    
    /** 私有构造，初始化并加载配置。 */
    private ClientConfig() {
        super(NAMING_CLIENT);
        resetConfig();
    }
    
    /** 获取 ClientConfig 单例。 */
    public static ClientConfig getInstance() {
        return INSTANCE;
    }
    
    /** 返回客户端过期时间。 */
    public long getClientExpiredTime() {
        return clientExpiredTime;
    }
    
    /** 设置客户端过期时间。 */
    public void setClientExpiredTime(long clientExpiredTime) {
        this.clientExpiredTime = clientExpiredTime;
    }
    
    /** 从环境变量/配置中心读取 clientExpiredTime。 */
    @Override
    protected void getConfigFromEnv() {
        clientExpiredTime =
            EnvUtil.getProperty(ClientConstants.CLIENT_EXPIRED_TIME_CONFIG_KEY, Long.class,
                ClientConstants.DEFAULT_CLIENT_EXPIRED_TIME);
    }
    
    /** 打印当前配置快照。 */
    @Override
    protected String printConfig() {
        return "ClientConfig{" + "clientExpiredTime=" + clientExpiredTime + '}';
    }
}
