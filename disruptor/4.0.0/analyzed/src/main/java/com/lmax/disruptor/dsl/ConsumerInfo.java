package com.lmax.disruptor.dsl;

import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;

import java.util.concurrent.ThreadFactory;

/**
 * DSL 内部使用的消费者元数据接口。
 */
interface ConsumerInfo
{
    /**
     * @return 本消费者关联的序号数组
     */
    Sequence[] getSequences();

    /**
     * @return 本消费者使用的序号屏障
     */
    SequenceBarrier getBarrier();

    /**
     * @return 是否为依赖链末端消费者
     */
    boolean isEndOfChain();

    /**
     * 使用给定线程工厂启动消费者。
     *
     * @param threadFactory 线程工厂
     */
    void start(ThreadFactory threadFactory);

    /**
     * 停止消费者。
     */
    void halt();

    /**
     * 标记本消费者已被其他屏障引用，不再视为链末端。
     */
    void markAsUsedInBarrier();

    /**
     * @return 消费者是否正在运行
     */
    boolean isRunning();
}
