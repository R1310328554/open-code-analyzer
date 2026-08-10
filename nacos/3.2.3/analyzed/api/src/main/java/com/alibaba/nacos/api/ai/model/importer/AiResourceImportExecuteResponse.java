/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.importer;

import java.io.Serializable;
import java.util.List;

/**
 * AI 资源导入执行结果响应。
 *
 * <p>汇总成功/失败/跳过数量，并逐条返回 {@link AiResourceImportResultItem} 明细，
 * 便于控制台展示导入报告与错误定位。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportExecuteResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    
    private int totalCount;
    
    private int successCount;
    
    private int failedCount;
    
    private int skippedCount;
    
    private List<AiResourceImportResultItem> results;
    
    /** 整体导入是否视为成功（通常无致命失败时为 true）。 */
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /** 返回参与导入的总条目数。 */
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    /** 返回成功导入条目数。 */
    public int getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
    
    /** 返回失败条目数。 */
    public int getFailedCount() {
        return failedCount;
    }
    
    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
    
    /** 返回被策略跳过的条目数。 */
    public int getSkippedCount() {
        return skippedCount;
    }
    
    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }
    
    /** 返回逐条导入结果列表。 */
    public List<AiResourceImportResultItem> getResults() {
        return results;
    }
    
    public void setResults(List<AiResourceImportResultItem> results) {
        this.results = results;
    }
}
