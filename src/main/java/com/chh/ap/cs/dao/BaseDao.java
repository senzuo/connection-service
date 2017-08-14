package com.chh.ap.cs.dao;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class BaseDao {

    private static final Logger log = Logger.getLogger(BaseDao.class);

    private DataSource dataSource;

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            System.out.println(111111111L);
        }
        return dataSource.getConnection();
    }


    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
