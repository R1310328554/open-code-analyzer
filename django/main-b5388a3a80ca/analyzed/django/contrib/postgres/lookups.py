from django.db.models import Transform
from django.db.models.lookups import PostgresOperatorLookup
from django.db.models.sql.query import Query

from .search import SearchVector, SearchVectorExact, SearchVectorField


# 包含（@>）— 左值包含右值（数组/JSON/范围等）
class DataContains(PostgresOperatorLookup):
    lookup_name = "contains"
    postgres_operator = "@>"


# 被包含（<@）
class ContainedBy(PostgresOperatorLookup):
    lookup_name = "contained_by"
    postgres_operator = "<@"


# 重叠（&&）— 右端为 Query 时包装为 ArraySubquery
class Overlap(PostgresOperatorLookup):
    lookup_name = "overlap"
    postgres_operator = "&&"

    def get_prep_lookup(self):
        from .expressions import ArraySubquery

        if isinstance(self.rhs, Query):
            self.rhs = ArraySubquery(self.rhs)
        return super().get_prep_lookup()


# HStore/JSON 是否含指定键（?）
class HasKey(PostgresOperatorLookup):
    lookup_name = "has_key"
    postgres_operator = "?"
    prepare_rhs = False


# 是否同时包含所有键（?&）
class HasKeys(PostgresOperatorLookup):
    lookup_name = "has_keys"
    postgres_operator = "?&"

    def get_prep_lookup(self):
        return [str(item) for item in self.rhs]


# 是否包含任一键（?|）
class HasAnyKeys(HasKeys):
    lookup_name = "has_any_keys"
    postgres_operator = "?|"


# 去除重音 UNACCENT 变换
class Unaccent(Transform):
    bilateral = True
    lookup_name = "unaccent"
    function = "UNACCENT"


# 全文搜索 @@ 查找 — 非 SearchVectorField 时自动包装 SearchVector
class SearchLookup(SearchVectorExact):
    lookup_name = "search"

    def process_lhs(self, qn, connection):
        if not isinstance(self.lhs.output_field, SearchVectorField):
            config = getattr(self.rhs, "config", None)
            self.lhs = SearchVector(self.lhs, config=config)
        lhs, lhs_params = super().process_lhs(qn, connection)
        return lhs, lhs_params


# 三元组相似度 %% 运算符
class TrigramSimilar(PostgresOperatorLookup):
    lookup_name = "trigram_similar"
    postgres_operator = "%%"


# 词级三元组相似 %%>
class TrigramWordSimilar(PostgresOperatorLookup):
    lookup_name = "trigram_word_similar"
    postgres_operator = "%%>"


# 严格词级三元组相似 %%>>
class TrigramStrictWordSimilar(PostgresOperatorLookup):
    lookup_name = "trigram_strict_word_similar"
    postgres_operator = "%%>>"
