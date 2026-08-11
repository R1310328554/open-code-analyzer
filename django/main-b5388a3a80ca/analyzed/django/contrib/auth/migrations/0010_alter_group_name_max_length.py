# 迁移 0010：Group.name 最大长度由 80 增至 150
from django.db import migrations, models


# 仅 AlterField
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0009_alter_user_last_name_max_length"),
    ]

    operations = [
        migrations.AlterField(
            model_name="group",
            name="name",
            field=models.CharField(max_length=150, unique=True, verbose_name="name"),
        ),
    ]
