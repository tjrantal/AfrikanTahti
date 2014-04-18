/*   Copyright 2009 Daniel Suni
 *
 *   This file is part of the Star of Africa.
 *
 *   The Star of Africa is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   The Star of Africa is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with the Star of Africa.  If not, see <http://www.gnu.org/licenses/>.
 */

package star_of_Africa;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * This class holds the values a gamepiece can have, as well as the images to
 * be displayed in the event window.
 * 
 * @author Daniel Suni
 * @version 1.0.0
 */
public enum GamePiece {
	RED,GREEN,BLUE,YELLOW,LILAC;

	/**
	 * Returns an image of the appropritate gamepiece at the requested size.
	 * 
	 * @param height	the desired height of the image in pixels. (The width will
	 * 					automatically be calculated.) Calling the method using 0 will
	 * 					result in an image of the original size.
	 * @return			the resized image of the gamepiece in question.
	 */
	ImageIcon getResizedIcon(int height) {
		Image image;
		URL imgURL = getClass().getResource("img/gp_red.png");
		switch(this) {
		case RED:
			imgURL = getClass().getResource("img/gp_red.png");
			break;
		case GREEN:
			imgURL = getClass().getResource("img/gp_green.png");
			break;
		case BLUE:
			imgURL = getClass().getResource("img/gp_blue.png");
			break;
		case YELLOW:
			imgURL = getClass().getResource("img/gp_yellow.png");
			break;
		case LILAC:
			imgURL = getClass().getResource("img/gp_lilac.png");
			break;
		}
		ImageIcon icon= new ImageIcon(imgURL);
		if (height == 0) {
			return icon;
		}
		image = icon.getImage();
		image = image.getScaledInstance(-1, height, Image.SCALE_DEFAULT);
		icon.setImage(image);
		return icon;
	}
}
