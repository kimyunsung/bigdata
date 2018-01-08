package com.sist.naver;

/*
 * 1.데이터 수집
 * 2.분석
 * 3.R
 * 4.몽고디비
 * */

import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.stereotype.Component;

import java.net.*;
import java.io.*;

@Component
public class NaverManager {
	public List<Item> getNewsAllData(String data){
		List<Item> list=new ArrayList<Item>();
		
		try {
			URL url=new URL("http://newssearch.naver.com/search.naver?where=rss&query="+URLEncoder.encode(data, "UTF-8"));
			JAXBContext jc=JAXBContext.newInstance(Rss.class);
			Unmarshaller un=jc.createUnmarshaller();
			//Unmarshaller(XML==>Object)
			//Marshaller(Object==>XML) //이클래스로 json도 만들 수 있다.
			
			Rss rss=(Rss) un.unmarshal(url);
			list=rss.getChannel().getItem();
			
			
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return list;		
	}
	
	public void naverBlogData(String title, int page) {
        String clientId = "u8d9L6wn9gGh4lLaDQoe";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "JDZGgXLDGd";//애플리케이션 클라이언트 시크릿값";
        try {
            String text = URLEncoder.encode(title, "UTF-8");
            //String apiURL = "https://openapi.naver.com/v1/search/blog?query="+ text; // json 결과
            String apiURL = "https://openapi.naver.com/v1/search/blog.xml?display=100&start="+page+"&query="+ text; // xml 결과
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            //System.out.println(response.toString());
            FileWriter fw=new FileWriter("/home/sist/food_data/food_reply.xml");
            fw.write(response.toString());
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        
    }
	
    public void naverXmlParse()
    {
    	try
    	{
    		File file=new File("/home/sist/food_data/food_reply.xml");
    		JAXBContext jc=
    				JAXBContext.newInstance(Rss.class);
    		Unmarshaller un=jc.createUnmarshaller();
    		// XML==>Object : Unmarshaller
    		// Object==>XML : Marshaller
    		Rss rss=(Rss)un.unmarshal(file);
    		List<Item> list=rss.getChannel().getItem();
    		String data="";
    		for(Item i:list)
    		{
    			data+=i.getDescription()+"\n";
    		}
    		data=data.substring(0,data.lastIndexOf("\n"));
    		//data=data.replaceAll("[^가-힣 ]", "");
    		FileWriter fw=new FileWriter("/home/sist/food_data/naver.txt");
    		fw.write(data);
    		fw.close();
    	}catch(Exception ex)
    	{
    		System.out.println(ex.getMessage());
    		ex.printStackTrace();
    	}
    }
	
}
