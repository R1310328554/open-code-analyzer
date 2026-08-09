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
package org.redisson.executor;

/**
 * 远程执行器专用类加载器，用于在 Worker 节点动态定义任务类字节码。
 * <p>
 * 通过 {@link #loadClass(String, byte[])} 将序列化后的 class 体注入 JVM。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonClassLoader extends ClassLoader {

    /** 以指定父加载器构造。 */
    public RedissonClassLoader(ClassLoader parent) {
        super(parent);
    }

    /** 将字节数组定义为指定全限定名的 Class（不触发 resolve）。 */
    public void loadClass(String name, byte[] body) {
        defineClass(name, body, 0, body.length);
    }
    
}
