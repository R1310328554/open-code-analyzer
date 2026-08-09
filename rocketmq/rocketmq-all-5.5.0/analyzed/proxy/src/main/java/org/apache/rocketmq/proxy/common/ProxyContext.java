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

package org.apache.rocketmq.proxy.common;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy 请求上下文：在单次 RPC/Remoting 调用链路中传递地址、客户端与协议等元数据。
 */
public class ProxyContext {
    /** 内部动作名前缀，用于区分 Proxy 内部触发的操作。 */
    public static final String INNER_ACTION_PREFIX = "Inner";
    /** 键值存储，键为 {@link ContextVariable} 常量名。 */
    private final Map<String, Object> value = new HashMap<>();

    /** 创建空上下文实例。 */
    public static ProxyContext create() {
        return new ProxyContext();
    }

    /** 创建内部动作上下文并设置 action 名称。 */
    public static ProxyContext createForInner(String actionName) {
        return create().setAction(INNER_ACTION_PREFIX + actionName);
    }

    public static ProxyContext createForInner(Class<?> clazz) {
        return createForInner(clazz.getSimpleName());
    }

    public Map<String, Object> getValue() {
        return this.value;
    }

    /** 写入任意键值并返回自身以支持链式调用。 */
    public ProxyContext withVal(String key, Object val) {
        this.value.put(key, val);
        return this;
    }

    public <T> T getVal(String key) {
        return (T) this.value.get(key);
    }

    /** 设置 Proxy 本地监听地址。 */
    public ProxyContext setLocalAddress(String localAddress) {
        this.withVal(ContextVariable.LOCAL_ADDRESS, localAddress);
        return this;
    }

    public String getLocalAddress() {
        return this.getVal(ContextVariable.LOCAL_ADDRESS);
    }

    /** 设置客户端远程地址。 */
    public ProxyContext setRemoteAddress(String remoteAddress) {
        this.withVal(ContextVariable.REMOTE_ADDRESS, remoteAddress);
        return this;
    }

    public String getRemoteAddress() {
        return this.getVal(ContextVariable.REMOTE_ADDRESS);
    }

    public ProxyContext setClientID(String clientID) {
        this.withVal(ContextVariable.CLIENT_ID, clientID);
        return this;
    }

    public String getClientID() {
        return this.getVal(ContextVariable.CLIENT_ID);
    }

    /** 绑定 Netty 通道以便后续协议识别。 */
    public ProxyContext setChannel(Channel channel) {
        this.withVal(ContextVariable.CHANNEL, channel);
        return this;
    }

    public Channel getChannel() {
        return this.getVal(ContextVariable.CHANNEL);
    }

    public ProxyContext setLanguage(String language) {
        this.withVal(ContextVariable.LANGUAGE, language);
        return this;
    }

    public String getLanguage() {
        return this.getVal(ContextVariable.LANGUAGE);
    }

    public ProxyContext setClientVersion(String clientVersion) {
        this.withVal(ContextVariable.CLIENT_VERSION, clientVersion);
        return this;
    }

    public String getClientVersion() {
        return this.getVal(ContextVariable.CLIENT_VERSION);
    }

    public ProxyContext setRemainingMs(Long remainingMs) {
        this.withVal(ContextVariable.REMAINING_MS, remainingMs);
        return this;
    }

    public Long getRemainingMs() {
        return this.getVal(ContextVariable.REMAINING_MS);
    }

    public ProxyContext setAction(String action) {
        this.withVal(ContextVariable.ACTION, action);
        return this;
    }

    public String getAction() {
        return this.getVal(ContextVariable.ACTION);
    }

    public ProxyContext setProtocolType(String protocol) {
        this.withVal(ContextVariable.PROTOCOL_TYPE, protocol);
        return this;
    }

    public String getProtocolType() {
        return this.getVal(ContextVariable.PROTOCOL_TYPE);
    }

    /** 设置多租户命名空间标识。 */
    public ProxyContext setNamespace(String namespace) {
        this.withVal(ContextVariable.NAMESPACE, namespace);
        return this;
    }

    public String getNamespace() {
        return this.getVal(ContextVariable.NAMESPACE);
    }

}
