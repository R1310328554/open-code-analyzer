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
 * MCP Server 导入预校验结果，汇总解析数量、有效/无效/重复统计及逐项明细。
 *
 * <p>URL 导入模式下还包含分页游标 {@link #nextCursor} 与 {@link #hasMore} 标志。</p>
 *
 * @author nacos
 */
public class McpServerImportValidationResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 整体验证是否通过。 */
    private boolean valid;
    
    /** 解析出的 Server 总数。 */
    private int totalCount;
    
    /** 有效 Server 数量。 */
    private int validCount;
    
    /** 无效 Server 数量。 */
    private int invalidCount;
    
    /** 重复 Server 数量。 */
    private int duplicateCount;
    
    /** 解析并校验后的 Server 明细列表。 */
    private List<McpServerValidationItem> servers;
    
    /** 整体验证错误信息列表。 */
    private List<String> errors;
    
    /** 下一页游标（仅 URL 导入）；无更多页时为 null。 */
    private String nextCursor;
    
    /** 是否还有更多分页数据可加载。 */
    private boolean hasMore;
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getValidCount() {
        return validCount;
    }
    
    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }
    
    public int getInvalidCount() {
        return invalidCount;
    }
    
    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }
    
    public int getDuplicateCount() {
        return duplicateCount;
    }
    
    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }
    
    public List<McpServerValidationItem> getServers() {
        return servers;
    }
    
    public void setServers(List<McpServerValidationItem> servers) {
        this.servers = servers;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public String getNextCursor() {
        return nextCursor;
    }
    
    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
    
    public boolean isHasMore() {
        return hasMore;
    }
    
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
