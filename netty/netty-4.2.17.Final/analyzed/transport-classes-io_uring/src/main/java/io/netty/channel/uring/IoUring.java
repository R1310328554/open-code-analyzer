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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.channel.unix.Buffer;
import io.netty.channel.unix.Limits;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteBuffer;

/**
 * io_uring 原生传输可用性与特性探测入口。
 * <p>静态初始化时创建探测 ring、检测内核版本与各 IORING 特性标志。</p>
 * <p>通过系统属性可配置 ring 大小、multi-shot、零拷贝阈值等。</p>
 */
public final class IoUring {

    private static final Throwable UNAVAILABILITY_CAUSE;
    private static final boolean IORING_CQE_F_SOCK_NONEMPTY_SUPPORTED;
    private static final boolean UNIX_DOMAIN_SOCKET_INQ_SUPPORTED;
    private static final boolean IORING_SPLICE_SUPPORTED;
    private static final boolean IORING_SEND_ZC_SUPPORTED;
    private static final boolean IORING_SENDMSG_ZC_SUPPORTED;
    private static final boolean IORING_ACCEPT_NO_WAIT_SUPPORTED;
    private static final boolean IORING_ACCEPT_MULTISHOT_SUPPORTED;
    private static final boolean IORING_RECV_MULTISHOT_SUPPORTED;
    private static final boolean IORING_RECVSEND_BUNDLE_SUPPORTED;
    private static final boolean IORING_POLL_ADD_MULTISHOT_SUPPORTED;
    private static final boolean IORING_REGISTER_IOWQ_MAX_WORKERS_SUPPORTED;
    private static final boolean IORING_SETUP_SUBMIT_ALL_SUPPORTED;
    private static final boolean IORING_SETUP_CQE_MIXED_SUPPORTED;
    private static final boolean IORING_SETUP_CQ_SIZE_SUPPORTED;
    private static final boolean IORING_SETUP_SINGLE_ISSUER_SUPPORTED;
    private static final boolean IORING_SETUP_DEFER_TASKRUN_SUPPORTED;
    private static final boolean IORING_SETUP_NO_SQARRAY_SUPPORTED;
    private static final boolean IORING_REGISTER_BUFFER_RING_SUPPORTED;
    private static final boolean IORING_REGISTER_BUFFER_RING_INC_SUPPORTED;
    private static final boolean IORING_ENTER_NO_IOWAIT_SUPPORTED;
    private static final boolean IORING_ACCEPT_MULTISHOT_ENABLED;
    private static final boolean IORING_RECV_MULTISHOT_ENABLED;
    private static final boolean IORING_RECVSEND_BUNDLE_ENABLED;
    private static final boolean IORING_POLL_ADD_MULTISHOT_ENABLED;
    private static final boolean IORING_ENTER_NO_IOWAIT_ENABLED;
    static final int NUM_ELEMENTS_IOVEC;
    static final int DEFAULT_RING_SIZE;
    static final int DEFAULT_CQ_SIZE;
    static final int DEFAULT_PENDING_OPS_INITIAL_CAPACITY;
    static final int DISABLE_SETUP_CQ_SIZE = -1;

    private static final InternalLogger logger;

    static {
        logger = InternalLoggerFactory.getInstance(IoUring.class);
        Throwable cause = null;
        boolean socketNonEmptySupported = false;
        boolean unixDomainSocketInqSupported = false;
        boolean spliceSupported = false;
        boolean sendZcSupported = false;
        boolean sendmsgZcSupported = false;
        boolean acceptSupportNoWait = false;
        boolean acceptMultishotSupported = false;
        boolean recvsendBundleSupported = false;
        boolean recvMultishotSupported = false;
        boolean pollAddMultishotSupported = false;
        boolean registerIowqWorkersSupported = false;
        boolean submitAllSupported = false;
        boolean cqeMixedSupported = false;
        boolean setUpCqSizeSupported = false;
        boolean singleIssuerSupported = false;
        boolean deferTaskrunSupported = false;
        boolean noSqarraySupported = false;
        boolean registerBufferRingSupported = false;
        boolean registerBufferRingIncSupported = false;
        boolean enterNoIoWaitSupported = false;
        int numElementsIoVec = 10;
        int pendingOpsInitialCapacity;

        String kernelVersion = "[unknown]";
        try {
            if (SystemPropertyUtil.getBoolean("io.netty.transport.noNative", false)) {
                cause = new UnsupportedOperationException(
                        "Native transport was explicit disabled with -Dio.netty.transport.noNative=true");
            } else {
                kernelVersion = Native.kernelVersion();
                Native.checkKernelVersion(kernelVersion);
                if (PlatformDependent.javaVersion() >= 9) {
                    RingBuffer ringBuffer = null;
                    try {
                        ringBuffer = Native.createRingBuffer(1, 0);
                        if ((ringBuffer.features() & Native.IORING_FEAT_SUBMIT_STABLE) == 0) {
                            // 仅在不支持的 <5.4 内核上出现
                            throw new UnsupportedOperationException("IORING_FEAT_SUBMIT_STABLE not supported!");
                        }
                        // IOV_MAX 默认约 1024，每 IOV 16 字节，默认预留约 160KB
                        numElementsIoVec = SystemPropertyUtil.getInt(
                                "io.netty.iouring.numElementsIoVec", 10 * Limits.IOV_MAX);
                        Native.IoUringProbe ioUringProbe = Native.ioUringProbe(ringBuffer.fd());
                        Native.checkAllIOSupported(ioUringProbe);
                        socketNonEmptySupported = Native.isCqeFSockNonEmptySupported(ioUringProbe);
                        unixDomainSocketInqSupported = Native.isUnixDomainSocketInqSupported();
                        spliceSupported = Native.isSpliceSupported(ioUringProbe);
                        recvsendBundleSupported = (ringBuffer.features() & Native.IORING_FEAT_RECVSEND_BUNDLE) != 0;
                        enterNoIoWaitSupported = (ringBuffer.features() & Native.IORING_FEAT_NO_IOWAIT) != 0;
                        sendZcSupported = Native.isSendZcSupported(ioUringProbe);
                        sendmsgZcSupported =  Native.isSendmsgZcSupported(ioUringProbe);
                        // IORING_FEAT_RECVSEND_BUNDLE 与 accept no-wait 同版本引入
                        acceptSupportNoWait = recvsendBundleSupported;

                        acceptMultishotSupported = Native.isAcceptMultishotSupported(ioUringProbe);
                        recvMultishotSupported = Native.isRecvMultishotSupported();
                        pollAddMultishotSupported = Native.isPollAddMultiShotSupported(ioUringProbe);
                        registerIowqWorkersSupported = Native.isRegisterIoWqWorkerSupported(ringBuffer.fd());
                        submitAllSupported = Native.ioUringSetupSupportsFlags(Native.IORING_SETUP_SUBMIT_ALL);
                        cqeMixedSupported = Native.ioUringSetupSupportsFlags(Native.IORING_SETUP_CQE_MIXED);
                        setUpCqSizeSupported = Native.ioUringSetupSupportsFlags(Native.IORING_SETUP_CQSIZE);
                        singleIssuerSupported = Native.ioUringSetupSupportsFlags(Native.IORING_SETUP_SINGLE_ISSUER);
                        // DEFER_TASKRUN 须同时设置 SINGLE_ISSUER（见 man io_uring_setup）
                        deferTaskrunSupported = Native.ioUringSetupSupportsFlags(
                                Native.IORING_SETUP_SINGLE_ISSUER | Native.IORING_SETUP_DEFER_TASKRUN);
                        noSqarraySupported = Native.ioUringSetupSupportsFlags(Native.IORING_SETUP_NO_SQARRAY);
                        registerBufferRingSupported = Native.isRegisterBufferRingSupported(ringBuffer.fd(), 0);
                        registerBufferRingIncSupported = Native.isRegisterBufferRingSupported(ringBuffer.fd(),
                                Native.IOU_PBUF_RING_INC);
                    } finally {
                        if (ringBuffer != null) {
                            try {
                                ringBuffer.close();
                            } catch (Exception ignore) {
                                // ignore
                            }
                        }
                    }
                } else {
                    cause = new UnsupportedOperationException("Java 9+ is required");
                }
            }
        } catch (Throwable t) {
            cause = t;
        }
        // 先赋值 static final，以便 printFeatures() 可读
        UNAVAILABILITY_CAUSE = cause;
        IORING_CQE_F_SOCK_NONEMPTY_SUPPORTED = socketNonEmptySupported;
        UNIX_DOMAIN_SOCKET_INQ_SUPPORTED = unixDomainSocketInqSupported;
        IORING_SPLICE_SUPPORTED = spliceSupported;
        IORING_SEND_ZC_SUPPORTED = sendZcSupported;
        IORING_SENDMSG_ZC_SUPPORTED = sendmsgZcSupported;
        IORING_ACCEPT_NO_WAIT_SUPPORTED = acceptSupportNoWait;
        IORING_ACCEPT_MULTISHOT_SUPPORTED = acceptMultishotSupported;
        IORING_RECV_MULTISHOT_SUPPORTED = recvMultishotSupported;
        IORING_RECVSEND_BUNDLE_SUPPORTED = recvsendBundleSupported;
        IORING_POLL_ADD_MULTISHOT_SUPPORTED = pollAddMultishotSupported;
        IORING_REGISTER_IOWQ_MAX_WORKERS_SUPPORTED = registerIowqWorkersSupported;
        IORING_SETUP_SUBMIT_ALL_SUPPORTED = submitAllSupported;
        IORING_SETUP_CQE_MIXED_SUPPORTED = cqeMixedSupported;
        IORING_SETUP_CQ_SIZE_SUPPORTED = setUpCqSizeSupported;
        IORING_SETUP_SINGLE_ISSUER_SUPPORTED = singleIssuerSupported;
        IORING_SETUP_DEFER_TASKRUN_SUPPORTED = deferTaskrunSupported;
        IORING_SETUP_NO_SQARRAY_SUPPORTED = noSqarraySupported;
        IORING_REGISTER_BUFFER_RING_SUPPORTED = registerBufferRingSupported;
        IORING_REGISTER_BUFFER_RING_INC_SUPPORTED = registerBufferRingIncSupported;
        IORING_ENTER_NO_IOWAIT_SUPPORTED = enterNoIoWaitSupported;

        IORING_ACCEPT_MULTISHOT_ENABLED = IORING_ACCEPT_MULTISHOT_SUPPORTED && SystemPropertyUtil.getBoolean(
                "io.netty.iouring.acceptMultiShotEnabled", true);
        IORING_RECV_MULTISHOT_ENABLED = IORING_RECV_MULTISHOT_SUPPORTED && SystemPropertyUtil.getBoolean(
                "io.netty.iouring.recvMultiShotEnabled", true);
        // 默认禁用 RECVSEND_BUNDLE（已知内核 bug，待修复）
        IORING_RECVSEND_BUNDLE_ENABLED = IORING_RECVSEND_BUNDLE_SUPPORTED && SystemPropertyUtil.getBoolean(
                "io.netty.iouring.recvsendBundleEnabled", false);
        IORING_POLL_ADD_MULTISHOT_ENABLED = IORING_POLL_ADD_MULTISHOT_SUPPORTED && SystemPropertyUtil.getBoolean(
               "io.netty.iouring.pollAddMultishotEnabled", true);
        IORING_ENTER_NO_IOWAIT_ENABLED = IORING_ENTER_NO_IOWAIT_SUPPORTED && SystemPropertyUtil.getBoolean(
                "io.netty.iouring.enterNoIoWaitEnabled", false);
        NUM_ELEMENTS_IOVEC = numElementsIoVec;

        DEFAULT_RING_SIZE =  Math.max(16, SystemPropertyUtil.getInt("io.netty.iouring.ringSize", 128));
        pendingOpsInitialCapacity = SystemPropertyUtil.getInt(
                "io.netty.iouring.pendingOpsInitialCapacity", DEFAULT_RING_SIZE);
        if (pendingOpsInitialCapacity <= 0) {
            int configuredCapacity = pendingOpsInitialCapacity;
            pendingOpsInitialCapacity = MathUtil.safeFindNextPositivePowerOfTwo(DEFAULT_RING_SIZE);
            logger.warn("Invalid value {} for -Dio.netty.iouring.pendingOpsInitialCapacity; using {} instead.",
                    configuredCapacity, pendingOpsInitialCapacity);
        } else if (Integer.bitCount(pendingOpsInitialCapacity) != 1) {
            int configuredCapacity = pendingOpsInitialCapacity;
            pendingOpsInitialCapacity = MathUtil.safeFindNextPositivePowerOfTwo(pendingOpsInitialCapacity);
            logger.warn("Rounding -Dio.netty.iouring.pendingOpsInitialCapacity from {} up to {}.",
                    configuredCapacity, pendingOpsInitialCapacity);
        }
        DEFAULT_PENDING_OPS_INITIAL_CAPACITY = pendingOpsInitialCapacity;
        if (IORING_SETUP_CQ_SIZE_SUPPORTED) {
            DEFAULT_CQ_SIZE = Math.max(DEFAULT_RING_SIZE,
                    SystemPropertyUtil.getInt("io.netty.iouring.cqSize", 4096));
        } else {
            DEFAULT_CQ_SIZE = DISABLE_SETUP_CQ_SIZE;
        }
        // 静态字段赋值完毕后输出 debug 日志
        if (cause != null) {
            if (logger.isTraceEnabled()) {
                logger.debug("IoUring support is not available using kernel {}", kernelVersion, cause);
            } else if (logger.isDebugEnabled()) {
                logger.debug("IoUring support is not available using kernel {}: {}", kernelVersion, cause.getMessage());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("IoUring support is available using kernel {}: {}", kernelVersion, supportedFeatures());
            }
        }
    }

    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    /**
     * Returns {@code true} if the io_uring native transport is both {@linkplain #isAvailable() available} and supports
     * <p>io_uring 传输是否可用且支持对应 TCP FastOpen 选项。</p>
     * {@linkplain ChannelOption#TCP_FASTOPEN_CONNECT client-side TCP FastOpen}.
     *
     * @return {@code true} if it's possible to use client-side TCP FastOpen via io_uring, otherwise {@code false}.
     */
    public static boolean isTcpFastOpenClientSideAvailable() {
        return isAvailable() && Native.IS_SUPPORTING_TCP_FASTOPEN_CLIENT;
    }

    /**
     * Returns {@code true} if the io_uring native transport is both {@linkplain #isAvailable() available} and supports
     * <p>io_uring 传输是否可用且支持对应 TCP FastOpen 选项。</p>
     * {@linkplain ChannelOption#TCP_FASTOPEN server-side TCP FastOpen}.
     *
     * @return {@code true} if it's possible to use server-side TCP FastOpen via io_uring, otherwise {@code false}.
     */
    public static boolean isTcpFastOpenServerSideAvailable() {
        return isAvailable() && Native.IS_SUPPORTING_TCP_FASTOPEN_SERVER;
    }

    static boolean isCqeFSockNonEmptySupported() {
        return IORING_CQE_F_SOCK_NONEMPTY_SUPPORTED;
    }

    static boolean isUnixDomainSocketInqSupported() {
        return UNIX_DOMAIN_SOCKET_INQ_SUPPORTED;
    }

    /**
     * Returns if SPLICE is supported or not.
     * <p>是否支持 SPLICE 操作。</p>
     *
     * @return {@code true} if supported, {@code false} otherwise.
     */
    public static boolean isSpliceSupported() {
        return IORING_SPLICE_SUPPORTED;
    }

    /**
     * Returns if {@code IORING_OP_SEND_ZC} is supported.
     * <p>是否支持 IORING_OP_SEND_ZC。</p>
     *
     * @return {@code true} if {@code IORING_OP_SEND_ZC} is supported, {@code false} otherwise.
     */
    static boolean isSendZcSupported() {
        return IORING_SEND_ZC_SUPPORTED;
    }

    /**
     * Returns if {@code IORING_OP_SENDMSG_ZC} is supported.
     * <p>是否支持 IORING_OP_SENDMSG_ZC。</p>
     *
     * @return {@code true} if {@code IORING_OP_SENDMSG_ZC} is supported, {@code false} otherwise.
     */
    static boolean isSendmsgZcSupported() {
        return IORING_SENDMSG_ZC_SUPPORTED;
    }

    static boolean isAcceptNoWaitSupported() {
        return IORING_ACCEPT_NO_WAIT_SUPPORTED;
    }

    static boolean isAcceptMultishotSupported() {
        return IORING_ACCEPT_MULTISHOT_SUPPORTED;
    }

    static boolean isRecvMultishotSupported() {
        return IORING_RECV_MULTISHOT_SUPPORTED;
    }

    static boolean isRecvsendBundleSupported() {
        return IORING_RECVSEND_BUNDLE_SUPPORTED;
    }

    static boolean isPollAddMultishotSupported() {
        return IORING_POLL_ADD_MULTISHOT_SUPPORTED;
    }

    static boolean isRegisterIowqMaxWorkersSupported() {
        return IORING_REGISTER_IOWQ_MAX_WORKERS_SUPPORTED;
    }

    static boolean isSetupCqeSizeSupported() {
        return IORING_SETUP_CQ_SIZE_SUPPORTED;
    }

    static boolean isSetupSubmitAllSupported() {
        return IORING_SETUP_SUBMIT_ALL_SUPPORTED;
    }

    static boolean isSetupCqeMixedSupported() {
        return IORING_SETUP_CQE_MIXED_SUPPORTED;
    }

    static boolean isSetupSingleIssuerSupported() {
        return IORING_SETUP_SINGLE_ISSUER_SUPPORTED;
    }

    static boolean isSetupDeferTaskrunSupported() {
        return IORING_SETUP_DEFER_TASKRUN_SUPPORTED;
    }

    static boolean isIoringSetupNoSqarraySupported() {
        return IORING_SETUP_NO_SQARRAY_SUPPORTED;
    }
    /**
     * Returns if it is supported to use a buffer ring.
     * <p>是否支持 buffer ring。</p>
     *
     * @return {@code true} if supported, {@code false} otherwise.
     */
    public static boolean isRegisterBufferRingSupported() {
        return IORING_REGISTER_BUFFER_RING_SUPPORTED;
    }

    /**
     * Returns if it is supported to use an incremental buffer ring.
     * <p>是否支持增量 buffer ring。</p>
     *
     * @return {@code true} if supported, {@code false} otherwise.
     */
    public static boolean isRegisterBufferRingIncSupported() {
        return IORING_REGISTER_BUFFER_RING_INC_SUPPORTED;
    }

    static boolean isIoringEnterNoIoWaitSupported() {
        return IORING_ENTER_NO_IOWAIT_SUPPORTED;
    }

    /**
     * Returns if {@code IORING_ENTER_NO_IOWAIT} is used or not. When enabled (and supported by the kernel),
     * <p>是否启用 IORING_ENTER_NO_IOWAIT（空闲 enter 不计入 iowait）。</p>
     * idle io_uring_enter(2) waits are not accounted as iowait, which makes server-side CPU metrics more
     * accurate but also suppresses the cpufreq governor's iowait boost.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public static boolean isIoringEnterNoIoWaitEnabled() {
        return IORING_ENTER_NO_IOWAIT_ENABLED;
    }

    /**
     * Returns if multi-shot ACCEPT is used or not.
     * <p>是否启用 multi-shot ACCEPT。</p>
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public static boolean isAcceptMultishotEnabled() {
        return IORING_ACCEPT_MULTISHOT_ENABLED;
    }

    /**
     * Returns if multi-shot RECV is used or not.
     * <p>是否启用 multi-shot RECV。</p>
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public static boolean isRecvMultishotEnabled() {
        return IORING_RECV_MULTISHOT_ENABLED;
    }

    /**
     * Returns if RECVSEND bundles are used or not.
     * <p>是否启用 RECVSEND bundle。</p>
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public static boolean isRecvsendBundleEnabled() {
        return IORING_RECVSEND_BUNDLE_ENABLED;
    }

    /**
     * Returns if multi-shot POLL_ADD is used or not.
     * <p>是否启用 multi-shot POLL_ADD。</p>
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public static boolean isPollAddMultishotEnabled() {
        return IORING_POLL_ADD_MULTISHOT_ENABLED;
    }

    public static void ensureAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw (Error) new UnsatisfiedLinkError(
                    "failed to load the required native library").initCause(UNAVAILABILITY_CAUSE);
        }
    }

    static long memoryAddress(ByteBuf buffer) {
        if (buffer.hasMemoryAddress()) {
            return buffer.memoryAddress();
        }
        // 使用 internalNioBuffer 减少对象创建；须加上 position 因 ByteBuffer 可能被多个 ByteBuf 共享
        ByteBuffer byteBuffer = buffer.internalNioBuffer(0, buffer.capacity());
        return Buffer.memoryAddress(byteBuffer) + byteBuffer.position();
    }

    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    private static String supportedFeatures() {
        if (!isAvailable()) {
            return "";
        }
        return "CQE_F_SOCK_NONEMPTY_SUPPORTED=" + IORING_CQE_F_SOCK_NONEMPTY_SUPPORTED
                + ", UNIX_DOMAIN_SOCKET_INQ_SUPPORTED=" + UNIX_DOMAIN_SOCKET_INQ_SUPPORTED
                + ", SPLICE_SUPPORTED=" + IORING_SPLICE_SUPPORTED
                + ", ACCEPT_NO_WAIT_SUPPORTED=" + IORING_ACCEPT_NO_WAIT_SUPPORTED
                + ", ACCEPT_MULTISHOT_SUPPORTED=" + IORING_ACCEPT_MULTISHOT_SUPPORTED
                + ", POLL_ADD_MULTISHOT_SUPPORTED=" + IORING_POLL_ADD_MULTISHOT_SUPPORTED
                + ", RECV_MULTISHOT_SUPPORTED=" + IORING_RECV_MULTISHOT_SUPPORTED
                + ", IORING_RECVSEND_BUNDLE_SUPPORTED=" + IORING_RECVSEND_BUNDLE_SUPPORTED
                + ", REGISTER_IOWQ_MAX_WORKERS_SUPPORTED=" + IORING_REGISTER_IOWQ_MAX_WORKERS_SUPPORTED
                + ", SETUP_SUBMIT_ALL_SUPPORTED=" + IORING_SETUP_SUBMIT_ALL_SUPPORTED
                + ", SETUP_CQE_MIXED_SUPPORTED=" + IORING_SETUP_CQE_MIXED_SUPPORTED
                + ", SETUP_CQ_SIZE_SUPPORTED=" + IORING_SETUP_CQ_SIZE_SUPPORTED
                + ", SETUP_SINGLE_ISSUER_SUPPORTED=" + IORING_SETUP_SINGLE_ISSUER_SUPPORTED
                + ", SETUP_DEFER_TASKRUN_SUPPORTED=" + IORING_SETUP_DEFER_TASKRUN_SUPPORTED
                + ", SETUP_NO_SQARRAY_SUPPORTED=" + IORING_SETUP_NO_SQARRAY_SUPPORTED
                + ", REGISTER_BUFFER_RING_SUPPORTED=" + IORING_REGISTER_BUFFER_RING_SUPPORTED
                + ", REGISTER_BUFFER_RING_INC_SUPPORTED=" + IORING_REGISTER_BUFFER_RING_INC_SUPPORTED
                + ", SEND_ZC_SUPPORTED=" + IORING_SEND_ZC_SUPPORTED
                + ", SENDMSG_ZC_SUPPORTED=" + IORING_SENDMSG_ZC_SUPPORTED
                + ", ENTER_NO_IOWAIT_SUPPORTED=" + IORING_ENTER_NO_IOWAIT_SUPPORTED;
    }

    /**
     * Returns a string representation of the io_uring support and feature set. This mirrors the
     * <p>返回 io_uring 支持与特性集的字符串表示。</p>
     * debug logging output that reports each individual feature's availability.
     */
    public static String featureString() {
        if (!isAvailable()) {
            Throwable t = unavailabilityCause();
            return "IoUring unavailable: " + (t == null ? "unknown cause" : t.toString());
        }
        return "IoUring features: " + supportedFeatures();
    }

    @Override
    public String toString() {
        return featureString();
    }

    private IoUring() {
    }
}
