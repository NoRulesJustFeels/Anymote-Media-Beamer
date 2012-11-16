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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.entertailion.java.anymote.client.AnymoteClientService;
import com.entertailion.java.anymote.client.AnymoteSender;
import com.entertailion.java.anymote.client.ClientListener;
import com.entertailion.java.anymote.client.DeviceSelectListener;
import com.entertailion.java.anymote.client.InputListener;
import com.entertailion.java.anymote.client.PinListener;
import com.entertailion.java.anymote.connection.TvDevice;
import com.entertailion.java.anymote.connection.TvDiscoveryService;
import com.entertailion.java.anymote.util.Log;
import com.google.anymote.Key.Code;

/**
 * Main window frame for the Beamer application
 * @author leon_nicholls
 *
 */
public class BeamerFrame extends JFrame  implements ActionListener, ClientListener, InputListener {
	private static final String LOG_TAG = "BeamerFrame";
	private static final String PROPERTY_LAST_DEVICE = "device.last";
	private static final String PROPERTY_MANUAL_IPS = "ip.manual";
	private int port = EmbeddedServer.HTTP_PORT;
	private EmbeddedServer embeddedServer;
	private AnymoteClientService anymoteClientService;
	private TvDiscoveryService tvDiscoveryService;
	private List<TvDevice> devices;
	private List<TvDevice> manualDevices = new ArrayList<TvDevice>();
	private AnymoteSender anymoteSender;
	private JComboBox deviceList;
    private JDialog progressDialog;
    private SwingPlatform swingPlatform = new SwingPlatform();
    private JButton refreshButton, settingsButton, playButton, pauseButton, stopButton, okButton, rewindButton, fastForwardButton;
    private ResourceBundle resourceBundle;
    private TvDevice manualDevice;
    private boolean startup = true;
    private boolean manualIpConnection = false;
	
	public BeamerFrame() {
		super();
		
		Locale locale = Locale.getDefault();
	    resourceBundle = ResourceBundle.getBundle("com/entertailion/java/beamer/resources/resources", locale);
		
		JPanel listPane = new JPanel();
		// show list of Google TV devices detected on the local network
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		JPanel devicePane = new JPanel();
		devicePane.setLayout(new BoxLayout(devicePane, BoxLayout.LINE_AXIS));
		deviceList = new JComboBox();
		deviceList.addActionListener(this);
		devicePane.add(deviceList);
		URL url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/refresh.png");
		ImageIcon icon = new ImageIcon(url, resourceBundle.getString("button.refresh"));
		refreshButton = new JButton(icon);
		refreshButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	// refresh the list of devices
            	deviceList.setSelectedIndex(0);
            	discoverDevices();
            }
        }); 
		refreshButton.setToolTipText(resourceBundle.getString("button.refresh"));
        devicePane.add(refreshButton);
        url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/settings.png");
		icon = new ImageIcon(url, resourceBundle.getString("button.manualIp"));
		settingsButton = new JButton(icon);
		settingsButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	// display manual IP dialog
            	String ip = JOptionPane.showInputDialog(BeamerFrame.this, resourceBundle.getString("input.ip"), "", 1);
				if (ip != null && anymoteClientService!=null) {
					int pos = 1;
		    		boolean found = false;
		    		// already in device list?
		    		for(TvDevice device:devices) {
			    		if (device.getAddress().getHostAddress().equals(ip)) {
			    			deviceList.setSelectedIndex(pos);
			    			found = true;
			    			break;
			    		}
			    		pos++;
			    	}	
		    		if (!found) {
						try {
							Inet4Address address = (Inet4Address) InetAddress
									.getByName(ip);
							manualDevice = new TvDevice(ip, address);
							manualIpConnection = true;
							anymoteClientService.connectDevice(manualDevice);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(BeamerFrame.this, resourceBundle.getString("invalid.ip"), resourceBundle.getString("dialog.error"), JOptionPane.ERROR_MESSAGE);
						}
		    		}
				}
            }
        });   
		settingsButton.setToolTipText(resourceBundle.getString("button.manualIp"));
        devicePane.add(settingsButton);
		listPane.add(devicePane);
		listPane.add(DragHereIcon.makeUI(this));
		
		// panel of playback buttons
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/ok.png");
		icon = new ImageIcon(url, resourceBundle.getString("button.ok"));
		okButton = new JButton(icon);
		okButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	sendKeyPress(Code.KEYCODE_DPAD_CENTER);
            }
        });   
		buttonPane.add(okButton);
		url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/rewind.png");
		icon = new ImageIcon(url, "Rewind");
		rewindButton = new JButton(icon);
		rewindButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	sendKeyPress(Code.KEYCODE_MEDIA_REWIND);
            }
        });   
		buttonPane.add(rewindButton);
		url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/play.png");
		icon = new ImageIcon(url, resourceBundle.getString("button.play"));
		playButton = new JButton(icon);
		playButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	sendKeyPress(Code.KEYCODE_MEDIA_PLAY);
            }
        });   
		buttonPane.add(playButton);
		url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/pause.png");
		icon = new ImageIcon(url, resourceBundle.getString("button.pause"));
		pauseButton = new JButton(icon);
		pauseButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	sendKeyPress(Code.KEYCODE_PAUSE);
            }
        });   
		buttonPane.add(pauseButton);
		url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/stop.png");
		icon = new ImageIcon(url, resourceBundle.getString("button.stop"));
		stopButton = new JButton(icon);
		stopButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	sendKeyPress(Code.KEYCODE_MEDIA_STOP);
            }
        });   
		buttonPane.add(stopButton);
		url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/fastforward.png");
		icon = new ImageIcon(url, resourceBundle.getString("button.fastForward"));
		fastForwardButton = new JButton(icon);
		fastForwardButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
            	sendKeyPress(Code.KEYCODE_MEDIA_FAST_FORWARD);
            }
        });   
		buttonPane.add(fastForwardButton);
		listPane.add(buttonPane);
		getContentPane().add(listPane);
		
		createProgressDialog();
		startWebserver();
		startAnymoteService();
		discoverDevices();
	}
	
	/**
	 * Start a web server to serve the videos to the media player on the Google TV device
	 */
	private void startWebserver() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean started = false;
		        while (!started) {
		            try {
		                embeddedServer = new EmbeddedServer(port);
		                Log.d(LOG_TAG, "Started web server on port "+port);
		                started = true;
		            } catch (IOException ioe) {
		            	ioe.printStackTrace();
		                port++;
		            } catch (Exception ex) {
		                break;
		            }
		        }
			}
			
		}).start();
	}
	
	/**
	 * Discover Google TV devices on the local network
	 * @see https://github.com/entertailion/Anymote-for-Java
	 */
	private void discoverDevices() {
		tvDiscoveryService = TvDiscoveryService.getInstance(swingPlatform);
        // discovering devices can take time, so do it in a thread
        new Thread(new Runnable() {
        	public void run() {
        		showProgressDialog(resourceBundle.getString("progress.discoveringDevices"));
        		devices = tvDiscoveryService.discoverTvs();
        		deviceList.removeAllItems();
                deviceList.addItem(resourceBundle.getString("devices.select"));  // no selection option
        		for(TvDevice device:devices) {
        			deviceList.addItem(device.getName()+" / "+device.getAddress().getHostName());
        		}
        		
        		Properties properties = loadProperties();
        		// add the manual IPs to the device list
    			for(TvDevice device:manualDevices) {
    				boolean alreadyFound = false;
    				for(TvDevice foundDevice:devices) {
    					if (foundDevice.getAddress().getHostAddress().equals(device.getAddress().getHostAddress())) {
    						alreadyFound = true;
    						break;
    					}
            		}
    				if (!alreadyFound) {
						deviceList.addItem(device.getAddress().getHostName());
						devices.add(device);
					}
    			}
    			
    			deviceList.invalidate();
    			hideProgressDialog();
    			
    			// automatically connect to the last connected device
        		if (startup) {
        			startup = false;
        			String lastDevice = properties.getProperty(PROPERTY_LAST_DEVICE);
        			if (lastDevice!=null) {
    		    		int pos = 1;
    		    		for(TvDevice device:devices) {
    			    		if (device.getAddress().getHostAddress().equals(lastDevice)) {
    			    			deviceList.setSelectedIndex(pos);
    			    			break;
    			    		}
    			    		pos++;
    			    	}	
        			}
        		}
        	}
        }).start();
	}
	
	/**
	 * Start the Anymote protocol client
	 * @see https://github.com/entertailion/Anymote-for-Java
	 */
	private void startAnymoteService() {
		// start in thread since creating the keystore can take time...
		new Thread(new Runnable() {
			public void run() {
				anymoteClientService = AnymoteClientService.getInstance(swingPlatform);
				anymoteClientService.attachClientListener(BeamerFrame.this);  // client service callback
				anymoteClientService.attachInputListener(BeamerFrame.this);  // user interaction callback
			}
		}).start();
	}
	
	/**
	 * Create a progress indicator
	 */
	private void createProgressDialog() {
		progressDialog = new JDialog(this, resourceBundle.getString("progress.dialog"), true);
	    JProgressBar progressBar = new JProgressBar(0, 100);
	    progressBar.setStringPainted(true);
	    progressBar.setIndeterminate(true);
	    progressDialog.add(BorderLayout.CENTER, progressBar);
	    progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	    progressDialog.setSize(300, 75);
	    progressDialog.setLocationRelativeTo(this);
	}
	
	/**
	 * Show the progress indicator with a title message
	 * @param message
	 */
	private void showProgressDialog(final String message) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	progressDialog.setLocationRelativeTo(BeamerFrame.this); // just in case window frame moved
            	progressDialog.setTitle(message);
            	progressDialog.setVisible(true);
            	//progressDialog.toFront();
            }
        });
	}
	
	/**
	 * Hide the progress indicator
	 */
	private void hideProgressDialog() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	progressDialog.setVisible(false);
            }
        });
	}
	
	/**
	 * Event handler for device dropdown list selection
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (manualIpConnection) {
			manualIpConnection = false;
			return;  // ignore selection due to manual ip
		}
        JComboBox cb = (JComboBox)e.getSource();
        int pos = cb.getSelectedIndex();
        // when device is selected, attempt to connect
		// if device hasn't been paired before, the PIN pairing dialog will be displayed
		if (anymoteClientService!=null) {
			if (devices!=null && pos>0) {
				final TvDevice device = devices.get(pos-1);
				if (anymoteClientService.getCurrentDevice()!=null) {
					anymoteClientService.cancelConnection();
				}
				anymoteClientService.connectDevice(device);
			} 
		}
    }
	
	/**
     * ClientListener callback when attempting a connecion to a Google TV device
     * @see com.entertailion.java.anymote.client.ClientListener#attemptToConnect(com.entertailion.java.anymote.connection.TvDevice)
     */
    public void attemptToConnect(TvDevice device) {
    	Log.d(LOG_TAG, "attemptToConnect");
    	showProgressDialog(resourceBundle.getString("progress.connecting"));
	}

    /** 
	 * ClientListener callback when Anymote is conneced to a Google TV device
	 * @see com.entertailion.java.anymote.client.ClientListener#onConnected(com.entertailion.java.anymote.client.AnymoteSender)
	 */
	public void onConnected(final AnymoteSender anymoteSender) {
		this.anymoteSender = anymoteSender;
		Log.d(LOG_TAG, "onConnected");
		hideProgressDialog();
	    if (anymoteSender != null) {
	    	Log.d(LOG_TAG, "Connection successful");
	    	if (manualIpConnection) {
	    		TvDevice currentDevice = anymoteClientService.getCurrentDevice();
	    		if (currentDevice!=null) {
		    		devices.add(currentDevice);
		    		manualDevices.add(currentDevice);
		    		deviceList.addItem(currentDevice.getAddress().getHostName());
		    		int pos = 1;
		    		for(TvDevice device:devices) {
			    		if (device.getAddress().getHostAddress().equals(currentDevice.getAddress().getHostAddress())) {
			    			deviceList.setSelectedIndex(pos);
			    			break;
			    		}
			    		pos++;
			    	}	
	    		}
	    	}
	    	
	    	storeProperties();
	    } else {
	    	Log.d(LOG_TAG, "Connection failed");
	    }
	}
	
	/**
	 * Send a keypress to the Google TV device using Anymote
	 * @param keycode
	 */
	private void sendKeyPress(Code keycode) {
		Log.d(LOG_TAG, "sendKeyPress="+keycode);
		if (keycode!=null && anymoteSender!=null) {
			try {
				anymoteSender.sendKeyPress(keycode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Send a uri to the Google TV device using Anymote
	 * @param keycode
	 */
	protected void sendMediaUrl(String file) {
		Log.d(LOG_TAG, "sendMediaUrl="+file);
		if (file!=null && anymoteSender!=null) {
			try {
				int pos = file.lastIndexOf('.');
				String extension = "";
				if (pos>-1) {
					extension = file.substring(pos);
				}
				String url = "http://"+swingPlatform.getNetworAddress().getHostAddress()+":"+port+"/video"+extension;
				//anymoteSender.sendUrl(url);  // GTV Chrome media player (unreliable)
				
				// standard Android intent string to invoke the default media player
				// requires media player to be installed like GTVBox Video Player
				anymoteSender.sendUrl("intent://"+swingPlatform.getNetworAddress().getHostAddress()+":"+port+"/video"+extension+"#Intent;scheme=http;action=android.intent.action.VIEW;type=video/"+extension+";end");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ClientListener callback when the Anymote service is disconnected from the Google TV device
	 * @see com.entertailion.java.anymote.client.ClientListener#onDisconnected()
	 */
	public void onDisconnected() { 
		this.anymoteSender = null;
		Log.d(LOG_TAG, "onDisconnected");
		hideProgressDialog();
	}

	/**
	 * ClientListener callback when the attempted connection to the Google TV device failed
	 * @see com.entertailion.java.anymote.client.ClientListener#onConnectionFailed()
	 */
	public void onConnectionFailed() {
		this.anymoteSender = null;
		Log.d(LOG_TAG, "onConnectionFailed");
		hideProgressDialog();
	}
	
	/** 
	 * InputListener callback for feedback on starting the device discovery process
	 * @see com.entertailion.java.anymote.client.InputListener#onDiscoveringDevices()
	 */
	public void onDiscoveringDevices() {
		Log.d(LOG_TAG, "onDiscoveringDevices");
		showProgressDialog(resourceBundle.getString("progress.discoveringDevices"));
	}
	
	/** 
	 * InputListener callback when a Google TV device needs to be selected
	 * @see com.entertailion.java.anymote.client.InputListener#onSelectDevice(java.util.List, com.entertailion.java.anymote.client.DeviceSelectListener)
	 */
	public void onSelectDevice(final List<TvDevice> trackedDevices, final DeviceSelectListener deviceSelectListener) {
		Log.d(LOG_TAG, "onSelectDevice");
		hideProgressDialog();
		// nothing to do; devices are selected in spinner
	}
	
	/**
	 * InputListener callback when PIN required to pair with Google TV device
	 * @see com.entertailion.java.anymote.client.InputListener#onPinRequired(com.entertailion.java.anymote.client.PinListener)
	 */
	public void onPinRequired(final PinListener pinListener) {
		Log.d(LOG_TAG, "onPinRequired");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	String pin = JOptionPane.showInputDialog(BeamerFrame.this, resourceBundle.getString("input.pin"), "", 1);
                if (pin != null) {
                	pinListener.onSecretEntered(pin);
                } else {
                	pinListener.onCancel();
                	hideProgressDialog();
                	deviceList.setSelectedIndex(0);
                }
            }
        });
	}
	
	private Properties loadProperties() {
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream("config.properties"));
			
			manualDevices.clear();
			String manualIps = prop.getProperty(PROPERTY_MANUAL_IPS);
			if (manualIps!=null) {
				String[] devices = manualIps.split(";");
				for(String device:devices) {
					Inet4Address address = (Inet4Address) InetAddress.getByName(device);
					manualDevices.add(new TvDevice(device, address));
				}
			}
		} catch (Exception ex) {
		}
		return prop;
	}

	private void storeProperties() {
		Properties prop = new Properties();

		try {
			prop.setProperty(PROPERTY_LAST_DEVICE, anymoteClientService.getCurrentDevice().getAddress().getHostAddress());
			if (manualDevice!=null) {
				manualDevices.add(manualDevice);
				manualDevice = null;
			}
			String manualIps = null;
			for(TvDevice device:manualDevices) {
				if (manualIps==null) {
					manualIps = device.getAddress().getHostAddress();
				} else {
					manualIps = manualIps + ";" + device.getAddress().getHostAddress();
				}
			}
			if (manualIps!=null) {
				prop.setProperty(PROPERTY_MANUAL_IPS, manualIps);
			}
			prop.store(new FileOutputStream("config.properties"), null);

		} catch (Exception ex) {
		}
	}

}
