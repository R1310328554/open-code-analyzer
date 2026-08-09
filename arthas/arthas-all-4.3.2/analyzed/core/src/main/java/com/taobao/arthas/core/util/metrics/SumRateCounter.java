package com.taobao.arthas.core.util.metrics;

/**
 * <pre>
 * 统计传入累计值之间的增量速率（相邻采样差值的滑动平均）。
 * 比如传入的数据是所有请求的总数，5 秒数据为：
 * 267, 457, 635, 894, 1398
 * 则统计的平均速率是：( (457-267) + (635-457) + (894-635) + (1398-894) ) / 4 = 282
 * </pre>
 * 
 * @author hengyunabc 2015年12月18日 下午3:40:26
 *
 */
public class SumRateCounter {

    /** 底层滑动窗口计数器，存储相邻采样差值 */
    RateCounter rateCounter;

    /** 上一次采样的累计值，用于计算本次增量 */
    Long previous = null;

    /** 使用默认窗口大小（5）构造 */
    public SumRateCounter() {
        rateCounter = new RateCounter();
    }

    /**
     * @param size 滑动窗口容量
     */
    public SumRateCounter(int size) {
        rateCounter = new RateCounter(size);
    }

    /** @return 当前窗口内有效样本数量 */
    public int size() {
        return rateCounter.size();
    }

    /**
     * 写入新的累计采样值；首次调用仅记录基准，从第二次起写入与上次的差值。
     *
     * @param value 当前累计计数（如总请求数）
     */
    public void update(long value) {
        if (previous == null) {
            previous = value;
            return;
        }
        rateCounter.update(value - previous);
        previous = value;
    }

    /** @return 窗口内增量样本的平均速率 */
    public double rate() {
        return rateCounter.rate();
    }

}
