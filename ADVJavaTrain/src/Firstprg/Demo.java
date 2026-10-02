package Firstprg;

import java.sql.SQLOutput;
import java.util.*;
public class Demo {
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
       int [] arr=new int[10];
        int [] arr2={10,20,20,40,50};
       int n=5;
       for(int i=0;i<n;i++){
           arr[i]=i;
        }
        System.out.println("Array elements are:");
        for(int i=0;i<n;i++){
            System.out.println("index"+(i+1)+":"+arr[i]);
        }
        for(int i=0;i<n;i++){
            System.out.println("index"+(i+1)+":"+arr2[i]);
        }
    }
}
