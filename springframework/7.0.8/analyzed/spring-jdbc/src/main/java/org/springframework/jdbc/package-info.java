/**
 * 该包中的类使 JDBC 更易于使用并减少常见错误的可能性。特别是，它们： <ul> <li>S 简化了错误处理，避免了应用程序代码中对 try/catch/finally 块的
 * 需要。 <li>以未经检查的异常的通用层次结构向应用程序代码呈现异常，使应用程序能够捕获数据访问异常而不依赖于 JDBC，并忽略捕获没有价值的致命异常。 OCAJAVA3DO允
 * 许修改错误处理的实现以针对不同的 RDBMS，而无需在应用程序代码中引入专有依赖项。 </ul>
 * <p> 这个包和相关包在 Rod Johnson 的 <a
 * href="https://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert 一对一 J2EE 设计和开发
 * </a>（Wrox，2002）的第 9 章中讨论。
 */
@NullMarked
package org.springframework.jdbc;

import org.jspecify.annotations.NullMarked;
