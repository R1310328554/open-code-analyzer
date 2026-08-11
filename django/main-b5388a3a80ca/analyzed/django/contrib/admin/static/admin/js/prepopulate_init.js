// 变更页启动脚本：为 prepopulated 字段绑定 prepopulate 插件
"use strict";
{
    const $ = django.jQuery;
    // 从模板注入的常量读取字段选择器、依赖与 maxLength 配置
    const fields = $("#django-admin-prepopulated-fields-constants").data(
        "prepopulatedFields",
    );
    // 标记 .prepopulated_field 并初始化 empty-form 与已有行的自动填充
    $.each(fields, function (index, field) {
        $(
            ".empty-form .form-row .field-" +
                field.name +
                ", .empty-form.form-row .field-" +
                field.name +
                ", .empty-form .form-row.field-" +
                field.name,
        ).addClass("prepopulated_field");
        $(field.id)
            .data("dependency_list", field.dependency_list)
            .prepopulate(
                field.dependency_ids,
                field.maxLength,
                field.allowUnicode,
            );
    });
}
