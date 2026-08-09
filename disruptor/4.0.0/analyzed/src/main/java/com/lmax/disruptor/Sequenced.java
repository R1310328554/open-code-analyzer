package com.lmax.disruptor;

/**
 * 与 {@link RingBuffer} 中条目排序相关的操作。
 * 详见子接口 {@link Sequencer} 与 {@link EventSequencer}。
 */
public interface Sequenced
{
    /**
     * 数据结构可容纳的条目容量。
     *
     * @return RingBuffer 的大小
     */
    int getBufferSize();

    /**
     * 缓冲区是否还有容量申领下一个序号。本方法为并发语义，返回值仅作容量参考。
     *
     * @param requiredCapacity 所需容量
     * @return 若有足够容量申领下一序号则为 {@code true}，否则为 {@code false}
     */
    boolean hasAvailableCapacity(int requiredCapacity);

    /**
     * 获取本序号器的剩余容量。
     *
     * @return 剩余槽位数
     */
    long remainingCapacity();

    /**
     * 申领下一个待发布事件的序号。
     *
     * @return 已申领的序号
     */
    long next();

    /**
     * 批量申领接下来 n 个待发布事件的序号，适用于批量生产。
     * 批量生产需注意用法与算术关系：
     * <pre>
     * int n = 10;
     * long hi = sequencer.next(n);
     * long lo = hi - (n - 1);
     * for (long sequence = lo; sequence &lt;= hi; sequence++) {
     *     // Do work.
     * }
     * sequencer.publish(lo, hi);
     * </pre>
     *
     * @param n 要申领的序号数量
     * @return 已申领的最高序号
     */
    long next(int n);

    /**
     * 尝试申领下一个待发布事件的序号。
     * 当至少存在 {@code requiredCapacity} 个可用槽位时返回槽位编号。
     *
     * @return 已申领的序号
     * @throws InsufficientCapacityException RingBuffer 无可用空间时抛出
     */
    long tryNext() throws InsufficientCapacityException;

    /**
     * 尝试批量申领接下来 n 个待发布事件的序号。
     * 用法参见 {@link Sequencer#next()}。
     *
     * @param n 要申领的序号数量
     * @return 已申领的最高序号
     * @throws InsufficientCapacityException RingBuffer 无可用空间时抛出
     */
    long tryNext(int n) throws InsufficientCapacityException;

    /**
     * 发布一个序号，在事件填充完成后调用。
     *
     * @param sequence 要发布的序号
     */
    void publish(long sequence);

    /**
     * 批量发布序号区间，在所有事件填充完成后调用。
     *
     * @param lo 要发布的起始序号
     * @param hi 要发布的结束序号
     */
    void publish(long lo, long hi);
}
