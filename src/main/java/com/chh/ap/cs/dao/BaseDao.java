package com.chh.ap.cs.dao;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSourceFactory;

public class BaseDao {
	private static final Logger log = Logger.getLogger(BaseDao.class);
	
	private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();
	
	private static DataSource ds = null;   
    
    static {
        try{
            InputStream in = BaseDao.class.getClassLoader().getResourceAsStream("config.properties");
//        	InputStream in = new FileInputStream("resources/config.properties");
            Properties props = new Properties();
            props.load(in);
            ds = DruidDataSourceFactory.createDataSource(props);
        }catch(Exception e){
        	log.error(e.getMessage(), e);
        	log.error("创建数据源异常！");
        }
    }
     
    private static Connection openConnection() throws Exception{
    	if(ds == null){
    		throw new Exception("数据源为空"); 
    	}
		return ds.getConnection();        
    }
    
    //获取Connection
    public Connection getConnection() throws Exception{
    	Connection conn = connectionHolder.get();
    	if(conn == null){
    		conn = openConnection();
    		connectionHolder.set(conn);
    	}
    	return conn;
    }
    
    //手动开启事务
	public void beginTransaction() throws Exception {
		Connection conn = connectionHolder.get();
		if (conn != null) {
			if (conn.getAutoCommit()) {
				conn.setAutoCommit(false); //手动提交
			}
		}
	}
	
	//提交事务
	public void commitTransaction() throws Exception {
		Connection conn = connectionHolder.get();
		if (conn != null) {
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		}
	}
	
	//事务回滚
	public void rollbackTransaction() throws Exception {
		Connection conn = connectionHolder.get();
		if (conn != null) {
			if (!conn.getAutoCommit()) {
				conn.rollback();
			}
		}
	}
    
	//关闭Connection(顺便设置事务为自动提交)
	public void closeConnection() throws Exception {
		Connection conn = connectionHolder.get();
		if (conn != null) {
			connectionHolder.remove();
			try {
				if (!conn.getAutoCommit()) {
					conn.setAutoCommit(true);
				}
				
			} catch (Exception e){
				
			}
			conn.close();
			//从ThreadLocal中清除Connection			
		}
	}
	
	public void close(ResultSet rs,Statement ps) throws Exception {
		if(rs != null){
			rs.close();
		}
		if(ps != null){
			ps.close();
		}
	}

	public void close(ResultSet rs,CallableStatement cs) throws Exception {
		if(rs != null){
			rs.close();
		}
		if(cs != null){
			cs.close();
		}
	}
	
}
