# 迁移 0012：User.first_name 最大长度由 30 增至 150
from django.db import migrations, models


# 仅 AlterField
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0011_update_proxy_permissions"),
    ]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="first_name",
            field=models.CharField(
                blank=True, max_length=150, verbose_name="first name"
            ),
        ),
    ]
