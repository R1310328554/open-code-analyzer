/* admin actions： changelist 批量操作勾选与全选 */
/* global QUnit, Actions *//* global QUnit, Actions */
"use strict";

// 挂载 result-table 并初始化 Actions
QUnit.module("admin.actions", {QUnit.module("admin.actions", {
    beforeEach: function () {
        // Number of results shown on page
        window._actions_icnt = "100";

        const $ = django.jQuery;
        $("#qunit-fixture").append($("#result-table").text());

        Actions(document.querySelectorAll("tr input.action-select"));
    },
});

// action-toggle 切换全部 action-select 勾选状态
QUnit.test("check", function (assert) {
    const $ = django.jQuery;
    assert.notOk($(".action-select").is(":checked"));
    $("#action-toggle").click();
    assert.ok($(".action-select").is(":checked"));
});
