package com.taobao.arthas.core.util.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <pre>
 * 统计平均速率，比如统计 5 秒内的平均速率。
 * 5 秒的数据是：234, 345,124,366,235，
 * 则速率是 (234+345+124+366+235)/5 = 260
 *
 * </pre>
 *
 * @author hengyunabc 2015年12月18日 下午3:40:19
 *
 */
public class RateCounter {
    private static final int BITS_PER_LONG = 63;
    /** 默认滑动窗口大小（样本槽位数） */
    public static final int DEFAULT_SIZE = 5;

    /** 已接收样本总数（含超出窗口的历史计数） */
    private final AtomicLong count = new AtomicLong();
    /** 固定容量环形缓冲区，存最近若干次采样值 */
    private final AtomicLongArray values;

    /** 使用默认窗口大小 {@link #DEFAULT_SIZE} 构造 */
    public RateCounter() {
        this(DEFAULT_SIZE);
    }

    /**
     * @param size 滑动窗口容量
     */
    public RateCounter(int size) {
        this.values = new AtomicLongArray(size);
        for (int i = 0; i < values.length(); i++) {
            values.set(i, 0);
        }
        count.set(0);
    }

    /**
     * @return 参与速率计算的样本数量（不超过窗口容量）
     */
    public int size() {
        final long c = count.get();
        if (c > values.length()) {
            return values.length();
        }
        return (int) c;
    }

    /**
     * 写入一次采样值；窗口未满时顺序填充，满后按 reservoir 策略随机替换旧槽位。
     *
     * @param value 本次采样数值
     */
    public void update(long value) {
        final long c = count.incrementAndGet();
        if (c <= values.length()) {
            values.set((int) c - 1, value);
        } else {
            final long r = nextLong(c);
            if (r < values.length()) {
                values.set((int) r, value);
            }
        }
    }

    /**
     * @return 当前窗口内样本的算术平均值；无样本时返回 0.0
     */
    public double rate() {
        long c = count.get();
        int countLength = 0;
        long sum = 0;
        if (c > values.length()) {
            countLength = values.length();
        } else {
            countLength = (int) c;
        }

        for (int i = 0; i < countLength; ++i) {
            sum += values.get(i);
        }

        if (countLength == 0) {
            return 0.0;
        }
        return sum / (double) countLength;
    }

    /**
     * 在 {@code [0, n)} 上均匀取伪随机 long，算法摘自
     * {@link java.util.Random#nextInt()}。
     *
     * @param n 上界（不含）
     * @return {@code [0..n)} 范围内的随机值
     */
    private static long nextLong(long n) {
        long bits, val;
        do {
            bits = ThreadLocalRandom.current().nextLong() & (~(1L << BITS_PER_LONG));
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }

}
