package com.chh.ap.cs.handler;

/**
 * 接入服务器的客户端类型判断器
 *
 * Created by Niow on 2016/6/22.
 */
public interface ClientRecognizer {

//    /**
//     * 未知
//     */
//    public static final int CLIENT_TYPE_UNKNOWN = 0;
//
//    /**
//     * OBD盒子：元征
//     */
//    public static final int CLIENT_TYPE_OBD_YUANZHEN = 1;
//
//    /**
//     * OBD盒子:迪娜
//     */
//    public static final int CLIENT_TYPE_OBD_DNA = 2;
//    
//    /**
//     * 元征OBD盒子最小帧长度
//     */
//    public static final int CLIENT_TYPE_OBD_YUANZHEN_FRAME_MIN_LENGTH=25;
//    
//    /**
//     * 迪娜OBD盒子最小帧长度
//     */
//    public static final int CLIENT_TYPE_OBD_DNA_FRAME_MIN_LENGTH=14;
    
    /**
     * 通过接收到的第一条消息来判断接入客户端的类型
     *
     * @param message 客户端传入服务端的第一条消息
     * @return 客户端类型
     */
    public int getClient(byte[] message);

}
