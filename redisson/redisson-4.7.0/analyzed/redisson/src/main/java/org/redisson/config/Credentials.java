/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.config;

import java.io.Serializable;

/**
 * Redis 连接认证凭据，包含用户名与密码。
 * <p>
 * 实现 {@link Serializable}，可在配置序列化中传递。
 *
 * @author Nikita Koksharov
 *
 */
public class Credentials implements Serializable {

    /** 认证用户名（Redis 6.0+ ACL 支持）。 */
    private String username;

    /** 认证密码或令牌。 */
    private String password;

    /** 无参构造函数。 */
    public Credentials() {
    }

    /** 使用用户名和密码构造凭据。 */
    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /** 返回认证密码。 */
    public String getPassword() {
        return password;
    }

    /** 返回认证用户名。 */
    public String getUsername() {
        return username;
    }

}
