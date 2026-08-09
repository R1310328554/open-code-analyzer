package com.taobao.arthas.core.distribution;

import com.taobao.arthas.core.command.model.ResultModel;

/**
 * 命令结果分发器：将命令执行过程中产生的分阶段结果推送给同一会话内的消费者。
 * <p>
 * 典型实现包括终端渲染（{@link impl.TermResultDistributorImpl}）、
 * 共享广播（{@link SharingResultDistributor}）与复合分发（{@link CompositeResultDistributor}）。
 *
 * @author gongdewei 2020-03-26
 */
public interface ResultDistributor {

    /**
     * 追加一条分阶段命令结果。
     * <p>
     * 实现类应保证此方法不长时间阻塞命令执行线程。
     *
     * @param result 命令产生的一条分阶段结果
     */
    void appendResult(ResultModel result);

    /**
     * 关闭分发器并释放相关资源（线程、队列、消费者等）。
     */
    void close();
}
