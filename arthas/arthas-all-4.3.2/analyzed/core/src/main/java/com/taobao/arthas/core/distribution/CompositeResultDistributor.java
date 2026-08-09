package com.taobao.arthas.core.distribution;

/**
 * 复合结果分发器接口：将命令结果同时广播给多个 {@link ResultDistributor}。
 * <p>
 * 典型场景为终端输出与远程隧道/Web 端并行推送；支持运行时动态增删子分发器。
 *
 * @author gongdewei 2020/4/30
 */
public interface CompositeResultDistributor extends ResultDistributor {

    /** 注册子分发器 */
    void addDistributor(ResultDistributor distributor);

    /** 移除子分发器 */
    void removeDistributor(ResultDistributor distributor);
}
