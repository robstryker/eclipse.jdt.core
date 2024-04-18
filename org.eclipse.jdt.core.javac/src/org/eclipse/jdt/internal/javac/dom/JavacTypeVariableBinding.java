package org.eclipse.jdt.internal.javac.dom;

import org.eclipse.jdt.core.dom.JavacBindingResolver;

import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;

/**
 * Note that this isn't API and isn't part of the IBinding tree type.
 * The sole purpose of this class is to help calculate getKey.
 */
class JavacTypeVariableBinding {
	private TypeVariableSymbol typeVar;
	private JavacBindingResolver resolver;

	JavacTypeVariableBinding(TypeVariableSymbol typeVar, JavacBindingResolver resolver) {
		this.typeVar = typeVar;
		this.resolver = resolver;
	}

	public String getKey() {
		StringBuilder builder = new StringBuilder();
		builder.append(typeVar.getSimpleName());
		builder.append(':');
		for (var bound : typeVar.getBounds()) {
			JavacTypeBinding boundTypeBinding = new JavacTypeBinding(bound.tsym, this.resolver, null);
			builder.append(boundTypeBinding.getKey());
		}
		return builder.toString();
	}
}
