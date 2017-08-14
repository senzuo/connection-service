package com.chh.ap.cs.push;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.dao.impl.PushMsgDao;

/**
 * 读取数据库中push数据到队列
 *
 * @author fulr
 */
public class ReadPushThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ReadPushThread.class);

    private PushManager mgr = null;

    private PushMsgDao pushMsgDao;

    private static final int MAX_LEN = 100;

    public ReadPushThread(PushManager mgr) {
        this.mgr = mgr;
        this.pushMsgDao = mgr.getPushMsgDao();
    }

    public void run() {
        log.info("读取push消息线程启动。");
        while (!mgr.stopFlag) {
            try {
                List<PushMsg> list = pushMsgDao.getPushMsg(MAX_LEN);
                if (list != null && list.size() > 0) {
                    log.info("读取push消息条数：{}", list.size());
                    for (PushMsg item : list) {
                        mgr.addQueue(item);
                    }
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(5000);
                }

            } catch (Exception e) {
                log.info("读取push消息发生异常。", e);
            }
        }
        log.info("读取push消息线程结束。");
    }

    public PushMsgDao getPushMsgDao() {
        return pushMsgDao;
    }

    public void setPushMsgDao(PushMsgDao pushMsgDao) {
        this.pushMsgDao = pushMsgDao;
    }
}
