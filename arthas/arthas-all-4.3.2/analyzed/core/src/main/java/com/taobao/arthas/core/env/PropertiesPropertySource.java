/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.core.env;

import java.util.Map;
import java.util.Properties;

/**
 * 基于 {@link java.util.Properties} 的 {@link PropertySource} 实现。
 * <p>
 * 继承 {@link MapPropertySource}，底层为 Properties（本质是 Hashtable）；
 * 仅支持 String 键值访问，与 {@link Properties#getProperty} 语义一致。
 * {@link #getPropertyNames()} 对 source 加 synchronized 保证线程安全。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public class PropertiesPropertySource extends MapPropertySource {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public PropertiesPropertySource(String name, Properties source) {
        super(name, (Map) source);
    }

    protected PropertiesPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        // Properties/Hashtable 非线程安全，枚举键时加锁
        synchronized (this.source) {
            return super.getPropertyNames();
        }
    }

}
