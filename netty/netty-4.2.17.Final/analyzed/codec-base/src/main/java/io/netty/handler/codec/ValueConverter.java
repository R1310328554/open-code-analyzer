/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec;

/**
 * 在通用 Java 类型与协议字段类型 {@code T} 之间双向转换。
 */
public interface ValueConverter<T> {
    /** 将任意对象转为 {@code T}。 */
    T convertObject(Object value);

    /** 将 {@code boolean} 转为 {@code T}。 */
    T convertBoolean(boolean value);

    /** 将 {@code T} 转为 {@code boolean}。 */
    boolean convertToBoolean(T value);

    /** 将 {@code byte} 转为 {@code T}。 */
    T convertByte(byte value);

    /** 将 {@code T} 转为 {@code byte}。 */
    byte convertToByte(T value);

    T convertChar(char value);

    char convertToChar(T value);

    T convertShort(short value);

    short convertToShort(T value);

    T convertInt(int value);

    int convertToInt(T value);

    T convertLong(long value);

    long convertToLong(T value);

    /** 将毫秒时间戳转为 {@code T}。 */
    T convertTimeMillis(long value);

    /** 将 {@code T} 转为毫秒时间戳。 */
    long convertToTimeMillis(T value);

    T convertFloat(float value);

    float convertToFloat(T value);

    T convertDouble(double value);

    double convertToDouble(T value);
}
