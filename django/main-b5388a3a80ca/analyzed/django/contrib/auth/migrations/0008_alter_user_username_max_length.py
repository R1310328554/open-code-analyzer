# 迁移 0008：User.username 最大长度由 30 增至 150
from django.contrib.auth import validators
from django.db import migrations, models


# AlterField 并同步 help_text
class Migration(migrations.Migration):
    dependencies = [
        ("auth", "0007_alter_validators_add_error_messages"),
    ]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="username",
            field=models.CharField(
                error_messages={"unique": "A user with that username already exists."},
                help_text=(
                    "Required. 150 characters or fewer. Letters, digits and @/./+/-/_ "
                    "only."
                ),
                max_length=150,
                unique=True,
                validators=[validators.UnicodeUsernameValidator()],
                verbose_name="username",
            ),
        ),
    ]
