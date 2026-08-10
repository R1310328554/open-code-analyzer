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

package org.keycloak.storage.user;

/**
 * 用户联邦同步结果：统计新增、更新、删除与失败数量，并可标记同步被忽略。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SynchronizationResult {

    /** 是否因同步已在进行而被忽略。 */
    private boolean ignored;

    /** 新增用户数。 */
    private int added;
    /** 更新用户数。 */
    private int updated;
    /** 删除用户数。 */
    private int removed;
    /** 同步失败用户数。 */
    private int failed;

    /** 同步是否被忽略。 */
    public boolean isIgnored() {
        return ignored;
    }

    /** 设置是否忽略同步。
     * @param ignored 忽略标志 */
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    /** 返回新增用户数。 */
    public int getAdded() {
        return added;
    }

    /** 设置新增用户数。
     * @param added 数量 */
    public void setAdded(int added) {
        this.added = added;
    }

    /** 返回更新用户数。 */
    public int getUpdated() {
        return updated;
    }

    /** 设置更新用户数。
     * @param updated 数量 */
    public void setUpdated(int updated) {
        this.updated = updated;
    }

    /** 返回删除用户数。 */
    public int getRemoved() {
        return removed;
    }

    /** 设置删除用户数。
     * @param removed 数量 */
    public void setRemoved(int removed) {
        this.removed = removed;
    }

    /** 返回同步失败用户数。 */
    public int getFailed() {
        return failed;
    }

    /** 设置同步失败用户数。
     * @param failed 数量 */
    public void setFailed(int failed) {
        this.failed = failed;
    }

    /** 新增计数加一。 */
    public void increaseAdded() {
        added++;
    }

    /** 更新计数加一。 */
    public void increaseUpdated() {
        updated++;
    }

    /** 删除计数加一。 */
    public void increaseRemoved() {
        removed++;
    }

    /** 失败计数加一。 */
    public void increaseFailed() {
        failed++;
    }

    /** 合并另一同步结果的计数。
     * @param other 待合并的结果 */
    public void add(SynchronizationResult other) {
        added += other.added;
        updated += other.updated;
        removed += other.removed;
        failed += other.failed;
    }

    /** 返回人类可读的同步状态摘要。 */
    public String getStatus() {
        if (ignored) {
            return "Synchronization ignored as it's already in progress";
        } else {
            String status = String.format("%d imported users, %d updated users", added, updated);
            if (removed > 0) {
                status += String.format(", %d removed users", removed);
            }
            if (failed != 0) {
                status += String.format(", %d users failed sync! See server log for more details", failed);
            }
            return status;
        }
    }

    @Override
    public String toString() {
        return String.format("UserFederationSyncResult [ %s ]", getStatus());
    }

    /** 创建空同步结果（各计数为零）。 */
    public static SynchronizationResult empty() {
        return new SynchronizationResult();
    }

    /** 创建标记为“已忽略”的同步结果。 */
    public static SynchronizationResult ignored() {
        SynchronizationResult result = new SynchronizationResult();
        result.setIgnored(true);
        return result;
    }
}
