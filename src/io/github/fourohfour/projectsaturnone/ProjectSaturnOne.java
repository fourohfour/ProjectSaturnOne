package io.github.fourohfour.projectsaturnone;

import io.github.fourohfour.projectsaturnone.flattener.Flattener;
import io.github.fourohfour.projectsaturnone.flattener.SyntaxTree;

public class ProjectSaturnOne {
	public static void main(String[] args) {
	      Flattener flattener = new Flattener(new String[]{"x = 2 + 3", "y = 3", "z = x"});
	      if (flattener.parse() == 0){
	          SyntaxTree tree = flattener.getTree();
	          tree.print();
	      }
	}
}
