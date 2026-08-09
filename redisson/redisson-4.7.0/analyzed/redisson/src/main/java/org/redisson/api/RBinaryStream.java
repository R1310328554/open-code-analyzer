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
package org.redisson.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.SeekableByteChannel;

/**
 * 二进制流容器，在 Redis 中存储字节序列。
 * <p>单个流最大容量为 512MB。
 *
 * @author Nikita Koksharov
 */
public interface RBinaryStream extends RBucket<byte[]> {

    /**
     * 返回用于读写二进制流的异步 {@link AsynchronousByteChannel}。
     * <p>该对象非线程安全。
     *
     * @return 异步字节通道
     */
    AsynchronousByteChannel getAsynchronousChannel();

    /**
     * 返回用于读写二进制流的可定位 {@link SeekableByteChannel}。
     * <p>该对象非线程安全。
     *
     * @return 可定位字节通道
     */
    SeekableByteChannel getChannel();

    /**
     * 返回用于读取二进制流的 {@link InputStream}。
     * <p>该对象非线程安全。
     *
     * @return 输入流
     */
    InputStream getInputStream();

    /**
     * 返回用于写入二进制流的 {@link OutputStream}。
     * <p>该对象非线程安全。
     *
     * @return 输出流
     */
    OutputStream getOutputStream();
    
}
