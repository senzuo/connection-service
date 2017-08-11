package com.chh.ap.cs.server;

/**
 * Created by Niow on 2016/6/24.
 */
public interface Server {

    public void beforeStart() throws Exception ;

    public void start()  throws Exception ;

    public boolean isRunning();

    public void stop()  throws Exception ;

    public void afterStop() throws Exception ;
}
