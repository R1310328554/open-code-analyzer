package com.taobao.arthas.core.distribution.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.distribution.PackingResultDistributor;
import com.taobao.arthas.core.shell.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * {@link PackingResultDistributor} 的默认实现：使用有界阻塞队列缓存命令结果，
 * 命令结束后通过 {@link #getResults()} 一次性 drain 全部数据。
 * <p>
 * 适用于同步 API 场景；队列满时丢弃新结果并记录警告日志。
 */
public class PackingResultDistributorImpl implements PackingResultDistributor {
    private static final Logger logger = LoggerFactory.getLogger(PackingResultDistributorImpl.class);

    /** 结果缓存队列，容量 500 */
    private BlockingQueue<ResultModel> resultQueue = new ArrayBlockingQueue<ResultModel>(500);
    private final Session session;

    public PackingResultDistributorImpl(Session session) {
        this.session = session;
    }

    @Override
    public void appendResult(ResultModel result) {
        // 非阻塞入队，满则丢弃并告警
        if (!resultQueue.offer(result)) {
            logger.warn("result queue is full: {}, discard later result: {}", resultQueue.size(), JSON.toJSONString(result));
        }
    }

    @Override
    public void close() {
        // 同步场景无需额外清理
    }

    @Override
    public List<ResultModel> getResults() {
        // 一次性取出并清空队列
        ArrayList<ResultModel> results = new ArrayList<ResultModel>(resultQueue.size());
        resultQueue.drainTo(results);
        return results;
    }

}
