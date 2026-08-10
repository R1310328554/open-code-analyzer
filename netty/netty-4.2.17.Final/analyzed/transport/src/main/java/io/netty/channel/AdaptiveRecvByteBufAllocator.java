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

import io.netty.util.internal.AdaptiveCalculator;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * The {@link RecvByteBufAllocator} that automatically increases and
 * decreases the predicted buffer size on feed back.
 * <p>根据每次读操作的实际填充情况自动增减预测接收缓冲区大小的 {@link RecvByteBufAllocator}。
 * 若上一次读满分配缓冲区则逐步增大预测值；若连续两次未能读满一定比例则逐步缩小；
 * 否则保持当前预测不变，从而在吞吐与内存之间自适应平衡。</p>
 */
public class AdaptiveRecvByteBufAllocator extends DefaultMaxMessagesRecvByteBufAllocator {

    /** 预测缓冲区大小的下限（字节）。 */
    public static final int DEFAULT_MINIMUM = 64;
    // Use an initial value that is bigger than the common MTU of 1500
    /** 初始预测大小，默认大于常见 MTU（1500）。 */
    public static final int DEFAULT_INITIAL = 2048;
    /** 预测缓冲区大小的上限（字节）。 */
    public static final int DEFAULT_MAXIMUM = 65536;

    /**
     * @deprecated There is state for {@link #maxMessagesPerRead()} which is typically based upon channel type.
     * <p>已废弃的全局默认实例；{@link #maxMessagesPerRead()} 状态与通道类型相关，不宜共享单例。</p>
     */
    @Deprecated
    public static final AdaptiveRecvByteBufAllocator DEFAULT = new AdaptiveRecvByteBufAllocator();

    /** 基于 {@link AdaptiveCalculator} 的自适应读缓冲区句柄。 */
    private final class HandleImpl extends MaxMessageHandle {
        private final AdaptiveCalculator calculator;

        HandleImpl(int minimum, int initial, int maximum) {
            calculator = new AdaptiveCalculator(minimum, initial, maximum);
        }

        @Override
        public void lastBytesRead(int bytes) {
            // If we read as much as we asked for we should check if we need to ramp up the size of our next guess.
            // This helps adjust more quickly when large amounts of data is pending and can avoid going back to
            // the selector to check for more data. Going back to the selector can add significant latency for large
            // data transfers.
            // 若本次读满请求字节数，记录样本以便下次增大预测，减少反复 selector 唤醒的延迟。
            if (bytes == attemptedBytesRead()) {
                calculator.record(bytes);
            }
            super.lastBytesRead(bytes);
        }

        /** 返回 {@link AdaptiveCalculator} 计算的下一预测缓冲区大小。 */
        @Override
        public int guess() {
            return calculator.nextSize();
        }

        /** 读循环结束时汇总本次总读取量并反馈给计算器。 */
        @Override
        public void readComplete() {
            calculator.record(totalBytesRead());
        }
    }

    private final int minimum;
    private final int initial;
    private final int maximum;

    /**
     * Creates a new predictor with the default parameters.  With the default
     * parameters, the expected buffer size starts from {@code 1024}, does not
     * go down below {@code 64}, and does not go up above {@code 65536}.
     * <p>使用默认参数（最小 64、初始 2048、最大 65536）创建自适应分配器。</p>
     */
    public AdaptiveRecvByteBufAllocator() {
        this(DEFAULT_MINIMUM, DEFAULT_INITIAL, DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new predictor with the specified parameters.
     * <p>按指定上下界与初始值创建自适应接收缓冲区分配器。</p>
     *
     * @param minimum  预测缓冲区大小的下界（含）
     * @param initial  尚无读反馈时的初始预测大小
     * @param maximum  预测缓冲区大小的上界（含）
     */
    public AdaptiveRecvByteBufAllocator(int minimum, int initial, int maximum) {
        checkPositive(minimum, "minimum");
        if (initial < minimum) {
            throw new IllegalArgumentException("initial: " + initial);
        }
        if (maximum < initial) {
            throw new IllegalArgumentException("maximum: " + maximum);
        }

        this.minimum = minimum;
        this.initial = initial;
        this.maximum = maximum;
    }

    /** 为每次读操作创建新的自适应 {@link Handle} 实例。 */
    @SuppressWarnings("deprecation")
    @Override
    public Handle newHandle() {
        return new HandleImpl(minimum, initial, maximum);
    }

    /** 配置是否在可能仍有数据时继续读，并返回 {@code this} 以支持链式调用。 */
    @Override
    public AdaptiveRecvByteBufAllocator respectMaybeMoreData(boolean respectMaybeMoreData) {
        super.respectMaybeMoreData(respectMaybeMoreData);
        return this;
    }
}
