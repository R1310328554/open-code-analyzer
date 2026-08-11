/* admin DateTimeShortcuts 单元测试：日期时间快捷控件与日历 */
/* global QUnit, DateTimeShortcuts *//* global QUnit, DateTimeShortcuts */
"use strict";

// 模块钩子：每个用例后清理 body 时区属性与警告 DOM
QUnit.module("admin.DateTimeShortcuts", {QUnit.module("admin.DateTimeShortcuts", {
    afterEach: function () {
        const $ = django.jQuery;
        $("body")
            .removeAttr("data-admin-server-timezone")
            .removeAttr("data-admin-utc-offset");
        $(".timezonewarning").remove();
    },
});

// 初始化：vDateField 旁出现 Today 与日历按钮
QUnit.test("init", function (assert) {
    const $ = django.jQuery;

    const dateField = $(
        '<input type="text" class="vDateField" value="2015-03-16"><br>',
    );
    $("#qunit-fixture").append(dateField);

    DateTimeShortcuts.init();

    const shortcuts = $(".datetimeshortcuts");
    assert.equal(shortcuts.length, 1);
    assert.equal(shortcuts.find("a:first").text(), "Today");
    assert.equal(shortcuts.find("button:last .date-icon").length, 1);

    // To prevent incorrect timezone warnings on date/time widgets, timezoneOffset
    // should be 0 when a timezone offset isn't set in the HTML body attribute.
    assert.equal(DateTimeShortcuts.timezoneOffset, 0);
});

// 自定义 clockHours 快捷项应出现在时间选择框
QUnit.test("custom time shortcuts", function (assert) {
    const $ = django.jQuery;
    const timeField = $(
        '<input type="text" name="time_test" class="vTimeField">',
    );
    $("#qunit-fixture").append(timeField);
    DateTimeShortcuts.clockHours.time_test = [["3 a.m.", 3]];
    DateTimeShortcuts.init();
    assert.equal($(".clockbox").find("a").first().text(), "3 a.m.");
});

// 单字段时间区与服务器不一致时显示警告文案
QUnit.test("time zone offset warning - single field", function (assert) {
    const $ = django.jQuery;
    const savedOffset = $("body").attr("data-admin-utc-offset");
    // Single date or time field.
    const timeField = $(
        '<input id="id_updated_at" type="text" name="updated_at" class="vTimeField">',
    );
    $("#qunit-fixture").append(timeField);
    $("body").attr(
        "data-admin-utc-offset",
        new Date().getTimezoneOffset() * -60 + 3600,
    );
    $("body").attr("data-admin-server-timezone", "America/Chicago");
    DateTimeShortcuts.init();
    $("body").attr("data-admin-utc-offset", savedOffset);
    assert.equal(
        $(".timezonewarning").text(),
        "Note: Enter times in the America/Chicago timezone. " +
            "(You are 1 hour behind.)",
    );
    assert.equal(
        $(".timezonewarning").attr("id"),
        "id_updated_at_timezone_warning_helptext",
    );
});

// DateTimeField 成对控件使用通用服务器时区提示
QUnit.test("time zone offset warning - date and time field", function (assert) {
    const $ = django.jQuery;
    const savedOffset = $("body").attr("data-admin-utc-offset");
    // DateTimeField with fieldset containing date and time inputs.
    const dateTimeField =
        '<p class="datetime">' +
        '<input id="id_updated_at_0" type="text" name="updated_at_0" class="vDateField">' +
        '<input id="id_updated_at_1" type="text" name="updated_at_1" class="vTimeField">' +
        "</p>";
    $("#qunit-fixture").append($(dateTimeField));
    $("body").attr(
        "data-admin-utc-offset",
        new Date().getTimezoneOffset() * -60 + 3600,
    );
    DateTimeShortcuts.init();
    $("body").attr("data-admin-utc-offset", savedOffset);
    assert.equal(
        $(".timezonewarning").text(),
        "Note: Enter times in the server timezone. (You are 1 hour behind.)",
    );
    assert.equal(
        $(".timezonewarning").attr("id"),
        "id_updated_at_timezone_warning_helptext",
    );
});

// 日历上一月/下一月链接的 aria-label 随当前月更新
QUnit.test("update aria labels - previous and next months", function (assert) {
    const $ = django.jQuery;
    const dateField = $('<input type="text" class="vDateField">');
    $("#qunit-fixture").append(dateField);
    DateTimeShortcuts.init();
    const num = DateTimeShortcuts.calendars.length - 1;
    const cal = DateTimeShortcuts.calendars[num];
    // Set to January 2026
    cal.currentMonth = 1;
    cal.currentYear = 2026;
    DateTimeShortcuts.updateNavAriaLabels(num);
    const cal_box = document.getElementById(
        DateTimeShortcuts.calendarDivName1 + num,
    );
    const prevLabel = cal_box
        .querySelector(".calendarnav-previous")
        .getAttribute("aria-label");
    const nextLabel = cal_box
        .querySelector(".calendarnav-next")
        .getAttribute("aria-label");
    assert.equal(prevLabel, "Previous (December 2025)");
    assert.equal(nextLabel, "Next (February 2026)");
});

// Today 链接 aria-label 含本地化当前日期
QUnit.test("today link has aria-label with current date", function (assert) {
    const $ = django.jQuery;
    const dateField = $(
        '<input type="text" class="vDateField" value="2026-04-12"><br>',
    );
    $("#qunit-fixture").append(dateField);
    DateTimeShortcuts.init();
    const todayLink = $(".datetimeshortcuts a:first");
    assert.equal(todayLink.text(), "Today");
    // "Today (April 12, 2026)"
    const today = new Date();
    const formattedDate = today.toLocaleDateString("en-US", {
        month: "long",
        day: "numeric",
        year: "numeric",
    });
    const expectedAriaLabel = `Today (${formattedDate})`;
    assert.equal(todayLink.attr("aria-label"), expectedAriaLabel);
});

// 服务器 UTC 偏移与浏览器不同时，高亮格应对齐服务器“今天”
QUnit.test("calendar today highlight with server offset", function (assert) {
    const $ = django.jQuery;
    const calDiv = $('<div id="test-calendar"></div>');
    $("#qunit-fixture").append(calDiv);

    // Simulate a server timezone that is 24 hours ahead of the browser.
    const localOffset = new Date().getTimezoneOffset() * -60;
    const serverOffset = localOffset + 86400;
    $("body").attr("data-admin-utc-offset", serverOffset);

    const expectedDate = new Date();
    expectedDate.setTime(
        expectedDate.getTime() + 1000 * (serverOffset - localOffset),
    );

    CalendarNamespace.draw(
        expectedDate.getMonth() + 1,
        expectedDate.getFullYear(),
        "test-calendar",
        function () {},
    );

    const todayCells = calDiv.find("td.today");
    assert.equal(todayCells.length, 1, "Exactly one cell marked as today");
    assert.equal(
        todayCells.find("a").text(),
        String(expectedDate.getDate()),
        "Today cell matches server-adjusted date",
    );
});
