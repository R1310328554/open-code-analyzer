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

import java.util.Arrays;
import java.util.Objects;

/**
 * 远程服务调用的唯一标识键：
 * 由服务接口、方法名与方法签名（参数类型哈希）组成，
 * 用于在 {@link BaseRemoteService} 中注册与查找本地方法实现。
 * <p>
 * 实现 {@link #equals} 与 {@link #hashCode}，可作为 Map 键使用。
 *
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceKey {

    /** 远程服务接口类型。 */
    private final Class<?> serviceInterface;
    /** 目标方法名。 */
    private final String methodName;
    /** 方法参数类型签名（用于重载区分）。 */
    private final long[] signature;

    /** @param serviceInterface 服务接口 @param method 方法名 @param signature 参数类型签名数组 */
    public RemoteServiceKey(Class<?> serviceInterface, String method, long[] signature) {
        super();
        this.serviceInterface = serviceInterface;
        this.methodName = method;
        this.signature = signature;
    }
    
    /** @return 方法名 */
    public String getMethodName() {
        return methodName;
    }

    /** @return 参数类型签名数组 */
    public long[] getSignature() {
        return signature;
    }
    
    /** @return 服务接口 Class */
    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    /** 比较接口、方法名与签名数组是否完全一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteServiceKey that = (RemoteServiceKey) o;
        return Objects.equals(serviceInterface, that.serviceInterface)
                    && Objects.equals(methodName, that.methodName)
                        && Objects.deepEquals(signature, that.signature);
    }

    /** 基于接口、方法名与签名数组计算哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(serviceInterface, methodName, Arrays.hashCode(signature));
    }
}
