package io.github.fourohfour.projectsaturnone.flattener;

import java.util.HashSet;
import java.util.Set;

public class SymbolTable {
	public static Set<Symbol> getBaseSymbols(){
		Set<Symbol> baseSymbols = new HashSet<>();
		return baseSymbols;
	}
	public static Symbol nullSymbol = new Symbol("null", "null");
	
	private Set<Symbol> symbols;
	public SymbolTable(){
		symbols = new HashSet<Symbol>();
		symbols.addAll(SymbolTable.getBaseSymbols());
	}
	
	public Symbol getSymbol(String name){
		for (Symbol symbol : symbols){
			if (symbol.getName() == name){
				return symbol;
			}
		}
		return SymbolTable.nullSymbol;
	}
	
	public void addSymbol(String name, String type){
		symbols.add(new Symbol(name, type));
	}
}
