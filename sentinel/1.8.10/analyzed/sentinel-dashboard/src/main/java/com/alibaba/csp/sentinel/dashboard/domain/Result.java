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
package com.alibaba.csp.sentinel.dashboard.domain;

/**
 * Dashboard REST API 统一响应包装。
 * <p>包含 success、code、msg 与泛型 data 载荷。
 *
 * @author leyou
 * @author Eric Zhao
 */
public class Result<R> {

    /** 请求是否成功。 */
    private boolean success;
    /** 业务错误码，成功时通常为 0。 */
    private int code;
    /** 提示或错误信息。 */
    private String msg;
    /** 响应数据体。 */
    private R data;

    /** 构造带数据的成功响应。 */
    public static <R> Result<R> ofSuccess(R data) {
        return new Result<R>()
            .setSuccess(true)
            .setMsg("success")
            .setData(data);
    }

    /** 构造仅含自定义消息的成功响应（无 data）。 */
    public static <R> Result<R> ofSuccessMsg(String msg) {
        return new Result<R>()
            .setSuccess(true)
            .setMsg(msg);
    }

    /** 构造失败响应。 */
    public static <R> Result<R> ofFail(int code, String msg) {
        Result<R> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /** 将异常类型与 message 写入 msg 的失败响应。 */
    public static <R> Result<R> ofThrowable(int code, Throwable throwable) {
        Result<R> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(throwable.getClass().getName() + ", " + throwable.getMessage());
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Result<R> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public int getCode() {
        return code;
    }

    public Result<R> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<R> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public R getData() {
        return data;
    }

    public Result<R> setData(R data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Result{" +
            "success=" + success +
            ", code=" + code +
            ", msg='" + msg + '\'' +
            ", data=" + data +
            '}';
    }
}
