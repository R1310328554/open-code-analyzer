/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.stomp;

import io.netty.handler.codec.Headers;
import io.netty.util.AsciiString;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * The multimap data structure for the STOMP header names and values. It also provides the constants for the standard
 * STOMP header names and values.
 * <p>STOMP 帧头部的多值映射接口，键值均为 {@link CharSequence}。下方常量预缓存 RFC 标准头部名，
 * 供编解码器与业务代码引用，避免重复分配字符串。</p>
 */
public interface StompHeaders extends Headers<CharSequence, CharSequence, StompHeaders> {

    /** 客户端支持的 STOMP 协议版本列表。 */
    AsciiString ACCEPT_VERSION = AsciiString.cached("accept-version");
    /** 连接目标主机。 */
    AsciiString HOST = AsciiString.cached("host");
    /** 登录用户名。 */
    AsciiString LOGIN = AsciiString.cached("login");
    /** 登录密码。 */
    AsciiString PASSCODE = AsciiString.cached("passcode");
    /** 心跳协商（发送:接收 毫秒对）。 */
    AsciiString HEART_BEAT = AsciiString.cached("heart-beat");
    /** 协商选用的协议版本。 */
    AsciiString VERSION = AsciiString.cached("version");
    /** 服务端分配的会话标识。 */
    AsciiString SESSION = AsciiString.cached("session");
    /** 服务端软件标识。 */
    AsciiString SERVER = AsciiString.cached("server");
    /** 消息目的地。 */
    AsciiString DESTINATION = AsciiString.cached("destination");
    /** 订阅或消息标识。 */
    AsciiString ID = AsciiString.cached("id");
    /** 消息确认模式。 */
    AsciiString ACK = AsciiString.cached("ack");
    /** 事务标识。 */
    AsciiString TRANSACTION = AsciiString.cached("transaction");
    /** 请求服务端回执的标识。 */
    AsciiString RECEIPT = AsciiString.cached("receipt");
    /** 服务端分配的消息 ID。 */
    AsciiString MESSAGE_ID = AsciiString.cached("message-id");
    /** 订阅标识（与 id 对应）。 */
    AsciiString SUBSCRIPTION = AsciiString.cached("subscription");
    /** 回执帧引用的 receipt 值。 */
    AsciiString RECEIPT_ID = AsciiString.cached("receipt-id");
    /** 错误帧中的描述文本。 */
    AsciiString MESSAGE = AsciiString.cached("message");
    /** 正文长度（字节），有则按定长读取。 */
    AsciiString CONTENT_LENGTH = AsciiString.cached("content-length");
    /** 正文 MIME 类型。 */
    AsciiString CONTENT_TYPE = AsciiString.cached("content-type");

    /**
     * {@link Headers#get(Object)} and convert the result to a {@link String}.
     * <p>取首个同名头部并转为 {@link String}；不存在时返回 {@code null}。</p>
     * @param name the name of the header to retrieve
     * @return the first header value if the header is found. {@code null} if there's no such header.
     */
    String getAsString(CharSequence name);

    /**
     * {@link Headers#getAll(Object)} and convert each element of {@link List} to a {@link String}.
     * <p>取某头部全部值并转为 {@link String} 列表；无值时返回空列表。</p>
     * @param name the name of the header to retrieve
     * @return a {@link List} of header values or an empty {@link List} if no values are found.
     */
    List<String> getAllAsString(CharSequence name);

    /**
     * {@link #iterator()} that converts each {@link Entry}'s key and value to a {@link String}.
     * <p>以 {@link String} 键值对迭代全部头部，便于日志与调试。</p>
     */
    Iterator<Entry<String, String>> iteratorAsString();

    /**
     * Returns {@code true} if a header with the {@code name} and {@code value} exists, {@code false} otherwise.
     * <p>
     * If {@code ignoreCase} is {@code true} then a case insensitive compare is done on the value.
     * <p>判断是否存在指定名值的头部；{@code ignoreCase} 为真时对值做大小写不敏感比较。</p>
     * @param name the name of the header to find
     * @param value the value of the header to find
     * @param ignoreCase {@code true} then a case insensitive compare is run to compare values.
     * otherwise a case sensitive compare is run to compare values.
     */
    boolean contains(CharSequence name, CharSequence value, boolean ignoreCase);
}
