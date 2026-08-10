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
package org.keycloak.common.util;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

/**
 * 路径模板匹配抽象基类：支持精确匹配、{@code *} 通配、{@code {param}} 模板及后缀模式。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public abstract class PathMatcher<P> {

    private static final char WILDCARD = '*';

    /** 在已注册路径中查找与 {@code targetUri} 最具体匹配的配置项。 */
    public P matches(final String targetUri) {
        final String normalizedUri = normalizeUri(targetUri);
        if (normalizedUri == null) {
            return null;
        }
        int patternCount = 0;
        int bracketsPatternCount = 0;
        P matchingPath = null;
        P matchingAnyPath = null;
        P matchingAnySuffixPath = null;

        for (P entry : getPaths()) {
            String expectedUri = getPath(entry);

            if (expectedUri == null || expectedUri.isEmpty()) {
                continue;
            }

            String matchingUri = null;

            if (exactMatch(expectedUri, normalizedUri)) {
                matchingUri = expectedUri;
            }

            if (isTemplate(expectedUri)) {
                String templateUri = buildUriFromTemplate(expectedUri, normalizedUri, false);

                if (templateUri != null) {
                    int length = expectedUri.split("\\/").length;
                    int bracketsLength = expectedUri.split("\\{").length;

                    if (exactMatch(templateUri, normalizedUri) && (patternCount == 0 || length > patternCount || bracketsLength < bracketsPatternCount)) {
                        matchingUri = templateUri;
                        P resolved = resolvePathConfig(entry, normalizedUri);

                        if (resolved != null) {
                            entry = resolved;
                        }

                        patternCount = length;
                        bracketsPatternCount = bracketsLength;
                    }
                }
            }

            if (matchingUri != null) {
                StringBuilder path = new StringBuilder(expectedUri);
                int patternIndex = path.indexOf("/" + WILDCARD);

                if (patternIndex != -1) {
                    path.delete(patternIndex, path.length());
                }

                patternIndex = path.indexOf("{");

                if (patternIndex != -1) {
                    path.delete(patternIndex, path.length());
                }

                String pathString = path.toString();

                if ("".equals(pathString)) {
                    pathString = "/";
                }

                if (matchingUri.equals(normalizedUri) || pathString.equals(normalizedUri)) {
                    if (patternCount == 0) {
                        return entry;
                    } else {
                        matchingPath = entry;
                    }
                }

                if (WILDCARD == expectedUri.charAt(expectedUri.length() - 1)) {
                    if (matchingAnyPath == null) {
                        matchingAnyPath = entry;
                    } else {
                        String resourcePath = getPath(matchingAnyPath);

                        if (resourcePath.split("/").length < matchingUri.split("/").length) {
                            matchingAnyPath = entry;
                        }
                    }
                } else {
                    int suffixIndex = expectedUri.indexOf(WILDCARD + ".");

                    if (suffixIndex != -1) {
                        String protectedSuffix = expectedUri.substring(suffixIndex + 1);

                        if (normalizedUri.endsWith(protectedSuffix)) {
                            matchingAnySuffixPath = entry;
                        }
                    }
                }
            }
        }

        if (matchingPath != null) {
            return matchingPath;
        }

        if (matchingAnySuffixPath != null) {
            return matchingAnySuffixPath;
        }

        return matchingAnyPath;
    }

    protected abstract String getPath(P entry);

    protected abstract Collection<P> getPaths();

    private boolean exactMatch(String expectedUri, String targetUri) {
        if (targetUri.equals(expectedUri)) {
            return true;
        }

        if (endsWithWildcard(expectedUri)) {
            String rootPath = expectedUri.substring(0, expectedUri.length() - 1);

            if (targetUri.startsWith(rootPath)) {
                return true;
            }

            return targetUri.equals(rootPath.substring(0, rootPath.length() - 1));
        }

        String suffix = "/*.";
        int suffixIndex = expectedUri.indexOf(suffix);

        if (suffixIndex != -1) {
            return targetUri.endsWith(expectedUri.substring(suffixIndex + suffix.length() - 1));
        }

        return false;
    }

    protected String buildUriFromTemplate(String template, String targetUri, boolean onlyFirstParam) {
        StringBuilder uri = new StringBuilder(template);
        String expectedUri = uri.toString();
        int patternStartIndex = expectedUri.indexOf("{");

        if (expectedUri.endsWith("/*")) {
            expectedUri = expectedUri.substring(0, expectedUri.length() - 2);
        }

        if (patternStartIndex == -1 || patternStartIndex >= targetUri.length()) {
            return null;
        }

        if (expectedUri.split("/").length > targetUri.split("/").length) {
            return null;
        }

        char[] expectedUriChars = expectedUri.toCharArray();
        char[] matchingUri = Arrays.copyOfRange(expectedUriChars, 0, patternStartIndex);
        int matchingUriLastIndex = matchingUri.length;
        String targetUriParams = targetUri.substring(patternStartIndex);

        if (Arrays.equals(matchingUri, Arrays.copyOf(targetUri.toCharArray(), matchingUri.length))) {
            matchingUri = Arrays.copyOf(matchingUri, targetUri.length());
            int paramIndex = 0;
            int lastPattern = 0;

            for (int i = patternStartIndex; i < expectedUriChars.length; i++) {
                if (matchingUriLastIndex >= matchingUri.length) {
                    break;
                }

                char c = expectedUriChars[i];

                if (c == '{' || c == '*') {
                    String[] params = targetUriParams.split("/");

                    for (int k = paramIndex; k <= (c == '*' ? params.length : paramIndex); k++) {
                        if (k == params.length) {
                            break;
                        }

                        int paramLength = params[k].length();

                        if (matchingUriLastIndex + paramLength > matchingUri.length) {
                            return null;
                        }
                        
                        StringBuilder value = new StringBuilder();

                        for (int j = 0; j < paramLength; j++) {
                            char valueChar = params[k].charAt(j);
                            value.append(valueChar);
                            matchingUri[matchingUriLastIndex++] = valueChar;
                        }

                        if (c == '{') {
                            int openBraceIndex = uri.indexOf("{", lastPattern);
                            int closingBraceIndex = uri.indexOf("}", lastPattern);
                            if (openBraceIndex == -1 || closingBraceIndex == -1 || closingBraceIndex < openBraceIndex) {
                                return null;
                            }
                            String paramName = uri.substring(openBraceIndex + 1, closingBraceIndex);
                            if (paramName.indexOf('/') != -1) {
                                return null;
                            }
                            uri.replace(openBraceIndex, closingBraceIndex + 1, value.toString());
                        }

                        if (value.length() > 0 && value.charAt(value.length() - 1) == '}') {
                            lastPattern = uri.indexOf(value.toString()) + value.length();
                        }

                        if (c == '*' && matchingUriLastIndex < matchingUri.length) {
                            matchingUri[matchingUriLastIndex++] = '/';
                        }
                    }

                    if (c == '{') {
                        i = expectedUri.indexOf('}', i);
                    }

                    if (i == expectedUri.lastIndexOf('}') && onlyFirstParam) {
                        return String.valueOf(matchingUri).substring(0, matchingUriLastIndex);
                    }
                } else {
                    if (c == '/') {
                        paramIndex++;
                    }
                    matchingUri[matchingUriLastIndex++] = c;
                }
            }

            return uri.toString();
        }

        return null;
    }

    public boolean endsWithWildcard(String expectedUri) {
        int length = expectedUri.length();
        return length > 0 && WILDCARD == expectedUri.charAt(length - 1);
    }

    private boolean isTemplate(String uri) {
        return uri.indexOf("{") != -1;
    }

    /**
     * 校验路径模板语法是否合法。
     *
     * @return 非法时返回错误描述，合法时返回 {@code null}
     */
    public static String validateTemplate(String uri) {
        boolean inBrace = false;
        boolean empty = true;

        for (int i = 0; i < uri.length(); i++) {
            char c = uri.charAt(i);

            if (c == '{') {
                if (inBrace) {
                    return "nested '{'";
                }
                inBrace = true;
                empty = true;
            } else if (c == '}') {
                if (!inBrace || empty) {
                    return "unexpected '}' or empty parameter name";
                }
                inBrace = false;
            } else if (inBrace) {
                if (c == '/') {
                    return "parameter name contains '/'";
                }
                empty = false;
            }
        }

        if (inBrace) {
            return "missing closing '}'";
        }

        int asteriskIndex = uri.indexOf('*');
        if (asteriskIndex != -1) {
            boolean validTrailing = uri.endsWith("/*") && asteriskIndex == uri.length() - 1;
            boolean validSuffix = asteriskIndex > 0 && uri.charAt(asteriskIndex - 1) == '/'
                    && asteriskIndex + 2 < uri.length() && uri.charAt(asteriskIndex + 1) == '.'
                    && uri.indexOf('*', asteriskIndex + 1) == -1
                    && uri.indexOf('/', asteriskIndex + 1) == -1;
            if (!validTrailing && !validSuffix) {
                return "wildcard '*' is only supported as trailing '/*' or as a suffix pattern '/*.ext'";
            }
        }

        return null;
    }

    protected P resolvePathConfig(P entry, String path) {
        return entry;
    }

    protected String normalizeUri(String uri) {
        if (uri == null) {
            return null;
        }

        // 剥离 matrix 参数，防止 /api/admin;x=1 类绕过（Servlet/JAX-RS 路由时会忽略）
        StringBuilder sb = new StringBuilder(uri.length());
        boolean inMatrix = false;
        for (int i = 0; i < uri.length(); i++) {
            char c = uri.charAt(i);
            if (c == ';') {
                inMatrix = true;
            } else if (c == '/') {
                inMatrix = false;
                sb.append(c);
            } else if (!inMatrix) {
                sb.append(c);
            }
        }
        String result = sb.toString();

        // 合并连续斜杠 — //foo 会被 URI 解析为 authority 而非路径
        while (result.contains("//")) {
            result = result.replace("//", "/");
        }

        // 规范化点段并解码百分号编码，防止 ../、./、%61dmin 等绕过；
        // 服务端通常已解码，策略执行器侧 getRequestURI() 仍可能保留编码故需处理
        try {
            // 解析前转义花括号 — Keycloak 模板使用 {param}，原生 URI 构造函数不接受
            result = result.replace("{", "%7B").replace("}", "%7D");
            result = new URI(result).normalize().getPath();
            if (result == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        // 解码 %2F 等可能再次引入双斜杠
        while (result.contains("//")) {
            result = result.replace("//", "/");
        }

        // 去掉尾部斜杠，防止 /api/admin/ 与 /api/admin 不一致导致绕过
        if (result.length() > 1 && result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}
