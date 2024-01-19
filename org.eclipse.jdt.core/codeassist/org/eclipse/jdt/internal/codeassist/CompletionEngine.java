/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Timo Kinnunen - Contributions for bug 377373 - [subwords] known limitations with JDT 3.8
 *     							Bug 420953 - [subwords] Constructors that don't match prefix not found
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *     Gábor Kövesdán - Contribution for Bug 350000 - [content assist] Include non-prefix matches in auto-complete suggestions
 *     Microsoft Corporation - Contribution for bug 575562 - improve completion search performance
 *     							Bug 578817 - skip the ignored types before creating CompletionProposals
 *     Microsoft Corporation - Contribution for bug 578815 - more candidates when completion after new keyword for an interface
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnJavadoc;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnJavadocTag;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.codeassist.complete.CompletionScanner;
import org.eclipse.jdt.internal.codeassist.complete.ICompletionParserFacade;
import org.eclipse.jdt.internal.codeassist.impl.AssistParser;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * This class is the entry point for source completions.
 * It contains two public APIs used to call CodeAssist on a given source with
 * a given environment, assisting position and storage (and possibly options).
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class CompletionEngine extends BaseCompletionEngine {
	/**
	 * The CompletionEngine is responsible for computing source completions.
	 *
	 * It requires a searchable name environment, which supports some specific search APIs, and a requestor to feed back
	 * the results to a UI.
	 *
	 * @param nameEnvironment
	 *            org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment used to resolve type/package references
	 *            and search for types/packages based on partial names.
	 *
	 * @param requestor
	 *            org.eclipse.jdt.internal.codeassist.ICompletionRequestor since the engine might produce answers of
	 *            various forms, the engine is associated with a requestor able to accept all possible completions.
	 *
	 * @param settings
	 *            java.util.Map set of options used to configure the code assist engine.
	 */
	public CompletionEngine(SearchableEnvironment nameEnvironment, CompletionRequestor requestor, Map settings,
			IJavaProject javaProject, WorkingCopyOwner owner, IProgressMonitor monitor) {
		super(nameEnvironment, requestor, settings, javaProject, owner, monitor);
	}

	@Override
	protected ICompletionParserFacade createCompletionParser(ProblemReporter problemReporter2, CompletionRequestor requestor2,
			IProgressMonitor monitor2) {
		return new CompletionParser(problemReporter2, requestor2.isExtendedContextRequired(), monitor2);
	}

	@Override
	public ICompletionParserFacade getParserFacade() {
		return (CompletionParser)getParser();
	}

	@Override
	public HashtableOfObject getTypeCache() {
		return this.typeCache;
	}

	@Override
	public int getOpenedBinaryTypes() {
		return this.openedBinaryTypes;
	}

	@Override
	public void incrementOpenedBinaryTypes() {
		this.openedBinaryTypes++;
	}

	@Override
	protected void buildContextSetTokenRange(InternalCompletionContext context, ASTNode astNode) {
		if (this.parser instanceof AssistParser) {
			buildContextSetTokenRangeFromAssistParser(context, astNode, (AssistParser) this.parser);
		}
	}

	protected void buildContextSetTokenRangeFromAssistParser(InternalCompletionContext context, ASTNode astNode, AssistParser parser2) {
		if (!(astNode instanceof CompletionOnJavadoc)) {
			CompletionScanner scanner = (CompletionScanner)parser2.scanner;
			context.setToken(scanner.completionIdentifier);
			context.setTokenRange(
					scanner.completedIdentifierStart - this.offset,
					scanner.completedIdentifierEnd - this.offset,
					scanner.endOfEmptyToken - this.offset);
		} else if(astNode instanceof CompletionOnJavadocTag) {
			CompletionOnJavadocTag javadocTag = (CompletionOnJavadocTag) astNode;
			context.setToken(CharOperation.concat(new char[]{'@'}, javadocTag.token));
			context.setTokenRange(
					javadocTag.tagSourceStart - this.offset,
					javadocTag.tagSourceEnd - this.offset,
					((CompletionScanner)parser2.javadocParser.scanner).endOfEmptyToken - this.offset);
		} else {
			CompletionScanner scanner = (CompletionScanner)parser2.javadocParser.scanner;
			context.setToken(scanner.completionIdentifier);
			context.setTokenRange(
					scanner.completedIdentifierStart - this.offset,
					scanner.completedIdentifierEnd - this.offset,
					scanner.endOfEmptyToken - this.offset);
		}
	}

	@Override
	protected IMissingTypesGuesser createMissingTypeGuesser() {
		return new MissingTypesGuesser(this);
	}

	@Override
	protected IUnresolvedReferenceNameFinder createUnresolvedReferenceNameFinder() {
		return new UnresolvedReferenceNameFinder(this, (CompletionParser)this.parser);
	}

	@Override
	protected int getEndOfEmptyToken() {
		Scanner scanner = this.parser.getScanner();
		if( scanner instanceof CompletionScanner ) {
			return ((CompletionScanner)scanner).endOfEmptyToken;
		}
		return -1;
	}

}
