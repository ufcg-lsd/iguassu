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

import org.fogbowcloud.app.jdfcompiler.grammar.Grammar;
import org.fogbowcloud.app.jdfcompiler.lexical.CommonLexicalAnalyzer;
import org.fogbowcloud.app.jdfcompiler.lexical.LexicalAnalyzer;
import org.fogbowcloud.app.jdfcompiler.lexical.LexicalException;
import org.fogbowcloud.app.jdfcompiler.semantic.CommonSemanticAnalyzer;
import org.fogbowcloud.app.jdfcompiler.semantic.SemanticActions;
import org.fogbowcloud.app.jdfcompiler.semantic.SemanticAnalyzer;
import org.fogbowcloud.app.jdfcompiler.semantic.exception.SemanticException;
import org.fogbowcloud.app.jdfcompiler.syntactical.CommonSyntacticalAnalyzer;
import org.fogbowcloud.app.jdfcompiler.syntactical.SyntacticalAnalyzer;
import org.fogbowcloud.app.jdfcompiler.syntactical.SyntacticalException;

/**
 * @see org.fogbowcloud.app.jdfcompiler.CompilerModulesFactory
 */
public class CommonCModulesFactory implements CompilerModulesFactory {

	/**
	 * The constructor.
	 */
	public CommonCModulesFactory() {

		// do nothing...
	}


	/**
	 * @see org.fogbowcloud.app.jdfcompiler.CompilerModulesFactory#createLexicalAnalyzer(String)
	 */
	public LexicalAnalyzer createLexicalAnalyzer( String sourceFile ) throws LexicalException {

		return new CommonLexicalAnalyzer( sourceFile );
	}


	/**
	 * @see org.fogbowcloud.app.jdfcompiler.CompilerModulesFactory#createSyntacticalAnalyzer(org.ourgrid.common.specification.lexical.LexicalAnalyzer,
	 *      org.ourgrid.common.specification.grammar.Grammar,
	 *      org.ourgrid.common.specification.semantic.SemanticAnalyzer)
	 */
	public SyntacticalAnalyzer createSyntacticalAnalyzer( LexicalAnalyzer lexicalAnalyzer, Grammar languageGrammar,
															SemanticAnalyzer semantic ) throws SyntacticalException {

		return new CommonSyntacticalAnalyzer( lexicalAnalyzer, languageGrammar, semantic );
	}


	/**
	 * @see org.fogbowcloud.app.jdfcompiler.CompilerModulesFactory#createSemanticAnalyzer(org.ourgrid.common.specification.semantic.SemanticActions)
	 */
	public SemanticAnalyzer createSemanticAnalyzer( SemanticActions actionsContainer ) throws SemanticException {

		CommonSemanticAnalyzer semantic = new CommonSemanticAnalyzer( actionsContainer );
		return semantic;
	}

}
