package com.atguigu.gmall.order.task;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

/**
 * 信号量   一般用于控制某个资源     获取许可  aqs try accique()  释放许可 release()
 */
public class SemphoreTest implements Serializable {

    public static void main(String[] args) throws InterruptedException {
        Semaphore   semaphore=new Semaphore(2);
        semaphore.acquire();
        semaphore.release();
    }
}
