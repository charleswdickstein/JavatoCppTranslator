package edu.nyu.oop;

import java.util.*;
import xtc.tree.GNode;

import edu.nyu.oop.util.cppNodeActions;

//The C++ AST created from going through our ClassSummary data structure taken from JavaAST
public class CPPAST {

    //Private the root node of the tree
    private GNode root;

    //Keeps tack of the ParentNodeOfRecentAdditionOfNode - use this to find the last Node point you want to add too
    private GNode recentParentNodeMutated;

    //All the diff classes
    private ArrayList<CppClassObject> cppClassObjectslist;

    //The main class
    private CppClassObject mainClass;

    //Keep track of the namespacenode that we add the Class Structs to
    private GNode linkToNamespaceNode;

    //Constructor
    public CPPAST(String name) {
        this.cppClassObjectslist = new ArrayList<CppClassObject>();
        //create new gnode as root
        this.root = cppNodeActions.createNewASTNode(name);
    }

    public void setCppClassObjectslist(ArrayList<CppClassObject> cppClassObjectslist) {
        this.cppClassObjectslist = cppClassObjectslist;
    }

    public ArrayList<CppClassObject> getCppClassObjectslist() {
        return cppClassObjectslist;
    }

    public void setMainClass(CppClassObject mainClass) {
        this.mainClass = mainClass;
    }

    public CppClassObject getMainClass() {
        return mainClass;
    }

    public void setRoot(GNode root) {
        this.root = root;
    }

    public GNode getRoot() {
        return root;
    }

    public void setRecentParentNodeMutated(GNode recentParentNodeMutated) {
        this.recentParentNodeMutated = recentParentNodeMutated;
    }

    public GNode getRecentParentNodeMutated() {
        return recentParentNodeMutated;
    }

    public void setLinkToNamespaceNode(GNode linkToNamespaceNode) {
        this.linkToNamespaceNode = linkToNamespaceNode;
    }

    public GNode getLinkToNamespaceNode() {
        return linkToNamespaceNode;
    }
}
