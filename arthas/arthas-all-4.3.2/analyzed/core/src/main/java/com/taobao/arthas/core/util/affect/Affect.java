package com.taobao.arthas.core.util.affect;

import static java.lang.System.currentTimeMillis;

/**
 * 命令执行影响的基础反馈：记录开始时间并计算耗时。
 * <p>watch、trace、tt 等命令的影响统计均继承此类。</p>
 * Created by vlinux on 15/5/21.
 * @author diecui1202 on 2017/10/26
 */
public class Affect {

    /** 对象创建时刻，作为耗时起点。 */
    private final long start = currentTimeMillis();

    /**
     * 自创建以来经过的毫秒数。
     *
     * @return 影响耗时（ms）
     */
    public long cost() {
        return currentTimeMillis() - start;
    }

    @Override
    public String toString() {
        return String.format("Affect cost in %s ms.", cost());
    }
}
