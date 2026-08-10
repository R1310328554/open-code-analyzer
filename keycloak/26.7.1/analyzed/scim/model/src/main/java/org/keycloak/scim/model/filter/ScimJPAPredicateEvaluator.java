package org.keycloak.scim.model.filter;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;

import org.keycloak.scim.filter.FilterUtils;
import org.keycloak.scim.filter.ScimFilterParser;
import org.keycloak.scim.filter.ScimFilterParserBaseVisitor;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;

/**
 * 将 SCIM 过滤 AST 转换为 JPA {@link jakarta.persistence.criteria.Predicate} 的访问者。
 * <p>遍历 {@link org.keycloak.scim.filter.ScimFilterParser} 语法树，委托 {@link ScimJPAPredicateProvider} 生成比较谓词，并处理 OR/AND/NOT 及 valuePath 嵌套逻辑。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ScimJPAPredicateEvaluator extends ScimFilterParserBaseVisitor<JPAFilterResult> {

    /** JPA Criteria 构建器。 */
    private final CriteriaBuilder cb;
    /** 按 SCIM 属性路径创建比较谓词的提供者。 */
    private final ScimJPAPredicateProvider predicateProvider;
    /** valuePath 嵌套时的父属性路径前缀。 */
    private String parentPath;

    /**
     * 构造求值器。
     *
     * @param resourceTypeProvider SCIM 资源类型提供者（用于自定义属性 JPA 映射）
     * @param schemas 可用的 {@link org.keycloak.scim.resource.schema.ModelSchema} 列表
     * @param cb Criteria 构建器
     * @param root 查询根实体
     */
    @SuppressWarnings("unchecked,rawtypes")
    public ScimJPAPredicateEvaluator(ScimResourceTypeProvider resourceTypeProvider, List schemas, CriteriaBuilder cb, Root<?> root) {
        this.cb = cb;
        this.predicateProvider = new ScimJPAPredicateProvider(resourceTypeProvider, schemas, cb, root);
    }

    /** 访问过滤根节点，委托 expression 子树。 */
    @Override
    public JPAFilterResult visitFilter(ScimFilterParser.FilterContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public JPAFilterResult visitExpression(ScimFilterParser.ExpressionContext ctx) {
        if (ctx.OR() != null) {
            JPAFilterResult left = visit(ctx.expression());
            JPAFilterResult right = visit(ctx.andExpression());

            // 逻辑 OR：若一侧不受支持，则结果退化为另一侧
            if (left.unsupported()) return right;
            if (right.unsupported()) return left;

            return JPAFilterResult.valid(cb.or(left.predicate(), right.predicate()));
        }
        return visit(ctx.andExpression());
    }

    @Override
    public JPAFilterResult visitAndExpression(ScimFilterParser.AndExpressionContext ctx) {
        if (ctx.AND() != null) {
            JPAFilterResult left = visit(ctx.andExpression());
            JPAFilterResult right = visit(ctx.notExpression());

            // 任一侧不受支持时，整个 AND 视为不受支持
            if (left.unsupported() || right.unsupported()) {
                return JPAFilterResult.unsupported(cb.disjunction());
            }
            return JPAFilterResult.valid(cb.and(left.predicate(), right.predicate()));
        }
        return visit(ctx.notExpression());
    }

    @Override
    public JPAFilterResult visitNotExpression(ScimFilterParser.NotExpressionContext ctx) {
        if (ctx.NOT() != null) {
            JPAFilterResult child = visit(ctx.notExpression());
            // 子节点因未知属性而不受支持时，按 RFC 7644，not(unknownAttr pr) 仍应返回空集
            if (child.unsupported()) {
                return child;
            }
            return JPAFilterResult.valid(cb.not(child.predicate()));
        }
        return visit(ctx.atom());
    }

    @Override
    public JPAFilterResult visitAtom(ScimFilterParser.AtomContext ctx) {
        if (ctx.valuePath() != null) {
            return visit(ctx.valuePath());
        }
        if (ctx.attributeExpression() != null) {
            return visit(ctx.attributeExpression());
        }
        return visit(ctx.expression());
    }

    /** 进入 valuePath 时设置 parentPath，使嵌套属性路径正确拼接。 */
    @Override
    public JPAFilterResult visitValuePath(ScimFilterParser.ValuePathContext ctx) {
        parentPath = ctx.ATTRPATH().getText();
        try {
            return visit(ctx.expression());
        } finally {
            parentPath = null;
        }
    }

    @Override
    public JPAFilterResult visitPresentExpression(ScimFilterParser.PresentExpressionContext ctx) {
        String scimAttrPath = resolveAttrPath(ctx.ATTRPATH().getText());
        String operator = ctx.PR().getText().toLowerCase();
        return predicateProvider.createPredicate(scimAttrPath, operator, null);
    }

    @Override
    public JPAFilterResult visitComparisonExpression(ScimFilterParser.ComparisonExpressionContext ctx) {
        String scimAttrPath = resolveAttrPath(ctx.ATTRPATH().getText());
        String operator = ctx.compareOp().getText().toLowerCase();
        String value = FilterUtils.extractCompValue(ctx.compValue());

        return predicateProvider.createPredicate(scimAttrPath, operator, value);
    }

    /** 将当前属性路径与 valuePath 父前缀合并。 */
    private String resolveAttrPath(String attrPath) {
        return parentPath != null ? parentPath + "." + attrPath : attrPath;
    }
}
