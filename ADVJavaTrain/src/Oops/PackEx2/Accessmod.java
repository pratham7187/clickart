package Oops.PackEx2;
class Ex{
     static int age=30;
     void display(){
        System.out.println(age);
    }
}
public class Accessmod {
    public static void main(String[] args) {
        Ex a=new Ex();
        a.display();
        a.age=10;
        a.display();
    }
}
