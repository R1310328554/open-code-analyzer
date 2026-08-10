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
package org.keycloak.admin.client;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * 解析 JAX-RS 创建响应的工具类，从 {@code Location} 头提取新资源的 ID。
 *
 * @author John D. Ament
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class CreatedResponseUtil {
    /**
     * 读取响应并确认状态为 201 Created，然后从 Location URI 的最后一段解析资源 ID。
     *
     * @param response 收到的 JAX-RS 响应
     * @return Location URI 路径末尾的 ID 字符串；无 Location 时返回 {@code null}
     * @throws WebApplicationException 若响应状态不是 201 Created
     */
    public static String getCreatedId(Response response) throws WebApplicationException {
        URI location = response.getLocation();
        if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
            Response.StatusType statusInfo = response.getStatusInfo();
            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            String errorMessage = "Create method returned status " +
                                  statusInfo.getReasonPhrase() + " (Code: " + statusInfo.getStatusCode() + "); " +
                                  "expected status: Created (201).";
            try {
                if (matches(MediaType.APPLICATION_JSON_TYPE, MediaType.valueOf(contentType))) {
                    // 尝试将服务端错误信息附加到异常消息中
                    @SuppressWarnings("raw")
                    Map responseBody = response.readEntity(Map.class);
                    if (responseBody != null) {
                        if (responseBody.containsKey("errorMessage")) {
                            errorMessage += " ErrorMessage: " + responseBody.get("errorMessage");
                        }
                        if (responseBody.containsKey("error")) {
                            errorMessage += " Error: " + responseBody.get("error");
                        }
                    }
                }
            } catch(Exception ignored){
                // 解析响应体失败时忽略
            }

            throw new WebApplicationException(errorMessage, response);
        }
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /** 比较两个 MediaType 的类型与子类型是否一致（忽略大小写）。 */
    private static boolean matches(MediaType a, MediaType b) {
        if (a == null) {
            return b == null;
        } else if (b == null) return false;

        return a.getType().equalsIgnoreCase(b.getType()) && a.getSubtype().equalsIgnoreCase(b.getSubtype());
    }
}
