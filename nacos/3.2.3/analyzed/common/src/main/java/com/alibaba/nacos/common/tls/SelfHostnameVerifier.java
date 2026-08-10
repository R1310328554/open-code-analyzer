/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.tls;

import com.alibaba.nacos.common.utils.InternetAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义主机名校验器：对 localhost、本机 IP 及纯 IPv4/IPv6 地址直接放行，
 * 其余主机名委托底层 {@link javax.net.ssl.HostnameVerifier} 校验。
 * IP 判断结果缓存在 {@link #HOSTS} 中避免重复解析。
 * A HostnameVerifier verify ipv4 and localhost.
 *
 * @author wangwei
 */

public final class SelfHostnameVerifier implements HostnameVerifier {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfHostnameVerifier.class);
    
    /** 委托的默认主机名校验器（如 JDK 默认实现） */
    private final HostnameVerifier hv;
    
    /** 主机名 → 是否为 IP 的校验结果缓存 */
    private static final ConcurrentHashMap<String, Boolean> HOSTS = new ConcurrentHashMap<>();
    
    /** localhost 主机名与本机 IP，校验时直接通过 */
    private static final String[] LOCALHOST_HOSTNAME = new String[] {InternetAddressUtil.LOCAL_HOST,
        InternetAddressUtil.localHostIp()};
    
    /** 包装已有 HostnameVerifier，扩展 IP/localhost 白名单逻辑 */
    public SelfHostnameVerifier(HostnameVerifier hv) {
        this.hv = hv;
    }
    
    /** localhost、本机 IP、IP 字面量返回 true，否则委托 hv */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (LOCALHOST_HOSTNAME[0].equalsIgnoreCase(hostname)
            || LOCALHOST_HOSTNAME[1].equals(hostname)) {
            return true;
        }
        if (isIp(hostname)) {
            return true;
        }
        return hv.verify(hostname, session);
    }
    
    /** 判断 host 是否为 IP 地址（带 ConcurrentHashMap 缓存） */
    private static boolean isIp(String host) {
        if (host == null || host.isEmpty()) {
            LOGGER.warn("host is empty, isIp = false");
            return false;
        }
        Boolean cacheHostVerify = HOSTS.get(host);
        if (cacheHostVerify != null) {
            return cacheHostVerify;
        }
        boolean isIp = InternetAddressUtil.isIp(host);
        HOSTS.putIfAbsent(host, isIp);
        return isIp;
    }
}
