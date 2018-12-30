package com.atguigu.gmall.order.task;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * CAS  ABA问题解决需要控制版本号
 */
public class AtomicStampleReferenceDemo {
    public static void main(String[] args) {
        AtomicStampedReference<Integer> stampedReference=new AtomicStampedReference<>(100,0);
        stampedReference.compareAndSet(100,101,
                stampedReference.getStamp(),stampedReference.getStamp()+1);
        boolean stamp = stampedReference.attemptStamp(101, 2);
        System.out.println("stamp = " + stamp);
    }
}
