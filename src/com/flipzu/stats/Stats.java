package com.flipzu.stats;
/**
 * Copyright 2011 Flipzu
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*  
*  Initial Release: Dario Rapisardi <dario@rapisardi.org>
*  
*/

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Takes accounting for broadcast statistics.
 * Singleton object.
 *
 * @author Dario Rapisardi <dario@rapisardi.org>
 *
 */
public class Stats {
	private static Stats INSTANCE = new Stats();
	Hashtable<UUID, Broadcast> bcasts = new Hashtable<UUID, Broadcast>();
	ArrayList<EstelaServer> servers = new ArrayList<EstelaServer>();
	
	private Stats() {}
	
	/**
	 * 
	 * @return singleton object
	 */
	public static Stats getInstance() {
		return INSTANCE;
	}
	
	/**
	 * removes server from stats
	 * @param server EstelaServer to be removed.
	 */
	public synchronized void clearHost( EstelaServer server ) {
				
		Enumeration<Broadcast> e = bcasts.elements();
		
		while ( e.hasMoreElements() ) {			
			Broadcast b = e.nextElement();
			b.delServer(server);
			if ( b.getNumServers() == 0 ) {
				bcasts.remove(b.getUuid());
			}
		}
		
		if ( servers.contains(server) ) {
			servers.remove(server);
		}

	}
	
	/**
	 * updates broadcast stats.
	 * @param server EstelaServer firing the event.
	 * @param bcast broadcast to be updated.
	 */
	public synchronized void updateBroadcast( EstelaServer server, EstelaProtocol.Broadcast bcast ) {
		UUID uuid = UUID.fromString(bcast.getUuid());
		if ( bcasts.containsKey(uuid) ) {
			Broadcast b  = bcasts.get(uuid);
			b.updateListeners(server, bcast.getListeners());
			return;
		}
		
		Broadcast b = new Broadcast(uuid, bcast.getUsername(), bcast.getId());
		b.updateListeners(server, bcast.getListeners());
		
		bcasts.put(uuid, b);
	}
	
	/**
	 * track a closing broadcast
	 * @param server EstelaServer firing the event.
	 * @param bcast broadcast to be closed.
	 */
	public synchronized void closeBroadcast( EstelaServer server, EstelaProtocol.Broadcast bcast ) {
		UUID uuid = UUID.fromString(bcast.getUuid());
		if ( bcasts.containsKey(uuid) ) {
			Broadcast b  = bcasts.get(uuid);
			b.delServer(server);
			if ( b.getNumServers() == 0 ) {
				bcasts.remove(uuid);
			}
			return;
		}
	}
	
	/**
	 * returns a broadcast by name
	 * @param username the username/name of the broadcast
	 * @return the Broadcast object for that username, null otherwise.
	 */
	public Broadcast getBroadcast( String username ) {
		
		Enumeration<Broadcast> e = bcasts.elements();
		
		while ( e.hasMoreElements() ) {
			Broadcast b = e.nextElement();
			if ( b.getUsername().equals(username)) {
				return b;
			}			
		}
		
		return null;
	}
	
	/**
	 * returns a broadcast by id
	 * @param id the id of the broadcast
	 * @return the Broadcast object for that id, null otherwise.
	 */
	public Broadcast getBroadcast( Integer id ) {
		
		Enumeration<Broadcast> e = bcasts.elements();
		
		while ( e.hasMoreElements() ) {
			Broadcast b = e.nextElement();
			if ( b.getId().equals(id)) {
				return b;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return a HastTable containing every broadcast in the stats table.
	 */
	public Hashtable<UUID, Broadcast> getBcasts() {
		return bcasts;
	}
	
	/**
	 * adds a server to the stats table.
	 * @param server the server to be added.
	 */
	public void addServer( EstelaServer server ) {
		servers.add(server);
	}
	
	/**
	 * returns an array list for every server registered to this stats server.
	 * @return an array list for every server registered to this stats server.
	 */
	public ArrayList<EstelaServer> getServers () {
		return this.servers;
	}

}
