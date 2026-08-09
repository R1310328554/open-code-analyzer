/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.MethodUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import javax.interceptor.InvocationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * CDI {@link SentinelResourceInterceptor} 的公共支撑：资源名、fallback/blockHandler 解析与异常追踪。
 *
 * @author Eric Zhao
 * @author seasidesky
 * @author dowenliu-xyz(hawkdowen@hotmail.com)
 */
public abstract class AbstractSentinelInterceptorSupport {

    protected void traceException(Throwable ex) {
        Tracer.trace(ex);
    }

    protected void traceException(Throwable ex, SentinelResourceBinding annotation) {
        Class<? extends Throwable>[] exceptionsToIgnore = annotation.exceptionsToIgnore();
        // 优先检查 exceptionsToIgnore 列表
        if (exceptionsToIgnore.length > 0 && exceptionBelongsTo(ex, exceptionsToIgnore)) {
            return;
        }
        if (exceptionBelongsTo(ex, annotation.exceptionsToTrace())) {
            traceException(ex);
        }
    }

    /**
     * 判断异常是否属于给定异常类型列表。
     *
     * @param ex         provided throwable
     * @param exceptions list of exceptions
     * @return true if it is in the list, otherwise false
     */
    protected boolean exceptionBelongsTo(Throwable ex, Class<? extends Throwable>[] exceptions) {
        if (exceptions == null) {
            return false;
        }
        for (Class<? extends Throwable> exceptionClass : exceptions) {
            if (exceptionClass.isAssignableFrom(ex.getClass())) {
                return true;
            }
        }
        return false;
    }

    protected String getResourceName(String resourceName, /*@NonNull*/ Method method) {
        // 注解 value 非空时直接使用
        if (StringUtil.isNotBlank(resourceName)) {
            return resourceName;
        }
        // 否则按方法签名生成资源名
        return MethodUtil.resolveMethodName(method);
    }

    protected Object handleFallback(InvocationContext ctx, SentinelResourceBinding annotation, Throwable ex)
        throws Throwable {
        return handleFallback(ctx, annotation.fallback(), annotation.defaultFallback(), annotation.fallbackClass(), ex);
    }

    protected Object handleFallback(InvocationContext ctx, String fallback, String defaultFallback,
                                    Class<?>[] fallbackClass, Throwable ex) throws Throwable {
        Object[] originArgs = ctx.getParameters();

        // 若配置了 fallback 则优先执行
        Method fallbackMethod = extractFallbackMethod(ctx, fallback, fallbackClass);
        if (fallbackMethod != null) {
            // 构造 fallback 参数
            int paramCount = fallbackMethod.getParameterTypes().length;
            Object[] args;
            if (paramCount == originArgs.length) {
                args = originArgs;
            } else {
                args = Arrays.copyOf(originArgs, originArgs.length + 1);
                args[args.length - 1] = ex;
            }

            try {
                if (isStatic(fallbackMethod)) {
                    return fallbackMethod.invoke(null, args);
                }
                return fallbackMethod.invoke(ctx.getTarget(), args);
            } catch (InvocationTargetException e) {
                // 解包 InvocationTargetException
                throw e.getTargetException();
            }
        }
        // fallback 缺失时尝试 defaultFallback
        return handleDefaultFallback(ctx, defaultFallback, fallbackClass, ex);
    }

    protected Object handleDefaultFallback(InvocationContext ctx, String defaultFallback,
                                           Class<?>[] fallbackClass, Throwable ex) throws Throwable {
        // 执行 defaultFallback
        Method fallbackMethod = extractDefaultFallbackMethod(ctx, defaultFallback, fallbackClass);
        if (fallbackMethod != null) {
            // Construct args.
            Object[] args = fallbackMethod.getParameterTypes().length == 0 ? new Object[0] : new Object[] {ex};
            try {
                if (isStatic(fallbackMethod)) {
                    return fallbackMethod.invoke(null, args);
                }
                return fallbackMethod.invoke(ctx.getTarget(), args);
            } catch (InvocationTargetException e) {
                // throw the actual exception
                throw e.getTargetException();
            }
        }

        // 无任何 fallback 时原样抛出
        throw ex;
    }

    protected Object handleBlockException(InvocationContext ctx, SentinelResourceBinding annotation, BlockException ex)
        throws Throwable {

        // 若配置了 blockHandler 则调用
        Method blockHandlerMethod = extractBlockHandlerMethod(ctx, annotation.blockHandler(),
            annotation.blockHandlerClass());
        if (blockHandlerMethod != null) {
            Object[] originArgs = ctx.getParameters();
            // Construct args.
            Object[] args = Arrays.copyOf(originArgs, originArgs.length + 1);
            args[args.length - 1] = ex;
            try {
                if (isStatic(blockHandlerMethod)) {
                    return blockHandlerMethod.invoke(null, args);
                }
                return blockHandlerMethod.invoke(ctx.getTarget(), args);
            } catch (InvocationTargetException e) {
                // throw the actual exception
                throw e.getTargetException();
            }
        }

        // 无 blockHandler 时降级到 fallback
        return handleFallback(ctx, annotation, ex);
    }

    private Method extractFallbackMethod(InvocationContext ctx, String fallbackName, Class<?>[] locationClass) {
        if (StringUtil.isBlank(fallbackName)) {
            return null;
        }
        boolean mustStatic = locationClass != null && locationClass.length >= 1;
        Class<?> clazz = mustStatic ? locationClass[0] : ctx.getTarget().getClass();
        Method originMethod = resolveMethod(ctx);
        MethodWrapper m = ResourceMetadataRegistry.lookupFallback(clazz, fallbackName, originMethod.getParameterTypes());
        if (m == null) {
            // 首次解析 fallback
            Method method = resolveFallbackInternal(originMethod, fallbackName, clazz, mustStatic);
            // 缓存解析结果
            ResourceMetadataRegistry.updateFallbackFor(clazz, fallbackName, originMethod.getParameterTypes(), method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method extractDefaultFallbackMethod(InvocationContext ctx, String defaultFallback,
                                                Class<?>[] locationClass) {
        if (StringUtil.isBlank(defaultFallback)) {
            SentinelResource annotationClass = ctx.getTarget().getClass().getAnnotation(SentinelResource.class);
            if (annotationClass != null && StringUtil.isNotBlank(annotationClass.defaultFallback())) {
                defaultFallback = annotationClass.defaultFallback();
                if (locationClass == null || locationClass.length < 1) {
                    locationClass = annotationClass.fallbackClass();
                }
            } else {
                return null;
            }
        }
        boolean mustStatic = locationClass != null && locationClass.length >= 1;
        Class<?> clazz = mustStatic ? locationClass[0] : ctx.getTarget().getClass();

        MethodWrapper m = ResourceMetadataRegistry.lookupDefaultFallback(clazz, defaultFallback);
        if (m == null) {
            // First time, resolve the default fallback.
            Class<?> originReturnType = resolveMethod(ctx).getReturnType();
            // defaultFallback 支持无参
            Class<?>[] defaultParamTypes = new Class<?>[0];
            // 或单参数 {@link Throwable}
            Class<?>[] paramTypeWithException = new Class<?>[] {Throwable.class};
            // 先查找无参版本
            Method method = findMethod(mustStatic, clazz, defaultFallback, originReturnType, defaultParamTypes);
            // 未找到时再查找带 Throwable 的版本
            if (method == null) {
                method = findMethod(mustStatic, clazz, defaultFallback, originReturnType, paramTypeWithException);
            }
            // Cache the method instance.
            ResourceMetadataRegistry.updateDefaultFallbackFor(clazz, defaultFallback, method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method resolveFallbackInternal(Method originMethod, String name, Class<?> clazz, boolean mustStatic) {
        // fallback 支持与原方法相同签名或末尾追加 Throwable
        Class<?>[] defaultParamTypes = originMethod.getParameterTypes();
        Class<?>[] paramTypesWithException = Arrays.copyOf(defaultParamTypes, defaultParamTypes.length + 1);
        paramTypesWithException[paramTypesWithException.length - 1] = Throwable.class;
        // 优先匹配原方法签名
        Method method = findMethod(mustStatic, clazz, name, originMethod.getReturnType(), defaultParamTypes);
        // 未找到时再匹配带 Throwable 的版本
        if (method == null) {
            method = findMethod(mustStatic, clazz, name, originMethod.getReturnType(), paramTypesWithException);
        }
        return method;
    }

    private Method extractBlockHandlerMethod(InvocationContext ctx, String name, Class<?>[] locationClass) {
        if (StringUtil.isBlank(name)) {
            return null;
        }

        boolean mustStatic = locationClass != null && locationClass.length >= 1;
        Class<?> clazz;
        if (mustStatic) {
            clazz = locationClass[0];
        } else {
            // 默认在当前目标类中查找
            clazz = ctx.getTarget().getClass();
        }
        Method originMethod = resolveMethod(ctx);
        MethodWrapper m = ResourceMetadataRegistry.lookupBlockHandler(clazz, name, originMethod.getParameterTypes());
        if (m == null) {
            // First time, resolve the block handler.
            Method method = resolveBlockHandlerInternal(originMethod, name, clazz, mustStatic);
            // Cache the method instance.
            ResourceMetadataRegistry.updateBlockHandlerFor(clazz, name, originMethod.getParameterTypes(), method);
            return method;
        }
        if (!m.isPresent()) {
            return null;
        }
        return m.getMethod();
    }

    private Method resolveBlockHandlerInternal(Method originMethod, String name, Class<?> clazz, boolean mustStatic) {
        Class<?>[] originList = originMethod.getParameterTypes();
        Class<?>[] parameterTypes = Arrays.copyOf(originList, originList.length + 1);
        parameterTypes[parameterTypes.length - 1] = BlockException.class;
        return findMethod(mustStatic, clazz, name, originMethod.getReturnType(), parameterTypes);
    }

    private boolean checkStatic(boolean mustStatic, Method method) {
        return !mustStatic || isStatic(method);
    }

    private Method findMethod(boolean mustStatic, Class<?> clazz, String name, Class<?> returnType,
                              Class<?>... parameterTypes) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (name.equals(method.getName()) && checkStatic(mustStatic, method)
                && returnType.isAssignableFrom(method.getReturnType())
                && Arrays.equals(parameterTypes, method.getParameterTypes())) {

                RecordLog.info("Resolved method [{}] in class [{}]", name, clazz.getCanonicalName());
                return method;
            }
        }
        // 当前类未找到则递归查找父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass)) {
            return findMethod(mustStatic, superClass, name, returnType, parameterTypes);
        } else {
            String methodType = mustStatic ? " static" : "";
            RecordLog.warn("Cannot find{} method [{}] in class [{}] with parameters {}",
                methodType, name, clazz.getCanonicalName(), Arrays.toString(parameterTypes));
            return null;
        }
    }

    private boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    protected Method resolveMethod(InvocationContext ctx) {
        Class<?> targetClass = ctx.getTarget().getClass();

        Method method = getDeclaredMethodFor(targetClass, ctx.getMethod().getName(),
            ctx.getMethod().getParameterTypes());
        if (method == null) {
            throw new IllegalStateException("Cannot resolve target method: " + ctx.getMethod().getName());
        }
        return method;
    }

    /**
     * 在类及其父类中按名称与参数类型查找 declared 方法。
     *
     * @param clazz          class where the method is located
     * @param name           method name
     * @param parameterTypes method parameter type list
     * @return resolved method, null if not found
     */
    private Method getDeclaredMethodFor(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethodFor(superClass, name, parameterTypes);
            }
        }
        return null;
    }
}
