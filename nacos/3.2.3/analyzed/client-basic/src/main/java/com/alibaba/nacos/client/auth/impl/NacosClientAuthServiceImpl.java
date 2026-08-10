/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.auth.impl;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.auth.impl.process.HttpLoginProcessor;
import com.alibaba.nacos.common.utils.RandomUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.alibaba.nacos.plugin.auth.spi.client.AbstractClientAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * a ClientAuthService implement.
 * <p>Nacos 用户名/密码客户端鉴权实现：向 Server 发起 HTTP 登录获取 accessToken，维护 {@link LoginIdentityContext} 并在 TTL 窗口内复用令牌，过期前随机提前刷新以分散集群登录压力。</p>
 *
 * @author wuyfee
 */

@SuppressWarnings("checkstyle:SummaryJavadoc")
public class NacosClientAuthServiceImpl extends AbstractClientAuthService {
    
    /** 安全相关日志记录器 */
    private static final Logger SECURITY_LOGGER =
        LoggerFactory.getLogger(NacosClientAuthServiceImpl.class);
    
    /** 访问令牌有效时长（秒），由登录响应 {@link NacosAuthLoginConstant#TOKENTTL} 解析得到。 */
    private long tokenTtl;
    
    /** 最近一次从 Server 成功刷新安全信息（登录或跳过）的时间戳（毫秒）。 */
    private long lastRefreshTime;
    
    /** 令牌到期前主动刷新的随机时间窗口（秒），范围约为 [tokenTtl/15, tokenTtl/10]。 */
    private long tokenRefreshWindow;
    
    /** 随请求携带的登录身份上下文，volatile 保证多线程可见性。 */
    private volatile LoginIdentityContext loginIdentityContext = new LoginIdentityContext();
    
    /** 重新登录冷却窗口（毫秒）；reLoginFlag 为 true 时在此窗口内不再重复登录。 */
    private final long reLoginWindow = 60000;
    
    /**
     * Login to servers.
     * <p>按 TTL/重登窗口判断是否需要登录；无用户名则视为匿名跳过；否则遍历 {@code serverList} 调用 {@link HttpLoginProcessor} 直至成功或全部失败。</p>
     *
     * @return true if login successfully
     */
    
    @Override
    public Boolean login(Properties properties) {
        try {
            // 是否处于强制重登模式（由上层 SecurityProxy 等设置）
            boolean reLoginFlag = Boolean.parseBoolean(
                loginIdentityContext.getParameter(NacosAuthLoginConstant.RELOGINFLAG, "false"));
            if (reLoginFlag) {
                if ((System.currentTimeMillis() - lastRefreshTime) < reLoginWindow) {
                    return true;
                }
            } else {
                if ((System.currentTimeMillis() - lastRefreshTime) < TimeUnit.SECONDS
                    .toMillis(tokenTtl - tokenRefreshWindow)) {
                    return true;
                }
            }
            
            // 未配置用户名则无需登录，仅更新时间戳避免频繁进入登录分支
            if (StringUtils.isBlank(properties.getProperty(PropertyKeyConst.USERNAME))) {
                lastRefreshTime = System.currentTimeMillis();
                return true;
            }
            
            // 依次尝试集群内各 Server 节点登录
            for (String server : this.serverList) {
                HttpLoginProcessor httpLoginProcessor = new HttpLoginProcessor(nacosRestTemplate);
                properties.setProperty(NacosAuthLoginConstant.SERVER, server);
                LoginIdentityContext identityContext = httpLoginProcessor.getResponse(properties);
                if (identityContext != null) {
                    if (identityContext.getAllKey().contains(NacosAuthLoginConstant.ACCESSTOKEN)) {
                        tokenTtl = Long.parseLong(
                            identityContext.getParameter(NacosAuthLoginConstant.TOKENTTL));
                        tokenRefreshWindow = generateTokenRefreshWindow(tokenTtl);
                        lastRefreshTime = System.currentTimeMillis();
                        
                        // 仅保留 accessToken，避免旧上下文携带过期参数
                        LoginIdentityContext newCtx = new LoginIdentityContext();
                        newCtx.setParameter(NacosAuthLoginConstant.ACCESSTOKEN,
                            identityContext.getParameter(NacosAuthLoginConstant.ACCESSTOKEN));
                        this.loginIdentityContext = newCtx;
                    }
                    return true;
                }
            }
        } catch (Throwable throwable) {
            SECURITY_LOGGER.warn("[SecurityProxy] login failed, error: ", throwable);
            return false;
        }
        return false;
    }
    
    /** {@inheritDoc} 返回当前缓存的登录身份（含 accessToken）。 */
    @Override
    public LoginIdentityContext getLoginIdentityContext(RequestResource resource) {
        return this.loginIdentityContext;
    }
    
    /** {@inheritDoc} 用户名密码模式无额外资源需释放。 */
    @Override
    public void shutdown() throws NacosException {
        
    }
    
    /**
     * Randomly generate TokenRefreshWindow, Avoid a large number of logins causing pressure on the Nacos server.
     * <p>随机生成刷新窗口，避免大量客户端在同一时刻集中刷新令牌压垮 Server。</p>
     * @param tokenTtl TTL of token in seconds.
     * @return tokenRefreshWindow, numerical range [tokenTtl/15 ~ tokenTtl/10]
     */
    public long generateTokenRefreshWindow(long tokenTtl) {
        long startNumber = tokenTtl / 15;
        long endNumber = tokenTtl / 10;
        return RandomUtils.nextLong(startNumber, endNumber);
    }
}
