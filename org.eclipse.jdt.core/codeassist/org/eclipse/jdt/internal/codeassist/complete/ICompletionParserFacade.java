/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;

/**
 * The purpose of this interface is to isolate how various internal classes
 * are using the parser, and to allow for drop-in replacements of different parser
 * implementations while conforming to the same interface.
 */
public interface ICompletionParserFacade extends IScannerProvider {

	HashtableOfObjectToInt getSourceEnds();

	CompilationUnitDeclaration parseCompilationUnitDeclaration(ICompilationUnit sourceUnit,
			CompilationResult result, int actualCompletionPosition2);

	ASTNode getEnclosingNode();
	ASTNode getAssistNodeParent();
	ASTNode getAssistNode();
	void setEnclosingNode(ASTNode providesStmt);
}
