package com.flipzu.stats;

/**
 * Estela stats server
 *
 * @author Dario Rapisardi <dario@flipzu.com>
 *
 */
public class EstelaServer {
	private String hostname;
	private String ip;
	private Integer read_port;
	private Integer write_port;
	private boolean main_server;
	
	public EstelaServer( String hostname, String ip, Integer read_port, Integer write_port ) {
		this.hostname = hostname;
		this.ip = ip;
		this.read_port = read_port;
		this.write_port = write_port;
		this.main_server = false;
	}
	
	public void setMainServer( boolean is_main_server ) {
		this.main_server = is_main_server;
	}
	
	public boolean isMainServer () {
		return this.main_server;
	}
	
	public String getHostname() {
		return hostname;
	}

	public String getIp() {
		return ip;
	}
	
	public void setIp( String ip ) {
		this.ip = ip;
	}

	public Integer getReadPort() {
		return read_port;
	}
	
	public Integer getWritePort() {
		return write_port;
	}
	
	@Override
	public boolean equals(Object other) {
		EstelaServer o = (EstelaServer) other;
		
		if ( this.hostname.equals(o.hostname) && 
				this.ip.equals(o.ip) &&
				this.read_port.equals(o.read_port) &&
				this.write_port.equals(o.write_port)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return this.hostname + "/" + this.ip + ":" + this.read_port + ":" + this.write_port;
	}
}
