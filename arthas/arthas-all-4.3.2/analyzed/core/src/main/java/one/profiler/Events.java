/*
 * Copyright The async-profiler authors
 * SPDX-License-Identifier: Apache-2.0
 */

package one.profiler;

/**
 * {@link AsyncProfiler#start(String, long)} 可用的预定义采样事件名常量。
 */
public class Events {
    /** CPU 周期采样（perf 事件）。 */
    public static final String CPU    = "cpu";
    /** 堆分配采样。 */
    public static final String ALLOC  = "alloc";
    /** 锁竞争/阻塞采样。 */
    public static final String LOCK   = "lock";
    /** 墙钟时间采样。 */
    public static final String WALL   = "wall";
    /** CPU 定时器（itimer 的 CPU 变体）。 */
    public static final String CTIMER = "ctimer";
    /** 传统 interval 定时器采样。 */
    public static final String ITIMER = "itimer";
}
