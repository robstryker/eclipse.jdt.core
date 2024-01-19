package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.internal.codeassist.UnresolvedReferenceNameFinder.UnresolvedReferenceNameRequestor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public interface IUnresolvedReferenceNameFinder {

	public void find(char[] startWith, Initializer initializer, ClassScope scope, int from, char[][] discouragedNames,
			UnresolvedReferenceNameRequestor nameRequestor);

	public void find(char[] startWith, AbstractMethodDeclaration methodDeclaration, int from, char[][] discouragedNames,
			UnresolvedReferenceNameRequestor nameRequestor);

	public void findAfter(char[] startWith, Scope scope, ClassScope classScope, int from, int to,
			char[][] discouragedNames, UnresolvedReferenceNameRequestor nameRequestor);

	public void findBefore(char[] startWith, Scope scope, ClassScope classScope, int from, int recordTo, int parseTo,
			char[][] discouragedNames, UnresolvedReferenceNameRequestor nameRequestor);
}
