package edu.nyu.oop;

import java.lang.reflect.Array;
import java.util.ArrayList;

import edu.nyu.oop.*;
import jdk.nashorn.internal.ir.ExpressionStatement;
import xtc.tree.GNode;
import java.util.Map;
import edu.nyu.oop.util.CppDataLayout;
import xtc.tree.Node;

public class CppDataLayoutM {


    public CppDataLayoutM() { }

    //MAIN METHOD IMPLEMENTATION
    public static class cppImplementationMainMethodClass {
        //Class to handle the main method
        String mainMethodName;
        String mainMethodReturnType;
        //Property to hold the block class for the method
        TranslatedBlock transLatedBlockForImpMainMethod;
        public static ArrayList<CustomClassObject> allTheClasses = new ArrayList<CustomClassObject>();
        public static ArrayList<String> allTheStaticVars = new ArrayList<String>();



        public cppImplementationMainMethodClass(CustomMethodClass m,  ArrayList<CustomClassObject> allTheClasses) {
            this.mainMethodName = "int main(int argc, char* argv[])";
            this.mainMethodReturnType = "return 0";
            this.allTheClasses = allTheClasses;
            //Instant the main methods block

            this.transLatedBlockForImpMainMethod = new TranslatedBlock(m.getMethodsBlock(), false, null, allTheClasses);
            this.transLatedBlockForImpMainMethod.returnStatement = "return 0";

        }
    }

    //METHODS IMPLEMENTATIONS
    public static class cppImplementationClass {
        String className;
        String vTableInit;
        String classMethodInit;
        String vTableDecl;
        String parentClass;
        String deafultConstructorImplementation;

        ArrayList<cppMethodImplementation> cppMethodImplementations;

        public ArrayList<cppMethodImplementation> getCppMethodImplementations() {
            return cppMethodImplementations;
        }

        //Default init for class
        //If the class override this with its own implementation this will be null
        cppMethodImplementation defaultInIt;

        public cppImplementationClass(CustomClassObject theClass, ArrayList<CustomClassObject> allTheClasses) {
            if (theClass.getParentClass() == "None") {
                this.parentClass = "Object";
            } else {
                this.parentClass = theClass.getParentClass();
            }
            this.className = theClass.getClassName();
            this.vTableInit = "__" + this.className + "::__" + this.className + "() : __vptr(&__vtable) \n             {}";
            this.classMethodInit = "Class __" + this.className + "::__class() {\n" +
                    "      static Class k =\n" +
                    "        new __Class(__rt::literal(\"inputs.javalang." + this.className + "\"), __" + parentClass + "::__class());\n" +
                    "      return k;\n" +
                    "    }";
            this.vTableDecl = "__" + this.className + "_VT " + "__" + this.className + "::__vtable;";
            this.cppMethodImplementations = new ArrayList<cppMethodImplementation>();

            //Get the constructor implementations via the class fields
            this.deafultConstructorImplementation = "__" + theClass.getClassName() + "::__" + theClass.getClassName() + "() : __vptr(&__vtable)";
            //Add the class fields
            for (CustomVariablesClass varClass : theClass.getClassVariables()) {
                if(varClass.getModifier() != null && !varClass.getModifier().contains("static")){
                    typeTranslatorToDeafultTypeClass deafultTypeClassTranslator = new typeTranslatorToDeafultTypeClass();
                    String deafultInstanceType = deafultTypeClassTranslator.translateDefaultType(varClass.getType());

                    this.deafultConstructorImplementation += ", " + varClass.getName() + "(" + deafultInstanceType + ")";
                }
                else if (varClass.getModifier() == null){
                    typeTranslatorToDeafultTypeClass deafultTypeClassTranslator = new typeTranslatorToDeafultTypeClass();
                    String deafultInstanceType = deafultTypeClassTranslator.translateDefaultType(varClass.getType());

                    this.deafultConstructorImplementation += ", " + varClass.getName() + "(" + deafultInstanceType + ")";
                }
            }
            this.deafultConstructorImplementation += "\n" + "    {}";


            //Create def init method for each class
            this.defaultInIt = new cppMethodImplementation();
            this.defaultInIt.name = theClass.className + " " + "__" + theClass.className + "::__init(" + theClass.className + " __this)";
            this.defaultInIt.returnType = "__this";
            this.defaultInIt.isConstuctor = true;
            TranslatedBlock blockForDef = new TranslatedBlock();
            blockForDef.classLevelInitFields = new ArrayList<String>();
            blockForDef.deafultConsturctorCall = "__Object::__init((Object) __this)";
            if(theClass.getParentClass() != "None"){
                blockForDef.deafultConsturctorCall = "__" + theClass.getParentClass() + "::__init((" + theClass.getParentClass() + ") __this)";
            }
            blockForDef.isConstructor = true;
            ArrayList<CustomVariablesClass> classFields = theClass.getClassVariables();
            for (CustomVariablesClass classField : classFields) {
                if (classField.declaratorRightSide != null) {

                    String finalString = "";
                    if (classField.declaratorRightSideType == "string") {
                        finalString += "__this->" + classField.getName() + " = " + "__rt::literal(" + classField.getDeclaratorRightSide() + ")";
                    } else if (classField.declaratorRightSideType == "integer") {
                        finalString += "__this->" + classField.getName() + " = " + classField.getDeclaratorRightSide();
                    }
                    blockForDef.classLevelInitFields.add(finalString);

                }
            }
            blockForDef.returnStatement = "return __this";
            this.defaultInIt.translatedBlock = blockForDef;

            //Process the methods
            for (CustomMethodClass method : theClass.getMethods()) {
                cppMethodImplementation methodImp = new cppMethodImplementation(method, this.className, theClass, allTheClasses);
                this.cppMethodImplementations.add(methodImp);

                //Check if the class overrides the default init in its methods
                if(method.getName() == "__init" && method.getParameters().size() == 0){
                    //if so
                    this.defaultInIt = null;
                }
            }

        }
    }


    // If newArrayExpression it has , check concrete dimensions
    public static class CustomFieldDeclaration{

        int position;

        boolean isClass;
        String fieldDeclarationLine;
        String allModifiers = "";
        String varType = "";
        String declaratorVar = "";
        String declaratorVal = "";
        boolean isArray = false;
        ArrayList<String> mainFileLines;

        public CustomFieldDeclaration(Node fieldDec, int position) {

            mainFileLines = new ArrayList<String>();
            this.position = position;

            String qualifiedIdentifier = "";
            for (int i = 0; i < fieldDec.size(); i++) {
                if (fieldDec.getNode(i).getName().equals("Modifiers")) {
                    Node modifiers = fieldDec.getNode(i);
                    // get all modifiers
                    for (int m = 0; m < modifiers.size(); m++ ){
                        allModifiers += modifiers.getString(m);
                    }
                }

                if (fieldDec.getNode(i).getName().equals("Type")) {
                    Node getType = fieldDec.getNode(i);;
                    if (getType.getNode(0) == null){continue;}
                    if (!getType.getNode(0).isEmpty()){
                        varType = getType.getNode(0).getString(0);
                    }



                    if (getType.getNode(1) != null && !getType.getNode(1).isEmpty()){

                        System.out.println("arrayistrue");

                        isArray = true;

                    }


                }

                if (fieldDec.getNode(i).getName().equals("Declarators")) {
                    //System.out.println("In declarators");
                    Node declarator = fieldDec.getNode(i).getNode(0);
                    declaratorVar = declarator.getString(0);
                    // if condition, the declarator is an array
                    if (declarator.getNode(2) != null){

                        Node expression = declarator.getNode(2);
                        String expressionName = expression.getName();

                        //System.out.println("expressionName " + expressionName);

                        if (expressionName == "SelectionExpression"){

                            //fieldDeclarationLine = processNameNode(expression.getNode(0)) + "->" + expression.getString(1);
                            // translation: ->

                            String lhs = expression.getNode(0).getString(0);
                            String rhs = expression.getString(1);

                            //for ()




                            declaratorVal = lhs +"->" + rhs;

                            if (isArray){

                               // __rt::Array<A> as =  new __rt::__Array<A>(10);
                                String thing = "__rt::Array<"+varType +">"+ declaratorVar;
                                System.out.println("thething123 " + thing);
                                declaratorVal = "new __rt::__Array<A>("+rhs+")";
                            }


                           // continue;
                            //res = processNameNode(n.getNode(0)) + "->" + n.getString(1);
                        }
                        else if (expressionName == "SubscriptExpression") {
                            System.out.println("insubscript");

                            Node subscriptExpression = declarator.getNode(2);
                            //System.out.println("$sub1 " + subscriptExpression);

                            if (!subscriptExpression.getNode(0).getName().equals("SubscriptExpression")) {
                                //System.out.println("$sub2 " + subscriptExpression);

                                //System.out.println("$idk2 " + subscriptExpression.getNode(0).getNode(0));
                                //System.out.println("$sub3 " + subscriptExpression);

                                declaratorVal += "[" + subscriptExpression.getNode(1).getString(0) + "]";

                            } else {
                                subscriptExpression = declarator.getNode(2).getNode(0);
                                //System.out.println("$sub1 " + subscriptExpression);
                               // System.out.println("$vvv " + subscriptExpression);
                                //System.out.println("$v1 " + declaratorVal);
                                declaratorVal += "[" + subscriptExpression.getNode(0).getString(0) + "]";
                                declaratorVal += "[" + subscriptExpression.getNode(1).getString(0) + "]";
                            }
                            // translation: (*var)[index]
                            //res = "(*" + processNameNode(n.getNode(0)) + ")[" + processNameNode(n.getNode(1)) + "]";
                        } else if (expressionName == "ThisExpression") {
                            // translation: __this
                            //res = "__this";
                        } else if (expressionName == "NewClassExpression") {
                            // translation: __class:__init
                            //fieldDeclarationLine = processNewClassExpression(expression);
                            //System.out.println("fdc123 " + fieldDeclarationLine);

                            Node newClassExpression = declarator.getNode(2);

                            declaratorVal += newClassExpression.getNode(2).getString(0);

                            Node arguments = newClassExpression.getNode(3);

                            declaratorVal += "(";

                            String args = "";

                           //A a =  __A::__init(new __A(), __rt::literal("A"));
                            for (int a = 0; a <arguments.size(); a++){
                             // args +=  processArguments(arguments.getNode(a));
                            //System.out.println("THEARGS " +  arguments.getNode(a).getString(0));
                               if (arguments.getNode(a).getName().equals("StringLiteral")){
                                   args += ", __rt::literal(" + arguments.getNode(a).getString(0) + ")";
                               }
                               else {
                                   args += "," + arguments.getNode(a).getString(0);
                               }

                            }
                            declaratorVal +=")";

                            //System.out.println("argsprint " + args);

                           fieldDeclarationLine =  varType + " " +  declaratorVar + " =  __" + varType + "::__init(new __" + varType  + ""+ args + ")";
//                        fieldDeclarationLine = "__rt::Ptr<" +qualifiedIdentifier + ", __rt::object_policy> "+ declaratorVar + " = new " + declaratorValue;

                            continue;

                        }  else if (expressionName == "NewArrayExpression"){
                            //System.out.println("anexpression1");
// __rt::Array<__rt::Array<A>> as = new __rt::__Array<__rt::Array<A>>(5);




                            System.out.println("express123 " + expression);
                            //declaratorVal += expression.getNode(0).getString(0)+")";
                            Node concreteDimensions = expression.getNode(1);
                            if (concreteDimensions.size() == 1){
                                declaratorVar = "__rt::Array<"+ varType + "> " + declaratorVar ;
                                // __rt::Array<A> as =  new __rt::__Array<A>(10);
                                declaratorVal = " new __rt::__Array<"+varType+">";

                            }
                            else{
                                declaratorVar = "__rt::Array<__rt::Array<"+ varType + ">> " + declaratorVar ;
                                // __rt::Array<A> as =  new __rt::__Array<A>(10);
                                declaratorVal = " new __rt::__Array<__rt::Array<"+varType+">>";
                            }
//                            System.out.println("expressurself " + expression);
//                            System.out.println("dimsize " + concreteDimensions.size());
                            for (int d = 0; d < concreteDimensions.size(); d++){
                                declaratorVal += "(" + concreteDimensions.getNode(d).getString(0) + ")";
                                break;
                            }
                            fieldDeclarationLine = declaratorVar + " = " + declaratorVal;
                            continue;
                        } // end of NewArrayExpression
                        // else can be integer literals and other primitive types
                        else{
                            System.out.println("imhere1234 " + declarator);

                            // System.out
                            declaratorVal += declarator.getNode(2).getString(0);
                            if (isArray){
                                System.out.println("thisisvar ");
                                fieldDeclarationLine = "__rt::Array<"+varType +"> " +declaratorVar + " = "+declaratorVal;
                                continue;
                            }
                        }

                        fieldDeclarationLine  = varType + " " + declaratorVar + " = " + declaratorVal;
                        //System.out.println("newfielddec22 " + fieldDeclarationLine);
                    }
                    else{
                        // there is no declaratorValue
System.out.println("imhere123 " + declarator);
                        declaratorVar = declarator.getString(0);
                        fieldDeclarationLine = varType + " " + declaratorVar;

//                        if (isArray){
//                            fieldDeclarationLine = varType + " new __rt::__Array<A>"+declaratorVal;
//                        }
                    }
                }
            }
        }



//
//
//
//            } // end of get Declarators
        } // end of CustomFieldDeclaration Constructor


    public static class CustomForLoop{
        int positon;
        TranslatedBlock forLoopsTranslatedBlock;
        String forLoopDecLine = ""; // <- e.g. for(int i = 0; i < as.length; i++)

        public CustomForLoop(Node forLoopNode, int position, CustomClassObject theForLoopsClass,  ArrayList<CustomClassObject> allTheClasses){
            this.positon = position;
            //System.out.println("this is for loop12345 " + forLoopNode);
            String loopIteratorType = "";
            String declaratorVar = "";
            String declaratorVal = "";
            String fullRelationalExpression = "";
            String rightHandVar = "";
            String incrementor;
            String postfixExpression = "";

            for (int i = 0; i < forLoopNode.getNode(0).size(); i++) {

                if (forLoopNode.getNode(0).getNode(i).getName().equals("Type")){
                    //System.out.println("$FOR TYPE");
                    Node getType = forLoopNode.getNode(0).getNode(i);
                    loopIteratorType = getType.getNode(0).getString(0);
                    CppDataLayout.typeTranslate trans = new CppDataLayout.typeTranslate();
                    loopIteratorType = trans.translateType(loopIteratorType);
                }

                if (forLoopNode.getNode(0).getNode(i).getName().equals("Declarators")){


                    Node declarator = forLoopNode.getNode(0).getNode(i).getNode(0);
                    declaratorVar = declarator.getString(0);
                    declaratorVal = declarator.getNode(2).getString(0);
                }
                if (forLoopNode.getNode(0).getNode(i).getName().equals("RelationalExpression")){

                    Node relationalExpression = forLoopNode.getNode(0).getNode(i);
                    String primaryId = relationalExpression.getNode(0).getString(0);
                    String operator = relationalExpression.getString(1);
                    Node selectionExpressionNode =  relationalExpression.getNode(2);
                    String selectionExpression = "";
                    if (selectionExpressionNode.getNode(0).getName().equals("SubscriptExpression")){
                        Node subscriptExpression = selectionExpressionNode.getNode(0);
                        selectionExpression += subscriptExpression.getNode(0).getString(0);
                        for (int s = 1; s < subscriptExpression.size(); s++){
                            selectionExpression += "[" + subscriptExpression.getNode(s).getString(0) + "]";
                        }
                    }else {
                        selectionExpression = selectionExpressionNode.getNode(0).getString(0);
                    }
                    String lengthField = "";
                    if (selectionExpressionNode.getString(1) != null) {
                        lengthField = "." + selectionExpressionNode.getString(1);
                    }
                    //for (int32_t i = 0; i < ({__rt::checkNotNull(args); args->length; }); i++) {
                    fullRelationalExpression += primaryId + " " + operator + "({__rt::checkNotNull("+ selectionExpression +"); "+selectionExpression+"->length; })";
                }
                if (forLoopNode.getNode(0).getNode(i).getName().equals("ExpressionList")){
                    //System.out.println("$FOR EXPRESSION");
                    Node expressionList = forLoopNode.getNode(0).getNode(i);
                    Node postfixExpressionNode = expressionList.getNode(0);
                     postfixExpression = postfixExpressionNode.getNode(0).getString(0) + postfixExpressionNode.getString(1);
                }
            } // end of get for loop header
            this.forLoopDecLine = "for (" + loopIteratorType + " " +  declaratorVar + " = " + declaratorVal  + "; " + fullRelationalExpression + "; " + postfixExpression + ")";
            //System.out.println("fullForLoop1234678 " + forLoopDecLine);
            for (int i = 0; i < forLoopNode.size(); i++) {
                //Use this to find the for loops block
                if(forLoopNode.getNode(i).getName().equals("Block")){
                    //This is the for loops block
                    this.forLoopsTranslatedBlock = new TranslatedBlock(forLoopNode.getNode(i), false, theForLoopsClass, allTheClasses);
                }
            }
        }
    }

    public static class CustomWhileLoop{
        int position;
        TranslatedBlock whileLoopTranslatedBlock;
        String whileLoopDeclarator = "while ";
        String conditional = "";
        String rhsVar = "";

        public CustomWhileLoop(Node whileLoopNode, int position, CustomClassObject theWhileLoopsClass,  ArrayList<CustomClassObject> allTheClasses){
            this.position = position;

            String primaryId = "";
            Node relationalExpressionNode = whileLoopNode.getNode(0);

            primaryId = relationalExpressionNode.getNode(0).getString(0);
            conditional = relationalExpressionNode.getString(1);
            rhsVar = relationalExpressionNode.getNode(2).getString(0);
            whileLoopDeclarator += "(" + primaryId +" " + conditional + " "  + rhsVar +")";


            for (int i = 0; i < whileLoopNode.size(); i++) {
                //Use this to find the for loops block
                if(whileLoopNode.getNode(i).getName().equals("Block")){
                    //This is the for loops block
                    this.whileLoopTranslatedBlock = new TranslatedBlock(whileLoopNode.getNode(i), false, theWhileLoopsClass, allTheClasses);
                }
            }
        }
    }

    public static class CustomBlockDec{
        int positon;
        TranslatedBlock customBlockDecTranslatedBlock;
        public CustomBlockDec(Node blockNode, int positon, CustomClassObject theBlocksClass,  ArrayList<CustomClassObject> allTheClasses){
            this.positon = positon;
            this.customBlockDecTranslatedBlock = new TranslatedBlock(blockNode, false, theBlocksClass, allTheClasses);
        }
    }

    public static class TranslatedBlock {
        // blocks have field declarations
        // Expression statements

        ArrayList<CustomFieldDeclaration> fieldDeclarations;
        public ArrayList<CustomFieldDeclaration> getFieldDeclarations() {
            return fieldDeclarations;
        }
        ArrayList<CustomExpressionStatement> expressionStatements;
        ArrayList<CustomForLoop> forLoops;
        ArrayList<CustomWhileLoop> whileLoops;
        ArrayList<CustomBlockDec> blockDecs;

        String returnStatement = "";

        //Constructor properties
        boolean isConstructor;
        String deafultConsturctorCall;
        ArrayList<String> classLevelInitFields;

        public TranslatedBlock(){
            //Fields for standared method block
            fieldDeclarations = new ArrayList<CustomFieldDeclaration>();
            expressionStatements = new ArrayList<CustomExpressionStatement>();
            forLoops = new ArrayList<CustomForLoop>();
            whileLoops = new ArrayList<CustomWhileLoop>();
            blockDecs = new ArrayList<CustomBlockDec>();
        };

        public TranslatedBlock(Node b, boolean flag, CustomClassObject theMethodsClass,  ArrayList<CustomClassObject> allTheClasses) {

            //If this is a constuctor handle the deafult/explcit constructor call
            //e.g. _Object::__init((Object) __this); OR __A::__init((A) __this, x1);
            this.isConstructor = flag;
            this.classLevelInitFields = new ArrayList<String>();
            this.deafultConsturctorCall = "__Object::__init((Object) __this)";

            //Fields for standared method block
            fieldDeclarations = new ArrayList<CustomFieldDeclaration>();
            expressionStatements = new ArrayList<CustomExpressionStatement>();
            forLoops = new ArrayList<CustomForLoop>();
            whileLoops = new ArrayList<CustomWhileLoop>();
            blockDecs = new ArrayList<CustomBlockDec>();

            for (int i = 0; i < b.size(); i++) {
                if (b.getNode(i).getName().equals("FieldDeclaration")) {
                    CustomFieldDeclaration fd = new CustomFieldDeclaration(b.getNode(i), i);
                    this.fieldDeclarations.add(fd);
                    //System.out.println("fdline " + fd.fieldDeclarationLine);
                }
                else if (b.getNode(i).getName().equals("ExpressionStatement")) {
                    Node curNode = b.getNode(i);
                    if (curNode.getNode(0).getName().equals("CallExpression")) {
                        if (curNode.getNode(0).getString(2).equals("super")) {;
                            continue;
                        }
                    }
                    CustomExpressionStatement ex = new CustomExpressionStatement(b.getNode(i), i, allTheClasses);
                    this.expressionStatements.add(ex);
                    //System.out.println("NODE\n " + b.getNode(i));
                }
                else if (b.getNode(i).getName().equals("ForStatement")){
                    //For loop
                    CustomForLoop forlp = new CustomForLoop(b.getNode(i), i, theMethodsClass, allTheClasses);
                    this.forLoops.add(forlp);
                }
                else if(b.getNode(i).getName().equals("WhileStatement")){
                    //While loop

                    CustomWhileLoop whilelp = new CustomWhileLoop((b.getNode(i)), i, theMethodsClass, allTheClasses);
                    this.whileLoops.add(whilelp);
                }
                else if(b.getNode(i).getName().equals("Block")){
                    //block within a blokc
                    CustomBlockDec blocDec = new CustomBlockDec(b.getNode(i), i, theMethodsClass, allTheClasses);
                    this.blockDecs.add(blocDec);
                }
                else if (b.getNode(i).getName() == "ReturnStatement") {
                    this.returnStatement = "return " + processNameNode(b.getNode(i).getNode(0));
                    if(this.returnStatement.contains("self")){
                        this.returnStatement = this.returnStatement.replace("self","Self");
                    }
                }
            }//End of for loop for block

            //Constructor helper stuff levae this at the end of the class
            //Handle Constructor Stuff
            if (this.isConstructor) {
                //Set return type to this
                this.returnStatement = "return __this";
                //Check if the clas extends object. If not then leave the init call to be Object
                if(theMethodsClass.getParentClass() != "None"){
                    this.deafultConsturctorCall = "__" + theMethodsClass.getParentClass() + "::__init((" + theMethodsClass.getParentClass() + ") __this)";
                }
                //First check what type of default constructor is called by going through the block
                String superOrThis = "";
                for (int k = 0; k < b.size(); k++) {
                    //Find if there is super etc..
                    if (b.getNode(k).getName().equals("ExpressionStatement")) {
                        if (b.getNode(k).getNode(0).getName().equals("CallExpression")) {
                            //Get the call expression node
                            Node callExpression = b.getNode(k).getNode(0);
                            if (callExpression.getString(2).equals("super")) {
                                //Super is called
                                String arguments = "";
                                Node argumentNode = callExpression.getNode(3);
                                for (int g = 0; g < argumentNode.size(); g++) {
                                    //Go through the arguments
                                    arguments += ", " + argumentNode.getNode(g).getString(0);
                                }
                                deafultConsturctorCall = "__" + theMethodsClass.getParentClass() + "::__init((" + theMethodsClass.getParentClass() + ") __this" + arguments + ")";
                            } else if (callExpression.getString(2).equals("this")) {
                                //This Call
                                deafultConsturctorCall = "__init(__this)";
                            }
                        }
                    }
                }

                //Handle Class level field InIts
                //Get the class level fields
                //TEST 003, 006, 007, 008, 025, 017, 021, 024
                ArrayList<CustomVariablesClass> classFields = theMethodsClass.getClassVariables();
                for (CustomVariablesClass classField : classFields) {
                    if(classField.declaratorRightSide != null){
                        //System.out.println("CON CLASS FIELD INIT");
                        //System.out.println(classField.getDeclaratorRightSide());
                        String finalString = "";
                        if (classField.declaratorRightSideType == "string"){
                            finalString += "__this->" + classField.getName() + " = " + "__rt::literal(" + classField.getDeclaratorRightSide() + ")";
                        }
                        else if(classField.declaratorRightSideType == "integer"){
                            finalString += "__this->" + classField.getName() + " = " + classField.getDeclaratorRightSide();
                        }
                        this.classLevelInitFields.add(finalString);
                        //System.out.println(finalString);
                    }
                }

                //System.out.println("THE CONSTRUCTOR CALL123 IN CONSTRUCTOR BLOCK BEGIN");
                //System.out.println(deafultConsturctorCall);
                //for (String field : this.classLevelInitFields){
                   // System.out.println(field);
               // }
                //System.out.println("THE CONSTRUCTOR CALL123 IN CONSTRUCTOR BLOCK END");

            }//End of if constructor statement

        }//END OF Translate Block Constructor
    }//End of TranslatedBlock class


    //Constructor helper stuff
    private static class typeTranslatorToDeafultTypeClass{
        public String translateDefaultType(String javaType) {
            String DefaultType = "";
            switch (javaType) {
                case "int":
                    DefaultType = "0";
                    break;

                case "byte":
                    DefaultType = "0";
                    break;

                case "char":
                    DefaultType = "\u0000";
                    break;

                case "double":
                    DefaultType = "0.0d";
                    break;

                case "float":
                    DefaultType = "0.0f";
                    break;

                case "long":
                    DefaultType = "0L";
                    break;

                case "short":
                    DefaultType = "0";
                    break;

                case "boolean":
                    DefaultType = "false";
                    break;

                case "string":
                    DefaultType = "__rt::literal(\"\")";
                    break;

                default:
                    DefaultType = "__rt::null()";
                    break;
            }
            return DefaultType;
        }
    }

    public static class cppMethodImplementation{
        String returnType;
        String name;
        String params;

        //Block stuff
        Node theBlock;
        TranslatedBlock translatedBlock;

        //See if the method is a constructor
        boolean isConstuctor = false;

        public cppMethodImplementation(){};

        public cppMethodImplementation(CustomMethodClass methodClass, String className, CustomClassObject theMethodsClass,  ArrayList<CustomClassObject> allTheClasses){
            CppDataLayout.typeTranslate typeTranslate = new CppDataLayout.typeTranslate();
            this.returnType = typeTranslate.translateType(methodClass.getReturnType());
            // name = __className
            this.name = this.returnType + " __" + className + "::" + methodClass.getName();

            if(methodClass.getName().contains("static")){
                this.params = "(";
            }
            else{
                this.params = "(" + className + " __this";
            }
            for (CustomVariablesClass var : methodClass.getParameters()) {
                this.params += ", " + var.type + " " + var.name;
            }
            this.params += ")";
            this.theBlock = methodClass.getMethodsBlock();

            //Check if this
            if(this.name.contains("init")){
                this.isConstuctor = true;
                this.returnType = "__this";
            }

//            System.out.println("METHOD NAME");
//            System.out.println(this.name);
//
//            System.out.println("METHOD PARAMS");
//            System.out.println(this.params);
//
//
//            System.out.println("METHOD RETURN");
//            System.out.println(this.returnType);
//
//            System.out.println("THE BLOCK");
//            System.out.println(this.theBlock);
//            System.out.println("block12345");

            //Translate the block for the method
            translatedBlock = new TranslatedBlock(this.theBlock, this.isConstuctor, theMethodsClass, allTheClasses);

//            System.out.println("FIELD DECLARATIONS COUNT");
//            System.out.println(this.translatedBlock.fieldDeclarations.size());

        }
    }

    public static String processNewClassExpression(Node newClassExpression) {
        String className = newClassExpression.getNode(2).getString(0);
        String ret = "__" + className + "::__init(new __" + className + "()";
        String arguments = "";
        if (newClassExpression.getNode(3) != null) {
            arguments = processArguments(newClassExpression.getNode(3));
        }
        if (arguments.length() > 0) {
            //ret += " ";
            ret += arguments;
        }
        ret += ")";
        return ret;
    }

    public static String processArguments(Node arguments) {
        String ret = "";
        for (int x = 0; x < arguments.size(); x++) {
            if (x != 1) {
                ret += ", " + processNameNode(arguments.getNode(x));
            } else {
                ret += processNameNode(arguments.getNode(x));
            }

        }
        return ret;

    }
    public static String processCastExpression(Node n) {
        String ret = "__rt::java_cast<" + n.getNode(0).getNode(0).getString(0) + ">(";
        ret += processNameNode(n.getNode(1)) + ")";
        return ret;
    }


    public static boolean isPrint(Node n) {
        //System.out.println("isPrint1233 " + n);

            if (n.getNode(0).getName() == "SelectionExpression") {
                if (n.getNode(0).getNode(0).getName() == "PrimaryIdentifier") {
                    if (n.getNode(0).getNode(0).getString(0).equals("System")) {
                        return true;
                    }
                }
            }
        return false;
    }

    public static boolean isMethodStatic(String callTo, String methodName) {
        ArrayList<CustomClassObject> theClasses = cppImplementationMainMethodClass.allTheClasses;

        // this is the check for static fields
        String nameToCheck = methodName + "_static";
        for (CustomClassObject c : theClasses){
            if (c.getClassName().contains(callTo)){
                // found the class
                for (CustomMethodClass m : c.getMethods()){
                    if (m.getName().equals(nameToCheck)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static String processCallExpression(Node n, int i) {
        String arguments = "";
        if (n.getNode(3) != null) {
            arguments = processArguments(n.getNode(3));
        }
        if (isPrint(n)) {
            // may want to consider differentiation between print and println
            return "std::cout << " + arguments.substring(2,arguments.length()) + " << std::endl";
        }


        String methodName = n.getString(2);
        String ret = "({";
        String callTo;
        if (n.getNode(0).getName().equals("CallExpression")) {
            String tmpCallTo = processCallExpression(n.getNode(0), i + 1);
            String iter = Integer.toString(i);
            String tmp = "tmp" + iter;
            ret += "Object " + tmp + " = " + tmpCallTo + "; ";
            ret += "__rt::checkNotNull(" + tmp + "); ";
            callTo = tmp;
        } if (n.getNode(0).getName().equals("CastExpression")) {
            String castType = n.getNode(0).getNode(0).getNode(0).getString(0);
            String castExpression = processCastExpression(n.getNode(0));
            String tmpCast = castType + " tmpCast = " + castExpression + ";";
            ret += tmpCast;
            callTo = "tmpCast";
        } else {
            callTo = processNameNode(n.getNode(0));
            if (isMethodStatic(callTo, methodName)) {
                String newRet = "__" + callTo + "::" + methodName + "_static" + "(" + arguments + ")";
                return newRet;
            }
            ret += "__rt::checkNotNull(" + callTo + "); ";
        }
        ret += callTo + "->__vptr->" + methodName + "(" + callTo + arguments + ");})";
        return ret;
    }



    public static String processNameNode(Node n) {
        String res = "";
        boolean justString = n.getName() == "PrimaryIdentifier" || n.getName() == "IntegerLiteral";
        if (justString) {
            res = n.getString(0);
            
        } else if (n.getName() == "SelectionExpression"){
            // translation: ->
            String getType = processNameNode(n.getNode(0));
           // for (CustomClassObject c : ){}
            //System.out.println("pleaseprint ");
          ArrayList<CustomClassObject> theClasses = cppImplementationMainMethodClass.allTheClasses;
          // this is the check for static fields
          for (CustomClassObject c : theClasses){
              if (c.getClassName().contains(getType)){
                  //System.out.println("prettypleaseprint");
                  for (CustomVariablesClass v : c.getClassVariables()){
                      if (v.getModifier().contains("static")){
                          //System.out.println("were in busines");
                          //System.out.println("idk1 " + n);
                          String getVar = n.getString(1);
                          String getVT = v.getType();
                         // String getVal = n.getNode(2).getString(0);
                          res = "__"+getType +"::"+getVar;
                          cppImplementationMainMethodClass.allTheStaticVars.add(getVT + " " + res);
                          return res;
//                         res = processNameNode(n.getNode(0)) + "->" + n.getString(1);
                      }
                  }
              }
          }

          //System.out.println("getclasses123 " + theClasses.size());
            res = processNameNode(n.getNode(0)) + "->" + n.getString(1);
            if(res.contains("self")){
                res = res.replace("self","Self");
            }
        } else if (n.getName() == "SubscriptExpression") {
            // translation: (*var)[index]
            res = "(*" + processNameNode(n.getNode(0)) + ")[" + processNameNode(n.getNode(1)) + "]";

        } else if (n.getName() == "ThisExpression") {
            // translation: __this
            res = "__this";
        } else if (n.getName() == "NewClassExpression") {
            // translation: __class:__init
            res = processNewClassExpression(n);
        } else if (n.getName() == "CastExpression") {
            // javalangcast thing
            res = processCastExpression(n);
        } else if (n.getName() == "AdditiveExpression") {
            // translation: no difference
            res = processNameNode(n.getNode(0)) + n.getString(1) + processNameNode(n.getNode(2));
        } else if (n.getName() == "MultiplicativeExpression") {
            // translation: no difference
            res = processNameNode(n.getNode(0)) + n.getString(1) + processNameNode(n.getNode(2));
        } else if (n.getName() == "Expression") {
            // translation: no difference
            res = processNameNode(n.getNode(0)) + " = " + processNameNode(n.getNode(2));

            if(res.contains("self")){
                res = res.replace("self","Self");
            }
        } else if (n.getName() == "CallExpression") {
            // translation: ->__vptr->methodCall with bracket stuff
            res = processCallExpression(n, 0);
        } else if (n.getName() == "StringLiteral") {
            // translation: __rt::literal(<string>)
            res = "__rt::literal(" + n.getString(0) + ")";
        }
        //ExpressionStat
        return res;
    }

    public static class CustomExpressionStatement{
        String expression;
        int position;

        public CustomExpressionStatement(Node sonNode, int pos, ArrayList<CustomClassObject> allTheClasses) {
            this.position = pos;
            this.expression = processNameNode(sonNode.getNode(0));

        }
    }
}

