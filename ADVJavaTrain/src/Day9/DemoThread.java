package Day9;

public class DemoThread extends Thread {
    public void run(){
        for(int i=1;i<=5;i++){
            System.out.println("Demo Thread");
        }
    }

    public static void main(String[] args) {
        System.out.println("Program starts");
        DemoThread d1=new DemoThread();
        d1.start();
        for(int i=1;i<=10;i++){
            System.out.println("main thread");
        }
        System.out.println("Program Ends");
    }
}
