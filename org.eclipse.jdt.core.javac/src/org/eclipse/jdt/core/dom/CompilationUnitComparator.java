package org.eclipse.jdt.core.dom;

import java.util.Comparator;
import java.util.List;

public class CompilationUnitComparator implements Comparator<CompilationUnit> {
	private int version;

	public CompilationUnitComparator(int version) {
		this.version = version;
	}
	@Override
	public int compare(CompilationUnit res, CompilationUnit res2) {
		compareStandard(res, res2);
		if( res.imports().size() != res2.imports().size()) {
			handleError("imports are a different size");
		}
		for( int i = 0; i < res.imports().size(); i++ ) {
			ImportDeclaration id1 = (ImportDeclaration)res.imports().get(i);
			ImportDeclaration id2 = (ImportDeclaration)res2.imports().get(i);
			compareImportDeclarations(id1, id2);
		}
		compare(res.getPackage(), res2.getPackage());
		
		
		if( res.getProblems().length != res2.getProblems().length) {
			//handleError("problem lengths differ");
		}
		
		if( res.types().size() != res2.types().size()) {
			handleError("list of types are a different size");
		}
		for( int i = 0; i < res.types().size(); i++ ) {
			compareTypes((AbstractTypeDeclaration)res.types().get(i), (AbstractTypeDeclaration)res2.types().get(i));
		}
		
		// Everything looks ok, but, there might be errors lurking. 
		nullSafeStringCompare(res, res2, "Comparator incomplete; CUs are different.");
		return 0;
	}
	
	private void compareImportDeclarations(ImportDeclaration id1, ImportDeclaration id2) {
		compareStandard(id1, id2);
		if( !id1.getName().toString().equals(id2.getName().toString())) {
			handleError("ImportDeclaration names do not match");
		}
		if( version != AST.JLS2_INTERNAL) {
			if( id1.isStatic() != id2.isStatic()) {
				handleError("ImportDeclaration static do not match");
			}
		}
		nullSafeStringCompare(id1, id2, "Comparator incomplete; imports are different.");
	}
	private void compareStandard(ASTNode res, ASTNode res2) {
		if( res == null && res2 == null ) 
			return; 
		if( res == null || res2 == null ) 
			handleError("difference: null");
			
		if( res.getStartPosition() != res2.getStartPosition()) {
			handleError("startPosition differ ");
		}
		if( res.getLength() != res2.getLength()) {
			if( Math.abs(res.getLength() - res2.getLength()) == 1) {
				handleWarning("Length differ:  off by 1 - warning");
			} else {
				handleError("length differ ");
			}
		}
		if( res.typeAndFlags != res2.typeAndFlags) {
			int flagDiff = Math.abs(res2.typeAndFlags - res.typeAndFlags); 
			flagDiff &= ~ASTNode.ORIGINAL;
			flagDiff &= ~ASTNode.RECOVERED;
			flagDiff &= ~ASTNode.PROTECT;
			flagDiff &= ~ASTNode.MALFORMED;
			if(  flagDiff != 0 ) {
				handleError("flags are wrong for node ");
			}
		}
	}
	
	private void compareStandardBodyDecl(BodyDeclaration o1, BodyDeclaration o2) {
		compareJavadoc(o1.getJavadoc(), o2.getJavadoc());
		if( o1.getModifiers() != o2.getModifiers()) {
			handleError("type modifier flags does not match");
		}
		if( version != AST.JLS2_INTERNAL) {
			compareModifiers(o1.modifiers(), o2.modifiers());
		}

	}
	
	private void compareModifiers(List l1, List l2) {
		if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
			handleError("Modifiers does not match null");				
		}
		if( l1.size() != l2.size() ) {
			handleError("Modifiers does not match size");
		}
		for( int i = 0; i < l1.size(); i++ ) {
			compareModifier((IExtendedModifier)l1.get(i), (IExtendedModifier)l2.get(i));
		}
	}
	private void compareTypes(AbstractTypeDeclaration object, AbstractTypeDeclaration object2) {
		if( object instanceof TypeDeclaration && object2 instanceof TypeDeclaration) {
			compareTypeDeclaration((TypeDeclaration)object, (TypeDeclaration)object2);
		} else if( object instanceof RecordDeclaration && object2 instanceof RecordDeclaration) {
			compareRecord((RecordDeclaration)object, (RecordDeclaration)object2);
		} else if( object instanceof AnnotationTypeDeclaration && object2 instanceof AnnotationTypeDeclaration) {
			compareAnnotationType((AnnotationTypeDeclaration)object, (AnnotationTypeDeclaration)object2);
		} else if( object instanceof EnumDeclaration && object2 instanceof EnumDeclaration) {
			compareEnumType((EnumDeclaration)object, (EnumDeclaration)object2);
		} else {
			handleError("Types not comparable");
		}
	}

	private void compareEnumType(EnumDeclaration o1, EnumDeclaration o2) {
		compareStandard(o1, o2);
		nullSafeStringCompare(o1, o2, "Comparator incomplete; enum decls are different.");
	}

	private void compareAnnotationType(AnnotationTypeDeclaration o1, AnnotationTypeDeclaration o2) {
		compareStandard(o1, o2);
		nullSafeStringCompare(o1, o2, "Comparator incomplete; annot types are different.");
	}

	private void compareRecord(RecordDeclaration o1, RecordDeclaration o2) {
		compareStandard(o1, o2);
		nullSafeStringCompare(o1, o2, "Comparator incomplete; records are different.");
	}

	private void compareTypeDeclaration(TypeDeclaration o1, TypeDeclaration o2) {
		compareStandard(o1, o2);
		compareStandardBodyDecl(o1, o2);
		if( !o1.getName().toString().equals(o2.getName().toString())) {
			handleError("type names do not match");
		}
		if( o1.isInterface() != o2.isInterface()) {
			handleError("type interface flag does not match");
		}
		if( version == AST.JLS2_INTERNAL) {
			nullSafeStringCompare(o1.internalGetSuperclass(), o2.internalGetSuperclass(), "internalGetSuperclass does not match");
		} else {
			compareTypeObj(o1.getSuperclassType(), o2.getSuperclassType());
		}
		if( version >= AST.JLS17_INTERNAL) {
			comparePermittedTypes(o1.permittedTypes(), o2.permittedTypes());
		}
		if( version == AST.JLS2_INTERNAL) {
			if( o1.internalSuperInterfaces().size() != o2.internalSuperInterfaces().size()) {
				handleError("superInterfaceNames does not match size");				
			}
			for( int i = 0; i < o1.internalSuperInterfaces().size(); i++ ) {
				compareSuperInterfaceNames(o1.internalSuperInterfaces().get(i), o2.internalSuperInterfaces().get(i));
			}
		} else {
			if( o1.superInterfaceTypes().size() != o2.superInterfaceTypes().size()) {
				handleError("superInterfaceTypes does not match size");				
			}
			for( int i = 0; i < o1.superInterfaceTypes().size(); i++ ) {
				compareSuperInterfaceTypes(o1.superInterfaceTypes().get(i), o2.superInterfaceTypes().get(i));
			}
		}
		if( version != AST.JLS2_INTERNAL) {
			List l1 = o1.typeParameters();
			List l2 = o2.typeParameters();
			
			if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
				handleError("Type params does not match null");				
			}
			if( l1.size() != l2.size() ) {
				handleError("Type params does not match size");
			}
			for( int i = 0; i < l1.size(); i++ ) {
				compareTypeParameter((TypeParameter)l1.get(i), (TypeParameter)l2.get(i));
			}
		}

		if( o1.bodyDeclarations().size() != o2.bodyDeclarations().size()) {
			handleError("Body declarations does not match size");
		}
		for( int i = 0; i < o1.bodyDeclarations().size(); i++ ) {
			compareBodyDeclaration(o1.bodyDeclarations().get(i), o2.bodyDeclarations().get(i));
		}
		
		nullSafeStringCompare(o1, o2, "Comparator incomplete; types are different.");
	}

	private void compareModifier(IExtendedModifier o1, IExtendedModifier o2) {
		compareStandard((ASTNode)o1, (ASTNode)o2);
		if( o1 instanceof NormalAnnotation na1 && o2 instanceof NormalAnnotation na2) {
			compareNormalAnnotation(na1, na2);
		}
		nullSafeStringCompare(o1, o2, "Comparator incomplete; modifiers are different.");
	}
	private void compareNormalAnnotation(NormalAnnotation na1, NormalAnnotation na2) {
		// TODO Auto-generated method stub
		int z = 5;
	}
	private void compareBodyDeclaration(Object object, Object object2) {
		if( object instanceof MethodDeclaration && object2 instanceof MethodDeclaration) {
			compareMethodDeclaration((MethodDeclaration)object, (MethodDeclaration)object2);
		} else if( object instanceof FieldDeclaration && object2 instanceof FieldDeclaration) {
			compareFieldDeclaration((FieldDeclaration)object, (FieldDeclaration)object2);
		} else if( object instanceof TypeDeclaration && object2 instanceof TypeDeclaration) {
			compareTypeDeclaration((TypeDeclaration)object, (TypeDeclaration)object2);
		} else if( object instanceof Initializer && object2 instanceof Initializer) {
			compareInitializer((Initializer)object, (Initializer)object2);
		} else if( object instanceof EnumDeclaration && object2 instanceof EnumDeclaration) {
			compareEnumDeclaration((EnumDeclaration)object, (EnumDeclaration)object2);
		} else if( object instanceof AnnotationTypeDeclaration && object2 instanceof AnnotationTypeDeclaration) {
			compareAnnotTypeDeclaration((AnnotationTypeDeclaration)object, (AnnotationTypeDeclaration)object2);
		} else if( object instanceof AnnotationTypeMemberDeclaration && object2 instanceof AnnotationTypeMemberDeclaration) {
			compareAnnotTypeMemberDeclaration((AnnotationTypeMemberDeclaration)object, (AnnotationTypeMemberDeclaration)object2);
		} else {
			handleError("Body declaration not comparable");
		}
	}

	private void compareAnnotTypeMemberDeclaration(AnnotationTypeMemberDeclaration o1,
			AnnotationTypeMemberDeclaration o2) {
		compareStandard(o1, o2);
		compareStandardBodyDecl(o1, o2);
		if( !o1.getName().toString().equals(o2.getName().toString())) {
			handleError("type names do not match");
		}
		compareTypeObj(o1.getType(), o2.getType());
		nullSafeStringCompare(o1, o2, "Comparator incomplete; annot types are different.");
	}
	private void compareAnnotTypeDeclaration(AnnotationTypeDeclaration o1, AnnotationTypeDeclaration o2) {
		compareStandard(o1, o2);
		compareStandardBodyDecl(o1, o2);
		if( !o1.getName().toString().equals(o2.getName().toString())) {
			handleError("type names do not match");
		}
		if( o1.bodyDeclarations().size() != o2.bodyDeclarations().size()) {
			handleError("Body declarations does not match size");
		}
		for( int i = 0; i < o1.bodyDeclarations().size(); i++ ) {
			compareBodyDeclaration(o1.bodyDeclarations().get(i), o2.bodyDeclarations().get(i));
		}
		nullSafeStringCompare(o1, o2, "Comparator incomplete; annot type decls are different.");
	}
	private void compareEnumDeclaration(EnumDeclaration o1, EnumDeclaration o2) {
		compareStandard(o1, o2);
		compareStandardBodyDecl(o1, o2);
		if( !o1.getName().toString().equals(o2.getName().toString())) {
			handleError("type names do not match");
		}
		if( version == AST.JLS2_INTERNAL) {
			// DO nothing
		} else {
			if( o1.superInterfaceTypes().size() != o2.superInterfaceTypes().size()) {
				handleError("superInterfaceTypes does not match size");				
			}
			for( int i = 0; i < o1.superInterfaceTypes().size(); i++ ) {
				compareSuperInterfaceTypes(o1.superInterfaceTypes().get(i), o2.superInterfaceTypes().get(i));
			}
		}
		if( o1.bodyDeclarations().size() != o2.bodyDeclarations().size()) {
			handleError("Body declarations does not match size");
		}
		for( int i = 0; i < o1.bodyDeclarations().size(); i++ ) {
			compareBodyDeclaration(o1.bodyDeclarations().get(i), o2.bodyDeclarations().get(i));
		}
		
		nullSafeStringCompare(o1, o2, "Comparator incomplete; enum decl are different.");
	}
	private void compareInitializer(Initializer o1, Initializer o2) {
		compareStandard(o1, o2);
		compareStandardBodyDecl(o1, o2);
		compareBlock(o1.getBody(), o2.getBody());
		nullSafeStringCompare(o1, o2, "Comparator incomplete; initializers are different.");
	}
	
	private void compareBlock(Block o1, Block o2) {
		boolean e1Null = o1 == null;
		boolean e2Null = o2 == null;
		
		if( (e1Null && e2Null))
			return;
		if( e1Null || e2Null ) {
			handleError("Block not comparable: null");
		}

		compareStandard(o1, o2);
		
		List s1 = o1.statements();
		List s2 = o2.statements();
		if( s1.size() != s2.size()) {
			handleError("Block statements wrong sizes");
		}
		for( int i = 0; i < s1.size(); i++ ) {
			compareStatement((Statement)s1.get(i), (Statement)s2.get(i));
		}
		
		nullSafeStringCompare(o1, o2, "Comparator incomplete; blocks are different.");
	}
	
	private void compareStatement(Statement o1, Statement o2) {
		compareStandard(o1, o2);
		if( o1 instanceof VariableDeclarationStatement vds1 && o2 instanceof VariableDeclarationStatement vds2) {
			compareVarDeclStmt(vds1, vds2);
		}
		if( o1 instanceof ExpressionStatement vds1 && o2 instanceof ExpressionStatement vds2) {
			compareExpression(vds1.getExpression(), vds2.getExpression());
		}
		nullSafeStringCompare(o1, o2, "Comparator incomplete; Statement are different.");
	}
	
	private void compareVarDeclStmt(VariableDeclarationStatement vds1, VariableDeclarationStatement vds2) {
		compareStandard(vds1, vds2);
		if( version != AST.JLS2_INTERNAL) {
			compareModifiers(vds1.modifiers(), vds2.modifiers());
		}
		List frag1 = vds1.fragments();
		List frag2 = vds2.fragments();
		if( frag1.size() != frag2.size()) {
			handleError("VariableDeclarationStatement fragments wrong sizes");
		}
		for( int i = 0; i < frag1.size(); i++ ) {
			compareVariableDeclaration((VariableDeclarationFragment)frag1.get(i), (VariableDeclarationFragment)frag2.get(i));
		}
		nullSafeStringCompare(vds1, vds2, "Comparator incomplete; VariableDeclarationStatement are different.");
	}
	private void compareExpression(Expression e1, Expression e2) {
		boolean e1Null = e1 == null;
		boolean e2Null = e2 == null;
		
		if( (e1Null && e2Null))
			return;
		if( e1Null || e2Null ) {
			handleError("Expression not comparable: null");
		}
		if( e1 instanceof LambdaExpression le1 && e2 instanceof LambdaExpression le2) {
			compareLambdaExpression(le1, le2);
		} else if( e1 instanceof NumberLiteral le1 && e2 instanceof NumberLiteral le2) {
			compareStandard(le1, le2);
			nullSafeStringCompare(le1.getToken(), le2.getToken());
		} else if( e1 instanceof NullLiteral le1 && e2 instanceof NullLiteral le2) {
			compareStandard(le1, le2); // TODO
		} else if( e1 instanceof MethodInvocation le1 && e2 instanceof MethodInvocation le2) {
			compareStandard(le1, le2); // TODO
		} else if( e1 instanceof ClassInstanceCreation le1 && e2 instanceof ClassInstanceCreation le2) {
			compareStandard(le1, le2); 
			compareExpression(le1.getExpression(), le2.getExpression());
			compareAnonymousClassDecl(le1.getAnonymousClassDeclaration(), le2.getAnonymousClassDeclaration());
			compareExpressionList(le1.arguments(), le2.arguments());
			if( version != AST.JLS2_INTERNAL) {
				compareTypeList(le1.typeArguments(), le2.typeArguments());
			}
		} else if( (e1 instanceof FieldAccess && e2 instanceof QualifiedName) || (e1 instanceof QualifiedName && e2 instanceof FieldAccess)) {
			if( e1.getStartPosition() != e2.getStartPosition()) {
				handleError("startPosition differ ");
			}
			if( e1.getLength() != e2.getLength()) {
				handleError("length differ ");
			}
			FieldAccess fa = (e1 instanceof FieldAccess e1a ? e1a : (FieldAccess)e2);
			QualifiedName qn = (e1 instanceof QualifiedName e1a ? e1a : (QualifiedName)e2);
			nullSafeStringCompare(fa.getName(), qn.getName());
			nullSafeStringCompare(fa.getExpression(), qn.getQualifier());
		} else if( e1 instanceof ArrayCreation ac1 && e2 instanceof ArrayCreation ac2) {
			compareStandard(e1, e2); 
			compareDimensionList(ac1.dimensions(), ac2.dimensions());
			compareExpression(ac1.getInitializer(), ac2.getInitializer());
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof ArrayInitializer ac1 && e2 instanceof ArrayInitializer ac2) {
			compareStandard(e1, e2);
			compareExpressionList(ac1.expressions(), ac2.expressions());
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof Assignment ac1 && e2 instanceof Assignment ac2) {
			compareStandard(e1, e2);
			compareExpression(ac1.getLeftHandSide(), ac2.getLeftHandSide());
			compareExpression(ac1.getRightHandSide(), ac2.getRightHandSide());
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof Name ac1 && e2 instanceof Name ac2) {
			compareStandard(e1, e2);
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof BooleanLiteral ac1 && e2 instanceof BooleanLiteral ac2) {
			compareStandard(e1, e2);
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof CharacterLiteral ac1 && e2 instanceof CharacterLiteral ac2) {
			compareStandard(e1, e2);
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof StringLiteral ac1 && e2 instanceof StringLiteral ac2) {
			compareStandard(e1, e2);
			nullSafeStringCompare(e1, e2);
		} else if( e1 instanceof SuperMethodInvocation ac1 && e2 instanceof SuperMethodInvocation ac2) {
			compareStandard(e1, e2);
			nullSafeStringCompare(e1, e2);
		} else {
			handleError("Expression not comparable");
		}
		nullSafeStringCompare(e1, e2);
	}

	private void compareExpressionList(List nl1, List nl2) {
		boolean nl1Null = nl1 == null;
		boolean nl2Null = nl2 == null;
		if( (nl1Null && nl2Null))
			return;
		if( nl1Null || nl2Null ) {
			handleError("Expression list not comparable: null");
		}
		for( int i = 0; i < nl1.size(); i++ ) {
			compareExpression((Expression)nl1.get(i), (Expression)nl2.get(i));
		}
	}
	

	private void compareTypeList(List nl1, List nl2) {
		boolean nl1Null = nl1 == null;
		boolean nl2Null = nl2 == null;
		if( (nl1Null && nl2Null))
			return;
		if( nl1Null || nl2Null ) {
			handleError("Expression list not comparable: null");
		}
		for( int i = 0; i < nl1.size(); i++ ) {
			compareTypeObj((Type)nl1.get(i), (Type)nl2.get(i));
		}
	}
	
	private void compareAnonymousClassDecl(AnonymousClassDeclaration o1,
			AnonymousClassDeclaration o2) {
		boolean nl1Null = o1 == null;
		boolean nl2Null = o2 == null;
		if( (nl1Null && nl2Null))
			return;
		if( nl1Null || nl2Null ) {
			handleError("Expression list not comparable: null");
		}

		compareStandard(o1, o2);

		if( o1.bodyDeclarations().size() != o2.bodyDeclarations().size()) {
			handleError("Body declarations does not match size");
		}
		for( int i = 0; i < o1.bodyDeclarations().size(); i++ ) {
			compareBodyDeclaration(o1.bodyDeclarations().get(i), o2.bodyDeclarations().get(i));
		}
		
	}
	private void compareLambdaExpression(LambdaExpression le1, LambdaExpression le2) {
		compareStandard(le1, le2);
		List params1 = le1.parameters();
		List params2 = le2.parameters();
		if( params1.size() != params2.size()) {
			handleError("VariableDeclarationStatement fragments wrong sizes");
		}
		for( int i = 0; i < params1.size(); i++ ) {
			Object o1FragI = params1.get(i);
			Object o2FragI = params2.get(i);
			if( o1FragI instanceof SingleVariableDeclaration z1 && o2FragI instanceof SingleVariableDeclaration z2) {
				compareVariableDeclaration(z1, z2);
			} else if( o1FragI instanceof VariableDeclarationFragment w1 && o2FragI instanceof VariableDeclarationFragment w2) {
				compareVariableDeclaration(w1, w2);
			} else {
				handleError("LambdaExpression params wrong types");
			}
		}
		ASTNode body1 = le1.getBody();
		ASTNode body2 = le2.getBody();
		if( body1 instanceof Expression z1 && body2 instanceof Expression z2) {
			compareExpression(z1, z2);
		} else if( body1 instanceof Block w1 && body2 instanceof Block w2) {
			compareBlock(w1, w2);
		} else {
			handleError("LambdaExpression body wrong types");
		}
	}
	private void compareFieldDeclaration(FieldDeclaration object, FieldDeclaration object2) {
		compareStandard(object, object2);
		compareStandardBodyDecl(object, object2);
		List l1 = object.fragments();
		List l2 = object2.fragments();
		if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
			handleError("fragments does not match null");				
		}
		if( l1 != null ) {
			if( l1.size() != l2.size() ) {
				handleError("fragments does not match size");
			}
			for( int i = 0; i < l1.size(); i++ ) {
				compareVariableDeclaration((VariableDeclarationFragment)l1.get(i), (VariableDeclarationFragment)l2.get(i));
			}
		}
		nullSafeStringCompare(object, object2, "Comparator incomplete; field decls are different.");
	}
	
	private void compareDimensionList(List l1, List l2) {
		if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
			handleError("dimensions does not match null");				
		}
		if( l1 != null ) {
			if( l1.size() != l2.size() ) {
				handleError("dimensions does not match size");
			}
			for( int i = 0; i < l1.size(); i++ ) {
				compareDimension((Dimension)l1.get(i), (Dimension)l2.get(i));
			}
		}
	}
	
	private void compareVariableDeclaration(VariableDeclaration object1,
			VariableDeclaration object2) {
		compareStandard(object1, object2);
		if( object1.getExtraDimensions() != object2.getExtraDimensions()) {
			handleError("getExtraDimensions does not match");
		}
		if( version > AST.JLS4_INTERNAL) {
			compareDimensionList(object1.extraDimensions(), object2.extraDimensions());
		}

		nullSafeStringCompare(object1.getName(), object2.getName());
		compareExpression(object1.getInitializer(), object2.getInitializer());
		nullSafeStringCompare(object1, object2, "Comparator incomplete; var decl fragments are different.");
	}
	
	private void compareMethodDeclaration(MethodDeclaration object, MethodDeclaration object2) {
		compareStandard(object, object2);
		compareStandardBodyDecl(object, object2);
		
		if( !object.getName().toString().equals(object2.getName().toString())) {
			handleError("getName does not match");
		}
		if( object.getExtraDimensions() != object2.getExtraDimensions()) {
			handleError("getExtraDimensions does not match");
		}
		if( version > AST.JLS4_INTERNAL) {
			List l1 = object.extraDimensions();
			List l2 = object2.extraDimensions();
			if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
				handleError("extraDimensions does not match null");				
			}
			if( l1 != null ) {
				if( l1.size() != l2.size() ) {
					handleError("extraDimensions does not match size");
				}
				for( int i = 0; i < l1.size(); i++ ) {
					compareDimension((Dimension)l1.get(i), (Dimension)l2.get(i));
				}
			}
		}
		if( version > AST.JLS15_INTERNAL) {
			if( object.isCompactConstructor() != object2.isCompactConstructor()) {
				handleError("isCompactConstructor does not match");
			}
		}
		if( object.isConstructor() != object2.isConstructor()) {
			handleError("isConstructor does not match");
		}
		if( version == AST.JLS2_INTERNAL) {
			compareTypeObj(object.getReturnType(), object2.getReturnType());
		} else {
			compareTypeObj(object.getReturnType2(), object2.getReturnType2());
		}
		if( version > AST.JLS4_INTERNAL) {
			nullSafeStringCompare(object.getReceiverQualifier(), object2.getReceiverQualifier());
			compareTypeObj(object.getReceiverType(), object2.getReceiverType());
		}
		if( version <= AST.JLS4_INTERNAL) {
			List l1 = object.thrownExceptions();
			List l2 = object2.thrownExceptions();
			if( (l1 == null && l2 != null) || (l1 != null && l2 == null)) {
				handleError("thrownExceptions does not match, nullity");
			}
			if( l1 != null ) {
				if( l1.size() != l2.size() ) {
					handleError("thrownExceptions does not match size");
				}
				for( int i = 0; i < l1.size(); i++ ) {
					nullSafeStringCompare(l1.get(i), l2.get(i));
				}
			}
		} else {
			List l1 = object.thrownExceptionTypes();
			List l2 = object2.thrownExceptionTypes();
			if( (l1 == null && l2 != null) || (l1 != null && l2 == null)) {
				handleError("thrownExceptionTypes does not match, nullity");
			}
			if( l1 != null ) {
				if( l1.size() != l2.size() ) {
					handleError("thrownExceptionTypes does not match size");
				}
				for( int i = 0; i < l1.size(); i++ ) {
					compareTypeObj((Type)l1.get(i), (Type)l2.get(i));
				}
			}
		}
		if( version != AST.JLS2_INTERNAL) {
			// TODO typeParameters
			List l1 = object.typeParameters();
			List l2 = object2.typeParameters();
			
			if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
				handleError("methoddecl type params does not match null");				
			}
			if( l1.size() != l2.size() ) {
				handleError("methoddecl type params does not match size");
			}
			for( int i = 0; i < l1.size(); i++ ) {
				compareTypeParameter((TypeParameter)l1.get(i), (TypeParameter)l2.get(i));
			}
		}
		
		List l1 = object.parameters();
		List l2 = object2.parameters();
		
		if( (l1 == null && l2 != null) || (l2 == null && l1 != null)) {
			handleError("methoddecl params does not match null");				
		}
		if( l1 != null && l2 != null ) {
			if( l1.size() != l2.size() ) {
				handleError("methoddecl params does not match size");
			}
			for( int i = 0; i < l1.size(); i++ ) {
				compareVariableDeclaration((SingleVariableDeclaration)l1.get(i), (SingleVariableDeclaration)l2.get(i));
			}
		}
		
		compareBlock(object.getBody(), object2.getBody());
		nullSafeStringCompare(object, object2, "Comparator incomplete; method decls are different.");
	}
	private void compareDimension(Dimension d1, Dimension d2) {
		compareStandard(d1, d2);
		// TODO more
		nullSafeStringCompare(d1, d2, "Comparator incomplete; dimensions are different.");
	}
	
	private void nullSafeStringCompare(Object o1, Object o2) {
		nullSafeStringCompare(o1, o2, "");
	}
	private void nullSafeStringCompare(Object o1, Object o2, String prefix) {

		if( o1 == null ) {
			if( o2 == null ) {
				return;
			}
			handleError(prefix + ": not equal nullity");
		}
		if( !o1.toString().equals(o2.toString())) {
			handleError(prefix + ": not equal");
		}
	}
	
	private void compareTypeParameter(TypeParameter typeParameter, TypeParameter typeParameter2) {
		compareStandard(typeParameter, typeParameter2);
		compareStandard(typeParameter.getName(), typeParameter2.getName());
		// TODO compare type bounds
		nullSafeStringCompare(typeParameter, typeParameter2, "Comparator incomplete; type param are different.");
	}
	private void compareSuperInterfaceTypes(Object object, Object object2) {
		nullSafeStringCompare(object, object, "Comparator incomplete; super interface types are different.");
	}
	private void compareSuperInterfaceNames(Object object, Object object2) {
		nullSafeStringCompare(object, object, "Comparator incomplete; super interface names are different.");
	}
	private void comparePermittedTypes(List permittedTypes, List permittedTypes2) {
		int x = 1;
		nullSafeStringCompare(permittedTypes, permittedTypes2, "Comparator incomplete; permitted types are different.");
	}
	private void compareTypeObj(Type superclassType, Type superclassType2) {
		compareStandard(superclassType, superclassType2);
		nullSafeStringCompare(superclassType, superclassType2, "Comparator incomplete; type objs are different.");
	}
	private void compareJavadoc(Javadoc javadoc, Javadoc javadoc2) {
		if( javadoc == null && javadoc2 == null )
			return;
		if( (javadoc == null && javadoc2 != null) || (javadoc2 == null && javadoc != null)) {
			handleError("javadoc not comparable: null");
		}
		compareStandard(javadoc, javadoc2);
		if( version == AST.JLS2_INTERNAL) {
			nullSafeStringCompare(javadoc.getComment(), javadoc2.getComment());
		} 
		
		List l1 = javadoc.tags();
		List l2 = javadoc2.tags();
		if( (l1 == null && l2 != null) || (l1 != null && l2 == null)) {
			handleError("javadoc tags does not match, nullity");
		}
		if( l1 != null ) {
			if( l1.size() != l2.size() ) {
				handleError("javadoc tags does not match size");
			}
			for( int i = 0; i < l1.size(); i++ ) {
				compareTagElement((TagElement)l1.get(i), (TagElement)l2.get(i));
			}
		}
	}

	private void compareTagElement(TagElement tagElement, TagElement tagElement2) {
		compareStandard(tagElement, tagElement2);
		//nullSafeStringCompare(tagElement, tagElement2, "Comparator incomplete; tag elements are different.");
	}
	public void compare(PackageDeclaration p1, PackageDeclaration p2) {
		compareStandard(p1, p2);
		if( p1 == null )
			return;
		
		if( p1.getLength() != p2.getLength()) {
			handleError("package lengths differ");
		}
		if( p1.typeAndFlags != p2.typeAndFlags) {
			handleError("flags are wrong for package declaration ");
		}
		if( p1.getName().equals(p2.getName())) {
			handleError("package names differ");
		}
		nullSafeStringCompare(p1, p2, "Comparator incomplete; package decls are different.");
	}

	private void handleError(String msg) {
		System.out.println("*** " + msg);
		throw new RuntimeException(msg);
	}

	private void handleWarning(String msg) {
		System.out.println("*** " + msg);
		//throw new RuntimeException(msg);
	}
}
