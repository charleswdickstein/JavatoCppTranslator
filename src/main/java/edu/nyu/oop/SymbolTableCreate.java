package edu.nyu.oop;

import edu.nyu.oop.util.ContextualVisitor;
import edu.nyu.oop.util.SymbolTableUtil;
import xtc.lang.JavaEntities;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.type.Type;
import xtc.type.Type.Tag;
import xtc.type.VariableT;
import xtc.util.Runtime;
import xtc.util.SymbolTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SymbolTableCreate extends ContextualVisitor {
    final private Map<String, Tag> summary = new HashMap<>();


    public SymbolTableCreate(Runtime runtime, SymbolTable table) {
        super(runtime, table);
    }


    public void visitMethodDeclaration(GNode n){
         SymbolTableUtil.enterScope(table, n);

       // summary.put(n.getString(3)+"()", Tag.FUNCTION);

    // Extract a list representing the parameters to this method.
    List<VariableT> params = SymbolTableUtil.extractFormalParams(table.current());
        for(VariableT p : params) {
        //runtime.console().p("my method ").p(n.getName());

       // Type type = (Type) table.current().lookup(name);

      //  runtime.console().p("my method ").p(n.getName()).p(" with type ").p(type.toString()).p(" at location ").p(n.getLocation().toString()).pln(type.toString());

     //   runtime.console.p("params ").p(params);
        summary.put(p.getName(), p.tag());
    }

    visit(n);
        SymbolTableUtil.exitScope(table, n);
}


//    public void visitClassDeclaration(GNode n) {
//        SymbolTableUtil.enterScope(table, n);
//
//       // summary.put(n.getString(3)+"()", Tag.FUNCTION);
//
//        // Extract a list representing the parameters to this method.
//        //List<VariableT> params = SymbolTableUtil.extractFormalParams(table.current());
//        //for(VariableT p : params) {
//            //runtime.console().p("my print ").p(n.getName());
//
//           // summary.put(p.getName(), p.tag());
//        //}
//
//        visit(n);
//        SymbolTableUtil.exitScope(table, n);
//    }


    // public void visitFieldDeclaration(GNode n){
    //    String name = n.getName();

    //     if (table.current().isDefined(name)) {
    //         Type type = (Type) table.current().lookup(name);
    //         runtime.console().loc(n).pln();
    //         summary.put(name, type.tag());
    //         if (JavaEntities.isParameterT(type))
    //             runtime.console().p("local variable ").p(name).p(" with type ").p(type.toString()).p(" at location ").p(n.getLocation().toString()).pln(type.toString());
    //         else if (JavaEntities.isFieldT(type))
    //             runtime.console().p("field ").p(name).p(" with type ").p(type.toString()).p(" at location ").p(n.getLocation().toString()).pln(type.toString());

    //     }
    // }



    //  public void visitMethodDeclaration(GNode n) {
    //     SymbolTableUtil.enterScope(table, n);

    //     summary.put(n.getString(3)+"()", Tag.FUNCTION);

    //     // Extract a list representing the parameters to this method.
    //     List<VariableT> params = SymbolTableUtil.extractFormalParams(table.current());
    //     for(VariableT p : params) {
    //         summary.put(p.getName(), p.tag());
    //     }

    //     visit(n);
    //     SymbolTableUtil.exitScope(table, n);
    // }



    // public void visitPrimaryIdentifier(GNode n) {
    //     String name = n.getString(0);

    //     if (table.current().isDefined(name)) {
    //         Type type = (Type) table.current().lookup(name);
    //         runtime.console().loc(n).pln();
    //         summary.put(name, type.tag());
    //         if (JavaEntities.isParameterT(type))
    //             runtime.console().p("local variable ").p(name).p(" with type ").p(type.toString()).p(" at location ").p(n.getLocation().toString()).pln(type.toString());
    //         else if (JavaEntities.isFieldT(type))
    //             runtime.console().p("field ").p(name).p(" with type ").p(type.toString()).p(" at location ").p(n.getLocation().toString()).pln(type.toString());

    //     }
    // }

    public Map<String, Tag> getSummary(Node n) {
        this.dispatch(n);
        return this.summary;
    }

    public void printSummary() {
        runtime.console().pln("printSummary()");
        Set<String> keys = summary.keySet();
        for(String k : keys) {
            runtime.console().pln("  " + k + ":" + summary.get(k));
        }
        runtime.console().flush();
    }

}
