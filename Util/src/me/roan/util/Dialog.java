package me.roan.util;

import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Dialog{
	private static JFrame parentFrame = null;
	private static Image icon = null;
	private static String title = "";
	
	public static final void setParentFrame(JFrame parent){
		parentFrame = parent;
	}
	
	public static final void setDialogIcon(Image icon){
		Dialog.icon = icon;
	}
	
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
	public static final boolean showOptionDialog(Object form, boolean resizable){
		return showDialog(form, resizable, new String[]{"Save", "Cancel"});
	}

	/**
	 * Shows the given object to the user.
	 * The dialog will have a 'Save' and
	 * 'Cancel' close option.
	 * @param form The object to display
	 * @return True if the 'Save' option was selected
	 */
	public static final boolean showOptionDialog(Object form){
		return showOptionDialog(form, false);
	}

	/**
	 * Poses the given object as a yes/no option
	 * dialog to the user.
	 * @param msg The object to display
	 * @return True if the user selected yes
	 */
	public static final boolean showConfirmDialog(Object msg){
		return showDialog(msg, false, new String[]{"Yes", "No"});
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
	 * with the given close options
	 * @param form The dialog to display
	 * @param resizable Whether or not the
	 *        dialog can be resized
	 * @param options The close options
	 *        for the dialog
	 * @return True if the used close option
	 *         is the first item in the close
	 *         options list, false otherwise
	 */
	public static final boolean showDialog(Object form, boolean resizable, String[] options){
		JOptionPane optionPane = new JOptionPane(form, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, 0);
		JDialog dialog = optionPane.createDialog(getParentFrame(), title);
		dialog.setResizable(resizable);
		dialog.setIconImage(icon);
		dialog.setVisible(true);
		return options[0].equals(optionPane.getValue());
	}
	
	private static final JFrame getParentFrame(){
		return (parentFrame == null || !parentFrame.isVisible()) ? null : parentFrame;
	}
}
