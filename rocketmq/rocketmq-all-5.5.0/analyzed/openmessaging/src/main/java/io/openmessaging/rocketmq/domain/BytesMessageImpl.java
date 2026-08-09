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
package io.openmessaging.rocketmq.domain;

import io.openmessaging.BytesMessage;
import io.openmessaging.KeyValue;
import io.openmessaging.Message;
import io.openmessaging.OMS;
import io.openmessaging.exception.OMSMessageFormatException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * OMS 字节消息实现：维护系统头、用户头与二进制消息体。
 */
public class BytesMessageImpl implements BytesMessage {
    /** 系统级消息头（Topic、Queue 等 OMS 标准键）。 */
    private KeyValue sysHeaders;
    /** 用户自定义消息头。 */
    private KeyValue userHeaders;
    /** 消息二进制载荷。 */
    private byte[] body;

    /** 创建空消息并初始化头容器。 */
    public BytesMessageImpl() {
        this.sysHeaders = OMS.newKeyValue();
        this.userHeaders = OMS.newKeyValue();
    }

    @Override
    /** 按类型读取消息体，当前仅支持 {@code byte[]}。 */
    public <T> T getBody(Class<T> type) throws OMSMessageFormatException {
        if (type == byte[].class) {
            return (T)body;
        }

        throw new OMSMessageFormatException("", "Cannot assign byte[] to " + type.getName());
    }

    @Override
    /** 设置消息体并返回自身以支持链式调用。 */
    public BytesMessage setBody(final byte[] body) {
        this.body = body;
        return this;
    }

    @Override
    public KeyValue sysHeaders() {
        return sysHeaders;
    }

    @Override
    public KeyValue userHeaders() {
        return userHeaders;
    }

    @Override
    public Message putSysHeaders(String key, int value) {
        sysHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putSysHeaders(String key, long value) {
        sysHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putSysHeaders(String key, double value) {
        sysHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putSysHeaders(String key, String value) {
        sysHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putUserHeaders(String key, int value) {
        userHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putUserHeaders(String key, long value) {
        userHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putUserHeaders(String key, double value) {
        userHeaders.put(key, value);
        return this;
    }

    @Override
    public Message putUserHeaders(String key, String value) {
        userHeaders.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
