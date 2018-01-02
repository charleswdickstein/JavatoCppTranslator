package edu.nyu.oop;

import org.slf4j.Logger;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * This class demostrates a trivial usage of Xtc's Visitor class.
 * You may use this as a base for your ScopeVisitor.
 */
public class CppHeaderVisitor extends xtc.tree.Visitor {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());



    //visitDependencies in CppAST
    public void visitDependencies(GNode n) throws IOException {
        for (int i = 0; i < n.size(); i++) {
            //System.out.println(n.getString(i));
        }
        visit(n);
    }

    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) dispatch((Node) o);
    }


}