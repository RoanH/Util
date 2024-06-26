/*
 * Util: General utilities for my public projects.
 * Copyright (C) 2019  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/Util
 *
 * Util is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.util;

import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.nio.file.Path;

import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import dev.roanh.util.FileSelector.FileExtension;

/**
 * Utility class for showing often
 * used dialog windows.
 * @author Roan
 */
public final class Dialog{
	/**
	 * The parent frame for dialog windows.
	 */
	private static JFrame parentFrame = null;
	/**
	 * The window icon for dialog windows.
	 */
	private static Image icon = null;
	/**
	 * The window title for dialog windows.
	 */
	private static String title = "";
	
	/**
	 * Prevent instantiation.
	 */
	private Dialog(){
	}
	
	/**
	 * Sets the parent window for the dialog windows.
	 * @param parent The parent window.
	 */
	public static final void setParentFrame(JFrame parent){
		parentFrame = parent;
	}
	
	/**
	 * Sets the window icon for the dialog windows.
	 * @param icon The dialog icon.
	 */
	public static final void setDialogIcon(Image icon){
		Dialog.icon = icon;
	}
	
	/**
	 * Sets the window title for the dialog windows.
	 * @param title The dialog title.
	 */
	public static final void setDialogTitle(String title){
		Dialog.title = title;
	}

	/**
	 * Shows the given object to the user.
	 * The dialog will have a 'Save' and 'Cancel' close option.
	 * @param form The object to display.
	 * @param resizable Whether the user should be able to resize the dialog.
	 * @param modalType The modality type for the dialog. Of primary
	 *        interest are {@link ModalityType#APPLICATION_MODAL}, which
	 *        prevents any interaction with the rest of the application,
	 *        and {@link ModalityType#MODELESS}, which does not block any
	 *        other application components.
	 * @return True if the 'Save' option was selected.
	 */
	public static final boolean showSaveDialog(Object form, boolean resizable, ModalityType modalType){
		return 0 == showDialog(form, resizable, modalType, new String[]{"Save", "Cancel"});
	}

	/**
	 * Shows the given object to the user.
	 * The dialog will have a 'Save' and 'Cancel' close option.
	 * @param form The object to display.
	 * @return True if the 'Save' option was selected.
	 */
	public static final boolean showSaveDialog(Object form){
		return showSaveDialog(form, false, ModalityType.APPLICATION_MODAL);
	}
	
	/**
	 * Shows the given object to the user.
	 * The dialog will have an 'OK' and 'Cancel' close option.
	 * @param form The object to display.
	 * @return True if the 'OK' option was selected.
	 */
	public static final boolean showSelectDialog(Object form){
		return 0 == showDialog(form, false, ModalityType.APPLICATION_MODAL, new String[]{"OK", "Cancel"});
	}

	/**
	 * Poses the given object as a yes/no option dialog to the user.
	 * @param msg The object to display.
	 * @return True if the user selected yes.
	 */
	public static final boolean showConfirmDialog(Object msg){
		return 0 == showDialog(msg, false, ModalityType.APPLICATION_MODAL, new String[]{"Yes", "No"});
	}

	/**
	 * Shows the given object to the user in a dialog.
	 * @param msg The object to display.
	 */
	public static final void showMessageDialog(Object msg){
		showMessageDialog(msg, false, ModalityType.APPLICATION_MODAL);
	}

	/**
	 * Shows the given object to the user in a dialog.
	 * @param msg The object to display.
	 * @param resizable Whether the user should be able to resize the dialog.
	 * @param modalType The modality type for the dialog. Of primary
	 *        interest are {@link ModalityType#APPLICATION_MODAL}, which
	 *        prevents any interaction with the rest of the application,
	 *        and {@link ModalityType#MODELESS}, which does not block any
	 *        other application components.
	 */
	public static final void showMessageDialog(Object msg, boolean resizable, ModalityType modalType){
		showDialog(msg, resizable, modalType, new String[]{"OK"});
	}

	/**
	 * Shows the given error message to the user
	 * @param error The error to display
	 */
	public static final void showErrorDialog(String error){
		showMessageDialog(error);
	}
	
	/**
	 * Shows the given object as a dialog with the given close options.
	 * @param form The dialog to display.
	 * @param options The close options for the dialog.
	 * @return The index of the option that was chosen in the options array
	 *         or -1 if the dialog window was closed.
	 */
	public static final int showDialog(Object form, String[] options){
		return showDialog(form, false, ModalityType.APPLICATION_MODAL, options);
	}
	
	/**
	 * Shows the given object as a dialog with the given close options.
	 * @param form The dialog to display.
	 * @param resizable Whether or not the dialog can be resized.
	 * @param modalType The modality type for the dialog. Of primary
	 *        interest are {@link ModalityType#APPLICATION_MODAL}, which
	 *        prevents any interaction with the rest of the application,
	 *        and {@link ModalityType#MODELESS}, which does not block any
	 *        other application components.
	 * @param options The close options for the dialog.
	 * @return The index of the option that was chosen in the options array
	 *         or -1 if the dialog window was closed.
	 */
	public static final int showDialog(Object form, boolean resizable, ModalityType modalType, String[] options){
		JOptionPane optionPane = new JOptionPane(form, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, 0);
		JDialog dialog = buildDialog(optionPane, resizable, modalType);
		dialog.setVisible(true);
		
		for(int i = 0; i < options.length; i++){
			if(options[i].equals(optionPane.getValue())){
				return i;
			}
		}

		return -1;
	}
	
	/**
	 * Shows a dialog with the given content and immediately returns without waiting
	 * for the user to close the presented dialog. The dialog should be closed manually
	 * by calling {@link JDialog#setVisible(boolean)} on the returned dialog.
	 * @param form The content of the dialog.
	 * @param resizable Whether the user should be able to resize the dialog.
	 * @param modalType The modality type for the dialog. Of primary
	 *        interest are {@link ModalityType#APPLICATION_MODAL}, which
	 *        prevents any interaction with the rest of the application,
	 *        and {@link ModalityType#MODELESS}, which does not block any
	 *        other application components.
	 * @return The dialog being presented to the user.
	 */
	public static final JDialog showDialog(Object form, boolean resizable, ModalityType modalType){
		JOptionPane pane = new JOptionPane(form);
		pane.setOptions(new Object[]{});
		JDialog dialog = buildDialog(pane, resizable, modalType);
		SwingUtilities.invokeLater(()->dialog.setVisible(true));
		return dialog;
	}
	
	/**
	 * Builds a new dialog with the given content pane and settings.
	 * @param pane The content for the dialog.
	 * @param resizable Whether the user should be able to resize the dialog.
	 * @param modalType The modality type for the dialog. Of primary
	 *        interest are {@link ModalityType#APPLICATION_MODAL}, which
	 *        prevents any interaction with the rest of the application,
	 *        and {@link ModalityType#MODELESS}, which does not block any
	 *        other application components.
	 * @return The constructed dialog.
	 */
	private static final JDialog buildDialog(JOptionPane pane, boolean resizable, ModalityType modalType){
		pane.setActionMap(new ActionMap());
		JDialog dialog = pane.createDialog(getParentFrame(), title);
		dialog.setResizable(resizable);
		dialog.setIconImage(icon);
		dialog.setModalityType(modalType);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		return dialog;
	}
	
	/**
	 * Opens a file open dialog.
	 * @param filters The file extension filters to use. If none
	 *        are provided then any extension is allowed.
	 * @return The file that was selected,
	 *         this file will exist on the 
	 *         file system. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 * @see FileSelector#registerFileExtension(String, String...)
	 */
	public static final Path showFileOpenDialog(FileExtension... filters){
		return FileSelector.showFileOpenDialog(filters);
	}
	
	/**
	 * Opens a folder open dialog.
	 * @return The folder that was selected,
	 *         this folder may or may not actually
	 *         exist on the file system. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 */
	public static final Path showFolderOpenDialog(){
		return FileSelector.showFolderOpenDialog();
	}
	
	/**
	 * Opens a file save dialog.
	 * @param name The initial name shown to save the file as.
	 * @return The file that was selected,
	 *         this file may or may not actually
	 *         exist on the file system. If the
	 *         file exists the user has already
	 *         agreed to overwrite it. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 */
	public static final Path showFileSaveDialog(String name){
		return showFileSaveDialog(null, name);
	}
	
	/**
	 * Opens a file save dialog.
	 * @param extension The file extension to enforce.
	 * @param name The initial name shown to save the file as.
	 * @return The file that was selected,
	 *         this file may or may not actually
	 *         exist on the file system. If the
	 *         file exists the user has already
	 *         agreed to overwrite it. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 * @see #showFileSaveDialog(String)
	 * @see FileSelector#registerFileExtension(String, String...)
	 */
	public static final Path showFileSaveDialog(FileExtension extension, String name){
		return FileSelector.showFileSaveDialog(extension, name);
	}
	
	/**
	 * Gets the parent frame for the dialog windows.
	 * @return The parent frame for the dialog windows.
	 * @see #setParentFrame(JFrame)
	 */
	public static final JFrame getParentFrame(){
		return (parentFrame == null || !parentFrame.isVisible()) ? null : parentFrame;
	}
	
	/**
	 * Gets the icon for the dialog windows.
	 * @return The icon for the dialog windows.
	 * @see #setDialogIcon(Image)
	 */
	public static Image getDialogIcon(){
		return icon;
	}
	
	/**
	 * Gets the title for the dialog windows.
	 * @return The title for the dialog windows.
	 * @see #setDialogTitle(String)
	 */
	public static String getDialogTitle(){
		return title;
	}
}
