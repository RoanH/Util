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

/**
 * File chooser implementation that when
 * possible shows the new style Windows
 * file selector and other falls back
 * to the Swing file chooser.
 * @author Roan
 * @see JFileChooser
 */
public class FileSelector{
	/**
	 * True if the native library
	 * is loaded.
	 */
	private static boolean initialised;
	/**
	 * File chooser instance if no
	 * native library is loaded.
	 */
	private static JFileChooser chooser;
	
	/**
	 * Opens a file open dialog.
	 * @return The file that was selected,
	 *         this file may or may not actually
	 *         exist on the file system. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 */
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
	
	/**
	 * Opens a folder open dialog.
	 * @return The folder that was selected,
	 *         this folder may or may not actually
	 *         exist on the file system. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 */
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
	
	/**
	 * Opens a file save dialog.
	 * @return The file that was selected,
	 *         this file may or may not actually
	 *         exist on the file system. If the
	 *         file exists the user has already
	 *         agreed to overwrite it. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 */
	public static final File showFileSaveDialog(){
		if(initialised){
			return toFile(showNativeFileSave());
		}else{
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			while(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				File file = chooser.getSelectedFile();
				if(file.exists() && !Dialog.showConfirmDialog(file.getName() + " already exists.\nDo you want to replace it?")){
					continue;
				}

				return file;
			}
			return null;
		}
	}
	
	/**
	 * Converts the given file path to a
	 * Java file instance.
	 * @param path The part to parse.
	 * @return A file instance for the given
	 *         path or <code>null</code> if
	 *         the given path was <code>null</code>.
	 *         The path is not validated in any way.
	 * @see File
	 */
	private static File toFile(String path){
		return path == null ? null : new File(path);
	}

	/**
	 * Opens the native file open dialog.
	 * @return The file that was selected or
	 *         <code>null</code> if no file was selected.
	 */
	private static native String showNativeFileOpen();
	
	/**
	 * Opens the native folder open dialog.
	 * @return The folder that was selected or
	 *         <code>null</code> if no folder was selected.
	 */
	private static native String showNativeFolderOpen();
	
	/**
	 * Opens the native file save dialog.
	 * @return The file that was selected or
	 *         <code>null</code> if no file was selected.
	 */
	private static native String showNativeFileSave();
	
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
						}
					}
				}catch(IOException ignore){
				}				
			}
		}
		
		if(!initialised){
			chooser = new JFileChooser();
		}
	}
}
