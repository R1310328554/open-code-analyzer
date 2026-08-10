/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.epoll;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;
import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/** TCP MD5 签名选项工具：校验密钥长度并增量更新 {@link AbstractEpollChannel} 上的 MD5 映射 */
final class TcpMd5Util {

    static Collection<InetAddress> newTcpMd5Sigs(AbstractEpollChannel channel, Collection<InetAddress> current,
                                         Map<InetAddress, byte[]> newKeys) throws IOException {
        checkNotNull(channel, "channel");
        checkNotNull(current, "current");
        checkNotNull(newKeys, "newKeys");

        // 校验新密钥非空且长度不超过 TCP_MD5SIG_MAXKEYLEN
        for (Entry<InetAddress, byte[]> e : newKeys.entrySet()) {
            final byte[] key = e.getValue();
            checkNotNullWithIAE(e.getKey(), "e.getKey");
            checkNonEmpty(key, e.getKey().toString());
            if (key.length > Native.TCP_MD5SIG_MAXKEYLEN) {
                throw new IllegalArgumentException("newKeys[" + e.getKey() +
                    "] has a key with invalid length; should not exceed the maximum length (" +
                        Native.TCP_MD5SIG_MAXKEYLEN + ')');
            }
        }

        // 移除新映射中不存在的旧地址（密钥置 null）
        for (InetAddress addr : current) {
            if (!newKeys.containsKey(addr)) {
                channel.socket.setTcpMd5Sig(addr, null);
            }
        }

        if (newKeys.isEmpty()) {
            return Collections.emptySet();
        }

        // 写入新映射并收集已设置地址
        final Collection<InetAddress> addresses = new ArrayList<InetAddress>(newKeys.size());
        for (Entry<InetAddress, byte[]> e : newKeys.entrySet()) {
            channel.socket.setTcpMd5Sig(e.getKey(), e.getValue());
            addresses.add(e.getKey());
        }

        return addresses;
    }

    private TcpMd5Util() {
    }
}
