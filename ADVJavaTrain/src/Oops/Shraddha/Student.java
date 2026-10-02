package Oops.Shraddha;
class Stud{
    String name;
    int age;
    void printInfo(){
        System.out.println(name);
        System.out.println(age);
    }
    Stud(){

    }
    Stud(String name,int age){
        this.name=name;
        this.age=age;
    }
    Stud(Stud s3){
        this.name=s3.name;
        this.age=s3.age;
    }
}
public class Student {
    public static void main(String[] args) {

        Stud s1 = new Stud();
        s1.name = "Pratham";
        s1.age = 21;
        Stud s2 = new Stud("NIJA", 56);
        Stud s3 = new Stud(s2);
        s1.printInfo();
        s2.printInfo();
        s3.printInfo();
    }
}
