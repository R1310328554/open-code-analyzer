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

package com.alibaba.nacos.ai.event;

import com.alibaba.nacos.common.notify.SlowEvent;

import java.io.Serial;

/**
 * Prompt 版本被下载时发布的慢事件。
 *
 * <p>由 {@code PromptDownloadCountManager} 消费，在内存中累加下载次数并定期刷入 DB。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class PromptDownloadEvent extends SlowEvent {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 命名空间 ID */
    private final String namespaceId;
    
    /** Prompt 名称 */
    private final String name;
    
    /** 被下载的版本号 */
    private final String version;
    
    public PromptDownloadEvent(String namespaceId, String name, String version) {
        this.namespaceId = namespaceId;
        this.name = name;
        this.version = version;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getVersion() {
        return version;
    }
}
