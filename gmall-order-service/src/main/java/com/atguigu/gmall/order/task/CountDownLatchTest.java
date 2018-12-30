package com.atguigu.gmall.order.task;

import java.util.concurrent.*;

/**
 * 4个线程工作，main线程统计四个线程结束后的任务
 */
public class CountDownLatchTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(4);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Future submit = executorService.submit(new Thread01(countDownLatch));
        Future submit1 = executorService.submit(new Thread02(countDownLatch));
        Future submit2 = executorService.submit(new Thread03(countDownLatch));
        Future submit3 = executorService.submit(new Thread04(countDownLatch));
        int count=(Integer) submit.get()+(Integer) submit1.get()+(Integer) submit2.get()+(Integer) submit3.get();
        System.out.println("主线程统计四个线程的工作"+count);
        executorService.shutdown();
    }

    static class Thread01 implements Callable {
        CountDownLatch countDownLatch;

        public Thread01(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Integer call() throws Exception {
            countDownLatch.countDown();
            return 100;

        }
    }

    static class Thread02 implements Callable {
        CountDownLatch countDownLatch;

        public Thread02(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Integer call() throws Exception {
            countDownLatch.countDown();
            return 100;

        }
    }

    static class Thread03 implements Callable {
        CountDownLatch countDownLatch;

        public Thread03(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Integer call() throws Exception {
            countDownLatch.countDown();
            return 300;

        }
    }

    static class Thread04 implements Callable {
        CountDownLatch countDownLatch;

        public Thread04(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public Integer call() throws Exception {
            countDownLatch.countDown();
            return 400;

        }
    }
}
