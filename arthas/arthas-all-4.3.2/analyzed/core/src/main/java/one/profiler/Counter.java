/*
 * Copyright The async-profiler authors
 * SPDX-License-Identifier: Apache-2.0
 */

package one.profiler;

/**
 * collapsed stacktraces 格式导出时使用的计数指标。
 * <p>
 * {@link #SAMPLES} 按采样次数计数；{@link #TOTAL} 按累计值（如 CPU 时间）计数。
 */
public enum Counter {
    /** 样本次数。 */
    SAMPLES,
    /** 累计总量（如 CPU 纳秒）。 */
    TOTAL
}
