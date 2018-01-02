package edu.nyu.oop;

import edu.nyu.oop.util.*;
import xtc.tree.GNode;
import xtc.tree.Node;
import java.util.*;
import java.util.ArrayList;




//This class generates the C++ Header AST from the classSummary object
public class CppHeaderASTCreator {

    public static ArrayList<CustomMethodClass> allMethods = new ArrayList<CustomMethodClass>();


    private CppHeaderASTCreator() {
    }

    public static HashMap<String, CustomClassObject> structsMap;



    public static CPPAST createNewCPPHeaderAstFrom(List<GNode> javaASTNodes) {

        //Create new CPPAST
        CPPAST cppHeaderAST = new CPPAST("CppASTRoot");

        //Add the C++ starting dep e.g #Pragma, #Include etc.. to the CPP Datastructure
        //Create new node to of the c++ dep
        GNode dep = cppNodeActions.createNewASTNode("Dependencies");
        //Add the node to the root of tree
        cppNodeActions.addNodeAsChildToParent(cppHeaderAST.getRoot(), dep);
        //Add the data to the child node
        cppNodeActions.addDataToNodeWithArray(dep, new ArrayList<String>(Arrays.asList("#pragma once",
                "#include \"java_lang.h\"", "#include <string>")));

        //Add Using Namespace for javalang to CPP AST
        GNode namespace = cppNodeActions.createNewASTNode("UsingNamespace");
        cppNodeActions.addNodeAsChildToParent(cppHeaderAST.getRoot(), namespace);
        cppNodeActions.addDataToNode(namespace, "java::lang");

        //Create all the JavaClassSummary's from the JavaAST
        //Holds the classSummary from JavaASTs
        List<TraverseAST.ClassSummary> javaClassSummaries = new ArrayList<TraverseAST.ClassSummary>();
        //Traverse AST traverses the JAVAAST and converts it into objects saved in ClassSummary
        TraverseAST jASTvisitor = new TraverseAST();
        for (GNode javaAST : javaASTNodes) {
            javaClassSummaries.add(jASTvisitor.getClassSummary(javaAST));
        }



        //With the Class Summary populate the CppAST Tree
        for (TraverseAST.ClassSummary javaData : javaClassSummaries) {
            //Add the nameSpaces to our C++ AST Tree
            GNode currentNamespace = addNameSpacesToCppAST(javaData, cppHeaderAST);
            addStructNodes(javaData, currentNamespace, cppHeaderAST);
        }

        //Check to print out the recent parent mutated of the ASTTree
        //System.out.println(cppHeaderAST.getLinkToNamespaceNode());

        //Create the cppClassObjects
        //createCppClassObject(javaClassSummaries, cppHeaderAST);
        //Fill the CPPClassObjects with their data layouts
        //fillCppClassObjectsWithDataLayout(javaClassSummaries, cppHeaderAST);

//        ArrayList<CppClassObject> clist = cppHeaderAST.getCppClassObjectslist();
//        for (CppClassObject c : clist){
//             System.out.println(c.getCppDataLayout());
//        }

        return cppHeaderAST;
    }

    public static void fillCppClassObjectsWithDataLayout(List<TraverseAST.ClassSummary> jClassSummaryList,CPPAST cppHeaderAST){
        //Get the list of classes in the cppAST
        ArrayList<CppClassObject> arrayListOfCppObj = cppHeaderAST.getCppClassObjectslist();

        //First create the default __Object dataLayout
        CppDataLayout __ObjectDataLayout = CppClassObject.create__ObjectDataLayout();
        //Set the static __ObjectDatalayout var in CppClassObject - so that all instances of CppClassObject has it
        CppClassObject.set__ObjectDatalayout(__ObjectDataLayout);

        //Go over all the Cpp class objects
        for(CppClassObject cobj:  arrayListOfCppObj){
            //System.out.println(cobj.getParentClass());

            CustomClassObject currJavaClass = null;
            //Get the current class we are looking at - get its JavaCustomObject from JavaClassSummary e.g. the child
            for (TraverseAST.ClassSummary javaClassSum : jClassSummaryList) {
                //Go through the javaClassSum and check all methods to see if the "main" method exists - if so set the mainFound flag to true
                //check if the  classes array in the java object is empty
                if (javaClassSum.classes.size() > 0) {
                    for (CustomClassObject customJavaClass : javaClassSum.classes) {
                        if(customJavaClass.className.equalsIgnoreCase(cobj.getOldJavaClassName())){
                            //System.out.println(cobj.getCppClassName() + " " + customJavaClass.className);
                            currJavaClass = customJavaClass;
                        }
                    }
                }
            }
            //Check for inherit
            if(cobj.getParentClass() == null && !cobj.getisMain()){
                //No parent class and not main - Therefore this class extends directly from __Object
                //System.out.println(cobj.getCppClassName() + " " + cobj.getOldJavaClassName());
                //Because this class will be the first in the high structure we pass it __Object Datalayout
                cobj.createCppDataLayoutExtends__Object(CppClassObject.get__ObjectDatalayout(),currJavaClass);
            }
            else if (cobj.getisMain()){
                //This is the main class
                //System.out.println(cobj.getCppClassName() + " " + cobj.getOldJavaClassName());
                cobj.createCppDataLayoutExtends__Object(CppClassObject.get__ObjectDatalayout(),currJavaClass);
            }
            else{
                //this is all other classes which are children
                //System.out.println(cobj.getCppClassName() + " " + cobj.getOldJavaClassName());
                //Get the parent

                CppClassObject parent = cobj.getParentClass();
                //System.out.println(cobj.getCppClassName() + " Parent: " + parent.getCppClassName());
                cobj.createCppDataLayoutExtendsParent(parent.getCppDataLayout(),currJavaClass);







            }
        }
    }


    //Helper method that creates the CPP classes Objects
    public static void createCppClassObject(List<TraverseAST.ClassSummary> jClassSummaryList, CPPAST cppAst) {

        //Empty list of cppClassObject
        List<CppClassObject> listOfCppClassObjects = new ArrayList<CppClassObject>();

        //Create a new cpp classHigh for all instances of cppClassObject
        CppClassObject.classHierarchy classHigh = new CppClassObject.classHierarchy();

        //Get the point in the AST where we want to add that data from the CPPASTObjects
        //This point will remain constant so we always know where to add the class structs for the cp //e.g at the end of name space
        GNode pointToAddClassesToNamespaceGnode = cppAst.getLinkToNamespaceNode();
        //System.out.println(pointToAddClassesToNamespaceGnode);

        //We also need to take into account the "main" class (main method) as it does not show up in the Cpp header
        CppClassObject mainClass = null;

        //Go through the class summary and create the respective CppClassObject
        int index = 0;
        for (TraverseAST.ClassSummary javaClassSum : jClassSummaryList) {
            //Go through the javaClassSum and check all methods to see if the "main" method exists - if so set the mainFound flag to true
            //check if the  classses array in the java object is empty
            if (javaClassSum.classes.size() > 0) {
                for (CustomClassObject customJavaClass : javaClassSum.classes) {
                    //Createa a new cppClassObject for its JavaClassObject counter part
                    CppClassObject newCppClassObject = new CppClassObject("__" + customJavaClass.className, customJavaClass);
                    newCppClassObject.setCppast(cppAst);
                    newCppClassObject.setLinkToNameSpaceGNodeInCppAST(pointToAddClassesToNamespaceGnode);
                    newCppClassObject.setOldJavaClassName(customJavaClass.className);

                    //Now check if the current class created is a main
                    for (CustomMethodClass javaMethod : customJavaClass.methods) {
                        if (javaMethod.name.equalsIgnoreCase("main")) {
                            //Main method found
                            mainClass = newCppClassObject;
                            mainClass.setisMain(true);
                            mainClass.setCppClassName("__MainClass");
                            mainClass.setOldJavaClassName(customJavaClass.className);
                        }
                    }
                    //Check the class High
                    //Add the class name and reference the class object created to the hashmap
                    String className;
                    if (newCppClassObject.getisMain()){
                        className = "__MainClass";
                    }
                    else{
                        className = newCppClassObject.getCppClassName();
                    }
                    classHigh.addClassNameForCppClassObject(className, newCppClassObject);

                    //Map the child and what its parent is
                    if(newCppClassObject.getCppClassName().equalsIgnoreCase("__MainClass")){
                        classHigh.addChildNameForParent(newCppClassObject.getCppClassName(),"__Object");
                    }
                    else{
                        //Not the main class so we get the extension from the java obj
                        String parentClass =  "__" + customJavaClass.parentClass;
                        //Check if the class extends none if so, then its extends Object
                        if(parentClass.equalsIgnoreCase("__None")){
                            parentClass = "__Object";
                        }
                        //Add to the hashMap
                        classHigh.addChildNameForParent(newCppClassObject.getCppClassName(),parentClass);
                    }
                    listOfCppClassObjects.add(newCppClassObject);
                }

                //System.out.println(listOfCppClassObjects);
                for (CppClassObject cppCObject: listOfCppClassObjects){
                    String parentClassName = classHigh.getParentNameForChild(cppCObject.getCppClassName());
                    classHigh.addChildClassToParent(cppCObject.getCppClassName(),parentClassName);
                    cppCObject.setParentClass(classHigh.getCppClassObjectFromName(parentClassName));
                }

                //Sorts the CppClassObject in order
                ArrayList <CppClassObject> newListOfCppClassObjects = new ArrayList<CppClassObject>();
                //Sort the listOfCppClassObjects to ref the high
                for (CppClassObject cppObject : listOfCppClassObjects){
                    //Get the parent class of current Cpp
                    CppClassObject parentClasCpp = cppObject.getParentClass();
                    //Check if the parentClassCpp is null && is not main
                    //If so - then this class extends obj and is on the top of the high structure
                    if(parentClasCpp == null && !cppObject.getisMain()){
                        //System.out.println(cppObject.getCppClassName());
                        //If not null then it does not inherit from Object
                        Queue <CppClassObject> remainingNodes = new LinkedList <CppClassObject>();
                        //Add the parent to the q
                        remainingNodes.add(cppObject);
                        while(remainingNodes.size() > 0){
                            CppClassObject currentCppObject = remainingNodes.poll();
                            newListOfCppClassObjects.add(currentCppObject);
                            //Get the children
                            ArrayList <String> listOfChildren = classHigh.getListOfChildrenForParent(currentCppObject.getCppClassName());
                            if (listOfChildren != null){
                                for (String nameOfChild : listOfChildren){
                                    //Get the child classObj
                                    CppClassObject childClassObj = classHigh.getCppClassObjectFromName(nameOfChild);
                                    remainingNodes.add(childClassObj);
                                }
                            }
                        }
                    }
                }

                //Add the main class to the end of the CppObjectList
                newListOfCppClassObjects.add(mainClass);

                //Set the arrayListOfCppClassObject in the AST and for every CppClassObject
                cppAst.setCppClassObjectslist(newListOfCppClassObjects);
                CppClassObject.setArrayListOfCppClassObjectsInTheCppAST(newListOfCppClassObjects);
                //System.out.println(cppAst.getCppClassObjectslist());
                //System.out.println(CppClassObject.getArrayListOfCppClassObjectsInTheCppAST());

                //Prints the HashMap in the classHigh structure
                //System.out.println(classHigh.getNameOfClassForCppClassObjectHashMap());
                //System.out.println(classHigh.getChildForParentHashMap());
                //System.out.println(classHigh.getParentForChildHashMap());

            }//End of if -> size check
        }//End of outer for loop

        //Add the main class to JavaAST
        cppAst.setMainClass(mainClass);
        //Set the classHigh for all CPPObjects at the end
        CppClassObject.setClassHierarchy(classHigh);
    }


    //Helper method to addJavaPackages as Namespaces to the the CPPAST
    public static GNode addNameSpacesToCppAST(TraverseAST.ClassSummary javaData, CPPAST cppAst) {
        //Get the root of our cppAST
        GNode rootOfCppAST = cppAst.getRoot();
        //Get the javaPackages, which are C++ Namespaces
        ArrayList<String> packages = javaData.packages;
        //For the javaPackages create its NameSpace
        //Keep track of which node to add the name space to
        GNode pointer = rootOfCppAST;
        //Create a new node for the name space
        GNode newNamespaceNode = cppNodeActions.createNewASTNode("Namespace");
        //Add the node to the root
        cppNodeActions.addNodeAsChildToParent(pointer, newNamespaceNode);
        //Add the data node
//        cppNodeActions.addDataToNodeWithArray(newNamespaceNode,packages);
        GNode lastNode = newNamespaceNode;
        String firstPackage = packages.get(0);
        cppNodeActions.addDataToNode(lastNode, firstPackage);
        for (int i = 1; i < packages.size(); i++) {
            String p = packages.get(i);
            GNode newNode = cppNodeActions.createNewASTNode("Namespace");
            cppNodeActions.addDataToNode(newNode, p);
            lastNode.add(newNode);
            lastNode = newNode;
        }
//        cppNodeActions.nestDataToNodeWithArray(newNamespaceNode, packages);
        //Update the recent node pointer
        cppAst.setRecentParentNodeMutated(lastNode);
        cppAst.setLinkToNamespaceNode(lastNode);

        // returns last added namespace to specific current namespace
        return lastNode;
    }


    public static void addStructNodes(TraverseAST.ClassSummary javaData, GNode currentNamespace, CPPAST rootNode) {
        //Get the root of our cppAST

        CppDataLayout dataLayout = new CppDataLayout();

        ArrayList<CppDataLayout.CppStruct> structs = new ArrayList<CppDataLayout.CppStruct>();
        ArrayList<CppDataLayout.VTable> VTables = new ArrayList<CppDataLayout.VTable>();
        structsMap = new HashMap<String, CustomClassObject>();

        //rearrange methods in classes
        for (CustomClassObject c: javaData.classes){
            // used for populating vtables
            structsMap.put(c.getClassName(), c);
        }

        // creates branch for structs
        for (CustomClassObject c: javaData.classes){
            CppDataLayout.CppStruct aStruct = new  CppDataLayout.CppStruct(c, structsMap);

            // added to the tree
            structs.add(aStruct);
        }

//        // creates branch for VTables
//        for (CustomClassObject c: javaData.classes){
//
//            CppDataLayout.VTable vTable = new CppDataLayout.VTable(c, structsMap);
//
//
//            VTables.add(vTable);
//
//        }

//
//        GNode pointer = rootOfCppAST;
//        GNode newStructNode = cppNodeActions.createNewASTNode("Struct");
//        cppNodeActions.addNodeAsChildToParent(pointer,newStructNode);
//        cppNodeActions.addStructToNodeWithArray(newStructNode, structs);
//        cppAst.setRecentParentNodeMutated(newStructNode);

        GNode pointer = currentNamespace;
        GNode newStructNode = cppNodeActions.createNewASTNode("Structs");
        cppNodeActions.addNodeAsChildToParent(pointer, newStructNode);
        cppNodeActions.addStructToNodeWithArray(newStructNode, structs);

        // this may not be necessary, but I'm keeping it rn just in case
        rootNode.setRecentParentNodeMutated(newStructNode);

    }
}











