/*
 * Copyright 2024 The Netty Project
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
package io.netty.channel.uring;

import io.netty.channel.IoEvent;
import io.netty.channel.IoRegistration;

import java.nio.ByteBuffer;

/**
 * {@link IoEvent} that will be produced as an result of a {@link IoUringIoOps}.
 * <p>io_uring 完成队列 CQE 的 Java 表示：opcode、res、flags 与 userData。</p>
 */
public final class IoUringIoEvent implements IoEvent {

    /** SQE 操作码（与 {@link Native} 中 IORING_OP_* 对应） */
    private byte opcode;
    /** 完成结果：正数为字节数，负数为 -errno */
    private int res;
    private int flags;
    /** 提交 SQE 时附带的 userData（快速路径为 packed id） */
    private long userData;
    private ByteBuffer extraCqeData;

    /**
     * Create a new instance
     * <p>创建新实例。</p>
     *
     * @param res       the result.
     * @param flags     the flags
     * @param opcode    the op code
     * @param data      the user data that was given as part of the submission.
     * @deprecated use {@link #IoUringIoEvent(int,int,byte,long)} instead.
     */
    @Deprecated
    public IoUringIoEvent(int res, int flags, byte opcode, short data) {
        this(res, flags, opcode, (long) data);
    }

    /**
     * Create a new instance
     * <p>创建新实例。</p>
     *
     * @param res       the result.
     * @param flags     the flags
     * @param opcode    the op code
     * @param userData  the user data that was given as part of the submission.
     */
    public IoUringIoEvent(int res, int flags, byte opcode, long userData) {
        this.res = res;
        this.flags = flags;
        this.opcode = opcode;
        this.userData = userData;
    }

    // 内部复用同一实例，减少 CQE 处理时的对象分配
    void update(int res, int flags, byte opcode, long userData, ByteBuffer extraCqeData) {
        this.res = res;
        this.flags = flags;
        this.opcode = opcode;
        this.userData = userData;
        this.extraCqeData = extraCqeData;
    }

    /**
     * Returns the result.
     *
     * @return  the result
     * <p>返回 CQE 的 res 字段（成功时为字节数或 0，失败为负 errno）。</p>
     */
    public int res() {
        return res;
    }

    /**
     * Returns the flags.
     * <p>返回 CQE 的 flags 字段。</p>
     *
     * @return flags
     */
    public int flags() {
        return flags;
    }

    /**
     * Returns the op code of the {@link IoUringIoOps}.
     * <p>返回对应 SQE 的操作码。</p>
     *
     * @return  opcode
     */
    public byte opcode() {
        return opcode;
    }

    /**
     * Returns the data that is passed as part of {@link IoUringIoOps}.
     * <p>返回 SQE 附带的 userData。</p>
     *
     * @return  data.
     * @deprecated use {@link #userData()} instead.
     */
    @Deprecated
    public short data() {
        return (short) userData;
    }

    /**
     * Returns the user data that was passed as part of the submission.
     * <p>返回提交时附带的 userData。</p>
     *
     * @return  user data.
     */
    public long userData() {
        return userData;
    }

    /**
     * Returns the extra data for the CQE. This will only be non-null of the ring was setup with
     * {@code IORING_SETUP_CQE32}. As this {@link ByteBuffer} maps into the shared completion queue its important
     * to not hold any reference to it outside of the {@link IoUringIoHandle#handle(IoRegistration, IoEvent)} method.
     *
     * @return extra data for the CQE or {@code null}.
     * <p>CQE32 模式下附加数据；映射共享 CQ，勿在 handle 外持有引用。</p>
     */
    public ByteBuffer extraCqeData() {
        return extraCqeData;
    }

    @Override
    public String toString() {
        return "IOUringIoEvent{" +
                "opcode=" + opcode +
                ", res=" + res +
                ", flags=" + flags +
                ", userData=" + userData +
                '}';
    }
}
