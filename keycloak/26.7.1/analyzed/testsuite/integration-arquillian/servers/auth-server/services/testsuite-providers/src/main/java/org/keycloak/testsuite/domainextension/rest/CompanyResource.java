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

package org.keycloak.testsuite.domainextension.rest;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.testsuite.domainextension.CompanyRepresentation;
import org.keycloak.testsuite.domainextension.spi.ExampleService;

import org.jboss.resteasy.reactive.NoCache;

/**
 * 公司资源的 JAX-RS 端点，提供 CRUD 风格的 REST 操作。
 */
public class CompanyResource {

	/** 当前 Keycloak 会话。 */
	private final KeycloakSession session;
	
	/**
	 * @param session Keycloak 会话
	 */
	public CompanyResource(KeycloakSession session) {
		this.session = session;
	}

    /** 列出当前 Realm 下的全部公司。 */
    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public List<CompanyRepresentation> getCompanies() {
        return session.getProvider(ExampleService.class).listCompanies();
    }

    /** 删除当前 Realm 下的全部公司记录。 */
    @DELETE
    @Path("")
    @NoCache
    public void deleteAllCompanies() {
        session.getProvider(ExampleService.class).deleteAllCompanies();
    }

    /**
     * 创建新公司并返回 201 Created 响应。
     *
     * @param rep 待创建的公司表示对象
     * @return 指向新资源的响应
     */
    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCompany(CompanyRepresentation rep) {
        session.getProvider(ExampleService.class).addCompany(rep);
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(rep.getId()).build()).build();
    }

    /**
     * 按标识查询单个公司。
     *
     * @param id 公司标识
     * @return 公司表示对象
     */
    @GET
    @NoCache
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompanyRepresentation getCompany(@PathParam("id") final String id) {
        return session.getProvider(ExampleService.class).findCompany(id);
    }

}
