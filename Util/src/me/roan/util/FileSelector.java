package me.roan.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

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
		
		System.out.println(System.getProperty("os.arch"));
		System.out.println(System.getProperty("os.name"));

	}
	
	static{
		initialised = false;

		if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")){
			String arch = System.getProperty("os.arch");
			if(arch.equals("amd64") || arch.equals("x86")){
				try(InputStream in = ClassLoader.getSystemResourceAsStream("me/roan/util/lib/" + arch + "/Util.dll")){
					if(in != null){
						Path tmp = Files.createTempFile("Util", ".dll");
						tmp.toFile().deleteOnExit();
						
						try(OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.CREATE)){
							byte[] buffer = new byte[1024];
							int len;
							while((len = in.read(buffer)) != -1){
								out.write(buffer, 0, len);
							}
							
							out.flush();
						}
						
						try{
							System.load(tmp.toAbsolutePath().toString());
							initialised = true;
						}catch(UnsatisfiedLinkError ignore){
							ignore.printStackTrace();//TODO remove
						}
					}
				}catch(IOException ignore){
					ignore.printStackTrace();//TODO remove
				}				
			}
		}
		
		if(!initialised){
			chooser = new JFileChooser();
		}
	}
}
