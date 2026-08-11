/* admin 侧边栏导航：nav-filter 按模型名快速过滤可见行 */
/* global QUnit, initSidebarQuickFilter *//* global QUnit, initSidebarQuickFilter */
"use strict";

// 模块钩子：从 fixture 注入 nav-sidebar DOM 并初始化过滤器
QUnit.module("admin.sidebar: filter", {QUnit.module("admin.sidebar: filter", {
    beforeEach: function () {
        const $ = django.jQuery;
        $("#qunit-fixture").append($("#nav-sidebar-filter").text());
        this.navSidebar = $("#nav-sidebar");
        this.navFilter = $("#nav-filter");
        initSidebarQuickFilter();
    },
});

// 输入子串应只显示匹配的 model-* 行；无匹配则全部隐藏
QUnit.test("filter by a model name", function (assert) {QUnit.test("filter by a model name", function (assert) {
    assert.equal(this.navSidebar.find("th[scope=row] a").length, 2);

    this.navFilter.val("us"); // Matches 'users'.
    this.navFilter[0].dispatchEvent(new Event("change"));
    assert.equal(this.navSidebar.find('tr[class^="model-"]:visible').length, 1);

    this.navFilter.val("nonexistent");
    this.navFilter[0].dispatchEvent(new Event("change"));
    assert.equal(this.navSidebar.find('tr[class^="model-"]:visible').length, 0);
});
