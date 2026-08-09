package com.taobao.arthas.core.distribution;

import com.taobao.arthas.core.command.model.ResultModel;

import java.util.List;

/**
 * 命令结果消费者接口：远程客户端（Web Console、Tunnel 等）通过轮询
 * 从 {@link SharingResultDistributor} 拉取命令输出。
 * <p>
 * 每个消费者维护独立的结果队列，支持长轮询批量拉取与健康状态检测。
 *
 * @author gongdewei 2020-03-26
 */
public interface ResultConsumer {

    /**
     * 将分阶段命令结果追加到消费者队列。
     *
     * @param result 命令产生的一条分阶段结果
     * @return {@code true} 表示成功入队；{@code false} 表示因队列满而丢弃了旧数据
     */
    boolean appendResult(ResultModel result);

    /**
     * 长轮询批量取出队列头部的结果包。
     * <p>
     * 内部会按批次大小、元素估算数量与等待超时综合决定是否立即返回。
     *
     * @return 一批待发送的结果；无数据或超时返回空列表
     */
    List<ResultModel> pollResults();

    /** 返回最近一次访问（poll）的时间戳，用于健康检测 */
    long getLastAccessTime();

    /** 关闭消费者，后续 poll 将返回空结果 */
    void close();

    /** 消费者是否已关闭 */
    boolean isClosed();

    /** 是否正在执行 poll 长轮询 */
    boolean isPolling();

    /** 获取消费者唯一标识 */
    String getConsumerId();

    /** 设置消费者唯一标识（由分发器在注册时分配） */
    void setConsumerId(String consumerId);

    /**
     * 判断消费者是否处于健康状态。
     * <p>
     * 不健康时（长时间未 poll 且队列已满）分发器可能中断当前命令。
     *
     * @return {@code true} 表示消费者仍在正常消费
     */
    boolean isHealthy();
}
