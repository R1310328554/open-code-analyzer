/*
 * Copyright 2025 The Netty Project
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
 * The status of a SOCKS5 private authentication response.
 * <p>
 * RFC 1928 reserves method codes 0x80-0xFE for private authentication methods but does not
 * specify the format of their subnegotiation. This class provides standard status codes
 * for the private authentication response that follow the pattern established by the
 * username/password authentication method in RFC 1929.
 * </p>
 *
 * <p>SOCKS5 私有认证应答状态。RFC 1928 未规定私有方法子协商格式，本类沿用 RFC 1929
 * 模式：{@link #SUCCESS}(0x00) 成功、{@link #FAILURE}(0xFF) 失败。</p>
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1928.txt">RFC 1928 Section 3</a>
 * @see <a href="https://www.ietf.org/rfc/rfc1929.txt">RFC 1929</a>
 */
public final class Socks5PrivateAuthStatus implements Comparable<Socks5PrivateAuthStatus> {

    /** 私有认证成功。 */
    public static final Socks5PrivateAuthStatus SUCCESS = new Socks5PrivateAuthStatus(0x00, "SUCCESS");
    /** 私有认证失败。 */
    public static final Socks5PrivateAuthStatus FAILURE = new Socks5PrivateAuthStatus(0xFF, "FAILURE");

    /**
     * Returns the {@link Socks5PrivateAuthStatus} instance that corresponds to the specified byte value.
     * <p>
     * This method returns a singleton instance for standard status codes:
     * <ul>
     *   <li>0x00: {@link #SUCCESS}</li>
     *   <li>0xFF: {@link #FAILURE}</li>
     * </ul>
     * For any other values, a new instance is created.
     *
     * @param b The byte value of the SOCKS5 private authentication status
     * @return The corresponding {@link Socks5PrivateAuthStatus} instance
     */
    public static Socks5PrivateAuthStatus valueOf(byte b) {
        switch (b) {
            case 0x00:
                return SUCCESS;
            case (byte) 0xFF:
                return FAILURE;
        }

        return new Socks5PrivateAuthStatus(b);
    }

    private final byte byteValue;
    private final String name;
    private String text;

    private Socks5PrivateAuthStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    /**
     * Creates a new SOCKS5 private authentication status.
     *
     * @param byteValue The byte value representing the authentication status
     *                  (0x00 for success, 0xFF for failure, or custom values)
     * @param name      The descriptive name of this status, must not be null
     * @throws NullPointerException if the name is null
     */
    public Socks5PrivateAuthStatus(int byteValue, String name) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public byte byteValue() {
        return byteValue;
    }

    /** 是否为认证成功状态 (0x00)。 */
    public boolean isSuccess() {
        return byteValue == 0;
    }

    @Override
    public int hashCode() {
        return byteValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Socks5PrivateAuthStatus)) {
            return false;
        }

        return byteValue == ((Socks5PrivateAuthStatus) obj).byteValue;
    }

    @Override
    public int compareTo(Socks5PrivateAuthStatus o) {
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
