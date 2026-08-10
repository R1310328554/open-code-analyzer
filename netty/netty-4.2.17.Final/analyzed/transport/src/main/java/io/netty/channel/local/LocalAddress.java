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
package io.netty.channel.local;

import static io.netty.util.internal.ObjectUtil.checkNonEmptyAfterTrim;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.UUID;

/**
 * An endpoint in the local transport.  Each endpoint is identified by a unique
 * case-insensitive string.
 *
 * <p>本地传输端点，由唯一的不区分大小写字符串 ID 标识。</p>
 */
public final class LocalAddress extends SocketAddress implements Comparable<LocalAddress> {

    private static final long serialVersionUID = 4644331421130916435L;

    /** 通配地址，绑定时会自动分配临时 ID。 */
    public static final LocalAddress ANY = new LocalAddress("ANY");

    private final String id;
    private final String strVal;

    /**
     * Creates a new ephemeral port based on the ID of the specified channel.
     * Note that we prepend an upper-case character so that it never conflicts with
     * the addresses created by a user, which are always lower-cased on construction time.
     *
     * <p>根据 channel 哈希生成临时地址；前缀大写字符避免与用户手动创建的小写地址冲突。</p>
     */
    LocalAddress(Channel channel) {
        StringBuilder buf = new StringBuilder(16);
        buf.append("local:E");
        buf.append(Long.toHexString(channel.hashCode() & 0xFFFFFFFFL | 0x100000000L));
        buf.setCharAt(7, ':');
        id = buf.substring(6);
        strVal = buf.toString();
    }

    /**
     * Creates a new instance with the specified ID.
     * <p>使用指定 ID 创建地址（构造时会转为小写）。</p>
     */
    public LocalAddress(String id) {
        this.id = checkNonEmptyAfterTrim(id, "id").toLowerCase();
        strVal = "local:" + this.id;
    }

    /**
     * Creates a new instance with a random ID based on the given class.
     * <p>基于类名与随机 UUID 生成唯一 ID。</p>
     */
    public LocalAddress(Class<?> cls) {
        this(cls.getSimpleName() + '/' + UUID.randomUUID());
    }

    /**
     * Returns the ID of this address.
     * <p>返回地址 ID 字符串。</p>
     */
    public String id() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LocalAddress)) {
            return false;
        }

        return id.equals(((LocalAddress) o).id);
    }

    @Override
    public int compareTo(LocalAddress o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return strVal;
    }
}
