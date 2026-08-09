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
package org.redisson.api.keys;

import org.redisson.api.MigrateMode;

/**
 * {@link org.redisson.api.RKeys#migrate(MigrateArgs)} 的参数实现类。
 * <p>
 * 聚合待迁移键、目标连接信息、超时、迁移模式及 ACL 认证等字段，
 * 实现迁移参数链上的全部接口。
 *
 * @author lyrric
 */
public class MigrateParams implements MigrateArgs, HostMigrateArgs, PortMigrateArgs, DatabaseMigrateArgs, TimeoutMigrateArgs, OptionalMigrateArgs {
    /** 待迁移的键名数组，需 Redis 3.0.6+。 */
    private final String[] keys;
    /** 目标 Redis 主机地址。 */
    private String host;
    /** 目标 Redis 端口。 */
    private int port;
    /** 目标逻辑数据库编号。 */
    private int database;
    /** 与目标实例通信过程中允许的最大空闲时间（毫秒）。 */
    private long timeout;
    /** 迁移模式，默认为 {@link org.redisson.api.MigrateMode#MIGRATE}。 */
    private MigrateMode mode = MigrateMode.MIGRATE;
    /** 目标实例 ACL 用户名，需 Redis 6.0+。 */
    private String username;
    /** 目标实例密码，需 Redis 4.0.7+。 */
    private String password;

    /** 以待迁移键数组构造参数对象。 */
    public MigrateParams(String[] keys) {
        this.keys = keys;
    }

    /** 设置目标主机并返回自身以继续链式调用。 */
    @Override
    public PortMigrateArgs host(String host) {
        this.host = host;
        return this;
    }

    /** 设置目标端口。 */
    @Override
    public DatabaseMigrateArgs port(int port) {
        this.port = port;
        return this;
    }

    /** 设置目标数据库编号。 */
    @Override
    public TimeoutMigrateArgs database(int database) {
        this.database = database;
        return this;
    }

    /** 设置通信超时（毫秒）。 */
    @Override
    public OptionalMigrateArgs timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /** 设置迁移模式（迁移或复制）。 */
    @Override
    public OptionalMigrateArgs mode(MigrateMode mode) {
        this.mode = mode;
        return this;
    }

    /** 设置目标实例 ACL 用户名。 */
    @Override
    public OptionalMigrateArgs username(String username) {
        this.username = username;
        return this;
    }

    /** 设置目标实例密码。 */
    @Override
    public OptionalMigrateArgs password(String password) {
        this.password = password;
        return this;
    }

    /** 返回待迁移键数组。 */
    public String[] getKeys() {
        return keys;
    }

    /** 返回目标主机。 */
    public String getHost() {
        return host;
    }

    /** 返回目标端口。 */
    public int getPort() {
        return port;
    }

    /** 返回目标数据库编号。 */
    public int getDatabase() {
        return database;
    }

    /** 返回通信超时毫秒数。 */
    public long getTimeout() {
        return timeout;
    }

    /** 返回迁移模式。 */
    public MigrateMode getMode() {
        return mode;
    }

    /** 返回目标 ACL 用户名。 */
    public String getUsername() {
        return username;
    }

    /** 返回目标密码。 */
    public String getPassword() {
        return password;
    }
}
