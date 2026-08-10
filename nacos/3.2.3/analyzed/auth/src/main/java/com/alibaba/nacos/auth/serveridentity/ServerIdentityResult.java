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

package com.alibaba.nacos.auth.serveridentity;

/**
 * Nacos 服务端身份校验结果。
 *
 * <p>封装 {@link ResultStatus} 状态与描述信息，供授权流程判断是否可跳过用户认证。</p>
 *
 * @author xiweng.yy
 */
public class ServerIdentityResult {
    
    /** 校验结果状态。 */
    private final ResultStatus status;
    
    /** 结果描述或错误信息。 */
    private final String message;
    
    /** 私有构造，通过静态工厂方法创建实例。 */
    private ServerIdentityResult(ResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }
    
    /** 返回校验状态。 */
    public ResultStatus getStatus() {
        return status;
    }
    
    /** 返回结果描述信息。 */
    public String getMessage() {
        return message;
    }
    
    /** 身份匹配成功。 */
    public static ServerIdentityResult success() {
        return new ServerIdentityResult(ResultStatus.MATCHED, "Server identity matched.");
    }
    
    /** 身份不匹配，需走常规用户认证。 */
    public static ServerIdentityResult noMatched() {
        return new ServerIdentityResult(ResultStatus.NOT_MATCHED, "Server identity not matched.");
    }
    
    /**
     * 校验过程失败。
     *
     * @param message 失败原因描述
     * @return 状态为 {@link ResultStatus#FAIL} 的结果
     */
    public static ServerIdentityResult fail(String message) {
        return new ServerIdentityResult(ResultStatus.FAIL, message);
    }
    
    /** 服务端身份校验结果状态枚举。 */
    public enum ResultStatus {
        
        /** 服务端身份匹配成功，可跳过用户认证。 */
        MATCHED,
        
        /** 服务端身份不匹配，需要常规认证。 */
        NOT_MATCHED,
        
        /** 校验过程出错（如配置缺失、内部异常）。 */
        FAIL;
    }
}
