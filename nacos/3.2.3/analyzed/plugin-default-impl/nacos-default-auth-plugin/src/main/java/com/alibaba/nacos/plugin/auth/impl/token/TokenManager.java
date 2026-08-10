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
 * JWT 令牌管理器接口。
 *
 * <p>负责签发、校验、解析令牌及查询有效期/TTL。</p>
 *
 * @author majorhe
 */
public interface TokenManager {
    
    /**
     * 根据 Spring Security 认证信息签发令牌。
     *
     * @param authentication auth info
     * @return token
     * @throws AccessException access exception
     */
    String createToken(Authentication authentication) throws AccessException;
    
    /**
     * Create token.
     *
     * @param userName auth info
     * @return token
     * @throws AccessException access exception
      * <p>JWT 令牌管理器接口。</p>
     */
    String createToken(String userName) throws AccessException;
    
    /**
     * 从令牌还原 Spring Security {@link Authentication}。
     *
     * @param token token
     * @return auth info
     * @throws AccessException access exception
     */
    Authentication getAuthentication(String token) throws AccessException;
    
    /**
     * 校验令牌有效性。
     *
     * @param token token
     * @throws AccessException access exception
     */
    void validateToken(String token) throws AccessException;
    
    /**
     * 解析令牌为 {@link NacosUser}。
     *
     * @param token token
     * @return nacos user object
     * @throws AccessException access exception
     */
    NacosUser parseToken(String token) throws AccessException;
    
    /**
     * 获取配置的令牌默认有效期（秒）。
     *
     * @return  token validity in seconds
     * @throws AccessException access exception
     */
    long getTokenValidityInSeconds() throws AccessException;
    
    /**
     * 获取指定令牌剩余 TTL（秒）。
     *
     * @param token token
     * @return token ttl in seconds
     * @throws AccessException access exception
     */
    long getTokenTtlInSeconds(String token) throws AccessException;
    
}
