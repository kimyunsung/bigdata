package com.sist.stream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sist.dao.OracleIngrDAO;
import com.sist.mapred.HadoopManager;
import com.sist.naver.NaverManager;

@Component
public class AutoRead {
	private Configuration hconf;// 하둡

	private JobConf jobConf;// 하둡에 저장

	private IngrRankDAO dao=new IngrRankDAO();
	
	@Autowired
	private NaverManager naverManager;
	
	@Autowired
	private OracleIngrDAO oracleIngrDAO;
	
	
	@Autowired
	private HadoopManager hadoopManager;
	
	
	private List<IngrRankVO> ingrList;
	String[] weather={
			"맑은 날",
			"흐린 날",
			"비오는 날",
			"눈오는 날",
			"비눈 오는 날",
			"황사/안개 있는 날"
	};
	
	@PostConstruct
	public void weatherInit(){
		ingrList=oracleIngrDAO.selectIngr();
		System.out.println("레시피 갯수는 "+ingrList.size());

	}
	
	@PostConstruct
	public void sparkInit() {
		try {
			// 참조변수를 이용하면 못쓴다.
			hconf = new Configuration();
			hconf.set("fs.default.name", "hdfs://NameNode:9000");
			jobConf = new JobConf(hconf);

			/*
			 * sconf=new
			 * SparkConf().setAppName("Twitter-Real").setMaster("local[2]");
			 * jsc=new JavaStreamingContext(sconf, new Duration(10000));
			 */

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	//@Scheduled(cron = "10 * * * * *") //매분 10초마다
	@Scheduled(fixedDelay = 6000) 
	public void hadoopFileRead(){
		
		String root="/user/hadoop_ns1/";
		try {
			FileSystem fs=FileSystem.get(hconf);
			FileStatus[] status=fs.listStatus(new Path(root));
			for (FileStatus sss : status) {
				
				String temp=sss.getPath().getName();
				if (!temp.startsWith("food_ns1")) {
					continue; //위단어로 시작하지 않으면 넘어간다. 다른폴더가 생겨도 상관없어진다.
				}
				
				FileStatus[] status2=fs.listStatus(new Path(root+sss.getPath().getName()));
				for (FileStatus ss : status2) {
					String name=ss.getPath().getName();
					if (!name.equals("_SUCCESS")&&!name.equals("_temporary")) {
						FSDataInputStream is=fs.open(new Path(root+sss.getPath().getName()+"/"+ss.getPath().getName()));
						BufferedReader br=new BufferedReader(new InputStreamReader(is));
						while (true) {
							String line=br.readLine();
							if (line==null) {
								break;
							}
							StringTokenizer st=new StringTokenizer(line);
							IngrRankVO vo=new IngrRankVO();
							vo.setName(st.nextToken().trim().replace("$", " "));
							vo.setCount(Integer.parseInt(st.nextToken().trim()));
							dao.foodInsert(vo);
							
							
						}
						br.close();
					}
				}
				//읽고 다음에 읽을때 다시 읽지 않기 위해 읽은 폴더를 지운다.
				fs.delete(new Path(root+sss.getPath().getName()), true);
				fs.close();//잘닫아주자.
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("몽고디비 갱신!!");
	}
	
	@Scheduled(fixedDelay = 10000) 
	public void naverWeatherRecipeRead(){
		IngrRankDAO ingrRankDAO=null;
		
		int page=1;
		int count=1;
		for (int i = 0; i < weather.length; i++) {
			ingrRankDAO=new IngrRankDAO(weather[i]);
			
			naverManager.naverBlogData(weather[i]+"에 추천하는 음식 재료", page);
			System.out.println(weather[i]);
			naverManager.naverXmlParse();
			
			hadoopManager.hadoopFileDelete();
			hadoopManager.copyFromLocal();
			hadoopManager.mapReduceExceute();
			hadoopManager.copyToLocal();
			
			List<IngrRankVO> list=readHadoopResult();
			for (IngrRankVO vo : list) {
				ingrRankDAO.foodInsert(vo);
				
			}
			System.out.println("page="+page);
			count++;
			if (count%6==0) {
				page++;
				count=1;
				if (page==10) {
					page=1;
				}
			}
			System.out.println("맵리듀스 완료");
		}
		
	}
	
	public List<IngrRankVO> readHadoopResult(){
		List<IngrRankVO> list=new ArrayList<IngrRankVO>();
		
		try {
			FileReader fileReader=new FileReader("/home/sist/food_data/food_result");
			BufferedReader bufferedReader=new BufferedReader(fileReader);
			
			String data="";
			while ((data=bufferedReader.readLine())!=null) {
				IngrRankVO vo=new IngrRankVO();
				
				StringTokenizer st=new StringTokenizer(data);
				
				vo.setName(st.nextToken());
				vo.setCount(Integer.parseInt(st.nextToken()));
				if (vo.getName().length()>1&&!vo.getName().equals("가지")) {
					list.add(vo);
					
				}
				
			};
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
				
		return list;
	}
}
