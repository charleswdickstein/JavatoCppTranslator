package edu.nyu.oop;

import java.util.*;

import edu.nyu.oop.util.CppDataLayout;
import xtc.tree.GNode;
import java.util.HashMap;

public class CppClassObject {

    private String oldJavaClassName;

    //Refer to the parent of this class
    private CppClassObject parentClass;

    //Name of the class
    private String cppClassName;

    //ArrayListWhichHoldsAll the CppClassObjects that exists e.g Class A, Class B etc..
    private static ArrayList <CppClassObject> arrayListOfCppClassObjectsInTheCppAST;

    //Pointer to the cppClassObjectFor "Main"
    private boolean isMain;

    //Simple class to keep track of the inheritance
    private static classHierarchy classHierarchyObj;


    //Link to the AST for easy excess
    private CPPAST cppast;

    //Set where in the AST the class will be added to i.e as a child of namespace
    private GNode linkToNameSpaceGNodeInCppAST;

    //DataStructure of the __Object that we will start checking from
    private static CppDataLayout __ObjectDatalayout;

    //Link to the cppDataLayout -> its constuctor takes a JavaClassSummary object and translate the JavaCode into the C++
    //representation e.g adding __ or (*) to method names, fields etc. cpp
    private CppDataLayout cppDataLayout;

    //Constructor for the CppClassObject
    public CppClassObject(String cppClassName, CustomClassObject javaClass) {
        this.cppClassName = cppClassName;
        this.cppDataLayout = new CppDataLayout();
        this.arrayListOfCppClassObjectsInTheCppAST = new ArrayList<CppClassObject>();
        this.isMain = false;
    }

    //Helper method to create the default OBJECT datalayout
    //e.g with all the default methods hashtable etc..
    public static CppDataLayout create__ObjectDataLayout(){
        //TODO change this to the default object constructor
        return new CppDataLayout();
    }

    //Helper method to create the CppDataLayout for each class
    public void createCppDataLayoutExtends__Object(CppDataLayout __ObjectDatalayout, CustomClassObject javaCustomClassObject){
        //constructor of CppDataLayout that takes in a __ObjectDataLayout and the javaCustom
        this.cppDataLayout = new CppDataLayout(__ObjectDatalayout,javaCustomClassObject);
    }

    public void createCppDataLayoutExtendsParent(CppDataLayout parentDataLayout, CustomClassObject javaCustomClassObject){
        this.cppDataLayout = new CppDataLayout(parentDataLayout,javaCustomClassObject);
    }

    //Setters and getters
    public void setCppast(CPPAST cppast) {
        this.cppast = cppast;
    }

    public CPPAST getCppast() {
        return cppast;
    }

    public void setCppDataLayout(CppDataLayout cppDataLayout) {
        this.cppDataLayout = cppDataLayout;
    }

    public CppDataLayout getCppDataLayout() {
        return cppDataLayout;
    }

    public static void setArrayListOfCppClassObjectsInTheCppAST(ArrayList<CppClassObject> arrayListOfCppClassObjectsInTheCppAST) {
        CppClassObject.arrayListOfCppClassObjectsInTheCppAST = arrayListOfCppClassObjectsInTheCppAST;
    }

    public static ArrayList<CppClassObject> getArrayListOfCppClassObjectsInTheCppAST() {
        return arrayListOfCppClassObjectsInTheCppAST;
    }

    public static void set__ObjectDatalayout(CppDataLayout __ObjectDatalayout) {
        CppClassObject.__ObjectDatalayout = __ObjectDatalayout;
    }

    public static CppDataLayout get__ObjectDatalayout() {
        return __ObjectDatalayout;
    }

    public void setOldJavaClassName(String oldJavaClassName) {
        this.oldJavaClassName = oldJavaClassName;
    }

    public String getOldJavaClassName() {
        return oldJavaClassName;
    }

    public void setParentClass(CppClassObject parentClass) {
        this.parentClass = parentClass;
    }

    public CppClassObject getParentClass() {
        return parentClass;
    }

    public void setisMain(boolean isMain){
        this.isMain = isMain;
    }
    public boolean getisMain(){
        return this.isMain;
    }

    public void setCppClassName(String cppClassName) {
        this.cppClassName = cppClassName;
    }

    public String getCppClassName() {
        return cppClassName;
    }

    public void setLinkToNameSpaceGNodeInCppAST(GNode linkToNameSpaceGNodeInCppAST) {
        this.linkToNameSpaceGNodeInCppAST = linkToNameSpaceGNodeInCppAST;
    }

    public GNode getLinkToNameSpaceGNodeInCppAST() {
        return linkToNameSpaceGNodeInCppAST;
    }


    @Override
    public String toString() {
        return this.cppClassName;
    }


    //Custom object to keep track of inheritance
    public static class classHierarchy {
        private HashMap<String, CppClassObject> nameOfClassForCppClassObjectHashMap;
        private HashMap<String, String> childForParentHashMap;
        private HashMap<String, ArrayList<String>> parentForChildHashMap;

        //Constructor for the class Hirarchy object
        public classHierarchy(){
            this.nameOfClassForCppClassObjectHashMap = new HashMap<String, CppClassObject>();
            this.childForParentHashMap = new HashMap<String, String>();
            this.parentForChildHashMap = new HashMap<String, ArrayList<String>>();
        }

        public void setNameOfClassForCppClassObjectHashMap(HashMap<String, CppClassObject> nameOfClassForCppClassObjectHashMap) {
            this.nameOfClassForCppClassObjectHashMap = nameOfClassForCppClassObjectHashMap;
        }

        public HashMap<String, CppClassObject> getNameOfClassForCppClassObjectHashMap() {
            return nameOfClassForCppClassObjectHashMap;
        }

        public void setChildForParentHashMap(HashMap<String, String> childForParentHashMap) {
            this.childForParentHashMap = childForParentHashMap;
        }

        public HashMap<String, String> getChildForParentHashMap() {
            return childForParentHashMap;
        }

        //Helper methods
        //Add the class name for classObject in the HashMap
        public void addClassNameForCppClassObject (String className, CppClassObject classObject){
            this.nameOfClassForCppClassObjectHashMap.put(className,classObject);
        }
        //Get the CppClassObjectFor a class name given
        public CppClassObject getCppClassObjectFromName (String className){
            return this.nameOfClassForCppClassObjectHashMap.get(className);
        }
        //Add childName for its respective parent to HashMap
        public void addChildNameForParent (String childClassName, String parentClassName){
            this.childForParentHashMap.put(childClassName, parentClassName);
        }
        //Get parentClassName when given a childClass Name
        public String getParentNameForChild (String childClassName){
            return this.childForParentHashMap.get(childClassName);
        }

        //Add the children for a given parent
        public void addChildClassToParent(String childClass, String parentClass){
            //Get the array list inside the hashmap
            ArrayList<String> listOfChildren = this.parentForChildHashMap.get(parentClass);
            //Add the the list of children
            //check if the childrens list is null
            if(listOfChildren == null){
                //create a new list
                listOfChildren = new ArrayList<String>();
                listOfChildren.add(childClass);
                this.parentForChildHashMap.put(parentClass, listOfChildren);
            }
            else{
                //list exist we just add
                listOfChildren.add(childClass);
            }
        }

        public ArrayList <String> getListOfChildrenForParent(String parentClass){
            return this.parentForChildHashMap.get(parentClass);
        }

        public HashMap<String, ArrayList<String>> getParentForChildHashMap() {
            return parentForChildHashMap;
        }
    }

    //Setter and getter for classHierarchy
    public static void setClassHierarchy(CppClassObject.classHierarchy classHierarchy) {
        CppClassObject.classHierarchyObj = classHierarchy;
    }
    public static CppClassObject.classHierarchy getClassHierarchy() {
        return classHierarchyObj;
    }

}