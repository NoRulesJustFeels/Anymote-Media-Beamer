/*
 * Copyright 2012 ENTERTAILION LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.entertailion.java.beamer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;

import com.entertailion.java.anymote.util.JavaPlatform;

/**
 * Platform-specific logic
 * @see https://github.com/entertailion/Anymote-for-Java
 * @author leon_nicholls
 *
 */
public class SwingPlatform extends JavaPlatform {

	public SwingPlatform() {

	}

	/**
	 * Get the network address.
	 * @return
	 */
	public Inet4Address getNetworAddress() {
		Inet4Address selectedInetAddress = null;
		try {
        	Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();

            while(list.hasMoreElements()) {
                NetworkInterface iface = list.nextElement();
                if(iface == null) continue;

                if(!iface.isLoopback() && iface.isUp()) {
                    Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
                    while (it.hasNext()) {
                        InterfaceAddress interfaceAddress = it.next();
                        if(interfaceAddress == null) continue;
                        InetAddress address = interfaceAddress.getAddress();
                        if (address instanceof Inet4Address) {
                        	if (address.getHostAddress().toString().charAt(0)!='0') {
                        		InetAddress networkAddress = interfaceAddress.getAddress();
                        		if (selectedInetAddress==null) {
                        			selectedInetAddress = (Inet4Address)networkAddress;
                        		} else if (iface.getName().startsWith("wlan") || iface.getName().startsWith("en")) {  // prefer wlan interface
                        			selectedInetAddress = (Inet4Address)networkAddress;
                        		}
                        	}
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }

        return selectedInetAddress;
	}

}
