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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 心跳消息实体：维护向 Dashboard 上报的键值对参数。
 * 构造时填充 hostname、ip、app 等静态字段；发送前调用 {@link #generateCurrentMessage()} 刷新版本与时间戳。
 *
 * @author leyou
 */
public class HeartbeatMessage {

    /** 心跳参数字典。 */
    private final Map<String, String> message = new HashMap<String, String>();

    public HeartbeatMessage() {
        message.put("hostname", HostNameUtil.getHostName());
        message.put("ip", TransportConfig.getHeartbeatClientIp());
        message.put("app", AppNameUtil.getAppName());
        // 应用类型（1.6.0 起）
        message.put("app_type", String.valueOf(SentinelConfig.getAppType()));
        message.put("port", String.valueOf(TransportConfig.getPort()));
    }

    /** 链式注册额外心跳字段。 */
    public HeartbeatMessage registerInformation(String key, String value) {
        message.put(key, value);
        return this;
    }

    /** 刷新动态字段并返回完整心跳参数 Map。 */
    public Map<String, String> generateCurrentMessage() {
        // Sentinel 版本号
        message.put("v", Constants.SENTINEL_VERSION);
        // 当前时间戳，用于 Dashboard 判活
        message.put("version", String.valueOf(TimeUtil.currentTimeMillis()));
        message.put("port", String.valueOf(TransportConfig.getPort()));
        return message;
    }
}
