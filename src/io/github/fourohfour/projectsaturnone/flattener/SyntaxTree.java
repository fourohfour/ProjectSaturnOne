package io.github.fourohfour.projectsaturnone.flattener;

public class SyntaxTree {
	private TreeNode base;
	
	public SyntaxTree(){
		base = new TreeNode("BASE", "BASE", -1, -1);
	}
	
	public TreeNode getBaseNode(){
		return base;
	}
	
	public void print(boolean printExpressions){
		base.printSelf(0, printExpressions);
	}
}
