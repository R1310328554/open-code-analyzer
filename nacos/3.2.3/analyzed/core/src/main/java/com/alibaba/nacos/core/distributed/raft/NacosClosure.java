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

package com.alibaba.nacos.core.distributed.raft;

import com.alibaba.nacos.consistency.entity.Response;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.error.RaftError;
import com.google.protobuf.Message;

/**
 * Nacos 对 JRaft {@link Closure} 的封装：在 Raft apply 完成后将 Status、Response 与异常一并回传上层。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NacosClosure implements Closure {
    
    /** 本次 apply 对应的 Protobuf 请求消息。 */
    private Message message;
    
    /** 外层回调 Closure（通常包装 FailoverClosure）。 */
    private Closure closure;
    
    /** 携带业务响应与异常的扩展 Status。 */
    private NacosStatus nacosStatus = new NacosStatus();
    
    /** 绑定请求消息与完成回调。 */
    public NacosClosure(Message message, Closure closure) {
        this.message = message;
        this.closure = closure;
    }
    
    @Override
    public void run(Status status) {
        nacosStatus.setStatus(status);
        closure.run(nacosStatus);
        clear();
    }
    
    /** apply 完成后释放引用，避免内存泄漏。 */
    private void clear() {
        message = null;
        closure = null;
        nacosStatus = null;
    }
    
    /** 由状态机设置业务层 Response。 */
    public void setResponse(Response response) {
        this.nacosStatus.setResponse(response);
    }
    
    /** 由状态机设置 apply 过程中的异常。 */
    public void setThrowable(Throwable throwable) {
        this.nacosStatus.setThrowable(throwable);
    }
    
    /** 返回原始请求消息。 */
    public Message getMessage() {
        return message;
    }
    
    // 将状态机内部 Throwable 透传至外层
    
    /** 扩展 JRaft Status，附加 Nacos Response 与 Throwable。 */
    public static class NacosStatus extends Status {
        
        /** 底层 JRaft Status 委托对象。 */
        private Status status;
        
        /** 业务处理结果。 */
        private Response response = null;
        
        /** apply 或 onRequest 抛出的异常。 */
        private Throwable throwable = null;
        
        public void setStatus(Status status) {
            this.status = status;
        }
        
        @Override
        public void reset() {
            status.reset();
        }
        
        @Override
        public boolean isOk() {
            return status.isOk();
        }
        
        @Override
        public int getCode() {
            return status.getCode();
        }
        
        @Override
        public void setCode(int code) {
            status.setCode(code);
        }
        
        @Override
        public RaftError getRaftError() {
            return status.getRaftError();
        }
        
        @Override
        public void setError(int code, String fmt, Object... args) {
            status.setError(code, fmt, args);
        }
        
        @Override
        public void setError(RaftError error, String fmt, Object... args) {
            status.setError(error, fmt, args);
        }
        
        @Override
        public String toString() {
            return status.toString();
        }
        
        @Override
        public Status copy() {
            NacosStatus copy = new NacosStatus();
            copy.status = this.status;
            copy.response = this.response;
            copy.throwable = this.throwable;
            return copy;
        }
        
        @Override
        public String getErrorMsg() {
            return status.getErrorMsg();
        }
        
        @Override
        public void setErrorMsg(String errMsg) {
            status.setErrorMsg(errMsg);
        }
        
        public Response getResponse() {
            return response;
        }
        
        public void setResponse(Response response) {
            this.response = response;
        }
        
        public Throwable getThrowable() {
            return throwable;
        }
        
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
        
    }
}
