package org.eclipse.jdt.internal.javac.dom;

import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;

/**
 * Note that this isn't API and isn't part of the IBinding tree type.
 * The sole purpose of this class is to help calculate getKey.
 */
class JavacTypeVariableBinding {
	private TypeVariableSymbol typeVar;

	JavacTypeVariableBinding(TypeVariableSymbol typeVar) {
		this.typeVar = typeVar;
	}

	public String getKey() {
		StringBuilder builder = new StringBuilder();
		builder.append(typeVar.getSimpleName());
		builder.append(':');
		boolean prependColon = typeVar.getBounds().size() > 1
				|| (typeVar.getBounds().size() > 0 && typeVar.getBounds().get(0).isInterface());
		for (var bound : typeVar.getBounds()) {
			if (prependColon) {
				builder.append(":");
			}
			JavacTypeBinding.getKey(builder, bound, false);
		}
		return builder.toString();
	}
}
