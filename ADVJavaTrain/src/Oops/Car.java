package Oops;

public class Car {
    String color;
    String name;
    double rate;
    void meth(String col,String name,int rate){
        color=col;
        this.name=name;
        this.rate=rate;
    }

    public static void main(String[] args) {
        Car c=new Car();
        c.meth("black","Bently",1000);

        System.out.println(c.color);
        System.out.println(c.name);
        System.out.println(c.rate);
    }
}


