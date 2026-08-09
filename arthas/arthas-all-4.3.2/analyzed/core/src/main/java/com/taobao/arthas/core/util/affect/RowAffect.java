package com.taobao.arthas.core.util.affect;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 按「行」统计的影响反馈（如批量输出时的记录条数）。
 * Created by vlinux on 15/5/21.
 */
public final class RowAffect extends Affect {

    /** 受影响行数计数器。 */
    private final AtomicInteger rCnt = new AtomicInteger();

    public RowAffect() {
    }

    /** 以初始行数构造。 */
    public RowAffect(int rCnt) {
        this.rCnt(rCnt);
    }

    /**
     * 影响行数统计
     *
     * @param mc 行影响计数
     * @return 当前影响行个数
     */
    public int rCnt(int mc) {
        return rCnt.addAndGet(mc);
    }

    /**
     * 获取影响行个数
     *
     * @return 影响行个数
     */
    public int rCnt() {
        return rCnt.get();
    }

    @Override
    public String toString() {
        return String.format("Affect(row-cnt:%d) cost in %s ms.",
                rCnt(),
                cost());
    }
}
