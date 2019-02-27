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
package org.fogbowcloud.app.jdfcompiler.syntactical;

import org.fogbowcloud.app.jdfcompiler.CodesTable;
import org.fogbowcloud.app.jdfcompiler.CompilerMessages;
import org.fogbowcloud.app.jdfcompiler.TokenDelimiter;
import org.fogbowcloud.app.jdfcompiler.grammar.Grammar;
import org.fogbowcloud.app.jdfcompiler.grammar.Rule;
import org.fogbowcloud.app.jdfcompiler.grammar.Symbol;
import org.fogbowcloud.app.jdfcompiler.lexical.LexicalAnalyzer;
import org.fogbowcloud.app.jdfcompiler.lexical.LexicalException;
import org.fogbowcloud.app.jdfcompiler.semantic.SemanticAnalyzer;
import org.fogbowcloud.app.jdfcompiler.semantic.exception.SemanticException;
import org.fogbowcloud.app.jdfcompiler.token.Token;

/**
 * @see org.ourgrid.common.specification.syntactical.SyntacticalDerivationTree
 *      Created on 21/05/2004
 */
public class CommonSyntacticalAnalyzer implements SyntacticalAnalyzer {

	private static transient final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(CommonSyntacticalAnalyzer.class);

	private LexicalAnalyzer lexical;

	private Grammar grammar;

	private SyntacticalDerivationTree sdt;

	private SemanticAnalyzer semantic;

	private CodesTable codesTable;

	private Token token;

	private Symbol actualSymbol;

	public static final int MODE_NORMAL = 0;

	public static final int MODE_READLINE = 1;

	public static final int MODE_READSTRING = 2;

	/**
	 * The constructor.
	 * 
	 * @param lexical
	 *            A lexical analyzer from where will be gotten the tokens from
	 *            source.
	 * @param grammar
	 *            The grammar entity that knows validat the language sources.
	 * @param semantic
	 *            A semantic analyzer that will handle the execution of the
	 *            semantic actions found as special token at grammar.
	 */
	public CommonSyntacticalAnalyzer(LexicalAnalyzer lexical, Grammar grammar, SemanticAnalyzer semantic) {

		this.lexical = lexical;
		this.grammar = grammar;
		this.semantic = null;
		this.codesTable = CodesTable.getInstance();
		this.sdt = new SyntacticalDerivationTree(grammar.getEndOfSourceSymbol(), grammar.getInitialSymbol());
		this.semantic = semantic;
	}

	/**
	 * @see org.ourgrid.common.specification.syntactical.SyntacticalAnalyzer#startCompilation()
	 */
	public void startCompilation() throws SyntacticalException {

		try {
			this.token = lexical.getToken();

			while (!sdt.top().equals(grammar.getEndOfSourceSymbol())) {
				if (!sdt.top().equals(Symbol.EMPTY) && !sdt.top().isSemanticAction()) {
					this.actualSymbol = this.getSymbolFromToken();
					LOG.debug("actual symbol: " + actualSymbol.getValue());
				}

				if (sdt.top().isTerminal()) {
					// will check if the stack top and actual symbol are equals
					checkSymbolsState();
				} else if (sdt.top().isSemanticAction()) {
					executeSemanticAction();
				} else { // sdt.top is a non terminal symbol!
					prepareRuleToFollow();
				}
			}

		} catch (LexicalException lex) {
			throw new SyntacticalException(lex.getMessage(), lex);
		} catch (SemanticException sex) {
			throw new SyntacticalException(sex.getMessage(), sex);
		}

	}

	/*
	 * Checks if the terminal symbol at the stack top and the symbol from source
	 * are equals. @throws LexicalException @throws SyntacticalException
	 */
	private void checkSymbolsState() throws SyntacticalException, SemanticException {

		if (actualSymbol.equals(sdt.top())) {
			sdt.pop();
			getNewToken();
		} else if (sdt.top().equals(Symbol.EMPTY)) {
			sdt.pop();
		} else {
			LOG.error("The stack top and the next symbol should be equals.");
			throw new SyntacticalException(
					CompilerMessages.SYNTACTICAL_COMPILATION_PROBLEM(token, sdt.top().getValue()));
		}

	}

	/*
	 * It will call the functions of the lexical module checking before wich one
	 * is more adequated on the actual situation. @throws SyntacticalException
	 * Wraps a lexical exceptions occured when getting a token from the lexical
	 * analyzer.
	 */
	private void getNewToken() throws SyntacticalException, SemanticException {

		while (sdt.top().isSemanticAction())
			this.executeSemanticAction();
		int operationalMode = this.semantic.getOperationMode();
		try {
			if (operationalMode == MODE_NORMAL) {
				token = lexical.getToken();
			} else if (operationalMode == MODE_READSTRING) {
				TokenDelimiter delim = new TokenDelimiter();
				delim.addDelimiter('\n');
				delim.addDelimiter(' ');
				delim.addDelimiter('\t');
				token = lexical.getToken(delim);
				LOG.debug("Token " + token.getSymbol());
			} else if (operationalMode == MODE_READLINE) {
				TokenDelimiter delim = new TokenDelimiter();
				delim.addDelimiter('\n');
				LOG.debug("Delimiters " + delim.toString());
				token = lexical.getToken(delim);
				LOG.debug("Token " + token.getSymbol());
			}
		} catch (LexicalException lex) {
			throw new SyntacticalException(lex.getMessage(), lex);
		}
	}

	/*
	 * Will search for a rule to follow based at the informations of the symbol
	 * at the stack top and the last token read. @throws SyntacticalException If
	 * any rule to follow was not found.
	 */
	private void prepareRuleToFollow() throws SyntacticalException, SemanticException {

		Rule toFollow = grammar.getRule(sdt.top(), actualSymbol);
		if (toFollow != null) {
			// will push the body of the rule at stack
			sdt.pop();
			sdt.pushRule(toFollow);
		} else if (actualSymbol.getValue().equals("\\n")) { // If did not
															// found a rule
															// but the
															// actual token
															// is a
															// end-of-line
															// symbol
			getNewToken();
		} else {
			LOG.error("The stack top and the next symbol are non_terminal and should find a rule to follow.");
			throw new SyntacticalException(
					CompilerMessages.SYNTACTICAL_COMPILATION_PROBLEM(token, sdt.top().getValue()));
		}
	}

	/*
	 * Will execute the semantic action at the top of the stack. @throws
	 * SemanticException If it could not execute the semantic action.
	 */
	private void executeSemanticAction() throws SemanticException {

		Symbol action = sdt.pop();
		semantic.performAction(action.getValue(), token);
	}

	/*
	 * Will get the right symbol from a token. It means that if it is necessary
	 * it will "transform" the token to the right symbol.
	 */
	private Symbol getSymbolFromToken() {

		if (token == null) {
			return Symbol.EOF;
		}

		Symbol toReturn = grammar.getSymbol(token.getSymbol());
		if (toReturn != null) {
			grammar.getRule(sdt.top(), toReturn);
			codesTable.getType(token.getSymbol());
		} else {
			toReturn = grammar.getSymbol(CodesTable.STRING);
		}
		return toReturn;
	}

}
