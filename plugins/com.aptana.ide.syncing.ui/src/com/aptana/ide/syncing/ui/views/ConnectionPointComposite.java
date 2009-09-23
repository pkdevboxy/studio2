/**
 * This file Copyright (c) 2005-2009 Aptana, Inc. This program is
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
package com.aptana.ide.syncing.ui.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.aptana.ide.core.CoreStrings;
import com.aptana.ide.core.FileUtils;
import com.aptana.ide.core.io.IConnectionPoint;
import com.aptana.ide.core.ui.CoreUIUtils;
import com.aptana.ide.core.ui.SWTUtils;
import com.aptana.ide.syncing.ui.SyncingUIPlugin;
import com.aptana.ide.syncing.ui.internal.SyncUtils;
import com.aptana.ide.ui.io.IOUIPlugin;
import com.aptana.ide.ui.io.actions.CopyFilesOperation;
import com.aptana.ide.ui.io.navigator.FileTreeContentProvider;
import com.aptana.ide.ui.io.navigator.FileTreeNameSorter;
import com.aptana.ide.ui.io.navigator.actions.FileSystemDeleteAction;
import com.aptana.ide.ui.io.navigator.actions.FileSystemRenameAction;
import com.aptana.ide.ui.io.navigator.actions.OpenFileAction;

/**
 * @author Michael Xia (mxia@aptana.com)
 */
public class ConnectionPointComposite implements SelectionListener, ISelectionChangedListener,
        IDoubleClickListener, TransferDragSourceListener, DropTargetListener {

    private static final String[] COLUMN_NAMES = {
            Messages.ConnectionPointComposite_Column_Filename,
            Messages.ConnectionPointComposite_Column_Size,
            Messages.ConnectionPointComposite_Column_LastModified };

    private Composite fMain;
    private Label fEndPointLabel;
    private ToolItem fUpItem;
    private ToolItem fRefreshItem;
    private ToolItem fHomeItem;
    private Link fPathLink;

    private TreeViewer fTreeViewer;
    private MenuItem fOpenItem;
    private MenuItem fDeleteItem;
    private MenuItem fRenameItem;
    private MenuItem fRefreshMenuItem;
    private MenuItem fPropertiesItem;

    private String fName;
    private IConnectionPoint fConnectionPoint;
    private List<IAdaptable> fEndPointData;

    public ConnectionPointComposite(Composite parent, String name) {
        fName = name;
        fEndPointData = new ArrayList<IAdaptable>();

        fMain = createControl(parent);
    }

    public Control getControl() {
        return fMain;
    }

    public IAdaptable getCurrentInput() {
        // the root input is always IAdaptable
        return (IAdaptable) fTreeViewer.getInput();
    }

    public IAdaptable[] getSelectedElements() {
        ISelection selection = fTreeViewer.getSelection();
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            return new IAdaptable[0];
        }
        Object[] elements = ((IStructuredSelection) selection).toArray();
        // the selection should be all IAdaptable objects, but just to make sure
        List<IAdaptable> list = new ArrayList<IAdaptable>();
        for (Object element : elements) {
            if (element instanceof IAdaptable) {
                list.add((IAdaptable) element);
            }
        }
        return list.toArray(new IAdaptable[list.size()]);
    }

    public void setFocus() {
        fMain.setFocus();
    }

    public void setConnectionPoint(IConnectionPoint connection) {
        if (fConnectionPoint == connection) {
            return;
        }
        fConnectionPoint = connection;

        fEndPointData.clear();
        if (fConnectionPoint == null) {
            fEndPointLabel.setText(""); //$NON-NLS-1$
        } else {
            fEndPointLabel.setText(fConnectionPoint.getName());
            fEndPointData.add(fConnectionPoint);
        }
        setPath(""); //$NON-NLS-1$

        fTreeViewer.setInput(connection);
        updateActionStates();
    }

    public void refresh() {
        Object input = fTreeViewer.getInput();
        IResource resource = null;
        if (input instanceof IAdaptable) {
            resource = (IResource) ((IAdaptable) input).getAdapter(IResource.class);
        }
        if (resource != null) {
            try {
                resource.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
            }
        }
        updateContent(fEndPointData.get(fEndPointData.size() - 1));
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        Object source = e.getSource();

        if (source == fUpItem) {
            goUp();
        } else if (source == fRefreshItem) {
            refresh();
        } else if (source == fHomeItem) {
            gotoHome();
        } else if (source == fOpenItem) {
            open(fTreeViewer.getSelection());
        } else if (source == fDeleteItem) {
            delete(fTreeViewer.getSelection());
        } else if (source == fRenameItem) {
            rename();
        } else if (source == fRefreshMenuItem) {
            refresh(fTreeViewer.getSelection());
        } else if (source == fPropertiesItem) {
            openPropertyPage(fTreeViewer.getSelection());
        } else if (source == fPathLink) {
            // e.text has the index; needs to increment by 1 since 0 for
            // fEndPointData is the root
            updateContent(fEndPointData.get(Integer.parseInt(e.text) + 1));
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        updateMenuStates();
    }

    public void doubleClick(DoubleClickEvent event) {
        open(event.getSelection());
    }

    public Transfer getTransfer() {
        return LocalSelectionTransfer.getTransfer();
    }

    public void dragFinished(DragSourceEvent event) {
        LocalSelectionTransfer.getTransfer().setSelection(null);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
    }

    public void dragSetData(DragSourceEvent event) {
        event.data = fTreeViewer.getSelection();
    }

    public void dragStart(DragSourceEvent event) {
        LocalSelectionTransfer.getTransfer().setSelection(fTreeViewer.getSelection());
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
    }

    public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_DEFAULT) {
            if ((event.operations & DND.DROP_COPY) == 0) {
                event.detail = DND.DROP_NONE;
            } else {
                event.detail = DND.DROP_COPY;
            }
        }
    }

    public void dragLeave(DropTargetEvent event) {
    }

    public void dragOperationChanged(DropTargetEvent event) {
    }

    public void dragOver(DropTargetEvent event) {
    }

    public void drop(DropTargetEvent event) {
        IFileStore targetStore = null;
        if (event.item == null) {
            targetStore = SyncUtils.getFileStore((IAdaptable) fTreeViewer.getInput());
        } else {
            TreeItem target = (TreeItem) event.item;
            targetStore = getFolderStore((IAdaptable) target.getData());
        }
        if (targetStore == null) {
            return;
        }

        if (event.data instanceof ITreeSelection) {
            ITreeSelection selection = (ITreeSelection) event.data;
            TreePath[] paths = selection.getPaths();
            if (paths.length > 0) {
                List<IAdaptable> elements = new ArrayList<IAdaptable>();
                for (TreePath path : paths) {
                    boolean alreadyIn = false;
                    for (TreePath path2 : paths) {
                        if (!path.equals(path2) && path.startsWith(path2, null)) {
                            alreadyIn = true;
                            break;
                        }
                    }
                    if (!alreadyIn) {
                        elements.add((IAdaptable) path.getLastSegment());
                    }
                }

                CopyFilesOperation operation = new CopyFilesOperation(getControl().getShell());
                operation.copyFiles(elements.toArray(new IAdaptable[elements.size()]), targetStore,
                        new JobChangeAdapter() {

                            @Override
                            public void done(IJobChangeEvent event) {
                                IOUIPlugin.refreshNavigatorView(fTreeViewer.getInput());
                                CoreUIUtils.getDisplay().asyncExec(new Runnable() {

                                    public void run() {
                                        refresh();
                                    }
                                });
                            }
                        });
            }
        }
    }

    public void dropAccept(DropTargetEvent event) {
    }

    protected Composite createControl(Composite parent) {
        Composite main = new Composite(parent, SWT.NONE);
        main.setLayout(new GridLayout());

        Composite top = createTopComposite(main);
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        fPathLink = new Link(main, SWT.NONE);
        fPathLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        // uses a bold font for path
        final Font font = new Font(fPathLink.getDisplay(), SWTUtils.boldFont(fPathLink.getFont()));
        fPathLink.setFont(font);
        fPathLink.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });
        fPathLink.addSelectionListener(this);

        TreeViewer treeViewer = createTreeViewer(main);
        treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        updateActionStates();

        return main;
    }

    private Composite createTopComposite(Composite parent) {
        Composite main = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        main.setLayout(layout);

        Label label = new Label(main, SWT.NONE);
        label.setText(fName + ":"); //$NON-NLS-1$

        fEndPointLabel = new Label(main, SWT.NONE);
        fEndPointLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createActionsBar(main);

        return main;
    }

    private ToolBar createActionsBar(Composite parent) {
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

        fUpItem = new ToolItem(toolbar, SWT.PUSH);
        fUpItem.setImage(SyncingUIPlugin.getImage("icons/full/obj16/up.png")); //$NON-NLS-1$
        fUpItem.setToolTipText(Messages.ConnectionPointComposite_TTP_Up);
        fUpItem.addSelectionListener(this);

        fRefreshItem = new ToolItem(toolbar, SWT.PUSH);
        fRefreshItem.setImage(SyncingUIPlugin.getImage("icons/full/obj16/refresh.gif")); //$NON-NLS-1$
        fRefreshItem.setToolTipText(Messages.ConnectionPointComposite_TTP_Refresh);
        fRefreshItem.addSelectionListener(this);

        fHomeItem = new ToolItem(toolbar, SWT.PUSH);
        fHomeItem.setImage(SyncingUIPlugin.getImage("icons/full/obj16/home.png")); //$NON-NLS-1$
        fHomeItem.setToolTipText(Messages.ConnectionPointComposite_TTP_Home);
        fHomeItem.addSelectionListener(this);

        return toolbar;
    }

    private TreeViewer createTreeViewer(Composite parent) {
        fTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);

        TreeColumn column = new TreeColumn(tree, SWT.LEFT);
        column.setWidth(300);
        column.setText(COLUMN_NAMES[0]);

        column = new TreeColumn(tree, SWT.LEFT);
        column.setWidth(50);
        column.setText(COLUMN_NAMES[1]);

        column = new TreeColumn(tree, SWT.LEFT);
        column.setWidth(125);
        column.setText(COLUMN_NAMES[2]);

        fTreeViewer.setContentProvider(new FileTreeContentProvider());
        fTreeViewer.setLabelProvider(new ConnectionPointLabelProvider());
        fTreeViewer.setComparator(new FileTreeNameSorter());
        fTreeViewer.addSelectionChangedListener(this);
        fTreeViewer.addDoubleClickListener(this);

        fTreeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_DEFAULT,
                new Transfer[] { LocalSelectionTransfer.getTransfer() }, this);
        fTreeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_DEFAULT,
                new Transfer[] { LocalSelectionTransfer.getTransfer() }, this);

        // builds the context menu
        tree.setMenu(createMenu(tree));

        return fTreeViewer;
    }

    private Menu createMenu(Control parent) {
        Menu menu = new Menu(parent);
        fOpenItem = new MenuItem(menu, SWT.PUSH);
        fOpenItem.setText(CoreStrings.OPEN);
        fOpenItem.setAccelerator(SWT.F3);
        fOpenItem.addSelectionListener(this);

        new MenuItem(menu, SWT.SEPARATOR);
        fDeleteItem = new MenuItem(menu, SWT.PUSH);
        fDeleteItem.setText(CoreStrings.DELETE);
        fDeleteItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
                ISharedImages.IMG_ETOOL_DELETE));
        fDeleteItem.setAccelerator(SWT.DEL);
        fDeleteItem.addSelectionListener(this);

        fRenameItem = new MenuItem(menu, SWT.PUSH);
        fRenameItem.setText(CoreStrings.RENAME);
        fRenameItem.setAccelerator(SWT.F2);
        fRenameItem.addSelectionListener(this);

        new MenuItem(menu, SWT.SEPARATOR);
        fRefreshMenuItem = new MenuItem(menu, SWT.PUSH);
        fRefreshMenuItem.setText(CoreStrings.REFRESH);
        fRefreshMenuItem.setImage(SyncingUIPlugin.getImage("/icons/full/obj16/refresh.gif")); //$NON-NLS-1$
        fRefreshMenuItem.setAccelerator(SWT.F5);
        fRefreshMenuItem.addSelectionListener(this);

        new MenuItem(menu, SWT.SEPARATOR);
        fPropertiesItem = new MenuItem(menu, SWT.PUSH);
        fPropertiesItem.setText(CoreStrings.PROPERTIES);
        fPropertiesItem.setAccelerator(SWT.ALT | '\r');
        fPropertiesItem.addSelectionListener(this);

        return menu;
    }

    private void goUp() {
        if (fEndPointData.size() > 1) {
            updateContent(fEndPointData.get(fEndPointData.size() - 2));
        } else {
            // already at root
            updateContent(fConnectionPoint);
        }
    }

    private void gotoHome() {
        updateContent(fConnectionPoint);
    }

    private void open(ISelection selection) {
        Object object = ((IStructuredSelection) selection).getFirstElement();
        if (object instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) object;
            IFileInfo fileInfo = SyncUtils.getFileInfo((IAdaptable) object);
            if (fileInfo.isDirectory()) {
                // goes into the folder
                updateContent(adaptable);
            } else {
                // opens the file in the editor
                OpenFileAction action = new OpenFileAction(CoreUIUtils.getActivePage());
                action.updateSelection((IStructuredSelection) selection);
                action.run();
            }
        }
    }

    private void delete(ISelection selection) {
        final FileSystemDeleteAction action = new FileSystemDeleteAction(getControl().getShell());
        action.updateSelection((IStructuredSelection) selection);
        action.addJobListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                CoreUIUtils.getDisplay().asyncExec(new Runnable() {

                    public void run() {
                        refresh();
                    }
                });
                action.removeJobListener(this);
            }
        });
        action.run();
    }

    private void rename() {
        FileSystemRenameAction action = new FileSystemRenameAction(getControl().getShell(),
                fTreeViewer.getTree());
        action.run();
        refresh();
    }

    private void refresh(ISelection selection) {
        if (selection.isEmpty()) {
            // refreshes the root
            refresh();
        } else {
            Object[] elements = ((IStructuredSelection) selection).toArray();
            IResource resource;
            for (Object element : elements) {
                resource = null;
                if (element instanceof IAdaptable) {
                    resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
                }
                if (resource != null) {
                    try {
                        resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                    } catch (CoreException e) {
                    }
                }
                fTreeViewer.refresh(element);
            }
        }
    }

    private void openPropertyPage(ISelection selection) {
        IAdaptable element = (IAdaptable) ((IStructuredSelection) selection).getFirstElement();
        PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(getControl().getShell(),
                element, null, null, null);
        dialog.open();
    }

    private void setComboData(IAdaptable data) {
        fEndPointData.clear();

        if (data instanceof IContainer) {
            // a workspace project/folder
            IContainer container = (IContainer) data;
            IContainer root = (IContainer) fConnectionPoint.getAdapter(IResource.class);

            String path = getRelativePath(root, container);
            if (path != null) {
                String[] segments = (new Path(path)).segments();
                IContainer segmentPath = root;
                for (String segment : segments) {
                    segmentPath = (IContainer) segmentPath.findMember(segment);
                    fEndPointData.add(segmentPath);
                }
            }
        } else {
            // a filesystem or remote path
            IFileStore fileStore = SyncUtils.getFileStore(data);
            if (fileStore != null) {
                IFileStore homeFileStore = SyncUtils.getFileStore(fConnectionPoint);
                while (fileStore.getParent() != null && !fileStore.equals(homeFileStore)) {
                    fEndPointData.add(0, fileStore);
                    fileStore = fileStore.getParent();
                }
            }
        }
        fEndPointData.add(0, fConnectionPoint);
    }

    private void setPath(String path) {
        String separator = "/"; //$NON-NLS-1$
        if (!path.startsWith(separator)) {
            path = separator + path;
        }

        StringBuilder linkPath = new StringBuilder(separator);
        String displayedPath = FileUtils.compressPath(path, 50);
        if (displayedPath.equals(path)) {
            String[] folders = path.split(separator);
            for (int i = 1; i < folders.length; ++i) {
                linkPath.append(MessageFormat.format("<a href=\"{0}\">{1}</a>", i - 1, folders[i])); //$NON-NLS-1$
                linkPath.append(separator);
            }
        } else {
            // deals with the compression
            String[] paths = displayedPath.split("/.../"); //$NON-NLS-1$
            String beginPath = paths[0];
            String[] beginFolders = beginPath.split(separator);
            for (int i = 1; i < beginFolders.length; ++i) {
                linkPath.append(MessageFormat.format(
                        "<a href=\"{0}\">{1}</a>", i - 1, beginFolders[i])); //$NON-NLS-1$
                linkPath.append(separator);
            }
            linkPath.append("...").append(separator); //$NON-NLS-1$
            String endPath = paths[1];
            String[] endFolders = endPath.split(separator);
            int startIndex = path.split(separator).length - 1 - endFolders.length;
            for (int i = 0; i < endFolders.length; ++i) {
                linkPath.append(MessageFormat.format(
                        "<a href=\"{0}\">{1}</a>", startIndex + i, endFolders[i])); //$NON-NLS-1$
                linkPath.append(separator);
            }
        }
        fPathLink.setText(Messages.ConnectionPointComposite_LBL_Path + linkPath.toString());
    }

    private void updateContent(IAdaptable rootElement) {
        setComboData(rootElement);

        if (rootElement instanceof IContainer) {
            setPath(getRelativePath((IContainer) fConnectionPoint.getAdapter(IResource.class),
                    (IContainer) rootElement));
        } else {
            IFileStore fileStore = SyncUtils.getFileStore(rootElement);
            if (fileStore != null) {
                String path = fileStore.toString();
                IFileStore homeFileStore = SyncUtils.getFileStore(fConnectionPoint);
                if (homeFileStore != null) {
                    String homePath = homeFileStore.toString();
                    int index = path.indexOf(homePath);
                    if (index > -1) {
                        path = path.substring(index + homePath.length());
                    }
                }
                setPath(path);
            }
        }
        fTreeViewer.setInput(rootElement);

        updateActionStates();
    }

    private void updateActionStates() {
        // disables the up button when it is at the root
        fUpItem.setEnabled(fEndPointData.size() > 1);
    }

    private void updateMenuStates() {
        ISelection selection = fTreeViewer.getSelection();
        boolean hasSelection = !selection.isEmpty();
        fOpenItem.setEnabled(hasSelection);
        fDeleteItem.setEnabled(hasSelection);
        fRenameItem.setEnabled(hasSelection);
        fPropertiesItem.setEnabled(hasSelection);
    }

    private static IFileStore getFolderStore(IAdaptable destination) {
        IFileStore store = SyncUtils.getFileStore(destination);
        IFileInfo info = SyncUtils.getFileInfo(destination);
        if (store != null && info != null && !info.isDirectory()) {
            store = store.getParent();
        }
        return store;
    }

    /**
     * @param root
     *            the root container
     * @param element
     *            a container under the root
     * @return the relative path string of the element from the root
     */
    private static String getRelativePath(IContainer root, IContainer element) {
        String rootPath = root.getFullPath().toString();
        String elementPath = element.getFullPath().toString();
        int index = elementPath.indexOf(rootPath);
        if (index == -1) {
            return null;
        }
        return elementPath.substring(index + rootPath.length());
    }
}
