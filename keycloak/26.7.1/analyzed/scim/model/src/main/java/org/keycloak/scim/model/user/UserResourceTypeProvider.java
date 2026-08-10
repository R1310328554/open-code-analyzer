package org.keycloak.scim.model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.fgap.evaluation.partial.PartialEvaluationStorageProvider;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.jpa.entities.UserGroupMembershipEntity;
import org.keycloak.scim.filter.FilterUtils;
import org.keycloak.scim.filter.ScimFilterParser;
import org.keycloak.scim.filter.ScimFilterParser.FilterContext;
import org.keycloak.scim.model.filter.ScimAttributeJpaExpressionResolver;
import org.keycloak.scim.model.filter.ScimJPAPredicateEvaluator;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.schema.attribute.Attribute;
import org.keycloak.scim.resource.spi.AbstractScimResourceTypeProvider;
import org.keycloak.scim.resource.user.User;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.userprofile.ValidationException.Error;
import org.keycloak.utils.StringUtil;

import static org.keycloak.models.jpa.PaginationUtils.paginateQuery;
import static org.keycloak.utils.StreamsUtil.closing;

/**
 * SCIM User 资源类型提供者。
 * <p>负责 User CRUD、User Profile 校验、JPA 过滤查询及 groups 属性的 JPA 表达式解析。</p>
 */
public class UserResourceTypeProvider extends AbstractScimResourceTypeProvider<UserModel, User> implements ScimAttributeJpaExpressionResolver {

    /** 构造 User 提供者，注册核心、Enterprise 与扩展 schema。 */
    public UserResourceTypeProvider(KeycloakSession session) {
        super(session, new UserCoreModelSchema(session), List.of(new UserEnterpriseModelSchema(session), new UserExtensionModelSchema(session)));
    }

    @Override
    public String getDescription() {
        return "User Account";
    }

    /** 通过 User Profile 创建用户并校验 SCIM 表示。 */
    @Override
    public User onCreate(User resource) {
        UserProfileProvider provider = session.getProvider(UserProfileProvider.class);
        String userName = resource.getUserName();

        if (userName == null) {
            throw new ModelValidationException("username is required");
        }

        UserProfile profile = provider.create(UserProfileContext.SCIM, Map.of(UserModel.USERNAME, userName));
        UserModel model = profile.create(false);

        populate(model, resource);

        try {
            profile = provider.create(UserProfileContext.SCIM, model);
            profile.validate();
        } catch (ValidationException ve) {
            throw handleValidationException(ve);
        }

        resource.setCreatedTimestamp(model.getCreatedTimestamp());
        resource.setLastModifiedTimestamp(model.getLastModifiedTimestamp());

        return resource;
    }

    /** 更新用户并触发 Profile 校验与时间戳刷新。 */
    @Override
    protected User onUpdate(UserModel model, User resource) {
        try {
            UserProfileProvider userProfileProvider = session.getProvider(UserProfileProvider.class);
            UserProfile profile = userProfileProvider.create(UserProfileContext.SCIM, model);
            profile.update();
        } catch (ValidationException ve) {
            throw handleValidationException(ve);
        }

        model.setLastModifiedTimestamp(Time.currentTimeMillis());
        resource.setCreatedTimestamp(model.getCreatedTimestamp());
        resource.setLastModifiedTimestamp(model.getLastModifiedTimestamp());

        return resource;
    }

    /** 按 ID 获取用户，排除服务账户链接用户。 */
    @Override
    protected UserModel getModel(String id) {
        RealmModel realm = session.getContext().getRealm();
        UserModel model = session.users().getUserById(realm, id);

        if (model == null || model.getServiceAccountClientLink() == null) {
            return model;
        }

        return null;
    }

    @Override
    protected String getRealmResourceType() {
        return AdminPermissionsSchema.USERS_RESOURCE_TYPE;
    }

    /** 管理员用户仅返回最小可见字段。 */
    @Override
    protected User createResourceTypeInstance(UserModel model, List<String> attributes, List<String> excludedAttributes) {
        if (session.getContext().getPermissions().isAdminUser(model)) {
            User user = new User();

            user.addSchema(getSchema());
            user.setId(model.getId());
            user.setUserName(model.getUsername());

            return user;
        }
        return super.createResourceTypeInstance(model, attributes, excludedAttributes);
    }

    @Override
    protected Stream<UserModel> getModels(SearchRequest searchRequest) {
        RealmModel realm = session.getContext().getRealm();
        Integer firstResult = searchRequest.getStartIndex() != null ? searchRequest.getStartIndex() - 1 : null;
        Integer maxResults = searchRequest.getCount();
        maxResults = maxResults != null ? Math.min(maxResults, DEFAULT_MAX_RESULTS) : DEFAULT_MAX_RESULTS;

        if (StringUtil.isNotBlank(searchRequest.getFilter())) {
            // 将 filter 解析为 AST
            ScimFilterParser.FilterContext filterContext = FilterUtils.parseFilter(searchRequest.getFilter());

            // 使用 JPA Criteria 执行带过滤的查询
            EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
            Root<UserEntity> root = query.from(UserEntity.class);

            List<Predicate> predicates = getUserPredicates(filterContext, cb, query, root);

            // distinct 并按 username 排序，与无过滤查询保持一致
            query.where(predicates).distinct(true).orderBy(cb.asc(root.get("username")));

            // 执行查询并映射为 UserModel 流
            return closing(paginateQuery(em.createQuery(query), firstResult, maxResults).getResultStream()
                    .map(entity -> new UserAdapter(session, realm, em, entity)));
        } else {
            return session.users().searchForUserStream(realm, Map.of(UserModel.INCLUDE_SERVICE_ACCOUNT, "false"), firstResult, maxResults);
        }
    }

    @Override
    public Long count(SearchRequest searchRequest) {
        RealmModel realm = session.getContext().getRealm();
        if (StringUtil.isNotBlank(searchRequest.getFilter())) {
            // parse filter into AST
            ScimFilterParser.FilterContext filterContext = FilterUtils.parseFilter(searchRequest.getFilter());

            // 使用 JPA 执行带过滤的 count 查询
            EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<UserEntity> root = query.from(UserEntity.class);

            List<Predicate> predicates = this.getUserPredicates(filterContext, cb, query, root);
            query.select(cb.countDistinct(root)).where(predicates);
            return em.createQuery(query).getSingleResult();
        } else {
            return (long) session.users().getUsersCount(realm, false);
        }
    }

    @Override
    public Class<User> getResourceType() {
        return User.class;
    }

    @Override
    public boolean onDelete(String id) {
        RealmModel realm = session.getContext().getRealm();
        return session.users().removeUser(realm, getModel(id));
    }

    @Override
    public void close() {

    }

    /** 将 User Profile {@link ValidationException} 转为 {@link ModelValidationException}。 */
    private ModelValidationException handleValidationException(ValidationException ve) {
        List<Error> errors = ve.getErrors();

        if (errors.isEmpty()) {
            throw new ModelValidationException(ve.getMessage());
        }

        Error firstError = errors.get(0);
        ModelValidationException exception = new ModelValidationException(firstError.getMessage());

        exception.setParameters(firstError.getMessageParameters());

        return exception;
    }

    /** 组装 User 查询谓词：SCIM 过滤、排除服务账户、realm 限制及 FGAP 授权。 */
    private List<Predicate> getUserPredicates(FilterContext filterContext, CriteriaBuilder cb, CriteriaQuery<?> query, Root<UserEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        // 使用与最终查询相同的 root 构建 SCIM 过滤谓词
        ScimJPAPredicateEvaluator evaluator = new ScimJPAPredicateEvaluator(this, getSchemas(), cb, root);
        predicates.add(evaluator.visit(filterContext).predicate());

        // 排除服务账户用户
        predicates.add(root.get("serviceAccountClientLink").isNull());

        // 限制为当前 realm
        RealmModel realm = session.getContext().getRealm();
        predicates.add(cb.equal(root.get("realmId"), realm.getId()));

        UserProvider userProvider = session.getProvider(UserProvider.class, "jpa");
        predicates.addAll(AdminPermissionsSchema.SCHEMA.applyAuthorizationFilters(session, AdminPermissionsSchema.USERS, (PartialEvaluationStorageProvider) userProvider, realm, cb, query, root));

        return predicates;
    }

    /** 为 groups 属性解析用户-组成员 Join 与 groupId 表达式。 */
    @Override
    public Expression<?> getAttributeExpression(Attribute<?, ?> attribute, CriteriaBuilder cb, Root<?> root, BiFunction<Class<?>, Supplier<Join<?, ?>>, Join<?, ?>> joinResolver) {
        if ("groups".equals(attribute.getName())) {
            Join<?, ?> join = joinResolver.apply(UserGroupMembershipEntity.class, () -> root.join(UserGroupMembershipEntity.class));
            join.on(cb.equal(root.get("id"), join.get("user").get("id")));
            return join.get("groupId");
        }
        return null;
    }

    @Override
    protected boolean isManageable(UserModel model) {
        return !session.getContext().getPermissions().isAdminUser(model);
    }
}
