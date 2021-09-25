package me.roan.util;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;

/**
 * Currently as of writing this class there is a bug in the JDK where
 * a program cannot correctly read resources from its JAR if it is
 * located on a path where the name of one of the directories ends with
 * an exclamation mark <code>(!)</code>. This bug has been reported a number
 * of times with the first report I could find being from the 9th of 
 * August, 2000 (see links below). 
 * <p>
 * The cause of this issue is with the {@linkplain java.net.JarURLConnection}
 * class. However, since this is a <code>java.*</code> it cannot be overridden
 * at run time using for example a custom class loader. In addition to this
 * distributing a fixed copy of the class would be breaking the license agreement.
 * <p>
 * Other possible fixes like using a custom system class loader do not work
 * for similar reasons since certain resources get loaded using the bootstrap
 * class loader so that one fails...
 * <p>
 * If there ever comes a time when I can delete this work around class I'll
 * be happy, but it's been a bug for almost 20 years as of writing this...
 * @author Roan
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-4361044">JDK Bug 4361044</a>
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-4523159">JDK Bug 4523159</a>
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-4730642">JDK Bug 4730642</a>
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-6249364">JDK Bug 6249364</a>
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-6390779">JDK Bug 6390779</a>
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-7188320">JDK Bug 7188320</a>
 * @see java.net.JarURLConnection
 */
public class ExclamationMarkPath{
	/**
	 * Current location of the program executable.
	 * <p>
	 * Set by {@link #check()}
	 */
	private static File exe;
	
	/**
	 * Checks if the current location of the program is
	 * affected by the bug. If the current location is
	 * not affected this subroutine just returns without
	 * doing anything.
	 * @param args Command line arguments that the program
	 *        was started with. If the last argument is
	 *        equal to <code>-relaunch</code> the warning
	 *        dialog is skipped and the program is immediately
	 *        relaunched from the temporary directory.
	 */
	public static void check(String... args){
		check(false, args);
	}
	
	/**
	 * Checks if the current location of the program is
	 * affected by the bug. If the current location is
	 * not affected this subroutine just returns without
	 * doing anything.
	 * @param suppressWarning Suppresses the normal warning
	 *        dialog and instead proceeds as if the user
	 *        chose to relaunch the program from the
	 *        temporary directory.
	 * @param args Command line arguments that the program
	 *        was started with. If the last argument is
	 *        equal to <code>-relaunch</code> the warning
	 *        dialog is skipped and the program is immediately
	 *        relaunched from the temporary directory.
	 */
	public static void check(boolean suppressWarning, String... args){
		if(check()){
			if(suppressWarning || (args != null && args.length != 0 && args[args.length - 1].equalsIgnoreCase("-relaunch"))){
				relaunchFromTemp(args);
			}else{
				showWarning(args);
			}
			System.exit(0);
		}
	}
	
	/**
	 * Shows a warning dialog to the user about the
	 * situation with the option to exit or try
	 * to relaunch from the temp directory.
	 * @param args The command line arguments that were passed.
	 */
	private static final void showWarning(String... args){
		int option = Dialog.showDialog(
			"It seems that the current location for the program has a directory whose name ends with\n"
			+ "an exclamation mark (!) on its path. Unfortunately due to a JDK Bug (one reported in August 2000)\n"
			+ "this will make the program not work correctly.\n\n"
			+ "The easiest way to solve this issue is to simply move the program\n"
			+ "or rename the directory whose name ends with an exclamation mark.\n\n"
			+ "If for some reason neither is an option the program can try to start itself from the temp directory\n"
			+ "by clicking the button on this dialog.\n\n"
			+ "Relevant JDK issue numbers are: 4361044, 4523159, 4730642, 6249364, 6390779 and 7188320.\n\n"
			+ "Unfortunately this is not an issue I can fix, providing this information instead\n"
			+ "of chrashing is the best I can do for the time being. The main reason for this is\n"
			+ "because the bug is located in a part of Java I cannot modify and which cannot\n"
			+ "be loaded or defined using custom class loaders.",
			new String[]{
				"Exit",
				"Start from temp directory (might not work)"
			}
		);
		
		if(option == 1){
			relaunchFromTemp(args);
		}
	}

	/**
	 * Checks if the current executable location
	 * is affected by the JDK bug.
	 * @return True if the current location is affected.
	 */
	private static final boolean check(){
		CodeSource source = ExclamationMarkPath.class.getProtectionDomain().getCodeSource();
		if(source == null){
			//Chances of there being no problem are higher
			return false;
		}
		
		//First get the exe location
		URL url = source.getLocation();
		try{
			exe = new File(url.toURI());
		}catch(URISyntaxException e){
			exe = new File(url.getPath());
		}
		
		//If there is an exclamation mark there is a problem
		return verifyPath(exe);
	}

	/**
	 * Re-launches the program from the temp directory
	 * if the program path contains a <code>(!)</code> this
	 * fixes the JDK bug. If this subroutine managed to
	 * execute properly the call to this subroutine will
	 * never return.
	 * @param args The original command line arguments
	 *        <code>null</code> allowed for none.
	 */
	private static final void relaunchFromTemp(String... args){
		if(!exe.exists()){
			//All hope lost
			return;
		}
		
		//Assume java isn't on the path
		File java = new File(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe");
		String jvm;
		if(java.exists()){
			jvm = java.getAbsolutePath();
		}else{
			//Hope java is on the path
			jvm = "java";
		}
		
		//Attempt to copy the program to the temp directory
		File tmp = null;
		try{
			tmp = Files.createTempFile(null, null).toFile();
			tmp.deleteOnExit();

			//Infinite loops are no fun
			if(verifyPath(tmp)){
				return;
			}
			
			Files.copy(exe.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		
		//Build the new process
		ProcessBuilder builder = new ProcessBuilder();
		if(args != null && args.length != 0){
			if(args.length == 1){
				builder.command(jvm, "-jar", tmp.getAbsolutePath(), args[0]);
			}else{
				String[] cmd = new String[args.length + 3];
				cmd[0] = jvm;
				cmd[1] = "-jar";
				cmd[2] = tmp.getAbsolutePath();
				System.arraycopy(args, 0, cmd, 3, args.length);
				builder.command(cmd);
			}
		}else{
			builder.command(jvm, "-jar", tmp.getAbsolutePath());
		}
		builder.redirectOutput(Redirect.INHERIT);
		builder.redirectError(Redirect.INHERIT);
		
		//Start and hope for the best
		try{
			System.exit(builder.start().waitFor());
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if any folder on the path to the
	 * given file ends with an exclamation mark 
	 * <code>(!)</code>.
	 * @param file The file to check the path of.
	 * @return True if an exclamation mark was
	 *         found on the path for the given file.
	 */
	private static final boolean verifyPath(File file){
		if(file == null){
			return false;
		}if(file.isFile()){
			return verifyPath(file.getParentFile());
		}else{
			if(file.getName().endsWith("!")){
				return true;
			}else{
				return verifyPath(file.getParentFile());
			}
		}
	}
}
