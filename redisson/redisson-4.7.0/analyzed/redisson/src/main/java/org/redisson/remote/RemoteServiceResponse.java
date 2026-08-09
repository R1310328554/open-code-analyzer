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
 * 远程 RPC 响应体：
 * 服务端执行完成后写入响应队列，客户端 {@link SyncRemoteProxy}
 * 或 {@link AsyncRemoteProxy} 按 {@link #id} 匹配并完成 Future。
 * <p>
 * 成功时 {@link #result} 有值；失败时 {@link #error} 携带异常。
 *
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceResponse implements RRemoteServiceResponse, Serializable {

    private static final long serialVersionUID = -1958922748139674253L;

    /** 正常返回值（可为 null）。 */
    private Object result;
    /** 执行异常（成功时为 null）。 */
    private Throwable error;
    /** 对应请求的 ID。 */
    private String id;
    
    /** 无参构造，供序列化使用。 */
    public RemoteServiceResponse() {
    }
    
    /** 成功响应：携带结果值。 */
    public RemoteServiceResponse(String id, Object result) {
        this.result = result;
        this.id = id;
    }

    /** 失败响应：携带异常。 */
    public RemoteServiceResponse(String id, Throwable error) {
        this.error = error;
        this.id = id;
    }
    
    /** @return 请求 ID */
    @Override
    public String getId() {
        return id;
    }

    /** @return 执行异常，无异常时为 null */
    public Throwable getError() {
        return error;
    }
    
    /** @return 方法返回值 */
    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "RemoteServiceResponse{" +
                "result=" + result +
                ", error=" + error +
                ", id='" + id + '\'' +
                '}';
    }
}
