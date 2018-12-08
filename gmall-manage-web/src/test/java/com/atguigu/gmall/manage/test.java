package com.atguigu.gmall.manage;

import org.junit.Test;


    public class test
    {
        public static void main(String[] args){
            test t = new test();
            System.out.println("Expected Result is:" + t.foo(3,5));
        }

        public int foo(int a,int b)
        {
            if(a<=0||b<=0)
                return 1;
            return 3*foo(a-1,b/2);

        }
    }

