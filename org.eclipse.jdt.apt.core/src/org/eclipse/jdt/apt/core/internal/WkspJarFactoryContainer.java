/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * Annotation processor factory container based on a jar file 
 * within the workspace.
 */
public class WkspJarFactoryContainer extends JarFactoryContainer {

	private final String _id;
	private final File _jarFile; // A java.io.File, not guaranteed to exist.

	/**
	 * Construct a workspace-jar container from an IPath representing
	 * the jar file's location in the workspace.  We will construct
	 * the container even if the file does not exist.
	 * @param jar an IPath representing a jar file in the workspace;
	 * the path is relative to the workspace root.
	 */
	public WkspJarFactoryContainer(IPath jar) {
		_id = jar.toString();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource res = root.findMember(_id);
		if (null == res) {
			// The file evidently doesn't exist on disk.  Do our best to 
			// construct a java.io.File for it anyway.
			_jarFile = root.getLocation().append(jar).toFile();
			
		}
		else if (res.getType() == IResource.FILE) {
			_jarFile = res.getLocation().toFile();
		}
		else {
			_jarFile = null;
			IStatus s = AptPlugin.createWarningStatus(
				null, "The factorypath entry " + _id + " does not refer to a jar file"); //$NON-NLS-1$ //$NON-NLS-2$
			AptPlugin.log(s);
		}
	}

	@Override
	public FactoryType getType() {
		return FactoryType.WKSPJAR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.JarFactoryContainer#getJarFile()
	 */
	@Override
	public File getJarFile() {
		return _jarFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.FactoryContainer#getId()
	 */
	@Override
	public String getId() {
		return _id;
	}
}
