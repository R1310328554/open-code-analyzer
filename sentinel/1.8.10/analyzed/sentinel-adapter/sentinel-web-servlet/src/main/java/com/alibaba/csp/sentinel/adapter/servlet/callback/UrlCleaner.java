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
package com.alibaba.csp.sentinel.adapter.servlet.callback;

/***
 * URL 清洗器，将原始 URL 统一为规范的资源名。
 *
 * @author youji.zj
 */
public interface UrlCleaner {

    /***
     * <p>处理 URL，对路径变量进行清洗与统一。</p>
     * <p>例如 collect_item_relation--10200012121-.html 会被转换为 collect_item_relation.html</p>
     *
     * @param originUrl original url
     * @return processed url
     */
    String clean(String originUrl);
}
