package edu.nyu.oop;

import org.junit.BeforeClass;
import org.junit.Test;
import xtc.tree.GNode;

/**
 * Here you will author a test to confirm that your LinkedListVisitor is functioning correctly.
 * Most of what you will need to do is to decide what assertions to make about the summary.
 * For example, how many scopes does it have? Or what line does a particular scope begin on?
 * Refer to MethodVisitorTest for a similar but simpler test.
 */
public class SymbolTableCreateTest {

    private static GNode node = null;

    @BeforeClass
    public static void beforeClass() {
        System.out.println("Executing SymbolTableCreateTest");
        node = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/test000/Test000.java");
        //XtcTestUtils.prettyPrintAst(node);
    }
    @Test
    public void testScopeSummary() {


       // Thing visitor = new LinkedListVisitor(XtcTestUtils.newRuntime());
      //  Thing.ScopeSummary summary = visitor.getSummary(node);
        //Thing sum = new Thing(XtcTestUtils.newRuntime());
        // test the total scopes is of size 13

    }


}


