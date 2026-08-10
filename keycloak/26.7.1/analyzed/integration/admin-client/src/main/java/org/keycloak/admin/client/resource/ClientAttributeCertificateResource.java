/*
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.keycloak.admin.client.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.KeyStoreConfig;
import org.keycloak.representations.idm.CertificateRepresentation;

/**
 * 客户端属性证书与密钥对的管理 REST 资源。
 * <p>
 * 用于管理客户端 SAML/OIDC 签名或加密所需的 X.509 证书与密钥，
 * 支持查看、生成、上传证书以及导出密钥库文件。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public interface ClientAttributeCertificateResource {

    /**
     * 获取当前证书/密钥的元数据信息。
     *
     * @return 证书表示对象
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    CertificateRepresentation getKeyInfo();

    /**
     * 生成新的密钥对及自签名证书。
     *
     * @return 新生成的证书表示对象
     */
    @POST
    @Path("generate")
    @Produces(MediaType.APPLICATION_JSON)
    CertificateRepresentation generate();

    /**
     * 上传 JKS 密钥库（含证书及可选私钥）。
     *
     * @param output 多部分表单数据，包含密钥库文件
     * @return 上传后的证书表示对象
     */
    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    CertificateRepresentation uploadJks(Object output);

    /**
     * 仅上传公钥证书，不包含私钥。
     *
     * @param output 多部分表单数据，包含证书文件
     * @return 上传后的证书表示对象
     */
    @POST
    @Path("upload-certificate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    CertificateRepresentation uploadJksCertificate(Object output);

    /**
     * 下载包含私钥与公钥证书的客户端密钥库文件。
     * <p>
     * 配置参数 {@code keySize} 与 {@code validity} 自 Keycloak 26.3 起支持；
     * 默认密钥长度 4096、有效期 3 年。更早版本默认 2048 位、10 年有效期。
     *
     * @param config 密钥库配置 JSON
     * @return 密钥库二进制内容
     */
    @POST
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    byte[] getKeystore(final KeyStoreConfig config);

    /**
     * 生成新密钥对与证书，并返回含私钥的密钥库文件。
     * <p>
     * 仅将生成的公钥证书保存至 Keycloak 数据库，私钥不会持久化。
     *
     * @param config 密钥库配置 JSON
     * @return 含私钥的密钥库二进制内容
     */
    @POST
    @Path("/generate-and-download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    byte[] generateAndGetKeystore(final KeyStoreConfig config);
}
