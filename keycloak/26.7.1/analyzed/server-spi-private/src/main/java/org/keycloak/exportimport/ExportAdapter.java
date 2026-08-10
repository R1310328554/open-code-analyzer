/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.exportimport;

import java.io.IOException;

/**
 * 导出适配器：解耦导出逻辑与向调用方交付数据的 API（HTTP 响应、文件流等）。
 *
 * @author Alexander Schwartz
 */
public interface ExportAdapter {
    /**
     * 设置导出内容的 MIME 类型。
     *
     * @param mediaType MIME 类型
     */
    void setType(String mediaType);

    /**
     * 将导出数据写入输出流；写入完成后由实现关闭流。
     *
     * @param consumer 接收 {@link java.io.OutputStream} 的消费者
     */
    void writeToOutputStream(ConsumerOfOutputStream consumer);

    /**
     * 可抛出 {@link IOException} 的输出流消费者（写入流时常见）。
     */
    @FunctionalInterface
    interface ConsumerOfOutputStream {
        void accept(java.io.OutputStream t) throws IOException;
    }
}
