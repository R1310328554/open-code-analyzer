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
package org.keycloak.services.resources;

import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.health.LoadBalancerCheckProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.MediaType;

import io.smallrye.common.annotation.NonBlocking;
import org.jboss.logging.Logger;

/**
 * 负载均衡健康检查资源（多站点部署）。
 * <p>向负载均衡器报告本 Keycloak 集群是否应接收流量。本端点为非阻塞，即使实例高负载时仍可返回状态。详见 {@link LoadBalancerCheckProvider#isDown()}。</p>
 *
 * @author <a href="mailto:aschwart@redhat.com">Alexander Schwartz</a>
 */
@Provider
@Path("/lb-check")
@NonBlocking
public class LoadBalancerResource {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(LoadBalancerResource.class);

    /** 注入的 Keycloak 会话 */
    @Context
    KeycloakSession session;

    /**
     * 返回多站点负载均衡器所需的本站点 UP/DOWN 状态。
     * <p />
     * While a loadbalancer will usually check for the returned status code, the additional text <code>UP</code> or <code>DOWN</down>
     * is returned for humans to see the status in the browser.
     * <p />
     * In contrast to other management endpoints of Quarkus, no information is returned to the caller about the internal state of Keycloak
     * as this endpoint might be publicly available from the internet and should return as little information as possible.
     *
     * @return 正常时 HTTP 200 与 UP，下线时 HTTP 503 与 DOWN
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN_UTF_8)
    public Response getStatusForLoadBalancer() {
        Set<LoadBalancerCheckProvider> healthStatusProviders = session.getAllProviders(LoadBalancerCheckProvider.class);
        if (healthStatusProviders.stream().anyMatch(LoadBalancerCheckProvider::isDown)) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("DOWN").build();
        } else {
            return Response.ok().entity("UP").build();
        }
    }

}
