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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This Singleton class is a wrapper for Apache Log
 * @author Dario Rapisardi <dario@rapisardi.org>
 *
 */
public class Debug {
	private static Debug INSTANCE = new Debug();
	
	private static final boolean logApi = Config.getInstance().isLogApi();
	private static final boolean logHttp = Config.getInstance().isLogHttp();

    private Log log = LogFactory.getLog(Debug.class);

	
    private Debug() { }
	
    /**
     * 
     * @return the singleton instance
     */
	public static Debug getInstance() {
		return INSTANCE;
	}
	
	/**
	 * log ERROR messages
	 * @param msg message to be logged.
	 * @param e the fatal exception.
	 */
	public void logError ( String msg, Exception e ) {		
		log.fatal( msg, e );
	}
	
	/**
	 * log ERROR messages
	 * @param msg message to be logged
	 * @param e the fatal throwable
	 */
	public void logError ( String msg, Throwable e ) {		
		log.fatal( msg, e );
	}
	
	/**
	 * log API calls.
	 * @param msg message to be logged
	 */
	public void logApi( String msg ) {
		if ( logApi ) {
			log.info(msg);	
		}		
	}
	
	/**
	 * log HTTP requests
	 * @param msg message to be logged
	 */
	public void logHttp( String msg ) {
		if ( logHttp ) {
			log.info(msg);
		}
	}
}
