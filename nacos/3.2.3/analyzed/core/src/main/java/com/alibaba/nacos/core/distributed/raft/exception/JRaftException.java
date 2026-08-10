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

package com.alibaba.nacos.core.distributed.raft.exception;

/**
 * JRaft 协议运行时异常：启动失败、apply 错误等不可恢复场景的统一包装。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class JRaftException extends RuntimeException {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 8802314713344513544L;
    
    /** 无参构造。 */
    public JRaftException() {
    }
    
    /** 带消息的构造。 */
    public JRaftException(String message) {
        super(message);
    }
    
    /** 带消息与原因的构造。 */
    public JRaftException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** 以原异常为原因的构造。 */
    public JRaftException(Throwable cause) {
        super(cause);
    }
    
    public JRaftException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
