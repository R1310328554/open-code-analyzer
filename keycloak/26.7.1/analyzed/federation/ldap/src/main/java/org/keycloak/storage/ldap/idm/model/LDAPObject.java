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

package org.keycloak.storage.ldap.idm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jboss.logging.Logger;

/**
 * LDAP 目录条目的内存表示：UUID、DN、objectClass、属性集、只读/range 属性及必填属性延迟提交。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPObject {

    private static final Logger logger = Logger.getLogger(LDAPObject.class);

    private String uuid;
    private LDAPDn dn;

    // 多数情况下 RDN 属性为 uid 或 cn
    private final List<String> rdnAttributeNames = new LinkedList<>();

    private final List<String> objectClasses = new LinkedList<>();

    // 只读属性名统一小写，避免大小写差异
    private final List<String> readOnlyAttributeNames = new LinkedList<>();

    private final Map<String, Set<String>> attributes = new HashMap<>();

    // 属性名大小写不敏感索引，保留原始大小写键
    private final Map<String, Map.Entry<String, Set<String>>> lowerCasedAttributes = new HashMap<>();

    // range 属性分段读取，仅记录当前已读上限
    private final Map<String, Integer> rangedAttributes = new HashMap<>();

    // 必填属性全部就绪后执行的回调
    private Consumer<LDAPObject> consumerOnMandatoryAttributesComplete;

    // mandatory attributes defined for the entry
    private Set<String> mandatoryAttributeNames;

    // mandatory attributes that remain not set
    private Set<String> mandatoryAttributeNamesRemaining;

    /**
     * 注册必填属性集合；全部赋值完成后执行 consumer（用于延迟 create/add）。
     */
    public void executeOnMandatoryAttributesComplete(Set<String> mandatoryAttributeNames, Consumer<LDAPObject> consumer) {
        this.consumerOnMandatoryAttributesComplete = consumer;
        this.mandatoryAttributeNames = new LinkedHashSet<>();
        this.mandatoryAttributeNamesRemaining = new LinkedHashSet<>();
        // initializes mandatory attributes
        if (mandatoryAttributeNames != null) {
            for (String name : mandatoryAttributeNames) {
                name = name.toLowerCase();
                this.mandatoryAttributeNames.add(name);
                Map.Entry<String, Set<String>> entry = lowerCasedAttributes.get(name);
                if (entry == null || entry.getValue().isEmpty()) {
                    this.mandatoryAttributeNamesRemaining.add(name);
                }
            }
        }
        executeConsumerOnMandatoryAttributesComplete();
    }

    public boolean isWaitingForExecutionOnMandatoryAttributesComplete() {
        return consumerOnMandatoryAttributesComplete != null;
    }

    public Set<String> getMandatoryAttributeNamesRemaining() {
        return mandatoryAttributeNamesRemaining;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LDAPDn getDn() {
        return dn;
    }

    public void setDn(LDAPDn dn) {
        this.dn = dn;
    }

    public List<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Collection<String> objectClasses) {
        this.objectClasses.clear();
        this.objectClasses.addAll(objectClasses);
    }

    public List<String> getReadOnlyAttributeNames() {
        return readOnlyAttributeNames;
    }

    public void addReadOnlyAttributeName(String readOnlyAttribute) {
        readOnlyAttributeNames.add(readOnlyAttribute.toLowerCase());
    }

    public void removeReadOnlyAttributeName(String readOnlyAttribute) {
        readOnlyAttributeNames.remove(readOnlyAttribute.toLowerCase());
    }

    public List<String> getRdnAttributeNames() {
        return rdnAttributeNames;
    }

    /**
     * 设置单个 RDN 属性名（常见场景）。
     * @param rdnAttributeName The RDN of the ldap object
     */
    public void setRdnAttributeName(String rdnAttributeName) {
        this.rdnAttributeNames.clear();
        this.rdnAttributeNames.add(rdnAttributeName);
    }

    public void setRdnAttributeNames(List<String> rdnAttributeNames) {
        this.rdnAttributeNames.clear();
        this.rdnAttributeNames.addAll(rdnAttributeNames);
    }

    public void addRdnAttributeName(String rdnAttributeName) {
        this.rdnAttributeNames.add(rdnAttributeName);
    }

    public void setSingleAttribute(String attributeName, String attributeValue) {
        Set<String> asSet = new LinkedHashSet<>();
        if (attributeValue != null) {
            asSet.add(attributeValue);
        }
        setAttribute(attributeName, asSet);
    }

    public void setAttribute(String attributeName, Set<String> attributeValue) {
        final String attributeNameLowerCase = attributeName.toLowerCase();
        final Set<String> valueSet = attributeValue == null? Collections.emptySet() : attributeValue;
        Map.Entry<String, Set<String>> entry = lowerCasedAttributes.get(attributeNameLowerCase);
        if (entry == null) {
            attributes.put(attributeName, valueSet);
            lowerCasedAttributes.put(attributeNameLowerCase, Map.entry(attributeName, valueSet));
        } else {
            // existing entry, maintain previous case for the attribute name
            attributes.put(entry.getKey(), valueSet);
            lowerCasedAttributes.put(attributeNameLowerCase, Map.entry(entry.getKey(), valueSet));
        }
        if (consumerOnMandatoryAttributesComplete != null) {
            if (!valueSet.isEmpty()) {
                mandatoryAttributeNamesRemaining.remove(attributeNameLowerCase);
            } else if (mandatoryAttributeNames.contains(attributeNameLowerCase)) {
                mandatoryAttributeNamesRemaining.add(attributeNameLowerCase);
            }
            executeConsumerOnMandatoryAttributesComplete();
        }
    }

    // 属性名大小写不敏感
    public String getAttributeAsString(String name) {
        Map.Entry<String, Set<String>> entry = lowerCasedAttributes.get(name.toLowerCase());
        if (entry == null || entry.getValue().isEmpty()) {
            return null;
        } else if (entry.getValue().size() > 1) {
            logger.warnf("Expected String but attribute '%s' has more values '%s' on object '%s' . Returning just first value", name, entry.getValue(), dn);
        }

        return entry.getValue().iterator().next();
    }

    // Case-insensitive. Return null if there is not value of attribute with given name or set with all values otherwise
    public Set<String> getAttributeAsSet(String name) {
        return getAttributeAsSetOrDefault(name, null);
    }

    public Set<String> getAttributeAsSetOrDefault(String name, Set<String> defaultValue) {
        Map.Entry<String, Set<String>> entry = lowerCasedAttributes.get(name.toLowerCase());
        return (entry == null) ? defaultValue : new LinkedHashSet<>(entry.getValue());
    }

    public boolean isRangeComplete(String name) {
        return !rangedAttributes.containsKey(name);
    }

    public int getCurrentRange(String name) {
        return rangedAttributes.get(name);
    }

    public boolean isRangeCompleteForAllAttributes() {
        return rangedAttributes.isEmpty();
    }

    public void addRangedAttribute(String name, int max) {
        Integer current = rangedAttributes.get(name);
        if (current == null || max > current) {
            rangedAttributes.put(name, max);
        }
    }

    public void populateRangedAttribute(LDAPObject obj, String name) {
        Set<String> newValues = obj.getAttributes().get(name);
        if (newValues != null && attributes.containsKey(name)) {
            attributes.get(name).addAll(newValues);
            if (!obj.isRangeComplete(name)) {
                addRangedAttribute(name, obj.getCurrentRange(name));
            } else {
                rangedAttributes.remove(name);
            }
        }
    }

    public Map<String, Set<String>> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        LDAPObject other = (LDAPObject) obj;

        return getUuid() != null && other.getUuid() != null && getUuid().equals(other.getUuid());
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LDAP Object [ dn: " + dn + " , uuid: " + uuid + ", attributes: " + attributes +
                ", readOnly attribute names: " + readOnlyAttributeNames + ", ranges: " + rangedAttributes + " ]";
    }

    private void executeConsumerOnMandatoryAttributesComplete() {
        if (mandatoryAttributeNamesRemaining.isEmpty()) {
            final Consumer<LDAPObject> consumer = consumerOnMandatoryAttributesComplete;
            consumerOnMandatoryAttributesComplete = null;
            mandatoryAttributeNames = null;
            mandatoryAttributeNamesRemaining = null;
            consumer.accept(this);
        }
    }
}
