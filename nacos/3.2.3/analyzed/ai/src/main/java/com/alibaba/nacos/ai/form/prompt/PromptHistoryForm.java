/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.form.prompt;

import com.alibaba.nacos.api.exception.api.NacosApiException;

import java.io.Serial;

/**
 * Prompt history query form.
 * <p>Prompt 版本历史分页查询表单；pageNo 从 1 起计，pageSize 默认 10 且上限为 {@link #MAX_PAGE_SIZE}。</p>
 *
 * @author nacos
 */
public class PromptHistoryForm extends PromptForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 单页最大条数上限。 */
    private static final int MAX_PAGE_SIZE = 50;
    
    /**
     * Page number (1-based).
     * <p>页码，从 1 开始；小于 1 时校验阶段重置为 1。</p>
     */
    private int pageNo = 1;
    
    /**
     * Page size.
     * <p>每页条数，默认 10，超过 MAX_PAGE_SIZE 时截断。</p>
     */
    private int pageSize = 10;
    
    @Override
    public void validate() throws NacosApiException {
        super.validate();
        
        // 页码非法时回退到第一页
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        // 超过单页上限时截断
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }
    
    public int getPageNo() {
        return pageNo;
    }
    
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
