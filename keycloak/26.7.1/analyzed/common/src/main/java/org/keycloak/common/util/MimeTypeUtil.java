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

import java.io.File;

import jakarta.activation.MimetypesFileTypeMap;

/**
 * 根据文件扩展名推断 MIME 类型，扩展 Jakarta Activation 默认映射。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class MimeTypeUtil {

    private static MimetypesFileTypeMap map = new MimetypesFileTypeMap();
    static {
        // 补充静态资源常用 MIME 类型
        map.addMimeTypes("text/css css CSS");
        map.addMimeTypes("text/javascript js JS");
        map.addMimeTypes("application/json json JSON");
        map.addMimeTypes("image/png png PNG");
        map.addMimeTypes("image/svg+xml svg SVG");
        map.addMimeTypes("text/html html htm HTML HTM");
        map.addMimeTypes("application/wasm wasm WASM");
    }

    /** 根据 {@link File} 推断 Content-Type。 */
    public static String getContentType(File file) {
        return map.getContentType(file);
    }

    /** 根据路径/文件名推断 Content-Type。 */
    public static String getContentType(String path) {
        return map.getContentType(path);
    }

}
