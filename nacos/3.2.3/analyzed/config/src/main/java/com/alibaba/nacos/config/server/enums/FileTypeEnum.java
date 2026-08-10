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

package com.alibaba.nacos.config.server.enums;

import com.alibaba.nacos.common.http.param.MediaType;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * 配置文件类型枚举：映射扩展名、逻辑类型与 HTTP Content-Type。
 * 用于发布/导出时选择正确的 MIME 与存储格式。
 * Config file type enum.
 *
 * @author klw
 * @date 2019/7/1 10:21
 */
public enum FileTypeEnum {
    
    /**
     * YAML 配置文件（扩展名 yaml）。
     * Yaml file.
     */
    YML("yaml", MediaType.TEXT_PLAIN),
    
    /**
     * Yaml file.
      * <p>配置文件类型与 Content-Type 映射；详见类级说明。</p>
     */
    /** YAML 别名，同 {@link #YML} */
    YAML("yaml", MediaType.TEXT_PLAIN),
    
    /**
     * 纯文本配置（扩展名 text）。
     * Text file.
     */
    TXT("text", MediaType.TEXT_PLAIN),
    
    /**
     * Text file.
      * <p>配置文件类型与 Content-Type 映射；详见类级说明。</p>
     */
    /** TEXT 别名，同 {@link #TXT} */
    TEXT("text", MediaType.TEXT_PLAIN),
    
    /**
     * JSON 配置文件。
     * Json file.
     */
    JSON("json", MediaType.APPLICATION_JSON),
    
    /**
     * XML 配置文件。
     * Xml file.
     */
    XML("xml", MediaType.APPLICATION_XML),
    
    /**
     * HTML 配置文件（htm 扩展名）。
     * Html file.
     */
    HTM("html", MediaType.TEXT_HTML),
    
    /**
     * Html file.
      * <p>配置文件类型与 Content-Type 映射；详见类级说明。</p>
     */
    /** HTML 别名，同 {@link #HTM} */
    HTML("html", MediaType.TEXT_HTML),
    
    /**
     * Java Properties 键值对配置。
     * Properties file.
     */
    PROPERTIES("properties", MediaType.TEXT_PLAIN);
    
    /** 逻辑文件类型（yaml/text/json 等） */
    /** File type corresponding to file extension. */
    
    private String fileType;
    
    /** 对应 HTTP Content-Type 头 */
    /** Http Content type corresponding to file extension. */
    
    private String contentType;
    
    FileTypeEnum(String fileType) {
        this.fileType = fileType;
        this.contentType = MediaType.TEXT_PLAIN;
    }
    
    FileTypeEnum(String fileType, String contentType) {
        this.fileType = fileType;
        this.contentType = contentType;
    }
    
    public String getFileType() {
        return this.fileType;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    /**
     * 按扩展名或类型名解析枚举；未匹配时默认 {@link #TEXT}。
     *
     * @param extOrFileType file extension or fileType
     * @return 匹配的 FileTypeEnum，或 TEXT
     */
    public static FileTypeEnum getFileTypeEnumByFileExtensionOrFileType(String extOrFileType) {
        if (StringUtils.isNotBlank(extOrFileType)) {
            String upperExtName = extOrFileType.trim().toUpperCase();
            for (FileTypeEnum value : VALUES) {
                if (value.name().equals(upperExtName)) {
                    return value;
                }
            }
        }
        return FileTypeEnum.TEXT;
    }
    
    private static final FileTypeEnum[] VALUES = FileTypeEnum.values();
}
