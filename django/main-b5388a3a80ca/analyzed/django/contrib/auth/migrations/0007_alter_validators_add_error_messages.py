# 迁移 0007：username 字段补充 validators 与 error_messages 元数据
from django.contrib.auth import validators
from django.db import migrations, models


# 无数据库结构变更
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0006_require_contenttypes_0002"),
    ]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="username",
            field=models.CharField(
                error_messages={"unique": "A user with that username already exists."},
                help_text=(
                    "Required. 30 characters or fewer. Letters, digits and @/./+/-/_ "
                    "only."
                ),
                max_length=30,
                unique=True,
                validators=[validators.UnicodeUsernameValidator()],
                verbose_name="username",
            ),
        ),
    ]
