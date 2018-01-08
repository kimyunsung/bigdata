package com.sist.mapred;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.sist.dao.DBManager;

import java.io.*;

public class FoodMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	private final IntWritable one = new IntWritable(1);
	private Text res = new Text();
	static List<String> ingrNameList;
	
	static {
		DBManager dbManager = DBManager.getInstance();
		Connection con = dbManager.getConnection();
		PreparedStatement pstmt = null;

		StringBuffer sql = new StringBuffer();
		sql.append("Select name from ingredient");
		
		ingrNameList=new ArrayList<String>();

		try {
			pstmt = con.prepareStatement(sql.toString());
			ResultSet rs=pstmt.executeQuery();
			while (rs.next()) {
				ingrNameList.add(rs.getString("name"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			
			dbManager.disConnect(con);
			System.out.println(ingrNameList.size());
		}

	}

	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)
			throws IOException, InterruptedException {
		String[] mydata=new String[ingrNameList.size()];
		
		int i=0;
		for (String s : ingrNameList) {
			mydata[i]=s;
			i++;
		}
		
		Pattern[] p = new Pattern[mydata.length];
		for (int a = 0; a < p.length; a++) {
			p[a] = Pattern.compile(mydata[a]);

		}
		Matcher[] m = new Matcher[mydata.length];
		for (int a = 0; a < p.length; a++) {
			m[a] = p[a].matcher(value.toString());
			while (m[a].find()) {
				res.set(mydata[a]);
				context.write(res, one);
			}
		}

	}

}
