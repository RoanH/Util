package me.roan.util;

import java.awt.Window.Type;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class SafeClassloader extends URLClassLoader{
	
	private ClassLoader parent;
	private JarFile jar;
	
	public SafeClassloader(ClassLoader parent){
		super(new URL[]{}, parent);
		this.parent = parent;
		URL loc = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		System.out.println(loc);
		System.out.println(loc.getFile());
		System.out.println(new File(loc.getFile()));
		System.out.println(new File(loc.getFile()).isDirectory());

		if(!new File(loc.getFile()).isDirectory()){
			try{
				jar = new JarFile(new File(loc.getFile()));
				
				ZipEntry entry = jar.getEntry("notafile");
				System.out.println("Entry: " + entry);
				
				
			}catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public final URL findResource(String name){
		URL res = super.findResource(name);
		
		System.out.println("Resource: " + res + " | name=" + name);
		
		return res;
	}
	
	@Override
	public final Enumeration<URL> findResources(String name) throws IOException{
		Enumeration<URL> res = super.findResources(name);
		
		System.out.println("Attempting to find resource: " + name + " found: " + res + " type: " + res.getClass());
		int c = 0;
		while(res.hasMoreElements()){
			System.out.println(c + " Resources: " + res.nextElement() + " | name=" + name);
		}
		
		//TODO only if hasNext false on super call
		//TODO for all proper error handling
		
		//jar
		System.out.println("JAR FILE START =====>");

		ZipEntry entry = jar.getEntry(name);
		InputStream in = jar.getInputStream(entry);
		
		File temp = Files.createTempFile(null, null).toFile();
		temp.deleteOnExit();

		OutputStream out = new FileOutputStream(temp);
		byte[] buffer = new byte[1024];//Generally small files
		int len;
		while((len = in.read(buffer)) != -1){
			out.write(buffer, 0, len);
		}
		in.close();
		out.flush();
		out.close();
		
		res = new Enumeration<URL>(){
			
			private File file = temp;

			@Override
			public boolean hasMoreElements(){
				return file != null;
			}

			@Override
			public URL nextElement(){
				try{
					return file.toURI().toURL();
				}catch(MalformedURLException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		};
		
		System.out.println("JAR FILE END <=====");
		
		return res;
	}
	
	@Override
	public final URL getResource(String name){
		URL res = super.getResource(name);
				
		System.out.println("Get resource: " + res + " | name=" + name);
				
		return res;
	}
	
	@Override
	public final InputStream getResourceAsStream(String name){
		//InputStream res = super.getResourceAsStream(name);
		
		InputStream res = null;
		try{
			res = jar.getInputStream(jar.getEntry(name));
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Stream: " + res + " | name=" + name);

		
		return res;
	}
}
