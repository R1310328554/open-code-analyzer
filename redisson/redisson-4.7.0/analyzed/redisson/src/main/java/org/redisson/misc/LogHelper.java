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
package org.redisson.misc;

import io.netty.buffer.ByteBuf;
import org.redisson.client.protocol.CommandData;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * 将 Redis 命令、参数等对象格式化为日志安全字符串。
 * <p>
 * 对 AUTH 密码、过长字符串/集合自动截断或脱敏，
 * 避免日志泄露敏感信息或输出过大。
 *
 * @author Philipp Marx
 */
public final class LogHelper {

    /** 集合日志最多展示的元素个数（可通过系统属性配置）。 */
    private static final int MAX_COLLECTION_LOG_SIZE = Integer.parseInt(System.getProperty("redisson.maxCollectionLogSize", "10"));
    /** 字符串日志最大长度（可通过系统属性配置）。 */
    private static final int MAX_STRING_LOG_SIZE = Integer.parseInt(System.getProperty("redisson.maxStringLogSize", "1000"));
//    private static final int MAX_BYTEBUF_LOG_SIZE = Integer.valueOf(System.getProperty("redisson.maxByteBufLogSize", "1000"));

    private LogHelper() {
    }
    
    /** 格式化 Redis 命令与参数；AUTH 命令隐藏密码。 */
    public static String toString(RedisCommand<?> command, Object... params) {
        if (RedisCommands.AUTH.equals(command)) {
            return "command: " + command + ", params: (password masked)";
        }
        return "command: " + command + ", params: " + LogHelper.toString(params);
    }
    
    /** 递归格式化任意对象为日志字符串。 */
    public static String toString(Object object) {
        if (object == null) {
            return "null";
        } else if (object instanceof String) {
            return toStringString((String) object);
        } else if (object.getClass().isArray()) {
            return toArrayString(object);
        } else if (object instanceof Collection) {
            return toCollectionString((Collection<?>) object);
        } else if (object instanceof CommandData) {
            CommandData<?, ?> cd = (CommandData<?, ?>) object;
            if (RedisCommands.AUTH.equals(cd.getCommand())) {
                return cd.getCommand() + ", params: (password masked)";
            }
            return cd.getCommand() + ", params: " + LogHelper.toString(cd.getParams()) + ", promise: " + cd.getPromise();
        } else if (object instanceof ByteBuf) {
            final ByteBuf byteBuf = (ByteBuf) object;
            // 因可能触发 Buffer Leak 检测，暂不展开 ByteBuf 内容
//            if (byteBuf.refCnt() > 0) {
//                if (byteBuf.writerIndex() > MAX_BYTEBUF_LOG_SIZE) {
//                    return new StringBuilder(byteBuf.toString(0, MAX_BYTEBUF_LOG_SIZE, CharsetUtil.UTF_8)).append("...").toString();
//                } else {
//                    return byteBuf.toString(0, byteBuf.writerIndex(), CharsetUtil.UTF_8);
//                }
//            }
            return byteBuf.toString();
        } else {
            return String.valueOf(object);
        }
    }

    /** 截断过长字符串并追加省略号。 */
    private static String toStringString(String string) {
        if (string.length() > MAX_STRING_LOG_SIZE) {
            return new StringBuilder(string.substring(0, MAX_STRING_LOG_SIZE)).append("...").toString();
        } else {
            return string;
        }
    }

    /** 格式化集合并限制最大元素数。 */
    private static String toCollectionString(Collection<?> collection) {
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder b = new StringBuilder(collection.size() * 3);
        b.append('[');
        int i = 0;
        for (Object object : collection) {
            b.append(toString(object));
            i++;

            if (i == collection.size()) {
                b.append(']');
                break;
            }
            b.append(", ");
            
            if (i == MAX_COLLECTION_LOG_SIZE) {
                b.append("...]");
                break;
            }
        }
        
        return b.toString();
    }

    /** 格式化数组并限制最大元素数。 */
    private static String toArrayString(Object array) {
        int length = Array.getLength(array) - 1;
        if (length == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder(length * 3);
        b.append('[');
        for (int i = 0;; ++i) {
            b.append(toString(Array.get(array, i)));

            if (i == length) {
                return b.append(']').toString();
            }

            b.append(", ");

            if (i == MAX_COLLECTION_LOG_SIZE - 1) {
                return b.append("...]").toString();
            }
        }
    }
}
