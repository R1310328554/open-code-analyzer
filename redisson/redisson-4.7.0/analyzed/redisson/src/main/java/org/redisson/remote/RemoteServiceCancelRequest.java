/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.remote;

import java.io.Serializable;

/**
 * 客户端发起的远程任务取消请求（写入 cancel-request Map）。
 * <p>
 * Worker 轮询该 Map，根据 {@link #isMayInterruptIfRunning()} 决定是否中断
 * 正在执行的方法；{@link #isSendResponse()} 控制是否回传取消确认。
 * 
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceCancelRequest implements Serializable {

    private static final long serialVersionUID = -4800574267648904260L;

    /** 是否允许中断已在执行的远程方法。 */
    private boolean mayInterruptIfRunning;
    /** Worker 是否向客户端发送 {@link RemoteServiceCancelResponse}。 */
    
    public RemoteServiceCancelRequest() {
    }

    /** @param mayInterruptIfRunning 可否中断执行中任务 @param sendResponse 是否回传取消响应 */
    public RemoteServiceCancelRequest(boolean mayInterruptIfRunning, boolean sendResponse) {
        this.mayInterruptIfRunning = mayInterruptIfRunning;
        this.sendResponse = sendResponse;
    }
    
    /** @return 是否需要取消响应 */
    public boolean isSendResponse() {
        return sendResponse;
    }
    
    /** @return 是否允许 interrupt 正在运行的远程调用 */
    public boolean isMayInterruptIfRunning() {
        return mayInterruptIfRunning;
    }
    
}
