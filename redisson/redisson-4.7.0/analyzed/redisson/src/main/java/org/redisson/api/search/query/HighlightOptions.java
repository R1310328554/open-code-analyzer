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
package org.redisson.api.search.query;

import java.util.List;

/**
 * 搜索结果高亮显示选项，用于配置高亮字段与包裹标签。
 *
 * @author Nikita Koksharov
 *
 */
public final class HighlightOptions {

    private List<String> fields;
    private String openTag;
    private String closeTag;

    private HighlightOptions() {
    }

    /** 创建默认高亮选项。 */
    public static HighlightOptions defaults() {
        return new HighlightOptions();
    }

    /**
     * 指定需要高亮的字段列表。
     *
     * @param fields 字段名列表
     * @return 当前选项
     */
    public HighlightOptions fields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * 设置高亮片段的开闭标签。
     *
     * @param open 开标签
     * @param close 闭标签
     * @return 当前选项
     */
    public HighlightOptions tags(String open, String close) {
        openTag = open;
        closeTag = close;
        return this;
    }

    /** 返回高亮字段列表。 */
    public List<String> getFields() {
        return fields;
    }

    /** 返回高亮开标签。 */
    public String getOpenTag() {
        return openTag;
    }

    /** 返回高亮闭标签。 */
    public String getCloseTag() {
        return closeTag;
    }
}
