/**
 * 单次 Disruptor 性能测试运行的结果上下文。
 */

package com.lmax.disruptor;

public class PerfTestContext
{
    private long disruptorOps;
    private long batchesProcessedCount;
    private long iterations;

    public PerfTestContext()
    {
    }

    /** @return Disruptor 每秒操作数 */
    public long getDisruptorOps()
    {
        return disruptorOps;
    }

    /** @param disruptorOps Disruptor 每秒操作数 */
    public void setDisruptorOps(final long disruptorOps)
    {
        this.disruptorOps = disruptorOps;
    }

    /** @return 已处理的批次数 */
    public long getBatchesProcessedCount()
    {
        return batchesProcessedCount;
    }

    /** @return 非单条处理事件所占比例（批处理占比） */
    public double getBatchPercent()
    {
        if (batchesProcessedCount == 0)
        {
            return 0;
        }
        return 1 - (double) batchesProcessedCount / iterations;
    }

    /** @return 平均批大小；无批次时返回 -1 */
    public double getAverageBatchSize()
    {
        if (batchesProcessedCount == 0)
        {
            return -1;
        }
        return (double) iterations / batchesProcessedCount;
    }

    /** @param batchesProcessedCount 批次数；@param iterations 总迭代次数 */
    public void setBatchData(final long batchesProcessedCount, final long iterations)
    {
        this.batchesProcessedCount = batchesProcessedCount;
        this.iterations = iterations;
    }
}
