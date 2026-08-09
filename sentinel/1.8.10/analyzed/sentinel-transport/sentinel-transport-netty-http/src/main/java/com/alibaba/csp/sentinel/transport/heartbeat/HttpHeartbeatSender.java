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
package com.alibaba.csp.sentinel.transport.heartbeat;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.endpoint.Protocol;
import com.alibaba.csp.sentinel.transport.heartbeat.client.HttpClientsFactory;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.List;

/**
 * 基于 Apache HttpClient 的心跳发送器：向 Dashboard 发起 GET 注册本机信息。
 * SPI 优先级 {@code ORDER_LOWEST - 100}，与 Netty 实现互斥加载。
 *
 * @author Eric Zhao
 * @author Carpenter Lee
 * @author Leo Li
 */
@Spi(order = Spi.ORDER_LOWEST - 100)
public class HttpHeartbeatSender implements HeartbeatSender {

    /** Apache HttpClient 实例，按 Dashboard 协议（HTTP/HTTPS）创建。 */
    private final CloseableHttpClient client;

    /** HTTP 200 视为心跳成功。 */
    private static final int OK_STATUS = 200;

    /** 连接与读超时（毫秒）。 */
    private final int timeoutMs = 3000;
    private final RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(timeoutMs)
        .setConnectTimeout(timeoutMs)
        .setSocketTimeout(timeoutMs)
        .build();

    /** 首个 Dashboard 端点的通信协议。 */
    private final Protocol consoleProtocol;
    /** Dashboard 主机名或 IP。 */
    private final String consoleHost;
    /** Dashboard 端口。 */
    private final int consolePort;

    public HttpHeartbeatSender() {
        List<Endpoint> dashboardList = TransportConfig.getConsoleServerList();
        if (dashboardList == null || dashboardList.isEmpty()) {
            RecordLog.info("[NettyHttpHeartbeatSender] 未配置可用的 Dashboard 地址");
            consoleProtocol = Protocol.HTTP;
            consoleHost = null;
            consolePort = -1;
        } else {
            consoleProtocol = dashboardList.get(0).getProtocol();
            consoleHost = dashboardList.get(0).getHost();
            consolePort = dashboardList.get(0).getPort();
            RecordLog.info("[NettyHttpHeartbeatSender] 已解析 Dashboard 地址: <{}:{}>", consoleHost, consolePort);
        }
        this.client = HttpClientsFactory.getHttpClientsByProtocol(consoleProtocol);
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (StringUtil.isEmpty(consoleHost)) {
            return false;
        }
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(consoleProtocol.getProtocol()).setHost(consoleHost).setPort(consolePort)
            .setPath(TransportConfig.getHeartbeatApiPath())
            .setParameter("app", AppNameUtil.getAppName())
            .setParameter("app_type", String.valueOf(SentinelConfig.getAppType()))
            .setParameter("v", Constants.SENTINEL_VERSION)
            .setParameter("version", String.valueOf(System.currentTimeMillis()))
            .setParameter("hostname", HostNameUtil.getHostName())
            .setParameter("ip", TransportConfig.getHeartbeatClientIp())
            .setParameter("port", TransportConfig.getPort())
            .setParameter("pid", String.valueOf(PidUtil.getPid()));

        HttpGet request = new HttpGet(uriBuilder.build());
        request.setConfig(requestConfig);
        // 发送心跳 GET 请求
        CloseableHttpResponse response = client.execute(request);
        response.close();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == OK_STATUS) {
            return true;
        } else if (clientErrorCode(statusCode) || serverErrorCode(statusCode)) {
            RecordLog.warn("[HttpHeartbeatSender] 心跳发送失败，目标 "
                + consoleHost + ":" + consolePort + ", http status code: " + statusCode);
        }

        return false;
    }

    @Override
    /** @return 默认心跳间隔 5000 毫秒。 */
    public long intervalMs() {
        return 5000;
    }

    /** 判断是否为 4xx 客户端错误状态码。 */
    private boolean clientErrorCode(int code) {
        return code > 399 && code < 500;
    }

    /** 判断是否为 5xx 服务端错误状态码。 */
    private boolean serverErrorCode(int code) {
        return code > 499 && code < 600;
    }
}
