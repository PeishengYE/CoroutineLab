package com.example.android.kotlincoroutines.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanIpAddress {

    public static Boolean isDone = false;


    public static Map<String, String> getMacIPTable(){
        isDone = false;
        ArrayList<InetAddress> ret = new ArrayList<InetAddress>();
        Map<String, String> macIPTable = new HashMap<>();

        int LoopCurrentIP = 0;

        String IPAddress = getIPAddress(true);
        String[] myIPArray = IPAddress.split("\\.");
        InetAddress currentPingAddr;

        for (int i = 0; i <= 255; i++) {
                try {
                    // build the next IP address
                    currentPingAddr = InetAddress.getByName(myIPArray[0] + "." +
                            myIPArray[1] + "." +
                            myIPArray[2] + "." +
                            Integer.toString(LoopCurrentIP));
                    String cureentIP = currentPingAddr.toString();
                    // 50ms Timeout for the "ping"
                    if (currentPingAddr.isReachable(50)) {

                        ret.add(currentPingAddr);
                        cureentIP = currentPingAddr.toString().substring(1);
                        Log.d("scanIpAddress: ",cureentIP + " is alive");
                        String mac = getMacFromArpCache(cureentIP);
                        Log.d("scanIpAddress: ",cureentIP + " with MAC: " + mac);
                        if ( mac != null )
                             macIPTable.put(mac,cureentIP);
                    }
                } catch (UnknownHostException ex) {
                } catch (IOException ex) {
                }
            LoopCurrentIP++;
        }
        isDone = true;
        return macIPTable;
  }

    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
//                Log.d("getMacFromArpCache: ","/proc/net/arp: " + line);
                //192.168.106.252  0x1         0x2         00:30:18:cf:35:d9     *        wlan0
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    Log.d("getMacFromArpCache: ","/proc/net/arp: " + splitted[3]);
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

}
