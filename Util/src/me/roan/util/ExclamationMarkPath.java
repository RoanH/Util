package me.roan.util;

import java.awt.Window.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Currently as of writing this class there is a bug in the JDK where
 * a program cannot correctly read resources from its JAR if it is
 * located on a path where the name of one of the directories ends with
 * an exclamation mark <tt>(!)</tt>. This bug has been reported a number
 * of times with the first report I could find being from the 9th of 
 * August, 2000 (see links below). 
 * <p>
 * The cause of this iss
 * 
 * 
 * 
 *  
 * 
 * 
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

	public static final boolean check(){
		CodeSource source = ExclamationMarkPath.class.getProtectionDomain().getCodeSource();
		if(source == null){
			//Chances of there being no problem are higher
			return true;
		}
		
		//First get the exe location
		URL url = source.getLocation();
		try{
			exe = new File(url.toURI());
		}catch(URISyntaxException e){
			exe = new File(url.getPath());
		}
		
		//If there is no exclamation mark there is no problem
		return !exe.getAbsolutePath().contains("!");
	}
	
	
	
	

	/**
	 * Re-launches the program from the temp directory
	 * if the program path contains a ! this fixes a
	 * bug in the native library loading
	 * @param args The original command line arguments
	 */
	private static final void relaunchFromTemp(String args){
		
		File jvm = new File(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe");
		if(!jvm.exists() || !exe.exists()){
			System.out.println("JVM exists: " + jvm.exists() + " Executable exists: " + exe.exists());
			Dialog.showMessageDialog("An error occured whilst trying to launch the program >.<");
			System.exit(0);
		}
		File tmp = null;
		try{
			tmp = File.createTempFile("kps", null);
			if(tmp.getAbsolutePath().contains("!")){
				Dialog.showMessageDialog("An error occured whilst trying to launch the program >.<");
				System.exit(0);
			}
			Files.copy(exe.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e){
			e.printStackTrace();
			Dialog.showMessageDialog("An error occured whilst trying to launch the program >.<");
			tmp.deleteOnExit();
			tmp.delete();
			System.exit(0);
		}
		ProcessBuilder builder = new ProcessBuilder();
		if(args != null){
			builder.command(jvm.getAbsolutePath(), "-jar", tmp.getAbsolutePath(), args);
		}else{
			builder.command(jvm.getAbsolutePath(), "-jar", tmp.getAbsolutePath());
		}
		Process proc = null;
		try{
			proc = builder.start();
		}catch(IOException e){
			e.printStackTrace();
			Dialog.showMessageDialog("An error occured whilst trying to launch the program >.<");
			tmp.deleteOnExit();
			tmp.delete();
			System.exit(0);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		try{
			while(proc.isAlive()){
				while((line = in.readLine()) != null){
					System.out.println(line);
				}
				try{
					Thread.sleep(1000);
				}catch(InterruptedException e){
				}
			}
		}catch(IOException e){
			System.err.print("Output stream chrashed :/");
		}
		tmp.deleteOnExit();
		tmp.delete();
		System.exit(0);
	}
}
