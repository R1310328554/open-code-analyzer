/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.cluster.health;

/**
 * 集群/模块就绪检查结果：包含是否成功及可读说明信息。
 * Readiness result.
 *
 * @author xiweng.yy
 */
public class ReadinessResult {
    
    /** 是否全部模块就绪。 */
    private final boolean success;
    
    /** 结果说明（成功时为 OK，失败时列出未就绪模块）。 */
    private final String resultMessage;
    
    /** 构造就绪结果。 */
    public ReadinessResult(boolean success, String resultMessage) {
        this.success = success;
        this.resultMessage = resultMessage;
    }
    
    /** 是否就绪成功。 */
    public boolean isSuccess() {
        return success;
    }
    
    /** 获取结果说明文本。 */
    public String getResultMessage() {
        return resultMessage;
    }
}
