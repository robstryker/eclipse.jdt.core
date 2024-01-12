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
package org.eclipse.jdt.internal.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;

/**
 * Process an AST to populate a tree of IJavaElement->JavaElementInfo
 */
class DOMToModelPopulator extends ASTVisitor {

	private final Map<IJavaElement, IElementInfo> toPopulate;
	private final Stack<JavaElement> elements = new Stack<>();
	private final Stack<JavaElementInfo> infos = new Stack<>();

	public DOMToModelPopulator(Map<IJavaElement, IElementInfo> newElements, CompilationUnit root, CompilationUnitElementInfo unitInfo) {
		this.toPopulate = newElements;
		this.elements.push(root);
		this.infos.push(unitInfo);
	}

	private static void addAsChild(JavaElementInfo parentInfo, IJavaElement childElement) {
		if (parentInfo instanceof OpenableElementInfo openable) {
			openable.addChild(childElement);
		} else if (parentInfo instanceof SourceTypeElementInfo type) {
			type.children = Arrays.copyOf(type.children, type.children.length + 1);
			type.children[type.children.length - 1] = childElement;
		}
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		newInfo.setSourceRangeStart(node.getStartPosition());
		newInfo.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
		newInfo.setFlags(node.getFlags());
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		// TODO other info
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}

	@Override
	public void endVisit(TypeDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(MethodDeclaration method) {
		SourceMethod newElement = new SourceMethod(this.elements.peek(), method.getName().toString(), new String[0]); // TODO proper argument types
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceMethodInfo info = new SourceMethodInfo();
		info.setArgumentNames(((List<SingleVariableDeclaration>)method.parameters()).stream().map(param -> param.getName().toString().toCharArray()).toArray(char[][]::new));
		info.setReturnType(method.getReturnType2().toString().toCharArray());
		info.setSourceRangeStart(method.getStartPosition());
		info.setSourceRangeEnd(method.getStartPosition() + method.getLength() - 1);
		info.setFlags(method.getFlags());
		info.setNameSourceStart(method.getName().getStartPosition());
		info.setNameSourceEnd(method.getName().getStartPosition() + method.getName().getLength() - 1);
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(MethodDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(FieldDeclaration field) {
		for (VariableDeclarationFragment fragment : (Collection<VariableDeclarationFragment>) field.fragments()) {
			SourceField newElement = new SourceField(this.elements.peek(), fragment.getName().toString());
			this.elements.push(newElement);
			addAsChild(this.infos.peek(), newElement);
			SourceFieldElementInfo info = new SourceFieldElementInfo();
			info.setTypeName(field.getType().toString().toCharArray());
			info.setSourceRangeStart(field.getStartPosition());
			info.setSourceRangeEnd(field.getStartPosition() + field.getLength() - 1);
			info.setFlags(field.getFlags());
			info.setNameSourceStart(fragment.getName().getStartPosition());
			info.setNameSourceEnd(fragment.getName().getStartPosition() + fragment.getName().getLength() - 1);
			// TODO populate info
			this.infos.push(info);
			this.toPopulate.put(newElement, info);
		}
		return true;
	}
	@Override
	public void endVisit(FieldDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

}
