package edu.nyu.oop;

import edu.nyu.oop.util.CppDataLayout;
import edu.nyu.oop.util.cppNodeActions;
import xtc.tree.GNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import xtc.tree.Node;



public class CppMASTCreator {


    public static CPPAST createNewCPPMAstFrom(List<GNode> javaASTNodes, CPPAST cppast, TraverseASTM visitor) {

        //Holds the ImplementationClassSummary from JavaASTs
        //Get all the data from JavaAst using Visitor
        List<TraverseASTM.ImplementationSummary> javaImplementationClassSummaries = new ArrayList<TraverseASTM.ImplementationSummary>();
        for (GNode javaAST : javaASTNodes) {
            
            javaImplementationClassSummaries.add(visitor.getImplementationSummary(javaAST));
        }

        //Create a new CppDataMLayout
        CppDataLayoutM layoutM = new CppDataLayoutM();

        //ArrayList to hold the implemented dataLayouts
        ArrayList<CppDataLayoutM.cppImplementationClass> listOfCppImpClassesDatalayout = new ArrayList<CppDataLayoutM.cppImplementationClass>();
        //Single data property to hold the main class
        CppDataLayoutM.cppImplementationMainMethodClass mainMethodClassm = null;


        //With the Class Summary Implementation populate the CppAST Tree
        for (TraverseASTM.ImplementationSummary implementationData : javaImplementationClassSummaries) {
            for (CustomClassObject classObj : implementationData.implementationClassObjects) {

                //Handle main class check
                //Get the list of methods that exist in the class
                ArrayList<CustomMethodClass> listOfMethods = classObj.getMethods();
                //Set flag to false
                boolean ismain = false;
                CustomMethodClass mainMethod = null;
                for (CustomMethodClass methodInClass : listOfMethods){
                    if (methodInClass.getName().contains("main")){
                        ismain = true;
                        mainMethod = methodInClass;
                    }
                }
               if (ismain){
                    //Handle for main method
                    mainMethodClassm = new CppDataLayoutM.cppImplementationMainMethodClass(mainMethod,implementationData.implementationClassObjects);
                }
              else{
                    //This is for normal methods
                    listOfCppImpClassesDatalayout.add(new CppDataLayoutM.cppImplementationClass(classObj, implementationData.implementationClassObjects));
                }
            }
        }

        //Add to the cpp AST Tree
        //Get the root node of the CPPAST tree
        GNode rootNodeCppAST = cppast.getRoot();
        //We add new dependencies so first get the dependency node
        Node dependNode = rootNodeCppAST.getNode(0);
        clearDepNode(dependNode);
        addNewDep(dependNode);

        //We add new namespaces for the mainMethod
        Node usingNamespaceNode = rootNodeCppAST.getNode(1);
        addNewNameSpaces(usingNamespaceNode);

        //Add the node for implementation classes
        GNode impClassNode = cppNodeActions.createNewASTNode("ImplementationClassses");
        cppNodeActions.addNodeAsChildToParent((GNode) cppast.getLinkToNamespaceNode(), impClassNode);

        //First add the class level information
        for (CppDataLayoutM.cppImplementationClass theClassLevel: listOfCppImpClassesDatalayout){
            //Get the namespace node
            Node theClassesImplementationNode = impClassNode;
            //Add the class level nodes
            Node ImplementationClassNode = addClassLevelImpNode(theClassesImplementationNode, theClassLevel);

            //Create a new MethodsImplementationNode
            GNode impMethodsNode = cppNodeActions.createNewASTNode("ImplementationMethods");
            cppNodeActions.addNodeAsChildToParent((GNode) ImplementationClassNode, impMethodsNode);

            //First check if there exists the default int
            if (theClassLevel.defaultInIt != null){
                impMethodsNode =  (GNode) addImplementationMethodNode (impMethodsNode, theClassLevel.defaultInIt);
            }

            //Add the method implementations to XTC tree
            for (CppDataLayoutM.cppMethodImplementation method : theClassLevel.cppMethodImplementations) {
                GNode ImplementationMethodsNode = impMethodsNode;
                addImplementationMethodNode(ImplementationMethodsNode,method);
            }

            //XtcPrettyPrintCustom.prettyPrintAst(ImplementationClassNode);
            //XtcPrettyPrintCustom.prettyPrintAst(cppast.getRoot());

        }//End of class for loop

        //Handle the main method
        GNode impMainMethodClassNode = cppNodeActions.createNewASTNode("ImplementationMain");
        cppNodeActions.addNodeAsChildToParent((GNode) cppast.getLinkToNamespaceNode(), impMainMethodClassNode);;
        addDataToMainMethodNode(impMainMethodClassNode, mainMethodClassm, rootNodeCppAST);

        //TODO PRINT OUR AST
        XtcPrettyPrintCustom.prettyPrintAst(cppast.getRoot());
        return cppast;
    }

    private static void addDataToMainMethodNode (Node mainMethodNode, CppDataLayoutM.cppImplementationMainMethodClass mainMethodClass, GNode rootNode){

        // get namespace name
        String nameSpaceName = rootNode.getNode(2).getNode(1).getString(0);
        cppNodeActions.addDataToNode((GNode) mainMethodNode, "using namespace inputs::" + nameSpaceName + ";");
        // add the main methods name
        cppNodeActions.addDataToNode((GNode) mainMethodNode,mainMethodClass.mainMethodName);
        GNode blockImplementationNode = cppNodeActions.createNewASTNode("MainMethodBlockImplementation");
        cppNodeActions.addNodeAsChildToParent((GNode) mainMethodNode, blockImplementationNode);
        addDataToBlockNode(blockImplementationNode, mainMethodClass.transLatedBlockForImpMainMethod,true);
       // cppImplementationMainMethodClass.allTheStaticVars
        GNode mainStaticVars = cppNodeActions.createNewASTNode("MainStaticVars");
        //cppNodeActions.addNodeAsChildToParent(v);
        cppNodeActions.addNodeAsChildToParent((GNode) mainMethodNode, mainStaticVars);

        for (String v : CppDataLayoutM.cppImplementationMainMethodClass.allTheStaticVars){
           // add(mainStaticVars, v,true);
            cppNodeActions.addDataToNode((GNode)mainStaticVars, v);
        }
    }


    //Helper method to add new dependencies
    private static void addNewDep (Node depNode){
        cppNodeActions.addDataToNode((GNode)depNode, "#include \"output.h\"");
        cppNodeActions.addDataToNode((GNode)depNode, "#include <iostream>");
    }

    private static void clearDepNode(Node depNode) {
        cppNodeActions.clearNode((GNode) depNode);
    }
    private static void addNewNameSpaces (Node usingNamespaceNode){
//        cppNodeActions.addDataToNode((GNode) usingNamespaceNode,"inputs::constructors");
        cppNodeActions.addDataToNode((GNode) usingNamespaceNode,"std");
    }

    private static Node addClassLevelImpNode (Node theClassesImplementationNode, CppDataLayoutM.cppImplementationClass classData){
        //Create new impl classNode
        GNode impClassNode = cppNodeActions.createNewASTNode("ImplementationClass");
        //Add the node to the namespace
        cppNodeActions.addNodeAsChildToParent((GNode) theClassesImplementationNode, impClassNode);

        //Add the class level data
        //First - Data layout constructor default initializes all instance fields
        cppNodeActions.addDataToNode((GNode) impClassNode, classData.deafultConstructorImplementation);

        //Second - Internal accessor for the objects class
        cppNodeActions.addDataToNode((GNode) impClassNode, classData.classMethodInit);

        //Third - // The vtable for the class
        cppNodeActions.addDataToNode((GNode) impClassNode, classData.vTableDecl);

        return impClassNode;
    }


    private static void addDataToBlockNode(GNode blockImplementationNode, CppDataLayoutM.TranslatedBlock transBlock, boolean isMain){
        //Get the total length of all the things inside a block
        int totalLengthOfStuff = transBlock.fieldDeclarations.size() + transBlock.expressionStatements.size() + transBlock.forLoops.size() + transBlock.whileLoops.size() + transBlock.blockDecs.size();
        int offset = 0;

        if(isMain){
            offset += 2;
            cppNodeActions.addDataToNode((GNode) blockImplementationNode, "__rt::Array<String> args = new __rt::__Array<String>(argc - 1)");
            GNode newForLoopNode = cppNodeActions.createNewASTNode("ForloopImplementation");
            GNode newForLoopBlockNode = cppNodeActions.createNewASTNode("ForloopBlock");

            cppNodeActions.addDataToNode(newForLoopNode, "for (int32_t i = 1; i < argc; i++)");
            cppNodeActions.addNodeAsChildToParent(newForLoopNode, newForLoopBlockNode);
            cppNodeActions.addDataToNode(newForLoopBlockNode, "(*args)[i] = __rt::literal(argv[i])");
            cppNodeActions.addNodeAsChildToParent((GNode) blockImplementationNode, newForLoopNode);

        }

        if (transBlock.isConstructor) {
            cppNodeActions.addDataToNode((GNode) blockImplementationNode, transBlock.deafultConsturctorCall);
            for (int i = 0; i < transBlock.classLevelInitFields.size(); i++) {
                cppNodeActions.addDataToNode((GNode) blockImplementationNode, transBlock.classLevelInitFields.get(i));
            }
            offset = transBlock.classLevelInitFields.size() + 1;
        }
        for (int i = 0; i < totalLengthOfStuff; i++) {
            cppNodeActions.addDataToNode((GNode) blockImplementationNode, "");
        }
        if (transBlock.returnStatement != "") {
            cppNodeActions.addDataToNode((GNode) blockImplementationNode, transBlock.returnStatement);
        }
        for (int i = 0; i < transBlock.expressionStatements.size(); i++) {
            CppDataLayoutM.CustomExpressionStatement ex = transBlock.expressionStatements.get(i);
            blockImplementationNode.set(offset + ex.position, ex.expression);
        }
        for (int i = 0; i < transBlock.fieldDeclarations.size(); i++) {
            CppDataLayoutM.CustomFieldDeclaration fd = transBlock.fieldDeclarations.get(i);
            blockImplementationNode.set(offset + fd.position, fd.fieldDeclarationLine);
        }

        //For loops
        for (int i = 0; i < transBlock.forLoops.size(); i++){
            CppDataLayoutM.CustomForLoop forlp = transBlock.forLoops.get(i);
            //Create new forLoop block Node
            GNode newForLoopNode = cppNodeActions.createNewASTNode("ForloopImplementation");
            GNode newForLoopBlockNode = cppNodeActions.createNewASTNode("ForloopBlock");
            //Add the for loop decl line
            cppNodeActions.addDataToNode(newForLoopNode, forlp.forLoopDecLine);
            cppNodeActions.addNodeAsChildToParent(newForLoopNode, newForLoopBlockNode);

            //Add the forLoop Node
            addDataToBlockNode(newForLoopBlockNode, forlp.forLoopsTranslatedBlock,false);
            //Add block to correct position
            blockImplementationNode.set(offset + forlp.positon, newForLoopNode);
        }

        //While loops
        for (int i = 0; i < transBlock.whileLoops.size(); i++){
            CppDataLayoutM.CustomWhileLoop whilelp = transBlock.whileLoops.get(i);
            //Create new forLoop block Node
            GNode newWhileLoopNode = cppNodeActions.createNewASTNode("WhileloopImplementation");
            GNode newWhileLoopBlockNode = cppNodeActions.createNewASTNode("WhileloopBlock");
            //Add the for loop decl line
            cppNodeActions.addDataToNode(newWhileLoopNode, whilelp.whileLoopDeclarator);
            cppNodeActions.addNodeAsChildToParent(newWhileLoopNode, newWhileLoopBlockNode);

            //Add the forLoop Node
            addDataToBlockNode(newWhileLoopBlockNode, whilelp.whileLoopTranslatedBlock,false);
            //Add block to correct position
            blockImplementationNode.set(offset + whilelp.position, newWhileLoopNode);
        }

        //Block code
        for (int i = 0; i < transBlock.blockDecs.size(); i++) {
            CppDataLayoutM.CustomBlockDec b = transBlock.blockDecs.get(i);
            // create new block node
            GNode newBlockNode = cppNodeActions.createNewASTNode("BlockDecsImplementation");
            // translate block
            addDataToBlockNode(newBlockNode, b.customBlockDecTranslatedBlock,false);
            // add block to correct position
            blockImplementationNode.set(offset + b.positon, newBlockNode);
        }
    }

    private static Node addImplementationMethodNode (Node ImplementationMethodsNode, CppDataLayoutM.cppMethodImplementation methodData){
        //Create new method implementation Node
        GNode impMethodNode = cppNodeActions.createNewASTNode("MethodImplementation");
        //Add to ImplementationMethods Node
        cppNodeActions.addNodeAsChildToParent((GNode) ImplementationMethodsNode, impMethodNode);

        //First add the method name to the node
        String methodFullNameCall = methodData.name + methodData.params;
        if(methodData.params == null){
            methodFullNameCall = methodData.name;
        }
        cppNodeActions.addDataToNode((GNode) impMethodNode, methodFullNameCall);

        //Next add the return type to the methodNode
        //First add the method name to the node
        cppNodeActions.addDataToNode((GNode) impMethodNode, methodData.returnType);

        //Next add the block Node to the method and process the block
        GNode blockImplementationNode = cppNodeActions.createNewASTNode("BlockImplementation");
        cppNodeActions.addNodeAsChildToParent((GNode) impMethodNode, blockImplementationNode);

        //Handle the block level implementation here
        //Get the translated block for the mothd
        CppDataLayoutM.TranslatedBlock transBlock = methodData.translatedBlock;

        //Add data to the block Node
        addDataToBlockNode(blockImplementationNode,transBlock,false);

        return  ImplementationMethodsNode;
    }
}
