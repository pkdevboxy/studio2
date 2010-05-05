/**
 * This file Copyright (c) 2005-2007 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.syncing.ui.ingo.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.progress.UIJob;

import com.aptana.ide.syncing.ui.SyncingUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public final class SyncUIUtils
{

	/**
	 * Update the sync labels
	 */
	public static void updateSyncLabels()
	{
		UIJob job = new UIJob("Updating sync labels") //$NON-NLS-1$
		{

			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				IDecoratorManager dm = SyncingUIPlugin.getDefault().getWorkbench().getDecoratorManager();
				dm.update("com.aptana.ide.syncing.SyncConnectionDecorator"); //$NON-NLS-1$
				dm.update("com.aptana.ide.syncing.SyncProjectConnectionDecorator"); //$NON-NLS-1$
				dm.update("com.aptana.ide.syncing.VirtualFileManagerSyncDecorator"); //$NON-NLS-1$
				return Status.OK_STATUS;
			}

		};
		job.setSystem(true);
		job.schedule();
	}
}