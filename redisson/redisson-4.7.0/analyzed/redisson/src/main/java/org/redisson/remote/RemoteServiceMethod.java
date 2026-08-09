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

import java.lang.reflect.Method;

/**
 * 远程服务端已注册的本地方法绑定：
 * 将 Spring/容器中的 Bean 实例与其 {@link Method} 反射对象配对，
 * 供 {@link BaseRemoteService} 在收到 {@link RemoteServiceRequest} 后反射调用。
 *
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceMethod {

    /** 承载方法实现的 Bean 实例。 */
    private final Object bean;
    /** 待调用的反射 Method。 */
    private final Method method;
    
    /** @param method 目标方法 @param bean 实现 Bean */
    public RemoteServiceMethod(Method method, Object bean) {
        super();
        this.method = method;
        this.bean = bean;
    }

    /** @return 实现 Bean */
    public Object getBean() {
        return bean;
    }
    
    /** @return 反射 Method */
    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "RemoteServiceMethod{" +
                "bean=" + bean +
                ", method=" + method +
                '}';
    }
}
