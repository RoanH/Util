package dev.roanh.util;

import java.awt.Image;
import java.io.File;
import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dev.roanh.util.FileSelector.FileExtension;

/**
 * Utility class for showing often
 * used dialog windows.
 * @author Roan
 */
public class Dialog{
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
	 * The dialog will have a 'Save' and
	 * 'Cancel' close option.
	 * @param form The object to display
	 * @param resizable Whether the user should
	 *        be able to resize the dialog
	 * @return True if the 'Save' option was selected
	 */
	public static final boolean showSaveDialog(Object form, boolean resizable){
		return 0 == showDialog(form, resizable, new String[]{"Save", "Cancel"});
	}

	/**
	 * Shows the given object to the user.
	 * The dialog will have a 'Save' and
	 * 'Cancel' close option.
	 * @param form The object to display
	 * @return True if the 'Save' option was selected
	 */
	public static final boolean showSaveDialog(Object form){
		return showSaveDialog(form, false);
	}
	
	/**
	 * Shows the given object to the user.
	 * The dialog will have a 'OK' and
	 * 'Cancel' close option.
	 * @param form The object to display
	 * @return True if the 'OK' option was selected
	 */
	public static final boolean showSelectDialog(Object form){
		return 0 == showDialog(form, false, new String[]{"OK", "Cancel"});
	}

	/**
	 * Poses the given object as a yes/no option
	 * dialog to the user.
	 * @param msg The object to display
	 * @return True if the user selected yes
	 */
	public static final boolean showConfirmDialog(Object msg){
		return 0 == showDialog(msg, false, new String[]{"Yes", "No"});
	}

	/**
	 * Shows the given object to the user in a dialog
	 * @param msg The object to display
	 */
	public static final void showMessageDialog(Object msg){
		showMessageDialog(msg, false);
	}

	/**
	 * Shows the given object to the user in a dialog
	 * @param msg The object to display
	 * @param resizable Whether the user should
	 *        be able to resize the dialog
	 */
	public static final void showMessageDialog(Object msg, boolean resizable){
		showDialog(msg, resizable, new String[]{"OK"});
	}

	/**
	 * Shows the given error message to the user
	 * @param error The error to display
	 */
	public static final void showErrorDialog(String error){
		showMessageDialog(error);
	}
	
	/**
	 * Shows the given object as a dialog
	 * with the given close options.
	 * @param form The dialog to display
	 * @param options The close options
	 *        for the dialog.
	 * @return The index of the option that
	 *         was chosen in the options array.
	 */
	public static final int showDialog(Object form, String[] options){
		return showDialog(form, false, options);
	}
	
	/**
	 * Shows the given object as a dialog
	 * with the given close options.
	 * @param form The dialog to display
	 * @param resizable Whether or not the
	 *        dialog can be resized.
	 * @param options The close options
	 *        for the dialog.
	 * @return The index of the option that
	 *         was chosen in the options array.
	 */
	public static final int showDialog(Object form, boolean resizable, String[] options){
		JOptionPane optionPane = new JOptionPane(form, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, 0);
		optionPane.setActionMap(new ActionMap());
		
		JDialog dialog = optionPane.createDialog(getParentFrame(), title);
		dialog.setResizable(resizable);
		dialog.setIconImage(icon);
		dialog.setVisible(true);
		
		for(int i = 0; i < options.length; i++){
			if(options[i].equals(optionPane.getValue())){
				return i;
			}
		}
		return -1;
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
	public static final File showFileOpenDialog(FileExtension... filters){
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
	public static final File showFolderOpenDialog(){
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
	 * @see Dialog#showFileSaveDialog(FileExtension, String)
	 * @see FileSelector#registerFileExtension(String, String...)
	 */
	public static final File showFileSaveDialog(String name){
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
	 * @see Dialog#showFileSaveDialog(String)
	 * @see FileSelector#registerFileExtension(String, String...)
	 */
	public static final File showFileSaveDialog(FileExtension extension, String name){
		return FileSelector.showFileSaveDialog(extension, name);
	}
	
	/**
	 * Gets the parent frame for the dialog windows.
	 * @return The parent frame for the dialog windows.
	 */
	private static final JFrame getParentFrame(){
		return (parentFrame == null || !parentFrame.isVisible()) ? null : parentFrame;
	}
}
