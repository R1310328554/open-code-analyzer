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

package com.alibaba.nacos.api.exception;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.utils.StringUtils;

/**
 * Nacos 统一异常类。
 *
 * <p>封装错误码（{@link #errCode}）与错误消息，客户端与服务端交互时统一抛出本异常。</p>
 *
 * @author Nacos
 */
public class NacosException extends Exception {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -3913902031489277776L;
    
    /** 错误码。 */
    private int errCode;
    
    /** 错误消息。 */
    private String errMsg;
    
    /** 根因异常。 */
    private Throwable causeThrowable;
    
    /** 无参构造。 */
    public NacosException() {
    }
    
    /**
     * 构造带错误码与消息的异常。
     *
     * @param errCode 错误码
     * @param errMsg  错误消息
     */
    public NacosException(final int errCode, final String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    
    /**
     * 构造带错误码与根因的异常。
     *
     * @param errCode   错误码
     * @param throwable 根因异常
     */
    public NacosException(final int errCode, final Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
        this.setCauseThrowable(throwable);
    }
    
    /**
     * 构造带错误码、消息与根因的异常。
     *
     * @param errCode   错误码
     * @param errMsg    错误消息
     * @param throwable 根因异常
     */
    public NacosException(final int errCode, final String errMsg, final Throwable throwable) {
        super(errMsg, throwable);
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.setCauseThrowable(throwable);
    }
    
    /** 获取错误码。 */
    public int getErrCode() {
        return this.errCode;
    }
    
    /** 获取错误消息（优先返回 errMsg，否则取根因消息）。 */
    public String getErrMsg() {
        if (!StringUtils.isBlank(this.errMsg)) {
            return this.errMsg;
        }
        if (this.causeThrowable != null) {
            return this.causeThrowable.getMessage();
        }
        return Constants.NULL;
    }
    
    /** 设置错误码。 */
    public void setErrCode(final int errCode) {
        this.errCode = errCode;
    }
    
    /** 设置错误消息。 */
    public void setErrMsg(final String errMsg) {
        this.errMsg = errMsg;
    }
    
    /** 设置根因异常（递归取最内层 cause）。 */
    public void setCauseThrowable(final Throwable throwable) {
        this.causeThrowable = this.getCauseThrowable(throwable);
    }
    
    /** 递归获取最内层根因异常。 */
    private Throwable getCauseThrowable(final Throwable t) {
        if (t.getCause() == null) {
            return t;
        }
        return this.getCauseThrowable(t.getCause());
    }
    
    /** 返回包含错误码与错误消息的字符串表示。 */
    @Override
    public String toString() {
        return "ErrCode:" + getErrCode() + ", ErrMsg:" + getErrMsg();
    }
    
    /* 客户端错误码：-400、-503 等直接抛给用户。 */
    
    /** 客户端参数无效（参数错误）。 */
    public static final int CLIENT_INVALID_PARAM = -400;
    
    /** 客户端连接断开。 */
    public static final int CLIENT_DISCONNECT = -401;
    
    /** 超过客户端限流阈值。 */
    public static final int CLIENT_OVER_THRESHOLD = -503;
    
    /* 服务端错误码：400/403 直接抛给用户；500/502/503 建议切换 IP 重试。 */
    
    /** 服务端参数无效（参数错误）。 */
    public static final int INVALID_PARAM = 400;
    
    /** 无访问权限（鉴权失败）。 */
    public static final int NO_RIGHT = 403;
    
    /** 资源不存在。 */
    public static final int NOT_FOUND = 404;
    
    /** 资源未修改（HTTP 304）。 */
    public static final int NOT_MODIFIED = 304;
    
    /** 写并发冲突。 */
    public static final int CONFLICT = 409;
    
    /** 配置已存在。 */
    public static final int CONFIG_ALREADY_EXISTS = 410;
    
    /** 服务端内部错误（如超时）。 */
    public static final int SERVER_ERROR = 500;
    
    /** 服务端未实现该请求（版本不支持或 API 错误）。 */
    public static final int SERVER_NOT_IMPLEMENTED = 501;
    
    /** 客户端异常（返回给服务端）。 */
    public static final int CLIENT_ERROR = -500;
    
    /** 网关异常（如 Nginx 后端 Server 不可用）。 */
    public static final int BAD_GATEWAY = 502;
    
    /** 超过服务端限流阈值。 */
    public static final int OVER_THRESHOLD = 503;
    
    /** 服务端尚未启动。 */
    public static final int INVALID_SERVER_STATUS = 300;
    
    /** 连接未注册。 */
    public static final int UN_REGISTER = 301;
    
    /** 未找到请求处理器。 */
    public static final int NO_HANDLER = 302;
    
    /** 资源未找到（客户端侧错误码）。 */
    public static final int RESOURCE_NOT_FOUND = -404;
    
    /** HTTP 客户端错误码，使用 Nacos RestTemplate 或 AsyncRestTemplate 时可能抛出。 */
    public static final int HTTP_CLIENT_ERROR_CODE = -500;
    
}
