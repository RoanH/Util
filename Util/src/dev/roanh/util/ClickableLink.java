package dev.roanh.util;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * MouseListener that opens the URL it is
 * instantiated with when triggered
 * @author Roan
 */
public final class ClickableLink implements MouseListener{
	/**
	 * The target link
	 */
	private URI uri = null;
	
	/**
	 * Constructs a new ClickableLink
	 * with the given url
	 * @param link The link to browse to
	 *        when clicked
	 */
	public ClickableLink(String link){
		try{
			uri = new URI(link);
		}catch(URISyntaxException e){
			//pity
		}
	}

	@Override
	public void mouseClicked(MouseEvent e){
		if(Desktop.isDesktopSupported() && uri != null){
			try{
				Desktop.getDesktop().browse(uri);
			}catch(IOException e1){
				//pity
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e){
	}

	@Override
	public void mouseReleased(MouseEvent e){
	}

	@Override
	public void mouseEntered(MouseEvent e){
	}

	@Override
	public void mouseExited(MouseEvent e){
	}
}