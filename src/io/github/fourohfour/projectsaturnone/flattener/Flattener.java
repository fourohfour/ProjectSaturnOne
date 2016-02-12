package io.github.fourohfour.projectsaturnone.flattener;

import java.util.ArrayList;
import java.util.Arrays;
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
			
			// Assignment Expression Boxing
			Map<Integer, TreeNode> ids = new HashMap<>();
			for (int n = 0; n < flatNodes.size(); n++){
				if (flatNodes.get(n).getType() == "ASSIGNMENT"){
					ids.put(0                   , new TreeNode("EXPRESSION", "ASSIGNMENT_BOX", lineCount, -1));
					ids.put(n                   , new TreeNode("_EXPREND", "ASSIGNMENT_BOX", lineCount, -1));
					ids.put(n + 1               , new TreeNode("EXPRESSION", "ASSIGNMENT_BOX", lineCount, -1));
					ids.put(flatNodes.size()    , new TreeNode("_EXPREND", "ASSIGNMENT_BOX", lineCount, -1));
				}
			}
			int shift = 0;
			for (Integer id : ids.keySet()){
				flatNodes.add(id + shift, ids.get(id));
				shift++;
			}
			
			
			// Expression Pass
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
			
			// Operator Pass
			for (int n = 0; n < flatNodes.size(); n++){
				TreeNode node = flatNodes.get(n);
				if (node.getType() == "OPERATOR"){
					try{
						TreeNode op1 = node.getPrev();
						TreeNode op2 = node.getNext();
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
						TreeNode containingExpr = new TreeNode("EXPRESSION", "OPERATOR_BOX", lineCount, -1);
						node.getParent().replaceChild(node, containingExpr);
						containingExpr.addChild(node);
					}
					catch (IndexOutOfBoundsException exception){
						this.error(lineCount, line, node.getName(), "Operator requires a token before and after itself.");
						return 1;
					}
				}

			}
			
			// Assignment Pass
			for (int n = 0; n < flatNodes.size(); n++){
				TreeNode node = flatNodes.get(n);
				if (node.getType() == "ASSIGNMENT"){
					TreeNode op1 = node.getPrev();
					TreeNode op2 = node.getNext();
					if (op1.getType().equals("EXPRESSION") && op2.getType().equals("EXPRESSION")){
						node.addChild(op1);
						node.addChild(op2);
					}
					else {
						this.error(lineCount, line, node.getName(), "ASSIGNMENT requires two EXPRESSIONS on either side.");
						return 1;
					}
				}
			}
			lineCount++;
		}
		return 0;
	}
	
	public SyntaxTree getTree(){
		return tree;
	}
	
	private String[] getTokens(String s){
		List<Character> str = new ArrayList<>();
		for (Character c : s.toCharArray()){
			str.add(c);
		}
		
		String[] specialTokens = new String[]{"(",")","+", "-", "*", "==","="};
		boolean locked = false;
		List<String> lockedTokens = new ArrayList<>();
		int lockCount = 0;
		List<Integer> spacePoints = new ArrayList<Integer>();
		int cCount = 0;
		
		for (char c : str){
			if (!locked){
				for (String token : specialTokens){
					if (token.charAt(0) == c){
						lockedTokens.add(token);
						locked = true;
					}
				}
			}
			String finalToken = null;
			if (locked) {
				System.out.println(c);
				List<String> newTokens = new ArrayList<>();
				for (String token : lockedTokens){
					if (token.charAt(lockCount) == c){
						if (token.length() == lockCount + 1){
							finalToken = token;
						}
						else {
							newTokens.add(token);
						}
					}
				}
				if (finalToken != null){
					spacePoints.add(cCount - lockCount);
					spacePoints.add(cCount + 1);
					lockCount = 0;
					lockedTokens = new ArrayList<>();
					locked = false;
				}
				else if (newTokens.size() == 0){
					lockCount = 0;
					lockedTokens = new ArrayList<>();
					locked = false;
				}
				else {
				    lockedTokens = newTokens;
				    lockCount++;
				}
			}
			cCount++;
		}
		
		int add = 0;
		for (int i : spacePoints){
			str.add(i + add, ' ');
			add++;
		}
		
		String finalStr = "";
		char last = ' ';
		for (Character c : str){
			if (!(last == ' ' && c == ' ')){
			    finalStr = finalStr + c;
			}
		    last = c;
		}
		System.out.println(Arrays.toString(finalStr.split(" ")));
		return finalStr.split(" ");
	}
}
