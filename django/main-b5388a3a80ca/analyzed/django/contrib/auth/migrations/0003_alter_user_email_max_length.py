# 迁移 0003：User.email 最大长度由 75 增至 254（RFC 5321）
from django.db import migrations, models


# 仅 AlterField
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0002_alter_permission_name_max_length"),
    ]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="email",
            field=models.EmailField(
                max_length=254, verbose_name="email address", blank=True
            ),
        ),
    ]
