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

package com.alibaba.nacos.plugin.ai.importer.model;

import java.util.List;
import java.util.Map;

/**
 * AI 资源导入插件返回的候选资源分页结果。
 *
 * <p>封装一页 {@link AiResourceImportCandidate} 列表及游标分页信息，
 * 供导入管理器向控制台返回可翻页的搜索结果。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportCandidatePage {
    
    /** 当前页的候选资源列表。 */
    private List<AiResourceImportCandidate> items;
    
    /** 下一页游标，无更多数据时可为空。 */
    private String nextCursor;
    
    /** 是否还有更多候选资源可拉取。 */
    private boolean hasMore;
    
    /** 来源侧附加元数据（例如源标识、配额提示等）。 */
    private Map<String, String> sourceMetadata;
    
    public List<AiResourceImportCandidate> getItems() {
        return items;
    }
    
    public void setItems(List<AiResourceImportCandidate> items) {
        this.items = items;
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
    
    public Map<String, String> getSourceMetadata() {
        return sourceMetadata;
    }
    
    public void setSourceMetadata(Map<String, String> sourceMetadata) {
        this.sourceMetadata = sourceMetadata;
    }
}
