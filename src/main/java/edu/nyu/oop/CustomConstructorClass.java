package edu.nyu.oop;

import java.util.ArrayList;

public class CustomConstructorClass {
    private String visibility;
    private ArrayList<CustomVariablesClass> parameters;
    private String name;

    public CustomConstructorClass(String n) {
        this.name = n;
        this.parameters = new ArrayList<CustomVariablesClass>();
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParameters(ArrayList<CustomVariablesClass> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(CustomVariablesClass var) {
        this.parameters.add(var);



    }



    public ArrayList<CustomVariablesClass> getParameters() {
        return parameters;
    }
}
