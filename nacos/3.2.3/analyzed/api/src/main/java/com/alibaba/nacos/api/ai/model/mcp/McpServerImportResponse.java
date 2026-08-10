/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.mcp;

import java.io.Serializable;
import java.util.List;

/**
 * MCP Server 批量导入响应，汇总导入成功/失败/跳过数量及逐项结果。
 *
 * @author nacos
 */
public class McpServerImportResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 整体导入是否成功。 */
    private boolean success;
    
    /** 待导入 Server 总数。 */
    private int totalCount = 0;
    
    /** 成功导入数量。 */
    private int successCount = 0;
    
    /** 导入失败数量。 */
    private int failedCount = 0;
    
    /** 跳过数量（如重复项）。 */
    private int skippedCount = 0;
    
    /** 各 Server 的导入结果列表。 */
    private List<McpServerImportResult> results;
    
    /** 整体错误信息。 */
    private String errorMessage;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
    
    public int getFailedCount() {
        return failedCount;
    }
    
    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
    
    public int getSkippedCount() {
        return skippedCount;
    }
    
    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }
    
    public List<McpServerImportResult> getResults() {
        return results;
    }
    
    public void setResults(List<McpServerImportResult> results) {
        this.results = results;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
