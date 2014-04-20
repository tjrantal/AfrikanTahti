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

/**
 * This class holds all the information about a player of the game.
 * 
 * @author Daniel Suni
 * @version 1.0.0
 */
public class Player {

	private boolean human;
	private Place place;
	private Place lockedDestination; // When seabound for a destination.
	private boolean boardedNoMoney;	//When seabound without money
	private boolean tangier;
	private int money;
	private boolean hasFoundTheStar;
	private int turnsLeftAsSlave; // When stuck on Slave Coast - not the same thing as being captured
	private boolean captured; // When being captured by pirates or beduins
	private boolean stranded;
	private String name;
	private GamePiece gamePiece;
	
	/**
	 * Constructs a player
	 * 
	 * @param human		is the player human? <code>false</code> == AI.
	 * @param tangier	does the player start at Tangier? <code>false</code> == Cairo.
	 * @param gamePiece	the player's gamepiece
	 * @param name		the player's name
	 */
	public Player (boolean human, boolean tangier, GamePiece gamePiece, String name) {
		this.gamePiece = gamePiece;
		this.tangier = tangier;
		this.human = human;
		this.name = name;
		money = 300;
		turnsLeftAsSlave = 0;
		hasFoundTheStar = false;
		captured = false;
		stranded = false;
		boardedNoMoney = false;
	}
	
	public boolean isCaptured() {
		return captured;
	}
	
	public boolean isHuman() {
		return human;
	}

	public GamePiece getGamePiece() {
		return gamePiece;
	}

	public String getName() {
		return name;
	}

	public boolean hasFoundTheStar() {
		return hasFoundTheStar;
	}
	
	public boolean isStranded() {
		return stranded;
	}
	
	public boolean startsAtTangier() {
		return tangier;
	}

	public int getMoney() {
		return money;
	}

	public Place getPlace() {
		return place;
	}

	public Place getLockedDestination() {
		return lockedDestination;
	}
	
	public boolean getBoardedNoMoney(){
		return boardedNoMoney;
	}
	
	public void setBoardedNoMoney(boolean value){
		boardedNoMoney = value;
	}
	
	public int getTurnsLeftAsSlave() {
		return turnsLeftAsSlave;
	}

	public void setCaptured(boolean captured) {
		this.captured = captured;
	}

	public void setHasFoundTheStar() {
		this.hasFoundTheStar = true;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void setPlace(Place place) {
		this.place = place;
	}
	
	public void resetRoute() {
		place.resetRoute();
	}

	public void setTurnsLeftAsSlave(int turnsLeftAsSlave) {
		this.turnsLeftAsSlave = turnsLeftAsSlave;
	}
	
	public void setStranded() {
		this.stranded = true;
	}
	
	public void setLockedDestination(Place lockedDestination) {
		this.lockedDestination = lockedDestination;
	}
	
	// For restarting the game with the same players.
	public void reset() {
		this.captured = false;
		this.hasFoundTheStar = false;
		this.lockedDestination = null;
		this.money = 300;
		this.stranded = false;
		this.turnsLeftAsSlave = 0;
	}
}
