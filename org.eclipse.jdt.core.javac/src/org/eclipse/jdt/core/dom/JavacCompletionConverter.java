/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.util.Context;

public class JavacCompletionConverter extends JavacConverter {

	public JavacCompletionConverter(AST ast, JCCompilationUnit javacCompilationUnit, Context context, String rawText,
			boolean buildJavadoc, int focalPoint) {
		super(ast, javacCompilationUnit, context, rawText, buildJavadoc, focalPoint);
	}
	protected Expression convertErroneousExpression(JCErroneous error) {
		Expression e = super.convertErroneousExpression(error);
		if( e == null ) {
			int pos = error.getPreferredPosition();
			char c = this.rawText.length() > pos ? this.rawText.charAt(pos) : 0;
			if (error.getErrorTrees().isEmpty() && c == '"') {
				  int newLine = this.rawText.indexOf('\n', pos);
				  int lineEnd = newLine == -1 ? this.rawText.length() - 1 : newLine;
				  String litText = this.rawText.substring(pos+1, lineEnd);
				  Expression res = convertStringToLiteral(litText, pos, lineEnd, null);
				  res.setSourceRange(pos,  lineEnd - pos);
				  return res;
				}
		}
		return e;
	}
}
