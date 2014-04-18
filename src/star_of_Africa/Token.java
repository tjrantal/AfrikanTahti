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
 * This class holds the values a token can have, as well as the images to
 * be displayed in the event window.
 * 
 * @author Daniel Suni
 * @version 1.0.0
 */
public enum Token {
	BLANK,HORSESHOE,ROBBER,TOPAZ,EMERALD,RUBY,STAR_OF_AFRICA;

	/**
	 * Returns an image of the appropritate token at the requested size.
	 * 
	 * @param height	the desired height of the image in pixels. (The width will
	 * 					automatically be calculated.) Calling the method using 0 will
	 * 					result in an image of the original size.
	 * @return			the resized image of the token in question.
	 */
	ImageIcon getResizedIcon(int height) {
		Image image;
		URL imgURL = getClass().getResource("img/blank.png");
		switch(this) {
		case BLANK:
			imgURL = getClass().getResource("img/blank.png");
			break;
		case HORSESHOE:
			imgURL = getClass().getResource("img/horseshoe.png");
			break;
		case ROBBER:
			imgURL = getClass().getResource("img/robber.png");
			break;
		case TOPAZ:
			imgURL = getClass().getResource("img/topaz.png");
			break;
		case EMERALD:
			imgURL = getClass().getResource("img/emerald.png");
			break;
		case RUBY:
			imgURL = getClass().getResource("img/ruby.png");
			break;
		case STAR_OF_AFRICA:
			imgURL = getClass().getResource("img/star_of_africa.png");
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

	/**
	 * @return	the key for the appropriate <code>Messages.properties</code>-string
	 * 			to be displayed when a token is revealed.
	 */
	String getMessage() {
		switch(this) {
		case BLANK:
			return "blank";
		case HORSESHOE:
			return "horseshoe";
		case ROBBER:
			return "robber";
		case TOPAZ:
			return "topaz";
		case EMERALD:
			return "emerald";
		case RUBY:
			return "ruby";
		case STAR_OF_AFRICA:
			return "congratulations";
		}
		return null;
	}

	/**
	 * @return	the value of the token
	 */
	int monetaryValue() {
		switch(this) {
		case TOPAZ:
			return 300;
		case EMERALD:
			return 500;
		case RUBY:
			return 1000;
		case ROBBER:
			return -10000; // More than the theoretical maximum amount a player may hold
		default:
			return 0;
		}	
	}
	
	/**
	 * @return	the filename, including the path, of the sound to be played
	 */
	String getSound() {
		switch (this) {
			case BLANK:
				return "snd/meh.wav";
			case ROBBER:
				return "snd/gunshots.wav";
			case STAR_OF_AFRICA:
				return "snd/tada.wav";
			case TOPAZ:
			case EMERALD:
			case RUBY:
				return "snd/ding.wav";
			default:
				return ""; // The horseshoe's effect will depend on the situation, and must be determined elsewhere
		}
	}

}
