/*
 * Copyright (C) 2008 Universidade Federal de Campina Grande
 *  
 * This file is part of OurGrid. 
 *
 * OurGrid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.fogbowcloud.app.jdfcompiler;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Description: This class provides access to the ourgrid's configuration model
 * Each module that needs specific configuration properties should extend this
 * class and sets its own properties.
 */
public abstract class Configuration implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String VERSION_FILE = "/resources/VERSION";
	

	private static Configuration singleInstance = null;

	public static final String PROP_LOGFILE = "logfile";

	public static final String PROP_LOG_PROPERTIES_FILE = "logProperties";

	public static final String MGROOT = "MGROOT";

	public static final String OGROOT = "OGROOT";

	private Properties currentProperties = new Properties( System.getProperties() );

	private boolean usingDefaultProperties = false;
		
	protected Configuration() {

	}

	public static final synchronized Configuration getInstance() {

		if ( Configuration.singleInstance == null ) {

			throw new Error( "You need to create a configuration!" );

		}
		return Configuration.singleInstance;
	}


	/**
	 * Creates, if not already created, the unique configuration instance.
	 * 
	 * @param configurationType The configuration type to be created.
	 * @return The unique configuration instance.
	 */
	public static final synchronized Configuration getInstance( String configurationType ) {

		if ( Configuration.singleInstance == null ) {
		}

		return Configuration.singleInstance;

	}


	/**
	 * Sets the singleton instance to null. Used only by test classes.
	 */
	public static final synchronized void reset() {

		Configuration.singleInstance = null;

	}


	/**
	 * Gets the value of a given property.
	 * 
	 * @param key The property name without the module specific prefix.
	 * @return The property value or null if the property was no setted.
	 */
	public final String getProperty( String key ) {

		String trimmedKey = key.trim();
		String property = (String) currentProperties.get(trimmedKey);

		if (property != null) {
			return property.trim();
		}

		int indexOfSeparator = trimmedKey.indexOf('.');
		if (indexOfSeparator >= 0) {
			property = (String) currentProperties.get(
					trimmedKey.substring(indexOfSeparator + 1));
			if (property != null) {
				return property.trim();
			}
		}
		
		return property;

	}


	/**
	 * Sets the value of a given property.
	 * 
	 * @param key The property name without the module specific prefix.
	 * @param value The property value.
	 */
	public final void setProperty( String key, String value ) {

		currentProperties.put(key.trim(), value.trim());
	}


	/**
	 * Verify if the property passed is enabled.
	 * 
	 * @param propertyKey the key name of the property.
	 * @return true if property is yes, false otherwise.
	 */
	public final boolean isEnabled( String propertyKey ) {

		String property = getProperty( propertyKey );
		return property != null && property.equalsIgnoreCase( "yes" );

	}


	/**
	 * This method reads properties from a file.
	 * 
	 * @param file The property file to be loaded.
	 */
	protected void loadPropertiesFromFile( String file ) {

		/** Get an abstraction for the properties file */
		File propertiesFile = new File( file );

		/* load the properties file, if it exists */
		try {
			currentProperties.load( new FileInputStream( propertiesFile ) );
		} catch ( IOException e ) {
			usingDefaultProperties = true;
		}

	}


	public boolean isUsingDefaultProperties() {

		return usingDefaultProperties;
	}




	/**
	 * @return local host name.
	 */
	public final String getHostname() {

		String hostname = "localhost";

		try {
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
		} catch ( UnknownHostException e ) {
			// Using default peer name.
		}

		return hostname;

	}




//	/**
//	 * Returns the module specific prefix used to identify it's own properties.
//	 * For example, the Broker properties prefix is "mg." , the OurGrid's one is
//	 * "peer." .
//	 * 
//	 * @return The module specific property prefix.
//	 */
//	protected abstract String getPrefix();


	/**
	 * Gets the module configuration rootdir.
	 * 
	 * @return The module configuration rootdir path.
	 */
	public abstract String getConfDir();


	/**
	 * Creates the unique configuration instance.
	 * 
	 * @param configurationType The configuration type to be created.
	 * @return The unique configuration instance.
	 */
	public static Configuration createInstance( String configurationType ) {

		reset();
		return getInstance( configurationType );
	}


	protected Properties getCurrentProperties() {

		return this.currentProperties;
	}








	public Map<String, Object> toMap() {
		
		Map<String, Object> result = CommonUtils.createSerializableMap();
		
		Set<Entry<Object, Object>> properties = currentProperties.entrySet();
		
		for (Entry<Object, Object> entry : properties) {
			result.put((String)entry.getKey(), entry.getValue());
		}
		
		return result;
	}
	
}