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
 * Represents all Opcodes that can occur in a {@link BinaryMemcacheMessage}.
 * <p/>
 * This class can be extended if a custom application needs to implement a superset of the normally supported
 * operations by a vanilla memcached protocol.
 *
 * <p>Memcache 二进制协议 opcode 常量表。带 Q 后缀的命令（如 GETQ、SETQ）为「quiet」变体：
 * 成功时不回响应包，用于 pipeline 批量操作以减少 RTT；无 Q 后缀则每条命令均有对应响应。</p>
 */
@UnstableApi
public final class BinaryMemcacheOpcodes {

    private BinaryMemcacheOpcodes() {
        // disallow construction
    }

    public static final byte GET = 0x00;
    public static final byte SET = 0x01;
    public static final byte ADD = 0x02;
    public static final byte REPLACE = 0x03;
    public static final byte DELETE = 0x04;
    public static final byte INCREMENT = 0x05;
    public static final byte DECREMENT = 0x06;
    public static final byte QUIT = 0x07;
    public static final byte FLUSH = 0x08;
    /** quiet GET：命中时不单独回包，常与 pipeline 末端的非 quiet 命令配合。 */
    public static final byte GETQ = 0x09;
    public static final byte NOOP = 0x0a;
    public static final byte VERSION = 0x0b;
    /** GET 且响应中带 key（用于未知 key 是否存在的探测场景）。 */
    public static final byte GETK = 0x0c;
    public static final byte GETKQ = 0x0d;
    public static final byte APPEND = 0x0e;
    public static final byte PREPEND = 0x0f;
    public static final byte STAT = 0x10;
    public static final byte SETQ = 0x11;
    public static final byte ADDQ = 0x12;
    public static final byte REPLACEQ = 0x13;
    public static final byte DELETEQ = 0x14;
    public static final byte INCREMENTQ = 0x15;
    public static final byte DECREMENTQ = 0x16;
    public static final byte QUITQ = 0x17;
    public static final byte FLUSHQ = 0x18;
    public static final byte APPENDQ = 0x19;
    public static final byte PREPENDQ = 0x1a;
    /** 刷新过期时间而不改写 value。 */
    public static final byte TOUCH = 0x1c;
    /** Get-And-Touch：读取 value 同时延长 TTL。 */
    public static final byte GAT = 0x1d;
    public static final byte GATQ = 0x1e;
    public static final byte GATK = 0x23;
    public static final byte GATKQ = 0x24;
    public static final byte SASL_LIST_MECHS = 0x20;
    public static final byte SASL_AUTH = 0x21;
    public static final byte SASL_STEP = 0x22;
}
