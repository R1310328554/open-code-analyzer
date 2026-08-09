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
package com.alibaba.csp.sentinel.transport.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;
import com.alibaba.csp.sentinel.transport.endpoint.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 传输层配置：解析 Dashboard 地址、心跳间隔、本地 API 端口等。
 * 配置项通过 {@link SentinelConfig} 读取，支持逗号分隔的多 Dashboard 地址。
 *
 * @author Carpenter Lee
 * @author Jason Joo
 * @author Leo Li
 */
public class TransportConfig {

    /** Dashboard 控制台地址配置键（支持逗号分隔多个 endpoint）。 */
    public static final String CONSOLE_SERVER = "csp.sentinel.dashboard.server";
    /** 本机命令/API 服务端口配置键。 */
    public static final String SERVER_PORT = "csp.sentinel.api.port";
    /** 心跳上报间隔（毫秒）配置键。 */
    public static final String HEARTBEAT_INTERVAL_MS = "csp.sentinel.heartbeat.interval.ms";
    /** 心跳上报使用的客户端 IP 配置键。 */
    public static final String HEARTBEAT_CLIENT_IP = "csp.sentinel.heartbeat.client.ip";
    /** 机器注册心跳 API 路径配置键。 */
    public static final String HEARTBEAT_API_PATH = "csp.sentinel.heartbeat.api.path";

    /** 默认机器注册心跳路径。 */
    public static final String HEARTBEAT_DEFAULT_PATH = "/registry/machine";

    /** 运行时实际绑定的 API 端口（由传输层启动后回填）。 */
    private static int runtimePort = -1;

    /**
     * 获取心跳间隔（毫秒）。
     *
     * @return 已配置且解析成功时返回间隔毫秒数，未配置或非法时返回 null
     */
    public static Long getHeartbeatIntervalMs() {
        String interval = SentinelConfig.getConfig(HEARTBEAT_INTERVAL_MS);
        try {
            return interval == null ? null : Long.parseLong(interval);
        } catch (Exception ex) {
            RecordLog.warn("[TransportConfig] Failed to parse heartbeat interval: " + interval);
            return null;
        }
    }

    /**
     * 解析 Dashboard 控制台地址列表，每项为 {@link Endpoint}（协议、主机、端口）。<br>
     * 仅支持 <b>HTTP</b> 与 <b>HTTPS</b> 协议前缀。
     *
     * @return Endpoint 列表，<b>永不为 null</b>；未配置时返回空列表
     */
    public static List<Endpoint> getConsoleServerList() {
        String config = SentinelConfig.getConfig(CONSOLE_SERVER);
        List<Endpoint> list = new ArrayList<Endpoint>();
        if (StringUtil.isBlank(config)) {
            return list;
        }

        int pos = -1;
        int cur = 0;
        while (true) {
            pos = config.indexOf(',', cur);
            if (cur < config.length() - 1 && pos < 0) {
                // 单段地址时将 pos 移到末尾
                pos = config.length();
            }
            if (pos < 0) {
                break;
            }
            if (pos <= cur) {
                cur ++;
                continue;
            }
            // 解析 host:port 或带协议前缀的地址
            String ipPortStr = config.substring(cur, pos);
            cur = pos + 1;
            if (StringUtil.isBlank(ipPortStr)) {
                continue;
            }
            ipPortStr = ipPortStr.trim();
            int port = 80;
            Protocol protocol = Protocol.HTTP;
            if (ipPortStr.startsWith("http://")) {
                ipPortStr = ipPortStr.substring(7);
            } else if (ipPortStr.startsWith("https://")) {
                ipPortStr = ipPortStr.substring(8);
                port = 443;
                protocol = Protocol.HTTPS;
            }
            int index = ipPortStr.indexOf(":");
            if (index == 0) {
                // 格式非法则跳过
                continue;
            }
            String host = ipPortStr;
            if (index >= 0) {
                try {
                    port = Integer.parseInt(ipPortStr.substring(index + 1));
                    if (port <= 1 || port >= 65535) {
                        throw new RuntimeException("Port number [" + port + "] over range");
                    }
                } catch (Exception e) {
                    RecordLog.warn("Parse port of dashboard server failed: " + ipPortStr, e);
                    // skip
                    continue;
                }
                host = ipPortStr.substring(0, index);
            }
            list.add(new Endpoint(protocol, host, port));
        }
        return list;
    }

    public static int getRuntimePort() {
        return runtimePort;
    }

    /**
     * 获取本机 HTTP 命令/API 服务端口。
     *
     * @return 端口号字符串；未配置且未设置 runtimePort 时可能为 null
     */
    public static String getPort() {
        if (runtimePort > 0) {
            return String.valueOf(runtimePort);
        }
        return SentinelConfig.getConfig(SERVER_PORT, true);
    }

    /**
     * 设置传输层实际监听的端口（启动成功后调用）。
     *
     * @param port 实际端口
     */
    public static void setRuntimePort(int port) {
        runtimePort = port;
    }

    /**
     * 获取心跳上报使用的本机 IP。
     * 未配置时回退为 {@link HostNameUtil#getIp()}。
     *
     * @return 客户端 IP
     */
    public static String getHeartbeatClientIp() {
        String ip = SentinelConfig.getConfig(HEARTBEAT_CLIENT_IP, true);
        if (StringUtil.isBlank(ip)) {
            ip = HostNameUtil.getIp();
        }
        return ip;
    }

    /**
     * 获取心跳注册 API 路径；须与 Dashboard 侧机器注册路径一致。
     * 未配置时使用 {@link #HEARTBEAT_DEFAULT_PATH}。
     *
     * @return 以 / 开头的 API 路径
     * @since 1.7.1
     */
    public static String getHeartbeatApiPath() {
        String apiPath = SentinelConfig.getConfig(HEARTBEAT_API_PATH);
        if (StringUtil.isBlank(apiPath)) {
            return HEARTBEAT_DEFAULT_PATH;
        }
        if (!apiPath.startsWith("/")) {
            apiPath = "/" + apiPath;
        }
        return apiPath;
    }
}
