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

package org.keycloak.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 本机主机名与 IP 地址解析工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HostUtils {

    /** 尽力解析本服务器最合适的主机名（小写、去首尾空白）。 */
    public static String getHostName() {
        return getHostNameImpl().trim().toLowerCase();
    }

    /** 根据主机名解析 IP 地址。 */
    public static String getIpAddress() {
        try {
            String hostname = getHostName();
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe);
        }
    }

    private static String getHostNameImpl() {
        // 优先使用 JBoss 绑定地址
        String bindAddr = System.getProperty("jboss.bind.address");
        if (bindAddr != null && !bindAddr.trim().equals("0.0.0.0")) {
            return bindAddr;
        }

        // 回退到 JBoss 限定主机名
        String qualifiedHostName = System.getProperty("jboss.qualified.host.name");
        if (qualifiedHostName != null) {
            return qualifiedHostName;
        }

        // 非 JBoss 环境：POSIX 系统常用 HOSTNAME
        qualifiedHostName = System.getenv("HOSTNAME");
        if (qualifiedHostName != null) {
            return qualifiedHostName;
        }

        // Windows 部分版本使用 COMPUTERNAME
        qualifiedHostName = System.getenv("COMPUTERNAME");
        if (qualifiedHostName != null) {
            return qualifiedHostName;
        }

        try {
            return NetworkUtils.canonize(getLocalHost().getHostName());
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
            return "unknown-host.unknown-domain";
        }
    }

    /**
     * 返回 localhost 的 {@link InetAddress}。
     *
     * @return InetAddress of the localhost
     * @throws UnknownHostException if localhost could not be resolved
     */
    private static InetAddress getLocalHost() throws UnknownHostException {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
        } catch (ArrayIndexOutOfBoundsException e) {  // macOS 缺陷 workaround，见 AS7-3223 与 JGRP-1404
            addr = InetAddress.getByName(null);
        }
        return addr;
    }
}
