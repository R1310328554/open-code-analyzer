/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

/**
 * 用户搜索查询解析工具。
 * <p>支持 {@code id:}、{@code username:}、{@code email:} 前缀精确查找，以及 {@code field:value} 键值对查询语法。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class SearchQueryUtils {

    /** 按用户 ID 搜索的前缀。 */
    public static final String SEARCH_ID_PREFIX = "id:";

    /** 按用户名搜索的前缀。 */
    public static final String SEARCH_USERNAME_PREFIX = "username:";

    /** 按邮箱搜索的前缀。 */
    public static final String SEARCH_EMAIL_PREFIX = "email:";

    /** 空白字符分隔符，用于拆分前缀搜索词。 */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** 用户搜索前缀枚举，绑定前缀字符串与 {@link UserProvider} 查找方法。 */
    public enum UserSearchPrefix {
        ID(SEARCH_ID_PREFIX, UserProvider::getUserById),
        USERNAME(SEARCH_USERNAME_PREFIX, UserProvider::getUserByUsername),
        EMAIL(SEARCH_EMAIL_PREFIX, UserProvider::getUserByEmail);

        private final String prefix;
        private final UserLookup lookup;

        UserSearchPrefix(String prefix, UserLookup lookup) {
            this.prefix = prefix;
            this.lookup = lookup;
        }

        /** @return 搜索前缀字符串 */
        public String getPrefix() {
            return prefix;
        }

        /** 使用绑定的查找方法检索用户。 */
        public UserModel lookup(UserProvider users, RealmModel realm, String term) {
            return lookup.apply(users, realm, term);
        }

        /** 去掉前缀后按空白拆分搜索词。 */
        public String[] splitTerms(String search) {
            return WHITESPACE.split(search.substring(prefix.length()).trim());
        }

        /** 根据搜索串前缀匹配对应枚举值，无匹配返回 null。 */
        public static UserSearchPrefix matching(String search) {
            for (UserSearchPrefix p : values()) {
                if (search.startsWith(p.prefix)) {
                    return p;
                }
            }
            return null;
        }

        /** 用户查找函数式接口。 */
        @FunctionalInterface
        private interface UserLookup {
            UserModel apply(UserProvider users, RealmModel realm, String term);
        }
    }

    /**
     * 解析 {@code field:value} 键值对查询串为 Map。
     * <p>支持引号转义与反斜杠转义。</p>
     *
     * @param query 查询字符串
     * @return 字段名 → 值 映射
     */
    public static Map<String, String> getFields(final String query) {
        Map<String, String> ret = new HashMap<>();
        char[] chars = query.trim().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            boolean inQuotes = false;
            boolean internal = false;
            String name = "";
            while (i < chars.length && chars[i] != ':') {
                if (chars[i] == '\\') {
                    if (chars[i+1] == '\"') {
                        i++;
                    }
                    else if (chars[i+1] == '\\') {
                        i+=2;
                        continue;
                    }
                }
                else if (chars[i] == '\"') {
                        if(!inQuotes && name.length() > 0) {
                            internal = true;
                        }
                        else if(internal) {
                            internal = false;
                        }
                        else {
                            inQuotes = !inQuotes;
                            i++;
                            continue;
                        }
                }
                else if(chars[i] == ' ' && !inQuotes) {
                    break;
                }
                name += chars[i];
                i++;
            }
            if(i == chars.length || chars[i] == ' ') {
                continue;
            }
            i++;
            inQuotes = false;
            internal = false;
            String value = "";
            while (i < chars.length) {
                if (chars[i] == '\\') {
                    if (chars[i+1] == '\"') {
                        i++;
                    }
                    else if (chars[i+1] == '\\') {
                        i+=2;
                        continue;
                    }
                }
                else if (chars[i] == '\"') {
                    if(!inQuotes && value.length() > 0) {
                        internal = true;
                    }
                    else if(internal) {
                        internal = false;
                    }
                    else {
                        inQuotes = !inQuotes;
                        i++;
                        continue;
                    }
                }
                else if(chars[i] == ' ' && !inQuotes) {
                    break;
                }
                value += chars[i];
                i++;
            }
            ret.put(name, value);
        }
        return ret;
    }
}
