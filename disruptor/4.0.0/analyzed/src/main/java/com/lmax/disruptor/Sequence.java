package com.lmax.disruptor;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/** 左侧填充字节，隔离 {@code value} 字段以避免伪共享。 */
class LhsPadding
{
    protected byte
        p10, p11, p12, p13, p14, p15, p16, p17,
        p20, p21, p22, p23, p24, p25, p26, p27,
        p30, p31, p32, p33, p34, p35, p36, p37,
        p40, p41, p42, p43, p44, p45, p46, p47,
        p50, p51, p52, p53, p54, p55, p56, p57,
        p60, p61, p62, p63, p64, p65, p66, p67,
        p70, p71, p72, p73, p74, p75, p76, p77;
}

/** 承载 volatile 序号值的核心字段层。 */
class Value extends LhsPadding
{
    protected long value;
}

/** 右侧填充字节，与左侧共同构成缓存行隔离。 */
class RhsPadding extends Value
{
    protected byte
        p90, p91, p92, p93, p94, p95, p96, p97,
        p100, p101, p102, p103, p104, p105, p106, p107,
        p110, p111, p112, p113, p114, p115, p116, p117,
        p120, p121, p122, p123, p124, p125, p126, p127,
        p130, p131, p132, p133, p134, p135, p136, p137,
        p140, p141, p142, p143, p144, p145, p146, p147,
        p150, p151, p152, p153, p154, p155, p156, p157;
}

/**
 * 并发序号类，用于跟踪环形缓冲区与事件处理器的消费/发布进度。
 * 支持 CAS、有序写等多种并发操作，并通过填充降低伪共享。
 */
public class Sequence extends RhsPadding
{
    static final long INITIAL_VALUE = -1L;
    private static final VarHandle VALUE_FIELD;

    static
    {
        try
        {
            VALUE_FIELD = MethodHandles.lookup().in(Sequence.class)
                    .findVarHandle(Sequence.class, "value", long.class);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建初始值为 -1 的序号。
     */
    public Sequence()
    {
        this(INITIAL_VALUE);
    }

    /**
     * 以指定初始值创建序号。
     *
     * @param initialValue 初始序号值
     */
    public Sequence(final long initialValue)
    {
        VarHandle.releaseFence();
        this.value = initialValue;
    }

    /**
     * 以 volatile 语义读取当前序号值。
     *
     * @return 当前序号
     */
    public long get()
    {
        long value = this.value;
        VarHandle.acquireFence();
        return value;
    }

    /**
     * 有序写入序号，在本写操作与先前任意 store 之间建立 Store/Store 屏障。
     *
     * @param value 新序号值
     */
    public void set(final long value)
    {
        VarHandle.releaseFence();
        this.value = value;
    }

    /**
     * 以 volatile 语义写入序号，在本写与先前写之间建立 Store/Store 屏障，
     * 与后续 volatile 读之间建立 Store/Load 屏障。
     *
     * @param value 新序号值
     */
    public void setVolatile(final long value)
    {
        VarHandle.releaseFence();
        this.value = value;
        VarHandle.fullFence();
    }

    /**
     * 对序号执行 CAS 操作。
     *
     * @param expectedValue 期望的当前值
     * @param newValue      要写入的新值
     * @return 操作成功返回 {@code true}，否则 {@code false}
     */
    public boolean compareAndSet(final long expectedValue, final long newValue)
    {
        return VALUE_FIELD.compareAndSet(this, expectedValue, newValue);
    }

    /**
     * 原子地将序号加一。
     *
     * @return 自增后的值
     */
    public long incrementAndGet()
    {
        return addAndGet(1);
    }

    /**
     * 原子地加上指定增量。
     *
     * @param increment 增量
     * @return 自增后的值
     */
    public long addAndGet(final long increment)
    {
        return (long) VALUE_FIELD.getAndAdd(this, increment) + increment;
    }

    /**
     * 原子地执行 getAndAdd。
     *
     * @param increment 增量
     * @return 自增前的值
     */
    public long getAndAdd(final long increment)
    {
        return (long) VALUE_FIELD.getAndAdd(this, increment);
    }

    @Override
    public String toString()
    {
        return Long.toString(get());
    }
}
