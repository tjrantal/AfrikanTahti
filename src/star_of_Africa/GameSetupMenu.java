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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * This class provides the GUI for setting up the game. I.e. deciding on the number
 * of players, their names, starting locations, gamepieces and whether the player
 * is human or AI.
 * 
 * This class contains the main-method that kicks off the program.
 * 
 * @author Daniel Suni
 * @version 1.0.3
 */

public class GameSetupMenu implements ActionListener {

	private ResourceBundle bundle = ResourceBundle.getBundle("star_of_Africa/prop/Messages", Locale.getDefault());
	private JFrame frame = new JFrame(bundle.getString("setupTitle"));
	private JMenuBar menuBar = new JMenuBar();
	private JMenu file = new JMenu(bundle.getString("file"));
	private JMenu help = new JMenu(bundle.getString("help"));
	private JMenuItem exit = new JMenuItem(bundle.getString("exit"),bundle.getString("exitChar").charAt(0));
	private JMenuItem rules = new JMenuItem(bundle.getString("rules"),bundle.getString("rulesChar").charAt(0));
	private JMenuItem howto = new JMenuItem(bundle.getString("howTo"),bundle.getString("howToChar").charAt(0));
	private JMenuItem about = new JMenuItem(bundle.getString("about"),bundle.getString("aboutChar").charAt(0));
	private JPanel panel,subpanel0,subpanel1,subpanel2,subpanel3,subpanel4;
	private JRadioButtonMenuItem human,computer;
	private JRadioButtonMenuItem tangier,cairo;
	private JRadioButtonMenuItem red,green,blue,yellow,lilac;
	private ButtonGroup who,start,colour;
	private JButton addPlayer = new JButton(bundle.getString("addPlayerButton"));
	private JButton startGame = new JButton(bundle.getString("startGameButton"));
	private JLabel label1 = new JLabel(bundle.getString("playerType")+":");
	private JLabel label2 = new JLabel(bundle.getString("startingPoint")+":");
	private JLabel label3 = new JLabel(bundle.getString("gamePiece")+":");
	private JLabel headline = new JLabel(bundle.getString("player")+" 1");
	private JLabel label4 = new JLabel(bundle.getString("name")+":");
	private JTextField playerName = new JTextField(bundle.getString("player")+" 1", 8);
	private Player[] player = new Player[6]; // Max 5 players (there is no player[0])
	private int playerNum = 1;
	private int height;
	private boolean[] piece = new boolean[5]; // Keeps track on whether a gampiece is still available

	/**
	 * Constructs a setup menu for the game. Requires no parameters, since it's the
	 * first thing to be run when starting a new game.
	 */
	public GameSetupMenu() {
		// Initialize the main components
		URL imgURL = getClass().getResource("img/icon.png");
		frame.setIconImage(new ImageIcon(imgURL).getImage());
		for (int i=0; i < piece.length; i++) {
			piece[i] = true;
		}
		file.setMnemonic(bundle.getString("fileChar").charAt(0));
		help.setMnemonic(bundle.getString("helpChar").charAt(0));
		menuBar.add(file);
		menuBar.add(help);
		file.add(exit);
		help.add(rules);
		help.add(howto);
		help.add(about);
		exit.addActionListener(this);
		rules.addActionListener(this);
		howto.addActionListener(this);
		help.addActionListener(this);
		about.addActionListener(this);
		frame.setJMenuBar(menuBar);
		panel = new JPanel();
		subpanel0 = new JPanel();
		subpanel1 = new JPanel();
		subpanel2 = new JPanel();
		subpanel3 = new JPanel();
		subpanel4 = new JPanel();
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		subpanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		subpanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		subpanel3.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setLayout(new GridLayout(5,1));

		// Human / AI buttongroup
		human = new JRadioButtonMenuItem(bundle.getString("human"));
		computer = new JRadioButtonMenuItem(bundle.getString("computer"));
		who = new ButtonGroup();
		who.add(computer);
		who.add(human);
		subpanel1.add(label1);
		subpanel1.add(computer);
		subpanel1.add(human);
		subpanel1.add(label4);
		subpanel1.add(playerName);
		computer.addItemListener(
				new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (computer.isSelected()) {
							playerName.setEnabled(false);
							tangier.setEnabled(false);
							cairo.setEnabled(false);
							red.setEnabled(false);
							green.setEnabled(false);
							blue.setEnabled(false);
							yellow.setEnabled(false);
							lilac.setEnabled(false);
						}
						else {
							playerName.setEnabled(true);
							tangier.setEnabled(true);
							cairo.setEnabled(true);
							red.setEnabled(piece[0]);
							green.setEnabled(piece[1]);
							blue.setEnabled(piece[2]);
							yellow.setEnabled(piece[3]);
							lilac.setEnabled(piece[4]);
						}
					}
				}
		);

		// Starting point buttongroup
		tangier = new JRadioButtonMenuItem("Tangier");
		cairo = new JRadioButtonMenuItem("Cairo");
		start = new ButtonGroup();
		start.add(tangier);
		start.add(cairo);
		subpanel2.add(label2);
		subpanel2.add(tangier);
		subpanel2.add(cairo);

		// Gamepiece buttongroup
		red = new JRadioButtonMenuItem(GamePiece.RED.getResizedIcon(40));
		green = new JRadioButtonMenuItem(GamePiece.GREEN.getResizedIcon(40));
		blue = new JRadioButtonMenuItem(GamePiece.BLUE.getResizedIcon(40));
		yellow = new JRadioButtonMenuItem(GamePiece.YELLOW.getResizedIcon(40));
		lilac = new JRadioButtonMenuItem(GamePiece.LILAC.getResizedIcon(40));
		colour = new ButtonGroup();
		colour.add(red);
		colour.add(green);
		colour.add(blue);
		colour.add(yellow);
		colour.add(lilac);
		subpanel3.add(label3);
		subpanel3.add(red);
		subpanel3.add(green);
		subpanel3.add(blue);
		subpanel3.add(yellow);
		subpanel3.add(lilac);

		// Last row
		subpanel4.add(addPlayer);
		subpanel4.add(startGame);
		addPlayer.addActionListener(this);
		startGame.addActionListener(this);

		// Finalize it all
		Font currentFont = headline.getFont();
		headline.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), 42));
		subpanel0.add(headline);
		panel.add(subpanel0);
		panel.add(subpanel1);
		panel.add(subpanel2);
		panel.add(subpanel3);
		panel.add(subpanel4);
		startGame.setEnabled(false);
		computer.setEnabled(false);
		human.setSelected(true);
		tangier.setSelected(true);
		red.setSelected(true);
		frame.setPreferredSize(new Dimension(500,325));
		frame.pack();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();	    
		frame.setLocation(ge.getCenterPoint().x - (frame.getSize().width/2), ge.getCenterPoint().y - (frame.getSize().height/2));
		height = ge.getDefaultScreenDevice().getDisplayMode().getHeight();
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {

		if (playerNum > 1)
			startGame.setEnabled(true);

		int rand;
		if (e.getSource() == addPlayer) {
			GamePiece gp = GamePiece.RED;
			if (human.isSelected()) {
				if (red.isSelected()) {
					gp = GamePiece.RED;
					red.setEnabled(false);
					piece[0] = false;
				}
				if (green.isSelected()) {
					gp = GamePiece.GREEN;
					green.setEnabled(false);
					piece[1] = false;
				}
				if (blue.isSelected()) {
					gp = GamePiece.BLUE;
					blue.setEnabled(false);
					piece[2] = false;
				}
				if (yellow.isSelected()) {
					gp = GamePiece.YELLOW;
					yellow.setEnabled(false);
					piece[3] = false;
				}
				if (lilac.isSelected()) {
					gp = GamePiece.LILAC;
					lilac.setEnabled(false);
					piece[4] = false;
				}
				player[playerNum] = new Player(true,tangier.isSelected(),gp, playerName.getText());
				computer.setEnabled(true);
			}

			else {
				// AI picks a random gamepiece (of those that are left)
				boolean found = false;
				while (!found) {
					rand = 1 + (int)(Math.random() * 5);
					switch(rand) {
					case 1:
						if (piece[0]) {
							gp = GamePiece.RED;
							red.setEnabled(false);
							piece[0] = false;
							found = true;
						}
						break;
					case 2:
						if (piece[1]) {
							gp = GamePiece.GREEN;
							green.setEnabled(false);
							piece[1] = false;
							found = true;
						}
						break;
					case 3:
						if (piece[2]) {
							gp = GamePiece.BLUE;
							blue.setEnabled(false);
							piece[2] = false;
							found = true;
						}
						break;
					case 4:
						if (piece[3]) {
							gp = GamePiece.YELLOW;
							yellow.setEnabled(false);
							piece[3] = false;
							found = true;
						}
						break;
					case 5:
						if (piece[4]) {
							gp = GamePiece.LILAC;
							lilac.setEnabled(false);
							piece[4] = false;
							found = true;
						}
						break;	
					}
				}

				// AI starts where there are fewer players, or at random spot if equal
				int tan = 0;
				int cai = 0;
				for (int i = 1; i < playerNum; i++) {
					if (player[i].startsAtTangier())
						tan++;
					else
						cai++;
				}
				boolean start = (tan < cai);
				if (tan == cai) {
					start = (Math.random() < 0.5);
				}
				player[playerNum] = new Player(false, start, gp, playerName.getText());
			}

			// Select the leftmost gamepiece that still hasn't been picked
			if (lilac.isEnabled())
				lilac.setSelected(true);
			if (yellow.isEnabled())
				yellow.setSelected(true);
			if (blue.isEnabled())
				blue.setSelected(true);
			if (green.isEnabled())
				green.setSelected(true);
			if (red.isEnabled())
				red.setSelected(true);
		}
		// If no more players are desired, we can start the game
		if (e.getSource() == startGame) {
			startGame.setEnabled(false);
			addPlayer.setEnabled(false);
			// First take the players and put them into a new table in random order
			Player[] temp = new Player[playerNum-1];
			boolean isOkay;
			for (int i=0; i<playerNum-1; i++) {
				do {
					isOkay = true;
					rand = 1 + (int)(Math.random() * (playerNum-1));
					for (int j=0; j<i; j++) {
						if (temp[j].equals(player[rand])) {
							isOkay = false;
						}
					}
				} while(!isOkay);
				temp[i] = player[rand];
			}
			new PlayingField(temp, height);
			frame.dispose();
		}

		if (e.getSource() == addPlayer) {
			playerNum++;

			if (playerNum <= 5) {
				headline.setText(bundle.getString("player")+" "+playerNum);
				playerName.setText(bundle.getString("player")+" "+playerNum);
			}

			// Max 5 players allowed
			if (playerNum > 5) {
				addPlayer.setEnabled(false);
			}
		}

		if (e.getSource() == exit) {
			System.exit(0);
		}

		if (e.getSource() == rules) {
			URL url = getClass().getResource(bundle.getString("rulesFile"));
			JFrame ruleFrame = new JFrame(bundle.getString("rules"));
			JScrollPane scroll = new JScrollPane();
			JEditorPane rulePane = new JEditorPane();
			URL imgURL = getClass().getResource("img/icon.png");
			ruleFrame.setIconImage(new ImageIcon(imgURL).getImage());
			try {
				rulePane.setContentType("text/html"); 
				rulePane.setPage(url);
			}
			catch (Exception x) {
				x.printStackTrace();
			}
			ruleFrame.add(scroll);
			scroll.setViewportView(rulePane);
			scroll.setPreferredSize(new Dimension(800,600));
			ruleFrame.pack();
			ruleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ruleFrame.setVisible(true);
		}

		if (e.getSource() == howto) {
			URL url = getClass().getResource(bundle.getString("instructionFile"));
			JFrame insFrame = new JFrame(bundle.getString("howTo"));
			JScrollPane scroll = new JScrollPane();
			JEditorPane insPane = new JEditorPane();
			URL imgURL = getClass().getResource("img/icon.png");
			insFrame.setIconImage(new ImageIcon(imgURL).getImage());
			try {
				insPane.setContentType("text/html"); 
				insPane.setPage(url);
			}
			catch (Exception x) {
				x.printStackTrace();
			}
			insFrame.add(scroll);
			scroll.setViewportView(insPane);
			scroll.setPreferredSize(new Dimension(800,600));
			insFrame.pack();
			insFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			insFrame.setVisible(true);
		}
		if (e.getSource() == about) {
			new About();
		}
	}

	public static void main(String[] args) {
		new GameSetupMenu();
	}
}
