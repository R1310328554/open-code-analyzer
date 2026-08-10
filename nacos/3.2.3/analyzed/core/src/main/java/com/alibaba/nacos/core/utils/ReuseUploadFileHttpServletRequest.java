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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.common.http.HttpUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传专用请求包装：继承 {@link StandardMultipartHttpServletRequest} 并缓存去重参数。
 * httprequest wrapper.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ReuseUploadFileHttpServletRequest extends StandardMultipartHttpServletRequest
    implements ReuseHttpRequest {
    
    /** 默认 multipart 文件字段名。 */
    private static final String DEFAULT_FILE_NAME = "file";
    
    private final HttpServletRequest request;
    
    private Map<String, String[]> stringMap;
    
    /** 解析 multipart 请求并初始化去重参数 Map。 */
    public ReuseUploadFileHttpServletRequest(HttpServletRequest request) throws MultipartException {
        super(request);
        this.request = request;
        this.stringMap = toDuplication(request);
    }
    
    @Override
    public Map<String, String[]> getParameterMap() {
        return stringMap;
    }
    
    @Override
    public String getParameter(String name) {
        String[] values = stringMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }
    
    @Override
    public String[] getParameterValues(String name) {
        return stringMap.get(name);
    }
    
    @Override
    public Object getBody() throws Exception {
        // 优先返回名为 file 的上传资源；否则回退为编码后的表单参数
        MultipartFile target = super.getFile(DEFAULT_FILE_NAME);
        if (Objects.nonNull(target)) {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add(DEFAULT_FILE_NAME, target.getResource());
            return parts;
        } else {
            // 配置发布接口可能声明 multipart 但实际无文件，回退为表单参数编码
            return HttpUtils.encodingParams(HttpUtils.translateParameterMap(stringMap),
                StandardCharsets.UTF_8.name());
        }
    }
}
