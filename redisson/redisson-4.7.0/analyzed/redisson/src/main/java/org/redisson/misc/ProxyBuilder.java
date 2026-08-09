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
package org.redisson.misc;

import org.redisson.api.RFuture;
import org.redisson.connection.ServiceManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 JDK {@link Proxy} 为 Redisson 对象创建动态代理。
 * <p>
 * 同步方法名以 {@code Async} 结尾时走 {@link Callback} 异步路径；
 * 否则委托到 implementation 或 instance 上的真实方法。
 * 方法映射结果缓存在 {@code METHODS_MAPPING} 中。
 *
 * @author Nikita Koksharov
 *
 */
//TODO refactor to common object and MethodHandles
//MethodHandle mh = MethodHandles.lookup().findVirtual(implementation.getClass(), method.getName(),
//                MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
public class ProxyBuilder {

    /** 异步方法调用时的执行钩子，由调用方决定线程与超时策略。 */
    public interface Callback {

        /** 执行返回 RFuture 的可调用体并处理结果。 */
        Object execute(Callable<RFuture<Object>> callable, Method instanceMethod);

    }

    /** 接口 Method + 实例类 → 实际反射 Method 的全局缓存。 */
    private static final ConcurrentMap<Tuple<Method, Class<?>>, Method> METHODS_MAPPING = new ConcurrentHashMap<>();

    /**
     * 创建类型 {@code clazz} 的动态代理。
     * <p>
     * {@code instance} 为 Redisson 实现对象；{@code implementation} 可选覆盖层。
     */
    public static <T> T create(Callback commandExecutor, Object instance, Object implementation, Class<T> clazz, ServiceManager serviceManager) {
        InvocationHandler handler = (proxy, method, args) -> {
            Method instanceMethod = getMethod(method, instance, implementation);

            // 接口方法对应 Async 后缀实现，走异步回调路径
            if (instanceMethod.getName().endsWith("Async")) {
                Callable<RFuture<Object>> callable = () -> (RFuture<Object>) instanceMethod.invoke(instance, args);
                return commandExecutor.execute(callable, method);
            }

            if (implementation != null
                    && instanceMethod.getDeclaringClass().isAssignableFrom(implementation.getClass())) {
                return instanceMethod.invoke(implementation, args);
            }

            return instanceMethod.invoke(instance, args);
        };
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
    }

    /** 解析接口方法到 instance/implementation 上的实际 Method，带缓存。 */
    private static Method getMethod(Method method, Object instance, Object implementation) throws NoSuchMethodException {
        Tuple<Method, Class<?>> key = new Tuple<>(method, instance.getClass());
        Method instanceMethod = METHODS_MAPPING.get(key);
        if (instanceMethod == null) {
            if (implementation != null) {
                try {
                    instanceMethod = implementation.getClass().getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    try {
                        instanceMethod = instance.getClass().getMethod(method.getName() + "Async", method.getParameterTypes());
                    } catch (Exception e2) {
                        instanceMethod = instance.getClass().getMethod(method.getName(), method.getParameterTypes());
                    }
                }
            } else {
                try {
                    instanceMethod = instance.getClass().getMethod(method.getName() + "Async", method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    instanceMethod = instance.getClass().getMethod(method.getName(), method.getParameterTypes());
                }
            }

            METHODS_MAPPING.put(key, instanceMethod);
        }
        return instanceMethod;
    }

}
