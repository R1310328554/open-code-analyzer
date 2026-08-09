package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.advisor.Advice;

import java.time.LocalDateTime;

/**
 * {@code tt} 命令记录的单次方法调用快照：不可变地保存 {@link Advice}、发生时间与耗时。
 * 存入 {@link TimeTunnelCommand} 的全局 map，供 -i/-w/-p/-d 等子命令回放或查看。
 */
class TimeFragment {

    /** @param advice 含 target/params/返回值或异常等完整调用上下文 */
    public TimeFragment(Advice advice, LocalDateTime gmtCreate, double cost) {
        this.advice = advice;
        this.gmtCreate = gmtCreate;
        this.cost = cost;
    }

    private final Advice advice;
    private final LocalDateTime gmtCreate;
    private final double cost;

    public Advice getAdvice() {
        return advice;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public double getCost() {
        return cost;
    }
}
