package com.chh.ap.cs.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.dao.BaseDao;
import com.chh.ap.cs.dao.IDeviceDao;

public class DeviceDao extends BaseDao implements IDeviceDao {

	@Override
	public SessionManager.SessionInfo login(String deviceId, int deviceType) throws Exception {
		SessionManager.SessionInfo sess = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{				
			con = getConnection();
			ps = con.prepareStatement("select id,sn,status from t_device where sn=? and device_type=?");
			ps.setString(1, deviceId);
			ps.setInt(2, deviceType);
			rs = ps.executeQuery();			
			while(rs.next()){
				sess = new SessionManager.SessionInfo(rs.getString(1));
				sess.setSn(rs.getString(2));
				sess.setStatus(rs.getInt(3));
				break;
			}
		}catch(Exception e){			
			throw e;
		}finally{
			close(rs, ps);
			closeConnection();
		}
		return sess;
	}

}
