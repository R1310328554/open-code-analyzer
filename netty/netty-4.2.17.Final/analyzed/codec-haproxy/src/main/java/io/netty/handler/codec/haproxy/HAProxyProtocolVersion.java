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
package io.netty.handler.codec.haproxy;

import static io.netty.handler.codec.haproxy.HAProxyConstants.*;

/**
 * HAProxy PROXY 协议规范版本枚举。
 * <p>
 * v1 为可读文本头，v2 为二进制头；由头部第 13 字节高 4 位标识。
 */
public enum HAProxyProtocolVersion {
    /** v1：人类可读文本格式头部。 */
    V1(VERSION_ONE_BYTE),
    /** v2：二进制格式头部。 */
    V2(VERSION_TWO_BYTE);

    /** 版本与命令字节的高 4 位为版本号。 */
    private static final byte VERSION_MASK = (byte) 0xf0;

    private final byte byteValue;

    /** 枚举构造，绑定规范定义的字节值。 */
    HAProxyProtocolVersion(byte byteValue) {
        this.byteValue = byteValue;
    }

    /**
     * 从版本与命令字节的高 4 位解析 {@link HAProxyProtocolVersion}。
     *
     * @param verCmdByte protocol version and command byte
     */
    public static HAProxyProtocolVersion valueOf(byte verCmdByte) {
        int version = verCmdByte & VERSION_MASK;
        switch ((byte) version) {
            case VERSION_TWO_BYTE:
                return V2;
            case VERSION_ONE_BYTE:
                return V1;
            default:
                throw new IllegalArgumentException("unknown version: " + version);
        }
    }

    /** 返回本版本对应的字节值。 */
    public byte byteValue() {
        return byteValue;
    }
}
