package edu.nyu.oop;

import java.util.List;

import edu.nyu.oop.util.cppNodeActions;

import org.junit.*;
import org.slf4j.Logger;
import xtc.tree.GNode;

import edu.nyu.oop.CppHeaderPrinter;


import static org.junit.Assert.*;

public class CppHVisitorTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppHVisitorTest.class);

    private static GNode node = null;

    CppHeaderVisitor visitor = new CppHeaderVisitor();


    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing MethodVisitorTest");
        node = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test012/Test012.java");
        XtcTestUtils.prettyPrintAst(node);
    }

    @Before
    public void before() {
        //Phase one
        List <GNode> javaASTNodes = PrimarySourceAndDep.getSourceAndDep(node);

        //Phase two
        //CppHeaderVisitor visitor = new CppHeaderVisitor();

        //Phase two
        CPPAST cppAST = CppHeaderASTCreator.createNewCPPHeaderAstFrom(javaASTNodes);
        //Print the CPPAST
        XtcTestUtils.prettyPrintAst(cppAST.getRoot());

        //Visit
        //visitor.visit(cppAST.getRoot());

        // print cpp node names for translation
        CppHeaderPrinter printer = new CppHeaderPrinter();
        printer.print(cppAST.getRoot());

        //Phase three ->Take cppAST from above and use visit methods to access the nodes and data
        //Print data into Cpp.h file
        //(cppAST.getRoot()) <- use this as your point to start


//        for (int i = 0; i < cppAST.getRoot().size(); i++){
//            if (cppAST.getRoot().getNode(i).getName() == "Dependencies"){
//                //Get the Dependencies
//                GNode depNode = (GNode) cppAST.getRoot().getNode(i);
//                for (int k = 0; k < depNode.size(); k++){
//                    cppNodeActions.simpleCPPDataNode datanode = (cppNodeActions.simpleCPPDataNode) depNode.get(i);
//                    System.out.println(datanode.getType());
//                    System.out.println(datanode.getValue());
//                }
//            }
//        }

    }

    @Test
    public void testVistCPPAstTree() {

    }

}
