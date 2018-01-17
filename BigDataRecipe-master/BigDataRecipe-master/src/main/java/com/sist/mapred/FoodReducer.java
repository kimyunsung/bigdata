package com.sist.mapred;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class FoodReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
	private IntWritable res=new IntWritable();

	@Override
	protected void reduce(Text key, Iterable<IntWritable> values,
			Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
		int sum=0;
		for (IntWritable i : values) {
			sum+=i.get();
			
		}
		res.set(sum);
		context.write(key, res);
	
	}
	
	
	
	
	
	
}
