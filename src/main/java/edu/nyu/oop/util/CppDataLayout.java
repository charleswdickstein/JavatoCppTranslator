package edu.nyu.oop.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import edu.nyu.oop.*;
import xtc.tree.GNode;
import java.util.Map;

import java.util.HashMap;
import java.util.Stack;

public class CppDataLayout {

    public static ArrayList<CppMethod> allMethods;

    public static ArrayList<CppStruct> structs;
    public static ArrayList<VTable> VTables;
    public static final int DEBUGGING = 1;

    public CppDataLayout(){
        //Use this default constructor to create the deafult __Object data layout
        structs = new ArrayList<CppStruct>();
        VTables = new ArrayList<VTable>();
    }

    public CppDataLayout(CppDataLayout parentDataLayout, CustomClassObject childJavaClassObject){
        //Use this constructor to take in a parentDataLayout and the javaCustomClass
        //Check javaCustomClassObj against the parentDataLayout for overidden methods
        structs = new ArrayList<CppStruct>();
        VTables = new ArrayList<VTable>();
        allMethods = new ArrayList<CppMethod>();
        ArrayList<VTMethod> parentMethods;
        parentDataLayout.getStructs();
    }

    @Override
    public String toString() {
        return super.toString();
    }
    // Helper method to translate java types to C++ types
    public static class typeTranslate {
        public String translateType(String javaType) {

            String cType = "";
            switch (javaType) {

                case "int":
                    cType = "int32_t";
                    break;

                case "VoidType":
                    cType = "void";
                    break;

                case "boolean":
                    cType = "bool";
                    break;

                default:
                    cType = javaType;
                    break;
            }
            return cType;
        }
    }

    // used for checking overriden methods only
    public static class CppFileObject{
        JavaFileObject javaFile;
        public CppFileObject(JavaFileObject javaFile){
            this.javaFile = javaFile;
        }
    }

    public static class CppFile{
        ArrayList<CppStruct> structs;
        public CppFile(CppStruct cppStruct){
            structs.add(cppStruct);
        }
    }

    public static class CppStruct {

        ArrayList<CppVar> variables;
        ArrayList<CppMethod> methods;
        VTable structVTable;

        String name;
        String classDeclarator;
        String VTableDeclarator;
        String parentName;
        String custTypedef;
        String VTPointer;

        ArrayList<CppConstructor> constructors;

        public CppStruct(CustomClassObject c, HashMap<String, CustomClassObject> classMap) {
            this.variables = new ArrayList<CppVar>();
            this.methods = new ArrayList<CppMethod>();
            this.parentName = c.getParentClass();
            // __rt::Ptr<__A_VT>
            this.VTPointer = "__" + c.getClassName() + "_VT* __vptr";

            this.name = "__" + c.getClassName();
            this.classDeclarator = "static Class __class()";
            this.VTableDeclarator = "static __" + c.getClassName() + "_" +"VT" + " __vtable";
            this.custTypedef = "__rt::Ptr<__" + c.getClassName() +"> " + c.getClassName();

            //__rt::Ptr<__A> A

            this.constructors = new ArrayList<CppConstructor>();

            // instantiates all data
            for (CustomMethodClass javaMethod : c.getMethods()) {
                CppMethod cMethod = new CppMethod(javaMethod, c);
                methods.add(cMethod);
            }
            boolean defaultConstructorAdded = false;
            if (c.getConstructors().size() > 0) {
                for (CustomConstructorClass javaConstructor : c.getConstructors()) {
                    CppMethod cppConstructor = new CppMethod(javaConstructor);
                    this.methods.add(cppConstructor);
                    if (javaConstructor.getParameters().size() == 0) {
                        defaultConstructorAdded = true;
                    }
                }
            }
            if (!defaultConstructorAdded) {
                CppMethod defaultConstructor = new CppMethod("__init", c.getClassName());
                CppParameter cppparm = new CppParameter(c.getClassName(), c.getClassName() + " __this");
                //defaultConstructor.parameters.add("");
                defaultConstructor.parameters.add(cppparm);
                this.methods.add(defaultConstructor);
            }

            for (CustomVariablesClass javaVar : c.getClassVariables()) {

                CppVar cVar = new CppVar(javaVar);
                this.variables.add(cVar);
            }
             this.structVTable = new VTable(c,classMap);
             structs.add(this);
        }

        public CppStruct(){}

        public String getName() {
            return name;
        }

        public String getParentName() {
            return parentName;
        }

        public ArrayList<CppMethod> getMethods() {
            return methods;
        }
    }

    public static class CppConstructor {
        ArrayList<CustomVariablesClass> parameters;
        String name;
        String visibility;

        public CppConstructor(CustomConstructorClass c) {
            this.name = "__" + c.getName();
            this.parameters = c.getParameters();
            this.visibility = c.getVisibility();
        }
    }

    public static class CppMethod {

        String name;

        String modifier;
        String visibility;
        String returnType;
        ArrayList<CppParameter> parameters;
        String methodOwner;
        String fullLine;

        public CppMethod(CustomMethodClass m, CustomClassObject c) {
            parameters = new ArrayList<CppParameter>();

            this.name =  m.getName();
            this.modifier = m.getModifier();
            this.visibility = m.getVisibility();
            this.methodOwner = m.getOwnerClass();
            typeTranslate translateType = new typeTranslate();
            this.returnType = translateType.translateType(m.getReturnType());
            // translates and add java method params to cpp method params
            CppParameter classParam = new CppParameter(c.getClassName(), c.getClassName());
            CppParameter objectParam = new CppParameter("Object", "Object");
            if (m.getName().equals("equals")){
                this.parameters.add(classParam);
                this.parameters.add(objectParam);
            }
            else if (!m.getName().contains("static")){
                this.parameters.add(classParam);

            }

            for (CustomVariablesClass javaParam : m.getParameters()) {
                CppParameter cParam = new CppParameter(javaParam);
                this.parameters.add(cParam);

            }
        }

        public CppMethod(CustomConstructorClass c) {
            parameters = new ArrayList<CppParameter>();

            this.name = "__init";
            this.returnType = c.getName();
            this.parameters.add(new CppParameter("__init", c.getName()));

            for (CustomVariablesClass javaParam :c.getParameters()) {
                CppParameter cParam = new CppParameter(javaParam);
                this.parameters.add(cParam);
            }

        }

        public CppMethod(String name, String returnType, ArrayList<CppParameter> params) {
            parameters = new ArrayList<CppParameter>();
            //for (params)
            params = parameters;

            this.name = name;
            this.returnType = returnType;
        }

        // used for creating default constructor object
        public CppMethod(String name, String returnType) {
            parameters = new ArrayList<CppParameter>();

            this.name = name;
            this.returnType = returnType;
        }

    }

    public static class Field {
        public String name;
        public String type;
        public ArrayList<String> modifiers;
        public Field() {
        }
    }

    public static class CppParameter {
        String name;
        String type;

        public CppParameter(CustomVariablesClass v) {
            typeTranslate translateType = new typeTranslate();
            this.name = v.getName();
            this.type = translateType.translateType(v.getType());


        }

        public CppParameter(String name, String type){
            this.name = name;
            this.type = type;
        }
    }

    public static class CppVar {
        String name;
        String type;
        String modifier;

        public CppVar(CustomVariablesClass v) {
            typeTranslate translateType = new typeTranslate();
            this.name = v.getName();
            this.type = translateType.translateType(v.getType());

            if (v.getModifier() != null && v.getModifier().contains("static")) {
                System.out.println("testmod11 " + v.getModifier());
                this.modifier = v.getModifier();
            }
            else{
                this.modifier = "";
            }
        }
    }


    public static class InheritedMethodChecker{
        String methodName;
        public InheritedMethodChecker(String m, boolean isOverride){
        }
    }

    public static ArrayList<CustomMethodClass> createDefaultMethods() {
        ArrayList<CustomMethodClass> methods = new ArrayList<CustomMethodClass>();

        // hashCode
        CustomMethodClass hashCode = new CustomMethodClass();
        hashCode.setName("hashCode");
        hashCode.setOwnerClass("None");
        hashCode.setReturnType("int");

        // getClass
        CustomMethodClass getClass = new CustomMethodClass();
        getClass.setName("getClass");
        getClass.setReturnType("Class");
        getClass.setOwnerClass("None");

        // toString
        CustomMethodClass toString = new CustomMethodClass();
        toString.setName("toString");
        toString.setReturnType("String");
        toString.setOwnerClass("None");

        // equals
        CustomMethodClass equals = new CustomMethodClass();
        equals.setName("equals");
        equals.setReturnType("boolean");
        equals.setOwnerClass("None");

        CustomMethodClass delete = new CustomMethodClass();
        delete.setName("__delete");
        delete.setReturnType("void");
        delete.setOwnerClass("None");

        methods.add(delete);
        methods.add(hashCode);
        methods.add(equals);
        methods.add(getClass);
        methods.add(toString);
       ;

        return methods;
    }

    public static ArrayList<String> getMethodNames(ArrayList<CustomMethodClass> methods) {
        ArrayList<String> methodNames = new ArrayList<String>();
        for (CustomMethodClass m : methods) {
            methodNames.add(m.getName());
        }
        return methodNames;
    }

    public static class VTInstantiator {
        String randoCurls;
        String isA;
        String methodName;
        String declarationName;
        ArrayList<CustomMethodClass> inheritedMethods;
        ArrayList<CustomVariablesClass> inheritedFields;
        //ArrayList<CustomMethodClass> inheritedMethods;
        ArrayList<VTInstantiatorMethod> VTInstantiatorMethods;
        ArrayList<VTMethod> VTMethods;


        boolean shouldOverride = false;

        public String getIsA() {
            return isA;
        }
        // non overridden methods
        public VTInstantiator(CustomClassObject currStruct, HashMap<String, CustomClassObject> classMap) {
            this.VTInstantiatorMethods = new ArrayList<VTInstantiatorMethod>();
            this.VTMethods = new ArrayList<VTMethod>();
            this.inheritedMethods = new ArrayList<CustomMethodClass>();
            this.inheritedFields  = new ArrayList<CustomVariablesClass>();

            this.isA = ": __is_a(__" + currStruct.getClassName() + "::__class()),";
            this.declarationName = "__" + currStruct.getClassName() + "_VT()";
            this.randoCurls = " {}";

            CustomClassObject tempStruct = currStruct;

            ArrayList<CustomMethodClass> defaultMethods = createDefaultMethods();
            ArrayList<String> names = getMethodNames(inheritedMethods);
            for (CustomMethodClass m : defaultMethods) {
                String name = m.getName();

                //if (!(names.contains(name))) {
                    inheritedMethods.add(m);
               // }
            }
            


            // gets inherited methods of a class
            while (tempStruct.getParentClass() != "None") {






                String getParentStructName = tempStruct.getParentClass();
                // get all methods of parent class
                CustomClassObject parentClass = classMap.get(getParentStructName);
                for (CustomMethodClass method : parentClass.getMethods()) {
                    ArrayList<String> inheritedMethodNames = getMethodNames(inheritedMethods);
                    // checking if method is overwritten
                    //inheritedMethods.add();
                    inheritedMethods.add(method);
                }
                tempStruct = parentClass;
            }

            // adding default methods to inheritedMethods to check for overriding


            ArrayList<VTInstantiatorMethod> overwrittenMethods = new ArrayList<VTInstantiatorMethod>();
            // check if current class overrides any of these methods
            int index = 0;
            ArrayList<String> inheritedMethodNames = getMethodNames(inheritedMethods);
            int k = 0;
            boolean isLastMethod = false;
            boolean isDeleteMethod = false;
            VTInstantiatorMethod vtiMethod;
            VTMethod vtMethod;

            //System.out.println("TESTSTRUCT1 " + currStruct.getClassName());

            for (CustomMethodClass inheritedM : inheritedMethods){
                //Check static
                if(inheritedM.getName().contains("static")){
                    continue;
                }

                isDeleteMethod = false;
                vtiMethod = new VTInstantiatorMethod(inheritedM, currStruct.getClassName(), isLastMethod, false);

                if (inheritedM.getName().equals("__delete")){
                    System.out.println("foundDELETE");
                    isDeleteMethod = true;

                }


                vtMethod = new VTMethod(inheritedM, currStruct.getClassName(), isLastMethod, false);

                VTInstantiatorMethods.add(vtiMethod);
                VTMethods.add(vtMethod);
            }



            for (CustomMethodClass m1 : currStruct.getMethods()) {
                if (k == currStruct.getMethods().size() -1 ){
                    isLastMethod = true;
                }
                m1.setOwnerClass(currStruct.getClassName());

                // if m isnt in overwritten methods, the method is new and should be adde3d to the struct
                // if method is old, check the current list of methods to isnert in the right position

                // first add all inherited method names
                //VTInstantiatorMethods.addAll(inheritedMethods);;



                // IF THE METHOD WAS NOT INHERITED ADD IT TO THE LIST OF METHODS
                if (!(inheritedMethodNames.contains(m1.getName()))) {
                    //Static
                    if(m1.getName().contains("static")){
                        continue;
                    }

                     vtiMethod = new VTInstantiatorMethod(m1, currStruct.getClassName(), isLastMethod, true);
//                    overwrittenMethods.add(vtiMethod);


                    vtMethod = new VTMethod(m1, currStruct.getClassName(), isLastMethod, true);

                    //System.out.println("testcomp123 "+ m1.getName());
                    VTInstantiatorMethods.add(vtiMethod);
                    VTMethods.add(vtMethod);


                }else{
                    // else the method is actually being
                    // overriden and you have to find the position of the original method
                    // to keep postitioning of the methods


                    // get the position of the method
                    for (int i= 0; i < VTInstantiatorMethods.size(); i++){


                        if (VTInstantiatorMethods.get(i).saveMethodName.equals(m1.getName())){
                            //Check static
                            if(m1.getName().contains("static")){
                                continue;
                            }
                            vtiMethod = new VTInstantiatorMethod(m1, currStruct.getClassName(), isLastMethod, true);
                            //System.out.println("testcompagain1 " + m1.getName() + " index: " + i);

                            VTInstantiatorMethods.set(i, vtiMethod);

                        }


                    }

                    //System.out.println("VTMETHODSSIZE123 " + VTMethods.size());
                    for (int i= 0; i < VTMethods.size(); i++){
                            //System.out.println("themeth " + VTMethods.get(i).methodName);

                        if (VTMethods.get(i).methodName.equals(m1.getName())){

                            //Check static
                            if(m1.getModifier().contains("static")){
                                continue;
                            }

                            vtMethod = new VTMethod(m1, currStruct.getClassName(), isLastMethod, true);
                            //System.out.println("testcompagain1 " + m1.getName() + " index: " + i);

                           VTMethods.set(i, vtMethod);

                        }


                    }


                }







                System.out.println(m1.getName());












            }
            System.out.println("test all vtmethods1234 " + currStruct.getClassName());

            int duplicate = 0;
            int getIndex = 0;


            for (int x = 0; x < VTMethods.size(); x++){
                //             getIndex++;
                duplicate = 0;

                String originalName = VTMethods.get(x).methodName;
                System.out.println("themeths " + originalName);
//\
                for (int j = 0; j < VTMethods.size(); j++) {


                    String currentName = VTMethods.get(j).methodName;

                    if (originalName.equals(currentName)){

                        duplicate++;
                    }
//                    if (VTInstantiatorMethods.get(x).saveMethodName.equals(VTInstantiatorMethods.get(j).saveMethodName)){
//                        duplicate++;
//                    }
                    if (duplicate >= 2){
                        System.out.println("duplicate122 " + originalName);
                        VTMethods.set(x, VTMethods.get(j));

                        VTMethods.remove(j);
//
//
                    }
//
//                }




                }

            }


            for (int x = 0; x < VTInstantiatorMethods.size(); x++){
   //             getIndex++;
                duplicate = 0;

                String originalName = VTInstantiatorMethods.get(x).saveMethodName;
//\
               for (int j = 0; j < VTInstantiatorMethods.size(); j++) {


                   String currentName = VTInstantiatorMethods.get(j).saveMethodName;

                  if (originalName.equals(currentName)){

                      duplicate++;
                  }
//                    if (VTInstantiatorMethods.get(x).saveMethodName.equals(VTInstantiatorMethods.get(j).saveMethodName)){
//                        duplicate++;
//                    }
                  if (duplicate >= 2){
                      System.out.println("duplicate12 " + originalName);
                      VTInstantiatorMethods.set(x, VTInstantiatorMethods.get(j));

                        VTInstantiatorMethods.remove(j);
//
//
                   }
//
//                }




               }

            }

//            for (CustomMethodClass m : inheritedMethods){
//                 isLastMethod = false;
//                if (index == currStruct.getMethods().size() -1 ){
//                    isLastMethod = true;
//                }
//
//                boolean isOverriden = false;
//                // if contains override
//                if (!currStruct.getMethodNames().contains(m.getName())){
//                    isOverriden = false;
//                    String className = currStruct.getClassName();
//                    // create overridden instantiator
//                    VTInstantiatorMethod vtiMethod = new VTInstantiatorMethod(m, className, isLastMethod, isOverriden);
//                    VTInstantiatorMethods.add(vtiMethod);
//                }
//                else {
//                    String className = currStruct.getClassName();
//                    isOverriden = true;
//                    VTInstantiatorMethod vtiMethod = new VTInstantiatorMethod(m, className, isLastMethod, isOverriden);
//                    overwrittenMethods.add(vtiMethod);
//                }
//                index++;
//            }






            //VTInstantiatorMethods.addAll(overwrittenMethods);
        }


        public String getDeclarationName() {
            return declarationName;
        }

        public ArrayList<VTMethod> getVTMethods() {
            return VTMethods;
        }

        public ArrayList<VTInstantiatorMethod> getVTInstantiatorMethods() {
            return VTInstantiatorMethods;
        }
    }

    public static String getArgs(ArrayList<CustomVariablesClass> vars) {
        String s = "";
        for (CustomVariablesClass v : vars) {
            s += ", " + v.getType();
        }
        return s;
    }

        public static class VTInstantiatorMethod{
            String objectReference;
            String returnTypeClassName;
            String fullLine;
            String saveClassName;
            String saveMethodName;
            boolean shouldOverride;
            public VTInstantiatorMethod(CustomClassObject current){}

            // for overriden methods
            public VTInstantiatorMethod(CustomMethodClass method, String className, boolean isLastMethod, boolean isOverridden){
                typeTranslate translateType = new typeTranslate();
                this.saveClassName = className;
                this.saveMethodName = method.getName();
                String returnT = translateType.translateType(method.getReturnType());
                fullLine = "";
                shouldOverride = false;
                if (method.getName().equals("__delete")){
                   // __delete(__rt::__delete<__A>),
                   // this.objectReference = method.getName() + "(&__" + className + "::" + method.getName() + ")";
                    this.fullLine = "__delete(__rt::__delete<__" + className + ">)";
                }
                else if (isOverridden) {;
                    this.objectReference = method.getName() + "(&__" + className + "::" + method.getName() + ")";
                    this.fullLine = this.objectReference;
                }
                else {
                    this.objectReference = method.getName();
                    // get parent class
                    ArrayList<CustomVariablesClass> vars = method.getParameters();
                    String args = getArgs(vars);
                    if (method.getOwnerClass() != "None") {
                        if (!method.getName().equals("equals")) {
                            this.returnTypeClassName = "((" + returnT + " (*)(" + className + args + "))" + " &__" + method.getOwnerClass() + "::" + method.getName() + ")";
                        }
                        else{
                            this.returnTypeClassName = "((" + returnT + " (*)(" + className + ", Object" +"))" + " &__" + method.getOwnerClass() + "::" + method.getName() + ")";

                        }
                    }
                    else {
                        if (!method.getName().equals("equals")) {
                            this.returnTypeClassName = "((" + returnT + " (*)(" + className + args + "))" + " &__Object::" + method.getName() + ")";
                        }
                        else{
                            this.returnTypeClassName = "((" + returnT + " (*)(" + className + ", Object" +"))" + " &__Object::" + method.getName() + ")";
                        }
                    }
                    this.fullLine = this.objectReference + this.returnTypeClassName;
                }
                if (!isLastMethod) {
                    this.returnTypeClassName += ",";
                }
            }

            // for default methods
            public VTInstantiatorMethod (String rTCN) {
                this.returnTypeClassName = rTCN;
                fullLine = this.returnTypeClassName;
            }

            // TODO: add constructor for custom non overidden methods

            public String getObjectReference() {
                return objectReference;
            }

            public String getFullLine() {
                return fullLine;
            }

            public String getReturnTypeClassName() {
                return returnTypeClassName;
            }
        }

        public static class VTable {
            String is_a;
            ArrayList<VTMethod> VTMethods;
            ArrayList<VTInstantiator> VTInstantiators;
            String name;

            public VTable(CustomClassObject currClass, HashMap<String, CustomClassObject> classMap) {

                this.name = currClass.getClassName();

                this.VTInstantiators = new ArrayList<VTInstantiator>();
                this.VTMethods = new ArrayList<VTMethod>();
                this.is_a = "Class __is_a";
                ArrayList<CustomMethodClass> VTInheritedmethods = new ArrayList<CustomMethodClass>();

                CustomClassObject tempStruct = currClass;

                ArrayList<CustomMethodClass> currentArrayList;
                Stack<ArrayList> arrayListStack= new Stack<ArrayList>();

                // to check inheritence with default methods
                ArrayList<String> names = new ArrayList<String>();
                // Get all methods that the current class inherits
//                while (tempStruct.getParentClass() != "None") {
//                    currentArrayList = new ArrayList<CustomMethodClass>();
//                    String getParentStructName = tempStruct.getParentClass();
//                    //System.out.println(getParentStructName);
//                    // get all methods of parent class
//                    CustomClassObject parentClass = classMap.get(getParentStructName);
//
//                    for (CustomMethodClass method : parentClass.getMethods()) {
//                        //System.out.println(method.getName());
//                        ArrayList<String> inheritedMethodNames = getMethodNames(VTInheritedmethods);
//                        // checking if method is overwritten
//                        // if method is static continue
//                        if (method.getModifier() != null &&method.getModifier().contains("static")) {
//                            continue;
//                        };
//                        if ((!inheritedMethodNames.contains(method.getName()))) {
//                            method.setOwnerClass(parentClass.getClassName());
//                            currentArrayList.add(method);
//                            names.add(method.getName());
//                        }
//                    }
//                    arrayListStack.push(currentArrayList);
//                    tempStruct = parentClass;
//                }

                // adding default methods to inheritedMethods to check for overriding
//                ArrayList<CustomMethodClass> defaultMethods = createDefaultMethods();
//
//                for (CustomMethodClass m : defaultMethods) {
//                    String name = m.getName();
//                    // if method is static dont add
//                    if (m.getModifier() != null && m.getModifier().contains("static")) {
//                        continue;
//                    }
//
//                    if (!(names.contains(name))) {
//                        VTInheritedmethods.add(m);
//                    }
//                }


                // unpack the stack
//                while (!arrayListStack.empty()) {
//                    VTInheritedmethods.addAll(arrayListStack.pop());
//                }

//                ArrayList<VTMethod> overwrittenVTMethods = new ArrayList<VTMethod>();
//                // check if current class overrides any of these methods
//                int index = 0;
//                for (CustomMethodClass m : VTInheritedmethods){
//                    boolean isLastMethod = false;
//                    if (index == currClass.getMethods().size() -1 ){
//                        isLastMethod = true;
//                    }
//                    boolean isOverriden = false;
//                    // if contains override
//                    if (currClass.getMethodNames().contains(m.getName())){
//                        isOverriden = true;
//                        m.setOwnerClass(currClass.getClassName());
//                        String className = currClass.getClassName();
//                        // create overridden instantiator
//                        VTMethod vtiMethod = new VTMethod(m, className, isLastMethod, isOverriden);
//                        overwrittenVTMethods.add(vtiMethod);
//                    }
//                    else {
//                        String className = currClass.getClassName();
//                        VTMethod vtiMethod = new VTMethod(m, className, isLastMethod, isOverriden);
//                        VTMethods.add(vtiMethod);
//                    }
//                    index++;
//
//                }
//                VTMethods.addAll(overwrittenVTMethods);
//                ArrayList<String> inheritedMethodNames = getMethodNames(VTInheritedmethods);
//                int k = 0;
//                boolean isLastMethod = false;
//                for (CustomMethodClass m : currClass.getMethods()) {
//                    if (k == currClass.getMethods().size() -1 ){
//                        isLastMethod = true;
//                    }
//                    // if method is static continue
//                    if (m.getModifier() != null && m.getModifier().contains("static")) {
//                        continue;
//                    }
//                    m.setOwnerClass(currClass.getClassName());
//                    // if m isnt in overwritten methods, add it to vtinstantiator
//                    if (!(inheritedMethodNames.contains(m.getName()))) {
//                        VTMethod vtiMethod = new VTMethod(m, currClass.getClassName(), isLastMethod, false);
//                        VTMethods.add(vtiMethod);
//                    }
//                }

                VTInstantiator vtInstantiator = new VTInstantiator(currClass, classMap);
                //System.out.println("thisthingy3 " + vtInstantiator.getVTMethods().size());
                VTInstantiators.add(vtInstantiator);

                for (VTMethod v : vtInstantiator.getVTMethods()){
                    VTMethods.add(v);
                }
                //System.out.println("again12 " + VTMethods.size());
                VTables.add(this);
            }
        }

        public static class VTMethod {

            String returnType;
            String pointer;
            String className;
            String returnTypeAndClassName;
            String entireMethod;
            String methodName;
            ArrayList<String> params;
            String objectReference;
            String returnTypeClassName;
            String fullLine;

            // for overriden methods
                public VTMethod(CustomMethodClass method, String className, boolean isLastMethod, boolean isOverridden){
                    typeTranslate translateType = new typeTranslate();
                    this.methodName = method.getName();
                    String returnT = translateType.translateType(method.getReturnType());
                    String parameters = className + ", ";
                    for (CustomVariablesClass javaParam : method.getParameters()) {
                        String parameter = javaParam.getType();
                        parameters += parameter + ", ";
                    }
                    // remove comma and space at the end


                    System.out.println("methname " + method.getName());
                    parameters = parameters.substring(0, parameters.length() - 2);
                    if (method.getName().equals("__delete")){
                        // void (*__delete)(__A*);
                        System.out.println("in delete");
                        this.fullLine = returnT + " (*" + method.getName() + ") (__" + parameters + "*)";

                    }
                    else if (!method.getName().equals("equals") && !method.getName().equals("__delete")) {
                        this.fullLine = returnT + " (*" + method.getName() + ") (" + parameters + ")";
                    }

                    else{
                        this.fullLine = returnT + " (*" + method.getName() + ") (" + className + "," + " Object"+ ")";
                    }
                }

                // default methods
            public VTMethod(String rt, String pt, String className) {
                params = new ArrayList<String>();
                this.pointer = "(*"+pt +")";
                this.returnType = rt;
                this.className = "("+ className +")";
                params.add(this.className);
                if (className.equals("hashCode")) {
                    params.add("Object");
                }
                    methodName = "";
                    methodName += (this.returnType + " " + this.pointer + "(" + this.className+")" +";");
                if (DEBUGGING == 1) {
                    //System.out.println("________________");
                    // System.out.println("return type " + this.returnType);
                    //System.out.println(methodName);
                    //System.out.println("________________");
                }
            }
            public String getClassName() {
                return className;
            }
        }
    public static ArrayList<CppStruct> getStructs() {
        return structs;
    }
}