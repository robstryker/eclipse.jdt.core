package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.dom.InfixExpression.Operator;

public class CustomASTMatcher extends ASTMatcher {
	private boolean nullSafeStringCompare(Object o1, Object o2) {
		if( o1 == null ) {
			if( o2 == null ) {
				return true;
			}
			return false;
		}
		return o1.toString().equals(o2.toString());
	}
	public boolean match(NumberLiteral node, Object other) {
		if( other instanceof PrefixExpression pe) {
			return nullSafeStringCompare(node, pe);
		}
		return super.match(node, other);
	}
	
	public boolean match(PrefixExpression node, Object other) {
		if( other instanceof NumberLiteral pe) {
			return nullSafeStringCompare(node, pe);
		}
		return super.match(node, other);
	}
	public boolean match(InfixExpression node, Object other) {
		if( other instanceof StringLiteral sa && node.getOperator() == Operator.PLUS && node.getLeftOperand() instanceof StringLiteral && node.getRightOperand() instanceof StringLiteral) {
			String v = sa.toString();
			String nodeString = node.toString();
			String replaced1 = nodeString.replaceAll("\" *\\+ *\"", "");
			if( replaced1.equals(v)) {
				return true;
			}
		}
		if( other instanceof InfixExpression sa ) {
			String v = sa.toString();
			String nodeString = node.toString();
			String replaced1 = nodeString.replaceAll("\" *\\+ *\"", "");
			if( replaced1.replaceAll(" ", "").equals(v.replaceAll(" ", ""))) {
				return true;
			}
		}
		return super.match(node, other);
	}
	public boolean match(StringLiteral node, Object other) {
		if( other instanceof InfixExpression ia && ia.getOperator() == Operator.PLUS && ia.getLeftOperand() instanceof StringLiteral && ia.getRightOperand() instanceof StringLiteral) {
			String v = ia.toString();
			String nodeString = node.toString();
			String replaced1 = v.replaceAll("\" \\+ \"", "");
			if( replaced1.equals(nodeString)) {
				return true;
			}
		}
		return super.match(node, other);
	}
	
	public boolean match(SimpleName node, Object other) {
		if( other instanceof QualifiedName fa) {
			return node.toString().equals(other.toString());
		} else {
			return super.match(node, other);
		}
	}
	public boolean match(QualifiedName node, Object other) {
		if( other instanceof FieldAccess fa) {
			return matchQualifiedNameToFieldAccess(node, fa);
		} else if( other instanceof SimpleName fa) {
			return node.toString().equals(other.toString());
		} else {
			return super.match(node, other);
		}
	}
	public boolean match(FieldAccess node, Object other) {
		if( other instanceof QualifiedName qn) {
			return matchQualifiedNameToFieldAccess(qn, node);
		} else {
			return super.match(node, other);
		}
	}
	
	public boolean match(SimpleType node, Object other) {
		if( other instanceof QualifiedType && node.toString().equals(other.toString())) {
			return true;
		}
		return super.match(node, other);
	}

	public boolean match(QualifiedType node, Object other) {
		if( other instanceof SimpleType && node.toString().equals(other.toString())) {
			return true;
		}
		return super.match(node, other);
	}

	public boolean matchQualifiedNameToFieldAccess(QualifiedName node, FieldAccess fa) {
		String qualifier1 = fa == null ? null : fa.getExpression() == null ? null : fa.getExpression().toString();
		String qualifier2 = node == null ? null : node.getQualifier() == null ? null : node.getQualifier().toString();
		boolean match1 = nullSafeStringCompare(qualifier1, qualifier2);

		String name1 = fa == null ? null : fa.getName() == null ? null : fa.getName().toString();
		String name2 = node == null ? null : node.getName() == null ? null : node.getName().toString();
		boolean match2 = nullSafeStringCompare(name1, name2);
		return match1 && match2;
	}
	
}