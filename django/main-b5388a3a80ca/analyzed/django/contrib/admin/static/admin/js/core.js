// core.js — Admin 前端通用 DOM 工具与 Date/String 扩展
// Core JavaScript helper functions
"use strict";

// quickElement(tagType, parentReference [, textInChildNode, attribute, attributeValue ...]);
// 快速创建元素、设置属性并挂到父节点
function quickElement() {
    const obj = document.createElement(arguments[0]);
    if (arguments[2]) {
        const textNode = document.createTextNode(arguments[2]);
        obj.appendChild(textNode);
    }
    const len = arguments.length;
    for (let i = 3; i < len; i += 2) {
        obj.setAttribute(arguments[i], arguments[i + 1]);
    }
    arguments[1].appendChild(obj);
    return obj;
}

// "a" is reference to an object
// 清空节点的所有子元素
function removeChildren(a) {
    while (a.hasChildNodes()) {
        a.removeChild(a.lastChild);
    }
}

// ----------------------------------------------------------------------------
// Find-position functions by PPK
// See https://www.quirksmode.org/js/findpos.html
// ----------------------------------------------------------------------------
// 计算元素相对文档的 X 坐标（PPK findPos 算法）
function findPosX(obj) {
    let curleft = 0;
    if (obj.offsetParent) {
        while (obj.offsetParent) {
            curleft += obj.offsetLeft - obj.scrollLeft;
            obj = obj.offsetParent;
        }
    } else if (obj.x) {
        curleft += obj.x;
    }
    return curleft;
}

// 计算元素相对文档的 Y 坐标
function findPosY(obj) {
    let curtop = 0;
    if (obj.offsetParent) {
        while (obj.offsetParent) {
            curtop += obj.offsetTop - obj.scrollTop;
            obj = obj.offsetParent;
        }
    } else if (obj.y) {
        curtop += obj.y;
    }
    return curtop;
}

//-----------------------------------------------------------------------------
// Date object extensions
// ----------------------------------------------------------------------------
{
    Date.prototype.getTwelveHours = function () {
        return this.getHours() % 12 || 12;
    };

    Date.prototype.getTwoDigitMonth = function () {
        return this.getMonth() < 9
            ? "0" + (this.getMonth() + 1)
            : this.getMonth() + 1;
    };

    Date.prototype.getTwoDigitDate = function () {
        return this.getDate() < 10 ? "0" + this.getDate() : this.getDate();
    };

    Date.prototype.getTwoDigitTwelveHour = function () {
        return this.getTwelveHours() < 10
            ? "0" + this.getTwelveHours()
            : this.getTwelveHours();
    };

    Date.prototype.getTwoDigitHour = function () {
        return this.getHours() < 10 ? "0" + this.getHours() : this.getHours();
    };

    Date.prototype.getTwoDigitMinute = function () {
        return this.getMinutes() < 10
            ? "0" + this.getMinutes()
            : this.getMinutes();
    };

    Date.prototype.getTwoDigitSecond = function () {
        return this.getSeconds() < 10
            ? "0" + this.getSeconds()
            : this.getSeconds();
    };

    Date.prototype.getAbbrevDayName = function () {
        return typeof window.CalendarNamespace === "undefined"
            ? "0" + this.getDay()
            : window.CalendarNamespace.daysOfWeekAbbrev[this.getDay()];
    };

    Date.prototype.getFullDayName = function () {
        return typeof window.CalendarNamespace === "undefined"
            ? "0" + this.getDay()
            : window.CalendarNamespace.daysOfWeek[this.getDay()];
    };

    Date.prototype.getAbbrevMonthName = function () {
        return typeof window.CalendarNamespace === "undefined"
            ? this.getTwoDigitMonth()
            : window.CalendarNamespace.monthsOfYearAbbrev[this.getMonth()];
    };

    Date.prototype.getFullMonthName = function () {
        return typeof window.CalendarNamespace === "undefined"
            ? this.getTwoDigitMonth()
            : window.CalendarNamespace.monthsOfYear[this.getMonth()];
    };

    // 按 strftime 风格格式化 Date（供日历 widget 使用）
    Date.prototype.strftime = function (format) {
        const fields = {
            a: this.getAbbrevDayName(),
            A: this.getFullDayName(),
            b: this.getAbbrevMonthName(),
            B: this.getFullMonthName(),
            c: this.toString(),
            d: this.getTwoDigitDate(),
            H: this.getTwoDigitHour(),
            I: this.getTwoDigitTwelveHour(),
            m: this.getTwoDigitMonth(),
            M: this.getTwoDigitMinute(),
            p: this.getHours() >= 12 ? "PM" : "AM",
            S: this.getTwoDigitSecond(),
            w: "0" + this.getDay(),
            x: this.toLocaleDateString(),
            X: this.toLocaleTimeString(),
            y: ("" + this.getFullYear()).substr(2, 4),
            Y: "" + this.getFullYear(),
            "%": "%",
        };
        let result = "",
            i = 0;
        while (i < format.length) {
            if (format.charAt(i) === "%") {
                result += fields[format.charAt(i + 1)];
                ++i;
            } else {
                result += format.charAt(i);
            }
            ++i;
        }
        return result;
    };

    // ----------------------------------------------------------------------------
    // String object extensions
    // ----------------------------------------------------------------------------
    // 按格式解析日期字符串为 UTC Date
    String.prototype.strptime = function (format) {
        const split_format = format.split(/[.\-/]/);
        const date = this.split(/[.\-/]/);
        let i = 0;
        let day, month, year;
        while (i < split_format.length) {
            switch (split_format[i]) {
                case "%d":
                    day = date[i];
                    break;
                case "%m":
                    month = date[i] - 1;
                    break;
                case "%Y":
                    year = date[i];
                    break;
                case "%y":
                    // A %y value in the range of [00, 68] is in the current
                    // century, while [69, 99] is in the previous century,
                    // according to the Open Group Specification.
                    if (parseInt(date[i], 10) >= 69) {
                        year = date[i];
                    } else {
                        year =
                            new Date(Date.UTC(date[i], 0)).getUTCFullYear() +
                            100;
                    }
                    break;
            }
            ++i;
        }
        // Create Date object from UTC since the parsed value is supposed to be
        // in UTC, not local time. Also, the calendar uses UTC functions for
        // date extraction.
        return new Date(Date.UTC(year, month, day));
    };
}
