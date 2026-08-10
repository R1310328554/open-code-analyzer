/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 批量任务完成计数器：为每个批次维护一个 {@link java.util.concurrent.atomic.AtomicBoolean}，
 * 调用方在单批成功后标记对应位，{@link #batchCompleted()} 可判断是否全部批次完成。
 * 常用于分片同步、批量推送等需等待全部分片就绪的场景。
 * batch task counter.
 *
 * @author shiyiyue
 */
public class BatchTaskCounter {
    
    /** 各批次完成标志列表，索引 0 对应第 1 批 */
    List<AtomicBoolean> batchCounter;
    
    /**
     * 按总批次数初始化计数器。
     *
     * @param totalBatch 批次总数
     */
    public BatchTaskCounter(int totalBatch) {
        initBatchCounter(totalBatch);
    }
    
    /**
     * 初始化批次标志数组，全部为未完成状态。
     *
     * @param totalBatch 批次总数
     */
    private void initBatchCounter(int totalBatch) {
        batchCounter = new ArrayList<>(totalBatch);
        for (int i = 0; i < totalBatch; i++) {
            batchCounter.add(i, new AtomicBoolean(false));
        }
    }
    
    /**
     * 标记指定批次已成功（批次号从 1 开始）。
     *
     * @param batch 已成功的批次序号
     */
    public void batchSuccess(int batch) {
        if (batch <= batchCounter.size()) {
            batchCounter.get(batch - 1).set(true);
        }
    }
    
    /**
     * 检查是否所有批次均已标记成功。
     *
     * @return 全部完成返回 true，否则 false
     */
    public boolean batchCompleted() {
        for (AtomicBoolean atomicBoolean : batchCounter) {
            if (!atomicBoolean.get()) {
                return false;
            }
        }
        return true;
    }
    
    /** 返回批次总数 */
    public int getTotalBatch() {
        return batchCounter.size();
    }
}
