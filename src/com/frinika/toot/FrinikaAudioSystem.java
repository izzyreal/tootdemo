/*
 * Created on Feb 11, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.toot;

import java.util.List;
import java.util.Observer;
import java.util.Observable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import uk.org.toot.audio.server.*;
import uk.org.toot.swingui.audioui.serverui.*;

import com.frinika.global.FrinikaConfig;
import com.frinika.toot.javasoundmultiplexed.*; // !!!

public class FrinikaAudioSystem {

	// order is important !!!
	private static SwitchedAudioClient mixerSwitch = new SwitchedAudioClient();

	private static AudioServer audioServer = getAudioServer();

	private static AudioServerConfiguration serverConfig;

	private static Object thief = null;

	private static int bufferSize;

	private static IOAudioProcess defaultOut;

	private static JFrame configureFrame;

	public static AudioServer getAudioServer() {
		if (audioServer != null)
			return audioServer;

		boolean multiplexIO = FrinikaConfig
		.getPropertyBoolean("multiplexed_audio");

		if (!multiplexIO) {
			audioServer = new MultiIOJavaSoundAudioServer();
		} else {
			System.out
			.println(" WARNING USING EXPERIMENTAL MULTIPLEXED AUDIO SERVER ");
			MultiplexedJavaSoundAudioServer s = new MultiplexedJavaSoundAudioServer();
			audioServer = s;
			configureMultiplexed(s);
		}

		serverConfig = AudioServerServices.createServerConfiguration(audioServer);
		serverConfig.addObserver(new Observer() {
			public void update(Observable obs, Object obj) {
				saveServerConfig();
			}
		});
		loadServerConfigPost();
		bufferSize = audioServer.createAudioBuffer("dummy").getSampleCount();
		audioServer.setClient(mixerSwitch);
		return audioServer;
	}

	// !!!
	private static void configureMultiplexed(
			MultiplexedJavaSoundAudioServer s) {
		List<String> list = s.getOutDeviceList();
		Object a[] = new Object[list.size()];
		a = list.toArray(a);

		Object selectedValue = JOptionPane.showInputDialog(null,
				"Select audio output", "output",
				JOptionPane.INFORMATION_MESSAGE, null, a, a[0]);

		s.setOutDevice((String) selectedValue);

		list = s.getInDeviceList();
		list.add(0, "NONE");
		a = new Object[list.size()];
		a = list.toArray(a);

		selectedValue = JOptionPane.showInputDialog(null, "Select audio input",
				"input", JOptionPane.INFORMATION_MESSAGE, null, a, a[0]);
		
		if (!((String) selectedValue).equals("NONE"))
			s.setInDevice((String) selectedValue);

	}

	/**
	 * 
	 * sets a new mixer.
	 * 
	 * @param mixer
	 *            new client for the server
	 * @return true if success. (fail if it is stolen)
	 */
	static public boolean installClient(AudioClient mixer) {
		if (thief != null) {
			// errorMessage(" server hgas been stolen by "+ thief);
			return false;
		}
		mixerSwitch.installClient(mixer);
		return true;
	}

	/**
	 * revert to previous mixer
	 * 
	 */
	static public void revertMixer() {
		if (thief != null) {
			// errorMessage(" server hgas been stolen by "+ thief);
			return;
		}
		mixerSwitch.revertClient();

	}

	static public IOAudioProcess audioOutputDialog(JFrame frame, String prompt)
			throws Exception {
		List<String> list = audioServer.getAvailableOutputNames();
		Object a[] = new Object[list.size()];
		a = list.toArray(a);

		Object selectedValue = JOptionPane.showInputDialog(frame,
				"Select audio output", prompt, JOptionPane.INFORMATION_MESSAGE,
				null, a, a[0]);
		if (selectedValue == null)
			return null;
		IOAudioProcess o = audioServer.openAudioOutput((String) selectedValue,
				"output");
		if (defaultOut == null)
			defaultOut = o;
		return o;
	}

	static public IOAudioProcess getDefaultOutput(JFrame frame) {
		if (defaultOut == null) {
			try {
				audioOutputDialog(frame, "Select default output");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return defaultOut;
	}

	static public IOAudioProcess audioInputDialog(JFrame frame, String prompt)
			throws Exception {
		List<String> list = audioServer.getAvailableInputNames();
		Object a[] = new Object[list.size()];
		a = list.toArray(a);

		Object selectedValue = JOptionPane.showInputDialog(frame,
				"Select audio input", prompt, JOptionPane.INFORMATION_MESSAGE,
				null, a, a[0]);

		return audioServer.openAudioInput((String) selectedValue, "input");
	}

	/**
	 * 
	 * Allow user to play with server parameters.
	 * 
	 */
	public static void latencyMeasureSet() {
		JFrame frame = new JFrame();
		frame.setTitle("Output buffer size");
		JPanel panel = new JPanel();
		LatencyTesterPanel lpanel = new LatencyTesterPanel(frame);

		panel.add(lpanel);
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);

	}

	// experimental initialisation through AudioServer latency API
	private static int totalLatency = -1;
	
	public static void setTotalLatency(int frames) {
		totalLatency = frames;
	}
	
	public static int getTotalLatency() {
		if ( totalLatency == -1 ) {
			totalLatency = audioServer.getInputLatencyFrames() +
						   audioServer.getOutputLatencyFrames();
			// TODO plus hardware latency frames times 2
		}
		return totalLatency;
	}
	/**
	 * 
	 * Allow user to play with server parameters.
	 * 
	 */
	public static void configure() {

		if (configureFrame != null) {
			configureFrame.setVisible(true);
			return;
		}

		final JComponent ui = AudioServerUIServices.createServerUI(audioServer, null); // TODO
		if ( ui == null ) return; // no server ui
		configureFrame = new JFrame();
		configureFrame.setAlwaysOnTop(true);
		configureFrame.setContentPane(ui);
		configureFrame.pack();
		configureFrame.setVisible(true);
	}

	/**
	 * 
	 * @return sample rate of the audioServer
	 */

	public static double getSampleRate() {
		return audioServer.getSampleRate();
	}

	private static void errorMessage(String msg) {
		try {
			throw new Exception(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JOptionPane.showMessageDialog(null, msg, "Frinika Message",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This allows you to have sole ownership of the server. Whilst stolen it is
	 * not possible for any one else to use the server. The thief must return it
	 * as sone as possible. This is intended to thwart anything that tries to be
	 * helpful by automatically switching clients
	 * 
	 * @param thief
	 * @return
	 */
	public static AudioServer stealAudioServer(Object thief,
			AudioClient client) {
		if (FrinikaAudioSystem.thief != null) {
			errorMessage(" server has already been stolen by " + thief);
			return null;
		}
		installClient(client);
		FrinikaAudioSystem.thief = thief;
		return audioServer;
	}

	/**
	 * return the audio server for general use. check no one cheats by
	 * pretending they stole it !!!
	 * 
	 * @param thief
	 */
	public static void returnAudioServer(Object thief) {
		if (thief != FrinikaAudioSystem.thief) {
			errorMessage(" attempt to prented to be audio server thief by "
					+ thief + "  real thief was " + FrinikaAudioSystem.thief);
			return;
		}
		FrinikaAudioSystem.thief = null;
		mixerSwitch.revertClient();
	}

	public static int getAudioBufferSize() {
		return bufferSize;
	}

	public static void loadServerConfigPost() {
		serverConfig.applyProperties(FrinikaConfig.getProperties());
	}

	public static void saveServerConfig() {
		serverConfig.mergeInto(FrinikaConfig.getProperties());
		FrinikaConfig.store();

	}

	// Unique object to tag serve stealing
	final static Object controlLossThief = new Object() {
		public String toString() {
			return "ControlLossLock";
		}
	};

	/**
	 * Called on exit. Put code in here to clse on any devices .
	 */
	public static void close() {
		// TODO Auto-generated method stub
	}

}
