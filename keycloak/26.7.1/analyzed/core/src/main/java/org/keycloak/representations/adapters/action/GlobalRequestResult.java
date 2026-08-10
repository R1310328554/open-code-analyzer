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

package org.keycloak.representations.adapters.action;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局管理请求（如 pushNotBefore、logoutAll）向集群各节点广播后的聚合结果。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GlobalRequestResult {

    /** 成功响应的节点请求 URI 列表。 */
    private List<String> successRequests;
    /** 失败响应的节点请求 URI 列表。 */
    private List<String> failedRequests;

    /** 追加一条成功请求 URI。 */
    public void addSuccessRequest(String reqUri) {
        if (successRequests == null) {
            successRequests = new ArrayList<>();
        }
        successRequests.add(reqUri);
    }

    /** 追加一条失败请求 URI。 */
    public void addFailedRequest(String reqUri) {
        if (failedRequests == null) {
            failedRequests = new ArrayList<>();
        }
        failedRequests.add(reqUri);
    }

    /** 批量追加成功请求 URI。 */
    public void addAllSuccessRequests(List<String> reqUris) {
        if (successRequests == null) {
            successRequests = new ArrayList<>();
        }
        successRequests.addAll(reqUris);
    }

    /** 批量追加失败请求 URI。 */
    public void addAllFailedRequests(List<String> reqUris) {
        if (failedRequests == null) {
            failedRequests = new ArrayList<>();
        }
        failedRequests.addAll(reqUris);
    }

    /** 合并另一 {@link GlobalRequestResult} 的成功与失败列表。 */
    public void addAll(GlobalRequestResult merged) {
        if (merged.getSuccessRequests() != null && merged.getSuccessRequests().size() > 0) {
            addAllSuccessRequests(merged.getSuccessRequests());
        }
        if (merged.getFailedRequests() != null && merged.getFailedRequests().size() > 0) {
            addAllFailedRequests(merged.getFailedRequests());
        }
    }

    /** @return 成功请求 URI 列表 */
    public List<String> getSuccessRequests() {
        return successRequests;
    }

    /** @return 失败请求 URI 列表 */
    public List<String> getFailedRequests() {
        return failedRequests;
    }
}
