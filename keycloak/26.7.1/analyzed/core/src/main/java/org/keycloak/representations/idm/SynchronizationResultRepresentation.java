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

package org.keycloak.representations.idm;

/**
 * 用户联合/同步操作结果的 REST 表示，汇总新增、更新、删除及失败的用户数量。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SynchronizationResultRepresentation {

    /** 同步是否被忽略（未执行）。 */
    private boolean ignored;

    /** 新增用户数量。 */
    private int added;
    /** 更新用户数量。 */
    private int updated;
    /** 删除用户数量。 */
    private int removed;
    /** 同步失败的用户数量。 */
    private int failed;

    /** 同步状态描述。 */
    private String status;

    /** 默认构造函数。 */
    public SynchronizationResultRepresentation() {
    }

    /** @return 是否被忽略 */
    public boolean isIgnored() {
        return ignored;
    }

    /** @param ignored 是否被忽略 */
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    /** @return 新增用户数量 */
    public int getAdded() {
        return added;
    }

    /** @param added 新增用户数量 */
    public void setAdded(int added) {
        this.added = added;
    }

    /** @return 更新用户数量 */
    public int getUpdated() {
        return updated;
    }

    /** @param updated 更新用户数量 */
    public void setUpdated(int updated) {
        this.updated = updated;
    }

    /** @return 删除用户数量 */
    public int getRemoved() {
        return removed;
    }

    /** @param removed 删除用户数量 */
    public void setRemoved(int removed) {
        this.removed = removed;
    }

    /** @return 失败用户数量 */
    public int getFailed() {
        return failed;
    }

    /** @param failed 失败用户数量 */
    public void setFailed(int failed) {
        this.failed = failed;
    }

    /** @return 同步状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 同步状态 */
    public void setStatus(String status) {
        this.status = status;
    }

}
