package me.roan.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	public static final File showFileOpenDialog(FileExtension... extensions){
		if(initialised){
			
			return toFile(showNativeFileOpen(0, 0));//XXX
		}else{
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.resetChoosableFileFilters();
			for(FileExtension ext : extensions){
				chooser.addChoosableFileFilter(ext.filter);
			}
			
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
	public static final File showFileSaveDialog(FileExtension filter, String name){
		if(initialised){
			return toFile(showNativeFileSave(filter.nativeID, name));
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
	
	public static FileExtension registerFileExtension(String description, String... extensions){
		if(extensions == null || extensions.length == 0){
			throw new IllegalArgumentException("Extensions array cannot be empty or null.");
		}
		Objects.requireNonNull(description, "Description may not be null.");
		
		FileExtension ext = new FileExtension();
		if(initialised){
			StringJoiner joiner = new StringJoiner(";*.", "*.", "");
			for(String extension : extensions){
				if(extension == null || extension.trim().isEmpty()){
					throw new IllegalArgumentException("Each extension must be non-null and non-empty.");
				}
				joiner.add(extension);
			}
			
			ext.nativeID = registerNativeFileExtension(description, joiner.toString(), extensions[0]);
			if(ext.nativeID == -1){
				throw new IllegalStateException("Failed to register native extension.");
			}
		}else{
			ext.filter = new FileNameExtensionFilter(description, extensions);
		}
		return ext;
	}
	
	/**
	 * Opens the native file open dialog.
	 * @return The file that was selected or
	 *         <code>null</code> if no file was selected.
	 */
	private static synchronized native String showNativeFileOpen(long types, int typec);
	
	/**
	 * Opens the native folder open dialog.
	 * @return The folder that was selected or
	 *         <code>null</code> if no folder was selected.
	 */
	private static synchronized native String showNativeFolderOpen();
	
	/**
	 * Opens the native file save dialog.
	 * @return The file that was selected or
	 *         <code>null</code> if no file was selected.
	 */
	private static synchronized native String showNativeFileSave(long type, String name);
	
	private static synchronized native long registerNativeFileExtension(String desc, String filter, String extension);
	
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
	
	public static void main(String[] args) throws InterruptedException{
		long png = registerNativeFileExtension("Images", "*.png;*.jpg", "png");
		long ini = registerNativeFileExtension("Ini", "*.ini", "ini");
		//registerNativeFileExtension("Png", "*.png", "png");
		//String out = showNativeFileOpen(1, png);
		//File out = showFileSaveDialog();
		//System.out.println("Returned:" + out);
		Thread.sleep(10000);
	}
	
	public static final class FileExtension{
		private long nativeID;
		private FileNameExtensionFilter filter;
		
		private FileExtension(){
		}
	}
}
