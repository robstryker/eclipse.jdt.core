/*******************************************************************************
 * Copyright (c) 2023, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.parser.JavadocTokenizer;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.Options;

class JavacCompilationUnitResolver implements ICompilationUnitResolver {

	@Override
	public void resolve(String[] sourceFilePaths, String[] encodings, String[] bindingKeys, FileASTRequestor requestor,
			int apiLevel, Map<String, String> compilerOptions, List<Classpath> list, int flags,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'resolve'");
	}

	@Override
	public void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
		// TODO ECJCompilationUnitResolver has support for dietParse and ignore method body
		// is this something we need?
		for (ICompilationUnit in : compilationUnits) {
			if (in instanceof org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerUnit) {
				requestor.acceptAST(in, parse(compilerUnit, apiLevel, compilerOptions, flags, null, monitor));
			}
		}
	}


	@Override
	public void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'parse'");
	}

	@Override
	public void resolve(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, IJavaProject project, WorkingCopyOwner workingCopyOwner, int flags,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'resolve'");
	}

	@Override
	public IBinding[] resolve(IJavaElement[] elements, int apiLevel, Map<String, String> compilerOptions,
			IJavaProject project, WorkingCopyOwner workingCopyOwner, int flags, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'resolve'");
	}

	@Override
	public CompilationUnit toCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
			boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths,
			NodeSearcher nodeSearcher, int apiLevel, Map<String, String> compilerOptions,
			WorkingCopyOwner workingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags, IProgressMonitor monitor) {
		// TODO currently only parse
		CompilationUnit res = parse(sourceUnit, apiLevel, compilerOptions, flags, project, monitor);
		if (initialNeedsToResolveBinding) {
			res.getPackage().resolveBinding();
		}
		// For comparison
		CompilationUnit res2  = CompilationUnitResolver.FACADE.toCompilationUnit(sourceUnit, initialNeedsToResolveBinding, project, classpaths, nodeSearcher, apiLevel, compilerOptions, typeRootWorkingCopyOwner, typeRootWorkingCopyOwner, flags, monitor);
		//res.typeAndFlags=res2.typeAndFlags;
		String res1a = res.toString();
		String res2a = res2.toString();
		return res;
	}

	public CompilationUnit parse(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, int apiLevel, Map<String, String> compilerOptions,
			int flags, IJavaProject javaProject, IProgressMonitor monitor) {
		SimpleJavaFileObject fileObject = new SimpleJavaFileObject(new File(new String(sourceUnit.getFileName())).toURI(), JavaFileObject.Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors) throws java.io.IOException {
				return new String(sourceUnit.getContents());
			}
		};
		Context context = new Context();
		Options options = Options.instance(context);
		options.put(Option.XLINT, Boolean.TRUE.toString()); // TODO refine according to compilerOptions
		options.put(Option.XLINT_CUSTOM, "all"); // TODO refine according to compilerOptions
		// TODO populate more from compilerOptions and/or project settings
		AST ast = createAST(compilerOptions, apiLevel, context);
//		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
//		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
//		ast.setDefaultNodeFlag(savedDefaultNodeFlag);
		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
		CompilationUnit res = ast.newCompilationUnit();
		context.put(DiagnosticListener.class, new DiagnosticListener<JavaFileObject>() {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				if (Objects.equals(diagnostic.getSource(), fileObject) ||
					diagnostic.getSource() instanceof DiagnosticSource source && Objects.equals(source.getFile(), fileObject)) {
					IProblem[] previous = res.getProblems();
					IProblem[] newProblems = Arrays.copyOf(previous, previous.length + 1);
					newProblems[newProblems.length - 1] = JavacConverter.convertDiagnostic(diagnostic);
					res.setProblems(newProblems);
				}
			}
		});
		JavacFileManager.preRegister(context);
		if (javaProject instanceof JavaProject internal) {
			configurePaths(internal, context);
		}
		Todo.instance(context); // initialize early
		com.sun.tools.javac.main.JavaCompiler javac = new com.sun.tools.javac.main.JavaCompiler(context);
		javac.keepComments = true;
		javac.genEndPos = true;
		javac.lineDebugInfo = true;
		JCCompilationUnit javacCompilationUnit = javac.parse(fileObject);
		JavacConverter converter = new JavacConverter(ast, javacCompilationUnit, context);
		converter.populateCompilationUnit(res, javacCompilationUnit);
		attachComments(res, context, fileObject, converter, compilerOptions);
		ast.setBindingResolver(new JavacBindingResolver(javac, javaProject, context, converter));
		//
		ast.setOriginalModificationCount(ast.modificationCount()); // "un-dirty" AST so Rewrite can process it
		return res;
	}

	private List<File> classpathEntriesToFiles(JavaProject project, Predicate<IClasspathEntry> select) {
		try {
			IClasspathEntry[] selected = Arrays.stream(project.getRawClasspath())
				.filter(select)
				.toArray(IClasspathEntry[]::new);
			return Arrays.stream(project.resolveClasspath(selected))
				.map(IClasspathEntry::getPath)
				.map(path -> {
					File asFile = path.toFile();
					if (asFile.exists()) {
						return asFile;
					}
					IResource asResource = project.getProject().getParent().findMember(path);
					if (asResource != null) {
						return asResource.getLocation().toFile();
					}
					return null;
				}).filter(Objects::nonNull)
				.filter(File::exists)
				.toList();
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return List.of();
		}
	}

	private void configurePaths(JavaProject javaProject, Context context) {
		JavacFileManager fileManager = (JavacFileManager)context.get(JavaFileManager.class);
		try {
			IResource member = javaProject.getProject().getParent().findMember(javaProject.getOutputLocation());
			if( member != null ) {
				File f = member.getLocation().toFile();
				List l = List.of(f);
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, l);
			}
			fileManager.setLocation(StandardLocation.SOURCE_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE));
			fileManager.setLocation(StandardLocation.CLASS_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() != IClasspathEntry.CPE_SOURCE));
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}

	private AST createAST(Map<String, String> options, int level, Context context) {
		AST ast = AST.newAST(level, JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)));
		String sourceModeSetting = options.get(JavaCore.COMPILER_SOURCE);
		long sourceLevel = CompilerOptions.versionToJdkLevel(sourceModeSetting);
		if (sourceLevel == 0) {
			// unknown sourceModeSetting
			sourceLevel = ClassFileConstants.JDK21; // TODO latest
		}
		ast.scanner.sourceLevel = sourceLevel;
		String compliance = options.get(JavaCore.COMPILER_COMPLIANCE);
		long complianceLevel = CompilerOptions.versionToJdkLevel(compliance);
		if (complianceLevel == 0) {
			// unknown sourceModeSetting
			complianceLevel = sourceLevel;
		}
		ast.scanner.complianceLevel = complianceLevel;
		ast.scanner.previewEnabled = JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES));
//		int savedDefaultNodeFlag = ast.getDefaultNodeFlag();
//		BindingResolver resolver = null;
//		if (isResolved) {
//			resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope, workingCopy.owner, new DefaultBindingResolver.BindingTables(), false, true);
//			((DefaultBindingResolver) resolver).isRecoveringBindings = (reconcileFlags & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0;
//			ast.setFlag(AST.RESOLVED_BINDINGS);
//		} else {
//			resolver = new BindingResolver();
//		}
//		ast.setFlag(reconcileFlags);
//		ast.setBindingResolver(resolver);
//
//		CompilationUnit unit = converter.convert(compilationUnitDeclaration, workingCopy.getContents());
//		unit.setLineEndTable(compilationUnitDeclaration.compilationResult.getLineSeparatorPositions());
//		unit.setTypeRoot(workingCopy.originalFromClone());
		return ast;
	}

	private class JavadocTokenizerFeedingComments extends JavadocTokenizer {
		public final List<org.eclipse.jdt.core.dom.Comment> comments = new ArrayList<>();
		private final JavacConverter converter;

		public JavadocTokenizerFeedingComments(ScannerFactory factory, char[] content, JavacConverter converter) {
			super(factory, content, content.length);
			this.converter = converter;
		}

		@Override
		protected Comment processComment(int pos, int endPos, CommentStyle style) {
			Comment res = super.processComment(pos, endPos, style);
			this.comments.add(this.converter.convert(res, pos, endPos));
			return res;
		}
	}

	/**
	 * Currently re-scans the doc to build the list of comments and then
	 * attach them to the already built AST.
	 * @param res
	 * @param context
	 * @param fileObject
	 * @param converter
	 * @param compilerOptions
	 */
	private void attachComments(CompilationUnit res, Context context, FileObject fileObject, JavacConverter converter, Map<String, String> compilerOptions) {
		try {
			char[] content =  fileObject.getCharContent(false).toString().toCharArray();
			ScannerFactory scannerFactory = ScannerFactory.instance(context);
			JavadocTokenizerFeedingComments commentTokenizer = new JavadocTokenizerFeedingComments(scannerFactory, content, converter);
			Scanner javacScanner = new Scanner(scannerFactory, commentTokenizer) {
				// subclass just to access constructor
				// TODO DefaultCommentMapper.this.scanner.linePtr == -1?
			};
			do { // consume all tokens to populate comments
				javacScanner.nextToken();
			} while (javacScanner.token() != null && javacScanner.token().kind != TokenKind.EOF);
//			commentTokenizer.comments.forEach(comment -> comment.setAlternateRoot(res));
			res.setCommentTable(commentTokenizer.comments.toArray(org.eclipse.jdt.core.dom.Comment[]::new));
			org.eclipse.jdt.internal.compiler.parser.Scanner ecjScanner = new ASTConverter(compilerOptions, false, null).scanner;
			ecjScanner.recordLineSeparator = true;
			ecjScanner.skipComments = false;
			try {
				ecjScanner.setSource(content);
				do {
					ecjScanner.getNextToken();
				} while (!ecjScanner.atEnd());
			} catch (InvalidInputException ex) {
				JavaCore.getPlugin().getLog().log(Status.error(ex.getMessage(), ex));
			}
			
			// need to scan with ecjScanner first to populate some line indexes used by the CommentMapper
			// on longer-term, implementing an alternative comment mapper based on javac scanner might be best
			res.initCommentMapper(ecjScanner);
			res.setCommentTable(commentTokenizer.comments.toArray(org.eclipse.jdt.core.dom.Comment[]::new)); // TODO only javadoc comments are in; need to add regular comments
			if (res.optionalCommentTable != null) {
				Arrays.stream(res.optionalCommentTable)
					.filter(Javadoc.class::isInstance)
					.map(Javadoc.class::cast)
					.forEach(doc -> attachToSibling(doc, res));
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void attachToSibling(Javadoc javadoc, CompilationUnit unit) {
		FindNextJavadocableSibling finder = new FindNextJavadocableSibling(javadoc.getStartPosition() + javadoc.getLength());
		unit.accept(finder);
		if (finder.nextNode != null) {
			if (finder.nextNode instanceof AbstractTypeDeclaration typeDecl) {
				typeDecl.setJavadoc(javadoc);
			} else if (finder.nextNode instanceof FieldDeclaration fieldDecl) {
				fieldDecl.setJavadoc(javadoc);
			} else if (finder.nextNode instanceof BodyDeclaration methodDecl) {
				methodDecl.setJavadoc(javadoc);
			}
			int endOffset = finder.nextNode.getStartPosition() + finder.nextNode.getLength();
			finder.nextNode.setSourceRange(javadoc.getStartPosition(), endOffset - javadoc.getStartPosition());
		}
	}
}
