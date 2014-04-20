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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class provides the GUI for the game itself. It's divided into 2 major sections:
 * The map on the left, and the status windows & controls on the right.
 * It allows the human players to control their gamepieces by using the control buttons,
 * and will notify the waiting AIPlayer class whenever an AI player need to make its
 * move.
 * 
 * This class also handles all of the in-game "bookkeeping" as to the players' money,
 * whether the Star has been found, et.c.
 * 
 * @author Daniel Suni
 * @version 1.0.4
 */

public class PlayingField implements ChangeListener,ActionListener {

	private ResourceBundle bundle = ResourceBundle.getBundle("star_of_Africa/prop/Messages", Locale.getDefault());
	private SoundPlayer soundPlayer = new SoundPlayer();
	private JFrame frame = new JFrame(bundle.getString("fieldTitle"));
	private Map map;
	private AIPlayer aip;
	private JPanel whole = new JPanel();
	private JPanel top = new JPanel();
	private JPanel right = new JPanel();
	private JPanel left = new JPanel();
	private JPanel eventPanel = new JPanel();
	private JPanel messagePanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel[] subpanel,playerTokens;
	private JSlider zoom;
	private JLabel zoomLabel = new JLabel(bundle.getString("zoom")+" %");
	private JLabel[] playerName, playerMoney, playerGamePiece;
	private JLabel eventLabel = new JLabel();
	private JLabel messageLabel = new JLabel(bundle.getString("gameBegin"));
	private JButton rollDice = new JButton(bundle.getString("rollDice"));
	private JButton rollToken = new JButton(bundle.getString("rollToken"));
	private JButton endTurn = new JButton(bundle.getString("endTurn"));
	private JButton boardShip = new JButton(bundle.getString("boardShip"));
	private JButton boardShipNoMoney = new JButton(bundle.getString("boardShipNoMoney"));
	private JButton boardPlane = new JButton(bundle.getString("boardPlane"));
	private JButton buyToken = new JButton(bundle.getString("buyToken"));
	private JButton quit = new JButton(bundle.getString("quit"));
	private JButton newGame = new JButton(bundle.getString("newGame"));
	private JCheckBoxMenuItem same = new JCheckBoxMenuItem(bundle.getString("samePlayers"));
	//private JCheckBoxMenuItem rollToTurn = new JCheckBoxMenuItem(bundle.getString("rollToTurn"));
	private double zoomLevel = 1.0;
	private int turn = 0;
	private int horseshoesFound = 0;
	private boolean starHasBeenFound = false;
	private boolean capetownHasBeenVisited = false;
	private boolean gameOver = false;
	private Player[] player;
	//private boolean[] rollForToken;
	private HashSet<Place> places = new HashSet<Place>();
	private HashSet<Place> cities = new HashSet<Place>();
	private Place[] startingPoints = new Place[2];
	private Token[] tokens = new Token[30];
	private Border box,selectedBox,eventBox;
	private Component emptyBox;
	private int eventWinSize,smallTokenSize,gridNumber,screenSize,fontSize,pieceSize,height;

	/**
	 * Constructs a PlayingField, and assigns it the players who will be playing on it.
	 * 
	 * @param player 	the players who will be participating in the game, in the same order
	 * 					that the turns are to be assigned.
	 * @param height	the vertical resolution of the current screen
	 */
	public PlayingField(Player[] player, int height) {
		this.player = player;
		this.height = height;
		//rollForToken = new boolean[player.length];
		//for (int i=0; i<rollForToken.length; i++) {
		//	rollForToken[i] = false;
		//}
		// First we adjust some visual parameters according to the monitor resolution we're dealing with.
		if (height >= 1200) {
			eventWinSize = 180;
			smallTokenSize = 30;
			gridNumber = 5;
			screenSize = 4;
			fontSize = 18;
			pieceSize = 20;
			zoom = new JSlider(100,250,100);
		}
		else if (height >= 1024) {
			eventWinSize = 100;
			smallTokenSize = 25;
			gridNumber = 6;
			screenSize = 3;
			fontSize = 16;
			pieceSize = 20;
			zoom = new JSlider(100,250,100);
		}
		else if (height >= 768) {
			eventWinSize = 60;
			smallTokenSize = 20;
			gridNumber = 8;
			screenSize = 2;
			fontSize = 14;
			pieceSize = 15;
			zoom = new JSlider(100,300,100);
		}
		else {
			eventWinSize = 30;
			smallTokenSize = 10;
			gridNumber = 15;
			screenSize = 1;
			fontSize = 10;
			pieceSize = 10;
			zoom = new JSlider(100,400,100);
		}
		
		// Set up the frame's icon
		URL imgURL;
		imgURL = getClass().getResource("img/icon.png");
		frame.setIconImage(new ImageIcon(imgURL).getImage());
		
		// This workaround overwrites the icon of the invisible shared frame of dialogs using the parameterless constructor.
		// (That means I get my icon to show in the upper left corner of JOptionDialogs, instead of the stupid coffee cup.)
		JDialog workaround = new JDialog();
		((Frame)workaround.getParent()).setIconImage(new ImageIcon(imgURL).getImage());
		
		setupPlaces();
		map = new Map(player,cities);
		map.setPlayingField(this);
		aip = new AIPlayer(cities, startingPoints, player, soundPlayer); // The AIPlayer class handles the brainwork for all AI
		aip.setPlayingField(this);  									 // players. That's why one instance is enough.
		aip.start(); // Starts, then waits for orders
		setupTokens();
		// Set up the visual elements
		playerName = new JLabel[player.length];
		playerMoney = new JLabel[player.length];
		playerTokens = new JPanel[player.length];
		playerGamePiece = new JLabel[player.length];
		subpanel = new JPanel[player.length];
		//rollToTurn.setEnabled(false);
		buyToken.setEnabled(false);
		endTurn.setEnabled(false);
		frame.add(whole);
		whole.setLayout(new BoxLayout(whole,BoxLayout.X_AXIS));
		whole.add(left);
		whole.add(right);
		left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
		left.add(top);
		left.add(map);
		right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));
		right.setAlignmentY(Component.TOP_ALIGNMENT);
		if (screenSize > 2) {
			right.add(Box.createVerticalStrut(38));
		}
		right.setPreferredSize(new Dimension(200,height-150));
		// Set up the player status windows
		for (int i=0; i<player.length; i++) {
			playerName[i] = new JLabel(player[i].getName());
			Font currentFont = playerName[i].getFont();
			playerName[i].setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), fontSize));
			playerMoney[i] = new JLabel(" Pound 300");
			playerTokens[i] = new JPanel();
			playerTokens[i].setLayout(new GridLayout(0,gridNumber));
			playerGamePiece[i] = new JLabel(player[i].getGamePiece().getResizedIcon(pieceSize));
			subpanel[i] = new JPanel();
			subpanel[i].setLayout(new BorderLayout(5,5));
			right.add(subpanel[i]);
			subpanel[i].add(playerGamePiece[i],BorderLayout.WEST);
			subpanel[i].add(playerName[i],BorderLayout.NORTH);
			subpanel[i].add(playerMoney[i], BorderLayout.CENTER);
			subpanel[i].add(playerTokens[i], BorderLayout.SOUTH);
		}
		if (screenSize > 2) {
			right.add(Box.createVerticalStrut(10));
		}
		right.add(eventPanel);
		emptyBox = Box.createRigidArea(new Dimension(eventWinSize,eventWinSize));
		eventPanel.add(emptyBox);
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		Border redline = BorderFactory.createLineBorder(Color.RED);
		box = BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
		selectedBox = BorderFactory.createCompoundBorder(redline, box);
		eventBox = BorderFactory.createTitledBorder(box, bundle.getString("eventWindow"),TitledBorder.CENTER,TitledBorder.TOP);
		if (screenSize != 1) {
			eventPanel.setBorder(eventBox);
		}
		else {
			Font currentFont = messageLabel.getFont();
			messageLabel.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(),fontSize));
		}
		subpanel[0].setBorder(selectedBox);
		right.add(messagePanel);
		messagePanel.add(messageLabel);
		for (int i=1; i<player.length;i++) {
			subpanel[i].setBorder(box);
		}
		// Set up the controls
		buttonPanel.setLayout(new GridLayout(7,1));
		//buttonPanel.add(rollToTurn);
		buttonPanel.add(rollDice);
		buttonPanel.add(rollToken);
		buttonPanel.add(buyToken);
		buttonPanel.add(boardShip);
		buttonPanel.add(boardShipNoMoney);
		buttonPanel.add(boardPlane);
		buttonPanel.add(endTurn);
		rollDice.addActionListener(this);
		rollToken.addActionListener(this);
		buyToken.addActionListener(this);
		boardShip.addActionListener(this);
		boardShipNoMoney.addActionListener(this);
		boardPlane.addActionListener(this);
		endTurn.addActionListener(this);
		right.add(buttonPanel);
		right.add(Box.createVerticalStrut(1000));
		zoom.setMajorTickSpacing(50);
		zoom.setMinorTickSpacing(10);
		zoom.setPaintTicks(true);
		zoom.setPaintLabels(true);
		zoom.addChangeListener(this);
		top.add(quit);
		top.add(zoomLabel);
		top.add(zoom);
		top.add(newGame);
		top.add(same);
		quit.setVisible(false);
		newGame.setVisible(false);
		same.setVisible(false);
		quit.addActionListener(this);
		newGame.addActionListener(this);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		soundPlayer.start();
		if (!player[0].isHuman()) { // If the first player is an AI player we have to kick off the game here.
			boardPlane.setEnabled(false);
			boardShip.setEnabled(false);
			rollDice.setEnabled(false);
			aip.makeMove(0);
		}
	}
	
	/**
	 * Calculates and displays all possible places where a diceroll entitles the
	 * player to move.
	 * 
	 * @param dice	the value of the diceroll.
	 */
	private void calculateMoveOptions(int dice) {
		// Is the player captured?
		if (player[turn].isCaptured()) {
			if (dice <= 2) {
				messageLabel.setText(bundle.getString("escaped"));
				player[turn].setCaptured(false);
			}
			else {
				messageLabel.setText(bundle.getString("failEscape"));
				return;
			}
		}

		// Is the player shipbound for a destination?
		if (player[turn].getLockedDestination() != null) {
			player[turn].resetRoute();
			ArrayList<Place> tempRoute = getSeaRoute(player[turn].getPlace(),null);
			// Only reason to make the moves a hashset is that it can use the same map.showMoveOptions as everything else
			HashSet<Place> possibleMove = new HashSet<Place>();
			tempRoute.trimToSize();
			if (tempRoute.size() - 1 <= dice) {
				possibleMove.add(player[turn].getLockedDestination());
			}
			else {
				possibleMove.add(tempRoute.get(dice));
			}
			map.showMoveOptions(possibleMove);
		}
		else {
			HashSet<Place> connected = new HashSet<Place>();
			HashSet<Place> canMove = new HashSet<Place>();
			HashSet<Place> temp = new HashSet<Place>(); // Required since you can't add to a set while iterating over it
			player[turn].resetRoute();
			Place tempOrigin = player[turn].getPlace();
			tempOrigin.appendRouteTo(player[turn].getPlace());	/*Route begins from the current place*/
			connected.add(tempOrigin);
			// Iterate for n times where n = diceroll - 1, always adding all neighboring places
			for (int i = 1 ; i < dice ; i++) {
				for (Place p : connected) {
					int connections = 0;
					for (Place q : p.getConnectedByLand()) {
						/*Append only, if it is not on the route already*/
						++connections;
						boolean doAdd = true;
						for (int r = 0;r<p.getRouteTo().size();++r){
							if (q.getX() == p.getRouteTo().get(r).getX() && q.getY() == p.getRouteTo().get(r).getY()){
								doAdd = false;
								break;
							}
						}
						if (doAdd){
						 	q.setRouteTo(p.getRouteTo());
							q.appendRouteTo(q);
							temp.add(q);
							//System.out.println("Step "+i+" connection "+connections+" routeLength "+q.getRouteTo().size());
							//q.printRouteTo();
							//System.out.println("");
						}
					}
				}
				connected.addAll(temp);
				temp.clear();
			}
			// The results of the last iteration go into a different HashSet, which then contains all the places that the
			// diceroll entitles the player to move to.
			for (Place p : connected) {
				for (Place q : p.getConnectedByLand()) {
					if (!connected.contains(q) || q.isCity() || q.isStart()) {
						/*Prevent adding duplicate*/
						boolean doAdd = true;
						for (int r = 0;r<p.getRouteTo().size();++r){
							if (q.getX() == p.getRouteTo().get(r).getX() && q.getY() == p.getRouteTo().get(r).getY()){
								doAdd = false;
								break;
							}
						}
						if (doAdd){
						 	q.setRouteTo(p.getRouteTo());
							q.appendRouteTo(q);
							canMove.add(q);
						}
						
					}
				}
			}
			map.showMoveOptions(canMove);
		}
	}

	/**
	 * Checks which places are immediately reachable by sea. This is a recursive method, which
	 * will call itself until all branches have been exhausted.
	 * 
	 * @param check			the place which is to be checked for connectivity.
	 * @param doNotCheck	a place which is not to be checked (to avoid re-checking checked
	 * 						places, which would lead to infinite loops).
	 * @return				all the places that are 1 searoute away from the origin.
	 */
	private HashSet<Place> getConnectedBySea(Place check, Place doNotCheck) {
		HashSet<Place> temp = new HashSet<Place>();
		for (Place p : check.getConnectedBySea()) {
			if (p != doNotCheck && !(check.isCity() || check.isStart())) {
				temp.addAll(getConnectedBySea(p,check));
			}
			if (check.isCity() || check.isStart()) {
				temp.add(check);
			}
		}
		return temp;
	}

	/**
	 * Assigns a player's status as stranded.
	 * 
	 * @param play	player whose status is to be set as stranded.
	 */
	private void setStranded(Player play) {
		play.setStranded();
		for (int i = 0; i < player.length; i++) {
			if (player[i].equals(play)) {
				playerMoney[i].setText(" Pound " + play.getMoney()+ " - "+bundle.getString("stranded"));
			}
		}
	}

	/**
	 * Announces the winner.
	 */
	private void gameWon() {
		soundPlayer.play("snd/cheers.wav");
		gameOver = true;
		rollDice.setEnabled(false);
		//rollToTurn.setEnabled(false);
		buyToken.setEnabled(false);
		boardPlane.setEnabled(false);
		boardShip.setEnabled(false);
		rollToken.setEnabled(false);
		boardShipNoMoney.setEnabled(false);
		endTurn.setEnabled(false);
		if (screenSize > 2) {
			quit.setVisible(true);
			newGame.setVisible(true);
			same.setVisible(true);
			same.setSelected(true);
		}
		map.revealTokens();
		JOptionPane.showMessageDialog(null, player[turn].getName()+" "+bundle.getString("wonGame"),bundle.getString("victory"),
				JOptionPane.INFORMATION_MESSAGE,Token.STAR_OF_AFRICA.getResizedIcon(100));
		// If the screen is too small to display the options in the main window, we'll have to do it by popup.
		if (screenSize <= 2) {
			showPopUpQuestion();
		}
	}

	/**
	 *  Announces that the game can no longer be won
	 *  
	 *  @param str	a plaintext string to be displayed, explaining the reason that the game can not be won
	 */
	private void gameUnwinnable(String str) {
		gameOver = true;
		rollDice.setEnabled(false);
		//rollToTurn.setEnabled(false);
		buyToken.setEnabled(false);
		boardPlane.setEnabled(false);
		boardShip.setEnabled(false);
		endTurn.setEnabled(false);
		if (screenSize > 2) {
			quit.setVisible(true);
			newGame.setVisible(true);
			same.setVisible(true);
			same.setSelected(true);
		}
		map.revealTokens();
		JOptionPane.showMessageDialog(null, bundle.getString("unwinnable")+"\n"+bundle.getString("reason")+": "+str,
				bundle.getString("gameOver"),JOptionPane.INFORMATION_MESSAGE,Token.ROBBER.getResizedIcon(100));
		if (screenSize <= 2) {
			showPopUpQuestion();
		}
	}
	
	/**
	 * Shows a popup question asking whether the players want to start a new game or not.
	 */
	private void showPopUpQuestion() {
		Object[] options = {bundle.getString("yesSame"),bundle.getString("yesDiff"),bundle.getString("no")};
		int n = JOptionPane.showOptionDialog(frame,bundle.getString("startNew"),bundle.getString("newGame"),
				JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[2]);
		if (n == JOptionPane.YES_OPTION) {
			same.setSelected(true);
			newGame.doClick();
		}
		else if (n == JOptionPane.NO_OPTION) {
			same.setSelected(false);
			newGame.doClick();
		}
		else {
			quit.doClick();
		}
	}

	/**
	 * Initializes the tokens. Creates the correct number of each token,
	 * shuffles them, and places them.
	 */
	private void setupTokens() {
		// Initialize the tokens
		for (int i=0; i<tokens.length; i++) {
			if (i<12) {
				tokens[i] = Token.BLANK;
			}
			else if (i<17) {
				tokens[i] = Token.HORSESHOE;
			}
			else if (i<20) {
				tokens[i] = Token.ROBBER;
			}
			else if (i<24) {
				tokens[i] = Token.TOPAZ;
			}
			else if (i<27) {
				tokens[i] = Token.EMERALD;
			}
			else if (i<29) {
				tokens[i] = Token.RUBY;
			}
			else {
				tokens[i] = Token.STAR_OF_AFRICA;
			}
		}

		// Shuffle the tokens
		Token temp;
		int rand;
		for (int i=0; i<tokens.length; i++) {
			rand = (int)(Math.random() * tokens.length);
			temp = tokens[i];
			tokens[i] = tokens[rand];
			tokens[rand] = temp;
		}

		// Place the tokens on the cities
		int i=0;
		for (Place p : cities) {
			p.setToken(tokens[i]);
			i++;
		}
	}

	/**
	 * Initializes all places where a player's gamepiece can possibly land,
	 * and the connections between these places.
	 */
	private void setupPlaces() {
		// Starting places
		Place Tangier = new Place(430,300,false,true);
		Tangier.setName("Tangier");
		Place Cairo = new Place(1178,469,false,true);
		Cairo.setName("Cairo");

		// Cities
		Place Morocco = new Place(326,487,true,false);
		Morocco.setName("Morocco");
		Place CanaryIslands = new Place(146,469,true,false);
		CanaryIslands.setName("Canary Islands");
		Place Egypt = new Place(1122,748,true,false);
		Egypt.setName("Egypt");
		Place Tunis = new Place(756,394,true,false);
		Tunis.setName("Tunis");
		Place Tripoli = new Place(831,515,true,false);
		Tripoli.setName("Tripoli");
		Place Sahara = new Place(606,648,true,false);
		Sahara.setName("Sahara");
		Place Suakin = new Place(1346,848,true,false);
		Suakin.setName("Suakin");
		Place DarFur = new Place(1044,986,true,false);
		DarFur.setName("Dar-Fur");
		Place AinGalaka = new Place(822,858,true,false);
		AinGalaka.setName("Ain-Galaka");
		Place Timbuktu = new Place(439,831,true,false);
		Timbuktu.setName("Timbuktu");
		Place CapeVerde = new Place(64,858,true,false);
		CapeVerde.setName("Cape Verde");
		Place SierraLeone = new Place(152,1041,true,false);
		SierraLeone.setName("Sierra Leone");
		Place GoldCoast = new Place(404,1120,true,false);
		GoldCoast.setName("Gold Coast");
		Place SlaveCoast = new Place(598,1140,true,false);
		SlaveCoast.setName("Slave Coast");
		Place Kandjama = new Place(778,1258,true,false);
		Kandjama.setName("Kandjama");
		Place BahrElGhasal = new Place(1138,1130,true,false);
		BahrElGhasal.setName("Bahr El Ghasal");
		Place LakeVictoria = new Place(1265,1263,true,false);
		LakeVictoria.setName("Lake Victoria");
		Place AddisAbeba = new Place(1403,1096,true,false);
		AddisAbeba.setName("Addis Abeba");
		Place CapeGuardafui = new Place(1662,1050,true,false);
		CapeGuardafui.setName("Cape Guardafui");
		Place Daressalam = new Place(1370,1438,true,false);
		Daressalam.setName("Daressalam");
		Place Mozambique = new Place(1384,1658,true,false);
		Mozambique.setName("Mozambique");
		Place Ocomba = new Place(1022,1456,true,false);
		Ocomba.setName("Ocomba");
		Place Congo = new Place(746,1510,true,false);
		Congo.setName("Congo");
		Place WhalefishBay = new Place(768,1830,true,false);
		WhalefishBay.setName("Whalefish Bay");
		Place StHelena = new Place(303,1618,true,false);
		StHelena.setName("St. Helena");
		Place VictoriaFalls = new Place(1056,1748,true,false);
		VictoriaFalls.setName("Victoria Falls");
		Place DragonMountain = new Place(1155,1908,true,false);
		DragonMountain.setName("Dragon Mountain");
		Place Tamatave = new Place(1601,1738,true,false);
		Tamatave.setName("Tamatave");
		Place CapeStMarie = new Place(1447,1952,true,false);
		CapeStMarie.setName("Cape St. Marie");
		Place Capetown = new Place(858,2114,true,false);
		Capetown.setName("Capetown");

		// Others
		Place Tangier_Morocco = new Place(406,468,false,false);
		Place Tangier_Tunis_land_1 = new Place(464,444,false,false);
		Place Tangier_Tunis_land_2 = new Place(519,476,false,false);
		Place Tangier_Tunis_land_3 = new Place(594,476,false,false);
		Place Tangier_Tunis_land_4 = new Place(658,440,false,false);
		Place Tangier_Tunis_sea_1 = new Place(574,350,false,false);
		Place Tangier_Tunis_sea_2 = new Place(645,352,false,false);
		Place Tangier_CanaryIsland_1 = new Place(286,390,false,false);
		Place Tangier_CanaryIsland_2 = new Place(224,410,false,false);
		Place TangierX_Sahara_1 = new Place(444,496,false,false);
		Place TangierX_Sahara_2 = new Place(490,535,false,false);
		Place TangierX_Sahara_3 = new Place(522,598,false,false);
		Place Morocco_CapeVerde_1 = new Place(404,561,false,false);
		Place Morocco_CapeVerde_2 = new Place(402,629,false,false);
		Place Morocco_CapeVerde_3 = new Place(354,682,false,false);
		Place Morocco_CapeVerde_4 = new Place(306,734,false,false);
		Place Morocco_CapeVerde_5 = new Place(256,777,false,false);
		Place Morocco_CapeVerde_6 = new Place(190,764,false,false);
		Place Morocco_CapeVerde_7 = new Place(136,792,false,false);
		Place CanaryIsland_CapeVerde_1 = new Place(63,536,false,false);
		Place CanaryIsland_CapeVerde_2 = new Place(40,610,false,false);
		Place CanaryIsland_CapeVerde_3 = new Place(33,692,false,false);
		Place CanaryIsland_CapeVerde_4 = new Place(39,756,false,false);
		Place CapeVerde_SierraLeone_land_1 = new Place(166,882,false,false);
		Place CapeVerde_SierraLeone_land_2 = new Place(226,920,false,false);
		Place CapeVerde_SierraLeone_land_3 = new Place(230,984,false,false);
		Place CapeVerde_SierraLeone_sea_1 = new Place(38,954,false,false);
		Place CapeVerde_SierraLeone_sea_2 = new Place(72,994,false,false);
		Place CapeVerdeX_StHelena_1 = new Place(38,1012,false,false);
		Place CapeVerdeX_StHelena_2 = new Place(46,1086,false,false);
		Place CapeVerdeX_StHelena_3 = new Place(58,1156,false,false);
		Place CapeVerdeX_StHelena_4 = new Place(77,1230,false,false);
		Place CapeVerdeX_StHelena_5 = new Place(102,1306,false,false);
		Place CapeVerdeX_StHelena_6 = new Place(136,1378,false,false);
		Place CapeVerdeX_StHelena_7 = new Place(170,1438,false,false);
		Place CapeVerdeX_StHelena_8 = new Place(218,1511,false,false);
		CapeVerdeX_StHelena_8.setHostile();
		Place SierraLeone_Timbuktu_1 = new Place(249,1033,false,false);
		Place SierraLeone_Timbuktu_2 = new Place(306,993,false,false);
		Place SierraLeone_Timbuktu_3 = new Place(360,970,false,false);
		Place SierraLeone_Timbuktu_4 = new Place(410,926,false,false);
		Place SierraLeoneX_GoldCoast = new Place(366,1032,false,false);
		Place SierraLeone_GoldCoast_sea_1 = new Place(162,1134,false,false);
		Place SierraLeone_GoldCoast_sea_2 = new Place(209,1190,false,false);
		Place SierraLeone_GoldCoast_sea_3 = new Place(268,1208,false,false);
		Place SierraLeone_GoldCoast_sea_4 = new Place(338,1202,false,false);
		Place Timbuktu_SlaveCoast_1 = new Place (520,889,false,false);
		Place Timbuktu_SlaveCoast_2 = new Place (562,936,false,false);
		Place Timbuktu_SlaveCoast_3 = new Place (594,993,false,false);
		Place Timbuktu_SlaveCoast_4 = new Place (591,1052,false,false);
		Place GoldCoast_SlaveCoast_1 = new Place (455,1213,false,false);
		Place GoldCoast_SlaveCoast_2 = new Place (538,1250,false,false);
		Place GoldCoast_SlaveCoast_3 = new Place (596,1244,false,false);
		Place SlaveCoastX_Congo_1 = new Place (604,1311,false,false);
		Place SlaveCoastX_Congo_2 = new Place (626,1384,false,false);
		Place SlaveCoastX_Congo_3 = new Place (662,1447,false,false);
		Place SlaveCoast_DarFur_1 = new Place (685,1098,false,false);
		Place SlaveCoast_DarFur_2 = new Place (748,1100,false,false);
		Place SlaveCoast_DarFur_3 = new Place (808,1092,false,false);
		Place SlaveCoast_DarFur_4 = new Place (859,1064,false,false);
		Place SlaveCoast_DarFur_5 = new Place (910,1042,false,false);
		Place SlaveCoast_DarFur_6 = new Place (966,1032,false,false);
		Place SlaveCoastX_AinGalaka_1 = new Place (789,1040,false,false);
		Place SlaveCoastX_AinGalaka_2 = new Place (778,986,false,false);
		Place SlaveCoastX_AinGalaka_3 = new Place (786,934,false,false);
		Place SlaveCoastX_Kandjama = new Place (796,1159,false,false);
		Place Tunis_Tripoli_1 = new Place (726,480,false,false);
		Place Tunis_Tripoli_2 = new Place (750,527,false,false);
		Place Tripoli_Egypt_1 = new Place (825,625,false,false);
		Place Tripoli_Egypt_2 = new Place (875,667,false,false);
		Place Tripoli_Egypt_3 = new Place (939,670,false,false);
		Place Tripoli_Egypt_4 = new Place (1004,662,false,false);
		Place Tripoli_Egypt_5 = new Place (1064,684,false,false);
		Place Cairo_Egypt_1 = new Place (1178,588,false,false);
		Place Cairo_Egypt_2 = new Place (1143,622,false,false);
		Place Cairo_Egypt_3 = new Place (1130,672,false,false);
		Place Egypt_DarFur_1 = new Place (1122,844,false,false);
		Place Egypt_DarFur_2 = new Place (1090,896,false,false);
		Place Sahara_DarFur_1 = new Place (704,676,false,false);
		Place Sahara_DarFur_2 = new Place (782,700,false,false);
		Sahara_DarFur_2.setHostile();
		Place Sahara_DarFur_3 = new Place (834,738,false,false);
		Place Sahara_DarFur_4 = new Place (875,773,false,false);
		Place Sahara_DarFur_5 = new Place (920,812,false,false);
		Place Sahara_DarFur_6 = new Place (962,856,false,false);
		Place Sahara_DarFur_7 = new Place (985,918,false,false);
		Place Cairo_Tunis_1 = new Place (1020,453,false,false);
		Place Cairo_Tunis_2 = new Place (958,426,false,false);
		Place Cairo_Tunis_3 = new Place (893,416,false,false);
		Place Cairo_Tunis_4 = new Place (837,410,false,false);
		Place Cairo_Suakin_1 = new Place (1264,606,false,false);
		Place Cairo_Suakin_2 = new Place (1308,663,false,false);
		Place Cairo_Suakin_3 = new Place (1346,730,false,false);
		Place DarFur_AinGalaka_1 = new Place (964,974,false,false);
		Place DarFur_AinGalaka_2 = new Place (919,942,false,false);
		Place DarFur_AinGalaka_3 = new Place (890,898,false,false);
		Place DarFur_BahrElGhasal = new Place (1116,1038,false,false);
		Place DarFur_Suakin_1 = new Place (1146,978,false,false);
		Place DarFur_Suakin_2 = new Place (1215,960,false,false);
		Place DarFur_Suakin_3 = new Place (1278,928,false,false);
		Place Suakin_AddisAbeba_1 = new Place (1358,941,false,false);
		Place Suakin_AddisAbeba_2 = new Place (1377,1000,false,false);
		Place AddisAbeba_CapeGuardafui_1 = new Place (1502,1088,false,false);
		Place AddisAbeba_CapeGuardafui_2 = new Place (1568,1090,false,false);
		Place Suakin_CapeGuardafui_1 = new Place (1436,872,false,false);
		Place Suakin_CapeGuardafui_2 = new Place (1478,938,false,false);
		Place Suakin_CapeGuardafui_3 = new Place (1516,992,false,false);
		Place Suakin_CapeGuardafui_4 = new Place (1576,1010,false,false);
		Place CapeGuardafui_Tamatave_1 = new Place (1702,1162,false,false);
		Place CapeGuardafui_Tamatave_2 = new Place (1704,1238,false,false);
		Place CapeGuardafui_Tamatave_3 = new Place (1702,1316,false,false);
		Place CapeGuardafui_Tamatave_4 = new Place (1701,1390,false,false);
		Place CapeGuardafui_Tamatave_5 = new Place (1694,1472,false,false);
		Place CapeGuardafui_Tamatave_6 = new Place (1685,1557,false,false);
		Place CapeGuardafui_Tamatave_7 = new Place (1664,1644,false,false);
		Place CapeGuardafui_Mozambique_1 = new Place (1676,1170,false,false);
		Place CapeGuardafui_Mozambique_2 = new Place (1656,1248,false,false);
		Place CapeGuardafui_Mozambique_3 = new Place (1630,1325,false,false);
		Place CapeGuardafui_Mozambique_4 = new Place (1604,1388,false,false);
		Place CapeGuardafui_Mozambique_5 = new Place (1564,1458,false,false);
		Place CapeGuardafui_Mozambique_6 = new Place (1522,1522,false,false);
		Place CapeGuardafui_Mozambique_7 = new Place (1472,1582,false,false);
		Place CapeGuardafui_Daressalam_1 = new Place (1600,1143,false,false);
		Place CapeGuardafui_Daressalam_2 = new Place (1548,1190,false,false);
		Place CapeGuardafui_Daressalam_3 = new Place (1485,1230,false,false);
		Place CapeGuardafui_Daressalam_4 = new Place (1430,1278,false,false);
		Place CapeGuardafui_Daressalam_5 = new Place (1396,1338,false,false);
		Place BahrElGhasal_LakeVictoria = new Place (1184,1210,false,false);
		Place AddisAbeba_LakeVictoria_1 = new Place (1374,1170,false,false);
		Place AddisAbeba_LakeVictoria_2 = new Place (1322,1196,false,false);
		Place LakeVictoria_Mozambique_1 = new Place (1309,1347,false,false);
		Place LakeVictoria_Mozambique_2 = new Place (1290,1415,false,false);
		Place LakeVictoria_Mozambique_3 = new Place (1284,1488,false,false);
		Place LakeVictoria_Mozambique_4 = new Place (1338,1530,false,false);
		Place LakeVictoria_Mozambique_5 = new Place (1366,1573,false,false);
		Place Kandjama_Congo_1 = new Place (785,1360,false,false);
		Place Kandjama_Congo_2 = new Place (793,1426,false,false);
		Place Congo_Ocomba_1 = new Place (830,1546,false,false);
		Place Congo_Ocomba_2 = new Place (890,1532,false,false);
		Place Congo_Ocomba_3 = new Place (937,1485,false,false);
		Place LakeVictoria_Ocomba_1 = new Place (1174,1291,false,false);
		Place LakeVictoria_Ocomba_2 = new Place (1132,1339,false,false);
		Place LakeVictoria_Ocomba_3 = new Place (1106,1394,false,false);
		Place Mozambique_DragonMountain_1 = new Place (1290,1686,false,false);
		Place Mozambique_DragonMountain_2 = new Place (1250,1734,false,false);
		Place Mozambique_DragonMountain_3 = new Place (1218,1772,false,false);
		Place Mozambique_DragonMountain_4 = new Place (1194,1826,false,false);
		Place MozambiqueX_VictoriaFalls = new Place (1159,1739,false,false);
		Place VictoriaFalls_DragonMountain_1 = new Place (1064,1848,false,false);
		Place VictoriaFalls_DragonMountain_2 = new Place (1060,1922,false,false);
		Place MozambiqueX_Congo_1 = new Place (1184,1706,false,false);
		Place MozambiqueX_Congo_2 = new Place (1127,1669,false,false);
		Place MozambiqueX_Congo_3 = new Place (1069,1634,false,false);
		Place MozambiqueX_Congo_4 = new Place (1004,1608,false,false);
		Place MozambiqueX_Congo_5 = new Place (932,1605,false,false);
		Place MozambiqueX_Congo_6 = new Place (868,1606,false,false);
		Place MozambiqueX_Congo_7 = new Place (799,1588,false,false);
		Place Congo_WhalefishBay_1 = new Place (676,1575,false,false);
		Place Congo_WhalefishBay_2 = new Place (648,1646,false,false);
		Place Congo_WhalefishBay_3 = new Place (647,1706,false,false);
		Place Congo_WhalefishBay_4 = new Place (672,1774,false,false);
		Place WhalefishBay_VictoriaFalls_1 = new Place (844,1780,false,false);
		Place WhalefishBay_VictoriaFalls_2 = new Place (904,1766,false,false);
		Place WhalefishBay_VictoriaFalls_3 = new Place (966,1772,false,false);
		Place WhalefishBay_Capetown_land_1 = new Place (848,1885,false,false);
		Place WhalefishBay_Capetown_land_2 = new Place (860,1946,false,false);
		Place WhalefishBay_Capetown_land_3 = new Place (840,2006,false,false);
		Place WhalefishBay_Capetown_sea_1 = new Place (702,1912,false,false);
		Place WhalefishBay_Capetown_sea_2 = new Place (694,1984,false,false);
		Place WhalefishBay_Capetown_sea_3 = new Place (718,2044,false,false);
		Place WhalefishBay_Capetown_sea_4 = new Place (764,2084,false,false);
		Place Mozambique_CapeStMarie_1 = new Place (1380,1778,false,false);
		Place Mozambique_CapeStMarie_2 = new Place (1374,1863,false,false);
		Place Tamatave_CapeStMarie_1 = new Place (1510,1766,false,false);
		Place Tamatave_CapeStMarie_2 = new Place (1511,1826,false,false);
		Place Tamatave_CapeStMarie_3 = new Place (1500,1881,false,false);
		Place Capetown_CapeStMarie_1 = new Place (962,2146,false,false);
		Place Capetown_CapeStMarie_2 = new Place (1030,2150,false,false);
		Place Capetown_CapeStMarie_3 = new Place (1096,2147,false,false);
		Place Capetown_CapeStMarie_4 = new Place (1162,2134,false,false);
		Place Capetown_CapeStMarie_5 = new Place (1230,2106,false,false);
		Place Capetown_CapeStMarie_6 = new Place (1296,2070,false,false);
		Place Capetown_CapeStMarie_7 = new Place (1358,2028,false,false);
		Place StHelena_CapetownX_1 = new Place (327,1724,false,false);
		StHelena_CapetownX_1.setHostile();
		Place StHelena_CapetownX_2 = new Place (342,1797,false,false);
		Place StHelena_CapetownX_3 = new Place (364,1866,false,false);
		Place StHelena_CapetownX_4 = new Place (394,1938,false,false);
		Place StHelena_CapetownX_5 = new Place (446,2002,false,false);
		Place StHelena_CapetownX_6 = new Place (534,2050,false,false);
		Place StHelena_CapetownX_7 = new Place (636,2059,false,false);

		// Setting the connections between the places

		// Tangier
		Tangier.setConnectedByAir(Morocco);
		Tangier.setConnectedByAir(Tripoli);
		Tangier.setConnectedBySea(Tangier_CanaryIsland_1);
		Tangier.setConnectedBySea(Tangier_Tunis_sea_1);
		Tangier.setConnectedByLand(Tangier_Morocco);
		Tangier.setConnectedByLand(Tangier_Tunis_land_1);
		places.add(Tangier);
		startingPoints[0] = Tangier;

		// Cairo
		Cairo.setConnectedByAir(Suakin);
		Cairo.setConnectedBySea(Cairo_Suakin_1);
		Cairo.setConnectedBySea(Cairo_Tunis_1);
		Cairo.setConnectedByLand(Cairo_Egypt_1);
		places.add(Cairo);
		startingPoints[1] = Cairo;

		// Tunis
		Tunis.setConnectedBySea(Tangier_Tunis_sea_2);
		Tunis.setConnectedBySea(Cairo_Tunis_4);
		Tunis.setConnectedByLand(Tangier_Tunis_land_4);
		Tunis.setConnectedByLand(Tunis_Tripoli_1);
		places.add(Tunis);
		cities.add(Tunis);

		// Tripoli
		Tripoli.setConnectedByAir(Tangier);
		Tripoli.setConnectedByAir(DarFur);
		Tripoli.setConnectedByAir(GoldCoast);
		Tripoli.setConnectedBySea(Cairo_Tunis_3);
		Tripoli.setConnectedByLand(Tunis_Tripoli_2);
		Tripoli.setConnectedByLand(Tripoli_Egypt_1);
		places.add(Tripoli);
		cities.add(Tripoli);

		// Canary Islands
		CanaryIslands.setConnectedBySea(Tangier_CanaryIsland_2);
		CanaryIslands.setConnectedBySea(CanaryIsland_CapeVerde_1);
		places.add(CanaryIslands);
		cities.add(CanaryIslands);

		// Morocco
		Morocco.setConnectedByAir(Tangier);
		Morocco.setConnectedByAir(SierraLeone);
		Morocco.setConnectedByAir(GoldCoast);
		Morocco.setConnectedByLand(Tangier_Morocco);
		Morocco.setConnectedByLand(Morocco_CapeVerde_1);
		places.add(Morocco);
		cities.add(Morocco);

		// Sahara
		Sahara.setConnectedByLand(TangierX_Sahara_3);
		Sahara.setConnectedByLand(Sahara_DarFur_1);
		places.add(Sahara);
		cities.add(Sahara);

		// Egypt
		Egypt.setConnectedByLand(Cairo_Egypt_3);
		Egypt.setConnectedByLand(Tripoli_Egypt_5);
		Egypt.setConnectedByLand(Egypt_DarFur_1);
		places.add(Egypt);
		cities.add(Egypt);

		// Suakin
		Suakin.setConnectedByAir(Cairo);
		Suakin.setConnectedByAir(DarFur);
		Suakin.setConnectedByAir(LakeVictoria);
		Suakin.setConnectedBySea(Cairo_Suakin_3);
		Suakin.setConnectedBySea(Suakin_CapeGuardafui_1);
		Suakin.setConnectedByLand(DarFur_Suakin_3);
		Suakin.setConnectedByLand(Suakin_AddisAbeba_1);
		places.add(Suakin);		
		cities.add(Suakin);

		// Cape Guardafui
		CapeGuardafui.setConnectedByAir(LakeVictoria);
		CapeGuardafui.setConnectedByAir(Tamatave);
		CapeGuardafui.setConnectedBySea(Suakin_CapeGuardafui_4);
		CapeGuardafui.setConnectedBySea(CapeGuardafui_Mozambique_1);
		CapeGuardafui.setConnectedBySea(CapeGuardafui_Tamatave_1);
		CapeGuardafui.setConnectedByLand(AddisAbeba_CapeGuardafui_2);
		CapeGuardafui.setConnectedByLand(CapeGuardafui_Daressalam_1);
		places.add(CapeGuardafui);
		cities.add(CapeGuardafui);

		// Addis Abeba
		AddisAbeba.setConnectedByLand(Suakin_AddisAbeba_2);
		AddisAbeba.setConnectedByLand(AddisAbeba_CapeGuardafui_1);
		AddisAbeba.setConnectedByLand(AddisAbeba_LakeVictoria_1);
		places.add(AddisAbeba);
		cities.add(AddisAbeba);

		// Dar-Fur
		DarFur.setConnectedByAir(Tripoli);
		DarFur.setConnectedByAir(Suakin);
		DarFur.setConnectedByAir(Ocomba);
		DarFur.setConnectedByLand(Egypt_DarFur_2);
		DarFur.setConnectedByLand(DarFur_AinGalaka_1);
		DarFur.setConnectedByLand(DarFur_Suakin_1);
		DarFur.setConnectedByLand(DarFur_BahrElGhasal);
		DarFur.setConnectedByLand(Sahara_DarFur_7);
		DarFur.setConnectedByLand(SlaveCoast_DarFur_6);
		places.add(DarFur);
		cities.add(DarFur);

		// Bahr El Ghasal
		BahrElGhasal.setConnectedByLand(DarFur_BahrElGhasal);
		BahrElGhasal.setConnectedByLand(BahrElGhasal_LakeVictoria);
		places.add(BahrElGhasal);
		cities.add(BahrElGhasal);

		// Lake Victoria
		LakeVictoria.setConnectedByAir(Suakin);
		LakeVictoria.setConnectedByAir(CapeGuardafui);
		LakeVictoria.setConnectedByAir(DragonMountain);
		LakeVictoria.setConnectedByLand(BahrElGhasal_LakeVictoria);
		LakeVictoria.setConnectedByLand(AddisAbeba_LakeVictoria_2);
		LakeVictoria.setConnectedByLand(LakeVictoria_Mozambique_1);
		LakeVictoria.setConnectedByLand(LakeVictoria_Ocomba_1);
		places.add(LakeVictoria);
		cities.add(LakeVictoria);

		// Ain-Galaka
		AinGalaka.setConnectedByLand(DarFur_AinGalaka_3);
		AinGalaka.setConnectedByLand(SlaveCoastX_AinGalaka_3);
		places.add(AinGalaka);
		cities.add(AinGalaka);

		// Timbuktu
		Timbuktu.setConnectedByLand(SierraLeone_Timbuktu_4);
		Timbuktu.setConnectedByLand(Timbuktu_SlaveCoast_1);
		places.add(Timbuktu);
		cities.add(Timbuktu);

		// Cape Verde
		CapeVerde.setConnectedBySea(CanaryIsland_CapeVerde_4);
		CapeVerde.setConnectedBySea(CapeVerde_SierraLeone_sea_1);
		CapeVerde.setConnectedByLand(Morocco_CapeVerde_7);
		CapeVerde.setConnectedByLand(CapeVerde_SierraLeone_land_1);
		places.add(CapeVerde);
		cities.add(CapeVerde);

		// Sierra Leone
		SierraLeone.setConnectedByAir(Morocco);
		SierraLeone.setConnectedByAir(StHelena);
		SierraLeone.setConnectedBySea(CapeVerde_SierraLeone_sea_2);
		SierraLeone.setConnectedBySea(SierraLeone_GoldCoast_sea_1);
		SierraLeone.setConnectedByLand(SierraLeone_Timbuktu_1);
		SierraLeone.setConnectedByLand(CapeVerde_SierraLeone_land_3);
		places.add(SierraLeone);
		cities.add(SierraLeone);

		// Gold Coast
		GoldCoast.setConnectedByAir(Morocco);
		GoldCoast.setConnectedByAir(Tripoli);
		GoldCoast.setConnectedByAir(Congo);
		GoldCoast.setConnectedByAir(WhalefishBay);
		GoldCoast.setConnectedBySea(SierraLeone_GoldCoast_sea_4);
		GoldCoast.setConnectedBySea(GoldCoast_SlaveCoast_1);
		GoldCoast.setConnectedByLand(SierraLeoneX_GoldCoast);
		places.add(GoldCoast);
		cities.add(GoldCoast);

		// Slave Coast
		SlaveCoast.setConnectedBySea(GoldCoast_SlaveCoast_3);
		SlaveCoast.setConnectedByLand(Timbuktu_SlaveCoast_4);
		SlaveCoast.setConnectedByLand(SlaveCoast_DarFur_1);
		places.add(SlaveCoast);
		cities.add(SlaveCoast);

		// Kandjama
		Kandjama.setConnectedByLand(SlaveCoastX_Kandjama);
		Kandjama.setConnectedByLand(Kandjama_Congo_1);
		places.add(Kandjama);
		cities.add(Kandjama);

		// Congo
		Congo.setConnectedByAir(GoldCoast);
		Congo.setConnectedByAir(WhalefishBay);
		Congo.setConnectedBySea(SlaveCoastX_Congo_3);
		Congo.setConnectedBySea(Congo_WhalefishBay_1);
		Congo.setConnectedByLand(Kandjama_Congo_2);
		Congo.setConnectedByLand(Congo_Ocomba_1);
		Congo.setConnectedByLand(MozambiqueX_Congo_7);
		places.add(Congo);
		cities.add(Congo);

		// Ocomba
		Ocomba.setConnectedByAir(DarFur);
		Ocomba.setConnectedByAir(Capetown);
		Ocomba.setConnectedByLand(Congo_Ocomba_3);
		Ocomba.setConnectedByLand(LakeVictoria_Ocomba_3);
		places.add(Ocomba);
		cities.add(Ocomba);

		// Daressalam
		Daressalam.setConnectedByLand(LakeVictoria_Mozambique_4);
		Daressalam.setConnectedByLand(CapeGuardafui_Daressalam_5);
		places.add(Daressalam);
		cities.add(Daressalam);

		// Mozambique
		Mozambique.setConnectedBySea(CapeGuardafui_Mozambique_7);
		Mozambique.setConnectedBySea(Mozambique_CapeStMarie_1);
		Mozambique.setConnectedByLand(Mozambique_DragonMountain_1);
		Mozambique.setConnectedByLand(LakeVictoria_Mozambique_5);
		places.add(Mozambique);
		cities.add(Mozambique);

		// Tamatave
		Tamatave.setConnectedByAir(CapeGuardafui);
		Tamatave.setConnectedByAir(Capetown);
		Tamatave.setConnectedBySea(CapeGuardafui_Tamatave_7);
		Tamatave.setConnectedByLand(Tamatave_CapeStMarie_1);
		places.add(Tamatave);
		cities.add(Tamatave);

		// Cape St. Marie
		CapeStMarie.setConnectedByAir(Capetown);
		CapeStMarie.setConnectedBySea(Mozambique_CapeStMarie_2);
		CapeStMarie.setConnectedBySea(Capetown_CapeStMarie_7);
		CapeStMarie.setConnectedByLand(Tamatave_CapeStMarie_3);
		places.add(CapeStMarie);
		cities.add(CapeStMarie);

		// Victoria Falls
		VictoriaFalls.setConnectedByLand(MozambiqueX_VictoriaFalls);
		VictoriaFalls.setConnectedByLand(VictoriaFalls_DragonMountain_1);
		VictoriaFalls.setConnectedByLand(WhalefishBay_VictoriaFalls_3);
		places.add(VictoriaFalls);
		cities.add(VictoriaFalls);

		// Dragon Mountain
		DragonMountain.setConnectedByAir(Capetown);
		DragonMountain.setConnectedByAir(LakeVictoria);
		DragonMountain.setConnectedByLand(VictoriaFalls_DragonMountain_2);
		DragonMountain.setConnectedByLand(Mozambique_DragonMountain_4);
		places.add(DragonMountain);
		cities.add(DragonMountain);

		// Whalefish Bay
		WhalefishBay.setConnectedByAir(Capetown);
		WhalefishBay.setConnectedByAir(Congo);
		WhalefishBay.setConnectedByAir(GoldCoast);
		WhalefishBay.setConnectedBySea(WhalefishBay_Capetown_sea_1);
		WhalefishBay.setConnectedBySea(Congo_WhalefishBay_4);
		WhalefishBay.setConnectedByLand(WhalefishBay_Capetown_land_1);
		WhalefishBay.setConnectedByLand(WhalefishBay_VictoriaFalls_1);
		places.add(WhalefishBay);
		cities.add(WhalefishBay);

		// Capetown
		Capetown.setConnectedByAir(StHelena);
		Capetown.setConnectedByAir(WhalefishBay);
		Capetown.setConnectedByAir(Ocomba);
		Capetown.setConnectedByAir(DragonMountain);
		Capetown.setConnectedByAir(Tamatave);
		Capetown.setConnectedByAir(CapeStMarie);
		Capetown.setConnectedBySea(WhalefishBay_Capetown_sea_4);
		Capetown.setConnectedBySea(Capetown_CapeStMarie_1);
		Capetown.setConnectedByLand(WhalefishBay_Capetown_land_3);
		places.add(Capetown);
		cities.add(Capetown);

		// St. Helena
		StHelena.setConnectedByAir(Capetown);
		StHelena.setConnectedByAir(SierraLeone);
		StHelena.setConnectedBySea(CapeVerdeX_StHelena_8);
		StHelena.setConnectedBySea(StHelena_CapetownX_1);
		places.add(StHelena);
		cities.add(StHelena);


		// Routes
		// Tangier-Tunis sea
		Tangier_Tunis_sea_1.setConnectedBySea(Tangier);
		Tangier_Tunis_sea_1.setConnectedBySea(Tangier_Tunis_sea_2);
		Tangier_Tunis_sea_2.setConnectedBySea(Tangier_Tunis_sea_1);
		Tangier_Tunis_sea_2.setConnectedBySea(Tunis);
		places.add(Tangier_Tunis_sea_1);
		places.add(Tangier_Tunis_sea_2);


		// Tangier-Tunis land
		Tangier_Tunis_land_1.setConnectedByLand(Tangier);
		Tangier_Tunis_land_1.setConnectedByLand(Tangier_Tunis_land_2);
		Tangier_Tunis_land_2.setConnectedByLand(Tangier_Tunis_land_1);
		Tangier_Tunis_land_2.setConnectedByLand(Tangier_Tunis_land_3);
		Tangier_Tunis_land_3.setConnectedByLand(Tangier_Tunis_land_2);
		Tangier_Tunis_land_3.setConnectedByLand(Tangier_Tunis_land_4);
		Tangier_Tunis_land_4.setConnectedByLand(Tangier_Tunis_land_3);
		Tangier_Tunis_land_4.setConnectedByLand(Tunis);
		places.add(Tangier_Tunis_land_1);
		places.add(Tangier_Tunis_land_2);
		places.add(Tangier_Tunis_land_3);
		places.add(Tangier_Tunis_land_4);

		// Tangier-Canary Islands
		Tangier_CanaryIsland_1.setConnectedBySea(Tangier);
		Tangier_CanaryIsland_1.setConnectedBySea(Tangier_CanaryIsland_2);
		Tangier_CanaryIsland_2.setConnectedBySea(Tangier_CanaryIsland_1);
		Tangier_CanaryIsland_2.setConnectedBySea(CanaryIslands);
		places.add(Tangier_CanaryIsland_1);
		places.add(Tangier_CanaryIsland_2);

		// Tangier-Morocco
		Tangier_Morocco.setConnectedByLand(Tangier);
		Tangier_Morocco.setConnectedByLand(Morocco);
		Tangier_Morocco.setConnectedByLand(TangierX_Sahara_1);
		places.add(Tangier_Morocco);

		// Tangier-Sahara
		TangierX_Sahara_1.setConnectedByLand(Tangier_Morocco);
		TangierX_Sahara_1.setConnectedByLand(TangierX_Sahara_2);
		TangierX_Sahara_2.setConnectedByLand(TangierX_Sahara_1);
		TangierX_Sahara_2.setConnectedByLand(TangierX_Sahara_3);
		TangierX_Sahara_3.setConnectedByLand(TangierX_Sahara_2);
		TangierX_Sahara_3.setConnectedByLand(Sahara);
		places.add(TangierX_Sahara_1);
		places.add(TangierX_Sahara_2);
		places.add(TangierX_Sahara_3);


		// Tunis-Tripoli
		Tunis_Tripoli_1.setConnectedByLand(Tunis);
		Tunis_Tripoli_1.setConnectedByLand(Tunis_Tripoli_2);
		Tunis_Tripoli_2.setConnectedByLand(Tunis_Tripoli_1);
		Tunis_Tripoli_2.setConnectedByLand(Tripoli);
		places.add(Tunis_Tripoli_1);
		places.add(Tunis_Tripoli_2);

		// Cairo-Tunis
		Cairo_Tunis_1.setConnectedBySea(Cairo);
		Cairo_Tunis_1.setConnectedBySea(Cairo_Tunis_2);
		Cairo_Tunis_2.setConnectedBySea(Cairo_Tunis_1);
		Cairo_Tunis_2.setConnectedBySea(Cairo_Tunis_3);
		Cairo_Tunis_3.setConnectedBySea(Cairo_Tunis_2);
		Cairo_Tunis_3.setConnectedBySea(Cairo_Tunis_4);
		Cairo_Tunis_3.setConnectedBySea(Tripoli);
		Cairo_Tunis_4.setConnectedBySea(Cairo_Tunis_3);
		Cairo_Tunis_4.setConnectedBySea(Tunis);
		places.add(Cairo_Tunis_1);
		places.add(Cairo_Tunis_2);
		places.add(Cairo_Tunis_3);
		places.add(Cairo_Tunis_4);

		// Tripoli-Egypt
		Tripoli_Egypt_1.setConnectedByLand(Tripoli);
		Tripoli_Egypt_1.setConnectedByLand(Tripoli_Egypt_2);
		Tripoli_Egypt_2.setConnectedByLand(Tripoli_Egypt_1);
		Tripoli_Egypt_2.setConnectedByLand(Tripoli_Egypt_3);
		Tripoli_Egypt_3.setConnectedByLand(Tripoli_Egypt_2);
		Tripoli_Egypt_3.setConnectedByLand(Tripoli_Egypt_4);
		Tripoli_Egypt_4.setConnectedByLand(Tripoli_Egypt_3);
		Tripoli_Egypt_4.setConnectedByLand(Tripoli_Egypt_5);
		Tripoli_Egypt_5.setConnectedByLand(Tripoli_Egypt_4);
		Tripoli_Egypt_5.setConnectedByLand(Egypt);
		places.add(Tripoli_Egypt_1);
		places.add(Tripoli_Egypt_2);
		places.add(Tripoli_Egypt_3);
		places.add(Tripoli_Egypt_4);
		places.add(Tripoli_Egypt_5);

		// Cairo-Egypt
		Cairo_Egypt_1.setConnectedByLand(Cairo);
		Cairo_Egypt_1.setConnectedByLand(Cairo_Egypt_2);
		Cairo_Egypt_2.setConnectedByLand(Cairo_Egypt_1);
		Cairo_Egypt_2.setConnectedByLand(Cairo_Egypt_3);
		Cairo_Egypt_3.setConnectedByLand(Cairo_Egypt_2);
		Cairo_Egypt_3.setConnectedByLand(Egypt);
		places.add(Cairo_Egypt_1);
		places.add(Cairo_Egypt_2);
		places.add(Cairo_Egypt_3);

		// Cairo-Suakin
		Cairo_Suakin_1.setConnectedBySea(Cairo);
		Cairo_Suakin_1.setConnectedBySea(Cairo_Suakin_2);
		Cairo_Suakin_2.setConnectedBySea(Cairo_Suakin_1);
		Cairo_Suakin_2.setConnectedBySea(Cairo_Suakin_3);
		Cairo_Suakin_3.setConnectedBySea(Cairo_Suakin_2);
		Cairo_Suakin_3.setConnectedBySea(Suakin);
		places.add(Cairo_Suakin_1);
		places.add(Cairo_Suakin_2);
		places.add(Cairo_Suakin_3);

		// Egypt-DarFur
		Egypt_DarFur_1.setConnectedByLand(Egypt);
		Egypt_DarFur_1.setConnectedByLand(Egypt_DarFur_2);
		Egypt_DarFur_2.setConnectedByLand(Egypt_DarFur_1);
		Egypt_DarFur_2.setConnectedByLand(DarFur);
		places.add(Egypt_DarFur_1);
		places.add(Egypt_DarFur_2);

		// Sahara-DarFur
		Sahara_DarFur_1.setConnectedByLand(Sahara);
		Sahara_DarFur_1.setConnectedByLand(Sahara_DarFur_2);
		Sahara_DarFur_2.setConnectedByLand(Sahara_DarFur_1);
		Sahara_DarFur_2.setConnectedByLand(Sahara_DarFur_3);
		Sahara_DarFur_3.setConnectedByLand(Sahara_DarFur_2);
		Sahara_DarFur_3.setConnectedByLand(Sahara_DarFur_4);
		Sahara_DarFur_4.setConnectedByLand(Sahara_DarFur_3);
		Sahara_DarFur_4.setConnectedByLand(Sahara_DarFur_5);
		Sahara_DarFur_5.setConnectedByLand(Sahara_DarFur_4);
		Sahara_DarFur_5.setConnectedByLand(Sahara_DarFur_6);
		Sahara_DarFur_6.setConnectedByLand(Sahara_DarFur_5);
		Sahara_DarFur_6.setConnectedByLand(Sahara_DarFur_7);
		Sahara_DarFur_7.setConnectedByLand(Sahara_DarFur_6);
		Sahara_DarFur_7.setConnectedByLand(DarFur);
		places.add(Sahara_DarFur_1);
		places.add(Sahara_DarFur_2);
		places.add(Sahara_DarFur_3);
		places.add(Sahara_DarFur_4);
		places.add(Sahara_DarFur_5);
		places.add(Sahara_DarFur_6);
		places.add(Sahara_DarFur_7);

		// DarFur-AinGalaka
		DarFur_AinGalaka_1.setConnectedByLand(DarFur);
		DarFur_AinGalaka_1.setConnectedByLand(DarFur_AinGalaka_2);
		DarFur_AinGalaka_2.setConnectedByLand(DarFur_AinGalaka_1);
		DarFur_AinGalaka_2.setConnectedByLand(DarFur_AinGalaka_3);
		DarFur_AinGalaka_3.setConnectedByLand(DarFur_AinGalaka_2);
		DarFur_AinGalaka_3.setConnectedByLand(AinGalaka);
		places.add(DarFur_AinGalaka_1);
		places.add(DarFur_AinGalaka_2);
		places.add(DarFur_AinGalaka_3);

		// Slave Coast-DarFur
		SlaveCoast_DarFur_1.setConnectedByLand(SlaveCoast);
		SlaveCoast_DarFur_1.setConnectedByLand(SlaveCoast_DarFur_2);
		SlaveCoast_DarFur_2.setConnectedByLand(SlaveCoast_DarFur_1);
		SlaveCoast_DarFur_2.setConnectedByLand(SlaveCoast_DarFur_3);
		SlaveCoast_DarFur_3.setConnectedByLand(SlaveCoast_DarFur_2);
		SlaveCoast_DarFur_3.setConnectedByLand(SlaveCoast_DarFur_4);
		SlaveCoast_DarFur_3.setConnectedByLand(SlaveCoastX_AinGalaka_1);
		SlaveCoast_DarFur_3.setConnectedByLand(SlaveCoastX_Kandjama);
		SlaveCoast_DarFur_4.setConnectedByLand(SlaveCoast_DarFur_3);
		SlaveCoast_DarFur_4.setConnectedByLand(SlaveCoast_DarFur_5);
		SlaveCoast_DarFur_5.setConnectedByLand(SlaveCoast_DarFur_4);
		SlaveCoast_DarFur_5.setConnectedByLand(SlaveCoast_DarFur_6);
		SlaveCoast_DarFur_6.setConnectedByLand(SlaveCoast_DarFur_5);
		SlaveCoast_DarFur_6.setConnectedByLand(DarFur);
		places.add(SlaveCoast_DarFur_1);
		places.add(SlaveCoast_DarFur_2);
		places.add(SlaveCoast_DarFur_3);
		places.add(SlaveCoast_DarFur_4);
		places.add(SlaveCoast_DarFur_5);
		places.add(SlaveCoast_DarFur_6);

		// Slave Coast-AinGalaka
		SlaveCoastX_AinGalaka_1.setConnectedByLand(SlaveCoastX_AinGalaka_2);
		SlaveCoastX_AinGalaka_1.setConnectedByLand(SlaveCoast_DarFur_3);
		SlaveCoastX_AinGalaka_2.setConnectedByLand(SlaveCoastX_AinGalaka_1);
		SlaveCoastX_AinGalaka_2.setConnectedByLand(SlaveCoastX_AinGalaka_3);
		SlaveCoastX_AinGalaka_3.setConnectedByLand(SlaveCoastX_AinGalaka_2);
		SlaveCoastX_AinGalaka_3.setConnectedByLand(AinGalaka);
		places.add(SlaveCoastX_AinGalaka_1);
		places.add(SlaveCoastX_AinGalaka_2);
		places.add(SlaveCoastX_AinGalaka_3);

		// Slave Coast-Kandjama
		SlaveCoastX_Kandjama.setConnectedByLand(SlaveCoast_DarFur_3);
		SlaveCoastX_Kandjama.setConnectedByLand(Kandjama);
		places.add(SlaveCoastX_Kandjama);

		// Canary Islands-Cape Verde
		CanaryIsland_CapeVerde_1.setConnectedBySea(CanaryIslands);
		CanaryIsland_CapeVerde_1.setConnectedBySea(CanaryIsland_CapeVerde_2);
		CanaryIsland_CapeVerde_2.setConnectedBySea(CanaryIsland_CapeVerde_1);
		CanaryIsland_CapeVerde_2.setConnectedBySea(CanaryIsland_CapeVerde_3);
		CanaryIsland_CapeVerde_3.setConnectedBySea(CanaryIsland_CapeVerde_2);
		CanaryIsland_CapeVerde_3.setConnectedBySea(CanaryIsland_CapeVerde_4);
		CanaryIsland_CapeVerde_4.setConnectedBySea(CanaryIsland_CapeVerde_3);
		CanaryIsland_CapeVerde_4.setConnectedBySea(CapeVerde);
		places.add(CanaryIsland_CapeVerde_1);
		places.add(CanaryIsland_CapeVerde_2);
		places.add(CanaryIsland_CapeVerde_3);
		places.add(CanaryIsland_CapeVerde_4);

		// Morocco-Cape Verde
		Morocco_CapeVerde_1.setConnectedByLand(Morocco);
		Morocco_CapeVerde_1.setConnectedByLand(Morocco_CapeVerde_2);
		Morocco_CapeVerde_2.setConnectedByLand(Morocco_CapeVerde_1);
		Morocco_CapeVerde_2.setConnectedByLand(Morocco_CapeVerde_3);
		Morocco_CapeVerde_3.setConnectedByLand(Morocco_CapeVerde_2);
		Morocco_CapeVerde_3.setConnectedByLand(Morocco_CapeVerde_4);
		Morocco_CapeVerde_4.setConnectedByLand(Morocco_CapeVerde_3);
		Morocco_CapeVerde_4.setConnectedByLand(Morocco_CapeVerde_5);
		Morocco_CapeVerde_5.setConnectedByLand(Morocco_CapeVerde_4);
		Morocco_CapeVerde_5.setConnectedByLand(Morocco_CapeVerde_6);
		Morocco_CapeVerde_6.setConnectedByLand(Morocco_CapeVerde_5);
		Morocco_CapeVerde_6.setConnectedByLand(Morocco_CapeVerde_7);
		Morocco_CapeVerde_7.setConnectedByLand(Morocco_CapeVerde_6);
		Morocco_CapeVerde_7.setConnectedByLand(CapeVerde);
		places.add(Morocco_CapeVerde_1);
		places.add(Morocco_CapeVerde_2);
		places.add(Morocco_CapeVerde_3);
		places.add(Morocco_CapeVerde_4);
		places.add(Morocco_CapeVerde_5);
		places.add(Morocco_CapeVerde_6);
		places.add(Morocco_CapeVerde_7);

		// Cape Verde-Sierra Leone land
		CapeVerde_SierraLeone_land_1.setConnectedByLand(CapeVerde);
		CapeVerde_SierraLeone_land_1.setConnectedByLand(CapeVerde_SierraLeone_land_2);
		CapeVerde_SierraLeone_land_2.setConnectedByLand(CapeVerde_SierraLeone_land_1);
		CapeVerde_SierraLeone_land_2.setConnectedByLand(CapeVerde_SierraLeone_land_3);
		CapeVerde_SierraLeone_land_3.setConnectedByLand(CapeVerde_SierraLeone_land_2);
		CapeVerde_SierraLeone_land_3.setConnectedByLand(SierraLeone);
		places.add(CapeVerde_SierraLeone_land_1);
		places.add(CapeVerde_SierraLeone_land_2);
		places.add(CapeVerde_SierraLeone_land_3);

		// Cape Verde-Sierra Leone sea
		CapeVerde_SierraLeone_sea_1.setConnectedBySea(CapeVerde);
		CapeVerde_SierraLeone_sea_1.setConnectedBySea(CapeVerde_SierraLeone_sea_2);
		CapeVerde_SierraLeone_sea_1.setConnectedBySea(CapeVerdeX_StHelena_1);
		CapeVerde_SierraLeone_sea_2.setConnectedBySea(CapeVerde_SierraLeone_sea_1);
		CapeVerde_SierraLeone_sea_2.setConnectedBySea(SierraLeone);
		places.add(CapeVerde_SierraLeone_sea_1);
		places.add(CapeVerde_SierraLeone_sea_2);

		// Cape Verde-St. Helena
		CapeVerdeX_StHelena_1.setConnectedBySea(CapeVerde_SierraLeone_sea_1);
		CapeVerdeX_StHelena_1.setConnectedBySea(CapeVerdeX_StHelena_2);
		CapeVerdeX_StHelena_2.setConnectedBySea(CapeVerdeX_StHelena_1);
		CapeVerdeX_StHelena_2.setConnectedBySea(CapeVerdeX_StHelena_3);
		CapeVerdeX_StHelena_3.setConnectedBySea(CapeVerdeX_StHelena_2);
		CapeVerdeX_StHelena_3.setConnectedBySea(CapeVerdeX_StHelena_4);
		CapeVerdeX_StHelena_4.setConnectedBySea(CapeVerdeX_StHelena_3);
		CapeVerdeX_StHelena_4.setConnectedBySea(CapeVerdeX_StHelena_5);
		CapeVerdeX_StHelena_5.setConnectedBySea(CapeVerdeX_StHelena_4);
		CapeVerdeX_StHelena_5.setConnectedBySea(CapeVerdeX_StHelena_6);
		CapeVerdeX_StHelena_6.setConnectedBySea(CapeVerdeX_StHelena_5);
		CapeVerdeX_StHelena_6.setConnectedBySea(CapeVerdeX_StHelena_7);
		CapeVerdeX_StHelena_7.setConnectedBySea(CapeVerdeX_StHelena_6);
		CapeVerdeX_StHelena_7.setConnectedBySea(CapeVerdeX_StHelena_8);
		CapeVerdeX_StHelena_8.setConnectedBySea(CapeVerdeX_StHelena_7);
		CapeVerdeX_StHelena_8.setConnectedBySea(StHelena);
		places.add(CapeVerdeX_StHelena_1);
		places.add(CapeVerdeX_StHelena_2);
		places.add(CapeVerdeX_StHelena_3);
		places.add(CapeVerdeX_StHelena_4);
		places.add(CapeVerdeX_StHelena_5);
		places.add(CapeVerdeX_StHelena_6);
		places.add(CapeVerdeX_StHelena_7);
		places.add(CapeVerdeX_StHelena_8);

		// Sierra Leone-Timbuktu
		SierraLeone_Timbuktu_1.setConnectedByLand(SierraLeone);
		SierraLeone_Timbuktu_1.setConnectedByLand(SierraLeone_Timbuktu_2);
		SierraLeone_Timbuktu_2.setConnectedByLand(SierraLeone_Timbuktu_1);
		SierraLeone_Timbuktu_2.setConnectedByLand(SierraLeone_Timbuktu_3);
		SierraLeone_Timbuktu_3.setConnectedByLand(SierraLeone_Timbuktu_2);
		SierraLeone_Timbuktu_3.setConnectedByLand(SierraLeone_Timbuktu_4);
		SierraLeone_Timbuktu_3.setConnectedByLand(SierraLeoneX_GoldCoast);
		SierraLeone_Timbuktu_4.setConnectedByLand(SierraLeone_Timbuktu_3);
		SierraLeone_Timbuktu_4.setConnectedByLand(Timbuktu);
		places.add(SierraLeone_Timbuktu_1);
		places.add(SierraLeone_Timbuktu_2);
		places.add(SierraLeone_Timbuktu_3);
		places.add(SierraLeone_Timbuktu_4);

		// Sierra Leone-Gold Coast land
		SierraLeoneX_GoldCoast.setConnectedByLand(SierraLeone_Timbuktu_3);
		SierraLeoneX_GoldCoast.setConnectedByLand(GoldCoast);
		places.add(SierraLeoneX_GoldCoast);

		// Sierra Leone-Gold Coast sea
		SierraLeone_GoldCoast_sea_1.setConnectedBySea(SierraLeone);
		SierraLeone_GoldCoast_sea_1.setConnectedBySea(SierraLeone_GoldCoast_sea_2);
		SierraLeone_GoldCoast_sea_2.setConnectedBySea(SierraLeone_GoldCoast_sea_1);
		SierraLeone_GoldCoast_sea_2.setConnectedBySea(SierraLeone_GoldCoast_sea_3);
		SierraLeone_GoldCoast_sea_3.setConnectedBySea(SierraLeone_GoldCoast_sea_2);
		SierraLeone_GoldCoast_sea_3.setConnectedBySea(SierraLeone_GoldCoast_sea_4);
		SierraLeone_GoldCoast_sea_4.setConnectedBySea(SierraLeone_GoldCoast_sea_3);
		SierraLeone_GoldCoast_sea_4.setConnectedBySea(GoldCoast);
		places.add(SierraLeone_GoldCoast_sea_1);
		places.add(SierraLeone_GoldCoast_sea_2);
		places.add(SierraLeone_GoldCoast_sea_3);
		places.add(SierraLeone_GoldCoast_sea_4);

		// Gold Coast-Slave Coast
		GoldCoast_SlaveCoast_1.setConnectedBySea(GoldCoast);
		GoldCoast_SlaveCoast_1.setConnectedBySea(GoldCoast_SlaveCoast_2);
		GoldCoast_SlaveCoast_2.setConnectedBySea(GoldCoast_SlaveCoast_1);
		GoldCoast_SlaveCoast_2.setConnectedBySea(GoldCoast_SlaveCoast_3);
		GoldCoast_SlaveCoast_3.setConnectedBySea(GoldCoast_SlaveCoast_2);
		GoldCoast_SlaveCoast_3.setConnectedBySea(SlaveCoast);
		GoldCoast_SlaveCoast_3.setConnectedBySea(SlaveCoastX_Congo_1);
		places.add(GoldCoast_SlaveCoast_1);
		places.add(GoldCoast_SlaveCoast_2);
		places.add(GoldCoast_SlaveCoast_3);

		// Timbuktu-Slave Coast
		Timbuktu_SlaveCoast_1.setConnectedByLand(Timbuktu);
		Timbuktu_SlaveCoast_1.setConnectedByLand(Timbuktu_SlaveCoast_2);
		Timbuktu_SlaveCoast_2.setConnectedByLand(Timbuktu_SlaveCoast_1);
		Timbuktu_SlaveCoast_2.setConnectedByLand(Timbuktu_SlaveCoast_3);
		Timbuktu_SlaveCoast_3.setConnectedByLand(Timbuktu_SlaveCoast_2);
		Timbuktu_SlaveCoast_3.setConnectedByLand(Timbuktu_SlaveCoast_4);
		Timbuktu_SlaveCoast_4.setConnectedByLand(Timbuktu_SlaveCoast_3);
		Timbuktu_SlaveCoast_4.setConnectedByLand(SlaveCoast);
		places.add(Timbuktu_SlaveCoast_1);
		places.add(Timbuktu_SlaveCoast_2);
		places.add(Timbuktu_SlaveCoast_3);
		places.add(Timbuktu_SlaveCoast_4);

		// Slave Coast-Congo
		SlaveCoastX_Congo_1.setConnectedBySea(GoldCoast_SlaveCoast_3);
		SlaveCoastX_Congo_1.setConnectedBySea(SlaveCoastX_Congo_2);
		SlaveCoastX_Congo_2.setConnectedBySea(SlaveCoastX_Congo_1);
		SlaveCoastX_Congo_2.setConnectedBySea(SlaveCoastX_Congo_3);
		SlaveCoastX_Congo_3.setConnectedBySea(SlaveCoastX_Congo_2);
		SlaveCoastX_Congo_3.setConnectedBySea(Congo);
		places.add(SlaveCoastX_Congo_1);
		places.add(SlaveCoastX_Congo_2);
		places.add(SlaveCoastX_Congo_3);

		// Kandjama-Congo
		Kandjama_Congo_1.setConnectedByLand(Kandjama);
		Kandjama_Congo_1.setConnectedByLand(Kandjama_Congo_2);
		Kandjama_Congo_2.setConnectedByLand(Kandjama_Congo_1);
		Kandjama_Congo_2.setConnectedByLand(Congo);
		places.add(Kandjama_Congo_1);
		places.add(Kandjama_Congo_2);

		// DarFur-Suakin
		DarFur_Suakin_1.setConnectedByLand(DarFur);
		DarFur_Suakin_1.setConnectedByLand(DarFur_Suakin_2);
		DarFur_Suakin_2.setConnectedByLand(DarFur_Suakin_1);
		DarFur_Suakin_2.setConnectedByLand(DarFur_Suakin_3);
		DarFur_Suakin_3.setConnectedByLand(DarFur_Suakin_2);
		DarFur_Suakin_3.setConnectedByLand(Suakin);
		places.add(DarFur_Suakin_1);
		places.add(DarFur_Suakin_2);
		places.add(DarFur_Suakin_3);

		// Suakin-Addis Abeba
		Suakin_AddisAbeba_1.setConnectedByLand(Suakin);
		Suakin_AddisAbeba_1.setConnectedByLand(Suakin_AddisAbeba_2);
		Suakin_AddisAbeba_2.setConnectedByLand(Suakin_AddisAbeba_1);
		Suakin_AddisAbeba_2.setConnectedByLand(AddisAbeba);
		places.add(Suakin_AddisAbeba_1);
		places.add(Suakin_AddisAbeba_2);

		// Suakin-Cape Guardafui
		Suakin_CapeGuardafui_1.setConnectedBySea(Suakin);
		Suakin_CapeGuardafui_1.setConnectedBySea(Suakin_CapeGuardafui_2);
		Suakin_CapeGuardafui_2.setConnectedBySea(Suakin_CapeGuardafui_1);
		Suakin_CapeGuardafui_2.setConnectedBySea(Suakin_CapeGuardafui_3);
		Suakin_CapeGuardafui_3.setConnectedBySea(Suakin_CapeGuardafui_2);
		Suakin_CapeGuardafui_3.setConnectedBySea(Suakin_CapeGuardafui_4);
		Suakin_CapeGuardafui_4.setConnectedBySea(Suakin_CapeGuardafui_3);
		Suakin_CapeGuardafui_4.setConnectedBySea(CapeGuardafui);
		places.add(Suakin_CapeGuardafui_1);
		places.add(Suakin_CapeGuardafui_2);
		places.add(Suakin_CapeGuardafui_3);
		places.add(Suakin_CapeGuardafui_4);

		// Addis Abeba-CapeGuardafui
		AddisAbeba_CapeGuardafui_1.setConnectedByLand(AddisAbeba);
		AddisAbeba_CapeGuardafui_1.setConnectedByLand(AddisAbeba_CapeGuardafui_2);
		AddisAbeba_CapeGuardafui_2.setConnectedByLand(AddisAbeba_CapeGuardafui_1);
		AddisAbeba_CapeGuardafui_2.setConnectedByLand(CapeGuardafui);
		places.add(AddisAbeba_CapeGuardafui_1);
		places.add(AddisAbeba_CapeGuardafui_2);

		// Addis Abeba-Lake Victoria
		AddisAbeba_LakeVictoria_1.setConnectedByLand(AddisAbeba);
		AddisAbeba_LakeVictoria_1.setConnectedByLand(AddisAbeba_LakeVictoria_2);
		AddisAbeba_LakeVictoria_2.setConnectedByLand(AddisAbeba_LakeVictoria_1);
		AddisAbeba_LakeVictoria_2.setConnectedByLand(LakeVictoria);
		places.add(AddisAbeba_LakeVictoria_1);
		places.add(AddisAbeba_LakeVictoria_2);

		// DarFur-Bahr El Ghasal
		DarFur_BahrElGhasal.setConnectedByLand(DarFur);
		DarFur_BahrElGhasal.setConnectedByLand(BahrElGhasal);
		places.add(DarFur_BahrElGhasal);

		// Bahr El Ghasal-Lake Victoria
		BahrElGhasal_LakeVictoria.setConnectedByLand(BahrElGhasal);
		BahrElGhasal_LakeVictoria.setConnectedByLand(LakeVictoria);
		places.add(BahrElGhasal_LakeVictoria);

		// Cape Guardafui-Daressalam
		CapeGuardafui_Daressalam_1.setConnectedByLand(CapeGuardafui);
		CapeGuardafui_Daressalam_1.setConnectedByLand(CapeGuardafui_Daressalam_2);
		CapeGuardafui_Daressalam_2.setConnectedByLand(CapeGuardafui_Daressalam_1);
		CapeGuardafui_Daressalam_2.setConnectedByLand(CapeGuardafui_Daressalam_3);
		CapeGuardafui_Daressalam_3.setConnectedByLand(CapeGuardafui_Daressalam_2);
		CapeGuardafui_Daressalam_3.setConnectedByLand(CapeGuardafui_Daressalam_4);
		CapeGuardafui_Daressalam_4.setConnectedByLand(CapeGuardafui_Daressalam_3);
		CapeGuardafui_Daressalam_4.setConnectedByLand(CapeGuardafui_Daressalam_5);
		CapeGuardafui_Daressalam_5.setConnectedByLand(CapeGuardafui_Daressalam_4);
		CapeGuardafui_Daressalam_5.setConnectedByLand(Daressalam);
		places.add(CapeGuardafui_Daressalam_1);
		places.add(CapeGuardafui_Daressalam_2);
		places.add(CapeGuardafui_Daressalam_3);
		places.add(CapeGuardafui_Daressalam_4);
		places.add(CapeGuardafui_Daressalam_5);

		// Cape Guardafui-Tamatave
		CapeGuardafui_Tamatave_1.setConnectedBySea(CapeGuardafui);
		CapeGuardafui_Tamatave_1.setConnectedBySea(CapeGuardafui_Tamatave_2);
		CapeGuardafui_Tamatave_2.setConnectedBySea(CapeGuardafui_Tamatave_1);
		CapeGuardafui_Tamatave_2.setConnectedBySea(CapeGuardafui_Tamatave_3);
		CapeGuardafui_Tamatave_3.setConnectedBySea(CapeGuardafui_Tamatave_2);
		CapeGuardafui_Tamatave_3.setConnectedBySea(CapeGuardafui_Tamatave_4);
		CapeGuardafui_Tamatave_4.setConnectedBySea(CapeGuardafui_Tamatave_3);
		CapeGuardafui_Tamatave_4.setConnectedBySea(CapeGuardafui_Tamatave_5);
		CapeGuardafui_Tamatave_5.setConnectedBySea(CapeGuardafui_Tamatave_4);
		CapeGuardafui_Tamatave_5.setConnectedBySea(CapeGuardafui_Tamatave_6);
		CapeGuardafui_Tamatave_6.setConnectedBySea(CapeGuardafui_Tamatave_5);
		CapeGuardafui_Tamatave_6.setConnectedBySea(CapeGuardafui_Tamatave_7);
		CapeGuardafui_Tamatave_7.setConnectedBySea(CapeGuardafui_Tamatave_6);
		CapeGuardafui_Tamatave_7.setConnectedBySea(Tamatave);
		places.add(CapeGuardafui_Tamatave_1);
		places.add(CapeGuardafui_Tamatave_2);
		places.add(CapeGuardafui_Tamatave_3);
		places.add(CapeGuardafui_Tamatave_4);
		places.add(CapeGuardafui_Tamatave_5);
		places.add(CapeGuardafui_Tamatave_6);
		places.add(CapeGuardafui_Tamatave_7);

		// Cape Guardafui-Mozambique
		CapeGuardafui_Mozambique_1.setConnectedBySea(CapeGuardafui);
		CapeGuardafui_Mozambique_1.setConnectedBySea(CapeGuardafui_Mozambique_2);
		CapeGuardafui_Mozambique_2.setConnectedBySea(CapeGuardafui_Mozambique_1);
		CapeGuardafui_Mozambique_2.setConnectedBySea(CapeGuardafui_Mozambique_3);
		CapeGuardafui_Mozambique_3.setConnectedBySea(CapeGuardafui_Mozambique_2);
		CapeGuardafui_Mozambique_3.setConnectedBySea(CapeGuardafui_Mozambique_4);
		CapeGuardafui_Mozambique_4.setConnectedBySea(CapeGuardafui_Mozambique_3);
		CapeGuardafui_Mozambique_4.setConnectedBySea(CapeGuardafui_Mozambique_5);
		CapeGuardafui_Mozambique_5.setConnectedBySea(CapeGuardafui_Mozambique_4);
		CapeGuardafui_Mozambique_5.setConnectedBySea(CapeGuardafui_Mozambique_6);
		CapeGuardafui_Mozambique_6.setConnectedBySea(CapeGuardafui_Mozambique_5);
		CapeGuardafui_Mozambique_6.setConnectedBySea(CapeGuardafui_Mozambique_7);
		CapeGuardafui_Mozambique_7.setConnectedBySea(CapeGuardafui_Mozambique_6);
		CapeGuardafui_Mozambique_7.setConnectedBySea(Mozambique);
		places.add(CapeGuardafui_Mozambique_1);
		places.add(CapeGuardafui_Mozambique_2);
		places.add(CapeGuardafui_Mozambique_3);
		places.add(CapeGuardafui_Mozambique_4);
		places.add(CapeGuardafui_Mozambique_5);
		places.add(CapeGuardafui_Mozambique_6);
		places.add(CapeGuardafui_Mozambique_7);

		// Lake Victoria-Ocomba
		LakeVictoria_Ocomba_1.setConnectedByLand(LakeVictoria);
		LakeVictoria_Ocomba_1.setConnectedByLand(LakeVictoria_Ocomba_2);
		LakeVictoria_Ocomba_2.setConnectedByLand(LakeVictoria_Ocomba_1);
		LakeVictoria_Ocomba_2.setConnectedByLand(LakeVictoria_Ocomba_3);
		LakeVictoria_Ocomba_3.setConnectedByLand(LakeVictoria_Ocomba_2);
		LakeVictoria_Ocomba_3.setConnectedByLand(Ocomba);
		places.add(LakeVictoria_Ocomba_1);
		places.add(LakeVictoria_Ocomba_2);
		places.add(LakeVictoria_Ocomba_3);

		// Lake Victoria-Mozambique
		LakeVictoria_Mozambique_1.setConnectedByLand(LakeVictoria);
		LakeVictoria_Mozambique_1.setConnectedByLand(LakeVictoria_Mozambique_2);
		LakeVictoria_Mozambique_2.setConnectedByLand(LakeVictoria_Mozambique_1);
		LakeVictoria_Mozambique_2.setConnectedByLand(LakeVictoria_Mozambique_3);
		LakeVictoria_Mozambique_3.setConnectedByLand(LakeVictoria_Mozambique_2);
		LakeVictoria_Mozambique_3.setConnectedByLand(LakeVictoria_Mozambique_4);
		LakeVictoria_Mozambique_4.setConnectedByLand(LakeVictoria_Mozambique_3);
		LakeVictoria_Mozambique_4.setConnectedByLand(LakeVictoria_Mozambique_5);
		LakeVictoria_Mozambique_4.setConnectedByLand(Daressalam);
		LakeVictoria_Mozambique_5.setConnectedByLand(LakeVictoria_Mozambique_4);
		LakeVictoria_Mozambique_5.setConnectedByLand(Mozambique);
		places.add(LakeVictoria_Mozambique_1);
		places.add(LakeVictoria_Mozambique_2);
		places.add(LakeVictoria_Mozambique_3);
		places.add(LakeVictoria_Mozambique_4);
		places.add(LakeVictoria_Mozambique_5);
		// HAND CRAMP, BRAIN CRAMP, BOREDOM CRAMP, AARGH!!!

		// Congo-Ocomba
		Congo_Ocomba_1.setConnectedByLand(Congo);
		Congo_Ocomba_1.setConnectedByLand(Congo_Ocomba_2);
		Congo_Ocomba_2.setConnectedByLand(Congo_Ocomba_1);
		Congo_Ocomba_2.setConnectedByLand(Congo_Ocomba_3);
		Congo_Ocomba_3.setConnectedByLand(Congo_Ocomba_2);
		Congo_Ocomba_3.setConnectedByLand(Ocomba);
		places.add(Congo_Ocomba_1);
		places.add(Congo_Ocomba_2);
		places.add(Congo_Ocomba_3);

		// Mozambique-Dragon Mountain
		Mozambique_DragonMountain_1.setConnectedByLand(Mozambique);
		Mozambique_DragonMountain_1.setConnectedByLand(Mozambique_DragonMountain_2);
		Mozambique_DragonMountain_2.setConnectedByLand(Mozambique_DragonMountain_1);
		Mozambique_DragonMountain_2.setConnectedByLand(Mozambique_DragonMountain_3);
		Mozambique_DragonMountain_2.setConnectedByLand(MozambiqueX_Congo_1);
		Mozambique_DragonMountain_3.setConnectedByLand(Mozambique_DragonMountain_2);
		Mozambique_DragonMountain_3.setConnectedByLand(Mozambique_DragonMountain_4);
		Mozambique_DragonMountain_3.setConnectedByLand(MozambiqueX_VictoriaFalls);
		Mozambique_DragonMountain_4.setConnectedByLand(Mozambique_DragonMountain_3);
		Mozambique_DragonMountain_4.setConnectedByLand(DragonMountain);
		places.add(Mozambique_DragonMountain_1);
		places.add(Mozambique_DragonMountain_2);
		places.add(Mozambique_DragonMountain_3);
		places.add(Mozambique_DragonMountain_4);

		// Mozambique-Congo
		MozambiqueX_Congo_1.setConnectedByLand(Mozambique_DragonMountain_2);
		MozambiqueX_Congo_1.setConnectedByLand(MozambiqueX_Congo_2);
		MozambiqueX_Congo_2.setConnectedByLand(MozambiqueX_Congo_1);
		MozambiqueX_Congo_2.setConnectedByLand(MozambiqueX_Congo_3);
		MozambiqueX_Congo_3.setConnectedByLand(MozambiqueX_Congo_2);
		MozambiqueX_Congo_3.setConnectedByLand(MozambiqueX_Congo_4);
		MozambiqueX_Congo_4.setConnectedByLand(MozambiqueX_Congo_3);
		MozambiqueX_Congo_4.setConnectedByLand(MozambiqueX_Congo_5);
		MozambiqueX_Congo_5.setConnectedByLand(MozambiqueX_Congo_4);
		MozambiqueX_Congo_5.setConnectedByLand(MozambiqueX_Congo_6);
		MozambiqueX_Congo_6.setConnectedByLand(MozambiqueX_Congo_5);
		MozambiqueX_Congo_6.setConnectedByLand(MozambiqueX_Congo_7);
		MozambiqueX_Congo_7.setConnectedByLand(MozambiqueX_Congo_6);
		MozambiqueX_Congo_7.setConnectedByLand(Congo);
		places.add(MozambiqueX_Congo_1);
		places.add(MozambiqueX_Congo_2);
		places.add(MozambiqueX_Congo_3);
		places.add(MozambiqueX_Congo_4);
		places.add(MozambiqueX_Congo_5);
		places.add(MozambiqueX_Congo_6);
		places.add(MozambiqueX_Congo_7);

		// Mozambique-Victoria Falls
		MozambiqueX_VictoriaFalls.setConnectedByLand(Mozambique_DragonMountain_3);
		MozambiqueX_VictoriaFalls.setConnectedByLand(VictoriaFalls);
		places.add(MozambiqueX_VictoriaFalls);

		// Mozambique-Cape St. Marie
		Mozambique_CapeStMarie_1.setConnectedBySea(Mozambique);
		Mozambique_CapeStMarie_1.setConnectedBySea(Mozambique_CapeStMarie_2);
		Mozambique_CapeStMarie_2.setConnectedBySea(Mozambique_CapeStMarie_1);
		Mozambique_CapeStMarie_2.setConnectedBySea(CapeStMarie);
		places.add(Mozambique_CapeStMarie_1);
		places.add(Mozambique_CapeStMarie_2);

		// Victoria Falls-Dragon Mountain
		VictoriaFalls_DragonMountain_1.setConnectedByLand(VictoriaFalls);
		VictoriaFalls_DragonMountain_1.setConnectedByLand(VictoriaFalls_DragonMountain_2);
		VictoriaFalls_DragonMountain_2.setConnectedByLand(VictoriaFalls_DragonMountain_1);
		VictoriaFalls_DragonMountain_2.setConnectedByLand(DragonMountain);
		places.add(VictoriaFalls_DragonMountain_1);
		places.add(VictoriaFalls_DragonMountain_2);

		// Congo-Whalefish Bay
		Congo_WhalefishBay_1.setConnectedBySea(Congo);
		Congo_WhalefishBay_1.setConnectedBySea(Congo_WhalefishBay_2);
		Congo_WhalefishBay_2.setConnectedBySea(Congo_WhalefishBay_1);
		Congo_WhalefishBay_2.setConnectedBySea(Congo_WhalefishBay_3);
		Congo_WhalefishBay_3.setConnectedBySea(Congo_WhalefishBay_2);
		Congo_WhalefishBay_3.setConnectedBySea(Congo_WhalefishBay_4);
		Congo_WhalefishBay_4.setConnectedBySea(Congo_WhalefishBay_3);
		Congo_WhalefishBay_4.setConnectedBySea(WhalefishBay);
		places.add(Congo_WhalefishBay_1);
		places.add(Congo_WhalefishBay_2);
		places.add(Congo_WhalefishBay_3);
		places.add(Congo_WhalefishBay_4);

		// Whalefish Bay-Victoria Falls
		WhalefishBay_VictoriaFalls_1.setConnectedByLand(WhalefishBay);
		WhalefishBay_VictoriaFalls_1.setConnectedByLand(WhalefishBay_VictoriaFalls_2);
		WhalefishBay_VictoriaFalls_2.setConnectedByLand(WhalefishBay_VictoriaFalls_1);
		WhalefishBay_VictoriaFalls_2.setConnectedByLand(WhalefishBay_VictoriaFalls_3);
		WhalefishBay_VictoriaFalls_3.setConnectedByLand(WhalefishBay_VictoriaFalls_2);
		WhalefishBay_VictoriaFalls_3.setConnectedByLand(VictoriaFalls);
		places.add(WhalefishBay_VictoriaFalls_1);
		places.add(WhalefishBay_VictoriaFalls_2);
		places.add(WhalefishBay_VictoriaFalls_3);

		// Whalefish Bay-Capetown land
		WhalefishBay_Capetown_land_1.setConnectedByLand(WhalefishBay);
		WhalefishBay_Capetown_land_1.setConnectedByLand(WhalefishBay_Capetown_land_2);
		WhalefishBay_Capetown_land_2.setConnectedByLand(WhalefishBay_Capetown_land_1);
		WhalefishBay_Capetown_land_2.setConnectedByLand(WhalefishBay_Capetown_land_3);
		WhalefishBay_Capetown_land_3.setConnectedByLand(WhalefishBay_Capetown_land_2);
		WhalefishBay_Capetown_land_3.setConnectedByLand(Capetown);
		places.add(WhalefishBay_Capetown_land_1);
		places.add(WhalefishBay_Capetown_land_2);
		places.add(WhalefishBay_Capetown_land_3);

		// Whalefish Bay-Capetown sea
		WhalefishBay_Capetown_sea_1.setConnectedBySea(WhalefishBay);
		WhalefishBay_Capetown_sea_1.setConnectedBySea(WhalefishBay_Capetown_sea_2);
		WhalefishBay_Capetown_sea_2.setConnectedBySea(WhalefishBay_Capetown_sea_1);
		WhalefishBay_Capetown_sea_2.setConnectedBySea(WhalefishBay_Capetown_sea_3);
		WhalefishBay_Capetown_sea_3.setConnectedBySea(WhalefishBay_Capetown_sea_2);
		WhalefishBay_Capetown_sea_3.setConnectedBySea(WhalefishBay_Capetown_sea_4);
		WhalefishBay_Capetown_sea_3.setConnectedBySea(StHelena_CapetownX_7);
		WhalefishBay_Capetown_sea_4.setConnectedBySea(WhalefishBay_Capetown_sea_3);
		WhalefishBay_Capetown_sea_4.setConnectedBySea(Capetown);
		places.add(WhalefishBay_Capetown_sea_1);
		places.add(WhalefishBay_Capetown_sea_2);
		places.add(WhalefishBay_Capetown_sea_3);
		places.add(WhalefishBay_Capetown_sea_4);

		// St. Helena-Capetown
		StHelena_CapetownX_1.setConnectedBySea(StHelena);
		StHelena_CapetownX_1.setConnectedBySea(StHelena_CapetownX_2);
		StHelena_CapetownX_2.setConnectedBySea(StHelena_CapetownX_1);
		StHelena_CapetownX_2.setConnectedBySea(StHelena_CapetownX_3);
		StHelena_CapetownX_3.setConnectedBySea(StHelena_CapetownX_2);
		StHelena_CapetownX_3.setConnectedBySea(StHelena_CapetownX_4);
		StHelena_CapetownX_4.setConnectedBySea(StHelena_CapetownX_3);
		StHelena_CapetownX_4.setConnectedBySea(StHelena_CapetownX_5);
		StHelena_CapetownX_5.setConnectedBySea(StHelena_CapetownX_4);
		StHelena_CapetownX_5.setConnectedBySea(StHelena_CapetownX_6);
		StHelena_CapetownX_6.setConnectedBySea(StHelena_CapetownX_5);
		StHelena_CapetownX_6.setConnectedBySea(StHelena_CapetownX_7);
		StHelena_CapetownX_7.setConnectedBySea(StHelena_CapetownX_6);
		StHelena_CapetownX_7.setConnectedBySea(WhalefishBay_Capetown_sea_3);
		places.add(StHelena_CapetownX_1);
		places.add(StHelena_CapetownX_2);
		places.add(StHelena_CapetownX_3);
		places.add(StHelena_CapetownX_4);
		places.add(StHelena_CapetownX_5);
		places.add(StHelena_CapetownX_6);
		places.add(StHelena_CapetownX_7);

		// Tamatave-Cape St. Marie
		Tamatave_CapeStMarie_1.setConnectedByLand(Tamatave);
		Tamatave_CapeStMarie_1.setConnectedByLand(Tamatave_CapeStMarie_2);
		Tamatave_CapeStMarie_2.setConnectedByLand(Tamatave_CapeStMarie_1);
		Tamatave_CapeStMarie_2.setConnectedByLand(Tamatave_CapeStMarie_3);
		Tamatave_CapeStMarie_3.setConnectedByLand(Tamatave_CapeStMarie_2);
		Tamatave_CapeStMarie_3.setConnectedByLand(CapeStMarie);
		places.add(Tamatave_CapeStMarie_1);
		places.add(Tamatave_CapeStMarie_2);
		places.add(Tamatave_CapeStMarie_3);

		// Capetown-Cape St. Marie
		Capetown_CapeStMarie_1.setConnectedBySea(Capetown);
		Capetown_CapeStMarie_1.setConnectedBySea(Capetown_CapeStMarie_2);
		Capetown_CapeStMarie_2.setConnectedBySea(Capetown_CapeStMarie_1);
		Capetown_CapeStMarie_2.setConnectedBySea(Capetown_CapeStMarie_3);
		Capetown_CapeStMarie_3.setConnectedBySea(Capetown_CapeStMarie_2);
		Capetown_CapeStMarie_3.setConnectedBySea(Capetown_CapeStMarie_4);
		Capetown_CapeStMarie_4.setConnectedBySea(Capetown_CapeStMarie_3);
		Capetown_CapeStMarie_4.setConnectedBySea(Capetown_CapeStMarie_5);
		Capetown_CapeStMarie_5.setConnectedBySea(Capetown_CapeStMarie_4);
		Capetown_CapeStMarie_5.setConnectedBySea(Capetown_CapeStMarie_6);
		Capetown_CapeStMarie_6.setConnectedBySea(Capetown_CapeStMarie_5);
		Capetown_CapeStMarie_6.setConnectedBySea(Capetown_CapeStMarie_7);
		Capetown_CapeStMarie_7.setConnectedBySea(Capetown_CapeStMarie_6);
		Capetown_CapeStMarie_7.setConnectedBySea(CapeStMarie);
		places.add(Capetown_CapeStMarie_1);
		places.add(Capetown_CapeStMarie_2);
		places.add(Capetown_CapeStMarie_3);
		places.add(Capetown_CapeStMarie_4);
		places.add(Capetown_CapeStMarie_5);
		places.add(Capetown_CapeStMarie_6);
		places.add(Capetown_CapeStMarie_7);

		// Setting player starting points
		for (int i = 0; i < player.length; i++) {
			if (player[i].startsAtTangier())
				player[i].setPlace(Tangier);
			else
				player[i].setPlace(Cairo);
		}
	}

	/**
	 * Finds the sea route between the player's current position, and his destination.
	 * This method is recursive, and will call itself until the route is found.
	 * 
	 * @param check			place to be checked for connectivity.
	 * @param doNotCheck	place not to be checked (to avoid re-checking checked places)
	 * @return				the route as an ArrayList of Places
	 */
	public ArrayList<Place> getSeaRoute(Place check, Place doNotCheck) {
		ArrayList<Place> temp = new ArrayList<Place>();
		if (check == player[turn].getLockedDestination()) { // Are we there yet?
			temp.add(check);
			return temp;
		}
		else if ((check.isCity() || check.isStart()) && check != player[turn].getPlace()) { // Was it a dead end?
			return null;
		}
		else { // Not a dead end and not there yet.
			for (Place p : check.getConnectedBySea()) {
				if (p != doNotCheck && getSeaRoute(p,check) != null) {
					temp.add(check);
					temp.addAll(getSeaRoute(p,check));
					return temp;
				}
			}
		}
		return null;
	}

	/**
	 * Opens a token, and displays its effects.
	 */
	public void openToken() {
		eventPanel.removeAll();
		eventPanel.add(eventLabel);
		eventLabel.setIcon(player[turn].getPlace().getToken().getResizedIcon(eventWinSize));
		messageLabel.setText(bundle.getString(player[turn].getPlace().getToken().getMessage()));
		endTurn.setEnabled(player[turn].isHuman());
		buyToken.setEnabled(false);
		soundPlayer.play(player[turn].getPlace().getToken().getSound());
		
		// Can the player get the Gold Coast bonus?
		int bonus = 1;
		if (player[turn].getPlace().getName().equals("Gold Coast")) {
			bonus = 2;
		}

		// Do the money math
		player[turn].setMoney(Math.max(player[turn].getMoney() + player[turn].getPlace().getToken().monetaryValue() * bonus, 0));
		playerMoney[turn].setText(" Pound " + player[turn].getMoney());

		// Add the token to the "bookkeeping"
		playerTokens[turn].add(new JLabel(player[turn].getPlace().getToken().getResizedIcon(smallTokenSize)));
		
		// Did he find the star?
		if (player[turn].getPlace().getToken() == Token.STAR_OF_AFRICA) {
			player[turn].setHasFoundTheStar();
			starHasBeenFound = true;
			// If all the horseshoes are out of the game, the game is already decided
			if (horseshoesFound == 5 && !player[turn].isStranded()) {
				gameWon();
			}
			if (horseshoesFound == 5 && player[turn].isStranded()) {
				gameUnwinnable(bundle.getString("starStranded"));
			}
		}

		// Was it a horseshoe?
		if (player[turn].getPlace().getToken() == Token.HORSESHOE) {
			horseshoesFound++;
			// Did he find a horseshoe after the discovery of the star?
			if (starHasBeenFound) {
				soundPlayer.play("snd/tada.wav");
				player[turn].setHasFoundTheStar();
				messageLabel.setText(bundle.getString("congratulations"));
				// Was it the last one? If so, everyone without either horseshoe or star are out of the game
				if (horseshoesFound == 5) {
					for (Player p : player) {
						if (!p.hasFoundTheStar()) {
							setStranded(p);
						}
					}
				}
			}
			else { // Horseshoe sound varies with the situation, which is why it's called from here
				soundPlayer.play("snd/meh.wav");
			}
		}

		// Did he draw a blank at Slave Coast?
		if (player[turn].getPlace().getName().equals("Slave Coast") && player[turn].getPlace().getToken() == Token.BLANK) {
			messageLabel.setText(bundle.getString("toughLuck"));
			player[turn].setTurnsLeftAsSlave(3);
		}

		// Was anyone stranded because of this action?
		player[turn].getPlace().setToken(null);
		for (Player p : player) {
			checkStranded(p);
		}
		map.refresh();
	}

	/**
	 * Checks if a player has become stranded. If the answer is yes, the player's
	 * status will also be updated to reflect this.
	 * 
	 * @param play	player who is to be checked.
	 * @return		<code>true</code> if the player is stranded, otherwise <code>false</code>.
	 */
	public boolean checkStranded(Player play) {
		/*TAKEN OUT BY THE NEW RULE, a player cannot be stranded*/
		// Is the player pennyless, and without tokens on one of the one-token-only-islands?
		
		/*
		if (play.getPlace().getName().equals("Canary Islands") || play.getPlace().getName().equals("St. Helena")) {
			if (play.getPlace().getToken() == null && play.getMoney() < 100) {
				setStranded(play);
				return true;
			}
		}
		
		boolean temp = true;
		if (play.getPlace().getX() >= 1447 && play.getPlace().getY() >= 1738) { // Is player on Madagascar?
			for (Place p : cities) {
				if (p.getName().equals("Cape St. Marie") || p.getName().equals("Tamatave")) { // Are there tokens on Madagascar?
					if (p.getToken() != null) {
						temp = false;
					}
				}
			}
			if (temp && play.getMoney() < 100) { // If not, and player has no money, he's stranded
				setStranded(play);
				return true;
			}
		}
		
		// Is the player "stranded" on the mainland without having found the star (or horseshoe)? (I.e. all the tiles that 
		// are left, are on islands, and the player has no money to get there)
		
		if (tokensOnlyLeftOnIslands() && play.getMoney() < 100 && !play.hasFoundTheStar() 
				&& play.getLockedDestination() == null && !playerOnIsland(play)) {
			setStranded(play);
			return true;
		}
		*/
		return false;
	}

	/**
	 * Checks whether there are only tokens on the islands left.
	 * 
	 * @return	<code>true</code> if there no longer are any tokens on the mainland,
	 * 			otherwise <code>false</code>.
	 */
	public boolean tokensOnlyLeftOnIslands() {
		boolean onlyIslands = true;
		for (Place p : cities) {
			if (p.getToken() != null) {
				if (!(p.getName().equals("Canary Islands") || p.getName().equals("St. Helena") ||
					p.getName().equals("Cape St. Marie") || p.getName().equals("Tamatave"))) {
					onlyIslands = false;
				}
			}
		}
		return onlyIslands;
	}

	/**
	 * Checks whether a player is on an island or not.
	 * 
	 * @param play	player to be checked.
	 * @return		<code>true</code> if the player is on an island, otherwise <code>false</code>.
	 */
	public boolean playerOnIsland(Player play) {
		return (play.getPlace().getName().equals("Canary Islands") || play.getPlace().getName().equals("St. Helena")
				|| (play.getPlace().getX() >= 1447 && play.getPlace().getY() >= 1738));
	}

	/**
	 * Calculates the appropriate budget that an AI player is to have at its disposal.
	 * 
	 * @return	the budget.
	 */
	public int calculateBudget() {
		if (starHasBeenFound) {
			if (!player[turn].hasFoundTheStar() && player[turn].getMoney() == 300) {
				return 200;
			}
			return player[turn].getMoney();
		}
		if (player[turn].getMoney() <= 100) {
			if (tokensOnlyLeftOnIslands() || playerOnIsland(player[turn])) {
				return player[turn].getMoney();
			}
			else {
				return 0;
			}
		}
		if (player[turn].getMoney() <= 500) {
			return 100;
		}
		if (player[turn].getMoney() <= 1000) {
			return 300;
		}
		return player[turn].getMoney() / 3;
	}

	/**
	 * Displays the result of a diceroll in the event window.
	 * 
	 * @param dice	the result to be displayed.
	 */
	public void showDiceRoll(int dice) {
		Dice d;
		String str = bundle.getString("six");		
		switch(dice) {
		case 1:
			d=Dice.ONE;
			str=bundle.getString("one");
			break;
		case 2:
			d=Dice.TWO;
			str=bundle.getString("two");
			break;
		case 3:
			d=Dice.THREE;
			str=bundle.getString("three");
			break;
		case 4:
			d=Dice.FOUR;
			str=bundle.getString("four");
			break;
		case 5:
			d=Dice.FIVE;
			str=bundle.getString("five");
			break;
		default:
			d=Dice.SIX;
		}
		eventPanel.removeAll();
		eventPanel.add(eventLabel);
		eventLabel.setIcon(d.getResizedIcon(eventWinSize));
		messageLabel.setText(bundle.getString("youRolled")+" "+str);
	}

	/**
	 * Updates the changes when a player (human or AI) has decided on a move. Also
	 * conducts some checks for special events that may occur after a move.
	 * 
	 * @param destination	the destination of the move.
	 */
	public void moveMade(Place destination) {
		if ((player[turn].getPlace().isCity() || player[turn].getPlace().isStart()) && player[turn].getLockedDestination() != null) {
			soundPlayer.play("snd/shiphorn.wav");
		}
		player[turn].setPlace(destination);
		buyToken.setEnabled(destination.getToken() != null && player[turn].getMoney() >= 100 && player[turn].isHuman());
		endTurn.setEnabled(player[turn].isHuman());
		// Was it a tile that got the player captured?
		if (destination.isHostile()) {
			player[turn].setCaptured(true);
			if (destination.getY() == 700) { // Was it the Sahara tile?
				messageLabel.setText(bundle.getString("beduins"));
			}
			else { // If not, it was one of the St. Helena tiles
				messageLabel.setText(bundle.getString("pirates"));
			}
		}
		// Was the player the first to arrive at Capetown?
		if (!capetownHasBeenVisited && destination.getName().equals("Capetown")) {
			player[turn].setMoney(player[turn].getMoney() + 500);
			playerMoney[turn].setText(" Pound " + player[turn].getMoney());
			messageLabel.setText(bundle.getString("capetown"));
			capetownHasBeenVisited = true;
			buyToken.setEnabled(true);
		}
		// Did the player arrive at the end of a sea route?
		if (destination == player[turn].getLockedDestination()) {
			player[turn].setLockedDestination(null);
		}
		map.showMove(destination, turn);
		player[turn].resetRoute();	/*Reset route*/
		map.refresh();
		// Did the player win?
		if (player[turn].hasFoundTheStar() && destination.isStart()) {
			gameWon();
		}
	}
	
	public void updateMoney() {
		playerMoney[turn].setText(" Pound " + player[turn].getMoney());
	}
	
	public boolean gameIsOver() {
		return gameOver;
	}

	// Reacts to changes in the zoom slider.
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			double oldLevel = zoomLevel; // Remember the old value in case the new one goes out of bounds
			zoomLevel = zoom.getValue()/100.0;
			if (!map.zoom(zoomLevel)) {
				zoomLevel = oldLevel;
				zoom.setValue((int)(zoomLevel*100));
				// According to Sun's java page the setValue-method should also update the slider's knob position, but since
				// that's not the case in real life, this workaround is required.
				zoom.setUI(zoom.getUI());
			}
		}

	}

	// Reacts to button clicks. The AI players also "poke" at this method to end their turn.
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == rollDice) {
			rollDice.setEnabled(false);
			rollToken.setEnabled(false);
			boardShip.setEnabled(false);
			boardShipNoMoney.setEnabled(false);
			boardPlane.setEnabled(false);
			
			/*If boarded without money on an unpaid ship voyage*/
			if (player[turn].getBoardedNoMoney()) {
				calculateMoveOptions(2);
				return;
			}
			
			
			// Roll the dice
			int rand = 1 + (int)(Math.random() * 6);
			showDiceRoll(rand);

			if (player[turn].isCaptured()) { // Is the player rolling to get free from pirates or beduins?
				if (rand <= 2) {
					messageLabel.setText(bundle.getString("escaped"));
					calculateMoveOptions(rand);
				}
				else {
					messageLabel.setText(bundle.getString("failEscape"));
					endTurn.setEnabled(true);
					return;
				}				
			}
			calculateMoveOptions(rand);
			/*
			rollForToken[turn] = rollToTurn.isSelected();
			if (!rollToTurn.isSelected()) {
				calculateMoveOptions(rand);
			}
			else if (rand >= 4) {
				openToken();
				rollForToken[turn] = false;
			}
			else {
				buyToken.setEnabled(false);
				endTurn.setEnabled(true);
			}
			*/
		}
		
		/*Added to just roll for token*/
		if (e.getSource() == rollToken) {
			rollDice.setEnabled(false);
			rollToken.setEnabled(false);
			boardShip.setEnabled(false);
			boardShipNoMoney.setEnabled(false);
			boardPlane.setEnabled(false);
			// Roll the dice
			int rand = 1 + (int)(Math.random() * 6);
			showDiceRoll(rand);
			if (rand >= 4) {
				openToken();
				//rollForToken[turn] = false;
			}
			else {
				buyToken.setEnabled(false);
				endTurn.setEnabled(true);
			}
		}

		if (e.getSource() == buyToken) {
			//rollToTurn.setEnabled(false);
			buyToken.setEnabled(false);
			boardPlane.setEnabled(false);
			boardShip.setEnabled(false);
			rollToken.setEnabled(false);
			boardShipNoMoney.setEnabled(false);
			rollDice.setEnabled(false);
			player[turn].setMoney(player[turn].getMoney() - 100);
			openToken();
		}

		if (e.getSource() == endTurn || e.getActionCommand().equals("End turn")) {
			subpanel[turn].setBorder(box); // Remove the turn indicator
			// Reset the event window & message area
			messageLabel.setText(" ");
			eventPanel.removeAll();
			eventPanel.add(emptyBox);
			checkStranded(player[turn]);

			// Select next player
			int originalTurn = turn;
			do {
				turn++;
				if (turn == player.length) {
					turn = 0;
				}
				if (turn == originalTurn) {
					// If everybody are stranded the game is unwinnable
					if (player[turn].isStranded()) { 
						gameUnwinnable(bundle.getString("allStranded"));
					}
				}
			} while (player[turn].isStranded() && !gameOver);			
			subpanel[turn].setBorder(selectedBox);

			// Set default view
			endTurn.setEnabled(false);
			rollDice.setEnabled(true);
			//rollToTurn.setSelected(rollForToken[turn] && player[turn].getPlace().getToken() != null);
			//rollToTurn.setEnabled(player[turn].getPlace().getToken() != null);
			
			rollToken.setEnabled(player[turn].getPlace().getToken() != null);
			
			
			buyToken.setEnabled(player[turn].getPlace().getToken() != null && player[turn].getMoney() >= 100);
			boardShip.setEnabled(!player[turn].getPlace().getConnectedBySea().isEmpty() && player[turn].getMoney() >= 100 && player[turn].getLockedDestination() == null);
			
			boardShipNoMoney.setEnabled(!player[turn].getPlace().getConnectedBySea().isEmpty() && player[turn].getLockedDestination() == null);
			player[turn].setBoardedNoMoney(!(player[turn].getLockedDestination() == null));
			
			boardPlane.setEnabled(!player[turn].getPlace().getConnectedByAir().isEmpty() && player[turn].getMoney() >= 300);

			// Is the player stuck at Slave Coast?
			if (player[turn].getTurnsLeftAsSlave() > 0) {
				endTurn.setEnabled(true);
				rollDice.setEnabled(false);
				boardShip.setEnabled(false);
				boardPlane.setEnabled(false);
				//rollToTurn.setEnabled(false);
				rollToken.setEnabled(false);
				boardShipNoMoney.setEnabled(false);
				messageLabel.setText(bundle.getString("still")+" "+player[turn].getTurnsLeftAsSlave()+" "+bundle.getString("turnsLeft"));
				if (player[turn].isHuman()) { // The counting for the computer is handled by the AIPLayer class
					player[turn].setTurnsLeftAsSlave(player[turn].getTurnsLeftAsSlave() - 1);
				}
			}

			if (player[turn].getPlace().getName().equals("Canary Islands") || player[turn].getPlace().getName().equals("St. Helena")) {
				//rollToTurn.setEnabled(player[turn].getPlace().getToken() != null);
				//rollToTurn.setSelected(player[turn].getPlace().getToken() != null);
				rollDice.setEnabled(player[turn].getPlace().getToken() != null);
			}
			
			if (!player[turn].isHuman()) {
				endTurn.setEnabled(false);
				rollDice.setEnabled(false);
				boardShip.setEnabled(false);
				boardPlane.setEnabled(false);
				//rollToTurn.setEnabled(false);
				buyToken.setEnabled(false);
				rollToken.setEnabled(false);
				boardShipNoMoney.setEnabled(false);
				aip.makeMove(turn);
			}
		}

		if (e.getSource() == boardPlane) {
			// Convert names of connected places to objects, since that's what the JOptionPane demands
			Object[] possibilities = new Object[player[turn].getPlace().getConnectedByAir().size()];
			int i=0;
			player[turn].resetRoute();
			for (Place p : player[turn].getPlace().getConnectedByAir()) {
				possibilities[i] = p.getName();
				i++;
			}
			String str = (String)JOptionPane.showInputDialog(frame,bundle.getString("selectDest"),bundle.getString("flyTo"),
					JOptionPane.PLAIN_MESSAGE,null,possibilities,possibilities[0]);

			// Parse all the cities and check which one was selected
			Place destination = null;
			for (Place p : player[turn].getPlace().getConnectedByAir()) {
				if (p.getName().equals(str)) {
					destination = p;
				}
			}

			if (destination != null) {
				soundPlayer.play("snd/airplane.wav");
				rollDice.setEnabled(false);
				boardShip.setEnabled(false);
				boardPlane.setEnabled(false);
				rollToken.setEnabled(false);
				boardShipNoMoney.setEnabled(false);
				player[turn].setMoney(player[turn].getMoney() - 300);
				playerMoney[turn].setText(" Pound " + player[turn].getMoney());
				moveMade(destination);
			}
		}

		if (e.getSource() == boardShip) {
			HashSet<Place> temp = new HashSet<Place>();
			for (Place p : player[turn].getPlace().getConnectedBySea()) {
				temp.addAll(getConnectedBySea(p,player[turn].getPlace()));
			}
			HashSet<Place> connectedCities = new HashSet<Place>();
			for (Place p : temp) {
				if (p.isCity() || p.isStart()) {
					connectedCities.add(p);
				}
			}
			Object[] possibilities = new Object[connectedCities.size()];
			int i=0;
			for (Place p : connectedCities) {
				possibilities[i] = p.getName();
				i++;
			}
			String str = (String)JOptionPane.showInputDialog(frame,bundle.getString("selectDest"),bundle.getString("takeShipTo"),
					JOptionPane.PLAIN_MESSAGE,null,possibilities,possibilities[0]);

			Place destination = null;
			for (Place p : connectedCities) {
				if (p.getName().equals(str)) {
					destination = p;
				}
			}
			if (destination != null) {
				rollDice.setEnabled(true);
				boardShip.setEnabled(false);
				boardPlane.setEnabled(false);
				endTurn.setEnabled(false);
				//rollToTurn.setEnabled(false);
				//rollToTurn.setSelected(false);
				rollToken.setEnabled(false);
				boardShipNoMoney.setEnabled(false);
				player[turn].setMoney(player[turn].getMoney() - 100);
				playerMoney[turn].setText(" Pound " + player[turn].getMoney());
				player[turn].setLockedDestination(destination);
			}
		}

		/*Board ship without money*/
		if (e.getSource() == boardShipNoMoney) {
			HashSet<Place> temp = new HashSet<Place>();
			for (Place p : player[turn].getPlace().getConnectedBySea()) {
				temp.addAll(getConnectedBySea(p,player[turn].getPlace()));
			}
			HashSet<Place> connectedCities = new HashSet<Place>();
			for (Place p : temp) {
				if (p.isCity() || p.isStart()) {
					connectedCities.add(p);
				}
			}
			Object[] possibilities = new Object[connectedCities.size()];
			int i=0;
			for (Place p : connectedCities) {
				possibilities[i] = p.getName();
				i++;
			}
			String str = (String)JOptionPane.showInputDialog(frame,bundle.getString("selectDest"),bundle.getString("takeShipTo"),
					JOptionPane.PLAIN_MESSAGE,null,possibilities,possibilities[0]);

			Place destination = null;
			for (Place p : connectedCities) {
				if (p.getName().equals(str)) {
					destination = p;
				}
			}
			if (destination != null) {
				rollDice.setEnabled(true);
				boardShip.setEnabled(false);
				boardPlane.setEnabled(false);
				endTurn.setEnabled(false);
				//rollToTurn.setEnabled(false);
				//rollToTurn.setSelected(false);
				rollToken.setEnabled(false);
				boardShipNoMoney.setEnabled(false);
				player[turn].setBoardedNoMoney(true);
				player[turn].setLockedDestination(destination);
			}
		}
		


		if (e.getSource() == newGame) {
			if (same.isSelected()) { // If the same players are desired...
				int rand;
				Player temp;
				for (int i = 0; i < player.length ; i++) { // ...we just reshuffle the existing players...
					rand = (int)(Math.random()*player.length);
					temp = player[i];
					player[i] = player[rand];
					player[rand] = temp;
				}
				for (Player p : player) { // ...reset all their variables...
					p.reset();
				}
				new PlayingField(player,height); // ...and start a new game with them.
			}
			else {
				new GameSetupMenu(); // Otherwise we fire up a new setup menu.
			}
			frame.dispose();
		}
		
		if (e.getSource() == quit) {
			System.exit(0);
		}
	}
}
