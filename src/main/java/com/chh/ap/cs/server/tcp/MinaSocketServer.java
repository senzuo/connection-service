package com.chh.ap.cs.server.tcp;

import java.net.InetSocketAddress;
import java.util.Map;

import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.server.Server;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.chh.ap.cs.handler.ClientRecognizer;
import com.chh.ap.cs.handler.protocol.MinaProtocolCodecFactory;

/**
 * MINA服务端
 * MINA设计原则是将网络连接管理和客户端、服务器端消息代码分离出来。
 *
 * @author Administrator
 */
public class MinaSocketServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(MinaSocketServer.class);

    //tcp服务器端口
    private String port;

    //服务器session超时时间
    private String sessionTime;

    private IoHandler ioHandler;
    
    /**
     * 设备识别器
     */
    private static ClientRecognizer clientRecognizer;
    /**
     * 设备类型：设备处理器
     */
    private static Map<Integer, ClientType> clientTypeMap;

    //启动tcp服务
    public void start() throws Exception {
        log.info("Tcp服务器开始启动");
        //第一步创建NioSocketAcceptor对象
        NioSocketAcceptor acceptor = new NioSocketAcceptor();

        //第三步获取MINA所有的拦截器，并且增加一个新拦截器
        //第一个参数codec就是拦截器的名称，可以随便命名。
        //ProtocolCodecFilter是MINA比较常用的拦截器，作用是将二进制数据和对象之间进行转换
        //TextLineCodecFactory是MINA内置的文本消息加解码的类
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaProtocolCodecFactory()));
		acceptor.getFilterChain().addLast("executor", new ExecutorFilter());

        //acceptor.getSessionConfig().setTcpNoDelay(true);

        //设置session什么情况下进入空闲状态
        //设置如果5秒钟，客户端没有向服务器发送消息、或者服务器也没有向客户端发送消息，session会话就进入空闲状态
        acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, Integer.parseInt(sessionTime));

        //第二步设置自己实现的Handler
        acceptor.setHandler(ioHandler);

        //第四步调用bind方法，设置端口9898
        acceptor.bind(new InetSocketAddress(Integer.parseInt(port)));

        log.info("Tcp服务器启动,端口[{}]", port);
    }

    @Override
    public void afterStop() throws Exception {
        log.info("Tcp服务器启动完毕");
    }

    @Override
    public void beforeStart() throws Exception {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void stop() throws Exception {

    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(String sessionTime) {
        this.sessionTime = sessionTime;
    }

    public IoHandler getIoHandler() {
        return ioHandler;
    }

    @Autowired
    public void setIoHandler(IoHandler ioHandler) {
        this.ioHandler = ioHandler;
    }

	public static ClientRecognizer getClientRecognizer() {
		return clientRecognizer;
	}

	public void setClientRecognizer(ClientRecognizer clientRecognizer) {
		MinaSocketServer.clientRecognizer = clientRecognizer;
	}

	public static Map<Integer, ClientType> getClientTypeMap() {
		return clientTypeMap;
	}

	public void setClientTypeMap(Map<Integer, ClientType> clientTypeMap) {
		MinaSocketServer.clientTypeMap = clientTypeMap;
	}
    
    
}
