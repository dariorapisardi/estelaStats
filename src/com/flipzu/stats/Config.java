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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * Singleton class representing the configuration options.
 *
 * @author Dario Rapisardi <dario@rapisardi.org>
 *
 */
public class Config {
	private static Config INSTANCE = new Config();
	
	private static Configuration config;		
	
	private Config()  {
		try {
			config = new PropertiesConfiguration("estelaStats.properties");
		} catch (ConfigurationException e) {
			config = null;
			Debug.getInstance().logError("Config, Config() exception ", e);
		}
	}
	
	/**
	 * Singleton getter
	 * @return the Configuration instance.
	 */
	public static Config getInstance() {
		return INSTANCE;
	}

	/**
	 * 
	 * @return TCP port this server is listening to for HTTP requests.
	 */
	public Integer getPort() {
		return config.getInt("port", 10005);
	}

	/**
	 * 
	 * @return TCP port this server is listening to for API requests.
	 */
	public Integer getApiPort() {
		return config.getInt("apiPort", 10006);
	}

	/**
	 * 
	 * @return the server name for HTTP requests.
	 */
	public String getServerName() {
		return config.getString("serverName", "estelaStats/0.1");
	}

	/**
	 * 
	 * @return the secret for stats access, eg: http://127.0.0.1:10005/servers?secret=12345
	 * @throws ConfigurationException if the secret is not defined in the properties file.
	 */
	public String getSecret() throws ConfigurationException {
		return config.getString("secret");
	}
	
	/**
	 * 
	 * @return true if we're logging API calls.
	 */
	public boolean isLogApi() {
		return config.getBoolean("logApi", false);
	}
	
	/**
	 * 
	 * @return true if we're logging HTTP requests.
	 */
	public boolean isLogHttp() {
		return config.getBoolean("logHttp", false);
	}	
}
