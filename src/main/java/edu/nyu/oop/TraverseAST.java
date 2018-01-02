package edu.nyu.oop;

import org.slf4j.Logger;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import java.util.Iterator;

import  edu.nyu.oop.CustomClassObject;

import java.util.List;
import java.util.ArrayList;
/**
 * This class demostrates a trivial usage of Xtc's Visitor class.
 * You may use this as a base for your ScopeVisitor.
 */
public class TraverseAST extends Visitor {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    // CustomClassObject
    private ClassSummary classSummary = new ClassSummary();

    public static final int DEBUGGING = 1;
    //Global var to keep track of the current class in AST
    public CustomClassObject currentClass;

    public void visitCompilationUnit(GNode n) {
        visit(n);
    }

    private void visitClassBody(GNode n){
        visit(n);
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

    private CustomConstructorClass traverseConstructorDeclaration(Node n) {
        // name is 2nd
        String name = n.getString(2);
        // modifiers node
        Node modifiers = n.getNode(0);
        CustomConstructorClass currentConstructor = new CustomConstructorClass(name);
        // only possible modifiers for constructor is visibility
        if (modifiers.size() == 0) {
            currentConstructor.setVisibility("");
        } else {
            System.out.println(modifiers.getNode(0).getString(0));
            currentConstructor.setVisibility(modifiers.getNode(0).getString(0));
        }
        Node params = n.getNode(3);
        if (params.size() > 0) {
            for (int i = 0; i < params.size(); i++) {
                // need to refactor, uses feilddeclaraion instead
                // of formalparamater. They're basically the same,
                // except for the difference outlined int the above
                // code
                currentConstructor.addParameter(TraverseFieldDeclaration(params.getNode(i)));
            }
        }
        return currentConstructor;
    }


    //VISIT METHODS
    public void visitPackageDeclaration(GNode n){
        //Get the node QualifiedIdentifier
        Node qualifiedIdentifier = n.getNode(1);
        //Get len of QualifiedIden
        int len = qualifiedIdentifier.size();
        for (int i = 0; i < len; i++){
            //for loop to get the names
            //System.out.println(n.getNode(1).getString(i));
            classSummary.packages.add(qualifiedIdentifier.getString(i));
        }
    }


    public void getInheritedFields(CustomClassObject currentClass, String classExtension){
        Iterator<CustomClassObject> iter = classSummary.classes.iterator();
        while (iter.hasNext()) {
            CustomClassObject c = iter.next();
            if (c.className.equals(classExtension)) {
                Iterator<CustomVariablesClass> iter2 = c.getClassVariables().iterator();
                while (iter2.hasNext()){
                    CustomVariablesClass v = iter2.next();
                    currentClass.addClassVariable(v);

                }
            }
        }

    }


    public void visitClassDeclaration(GNode n) {
        //Entering the class scope & create a new class object
        currentClass = new CustomClassObject();
        //Get the class name
        currentClass.className = n.getString(1);

        //Get the class extensions
        Node extention = n.getNode(3);
        String classExtension = "None";
        if (extention != null){
            System.out.println("currentClass123 " + currentClass.getClassName());
            //there is an extension
            classExtension = extention.getNode(0).getNode(0).getString(0);
            // get inherited fields
            getInheritedFields(currentClass, classExtension);
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
                //For DEBUG
                if (DEBUGGING == 1){
                    for (CustomVariablesClass v : currentClass.classVariables){
                        //System.out.println("Class vars " + v.name);
                    }
                }
            }
        }

        visit(n);
        //Class scope exists. Hence, end of the class
        //Save the class object to the array of class objects
        classSummary.classes.add(currentClass);
        classSummary.javaFile.classes.add(currentClass);
        //Make the pointer point to NULL
       // System.out.println("End of class: " + currentClass.className);
        currentClass = null;
    }



    public void visitMethodDeclaration(GNode n) {
        CustomMethodClass currentMethodObj = new CustomMethodClass();
        Node currMethod = n;
        if (n.getString(3) != null){
            currentMethodObj.name = n.getString(3);
        }

        //Get method return type
        Node returnType = n.getNode(2);
        //System.out.println("Method Return " + returnType.size());
        if(returnType.size() > 0){
            //Return type is not void
            //Get the return type
            String rt = returnType.getNode(0).getString(0);
            currentMethodObj.returnType = rt;
            // System.out.println("Method Return Type " + rt);
        }
        else{
            //Return type is void
            currentMethodObj.returnType = returnType.getName();
            //System.out.println("Method Return Type " + returnType.getName());
        }

        Node methodModifers = currMethod.getNode(0);
        String wholeModifier = "";
        int totalModsInMethod = currMethod.getNode(0).size();
        for (int j = 0; j < totalModsInMethod; j++) {
            String modifierVal = currMethod.getNode(0).getNode(j).getString(0);
            if (checkMethodVisibility(modifierVal) == true) {
                // System.out.println("vis");
                currentMethodObj.visibility = modifierVal;
            } else {
                wholeModifier += modifierVal;
            }
            //System.out.println(methodModifers.getNode(0).getString(j));
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
                    //System.out.println(currentMethodObj.name);
                }
            }
        }

        //Handle OverLoading
        String currNamem = currentMethodObj.getName();
        String newName = currNamem;
        ArrayList<CustomVariablesClass> parameters = currentMethodObj.parameters;
        //Get all the currentMethods param's
        for (CustomVariablesClass var: parameters){
            newName = newName + "_" + var.getType();
        }

        //Handle static
        if(currentMethodObj.modifier != null && currentMethodObj.modifier.contains("static")){
            newName += "_static";
        }

        //Save the new name for the method
        currentMethodObj.name = newName;
        currentClass.methods.add(currentMethodObj);

        visit(n);
        System.out.println("Class " + currentClass.className);
        for (CustomMethodClass cm : currentClass.methods){
            //System.out.println("TESTMETHODS1234567 \n" + cm.getName());
        }
    }


    public void visitConstructorDeclaration(GNode n){
        CustomConstructorClass constructor = traverseConstructorDeclaration(n);
        currentClass.addConstructor(constructor);
    }


    public void visit(Node n) {
        for (Object o : n) if (o instanceof Node) dispatch((Node) o);
    }


    public ClassSummary getClassSummary(Node n) {
        super.dispatch(n);
        return classSummary;
    }

    //Class Summary represents a file
    static class ClassSummary{
        ArrayList<CustomClassObject> classes = new ArrayList<CustomClassObject>();
        ArrayList<String> packages = new ArrayList<String>();
        JavaFileObject javaFile =  new JavaFileObject();
        ArrayList<CustomMethodClass> methods = new ArrayList<CustomMethodClass>();
    }
}