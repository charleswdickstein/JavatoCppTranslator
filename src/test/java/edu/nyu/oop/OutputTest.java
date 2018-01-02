package edu.nyu.oop;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import xtc.tree.GNode;

import java.util.List;

public class OutputTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TraverseASTTest.class);

    private static GNode node = null;

    // private TraverseAST.MethodSummary summary;

    private TraverseAST.ClassSummary classSummary;
    private TraverseASTM.ImplementationSummary implementationSummary;


    @BeforeClass
    public static void beforeClass() {
        logger.debug("Executing TraverseASTTest");

        node = (GNode) XtcTestUtils.loadTestFile("src/test/java/inputs/Test010/Test010.java");// appropriate test goes here


        // XtcTestUtils.prettyPrintAst(node);
    }

    @Before
    public void before() { // change this to import the proper c++ implementation code

        TraverseAST visitor = new TraverseAST();
        //summary = visitor.getSummary(node);

        //Test Phase one
        System.out.println("Start Phase One");
        List<GNode> javaASTNodes = PrimarySourceAndDep.getSourceAndDep(node);
        //System.out.println("Total Number of AST's found: " + javaASTNodes.size());
        //  GNode firstNode = javaASTNodes.get(0); //Get the first node
        //System.out.println(firstNode.getLocation().file);
        //System.out.println(firstNode);
        //Print packages

        // add other phases

        System.out.println("END Phase One");

        //TEST the values are correct from the JavaAST
        classSummary = visitor.getClassSummary(javaASTNodes.get(0));

        //Test the values for JavaAST Implementation
        //SymbolTable table = new SymbolTableBuilder(runtime).getTable(n);
        //TraverseASTM visitorM = new TraverseASTM();
        //implementationSummary = visitorM.getImplementationSummary(javaASTNodes.get(0));

    }

    @Test
    public void testOutput() {

    }
//        class A {
//            public String toString() {
//                return "A";
//            }
//        }
//
//        public class Test001 {
//            public static void main(String[] args) {
//                A a = new A();
//                System.out.println(a.toString());
//            }
//        }

        /*
        __A::__String toString(__A this){
            return __rt::literal ("A");
        }
        #include <iostream>

        #include "java_lang.h"
        #include "output.h"

        using namespace inputs::javalang;
        using namespace java::lang;
        using namespace std;

        using __rt::__Array;
        using __rt::Array;
        using __rt::Ptr;

        int main(void) {
            __A a = new __A::__init(__A());
            std::cout << a->__vptr->toString(a) << std::endl;
        }
        //    class A {
//        String a;
//
//        public void setA(String x) {
//            a = x;
//        }
//
//        public void printOther(A other) {
//            System.out.println(other.toString());
//        }
//
//        public String toString() {
//            return a;
//        }
//    }
//
//    class B1 extends A {
//        String b;
//    }
//
//    class B2 extends A {
//        String b;
//    }
//
//    class C extends B1 {
//        String c;
//    }
//
//    public class Test010 {
//        public static void main(String[] args) {
//            A a = new A();
//            a.setA("A");
//            B1 b1 = new B1();
//            b1.setA("B1");
//            B2 b2 = new B2();
//            b2.setA("B2");
//            C c = new C();
//            c.setA("C");
//            a.printOther(a);
//            a.printOther(b1);
//            a.printOther(b2);
//            a.printOther(c);
//        }


        void __A::__setA(__A this, __String x){
            this.a = x;
        }
        void _A::__printOther(__A this, __A other){
            cout << other->__vptr->toString(other); << endl;
        }
        String _A::__toString(__A this) {
            return this->a;
        }

        __A a = __A::__init(new __A());
        a->vptr->setA(a, __rt::literal("A"));
        __B1 b1 = __B1(new __B1());
        b1->vptr->setA(b1, __rt::literal("B1"));
        __B2 b2 = __B2(new __B2());
        b2->vptr->setA(b2, __rt::literal("B2"));
        __C c = __C(new __C());
        c->vptr->setA(c, __rt::literal("C"));
        a->vptr->printOther(a,a);
        a->vptr->printOther(a,b1);
        a->vptr->printOther(a,b2);
        a->vptr->printOther(a,c);

        */
}
