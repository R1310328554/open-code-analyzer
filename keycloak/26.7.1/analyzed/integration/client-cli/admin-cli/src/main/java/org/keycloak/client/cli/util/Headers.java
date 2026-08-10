/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.cli.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.apache.http.entity.ContentType;

/**
 * 大小写不敏感、保持插入顺序的 HTTP 头集合。
 * <p>
 * 内部以小写键索引 {@link Header}，便于查找与去重。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Headers implements Iterable<Header> {

    /** 以小写头名为键的有序映射。 */
    private LinkedHashMap<String, Header> headers = new LinkedHashMap<>();

    /**
     * 添加或覆盖指定头。
     *
     * @param header 头名称
     * @param value 头值
     */
    public void add(String header, String value) {
        headers.put(header.toLowerCase(), new Header(header, value));
    }

    /**
     * 仅当该头尚不存在时添加。
     *
     * @param header 头名称
     * @param value 头值
     * @return 成功添加时返回 {@code true}
     */
    public boolean addIfMissing(String header, String value) {
        String key = header.toLowerCase();
        if (!headers.containsKey(key)) {
            headers.put(key, new Header(header, value));
            return true;
        }
        return false;
    }

    /** 判断是否包含指定头（大小写不敏感）。 */
    public boolean contains(String header) {
        String key = header.toLowerCase();
        return headers.containsKey(key);
    }

    /** 按名称获取头，不存在时返回 {@code null}。 */
    public Header get(String header) {
        return headers.get(header.toLowerCase());
    }

    @Override
    public Iterator<Header> iterator() {
        return headers.values().iterator();
    }

    /** 解析 {@code Content-Type} 头为 Apache {@link ContentType}。 */
    public Optional<ContentType> getContentType() {
        return Optional.ofNullable(headers.get("content-type")).map(Header::getValue).map(ContentType::parse);
    }
}
