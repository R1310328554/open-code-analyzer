package com.alibaba.csp.sentinel.slots.block.flow.param;

/**
 * 令牌桶更新状态快照：记录上次补令时间与剩余可用 QPS。
 * 由 {@link ParamFlowChecker} 在令牌计算过程中创建并传递。
 */
class TokenUpdateStatus {

    /** 上次补充令牌的时间戳（毫秒）。 */
    private final long lastAddTokenTime;

    /** 当前剩余可用 QPS（请求计数）。 */
    private final long restQps;

    /**
     * @param lastAddTokenTime 上次补令时间戳
     * @param restQps 剩余 QPS
     */
    public TokenUpdateStatus(long lastAddTokenTime, long restQps) {
        this.lastAddTokenTime = lastAddTokenTime;
        this.restQps = restQps;
    }

    /** @return 上次补充令牌的时间戳。 */
    public long getLastAddTokenTime() {
        return lastAddTokenTime;
    }

    /** @return 剩余可用 QPS。 */
    public long getRestQps() {
        return restQps;
    }

    @Override
    public String toString() {
        return "TokenUpdateStatus{" +
                "hash=" + System.identityHashCode(this) +
                ", lastAddTokenTime=" + lastAddTokenTime +
                ", requestCount=" + restQps +
                '}';
    }
}
