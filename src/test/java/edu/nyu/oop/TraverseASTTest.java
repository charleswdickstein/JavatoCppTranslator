package edu.nyu.oop;

import org.junit.*;
import org.slf4j.Logger;
import xtc.tree.GNode;
import java.util.ArrayList;
import edu.nyu.oop.util.JavaFiveImportParser;
import java.util.List;
import xtc.tree.Location;
import edu.nyu.oop.util.SymbolTableBuilder;
import edu.nyu.oop.util.SymbolTableUtil;

import static org.junit.Assert.*;

public class TraverseASTTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TraverseASTTest.class);

    private static GNode node = null;

   // private TraverseAST.MethodSummary summary;

    private TraverseAST.ClassSummary classSummary;
    private TraverseASTM.ImplementationSummary implementationSummary;


    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing TraverseASTTest");

        node = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test010/Test010.java");

        //XtcTestUtils.prettyPrintAst(node);
    }

    @Before
    public void before() {

        TraverseAST visitor = new TraverseAST();
        //summary = visitor.getSummary(node);

        //Test Phase one
        System.out.println("Start Phase One");
        List <GNode> javaASTNodes = PrimarySourceAndDep.getSourceAndDep(node);
        //System.out.println("Total Number of AST's found: " + javaASTNodes.size());
      //  GNode firstNode = javaASTNodes.get(0); //Get the first node
        //System.out.println(firstNode.getLocation().file);
        //System.out.println(firstNode);
        //Print packages

        System.out.println("END Phase One");

        //TEST the values are correct from the JavaAST
        classSummary = visitor.getClassSummary(javaASTNodes.get(0));

        //Test the values for JavaAST Implementation
        //SymbolTable table = new SymbolTableBuilder(runtime).getTable(n);
        //TraverseASTM visitorM = new TraverseASTM();
        //implementationSummary = visitorM.getImplementationSummary(javaASTNodes.get(0));

    }

    @Test
    public void testMethodSummary1() {
        // Assert that the correct number of methods were counted by our visitor
        //System.out.println(classSummary.classes.toString());
        System.out.println("Print classes");
//        System.out.println("Packages: " + classSummary.packages);
        for (int i =0; i < classSummary.classes.size(); i++){

//            System.out.println("Class Name: " + classSummary.classes.get(i).className + " extends " + classSummary.classes.get(i).getParentClass());

            //Print all constructors
            ArrayList<CustomConstructorClass> constructors = classSummary.classes.get(i).getConstructors();
            for (int k = 0; k < constructors.size(); k++) {
//                System.out.println("Constructor " + k );
                CustomConstructorClass c = constructors.get(k);
//                System.out.println("Con vis " + c.getVisibility());
//                System.out.println("Con name " + c.getName());
                ArrayList<CustomVariablesClass> vars = c.getParameters();
                for (int l = 0; l < vars.size(); l++) {
//                    System.out.println("Construct Param " + k + " " + l);
                    CustomVariablesClass v = vars.get(l);
//                    System.out.println("Con param vis " + v.visibility);
//                    System.out.println("Con param mod " + v.modifier);
//                    System.out.println("Con param name " +v.name);
//                    System.out.println("Con param type " +v.type);
                }
            }

            //Print all class vars
            ArrayList<CustomVariablesClass> vars = classSummary.classes.get(i).getClassVariables();
            for (int k = 0; k < vars.size(); k++){
                System.out.println("Class Var " + k);
                CustomVariablesClass v = vars.get(k);
                System.out.println("var vis " + v.visibility);
                System.out.println("var mod " + v.modifier);
                System.out.println("var name " + v.name);
                System.out.println("var type " + v.type);
            }

            //Print all the methods in the class
            for (int j = 0; j < classSummary.classes.get(i).methods.size(); j++){
                System.out.println("Method " + j);
                System.out.println("m return " + classSummary.classes.get(i).methods.get(j).returnType);
                System.out.println("m vis " + classSummary.classes.get(i).methods.get(j).visibility);
                System.out.println("m mod " + classSummary.classes.get(i).methods.get(j).modifier);
                System.out.println("m name " + classSummary.classes.get(i).methods.get(j).name);
                ArrayList<CustomVariablesClass> methodParam = classSummary.classes.get(i).methods.get(j).getParameters();
                for (int m = 0; m < methodParam.size(); m++){
                    System.out.println("Method Param " + j + " " + m);
                    CustomVariablesClass v = methodParam.get(m);
                    System.out.println("var vis " + v.visibility);
                    System.out.println("var mod " + v.modifier);
                    System.out.println("var name " + v.name);
                    System.out.println("var type " + v.type);
                }
            }

        }


        //assertEquals(3, classSummary.classes.size());
        //assertEquals(3, classSummary.classes);

//        assretEquals(1, classSummary.classes.get(1).getConstructors().size());
        // to see constructor details of the first class in the file
//        ArrayList<CustomConstructorClass> constructors = classSummary.classes.get(0).getConstructors();
//        for (int i = 0; i < constructors.size(); i++) {
//            CustomConstructorClass c = constructors.get(i);
//            System.out.println(c.getVisibility());
//            System.out.println(c.getName());
//            ArrayList<CustomVariablesClass> vars = c.getParameters();
//            for (int j = 0; j < vars.size(); j++) {
//                CustomVariablesClass v = vars.get(j);
//                System.out.println(v.visibility);
//                System.out.println(v.modifier);
//                System.out.println(v.name);
//                System.out.println(v.type);
//            }
//        }
    }

//    @Test
//    public void testMethodSummary2() {
//        // Assert that the toString method name was collected
//        assertTrue(summary.names.contains("toString"));
//    }
////
//    @Test
//    public void testMethodSummary3() {
//        // Assert that the main method name was collected
//        assertTrue(summary.names.contains("main"));
//    }

}
