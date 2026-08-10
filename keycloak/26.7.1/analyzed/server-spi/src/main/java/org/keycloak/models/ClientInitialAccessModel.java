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

package org.keycloak.models;

/**
 * 客户端初始访问令牌模型：限制动态客户端注册的次数与有效期。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientInitialAccessModel {

    private String id;

    private int timestamp;

    private int expiration;

    private int count;

    private int remainingCount;

    /** @return 初始访问记录 ID */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** @return 创建时间戳（秒） */
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    /** @return 过期时间（秒，相对创建时间） */
    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    /** @return 允许注册的最大客户端数 */
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /** @return 剩余可用注册次数 */
    public int getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(int remainingCount) {
        this.remainingCount = remainingCount;
    }
}
