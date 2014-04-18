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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * This class displays the "About" message, should the user wish to know more
 * about the program.
 * 
 * @author Daniel Suni
 * @version 1.0.1
 */
public class About {

	private ResourceBundle bundle = ResourceBundle.getBundle("star_of_Africa/prop/Messages", Locale.getDefault());
	private JFrame frame = new JFrame(bundle.getString("about"));
	private JPanel all = new JPanel();
	private JPanel top = new JPanel();
	private JPanel middle = new JPanel();
	private JPanel bottom = new JPanel();
	private JLabel topIcon,topText;
	private JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
	private JPanel about = new JPanel();
	private JPanel author = new JPanel();
	private JScrollPane license = new JScrollPane();
	private JLabel aboutLabel, authorLabel, licenseLabel;
	private JButton exit = new JButton(bundle.getString("exit"));

	public About() {
		URL imgURL;
		imgURL = getClass().getResource("img/icon.png");
		ImageIcon icon = new ImageIcon(imgURL);
		frame.setIconImage(icon.getImage());
		frame.add(all);
		all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));
		all.add(top);
		all.add(middle);
		all.add(bottom);
		top.setLayout(new FlowLayout(FlowLayout.LEFT));
		topIcon = new JLabel(icon);
		topText = new JLabel(bundle.getString("aboutHeadLine"));
		top.add(topIcon);
		top.add(topText);
		middle.add(tab);
		tab.addTab(bundle.getString("aboutShort"), about);
		tab.addTab(bundle.getString("author"), author);
		tab.addTab(bundle.getString("license"), license);
		aboutLabel = new JLabel(bundle.getString("aboutLong"));
		authorLabel = new JLabel(bundle.getString("authorLong"));
		licenseLabel = new JLabel();
		about.add(aboutLabel);
		author.add(authorLabel);
		license.setPreferredSize(new Dimension(575,200));
		license.setViewportView(licenseLabel);
		String licenseStr = bundle.getString("licenseLong");
		URL txtURL = getClass().getResource("docs/COPYING");		
		try {
			InputStream inStr = txtURL.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inStr));
			String line;
			// Read the file one line at a time
			while ((line = br.readLine()) != null)   {
				licenseStr += line;
				licenseStr += "<br>";
			}
			inStr.close();
		}
		catch (FileNotFoundException fnf) {
			licenseStr += "There was an error displaying the license: The file &quot;COPYING&quot; was not found." +
					"<br><br>You should have received a copy of this file along with the program.<br>If you've misplaced it you " +
					"can still find the text of the license at:<br>http://www.gnu.org/licenses";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		licenseLabel.setText(licenseStr);
		bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(exit);
		exit.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.dispose();
					}
				}
		);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame .setPreferredSize(new Dimension(600,450));
		frame.pack();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();	    
		frame.setLocation(ge.getCenterPoint().x - (frame.getSize().width/2), ge.getCenterPoint().y - (frame.getSize().height/2));
		frame.setVisible(true);
	}
}
