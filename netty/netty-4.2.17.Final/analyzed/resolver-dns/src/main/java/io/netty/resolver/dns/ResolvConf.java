/*
 * Copyright 2024 The Netty Project
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
package io.netty.resolver.dns;

import io.netty.util.internal.BoundedInputStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 从 {@code /etc/resolv.conf} 解析 {@code nameserver} 条目，适用于 Linux 与 macOS。
 */
final class ResolvConf {
    /** 解析得到的 DNS 服务器地址列表（不可变）。 */
    private final List<InetSocketAddress> nameservers;

    /**
     * 从给定 {@link BufferedReader} 读取并按 {@code resolv.conf} 语法提取 {@code nameserver}。
     * <p>参见 {@code man resolv.conf}。</p>
     *
     * @param reader 从中读取配置内容，由调用方负责关闭
     */
    static ResolvConf fromReader(BufferedReader reader) throws IOException {
        return new ResolvConf(reader);
    }

    /**
     * 从给定文件读取并按 {@code resolv.conf} 语法提取 {@code nameserver}。
     * <p>参见 {@code man resolv.conf}。</p>
     */
    static ResolvConf fromFile(String file) throws IOException {
        // 限制 1 MB 读取量，避免异常大文件
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new BoundedInputStream(new FileInputStream(file), 1024 * 1024)))) {
            return fromReader(reader);
        }
    }

    /**
     * 返回系统 {@code /etc/resolv.conf} 中的 {@code nameserver} 列表。
     * <p>类加载期间仅读取一次。</p>
     */
    static ResolvConf system() {
        ResolvConf resolvConv = ResolvConfLazy.machineResolvConf;
        if (resolvConv != null) {
            return resolvConv;
        }
        throw new IllegalStateException("/etc/resolv.conf could not be read");
    }

    private ResolvConf(BufferedReader reader) throws IOException {
        List<InetSocketAddress> nameservers = new ArrayList<>();
        String ln;
        while ((ln = reader.readLine()) != null) {
            ln = ln.trim();
            if (ln.isEmpty()) {
                continue;
            }

            if (ln.startsWith("nameserver")) {
                ln = ln.substring("nameserver".length());
                // 截断行内注释
                int cIndex = ln.indexOf('#');
                if (cIndex != -1) {
                    ln = ln.substring(0, cIndex);
                }
                ln = ln.trim();
                if (ln.isEmpty()) {
                    continue;
                }
                nameservers.add(new InetSocketAddress(ln, 53));
            }
        }
        this.nameservers = Collections.unmodifiableList(nameservers);
    }

    /** 返回解析到的 nameserver 地址列表。 */
    List<InetSocketAddress> getNameservers() {
        return nameservers;
    }

    /** 懒加载系统 resolv.conf，失败时 {@code machineResolvConf} 为 null。 */
    private static final class ResolvConfLazy {
        static final ResolvConf machineResolvConf;

        static {
            ResolvConf resolvConf;
            try {
                resolvConf = ResolvConf.fromFile("/etc/resolv.conf");
            } catch (IOException | SecurityException e) {
                resolvConf = null;
            }
            machineResolvConf = resolvConf;
        }
    }
}
