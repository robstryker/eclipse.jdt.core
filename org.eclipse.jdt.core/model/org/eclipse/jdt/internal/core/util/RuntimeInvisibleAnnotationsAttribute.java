/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IAnnotation;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IRuntimeInvisibleAnnotationsAttribute;

/**
 * Default implementation of IRuntimeInvisibleAnnotations
 */
public class RuntimeInvisibleAnnotationsAttribute
	extends ClassFileAttribute
	implements IRuntimeInvisibleAnnotationsAttribute {

	private static final IAnnotation[] NO_ENTRIES = new IAnnotation[0];
	private final int annotationsNumber;
	private IAnnotation[] annotations;

	/**
	 * Constructor for RuntimeInvisibleAnnotations.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public RuntimeInvisibleAnnotationsAttribute(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset)
			throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		final int length = u2At(classFileBytes, 6, offset);
		this.annotationsNumber = length;
		if (length != 0) {
			int readOffset = 8;
			this.annotations = new IAnnotation[length];
			for (int i = 0; i < length; i++) {
				Annotation annotation = new Annotation(classFileBytes, constantPool, offset + readOffset);
				this.annotations[i] = annotation;
				readOffset += annotation.sizeInBytes();
			}
		} else {
			this.annotations = NO_ENTRIES;
		}
	}

	@Override
	public IAnnotation[] getAnnotations() {
		return this.annotations;
	}

	@Override
	public int getAnnotationsNumber() {
		return this.annotationsNumber;
	}
}
