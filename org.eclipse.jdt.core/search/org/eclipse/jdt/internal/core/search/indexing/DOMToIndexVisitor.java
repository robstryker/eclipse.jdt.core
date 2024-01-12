/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

class DOMToIndexVisitor extends ASTVisitor {

	private SourceIndexer sourceIndexer;

	public DOMToIndexVisitor(SourceIndexer sourceIndexer) {
		this.sourceIndexer = sourceIndexer;
	}

	@Override
	public boolean visit(TypeDeclaration unit) {
		ITypeBinding binding = unit.resolveBinding();
		if (unit.isInterface()) {
			this.sourceIndexer.addInterfaceDeclaration(unit.getModifiers(), unit.resolveBinding().getPackage().getName().toCharArray(), unit.getName().toString().toCharArray(), null, Arrays.stream(binding.getInterfaces()).map(type -> type.getName().toCharArray()).toArray(char[][]::new), null, false);
		} else {
			this.sourceIndexer.addClassDeclaration(unit.getModifiers(), unit.resolveBinding().getPackage().getName().toCharArray(), unit.getName().toString().toCharArray(), null, binding.getSuperclass() == null ? null : binding.getSuperclass().getName().toCharArray(),
				Arrays.stream(binding.getInterfaces()).map(type -> type.getName().toCharArray()).toArray(char[][]::new), null, false);
		}
		// TODO other types
		return true;
	}

	@Override
	public boolean visit(RecordDeclaration recordDecl) {
		// copied processing of TypeDeclaration
		ITypeBinding binding = recordDecl.resolveBinding();
		this.sourceIndexer.addClassDeclaration(recordDecl.getModifiers(), recordDecl.resolveBinding().getPackage().getName().toCharArray(), recordDecl.getName().toString().toCharArray(), null, binding.getSuperclass() == null ? null : binding.getSuperclass().getName().toCharArray(),
				Arrays.stream(binding.getInterfaces()).map(type -> type.getName().toCharArray()).toArray(char[][]::new), null, false);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration method) {
		char[] methodName = method.getName().toString().toCharArray();
		char[][] parameterTypes = ((List<VariableDeclaration>)method.parameters()).stream()
			.map(VariableDeclaration::resolveBinding)
			.map(IVariableBinding::getType)
			.map(ITypeBinding::getName)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		char[] returnType = method.getReturnType2().resolveBinding().getName().toCharArray();
		char[][] exceptionTypes = ((List<Type>)method.thrownExceptionTypes()).stream()
			.map(Type::resolveBinding)
			.map(ITypeBinding::getName)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		this.sourceIndexer.addMethodDeclaration(methodName, parameterTypes, returnType, exceptionTypes);
		IMethodBinding binding = method.resolveBinding();
		char[] typeName = binding.getDeclaringClass().getName().toCharArray();
		char[][] parameterNames = ((List<VariableDeclaration>)method.parameters()).stream()
			.map(VariableDeclaration::getName)
			.map(SimpleName::toString)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		this.sourceIndexer.addMethodDeclaration(typeName,
			null /* TODO: fully qualified name of enclosing type? */,
			methodName,
			parameterTypes.length,
			null,
			parameterTypes,
			parameterNames,
			returnType,
			method.getModifiers(),
			binding.getDeclaringClass().getPackage().getName().toCharArray(),
			0 /* TODO What to put here? */,
			exceptionTypes,
			0 /* TODO ExtraFlags.IsLocalType ? */);
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration field) {
		char[] typeName = field.getType().resolveBinding().getName().toCharArray();
		for (VariableDeclarationFragment fragment: (List<VariableDeclarationFragment>)field.fragments()) {
			this.sourceIndexer.addFieldDeclaration(typeName, fragment.getName().toString().toCharArray());
		}
		return true;
	}
	
	// TODO (cf SourceIndexer and SourceIndexerRequestor)
	// * Module: addModuleDeclaration/addModuleReference/addModuleExportedPackages
	// * Lambda: addIndexEntry/addClassDeclaration
	// * addMethodReference
	// * addConstructorReference
}
