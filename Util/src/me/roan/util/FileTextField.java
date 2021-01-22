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

public class FileTextField extends JTextField implements DropTargetListener, DocumentListener{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 5787255014444489626L;
	private Consumer<String> listener;

	public FileTextField(){
		this(null);
	}
	
	public FileTextField(Consumer<String> listener){
		this.getDocument().addDocumentListener(this);
		new DropTarget(this, this);
	}
	
	public void setChangeListener(Consumer<String> listener){
		this.listener = listener;
	}
	
	private void update(){
		if(listener != null){
			listener.accept(this.getText());
		}
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
