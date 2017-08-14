package com.chh.ap.cs.dao;

import com.chh.ap.cs.client.SessionManager.SessionInfo;

public interface IDeviceDao {

	public SessionInfo login(String deviceId) throws Exception;

	public void updateDeviceLogin(String devicdId) throws Exception;

	public void updateDeviceLost(String deviceId) throws Exception;
}
