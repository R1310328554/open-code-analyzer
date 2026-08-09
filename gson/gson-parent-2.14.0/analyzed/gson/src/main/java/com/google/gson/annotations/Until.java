/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson.annotations;

import com.google.gson.GsonBuilder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注成员或类型应存在至某版本（不含）；Gson 版本 ≥ Until 值时排除该字段。
 * 须 {@link GsonBuilder#setVersion(double)} 才生效。
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @see GsonBuilder#setVersion(double)
 * @see Since
 * @since 1.3
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Until {
  /** 存在至该版本（不含）：{@code gsonVersion < value} */
  double value();
}
