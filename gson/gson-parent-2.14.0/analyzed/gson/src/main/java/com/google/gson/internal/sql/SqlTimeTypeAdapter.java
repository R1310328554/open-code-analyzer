/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.internal.sql;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * {@link java.sql.Time} 的类型适配器。表面无状态，但 {@link java.text.DateFormat} 在创建时捕获时区与 {@link java.util.Locale}，
 * 且非线程安全，故读写方法须同步。
 */
@SuppressWarnings("JavaUtilDate")
final class SqlTimeTypeAdapter extends TypeAdapter<Time> {
  static final TypeAdapterFactory FACTORY =
      new TypeAdapterFactory() {
        @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          return typeToken.getRawType() == Time.class
              ? (TypeAdapter<T>) new SqlTimeTypeAdapter()
              : null;
        }
      };

  private final DateFormat format = new SimpleDateFormat("hh:mm:ss a");

  private SqlTimeTypeAdapter() {}

  @Override
  public Time read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    String s = in.nextString();
    synchronized (this) {
      TimeZone originalTimeZone = format.getTimeZone(); // Save the original time zone
      try {
        Date date = format.parse(s);
        return new Time(date.getTime());
      } catch (ParseException e) {
        throw new JsonSyntaxException(
            "Failed parsing '" + s + "' as SQL Time; at path " + in.getPreviousPath(), e);
      } finally {
        format.setTimeZone(originalTimeZone); // Restore the original time zone
      }
    }
  }

  @Override
  public void write(JsonWriter out, Time value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    String timeString;
    synchronized (this) {
      timeString = format.format(value);
    }
    out.value(timeString);
  }
}
