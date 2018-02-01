package com.timeyang.flink.streaming;

import java.util.Arrays;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class JavaWordCountApp {

    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<String> input = env.fromElements("Please count", "the words", "but not this");

        // filter out strings that contain "not"
        input.filter(line -> !line.contains("not"))
                // split each line by space
                .map(line -> line.split(" "))
                // emit a pair <word,1> for each array element
                .flatMap((String[] wordArray, Collector<Tuple2<String, Integer>> out)
                        -> Arrays.stream(wordArray).forEach(t -> out.collect(new Tuple2<>(t, 1)))
                )
                .groupBy(0).sum(1)
                // print
                .print();
    }

}
