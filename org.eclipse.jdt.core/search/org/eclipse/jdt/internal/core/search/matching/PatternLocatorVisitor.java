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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.dom.*;

/**
 * Visits an AST to feel the possible match with nodes
 */
class PatternLocatorVisitor extends ASTVisitor {

	private final PatternLocator patternLocator;
	private final MatchingNodeSet nodeSet;

	public PatternLocatorVisitor(PatternLocator patternLocator, MatchingNodeSet nodeSet) {
		super(true);
		this.patternLocator = patternLocator;
		this.nodeSet = nodeSet;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(MethodInvocation node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveMethodBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(ExpressionMethodReference node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveMethodBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(SuperMethodReference node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveMethodBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(SuperMethodInvocation node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveMethodBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}

	private boolean visitAbstractTypeDeclaration(AbstractTypeDeclaration node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(EnumDeclaration node) {
		return visitAbstractTypeDeclaration(node);
	}
	@Override
	public boolean visit(TypeDeclaration node) {
		return visitAbstractTypeDeclaration(node);
	}
	@Override
	public boolean visit(RecordDeclaration node) {
		return visitAbstractTypeDeclaration(node);
	}
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}

	private boolean visitType(Type node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(SimpleType type) {
		visitType(type);
		Name n = type.getName();
		if( n instanceof QualifiedName qn ) {
			Name qualifier = qn.getQualifier();
			if( qualifier instanceof SimpleName sn1 ) {
				visit(sn1);
			} else if( qualifier instanceof QualifiedName qn1) {
				visit(qn1);
			}
		}
		return false; // No need to visit single name child
	}
	@Override
	public boolean visit(QualifiedType type) {
		return visitType(type);
	}
	@Override
	public boolean visit(NameQualifiedType type) {
		return visitType(type);
	}
	@Override
	public boolean visit(ParameterizedType node) {
		return visitType(node);
	}
	@Override
	public boolean visit(IntersectionType node) {
		return visitType(node);
	}
	@Override
	public boolean visit(UnionType node) {
		return visitType(node);
	}
	@Override
	public boolean visit(ClassInstanceCreation node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveConstructorBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(CreationReference node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveMethodBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveConstructorBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(SimpleName node) {
		if (
			node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
			node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
			node.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
			node.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
			node.getLocationInParent() == MethodDeclaration.NAME_PROPERTY) {
			return false; // skip as parent was most likely already matched
		}
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			IBinding b = node.resolveBinding();
			level = this.patternLocator.resolveLevel(b);
		}
		this.nodeSet.addMatch(node, level);
		return level == 0;
	}
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			int l1 = this.patternLocator.resolveLevel(node.resolveVariable());
			int l2 = this.patternLocator.resolveLevel(node.resolveConstructorBinding());
			level = Math.max(l1, l2);
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(QualifiedName node) {
		if (node.getLocationInParent() == SimpleType.NAME_PROPERTY) {
			return false; // type was already checked
		}
		int level = this.patternLocator.match(node, this.nodeSet);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.mustResolve || this.patternLocator.mustResolve)) {
			level = this.patternLocator.resolveLevel(node.resolveBinding());
		}
		this.nodeSet.addMatch(node, level);
		if( (level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.IMPOSSIBLE_MATCH ) {
			return true;
		}
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return true;
	}
}
