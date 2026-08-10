// @ts-check
/*
 *   This content is licensed according to the W3C Software License at
 *   https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 *   File:   menu-button-links.js
 *
 *   Desc:   Creates a menu button that opens a menu of links
 *
 *   Modified by Peter Keuter to adhere to the coding standards of Keycloak
 *   Original file: https://www.w3.org/WAI/content-assets/wai-aria-practices/patterns/menu-button/examples/js/menu-button-links.js
 *   Source: https://www.w3.org/TR/wai-aria-practices/examples/menu-button/menu-button-links.html
 */

// 基于 WAI-ARIA 实践的链接菜单按钮组件，支持键盘导航与无障碍访问
class MenuButtonLinks {
  // 初始化 DOM 节点、菜单项列表及事件绑定
  constructor(domNode) {
    this.domNode = domNode;
    this.buttonNode = domNode.querySelector("button");
    this.menuNode = domNode.querySelector('[role="menu"]');
    this.menuitemNodes = [];
    this.firstMenuitem = false;
    this.lastMenuitem = false;
    this.firstChars = [];

    this.buttonNode.addEventListener("keydown", (e) => this.onButtonKeydown(e));
    this.buttonNode.addEventListener("click", (e) => this.onButtonClick(e));

    const nodes = domNode.querySelectorAll('[role="menuitem"]');

    for (const menuitem of nodes) {
      this.menuitemNodes.push(menuitem);
      menuitem.tabIndex = -1;
      // 记录各菜单项首字符，用于字母快速跳转
      this.firstChars.push(menuitem.textContent.trim()[0].toLowerCase());

      menuitem.addEventListener("keydown", (e) => this.onMenuitemKeydown(e));

      menuitem.addEventListener("mouseover", (e) =>
        this.onMenuitemMouseover(e)
      );

      if (!this.firstMenuitem) {
        this.firstMenuitem = menuitem;
      }
      this.lastMenuitem = menuitem;
    }

    domNode.addEventListener("focusin", () => this.onFocusin());
    domNode.addEventListener("focusout", () => this.onFocusout());

    // 点击菜单外部区域时关闭弹出层
    window.addEventListener(
      "mousedown",
      (e) => this.onBackgroundMousedown(e),
      true
    );
  }

  // 将焦点移至指定菜单项并更新 tabIndex
  setFocusToMenuitem = (newMenuitem) =>
    this.menuitemNodes.forEach((item) => {
      if (item === newMenuitem) {
        item.tabIndex = 0;
        newMenuitem.focus();
      } else {
        item.tabIndex = -1;
      }
    });

  setFocusToFirstMenuitem = () => this.setFocusToMenuitem(this.firstMenuitem);

  setFocusToLastMenuitem = () => this.setFocusToMenuitem(this.lastMenuitem);

  // 焦点移至上一项，首项时循环至末项
  setFocusToPreviousMenuitem = (currentMenuitem) => {
    let newMenuitem, index;

    if (currentMenuitem === this.firstMenuitem) {
      newMenuitem = this.lastMenuitem;
    } else {
      index = this.menuitemNodes.indexOf(currentMenuitem);
      newMenuitem = this.menuitemNodes[index - 1];
    }

    this.setFocusToMenuitem(newMenuitem);

    return newMenuitem;
  };

  // 焦点移至下一项，末项时循环至首项
  setFocusToNextMenuitem = (currentMenuitem) => {
    let newMenuitem, index;

    if (currentMenuitem === this.lastMenuitem) {
      newMenuitem = this.firstMenuitem;
    } else {
      index = this.menuitemNodes.indexOf(currentMenuitem);
      newMenuitem = this.menuitemNodes[index + 1];
    }
    this.setFocusToMenuitem(newMenuitem);

    return newMenuitem;
  };

  // 按首字符在菜单项间跳转焦点
  setFocusByFirstCharacter = (currentMenuitem, char) => {
    let start, index;

    if (char.length > 1) {
      return;
    }

    char = char.toLowerCase();

    // 从当前项之后开始搜索匹配首字符
    start = this.menuitemNodes.indexOf(currentMenuitem) + 1;
    if (start >= this.menuitemNodes.length) {
      start = 0;
    }

    index = this.firstChars.indexOf(char, start);

    // 未找到则从列表开头继续搜索
    if (index === -1) {
      index = this.firstChars.indexOf(char, 0);
    }

    if (index > -1) {
      this.setFocusToMenuitem(this.menuitemNodes[index]);
    }
  };

  // 工具方法：从指定索引起查找首字符匹配项

  getIndexFirstChars = (startIndex, char) => {
    for (let i = startIndex; i < this.firstChars.length; i++) {
      if (char === this.firstChars[i]) {
        return i;
      }
    }
    return -1;
  };

  // 弹出菜单显示/隐藏

  openPopup = () => {
    this.menuNode.style.display = "block";
    this.buttonNode.setAttribute("aria-expanded", "true");
  };

  closePopup = () => {
    if (this.isOpen()) {
      this.buttonNode.setAttribute("aria-expanded", "false");
      this.menuNode.style.removeProperty("display");
    }
  };

  isOpen = () => {
    return this.buttonNode.getAttribute("aria-expanded") === "true";
  };

  // 焦点进入/离开容器时的样式处理

  onFocusin = () => {
    this.domNode.classList.add("focus");
  };

  onFocusout = () => {
    this.domNode.classList.remove("focus");
  };

  // 菜单按钮键盘事件：打开/关闭菜单并移动焦点
  onButtonKeydown = (event) => {
    const key = event.key;
    let flag = false;

    switch (key) {
      case " ":
      case "Enter":
      case "ArrowDown":
      case "Down":
        this.openPopup();
        this.setFocusToFirstMenuitem();
        flag = true;
        break;

      case "Esc":
      case "Escape":
        this.closePopup();
        this.buttonNode.focus();
        flag = true;
        break;

      case "Up":
      case "ArrowUp":
        this.openPopup();
        this.setFocusToLastMenuitem();
        flag = true;
        break;

      default:
        break;
    }

    if (flag) {
      event.stopPropagation();
      event.preventDefault();
    }
  };

  // 按钮点击：切换弹出菜单开关状态
  onButtonClick(event) {
    if (this.isOpen()) {
      this.closePopup();
      this.buttonNode.focus();
    } else {
      this.openPopup();
      this.setFocusToFirstMenuitem();
    }

    event.stopPropagation();
    event.preventDefault();
  }

  // 菜单项键盘导航：跳转链接、上下移动、首末项、字母筛选等
  onMenuitemKeydown(event) {
    const tgt = event.currentTarget;
    const key = event.key;
    let flag = false;

    const isPrintableCharacter = (str) => str.length === 1 && str.match(/\S/);

    if (event.ctrlKey || event.altKey || event.metaKey) {
      return;
    }

    if (event.shiftKey) {
      if (isPrintableCharacter(key)) {
        this.setFocusByFirstCharacter(tgt, key);
        flag = true;
      }

      if (event.key === "Tab") {
        this.buttonNode.focus();
        this.closePopup();
        flag = true;
      }
    } else {
      switch (key) {
        case " ":
          window.location.href = tgt.href;
          break;

        case "Esc":
        case "Escape":
          this.closePopup();
          this.buttonNode.focus();
          flag = true;
          break;

        case "Up":
        case "ArrowUp":
          this.setFocusToPreviousMenuitem(tgt);
          flag = true;
          break;

        case "ArrowDown":
        case "Down":
          this.setFocusToNextMenuitem(tgt);
          flag = true;
          break;

        case "Home":
        case "PageUp":
          this.setFocusToFirstMenuitem();
          flag = true;
          break;

        case "End":
        case "PageDown":
          this.setFocusToLastMenuitem();
          flag = true;
          break;

        case "Tab":
          this.closePopup();
          break;

        default:
          if (isPrintableCharacter(key)) {
            this.setFocusByFirstCharacter(tgt, key);
            flag = true;
          }
          break;
      }
    }

    if (flag) {
      event.stopPropagation();
      event.preventDefault();
    }
  }

  // 鼠标悬停时将焦点移至对应菜单项
  onMenuitemMouseover(event) {
    const tgt = event.currentTarget;
    tgt.focus();
  }

  // 在菜单区域外按下鼠标时关闭弹出层
  onBackgroundMousedown(event) {
    if (!this.domNode.contains(event.target)) {
      if (this.isOpen()) {
        this.closePopup();
        this.buttonNode.focus();
      }
    }
  }
}

// 初始化页面上所有 .menu-button-links 容器
const menuButtons = document.querySelectorAll(".menu-button-links");
for (const button of menuButtons) {
  new MenuButtonLinks(button);
}
