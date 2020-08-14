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
		Objects.requireNonNull(extensions, "The extensions array cannot be null.");
		if(initialised){
			long filters = 0;
			for(FileExtension extension : extensions){
				Objects.requireNonNull(extension, "File extensions cannot be null");
				filters |= extension.nativeID;
			}
			return toFile(showNativeFileOpen(filters, extensions.length));
		}else{
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.resetChoosableFileFilters();
			for(FileExtension ext : extensions){
				chooser.addChoosableFileFilter(ext.filter);
			}
			chooser.setAcceptAllFileFilterUsed(extensions.length == 0);
			
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
			chooser.resetChoosableFileFilters();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			while(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				File selected = chooser.getSelectedFile();
				if(selected.exists()){
					return selected;
				}else{
					Dialog.showMessageDialog("The given folder does not exist.\nCheck the path and try again.");
				}
			}
			return null;
//			if(){
//				return chooser.getSelectedFile();
//			}else{
//				return null;
//			}
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
			//TODO handle filter and name
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
		//long png = registerNativeFileExtension("Images", "*.png;*.jpg", "png");
		//long ini = registerNativeFileExtension("Ini", "*.ini", "ini");
		//registerNativeFileExtension("Png", "*.png", "png");
		//String out = showNativeFileOpen(1, png);
		//File out = showFileSaveDialog();
		//System.out.println("Returned:" + out);
		
		
		

		chooser = new JFileChooser();
		initialised = false;
		
		showFolderOpenDialog();
		
		FileExtension e01 = registerFileExtension("Ext 1", "png", "jpg");
		FileExtension e02 = registerFileExtension("Ext 2", "png");
		FileExtension e03 = registerFileExtension("Ext 3", "png");
		FileExtension e04 = registerFileExtension("Ext 4", "png");
		FileExtension e05 = registerFileExtension("Ext 5", "png");
		FileExtension e06 = registerFileExtension("Ext 6", "png");
		FileExtension e07 = registerFileExtension("Ext 7", "png");
		FileExtension e08 = registerFileExtension("Ext 8", "png");
		FileExtension e09 = registerFileExtension("Ext 9", "png");
		FileExtension e10 = registerFileExtension("Ext 10", "png");
		FileExtension e11 = registerFileExtension("Ext 11", "png");
		FileExtension e12 = registerFileExtension("Ext 12", "png");
		FileExtension e13 = registerFileExtension("Ext 13", "png");
		FileExtension e14 = registerFileExtension("Ext 14", "png");
		FileExtension e15 = registerFileExtension("Ext 15", "png");
		FileExtension e16 = registerFileExtension("Ext 16", "png");
		FileExtension e17 = registerFileExtension("Ext 17", "png");
		FileExtension e18 = registerFileExtension("Ext 18", "png");
		FileExtension e19 = registerFileExtension("Ext 19", "png");
		FileExtension e20 = registerFileExtension("Ext 20", "png");
		FileExtension e21 = registerFileExtension("Ext 21", "png");
		FileExtension e22 = registerFileExtension("Ext 22", "png");
		FileExtension e23 = registerFileExtension("Ext 23", "png");
		FileExtension e24 = registerFileExtension("Ext 24", "png");
		FileExtension e25 = registerFileExtension("Ext 25", "png");
		FileExtension e26 = registerFileExtension("Ext 26", "png");
		FileExtension e27 = registerFileExtension("Ext 27", "png");
		FileExtension e28 = registerFileExtension("Ext 28", "png");
		FileExtension e29 = registerFileExtension("Ext 29", "png");
		FileExtension e30 = registerFileExtension("Ext 30", "png");
		FileExtension e31 = registerFileExtension("Ext 31", "png");
		FileExtension e32 = registerFileExtension("Ext 32", "png");
		FileExtension e33 = registerFileExtension("Ext 33", "png");
		FileExtension e34 = registerFileExtension("Ext 34", "png");
		FileExtension e35 = registerFileExtension("Ext 35", "png");
		FileExtension e36 = registerFileExtension("Ext 36", "png");
		FileExtension e37 = registerFileExtension("Ext 37", "png");
		FileExtension e38 = registerFileExtension("Ext 38", "png");
		FileExtension e39 = registerFileExtension("Ext 39", "png");
		FileExtension e40 = registerFileExtension("Ext 40", "png");
		FileExtension e41 = registerFileExtension("Ext 41", "png");
		FileExtension e42 = registerFileExtension("Ext 42", "png");
		FileExtension e43 = registerFileExtension("Ext 43", "png");
		FileExtension e44 = registerFileExtension("Ext 44", "png");
		FileExtension e45 = registerFileExtension("Ext 45", "png");
		FileExtension e46 = registerFileExtension("Ext 46", "png");
		FileExtension e47 = registerFileExtension("Ext 47", "png");
		FileExtension e48 = registerFileExtension("Ext 48", "png");
		FileExtension e49 = registerFileExtension("Ext 49", "png");
		FileExtension e50 = registerFileExtension("Ext 50", "png");
		FileExtension e51 = registerFileExtension("Ext 51", "png");
		FileExtension e52 = registerFileExtension("Ext 52", "png");
		FileExtension e53 = registerFileExtension("Ext 53", "png");
		FileExtension e54 = registerFileExtension("Ext 54", "png");
		FileExtension e55 = registerFileExtension("Ext 55", "png");
		FileExtension e56 = registerFileExtension("Ext 56", "png");
		FileExtension e57 = registerFileExtension("Ext 57", "png");
		FileExtension e58 = registerFileExtension("Ext 58", "png");
		FileExtension e59 = registerFileExtension("Ext 59", "png");
		FileExtension e60 = registerFileExtension("Ext 60", "png");
		FileExtension e61 = registerFileExtension("Ext 61", "png");
		FileExtension e62 = registerFileExtension("Ext 62", "png");
		FileExtension e63 = registerFileExtension("Ext 63", "png");
		FileExtension e64 = registerFileExtension("Ext 64!", "png");
		FileExtension e65 = registerFileExtension("Ext 65!", "jpg");
		FileExtension e66 = registerFileExtension("Ext 66!", "jpg");


		File f = showFileOpenDialog(
			e01,
			e02,
			e03,
			e04,
			e05,
			e06,
			e07,
			e08,
			e09,
			e10,
			e11,
			e12,
			e13,
			e14,
			e15,
			e16,
			e17,
			e18,
			e19,
			e20,
			e21,
			e22,
			e23,
			e24,
			e25,
			e26,
			e27,
			e28,
			e29,
			e30,
			e31,
			e32,
			e33,
			e34,
			e35,
			e36,
			e37,
			e38,
			e39,
			e40,
			e41,
			e42,
			e43,
			e44,
			e45,
			e46,
			e47,
			e48,
			e49,
			e50,
			e51,
			e52,
			e53,
			e54,
			e55,
			e56,
			e57,
			e58,
			e59,
			e60,
			e61,
			e62,
			e63,
			e64,
			e65,
			e66
		);
		
		
		
		
		
		System.out.println(f);
		Thread.sleep(10000);
	}
	
	public static final class FileExtension{
		private long nativeID;
		private FileNameExtensionFilter filter;
		
		private FileExtension(){
		}
	}
}
