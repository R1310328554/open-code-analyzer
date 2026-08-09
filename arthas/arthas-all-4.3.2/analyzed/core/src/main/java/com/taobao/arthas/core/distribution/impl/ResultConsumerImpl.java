package com.taobao.arthas.core.distribution.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.distribution.DistributorOptions;
import com.taobao.arthas.core.distribution.ResultConsumer;
import com.taobao.arthas.core.distribution.ResultConsumerHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ResultConsumer} 的默认实现：基于有界队列 + 长轮询批量拉取。
 * <p>
 * 核心策略：
 * <ul>
 *   <li>append 非阻塞，队列满时丢弃最旧数据</li>
 *   <li>poll 按批次大小、元素估算数量与超时综合决定返回时机</li>
 *   <li>健康检测：正在 poll、队列未满或近期有访问均视为健康</li>
 * </ul>
 *
 * @author gongdewei 2020/3/27
 */
public class ResultConsumerImpl implements ResultConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ResultConsumerImpl.class);
    /** 待发送结果队列 */
    private BlockingQueue<ResultModel> resultQueue;
    /** 最近一次 poll 访问时间戳 */
    private volatile long lastAccessTime;
    /** 是否正在执行 poll 长轮询 */
    private volatile boolean polling;
    /** poll 操作互斥锁，同一时刻只允许一个 poll 线程 */
    private ReentrantLock lock = new ReentrantLock();
    /** 单次 poll 返回的最大结果条数 */
    private int resultBatchSizeLimit = 20;
    /** 结果队列容量，默认取自 {@link DistributorOptions#resultQueueSize} */
    private int resultQueueSize = DistributorOptions.resultQueueSize;
    /** 长轮询最大等待时间（毫秒） */
    private long pollTimeLimit = 2 * 1000;
    private String consumerId;
    /** 消费者是否已关闭 */
    private boolean closed;
    /** 当前批次已累积的元素估算数量 */
    private long sendingItemCount;

    public ResultConsumerImpl() {
        lastAccessTime = System.currentTimeMillis();
        resultQueue = new ArrayBlockingQueue<ResultModel>(resultQueueSize);
    }

    @Override
    public boolean appendResult(ResultModel result) {
        // 某些 Consumer 可能已断开不再读取，此处必须非阻塞，不能堵塞命令线程
        boolean discard = false;
        while (!resultQueue.offer(result)) {
            ResultModel discardResult = resultQueue.poll();
            discard = true;
        }
        return !discard;
    }

    @Override
    public List<ResultModel> pollResults() {
        try {
            lastAccessTime = System.currentTimeMillis();
            long accessTime = lastAccessTime;
            if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                polling = true;
                sendingItemCount = 0;
                long firstResultTime = 0;
                // sendingDelay：首条结果入批后的等待时长
                long sendingDelay = 0;
                // waitingTime：自本次 poll 开始的总等待时长
                long waitingTime = 0;
                List<ResultModel> sendingResults = new ArrayList<ResultModel>(resultBatchSizeLimit);

                while (!closed
                        &&sendingResults.size() < resultBatchSizeLimit
                        && sendingDelay < 100
                        && waitingTime < pollTimeLimit) {
                    ResultModel aResult = resultQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (aResult != null) {
                        sendingResults.add(aResult);
                        // 记录首次取到数据的时间
                        if (firstResultTime == 0) {
                            firstResultTime = System.currentTimeMillis();
                        }
                        // 估算元素数量，达到阈值则立即 flush
                        if (shouldFlush(sendingResults, aResult)) {
                            break;
                        }
                    } else {
                        if (firstResultTime > 0) {
                            // 已取到部分数据且队列暂时为空，计算发送延时
                            sendingDelay = System.currentTimeMillis() - firstResultTime;
                        }
                        // 计算长轮询总等待时间
                        waitingTime = System.currentTimeMillis() - accessTime;
                    }
                }

                if(logger.isDebugEnabled()) {
                    logger.debug("pollResults: {}, results: {}", sendingResults.size(), JSON.toJSONString(sendingResults));
                }
                return sendingResults;
            }
        } catch (InterruptedException e) {
            // 线程被中断，返回空列表
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lastAccessTime = System.currentTimeMillis();
                polling = false;
                lock.unlock();
            }
        }
        return Collections.emptyList();
    }

    /**
     * 根据元素估算数量判断当前批次是否应立即发送。
     *
     * @param sendingResults 已收集的待发送结果
     * @param last 最新追加的一条结果
     * @return {@code true} 表示应立刻返回当前批次
     */
    private boolean shouldFlush(List<ResultModel> sendingResults, ResultModel last) {
        // TODO 可引入更精细的估算模型，由每个 model 自行统计元素数量
        sendingItemCount += ResultConsumerHelper.getItemCount(last);
        return sendingItemCount >= 100;
    }

    @Override
    public boolean isHealthy() {
        // 正在 poll、队列未满或 1 秒内有访问均视为健康
        return isPolling()
                || resultQueue.size() < resultQueueSize
                || System.currentTimeMillis() - lastAccessTime < 1000;
    }

    @Override
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public void close(){
        this.closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean isPolling() {
        return polling;
    }

    public int getResultBatchSizeLimit() {
        return resultBatchSizeLimit;
    }

    public void setResultBatchSizeLimit(int resultBatchSizeLimit) {
        this.resultBatchSizeLimit = resultBatchSizeLimit;
    }

    @Override
    public String getConsumerId() {
        return consumerId;
    }

    @Override
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

}
