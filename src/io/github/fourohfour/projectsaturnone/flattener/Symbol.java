package io.github.fourohfour.projectsaturnone.flattener;

public class Symbol {
	private String name;
	private String type;
	
	public Symbol(String name, String type){
		this.name = name;
		this.type = type;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getType(){
		return this.type;
	}
}
