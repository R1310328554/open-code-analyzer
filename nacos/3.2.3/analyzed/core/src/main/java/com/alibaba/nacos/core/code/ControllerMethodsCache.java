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

package com.alibaba.nacos.core.code;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.packagescan.DefaultPackageScan;
import com.alibaba.nacos.common.utils.ArrayUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.code.RequestMappingInfo.RequestMappingInfoComparator;
import com.alibaba.nacos.core.code.condition.ParamRequestCondition;
import com.alibaba.nacos.core.code.condition.PathRequestCondition;
import com.alibaba.nacos.sys.env.EnvUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.nacos.sys.env.Constants.REQUEST_PATH_SEPARATOR;

/**
 * Controller 方法缓存：扫描 {@link RequestMapping} 注解，按 HTTP 方法与路径索引到 {@link Method}，供 Nacos 自研 HTTP 分发（如鉴权 Filter）快速解析目标处理器。
 * Method cache.
 *
 * @author nkorange
 * @since 1.2.0
 */
@Component
public class ControllerMethodsCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMethodsCache.class);
    
    /** 映射条件组合到 Controller 方法。 */
    private ConcurrentMap<RequestMappingInfo, Method> methods = new ConcurrentHashMap<>();
    
    /** 按「HTTP 方法 + 路径」键索引候选 {@link RequestMappingInfo} 列表。 */
    private final ConcurrentMap<String, List<RequestMappingInfo>> urlLookup =
        new ConcurrentHashMap<>();
    
    /** 已扫描过的 Controller 类，避免重复注册。 */
    private final Set<Class> scannedClass = new HashSet<>();
    
    /**
     * 根据 HTTP 请求解析并返回最匹配的 Controller 方法。
     *
     * @param request 当前 HTTP 请求
     * @return 匹配的方法，未命中返回 null
     */
    public Method getMethod(HttpServletRequest request) {
        String path = getPath(request);
        String httpMethod = request.getMethod();
        String urlKey = httpMethod + REQUEST_PATH_SEPARATOR
            + stripContextPath(path, resolveContextPath(request));
        List<RequestMappingInfo> requestMappingInfos = urlLookup.get(urlKey);
        if (CollectionUtils.isEmpty(requestMappingInfos)) {
            return null;
        }
        List<RequestMappingInfo> matchedInfo = findMatchedInfo(requestMappingInfos, request);
        if (CollectionUtils.isEmpty(matchedInfo)) {
            return null;
        }
        RequestMappingInfo bestMatch = matchedInfo.get(0);
        if (matchedInfo.size() > 1) {
            RequestMappingInfoComparator comparator = new RequestMappingInfoComparator();
            matchedInfo.sort(comparator);
            bestMatch = matchedInfo.get(0);
            RequestMappingInfo secondBestMatch = matchedInfo.get(1);
            if (comparator.compare(bestMatch, secondBestMatch) == 0) {
                throw new IllegalStateException(
                    "Ambiguous methods mapped for '" + request.getRequestURI() + "': {" + bestMatch
                        + ", "
                        + secondBestMatch + "}");
            }
        }
        return methods.get(bestMatch);
    }
    
    /** 解析请求上下文路径，空则回退 {@link EnvUtil#getContextPath()}。 */
    private String resolveContextPath(HttpServletRequest request) {
        String requestContextPath = request.getContextPath();
        return StringUtils.isEmpty(requestContextPath) ? EnvUtil.getContextPath()
            : requestContextPath;
    }
    
    /** 从 URI 路径中剥离 servlet 上下文前缀。 */
    private String stripContextPath(String path, String contextPath) {
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(contextPath)) {
            return path;
        }
        if (path.startsWith(contextPath)) {
            String stripped = path.substring(contextPath.length());
            return StringUtils.isEmpty(stripped) ? StringUtils.EMPTY : stripped;
        }
        return path;
    }
    
    /** 将 request URI 解析为不含 query 的路径字符串。 */
    private String getPath(HttpServletRequest request) {
        try {
            return new URI(request.getRequestURI()).getPath();
        } catch (URISyntaxException e) {
            LOGGER.error("parse request to path error", e);
            throw new NacosRuntimeException(NacosException.NOT_FOUND, "Invalid URI");
        }
    }
    
    /** 按请求参数条件过滤候选映射列表。 */
    private List<RequestMappingInfo> findMatchedInfo(List<RequestMappingInfo> requestMappingInfos,
        HttpServletRequest request) {
        List<RequestMappingInfo> matchedInfo = new ArrayList<>();
        for (RequestMappingInfo requestMappingInfo : requestMappingInfos) {
            ParamRequestCondition matchingCondition = requestMappingInfo.getParamRequestCondition()
                .getMatchingCondition(request);
            if (matchingCondition != null) {
                matchedInfo.add(requestMappingInfo);
            }
        }
        return matchedInfo;
    }
    
    /**
     * 扫描指定包下带 {@link RequestMapping} 的类并注册 URL 到方法的映射。
     *
     * @param packageName package name
     */
    public void initClassMethod(String packageName) {
        DefaultPackageScan packageScan = new DefaultPackageScan();
        Set<Class<Object>> classesList =
            packageScan.getTypesAnnotatedWith(packageName, RequestMapping.class);
        for (Class clazz : classesList) {
            initClassMethod(clazz);
        }
    }
    
    /**
     * 批量扫描给定 Controller 类列表并注册映射。
     *
     * @param classesList class list
     */
    public void initClassMethod(Set<Class<?>> classesList) {
        for (Class clazz : classesList) {
            initClassMethod(clazz);
        }
    }
    
    /**
     * 扫描单个 Controller 类的方法级与组合注解映射。
     *
     * @param clazz {@link Class}
     */
    private void initClassMethod(Class<?> clazz) {
        if (scannedClass.contains(clazz)) {
            return;
        }
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        for (String classPath : requestMapping.value()) {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    parseSubAnnotations(method, classPath);
                    continue;
                }
                requestMapping = method.getAnnotation(RequestMapping.class);
                RequestMethod[] requestMethods = requestMapping.method();
                if (requestMethods.length == 0) {
                    requestMethods = new RequestMethod[1];
                    requestMethods[0] = RequestMethod.GET;
                }
                // FIXME: vipserver 需要同一映射支持多种 HTTP 方法
                for (RequestMethod requestMethod : requestMethods) {
                    String[] value = requestMapping.value();
                    if (value.length > 0) {
                        for (String methodPath : requestMapping.value()) {
                            String urlKey = requestMethod.name() + REQUEST_PATH_SEPARATOR
                                + classPath + methodPath;
                            addUrlAndMethodRelation(urlKey, requestMapping.params(), method);
                        }
                    } else {
                        String urlKey = requestMethod.name() + REQUEST_PATH_SEPARATOR + classPath;
                        addUrlAndMethodRelation(urlKey, requestMapping.params(), method);
                    }
                }
            }
        }
        scannedClass.add(clazz);
    }
    
    /** 解析 Get/Post/Put/Delete/Patch 等组合注解并写入缓存。 */
    private void parseSubAnnotations(Method method, String classPath) {
        
        final GetMapping getMapping = method.getAnnotation(GetMapping.class);
        final PostMapping postMapping = method.getAnnotation(PostMapping.class);
        final PutMapping putMapping = method.getAnnotation(PutMapping.class);
        final DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        final PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        
        if (getMapping != null) {
            put(RequestMethod.GET, classPath, getMapping.value(), getMapping.params(), method);
        }
        
        if (postMapping != null) {
            put(RequestMethod.POST, classPath, postMapping.value(), postMapping.params(), method);
        }
        
        if (putMapping != null) {
            put(RequestMethod.PUT, classPath, putMapping.value(), putMapping.params(), method);
        }
        
        if (deleteMapping != null) {
            put(RequestMethod.DELETE, classPath, deleteMapping.value(), deleteMapping.params(),
                method);
        }
        
        if (patchMapping != null) {
            put(RequestMethod.PATCH, classPath, patchMapping.value(), patchMapping.params(),
                method);
        }
        
    }
    
    /** 将指定 HTTP 方法与路径组合注册到 urlLookup。 */
    private void put(RequestMethod requestMethod, String classPath, String[] requestPaths,
        String[] requestParams,
        Method method) {
        if (ArrayUtils.isEmpty(requestPaths)) {
            String urlKey = requestMethod.name() + REQUEST_PATH_SEPARATOR + classPath;
            addUrlAndMethodRelation(urlKey, requestParams, method);
            return;
        }
        for (String requestPath : requestPaths) {
            String urlKey = requestMethod.name() + REQUEST_PATH_SEPARATOR + classPath + requestPath;
            addUrlAndMethodRelation(urlKey, requestParams, method);
        }
    }
    
    /** 建立 urlKey、参数条件与 Method 的三元关联。 */
    private void addUrlAndMethodRelation(String urlKey, String[] requestParam, Method method) {
        RequestMappingInfo requestMappingInfo = new RequestMappingInfo();
        requestMappingInfo.setPathRequestCondition(new PathRequestCondition(urlKey));
        requestMappingInfo.setParamRequestCondition(new ParamRequestCondition(requestParam));
        List<RequestMappingInfo> requestMappingInfos =
            urlLookup.computeIfAbsent(urlKey, k -> new ArrayList<>());
        // 兼容 #4701：同时注册带尾斜杠的 urlKey
        urlLookup.computeIfAbsent(urlKey + "/", k -> requestMappingInfos);
        requestMappingInfos.add(requestMappingInfo);
        methods.put(requestMappingInfo, method);
    }
}
