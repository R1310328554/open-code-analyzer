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
package org.redisson.codec;

import java.io.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 自定义 {@link ObjectInputStream}，支持指定 ClassLoader 与反序列化类白名单。
 * <p>
 * 在 Java 序列化场景下，优先用传入的 ClassLoader 加载类；
 * 若配置了 {@code allowedClasses}，则只允许白名单内的类名通过，否则抛出 {@link InvalidClassException}。
 *
 * @author Nikita Koksharov
 *
 */
public class CustomObjectInputStream extends ObjectInputStream {

    /** 用于加载反序列化类的 ClassLoader。 */
    private final ClassLoader classLoader;
    /** 允许反序列化的全限定类名集合；null 表示不限制。 */
    private Set<String> allowedClasses;

    /**
     * 带 ClassLoader 与白名单的构造器。
     *
     * @param classLoader 类加载器
     * @param in 底层输入流
     * @param allowedClasses 允许反序列化的类名集合
     */
    public CustomObjectInputStream(ClassLoader classLoader, InputStream in, Set<String> allowedClasses) throws IOException {
        super(in);
        this.classLoader = classLoader;
        this.allowedClasses = allowedClasses;
    }

    /** 仅指定 ClassLoader，不限制可反序列化类。 */
    public CustomObjectInputStream(ClassLoader classLoader, InputStream in) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }
    
    /**
     * 解析普通类：先校验白名单，再用指定 ClassLoader 加载；
     * 加载失败时回退到父类默认行为。
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            String name = desc.getName();
            if (allowedClasses != null && !allowedClasses.contains(name)) {
                throw new InvalidClassException("Class " + name + " isn't allowed");
            }
            return Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
    
    /**
     * 解析动态代理类：用指定 ClassLoader 加载各接口并生成代理类。
     */
    @Override
    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        List<Class<?>> loadedClasses = new ArrayList<Class<?>>(interfaces.length);
        
        for (String name : interfaces) {
            Class<?> clazz = Class.forName(name, false, classLoader);
            loadedClasses.add(clazz);
        }
        
        return Proxy.getProxyClass(classLoader, loadedClasses.toArray(new Class[0]));
    }
    
}
