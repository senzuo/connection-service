package com.chh.ap.cs.dao;

import java.util.List;

import com.chh.ap.cs.push.PushMsg;

public interface IPushMsgDao {

	public List<PushMsg> getPushMsg(int maxLen) throws Exception;
	
//	public void savePushedMsg(List<PushMsg> msgList) throws Exception;
}
