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
package org.fogbowcloud.app.jdfcompiler.semantic;

import java.io.Serializable;

import org.fogbowcloud.app.jdfcompiler.job.IOEntry;

/**
 * This entity handles the input and output entries for a task.
 * 
 * @see IOEntry Created on Jul 1, 2004
 */
public class IOCommand extends JDLCommand  implements Serializable {

	public enum IOType {
		IN, OUT
	}
	
	/**
	 * Serial identification of the class. It need to be changed only if the
	 * class interface is changed.
	 */
	private static final long serialVersionUID = 33L;

	IOEntry entry;

	/**
	 * An empty constructor
	 */
	public IOCommand() {
		super();
		this.setBlockType(JDLCommandType.IO);
	}


	/**
	 * Inserts a new input/output entry at this block of I/O commands.
	 * 
	 * @param condition The condition that tells if the I/O command will be
	 *        executed. It happens only when the command was written inside a
	 *        if/else block.
	 * @param entry A IOEntry object that defines the command and the paths of
	 *        origin and destiny of a file.
	 */
	public void putEntry( String condition, IOEntry entry ) {

		this.entry = entry;
	}


	/**
	 * Inserts a new input/output entry at this block of I/O commands, using as
	 * condition the empty string, that means this entry will always be used.
	 * 
	 * @param entry The input/output entry.
	 */
	public void putEntry( IOEntry entry ) {

		this.putEntry( "", entry );
	}


	/**
	 * Returns a collection with the entries related with a condition. To obtain
	 * all the conditions at this block use this.getConditions.
	 * 
	 * @param condition The condition that indexes all the entries that will be
	 *        used if it is true.
	 * @return The collection of entries related with a condition - null if the
	 *         condition does not exist.
	 */
	public IOEntry getEntry() {

		
		return this.entry;

	}

	/**
	 * Returns a string representation of an IOBlock.
	 */
	@Override
	public String toString() {

	return entry.toString();
	}


	@Override
	public int hashCode() {

		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((this.entry == null) ? 0 : this.entry.hashCode());
		return result;
	}


	@Override
	public boolean equals( Object obj ) {

		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		final IOCommand other = (IOCommand) obj;
		if ( !(this.entry == null ? other.entry == null : this.entry.equals( other.entry )) )
			return false;
		return true;
	}
}
