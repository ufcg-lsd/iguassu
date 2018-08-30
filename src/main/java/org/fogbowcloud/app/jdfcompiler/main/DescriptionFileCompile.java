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
package org.fogbowcloud.app.jdfcompiler.main;

import java.util.List;

import org.fogbowcloud.app.jdfcompiler.CompilerMessages;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;

/**
 * Strategy to compile description files.
 */
public class DescriptionFileCompile {

	/**
	 * @param descriptionFilePath
	 * @return
	 * @throws CompilerException
	 */
	public static JobSpecification compileJDF( String descriptionFilePath ) throws CompilerException {

		CommonCompiler compiler = new CommonCompiler();

		compiler.compile( descriptionFilePath, FileType.JDF );

		List answer = compiler.getResult();

		if ( answer == null ) {
			throw new CompilerException("Job " + CompilerMessages.DESCRIPTION_FILE_IS_EMPTY );
		}

		return (JobSpecification) answer.get( 0 );
	}


//	/**
//	 * @param descriptionFilePath
//	 * @return
//	 * @throws CompilerException
//	 */
//	@Req("REQ066")
//	public static List<PeerSpecification> compileGDF( String descriptionFilePath ) throws CompilerException {
//
//		CommonCompiler compiler = new CommonCompiler();
//
//		try {
//
//			compiler.compile( descriptionFilePath, FileType.GDF );
//		} catch ( CompilerException cex ) {
//			throw cex;
//
//		}
//
//		List answer = compiler.getResult();
//
//		if ( answer == null ) {
//			throw new CompilerException("Grid " + CompilerMessages.DESCRIPTION_FILE_IS_EMPTY );
//		}
//
//		return answer;
//	}


//	/**
//	 * @param descriptionFilePath
//	 * @return
//	 * @throws CompilerException
//	 */
//	@Req({"REQ010", "REQ101"})
//	public static List<WorkerSpecification> compileSDF( String descriptionFilePath ) throws CompilerException {
//
//		CommonCompiler compiler = new CommonCompiler();
//
//		compiler.compile( descriptionFilePath, FileType.SDF );
//
//		List answer = compiler.getResult();
//
//		if ( answer == null ) {
//			throw new CompilerException("Site " + CompilerMessages.DESCRIPTION_FILE_IS_EMPTY );
//		}
//
//		return answer;
//	}
	
}
