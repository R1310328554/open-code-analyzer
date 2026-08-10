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

package com.alibaba.nacos.plugin.auth.impl.token;

import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import org.springframework.security.core.Authentication;

/**
 * {@link TokenManager} 委托包装：统一转发至实际实现（如 {@link CachedJwtTokenManager}）。
 *
 * @author majorhe
 */
public class TokenManagerDelegate implements TokenManager {
    
    /** 是否启用 JWT 令牌本地缓存的配置项键名。 */
    public static final String NACOS_AUTH_TOKEN_CACHING_ENABLED =
        "nacos.core.auth.plugin.nacos.token.cache.enable";
    
    private final TokenManager delegate;
    
    /** 注入底层 TokenManager 实现。 */
    public TokenManagerDelegate(TokenManager delegate) {
        this.delegate = delegate;
    }
    
    /** 返回实际执行的 TokenManager。 */
    private TokenManager getExecuteTokenManager() {
        return delegate;
    }
    
    @Override
    public String createToken(Authentication authentication) throws AccessException {
        return getExecuteTokenManager().createToken(authentication);
    }
    
    @Override
    public String createToken(String userName) throws AccessException {
        return getExecuteTokenManager().createToken(userName);
    }
    
    @Override
    public Authentication getAuthentication(String token) throws AccessException {
        return getExecuteTokenManager().getAuthentication(token);
    }
    
    @Override
    public void validateToken(String token) throws AccessException {
        getExecuteTokenManager().validateToken(token);
    }
    
    @Override
    public NacosUser parseToken(String token) throws AccessException {
        return getExecuteTokenManager().parseToken(token);
    }
    
    @Override
    public long getTokenValidityInSeconds() throws AccessException {
        return getExecuteTokenManager().getTokenValidityInSeconds();
    }
    
    @Override
    public long getTokenTtlInSeconds(String token) throws AccessException {
        return getExecuteTokenManager().getTokenTtlInSeconds(token);
    }
    
}
