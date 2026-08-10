/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.xml;

/**
 * XML processing instruction
 *
 * <p>XML 处理指令（PI）事件，对应 {@code <?target data?>} 语法。
 * {@code target} 为指令目标（如 {@code xml-stylesheet}），{@code data} 为剩余指令体。</p>
 */
public class XmlProcessingInstruction {

    /** 处理指令体（target 之后的文本）。 */
    private final String data;
    /** 处理指令目标名。 */
    private final String target;

    public XmlProcessingInstruction(String data, String target) {
        this.data = data;
        this.target = target;
    }

    /** 返回指令体内容。 */
    public String data() {
        return data;
    }

    /** 返回指令目标。 */
    public String target() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlProcessingInstruction that = (XmlProcessingInstruction) o;

        if (data != null ? !data.equals(that.data) : that.data != null) {
            return false;
        }
        if (target != null ? !target.equals(that.target) : that.target != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XmlProcessingInstruction{" +
                "data='" + data + '\'' +
                ", target='" + target + '\'' +
                '}';
    }

}
