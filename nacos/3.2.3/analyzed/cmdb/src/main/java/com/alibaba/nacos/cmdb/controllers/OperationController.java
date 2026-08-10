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

package com.alibaba.nacos.cmdb.controllers;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.cmdb.memory.CmdbProvider;
import com.alibaba.nacos.cmdb.utils.UtilsAndCommons;
import com.alibaba.nacos.core.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Operation controller.
 * <p>CMDB 运维 REST 控制器，挂载于 {@link UtilsAndCommons#NACOS_CMDB_CONTEXT}{@code /ops}，提供标签查询等调试/运维接口。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
@RestController
@RequestMapping(UtilsAndCommons.NACOS_CMDB_CONTEXT + "/ops")
public class OperationController {
    
    /** 内存 CMDB 数据提供者 */
    @Autowired
    private CmdbProvider cmdbProvider;
    
    /**
     * query label.
     * <p>按实体名 {@code entry} 与标签名 {@code label} 查询 IP 实体的标签值；参数通过 {@link WebUtils#required} 从请求中强制读取。</p>
     *
     * @param request http request
     * @return query result
     * @throws Exception exception
     */
    @Since("0.7.0")
    @RequestMapping(value = "/label", method = RequestMethod.GET)
    public String queryLabel(HttpServletRequest request) throws Exception {
        String entry = WebUtils.required(request, "entry");
        String label = WebUtils.required(request, "label");
        return cmdbProvider.queryLabel(entry, "ip", label);
    }
}
