package me.roan.util;

import java.io.File;

import javax.swing.JFileChooser;

public class FileSelector{
	private static boolean initialised;


	private static final void showFileOpenSwing(){
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		
		System.out.println("Swing: " + chooser.getSelectedFile());
	}

	private static native String showNativeFileOpen();
	
	private static native String showNativeFolderOpen();
	
	private static native String showNativeFileSave();

	public static void main(String[] args){
		if(initialised){
			showNativeFileOpen();
		}else{
			showFileOpenSwing();
		}
	}
	
	static{
		try{
			//TODO load from jar
			System.load(new File("Util.dll").getAbsolutePath());
			initialised = true;
		}catch(UnsatisfiedLinkError e){
			e.printStackTrace();//TODO remove
			initialised = false;
		}
	}
}
