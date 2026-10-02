package Firstprg;

public class Student {

        int age=13;
        String name;

  Student(int n,String m){
      age=n;
      name=m;
  }

    public static void main(String[] args) {

        Student s1=new Student(19,"pratham");
        System.out.println(s1.age);
        System.out.println(s1.name);
    }
}
