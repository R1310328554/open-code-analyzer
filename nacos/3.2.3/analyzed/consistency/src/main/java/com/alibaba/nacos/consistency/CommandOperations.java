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

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.model.RestResultUtils;

import java.util.Map;

/**
 * 一致性协议运维命令接口：供外部通过命令 Map 触发协议层运维操作。
 * Operation and maintenance command interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface CommandOperations {
    
    /**
     * 运维命令执行入口，默认返回成功空结果，子类可覆盖实现具体命令。
     * Operation and maintenance interface operation entry.
     *
     * @param commands commands
     * @return execute success
     */
    default RestResult<String> execute(Map<String, String> commands) {
        return RestResultUtils.success();
    }
    
}
