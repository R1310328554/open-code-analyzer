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

package org.keycloak.testsuite.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.common.enums.HostnameVerificationPolicy;
import org.keycloak.common.profile.PropertiesProfileConfigResolver;
import org.keycloak.common.util.HtmlUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.testframework.remote.providers.runonserver.FetchOnServer;
import org.keycloak.testframework.remote.providers.runonserver.RunOnServer;
import org.keycloak.testframework.remote.providers.runonserver.SerializationUtil;
import org.keycloak.testsuite.components.amphibian.TestAmphibianProvider;
import org.keycloak.testsuite.events.TestEventsListenerProvider;
import org.keycloak.testsuite.model.infinispan.InfinispanTestUtil;
import org.keycloak.testsuite.util.FeatureDeployerUtil;
import org.keycloak.timer.TimerProvider;
import org.keycloak.truststore.FileTruststoreProvider;
import org.keycloak.truststore.FileTruststoreProviderFactory;
import org.keycloak.truststore.TruststoreProvider;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.MediaType;

import org.jboss.resteasy.reactive.NoCache;

import static java.util.Objects.requireNonNull;


/**
 * 集成测试 REST 资源提供者，暴露 Infinispan、事件队列、特性开关等测试端点。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TestingResourceProvider implements RealmResourceProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 已暂停的定时器任务上下文。 */
    private final Map<String, TimerProvider.TimerTaskContext> suspendedTimerTasks;

    /** 当前 HTTP 请求。 */
    private final HttpRequest request;

    /** 所属工厂，用于 truststore SPI 测试状态共享。 */
    private final TestingResourceProviderFactory factory;

    /** {@inheritDoc} 返回自身作为 JAX-RS 资源。 */
    @Override
    public Object getResource() {
        return this;
    }

    /**
     * 构造测试资源提供者。
     *
     * @param session Keycloak 会话
     * @param factory 所属工厂
     * @param suspendedTimerTasks 暂停的定时器任务映射
     */
    public TestingResourceProvider(KeycloakSession session, TestingResourceProviderFactory factory, Map<String, TimerProvider.TimerTaskContext> suspendedTimerTasks) {
        this.session = session;
        this.factory = factory;
        this.suspendedTimerTasks = suspendedTimerTasks;
        this.request = session.getContext().getHttpRequest();
    }

    /** 启用 Infinispan 测试时间服务。 */
    @POST
    @Path("/set-testing-infinispan-time-service")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setTestingInfinispanTimeService() {
        InfinispanTestUtil.setTestingTimeService(session);
        return Response.noContent().build();
    }

    /** 恢复 Infinispan 默认时间服务。 */
    @POST
    @Path("/revert-testing-infinispan-time-service")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revertTestingInfinispanTimeService() {
        InfinispanTestUtil.revertTimeService(session);
        return Response.noContent().build();
    }

    /** 从测试事件队列中轮询一条用户事件。 */
    @POST
    @Path("/poll-event-queue")
    @Produces(MediaType.APPLICATION_JSON)
    public EventRepresentation getEvent() {
        Event event = TestEventsListenerProvider.poll();
        if (event != null) {
            return ModelToRepresentation.toRepresentation(event);
        } else {
            return null;
        }
    }

    /** 从测试管理事件队列中轮询一条管理事件。 */
    @POST
    @Path("/poll-admin-event-queue")
    @Produces(MediaType.APPLICATION_JSON)
    public AdminEventRepresentation getAdminEvent() {
        AdminEvent adminEvent = TestEventsListenerProvider.pollAdminEvent();
        if (adminEvent != null) {
            return ModelToRepresentation.toRepresentation(adminEvent);
        } else {
            return null;
        }
    }

    /** 清空用户事件测试队列。 */
    @POST
    @Path("/clear-event-queue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearEventQueue() {
        TestEventsListenerProvider.clear();
        return Response.noContent().build();
    }

    /** 清空管理事件测试队列。 */
    @POST
    @Path("/clear-admin-event-queue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearAdminEventQueue() {
        TestEventsListenerProvider.clearAdminEvents();
        return Response.noContent().build();
    }

    @Override
    public void close() {
    }

    /** 返回测试两栖组件的详细信息映射。 */
    @GET
    @Path("/test-amphibian-component")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, Object>> getTestAmphibianComponentDetails() {
        RealmModel realm = session.getContext().getRealm();
        return realm.getComponentsStream(realm.getId(), TestAmphibianProvider.class.getName())
                .collect(Collectors.toMap(
                        ComponentModel::getName,
                        componentModel -> {
                            TestAmphibianProvider t = session.getComponentProvider(TestAmphibianProvider.class, componentModel.getId());
                            return t == null ? null : t.getDetails();
                        }));
    }

    /** 设置 Kerberos 配置文件路径系统属性。 */
    @PUT
    @Path("/set-krb5-conf-file")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setKrb5ConfFile(@QueryParam("krb5-conf-file") String krb5ConfFile) {
        System.setProperty("java.security.krb5.conf", krb5ConfFile);
    }

    /**
     * 在服务器端执行序列化的 {@link RunOnServer} 或 {@link FetchOnServer} 任务。
     *
     * @param runOnServer Base64 编码的序列化任务
     * @return 执行结果 JSON 或异常编码
     */
    @POST
    @Path("/run-on-server")
    @Consumes(MediaType.TEXT_PLAIN_UTF_8)
    @Produces(MediaType.TEXT_PLAIN_UTF_8)
    public String runOnServer(String runOnServer) {
        try {
            Object r = SerializationUtil.decode(runOnServer, TestClassLoader.getInstance());

            if (r instanceof FetchOnServer) {
                Object result = ((FetchOnServer) r).run(session);
                return result != null ? JsonSerialization.writeValueAsString(result) : null;
            } else if (r instanceof RunOnServer) {
                ((RunOnServer) r).run(session);
                return null;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Throwable t) {
            return SerializationUtil.encodeException(t);
        }
    }


    /**
     * 在服务器端反射执行模型层单元测试方法。
     *
     * @param testClassName 测试类全限定名
     * @param testMethodName 接受 {@link KeycloakSession} 的测试方法名
     * @return {@code SUCCESS} 或序列化异常
     */
    @POST
    @Path("/run-model-test-on-server")
    @Consumes(MediaType.TEXT_PLAIN_UTF_8)
    @Produces(MediaType.TEXT_PLAIN_UTF_8)
    public String runModelTestOnServer(@QueryParam("testClassName") String testClassName,
                                       @QueryParam("testMethodName") String testMethodName) {
        try {
            Class<?> testClass = TestClassLoader.getInstance().loadClass(testClassName);
            Method testMethod = testClass.getDeclaredMethod(testMethodName, KeycloakSession.class);

            Object test = testClass.getDeclaredConstructor().newInstance();
            testMethod.invoke(test, session);

            return "SUCCESS";
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
            }

            return SerializationUtil.encodeException(t);
        }
    }

    /** 在 profile.properties 中设置特性开关状态。 */
    private void setFeatureInProfileFile(File file, Profile.Feature featureProfile, String newState) {
        doWithProperties(file, props -> props.setProperty(PropertiesProfileConfigResolver.getPropertyKey(featureProfile), newState));
    }

    /** 从 profile.properties 中移除特性配置项。 */
    private void unsetFeatureInProfileFile(File file, Profile.Feature featureProfile) {
        doWithProperties(file, props -> props.remove(PropertiesProfileConfigResolver.getPropertyKey(featureProfile)));
    }

    /** 读取、修改并写回 profile.properties 文件。 */
    private void doWithProperties(File file, Consumer<Properties> callback) {

        Properties properties = new Properties();
        if (file.isFile() && file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Unable to read profile.properties file");
            }
        }

        callback.accept(properties);

        if (file.isFile() && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, null);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to profile.properties file");
        }
    }

    /** 返回当前已禁用的特性集合。 */
    @GET
    @Path("/list-disabled-features")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Profile.Feature> listDisabledFeatures() {
        return Profile.getInstance().getDisabledFeatures();
    }

    /** 启用指定特性并返回更新后的禁用特性集合。 */
    @POST
    @Path("/enable-feature/{feature}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Profile.Feature> enableFeature(@PathParam("feature") String feature) {
        return updateFeature(feature, true);
    }

    /** 禁用指定特性并返回更新后的禁用特性集合。 */
    @POST
    @Path("/disable-feature/{feature}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Profile.Feature> disableFeature(@PathParam("feature") String feature) {
        return updateFeature(feature, false);
    }

    /** 将特性重置为 profile 默认配置。 */
    @POST
    @Path("/reset-feature/{feature}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void resetFeature(@PathParam("feature") String featureKey) {

        featureKey = featureKey.contains(":") ? featureKey.split(":")[0] : featureKey;
        Profile.Feature feature = Profile.getFeatureVersions(featureKey).iterator().next();

        if (feature == null) {
            System.err.printf("Feature '%s' doesn't exist!!\n", featureKey);
            throw new BadRequestException();
        }

        FeatureDeployerUtil.initBeforeChangeFeature(feature);

        String jbossServerConfigDir = System.getProperty("jboss.server.config.dir");
        // 在 JBoss 容器中需写入 profile.properties，否则重启后系统属性变更会丢失
        if (jbossServerConfigDir != null) {
            File file = new File(jbossServerConfigDir, "profile.properties");
            unsetFeatureInProfileFile(file, feature);
        }
    }

    /** 更新特性开关状态，必要时持久化到 profile.properties 并重新部署 SPI。 */
    private Set<Profile.Feature> updateFeature(String featureKey, boolean shouldEnable) {
        Collection<Profile.Feature> features = null;

        if (featureKey.contains(":")) {
            String unversionedKey = featureKey.split(":")[0];
            int version = Integer.parseInt(featureKey.split(":")[1].replace("v", ""));

            for (Feature versionedFeature : Profile.getFeatureVersions(unversionedKey)) {
                if (versionedFeature.getVersion() == version) {
                    features = Set.of(versionedFeature);
                    break;
                }
            }
        } else {
            features = Profile.getFeatureVersions(featureKey);
        }

        if (features == null || features.isEmpty()) {
            System.err.printf("Feature '%s' doesn't exist!!\n", featureKey);
            throw new BadRequestException();
        }

        for (Feature feature : features) {
            if (Profile.getInstance().getFeatures().get(feature) != shouldEnable) {
                FeatureDeployerUtil.initBeforeChangeFeature(feature);

                String jbossServerConfigDir = System.getProperty("jboss.server.config.dir");
                // 在 JBoss 容器中需写入 profile.properties，否则重启后系统属性变更会丢失
                if (jbossServerConfigDir != null) {
                    setFeatureInProfileFile(new File(jbossServerConfigDir, "profile.properties"), feature, shouldEnable ? "enabled" : "disabled");
                }

                Profile current = Profile.getInstance();

                Map<Feature, Boolean> updatedFeatures = new HashMap<>(current.getFeatures());
                updatedFeatures.put(feature, shouldEnable);

                Profile.init(current.getName(), updatedFeatures);

                if (shouldEnable) {
                    FeatureDeployerUtil.deployFactoriesAfterFeatureEnabled(feature);
                } else {
                    FeatureDeployerUtil.undeployFactoriesAfterFeatureDisabled(feature);
                }
            }
        }

        return Profile.getInstance().getDisabledFeatures();
    }

    /**
     * 生成自动提交 POST 表单的 HTML 页面，用于模拟 WebDriver 难以发送的自定义 POST 请求。
     * <p>
     * 详见 URLUtils.sendPOSTWithWebDriver
     *
     * @param postRequestUrl 目标 POST 绝对 URL，可含查询参数
     * @param encodedFormParameters URL 编码的表单参数，格式为 {@code param1=value1&param2=value2}
     * @return 含自动提交表单的 HTML 响应
     */
    @GET
    @Path("/simulate-post-request")
    @Produces(MediaType.TEXT_HTML_UTF_8)
    public Response simulatePostRequest(@QueryParam("postRequestUrl") String postRequestUrl,
                                        @QueryParam("encodedFormParameters") String encodedFormParameters) {
        Map<String, String> params = new HashMap<>();

        // 解析 POST 请求参数
        for (String param : encodedFormParameters.split("&")) {
            String[] paramParts = param.split("=");
            String value = paramParts.length == 2 ? paramParts[1] : "";
            params.put(paramParts[0], value);
        }

        // 手动构造自动提交 POST 表单 HTML
        StringBuilder builder = new StringBuilder();

        builder.append("<HTML>");
        builder.append("  <HEAD>");
        builder.append("    <TITLE>OIDC Form_Post Response</TITLE>");
        builder.append("  </HEAD>");
        builder.append("  <BODY Onload=\"document.forms[0].submit()\">");

        builder.append("    <FORM METHOD=\"POST\" ACTION=\"").append(postRequestUrl).append("\">");

        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.append("  <INPUT TYPE=\"HIDDEN\" NAME=\"")
                    .append(param.getKey())
                    .append("\" VALUE=\"")
                    .append(HtmlUtils.escapeAttribute(param.getValue()))
                    .append("\" />");
        }

        builder.append("      <NOSCRIPT>");
        builder.append("        <P>JavaScript is disabled. We strongly recommend to enable it. Click the button below to continue .</P>");
        builder.append("        <INPUT name=\"continue\" TYPE=\"SUBMIT\" VALUE=\"CONTINUE\" />");
        builder.append("      </NOSCRIPT>");
        builder.append("    </FORM>");
        builder.append("  </BODY>");
        builder.append("</HTML>");

        return Response.status(Response.Status.OK)
                .type(jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE)
                .entity(builder.toString()).build();

    }

    /** 按名称查找 Realm，不存在时抛出 404。 */
    private RealmModel getRealmByName(String realmName) {
        RealmProvider realmProvider = session.getProvider(RealmProvider.class);
        RealmModel realm = realmProvider.getRealmByName(realmName);
        if (realm == null) {
            throw new NotFoundException("Realm not found");
        }
        return realm;
    }

    /** 禁用 truststore SPI 并保存原始提供者以便恢复。 */
    @GET
    @Path("/disable-truststore-spi")
    @NoCache
    public void disableTruststoreSpi() {
        FileTruststoreProviderFactory factory = (FileTruststoreProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(TruststoreProvider.class);
        this.factory.truststoreProvider = factory.create(session);
        factory.setProvider(null);
    }

    /** 修改 truststore SPI 的主机名验证策略。 */
    @GET
    @Path("/modify-truststore-spi-hostname-policy")
    @NoCache
    public void modifyTruststoreSpiHostnamePolicy(@QueryParam("hostnamePolicy") final HostnameVerificationPolicy hostnamePolicy) {
        FileTruststoreProviderFactory fact = (FileTruststoreProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(TruststoreProvider.class);
        this.factory.truststoreProvider = fact.create(session);
        FileTruststoreProvider origTrustProvider = (FileTruststoreProvider) this.factory.truststoreProvider;
        TruststoreProvider newTrustProvider = new FileTruststoreProvider(
                origTrustProvider.getTruststore(), hostnamePolicy,
                Collections.unmodifiableMap(origTrustProvider.getRootCertificates()),
                Collections.unmodifiableMap(origTrustProvider.getIntermediateCertificates()),
                null, null, null);
        fact.setProvider(newTrustProvider);
    }

    /** 恢复先前禁用的 truststore SPI 提供者。 */
    @GET
    @Path("/reenable-truststore-spi")
    @NoCache
    public void reenableTruststoreSpi() {
        if (this.factory.truststoreProvider == null) {
            throw new IllegalStateException("Cannot reenable provider as it was not disabled");
        }
        FileTruststoreProviderFactory factory = (FileTruststoreProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(TruststoreProvider.class);
        factory.setProvider(this.factory.truststoreProvider);
    }

    /** 返回带编程式 Cache-Control max-age 的无内容响应，用于测试 @NoCache 行为。 */
    @GET
    @Path("/no-cache-annotated-endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getNoCacheAnnotatedEndpointResponse(@QueryParam("programmatic_max_age_value") Integer programmaticMaxAgeValue) {
        requireNonNull(programmaticMaxAgeValue);

        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(programmaticMaxAgeValue);

        return Response.noContent().cacheControl(cacheControl).build();
    }

    /** 返回空白 HTML 页面，供测试占位使用。 */
    @GET
    @Path("/blank")
    @Produces(MediaType.TEXT_HTML_UTF_8)
    public Response getBlankPage() {
        return Response.ok("<html><body></body></html>").build();
    }
}
