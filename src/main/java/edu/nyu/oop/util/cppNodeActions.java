package edu.nyu.oop.util;

import xtc.tree.GNode;
import xtc.tree.Node;

import java.util.ArrayList;


public abstract class cppNodeActions {

    //Create a new node
    public static GNode createNewASTNode(String type){
        GNode newNode = GNode.create(type);

        return newNode;
    }

    public static void clearNode(GNode node) {
        while (node.size() > 0) {
            node.remove(0);
        }
    }

    public static GNode addNodeAsChildToParent(GNode parent, GNode child){
        parent.add(child);
        return parent;
    }

    public static GNode addDataToNode(GNode node ,Object value){
        //Add as a child
        node.add(value);
        //Add the data node to node
        return node;
    }

    //Add data to node for multiple varaibles

    public static GNode addDataToNodeWithArray(GNode node ,ArrayList<String> listOfData){
        if(listOfData == null && listOfData.size() == 0){
            return null;
        }
        for (String value : listOfData){
            //Add to the parent
            node.add(value);
        }
        return node;
    }

    public static GNode addStructToNodeWithArray(GNode node ,ArrayList<CppDataLayout.CppStruct> listOfData){
        if(listOfData == null && listOfData.size() == 0){
            return null;
        }
        for (CppDataLayout.CppStruct value : listOfData){
            //Add to the parent
            GNode newNode = createNewASTNode("Struct");
            newNode.add(value.name);
            newNode.add(value.custTypedef);
            newNode.add(value.VTPointer);
            newNode.add(value.classDeclarator);
            newNode.add(value.VTableDeclarator);
            //newNode.add(value.structVTable);

            addFieldsToNodeWithArray(newNode, value.variables);

            addMethodsToNodeWithArray(newNode, value.methods);

            node.add(newNode);

            addVTableToNodeWithArray(node, value.structVTable);
        }
        return node;
    }


    public static GNode addVTableToNodeWithArray(GNode node , CppDataLayout.VTable listOfData){




        if(listOfData == null){
            return null;
        }
       // for (CppDataLayout.VTable value : listOfData){
            //Add to the parent
            GNode newNode = createNewASTNode("VTable");


                newNode.add(listOfData.name);


            addVTMethodsToNodeWithArray(newNode, listOfData.VTMethods);
            addVTInstantiatorsToNodeWithArray(newNode, listOfData.VTInstantiators);
            newNode.add(listOfData.is_a);
            //addInstantiatorMethodsToNodeWithArray(newNode, value.VTInstantiators.getVTInstantiatorMethods());

//            addFieldsToNodeWithArray(newNode, value.variables);

            node.add(newNode);
        //}
        return node;

    }


        public static void addMethodsToNodeWithArray(GNode node, ArrayList<CppDataLayout.CppMethod> methods) {

        GNode newNode = createNewASTNode("Methods");
        for (CppDataLayout.CppMethod method : methods) {
            GNode methodNode = createNewASTNode("Method");

            methodNode.add(method.name);
            methodNode.add(method.returnType);
            methodNode.add(method.fullLine);

            methodNode.add(createNewASTNode("Visibility").add(method.visibility));
            methodNode.add(createModifiersNode(method.modifier));
            methodNode.add(createParametersNode(method.parameters));
            newNode.add(methodNode);
        }
        node.add(newNode);

    }






    public static void addInstantiatorMethodsToNodeWithArray(GNode node, ArrayList<CppDataLayout.VTInstantiatorMethod> vtiMethods) {

        GNode newNode = createNewASTNode("InstantiatorMethods");
        for (CppDataLayout.VTInstantiatorMethod vtiMethod : vtiMethods) {
            GNode methodNode = createNewASTNode("InstantiatorMethod");

            methodNode.add(vtiMethod.getFullLine());

            newNode.add(methodNode);
        }

        node.add(newNode);

    }

    public static void addVTMethodsToNodeWithArray(GNode node, ArrayList<CppDataLayout.VTMethod> methods) {

        GNode newNode = createNewASTNode("VTMethods");
        for (CppDataLayout.VTMethod method : methods) {
            GNode methodNode = createNewASTNode("VTMethod");

            methodNode.add(method.fullLine);

            newNode.add(methodNode);
        }
        node.add(newNode);

    }



    public static void addVTInstantiatorsToNodeWithArray(GNode node, ArrayList<CppDataLayout.VTInstantiator> instantiators) {

        GNode newNode = createNewASTNode("VTInstantiators");
        for (CppDataLayout.VTInstantiator instantiator : instantiators) {
            GNode instantiatorNode = createNewASTNode("VTInstantiator");

            instantiatorNode.add(instantiator.getDeclarationName());
            instantiatorNode.add(instantiator.getIsA());

            GNode instantiatorMethodsNode = createNewASTNode("InstantiatorMethods");

            for (CppDataLayout.VTInstantiatorMethod vtiM : instantiator.getVTInstantiatorMethods()){


                instantiatorMethodsNode.add(vtiM.getFullLine());


            }
            instantiatorNode.add(instantiatorMethodsNode);
            instantiatorNode.add(instantiator.randoCurls);
            newNode.add(instantiatorNode);
        }

        node.add(newNode);

    }





    public static GNode createModifiersNode(String modifier) {
        GNode newNode = createNewASTNode("Modifiers");
        if (modifier == null) {
            return newNode;
        }
        String[] mods = modifier.split(" ");
        for (String mod: mods) {
            newNode.add(mod);
        }
        return newNode;
    }

    public static GNode createParametersNode(ArrayList<CppDataLayout.CppParameter> params) {
        GNode newNode = createNewASTNode("Parameters");
        for (CppDataLayout.CppParameter p: params) {

           GNode parameterNode = createNewASTNode("Parameter");
           parameterNode.add(p.type);
           parameterNode.add(p.name);
           newNode.add(parameterNode);

        }

        return newNode;
    }

    public static void addFieldsToNodeWithArray(GNode node, ArrayList<CppDataLayout.CppVar> vars) {

        GNode newNode = createNewASTNode("Fields");
        for (CppDataLayout.CppVar v : vars) {
            GNode fieldNode = createNewASTNode("Field");
            fieldNode.add(v.type);
            fieldNode.add(v.name);
            fieldNode.add(createModifiersNode(v.modifier));

            newNode.add(fieldNode);
        }
        node.add(newNode);
    }



    //Implementations stuff


}




