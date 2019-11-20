package me.roan.util;

import java.io.File;

import javax.swing.JFileChooser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

@SuppressWarnings("restriction")
public class FileDialog{
	private static Boolean hasJFX = null;

	public static final synchronized boolean hasJFX(){
		if(hasJFX == null){
			try{
				Class.forName("javafx.embed.swing.JFXPanel");
				new JFXPanel();
				hasJFX = Boolean.TRUE;
			}catch(ClassNotFoundException e){
				hasJFX = Boolean.FALSE;
			}
		}
		return hasJFX.booleanValue();
	}

	private static final void showFileOpenSwing(){
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		
		System.out.println("Swing: " + chooser.getSelectedFile());
	}

	private static final void showFileOpenJFX(){
		final FileData data = new FileData();
		Platform.runLater(()->{
			FileChooser chooser = new FileChooser();

			//Set extension filter
			//FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
			//FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
			//chooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

			
			
			//Show open file dialog
			data.file = chooser.showOpenDialog(null);

			synchronized(data){
				data.notify();
			}
		});

		try{
			synchronized(data){
				data.wait();
			}
		}catch(InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("JFX: " + data.file);
	}

	public static void main(String[] args){
		if(hasJFX()){
			showFileOpenJFX();
		}else{
			showFileOpenSwing();
		}
	}

	private static class FileData{
		File file;
	}
}
