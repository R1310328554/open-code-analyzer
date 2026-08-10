/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 用户配置属性元数据：读写权限、必填条件、校验器、GUI 顺序及注解等。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AttributeMetadata {

    /** 恒为真的上下文谓词。 */
    public static final Predicate<AttributeContext> ALWAYS_TRUE = context -> true;
    /** 恒为假的上下文谓词。 */
    public static final Predicate<AttributeContext> ALWAYS_FALSE = context -> false;

    /** 属性内部名称。 */
    private final String attributeName;
    /** 属性展示名称。 */
    private String attributeDisplayName;
    /** 所属属性组元数据。 */
    private AttributeGroupMetadata attributeGroupMetadata;
    /** 属性是否在当前上下文可见/启用。 */
    private Predicate<AttributeContext> selector;
    /** 可写条件列表（全部满足才可编辑）。 */
    private final List<Predicate<AttributeContext>> writeAllowed = new ArrayList<>();
    /** 必填条件谓词；为 {@code null} 时视为必填。 */
    private Predicate<AttributeContext> required;
    /** 可读条件列表。 */
    private final List<Predicate<AttributeContext>> readAllowed = new ArrayList<>();
    /** 属性校验器元数据列表。 */
    private List<AttributeValidatorMetadata> validators;
    /** 静态注解映射。 */
    private Map<String, Object> annotations;
    /** UI 排序权重。 */
    private int guiOrder;
    /** 是否允许多值。 */
    private boolean multivalued;
    /** 默认值。 */
    private String defaultValue;
    /** 动态解析注解的装饰函数。 */
    private Function<AttributeContext, Map<String, Object>> annotationDecorator = (c) -> c.getMetadata().getAnnotations();
    /** 是否为内置默认属性。 */
    private boolean defaultAttribute;

    /** 包内构造：默认可读写且可见。 */
    AttributeMetadata(String attributeName, int guiOrder) {
        this(attributeName, guiOrder, ALWAYS_TRUE, ALWAYS_TRUE, ALWAYS_TRUE, ALWAYS_TRUE);
    }

    AttributeMetadata(String attributeName, int guiOrder, Predicate<AttributeContext> writeAllowed, Predicate<AttributeContext> required) {
        this(attributeName, guiOrder, ALWAYS_TRUE, writeAllowed, required, ALWAYS_TRUE);
    }

    AttributeMetadata(String attributeName, int guiOrder, Predicate<AttributeContext> selector) {
        this(attributeName, guiOrder, selector, ALWAYS_FALSE, ALWAYS_TRUE, ALWAYS_TRUE);
    }

    AttributeMetadata(String attributeName, int guiOrder, List<String> scopes, Predicate<AttributeContext> writeAllowed, Predicate<AttributeContext> required) {
        this(attributeName, guiOrder, context -> {
            KeycloakSession session = context.getSession();
            AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();

            if (authSession == null) {
                return false;
            }

            ClientScopeProvider clientScopes = session.clientScopes();
            RealmModel realm = session.getContext().getRealm();

            // 部分认证流（如注册）下客户端 scope 匹配可能失效
            // TODO UserProfile - LOOKS LIKE THIS DOESN'T WORK FOR SOME AUTH FLOWS, LIKE
            // REGISTER?
            if (authSession.getClientScopes().stream().anyMatch(scopes::contains)) {
                return true;
            }

            return authSession.getClientScopes().stream()
                    .map(id -> clientScopes.getClientScopeById(realm, id).getName()).anyMatch(scopes::contains);
        }, writeAllowed, required, ALWAYS_TRUE);
    }

    AttributeMetadata(String attributeName, int guiOrder, Predicate<AttributeContext> selector, Predicate<AttributeContext> writeAllowed,
            Predicate<AttributeContext> required,
            Predicate<AttributeContext> readAllowed) {
        this.attributeName = attributeName;
        this.guiOrder = guiOrder;
        this.selector = selector;
        addWriteCondition(writeAllowed);
        this.required = required;
        addReadCondition(readAllowed);
    }

    AttributeMetadata(String attributeName, int guiOrder, Predicate<AttributeContext> selector, List<Predicate<AttributeContext>> writeAllowed,
                      Predicate<AttributeContext> required,
                      List<Predicate<AttributeContext>> readAllowed) {
        this.attributeName = attributeName;
        this.guiOrder = guiOrder;
        this.selector = selector;
        this.writeAllowed.addAll(writeAllowed);
        this.required = required;
        this.readAllowed.addAll(readAllowed);
    }

    /** @return 属性名称 */
    public String getName() {
        return attributeName;
    }

    /** @return 默认值 */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** @return UI 排序 */
    public int getGuiOrder() {
        return guiOrder;
    }

    /** 设置 UI 排序。
     * @return this */
    public AttributeMetadata setGuiOrder(int guiOrder) {
        this.guiOrder = guiOrder;
        return this;
    }

    /** @return 属性组元数据 */
    public AttributeGroupMetadata getAttributeGroupMetadata() {
        return attributeGroupMetadata;
    }

    /** 当前上下文是否选中/展示该属性。
     * @param context 属性上下文
     * @return 选中时 {@code true} */
    public boolean isSelected(AttributeContext context) {
        return selector.test(context);
    }

    /** 设置可见性选择器。 */
    public void setSelector(Predicate<AttributeContext> selector) {
        this.selector = selector;
    }

    private boolean allConditionsMet(List<Predicate<AttributeContext>> predicates, AttributeContext context) {
        return predicates.stream().allMatch(p -> p.test(context));
    }

    /** 追加可读条件。
     * @return this */
    public AttributeMetadata addReadCondition(Predicate<AttributeContext> readAllowed) {
        this.readAllowed.add(readAllowed);
        return this;
    }

    /** 追加可写条件。
     * @return this */
    public AttributeMetadata addWriteCondition(Predicate<AttributeContext> writeAllowed) {
        this.writeAllowed.add(writeAllowed);
        return this;
    }
    /** @return 在给定上下文中是否只读 */
    public boolean isReadOnly(AttributeContext context) {
        return !canEdit(context);
    }

    /** @return 是否可查看 */
    public boolean canView(AttributeContext context) {
        return allConditionsMet(readAllowed, context);
    }

    /** @return 是否可编辑 */
    public boolean canEdit(AttributeContext context) {
        return allConditionsMet(writeAllowed, context);
    }

    /**
     * 根据必填谓词判断属性是否必填；谓词为 {@code null} 时视为必填。
     * @param context to evaluate requirement of the attribute from
     * @return true if attribute is required in provided context
     */
    public boolean isRequired(AttributeContext context) {
        return required == null || required.test(context);
    }

    /** @return 校验器元数据列表 */
    public List<AttributeValidatorMetadata> getValidators() {
        return validators;
    }

    /** 合并校验器（去重）。
     * @return this */
    public AttributeMetadata addValidators(List<AttributeValidatorMetadata> validators) {
        if (this.validators == null) {
            this.validators = new ArrayList<>();
        }

        this.validators.removeIf(validators::contains);
        this.validators.addAll(validators.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        return this;
    }

    /** @return 静态注解映射 */
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    /** 合并注解。
     * @return this */
    public AttributeMetadata addAnnotations(Map<String, Object> annotations) {
        if(annotations != null) {
            if(this.annotations == null) {
                this.annotations = new HashMap<>();
            }

            this.annotations.putAll(annotations);
        }
        return this;
    }

    /** 设置是否多值。 */
    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    /** @return 是否多值 */
    public boolean isMultivalued() {
        return multivalued;
    }

    @Override
    public AttributeMetadata clone() {
        AttributeMetadata cloned = new AttributeMetadata(attributeName, guiOrder, selector, new ArrayList<>(writeAllowed), required, new ArrayList<>(readAllowed));
        // 克隆校验器列表以便增删；校验器实例本身不克隆
        // we clone validators list to allow adding or removing validators. Validators
        // itself are not cloned as we do not expect them to be reconfigured.
        if (validators != null) {
            cloned.addValidators(validators);
        }
        // 克隆注解映射以便修改
        //we clone annotations map to allow adding to or removing from it
        if(annotations != null) {
            cloned.addAnnotations(annotations);
        }
        cloned.setAttributeDisplayName(attributeDisplayName);
        if (attributeGroupMetadata != null) {
            cloned.setAttributeGroupMetadata(attributeGroupMetadata.clone());
        }
        cloned.setMultivalued(multivalued);
        cloned.setDefaultValue(defaultValue);
        cloned.setAnnotationDecorator(annotationDecorator);
        cloned.setDefault(defaultAttribute);
        return cloned;
    }

    /** @return 展示名（空则回退属性名） */
    public String getAttributeDisplayName() {
        if(attributeDisplayName == null || attributeDisplayName.trim().isEmpty())
            return attributeName;
        return attributeDisplayName;
    }

    /** 设置展示名。
     * @return this */
    public AttributeMetadata setAttributeDisplayName(String attributeDisplayName) {
        if(attributeDisplayName != null)
            this.attributeDisplayName = attributeDisplayName;
        return this;
    }

    /** 设置属性组。
     * @return this */
    public AttributeMetadata setAttributeGroupMetadata(AttributeGroupMetadata attributeGroupMetadata) {
        if(attributeGroupMetadata != null)
            this.attributeGroupMetadata = attributeGroupMetadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AttributeMetadata)) return false;

        AttributeMetadata that = (AttributeMetadata) o;

        return that.getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return attributeName.hashCode();
    }

    /** 设置必填谓词。
     * @return this */
    public AttributeMetadata setRequired(Predicate<AttributeContext> required) {
        this.required = required;
        return this;
    }

    /** 替换校验器列表。
     * @return this */
    public AttributeMetadata setValidators(List<AttributeValidatorMetadata> validators) {
        this.validators = validators;
        return this;
    }

    /** 设置默认值。
     * @return this */
    public AttributeMetadata setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /** 按上下文解析注解（可经装饰器动态生成）。
     * @param context 属性上下文
     * @return 注解映射 */
    public Map<String, Object> getAnnotations(AttributeContext context) {
        return annotationDecorator.apply(context);
    }

    /** 设置注解装饰器。
     * @return this */
    public AttributeMetadata setAnnotationDecorator(Function<AttributeContext, Map<String, Object>> annotationDecorator) {
        this.annotationDecorator = annotationDecorator;
        return this;
    }

    /** 标记是否为内置默认属性。 */
    public void setDefault(boolean defaultAttribute) {
        this.defaultAttribute = defaultAttribute;
    }

    /** @return 是否为内置默认属性 */
    public boolean isDefault() {
        return defaultAttribute;
    }
}
