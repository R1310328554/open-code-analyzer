from django.db import NotSupportedError
from django.db.models.expressions import Func, Value
from django.db.models.fields import CharField, IntegerField, TextField
from django.db.models.functions import Cast, Coalesce
from django.db.models.lookups import Transform

"""
django.db.models.functions.text — 文本处理与哈希数据库函数。

连接/截取/大小写/填充/哈希等，含各后端 SHA 实现 Mixin。
"""

# MySQL：SHA2 函数模板适配
class MySQLSHA2Mixin:from django.db.models.lookups import Transform


class MySQLSHA2Mixin:
    def as_mysql(self, compiler, connection, **extra_context):
        return super().as_sql(
            compiler,
            connection,
            template="SHA2(%%(expressions)s, %s)" % self.function[3:],
            **extra_context,
        )


# Oracle：STANDARD_HASH + RAWTOHEX 实现哈希
class OracleHashMixin:
    def as_oracle(self, compiler, connection, **extra_context):
        return super().as_sql(
            compiler,
            connection,
            template=(
                "LOWER(RAWTOHEX(STANDARD_HASH(UTL_I18N.STRING_TO_RAW("
                "%(expressions)s, 'AL32UTF8'), '%(function)s')))"
            ),
            **extra_context,
        )


# PostgreSQL：pgcrypto DIGEST + ENCODE 实现哈希
class PostgreSQLSHAMixin:
    def as_postgresql(self, compiler, connection, **extra_context):
        return super().as_sql(
            compiler,
            connection,
            template="ENCODE(DIGEST(%(expressions)s, '%(function)s'), 'hex')",
            function=self.function.lower(),
            **extra_context,
        )


# 码点转字符 CHR/CHAR
class Chr(Transform):
    function = "CHR"
    lookup_name = "chr"
    output_field = CharField()

    def as_mysql(self, compiler, connection, **extra_context):
        return super().as_sql(
            compiler,
            connection,
            function="CHAR",
            template="%(function)s(%(expressions)s USING utf16)",
            **extra_context,
        )

    def as_oracle(self, compiler, connection, **extra_context):
        return super().as_sql(
            compiler,
            connection,
            template="%(function)s(%(expressions)s USING NCHAR_CS)",
            **extra_context,
        )

    def as_sqlite(self, compiler, connection, **extra_context):
        return super().as_sql(compiler, connection, function="CHAR", **extra_context)


# 连接两个字符串；Concat 递归调用以支持多参数
class ConcatPair(Func):
    """
    Concatenate two arguments together. This is used by `Concat` because not
    all backend databases support more than two arguments.
    """

    function = "CONCAT"

    def pipes_concat_sql(self, compiler, connection, **extra_context):
        coalesced = self.coalesce()
        return super(ConcatPair, coalesced).as_sql(
            compiler,
            connection,
            template="(%(expressions)s)",
            arg_joiner=" || ",
            **extra_context,
        )

    as_sqlite = pipes_concat_sql

    def as_postgresql(self, compiler, connection, **extra_context):
        c = self.copy()
        c.set_source_expressions(
            [
                (
                    expression
                    if isinstance(expression.output_field, (CharField, TextField))
                    else Cast(expression, TextField())
                )
                for expression in c.get_source_expressions()
            ]
        )
        return c.pipes_concat_sql(compiler, connection, **extra_context)

    def as_mysql(self, compiler, connection, **extra_context):
        # Use CONCAT_WS with an empty separator so that NULLs are ignored.
        return super().as_sql(
            compiler,
            connection,
            function="CONCAT_WS",
            template="%(function)s('', %(expressions)s)",
            **extra_context,
        )

    def coalesce(self):
        # null on either side results in null for expression, wrap with
        # coalesce
        c = self.copy()
        c.set_source_expressions(
            [
                Coalesce(expression, Value(""))
                for expression in c.get_source_expressions()
            ]
        )
        return c


# 多字符串连接；NULL 时部分后端用 Coalesce 包装
class Concat(Func):
    """
    Concatenate text fields together. Backends that result in an entire
    null expression when any arguments are null will wrap each argument in
    coalesce functions to ensure a non-null result.
    """

    function = None
    template = "%(expressions)s"

    def __init__(self, *expressions, **extra):
        if len(expressions) < 2:
            raise ValueError("Concat must take at least two expressions")
        paired = self._paired(expressions, output_field=extra.get("output_field"))
        super().__init__(paired, **extra)

    def _paired(self, expressions, output_field):
        # wrap pairs of expressions in successive concat functions
        # exp = [a, b, c, d]
        # -> ConcatPair(a, ConcatPair(b, ConcatPair(c, d))))
        if len(expressions) == 2:
            return ConcatPair(*expressions, output_field=output_field)
        return ConcatPair(
            expressions[0],
            self._paired(expressions[1:], output_field=output_field),
            output_field=output_field,
        )


# 取字符串左侧 n 个字符 LEFT
class Left(Func):
    function = "LEFT"
    arity = 2
    output_field = CharField()

    def __init__(self, expression, length, **extra):
        """
        expression: the name of a field, or an expression returning a string
        length: the number of characters to return from the start of the string
        """
        if not hasattr(length, "resolve_expression"):
            if length < 1:
                raise ValueError("'length' must be greater than 0.")
        super().__init__(expression, length, **extra)

    def get_substr(self):
        return Substr(self.source_expressions[0], Value(1), self.source_expressions[1])

    def as_oracle(self, compiler, connection, **extra_context):
        return self.get_substr().as_oracle(compiler, connection, **extra_context)

    def as_sqlite(self, compiler, connection, **extra_context):
        return self.get_substr().as_sqlite(compiler, connection, **extra_context)


# 字符串长度 LENGTH/CHAR_LENGTH
class Length(Transform):
    """Return the number of characters in the expression."""

    function = "LENGTH"
    lookup_name = "length"
    output_field = IntegerField()

    def as_mysql(self, compiler, connection, **extra_context):
        return super().as_sql(
            compiler, connection, function="CHAR_LENGTH", **extra_context
        )


# 转小写 LOWER
class Lower(Transform):
    function = "LOWER"
    lookup_name = "lower"


# 左侧填充 LPAD
class LPad(Func):
    function = "LPAD"
    output_field = CharField()

    def __init__(self, expression, length, fill_text=Value(" "), **extra):
        if (
            not hasattr(length, "resolve_expression")
            and length is not None
            and length < 0
        ):
            raise ValueError("'length' must be greater or equal to 0.")
        super().__init__(expression, length, fill_text, **extra)


# 去左空白 LTRIM
class LTrim(Transform):
    function = "LTRIM"
    lookup_name = "ltrim"


# MD5 哈希
class MD5(OracleHashMixin, Transform):
    function = "MD5"
    lookup_name = "md5"


# 首字符码点 ASCII/ORD/UNICODE
class Ord(Transform):
    function = "ASCII"
    lookup_name = "ord"
    output_field = IntegerField()

    def as_mysql(self, compiler, connection, **extra_context):
        return super().as_sql(compiler, connection, function="ORD", **extra_context)

    def as_sqlite(self, compiler, connection, **extra_context):
        return super().as_sql(compiler, connection, function="UNICODE", **extra_context)


# 重复字符串 REPEAT
class Repeat(Func):
    function = "REPEAT"
    output_field = CharField()

    def __init__(self, expression, number, **extra):
        if (
            not hasattr(number, "resolve_expression")
            and number is not None
            and number < 0
        ):
            raise ValueError("'number' must be greater or equal to 0.")
        super().__init__(expression, number, **extra)

    def as_oracle(self, compiler, connection, **extra_context):
        expression, number = self.source_expressions
        length = None if number is None else Length(expression) * number
        rpad = RPad(expression, length, expression)
        return rpad.as_sql(compiler, connection, **extra_context)


# 子串替换 REPLACE
class Replace(Func):
    function = "REPLACE"

    def __init__(self, expression, text, replacement=Value(""), **extra):
        super().__init__(expression, text, replacement, **extra)


# 反转字符串 REVERSE；Oracle 用 LISTAGG 子查询
class Reverse(Transform):
    function = "REVERSE"
    lookup_name = "reverse"

    def as_oracle(self, compiler, connection, **extra_context):
        # REVERSE in Oracle is undocumented and doesn't support multi-byte
        # strings. Use a special subquery instead.
        suffix = connection.features.bare_select_suffix
        sql, params = super().as_sql(
            compiler,
            connection,
            template=(
                "(SELECT LISTAGG(s) WITHIN GROUP (ORDER BY n DESC) FROM "
                f"(SELECT LEVEL n, SUBSTR(%(expressions)s, LEVEL, 1) s{suffix} "
                "CONNECT BY LEVEL <= LENGTH(%(expressions)s)) "
                "GROUP BY %(expressions)s)"
            ),
            **extra_context,
        )
        return sql, params * 3


# 取字符串右侧 n 个字符 RIGHT
class Right(Left):
    function = "RIGHT"

    def get_substr(self):
        return Substr(
            self.source_expressions[0],
            self.source_expressions[1] * Value(-1),
            self.source_expressions[1],
        )


# 右侧填充 RPAD
class RPad(LPad):
    function = "RPAD"


# 去右空白 RTRIM
class RTrim(Transform):
    function = "RTRIM"
    lookup_name = "rtrim"


# SHA1 哈希
class SHA1(OracleHashMixin, PostgreSQLSHAMixin, Transform):
    function = "SHA1"
    lookup_name = "sha1"


# SHA224 哈希；Oracle 不支持
class SHA224(MySQLSHA2Mixin, PostgreSQLSHAMixin, Transform):
    function = "SHA224"
    lookup_name = "sha224"

    def as_oracle(self, compiler, connection, **extra_context):
        raise NotSupportedError("SHA224 is not supported on Oracle.")


# SHA256 哈希
class SHA256(MySQLSHA2Mixin, OracleHashMixin, PostgreSQLSHAMixin, Transform):
    function = "SHA256"
    lookup_name = "sha256"


# SHA384 哈希
class SHA384(MySQLSHA2Mixin, OracleHashMixin, PostgreSQLSHAMixin, Transform):
    function = "SHA384"
    lookup_name = "sha384"


# SHA512 哈希
class SHA512(MySQLSHA2Mixin, OracleHashMixin, PostgreSQLSHAMixin, Transform):
    function = "SHA512"
    lookup_name = "sha512"


# 子串首次出现位置 INSTR/STRPOS（1 索引，未找到为 0）
class StrIndex(Func):
    """
    Return a positive integer corresponding to the 1-indexed position of the
    first occurrence of a substring inside another string, or 0 if the
    substring is not found.
    """

    function = "INSTR"
    arity = 2
    output_field = IntegerField()

    def as_postgresql(self, compiler, connection, **extra_context):
        return super().as_sql(compiler, connection, function="STRPOS", **extra_context)


# 子串 SUBSTRING/SUBSTR
class Substr(Func):
    function = "SUBSTRING"
    output_field = CharField()

    def __init__(self, expression, pos, length=None, **extra):
        """
        expression: the name of a field, or an expression returning a string
        pos: an integer > 0, or an expression returning an integer
        length: an optional number of characters to return
        """
        if not hasattr(pos, "resolve_expression"):
            if pos < 1:
                raise ValueError("'pos' must be greater than 0")
        expressions = [expression, pos]
        if length is not None:
            expressions.append(length)
        super().__init__(*expressions, **extra)

    def as_sqlite(self, compiler, connection, **extra_context):
        return super().as_sql(compiler, connection, function="SUBSTR", **extra_context)

    def as_oracle(self, compiler, connection, **extra_context):
        return super().as_sql(compiler, connection, function="SUBSTR", **extra_context)


# 去两端空白 TRIM
class Trim(Transform):
    function = "TRIM"
    lookup_name = "trim"


# 转大写 UPPER
class Upper(Transform):
    function = "UPPER"
    lookup_name = "upper"
