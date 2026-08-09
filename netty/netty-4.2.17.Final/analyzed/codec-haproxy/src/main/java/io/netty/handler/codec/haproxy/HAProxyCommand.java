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

/**
 * HAProxy 代理协议头部的命令字段。
 * <p>
 * 标识连接是代理主动建立（LOCAL）还是代客户端转发（PROXY）。
 */
public enum HAProxyCommand {
    /**
     * LOCAL：代理自身发起的连接，非代客户端转发。
     */
    LOCAL(HAProxyConstants.COMMAND_LOCAL_BYTE),
    /**
     * PROXY：代客户端建立的连接，头部携带原始源/目的地址。
     */
    PROXY(HAProxyConstants.COMMAND_PROXY_BYTE);

    /** 命令编码在版本/命令字节的低 4 位。 */
    private static final byte COMMAND_MASK = 0x0f;

    private final byte byteValue;

    /** 创建枚举常量。 */
    HAProxyCommand(byte byteValue) {
        this.byteValue = byteValue;
    }

    /**
     * 根据字节低 4 位解析 {@link HAProxyCommand}。
     *
     * @param verCmdByte 协议版本与命令合并字节
     */
    public static HAProxyCommand valueOf(byte verCmdByte) {
        int cmd = verCmdByte & COMMAND_MASK;
        switch ((byte) cmd) {
            case HAProxyConstants.COMMAND_PROXY_BYTE:
                return PROXY;
            case HAProxyConstants.COMMAND_LOCAL_BYTE:
                return LOCAL;
            default:
                throw new IllegalArgumentException("unknown command: " + cmd);
        }
    }

    /** 返回该命令的字节值。 */
    public byte byteValue() {
        return byteValue;
    }
}
