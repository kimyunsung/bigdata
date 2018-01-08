package com.sist.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DBManager { //여기서 class는 클래스 생성하는 예약어
//이파일의 확장자를java로바꾸고db정보입력하세요
	static private DBManager instance;
	private String driver="oracle.jdbc.driver.OracleDriver";
	private String url="jdbc:oracle:thin:@211.238.142.123:1521:ORCL"; //@다음공유폴더db접속 ip입력하세요
	private String user="itmenu";//빈칸에 username입력하세요
	private String password="unemti";//빈칸에 비밀번호입력하세요
	
	private Connection con;
	
	
	private DBManager(){
		/*1.드라이버 로드
		 *2. 접속
		 *3. 쿼리문 수행
		 *4. 반납, 해체*/
		try {
			Class.forName(driver); //Class class는 클래스에대한 정보를 가지고 있다. 어떤 메소드와 변수로 구성되어 있는지 알 수 있다.
			con=DriverManager.getConnection(url, user, password);
			if (con!=null) {
				System.out.println("DB접속성공");
			} else {
				System.out.println("DB접속실패");			
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}

	static public DBManager getInstance() {
		if (instance==null) {
			instance=new DBManager();
		}		
		
		return instance;
	}

	public Connection getConnection() {
		return con;
	}

	public void disConnect(Connection con) {
		if (con!=null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}	
	
}