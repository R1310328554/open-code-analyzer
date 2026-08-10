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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URI 各组件（路径、查询、片段、用户信息等）的 RFC 3986 编码/解码工具。
 *
 * <p>源自 RESTEasy，支持保留已有百分号编码与 URI 模板参数 {@code {x}}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Encode
{
   private static final String UTF_8 = StandardCharsets.UTF_8.name();

   private static final Pattern PARAM_REPLACEMENT = Pattern.compile("_resteasy_uri_parameter");

   private static final String[] pathEncoding = new String[128];
   private static final String[] pathSegmentEncoding = new String[128];
   private static final String[] matrixParameterEncoding = new String[128];
   private static final String[] queryNameValueEncoding = new String[128];
   private static final String[] queryStringEncoding = new String[128];
   private static final String[] userInfoStringEncoding = new String[128];

   static
   {
      /*
       * 按 <a href="http://ietf.org/rfc/rfc3986.txt">RFC 3986</a> 编码，允许 PCHAR 与 '/'
       *
       * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
       * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                     / "*" / "+" / "," / ";" / "="
       * pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
       *
       */
      for (int i = 0; i < 128; i++)
      {
         if (i >= 'a' && i <= 'z') continue;
         if (i >= 'A' && i <= 'Z') continue;
         if (i >= '0' && i <= '9') continue;
         switch ((char) i)
         {
            case '-':
            case '.':
            case '_':
            case '~':
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '/':
            case ';':
            case '=':
            case ':':
            case '@':
               continue;
         }
         try {
            pathEncoding[i] = URLEncoder.encode(String.valueOf((char) i), UTF_8);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
         }
      }
      pathEncoding[' '] = "%20";
      System.arraycopy(pathEncoding, 0, matrixParameterEncoding, 0, pathEncoding.length);
      matrixParameterEncoding[';'] = "%3B";
      matrixParameterEncoding['='] = "%3D";
      matrixParameterEncoding['/'] = "%2F"; // RESTEASY-729
      System.arraycopy(pathEncoding, 0, pathSegmentEncoding, 0, pathEncoding.length);
      pathSegmentEncoding['/'] = "%2F";
      /*
       * Encode via <a href="http://ietf.org/rfc/rfc3986.txt">RFC 3986</a>.
       *
       * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
       * space encoded as '+'
       *
       */
      for (int i = 0; i < 128; i++)
      {
         if (i >= 'a' && i <= 'z') continue;
         if (i >= 'A' && i <= 'Z') continue;
         if (i >= '0' && i <= '9') continue;
         switch ((char) i)
         {
            case '-':
            case '.':
            case '_':
            case '~':
            case '?':
               continue;
            case ' ':
               queryNameValueEncoding[i] = "+";
               continue;
         }
         try {
            queryNameValueEncoding[i] = URLEncoder.encode(String.valueOf((char) i), UTF_8);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
         }
      }

      /*
       * query       = *( pchar / "/" / "?" )

       */
      for (int i = 0; i < 128; i++)
      {
         if (i >= 'a' && i <= 'z') continue;
         if (i >= 'A' && i <= 'Z') continue;
         if (i >= '0' && i <= '9') continue;
         switch ((char) i)
         {
            case '-':
            case '.':
            case '_':
            case '~':
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
            case ':':
            case '@':
            case '?':
            case '/':
               continue;
            case ' ':
               queryStringEncoding[i] = "%20";
               continue;
         }
         try {
            queryStringEncoding[i] = URLEncoder.encode(String.valueOf((char) i), UTF_8);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
         }
      }

     /*
      * userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
      * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
      * pct-encoded   = "%" HEXDIG HEXDIG
      * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
      *                   / "*" / "+" / "," / ";" / "="
      */
      for (int i = 0; i < 128; i++)
      {
         if (i >= 'a' && i <= 'z') continue;
         if (i >= 'A' && i <= 'Z') continue;
         if (i >= '0' && i <= '9') continue;
         switch ((char) i)
         {
            case '-':
            case '.':
            case '_':
            case '~':
            case '!':
            case '$':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case ';':
            case '=':
            case ':':
               continue;
            case ' ':
               userInfoStringEncoding[i] = "%20";
               continue;
         }
         userInfoStringEncoding[i] = URLEncoder.encode(String.valueOf((char) i));
      }
   }

   /**
    * 保留已有 "%..." 编码与 URI 模板参数。
    */
   public static String encodeQueryString(String value)
   {
      return encodeValue(value, queryStringEncoding);
   }

   /**
    * 保留 "%..." 编码，但不保留模板参数。
    * @param value
    * @return
    */
   public static String encodeQueryStringNotTemplateParameters(String value) {
      return encodeNonCodes(encodeFromArray(value, queryStringEncoding, false));
   }

   /**
    * 保留已有 "%..." 编码与 URI 模板参数。
    * @param value The user-info value to encode
    * @return The user-info encoded
    */
   public static String encodeUserInfo(String value) {
      return encodeValue(value, userInfoStringEncoding);
   }

   /**
    * 保留 "%..." 编码，但不保留模板参数。
    * @param value The user-info to encode
    * @return The user-info encoded
    */
   public static String encodeUserInfoNotTemplateParameters(String value) {
      return encodeNonCodes(encodeFromArray(value, userInfoStringEncoding, false));
   }

   /**
    * 保留 "%..." 编码、矩阵参数、模板参数与 '/' 字符。
    */
   public static String encodePath(String value)
   {
      return encodeValue(value, pathEncoding);
   }

   /**
    * 保留 "%..." 编码、矩阵参数与模板参数。
    */
   public static String encodePathSegment(String value)
   {
      return encodeValue(value, pathSegmentEncoding);
   }

   /**
    * 保留已有 "%..." 编码与 URI 模板参数。
    */
   public static String encodeFragment(String value)
   {
      return encodeValue(value, queryStringEncoding);
   }

   /**
    * 保留 "%..." 编码，但不保留模板参数。
    * @param value
    * @return
    */
   public static String encodeFragmentNotTemplateParameters(String value)
   {
      return encodeNonCodes(encodeFromArray(value, queryStringEncoding, false));
   }

   /**
    * 保留已有 "%..." 编码与 URI 模板参数。
    */
   public static String encodeMatrixParam(String value)
   {
      return encodeValue(value, matrixParameterEncoding);
   }

   /**
    * 保留已有 "%..." 编码与 URI 模板参数。
    */
   public static String encodeQueryParam(String value)
   {
      return encodeValue(value, queryNameValueEncoding);
   }

   //private static final Pattern nonCodes = Pattern.compile("%([^a-fA-F0-9]|$)");
   private static final Pattern nonCodes = Pattern.compile("%([^a-fA-F0-9]|[a-fA-F0-9]$|$|[a-fA-F0-9][^a-fA-F0-9])");
   private static final Pattern encodedChars = Pattern.compile("%([a-fA-F0-9][a-fA-F0-9])");
   private static final Pattern encodedCharsMulti = Pattern.compile("((%[a-fA-F0-9][a-fA-F0-9])+)");

   public static String decodePath(String path)
   {
      Matcher matcher = encodedCharsMulti.matcher(path);
      int start=0;
      StringBuilder builder = new StringBuilder();
      CharsetDecoder decoder = Charset.forName(UTF_8).newDecoder();
      while (matcher.find())
      {
    	 builder.append(path, start, matcher.start());
         decoder.reset();
         String decoded = decodeBytes(matcher.group(1), decoder);
         builder.append(decoded);
         start = matcher.end();
      }
      builder.append(path, start, path.length());
      return builder.toString();
   }

   private static String decodeBytes(String enc, CharsetDecoder decoder)
   {
      Matcher matcher = encodedChars.matcher(enc);
      ByteBuffer bytes = ByteBuffer.allocate(enc.length() / 3);
      while (matcher.find())
      {
         int b = Integer.parseInt(matcher.group(1), 16);
         bytes.put((byte) b);
      }
      bytes.flip();
      try
      {
         return decoder.decode(bytes).toString();
      }
      catch (CharacterCodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * 若 '%' 非合法编码序列则将其编码为 %25
    *
    * @param string
    * @return
    */
   public static String encodeNonCodes(String string)
   {
      Matcher matcher = nonCodes.matcher(string);
      StringBuilder builder = new StringBuilder();


      // 不使用无参 matcher.find()，避免漏编码第二个 %
      //      coupled with matcher.appendReplacement()
      //      because the matched text may contain
      //      a second % and we must make sure we
      //      encode it (if necessary).
      int idx = 0;
      while (matcher.find(idx))
      {
         int start = matcher.start();
         builder.append(string.substring(idx, start));
         builder.append("%25");
         idx = start + 1;
      }
      builder.append(string.substring(idx));
      return builder.toString();
   }

   public static boolean savePathParams(String segment, StringBuilder newSegment, List<String> params)
   {
      boolean foundParam = false;
      // 正则中可能含 '{' '}'，先替换以便匹配
      segment = PathHelper.replaceEnclosedCurlyBraces(segment);
      Matcher matcher = PathHelper.URI_TEMPLATE_PATTERN.matcher(segment);
      int start = 0;
      while (matcher.find())
      {
    	 newSegment.append(segment, start, matcher.start());
         foundParam = true;
         String group = matcher.group();
         // 恢复先前替换的大括号
         params.add(PathHelper.recoverEnclosedCurlyBraces(group));
         newSegment.append("_resteasy_uri_parameter");
         start = matcher.end();
      }
      newSegment.append(segment, start, segment.length());
      return foundParam;
   }

   /**
    * 保留 "%..." 编码与模板参数 "{x}"
    *
    * @param segment
    * @param encoding
    * @return
    */
   public static String encodeValue(String segment, String[] encoding)
   {
      ArrayList<String> params = new ArrayList<String>();
      boolean foundParam = false;
      StringBuilder newSegment = new StringBuilder();
      if (savePathParams(segment, newSegment, params))
      {
         foundParam = true;
         segment = newSegment.toString();
      }
      String result = encodeFromArray(segment, encoding, false);
      result = encodeNonCodes(result);
      segment = result;
      if (foundParam)
      {
         segment = pathParamReplacement(segment, params);
      }
      return segment;
   }

   /**
    * 按 <a href="http://ietf.org/rfc/rfc3986.txt">RFC 3986</a> 编码，允许 PCHAR 与 '/'
    * <p/>
    * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
    * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
    * / "*" / "+" / "," / ";" / "="
    * pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
    */
   public static String encodePathAsIs(String segment)
   {
      return encodeFromArray(segment, pathEncoding, true);
   }

   /**
    * 保留合法编码（如 %2D），非法序列（如 %p）会被重新编码
    *
    * @param segment
    * @return
    */
   public static String encodePathSaveEncodings(String segment)
   {
      String result = encodeFromArray(segment, pathEncoding, false);
      result = encodeNonCodes(result);
      return result;
   }

   /**
    * 按 <a href="http://ietf.org/rfc/rfc3986.txt">RFC 3986</a> 编码，允许 PCHAR 与 '/'
    * <p/>
    * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
    * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
    * / "*" / "+" / "," / ";" / "="
    * pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
    */
   public static String encodePathSegmentAsIs(String segment)
   {
      return encodeFromArray(segment, pathSegmentEncoding, true);
   }

   /**
    * 保留合法编码（如 %2D），非法序列（如 %p）会被重新编码
    *
    * @param segment
    * @return
    */
   public static String encodePathSegmentSaveEncodings(String segment)
   {
      String result = encodeFromArray(segment, pathSegmentEncoding, false);
      result = encodeNonCodes(result);
      return result;
   }


   /**
    * 对查询参数名或值进行完整编码。
    *
    * @param nameOrValue
    * @return
    */
   public static String encodeQueryParamAsIs(String nameOrValue)
   {
      return encodeFromArray(nameOrValue, queryNameValueEncoding, true);
   }

   /**
    * 保留合法编码（如 %2D），非法序列（如 %p）会被重新编码
    *
    * @param segment
    * @return
    */
   public static String encodeQueryParamSaveEncodings(String segment)
   {
      String result = encodeFromArray(segment, queryNameValueEncoding, false);
      result = encodeNonCodes(result);
      return result;
   }

   /**
    * 对用户信息部分进行完整编码。
    *
    * @param nameOrValue
    * @return
    */
   public static String encodeUserInfoAsIs(String nameOrValue)
   {
      return encodeFromArray(nameOrValue, userInfoStringEncoding, true);
   }

   /**
    * 保留合法编码（如 %2D），非法序列（如 %p）会被重新编码
    *
    * @param segment
    * @return
    */
   public static String encodeUserInfoSaveEncodings(String segment)
   {
      String result = encodeFromArray(segment, userInfoStringEncoding, false);
      result = encodeNonCodes(result);
      return result;
   }

   public static String encodeFragmentAsIs(String nameOrValue)
   {
      return encodeFromArray(nameOrValue, queryNameValueEncoding, true);
   }

   protected static String encodeFromArray(String segment, String[] encodingMap, boolean encodePercent)
   {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < segment.length(); i++)
      {
    	 char currentChar = segment.charAt(i);
         if (!encodePercent && currentChar == '%')
         {
            result.append(currentChar);
            continue;
         }
         String encoding = encode(currentChar, encodingMap);
         if (encoding == null)
         {
            result.append(currentChar);
         }
         else
         {
            result.append(encoding);
         }
      }
      return result.toString();
   }

   /**
    * @param zhar        integer representation of character
    * @param encodingMap encoding map
    * @return URL 编码后的字符
    */
   private static String encode(int zhar, String[] encodingMap)
   {
      String encoded;
      if (zhar < encodingMap.length)
      {
         encoded = encodingMap[zhar];
      }
      else
      {
         try
         {
            encoded = URLEncoder.encode(Character.toString((char) zhar), UTF_8);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new RuntimeException(e);
         }
      }
      return encoded;
   }

   public static String pathParamReplacement(String segment, List<String> params)
   {
      StringBuilder newSegment = new StringBuilder();
      Matcher matcher = PARAM_REPLACEMENT.matcher(segment);
      int i = 0;
      int start = 0;
      while (matcher.find())
      {
    	 newSegment.append(segment, start, matcher.start());
         String replacement = params.get(i++);
     	 newSegment.append(replacement);
		 start = matcher.end();
      }
  	  newSegment.append(segment, start, segment.length());
      segment = newSegment.toString();
      return segment;
   }

   /**
    * 解码已 URL 编码的多值映射
    *
    * @param map
    * @return
    */
   public static MultivaluedHashMap<String, String> decode(MultivaluedHashMap<String, String> map)
   {
       MultivaluedHashMap<String, String> decoded = new MultivaluedHashMap<String, String>();
      for (Map.Entry<String, List<String>> entry : map.entrySet())
      {
         List<String> values = entry.getValue();
         for (String value : values)
         {
            try
            {
               decoded.add(URLDecoder.decode(entry.getKey(), UTF_8), URLDecoder.decode(value, UTF_8));
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      return decoded;
   }
   
   /**
    * 解码已 URL 编码的多值映射
    *
    * @param map
    * @param charset
    * @return
    */
   public static MultivaluedHashMap<String, String> decode(MultivaluedHashMap<String, String> map, String charset)
   {
      if (charset == null)
      {
         charset = UTF_8;
      }
      MultivaluedHashMap<String, String> decoded = new MultivaluedHashMap<String, String>();
      for (Map.Entry<String, List<String>> entry : map.entrySet())
      {
         List<String> values = entry.getValue();
         for (String value : values)
         {
            try
            {
               decoded.add(URLDecoder.decode(entry.getKey(), charset), URLDecoder.decode(value, charset));
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      return decoded;
   }

   public static MultivaluedHashMap<String, String> encode(MultivaluedHashMap<String, String> map)
   {
       MultivaluedHashMap<String, String> decoded = new MultivaluedHashMap<String, String>();
      for (Map.Entry<String, List<String>> entry : map.entrySet())
      {
         List<String> values = entry.getValue();
         for (String value : values)
         {
            try
            {
               decoded.add(URLEncoder.encode(entry.getKey(), UTF_8), URLEncoder.encode(value, UTF_8));
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      return decoded;
   }

   public static String decode(String string)
   {
      try
      {
         return URLDecoder.decode(string, UTF_8);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }


   /**
    * @param string
    * @return URL 编码结果
    */
   public static String urlEncode(String string) {
      try {
         return URLEncoder.encode(string, UTF_8);
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * @param string
    * @return URL 解码结果
    */
   public static String urlDecode(String string) {
      return decode(string);
   }

}
