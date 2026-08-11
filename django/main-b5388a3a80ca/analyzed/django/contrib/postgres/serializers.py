from django.db.migrations.serializer import BaseSerializer


# 迁移序列化器 — 将 psycopg range 对象序列化为模块引用
class RangeSerializer(BaseSerializer):
    # 修正 psycopg2._range 为公开导入路径 psycopg2.extras
    def serialize(self):
        module = self.value.__class__.__module__
        # Ranges are implemented in psycopg2._range but the public import
        # location is psycopg2.extras.
        module = "psycopg2.extras" if module == "psycopg2._range" else module
        return "%s.%r" % (module, self.value), {"import %s" % module}
