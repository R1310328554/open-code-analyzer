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

package com.alibaba.nacos.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * 类路径与文件系统资源加载工具：支持 {@code classpath:} 前缀、
 * 指定 ClassLoader 及 Properties/Reader/File 多种形式读取。
 * resource util.
 *
 * @author boyan
 */
public class ResourceUtils {
    
    private ResourceUtils() {
    }
    
    /** classpath 资源 URI 前缀 */
    private static final String CLASSPATH_PREFIX = "classpath:";
    
    /**
     * Returns the URL of the resource on the classpath.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /**
     * 解析资源为 URL：{@code classpath:} 走类加载器，否则尝试 URL 或本地文件。
     *
     * @param resource 资源路径
     * @return 资源 URL
     * @throws IOException 资源不存在或无法读取
     */
    public static URL getResourceUrl(String resource) throws IOException {
        if (resource.startsWith(CLASSPATH_PREFIX)) {
            String path = resource.substring(CLASSPATH_PREFIX.length());
            
            ClassLoader classLoader = ResourceUtils.class.getClassLoader();
            
            URL url = (classLoader != null ? classLoader.getResource(path)
                : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException("Resource [" + resource + "] does not exist");
            }
            
            return url;
        }
        
        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return new File(resource).toURI().toURL();
        }
    }
    
    /**
     * Returns the URL of the resource on the classpath.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 使用指定 ClassLoader 查找 classpath 资源 URL */
    public static URL getResourceUrl(ClassLoader loader, String resource) throws IOException {
        URL url = null;
        if (loader != null) {
            url = loader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }
    
    /**
     * Returns a resource on the classpath as a Stream object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 以 InputStream 打开 classpath 资源 */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader loader = ResourceUtils.class.getClassLoader();
        return getResourceAsStream(loader, resource);
    }
    
    /**
     * Returns a resource on the classpath as a Stream object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 使用指定 ClassLoader 以 InputStream 打开资源 */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource)
        throws IOException {
        InputStream in = null;
        if (loader != null) {
            in = loader.getResourceAsStream(resource);
        }
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }
    
    /**
     * Returns a resource on the classpath as a Properties object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 加载 classpath 资源为 {@link Properties} */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        ClassLoader loader = ResourceUtils.class.getClassLoader();
        return getResourceAsProperties(loader, resource);
    }
    
    /**
     * Returns a resource on the classpath as a Properties object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 使用指定 ClassLoader 加载 Properties 资源 */
    public static Properties getResourceAsProperties(ClassLoader loader, String resource)
        throws IOException {
        Properties props = new Properties();
        InputStream in = getResourceAsStream(loader, resource);
        props.load(in);
        IoUtils.closeQuietly(in);
        return props;
    }
    
    /**
     * Returns a resource on the classpath as a Reader object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 以指定字符集创建 {@link InputStreamReader} 读取资源 */
    public static InputStreamReader getResourceAsReader(String resource, String charsetName)
        throws IOException {
        return new InputStreamReader(getResourceAsStream(resource), charsetName);
    }
    
    /**
     * Returns a resource on the classpath as a Reader object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 使用指定 ClassLoader 与字符集创建 Reader */
    public static Reader getResourceAsReader(ClassLoader loader, String resource,
        String charsetName)
        throws IOException {
        return new InputStreamReader(getResourceAsStream(loader, resource), charsetName);
    }
    
    /**
     * Returns a resource on the classpath as a File object.
     *
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 将 classpath 资源解析为 {@link File}（基于 URL.getFile） */
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceUrl(resource).getFile());
    }
    
    /**
     * Returns a resource on the classpath as a File object.
     *
     * @param url The resource url to find
     * @return The resource
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 由 URL 构造 File 对象 */
    public static File getResourceAsFile(URL url) {
        return new File(url.getFile());
    }
    
    /**
     * Returns a resource on the classpath as a File object.
     *
     * @param loader   The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws IOException If the resource cannot be found or read
      * <p>classpath 与文件资源加载；详见类级说明。</p>
     */
    /** 使用指定 ClassLoader 将资源解析为 File */
    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceUrl(loader, resource).getFile());
    }
    
}
