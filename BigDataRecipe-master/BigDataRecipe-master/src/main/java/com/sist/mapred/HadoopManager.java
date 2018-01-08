package com.sist.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.mapreduce.JobRunner;
import org.springframework.stereotype.Component;

@Component
public class HadoopManager {
	 @Autowired
     private Configuration conf;
	 @Autowired
	 private JobRunner jr;
	 public void hadoopFileDelete()
	 {
		 try
		 {
			 FileSystem fs=FileSystem.get(conf);
			/* if(fs.exists(new Path("/music_input/daum.txt")))
			 {
				 fs.delete(new Path("/music_input/daum.txt"),true);
			 }*/
			 if(fs.exists(new Path("/food_input_ns1/naver.txt")))
			 {
				 fs.delete(new Path("/food_input_ns1/naver.txt"),true);
			 }
			 if(fs.exists(new Path("/food_output_ns1")))
			 {
				 fs.delete(new Path("/food_output_ns1"),true);
			 }
			 fs.close();
		 }catch(Exception ex)
		 {
			 System.out.println(ex.getMessage());
		 }
	 }
	 public void copyFromLocal()
	 {
		 try
		 {
			 FileSystem fs=FileSystem.get(conf);
			/* fs.copyFromLocalFile(new Path("/home/sist/music_data/daum.txt"),
					 new Path("/music_input/daum.txt"));*/
			 fs.copyFromLocalFile(new Path("/home/sist/food_data/naver.txt"),
					 new Path("/food_input_ns1/naver.txt"));
			 fs.close();
		 }catch(Exception ex)
		 {
			 System.out.println(ex.getMessage());
		 }
	 }
	 public void copyToLocal()
	 {
		 try
		 {
			 FileSystem fs=FileSystem.get(conf);
			 fs.copyToLocalFile(new Path("/food_output_ns1/part-r-00000"),
					 new Path("/home/sist/food_data/food_result"));
			 
			 fs.close();
		 }catch(Exception ex){}
	 }
	 public void mapReduceExceute()
	 {
		try
		{
			jr.call();
		}catch(Exception ex){}
	 }
}
