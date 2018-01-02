package edu.nyu.oop;

import edu.nyu.oop.util.ContextualVisitor;
import edu.nyu.oop.util.SymbolTableUtil;
import edu.nyu.oop.util.TypeUtil;
import org.slf4j.Logger;
import xtc.lang.JavaEntities;

import xtc.Constants;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.util.SymbolTable;
import xtc.util.Runtime;
import xtc.type.*;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.*;


public class TraverseASTM extends ContextualVisitor {

    //Keep track if its a constructor
    //Because symboltable simplifies the AST
    boolean isConstructor = false;

    // CustomClassObject
    private ImplementationSummary implementationSummary = new ImplementationSummary();

    //Global var to keep track of the current class in AST
    public CustomClassObject currentClass;

    //Class Summary represents a file
    static class ImplementationSummary{
        ArrayList<CustomClassObject> implementationClassObjects = new ArrayList<CustomClassObject>();
    }

    public ImplementationSummary getImplementationSummary(Node n) {
        super.dispatch(n);
        return implementationSummary;
    }

    public TraverseASTM(Runtime runtime, SymbolTable table) {
        super(runtime, table);
    }

    public GNode makeThisExpression() {
        GNode _this = GNode.create("ThisExpression", null);
        TypeUtil.setType(_this, JavaEntities.currentType(table));
        return _this;
    }

    //Add our own VisitMethods for fields etc
    @Override
    public void visitClassDeclaration(GNode n) {
        SymbolTableUtil.enterScope(table, n);
        table.mark(n);

        //Entering the class scope & create a new class object
        currentClass = new CustomClassObject();
        //Get the class name
        currentClass.className = n.getString(1);

        //Get the class extensions
        Node extention = n.getNode(3);
        String classExtension = "None";
        if (extention != null){
            //there is an extension
            classExtension = extention.getNode(0).getNode(0).getString(0);

        }
        //Add the extension to the class
        currentClass.setParentClass(classExtension);
        currentClass.parentClass = classExtension;

        // Get all modifiers of the class and add them to the class object
        Node modifiers  = n.getNode(0);
        for (int i = 0; i < modifiers.size(); i++) {
            Node curNode = modifiers.getNode(i);
            String modifierName = curNode.getString(0);
            currentClass.modifiers.add(modifierName);

        }

        //Get the field decleration node
        Node classBody = n.getNode(5);
        //Get the size of the field
        int classBodySize = n.getNode(5).size();
        // check all field declarations
        for (int i = 0; i < classBodySize; i++){
            Node curNode = classBody.getNode(i);
            // if a field Declaration is found get the class variable contained within it
            if (curNode.getName().equals("FieldDeclaration")){
                // calls a custom method TraverseFieldDeclaration to collect variable info
                CustomVariablesClass aVar = TraverseFieldDeclaration(curNode);
                currentClass.addClassVariable(aVar);
            }
        }

        visit(n);
        SymbolTableUtil.exitScope(table, n);

        //Class scope exists. Hence, end of the class
        //Save the class object to the array of class objects
        implementationSummary.implementationClassObjects.add(currentClass);
        currentClass = null;
    }

    // this function is used to get class Vars
    public CustomVariablesClass TraverseFieldDeclaration(Node n){
        CustomVariablesClass varToReturn = new CustomVariablesClass();
        if (n.getNode(0).getName().equals("Modifiers") && n.getNode(0).size() > 0){
            String varModifiers = "";
            int amountOfModifers = n.getNode(0).size();
            for (int i = 0; i < amountOfModifers; i++){
                varModifiers += n.getNode(0).getNode(0).getString(i) + " ";
            }
            varToReturn.modifier = varModifiers;
        }

        // Get Qualified Identifier
        String qualifiedIdentifier = n.getNode(1).getNode(0).getString(0);
        varToReturn.type = qualifiedIdentifier;
        // Get Declarator Identifier
        String declarator;
        if (n.getNode(2) != null) {
            // then this is a declarator
            declarator = n.getNode(2).getNode(0).getString(0);
            //Check if the right side has any value
            if (n.getNode(2).getNode(0).getNode(2) != null){
                //Right side value exists
                //Get the value
                varToReturn.declaratorRightSide = n.getNode(2).getNode(0).getNode(2).getString(0);
                if(n.getNode(2).getNode(0).getNode(2).getName() == "StringLiteral" && !varToReturn.modifier.contains("static")){
                    varToReturn.declaratorRightSideType = "string";
                }
                else if(n.getNode(2).getNode(0).getNode(2).getName() == "IntegerLiteral"&& !varToReturn.modifier.contains("static")){
                    varToReturn.declaratorRightSideType = "integer";
                }
            }
        } else {
            // then this is a formalparameter
            declarator = n.getString(3);
        }

        if(declarator.contains("self")){
            declarator = "Self";
        }
        varToReturn.name = declarator;
        String theVariable = qualifiedIdentifier + " " + declarator;
        return varToReturn;
    }

    @Override
    public void visitMethodDeclaration(GNode n) {
        SymbolTableUtil.enterScope(table, n);
        table.mark(n);
        CustomMethodClass currentMethodObj = new CustomMethodClass();
        Node currMethod = n;

        //Get method return type
        if (n.getNode(2) == null) {
            // constructor
            GNode classType = GNode.create("Type", GNode.create("QualifiedIdentifier", n.getString(3)));
            n.set(2, classType);
            n.set(3, "__init");
            isConstructor = true;
        }
        Node returnType = n.getNode(2);

        if (n.getString(3) != null){
            currentMethodObj.name = n.getString(3);
        }

        if(returnType.size() > 0){
            //Return type is not void
            //Get the return type
            String rt = returnType.getNode(0).getString(0);
            currentMethodObj.returnType = rt;
        }
        else{
            //Return type is void
            currentMethodObj.returnType = returnType.getName();
        }

        Node methodModifers = currMethod.getNode(0);
        String wholeModifier = "";
        int totalModsInMethod = currMethod.getNode(0).size();
        for (int j = 0; j < totalModsInMethod; j++) {
            String modifierVal = currMethod.getNode(0).getNode(j).getString(0);
            if (checkMethodVisibility(modifierVal) == true) {
                currentMethodObj.visibility = modifierVal;
            } else {
                wholeModifier += modifierVal;
            }
            currentMethodObj.modifier = wholeModifier;
        }

        if (currMethod.getNode(4).getName().equals("FormalParameters") && currMethod.getNode(4).size() > 0) {
            Node formalParams = currMethod.getNode(4);
            for (int i = 0; i < currMethod.getNode(4).size(); i++) {
                if (currMethod.getNode(4).getNode(i).getName().equals("FormalParameter")) {
                    CustomVariablesClass aVar = new CustomVariablesClass();
                    Node currentformalParameter = currMethod.getNode(4).getNode(i);
                    if (currentformalParameter == null){
                        continue;
                    }
                    CustomVariablesClass myVar = new CustomVariablesClass();
                    // get method parameters
                    myVar.modifier = "";
                    // get parameter variable type
                    if (currentformalParameter.getNode(1).getName().equals("Type")) {
                        Node getVarName = currentformalParameter.getNode(1).getNode(0);
                        myVar.type = getVarName.getString(0);
                    }
                    //Get parameter var name
                    if (currentformalParameter.getString(3) != null)
                        myVar.name = currentformalParameter.getString(3);
                        currentMethodObj.parameters.add(myVar);
                }
            }
        }

        //Handle Overloading Here
        //We simply change all method names to include their paramter types
        //By doing so we handle Overriding cases, Overloading cases and do not need to keep track of inheritence
        //Get current method name
        if (!isConstructor){
            String currNamem = currentMethodObj.getName();
            String newName = currNamem;
            ArrayList<CustomVariablesClass> parameters = currentMethodObj.parameters;
            //Get all the currentMethods param's
            for (CustomVariablesClass var: parameters){
                newName = newName + "_" + var.getType();
            }
            //Save the new name for the method
            currentMethodObj.name = newName;
        }

        //Handle static
        if(currentMethodObj.modifier != null && currentMethodObj.modifier.contains("static")){
            currentMethodObj.name += "_static";
        }

        //Get the methods block
        for (int i = 0; i < currMethod.size(); i++){
            if (currMethod.get(i) != null && currMethod.get(i).getClass().equals(String.class)){
              continue;
            }
            Node currentNode = currMethod.getNode(i);
            if (currentNode != null && currentNode.getName() == "Block"){
                currentMethodObj.setMethodsBlock(currentNode);
                break;
            }
        }



        currentClass.methods.add(currentMethodObj);
        isConstructor = false;
        visit(n);
        SymbolTableUtil.exitScope(table, n);
    }
    // this is a method to check if a Modifer()'s string is a visibility parameter
    public boolean checkMethodVisibility(String check){
        switch (check){
            case "public":
                return true;
            // break;
            case "private":
                return true;
            // break;
            case "protected":
                return true;
            // break;
            default:
                return false;
        }
    }

    public void visitCallExpression(GNode n) {
        visit(n);
        Node receiver = n.getNode(0);
        String methodName = n.getString(2);

        System.out.println("thispla");
        if (methodName == "super") {
            return;
        }

        Type typeToSearch = JavaEntities.currentType(table);
        List<Type> actuals = JavaEntities.typeList((List) dispatch(n.getNode(3)));

        if (receiver == null &&
                !"super".equals(methodName) &&
                !"this".equals(methodName)) {

            // find type to search for relevant methods
            //Type typeToSearch = JavaEntities.currentType(table);

            // find type of called method
            //List<Type> actuals = JavaEntities.typeList((List) dispatch(n.getNode(3)));
            MethodT method =
                    JavaEntities.typeDotMethod(table, classpath(), typeToSearch, true, methodName, actuals);


            if (method == null) return;

            //make 'this' access explicit
            if (!TypeUtil.isStaticType(method)) {
                n.set(0, makeThisExpression());
            }

        }

        receiver = n.getNode(0);
        if (receiver != null) {

        }

        Type t = JavaEntities.resolveIfAlias(table, classpath(), table.current().getQualifiedName(), TypeUtil.getType(receiver));
//        String calleeClassName = TypeUtil.getType(receiver).toAlias().getName();
        String calleeClassName;
        if (!t.hasAlias()) {
            calleeClassName = "";
        } else {
            calleeClassName = t.toAlias().getName();
        }
        String baseMethodName = new String(methodName);

        ArrayList<String> calleeMethodNames = new ArrayList<String>();
        for (CustomClassObject classObject : implementationSummary.implementationClassObjects) {
            if (classObject.getClassName().equals(calleeClassName)) {
                calleeMethodNames = classObject.getMethodNames();
            }
        }
        for (Type at : actuals){
            if (at.hasScope()) {
                String[] splittedScope = at.getScope().split("\\.");
                String type = splittedScope[splittedScope.length - 1];
                methodName += "_" + type;
            } else if (at.hasConstant()) {
                // if overloaded with int
            }
        }
        // if methodName is in class methods:
            // add it
        // else
            // search classmethods for closest match (using .contains)
            // add that

//        ArrayList<String> allMethodNames = receiver.get.getMethodNames();
        boolean methodSet = false;
        for (int i = 0; i < calleeMethodNames.size(); i++) {
            // remove the base method
            if (calleeMethodNames.get(i).equals(baseMethodName)) {
                calleeMethodNames.remove(i);
            } else if (calleeMethodNames.get(i).equals(methodName)) {
                methodSet = true;
                n.set(2, methodName);
                break;
            }
        }

        if (!methodSet) {
            for (int i  = 0; i < calleeMethodNames.size(); i++) {
                if (calleeMethodNames.get(i).contains(baseMethodName)) {
                    n.set(2, calleeMethodNames.get(i));
                    methodSet = true;
                    break;
                }
            }
        }

        if (!methodSet) {
            n.set(2, methodName);
        }

    }


    public Node visitPrimaryIdentifier(GNode n) {
        String fieldName = n.getString(0);

        // find type to search for relevant fields
        ClassOrInterfaceT typeToSearch = JavaEntities.currentType(table);
        if (typeToSearch == null) return n;

        // find type of
        VariableT field = null;
        SymbolTable.Scope oldScope = table.current();
        JavaEntities.enterScopeByQualifiedName(table, typeToSearch.getScope());
        for (final VariableT f : JavaEntities.fieldsOwnAndInherited(table, classpath(), typeToSearch))
            if (f.getName().equals(fieldName)) {
                field = f;
                break;
            }
        table.setScope(oldScope);

        if (field == null) return n;

        //make 'this' access explicit
        Type t = (Type) table.lookup(fieldName);
        if (t == null || !t.isVariable()) {
            t = field;
        }

        if (JavaEntities.isFieldT(t) && !TypeUtil.isStaticType(t)) {
            GNode n1 = GNode.create("SelectionExpression", makeThisExpression(), fieldName);
            TypeUtil.setType(n1, TypeUtil.getType(n));
            return n1;
        }

        return n;
    }

    public List<Type> visitArguments(final GNode n) {
        List<Type> result = new ArrayList<Type>(n.size());
        for (int i = 0; i < n.size(); i++) {
            GNode argi = n.getGeneric(i);
            Type ti = (Type) argi.getProperty(Constants.TYPE);
            if (ti.isVariable()) {
                VariableT vi = ti.toVariable();
                ti = vi.getType();
            }
            result.add(ti);
            Object argi1 = dispatch(argi);
            if (argi1 != null && argi1 instanceof Node) {
                n.set(i, argi1);
            }
        }
        return result;
    }

    public void visit(GNode n) {
        for (int i = 0; i < n.size(); ++i) {
            Object o = n.get(i);
            if (o instanceof Node) {
                Object o1 = dispatch((Node) o);
                if (o1 != null && o1 instanceof Node) {
                    n.set(i, o1);
                }
            }
        }
    }



}
