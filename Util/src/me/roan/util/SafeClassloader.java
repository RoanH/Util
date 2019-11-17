package me.roan.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
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
		
		System.out.println("Resources: " + res + " | name=" + name);
		
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
