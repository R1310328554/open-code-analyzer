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

package io.netty.handler.codec.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/**
 * The status of {@link Socks5PasswordAuthResponse}.
 *
 * <p>SOCKS5 用户名/密码子协商应答状态（RFC 1929）。
 * 标准值：{@link #SUCCESS}(0x00) 认证成功，{@link #FAILURE}(0xFF) 认证失败；
 * 非标准字节值会动态创建 {@code UNKNOWN} 实例。</p>
 */
public class Socks5PasswordAuthStatus implements Comparable<Socks5PasswordAuthStatus> {

    /** 认证成功，可进入 SOCKS 命令阶段。 */
    public static final Socks5PasswordAuthStatus SUCCESS = new Socks5PasswordAuthStatus(0x00, "SUCCESS");
    /** 认证失败，连接应终止。 */
    public static final Socks5PasswordAuthStatus FAILURE = new Socks5PasswordAuthStatus(0xFF, "FAILURE");

    /** 按字节值查找状态；0x00/0xFF 返回单例，其余创建 UNKNOWN 实例。 */
    public static Socks5PasswordAuthStatus valueOf(byte b) {
        switch (b) {
        case 0x00:
            return SUCCESS;
        case (byte) 0xFF:
            return FAILURE;
        }

        return new Socks5PasswordAuthStatus(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    public Socks5PasswordAuthStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5PasswordAuthStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public byte byteValue() {
        return byteValue;
    }

    /** 是否为 RFC 1929 定义的认证成功状态 (0x00)。 */
    public boolean isSuccess() {
        return byteValue == 0;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks5PasswordAuthStatus)) {
            return false;
        }

        return byteValue == ((Socks5PasswordAuthStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5PasswordAuthStatus o) {
        return byteValue - o.byteValue;
    }

    @Override
    public String toString() {
        String text = this.text;
        if (text == null) {
            this.text = text = name + '(' + (byteValue & 0xFF) + ')';
        }
        return text;
    }
}
