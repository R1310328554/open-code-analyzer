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

/**
 * 路径编解码 SPI：各操作系统可实现本接口，对文件系统非法字符进行编码/解码，由 {@link PathEncoderManager} 按 os.name 匹配加载。
 * To encode path if illegal,an os may have a PathEncoder.
 *
 * @author daydreamer-ia
 */
public interface PathEncoder {
    
    /**
     * encode path.
     *
     * @param str origin
     * @param charset charset
     * @return new path
      * <p>路径编码 SPI；详见类级说明。</p>
     */
    /** 按指定字符集将原始路径编码为文件系统安全形式 */
    String encode(String str, String charset);
    
    /**
     * decode path.
     *
     * @param str new path
     * @param charset charset
     * @return origin path
      * <p>路径编码 SPI；详见类级说明。</p>
     */
    /** 将编码路径解码回原始字符串 */
    String decode(String str, String charset);
    
    /**
     * return simple lowercase os name.
     *
     * @return simple lowercase os name
      * <p>路径编码 SPI；详见类级说明。</p>
     */
    String name();
    
    /**
     * whether to encode.
     *
     * @param key key
     * @return whether to encode.
      * <p>路径编码 SPI；详见类级说明。</p>
     */
    /** 判断给定路径片段是否需要进行编码 */
    boolean needEncode(String key);
}
