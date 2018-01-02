package inputs.CustomTests;


class A {


    int theInt;
    boolean theBoolean;
    float theFloat;
    long theLong;
    short theShort;
    char theChar;
    double theDouble;
    byte theByte;
    A A;

    String a = "greatstring123";

    public void setA(String x) {
        //int x = 1;
        a = x;
    }

    public void printOther(A other) {
        System.out.println(other.toString());
    }

    public String toString() {
        return a;
    }



}

class B1 extends A {
    public void helloWorld(){}
    public B1(){}
    public B1(int x, int y){}
    public B1(A a){}
    String b;

    A A;
    B1 B;

}

class B2 extends B1 {
    public void goodbye(){}
    public void helloWorld(){}
    public void helloJohn(){}

    String b;
}

class C extends B2 {
    public void helloDave(){}
    public String toString() {
        return "123";
    }
    String c;
}

public class CustomTest1 {
    public static void main(String[] args) {
        A a = new A();
        int[] x = new int[5];
        int[] xox2 = new int[5];
        int[][] y = new int[3][3];
        y[0][0] = 1;
        x[0] = 1;
        B1 b12 = new B1(111,232323);
        B1 b$11 = new B1(111,232323);
        B1 b$222 = new B1(a);
        Object[] $as = new B1[10];
        int $thing11 = y[0][0];
        int $thing22 = x[0];

        String xyz = "greatstring22";
        int intCheck = 42;
        a.setA("A123");
        B1 b1 = new B1();
        b1.setA("B1");
        B2 b2 = new B2();
        b2.setA("B2");
//        B1 obj = b2.setA("B2");
        C c = new C();
        c.setA("C");
        a.printOther(a);
        a.printOther(b1);
        a.printOther(b2);
        a.printOther(c);
    }
}