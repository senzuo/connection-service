package com.chh.ap.cs.simulator;

import com.chh.ap.cs.simulator.device.Device;
import com.chh.ap.cs.simulator.device.HtwxOBD;

import java.io.IOException;

/**
 * Created by niow on 2017/8/14.
 */
public class Simulator {

    public static void main(String[] args) {
        Device device = new HtwxOBD();
        try {
            device.connect();
            device.sendLogin();
            Thread.sleep(10000L);
            device.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
