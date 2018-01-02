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

public class CppHeaderPrinter extends Visitor {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private Printer printer;

    private String outputLocation = XtcProps.get("output.location");

    public CppHeaderPrinter() {
        Writer w = null;
        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/output.h");
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

    public void visitDependencies(GNode n) throws IOException {
        for (int i = 0; i < n.size(); i++) {
            printer.pln( n.getString(i));
        }
        printer.pln("");
        visit(n);
    }

    public void visitUsingNamespace(GNode n) throws IOException {
        printer.pln("using namespace " + n.getString(0) + ";");
        visit(n);
        printer.pln("");
    }

    int nameSpaceCount = 0;
    public void visitNamespace(GNode n) throws IOException {
        if(nameSpaceCount == 0){
            printer.pln("namespace " + n.getString(0) + " {");
        }
        else{
            printer.incr().indent().pln("namespace " + n.getString(0) + " {");
        }
        nameSpaceCount++;
        visit(n);
        nameSpaceCount = 0;
        printer.indent().pln("}");
        //Decrement the printer for final brace
        printer.decr();
    }
    public void visitStructs(GNode n) throws IOException {
        printer.indent().pln("");
        visit(n);
    }
    public void visitStruct(GNode n) throws IOException {
        printer.incr().indent().pln("struct " + n.getString(0) + ";");
        printer.indent().pln("struct " + n.getString(0) + "_VT;");
        printer.indent().pln("typedef " + n.getString(1) + ";");
        printer.indent().pln("");
        printer.indent().pln("struct " + n.getString(0) + " { ");
        // print vtable pointer
        //Increment the printer again for the struct stuff
        printer.incr();
        printer.indent().pln("");
        printer.indent().pln(n.getString(2) +";");
        printer.indent().pln("");
        // the constructor for the struct (initializer)
        printer.indent().pln(n.getString(0) + "();");

        printer.pln("");

        visit(n);

        printer.indent().pln(n.getString(3)+";");
        printer.pln("");
        printer.indent().pln(n.getString(4)+";");

        //Decrement the printer before closing
        printer.decr();
        printer.indent().pln("};");
        printer.pln("");

        //decrement printer for the new struct that comes after
        printer.decr();

    }
    public void visitMethods(GNode n) throws IOException {
        visit(n);

        printer.p("");
    }

    public void visitVisability(GNode n) throws IOException {

    }
    public void visitParameters(GNode n) {
        printer.p('(');
        visit(n);
        // TODO: remove comma at the end of the line
        // before closing parentheses
        StringBuilder sb = new StringBuilder();
        Node curNode;
        for (int i = 0; i < n.size(); i++) {
            curNode = n.getNode(i);
            sb.append(curNode.getString(0));
            sb.append(", " );
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        printer.p(sb.toString());
        printer.p(')');
    }
    public void visitParameter(GNode n) {
        visit(n);
    }
    public void visitMethod(GNode n) throws IOException {
        // all public for now

        CppDataLayout.typeTranslate tt = new CppDataLayout.typeTranslate();
        String type = tt.translateType(n.getString(1));
        printer.indent().p("static " + n.getString(1) + " " + n.getString(0));
        printer = printer.buffer();
        visit(n);
        printer.pln(";");
    }
    public void visitFields(GNode n) throws IOException {
        visit(n);
    }
    public void visitField(GNode n) throws IOException {
        if(n.getNode(2).size() > 0){
            printer.indent().pln(n.getNode(2).getString(0)+ " " + n.getString(0) + " " + n.getString(1) + ";");
        }
        else{
            printer.indent().pln(n.getString(0) + " " + n.getString(1) + ";");
        }
        visit(n);
        printer.pln("");
    }

    public void visitVTable(GNode n) throws IOException {
        printer.incr();

        printer.indent().pln("struct __" + n.getString(0) + "_VT { ");
        //Print __is_a
        printer.incr().indent().pln(n.getString(3) + ";");
        printer.pln("");

        visit(n);
        //decrement
        printer.decr();
        printer.decr();
        printer.indent().pln("};");;
        //Decrement for next vtable
        printer.decr();
        printer.pln("");
    }

    public void visitVTMethod(GNode n) {
        printer.indent().pln(n.getString(0) + ";");
        visit(n);
    }

    public void visitVTMethods(GNode n) {
        visit(n);
        printer.pln();
    }

    public void visitVTInstantiators(GNode n) {
        visit(n);
    }

    public void visitVTInstantiator(GNode n) {
        //Print Vtable name
        printer.indent().pln(n.getString(0));
        //print __is_a
        printer.indent().pln(n.getString(1));
        visit(n);
        printer.pln(n.getString(3));
    }

    public void visitInstantiatorMethods(GNode n) {
        printer.incr();
        for (int i = 0; i < n.size(); i++) {
            if (i < n.size() - 1) {
                printer.indent().pln(n.getString(i) + ",");
            } else {
                printer.indent().p(n.getString(i));
            }
        }
        visit(n);
    }


    private void headOfFile() {
        printer.pln("#include <iostream>");
        printer.pln("#include \"java_lang.h\"");
        printer.pln();
        printer.pln("using namespace java::lang;");
        printer.pln("using namespace std;");
        printer.pln();
        printer.pln("int main(void) {");
    }

    private void cout(String line) {
        printer.incr().indent().pln("cout << \"" + line + "\" << endl;").decr();
    }

    private void tailOfFile() {
        printer.incr().indent().pln("return 0;");
        printer.decr(); // not really necessary, but for demonstration.
        printer.pln("}");
    }

}
