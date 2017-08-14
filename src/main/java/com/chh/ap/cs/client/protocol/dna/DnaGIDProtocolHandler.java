package com.chh.ap.cs.client.protocol.dna;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.chh.ap.cs.client.ClientType;
import com.chh.ap.cs.client.SessionManager;
import com.chh.ap.cs.client.protocol.dna.packet.DnaPacket;
import com.chh.ap.cs.util.ByteArrayUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chh.ap.cs.client.protocol.base.ProtocolHandler;
import com.chh.ap.cs.dao.impl.DeviceDao;
import com.chh.ap.cs.push.PushMsg;

/**
 * 迪娜盒子协议解析器
 *
 * @author fulr
 */
public class DnaGIDProtocolHandler implements ProtocolHandler {

    public static final Logger log = LoggerFactory.getLogger(DnaGIDProtocolHandler.class);

    public static Map<Byte, Byte> reqAndRespMap = new HashMap<Byte, Byte>();

    static {
        reqAndRespMap.put((byte) 0x00, (byte) 0x80);
        reqAndRespMap.put((byte) 0x01, (byte) 0x80);
        reqAndRespMap.put((byte) 0x02, (byte) 0x80);
        reqAndRespMap.put((byte) 0x03, (byte) 0x80);
        reqAndRespMap.put((byte) 0x04, (byte) 0xA4);//
        reqAndRespMap.put((byte) 0x05, (byte) 0x80);
        reqAndRespMap.put((byte) 0x06, (byte) 0x80);
        reqAndRespMap.put((byte) 0x07, (byte) 0x80);
        reqAndRespMap.put((byte) 0x08, (byte) 0x80);
        reqAndRespMap.put((byte) 0x09, (byte) 0x80);
        reqAndRespMap.put((byte) 0x0A, (byte) 0x80);
        reqAndRespMap.put((byte) 0x0B, (byte) 0x80);
        reqAndRespMap.put((byte) 0x0C, (byte) 0x80);
        reqAndRespMap.put((byte) 0x0D, (byte) 0x80);

        reqAndRespMap.put((byte) 0x0E, (byte) 0x80);
        reqAndRespMap.put((byte) 0x0F, (byte) 0x80);
        reqAndRespMap.put((byte) 0x95, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA0, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA1, (byte) 0x80);
        reqAndRespMap.put((byte) 0xB0, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA2, (byte) 0xA4);//
        reqAndRespMap.put((byte) 0xA3, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA4, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA5, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA6, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA7, (byte) 0x80);

        reqAndRespMap.put((byte) 0xA8, (byte) 0x80);
        reqAndRespMap.put((byte) 0xA9, (byte) 0x80);
        reqAndRespMap.put((byte) 0xAA, (byte) 0x80);
        reqAndRespMap.put((byte) 0xAB, (byte) 0x80);
        reqAndRespMap.put((byte) 0xAC, (byte) 0x80);
        reqAndRespMap.put((byte) 0x10, (byte) 0x80);

        reqAndRespMap.put((byte) 0x93, (byte) 0x83);//
        reqAndRespMap.put((byte) 0x94, (byte) 0x84);//
    }


    private DeviceDao deviceDao = new DeviceDao();

    @Override
    public MessageDecoderResult decodable(byte[] ba) {
        short dataLen = ByteArrayUtil.getUSHORT2(ba, DnaPacket.INDEX_OF_DATA_LEN);
        int packetLen = dataLen + DnaPacket.CLIENT_FRAME_LENGTH;
        if (ba.length >= packetLen) {
            return MessageDecoderResult.OK;
        }
        return MessageDecoderResult.NEED_DATA;
    }

    @Override
    public MessageDecoderResult decode(IoSession session, IoBuffer in,
                                       ProtocolDecoderOutput out) throws Exception {
        //数据帧结构体
        byte[] msgFrame = new byte[DnaPacket.CLIENT_FRAME_LENGTH];
        in.get(msgFrame);
        //业务数据包
        short dataLen = ByteArrayUtil.getUSHORT2(msgFrame, DnaPacket.INDEX_OF_DATA_LEN);
        int packetLen = dataLen + DnaPacket.CLIENT_FRAME_LENGTH;
        //完整的数据包
        byte[] msgPacket = new byte[packetLen];

        System.arraycopy(msgFrame, 0, msgPacket, 0, msgFrame.length);
        in.get(msgPacket, msgFrame.length, dataLen);

        out.write(msgPacket);
        return MessageDecoderResult.OK;
    }

    @Override
    public SessionManager.SessionInfo login(IoSession ioSession, byte[] message) {
        long deviceId = ByteArrayUtil.getULONG6(message, 4);

//		SessionInfo sess = new SessionInfo(1L,String.valueOf(deviceId));
        SessionManager.SessionInfo sess = null;
        try {
            sess = deviceDao.login(String.valueOf(deviceId));
            if (sess == null) {
                log.info("设备[ID:{}]未入库，登录失败。", deviceId);
            }
        } catch (Exception e) {
            log.error("设备[ID:{}]登录失败，发生异常。", deviceId, e);
        }
        return sess;
    }

    @Override
    public void messageHandler(IoSession ioSession, byte[] message) {
        byte cmdId = message[3];

        //设备主动上行消息，才响应
        Byte respCmd = reqAndRespMap.get(cmdId);
        if (respCmd != null) {
            //响应client
            short dataLen;

            if (respCmd.byteValue() == (byte) 0xA4) {
                dataLen = 3;//报警数据
            } else if (respCmd.byteValue() == (byte) 0x83) {
//				dataLen = 1;//2.7.1	车机获取设置参数 
                dataLen = 28;
                //TODO
            } else if (respCmd.byteValue() == (byte) 0x84) {
                dataLen = 6;//2.8.1	车机获取零时区时间
            } else {
                dataLen = 1;
            }
            int packetLen = dataLen + DnaPacket.CLIENT_FRAME_LENGTH;
            byte[] res = new byte[packetLen];
            int index = 0;
            res[index++] = message[0];
            res[index++] = message[1];
            res[index++] = message[2];
//			if(cmdId==0x04||cmdId==0xA2){
//				res[index++] = (byte) 0xA4;//报警数据
//			} else {
//				res[index++] = (byte) 0x80;
//			}
            res[index++] = respCmd;

            System.arraycopy(message, index, res, index, 6);
            index += 6;
            ByteArrayUtil.setUSHORT2(res, index, dataLen);
            index += 2;

            //set data
            if (respCmd.byteValue() == (byte) 0xA4) {
                System.arraycopy(message, 38, res, index, 3);
                index += 3;
            } else if (respCmd.byteValue() == (byte) 0x83) {
                //2.7.1	车机获取设置参数
                //TODO
                SessionManager.SessionInfo sessInfo = SessionManager.getInstance().getSessionInfo(ioSession);
                String lpn = "京A11111";
                short color = 1;
                long deviceId = Long.parseLong(sessInfo.getSn());
                try {
                    byte[] lpnBytes = lpn.getBytes("GBK");
                    System.arraycopy(lpnBytes, 0, res, index, lpnBytes.length);
                    index += 20;

                    ByteArrayUtil.setUSHORT2(res, index, color);
                    index += 2;

                    ByteArrayUtil.setULONG6(res, index, deviceId);
                    index += 6;
                } catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
                    log.error("获取车机获取设置参数失败，车牌号格式化失败。");
                }

//				res[index++] = 0;
            } else if (respCmd.byteValue() == (byte) 0x84) {
                //2.8.1	车机获取零时区时间
                TimeZone timeZone = TimeZone.getTimeZone("GMT+00:00");
                Calendar calendar = Calendar.getInstance(timeZone);

                res[index++] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
                res[index++] = (byte) calendar.get(Calendar.MINUTE);
                res[index++] = (byte) calendar.get(Calendar.SECOND);
                res[index++] = (byte) (calendar.get(Calendar.YEAR) - 2000);
                res[index++] = (byte) calendar.get(Calendar.MONTH);
                res[index++] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            } else {
                res[index++] = message[3];
            }


            res[index++] = DnaPacket.generateXOR(res, 2, packetLen - 4);
            res[index] = (byte) 0xFB;

            ioSession.write(res);
        }

    }


    @Override
    public void pushMsg(IoSession ioSession, PushMsg msg) {
        Byte cmdId = null;
        switch (msg.getMsgType()) {
            case PushMsg.MSG_TYPE_QUERY_CAN_PARAM: {
                //
                cmdId = 0x32;
                break;
            }
            case PushMsg.MSG_TYPE_SET_CAN_PARAM: {
                cmdId = 0x51;
                break;
            }
        }
        if (cmdId != null) {
            DnaPacket p = new DnaPacket(ioSession, cmdId);
            //TODO
            //设置消息体
            if (msg.getMsgData() != null) {
                p.setData(msg.getMsgData());
            }
            ioSession.write(p.toBytes());
        } else {
            //命令不支持
            log.error("设备[ID:{}]消息[ID:{}]推送失败，消息不支持。", msg.getDeviceId(), msg.getId());
        }

    }

}
