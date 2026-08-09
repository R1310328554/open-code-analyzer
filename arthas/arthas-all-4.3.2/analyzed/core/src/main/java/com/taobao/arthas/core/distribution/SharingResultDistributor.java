package com.taobao.arthas.core.distribution;

import java.util.List;

/**
 * 共享式结果分发器：同一会话内允许多个 {@link ResultConsumer} 同时订阅命令输出。
 * <p>
 * 新加入的消费者会收到历史队列中的结果副本，实现多客户端同步观看同一命令执行过程。
 *
 * @see SharingResultDistributorImpl
 */
public interface SharingResultDistributor extends ResultDistributor {

    /**
     * 向共享会话注册新的结果消费者。
     *
     * @param consumer 待注册的消费者实例
     */
    void addConsumer(ResultConsumer consumer);

    /**
     * 从共享会话移除消费者并关闭之。
     *
     * @param consumer 待移除的消费者实例
     */
    void removeConsumer(ResultConsumer consumer);

    /**
     * 获取当前会话内所有活跃消费者。
     *
     * @return 消费者列表（线程安全）
     */
    List<ResultConsumer> getConsumers();

    /**
     * 按 ID 查找消费者。
     *
     * @param consumerId 注册时分配的消费者标识
     * @return 匹配的消费者，未找到返回 {@code null}
     */
    ResultConsumer getConsumer(String consumerId);
}
