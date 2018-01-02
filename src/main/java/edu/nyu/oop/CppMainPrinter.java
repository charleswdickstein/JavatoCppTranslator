package edu.nyu.oop;

import java.io.*;
import java.util.*;

import edu.nyu.oop.util.XtcProps;
import org.slf4j.Logger;
import xtc.lang.CPrinter;
import xtc.tree.Node;
import xtc.tree.GNode;
import xtc.tree.Printer;
import xtc.tree.Visitor;
import edu.nyu.oop.util.*;


public class CppMainPrinter  extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private Printer printer;

    private String outputLocation = XtcProps.get("output.location");

    public CppMainPrinter() {
        Writer w = null;
        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/main.cpp");
            OutputStreamWriter ows = new OutputStreamWriter(fos, "utf-8");
            w = new BufferedWriter(ows);
            this.printer = new Printer(w);
        } catch (Exception e) {
            throw new RuntimeException("Output location not found. Create the /output directory.");
        }

        // Register the visitor as being associated with this printer.
        // We do this so we get some nice convenience methods on the printer,
        // such as "dispatch", You should read the code for Printer to learn more.
        printer.register(this);
    }

    public void print(Node n) {
        dispatch(n);
        printer.flush(); // important!
    }

    // Print all the node names in an Ast
    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) dispatch((Node) o);
    }

    //Custom VisitMethods
    public void visitDependencies(GNode n) throws IOException {
        for (int i = 0; i < n.size(); i++) {
            printer.pln( n.getString(i));
        }
        printer.pln("");
        visit(n);
    }

    public void visitUsingNamespace(GNode n) throws IOException {
        for (int i = 0; i < n.size(); i ++){
            printer.pln("using namespace " + n.getString(i) + ";");
        }
        visit(n);
        printer.pln("");
    }

    public void visitNamespace(GNode n) throws IOException {
        visit(n);
    }


    public void visitImplementationMain(GNode n) throws IOException {
        printer.indent().pln(n.getString(0));
        printer.indent().pln("");
        // this is the static declarator
        //for (int i = 0; i < n.getNode(3).size(); i++){
        if (!n.getNode(3).isEmpty()){
            printer.indent().pln(n.getNode(3).getString(0) +";");

        }

        printer.indent().pln(n.getString(1) + " {");
        visit(n);
        printer.decr();
        printer.indent().pln("}");
    }


    public void visitMainMethodBlockImplementation(GNode n) throws IOException{
        printer.incr();
        //Use a for loop to go through the block stuff
        for (int i = 0; i < n.size(); i ++){
            //Check if the node is a string or not
            if(n.get(i).toString().contains("ForloopImplementation") || n.get(i).toString().contains("WhileloopImplementation") || n.get(i).toString().contains("BlockImplementation") ){
                //ForLoop, Whileloop and block are found
                //System.out.println(n.getNode(i).getName());
                //Call visit to find deeper nodes
                printer.indent().pln(n.getNode(i).getString(0) + " {");
                visit(n.getNode(i));
                printer.indent().pln("}");
            }
            else if(n.get(i).toString().contains("BlockDecsImplementation")){
                printer.indent().pln ("{"+(n.getNode(i).getString(0))+";");
                visit(n.getNode(i));
                printer.indent().pln("}");
            }
            else{
                //Print the thing there
                printer.indent().pln(n.getString(i) + ";");
            }
        }
        printer.decr();

    }

    public void visitForloopImplementation(GNode n) throws IOException{
        //Print for loop dec statement
        printer.indent().pln(n.getString(0));
        visit(n);
    }

    public void visitForloopBlock(GNode n) throws IOException{
        //Use a for loop to go through the block stuff
        printer.incr();
        for (int i = 0; i < n.size(); i ++){
            //Check if the node is a string or not
            if(n.get(i).toString().contains("ForloopImplementation") || n.get(i).toString().contains("WhileloopImplementation") || n.get(i).toString().contains("BlockImplementation") ||  n.get(i).toString().contains("BlockDecsImplementation")){
                //ForLoop, Whileloop and block are found
                //System.out.println(n.getNode(i).getName());
                //Call visit to find deeper nodes
                printer.indent().pln(n.getNode(i).getString(0) + " {");
                visit(n.getNode(i));
                printer.indent().pln("}");
            }
            else{
                //Print the thing there
                printer.indent().pln(n.getString(i) + ";");
            }
        }
        printer.decr();
    }

    public void visitWhileloopImplementation(GNode n) throws IOException{
        //Print while loop dec statement
        printer.indent().pln(n.getString(0));
        visit(n);
    }

    public void visitWhileloopBlock(GNode n) throws  IOException{
        //Use a for loop to go through the block stuff
        printer.incr();
        for (int i = 0; i < n.size(); i ++){
            //Check if the node is a string or not
            if(n.get(i).toString().contains("ForloopImplementation") || n.get(i).toString().contains("WhileloopImplementation") || n.get(i).toString().contains("BlockImplementation") ||  n.get(i).toString().contains("BlockDecsImplementation")){
                //ForLoop, Whileloop and block are found
                //System.out.println(n.getNode(i).getName());
                //Call visit to find deeper nodes
                printer.indent().pln(n.getNode(i).getString(0) + " {");
                visit(n.getNode(i));
                printer.indent().pln("}");
            }
            else{
                //Print the thing there
                printer.indent().pln(n.getString(i) + ";");
            }
        }
        visit(n);
        printer.decr();
    }

    public void visitBlockDecsImplementation(GNode n) throws  IOException{
        //Use a for loop to go through the block stuff
        for (int i = 0; i < n.size(); i ++){
            //Check if the node is a string or not
            if(n.get(i).toString().contains("ForloopImplementation") || n.get(i).toString().contains("WhileloopImplementation") || n.get(i).toString().contains("BlockImplementation") ||  n.get(i).toString().contains("BlockDecsImplementation")){
                //ForLoop, Whileloop and block are found
                //System.out.println(n.getNode(i).getName());
                //Call visit to find deeper nodes
                printer.indent().pln( "{" + n.getNode(i).getString(0) + ";");
                visit(n.getNode(i));
                printer.indent().pln("}");
            }
            else{
                //Print the thing there
                printer.indent().pln(n.getString(i) + ";");
            }
        }
        printer.decr();
    }
}
