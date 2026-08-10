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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * CLI {@code --fields} 返回字段规范解析器与树形过滤器。
 * <p>
 * 支持逗号分隔字段名、括号嵌套子字段及 {@code *} 通配符与 {@code -field} 排除语法；
 * 与 {@link FilterUtil} 配合裁剪 Admin API JSON 输出。
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ReturnFields implements Iterable<String> {

    /** 包含全部字段（无嵌套子树）。 */
    public static ReturnFields ALL = new ReturnFields() {
        @Override
        public ReturnFields child(String field) {
            return NONE;
        }

        @Override
        public boolean included(String... pathSegments) {
            return true;
        }

        @Override
        public boolean excluded(String field) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            return Collections.singletonList("*").iterator();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        public boolean isAll() {
            return true;
        }

        @Override
        public String toString() {
            return "[ReturnFields ALL]";
        }
    };

    /** 不包含任何字段。 */
    public static ReturnFields NONE = new ReturnFields() {
        @Override
        public ReturnFields child(String field) {
            return this;
        }

        @Override
        public boolean included(String... pathSegments) {
            return false;
        }

        @Override
        public boolean excluded(String field) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            List<String> emptyList = Collections.emptyList();
            return emptyList.iterator();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isAll() {
            return false;
        }

        @Override
        public String toString() {
            return "[ReturnFields NONE]";
        }
    };

    /** 递归包含全部字段（嵌套层级同样匹配 {@code *}）。 */
    public static ReturnFields ALL_RECURSIVELY = new ReturnFields() {
        @Override
        public ReturnFields child(String field) {
            return this;
        }

        @Override
        public boolean included(String... pathSegments) {
            return true;
        }

        @Override
        public boolean excluded(String field) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            List<String> emptyList = Collections.emptyList();
            return emptyList.iterator();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isAll() {
            return true;
        }
    };
    
    /** 解析器目标状态（逗号、括号、标识符组合）。 */
    private enum TargetState {
        IdentCommaOpen,
        Ident,
        Comma,
        Anything
    }

    /** 当前字段 token 解析状态。 */
    private enum FieldState {
        start,
        name,
        end
    }


    /** 当前层级的字段名到子树映射。 */
    private HashMap<String, ReturnFields> fields = new LinkedHashMap<>();
    
    
    
    /** 构造空字段集。 */
    public ReturnFields() {}
    
    /**
     * 从规范字符串解析字段树。
     *
     * @param spec 字段规范，如 {@code id,name,roles(name)}
     */
    public ReturnFields(String spec) {

        if (spec == null || spec.trim().length() == 0) {
            throw new IllegalArgumentException("Fields spec is null or empty!");
        }
        // 解析规范并构建嵌套子字段树
        char[] buf = spec.toCharArray();
        StringBuilder token = new StringBuilder(buf.length);

        // 栈：跟踪嵌套括号深度
        LinkedList<HashMap<String, ReturnFields>> specs = new LinkedList<>();
        specs.add(fields);

        // 解析器状态机
        FieldState fldState = FieldState.start;
        TargetState state = TargetState.Ident;

        int i;
        for (i = 0; i < buf.length; i++) {
            char c = buf[i];

            if (c == ',') {
                if (state == TargetState.Ident) {
                    error(spec, i);
                }
                if (fldState == FieldState.name) {
                    specs.getLast().put(token.toString(), null);
                    token.setLength(0);
                }
                state = TargetState.Ident;
                fldState = FieldState.start;
            } else if (c == '(') {
                if (state != TargetState.IdentCommaOpen && state != TargetState.Anything) {
                    error(spec, i);
                }
                ReturnFields sub = new ReturnFields();
                specs.getLast().put(token.toString(), sub);
                specs.add(sub.fields);
                token.setLength(0);

                state = TargetState.Ident;
                fldState = FieldState.start;
            } else if (c == ')') {
                if (state != TargetState.Anything) {
                    error(spec, i);
                }
                if (fldState == FieldState.name) {
                    specs.getLast().put(token.toString(), null);
                    token.setLength(0);

                }
                specs.removeLast();

                fldState = FieldState.end;
                state = specs.size() > 1 ? TargetState.Anything : TargetState.Comma;
            } else {
                token.append(c);
                if (fldState == FieldState.start) {
                    fldState = FieldState.name;
                    state = specs.size() > 1 ? TargetState.Anything : TargetState.IdentCommaOpen;
                }
            }
        }

        if (specs.size() > 1) {
            error(spec, i);
        }

        if (token.length() > 0) {
            specs.getLast().put(token.toString(), null);
        } else if (!(state == TargetState.Anything || state == TargetState.Comma)) {
            error(spec, i);
        }
    }

    private void error(String spec, int i) {
        throw new RuntimeException("Invalid fields specification at position " + i + ": " + spec);
    }

    
    
    /**
     * 获取 JSONObject 类型子字段的 {@link ReturnFields} 子树。
     *
     * <p>基本类型字段始终返回 {@link #NONE}；请改用 {@link #included(String...)} 判断。</p>
     *
     * @param field 嵌套返回的子字段名
     * @return 子字段对应的 {@link ReturnFields}，无匹配时为 {@link #NONE}
     */
    public ReturnFields child(String field) {
        ReturnFields returnFields = fields.get(field);
        if (returnFields == null) {
            returnFields = fields.get("*");
            if (returnFields == null) {
                returnFields = ReturnFields.NONE;
            }
        }
        return returnFields;
    }

    /**
     * 判断指定路径是否应出现在 JSON 响应中。
     *
     * <p>可通过多个路径段相对当前嵌套层级进行任意深度检查。</p>
     *
     * @param pathSegments 在返回字段树中测试的路径段
     * @return 若该路径应包含在 JSON 响应中则为 {@code true}
     */
    public boolean included(String... pathSegments) {

        if (pathSegments == null || pathSegments.length == 0) {
            throw new IllegalArgumentException("No path specified!");
        }
        ReturnFields current = this;

        for (String path : pathSegments) {
            if (current == null) {
                return false;
            }

            if (current.fields.containsKey("-" + path)) {
                return false;
            }
            if (current.fields.containsKey("*")) {
                return true;
            }
            if (!current.fields.containsKey(path)) {
                return false;
            }
            current = current.fields.get(path);
        }
        return true;
    }

    /**
     * 判断字段是否被显式排除（{@code -field} 语法）。
     *
     * @param field 待检查的字段名
     * @return 若字段被显式排除则为 {@code true}
     */
    public boolean excluded(String field) {
        if (fields.containsKey("-" + field)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 迭代当前层级应包含在响应中的子字段名。
     *
     * <p>对嵌套字段规范，对本迭代器返回的名称调用 {@link #child(String)}。</p>
     *
     * @return 子字段名迭代器
     */
    public Iterator<String> iterator() {
        return fields.keySet().iterator();
    }

    /**
     * 判断是否不返回任何字段。
     *
     * @return 字段集为空时为 {@code true}，否则为 {@code false}
     */
    public boolean isEmpty() {
        return this.fields.isEmpty();
    }

    /** 是否包含通配符 {@code *}（即返回全部字段）。 */
    public boolean isAll() {
        return this.fields.keySet().contains("*");
    }

    @Override
    public String toString() {
        return "[ReturnFieldsImpl: fields=" + this.fields + "]";
    }
}
