package io.github.fourohfour.projectsaturnone.flattener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flattener {
	public static char[] numerics = {'0','1','2','3','4','5','6','7','8','9'};
	public static String[] operators = {"+", "-", "*", "=="};
	public static String[] operandTypes = {"VARIABLE", "INTEGER", "EXPRESSION"};
	private String[] program;
	private SyntaxTree tree;
	private SymbolTable table;
	
	private String getTokenType(String token){
		
		Symbol s = table.getSymbol(token);
		if (!(s.getName().equalsIgnoreCase("null"))){
			return s.getType();
		}
		
		if (token.equalsIgnoreCase("=")){
			return "ASSIGNMENT";
		}
		if (token.equalsIgnoreCase("(")){
			return "EXPRESSION";
		}
		if (token.equalsIgnoreCase(")")){
			return "_EXPREND";
		}
		
		for (String op : operators){
			if (token.equalsIgnoreCase(op)){
				return "OPERATOR";
			}
		}
		
		boolean found = false;
		for (char c : token.toCharArray()){
			found = false;
			for (char n : numerics){
				if (c == n){
					found = true;
					break;
				}
			}
			if (!found){
				break;
			}
		}
		if (found){
			return "INTEGER";
		}
		return "VARIABLE";
	}
	
	
	public void error(Integer lineno, String line, String token, String reason){
		System.out.println("ERROR: On Line " + lineno.toString() + ": " + line);
		System.out.println("     : On Token: " + token);
		System.out.println("     : " + reason);
	}
	
	public Flattener(String[] program){
		this.program = program;
	}
	
	public int parse(){
		table = new SymbolTable(); 
		tree = new SyntaxTree();
		TreeNode base = tree.getBaseNode();
		int lineCount = 1;
		for (String line : program){
			String[] tokens = getTokens(line);
			String[] types  = new String[tokens.length];
			List<TreeNode> flatNodes = new ArrayList<>();
			// Type Annotation
			for (int t = 0; t < tokens.length; t++){
				String token = tokens[t];
				types[t] = this.getTokenType(token);
				flatNodes.add(new TreeNode(types[t], token, lineCount, t));
			}
			
			Map<Integer, TreeNode> ids = new HashMap<>();
			for (int n = 0; n < flatNodes.size(); n++){
				if (flatNodes.get(n).getType() == "ASSIGNMENT"){
					ids.put(0                   , new TreeNode("EXPRESSION", "AUTO", lineCount, -1));
					ids.put(n                   , new TreeNode("_EXPREND", "AUTO", lineCount, -1));
					ids.put(n + 1               , new TreeNode("EXPRESSION", "AUTO", lineCount, -1));
					ids.put(flatNodes.size()    , new TreeNode("_EXPREND", "AUTO", lineCount, -1));
				}
			}
			int shift = 0;
			for (Integer id : ids.keySet()){
				flatNodes.add(id + shift, ids.get(id));
				shift++;
			}
			
			
			// Bunching
			TreeNode curbase = base;
			for (int n = 0; n < flatNodes.size(); n++){
				TreeNode node = flatNodes.get(n);
				if (node.getType() == "EXPRESSION"){
				    curbase.addChild(node);
				    curbase = node;
				}
				else if (node.getType() == "_EXPREND"){
					curbase = curbase.getParent();
				}
				else {
					curbase.addChild(node);
				}
			}
			
			for (int n = 0; n < flatNodes.size(); n++){
				TreeNode node = flatNodes.get(n);
				if (node.getType() == "OPERATOR"){
					try{
						TreeNode op1 = flatNodes.get(n-1);
						TreeNode op2 = flatNodes.get(n+1);
						int correctTypes = 0;
						for (String type : operandTypes){
							if (op1.getType().equals(type)){
								correctTypes += 10;
							}
							if (op2.getType().equals(type)){
								correctTypes += 1;
							}
						}
						if (correctTypes == 11){
							node.addChild(op1);
							node.addChild(op2);
						}
						else {
							this.error(lineCount, line, node.getName(), "(" + op1.getType() + ", " +op2.getType() + ") are not allowed operand types for operation " + node.getName());
							return 1;
						}
					}
					catch (IndexOutOfBoundsException exception){
						this.error(lineCount, line, node.getName(), "Operator requires a token before and after itself.");
						return 1;
					}
				}
				else if (node.getType() == "ASSIGNMENT"){
					//pass
				}
			}
			lineCount++;
		}
		return 0;
	}
	
	public SyntaxTree getTree(){
		return tree;
	}
	
//	public String[] flatten(){
//		
//	}
	
	private String[] getTokens(String s){
		return s.split(" ");
	}
}
