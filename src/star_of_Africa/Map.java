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
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This class provides the map interface. It implements MouseListener so that
 * moving the gamepiece by clicking is possible and it consists of a JLayeredPane
 * inside a JScrollPane. All visual elements in this class depend on the zoom
 * level, the screen resolution, and their original size. For this reason the
 * algorithms to calculate their position become rather convoluted.
 * 
 * @author Daniel Suni
 * @version 1.0.0
 */
public class Map extends JPanel implements MouseListener {
	
	// I've tried to use explanatory constants instead of "magic numbers" to reduce the convolutedness of the formulae
	private final double RATIO = 4.0/3;
	private final int WIDTH = 1737; // Original map image size.
	private final int HEIGHT = 2373;
	private final int PIECE_SCALE_CONSTANT = 60; // Determined by trial and error
	private final int PIECE_SEPARATION_CONSTANT = 10; // Distance between pieces that end up in the same place. (Also found by T&E)
	private final int ANIMATION_FRAME_DELAY = 20;//83; // 1000 / 12 ~= 83. I.e. will give an animation of 12 fps
	private double scale; // Used to calculate the actual size of the images, given original image size, monitor resolution and zoom level
	private boolean tokensRevealed = false; // After the game all tokens are revealed, and this class must know this
    private ImageIcon gameBoard,tokenBack,cityRing,smallRing;
    private ImageIcon[] piece;
    private Image image,original,tokenImg,tokenOriginal,cityImg,smallImg,cityOriginal,smallOriginal;
    private Dimension dim;
    private JScrollPane scrollPane;
    private JLayeredPane layerPane = new JLayeredPane();
    private JLabel background;
    private JLabel[] pieceLabel,tokenLabel;
    private Player[] player;
    private HashSet<Place> cities,possibleMoves;
    private HashSet<JLabel> rings = new HashSet<JLabel>();
    private PlayingField pf;

    /**
     * Constructs the map. By default the map size will be the screen size - 150
     * pixels (to make room for other components). Tokens will be placed an all of
     * the cities, and the players will be placed on the starting point they picked
     * earlier.
     * 
     * @param player	the players paticipating in the game.
     * @param cities	the cities on the map.
     */
    public Map(Player[] player, HashSet<Place> cities) {
    	this.player = player;
    	this.cities = cities;
    	possibleMoves = new HashSet<Place>();
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));    
        pieceLabel = new JLabel[player.length];
        tokenLabel = new JLabel[30]; // 30 cities on the map
        piece = new ImageIcon[player.length];
              
        // Get the screen size
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    dim = new Dimension(ge.getDefaultScreenDevice().getDisplayMode().getWidth(),ge.getDefaultScreenDevice().getDisplayMode().getHeight());
        dim = trimDimension(dim);
        scale = HEIGHT/dim.getHeight();
        
        // Set up the background image (i.e. the map)
        URL imgURL = getClass().getResource("img/board.png");
        gameBoard = new ImageIcon(imgURL);
        original = gameBoard.getImage();
        image = original.getScaledInstance(-1, (int)dim.getHeight(), Image.SCALE_FAST);
        gameBoard.setImage(image);
        background = new JLabel(gameBoard);      
        background.setBounds(0, 0, gameBoard.getIconWidth(), gameBoard.getIconHeight());
        
        // Set up rings (to indicate where player may move)
        imgURL = getClass().getResource("img/ring.png");
        cityRing = new ImageIcon(imgURL);
        cityOriginal = cityRing.getImage();
        imgURL = getClass().getResource("img/ring_small.png");
        smallRing = new ImageIcon(imgURL);
        smallOriginal = smallRing.getImage();
        
        // Set up the game pieces
        for (int i=0; i<player.length; i++) {
        	piece[i] = player[i].getGamePiece().getResizedIcon((int)(PIECE_SCALE_CONSTANT/scale));
        	pieceLabel[i] = new JLabel(piece[i]);
        	pieceLabel[i].setBounds(getPieceX(player[i],i),getPieceY(player[i],i),piece[i].getIconWidth(),piece[i].getIconHeight());
        	layerPane.add(pieceLabel[i], new Integer(3),i);
        }
        
        // Set up the tokens
        imgURL = getClass().getResource("img/token.png");
        tokenBack = new ImageIcon(imgURL);
        tokenOriginal = tokenBack.getImage();
        tokenImg = tokenOriginal.getScaledInstance(-1, (int)(tokenBack.getIconHeight()/scale), Image.SCALE_DEFAULT);
        tokenBack.setImage(tokenImg);
        int i=0;
        for (Place p : cities) {
        	tokenLabel[i] = new JLabel(tokenBack);
        	tokenLabel[i].setBounds((int)(p.getX()/scale-tokenBack.getIconWidth()/2.0),
        			(int)(p.getY()/scale-tokenBack.getIconHeight()/2.0), tokenBack.getIconWidth(), tokenBack.getIconHeight());
        	layerPane.add(tokenLabel[i], new Integer(1),i);
        	i++;
        }
        
        // Set up the LayeredPane
        layerPane.setPreferredSize(new Dimension(gameBoard.getIconWidth(), gameBoard.getIconHeight()));     
        layerPane.add(background, new Integer(0));
        
        // Set up the ScrollPane
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(layerPane);
        add(scrollPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Set up MouseListener
        layerPane.addMouseListener(this);
        addMouseListener(this);
    }

    // Trims the dimensions to a size that will fit on the users screen 
    private Dimension trimDimension (Dimension dim) {
    	dim.setSize(dim.getWidth(), dim.getHeight()-150); // Reduce height to make room for other components
    	dim.width = Math.min((int)(dim.height * RATIO),(int)dim.getWidth());
    	return dim;	
    }
    
    // Returns the X-coordinate of the gamepiece
    private int getPieceX (Player p, int level) {
    	int temp = (int)(p.getPlace().getX()/scale - piece[level].getIconWidth()/2.0);
    	for (int i=level-1; i>=0; i--) {
    		if (p.getPlace().equals(player[i].getPlace())) {
    			temp+=(int)(PIECE_SEPARATION_CONSTANT/scale);
    		}
    	}
    	return temp;
    }
    
    // Returns the Y-coordinate of the gamepiece
    private int getPieceY (Player p, int level) {
    	return (int)(p.getPlace().getY()/scale - piece[level].getIconHeight()/2.0);
    }
    
    /**
     * Checks which place the player wants to move his gamepiece to.
     * 
     * @param x		the x-coordinate of the mouse click
     * @param y		the y-coodrinate of the mouse click
     * @return		the Place that corresponds to the mouse click coordinates,
     * 				<code>null</code> if there is no valid Place that corresponds.
     */
    private Place getMove(int x, int y) {
    	double distance;
    	// If player click within the circle of any of the possible place to move to, return that place...
    	for (Place p : possibleMoves) {
    		distance = Math.sqrt(Math.pow(x-(p.getX()/scale), 2) + Math.pow(y-(p.getY()/scale), 2));
    		if ((p.isCity() || p.isStart()) && distance < cityRing.getIconHeight()/2.0) {
    			return p;
    		}
    		if (!p.isCity() && distance < smallRing.getIconHeight()/2.0) {
    			return p;
    		}
    	}
    	// ...otherwise return null
    	return null;
    }
    
    /**
     * Changes the zoom of the map.
     * 
     * @param zoomlevel	the new zoomlevel. Should have a value >= 1.00.
     * @return			<code>true</code> if the operation succeeded, <code>false</code>
     * 					if the resulting image would have been too big. An image too big,
     * 					can cause a memory heap overflow.
     */
    public boolean zoom (double zoomlevel) {
    	// Resize the map
    	int y = (int)(dim.getHeight()*zoomlevel);
    	if (y > 3000) {
    		return false;
    	}
    	image = original.getScaledInstance(-1, y, Image.SCALE_FAST);
    	gameBoard.setImage(image);
    	int x = gameBoard.getIconWidth();
    	
    	// Rezise the Layered Pane
    	layerPane.setPreferredSize(new Dimension(x, y));
    	background.setBounds(0, 0, x, y);
        background.setIcon(gameBoard);
        
        // Resize and reposition the game pieces
        scale = HEIGHT/(dim.getHeight()*zoomlevel);
        for (int i=0; i<player.length; i++) {
        	piece[i] = player[i].getGamePiece().getResizedIcon((int)(PIECE_SCALE_CONSTANT/scale));
        	pieceLabel[i].setIcon(piece[i]);
        	pieceLabel[i].setBounds(getPieceX(player[i],i),getPieceY(player[i],i),piece[i].getIconWidth(),piece[i].getIconHeight());
        }
        
        // Resize and reposition the tokens
        tokenBack.setImage(tokenOriginal);
        tokenImg = tokenOriginal.getScaledInstance(-1, (int)(tokenBack.getIconHeight()/scale), Image.SCALE_DEFAULT);
        tokenBack.setImage(tokenImg);
        int i=0;
        for (Place p : cities) {
        	if (p.getToken() != null) {
        		tokenLabel[i].setIcon(tokenBack);
        	}
        	else {
        		tokenLabel[i].setIcon(null);
        	}
        	tokenLabel[i].setBounds((int)(p.getX()/scale-tokenBack.getIconWidth()/2.0),
        			(int)(p.getY()/scale-tokenBack.getIconHeight()/2.0), tokenBack.getIconWidth(), tokenBack.getIconHeight());
        	i++;
        }
        
        // Resize and reposition movement markers
        for (JLabel l : rings) {
        	layerPane.remove(l);
        }
        rings.clear();
        showMoveOptions(possibleMoves);
 
        // Set scrollbar policy
        if (zoomlevel > 1) {
        	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        else {
        	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }
        
        // If tokens are already revealed we want them to stay revealed
        if (tokensRevealed) {
        	revealTokens();
        }
        
        // Make the changes visible
        scrollPane.imageUpdate(image, -1, 0, 0, x, y);
        scrollPane.revalidate();
        return true;
    }
    
    /**
     * Displays all the places that the player is allowed to move to by encircling
     * them in red.
     * 
     * @param possibleMoves	the places to be encircled
     */
    public void showMoveOptions(HashSet<Place> possibleMoves) {
    	this.possibleMoves = possibleMoves;
    	cityRing.setImage(cityOriginal);
    	cityImg = cityOriginal.getScaledInstance(-1, (int)(cityRing.getIconHeight()/scale), Image.SCALE_DEFAULT);
    	cityRing.setImage(cityImg);
    	smallRing.setImage(smallOriginal);
    	smallImg = smallOriginal.getScaledInstance(-1, (int)(smallRing.getIconHeight()/scale), Image.SCALE_DEFAULT);
    	smallRing.setImage(smallImg);
    	for (Place p : possibleMoves) {
    		if (p.isCity() || p.isStart()) {
    			JLabel temp = new JLabel(cityRing);
    			temp.setBounds((int)(p.getX()/scale - cityRing.getIconWidth()/2.0), (int)(p.getY()/scale - cityRing.getIconHeight()/2.0),
    					cityRing.getIconWidth(), cityRing.getIconHeight());
    			rings.add(temp);
    		}
    		else {
    			JLabel temp = new JLabel(smallRing);
    			temp.setBounds((int)(p.getX()/scale - smallRing.getIconWidth()/2.0), (int)(p.getY()/scale - smallRing.getIconHeight()/2.0),
    					smallRing.getIconWidth(), smallRing.getIconHeight());
    			rings.add(temp);
    		}
    	}
    	for (JLabel l : rings) {
    		layerPane.add(l,new Integer(2));
    	}
    }
    
    /**
     * Makes changes visible by updating the visual components.
     * (Typically used for changes not related to zooming.)
     */
    public void refresh() {
    	// Gamepieces
    	for (int i=0; i<player.length; i++) {
        	pieceLabel[i].setBounds(getPieceX(player[i],i),getPieceY(player[i],i),piece[i].getIconWidth(),piece[i].getIconHeight());
        }
    	// Tokens
    	int i=0;
        for (Place p : cities) {
        	if (p.getToken() != null) {
        		tokenLabel[i].setIcon(tokenBack);
        	}
        	else {
        		tokenLabel[i].setIcon(null);
        	}
        	tokenLabel[i].setBounds((int)(p.getX()/scale-tokenBack.getIconWidth()/2.0),
        			(int)(p.getY()/scale-tokenBack.getIconHeight()/2.0), tokenBack.getIconWidth(), tokenBack.getIconHeight());
        	i++;
        }
        if (tokensRevealed) {
        	revealTokens();
        }
        // Make the changes visible
        scrollPane.imageUpdate(image, -1, 0, 0, gameBoard.getIconWidth(), gameBoard.getIconHeight());
        this.paintImmediately(0, 0, this.getWidth(), this.getHeight());
    }
    
    /**
     * Reveals all unopened tokens. (Used at the end of the game.)
     */
    public void revealTokens() {
    	tokensRevealed = true;
    	int i=0;
        for (Place p : cities) {
        	if (p.getToken() != null) {
        		tokenLabel[i].setIcon(p.getToken().getResizedIcon(tokenLabel[i].getHeight()));
        	}
        	else {
        		tokenLabel[i].setIcon(null);
        	}
        	tokenLabel[i].setBounds((int)(p.getX()/scale-tokenBack.getIconWidth()/2.0),
        			(int)(p.getY()/scale-tokenBack.getIconHeight()/2.0), tokenBack.getIconWidth(), tokenBack.getIconHeight());
        	i++;
        }
        scrollPane.imageUpdate(image, -1, 0, 0, gameBoard.getIconWidth(), gameBoard.getIconHeight());
    }
    
    /**
     * Animates the moving of a player's gamepiece from one place to another.
     * 
     * @param destination	the Place the piece is to be moved to
     * @param turn			the current turn. (This reveals which player is moving,
     * 						and by implication which piece is to be moved.)
     */
    public void showMove(Place destination, int turn) {
    	int startx = pieceLabel[turn].getX();
    	int starty = pieceLabel[turn].getY();
    	int destx = (int)(destination.getX()/scale - piece[turn].getIconWidth()/2.0);
    	int desty = (int)(destination.getY()/scale - piece[turn].getIconHeight()/2.0);
    	int deltax = startx - destx;
    	int deltay = starty - desty;
    	int width = piece[turn].getIconWidth() + Math.abs(deltax)/36 + 3; // Width of box that need to be repainted (+3 pixel margin)
    	int height = piece[turn].getIconHeight() + Math.abs(deltay)/36 + 3 ; // Height
    	int x = startx;
    	int y = starty;
    	int newx, newy;
    	long time = System.currentTimeMillis();
    	for (int i = 1 ; i <= 36 ; i++) { // 36 frames @ 12 fps = 3 sec
    		newx = x;
    		newy = y;
    		x = (int)(startx - deltax*(i/36.0));
    		y = (int)(starty - deltay*(i/36.0));
    		if (deltax > 0) {
    			newx = x;
    		}
    		if (deltay > 0) {
    			newy = y;
    		}
    		pieceLabel[turn].setBounds(x, y, pieceLabel[turn].getWidth(), pieceLabel[turn].getHeight());
    		scrollPane.imageUpdate(image, -1, 0, 0, gameBoard.getIconWidth(), gameBoard.getIconHeight());
    		this.paintImmediately(newx + layerPane.getX(), newy + layerPane.getY(), width, height);
    		time += ANIMATION_FRAME_DELAY;
    		try {
    	        Thread.sleep(Math.max(0,time - System.currentTimeMillis()));
    		}
    		catch (InterruptedException e) {
    	        e.printStackTrace();
    		}
    	}
    	for (JLabel l : rings) {
        	layerPane.remove(l);
        }
    	rings.clear();
        possibleMoves.clear();
    }
    
    public void setPlayingField(PlayingField pf) {
    	this.pf=pf;
    }
    
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    public void mouseClicked(MouseEvent e) {
    	if (e.getButton() == MouseEvent.BUTTON1) {
    		Place destination = getMove(e.getX(),e.getY());
    		if (destination != null) {
    	        pf.moveMade(destination);
    		}
    	}
    	
    }
    
}



