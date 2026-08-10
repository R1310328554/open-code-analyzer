/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 带固定间隔或指数退避的重试执行工具。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class Retry {


    /**
     * 最多执行 {@code attemptsCount} 次 {@code runnable}，每次失败后间隔 {@code intervalMillis} 毫秒。
     * 若抛出 {@link RuntimeException} 或 {@link AssertionError} 则重试。
     *
     * @param runnable 待执行任务
     * @param attemptsCount Total number of attempts to execute the {@code runnable}
     * @param intervalMillis 重试间隔（毫秒）
     * @return Index of the first successful invocation, starting from 0.
     */
    public static int execute(Runnable runnable, int attemptsCount, long intervalMillis) {
        int iteration = 0;
        while (true) {
            try {
                runnable.run();
                return iteration;
            } catch (RuntimeException | AssertionError e) {
                attemptsCount--;
                iteration++;
                if (attemptsCount > 0) {
                    try {
                        if (intervalMillis > 0) {
                            Thread.sleep(intervalMillis);
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        ie.addSuppressed(e);
                        throw new RuntimeException(ie);
                    }
                } else {
                    throw e;
                }
            }
        }
    }


    /**
     * 最多执行 {@code attemptsCount} 次，失败间隔采用指数退避 + 抖动。
     * 详见 https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/
     *
     * <p>延迟基数为 {@code intervalBaseMillis}。</p>
     *
     * @param runnable 待执行任务（可获知当前迭代次数）
     * @param attemptsCount Total number of attempts to execute the {@code runnable}
     * @param intervalBaseMillis base for the exponential backoff + jitter
     *
     * @return Index of the first successful invocation, starting from 0.
     */
    public static int executeWithBackoff(AdvancedRunnable runnable, int attemptsCount, int intervalBaseMillis) {
        return executeWithBackoff(runnable, null, attemptsCount, intervalBaseMillis);
    }

    /** 带异常回调的指数退避重试。 */
    public static int executeWithBackoff(AdvancedRunnable runnable, ThrowableCallback throwableCallback, int attemptsCount, int intervalBaseMillis) {
        int iteration = 0;
        while (true) {
            try {
                runnable.run(iteration);
                return iteration;
            } catch (RuntimeException | AssertionError e) {

                if (throwableCallback != null) {
                    throwableCallback.handleThrowable(iteration, e);
                }

                iteration++;
                if (iteration >= attemptsCount) {
                    throw e;
                }
                if (intervalBaseMillis <= 0) {
                    continue;
                }
                try {
                    long delay = computeBackoffInterval(intervalBaseMillis, iteration);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    e.addSuppressed(ie);
                    throw e;
                }
            }
        }
    }

    /** 在 {@code timeout} 时限内以指数退避重试。 */
    public static int executeWithBackoff(AdvancedRunnable runnable, Duration timeout, int intervalBaseMillis) {
        return executeWithBackoff(runnable, null, timeout, intervalBaseMillis);
    }

    /** 带异常回调、超时与指数退避的重试。 */
    public static int executeWithBackoff(AdvancedRunnable runnable, ThrowableCallback throwableCallback, Duration timeout, int intervalBaseMillis) {
        long maximumTime = Time.currentTimeMillis() + timeout.toMillis();

        int iteration = 0;
        while (true) {
            try {
                runnable.run(iteration);
                return iteration;
            } catch (RuntimeException | AssertionError e) {

                if (throwableCallback != null) {
                    throwableCallback.handleThrowable(iteration, e);
                }

                iteration++;
                long remainingTime = maximumTime - Time.currentTimeMillis();
                if (remainingTime <= 0) {
                    throw e;
                }

                if (intervalBaseMillis <= 0) {
                    continue;
                }
                try {
                    long delay = Math.min(remainingTime, computeBackoffInterval(intervalBaseMillis, iteration));
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    e.addSuppressed(ie);
                    throw e;
                }
            }
        }
    }

    /** 计算第 {@code iteration} 次退避的随机延迟上界（毫秒）。 */
    public static int computeBackoffInterval(int base, int iteration) {
        return ThreadLocalRandom.current().nextInt(computeIterationBase(base, iteration));
    }

    /** 第 {@code iteration} 次退避的基数：{@code base * 2^iteration}。 */
    private static int computeIterationBase(int base, int iteration) {
        return base * (1 << iteration);
    }

    /**
     * 最多执行 {@code attemptsCount} 次 {@code supplier}，固定间隔 {@code intervalMillis}。
     *
     * @param supplier 带迭代次数的供应器
     * @param attemptsCount Total number of attempts to execute the {@code runnable}
     * @param intervalMillis 重试间隔（毫秒）
     * @return Value generated by the {@code supplier}.
     */
    public static <T> T call(Supplier<T> supplier, int attemptsCount, long intervalMillis) {
        int iteration = 0;
        while (true) {
            try {
                return supplier.get(iteration);
            } catch (Exception | AssertionError e) {
                attemptsCount--;
                iteration++;
                if (attemptsCount > 0) {
                    try {
                        if (intervalMillis > 0) {
                            Thread.sleep(intervalMillis);
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        ie.addSuppressed(e);
                        throw new RuntimeException(ie);
                    }
                } else {
                    throw e;
                }
            }
        }
    }


    /**
     * 可获知当前迭代次数的 Runnable 变体。
     */
    public interface AdvancedRunnable {

        void run(int iteration);

    }

    /**
     * 重试失败时的异常回调（补充迭代次数与异常信息）。
     */
    public interface ThrowableCallback {

        void handleThrowable(int iteration, Throwable t);

    }

    /**
     * 带迭代次数参数的 Supplier 变体（JDK 8 前兼容用途）。
     */
    public interface Supplier<T> {

        /**
         * 获取结果。
         *
         * @return a result
         */
        T get(int iteration);
    }


}
