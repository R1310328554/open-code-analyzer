package com.taobao.arthas.core.distribution.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.command.model.InputStatusModel;
import com.taobao.arthas.core.command.model.MessageModel;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.distribution.DistributorOptions;
import com.taobao.arthas.core.distribution.ResultConsumer;
import com.taobao.arthas.core.distribution.SharingResultDistributor;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.Job;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link SharingResultDistributor} 的默认实现：后台守护线程从待分发队列取结果，
 * 广播给所有已注册的 {@link ResultConsumer}，并维护历史结果供新加入的消费者回放。
 * <p>
 * 当所有消费者均不健康或待分发队列溢出时，会中断当前前台 Job 以保护目标 JVM。
 */
public class SharingResultDistributorImpl implements SharingResultDistributor {
    private static final Logger logger = LoggerFactory.getLogger(SharingResultDistributorImpl.class);

    /** 当前会话内所有活跃消费者（CopyOnWrite 保证遍历安全） */
    private List<ResultConsumer> consumers = new CopyOnWriteArrayList<ResultConsumer>();
    /** 业务线程写入的待分发队列，容量较小以快速感知背压 */
    private BlockingQueue<ResultModel> pendingResultQueue = new ArrayBlockingQueue<ResultModel>(10);
    private final Session session;
    /** 后台分发线程 */
    private Thread distributorThread;
    private volatile boolean running;
    /** 消费者序号生成器，用于构造唯一 consumerId */
    private AtomicInteger consumerNumGenerator = new AtomicInteger(0);

    /** 内部共享消费者：缓存历史结果，供新消费者注册时回放 */
    private SharingResultConsumerImpl sharingResultConsumer = new SharingResultConsumerImpl();
    
    /** 是否已因消费者不健康而中断过 Job，避免重复中断导致死循环 */
    private volatile boolean interruptedForUnhealthy = false;

    public SharingResultDistributorImpl(Session session) {
        this.session = session;
        this.running = true;
        distributorThread = new Thread(new DistributorTask(), "ResultDistributor");
        distributorThread.setDaemon(true);  // 守护线程，不阻止 JVM 退出
        distributorThread.start();
    }

    @Override
    public void appendResult(ResultModel result) {
        // 必须避免阻塞命令执行业务线程
        try {
            if (!pendingResultQueue.offer(result, 100, TimeUnit.MILLISECONDS)) {
                ResultModel discardResult = pendingResultQueue.poll();
                // 正常情况不应触发；说明分发线程阻塞或异常终止
                // 队列满则中断当前命令
                interruptJob("result queue is full: "+ pendingResultQueue.size());
            }
        } catch (InterruptedException e) {
            // 忽略中断
        }
    }

    /** 中断当前前台 Job 并向队列写入提示消息 */
    private void interruptJob(String message) {
        Job job = session.getForegroundJob();
        if (job != null) {
            logger.warn(message+", current job was interrupted.", job.id());
            job.interrupt();
            pendingResultQueue.offer(new MessageModel(message+", current job was interrupted."));
        }
    }

    /** 后台分发循环：从 pending 队列取结果并广播给所有消费者 */
    private void distribute() {
        while (running) {
            try {
                ResultModel result = pendingResultQueue.poll(100, TimeUnit.MILLISECONDS);
                if (result != null) {
                    // 先写入历史缓存，再广播
                    sharingResultConsumer.appendResult(result);
                    
                    // 无消费者时跳过健康检查
                    if (consumers.isEmpty()) {
                        continue;
                    }
                    
                    // 统计健康消费者数量，并逐一分发
                    int healthCount = 0;
                    for (int i = 0; i < consumers.size(); i++) {
                        ResultConsumer consumer = consumers.get(i);
                        if(consumer.isHealthy()){
                            healthCount += 1;
                        }
                        consumer.appendResult(result);
                    }
                    // 全部不健康则中断命令；标志位防止重复中断
                    if (healthCount == 0 && !interruptedForUnhealthy) {
                        interruptedForUnhealthy = true;
                        interruptJob("all consumers are unhealthy");
                    }
                } else {
                    // 队列为空时，若有消费者恢复健康则重置中断标志
                    if (interruptedForUnhealthy) {
                        for (int i = 0; i < consumers.size(); i++) {
                            if (consumers.get(i).isHealthy()) {
                                interruptedForUnhealthy = false;
                                break;
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                // 线程被中断，正常退出循环
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable e) {
                logger.warn("distribute result failed: " + e.getMessage(), e);
            }
        }
        logger.debug("ResultDistributor thread exited");
    }

    @Override
    public void close() {
        this.running = false;
        
        // 中断分发线程，使其尽快退出 poll 阻塞
        if (distributorThread != null) {
            distributorThread.interrupt();
        }
        
        // 逐个关闭并清空消费者
        for (ResultConsumer consumer : consumers) {
            try {
                consumer.close();
            } catch (Exception e) {
                logger.warn("close consumer failed: " + e.getMessage(), e);
            }
        }
        consumers.clear();
        
        // 清空待分发队列
        pendingResultQueue.clear();
    }

    @Override
    public void addConsumer(ResultConsumer consumer) {
        int consumerNo = consumerNumGenerator.incrementAndGet();
        String consumerId = UUID.randomUUID().toString().replaceAll("-", "") + "_" + consumerNo;
        consumer.setConsumerId(consumerId);

        // 将历史队列中的消息复制给新消费者，实现加入即同步
        sharingResultConsumer.copyTo(consumer);

        consumers.add(consumer);
    }

    @Override
    public void removeConsumer(ResultConsumer consumer) {
        consumers.remove(consumer);
        consumer.close();
    }

    @Override
    public List<ResultConsumer> getConsumers() {
        return consumers;
    }

    @Override
    public ResultConsumer getConsumer(String consumerId) {
        for (int i = 0; i < consumers.size(); i++) {
            ResultConsumer consumer = consumers.get(i);
            if (consumer.getConsumerId().equals(consumerId)) {
                return consumer;
            }
        }
        return null;
    }

    /** 后台分发任务，运行在独立守护线程 */
    private class DistributorTask implements Runnable {
        @Override
        public void run() {
            distribute();
        }
    }

    /**
     * 内部共享结果缓存：维护会话级历史队列，供新消费者回放。
     * 不对外提供 poll，仅作为 append + copyTo 的中间存储。
     */
    private static class SharingResultConsumerImpl implements ResultConsumer {
        /** 历史结果队列 */
        private BlockingQueue<ResultModel> resultQueue = new ArrayBlockingQueue<ResultModel>(DistributorOptions.resultQueueSize);
        /** copyTo 时加锁，保证消息顺序 */
        private ReentrantLock queueLock = new ReentrantLock();
        /** 最近一次输入状态（不进入历史队列，copy 时最后发送） */
        private InputStatusModel lastInputStatus;

        @Override
        public boolean appendResult(ResultModel result) {
            queueLock.lock();
            try {
                // 输入状态单独保存，不参与历史回放队列
                if (result instanceof InputStatusModel) {
                    lastInputStatus = (InputStatusModel) result;
                    return true;
                }
                while (!resultQueue.offer(result)) {
                    ResultModel discardResult = resultQueue.poll();
                }
            } finally {
                if (queueLock.isHeldByCurrentThread()) {
                    queueLock.unlock();
                }
            }
            return true;
        }

        /** 将历史队列内容按序复制到目标消费者 */
        public void copyTo(ResultConsumer consumer) {
            // 加锁复制，仅阻塞分发线程，不影响业务线程
            queueLock.lock();
            try {
                for (ResultModel result : resultQueue) {
                    consumer.appendResult(result);
                }
                // 最后补发输入状态
                if (lastInputStatus != null) {
                    consumer.appendResult(lastInputStatus);
                }
            } finally {
                if (queueLock.isHeldByCurrentThread()) {
                    queueLock.unlock();
                }
            }
        }

        @Override
        public List<ResultModel> pollResults() {
            return null;
        }

        @Override
        public long getLastAccessTime() {
            return 0;
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public boolean isPolling() {
            return false;
        }

        @Override
        public String getConsumerId() {
            return "shared-consumer";
        }

        @Override
        public void setConsumerId(String consumerId) {
        }

        @Override
        public boolean isHealthy() {
            return true;
        }
    }
}
