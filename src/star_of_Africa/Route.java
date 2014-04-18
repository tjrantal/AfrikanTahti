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

import java.util.ArrayList;

/**
 * This class can hold a route from Place A to Place B. The route consists
 * of an ArrayList of places, where the first element is the source, and the last
 * is the destination. The route may be a mix of land air and sea routes and the
 * requirements for a valid route is that:
 * <ul>
 * <li>Each place on the route must *somehow* be connected to both the previous and
 * the next place.
 * <li>The route must consist of two places or more.
 * <li>Each place on the route must appear only once.
 * </ul></p>
 * This class implements comparable, so that one may directly compare two routes and
 * find out which one will statistically take the fewest turns.
 * 
 * @author Daniel Suni
 * @version 1.0.5
 */
public class Route implements Comparable {

	private final double AVERAGE_TURNS_PER_STEP = 1/3.5;
	private final double AVERAGE_TURN_LOSS_BOARDING = 1/2.5;
	private ArrayList<Place> places = new ArrayList<Place>();
	
	/**
	 * Calculates the total cost of a route.
	 * 
	 * @return	the cost to travel along the route from start to finish
	 */
	public int getCost() {
		int cost = 0;
		if (places.size() <= 1) {
			return cost;
		}
		
		for (int i=0 ; i<places.size()-1 ; i++) {
			if (places.get(i).getConnectedByAir().contains(places.get(i+1))) {
				cost += 300;
			}
			if ((places.get(i).isCity() || places.get(i).isStart()) && places.get(i).getConnectedBySea().contains(places.get(i+1))) {
				cost += 100;
			}
		}
		return cost;
	}
	
	/**
	 * Calculates how many turns it will take ON AVERAGE to follow the route from start to finish.
	 * 
	 * @return	the number of turns the route is likely to take
	 */
	public double getEstimatedTurns() {
		double turns = 1;
		if (places.size() <= 2) {
			return turns;
		}
		
		for (int i=1 ; i<places.size()-1 ; i++) {
			if (places.get(i).getConnectedByAir().contains(places.get(i+1))) {
				if (i!=places.size()-1 && !places.get(i-1).getConnectedByAir().contains(places.get(i))) {
					turns += AVERAGE_TURN_LOSS_BOARDING;
				}
			}
			else if (places.get(i).getConnectedBySea().contains(places.get(i+1))) {
				turns += AVERAGE_TURNS_PER_STEP; // Traveling at sea is the same as traveling on land...
				// ...except that you lose some "dice points" due to boarding. If you start in a city, or the destination
				// is a city nothing is lost though.
				if (places.get(i).isCity() && i!=0) { 
					turns += AVERAGE_TURN_LOSS_BOARDING;
				}
				if (places.get(i+1).isCity() && i!=places.size()-1) {
					turns += AVERAGE_TURN_LOSS_BOARDING;
				}
			}
			else {
				turns += AVERAGE_TURNS_PER_STEP;
			}
		}
		return turns;
	}
	
	public void add (Place p) {
		places.add(p);
	}
	
	public void addAll(Route r) {
		places.addAll(r.getList());
	}
	
	public void clear() {
		places.clear();
	}
	
	public int size() {
		return places.size();
	}
	
	public Place get(int p) {
		if (p < places.size() && p >= 0) {
			return places.get(p);
		}
		return null;
	}
	
	public boolean contains(Place p) {
		return places.contains(p);
	}

	/**
	 * Calculates how far down the route in question a diceroll will take the player,
	 * taking into account the fact that the route may be a mix of land, sea and air
	 * connections.
	 * 
	 * @param rand	the value of the dice roll
	 * @return		the Place on the route in question where the given dice roll will
	 * 				take the player.
	 */
	public Place getDestination(int rand) {
		// Immediately checks if the first leg is by air, so that such a case won't be caught by the junction check later.
		if (places.get(0).getConnectedByAir().contains(places.get(1))) {
			return places.get(1);
		}
		
		int i=0;
		while (i < places.size()-1) {
			// Are we arriving at a junction where player must board a ship or plane?
			if (places.get(i).getConnectedByAir().contains(places.get(i+1)) || 
			(i > 0 && (places.get(i).isCity() || places.get(i).isStart()) && places.get(i).getConnectedBySea().contains(places.get(i+1)))) {
				return places.get(i);
			}
			if (i == rand) {
				return places.get(i);
			}
			if (places.get(i).getConnectedBySea().contains(places.get(i+1)) && 
					(places.get(i+1).isCity() || places.get(i+1).isStart())) {
				return places.get(i+1);
			}
			i++;
		}
		return places.get(places.size()-1);
	}
	
	public Place getLockedDestination() {
		int i = 1;
		while (true) {
			if (places.get(i).isCity() || places.get(i).isStart()) {
				return places.get(i);
			}
			i++;
		}
	}
	
	public ArrayList<Place> getList() {
		return places;
	}
	
	public int compareTo(Object o) throws ClassCastException {
		if (o == null) {
			return -1;
		}
		if (!(o instanceof Route)) {
			throw new ClassCastException("A Route object is expected.");
		}
		if (this.getEstimatedTurns() - ((Route)(o)).getEstimatedTurns() < 0) {
			return -1;
		}
		if (this.getEstimatedTurns() - ((Route)(o)).getEstimatedTurns() > 0) {
			return 1;
		}
		return 0;
	}
	
	// Not really used by the game itself, but sometimes useful for debugging
	public String toString() {
		String str = "***\n";
		for (Place p : places) {
			str += p.getName()+" "+p.getX()+" "+p.getY()+"\n";
		}
		return str;
	}
}
