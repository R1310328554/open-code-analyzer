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

package org.apache.rocketmq.remoting.protocol;

import java.util.HashMap;

import org.apache.rocketmq.remoting.exception.RemotingCommandException;

import io.netty.buffer.ByteBuf;

/**
 * 高性能 Remoting 请求头：直接对 {@link ByteBuf} 编解码，绕过反射字段映射。
 */
public interface FastCodesHeader {

    /** 从字段 Map 取值；缺失时记错误日志但不抛异常，保持与旧解码兼容。 */
    default String getAndCheckNotNull(HashMap<String, String> fields, String field) {
        String value = fields.get(field);
        if (value == null) {
            String headerClass = this.getClass().getSimpleName();
            RemotingCommand.log.error("the custom field {}.{} is null", headerClass, field);
            // 不抛异常，与 RemotingCommand.decodeCommandCustomHeader 行为一致
        }
        return value;
    }

    /** value 非空时按 RocketMQ 字符串格式写入键值对。 */
    default void writeIfNotNull(ByteBuf out, String key, Object value) {
        if (value != null) {
            RocketMQSerializable.writeStr(out, true, key);
            RocketMQSerializable.writeStr(out, false, value.toString());
        }
    }

    /** 将请求头字段序列化到 ByteBuf。 */
    void encode(ByteBuf out);

    /** 从 extFields 解析并填充请求头。 */
    void decode(HashMap<String, String> fields) throws RemotingCommandException;


}
