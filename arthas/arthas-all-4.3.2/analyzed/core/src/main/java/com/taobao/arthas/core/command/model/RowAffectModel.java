package com.taobao.arthas.core.command.model;

import com.taobao.arthas.core.util.affect.RowAffect;

/**
 * 行级影响统计的结构化结果：报告命令实际匹配/处理的行数或条目数。
 * <p>
 * 常用于批量扫描类、方法时的进度反馈；{@link #getRowCount} 委托底层
 * {@link RowAffect#rCnt()}，type 固定为 {@code row_affect} 供客户端识别。
 *
 * @author gongdewei 2020/4/8
 */
public class RowAffectModel extends ResultModel {
    /** 行/条目影响计数器，封装成功与失败统计 */
    private RowAffect affect;

    public RowAffectModel() {
    }

    public RowAffectModel(RowAffect affect) {
        this.affect = affect;
    }

    @Override
    public String getType() {
        return "row_affect";
    }

    /** 返回受影响行数（或条目数），affect 为 null 时可能 NPE，由调用方保证 */
    public int getRowCount() {
        return affect.rCnt();
    }

    public RowAffect affect() {
        return affect;
    }
}
