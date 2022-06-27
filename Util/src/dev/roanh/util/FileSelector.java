/*
 * Util: General utilities for my public projects.
 * Copyright (C) 2019  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/Util
 *
 * Util is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * File chooser implementation that when
 * possible shows the new style Windows
 * file selector and otherwise falls back
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
	private static JFileChooser chooser = null;
	
	/**
	 * Opens a file open dialog.
	 * @param extensions The file extension filters to use. If none
	 *        are provided then any extension is allowed.
	 * @return The file that was selected,
	 *         this file will exist on the 
	 *         file system. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 * @see #registerFileExtension(String, String...)
	 */
	public static final Path showFileOpenDialog(FileExtension... extensions){
		Objects.requireNonNull(extensions, "The extensions array cannot be null.");
		if(initialised){
			long filters = 0;
			for(FileExtension extension : extensions){
				Objects.requireNonNull(extension, "File extensions cannot be null");
				filters |= extension.nativeID;
			}
			return toPath(showNativeFileOpen(filters, extensions.length));
		}else{
			if(chooser == null){
				chooser = new JFileChooser();
			}
			
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.resetChoosableFileFilters();
			for(FileExtension ext : extensions){
				chooser.addChoosableFileFilter(ext.filter);
			}
			chooser.setAcceptAllFileFilterUsed(extensions.length == 0);
			
			while(chooser.showOpenDialog(Dialog.getParentFrame()) == JFileChooser.APPROVE_OPTION){
				Path selected = chooser.getSelectedFile().toPath();
				if(Files.exists(selected)){
					return selected;
				}else{
					Dialog.showMessageDialog("The given file does not exist.\nCheck the path and try again.");
				}
			}
			return null;
		}
	}
	
	/**
	 * Opens a folder open dialog.
	 * @return The folder that was selected,
	 *         this folder will exist on the 
	 *         file system. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 */
	public static final Path showFolderOpenDialog(){
		if(initialised){
			return toPath(showNativeFolderOpen());
		}else{
			if(chooser == null){
				chooser = new JFileChooser();
			}
			
			chooser.resetChoosableFileFilters();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			while(chooser.showOpenDialog(Dialog.getParentFrame()) == JFileChooser.APPROVE_OPTION){
				Path selected = chooser.getSelectedFile().toPath();
				if(Files.exists(selected)){
					return selected;
				}else{
					Dialog.showMessageDialog("The given folder does not exist.\nCheck the path and try again.");
				}
			}
			return null;
		}
	}
	
	/**
	 * Opens a file save dialog.
	 * @param filter The file extension to enforce.
	 * @param name The initial name shown to save the file as.
	 * @return The file that was selected,
	 *         this file may or may not actually
	 *         exist on the file system. If the
	 *         file exists the user has already
	 *         agreed to overwrite it. If the
	 *         operation was cancelled 
	 *         <code>null</code> is returned.
	 * @throws NullPointerException When the provided
	 *         default name is <code>null</code>.
	 * @see #registerFileExtension(String, String...)
	 */
	public static final Path showFileSaveDialog(FileExtension filter, String name){
		Objects.requireNonNull(name, "Provided default name cannot be null.");
		if(initialised){
			return toPath(showNativeFileSave(filter != null ? filter.nativeID : 0, name));
		}else{
			if(chooser == null){
				chooser = new JFileChooser();
			}
			
			String extension;
			if(filter == null){
				chooser.setSelectedFile(new File(name));
				chooser.setAcceptAllFileFilterUsed(true);
				chooser.resetChoosableFileFilters();
				extension = "";
			}else{
				extension = "." + filter.filter.getExtensions()[0].toLowerCase(Locale.ROOT);
				chooser.setSelectedFile(new File(name + extension));
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(filter.filter);
			}
			
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			while(chooser.showSaveDialog(Dialog.getParentFrame()) == JFileChooser.APPROVE_OPTION){
				Path file = chooser.getSelectedFile().toPath();
				if(!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(extension)){
					file = file.resolveSibling(file.getFileName().toString() + extension);
				}
				
				if(Files.exists(file) && !Dialog.showConfirmDialog(file.getFileName().toString() + " already exists.\nDo you want to replace it?")){
					continue;
				}

				return file;
			}
			return null;
		}
	}
	
	/**
	 * Converts the given file path to a
	 * Java path instance.
	 * @param path The path to parse.
	 * @return A file instance for the given
	 *         path or <code>null</code> if
	 *         the given path was <code>null</code>.
	 *         The path is not validated in any way.
	 * @see Path
	 */
	private static Path toPath(String path){
		return path == null ? null : Paths.get(path);
	}
	
	/**
	 * Registers a new file extension for use as
	 * extension filter in the file selector. Note that
	 * only a maximum of 64 native file extensions can be registered.
	 * @param description The description of the extension.
	 * @param extensions The extensions matched by the extension
	 *        filter. The extensions should not include the period
	 *        before their name.
	 * @return The newly registered file extension.
	 * @throws NullPointerException When the given description is <code>null</code>.
	 * @throws IllegalArgumentException When one of the extensions is invalid.
	 * @throws TooManyExtensionsException When an attempt is made to register more
	 *         than 64 native file extensions.
	 * @throws IllegalStateException When registering the native file extension failed.
	 * @see FileExtension
	 */
	public static FileExtension registerFileExtension(String description, String... extensions) throws NullPointerException, IllegalArgumentException, TooManyExtensionsException, IllegalStateException{
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
			}else if(ext.nativeID == -2){
				throw new TooManyExtensionsException();
			}
		}else{
			ext.filter = new FileNameExtensionFilter(description, extensions);
		}
		return ext;
	}
	
	/**
	 * Opens the native file open dialog.
	 * @param types The bitwise combination of the IDs of all the
	 *        registered file extension filters that should be enabled.
	 * @param typec The number of bits set in <code>types</code>.
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
	 * @param type The ID of the file extension filter to enable, if
	 *        0 then any file extension will be allowed for saving.
	 * @param name The initial suggested save file name.
	 * @return The file that was selected or
	 *         <code>null</code> if no file was selected.
	 */
	private static synchronized native String showNativeFileSave(long type, String name);
	
	/**
	 * Registers a new native extension.
	 * @param desc The description for the extension.
	 * @param filter The filter string to use for the extension.
	 * @param extension The default extension to use for files
	 *        used under the extension.
	 * @return The ID of the newly registered extension.
	 */
	private static synchronized native long registerNativeFileExtension(String desc, String filter, String extension);
	
	static{
		initialised = false;

		if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")){
			String arch = System.getProperty("os.arch");
			if(arch.equals("amd64") || arch.equals("x86")){
				try(InputStream in = ClassLoader.getSystemResourceAsStream("dev/roanh/util/lib/" + arch + "/Util.dll")){
					if(in != null){
						Path tmp = null;
						if(Util.VERSION != null){
							tmp = Paths.get(System.getProperty("java.io.tmpdir")).resolve("RoanH-Util-v" + Util.VERSION + "-" + arch + ".dll");
						}

						if(tmp == null || Files.notExists(tmp)){
							tmp = Files.createTempFile("Util", ".dll");
							tmp.toFile().deleteOnExit();
							
							try(OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.CREATE)){
								byte[] buffer = new byte[1024];
								int len;
								while((len = in.read(buffer)) != -1){
									out.write(buffer, 0, len);
								}
								
								out.flush();
							}
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
	}
	
	/**
	 * Registration of a file extension that can be used
	 * to restrict the type of a saved file and restrict
	 * which files can be opened.
	 * @author Roan
	 * @see FileSelector#registerFileExtension(String, String...)
	 */
	public static final class FileExtension{
		/**
		 * The native ID of the registered extension.
		 */
		private long nativeID;
		/**
		 * The Swing extension filter for this extension.
		 */
		private FileNameExtensionFilter filter;
		
		/**
		 * Constructs a new FileExtension.
		 */
		private FileExtension(){
		}
	}
	
	/**
	 * Exception that is thrown when an attempt is made
	 * to register more than the maximum of 64 native
	 * file extensions.
	 * @author Roan
	 * @see FileSelector#registerFileExtension(String, String...)
	 */
	public static final class TooManyExtensionsException extends RuntimeException{
		/**
		 * Serial ID.
		 */
		private static final long serialVersionUID = 5228035256972704523L;

		/**
		 * Constructs a new too many extensions exception.
		 */
		private TooManyExtensionsException(){
			super("Can only register a maximum of 64 native file extensions.");
		}
	}
}
