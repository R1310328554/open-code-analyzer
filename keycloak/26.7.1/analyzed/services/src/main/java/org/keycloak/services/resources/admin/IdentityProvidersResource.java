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

package org.keycloak.services.resources.admin;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.util.PemUtils;
import org.keycloak.common.util.StreamUtil;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.http.FormPartValue;
import org.keycloak.models.IdentityProviderCapability;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.models.utils.StripSecretsUtils;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.CertificateRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.util.CertificateInfoHelper;
import org.keycloak.utils.ReservedCharValidator;
import org.keycloak.utils.StringUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * 身份提供者（Identity Provider）集合 REST 资源。
 * <p>导入配置、上传证书、列出/创建 IdP 实例，并路由到单个 IdP 子资源。</p>
 *
 * @resource Identity Providers
 * @author Pedro Igor
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class IdentityProvidersResource {

    /** 当前领域 */
    private final RealmModel realm;
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** 构造身份提供者集合资源。
     * @param realm 当前领域
     * @param session Keycloak 会话
     * @param auth 权限评估器
     * @param adminEvent 管理事件构建器
     */
    public IdentityProvidersResource(RealmModel realm, KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.realm = realm;
        this.session = session;
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.IDENTITY_PROVIDER);
    }

    /**
     * 按 provider ID 获取身份提供者工厂。
     * @param providerId 提供者 ID
     * @return 工厂实例
     */
    @Path("/providers/{provider_id}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.IDENTITY_PROVIDERS)
    @Operation( summary = "Get the identity provider factory for that provider id")
    public IdentityProviderFactory getIdentityProviderFactory(@Parameter(description = "The provider id to get the factory") @PathParam("provider_id") String providerId) {
        this.auth.realm().requireViewIdentityProviders();
        IdentityProviderFactory providerFactory = getProviderFactoryById(providerId);
        if (providerFactory != null) {
            return providerFactory;
        }
        throw new BadRequestException();
    }

    /** 从上传的 JSON 文件导入身份提供者配置 */
    @POST
    @Path("import-config")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.IDENTITY_PROVIDERS)
    @Operation( description = "Import identity provider from uploaded JSON file")
    public Map<String, String> importFrom() throws IOException {
        this.auth.realm().requireManageIdentityProviders();
        MultivaluedMap<String, FormPartValue> formDataMap = session.getContext().getHttpRequest().getMultiPartFormParameters();
        if (!(formDataMap.containsKey("providerId") && formDataMap.containsKey("file"))) {
            throw new BadRequestException();
        }
        String providerId = formDataMap.getFirst("providerId").asString();
        String config = StreamUtil.readString(formDataMap.getFirst("file").asInputStream());
        IdentityProviderFactory<?> providerFactory = getProviderFactoryById(providerId);
        return providerFactory.parseConfig(session, config);
    }

    /**
     * 上传证书/JWKS/公钥并返回证书表示。
     * @return 证书表示
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_ATTRIBUTE_CERTIFICATE)
    @Operation( summary = "Uploads a certificate, prepares the jwks or public key associated, and returns the certificate representation.")
    @Path("upload-certificate")
    public CertificateRepresentation uploadCertificate() throws IOException {
        auth.realm().requireManageIdentityProviders();
        try {
            CertificateRepresentation info = CertificateInfoHelper.getCertificateFromRequest(session);
            if (info.getJwks() != null || info.getPublicKey() != null) {
                // 上传的是 JWKS 或公钥
                return info;
            } else if (info.getCertificate() != null) {
                // 从证书文件提取公钥
                X509Certificate certificate = KeycloakModelUtils.getCertificate(info.getCertificate());
                String pubKeyPem = PemUtils.encodeKey(certificate.getPublicKey());
                info.setPublicKey(pubKeyPem);
                return info;
            } else {
                throw new ErrorResponseException("certificate-not-found", "Invalid certificate/key in file", Response.Status.BAD_REQUEST);
            }
        } catch (IllegalStateException ise) {
            throw new ErrorResponseException("certificate-not-found", "Certificate or key error loding from uploaded file", Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 从远程 URL 拉取元数据并解析 IdP 配置。
     * @param data 含 providerId 与 fromUrl 的 JSON
     * @return 解析后的配置映射
     * @throws IOException 网络或解析失败
     */
    @POST
    @Path("import-config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.IDENTITY_PROVIDERS)
    @Operation( summary = "Import identity provider from JSON body")
    public Map<String, String> importFrom(@Parameter(description = "JSON body") Map<String, Object> data) throws IOException {
        this.auth.realm().requireManageIdentityProviders();
        if (data == null || !(data.containsKey("providerId") && data.containsKey("fromUrl"))) {
            throw new BadRequestException();
        }

        ReservedCharValidator.validateNoSpace((String)data.get("alias"));

        String providerId = data.get("providerId").toString();
        String from = data.get("fromUrl").toString();
        String file = session.getProvider(HttpClientProvider.class).getString(from);
        IdentityProviderFactory providerFactory = getProviderFactoryById(providerId);
        Map<String, String> config = providerFactory.parseConfig(session, file);
        // 按需写入元数据描述符 URL
        config.put(IdentityProviderModel.METADATA_DESCRIPTOR_URL, from);
        return config;
    }

    /**
     * 分页列出身份提供者实例（支持类型/能力/名称/仅领域级过滤）。
     * @param search 名称搜索（前缀/包含/精确）
     * @param briefRepresentation 是否简要表示
     * @param firstResult 分页偏移
     * @param maxResults 最大条数
     * @return IdP 表示流
     */
    @GET
    @Path("instances")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.IDENTITY_PROVIDERS)
    @Operation(summary = "List identity providers")
    public Stream<IdentityProviderRepresentation> getIdentityProviders(
            @Parameter(description = "Filter by identity providers type") @QueryParam("type") String type,
            @Parameter(description = "Filter by identity providers capability") @QueryParam("capability") String capability,
            @Parameter(description = "Filter specific providers by name. Search can be prefix (name*), contains (*name*) or exact (\"name\"). Default prefixed.") @QueryParam("search") String search,
            @Parameter(description = "Boolean which defines whether brief representations are returned (default: false)") @QueryParam("briefRepresentation") Boolean briefRepresentation,
            @Parameter(description = "Pagination offset") @QueryParam("first") Integer firstResult,
            @Parameter(description = "Maximum results size (defaults to 100)") @QueryParam("max") Integer maxResults,
            @Parameter(description = "Boolean which defines if only realm-level IDPs (not associated with orgs) should be returned (default: false)") @QueryParam("realmOnly") Boolean realmOnly) {
        this.auth.realm().requireViewIdentityProviders();

        if (maxResults == null) {
            maxResults = 100; // 默认最多 100 条
        }

        Function<IdentityProviderModel, IdentityProviderRepresentation> toRepresentation = Optional.ofNullable(briefRepresentation).orElse(false)
                ? m -> ModelToRepresentation.toBriefRepresentation(realm, m)
                : m -> StripSecretsUtils.stripSecrets(session, ModelToRepresentation.toRepresentation(session, realm, m));

        boolean searchRealmOnlyIDPs = Optional.ofNullable(realmOnly).orElse(false);

        IdentityProviderQuery query;
        if (type != null) {
            query = IdentityProviderQuery.type(IdentityProviderType.valueOf(type));
        } else if (capability != null) {
            query = IdentityProviderQuery.capability(IdentityProviderCapability.valueOf(capability));
        } else {
            query = IdentityProviderQuery.any();
        }

        if (StringUtil.isNotBlank(search)) {
            query.with(IdentityProviderModel.SEARCH, search);
        }
        if (searchRealmOnlyIDPs) {
            query.with(IdentityProviderModel.ORGANIZATION_ID, null);
        }

        return session.identityProviders().getAllStream(query, firstResult, maxResults).map(toRepresentation);
    }

    /**
     * 创建新身份提供者。
     * @param representation IdP 表示
     * @return 201 Created
     */
    @POST
    @Path("instances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.IDENTITY_PROVIDERS)
    @Operation( summary = "Create a new identity provider")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response create(@Parameter(description = "JSON body") IdentityProviderRepresentation representation) {
        this.auth.realm().requireManageIdentityProviders();

        ReservedCharValidator.validateNoSpace(representation.getAlias());

        try {
            IdentityProviderModel identityProvider = RepresentationToModel.toModel(realm, representation, session);
            session.identityProviders().create(identityProvider);

            representation.setInternalId(identityProvider.getInternalId());
            representation.setHideOnLogin(identityProvider.isHideOnLogin()); // update in case of legacy hide on login attr was used.
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), identityProvider.getAlias())
                    .representation(StripSecretsUtils.stripSecrets(session, representation)).success();

            return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(representation.getAlias()).build()).build();
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            if (message == null) {
                message = "Invalid request";
            }

            throw ErrorResponse.error(message, BAD_REQUEST);
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Identity Provider " + representation.getAlias() + " already exists");
        }
    }

    /** 按别名获取单个 IdP 子资源。
     * @param alias IdP 别名
     * @return {@link IdentityProviderResource}
     */
    @Path("instances/{alias}")
    public IdentityProviderResource getIdentityProvider(@PathParam("alias") String alias) {
        this.auth.realm().requireViewIdentityProviders();
        IdentityProviderModel identityProviderModel = session.identityProviders().getByIdOrAlias(alias);

        return new IdentityProviderResource(this.auth, realm, session, identityProviderModel, adminEvent);
    }

    /** 按 ID 查找身份提供者工厂 */
    private IdentityProviderFactory<?> getProviderFactoryById(String providerId) {
        return getProviderFactories()
                .filter(providerFactory -> Objects.equals(providerId, providerFactory.getId()))
                .map(IdentityProviderFactory.class::cast)
                .findFirst()
                .orElse(null);
    }

    /** 合并标准与社会化 IdP 提供者工厂流 */
    private Stream<ProviderFactory> getProviderFactories() {
        return Stream.concat(session.getKeycloakSessionFactory().getProviderFactoriesStream(IdentityProvider.class),
                session.getKeycloakSessionFactory().getProviderFactoriesStream(SocialIdentityProvider.class));
    }
}
