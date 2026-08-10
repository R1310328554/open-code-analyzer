/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.socks;

import io.netty.util.internal.StringUtil;

/**
 * SOCKS 编解码模块内部共享常量与 IPv6 格式化工具。
 * <p>各 {@link ReplayingDecoder} 在协议版本错误或地址类型未知时，
 * 向 pipeline 输出单例 {@link #UNKNOWN_SOCKS_REQUEST}/{@link #UNKNOWN_SOCKS_RESPONSE}，
 * 避免抛异常中断连接。</p>
 */
final class SocksCommonUtils {
    /** 解码失败时使用的请求占位符，类型为 {@link UnknownSocksRequest}。 */
    public static final SocksRequest UNKNOWN_SOCKS_REQUEST = new UnknownSocksRequest();
    /** 解码失败时使用的响应占位符，类型为 {@link UnknownSocksResponse}。 */
    public static final SocksResponse UNKNOWN_SOCKS_RESPONSE = new UnknownSocksResponse();

    /**
     * A constructor to stop this class being constructed.
     */
    private SocksCommonUtils() {
        // NOOP
    }

    private static final char ipv6hextetSeparator = ':';

    /**
     * Converts numeric IPv6 to standard (non-compressed) format.
     * <p>将 16 字节大端 IPv6 地址转为 8 组十六进制、冒号分隔的标准字符串（不压缩连续零段），
     * 供 {@link SocksCmdRequestDecoder} 等在 DOMAIN 之外解析 ATYP=0x04 时使用。</p>
     */
    public static String ipv6toStr(byte[] src) {
        assert src.length == 16;
        StringBuilder sb = new StringBuilder(39);
        ipv6toStr(sb, src, 0, 8);
        return sb.toString();
    }

    /** 在 [fromHextet, toHextet) 范围内逐 hextet 追加，组间插入 {@link #ipv6hextetSeparator}。 */
    private static void ipv6toStr(StringBuilder sb, byte[] src, int fromHextet, int toHextet) {
        int i;
        toHextet --;
        for (i = fromHextet; i < toHextet; i++) {
            appendHextet(sb, src, i);
            sb.append(ipv6hextetSeparator);
        }

        appendHextet(sb, src, i);
    }

    /** 每个 hextet 占 src 中 2 字节，转为 4 位十六进制小写。 */
    private static void appendHextet(StringBuilder sb, byte[] src, int i) {
        StringUtil.toHexString(sb, src, i << 1, 2);
    }

}
