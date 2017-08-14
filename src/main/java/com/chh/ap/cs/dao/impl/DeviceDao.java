package com.chh.ap.cs.dao.impl;

import java.sql.*;

import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.dao.BaseDao;
import com.chh.ap.cs.dao.IDeviceDao;
import com.chh.ap.cs.util.DBUtil;

public class DeviceDao extends BaseDao implements IDeviceDao {

    @Override
    public SessionManager.SessionInfo login(String deviceId) throws Exception {
        SessionManager.SessionInfo sess = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("select id,status from t_device where id=?");
            ps.setString(1, deviceId);
            rs = ps.executeQuery();
            while (rs.next()) {
                sess = new SessionManager.SessionInfo(rs.getString(1));
                sess.setSn(rs.getString(1));
                sess.setStatus(rs.getInt(2));
                break;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtil.close(null, ps, con);
        }
        return sess;
    }

    @Override
    public void updateDeviceLogin(String deviceId) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("update t_device set status=?,access_time=? where id=?");
            ps.setInt(1, SessionManager.SessionInfo.STATUS_ONLINE);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, deviceId);
            ps.execute();
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtil.close(null, ps, con);
        }
    }

    @Override
    public void updateDeviceLost(String deviceId) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("update t_device set status=?,lost_time=? where id=?");
            ps.setInt(1, SessionManager.SessionInfo.STATUS_LOST);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, deviceId);
            ps.execute();
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtil.close(null, ps, con);
        }
    }
}
