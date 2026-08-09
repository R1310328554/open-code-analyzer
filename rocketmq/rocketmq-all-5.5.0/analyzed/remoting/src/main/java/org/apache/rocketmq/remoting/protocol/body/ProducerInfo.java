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

package org.apache.rocketmq.remoting.protocol.body;

import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;


/**
 * 单个生产者客户端元信息：clientId、网络地址、SDK 语言与协议版本。
 */
public class ProducerInfo extends RemotingSerializable {
    /** 客户端唯一标识。 */
    private String clientId;
    /** 客户端远程 IP 地址。 */
    private String remoteIP;
    /** SDK 语言类型。 */
    private LanguageCode language;
    /** Remoting 协议版本号。 */
    private int version;
    /** 最近一次心跳/注册更新时间戳。 */
    private long lastUpdateTimestamp;

    /** 全字段构造。 */
    public ProducerInfo(String clientId, String remoteIP, LanguageCode language, int version, long lastUpdateTimestamp) {
        this.clientId = clientId;
        this.remoteIP = remoteIP;
        this.language = language;
        this.version = version;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /** 返回客户端 ID。 */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 返回远程 IP。 */
    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    /** 返回 SDK 语言。 */
    public LanguageCode getLanguage() {
        return language;
    }

    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    /** 返回协议版本。 */
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /** 返回最近更新时间戳。 */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return String.format("clientId=%s,remoteIP=%s, language=%s, version=%d, lastUpdateTimestamp=%d",
                clientId, remoteIP, language.name(), version, lastUpdateTimestamp);
    }
}
