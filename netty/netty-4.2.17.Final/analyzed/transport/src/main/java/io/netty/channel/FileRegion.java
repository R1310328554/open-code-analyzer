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
package io.netty.channel;

import io.netty.util.ReferenceCounted;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * A region of a file that is sent via a {@link Channel} which supports
 * <a href="https://en.wikipedia.org/wiki/Zero-copy">zero-copy file transfer</a>.
 * <p>通过支持零拷贝传输的 {@link Channel} 发送的文件区域，实现 {@link ReferenceCounted}。</p>
 *
 * <h3>Upgrade your JDK / JRE</h3>
 * <p>使用零拷贝前请将 JDK 升级至 1.6.0_18 或更高版本。</p>
 *
 * {@link FileChannel#transferTo(long, long, WritableByteChannel)} has at least
 * four known bugs in the old versions of Sun JDK and perhaps its derived ones.
 * Please upgrade your JDK to 1.6.0_18 or later version if you are going to use
 * zero-copy file transfer.
 * <ul>
 * <li><a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5103988">5103988</a>
 *   - FileChannel.transferTo() should return -1 for EAGAIN instead throws IOException</li>
 * <li><a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6253145">6253145</a>
 *   - FileChannel.transferTo() on Linux fails when going beyond 2GB boundary</li>
 * <li><a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6427312">6427312</a>
 *   - FileChannel.transferTo() throws IOException "system call interrupted"</li>
 * <li><a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6524172">6470086</a>
 *   - FileChannel.transferTo(2147483647, 1, channel) causes "Value too large" exception</li>
 * </ul>
 *
 * <h3>Check your operating system and JDK / JRE</h3>
 * <p>若操作系统或 JDK 不支持零拷贝，使用 {@link FileRegion} 可能失败或性能更差（如 Windows 上大文件）。</p>
 *
 * If your operating system (or JDK / JRE) does not support zero-copy file
 * transfer, sending a file with {@link FileRegion} might fail or yield worse
 * performance.  For example, sending a large file doesn't work well in Windows.
 *
 * <h3>Not all transports support it</h3>
 * <p>并非所有传输实现都支持 {@link FileRegion}。</p>
 */
public interface FileRegion extends ReferenceCounted {

    /**
     * Returns the offset in the file where the transfer began.
     * <p>返回文件中传输起始位置的偏移量。</p>
     */
    long position();

    /**
     * Returns the bytes which was transferred already.
     * <p>返回已传输的字节数（已弃用，请使用 {@link #transferred()}）。</p>
     *
     * @deprecated Use {@link #transferred()} instead.
     */
    @Deprecated
    long transfered();

    /**
     * Returns the bytes which was transferred already.
     * <p>返回已传输的字节数。部分异步传输（如 io_uring 对非 {@link DefaultFileRegion} 的分块发送）
     * 在字节入队提交时即递增计数，可能早于实际送达对端；通道关闭或写失败后计数可能偏大。</p>
     * <p>
     * Note: some asynchronous transports (such as the {@code io_uring} transport when falling
     * back to a chunked send for non-{@link DefaultFileRegion} implementations) advance this
     * counter when bytes have been queued for submission, which may be before they reach the
     * peer. If the channel is closed or the write fails after queuing, the reported value may
     * overstate the number of bytes actually delivered.
     */
    long transferred();

    /**
     * Returns the number of bytes to transfer.
     * <p>返回待传输的总字节数。</p>
     */
    long count();

    /**
     * Transfers the content of this file region to the specified channel.
     * <p>将本文件区域内容传输到目标通道；{@code position} 为相对本区域起始的偏移。</p>
     *
     * @param target    the destination of the transfer
     * @param position  the relative offset of the file where the transfer
     *                  begins from.  For example, <tt>0</tt> will make the
     *                  transfer start from {@link #position()}th byte and
     *                  <tt>{@link #count()} - 1</tt> will make the last
     *                  byte of the region transferred.
     */
    long transferTo(WritableByteChannel target, long position) throws IOException;

    @Override
    FileRegion retain();

    @Override
    FileRegion retain(int increment);

    @Override
    FileRegion touch();

    @Override
    FileRegion touch(Object hint);
}
