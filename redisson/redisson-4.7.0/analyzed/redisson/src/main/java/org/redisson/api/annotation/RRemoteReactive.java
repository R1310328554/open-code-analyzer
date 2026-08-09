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
package org.redisson.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口为远程服务的 Reactor 响应式客户端代理；方法签名须与远程服务接口一致，
 * 但返回类型必须为 {@code reactor.core.publisher.Mono}。
 * <p>
 * 不必声明远程服务的全部方法，仅添加需要调用的即可。
 * 
 * @see reactor.core.publisher.Mono
 * 
 * @author Nikita Koksharov
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RRemoteReactive {

    /**
     * 注册远程服务时对应的远程接口类。
     * 
     * @return 远程服务接口 Class
     */
    Class<?> value();
    
}
