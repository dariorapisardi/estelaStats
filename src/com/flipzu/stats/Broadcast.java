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
 * Represents a live broadcast in one of the streaming servers.
 * Also takes account of number of active listeners.
 * @author Dario Rapisardi <dario@rapisardi.org>
 */
public class Broadcast {
	private UUID uuid;
	private String username;	
	private Integer id;	
	private Hashtable<EstelaServer, Integer> servers = new Hashtable<EstelaServer, Integer>();
	
	/**
	 * Constructor
	 * @param uuid unique UUID for the broadcast, internal for the streaming servers.
	 * @param username owner/name of the broadcast.
	 * @param id application broadcast id.
	 */
	public Broadcast( UUID uuid, String username, Integer id ) {
		this.username = username;
		this.id = id;
		this.uuid = uuid;
	}
	
	/**
	 * @return the username/name of this broadcast
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * 
	 * @return the number of listeners for this broadcast
	 */
	public Integer getListeners() {
		Integer listeners = 0;
		
		Enumeration<Integer> e = servers.elements();
		
		while ( e.hasMoreElements() ) {
			listeners += e.nextElement();			
		}
		
		return listeners;
	}

	/**
	 * 
	 * @return the application ID for this broadcast.
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * 
	 * @return the unique UUID for this broadcast.
	 */
	public UUID getUuid() {
		return uuid;
	}
	
	/**
	 * updates the internal statistical table.
	 * @param server a EstelaServer instance with new/updated info.
	 * @param listeners number of listeners for this EstelaServer
	 */
	public synchronized void updateListeners( EstelaServer server, Integer listeners ) {
		
		/* first one is the main server */		
		if ( servers.size() == 0 ) {
			server.setMainServer(true);
		} else {
			delServer(server);			
		}
		
		servers.put(server, listeners);	
	}
	
	/**
	 * deletes a server (no longer serving this broadcast)
	 * @param server the EstelaServer to be deleted.
	 */
	public synchronized void delServer( EstelaServer server ) {
		Enumeration<EstelaServer> e = servers.keys();
		
		while ( e.hasMoreElements() ) {
			EstelaServer s = e.nextElement();
			if ( s.equals(server)) {				
				servers.remove(s);
				return;
			}
		}
	}
	
	/**
	 * 
	 * @return the number of EstelaServer instances serving
	 * this broadcast.
	 */
	public Integer getNumServers () {
		return servers.size();
	}
	
	/**
	 * 
	 * @return the EstelaServer instances serving this broadcast.
	 */
	public ArrayList<EstelaServer> getServers() {
		Enumeration<EstelaServer> e = servers.keys();
		
		ArrayList<EstelaServer> l = new ArrayList<EstelaServer>();
		
		while ( e.hasMoreElements() ) {
			EstelaServer s = e.nextElement();
			l.add(s);
		}
		
		return l;
	}
	
	/* return number of listener */
	/**
	 * Checks if this Broadcast is being served by
	 * a particular EstelaServer
	 * @param server the EstelaServer to check for.
	 * @return the number of listeners for this EstelaServer, -1 if this server is not serving this broadcast. 
	 */
	public Integer hasServer ( EstelaServer server ) {
		
		Enumeration<EstelaServer> e = servers.keys();
		while ( e.hasMoreElements() ) {			
			EstelaServer s = e.nextElement();
			if ( s.equals(server)) {
				return servers.get(s);	
			}			
		}
			
		return -1;
	}
}
