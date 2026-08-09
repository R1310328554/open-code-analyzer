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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Remoting 可序列化基类：基于 Fastjson2 提供 JSON/字节数组编解码。
 */
public abstract class RemotingSerializable {
    /** JSON 序列化统一使用 UTF-8。 */
    private final static Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    /** 将任意对象编码为 UTF-8 JSON 字节数组。 */
    public static byte[] encode(final Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONBytes(obj, CHARSET_UTF8);
    }

    /** 将对象转为 JSON 字符串，可选 PrettyFormat。 */
    public static String toJson(final Object obj, boolean prettyFormat) {
        if (prettyFormat) {
            return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
        }
        return JSON.toJSONString(obj);
    }

    /** 从 JSON 字节数组反序列化为指定类型。 */
    public static <T> T decode(final byte[] data, Class<T> classOfT) {
        if (data == null) {
            return null;
        }
        return JSON.parseObject(data, classOfT);
    }

    /** 解析 JSON 数组为 List&lt;T&gt;。 */
    public static <T> List<T> decodeList(final byte[] data, Class<T> classOfT) {
        if (data == null) {
            return null;
        }
        return JSON.parseArray(data, 0, data.length, CHARSET_UTF8, classOfT);
    }

    /** 从 JSON 字符串反序列化。 */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }

    /** 将当前实例编码为 JSON 字节数组。 */
    public byte[] encode() {
        return JSON.toJSONBytes(this, CHARSET_UTF8);
    }

    /**
     * 允许调用方传入 Fastjson2 {@link JSONWriter.Feature} 控制序列化行为。
     *
     * @param features 要启用的特性
     * @return 序列化后的字节数组
     */
    public byte[] encode(JSONWriter.Feature... features) {
        return JSON.toJSONBytes(this, CHARSET_UTF8, features);
    }

    /** 紧凑 JSON 字符串。 */
    public String toJson() {
        return toJson(false);
    }

    /** 可选格式化输出的 JSON 字符串。 */
    public String toJson(final boolean prettyFormat) {
        return toJson(this, prettyFormat);
    }
}