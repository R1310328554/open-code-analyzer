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

package com.alibaba.nacos.config.server.paramcheck;

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.core.exception.ErrorCode;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置长轮询监听 HTTP 参数提取器：解析 {@code Listening-Configs} 编码串，
 * 按行/字段分隔符拆分为多条 {@link ParamInfo}（dataId、group、可选 tenant）。
 * ConfigListener http param extractor.
 *
 * @author zhuoguang
 */
public class ConfigListenerHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** 单行内字段分隔符（ASCII 2） */
    static final char WORD_SEPARATOR_CHAR = (char) 2;
    
    /** 多配置行分隔符（ASCII 1） */
    static final char LINE_SEPARATOR_CHAR = (char) 1;
    
    /**
     * 解码并解析 Listening-Configs 批量监听参数。
     *
     * @param request HTTP 请求
     * @return 待校验的监听配置列表
     * @throws NacosRuntimeException URL 解码失败时抛出
     */
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) throws NacosRuntimeException {
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        String listenConfigs = request.getParameter("Listening-Configs");
        if (StringUtils.isBlank(listenConfigs)) {
            return paramInfos;
        }
        try {
            listenConfigs = URLDecoder.decode(listenConfigs, Constants.ENCODE);
        } catch (UnsupportedEncodingException e) {
            throw new NacosRuntimeException(ErrorCode.UnKnowError.getCode(), e);
        }
        if (StringUtils.isBlank(listenConfigs)) {
            return paramInfos;
        }
        String[] lines = listenConfigs.split(Character.toString(LINE_SEPARATOR_CHAR));
        for (String line : lines) {
            ParamInfo paramInfo = new ParamInfo();
            String[] words = line.split(Character.toString(WORD_SEPARATOR_CHAR));
            if (words.length < 2 || words.length > 4) {
                throw new IllegalArgumentException("invalid probeModify");
            }
            paramInfo.setDataId(words[0]);
            paramInfo.setGroup(words[1]);
            if (words.length == 4) {
                paramInfo.setNamespaceId(words[3]);
            }
            paramInfos.add(paramInfo);
        }
        return paramInfos;
    }
}
