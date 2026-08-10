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

package org.keycloak.representations.adapters.action;

import org.keycloak.Token;
import org.keycloak.TokenCategory;
import org.keycloak.common.util.Time;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 由管理服务器下发至受管客户端（adapter）的管理动作令牌抽象基类，实现 {@link Token} 接口。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AdminAction implements Token {
    /** 动作唯一标识。 */
    protected String id;
    /** 过期时间（Unix 秒级时间戳）。 */
    protected int expiration;
    /** 目标资源标识（通常为 adapter 部署路径或 client 标识）。 */
    protected String resource;
    /** 动作类型字符串（如 LOGOUT）。 */
    protected String action;

    /** 默认无参构造器。 */
    public AdminAction() {
    }

    /**
     * 构造含全部核心字段的管理动作。
     *
     * @param id 动作 ID
     * @param expiration 过期时间（秒）
     * @param resource 目标资源
     * @param action 动作类型
     */
    public AdminAction(String id, int expiration, String resource, String action) {
        this.id = id;
        this.expiration = expiration;
        this.resource = resource;
        this.action = action;
    }

    /** @return 动作 ID */
    public String getId() {
        return id;
    }

    /** @param id 动作 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 当前时间是否已超过 {@link #expiration} */
    @JsonIgnore
    public boolean isExpired() {
        return Time.currentTime() > expiration;
    }

    /**
     * 自 Unix 纪元起的过期秒数。
     *
     * @return 过期时间戳（秒）
     */
    public int getExpiration() {
        return expiration;
    }

    /** @param expiration 过期时间戳（秒） */
    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    /** @return 目标资源标识 */
    public String getResource() {
        return resource;
    }

    /** @param resource 目标资源标识 */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /** @return 动作类型 */
    public String getAction() {
        return action;
    }

    /** @param action 动作类型 */
    public void setAction(String action) {
        this.action = action;
    }

    /** 校验动作载荷是否合法；由子类实现具体规则。 */
    public abstract boolean validate();

    /** @return 令牌类别 {@link TokenCategory#ADMIN} */
    @Override
    public TokenCategory getCategory() {
        return TokenCategory.ADMIN;
    }
}
