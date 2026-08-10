/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

/**
 * 配置删除响应。
 *
 * <p>服务端处理配置删除请求后返回操作结果。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigRemoveResponse.java, v 0.1 2020年07月16日 4:59 PM liuzunfei Exp $
 */
public class ConfigRemoveResponse extends Response {
    
    /** 无参构造，默认表示删除成功。 */
    public ConfigRemoveResponse() {
        super();
    }
    
    /**
     * 构建删除成功响应。
     *
     * @return 成功响应实例
     */
    public static ConfigRemoveResponse buildSuccessResponse() {
        return new ConfigRemoveResponse();
    }
    
    /**
     * 构建删除失败响应。
     *
     * @param errorMsg 错误描述信息
     * @return 失败响应实例
     */
    public static ConfigRemoveResponse buildFailResponse(String errorMsg) {
        ConfigRemoveResponse removeResponse = new ConfigRemoveResponse();
        removeResponse.setResultCode(ResponseCode.FAIL.getCode());
        removeResponse.setMessage(errorMsg);
        return removeResponse;
    }
}
