/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.tools.admin.common;

/**
 * 管理工具统一返回体：包含 success、code、errorMsg 与泛型 data 载荷。
 */
public class AdminToolResult<T> {

    /** 操作是否成功。 */
    private boolean success;
    /** 结果码，对应 {@link AdminToolsResultCodeEnum}。 */
    private int code;
    /** 错误或提示信息。 */
    private String errorMsg;
    /** 业务数据载荷。 */
    private T data;

    /** 构造完整结果对象。 */
    public AdminToolResult(boolean success, int code, String errorMsg, T data) {
        this.success = success;
        this.code = code;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    /** 构造成功结果，code 为 {@link AdminToolsResultCodeEnum#SUCCESS}。 */
    public static AdminToolResult success(Object data) {
        return new AdminToolResult(true, AdminToolsResultCodeEnum.SUCCESS.getCode(), "success", data);
    }

    /** 构造失败结果（无 data）。 */
    public static AdminToolResult failure(AdminToolsResultCodeEnum errorCodeEnum, String errorMsg) {
        return new AdminToolResult(false, errorCodeEnum.getCode(), errorMsg, null);
    }

    /** 构造失败结果（附带部分 data）。 */
    public static AdminToolResult failure(AdminToolsResultCodeEnum errorCodeEnum, String errorMsg, Object data) {
        return new AdminToolResult(false, errorCodeEnum.getCode(), errorMsg, data);
    }

    /** 返回是否成功。 */
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** 返回结果码。 */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /** 返回错误信息。 */
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /** 返回业务数据。 */
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
