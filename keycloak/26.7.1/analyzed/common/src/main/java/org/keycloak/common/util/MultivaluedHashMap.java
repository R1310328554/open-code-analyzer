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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link MultivaluedMap} 的 {@link HashMap} 实现，键对应值列表。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@SuppressWarnings("serial")
public class MultivaluedHashMap<K, V> extends HashMap<K, List<V>> implements MultivaluedMap<K, V>
{
   /** 空的多值映射。 */
   public MultivaluedHashMap() {
   }

   /** 从普通 {@code Map<K, List<V>>} 拷贝构造。 */
   public MultivaluedHashMap(Map<K, List<V>> map) {
      if (map == null) {
         throw new IllegalArgumentException("Map can not be null");
      }
      putAll(map);
   }


   /** 从另一个多值映射合并拷贝。 */
   public MultivaluedHashMap(MultivaluedHashMap<K, V> config) {
      addAll(config);
   }
}
