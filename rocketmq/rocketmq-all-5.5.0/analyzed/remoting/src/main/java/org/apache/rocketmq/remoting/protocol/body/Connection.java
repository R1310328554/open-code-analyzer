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

/**
 * 消费者连接元数据：clientId、网络地址、语言与协议版本。
 */
public class Connection {
    /** 客户端唯一标识。 */
    private String clientId;
    /** 客户端网络地址。 */
    private String clientAddr;
    /** 客户端 SDK 语言类型。 */
    private LanguageCode language;
    /** Remoting 协议版本号。 */
    private int version;

    /** 返回客户端 ID。 */
    public String getClientId() {
        return clientId;
    }

    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 返回客户端地址。 */
    public String getClientAddr() {
        return clientAddr;
    }

    /** 设置客户端地址。 */
    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    /** 返回 SDK 语言。 */
    public LanguageCode getLanguage() {
        return language;
    }

    /** 设置 SDK 语言。 */
    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    /** 返回协议版本。 */
    public int getVersion() {
        return version;
    }

    /** 设置协议版本。 */
    public void setVersion(int version) {
        this.version = version;
    }
}
