"""
django.db.models.sql.constants — ORM SQL 层常量。

迭代分块大小、cursor 返回类型与 JOIN/ORDER 方向映射。
"""
"""
Constants specific to the SQL storage portion of the ORM.
"""

# Size of each "chunk" for get_iterator calls.
# Larger values are slightly faster at the expense of more storage space.
# get_iterator 每次从数据库拉取的行数
GET_ITERATOR_CHUNK_SIZE = 100GET_ITERATOR_CHUNK_SIZE = 100

# Namedtuples for sql.* internal use.

# How many results to expect from a cursor.execute call
# execute_sql 期望返回多行结果
MULTI = "multi"MULTI = "multi"
# execute_sql 期望返回单行
SINGLE = "single"SINGLE = "single"
# execute_sql 不取结果集
NO_RESULTS = "no results"NO_RESULTS = "no results"
# Rather than returning results, returns:
# execute_sql 返回原始 cursor
CURSOR = "cursor"CURSOR = "cursor"
# execute_sql 返回受影响行数
ROW_COUNT = "row count"ROW_COUNT = "row count"

ORDER_DIR = {
    "ASC": ("ASC", "DESC"),
    "DESC": ("DESC", "ASC"),
}

# SQL join types.
# 内连接类型标识
INNER = "INNER JOIN"INNER = "INNER JOIN"
# 左外连接类型标识
LOUTER = "LEFT OUTER JOIN"LOUTER = "LEFT OUTER JOIN"
