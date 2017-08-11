package com.chh.ap.cs.handler;

import com.chh.ap.cs.client.ClientType;

/**
 * Created by Niow on 2016/6/24.
 */
public class ClientRecognizerImpl implements ClientRecognizer {

    @Override
    public int getClient(byte[] msg) {
//        return ClientRecognizer.CLIENT_TYPE_OBD_YUANZHEN;
        if (msg != null) {
            if (msg.length >= 2 && msg[0] == (byte) 0XFA && msg[1] == (byte) 0XFA) {
                return ClientType.CLIENT_TYPE_OBD_DNA;
            }
//    		//元征的 （无帧头序列号 判断方法 ff49434152）
//    		else if (msg.length>=5&&msg[0]==(byte)0xFF&&msg[1]=='I'&&msg[2]=='C'&&msg[3]=='A'&&msg[4]=='R'){
//    			return ClientType.CLIENT_TYPE_OBD_YUANZHEN;
//    		}
            //序号号都是以 97 开头 （02393733343930303030303334ff49434152）
            else if (msg.length >= 18 && msg[0] == (byte) 0x02 && msg[1] == (byte) 0x39 && msg[13] == (byte) 0xFF && msg[14] == 'I' && msg[15] == 'C' && msg[16] == 'A' && msg[17] == 'R') {
                return ClientType.CLIENT_TYPE_OBD_YUANZHEN;
            } else if (msg.length >= 2 && msg[0] == 0X40 && msg[1] == 0X40) {
                return ClientType.CLIENT_TYPE_OBD_HTWX;
            }
        }
        return ClientType.CLIENT_TYPE_UNKNOWN;
    }

}
