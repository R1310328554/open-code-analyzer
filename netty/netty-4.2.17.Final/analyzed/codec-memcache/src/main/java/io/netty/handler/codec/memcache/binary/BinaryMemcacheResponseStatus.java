/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.memcache.binary;

import io.netty.util.internal.UnstableApi;

/**
 * Contains all possible status values a {@link BinaryMemcacheResponse} can return.
 *
 * <p>Memcache 二进制响应 status 码：0 表示成功；0x01–0x06 为常见业务错误（键不存在、CAS 冲突等）；
 * 0x20–0x21 用于 SASL 认证流程；0x81+ 为协议/资源类错误。</p>
 */
@UnstableApi
public final class BinaryMemcacheResponseStatus {

    private BinaryMemcacheResponseStatus() {
        // disallow construction
    }

    public static final short SUCCESS = 0x00;
    /** Key not found — 对应 GET 未命中或 DELETE 目标不存在。 */
    public static final short KEY_ENOENT = 0x01;
    /** Key exists — ADD 时键已存在。 */
    public static final short KEY_EEXISTS = 0x02;
    /** Value too large — 超出服务器 item 大小限制。 */
    public static final short E2BIG = 0x03;
    /** Invalid arguments — 如非法 expiration 或 delta 格式。 */
    public static final short EINVA = 0x04;
    /** CAS 不匹配导致 SET/DELETE 等条件写未执行。 */
    public static final short NOT_STORED = 0x05;
    /** INCR/DECR 的 delta 非数字字符串。 */
    public static final short DELTA_BADVAL = 0x06;
    public static final short AUTH_ERROR = 0x20;
    /** SASL 多步认证尚未完成。 */
    public static final short AUTH_CONTINUE = 0x21;
    public static final short UNKNOWN_COMMAND = 0x81;
    public static final short ENOMEM = 0x82;
}
