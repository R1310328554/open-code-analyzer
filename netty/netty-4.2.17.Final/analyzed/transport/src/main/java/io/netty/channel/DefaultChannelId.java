/*
 * Copyright 2013 The Netty Project
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

import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.util.internal.MacAddressUtil.defaultMachineId;
import static io.netty.util.internal.MacAddressUtil.parseMAC;

/**
 * The default {@link ChannelId} implementation.
 * <p>默认 {@link ChannelId}：由机器 MAC、进程 ID、序列号、时间戳与随机数组成，
 * 保证进程内唯一且可排序；支持短文本与长文本两种字符串形式。</p>
 */
public final class DefaultChannelId implements ChannelId {

    private static final long serialVersionUID = 809640043754842613L;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelId.class);
    /** 本 JVM 实例的机器标识（通常为 MAC 地址字节） */
    private static final byte[] MACHINE_ID;
    private static final int PROCESS_ID_LEN = 4;
    /** 本 JVM 进程 ID */
    private static final int PROCESS_ID;
    private static final int SEQUENCE_LEN = 4;
    private static final int TIMESTAMP_LEN = 8;
    private static final int RANDOM_LEN = 4;

    /** 进程内递增序列，用于区分同一纳秒内的多个 ID */
    private static final AtomicInteger nextSequence = new AtomicInteger();

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * Returns a new {@link DefaultChannelId} instance.
     * <p>生成新的全局唯一通道 ID。</p>
     */
    public static DefaultChannelId newInstance() {
        return new DefaultChannelId(MACHINE_ID,
                                    PROCESS_ID,
                                    nextSequence.getAndIncrement(),
                                    Long.reverse(System.nanoTime()) ^ System.currentTimeMillis(),
                                    ThreadLocalRandom.current().nextInt());
    }

    static {
        int processId = -1;
        String customProcessId = SystemPropertyUtil.get("io.netty.processId");
        if (customProcessId != null) {
            try {
                processId = Integer.parseInt(customProcessId);
            } catch (NumberFormatException e) {
                // Malformed input.
            }

            if (processId < 0) {
                processId = -1;
                logger.warn("-Dio.netty.processId: {} (malformed)", customProcessId);
            } else if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty.processId: {} (user-set)", processId);
            }
        }

        if (processId < 0) {
            processId = defaultProcessId();
            if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty.processId: {} (auto-detected)", processId);
            }
        }

        PROCESS_ID = processId;

        byte[] machineId = null;
        String customMachineId = SystemPropertyUtil.get("io.netty.machineId");
        if (customMachineId != null) {
            try {
                machineId = parseMAC(customMachineId);
            } catch (Exception e) {
                logger.warn("-Dio.netty.machineId: {} (malformed)", customMachineId, e);
            }
            if (machineId != null) {
                logger.debug("-Dio.netty.machineId: {} (user-set)", customMachineId);
            }
        }

        if (machineId == null) {
            machineId = defaultMachineId();
            if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty.machineId: {} (auto-detected)", MacAddressUtil.formatAddress(machineId));
            }
        }

        MACHINE_ID = machineId;
    }

    /** 通过 Java 9+ {@link ProcessHandle#pid()} 获取进程 ID。 */
    static int processHandlePid(ClassLoader loader) {
        // pid is positive on unix, non{-1,0} on windows
        int nilValue = -1;
        if (PlatformDependent.javaVersion() >= 9) {
            Long pid;
            try {
                Class<?> processHandleImplType = Class.forName("java.lang.ProcessHandle", true, loader);
                Method processHandleCurrent = processHandleImplType.getMethod("current");
                Object processHandleInstance = processHandleCurrent.invoke(null);
                Method processHandlePid = processHandleImplType.getMethod("pid");
                pid = (Long) processHandlePid.invoke(processHandleInstance);
            } catch (Exception e) {
                logger.debug("Could not invoke ProcessHandle.current().pid();", e);
                return nilValue;
            }
            if (pid > Integer.MAX_VALUE || pid < Integer.MIN_VALUE) {
                throw new IllegalStateException("Current process ID exceeds int range: " + pid);
            }
            return pid.intValue();
        }
        return nilValue;
    }

    /** 通过 JMX RuntimeMXBean 或 Android {@code Process.myPid()} 获取进程 ID。 */
    static int jmxPid(ClassLoader loader) {
        String value;
        try {
            // Invoke java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
            Class<?> mgmtFactoryType = Class.forName("java.lang.management.ManagementFactory", true, loader);
            Class<?> runtimeMxBeanType = Class.forName("java.lang.management.RuntimeMXBean", true, loader);

            Method getRuntimeMXBean = mgmtFactoryType.getMethod("getRuntimeMXBean", EmptyArrays.EMPTY_CLASSES);
            Object bean = getRuntimeMXBean.invoke(null, EmptyArrays.EMPTY_OBJECTS);
            Method getName = runtimeMxBeanType.getMethod("getName", EmptyArrays.EMPTY_CLASSES);
            value = (String) getName.invoke(bean, EmptyArrays.EMPTY_OBJECTS);
        } catch (Throwable t) {
            logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(); Android?", t);
            try {
                // Invoke android.os.Process.myPid()
                Class<?> processType = Class.forName("android.os.Process", true, loader);
                Method myPid = processType.getMethod("myPid", EmptyArrays.EMPTY_CLASSES);
                value = myPid.invoke(null, EmptyArrays.EMPTY_OBJECTS).toString();
            } catch (Throwable t2) {
                logger.debug("Could not invoke Process.myPid(); not Android?", t2);
                value = "";
            }
        }

        int atIndex = value.indexOf('@');
        if (atIndex >= 0) {
            value = value.substring(0, atIndex);
        }

        int pid;
        try {
            pid = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // value did not contain an integer.
            pid = -1;
        }

        if (pid < 0) {
            pid = ThreadLocalRandom.current().nextInt();
            logger.warn("Failed to find the current process ID from '{}'; using a random value: {}",  value, pid);
        }

        return pid;
    }

    /** 自动检测当前进程 ID：优先 ProcessHandle，其次 JMX/Android。 */
    static int defaultProcessId() {
        ClassLoader loader = PlatformDependent.getClassLoader(DefaultChannelId.class);
        int processId = processHandlePid(loader);
        if (processId != -1) {
            return processId;
        }
        return jmxPid(loader);
    }

    /** 机器 MAC 地址字节 */
    private final byte[] machineId;
    /** 进程 ID */
    private final int processId;
    /** 进程内序列号 */
    private final int sequence;
    /** 创建时的时间戳（纳秒与毫秒混合） */
    private final long timestamp;
    /** 随机分量，降低碰撞概率 */
    private final int random;
    /** 预计算的 hashCode */
    private final int hashCode;

    /** 短文本形式缓存（仅 random 部分） */
    private transient String shortValue;
    /** 长文本形式缓存（完整十六进制） */
    private transient String longValue;

    /**
     * Visible for testing
     * <p>包可见构造函数，供单元测试构造确定性 ID。</p>
     */
    DefaultChannelId(final byte[] machineId, final int processId, final int sequence,
                     final long timestamp, final int random) {
        this.machineId = machineId;
        this.processId = processId;
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.random = random;
        hashCode = computeHashCode();
    }

    private int computeHashCode() {
        int h = Arrays.hashCode(machineId);
        h = 31 * h + processId;
        h = 31 * h + sequence;
        h = 31 * h + Long.hashCode(timestamp);
        h = 31 * h + random;
        return h;
    }

    /** 返回短文本 ID（8 位十六进制 random）。 */
    @Override
    public String asShortText() {
        String shortValue = this.shortValue;
        if (shortValue == null) {
            final StringBuilder buf = new StringBuilder(RANDOM_LEN * 2);
            appendHexInt(buf, random);
            this.shortValue = shortValue = buf.toString();
        }
        return shortValue;
    }

    /** 返回长文本 ID（machineId-processId-sequence-timestamp-random）。 */
    @Override
    public String asLongText() {
        String longValue = this.longValue;
        if (longValue == null) {
            this.longValue = longValue = newLongValue();
        }
        return longValue;
    }

    private String newLongValue() {
        final int machineIdLen = machineId.length;
        final StringBuilder buf = new StringBuilder(
                2 * (machineIdLen + PROCESS_ID_LEN + SEQUENCE_LEN + TIMESTAMP_LEN + RANDOM_LEN) + 4);
        appendHexBytes(buf, machineId);
        buf.append('-');
        appendHexInt(buf, processId);
        buf.append('-');
        appendHexInt(buf, sequence);
        buf.append('-');
        appendHexLong(buf, timestamp);
        buf.append('-');
        appendHexInt(buf, random);
        return buf.toString();
    }

    private static void appendHexBytes(StringBuilder buf, byte[] bytes) {
        for (byte b : bytes) {
            buf.append(HEX_CHARS[(b & 0xFF) >>> 4]);
            buf.append(HEX_CHARS[b & 0xF]);
        }
    }

    private static void appendHexInt(StringBuilder buf, int value) {
        for (int i = 28; i >= 0; i -= 4) {
            buf.append(HEX_CHARS[(value >>> i) & 0xF]);
        }
    }

    private static void appendHexLong(StringBuilder buf, long value) {
        for (int i = 60; i >= 0; i -= 4) {
            buf.append(HEX_CHARS[(int) ((value >>> i) & 0xF)]);
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /** 按 machineId、processId、sequence、timestamp、random 无符号顺序比较。 */
    @Override
    public int compareTo(final ChannelId o) {
        if (this == o) {
            // 同一实例，短路返回
            return 0;
        }
        if (o instanceof DefaultChannelId) {
            final DefaultChannelId other = (DefaultChannelId) o;
            int cmp = compareBytes(machineId, other.machineId);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compareUnsigned(processId, other.processId);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compareUnsigned(sequence, other.sequence);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Long.compareUnsigned(timestamp, other.timestamp);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compareUnsigned(random, other.random);
        }

        return asLongText().compareTo(o.asLongText());
    }

    private static int compareBytes(byte[] a, byte[] b) {
        int len1 = a.length;
        int len2 = b.length;
        int len = Math.min(len1, len2);
        for (int k = 0; k < len; k++) {
            int cmp = (a[k] & 0xFF) - (b[k] & 0xFF);
            if (cmp != 0) {
                return cmp;
            }
        }
        return len1 - len2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultChannelId)) {
            return false;
        }
        DefaultChannelId other = (DefaultChannelId) obj;
        return hashCode == other.hashCode
                && random == other.random
                && processId == other.processId
                && sequence == other.sequence
                && timestamp == other.timestamp
                && Arrays.equals(machineId, other.machineId);
    }

    /** 默认 toString 使用短文本形式。 */
    @Override
    public String toString() {
        return asShortText();
    }
}
