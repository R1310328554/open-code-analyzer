/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.representations.idm;

/**
 * Client Initial Access Token 的 REST 表示，用于动态客户端注册时的临时授权凭证。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientInitialAccessPresentation {

    /** 令牌记录 ID。 */
    private String id;

    /** 初始访问令牌字符串。 */
    private String token;

    /** 创建时间（Unix 秒级时间戳）。 */
    private Integer timestamp;

    /** 过期时间（秒），自创建起计。 */
    private Integer expiration;

    /** 允许注册客户端的总次数。 */
    private Integer count;

    /** 剩余可用次数。 */
    private Integer remainingCount;

    /** @return 记录 ID */
    public String getId() {
        return id;
    }

    /** @param id 记录 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 令牌字符串 */
    public String getToken() {
        return token;
    }

    /** @param token 令牌字符串 */
    public void setToken(String token) {
        this.token = token;
    }

    /** @return 创建时间戳（秒） */
    public Integer getTimestamp() {
        return timestamp;
    }

    /** @param timestamp 创建时间戳（秒） */
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    /** @return 过期秒数 */
    public Integer getExpiration() {
        return expiration;
    }

    /** @param expiration 过期秒数 */
    public void setExpiration(Integer expiration) {
        this.expiration = expiration;
    }

    /** @return 总可用次数 */
    public Integer getCount() {
        return count;
    }

    /** @param count 总可用次数 */
    public void setCount(Integer count) {
        this.count = count;
    }

    /** @return 剩余可用次数 */
    public Integer getRemainingCount() {
        return remainingCount;
    }

    /** @param remainingCount 剩余可用次数 */
    public void setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
    }
}
