/* admin URLify：将标题 slug 化为 URL 安全字符串 */
/* global QUnit, URLify *//* global QUnit, URLify */
"use strict";

// URLify  slug 规则测试
QUnit.module("admin.URLify");

// 空输入返回空 slug
QUnit.test("empty string", function (assert) {
    assert.strictEqual(URLify("", 8, true), "");
});

// allowUnicode 时保留停用词 the 等
QUnit.test("preserve nonessential words", function (assert) {
    assert.strictEqual(URLify("the D is silent", 15, true), "the-d-is-silent");
});

// 剥离 #@ 等非 URL 字符
QUnit.test("strip non-URL characters", function (assert) {
    assert.strictEqual(URLify("D#silent@", 7, true), "dsilent");
});

// 连续空白合并为单个连字符
QUnit.test("merge adjacent whitespace", function (assert) {
    assert.strictEqual(URLify("D   silent", 8, true), "d-silent");
});

// 超长截断后去掉尾部连字符
QUnit.test("trim trailing hyphens", function (assert) {
    assert.strictEqual(URLify("D silent always", 9, true), "d-silent");
});

// 非 ASCII 字符在允许时保留
QUnit.test("non-ASCII string", function (assert) {
    assert.strictEqual(URLify("Kaupa-miða", 255, true), "kaupa-miða");
});
