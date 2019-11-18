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
 * an exclamation mark <tt>(!)</tt>. This bug has been reported a number
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
	
	private static File exe;
	
	public static void handleSituation(String args){
		if(check()){
			showWarning(args);
		}
	}
	
	private static final void showWarning(String args){
		
		
		//TODO show warning and info dialog and provide the option to relaunch from temp
		
		
		
		//RL anyway
		relaunchFromTemp(args);
		Dialog.showMessageDialog("An error occured whilst trying to launch the program >.<");

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
		return exe.getAbsolutePath().contains("!");
	}

	/**
	 * Re-launches the program from the temp directory
	 * if the program path contains a ! this fixes the
	 * JDK bug. If this subroutine managed to execute
	 * properly the call to this subroutine will never
	 * return.
	 * @param args The original command line arguments
	 *        <code>null</code> allowed for none.
	 */
	private static final void relaunchFromTemp(String args){
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
			tmp = File.createTempFile(null, null);
			tmp.deleteOnExit();

			//Infinite loops are no fun
			if(tmp.getAbsolutePath().contains("!")){
				return;
			}
			
			Files.copy(exe.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		
		//Build the new process
		ProcessBuilder builder = new ProcessBuilder();
		if(args != null){
			builder.command(jvm, "-jar", tmp.getAbsolutePath(), args);
		}else{
			builder.command(jvm, "-jar", tmp.getAbsolutePath());
		}
		builder.redirectOutput(Redirect.INHERIT);
		builder.redirectError(Redirect.INHERIT);
		
		//Start an hope for the best
		try{
			System.exit(builder.start().waitFor());
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
		}
	}
}
