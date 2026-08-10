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

package com.alibaba.nacos.core.model.request;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * 动态调整日志级别的 HTTP 请求体，指定 logger 名称与目标级别。
 * <p>实现 {@link NacosForm}，logName 与 logLevel 均为必填。</p>
 * Request entity for log operator interface.
 *
 * @author wuzhiguo
 */
public class LogUpdateRequest implements NacosForm {
    
    /** Logger 名称（通常为包名或类名）。 */
    private String logName;
    
    /** 目标日志级别（如 DEBUG、INFO、WARN）。 */
    private String logLevel;
    
    /** 获取 Logger 名称。 */
    public String getLogName() {
        return logName;
    }
    
    /** 设置 Logger 名称。 */
    public void setLogName(String logName) {
        this.logName = logName;
    }
    
    /** 获取目标日志级别。 */
    public String getLogLevel() {
        return logLevel;
    }
    
    /** 设置目标日志级别。 */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    /** 校验 logName 与 logLevel 均非空。 */
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(logName)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_MISSING,
                "Log name is required.");
        }
        if (StringUtils.isBlank(logLevel)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_MISSING,
                "Log level is required.");
        }
    }
}
