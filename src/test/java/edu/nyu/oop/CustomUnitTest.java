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
import xtc.util.SymbolTable;
import xtc.util.Tool;
import xtc.util.Runtime;
import static org.junit.Assert.*;


public class CustomUnitTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUnitTest.class);

    private static GNode node = null;

    private static TraverseAST.ClassSummary classSummary;
    private static CPPAST cppAST;
    private static TraverseASTM.ImplementationSummary implementationSummary;

    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing custom unit tests");
        node = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test040/Test040.java");

        //Pass through only one time

        Runtime runtime = XtcTestUtils.newRuntime();

        TraverseAST visitor = new TraverseAST();
        List<GNode> javaASTNodes = PrimarySourceAndDep.getSourceAndDep(node);
        classSummary = visitor.getClassSummary(javaASTNodes.get(0));


        CPPAST cppAST = CppHeaderASTCreator.createNewCPPHeaderAstFrom(javaASTNodes);

        SymbolTable table = new SymbolTableBuilder(runtime).getTable(node);
        TraverseASTM visitorM = new TraverseASTM(runtime, table);
        cppAST = CppMASTCreator.createNewCPPMAstFrom(javaASTNodes,cppAST,visitorM);


        implementationSummary = visitorM.getImplementationSummary(javaASTNodes.get(0));


    }

    /*
    @SuppressWarnings("Duplicates")
    public void before() {
        TraverseAST visitor = new TraverseAST();
        List<GNode> javaASTNodes = PrimarySourceAndDep.getSourceAndDep(node);
        classSummary = visitor.getClassSummary(javaASTNodes.get(0));

    }
    */
    @Test
    public void testConstructors() {
        System.out.println();

        System.out.println("Constructors collected by TraverseAST in test file: (Expected 0) ");
        int num = 0;
        for (int i = 0; i < classSummary.classes.size(); i++) {
            ArrayList<CustomConstructorClass> constructors = classSummary.classes.get(i).getConstructors();
            for (int k = 0; k < constructors.size(); k++) {
                CustomConstructorClass c = constructors.get(k);
                System.out.print(c.getName() + " ");
                num++;
            }
        }

        if(num == 0)
            System.out.println("No constructors were found for this file.");


        System.out.println();
        assertEquals(0, num);
    }

    @Test
    public void testFields() {
        System.out.println();

        int num = 0;
        System.out.println("Class variables collected by TraverseAST in test file: (Expected 0) ");
        for (int i = 0; i < classSummary.classes.size(); i++) {
            ArrayList<CustomVariablesClass> vars = classSummary.classes.get(i).getClassVariables();
            for (int k = 0; k < vars.size(); k++) {
                CustomVariablesClass v = vars.get(k);
                System.out.print(v.type + " " + v.name + " ");
                num++;
            }
        }

        if(num == 0)
            System.out.println("No class variables were found for this file.");


        System.out.println();
        assertEquals(0, num);
    }

    @Test
    public void testNumClasses() {
        System.out.println();
        int num;
        num = classSummary.classes.size();
        System.out.println("Total number of classes collected by TraverseAST in test file: (Expected 4) " + "\n" + num);


        System.out.println();
        assertEquals(4, num);
    }

    @Test
    public void testMethodsOfA(){
        System.out.println();

        ArrayList<CustomClassObject> testList = implementationSummary.implementationClassObjects;
        String method1 = "m_Object_Object";
        String method2 = "m_A_Object";
        String method3 = "m_Object_A";

        ArrayList<String> methodNames = testList.get(0).getMethodNames();
        assertEquals(3, methodNames.size());
        ArrayList<String> tests = new ArrayList<String>();

        for(int i = 0; i < methodNames.size(); i++){
            tests.add(methodNames.get(i));
        }

        System.out.println("Methods in implementationSummary for class A in test040: " + "\n" +
                methodNames.get(0) + " " + methodNames.get(1) + " " + methodNames.get(2) + " ");


        assertEquals(method1, methodNames.get(0));
        assertEquals(method2, methodNames.get(1));
        assertEquals(method3, methodNames.get(2));

        System.out.println();
    }

    @Test
    public void testMethodsOfC(){
        System.out.println();

        ArrayList<CustomClassObject> testList = implementationSummary.implementationClassObjects;
        assertEquals(0, testList.get(2).getMethodNames().size());

        System.out.println("No methods found for class C in test040.");


        System.out.println();
    }

}


