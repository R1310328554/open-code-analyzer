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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Template Utils.
 * <p>字符串条件执行模板：在字符串非空、为空或空白时分别执行回调，异常统一记录日志而不向上抛出，供客户端初始化逻辑复用。</p>
 *
 * @author Nacos
 */
public class TemplateUtils {
    
    /** 本类日志记录器 */
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateUtils.class);
    
    /**
     * Execute if string not empty.
     * <p>当 {@code source} 非空时执行 {@code runnable}；执行异常仅打 ERROR 日志。</p>
     *
     * @param source   source
     * @param runnable execute runnable
     */
    public static void stringNotEmptyAndThenExecute(String source, Runnable runnable) {
        
        if (StringUtils.isNotEmpty(source)) {
            
            try {
                runnable.run();
            } catch (Exception e) {
                LOGGER.error("string not empty and then execute cause an exception.", e);
            }
        }
    }
    
    /**
     * Execute if string empty.
     * <p>当 {@code source} 为空时调用 {@code callable} 获取默认值；否则对非 null 值 {@code trim()} 后返回。</p>
     *
     * @param source   empty source
     * @param callable execute callable
     * @return result
     */
    public static String stringEmptyAndThenExecute(String source, Callable<String> callable) {
        
        if (StringUtils.isEmpty(source)) {
            
            try {
                return callable.call();
            } catch (Exception e) {
                LOGGER.error("string empty and then execute cause an exception.", e);
            }
        }
        
        return source == null ? null : source.trim();
    }
    
    /**
     * Execute if string blank.
     * <p>当 {@code source} 为空白（{@link StringUtils#isBlank}）时调用 {@code callable}；否则对非 null 值 {@code trim()} 后返回。</p>
     *
     * @param source   empty source
     * @param callable execute callable
     * @return result
     */
    public static String stringBlankAndThenExecute(String source, Callable<String> callable) {
        
        if (StringUtils.isBlank(source)) {
            
            try {
                return callable.call();
            } catch (Exception e) {
                LOGGER.error("string empty and then execute cause an exception.", e);
            }
        }
        
        return source == null ? null : source.trim();
    }
}
