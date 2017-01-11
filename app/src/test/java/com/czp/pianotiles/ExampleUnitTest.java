package com.czp.pianotiles;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        ArrayList<String> data = new ArrayList<>();
        for(int i = 0; i < 10 ; i ++){
            data.add(i+"aaaa");
        }
        data.remove(data.size()-1);
        data.remove(data.size()-1);
        data.remove(data.size()-1);
        data.remove(data.size()-1);
        data.add(0,"哈哈");
        data.add(0,"哈哈");
        data.add(0,"哈哈");
        data.add(0,"哈哈");

        for(String i : data){
            System.out.println(i);
        }
    }
}