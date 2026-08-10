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

package com.alibaba.nacos.plugin.auth.impl.token.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.jwt.NacosJwtParser;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManager;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 {@link NacosJwtParser} 的 JWT 令牌管理器。
 *
 * <p>监听 {@link ServerConfigChangeEvent} 热更新密钥与过期时间；鉴权关闭时返回占位 token。</p>
 *
 * @author wfnuser
 * @author nkorange
 */
public class JwtTokenManager extends Subscriber<ServerConfigChangeEvent> implements TokenManager {
    
    /** 鉴权关闭时的占位 token 字符串。 */
    private static final String AUTH_DISABLED_TOKEN = "AUTH_DISABLED";
    
    /** 令牌默认有效期（秒）。 */

    private volatile long tokenValidityInSeconds;
    
    private volatile NacosJwtParser jwtParser;
    
    private final AuthConfigs authConfigs;
    
    public JwtTokenManager(AuthConfigs authConfigs) {
        this.authConfigs = authConfigs;
        NotifyCenter.registerSubscriber(this);
        processProperties();
    }
    
    /** 从环境变量加载过期时间与 Base64 密钥并初始化解析器。 */
    private void processProperties() {
        this.tokenValidityInSeconds =
            EnvUtil.getProperty(AuthConstants.TOKEN_EXPIRE_SECONDS, Long.class,
                AuthConstants.DEFAULT_TOKEN_EXPIRE_SECONDS);
        
        String encodedSecretKey = EnvUtil.getProperty(AuthConstants.TOKEN_SECRET_KEY,
            AuthConstants.DEFAULT_TOKEN_SECRET_KEY);
        try {
            this.jwtParser = new NacosJwtParser(encodedSecretKey);
        } catch (Exception e) {
            this.jwtParser = null;
            if (authConfigs.isAuthEnabled() || authConfigs.isConsoleAuthEnabled()) {
                throw new IllegalArgumentException(
                    "the length of secret key must great than or equal 32 bytes; And the secret key  must be encoded by base64."
                        + "Please see https://nacos.io/docs/latest/manual/admin/auth/",
                    e);
            }
        }
        
    }
    
    /**
     * Create token.
     *
     * @param authentication auth info
     * @return token
      * <p>基于 NacosJwtParser 的 JWT 管理器。</p>
     */
    @Deprecated
    public String createToken(Authentication authentication) {
        return createToken(authentication.getName());
    }
    
    /**
     * Create token.
     *
     * @param userName auth info
     * @return token
      * <p>基于 NacosJwtParser 的 JWT 管理器。</p>
     */
    public String createToken(String userName) {
        // 鉴权开启或已配置密钥时才签发真实 JWT
        if (!authConfigs.isAuthEnabled() && null == jwtParser) {
            return AUTH_DISABLED_TOKEN;
        } else if (authConfigs.isAuthEnabled()) {
            // 鉴权开启时必须校验密钥已配置
            checkJwtParser();
        }
        return jwtParser.jwtBuilder().setUserName(userName)
            .setExpiredTime(this.tokenValidityInSeconds).compact();
    }
    
    /**
     * Get auth Info.
     *
     * @param token token
     * @return auth info
      * <p>基于 NacosJwtParser 的 JWT 管理器。</p>
     */
    @Deprecated
    public Authentication getAuthentication(String token) throws AccessException {
        NacosUser nacosUser = jwtParser.parse(token);
        
        List<GrantedAuthority> authorities =
            AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils.EMPTY);
        
        User principal = new User(nacosUser.getUserName(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
    
    /**
     * validate token.
     *
     * @param token token
      * <p>基于 NacosJwtParser 的 JWT 管理器。</p>
     */
    public void validateToken(String token) throws AccessException {
        parseToken(token);
    }
    
    /** 解析 JWT 为 NacosUser，未配置密钥时抛错。 */
    public NacosUser parseToken(String token) throws AccessException {
        checkJwtParser();
        return jwtParser.parse(token);
    }
    
    public long getTokenValidityInSeconds() {
        return tokenValidityInSeconds;
    }
    
    @Override
    public long getTokenTtlInSeconds(String token) throws AccessException {
        if (!authConfigs.isAuthEnabled()) {
            return tokenValidityInSeconds;
        }
        return jwtParser.getExpireTimeInSeconds(token)
            - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }
    
    public long getExpiredTimeInSeconds(String token) throws AccessException {
        if (!authConfigs.isAuthEnabled()) {
            return tokenValidityInSeconds;
        }
        return jwtParser.getExpireTimeInSeconds(token);
    }
    
    @Override
    public void onEvent(ServerConfigChangeEvent event) {
        processProperties();
    }
    
    @Override
    public Class<? extends Event> subscribeType() {
        return ServerConfigChangeEvent.class;
    }
    
    /** 鉴权开启时校验 jwtParser 已初始化。 */
    private void checkJwtParser() {
        if (null == jwtParser) {
            throw new NacosRuntimeException(NacosException.INVALID_PARAM,
                "Please config `nacos.core.auth.plugin.nacos.token.secret.key`, detail see https://nacos.io/docs/latest/manual/admin/auth/");
        }
    }
}
