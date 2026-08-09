package com.taobao.arthas.core.distribution;

/**
 * 命令结果分发器的全局配置项。
 * <p>
 * 集中管理 {@link ResultConsumerImpl}、{@link SharingResultDistributorImpl} 等
 * 组件共用的队列容量等参数，避免硬编码分散在各实现类中。
 *
 * @author gongdewei 2020/5/18
 */
public class DistributorOptions {

    /**
     * {@link ResultConsumer} 结果队列的最大长度，用于控制内存中缓存的命令结果数据量。
     * <p>
     * 队列满时采用丢弃最旧数据的策略，保证业务线程不被阻塞。
     */
    public static int resultQueueSize = 50;

}
