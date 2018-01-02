package edu.nyu.oop;

import xtc.tree.Node;

import java.util.ArrayList;

public class CustomMethodClass {

    String visibility;
    String modifier;
    String name;
    ArrayList<CustomVariablesClass> parameters;
    String returnType;
    String ownerClass;

    public Node methodsBlock;


    public void setMethodsBlock(Node methodsBlock) {
        this.methodsBlock = methodsBlock;
    }

    public Node getMethodsBlock() {
        return methodsBlock;
    }

    public CustomMethodClass(){
        parameters = new ArrayList<CustomVariablesClass>();
    }

    public ArrayList<CustomVariablesClass> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public String getModifier() {
        return modifier;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setOwnerClass(String ownerClass) {
        this.ownerClass = ownerClass;
    }

    public String getOwnerClass() {
        return ownerClass;
    }

    public void setParameters(ArrayList<CustomVariablesClass> parameters) {
        this.parameters = parameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
