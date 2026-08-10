/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.paramcheck;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.spi.NacosServiceLoader;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数提取器管理器：通过 SPI 加载 HTTP/gRPC 提取器实现，并提供 {@link Extractor} 注解供 Controller 或 gRPC Handler 指定提取策略。
 * param checker to manager Extractor.
 *
 * @author 985492783@qq.com
 * @date 2023/11/7 16:29
 */

public class ExtractorManager {
    
    /**
     * 声明 HTTP 或 gRPC 请求应使用的参数提取器类；ParamChecker 优先读取方法上的注解，缺失时回退到 Controller/Handler 类级别。
     * ParamChecker will first look for the Checker annotation in the handler method, and if that annotation is null, it
     * will try to find the Checker annotation on the class where the handler method is located, and then load in the
     * target ParamExtractor in the Checker annotation.
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Extractor {
        
        /**
         * 指定 HTTP 参数提取器实现类，仅对 @Controller 类或方法生效。
         * Configure a Class to locate a specific Extractor, which takes effect only on the @Controller annotated class
         * or method.
         *
         * @return Class<? extends AbstractHttpParamExtractor>
         */
        Class<? extends AbstractHttpParamExtractor> httpExtractor() default DefaultHttpExtractor.class;
        
        /**
         * 指定 gRPC 参数提取器实现类，仅对 grpcHandler 生效。
         * Configure a Class to locate a specific Extractor, which takes effect only on grpcHandler.
         *
         * @return Class<? extends AbstractRpcParamExtractor>
         */
        Class<? extends AbstractRpcParamExtractor> rpcExtractor() default DefaultGrpcExtractor.class;
    }
    
    /** 默认 HTTP 提取器：不提取任何参数，返回空列表。 */
    public static class DefaultHttpExtractor extends AbstractHttpParamExtractor {
        
        @Override
        public List<ParamInfo> extractParam(HttpServletRequest params) {
            return Collections.emptyList();
        }
    }
    
    /** 默认 gRPC 提取器：不提取任何参数，返回空列表。 */
    public static class DefaultGrpcExtractor extends AbstractRpcParamExtractor {
        
        @Override
        public List<ParamInfo> extractParam(Request request) {
            return Collections.emptyList();
        }
    }
    
    /** gRPC 提取器类 → 单例实例 缓存。 */
    private static Map<Class<? extends AbstractRpcParamExtractor>, AbstractRpcParamExtractor> rpcManager =
        new ConcurrentHashMap<>();
    
    /** HTTP 提取器类 → 单例实例 缓存。 */
    private static Map<Class<? extends AbstractHttpParamExtractor>, AbstractHttpParamExtractor> httpManager =
        new ConcurrentHashMap<>();
    
    /** 启动时通过 SPI 加载所有 HTTP/gRPC 参数提取器实现。 */
    static {
        NacosServiceLoader.load(AbstractHttpParamExtractor.class).forEach(checker -> {
            httpManager.put(checker.getClass(), checker);
        });
        NacosServiceLoader.load(AbstractRpcParamExtractor.class).forEach(checker -> {
            rpcManager.put(checker.getClass(), checker);
        });
    }
    
    /** 按注解配置获取 gRPC 提取器，未注册时返回 {@link DefaultGrpcExtractor}。 */
    public static AbstractRpcParamExtractor getRpcExtractor(Extractor extractor) {
        return rpcManager.computeIfAbsent(extractor.rpcExtractor(),
            (key) -> new DefaultGrpcExtractor());
    }
    
    /** 按注解配置获取 HTTP 提取器，未注册时返回 {@link DefaultHttpExtractor}。 */
    public static AbstractHttpParamExtractor getHttpExtractor(Extractor extractor) {
        return httpManager.computeIfAbsent(extractor.httpExtractor(),
            (key) -> new DefaultHttpExtractor());
    }
}
