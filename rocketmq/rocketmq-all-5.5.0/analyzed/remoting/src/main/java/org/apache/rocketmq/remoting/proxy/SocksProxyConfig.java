/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.proxy;

/**
 * SOCKS 代理连接配置：地址与可选认证凭据。
 */
public class SocksProxyConfig {
    /** 代理服务器地址（host:port）。 */
    private String addr;
    /** 代理认证用户名（可为空）。 */
    private String username;
    /** 代理认证密码（可为空）。 */
    private String password;

    /** 默认无参构造。 */
    public SocksProxyConfig() {
    }

    /** 仅指定代理地址。 */
    public SocksProxyConfig(String addr) {
        this.addr = addr;
    }

    /** 指定地址与认证信息。 */
    public SocksProxyConfig(String addr, String username, String password) {
        this.addr = addr;
        this.username = username;
        this.password = password;
    }

    /** 返回代理地址。 */
    public String getAddr() {
        return addr;
    }

    /** 设置代理地址。 */
    public void setAddr(String addr) {
        this.addr = addr;
    }

    /** 返回用户名。 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名。 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 返回密码。 */
    public String getPassword() {
        return password;
    }

    /** 设置密码。 */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 返回配置摘要（含密码字段，慎用日志）。 */
    @Override
    public String toString() {
        return String.format("SocksProxy address: %s, username: %s, password: %s", addr, username, password);
    }
}
