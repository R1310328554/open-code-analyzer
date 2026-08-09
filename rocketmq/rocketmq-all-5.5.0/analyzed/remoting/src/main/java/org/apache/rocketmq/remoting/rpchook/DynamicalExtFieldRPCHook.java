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
package org.apache.rocketmq.remoting.rpchook;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 动态扩展字段 RPC 钩子：从系统属性或环境变量读取 Zone 信息并注入请求。
 */
public class DynamicalExtFieldRPCHook implements RPCHook {

    /** 请求发出前：若配置了 Zone 名称/模式，则写入 RemotingCommand 扩展字段。 */
    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        // 优先读 JVM 属性，否则读环境变量
        String zoneName = System.getProperty(MixAll.ROCKETMQ_ZONE_PROPERTY, System.getenv(MixAll.ROCKETMQ_ZONE_ENV));
        if (StringUtils.isNotBlank(zoneName)) {
            request.addExtField(MixAll.ZONE_NAME, zoneName);
        }
        String zoneMode = System.getProperty(MixAll.ROCKETMQ_ZONE_MODE_PROPERTY, System.getenv(MixAll.ROCKETMQ_ZONE_MODE_ENV));
        if (StringUtils.isNotBlank(zoneMode)) {
            request.addExtField(MixAll.ZONE_MODE, zoneMode);
        }
    }

    /** 响应返回后：本钩子无需处理，留空实现。 */
    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

    }
}
