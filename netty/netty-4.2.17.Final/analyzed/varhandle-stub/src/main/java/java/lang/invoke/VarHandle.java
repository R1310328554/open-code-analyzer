/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package java.lang.invoke;

/**
 * A stub for the VarHandle class.<br>
 * <p>{@link VarHandle} 编译桩：使 Netty 在 Java 8 目标发布编译时能通过类型检查； 运行时不可由应用 ClassLoader 加载 {@code java.lang.invoke} 特权包中的此类。</p>
 * This stub is used to allow Java 8 release compilation to work as expected.
 * The sole limit of this stub is that since {@code java.lang.invoke} is a privileged package
 * it cannot be used at runtime (e.g. loaded by a classloader).<p>
 * For example, if {@code SomeClass} is loaded at runtime:
 * <pre>
 *     class SomeClass {
 *          private static final VarHandle VH = // ... obtained somehow;
 *
 *          /** 桩实现：运行时调用将抛出 {@link UnsupportedOperationException} */
    public static void storeStoreFence() {
 *              if (VH == null) {
 *                  return;
 *              }
 *              VH.storeStoreFence();
 *          }
 *     }
 * </pre>
 * this is not going to work on Java 8.<p>
 * <p>在 Java 8 上直接静态引用 VarHandle 会在类初始化时加载桩类而失败； 可用 Holder 间接引用或 {@code Object} 强转规避。</p>
 * To fix it is possible to use an holder class (which won't be loaded at runtime):
 * <pre>
 *     class SomeClass {
 *          static class Holder {
 *              private static final VarHandle VH = // ... obtained somehow;
 *          }
 *
 *          public static void storeStoreFence() {
 *              VarHandle vh = Holder.VH;
 *              if (vh == null) {
 *                  return;
 *              }
 *              vh.storeStoreFence();
 *          }
 *     }
 * </pre>
 * Or:
 * <pre>
 *     class SomeClass {
 *          private static final Object VH = // ... obtained somehow;
 *
 *          public static void storeStoreFence() {
 *              if (VH == null) {
 *                  return;
 *              }
 *              ((VarHandle)VH).storeStoreFence();
 *          }
 *     }
 * </pre>
 *
 * <p>桩方法声明为 native 是为满足 JLS 签名多态要求，使 Java 8 编译器按 调用点形式参数/返回值类型生成字节码（尽管运行时不会执行此桩）。</p>
 * The reason why the methods on the stub are declared as native is to allow
 * {@link java.lang.invoke.MethodHandle.PolymorphicSignature} to work as expected,
 * see <a href="https://docs.oracle.com/javase/specs/jls/se9/html/jls-15.html#jls-15.12.3">JLS 15.12.3</a>:<br>
 * <pre>
 *   A method is signature polymorphic if all of the following are true:
 *   - It is declared in the java.lang.invoke.MethodHandle class or the java.lang.invoke.VarHandle class.
 *   - It has a single variable arity parameter (§8.4.1) whose declared type is Object[].
 *   - It is native.
 * </pre>
 * This seems counter-intuitive since this stub is not going to be used at runtime, but it is required to allow Java 8
 * compilation to produce {@code VarHandle}'s method invocations with parameters and result types with
 * the types of the formal ones of the compile-time declaration.
 * <p>编译桩不参与运行时；生产环境应使用 JDK 9+ 自带 {@link VarHandle}。</p>
 *
 */
public class VarHandle {

    /** 签名多态读操作桩（编译期用） */
    @MethodHandle.PolymorphicSignature
    public native Object get(Object... args);

    /** acquire 语义读 */
    @MethodHandle.PolymorphicSignature
    public native Object getAcquire(Object... args);

    /** 签名多态写操作桩 */
    @MethodHandle.PolymorphicSignature
    public native void set(Object... args);

    /** release 语义写 */
    @MethodHandle.PolymorphicSignature
    public native void setRelease(Object... args);

    /** 原子 get-and-add 桩 */
    @MethodHandle.PolymorphicSignature
    public native Object getAndAdd(Object... args);

    /** compare-and-set 桩 */
    @MethodHandle.PolymorphicSignature
    public native boolean compareAndSet(Object... args);

    public static void storeStoreFence() {
        throw new UnsupportedOperationException("Not implemented in varhandle-stub");
    }
}
