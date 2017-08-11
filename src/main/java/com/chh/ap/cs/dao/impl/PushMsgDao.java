package com.chh.ap.cs.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.chh.ap.cs.dao.BaseDao;
import com.chh.ap.cs.dao.IPushMsgDao;
import com.chh.ap.cs.push.PushMsg;

public class PushMsgDao extends BaseDao implements IPushMsgDao {

	@Override
	public List<PushMsg> getPushMsg(int maxLen) throws Exception {
		List<PushMsg> res = new ArrayList<PushMsg>();
		Connection con = null;
		PreparedStatement updatePs = null;
		PreparedStatement selectPs = null;
		PreparedStatement cpHisPs = null;
		PreparedStatement delPs = null;
		ResultSet rs = null;
		try{				
			con = getConnection();
			//设置临时标记
			updatePs = con.prepareStatement("update t_device_push_msg set temp_flag = 1 limit ?");
			updatePs.setInt(1,maxLen);
			updatePs.execute();
			//取出数据
			selectPs = con.prepareStatement("select id,device_id,msg_type,msg_data,create_time from t_device_push_msg where temp_flag = 1");
			rs = selectPs.executeQuery();			
			while(rs.next()){
				PushMsg msg = new PushMsg();
				msg.setId(rs.getLong(1));
				msg.setDeviceId(rs.getString(2));
				msg.setMsgType(rs.getInt(3));
				msg.setMsgData(rs.getBytes(4));
				msg.setCreateTime(rs.getTimestamp(5));
				res.add(msg);
			}
			//写入history
			cpHisPs = con.prepareStatement("INSERT INTO t_device_push_msg_history(id,device_id,msg_type,msg_data)  SELECT d.id,d.device_id,d.msg_type,d.msg_data FROM t_device_push_msg d WHERE d.temp_flag = 1 ");
			cpHisPs.execute();
			//从发送表删除
			delPs = con.prepareStatement("delete from t_device_push_msg where temp_flag = 1");
			delPs.execute();
		}catch(Exception e){			
			throw e;
		}finally{
			close(null,updatePs);
			close(rs, selectPs);
			close(null,cpHisPs);
			close(null,delPs);
			closeConnection();
		}
		return res;
	}

}
