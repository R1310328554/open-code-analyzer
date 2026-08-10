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

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * Default {@link FileRegion} implementation which transfer data from a {@link FileChannel} or {@link File}.
 *
 * Be aware that the {@link FileChannel} will be automatically closed once {@link #refCnt()} returns
 * {@code 0}.
 * <p>默认 {@link FileRegion}：通过 {@link FileChannel#transferTo} 零拷贝发送文件数据。
 * 引用计数归零时会自动关闭底层 {@link FileChannel}。</p>
 */
public class DefaultFileRegion extends AbstractReferenceCounted implements FileRegion {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultFileRegion.class);
    /** 延迟打开的源文件（构造时仅保存引用） */
    private final File f;
    /** 传输起始偏移（相对文件开头） */
    private final long position;
    /** 待传输总字节数 */
    private final long count;
    /** 已成功传输的字节数 */
    private long transferred;
    /** 底层文件通道，可能延迟打开 */
    private FileChannel file;

    /**
     * Create a new instance
     * <p>直接使用已打开的 {@link FileChannel} 创建文件区域。</p>
     *
     * @param fileChannel      the {@link FileChannel} which should be transferred
     * @param position         the position from which the transfer should start
     * @param count            the number of bytes to transfer
     */
    public DefaultFileRegion(FileChannel fileChannel, long position, long count) {
        this.file = ObjectUtil.checkNotNull(fileChannel, "fileChannel");
        this.position = checkPositiveOrZero(position, "position");
        this.count = checkPositiveOrZero(count, "count");
        this.f = null;
    }

    /**
     * Create a new instance using the given {@link File}. The {@link File} will be opened lazily or
     * explicitly via {@link #open()}.
     * <p>基于 {@link File} 创建，首次传输或调用 {@link #open()} 时才打开文件描述符。</p>
     *
     * @param file         the {@link File} which should be transferred
     * @param position     the position from which the transfer should start
     * @param count        the number of bytes to transfer
     */
    public DefaultFileRegion(File file, long position, long count) {
        this.f = ObjectUtil.checkNotNull(file, "file");
        this.position = checkPositiveOrZero(position, "position");
        this.count = checkPositiveOrZero(count, "count");
    }

    /**
     * Returns {@code true} if the {@link FileRegion} has a open file-descriptor
     * <p>底层文件描述符是否已打开。</p>
     */
    public boolean isOpen() {
        return file != null;
    }

    /**
     * Explicitly open the underlying file-descriptor if not done yet.
     * <p>若尚未打开且引用计数 &gt; 0，则以只读方式打开文件并获取 {@link FileChannel}。</p>
     */
    public void open() throws IOException {
        if (!isOpen() && refCnt() > 0) {
            // Only open if this DefaultFileRegion was not released yet.
            // 仅在尚未 release 时打开，避免对已释放对象操作文件
            file = new RandomAccessFile(f, "r").getChannel();
        }
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public long count() {
        return count;
    }

    @Deprecated
    @Override
    public long transfered() {
        return transferred;
    }

    @Override
    public long transferred() {
        return transferred;
    }

    /**
     * <p>将文件片段传输到目标 {@link WritableByteChannel}；{@code position} 为相对本区域的偏移。</p>
     */
    @Override
    public long transferTo(WritableByteChannel target, long position) throws IOException {
        long count = this.count - position;
        if (count < 0 || position < 0) {
            throw new IllegalArgumentException(
                    "position out of range: " + position +
                    " (expected: 0 - " + (this.count - 1) + ')');
        }
        if (count == 0) {
            return 0L;
        }
        if (refCnt() == 0) {
            throw new IllegalReferenceCountException(0);
        }
        // Call open to make sure fc is initialized. This is a no-oop if we called it before.
        // 确保 FileChannel 已初始化（重复调用 open 为无操作）
        open();

        long written = file.transferTo(this.position + position, count, target);
        if (written > 0) {
            transferred += written;
        } else if (written == 0) {
            // If the amount of written data is 0 we need to check if the requested count is bigger then the
            // actual file itself as it may have been truncated on disk.
            //
            // See https://github.com/netty/netty/issues/8868
            // 写入 0 字节时校验文件是否已被截断，避免 silent failure
            validate(this, position);
        }
        return written;
    }

    /** 引用计数归零时关闭 {@link FileChannel}。 */
    @Override
    protected void deallocate() {
        FileChannel file = this.file;

        if (file == null) {
            return;
        }
        this.file = null;

        try {
            file.close();
        } catch (IOException e) {
            logger.warn("Failed to close a file.", e);
        }
    }

    @Override
    public FileRegion retain() {
        super.retain();
        return this;
    }

    @Override
    public FileRegion retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public FileRegion touch() {
        return this;
    }

    @Override
    public FileRegion touch(Object hint) {
        return this;
    }

    /**
     * <p>校验请求传输范围是否超出磁盘上实际文件大小（处理文件被截断的情况）。</p>
     */
    static void validate(DefaultFileRegion region, long position) throws IOException {
        // If the amount of written data is 0 we need to check if the requested count is bigger then the
        // actual file itself as it may have been truncated on disk.
        //
        // See https://github.com/netty/netty/issues/8868
        long size = region.file.size();
        long count = region.count - position;
        if (region.position + count + position > size) {
            throw new IOException("Underlying file size " + size + " smaller then requested count " + region.count);
        }
    }
}
