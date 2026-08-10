/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.theme;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.owasp.html.Encoding;

/**
 * FreeMarker 模板 HTML 消毒方法。
 * <p>配合 {@code ?no_esc} 使用：允许 HTML 输出但剥离不安全标签与属性。</p>
 */
public class KeycloakSanitizerMethod implements TemplateMethodModelEx {
    
    /** href 属性匹配，用于后续 URL 修复。 */
    private static final Pattern HREF_PATTERN = Pattern.compile("\\s+href=\"([^\"]*)\"");
    
    /** 解码、消毒 HTML 并修复 href 中的编码字符。 */
    @Override
    public Object exec(List list) throws TemplateModelException {
        if ((list.isEmpty()) || (list.get(0) == null)) {
            throw new NullPointerException("Can not escape null value.");
        }
        
        String html = list.get(0).toString();

        html = decodeHtmlFull(html);

        String sanitized = KeycloakSanitizerPolicy.POLICY_DEFINITION.sanitize(html);
        
        return fixURLs(sanitized);
    }


    /** 反复 HTML 解码直至稳定或达到最大次数，防止多重编码绕过。 */
    // Fully decode HTML. Assume it can be encoded multiple times
    private String decodeHtmlFull(String html) {
        if (html == null) return null;

        int MAX_DECODING_COUNT = 5; // 最多尝试 5 次解码（应对多次 HTML 编码）
        String decodedHtml;

        for (int i = 0; i < MAX_DECODING_COUNT; i++) {
            decodedHtml = Encoding.decodeHtml(html);
            if (decodedHtml.equals(html)) {
                // 已完全解码
                return html;
            } else {
                // 继续下一轮解码
                html = decodedHtml;
            }
        }

        return "";
    }

    /** 还原 href 中被过度转义的 {@code =}、{@code &} 与 {@code ..} 序列。 */
    private String fixURLs(String msg) {
        Matcher matcher = HREF_PATTERN.matcher(msg);
        if (matcher.find()) {
            int last = 0;
            StringBuilder result = new StringBuilder(msg.length());
            do {
                String href = matcher.group(1).replaceAll("&#61;", "=")
                        .replaceAll("\\.\\.", ".")
                        .replaceAll("&amp;", "&");
                result.append(msg.substring(last, matcher.start(1))).append(href);
                last = matcher.end(1);
            } while (matcher.find());
            result.append(msg.substring(last));
            return result.toString();
        }
        return msg;
    }
    
}
