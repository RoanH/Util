package me.roan.util;

import java.io.File;

import javax.swing.JFileChooser;

public class FileSelector{
	private static boolean initialised;
	private static JFileChooser chooser;
	
	public static final File showFileOpenDialog(){
		if(initialised){
			return toFile(showNativeFileOpen());
		}else{
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				return chooser.getSelectedFile();
			}else{
				return null;
			}
		}
	}
	
	public static final File showFolderOpenDialog(){
		if(initialised){
			return toFile(showNativeFolderOpen());
		}else{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				return chooser.getSelectedFile();
			}else{
				return null;
			}
		}
	}
	
	public static final File showFileSaveDialog(){
		if(initialised){
			return toFile(showNativeFileSave());
		}else{
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				return chooser.getSelectedFile();
			}else{
				return null;
			}
		}
	}
	
	private static File toFile(String path){
		return path == null ? null : new File(path);
	}

	private static native String showNativeFileOpen();
	
	private static native String showNativeFolderOpen();
	
	private static native String showNativeFileSave();

	public static void main(String[] args){
		System.out.println("fopen (file): " + showFileOpenDialog());
		System.out.println("fopen (folder): " + showFolderOpenDialog());
		System.out.println("fsave: " + showFileSaveDialog());
	}
	
	static{
		try{
			//TODO load from jar
			System.load(new File("Util.dll").getAbsolutePath());
			initialised = true;
		}catch(UnsatisfiedLinkError e){
			e.printStackTrace();//TODO remove
			initialised = false;
			chooser = new JFileChooser();
		}
	}
}
