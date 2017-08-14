package com.chh.ap.cs.push;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.dao.impl.PushMsgDao;
import com.chh.ap.cs.server.Server;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.util.Constant;

public class PushManager implements Server {

	private static final Logger log = LoggerFactory.getLogger(PushManager.class);

	public volatile boolean stopFlag = false;
	
//	private BlockingQueue<PushMsg> pushMsgQueue; // 当前消息队列
	private List<PushMsg> dataQueue = new LinkedList<PushMsg>();
	
    private static final int MAX_SIZE = 30;

    // BufferedMultiExportRepository和异步输出线程 锁
    private ReentrantLock lock = new ReentrantLock();

    // BufferedMultiExportRepository和异步输出线程 锁condition 如果dataQueue为空 则异步输出线程挂起
    private Condition empty = lock.newCondition();

    private Condition full = lock.newCondition();
    
    private PushThread pushThread;

    private PushMsgDao pushMsgDao;
	
    @Override
    public void start()  throws Exception {
		//线程push消息
		PushThread pt = new PushThread();
		pt.start();
		//线程读取数据库消息=》队列
		ReadPushThread rpt = new ReadPushThread(this);
		rpt.start();
		
		
	}
	
    void addQueue(PushMsg dataBlock) {
        if (this.pushThread == null) {
        	pushThread = new PushThread();
        	pushThread.start();
        }
        try {
            lock.lockInterruptibly();
            while (dataQueue.size() == MAX_SIZE) {
                full.await();
            }
            this.dataQueue.add(dataBlock);
            empty.signalAll();
        } catch (InterruptedException e) {
            full.signalAll();
            log.error("获取Lock异常", e);
        } finally {
            lock.unlock();
        }
    }
	
    
    class PushThread extends Thread {

    	
    	public void run(){
            this.setName("Push异步分发线程");
            log.debug("Push异步分发线程启动。");
            int distributedNum = 0;
            // 如果没有提交或者临时队列中仍然有数据 则线程一直运行
            PushMsg dataBlock = null;
            while (true) {
                try {
                    lock.lockInterruptibly();
                    while (dataQueue.isEmpty() && !stopFlag) {
                        empty.awaitNanos(100000000L);
                    }
                    if (!dataQueue.isEmpty()) {
                        dataBlock = dataQueue.remove(0);
                        full.signal();
                    }
                } catch (InterruptedException e) {
                    log.error("Push异步分发线程异常。", e);
                } finally {
                    lock.unlock();
                }
                /**
                 * <pre>
                 * 修改说明：
                 * 	这个地方要检测一下 dataBlock是否为null，
                 * 	否则commitFlag=true且dataQueue.isEmpty()时将会出错.
                 * 	distribute完后，dataBlock要置为null，否则按原先模式会造成最后一个dataBlock重复入库(1-N次)
                 * </pre>
                 */
                if (dataBlock != null) {
//                	TODO
                	pushMsg(dataBlock);
                    distributedNum++;
                    dataBlock = null;
                }
                if (stopFlag && dataQueue.isEmpty()) {
                    break;
                }
            }
            log.debug("Push异步分发线程完成,共分发数据{}", new Object[]{distributedNum});
        }
    }


	public void pushMsg(PushMsg msg) {
		IoSession session = SessionManager.getInstance().getSessionById(msg.getDeviceId());
		if(session!=null){
			ClientType clientType = (ClientType) session.getAttribute(Constant.SESSION_CLIENT_TYPE);
			clientType.getProtocolHandler().pushMsg(session, msg);
		} else {
			log.error("Push异步分发消息【ID：{}】失败,设备未连接服务器。", msg.getId());
		}
	}
	
	
	@Override
	public void beforeStart() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		stopFlag = true;
	}

	@Override
	public void afterStop() throws Exception {
		// TODO Auto-generated method stub
		
	}

    public PushMsgDao getPushMsgDao() {
        return pushMsgDao;
    }

    public void setPushMsgDao(PushMsgDao pushMsgDao) {
        this.pushMsgDao = pushMsgDao;
    }
}
