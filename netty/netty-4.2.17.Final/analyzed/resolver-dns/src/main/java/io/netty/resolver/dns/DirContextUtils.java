/*
 * Copyright 2018 The Netty Project
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

import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;

/**
 * 通过 JNDI {@link DirContext} 从运行环境读取系统 DNS 名称服务器地址的工具类。
 */
final class DirContextUtils {
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(DirContextUtils.class);

    private DirContextUtils() { }

    /**
     * 使用 jndi-dns 将检测到的默认名称服务器追加到 {@code defaultNameServers}。
     *
     * @param defaultNameServers 待填充的列表
     * @param defaultPort  URI 未指定端口时使用的 DNS 端口
     */
    static void addNameServers(List<InetSocketAddress> defaultNameServers, int defaultPort) {
        // 使用 jndi-dns 获取默认名称服务器。
        //
        // 参考:
        // - https://docs.oracle.com/javase/8/docs/technotes/guides/jndi/jndi-dns.html
        // - https://mail.openjdk.java.net/pipermail/net-dev/2017-March/010695.html
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url", "dns://");

        try {
            DirContext ctx = new InitialDirContext(env);
            String dnsUrls = (String) ctx.getEnvironment().get("java.naming.provider.url");
            // URL 为空时跳过，否则会触发异常。
            if (dnsUrls != null && !dnsUrls.isEmpty()) {
                String[] servers = dnsUrls.split(" ");
                for (String server : servers) {
                    try {
                        URI uri = new URI(server);
                        String host = uri.getHost();
                        if (host == null || host.isEmpty()) {
                            logger.debug(
                                    "Skipping a nameserver URI as host portion could not be extracted: {}", server);
                            // 无法解析主机部分则跳过该条目。
                            continue;
                        }
                        int port  = uri.getPort();
                        defaultNameServers.add(SocketUtils.socketAddress(uri.getHost(), port == -1 ?
                                defaultPort : port));
                    } catch (URISyntaxException e) {
                        logger.debug("Skipping a malformed nameserver URI: {}", server, e);
                    }
                }
            }
        } catch (Exception ex) {
            // 失败时上层会尝试反射等其他引导方式。
            logger.debug("Unable to obtain nameservers via InitialDirContext", ex);
        }
    }
}
