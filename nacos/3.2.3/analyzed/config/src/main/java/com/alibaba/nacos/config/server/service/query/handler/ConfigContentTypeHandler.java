/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.query.handler;

import com.alibaba.nacos.config.server.enums.FileTypeEnum;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;

import java.io.IOException;

/**
 * 配置内容类型处理器：在下游返回有效配置后，根据扩展名或已有 contentType 解析 {@link com.alibaba.nacos.config.server.enums.FileTypeEnum} 并设置 HTTP Content-Type 头。
 * The type Config content type handler.
 * @author Sunrisea
 */
public class ConfigContentTypeHandler extends AbstractConfigQueryHandler {
    
    private static final String CONFIG_CONTENT_TYPE_HANDLER_NAME = "ConfigContentTypeHandler";
    
    @Override
    public String getName() {
        return CONFIG_CONTENT_TYPE_HANDLER_NAME;
    }
    
    @Override
    public ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException {
        // 先委托后续处理器获取配置内容
        ConfigQueryChainResponse response = getNextHandler().handle(request);
        // 未找到或特殊 Tag 未命中时直接透传
        if (response.getStatus() == ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND
            || response
                .getStatus() == ConfigQueryChainResponse.ConfigQueryStatus.SPECIAL_TAG_CONFIG_NOT_FOUND) {
            return response;
        }
        String contentType =
            response.getContentType() != null ? response.getContentType()
                : FileTypeEnum.TEXT.getFileType();
        FileTypeEnum fileTypeEnum =
            FileTypeEnum.getFileTypeEnumByFileExtensionOrFileType(contentType);
        String contentTypeHeader = fileTypeEnum.getContentType();
        response.setContentType(contentTypeHeader);
        return response;
    }
}
