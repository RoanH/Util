package me.roan.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Text field that supports drag and drop events for files.
 * @author Roan
 */
public class FileTextField extends JTextField implements DropTargetListener, DocumentListener{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 5787255014444489626L;
	/**
	 * Consumer that gets notified when the selected folder or file changes.
	 */
	private Consumer<String> listener;

	/**
	 * Constructs a new file text field.
	 */
	public FileTextField(){
		this(null);
	}
	
	/**
	 * Constructs a new file text field with
	 * the given listener.
	 * @param listener The listener to call
	 *        when the field content changes.
	 */
	public FileTextField(Consumer<String> listener){
		this.getDocument().addDocumentListener(this);
		new DropTarget(this, this);
		setListener(listener);
	}
	
	/**
	 * Sets the listener to send selection updates to.
	 * @param listener The listener to send selection
	 *        changes to (can be <code>null</code>).
	 */
	public void setListener(Consumer<String> listener){
		this.listener = listener;
	}
	
	/**
	 * Sends a content update to the
	 * listener if one is set.
	 * @see #setListener(Consumer)
	 */
	private void update(){
		if(listener != null){
			listener.accept(getText());
		}
	}
	
	/**
	 * Gets the content of this text field as a file.
	 * @return The content of this text field as a file.
	 */
	public File getFile(){
		return new File(getText());
	}

	@Override
	public void insertUpdate(DocumentEvent e){
		update();
	}

	@Override
	public void removeUpdate(DocumentEvent e){
		update();
	}

	@Override
	public void changedUpdate(DocumentEvent e){
		update();
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde){
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde){
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde){
	}

	@Override
	public void dragExit(DropTargetEvent dte){
	}

	@Override
	public void drop(DropTargetDropEvent dtde){
		if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && this.isEnabled()){
			try{
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				if(files.size() > 0){
					this.setText(files.get(0).getAbsolutePath());
				}
			}catch(UnsupportedFlavorException | IOException ignore){
				//Pity, but not important
			}
		}
	}
}
