# 迁移 0002：将 Permission.name 最大长度由 50 增至 255
from django.db import migrations, models


# 仅 AlterField，无数据迁移
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0001_initial"),
    ]

    operations = [
        migrations.AlterField(
            model_name="permission",
            name="name",
            field=models.CharField(max_length=255, verbose_name="name"),
        ),
    ]
