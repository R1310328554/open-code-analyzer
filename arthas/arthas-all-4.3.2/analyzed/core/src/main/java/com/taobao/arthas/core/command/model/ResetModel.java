package com.taobao.arthas.core.command.model;

import com.taobao.arthas.core.util.affect.EnhancerAffect;

/**
 * reset 命令的结构化结果：撤销增强（watch/trace/monitor 等）的影响范围统计。
 * <p>
 * {@link #affect} 描述本次 reset 触达或恢复的类/方法数量；可传入底层
 * {@link EnhancerAffect} 由工厂转换为 VO，便于 JSON 序列化输出。
 *
 * @author gongdewei 2020/6/22
 */
public class ResetModel extends ResultModel {

    /** 增强器影响统计（成功/失败类数、方法数等） */
    private EnhancerAffectVO affect;

    public ResetModel(EnhancerAffectVO affect) {
        this.affect = affect;
    }

    public ResetModel(EnhancerAffect affect) {
        this.affect = EnhancerModelFactory.createEnhancerAffectVO(affect);
    }

    @Override
    public String getType() {
        return "reset";
    }

    /** 用新的 EnhancerAffect 更新统计并支持链式调用 */
    public ResetModel affect(EnhancerAffect affect) {
        this.affect = EnhancerModelFactory.createEnhancerAffectVO(affect);
        return this;
    }

    public EnhancerAffectVO getAffect() {
        return affect;
    }
}
