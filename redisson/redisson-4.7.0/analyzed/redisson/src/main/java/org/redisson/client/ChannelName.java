/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.netty.util.CharsetUtil;

/**
 * Redis 频道名称，实现 {@link CharSequence} 以便与 Netty 及协议层互操作。
 * <p>
 * 内部同时保存 UTF-8 字节数组与字符串形式，支持键空间通知与客户端追踪频道识别。
 *
 * @author Nikita Koksharov
 *
 */
public class ChannelName implements CharSequence {

    /** 客户端缓存追踪失效通知频道。 */
    public static final ChannelName TRACKING = new ChannelName("__redis__:invalidate");

    /** 创建仅含单个 {@link ChannelName} 的列表。 */
    public static List<ChannelName> newList(ChannelName name) {
        List<ChannelName> result = new ArrayList<>(1);
        result.add(name);
        return result;
    }

    /** 由字符串频道名创建单元素列表。 */
    public static List<ChannelName> newList(String name) {
        List<ChannelName> result = new ArrayList<>(1);
        result.add(new ChannelName(name));
        return result;
    }

    /** 频道名的 UTF-8 字节表示。 */
    private final byte[] name;
    /** 频道名字符串形式。 */
    private final String str;

    public ChannelName(byte[] name) {
        super();
        this.name = name;
        this.str = new String(name, CharsetUtil.UTF_8);
    }

    public ChannelName(String name) {
        this(name.getBytes(CharsetUtil.UTF_8));
    }

    @Override
    public String toString() {
        return str;
    }

    /** 返回频道名的 UTF-8 字节数组。 */
    public byte[] getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(name);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChannelName) {
            return Arrays.equals(name, ((ChannelName) obj).name);
        }
        if (obj instanceof CharSequence) {
            return toString().equals(obj);
        }
        return false;
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /** 判断是否为键空间或键事件通知频道（{@code __keyspace}、{@code __keyevent} 等前缀）。 */
    public boolean isKeyspace() {
        return str.startsWith("__keyspace") || str.startsWith("__keyevent")
                || str.startsWith("__subkeyspace") || str.startsWith("__subkeyevent");
    }

    /** 判断是否为客户端缓存追踪失效频道。 */
    public boolean isTracking() {
        return str.equals(TRACKING.toString());
    }

}
