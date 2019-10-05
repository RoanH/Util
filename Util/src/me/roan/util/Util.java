package me.roan.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Util{
	
	public static JLabel getVersionLabel(String repository, String currentVersion){
		JLabel ver = new JLabel("<html><center><i>Version: " + currentVersion + ", latest version: <font color=gray>loading</font></i></center></html>", SwingConstants.CENTER);
		new Thread(()->{
			String version = checkVersion(repository);
			ver.setText("<html><center><i>Version: " + currentVersion + ", latest version: " + (version == null ? "unknown :(" : version) + "</i></center></html>");
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
