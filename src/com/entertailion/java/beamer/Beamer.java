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

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.WindowConstants;

import com.entertailion.java.anymote.util.Log;

/**
 * Main class for Beamer app. Beam media/videos to Google TV devices using the Anymote protocol.
 * @see https://github.com/entertailion/Anymote-Media-Beamer
 * @author leon_nicholls
 *
 */
public class Beamer {
	private static final String LOG_TAG = "Beamer";
	private static BeamerFrame beamerFrame;
	
	/**
	 * Main entry point
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	/**
	 * Create the main window frame
	 */
	public static void createAndShowGUI() {
		Log.d(LOG_TAG, "createAndShowGUI");
		beamerFrame = new BeamerFrame();
		// change the default app icon; might not work for all platforms
		URL url = ClassLoader.getSystemResource("com/entertailion/java/beamer/resources/logo.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		beamerFrame.setIconImage(img);
		beamerFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		beamerFrame.setSize(420, 250);
		beamerFrame.setLocationRelativeTo(null);
		beamerFrame.setVisible(true);
	}

}
