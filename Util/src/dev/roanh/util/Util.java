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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class with various utility subroutines.
 * @author Roan
 */
public class Util{
	/**
	 * The current Util version as a string without the 'v' prefix.
	 * If Util is built locally and not included as a release artifact
	 * this field will likely be <code>null</code>.
	 */
	public static final String VERSION = readArtifactVersion("dev.roanh.util", "util");
	/**
	 * Version label format in italics.
	 * @see #VERSION_FORMAT
	 */
	private static final String VERSION_FORMAT_ITALICS = "<html><center><i>Version: %1$s, latest version: %2$s</i></center></html>";
	/**
	 * Version label format.
	 * @see #VERSION_FORMAT_ITALICS
	 */
	private static final String VERSION_FORMAT = "<html><center>Version: %1$s, latest version: %2$s</center></html>";
	
	/**
	 * Gets a version label that automatically updates
	 * with the latest version after some time. The
	 * label text will be in italics and centred.
	 * @param repository The repository to check the version for.
	 * @param currentVersion The current version of the software.
	 * @return An automatically updating label with the
	 *         latest version.
	 * @see SwingConstants
	 * @see #getVersionLabel(String, String, boolean, int)
	 */
	public static JLabel getVersionLabel(String repository, String currentVersion){
		return getVersionLabel(repository, currentVersion, true, SwingConstants.CENTER);
	}
	
	/**
	 * Gets a version label that automatically updates
	 * with the latest version after some time.
	 * @param repository The repository to check the version for.
	 * @param currentVersion The current version of the software.
	 * @param italics Whether or not to display the
	 *        text in italics.
	 * @param alignment The text alignment inside the label.
	 * @return An automatically updating label with the
	 *         latest version.
	 * @see SwingConstants
	 * @see #getVersionLabel(String, String)
	 */
	public static JLabel getVersionLabel(String repository, String currentVersion, boolean italics, int alignment){
		return getVersionLabel("RoanH", repository, currentVersion, italics, alignment);
	}
	
	/**
	 * Gets a version label that automatically updates
	 * with the latest version after some time.
	 * @param user The user that owns the repository.
	 * @param repository The repository to check the version for.
	 * @param currentVersion The current version of the software.
	 * @param italics Whether or not to display the
	 *        text in italics.
	 * @param alignment The text alignment inside the label.
	 * @return An automatically updating label with the
	 *         latest version.
	 * @see SwingConstants
	 * @see #getVersionLabel(String, String)
	 */
	public static JLabel getVersionLabel(String user, String repository, String currentVersion, boolean italics, int alignment){
		return getVersionLabel(user, repository, currentVersion, "unknown", italics, alignment);
	}
	
	/**
	 * Gets a version label that automatically updates
	 * with the latest version after some time.
	 * @param user The user that owns the repository.
	 * @param repository The repository to check the version for.
	 * @param currentVersion The current version of the software.
	 * @param def Default version name to set if the most up to date
	 *        version cannot be determined.
	 * @param italics Whether or not to display the
	 *        text in italics.
	 * @param alignment The text alignment inside the label.
	 * @return An automatically updating label with the
	 *         latest version.
	 * @see SwingConstants
	 * @see #getVersionLabel(String, String)
	 */
	public static JLabel getVersionLabel(String user, String repository, String currentVersion, String def, boolean italics, int alignment){
		JLabel ver = new JLabel(String.format(italics ? VERSION_FORMAT_ITALICS : VERSION_FORMAT, currentVersion, "<i><font color=gray>loading</font></i>"), alignment);
		checkVersion(user, repository, version->ver.setText(String.format(italics ? VERSION_FORMAT_ITALICS : VERSION_FORMAT, currentVersion, version.orElse(def))));
		return ver;
	}
	
	/**
	 * Asynchronously checks the latest version releases in the given repository.
	 * @param user The user that owns the repository.
	 * @param repository The repository to check in.
	 * @param callback The callback to pass the found latest version to
	 *        one is has been determined.
	 */
	public static void checkVersion(String user, String repository, Consumer<Optional<String>> callback){
		Thread thread = new Thread(()->callback.accept(Optional.ofNullable(checkVersion(user, repository))), "Version Checker");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * Gets the latest version releases in the given repository.
	 * @param user The user that owns the repository.
	 * @param repository The repository to check in.
	 * @return The latest version.
	 */
	public static final String checkVersion(String user, String repository){
		try{
			HttpURLConnection con = (HttpURLConnection)new URL("https://api.github.com/repos/" + user + "/" + repository + "/tags").openConnection();
			con.setRequestMethod("GET");
			con.addRequestProperty("Accept", "application/vnd.github.v3+json");
			con.setConnectTimeout(10000);
			
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))){
				String line = reader.readLine();
				reader.close();
				
				String[] versions = line.split("\"name\":\"v");
				int major = 1;
				int minor = 0;
				for(int i = 1; i < versions.length; i++){
					String[] tmp = versions[i].split("\",\"")[0].split("\\.");
					int num = Integer.parseInt(tmp[0]);
					
					if(num > major){
						major = num;
						minor = Integer.parseInt(tmp[1]);
					}else if(num == major){
						minor = Math.max(minor, Integer.parseInt(tmp[1]));
					}
				}
				return "v" + major + "." + minor;
			}
		}catch(Exception e){
			return null;
			//No Internet access or something else is wrong,
			//No problem though since this isn't a critical function
		}
	}
	
	/**
	 * Sets the program look and feel to the default
	 * for the operating system the program is running on.
	 */
	public static final void installUI(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Throwable ignore){
		}
	}
	
	/**
	 * Reads the <code>version</code> field from the given Maven artifact
	 * included in the current jar(s).
	 * @param group The group ID for the artifact.
	 * @param artifact The name of the artifact to get the version of.
	 * @return The version field of the given Maven artifact or <code>
	 *         null</code> if no version information was found for the
	 *         given artifact.
	 */
	public static final String readArtifactVersion(String group, String artifact){
		try(InputStream pom = ClassLoader.getSystemResourceAsStream("META-INF/maven/" + group + "/" + artifact + "/pom.xml")){
			if(pom == null){
				return null;
			}
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setIgnoringElementContentWhitespace(true);
			
			NodeList data = docFactory.newDocumentBuilder().parse(pom).getElementsByTagName("project").item(0).getChildNodes();
			for(int i = 0; i < data.getLength(); i++){
				Node node = data.item(i);
				if(node.getNodeName().equals("version")){
					return node.getTextContent();
				}
			}
			
			return null;
		}catch(Exception e){
			return null;
		}
	}
}
