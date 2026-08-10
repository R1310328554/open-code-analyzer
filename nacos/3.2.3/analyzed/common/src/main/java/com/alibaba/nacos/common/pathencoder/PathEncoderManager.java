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

package com.alibaba.nacos.common.pathencoder;

import com.alibaba.nacos.common.spi.NacosServiceLoader;

import java.nio.charset.Charset;
import java.util.Collection;

/**
 * 路径编码管理器单例：启动时通过 SPI 加载与当前 OS 匹配的 {@link PathEncoder}，对外提供 encode/decode 便捷方法。
 * To expose interface from {@link PathEncoder}.
 *
 * @author daydreamer-ia
 */
public class PathEncoderManager {
    
    /**
     * singleton.
      * <p>路径编码管理器；详见类级说明。</p>
     */
    private static final PathEncoderManager INSTANCE = new PathEncoderManager();
    
    /**
     * encoder.
      * <p>路径编码管理器；详见类级说明。</p>
     */
    /** 当前 OS 匹配到的 PathEncoder，无匹配时为 null */
    private PathEncoder targetEncoder = null;
    
    /** 构造时 SPI 加载 PathEncoder，按 os.name 包含关系匹配首个实现 */
    private PathEncoderManager() {
        // load path encoder
        Collection<PathEncoder> load = NacosServiceLoader.load(PathEncoder.class);
        if (!load.isEmpty()) {
            String currentOs = System.getProperty("os.name").toLowerCase();
            for (PathEncoder pathEncoder : load) {
                // match first
                if (currentOs.contains(pathEncoder.name())) {
                    targetEncoder = pathEncoder;
                    break;
                }
            }
        }
    }
    
    /**
     * encode path if necessary.
     *
     * @param path    origin path
     * @param charset charset of origin path
     * @return encoded path
      * <p>路径编码管理器；详见类级说明。</p>
     */
    /** 必要时编码路径；path 或 charset 为 null 时原样返回 */
    public String encode(String path, String charset) {
        if (path == null || charset == null) {
            return path;
        }
        if (targetEncoder != null && targetEncoder.needEncode(path)) {
            return targetEncoder.encode(path, charset);
        }
        return path;
    }
    
    /**
     * encode path if necessary.
     *
     * @param path origin path
     * @return encoded path
      * <p>路径编码管理器；详见类级说明。</p>
     */
    public String encode(String path) {
        return encode(path, Charset.defaultCharset().name());
    }
    
    /**
     * decode path.
     *
     * @param path    encoded path
     * @param charset charset of encoded path
     * @return origin path
      * <p>路径编码管理器；详见类级说明。</p>
     */
    /** 使用 targetEncoder 解码；无编码器时原样返回 */
    public String decode(String path, String charset) {
        if (path == null || charset == null) {
            return path;
        }
        if (targetEncoder != null) {
            return targetEncoder.decode(path, charset);
        }
        return path;
    }
    
    /**
     * decode path.
     *
     * @param path encoded path
     * @return origin path
      * <p>路径编码管理器；详见类级说明。</p>
     */
    public String decode(String path) {
        return decode(path, Charset.defaultCharset().name());
    }
    
    /**
     * get singleton.
     *
     * @return singleton.
      * <p>路径编码管理器；详见类级说明。</p>
     */
    /** 返回全局单例 */
    public static PathEncoderManager getInstance() {
        return INSTANCE;
    }
    
}
