package io.github.fourohfour.projectsaturnone.flattener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeNode {
	private String type;
	private String name;
	private TreeNode parent;
	private List<TreeNode> children;
	private Integer line;
	private Integer token;
	private int id;
	private static int idtick = 0;


	public TreeNode(String type, String name, Integer line, Integer token){
		this.parent = null;
		this.type = type;
		this.name = name;
		this.line = line;
		this.token = token;
		this.children = new ArrayList<>();
		this.id = idtick;
		idtick++;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getName(){
		return this.name;
	}
	
	public TreeNode getParent(){
		return this.parent;
	}
	
	public void setParent(TreeNode p){
		if (this.parent != null){
			this.parent.removeChild(this);
		}
		this.parent = p;
	}
	
	public TreeNode getPrev(){
		List<TreeNode> siblings = this.getParent().getChildren();
		int pos = siblings.indexOf(this);
		if (pos == 0){
			return null;
		}
		return siblings.get(pos - 1);
	}
	
	public TreeNode getNext(){
		List<TreeNode> siblings = this.getParent().getChildren();
		int pos = siblings.indexOf(this);
		if (pos == siblings.size() - 1){
			return null;
		}
		return siblings.get(pos + 1);
	}
	
	public Integer getLine(){
		return this.line;
	}
	
	public Integer getToken(){
		return this.token;
	}
	
	public void replaceChild(TreeNode o, TreeNode n){
		int pos = this.getChildren().indexOf(o);
		o.setParent(null);
		this.getChildren().remove(o);
		this.getChildren().add(pos, n);
	}
	
	public void addChild(TreeNode t){
		t.setParent(this);
		children.add(t);
	}
	
	public void removeChild(TreeNode t){
		children.remove(t);
	}
	
	public List<TreeNode> getChildren(){
		return children;
	}
	
	public void printSelf(int indentLevel, boolean printExpressions){
		if (this.getType() == "EXPRESSION" && !printExpressions){
			for (TreeNode child : this.getChildren()){
				child.printSelf(indentLevel, false);
			}
		}
		else {
			String repeated = new String(new char[indentLevel]).replace("\0", "    ");
			System.out.println(repeated.concat(this.type + " " + this.name));
			for (TreeNode child : this.getChildren()){
				child.printSelf(indentLevel + 1, printExpressions);
			}
		}
	}
	
	@Override
	public String toString(){
		return String.valueOf(this.id) + " " + this.type + " " + this.name;
	}
	
	@Override
	public boolean equals(Object other){
		if (other instanceof TreeNode){
			TreeNode otherNode = (TreeNode) other;
			if (otherNode.id == this.id){
				return true;
			}
		}
		return false;
	}
}
