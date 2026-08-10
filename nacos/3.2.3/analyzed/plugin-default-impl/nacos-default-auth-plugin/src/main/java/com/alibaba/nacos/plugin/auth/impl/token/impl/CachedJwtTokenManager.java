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

import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManager;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 带本地缓存的 JWT 令牌管理器。
 *
 * <p>按 token 与 username 双索引缓存解析结果，定时清理过期项；临近过期时自动刷新。</p>
 *
 * @author majorhe
 */
public class CachedJwtTokenManager implements TokenManager {
    
    /** token 字符串 → 缓存实体。 */

    private volatile Map<String, TokenEntity> tokenMap = new ConcurrentHashMap<>(1024);
    
    /** 用户名 → 本机签发的 token 实体。 */

    private volatile Map<String, TokenEntity> userMap = new ConcurrentHashMap<>(128);
    
    private final JwtTokenManager jwtTokenManager;
    
    public CachedJwtTokenManager(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }
    
    /** 每分钟扫描并移除过期 token/user 缓存。 */
    @Scheduled(initialDelay = 30000, fixedDelay = 60000)
    private void cleanExpiredToken() {
        List<String> tokens = new ArrayList<>();
        tokenMap.forEach((k, v) -> {
            if (v.getExpiredTimeMills() < System.currentTimeMillis()) {
                tokens.add(k);
            }
        });
        tokens.forEach(e -> tokenMap.remove(e));
        List<String> users = new ArrayList<>();
        userMap.forEach((k, v) -> {
            if (v.getExpiredTimeMills() < System.currentTimeMillis()) {
                users.add(k);
            }
        });
        users.forEach(e -> userMap.remove(e));
    }
    
    @Override
    public String createToken(Authentication authentication) throws AccessException {
        return createToken(authentication.getName());
    }
    
    /**
     * Create token.
     *
     * @param username auth info
     * @return token
     * @throws AccessException access exception
      * <p>带本地缓存的 JWT 令牌管理器。</p>
     */
    /** 签发或复用未临近过期的缓存 token。 */
    public String createToken(String username) throws AccessException {
        TokenEntity cached = userMap.get(username);
        if (cached != null) {
            if (!needRefresh(cached.getExpiredTimeMills())) {
                return cached.getToken();
            }
        }
        String token = jwtTokenManager.createToken(username);
        NacosUser user = jwtTokenManager.parseToken(token);
        long expiredTime =
            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getTokenValidityInSeconds());
        Authentication authentication = jwtTokenManager.getAuthentication(token);
        TokenEntity model = new TokenEntity(token, username, expiredTime, authentication, user);
        tokenMap.put(token, model);
        userMap.put(username, model);
        return token;
    }
    
    /**
     * Get auth Info.
     *
     * @param token token
     * @return auth info
     * @throws AccessException access exception
      * <p>带本地缓存的 JWT 令牌管理器。</p>
     */
    public Authentication getAuthentication(String token) throws AccessException {
        TokenEntity cached = tokenMap.get(token);
        if (cached != null) {
            return cached.getAuthentication();
        }
        return jwtTokenManager.getAuthentication(token);
    }
    
    /**
     * validate token.
     *
     * @param token token
     * @throws AccessException access exception
      * <p>带本地缓存的 JWT 令牌管理器。</p>
     */
    public void validateToken(String token) throws AccessException {
        if (tokenMap.get(token) != null) {
            return;
        }
        // 无效 token 时 jwtTokenManager 会抛异常
        jwtTokenManager.validateToken(token);
        // 校验通过后回填缓存
        Authentication authentication = jwtTokenManager.getAuthentication(token);
        String username = authentication.getName();
        if (username == null || username.isEmpty()) {
            return;
        }
        long expiredTime =
            TimeUnit.SECONDS.toMillis(jwtTokenManager.getExpiredTimeInSeconds(token));
        if (expiredTime <= System.currentTimeMillis()) {
            return;
        }
        NacosUser user = jwtTokenManager.parseToken(token);
        tokenMap.putIfAbsent(token,
            new TokenEntity(token, username, expiredTime, authentication, user));
    }
    
    @Override
    public NacosUser parseToken(String token) throws AccessException {
        TokenEntity cached = tokenMap.get(token);
        if (cached != null) {
            return cached.getNacosUser();
        }
        Authentication authentication = jwtTokenManager.getAuthentication(token);
        String username = authentication.getName();
        if (username == null || username.isEmpty()) {
            throw new AccessException("invalid token, username is empty");
        }
        long expiredTime =
            TimeUnit.SECONDS.toMillis(jwtTokenManager.getExpiredTimeInSeconds(token));
        if (expiredTime <= System.currentTimeMillis()) {
            throw new AccessException("expired token");
        }
        NacosUser user = jwtTokenManager.parseToken(token);
        tokenMap.putIfAbsent(token,
            new TokenEntity(token, username, expiredTime, authentication, user));
        return user;
    }
    
    public long getTokenTtlInSeconds(String token) throws AccessException {
        TokenEntity cached = tokenMap.get(token);
        if (cached != null) {
            return TimeUnit.MILLISECONDS.toSeconds(
                cached.getExpiredTimeMills() - System.currentTimeMillis());
        }
        return jwtTokenManager.getTokenTtlInSeconds(token);
    }
    
    @Override
    public long getTokenValidityInSeconds() {
        return jwtTokenManager.getTokenValidityInSeconds();
    }
    
    /** 剩余有效期不足 1/10 时需刷新 token。 */
    private boolean needRefresh(long expiredTimeMills) {
        long refreshWindowMills = TimeUnit.SECONDS.toMillis(getTokenValidityInSeconds() / 10);
        return System.currentTimeMillis() + refreshWindowMills > expiredTimeMills;
    }
    
    /** 内存缓存的 token 元数据（认证信息、用户、过期时间）。 */
    static class TokenEntity {
        
        private String token;
        
        private String userName;
        
        private long expiredTimeMills;
        
        private Authentication authentication;
        
        private NacosUser nacosUser;
        
        public TokenEntity(String token, String userName, long expiredTimeMills,
            Authentication authentication,
            NacosUser nacosUser) {
            this.token = token;
            this.userName = userName;
            this.expiredTimeMills = expiredTimeMills;
            this.authentication = authentication;
            this.nacosUser = nacosUser;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        public long getExpiredTimeMills() {
            return expiredTimeMills;
        }
        
        public void setExpiredTimeMills(long expiredTimeMills) {
            this.expiredTimeMills = expiredTimeMills;
        }
        
        public Authentication getAuthentication() {
            return authentication;
        }
        
        public void setAuthentication(Authentication authentication) {
            this.authentication = authentication;
        }
        
        public NacosUser getNacosUser() {
            return nacosUser;
        }
        
        public void setNacosUser(NacosUser nacosUser) {
            this.nacosUser = nacosUser;
        }
        
        @Override
        public String toString() {
            return "TokenEntity{" + "token='" + token + '\'' + ", userName='" + userName + '\''
                + ", expiredTimeMills="
                + expiredTimeMills + ", authentication=" + authentication + ", nacosUser="
                + nacosUser + '}';
        }
    }
    
}
