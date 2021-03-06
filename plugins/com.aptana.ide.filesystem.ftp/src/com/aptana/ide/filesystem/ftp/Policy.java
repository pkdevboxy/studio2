/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
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
 * with certain other free and open source software ("FOSS") code and certain additional terms
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

package com.aptana.ide.filesystem.ftp;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.aptana.ide.core.io.IBaseRemoteConnectionPoint;
import com.aptana.ide.core.io.vfs.IExtendedFileInfo;

/**
 * @author Max Stepanov
 *
 */
public final class Policy {

	/**
	 * 
	 */
	private Policy() {
	}
	
	public static String generateAuthId(String proto, IBaseRemoteConnectionPoint connectionPoint) {
		return generateAuthId(proto, connectionPoint.getLogin(), connectionPoint.getHost(), connectionPoint.getPort());
	}

	public static String generateAuthId(String proto, String login, String host, int port) {
		if (host != null && host.length() > 0 && port > 0 && login != null && login.length() > 0) {
			return MessageFormat.format("{0}/{1}@{2}:{3}", new Object[] { //$NON-NLS-1$
					proto, login, host, Integer.toString(port)
			});
		}
		return null;
	}

	public static long permissionsFromString(String string) {
		long permissions = 0;
		if (string != null && string.length() >= 10) {
			int index = 1;
			permissions |= (string.charAt(index++) == 'r') ? IExtendedFileInfo.PERMISSION_OWNER_READ : 0;
			permissions |= (string.charAt(index++) == 'w') ? IExtendedFileInfo.PERMISSION_OWNER_WRITE : 0;
			permissions |= (string.charAt(index++) == 'x') ? IExtendedFileInfo.PERMISSION_OWNER_EXECUTE : 0;
			
			permissions |= (string.charAt(index++) == 'r') ? IExtendedFileInfo.PERMISSION_GROUP_READ : 0;
			permissions |= (string.charAt(index++) == 'w') ? IExtendedFileInfo.PERMISSION_GROUP_WRITE : 0;
			permissions |= (string.charAt(index++) == 'x') ? IExtendedFileInfo.PERMISSION_GROUP_EXECUTE : 0;
			
			permissions |= (string.charAt(index++) == 'r') ? IExtendedFileInfo.PERMISSION_OTHERS_READ : 0;
			permissions |= (string.charAt(index++) == 'w') ? IExtendedFileInfo.PERMISSION_OTHERS_WRITE : 0;
			permissions |= (string.charAt(index++) == 'x') ? IExtendedFileInfo.PERMISSION_OTHERS_EXECUTE : 0;
		}
		return permissions;
	}
	
	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		return monitor == null ? new NullProgressMonitor() : monitor;
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}

	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}


}
