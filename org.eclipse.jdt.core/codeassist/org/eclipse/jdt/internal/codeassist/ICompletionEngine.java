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
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public interface ICompletionEngine {
	public HashtableOfObject getTypeCache();
	public int getOpenedBinaryTypes();
	public void incrementOpenedBinaryTypes();
}
