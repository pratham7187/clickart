package Oops.Shraddha;

import java.sql.SQLOutput;

class Book{
    String title;
    String author;
    double price;
    void display(){
        System.out.println(title+" "+author+" "+price);
    }
    Book(){

    }
    Book(String title,String author,double price){
        this.title=title;
        this.author=author;
        this.price=price;
    }
    Book(Book b3){
        this.title=b3.title;
        this.author=b3.author;
        this.price=b3.price;
    }
}
public class EX {
    public static void main(String[] args) {


        Book b1 = new Book();
        b1.title = "got";
        b1.author = "pratham";
        b1.price = 500.00;
        Book b2 = new Book("wwe","roman",600);

        Book b3 = new Book(b2);
        b3.price=1000;
        b1.display();
        b2.display();
        b3.display();
    }
}
