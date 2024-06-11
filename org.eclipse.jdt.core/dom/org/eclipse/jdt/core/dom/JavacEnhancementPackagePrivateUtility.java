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

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * This class is an addition made in the incubator repository with the intention of
 * exposing some package-private details to an interested extender, without having to make
 * changes to more files.
 *
 * This should not be committed to JDT master.
 *
 * @since 3.38
 */
public class JavacEnhancementPackagePrivateUtility {
	public static BindingResolver getBindingResolver(CompilationUnit cu) {
		return cu.getAST().getBindingResolver();
	}
	public static void setBindingResolver(AST ast, BindingResolver resolver) {
		ast.setBindingResolver(resolver);
	}
	public static class InternalNameEnvironmentWithProgress extends NameEnvironmentWithProgress {
		public InternalNameEnvironmentWithProgress(Classpath[] paths, String[] initialFileNames,
				IProgressMonitor monitor) {
			super(paths, initialFileNames, monitor);
		}
	}

	public static int getDefaultNodeFlag(AST ast) {
		return ast.getDefaultNodeFlag();
	}

	public static void setDefaultNodeFlag(AST ast, int val) {
		ast.setDefaultNodeFlag(val);
	}

	public static void setOriginalModificationCount(AST ast, long count) {
		ast.setOriginalModificationCount(count);
	}

	public static void setAstFlag(AST ast, int flag) {
		ast.setFlag(flag);
	}

	public static Scanner getAstScanner(AST ast) {
		return ast.scanner;
	}

	public static ASTConverter createASTConverter(Map<String, String> options, boolean resolveBindings, IProgressMonitor monitor) {
		return new ASTConverter(options, resolveBindings, monitor);
	}
	public static org.eclipse.jdt.internal.compiler.parser.Scanner getScannerForNewASTConverter(Map<String, String> options, boolean resolveBindings, IProgressMonitor monitor) {
		return createASTConverter(options, resolveBindings, monitor).scanner;
	}

	public static void setCompilationUnitCommentTable(CompilationUnit cu, Comment[] commentTable) {
		cu.setCommentTable(commentTable);
	}

	public static void initCommentMapper(CompilationUnit cu, Scanner scanner) {
		cu.initCommentMapper(scanner);
	}

	public static void setCompilationUnitProblems(CompilationUnit cu, IProblem[] problems) {
	    cu.setProblems(problems);
	}

	public static ITypeBinding createTypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
		return new TypeBinding(resolver, binding);
	}

	public static int getApiLevel(AST ast) {
		return ast.apiLevel;
	}


	public static void internalSetModifiers(VariableDeclarationStatement res, int jls2Flags) {
		res.internalSetModifiers(jls2Flags);
	}

	public static void internalSetModifiers(BodyDeclaration res, int jls2Flags) {
		res.internalSetModifiers(jls2Flags);
	}

	public static void internalSetModifiers(SingleVariableDeclaration res, int jls2ModifiersFlags) {
		res.internalSetModifiers(jls2ModifiersFlags);
	}
    public static void setLineEndTable(CompilationUnit res, int[] lineEndPosTable) {
    	res.setLineEndTable(lineEndPosTable);
	}

	public static void internalSetIdentifier(SimpleName name, String string) {
		name.internalSetIdentifier(string);
	}

	public static void internalSetReturnType(MethodDeclaration res, Type retType) {
		res.internalSetReturnType(retType);
	}
	public static void internalSetEscapedValue(TextBlock res, String rawValue, String string) {
		res.internalSetEscapedValue(rawValue, string);
	}
	public static void internalSetModifiers(VariableDeclarationExpression res, int jls2Flags) {
		res.internalSetModifiers(jls2Flags);
	}

	public static int getExtraArrayDimensions(VariableDeclarationFragment fragment) {
		return fragment.extraArrayDimensions;
	}
	public static void setParentName(Name parentName, ASTNode parent, StructuralPropertyDescriptor property) {
		parentName.setParent(parent, property);
	}



	public static SimpleName cloneSimpleName(SimpleName name, AST ast2) {
		return (SimpleName)name.clone(ast2);
	}

	public static SimpleName createSimpleName(AST ast2) {
		return new SimpleName(ast2);
	}

	public static TypeParameter createTypeParameter(AST ast2) {
		return new TypeParameter(ast2);
	}

	public static AnnotationTypeMemberDeclaration createAnnotationTypeMemberDeclaration(AST ast2) {
		return new AnnotationTypeMemberDeclaration(ast2);
	}

	public static NameQualifiedType createNameQualifiedType(AST ast2) {
		return new NameQualifiedType(ast2);
	}

	public static QualifiedType createQualifiedType(AST ast2) {
		return new QualifiedType(ast2);
	}
	public static MemberValuePair createMemberValuePair(AST ast2) {
		return new MemberValuePair(ast2);
	}

	public static EnumConstantDeclaration createEnumConstantDeclaration(AST ast2) {
		return new EnumConstantDeclaration(ast2);
	}


}
