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