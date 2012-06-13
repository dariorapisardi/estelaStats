package com.flipzu.stats;

/**
 * Estela stats server
 *
 * @author Dario Rapisardi <dario@flipzu.com>
 *
 */
public class Request {
	private String path;
	private String key;
	private String value;
	
	public String getPath() {
		return path;
	}
	
	public void setPath( String path ) {
		this.path = path;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey( String key ) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue( String value ) {
		this.value = value;
	}

}
