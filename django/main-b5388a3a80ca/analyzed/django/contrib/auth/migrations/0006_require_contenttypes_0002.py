# 迁移 0006：声明依赖 contenttypes.0002，确保 post_migrate 前 ContentType 就绪
from django.db import migrations


# 空 operations，仅调整迁移依赖顺序
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0005_alter_user_last_login_null"),
        ("contenttypes", "0002_remove_content_type_name"),
    ]

    operations = [
        # Ensure the contenttypes migration is applied before sending
        # post_migrate signals (which create ContentTypes).
    ]
