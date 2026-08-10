/*
 * Copyright 2019 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.resolver.dns.macos;

import io.netty.channel.unix.NativeInetAddress;

import java.net.InetSocketAddress;

/**
 * 对应 macOS 系统头文件中的 {@code dns_resolver_t} 结构体，封装单条 DNS 解析器配置。
 */
final class DnsResolver {

    /** 搜索域（search domain），可为 {@code null} */
    private final String domain;
    /** 该解析器关联的 DNS  nameserver 列表 */
    private final InetSocketAddress[] nameservers;
    /** DNS 查询端口；为 0 时通常回退到 53 */
    private final int port;
    /** 域名搜索后缀列表 */
    private final String[] searches;
    /** 解析器选项字符串（如 {@code mdns}） */
    private final String options;
    /** 查询超时（秒） */
    private final int timeout;
    /** 搜索顺序；数值越小优先级越高 */
    private final int searchOrder;

    /**
     * 由 JNI 层传入的原始字段构造解析器快照。
     *
     * @param domain 搜索域
     * @param nameservers 原始字节形式的 nameserver 地址数组
     * @param port DNS 端口
     * @param searches 搜索后缀
     * @param options 选项字符串
     * @param timeout 超时秒数
     * @param searchOrder 搜索顺序
     */
    DnsResolver(String domain, byte[][] nameservers, int port,
                String[] searches, String options, int timeout, int searchOrder) {
        this.domain = domain;
        if (nameservers == null) {
            this.nameservers = new InetSocketAddress[0];
        } else {
            this.nameservers = new InetSocketAddress[nameservers.length];
            for (int i = 0; i < nameservers.length; i++) {
                byte[] addr = nameservers[i];
                this.nameservers[i] = NativeInetAddress.address(addr, 0, addr.length);
            }
        }
        this.port = port;
        this.searches = searches;
        this.options = options;
        this.timeout = timeout;
        this.searchOrder = searchOrder;
    }

    /** @return 搜索域 */
    String domain() {
        return domain;
    }

    /** @return nameserver 地址数组 */
    InetSocketAddress[] nameservers() {
        return nameservers;
    }

    /** @return DNS 端口 */
    int port() {
        return port;
    }

    /** @return 搜索后缀列表 */
    String[] searches() {
        return searches;
    }

    /** @return 解析器选项 */
    String options() {
        return options;
    }

    /** @return 查询超时（秒） */
    int timeout() {
        return timeout;
    }

    /** @return 搜索顺序 */
    int searchOrder() {
        return searchOrder;
    }
}
