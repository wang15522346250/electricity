package com.atguigu.gmall.order.task;

/**
 * 两个线程，print-1   print-2   分别打印print-1 1   print-2 2    print-1 3     print-2 4  依次递增至100
 */
public class ThreadTest {
    static volatile int count = 0;
    static volatile boolean flag = false;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            while (count < 100) {
                if (!flag && count % 2 == 0) {
                    count++;
                    System.out.println(Thread.currentThread() + "" + count);
                    flag = true;
                }
            }
        }, "print-1");

        Thread t2 = new Thread(() -> {
            while (count < 100) {
                if (flag && count % 2 != 0) {
                    count++;
                    System.out.println(Thread.currentThread() + "" + count);
                    flag=false;
                }
            }
        },"print-2");
        t1.start();
        t2.start();
    }

}
