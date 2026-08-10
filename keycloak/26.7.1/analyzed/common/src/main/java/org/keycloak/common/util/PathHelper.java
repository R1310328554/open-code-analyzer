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

package org.keycloak.common.util;

import java.util.regex.Pattern;


/**
 * URI 模板参数工具：Java 正则不支持命名组，此处用捕获组模拟。
 *
 * @author Ryan J. McDonough
 * @author Bill Burke
 * @since 1.0
 *        Nov 8, 2006
 */
public class PathHelper
{
   /** 模板参数名：字母/数字/点/连字符，首字符为字。 */
   public static final String URI_PARAM_NAME_REGEX = "\\w[\\w\\.-]*";
   public static final String URI_PARAM_REGEX_REGEX = "[^{}][^{}]*";
   public static final String URI_PARAM_REGEX = "\\{\\s*(" + URI_PARAM_NAME_REGEX + ")\\s*(:\\s*(" + URI_PARAM_REGEX_REGEX + "))?\\}";
   public static final Pattern URI_PARAM_PATTERN = Pattern.compile(URI_PARAM_REGEX);

   /** 匹配 {@code {*}} 形式 URI 模板占位符的正则。 */
   public static final Pattern URI_TEMPLATE_PATTERN = Pattern.compile("(\\{([^{}]+)\\})");

   public static final char openCurlyReplacement = 6;
   public static final char closeCurlyReplacement = 7;

   /** 嵌套花括号占位符：内层 {@code {} } 替换为特殊字符以便正则匹配。 */
   public static String replaceEnclosedCurlyBraces(String str)
   {
      char[] chars = str.toCharArray();
      int open = 0;
      for (int i = 0; i < chars.length; i++)
      {
         if (chars[i] == '{')
         {
            if (open != 0) chars[i] = openCurlyReplacement;
            open++;
         }
         else if (chars[i] == '}')
         {
            open--;
            if (open != 0)
            {
               chars[i] = closeCurlyReplacement;
            }
         }
      }
      return new String(chars);
   }

   /** 将 {@link #replaceEnclosedCurlyBraces} 的占位符还原为花括号。 */
   public static String recoverEnclosedCurlyBraces(String str)
   {
      return str.replace(openCurlyReplacement, '{').replace(closeCurlyReplacement, '}');
   }

}