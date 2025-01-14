/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * A signature provider is an object, usually a binding of some type,
 * that is capable of providing a signature, such as a method signature
 * or a type signature.
 *
 * This class is Provisional API for use by JDT/UI or JDT/Debug, which may possibly be removed in a future version.
 *
 * @since 3.42
 * @noimplement This interface is not intended to be implemented by clients.
 *
 */
public interface ISignatureProvider {
	/**
	 * Returns the signature for this object.
	 * <p>
	 * Within a single cluster of bindings (produced by the same call to an
	 * {@code ASTParser#create*(*)} method)), each binding has a distinct signature.
	 * The signatures are generated in a manner that is predictable and as
	 * stable as possible. This last property makes these signatures useful for
	 * comparing bindings between different clusters of bindings (for example,
	 * the bindings between the "before" and "after" ASTs of the same
	 * compilation unit).
	 * </p>
	 * <p>
	 * The exact details of how the keys are generated is unspecified.
	 * However, it is a function of the following information:
	 * <ul>
	 * <li>packages - the name of the package (for an unnamed package,
	 *   some internal id)</li>
	 * <li>classes or interfaces - the VM name of the type and the key
	 *   of its package</li>
	 * <li>array types - the key of the component type and number of
	 *   dimensions</li>
	 * <li>primitive types - the name of the primitive type</li>
	 * <li>fields - the name of the field and the key of its declaring
	 *   type</li>
	 * <li>methods - the name of the method, the key of its declaring
	 *   type, and the keys of the parameter types</li>
	 * <li>constructors - the key of its declaring class, and the
	 *   keys of the parameter types</li>
	 * <li>local variables - the name of the local variable, the index of the
	 *   declaring block relative to its parent, the key of its method</li>
	 * <li>local types - the name of the type, the index of the declaring
	 *   block relative to its parent, the key of its method</li>
	 * <li>anonymous types - the occurrence count of the anonymous
	 *   type relative to its declaring type, the key of its declaring type</li>
	 * <li>enum types - treated like classes</li>
	 * <li>annotation types - treated like interfaces</li>
	 * <li>type variables - the name of the type variable and
	 * the key of the generic type or generic method that declares that
	 * type variable</li>
	 * <li>wildcard types - the key of the optional wildcard type bound</li>
     * <li>capture type bindings - the key of the wildcard captured</li>
	 * <li>generic type instances - the key of the generic type and the keys
	 * of the type arguments used to instantiate it, and whether the
	 * instance is explicit (a parameterized type reference) or
	 * implicit (a raw type reference)</li>
	 * <li>generic method instances - the key of the generic method and the keys
	 * of the type arguments used to instantiate it, and whether the
	 * instance is explicit (a parameterized method reference) or
	 * implicit (a raw method reference)</li>
	 * <li>members of generic type instances - the key of the generic type
	 * instance and the key of the corresponding member in the generic
	 * type</li>
	 * <li>annotations - the key of the annotated element and the key of
	 * the annotation type</li>
	 * </ul>
	 * <p>
	 * The key for a type binding does <em>not</em> contain {@link ITypeBinding#getTypeAnnotations() type annotations},
	 * so type bindings with different type annotations may have the same key (iff they denote the same un-annotated type).
	 * By construction, this also applies to method bindings if their declaring types contain type annotations.
	 * </p>
	 * <p>Note that the key for member value pair bindings is
	 * not yet implemented. This method returns <code>null</code> for that kind of bindings.<br>
	 * Recovered bindings have a unique key.
	 * </p>
	 *
	 * @return the key for this binding
	 */
	public String getSignature();
}
