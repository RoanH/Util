package me.roan.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Class with various utility subroutines.
 * @author Roan
 */
public class Util{
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
	 * label text will be in italics and centered.
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
		JLabel ver = new JLabel(String.format(italics ? VERSION_FORMAT_ITALICS : VERSION_FORMAT, currentVersion, "<i><font color=gray>loading</font></i>"), alignment);
		new Thread(()->{
			String version = checkVersion(repository);
			ver.setText(String.format(italics ? VERSION_FORMAT_ITALICS : VERSION_FORMAT, currentVersion, version == null ? "unknown :(" : version));
		}, "Version Checker").start();
		return ver;
	}
	
	/**
	 * Checks the version to see
	 * if we are running the latest version
	 * @param repository The repository to check in
	 * @return The latest version
	 */
	public static final String checkVersion(String repository){
		try{
			HttpURLConnection con = (HttpURLConnection)new URL("https://api.github.com/repos/RoanH/" + repository + "/tags").openConnection();
			con.setRequestMethod("GET");
			con.addRequestProperty("Accept", "application/vnd.github.v3+json");
			con.setConnectTimeout(10000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = reader.readLine();
			reader.close();
			String[] versions = line.split("\"name\":\"v");
			int max_main = 3;
			int max_sub = 0;
			String[] tmp;
			for(int i = 1; i < versions.length; i++){
				tmp = versions[i].split("\",\"")[0].split("\\.");
				if(Integer.parseInt(tmp[0]) > max_main){
					max_main = Integer.parseInt(tmp[0]);
					max_sub = Integer.parseInt(tmp[1]);
				}else if(Integer.parseInt(tmp[0]) < max_main){
					continue;
				}else{
					if(Integer.parseInt(tmp[1]) > max_sub){
						max_sub = Integer.parseInt(tmp[1]);
					}
				}
			}
			return "v" + max_main + "." + max_sub;
		}catch(Exception e){
			return null;
			//No Internet access or something else is wrong,
			//No problem though since this isn't a critical function
		}
	}
}
