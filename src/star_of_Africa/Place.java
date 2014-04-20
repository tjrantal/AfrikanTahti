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

import java.util.HashSet;
import java.util.ArrayList;	/**/

/**
 * This class represents any place on the map where it is possible to move
 * a gamepiece. Every place also keeps track of which other places it is
 * connected to, and how.
 * 
 * @author Daniel Suni
 * @version 1.0.0
 */
public class Place {
	
	private int x;
	private int y;
	private HashSet<Place> connectedByLand = new HashSet<Place>();
	private HashSet<Place> connectedBySea = new HashSet<Place>();
	private HashSet<Place> connectedByAir = new HashSet<Place>();
	private ArrayList<Place> routeTo = new ArrayList<Place>();	/*To store the route to this place*/
	private boolean city;  // Is this a city or not?
	private boolean start; // Is this a starting point or not?
	private boolean hostile; // True for the special places near St. Helena and Sahara, where the player can be captured
	private Token token;  // The token for this place. If empty or not a city, this value is null.
	private String name = ""; // Only cities and starting points have names.
	
	/**
	 * Constructs a place.
	 * 
	 * @param x		the x-coordinate of the place relative to the original unresized map
	 * @param y		the y-coordinate of the place relative to the original unresized map
	 * @param city	the information of whether the place is a city or not
	 * @param start	the information of whether the place is a starting point or not
	 */
	public Place(int x, int y, boolean city, boolean start) {
		this.x = x;
		this.y = y;
		this.city = city;
		this.start = start;
		hostile = false;
	}

	/**Return a copy of the route to this place*/
	public ArrayList<Place> getRouteTo(){
		ArrayList<Place> route = new ArrayList<Place>();
		for (int i = 0;i<routeTo.size();++i){
			route.add(routeTo.get(i));
		}
		return route;
	}
	
	/**Set the route to this place*/
	public void setRouteTo(ArrayList<Place> arr){
		routeTo = arr;
	}
	
	/**Reset the route to this place*/
	public void resetRoute(){
		routeTo.clear();
	}
	
	/**Append to the route to this place*/
	public void appendRouteTo(Place place){
		routeTo.add(place);
	}
	
	/**Print the route to this place*/
	public void printRouteTo(){
		for (int r = 0;r<routeTo.size();++r){
			System.out.print("X "+routeTo.get(r).getX()+" Y "+routeTo.get(r).getY()+" ");
		}
		System.out.println("");
	}


	public HashSet<Place> getConnectedByLand() {
		return connectedByLand;
	}
	
	public HashSet<Place> getConnectedBySea() {
		return connectedBySea;
	}
	
	public HashSet<Place> getConnectedByAir() {
		return connectedByAir;
	}
	
	public boolean isCity() {
		return city;
	}
	
	public boolean isStart() {
		return start;
	}
	
	public boolean isHostile() {
		return hostile;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public String getName() {
		return name;
	}
	
	public Token getToken() {
		return token;
	}
	
	public void setConnectedByLand(Place place) {
		connectedByLand.add(place);
	}
	
	public void setConnectedBySea(Place place) {
		connectedBySea.add(place);
	}
	
	public void setConnectedByAir(Place place) {
		connectedByAir.add(place);
	}
	
	public void setToken(Token token) {
		this.token = token;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setHostile() {
		hostile = true;
	}
	
}
