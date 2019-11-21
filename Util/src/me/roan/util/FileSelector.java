package me.roan.util;

import java.io.File;

import javax.swing.JFileChooser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

@SuppressWarnings("restriction")
public class FileSelector{
	private static final boolean initialised;


	private static final void showFileOpenSwing(){
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		
		System.out.println("Swing: " + chooser.getSelectedFile());
	}

	private static native String showNativeFileOpen();

	public static void main(String[] args){
		if(initialised){
			showNativeFileOpen();
		}else{
			showFileOpenSwing();
		}
	}

	private static class FileData{
		File file;
	}
	
	static{
		
		System.load(new File("Util.dll").getAbsolutePath());
		
		
		initialised = true;
	}
}
