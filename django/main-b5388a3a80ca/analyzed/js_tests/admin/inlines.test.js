/* admin.inlines：tabular 内联 formset 增删与 min/max 约束 */
/* global QUnit *//* global QUnit */
"use strict";

// 表格内联：prefix、addText 与 dynamic 行
QUnit.module("admin.inlines: tabular formsets", {QUnit.module("admin.inlines: tabular formsets", {
    beforeEach: function () {
        const $ = django.jQuery;

        this.addText = "Add another";

        $("#qunit-fixture").append($("#tabular-formset").text());
        this.table = $("table.inline");
        this.inlineRow = this.table.find("tr");
        this.inlineRow.tabularFormset("table.inline tr.form-row", {
            prefix: "first",
            addText: this.addText,
            deleteText: "Remove",
        });
    },
});

// 初始仅模板行与添加链接
QUnit.test("no forms", function (assert) {
    assert.ok(this.inlineRow.hasClass("dynamic-first"));
    assert.equal(this.table.find(".add-row a").text(), this.addText);
});

// 点击添加生成 first-N 新行
QUnit.test("add form", function (assert) {
    const addButton = this.table.find(".add-row a");
    assert.equal(addButton.text(), this.addText);
    addButton.click();
    assert.ok(this.table.find("#first-1"));
});

// 动态行显示 inline-deletelink
QUnit.test("added form has remove button", function (assert) {
    const addButton = this.table.find(".add-row a");
    assert.equal(addButton.text(), this.addText);
    addButton.click();
    assert.equal(this.table.find("#first-1 .inline-deletelink").length, 1);
});

// 触发 formset:added / formset:removed 事件
QUnit.test("add/remove form events", function (assert) {
    assert.expect(5);
    const addButton = this.table.find(".add-row a");
    document.addEventListener(
        "formset:added",
        (event) => {
            assert.ok(true, "event `formset:added` triggered");
            assert.equal(true, event.target.matches("#first-1"));
            assert.equal(event.detail.formsetName, "first");
        },
        { once: true },
    );
    addButton.click();
    const deleteLink = this.table.find(".inline-deletelink");
    document.addEventListener(
        "formset:removed",
        (event) => {
            assert.ok(true, "event `formset:removed` triggered");
            assert.equal(event.detail.formsetName, "first");
        },
        { once: true },
    );
    deleteLink.click();
});

// 自定义 addButton 替代默认 add-row
QUnit.test("existing add button", function (assert) {
    const $ = django.jQuery;
    $("#qunit-fixture").empty(); // Clear the table added in beforeEach
    $("#qunit-fixture").append($("#tabular-formset").text());
    this.table = $("table.inline");
    this.inlineRow = this.table.find("tr");
    this.table.append('<i class="add-button"></i>');
    const addButton = this.table.find(".add-button");
    this.inlineRow.tabularFormset("table.inline tr", {
        prefix: "first",
        deleteText: "Remove",
        addButton: addButton,
    });
    assert.equal(this.table.find(".add-row a").length, 0);
    addButton.click();
    assert.ok(this.table.find("#first-1"));
});

// 含校验错误与 has_original 行的 formset
QUnit.module("admin.inlines: tabular formsets with validation errors", {QUnit.module("admin.inlines: tabular formsets with validation errors", {
    beforeEach: function () {
        const $ = django.jQuery;

        $("#qunit-fixture").append(
            $("#tabular-formset-with-validation-error").text(),
        );
        this.table = $("table.inline");
        this.inlineRows = this.table.find("tr.form-row");
        this.inlineRows.tabularFormset("table.inline tr.form-row", {
            prefix: "second",
        });
    },
});

// 已有实例行用删除 checkbox 而非链接
QUnit.test("first form has delete checkbox and no button", function (assert) {
    const tr = this.inlineRows.slice(0, 1);
    assert.ok(tr.hasClass("dynamic-second"));
    assert.ok(tr.hasClass("has_original"));
    assert.equal(tr.find("td.delete input").length, 1);
    assert.equal(tr.find("td.delete .inline-deletelink").length, 0);
});

// 非 original 动态行用删除链接
QUnit.test("dynamic form has remove button", function (assert) {
    const tr = this.inlineRows.slice(1, 2);
    assert.ok(tr.hasClass("dynamic-second"));
    assert.notOk(tr.hasClass("has_original"));
    assert.equal(tr.find(".inline-deletelink").length, 1);
});

// empty-form 模板行无删除控件
QUnit.test("dynamic template has nothing", function (assert) {
    const tr = this.inlineRows.slice(2, 3);
    assert.ok(tr.hasClass("empty-form"));
    assert.notOk(tr.hasClass("dynamic-second"));
    assert.notOk(tr.hasClass("has_original"));
    assert.equal(tr.find("td.delete")[0].innerHTML, "");
});

// 删除行时一并移除 row-form-errors 行
QUnit.test(
    "removing a form-row also removed related row with non-field errors",QUnit.test(
    "removing a form-row also removed related row with non-field errors",
    function (assert) {
        const $ = django.jQuery;
        assert.ok(this.table.find(".row-form-errors").length);
        const tr = this.inlineRows.slice(1, 2);
        const trWithErrors = tr.prev();
        assert.ok(trWithErrors.hasClass("row-form-errors"));
        const deleteLink = tr.find("a.inline-deletelink");
        deleteLink.trigger($.Event("click", { target: deleteLink }));
        assert.notOk(this.table.find(".row-form-errors").length);
    },
);

// max_num 达上限时隐藏添加按钮
QUnit.module("admin.inlines: tabular formsets with max_num", {QUnit.module("admin.inlines: tabular formsets with max_num", {
    beforeEach: function () {
        const $ = django.jQuery;
        $("#qunit-fixture").append(
            $("#tabular-formset-with-validation-error").text(),
        );
        this.table = $("table.inline");
        this.maxNum = $("input.id_second-MAX_NUM_FORMS");
        this.maxNum.val(2);
        this.inlineRows = this.table.find("tr.form-row");
        this.inlineRows.tabularFormset("table.inline tr.form-row", {
            prefix: "second",
        });
    },
});

// 已达 max_num 时不显示添加
QUnit.test(
    "does not show the add button if already at max_num",QUnit.test(
    "does not show the add button if already at max_num",
    function (assert) {
        const addButton = this.table.find("tr.add_row > td > a");
        assert.notOk(addButton.is(":visible"));
    },
);

// 删除一行后仍受 max 限制（按钮不可见）
QUnit.test("make addButton visible again", function (assert) {
    const $ = django.jQuery;
    const addButton = this.table.find("tr.add_row > td > a");
    const removeButton = this.table
        .find("tr.form-row:first")
        .find("a.inline-deletelink");
    removeButton.trigger($.Event("click", { target: removeButton }));
    assert.notOk(addButton.is(":visible"));
});

// min_num 达下限时隐藏删除按钮
QUnit.module("admin.inlines: tabular formsets with min_num", {QUnit.module("admin.inlines: tabular formsets with min_num", {
    beforeEach: function () {
        const $ = django.jQuery;
        $("#qunit-fixture").append(
            $("#tabular-formset-with-validation-error").text(),
        );
        this.table = $("table.inline");
        this.minNum = $("input#id_second-MIN_NUM_FORMS");
        this.minNum.val(2);
        this.inlineRows = this.table.find("tr.form-row");
        this.inlineRows.tabularFormset("table.inline tr.form-row", {
            prefix: "second",
        });
    },
});

// 已达 min_num 时不显示删除链接
QUnit.test(
    "does not show the remove buttons if already at min_num",QUnit.test(
    "does not show the remove buttons if already at min_num",
    function (assert) {
        assert.notOk(this.table.find(".inline-deletelink:visible").length);
    },
);

// 添加一行后删除按钮重新可见
QUnit.test("make removeButtons visible again", function (assert) {
    const $ = django.jQuery;
    const addButton = this.table.find("tr.add-row > td > a");
    addButton.trigger($.Event("click", { target: addButton }));
    assert.equal(this.table.find(".inline-deletelink:visible").length, 2);
});
