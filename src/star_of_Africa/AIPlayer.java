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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class is reponsible for all AI logic. It decides where the players should move,
 * and which route they should take. Upon starting it goes into waiting, and needs to be
 * notified to make a move. The reason the AI was implemented as a separate thread was
 * not to tie down the GUI while the AI players were making their moves.
 * 
 * @author Daniel Suni
 * @version 1.0.4
 */
public class AIPlayer extends Thread {

	private final long DELAY = 2000;
	private Player[] player;
	private PlayingField pf;
	private SoundPlayer soundPlayer;
	private int turn;
	private Place[] startingPoints;
	private HashSet<Place> cities;
	private boolean nothingToDo = true;
	private boolean firstRun = true;
	
	public AIPlayer(HashSet<Place> cities, Place[] startingPoints, Player[] player, SoundPlayer soundPlayer) {
		this.cities = cities;
		this.startingPoints = startingPoints;
		this.player = player;
		this.soundPlayer = soundPlayer;
	}
	
	private void pause(long time) {
		try {
			sleep(time);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the method that gets kicked off by the run()-method whenever it is notified.
	 */
	private void makeAIMove() {
		int rand = (int)(Math.random()*6) + 1;
		int budget = pf.calculateBudget();
		boolean moved = false;
		// An enslaved player can't do anything
		if (player[turn].getTurnsLeftAsSlave() > 0) {
			player[turn].setTurnsLeftAsSlave(player[turn].getTurnsLeftAsSlave()-1);
			return;
		}

		// Neither can a captured one who doesn't roll 1 or 2
		if (player[turn].isCaptured() && rand > 2) {
			pf.showDiceRoll(rand);
			pause(DELAY);
			return;
		}
		if (player[turn].isCaptured() && rand <= 2) {
			player[turn].setCaptured(false);
		}

		// Or a stranded one...
		if (pf.checkStranded(player[turn])) {
			return;
		}

		// Is the player shipbound?
		if (player[turn].getLockedDestination() != null) {
			ArrayList<Place> tempRoute = pf.getSeaRoute(player[turn].getPlace(),null);
			tempRoute.trimToSize();
			pf.showDiceRoll(rand);
			pause(DELAY);
			if (tempRoute.size() - 1 <= rand) {
				pf.moveMade(player[turn].getLockedDestination());
			}
			else {
				pf.moveMade(tempRoute.get(rand));
			}
			moved = true;
		}

		// Does the player have the star?
		else if (player[turn].hasFoundTheStar()) {
			Route r = new Route();
			Route[] routes = new Route[3];
			routes[0] = getShortestRoute(startingPoints[0],budget,budget >= 300);
			routes[1] = getShortestRoute(startingPoints[1],budget,budget >= 300);
			routes[2] = calculateAirRoute(budget);
			if (routes[0].compareTo(routes[1]) <= 0 && routes[0].compareTo(routes[2]) <= 0) {
				r = routes[0];
			}
			else if (routes[1].compareTo(routes[0]) <= 0 && routes[1].compareTo(routes[2]) <= 0) {
				r = routes[1];
			}
			else {
				r = routes[2];
			}
			// If traveling by sea or by air, the player has to pay...
			if (r.get(0).isCity() && r.get(0).getConnectedBySea().contains(r.get(1))) {
				player[turn].setMoney(player[turn].getMoney() - 100);
				player[turn].setLockedDestination(r.getLockedDestination());
			}
			if (r.get(0).isCity() && r.get(0).getConnectedByAir().contains(r.get(1))) {
				soundPlayer.play("snd/airplane.wav");
				player[turn].setMoney(player[turn].getMoney() - 300);
			}
			else {
				pf.showDiceRoll(rand);
				pause(DELAY);
			}
			pf.moveMade(r.getDestination(rand));
		}

		// Is the player standing on a city with a token?
		else if (player[turn].getPlace().getToken() != null) {
			pf.showDiceRoll(rand);
			pause(DELAY);
			if (rand >= 4) {
				pf.openToken();
				pause(DELAY);
			}
		}

		else {
			// If we're not standing on a token, find the best token, and the best route to it
			if (player[turn].getPlace().getToken() == null) {
				Route route = getShortestRoute(getOptimalDestination(budget),budget,budget >= 300);

				// If traveling by sea or by air, the player has to pay...
				if ((route.get(0).isCity() || route.get(0).isStart()) && route.get(0).getConnectedBySea().contains(route.get(1))) {
					player[turn].setMoney(player[turn].getMoney() - 100);
					player[turn].setLockedDestination(route.getLockedDestination());
				}
				if ((route.get(0).isCity() || route.get(0).isStart()) && route.get(0).getConnectedByAir().contains(route.get(1))) {
					soundPlayer.play("snd/airplane.wav");
					player[turn].setMoney(player[turn].getMoney() - 300);
				}
				else {
					pf.showDiceRoll(rand);
					pause(DELAY);
				}
				pf.moveMade(route.getDestination(rand));
				moved = true;
			}
		}

		// If we moved onto a token we probably want to open it
		if (moved) {
			// Are we on the mainland with money enough to open the token?
			if (player[turn].getPlace().getToken() != null && player[turn].getMoney() >= 100 && !pf.playerOnIsland(player[turn])) {
				player[turn].setMoney(player[turn].getMoney() - 100);
				pf.updateMoney();
				pf.openToken();
				pause(DELAY);
			}
			// Are we on an island with money enough to open the token AND still get off the island?
			if (player[turn].getPlace().getToken() != null && player[turn].getMoney() >= 200 && pf.playerOnIsland(player[turn])) {
				player[turn].setMoney(player[turn].getMoney() - 100);
				pf.updateMoney();
				pf.openToken();
				pause(DELAY);
			}
		}
		pf.updateMoney();
	}
	
	/**
	 * Calculates the optimal destination. For now the method isn't all that clever. It basically
	 * a slightly enhanced "look-up-the-nearest-token"-routine. Despite its simplicity it still
	 * gives good results most of the time.
	 * 
	 * @param budget	how much money the player has allocated to traveling
	 * @return			the place that is deemed to be optimal
	 */
	private Place getOptimalDestination(int budget) {
		int i = 1;
		boolean found = false;
		HashSet<Place> temp;
		Place destination = null;
		int land = 1000;
		int sea = 0;
		// Get nearest token by land
		do {
			temp = calculateRoute(player[turn].getPlace(),i,false);
			for (Place p : temp) {
				if (p.getToken() != null) {
					destination = p;
					land = i;
					found = true;
				}
			}
			i++;
			if (i > 50) {
				break;
			}
		} while (!found);

		// Get nearest token by sea
		if (budget >= 100) {
			found = false;
			i = 1;
			do {
				temp = calculateRoute(player[turn].getPlace(),i,true);
				for (Place p : temp) {
					if (p.getToken() != null) {
						if (land - i > 3) {
							destination = p;
						}
						sea = i;
						found = true;
					}
				}
				i++;
			} while (!found);
		}

		// If there aren't any tokens nearby, and money isn't a problem, suggest a plane reachable destination.
		if (budget >= 300 && Math.min(land, sea) > 8) {
			for (Place p : player[turn].getPlace().getConnectedByAir()) {
				if (p.getToken() != null) {
					return p;
				}
			}
		}
		return destination;
	} 

	/**
	 * Looks up the next leg of the route by either land or sea - a leg being a part of the route
	 * that has no intersections to worry about. As soon as an intersection is encountered, the
	 * method returns the ball to its caller, which gets to sort it out.
	 * 
	 * @param previous		the route so far. Needed so that the method knows not to backtrack.
	 * @param destination	the destination
	 * @param sea			shall the method check sea routes or not
	 * @param seaOnly		shall the method check sea routes only
	 * @return				the previous Route appended with the leg to the next intersection. If
	 * 						the route doesn't lead to the destination, or leads to the destination
	 * 						in a convoluted manner that can not be the best route, <code>null</code>
	 * 						will be returned.
	 */
	private Route getRouteToNextIntersection(Route previous, Place destination, boolean sea, boolean seaOnly) {
		if (seaOnly) {
			sea = true;
		}
		Place temp = previous.get(previous.size() - 1);
		Place newPlace = null;
		Route newRoute = new Route();
		HashSet<Place> helper = new HashSet<Place>();
		int i;
		while (true) {
			i = 0;
			helper.clear();
			if (!seaOnly) {
				helper.addAll(temp.getConnectedByLand());
			}
			if (sea) {
				helper.addAll(temp.getConnectedBySea());
			}
			for (Place p : helper) { // Get all neighbors to the last place
				if (!previous.contains(p) && !newRoute.contains(p)) { // If that place isn't already on the route...
					i++;
					newPlace = p;	// ...add it.
					if (p.equals(destination)) { // This means we're done with this route.
						newRoute.add(p);
						return newRoute;
					}
				}
			}
			// Dead end, loop, or sea route when we're not looking for one.
			if (i == 0) {
				return null;
			}
			// Just another step on the route
			if (i == 1) {
				newRoute.add(newPlace);
			}
			// We've arrived at an intersection
			if (i > 1) {
				return newRoute;
			}
			temp = newPlace;
		}
	} 

	/**
	 * Gets the shortest route, where shortest means shortest average time to get there.
	 * 
	 * @param destination	the destination the player wants to go to
	 * @param budget		the amount of money the player has allocated to traveling
	 * @param planeAllowed	true if plane routes should be considered, otherwise false
	 * @return				the fastest route from the player's location to his destination
	 */
	private Route getShortestRoute(Place destination, int budget, boolean planeAllowed) {
		HashSet<Route> routes = new HashSet<Route>();
		HashSet<Route> tempRoutes = new HashSet<Route>();
		HashSet<Place> helper = new HashSet<Place>();
		Route route = new Route();
		Route tempRoute;
		boolean seaRouteOnly;
		boolean tokensOnMadagascar;
		route.add(player[turn].getPlace());
		routes.add(route);
		// If the destination is right next door, we just add it and return the result
		if (player[turn].getPlace().getConnectedByLand().contains(destination)) {
			route.add(destination);
			return route;
		}
		boolean done = false;
		do {
			for (Route r : routes) {
				helper.clear();
				helper.addAll(r.get(r.size() - 1).getConnectedByLand());
				helper.addAll(r.get(r.size() - 1).getConnectedBySea());
				if (r.size() == 1 && budget >= 300 && planeAllowed) {
					helper.addAll(r.get(r.size() - 1).getConnectedByAir());
				}
				for (Place p : helper) {
					tempRoute = new Route();
					if (r.get(r.size() - 1).equals(destination)) { // If we're already at the destination nothing needs to be added
						tempRoutes.add(r);
					}
					else if (!r.contains(p)) { // If not we just check that we're not going in loops, and add another part
						tempRoute.addAll(r); // Add the route so far
						tempRoute.add(p); // Decide which branch to take
						
						// In the rare case where the player is on an island and there are no tokens left on the mainland
						// we'll check only the sea routes, because checking only 1 sea route isn't sufficient, and checking
						// 2 + all the land routes will take a fairly long time and sometimes cause a memory heap overflow
						// since checking these routes is an O(e^n) operation :-(
						seaRouteOnly = (pf.playerOnIsland(player[turn]) && pf.tokensOnlyLeftOnIslands());
						
						// Madagascar must be given special consideration since it has a land route between the cities, and
						// using that is *clearly* the best option
						tokensOnMadagascar = false;
						for (Place city : cities) {
							if ((city.getName().equals("Cape St. Marie") || city.getName().equals("Tamatave")) && city.getToken() != null) {
								tokensOnMadagascar = true;
							}
						}
						if (player[turn].getPlace().getX() >= 1447 && player[turn].getPlace().getY() >= 1738 && tokensOnMadagascar) {
							seaRouteOnly = false;
						}
						
						// Append the route to the next intersection
						Route helperRoute = getRouteToNextIntersection(tempRoute,destination,r.getCost() <= Math.min(100, budget), seaRouteOnly);
						if (helperRoute != null) {
							tempRoute.addAll(helperRoute); // Add route to next intersection
							if (tempRoute.getCost() <= budget) {
								tempRoutes.add(tempRoute);
							}
						}

					}
				}
			}
			routes.clear();
			routes.addAll(tempRoutes);
			tempRoutes.clear();
			done = true;
			// If any of the routes ends somwhere else than the designated destination, we're not done yet.
			for (Route r : routes) {
				if (!r.get(r.size() - 1).equals(destination)) {
					done = false;
				}
				if (done) {
					route = r;
				}
			}
		} while (!done);

		// If the player doesn't have the star, but does have the budget, we might consider destinations (by air) that the
		// getOptimalDestinations-method didn't think of.
		if (!player[turn].hasFoundTheStar() && budget >= 300 && planeAllowed) {
			for (Place p : player[turn].getPlace().getConnectedByAir()) {
				if (p.getToken() != null) {
					Route r = new Route();
					r.add(player[turn].getPlace());
					r.add(p);
					routes.add(r);
				}
			}
		}
		
		// Which one is best?
		for (Route r : routes) {
			if (route.compareTo(r) > 0) {
				route = r;
			}
		}
		// If we were unable to get a valid route using these means, we'll have to increase the budget and try again.
		// However, since we by increasing this amount might actually exceed the amount of money that the player has
		// at his disposal we must forbid the use of plane routes, lest we eventually encounter a situation where
		// the player will spend more money than he has
		if (route.size() < 2) {
			return getShortestRoute(destination, budget + 100, false);
		}
		return route;
	}
	
	/**
	 * This method calculates the air route from the player's location that, given
	 * the player's budget, will get him as close to one of the starting points as
	 * possible. This is used by rich AI players who are heading back home in a hurry.
	 * 
	 * @param budget	the amount of money the player has
	 * @return			the optimal route
	 */
	private Route calculateAirRoute (int budget) {
		if (player[turn].getPlace().getConnectedByAir().isEmpty() || budget < 300) {
			return null;
		}
		int num = budget / 300;
		HashSet<Place> connected = new HashSet<Place>();
		HashSet<Place> connections = new HashSet<Place>();
		HashSet<Place> temp = new HashSet<Place>(); // Required since you can't add to a set while iterating over it
		connected.add(player[turn].getPlace());
		for (int i = 1 ; i <= num ; i++) {
			for (Place p : connected) {
				connections.clear();
				connections.addAll(p.getConnectedByAir()); // Add all air routes...
				for (Place q : connections) {
					temp.add(q); // Add the connected places to the temporary variable...
				}
			}
			connected.addAll(temp); // ...and add all those to our main set
			temp.clear();
			// If we're already at our destination, there is no reason to look further
			if (connected.contains(startingPoints[0])) {
				return getShortestAirRoute(startingPoints[0]);
			}
			if (connected.contains(startingPoints[1])) {
				return getShortestAirRoute(startingPoints[1]);
			}
		}
		
		// Now that we have all air reachable destinations, let's check which one is closest to the goal
		Place destination = player[turn].getPlace();
		int minDistance = 1000;
		int distance = 10000;
		int i;
		boolean found;
		for (Place q : connected) {
			i = 0;
			found = false;
			do {
				temp = calculateRoute(q,i,budget % 300 >= 100);
				for (Place p : temp) {
					if (p.isStart()) {
						distance = i;
						found = true;
					}
				}
				i++;
				if (i > 50) {
					distance = 10000;
					break;
				}
			} while (!found);
			if (distance < minDistance) {
				minDistance = distance;
				destination = q;
			}
		}
		return getShortestAirRoute(destination);
	}

	/**
	 * This method checks which places the computer can reach by taking a certain amount
	 * of steps. It is used by being called repeatedly, with increasing step-count in
	 * order to find the distance from one place to another, when the precise route is
	 * not (yet) important.
	 * 
	 * @param source	the place from where the distance is to be counted
	 * @param steps		the number of steps to be taken
	 * @param sea		are sea routes to be checked
	 * @return			the cities and starting points which are reachable with the
	 * 					given number of steps
	 */
	private HashSet<Place> calculateRoute (Place source, int steps, boolean sea) {
		HashSet<Place> connected = new HashSet<Place>();
		HashSet<Place> canMove = new HashSet<Place>();
		HashSet<Place> connections = new HashSet<Place>();
		HashSet<Place> temp = new HashSet<Place>(); // Required since you can't add to a set while iterating over it
		connected.add(source);
		for (int i = 1 ; i < steps ; i++) { // Go through the loop one time less than required for an answer
			for (Place p : connected) {
				connections.clear();
				connections.addAll(p.getConnectedByLand()); // Add all landroutes...
				if (sea) {
					connections.addAll(p.getConnectedBySea()); // ...and searoutes if requested
				}
				for (Place q : connections) {
					temp.add(q); // Add the connected places to the temporary variable...
				}
			}
			connected.addAll(temp); // ...and add all those to our main set
			temp.clear();
		}
		for (Place p : connected) {
			connections.clear();
			connections.addAll(p.getConnectedByLand());
			if (sea) {
				connections.addAll(p.getConnectedBySea());
			}
			for (Place q : connections) {
				if (!connected.contains(q) || q.isCity() || q.isStart()) {
					canMove.add(q);
				}
			}
		}
		return canMove;
	}
	
	/**
	 * Gets the shortest (fewest hops) air route between two places without considering more
	 * complex things such as budgets.
	 * 
	 * @param destination	the destination
	 * @return				the shortest route
	 */
	private Route getShortestAirRoute(Place destination) {
		Route route = new Route();
		route.add(player[turn].getPlace());
		HashSet<Route> routes = new HashSet<Route>();
		HashSet<Route> temp = new HashSet<Route>();
		routes.add(route);
		while (true) {
			for (Route r : routes) {
				for (Place p : r.get(r.size()-1).getConnectedByAir()) {
					Route q = new Route();
					q.addAll(r);
					q.add(p);
					if (p.equals(destination)) {
						return q;
					}
					temp.add(q);
				}
			}
			routes.clear();
			routes.addAll(temp);
			temp.clear();
		}
	}
	
	public void setPlayingField(PlayingField pf) {
		this.pf = pf;
	}
	
	public void makeMove(int turn) {
		this.turn = turn;
		nothingToDo = false;
		synchronized(this) {
			notify();
		}
	}
	
	public void run() {
		while (true) {
			nothingToDo = true;
			synchronized(this) {
				if (!firstRun && !pf.gameIsOver()) { // The first time, and the last time we don't want to do this, but normally we do
					pf.actionPerformed(new ActionEvent(player[turn],0,"End turn"));
				}
				firstRun = false;
				while (nothingToDo) {
					try {
						wait();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			makeAIMove();
		}
	}
}
