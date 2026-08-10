/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.impl.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.console.config.NacosConsoleAuthConfig;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.alibaba.nacos.plugin.auth.spi.client.AbstractClientAuthService;

import java.util.List;
import java.util.Properties;

/**
 * Console 远程 Maintainer 客户端认证插件：为远程运维客户端注入服务端身份凭证。
 * Client Auth Plugin implementation for console remote maintainer client.
 *
 * @author xiweng.yy
 */
public class ConsoleMaintainerClientAuthPlugin extends AbstractClientAuthService {
    
    /** 缓存登录身份上下文，供远程请求携带服务端身份头 */
    private LoginIdentityContext identityContext = new LoginIdentityContext();
    
    /** 初始化认证：若启用服务端身份则写入 {@link LoginIdentityContext} */
    @Override
    public Boolean login(Properties properties) {
        NacosConsoleAuthConfig authConfig =
            (NacosConsoleAuthConfig) NacosAuthConfigHolder.getInstance()
                .getNacosAuthConfigByScope(NacosConsoleAuthConfig.NACOS_CONSOLE_AUTH_SCOPE);
        if (authConfig.isSupportServerIdentity()) {
            identityContext.setParameter(authConfig.getServerIdentityKey(),
                authConfig.getServerIdentityValue());
        }
        return true;
    }
    
    /** 设置远程 Nacos 集群地址列表（Console 模式下无需额外处理） */
    @Override
    public void setServerList(List<String> serverList) {
    }
    
    /** 注入 HTTP 客户端模板（Console 模式下无需额外处理） */
    @Override
    public void setNacosRestTemplate(NacosRestTemplate nacosRestTemplate) {
    }
    
    /** 返回当前请求应携带的登录身份上下文 */
    @Override
    public LoginIdentityContext getLoginIdentityContext(RequestResource resource) {
        return identityContext;
    }
    
    /** 关闭认证插件（无额外资源需释放） */
    @Override
    public void shutdown() throws NacosException {
        
    }
}
