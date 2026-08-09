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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpClient;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpRequest;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpResponse;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;

import java.util.List;

/**
 * 基于 {@link SimpleHttpClient} 的心跳发送器：向 Dashboard 列表轮询 POST 注册信息。
 * 命令端口未初始化时不发送心跳。
 *
 * @author Eric Zhao
 * @author Carpenter Lee
 * @author Leo Li
 */
public class SimpleHttpHeartbeatSender implements HeartbeatSender {

    /** HTTP 200 视为成功。 */
    private static final int OK_STATUS = 200;

    /** 默认心跳间隔 10 秒。 */
    private static final long DEFAULT_INTERVAL = 1000 * 10;

    /** 心跳消息构建器。 */
    private final HeartbeatMessage heartBeat = new HeartbeatMessage();
    /** 简易 HTTP 客户端。 */
    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    /** Dashboard 地址列表。 */
    private final List<Endpoint> addressList;

    /** 当前轮询的 Dashboard 索引。 */
    private int currentAddressIdx = 0;

    public SimpleHttpHeartbeatSender() {
        // 从 TransportConfig 读取 Dashboard 地址列表
        List<Endpoint> newAddrs = TransportConfig.getConsoleServerList();
        if (newAddrs.isEmpty()) {
            RecordLog.warn("[SimpleHttpHeartbeatSender] Dashboard 地址未配置或不可用");
        } else {
            RecordLog.info("[SimpleHttpHeartbeatSender] Default console address list retrieved: {}", newAddrs);
        }
        this.addressList = newAddrs;
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (TransportConfig.getRuntimePort() <= 0) {
            RecordLog.info("[SimpleHttpHeartbeatSender] 命令端口未初始化，跳过心跳");
            return false;
        }
        Endpoint addrInfo = getAvailableAddress();
        if (addrInfo == null) {
            return false;
        }

        SimpleHttpRequest request = new SimpleHttpRequest(addrInfo, TransportConfig.getHeartbeatApiPath());
        request.setParams(heartBeat.generateCurrentMessage());
        try {
            SimpleHttpResponse response = httpClient.post(request);
            if (response.getStatusCode() == OK_STATUS) {
                return true;
            } else if (clientErrorCode(response.getStatusCode()) || serverErrorCode(response.getStatusCode())) {
                RecordLog.warn("[SimpleHttpHeartbeatSender] 心跳发送失败，目标 " + addrInfo
                    + ", http status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            RecordLog.warn("[SimpleHttpHeartbeatSender] Failed to send heartbeat to " + addrInfo, e);
        }
        return false;
    }

    @Override
    public long intervalMs() {
        return DEFAULT_INTERVAL;
    }

    /** 按 currentAddressIdx 轮询返回可用 Dashboard 端点。 */
    private Endpoint getAvailableAddress() {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }
        if (currentAddressIdx < 0) {
            currentAddressIdx = 0;
        }
        int index = currentAddressIdx % addressList.size();
        return addressList.get(index);
    }

    private boolean clientErrorCode(int code) {
        return code > 399 && code < 500;
    }

    private boolean serverErrorCode(int code) {
        return code > 499 && code < 600;
    }
}
