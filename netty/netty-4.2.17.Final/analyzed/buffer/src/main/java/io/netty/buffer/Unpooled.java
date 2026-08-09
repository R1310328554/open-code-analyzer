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
package io.netty.buffer;

import io.netty.buffer.CompositeByteBuf.ByteWrapper;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 * 通过分配、包装或拷贝创建 {@link ByteBuf} 的静态工厂。
 * <p>
 * 建议 {@code import static io.netty.buffer.Unpooled.*;} 静态导入常用方法。
 * <h3>新建缓冲区</h3>
 * {@link #buffer(int)} 堆内存；{@link #directBuffer(int)} 直接内存。
 * <h3>包装（wrapped）</h3>
 * 视图共享底层数组/NIO 缓冲，修改互相可见；多段可用 varargs {@code wrappedBuffer()} 减少拷贝。
 * <h3>拷贝（copied）</h3>
 * 深拷贝，与原数据无共享；{@code copiedBuffer()} 亦可合并多段为单一缓冲区。
 */
public final class Unpooled {

    /** 内部使用的非池化分配器。 */
    private static final ByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;

    /** 大端字节序常量。 */
    /**
     * Big endian byte order.
     */
    public static final ByteOrder BIG_ENDIAN = ByteOrder.BIG_ENDIAN;

    /** 小端字节序常量。 */
    /**
     * Little endian byte order.
     */
    public static final ByteOrder LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    /** 容量为 0 的空缓冲区单例。 */
    /**
     * A buffer whose capacity is {@code 0}.
     */
    @SuppressWarnings("checkstyle:StaticFinalBuffer")  // EmptyByteBuf is not writeable or readable.
    public static final ByteBuf EMPTY_BUFFER = ALLOC.buffer(0, 0);

    static {
        assert EMPTY_BUFFER instanceof EmptyByteBuf: "EMPTY_BUFFER must be an EmptyByteBuf.";
    }

    /** 创建可按需扩容的大端堆缓冲区（较小初始容量）。 */
    public static ByteBuf buffer() {
        return ALLOC.heapBuffer();
    }

    /** 创建可按需扩容的大端直接缓冲区。 */
    public static ByteBuf directBuffer() {
        return ALLOC.directBuffer();
    }

    /** 指定初始容量的大端堆缓冲区；readerIndex/writerIndex 均为 0。 */
    public static ByteBuf buffer(int initialCapacity) {
        return ALLOC.heapBuffer(initialCapacity);
    }

    /** 指定初始容量的大端直接缓冲区；读写索引均为 0。 */
    public static ByteBuf directBuffer(int initialCapacity) {
        return ALLOC.directBuffer(initialCapacity);
    }

    /** 堆缓冲区：初始容量 initialCapacity，最大 maxCapacity。 */
    public static ByteBuf buffer(int initialCapacity, int maxCapacity) {
        return ALLOC.heapBuffer(initialCapacity, maxCapacity);
    }

    /** 直接缓冲区：初始 initialCapacity，上限 maxCapacity。 */
    public static ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
        return ALLOC.directBuffer(initialCapacity, maxCapacity);
    }

    /** 包装 byte 数组；修改数组对视图可见。 */
    public static ByteBuf wrappedBuffer(byte[] array) {
        if (array.length == 0) {
            return EMPTY_BUFFER;
        }
        return new UnpooledHeapByteBuf(ALLOC, array, array.length);
    }

    /** 包装数组子区域 [offset, offset+length)。 */
    public static ByteBuf wrappedBuffer(byte[] array, int offset, int length) {
        if (length == 0) {
            return EMPTY_BUFFER;
        }

        if (offset == 0 && length == array.length) {
            return wrappedBuffer(array);
        }

        return wrappedBuffer(array).slice(offset, length);
    }

    /** 包装 NIO {@link ByteBuffer} 当前可读区域。 */
    public static ByteBuf wrappedBuffer(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return EMPTY_BUFFER;
        }
        if (!buffer.isDirect() && buffer.hasArray()) {
            return wrappedBuffer(
                    buffer.array(),
                    buffer.arrayOffset() + buffer.position(),
                    buffer.remaining()).order(buffer.order());
        } else if (PlatformDependent.hasUnsafe()) {
            if (buffer.isReadOnly()) {
                if (buffer.isDirect()) {
                    return new ReadOnlyUnsafeDirectByteBuf(ALLOC, buffer);
                } else {
                    return new ReadOnlyByteBufferBuf(ALLOC, buffer);
                }
            } else {
                return new UnpooledUnsafeDirectByteBuf(ALLOC, buffer, buffer.remaining());
            }
        } else {
            if (buffer.isReadOnly()) {
                return new ReadOnlyByteBufferBuf(ALLOC, buffer);
            }  else {
                return new UnpooledDirectByteBuf(ALLOC, buffer, buffer.remaining());
            }
        }
    }

    /** 包装指定内存地址；doFree 为 true 时引用计数归零自动释放。 */
    public static ByteBuf wrappedBuffer(long memoryAddress, int size, boolean doFree) {
        return new WrappedUnpooledUnsafeDirectByteBuf(ALLOC, memoryAddress, size, doFree);
    }

    /**
     * 包装 buffer 的可读字节（slice）；无可读内容则 release 并返回 EMPTY。
     * @param buffer 被包装缓冲区，引用计数所有权转移给本方法
     * @return 可读部分或空缓冲；调用方负责 release 返回值
     */
    public static ByteBuf wrappedBuffer(ByteBuf buffer) {
        if (buffer.isReadable()) {
            return buffer.slice();
        } else {
            buffer.release();
            return EMPTY_BUFFER;
        }
    }

    /** 零拷贝组合包装多个 byte 数组。 */
    public static ByteBuf wrappedBuffer(byte[]... arrays) {
        return wrappedBuffer(arrays.length, arrays);
    }

    /**
     * 组合包装多个 {@link ByteBuf} 可读部分；引用计数转移给本方法。
     * @param buffers 待包装缓冲区
     * @return 组合视图；调用方负责 release
     */
    public static ByteBuf wrappedBuffer(ByteBuf... buffers) {
        return wrappedBuffer(buffers.length, buffers);
    }

    /** 组合包装多个 NIO {@link ByteBuffer} 切片。 */
    public static ByteBuf wrappedBuffer(ByteBuffer... buffers) {
        return wrappedBuffer(buffers.length, buffers);
    }

    static <T> ByteBuf wrappedBuffer(int maxNumComponents, ByteWrapper<T> wrapper, T[] array) {
        switch (array.length) {
        case 0:
            break;
        case 1:
            if (!wrapper.isEmpty(array[0])) {
                return wrapper.wrap(array[0]);
            }
            break;
        default:
            for (int i = 0, len = array.length; i < len; i++) {
                T bytes = array[i];
                if (bytes == null) {
                    return EMPTY_BUFFER;
                }
                if (!wrapper.isEmpty(bytes)) {
                    return new CompositeByteBuf(ALLOC, false, maxNumComponents, wrapper, array, i);
                }
            }
        }

        return EMPTY_BUFFER;
    }

    /**
     * Creates a new big-endian composite buffer which wraps the specified
     * arrays without copying them.  A modification on the specified arrays'
     * content will be visible to the returned buffer.
     */
    public static ByteBuf wrappedBuffer(int maxNumComponents, byte[]... arrays) {
        return wrappedBuffer(maxNumComponents, CompositeByteBuf.BYTE_ARRAY_WRAPPER, arrays);
    }

    /**
     * Creates a new big-endian composite buffer which wraps the readable bytes of the
     * specified buffers without copying them.  A modification on the content
     * of the specified buffers will be visible to the returned buffer.
     * @param maxNumComponents Advisement as to how many independent buffers are allowed to exist before
     * consolidation occurs.
     * @param buffers The buffers to wrap. Reference count ownership of all variables is transferred to this method.
     * @return The readable portion of the {@code buffers}. The caller is responsible for releasing this buffer.
     */
    public static ByteBuf wrappedBuffer(int maxNumComponents, ByteBuf... buffers) {
        switch (buffers.length) {
        case 0:
            break;
        case 1:
            ByteBuf buffer = buffers[0];
            if (buffer.isReadable()) {
                return wrappedBuffer(buffer.order(BIG_ENDIAN));
            } else {
                buffer.release();
            }
            break;
        default:
            for (int i = 0; i < buffers.length; i++) {
                ByteBuf buf = buffers[i];
                if (buf.isReadable()) {
                    return new CompositeByteBuf(ALLOC, false, maxNumComponents, buffers, i);
                }
                buf.release();
            }
            break;
        }
        return EMPTY_BUFFER;
    }

    /**
     * Creates a new big-endian composite buffer which wraps the slices of the specified
     * NIO buffers without copying them.  A modification on the content of the
     * specified buffers will be visible to the returned buffer.
     */
    public static ByteBuf wrappedBuffer(int maxNumComponents, ByteBuffer... buffers) {
        return wrappedBuffer(maxNumComponents, CompositeByteBuf.BYTE_BUFFER_WRAPPER, buffers);
    }

    /** 创建无组件的组合缓冲区。 */
    public static CompositeByteBuf compositeBuffer() {
        return compositeBuffer(AbstractByteBufAllocator.DEFAULT_MAX_COMPONENTS);
    }

    /**
     * Returns a new big-endian composite buffer with no components.
     */
    public static CompositeByteBuf compositeBuffer(int maxNumComponents) {
        return new CompositeByteBuf(ALLOC, false, maxNumComponents);
    }

    /** 拷贝 byte 数组；writerIndex = array.length。 */
    public static ByteBuf copiedBuffer(byte[] array) {
        if (array.length == 0) {
            return EMPTY_BUFFER;
        }
        return wrappedBuffer(array.clone());
    }

    /** 拷贝数组子区域。 */
    public static ByteBuf copiedBuffer(byte[] array, int offset, int length) {
        if (length == 0) {
            return EMPTY_BUFFER;
        }
        byte[] copy = PlatformDependent.allocateUninitializedArray(length);
        System.arraycopy(array, offset, copy, 0, length);
        return wrappedBuffer(copy);
    }

    /** 拷贝 NIO ByteBuffer 剩余内容。 */
    public static ByteBuf copiedBuffer(ByteBuffer buffer) {
        int length = buffer.remaining();
        if (length == 0) {
            return EMPTY_BUFFER;
        }
        byte[] copy = PlatformDependent.allocateUninitializedArray(length);
        // duplicate 避免 get 时改变原 buffer position（见 netty#3896）
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.get(copy);
        return wrappedBuffer(copy).order(duplicate.order());
    }

    /** 拷贝 ByteBuf 可读字节。 */
    public static ByteBuf copiedBuffer(ByteBuf buffer) {
        int readable = buffer.readableBytes();
        if (readable > 0) {
            ByteBuf copy = buffer(readable);
            copy.writeBytes(buffer, buffer.readerIndex(), readable);
            return copy;
        } else {
            return EMPTY_BUFFER;
        }
    }

    /** 合并拷贝多个 byte 数组。 */
    public static ByteBuf copiedBuffer(byte[]... arrays) {
        switch (arrays.length) {
        case 0:
            return EMPTY_BUFFER;
        case 1:
            if (arrays[0].length == 0) {
                return EMPTY_BUFFER;
            } else {
                return copiedBuffer(arrays[0]);
            }
        }

        // 合并多个数组为单一数组
        int length = 0;
        for (byte[] a: arrays) {
            if (Integer.MAX_VALUE - length < a.length) {
                throw new IllegalArgumentException(
                        "The total length of the specified arrays is too big.");
            }
            length += a.length;
        }

        if (length == 0) {
            return EMPTY_BUFFER;
        }

        byte[] mergedArray = PlatformDependent.allocateUninitializedArray(length);
        for (int i = 0, j = 0; i < arrays.length; i ++) {
            byte[] a = arrays[i];
            System.arraycopy(a, 0, mergedArray, j, a.length);
            j += a.length;
        }

        return wrappedBuffer(mergedArray);
    }

    /**
     * 合并拷贝多个 ByteBuf 可读字节；字节序须一致。
     *
     * @throws IllegalArgumentException 字节序不一致
     */
    public static ByteBuf copiedBuffer(ByteBuf... buffers) {
        switch (buffers.length) {
        case 0:
            return EMPTY_BUFFER;
        case 1:
            return copiedBuffer(buffers[0]);
        }

        // 合并多个缓冲区内容
        ByteOrder order = null;
        int length = 0;
        for (ByteBuf b: buffers) {
            int bLen = b.readableBytes();
            if (bLen <= 0) {
                continue;
            }
            if (Integer.MAX_VALUE - length < bLen) {
                throw new IllegalArgumentException(
                        "The total length of the specified buffers is too big.");
            }
            length += bLen;
            if (order != null) {
                if (!order.equals(b.order())) {
                    throw new IllegalArgumentException("inconsistent byte order");
                }
            } else {
                order = b.order();
            }
        }

        if (length == 0) {
            return EMPTY_BUFFER;
        }

        byte[] mergedArray = PlatformDependent.allocateUninitializedArray(length);
        for (int i = 0, j = 0; i < buffers.length; i ++) {
            ByteBuf b = buffers[i];
            int bLen = b.readableBytes();
            b.getBytes(b.readerIndex(), mergedArray, j, bLen);
            j += bLen;
        }

        return wrappedBuffer(mergedArray).order(order);
    }

    /** 合并拷贝多个 ByteBuffer 剩余内容；字节序须一致。 */
    public static ByteBuf copiedBuffer(ByteBuffer... buffers) {
        switch (buffers.length) {
        case 0:
            return EMPTY_BUFFER;
        case 1:
            return copiedBuffer(buffers[0]);
        }

        // Merge the specified buffers into one buffer.
        ByteOrder order = null;
        int length = 0;
        for (ByteBuffer b: buffers) {
            int bLen = b.remaining();
            if (bLen <= 0) {
                continue;
            }
            if (Integer.MAX_VALUE - length < bLen) {
                throw new IllegalArgumentException(
                        "The total length of the specified buffers is too big.");
            }
            length += bLen;
            if (order != null) {
                if (!order.equals(b.order())) {
                    throw new IllegalArgumentException("inconsistent byte order");
                }
            } else {
                order = b.order();
            }
        }

        if (length == 0) {
            return EMPTY_BUFFER;
        }

        byte[] mergedArray = PlatformDependent.allocateUninitializedArray(length);
        for (int i = 0, j = 0; i < buffers.length; i ++) {
            // Duplicate the buffer so we not adjust the position during our get operation.
            // See https://github.com/netty/netty/issues/3896
            ByteBuffer b = buffers[i].duplicate();
            int bLen = b.remaining();
            b.get(mergedArray, j, bLen);
            j += bLen;
        }

        return wrappedBuffer(mergedArray).order(order);
    }

    /** 将字符串按 charset 编码拷贝到新堆缓冲。 */
    public static ByteBuf copiedBuffer(CharSequence string, Charset charset) {
        ObjectUtil.checkNotNull(string, "string");
        if (CharsetUtil.UTF_8.equals(charset)) {
            return copiedBufferUtf8(string);
        }
        if (CharsetUtil.US_ASCII.equals(charset)) {
            return copiedBufferAscii(string);
        }
        if (string instanceof CharBuffer) {
            return copiedBuffer((CharBuffer) string, charset);
        }

        return copiedBuffer(CharBuffer.wrap(string), charset);
    }

    private static ByteBuf copiedBufferUtf8(CharSequence string) {
        boolean release = true;
        // 与其他 copiedBuffer 实现行为一致
        int byteLength = ByteBufUtil.utf8Bytes(string);
        ByteBuf buffer = ALLOC.heapBuffer(byteLength);
        try {
            ByteBufUtil.reserveAndWriteUtf8(buffer, string, byteLength);
            release = false;
            return buffer;
        } finally {
            if (release) {
                buffer.release();
            }
        }
    }

    private static ByteBuf copiedBufferAscii(CharSequence string) {
        boolean release = true;
        // Mimic the same behavior as other copiedBuffer implementations.
        ByteBuf buffer = ALLOC.heapBuffer(string.length());
        try {
            ByteBufUtil.writeAscii(buffer, string);
            release = false;
            return buffer;
        } finally {
            if (release) {
                buffer.release();
            }
        }
    }

    /** 编码字符串子串 [offset, offset+length)。 */
    public static ByteBuf copiedBuffer(
            CharSequence string, int offset, int length, Charset charset) {
        ObjectUtil.checkNotNull(string, "string");
        if (length == 0) {
            return EMPTY_BUFFER;
        }

        if (string instanceof CharBuffer) {
            CharBuffer buf = (CharBuffer) string;
            if (buf.hasArray()) {
                return copiedBuffer(
                        buf.array(),
                        buf.arrayOffset() + buf.position() + offset,
                        length, charset);
            }

            buf = buf.slice();
            buf.limit(length);
            buf.position(offset);
            return copiedBuffer(buf, charset);
        }

        return copiedBuffer(CharBuffer.wrap(string, offset, offset + length), charset);
    }

    /** 将 char 数组按 charset 编码拷贝。 */
    public static ByteBuf copiedBuffer(char[] array, Charset charset) {
        ObjectUtil.checkNotNull(array, "array");
        return copiedBuffer(array, 0, array.length, charset);
    }

    /** 编码 char 数组子区域。 */
    public static ByteBuf copiedBuffer(char[] array, int offset, int length, Charset charset) {
        ObjectUtil.checkNotNull(array, "array");
        if (length == 0) {
            return EMPTY_BUFFER;
        }
        return copiedBuffer(CharBuffer.wrap(array, offset, length), charset);
    }

    private static ByteBuf copiedBuffer(CharBuffer buffer, Charset charset) {
        return ByteBufUtil.encodeString0(ALLOC, true, buffer, charset, 0);
    }

    /**
     * 只读包装（禁止写操作）；读写索引与源相同。
     *
     * @deprecated 请使用 {@link ByteBuf#asReadOnly()}。
     */
    @Deprecated
    public static ByteBuf unmodifiableBuffer(ByteBuf buffer) {
        ByteOrder endianness = buffer.order();
        if (endianness == BIG_ENDIAN) {
            return newReadyOnlyBuffer(buffer);
        }

        return newReadyOnlyBuffer(buffer.order(BIG_ENDIAN)).order(LITTLE_ENDIAN);
    }

    private static ReadOnlyByteBuf newReadyOnlyBuffer(ByteBuf buffer) {
        // 仅当 unwrap 为空或为 AbstractByteBuf 时用 ReadOnlyAbstractByteBuf，否则后续 CCE
        return buffer instanceof AbstractByteBuf && (
                buffer.unwrap() == null || buffer.unwrap() instanceof AbstractByteBuf) ?
                new ReadOnlyAbstractByteBuf((AbstractByteBuf) buffer) :
                new ReadOnlyByteBuf(buffer);
    }

    /** 4 字节大端缓冲，写入单个 int。 */
    public static ByteBuf copyInt(int value) {
        ByteBuf buf = buffer(4);
        buf.writeInt(value);
        return buf;
    }

    /** 大端缓冲，依次写入多个 int。 */
    public static ByteBuf copyInt(int... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 4);
        for (int v: values) {
            buffer.writeInt(v);
        }
        return buffer;
    }

    /** 2 字节大端缓冲，写入 short。 */
    public static ByteBuf copyShort(int value) {
        ByteBuf buf = buffer(2);
        buf.writeShort(value);
        return buf;
    }

    /** 大端缓冲，依次写入多个 short（short[] 重载）。 */
    public static ByteBuf copyShort(short... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 2);
        for (int v: values) {
            buffer.writeShort(v);
        }
        return buffer;
    }

    /** 大端缓冲，依次写入多个 short（int[] 重载）。 */
    public static ByteBuf copyShort(int... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 2);
        for (int v: values) {
            buffer.writeShort(v);
        }
        return buffer;
    }

    /** 3 字节大端 medium。 */
    public static ByteBuf copyMedium(int value) {
        ByteBuf buf = buffer(3);
        buf.writeMedium(value);
        return buf;
    }

    /** 依次写入多个 24-bit medium。 */
    public static ByteBuf copyMedium(int... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 3);
        for (int v: values) {
            buffer.writeMedium(v);
        }
        return buffer;
    }

    /** 8 字节大端 long。 */
    public static ByteBuf copyLong(long value) {
        ByteBuf buf = buffer(8);
        buf.writeLong(value);
        return buf;
    }

    /** 依次写入多个 long。 */
    public static ByteBuf copyLong(long... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 8);
        for (long v: values) {
            buffer.writeLong(v);
        }
        return buffer;
    }

    /** 单字节 boolean。 */
    public static ByteBuf copyBoolean(boolean value) {
        ByteBuf buf = buffer(1);
        buf.writeBoolean(value);
        return buf;
    }

    /** 依次写入多个 boolean。 */
    public static ByteBuf copyBoolean(boolean... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length);
        for (boolean v: values) {
            buffer.writeBoolean(v);
        }
        return buffer;
    }

    /** 4 字节 float。 */
    public static ByteBuf copyFloat(float value) {
        ByteBuf buf = buffer(4);
        buf.writeFloat(value);
        return buf;
    }

    /** 依次写入多个 float。 */
    public static ByteBuf copyFloat(float... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 4);
        for (float v: values) {
            buffer.writeFloat(v);
        }
        return buffer;
    }

    /** 8 字节 double。 */
    public static ByteBuf copyDouble(double value) {
        ByteBuf buf = buffer(8);
        buf.writeDouble(value);
        return buf;
    }

    /** 依次写入多个 double。 */
    public static ByteBuf copyDouble(double... values) {
        if (values == null || values.length == 0) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = buffer(values.length * 8);
        for (double v: values) {
            buffer.writeDouble(v);
        }
        return buffer;
    }

    /** 不可释放视图：忽略 retain/release。 */
    public static ByteBuf unreleasableBuffer(ByteBuf buf) {
        return new UnreleasableByteBuf(buf);
    }

    /**
     * 不可变组合包装（可能拷贝数组）；不会对子缓冲做 slice 减 GC。
     *
     * @deprecated 请使用 {@link #wrappedUnmodifiableBuffer(ByteBuf...)}。
     */
    @Deprecated
    public static ByteBuf unmodifiableBuffer(ByteBuf... buffers) {
        return wrappedUnmodifiableBuffer(true, buffers);
    }

    /**
     * 不可变组合包装；可能直接引用传入数组，之后勿修改源缓冲。
     */
    public static ByteBuf wrappedUnmodifiableBuffer(ByteBuf... buffers) {
        return wrappedUnmodifiableBuffer(false, buffers);
    }

    private static ByteBuf wrappedUnmodifiableBuffer(boolean copy, ByteBuf... buffers) {
        switch (buffers.length) {
        case 0:
            return EMPTY_BUFFER;
        case 1:
            return buffers[0].asReadOnly();
        default:
            if (copy) {
                buffers = Arrays.copyOf(buffers, buffers.length, ByteBuf[].class);
            }
            return new FixedCompositeByteBuf(ALLOC, buffers);
        }
    }

    private Unpooled() {
        // 工具类，禁止实例化
    }
}
