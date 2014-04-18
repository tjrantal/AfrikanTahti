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

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

/**
 * This class is responsible for playing the sound effects. Upon running,
 * it will wait for a method to give it a valid filename to play, and to notify
 * that it's ready. Playing 2 sounds on top of each other is not possible. The
 * later sound will have to wait for the first to finish playing.
 * 
 * @author Daniel Suni
 * @version 1.0.1
 */
public class SoundPlayer extends Thread {

	private String filename = "";
	private Clip soundClip;

	/**
	 * Wakes up the class to play a file.
	 * 
	 * @param filename	the filename including the path
	 */
	public void play(String filename) {
		if (filename.equals("")) {
			return;
		}
		synchronized (this) {
			this.filename = filename;
			URL soundURL = getClass().getResource(filename);
			Line.Info linfo = new Line.Info(Clip.class);
			try {
				Line line = AudioSystem.getLine(linfo);
				soundClip = (Clip)line;
				AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL);
			    soundClip.open(ais);
			}
			catch (LineUnavailableException lue) {
				System.err.println("Audio line currently not available.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			notify();
		}
	}

	public void run() {
		while (true) {
			synchronized (this) {
				while (filename.equals("")) {
					try {
						wait();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (soundClip != null) {
					soundClip.setFramePosition(0);
					soundClip.start();
				}
				else {
					System.err.println("Error loading sound");
				}
				filename = "";
				soundClip = null;
			}
		}
	}
}

