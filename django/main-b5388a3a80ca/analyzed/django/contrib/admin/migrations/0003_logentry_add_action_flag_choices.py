# 迁移 0003：为 action_flag 添加 Addition/Change/Deletion 选项（无数据库结构变更）
from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ("admin", "0002_logentry_remove_auto_add"),
    ]

    # No database changes; adds choices to action_flag.
    operations = [
        migrations.AlterField(
            model_name="logentry",
            name="action_flag",
            field=models.PositiveSmallIntegerField(
                choices=[(1, "Addition"), (2, "Change"), (3, "Deletion")],
                verbose_name="action flag",
            ),
        ),
    ]
