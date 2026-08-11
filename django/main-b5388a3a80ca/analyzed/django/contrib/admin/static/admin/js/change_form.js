// change_form.js — 打开变更/新增表单时聚焦第一个可编辑字段
"use strict";
{
    const inputTags = ["BUTTON", "INPUT", "SELECT", "TEXTAREA"];
    const modelName = document.getElementById("django-admin-form-add-constants")
        .dataset.modelName;
    if (modelName) {
        const form = document.getElementById(modelName + "_form");
        for (const element of form.elements) {
            // HTMLElement.offsetParent returns null when the element is not
            // rendered.
            if (
                inputTags.includes(element.tagName) &&
                !element.disabled &&
                element.offsetParent
            ) {
                element.focus();
                break;
            }
        }
    }
}
