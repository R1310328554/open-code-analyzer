# 迁移 0005：User.last_login 改为 nullable，允许从未登录的用户
from django.db import migrations, models


# 仅 AlterField
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0004_alter_user_username_opts"),
    ]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="last_login",
            field=models.DateTimeField(
                null=True, verbose_name="last login", blank=True
            ),
        ),
    ]
