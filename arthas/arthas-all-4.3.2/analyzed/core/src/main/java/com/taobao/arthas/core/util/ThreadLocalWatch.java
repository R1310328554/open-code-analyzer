package com.taobao.arthas.core.util;

/**
 * 基于 ThreadLocal 的轻量级纳秒计时器。
 * <p>
 * 使用固定大小 long 环形栈记录时间戳，避免在业务线程 ThreadLocal 中持有
 * ArthasClassLoader 加载的对象，便于 Agent detach 后回收。
 *
 * @author vlinux 16/6/1.
 * @author hengyunabc 2016-10-31
 */
public class ThreadLocalWatch {

    /**
     * 用 long[] 做一个固定大小的 ring stack，避免把 ArthasClassLoader 加载的对象塞到业务线程的 ThreadLocalMap 里，
     * 从而在 stop/detach 后导致 ArthasClassLoader 无法被 GC 回收。
     *
     * <pre>
     * 约定：
     * - stack[0] 存储当前 pos（0..cap）
     * - stack[1..cap] 存储数据
     * </pre>
     */
    private static final int DEFAULT_STACK_SIZE = 1024 * 4;
    private final ThreadLocal<long[]> timestampRef = ThreadLocal.withInitial(() -> new long[DEFAULT_STACK_SIZE + 1]);

    /** 记录当前纳秒时间并入栈，返回该时间戳。 */
    public long start() {
        final long timestamp = System.nanoTime();
        push(timestampRef.get(), timestamp);
        return timestamp;
    }

    /** 弹出栈顶时间戳并返回与当前的纳秒差值。 */
    public long cost() {
        return (System.nanoTime() - pop(timestampRef.get()));
    }

    /** 同 {@link #cost()}，结果转换为毫秒。 */
    public double costInMillis() {
        return (System.nanoTime() - pop(timestampRef.get())) / 1000000.0;
    }

    /** 窥视栈顶时间戳计算耗时，不弹出栈。 */
    public double costInMillisWithoutPop() {
        long timestamp = peek(timestampRef.get());
        if (timestamp == 0) {
            return 0.0;
        }
        return (System.nanoTime() - timestamp) / 1000000.0;
    }

    /**
     * 
     * <pre>
     * 一个特殊的stack，为了追求效率，避免扩容。
     * 因为这个stack的push/pop 并不一定成对调用，比如可能push执行了，但是后面的流程被中断了，pop没有被执行。
     * 如果不固定大小，一直增长的话，极端情况下可能应用有内存问题。
     * 如果到达容量，pos会重置，循环存储数据。所以使用这个Stack如果在极端情况下统计的数据会不准确，只用于monitor/watch等命令的计时。
     * 
     * </pre>
     * 
     * @author hengyunabc 2019-11-20
     *
     */
    static void push(long[] stack, long value) {
        int cap = stack.length - 1;
        int pos = (int) stack[0];
        if (pos < cap) {
            pos++;
        } else {
            // 栈满时回绕到索引 1，牺牲精确性换取 bounded 内存            // if stack is full, reset pos
            pos = 1;
        }
        stack[pos] = value;
        stack[0] = pos;
    }

    static long pop(long[] stack) {
        int cap = stack.length - 1;
        int pos = (int) stack[0];
        if (pos > 0) {
            long value = stack[pos];
            stack[0] = pos - 1;
            return value;
        }

        pos = cap;
        long value = stack[pos];
        stack[0] = pos - 1;
        return value;
    }

    static long peek(long[] stack) {
        int pos = (int) stack[0];
        if (pos > 0) {
            return stack[pos];
        }
        return 0;
    }
}
