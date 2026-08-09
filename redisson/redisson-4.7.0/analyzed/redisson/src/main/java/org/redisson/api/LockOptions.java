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

import java.util.concurrent.ThreadLocalRandom;

/**
 * 分布式锁对象的退避（back-off）配置。
 * <p>供 {@linkplain org.redisson.RedissonSpinLock} 等自旋锁在获取失败时
 * 按策略休眠后重试。
 *
 * @author Danila Varatyntsev
 */
public class LockOptions {

    /**
     * {@linkplain BackOffPolicy} 工厂接口。
     */
    public interface BackOff {
        BackOffPolicy create();
    }

    /**
     * 为 {@linkplain org.redisson.RedissonSpinLock} 退避算法生成休眠时长。
     */
    public interface BackOffPolicy {

        /**
         * 生成并返回下一次休眠时长
         *
         * @return 下一次休眠时长（毫秒）
         */
        long getNextSleepPeriod();
    }

    /**
     * 指数退避算法：休眠从 {@linkplain #initialDelay} 起，每次乘以
     * {@linkplain #multiplier}，且不超过 {@linkplain #maxDelay}。
     */
    public static class ExponentialBackOff implements BackOff {
        private long maxDelay = 128;
        private long initialDelay = 1;
        private int multiplier = 2;

        @Override
        public BackOffPolicy create() {
            return new ExponentialBackOffPolicy(initialDelay, maxDelay, multiplier);
        }

        /**
         * 设置最大退避延迟。
         * <p>
         * 默认值为 <code>128</code>。
         *
         * @param maxDelay 最大休眠时长，须为正数
         * @return ExponentialBackOff 实例
         */
        public ExponentialBackOff maxDelay(long maxDelay) {
            if (maxDelay <= 0) {
                throw new IllegalArgumentException("maxDelay should be positive");
            }
            this.maxDelay = maxDelay;
            return this;
        }

        public long getMaxDelay() {
            return maxDelay;
        }

        /**
         * 设置初始退避延迟。
         * <p>
         * 默认值为 <code>1</code>。
         *
         * @param initialDelay 初始休眠时长，须为正数
         * @return ExponentialBackOff 实例
         */
        public ExponentialBackOff initialDelay(long initialDelay) {
            if (initialDelay <= 0) {
                throw new IllegalArgumentException("initialDelay should be positive");
            }
            this.initialDelay = initialDelay;
            return this;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        /**
         * 设置退避延迟倍数。
         * <p>
         * 默认值为 <code>2</code>。
         *
         * @param multiplier 休眠时长倍数，须为正数
         * @return ExponentialBackOff 实例
         */
        public ExponentialBackOff multiplier(int multiplier) {
            if (multiplier <= 0) {
                throw new IllegalArgumentException("multiplier should be positive");
            }
            this.multiplier = multiplier;
            return this;
        }

        public int getMultiplier() {
            return multiplier;
        }
    }

    /** 指数退避策略实现：休眠时长指数增长并加入随机抖动。 */

    private static final class ExponentialBackOffPolicy implements BackOffPolicy {

        private final long maxDelay;
        private final int multiplier;
        private int fails;
        private long nextSleep;

        private ExponentialBackOffPolicy(long initialDelay, long maxDelay, int multiplier) {
            this.nextSleep = initialDelay;
            this.maxDelay = maxDelay;
            this.multiplier = multiplier;
        }

        @Override
        public long getNextSleepPeriod() {
            if (nextSleep == maxDelay) {
                return maxDelay;
            }
            long result = nextSleep;
            nextSleep = nextSleep * multiplier + ThreadLocalRandom.current().nextInt(++fails);
            nextSleep = Math.min(maxDelay, nextSleep);
            return result;
        }
    }

    /**
     * 固定退避算法：休眠时长由 {@linkplain #delay} 决定。
     * <p>为减轻多线程同时重试的惊群效应，可在各次休眠上叠加小幅随机值。
     */
    public static class ConstantBackOff implements BackOff {
        private long delay = 64;

        @Override
        public BackOffPolicy create() {
            return new ConstantBackOffPolicy(delay);
        }

        /**
         * 设置固定退避延迟。
         * <p>
         * 默认值为 <code>64</code>。
         *
         * @param delay 休眠时长，须为正数
         * @return ConstantBackOff 实例
         */
        public ConstantBackOff delay(long delay) {
            if (delay <= 0) {
                throw new IllegalArgumentException("delay should be positive");
            }
            this.delay = delay;
            return this;
        }

        public long getDelay() {
            return delay;
        }
    }

    /** 固定休眠时长的退避策略实现。 */

    private static final class ConstantBackOffPolicy implements BackOffPolicy {

        private final long delay;

        private ConstantBackOffPolicy(long delay) {
            this.delay = delay;
        }

        @Override
        public long getNextSleepPeriod() {
            return delay;
        }
    }

    /**
     * 创建默认配置的指数退避工厂。
     *
     * @return BackOff 实例
     */
    public static BackOff defaults() {
        return new ExponentialBackOff();
    }
}
