/*
 * Created on Apr 14, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Vector;


/**
 * Global settings to be stored and restored when starting the program
 * 
 * @author Peter Johan Salomonsen
 */
public class FrinikaConfig {

	private static File userFrinikaDir = new File(System
			.getProperty("user.home"), "frinika");

	private static Properties properties;

	public static boolean bigEndian=true;

	public static float sampleRate=44100.0f;

	public static boolean autoConnect=false;
	
	private static final String CONFIG_JACK_AUTOCONNECT = "false";
	
	private static final String CONFIG_FILENAME = "FrinikaConfig.xml";

	private static final String CONFIG_ENCODING = "UTF-8";

	private static final String CONFIG_LAST_PROJECT_FILENAME = "LastProjectFilename";

	private static final String CONFIG_MIDIIN_LIST = "MidiInList";


	private static final String CONFIG_AUDIO_BUFFER_LEN = "Latency"; 
	
	private static final String CONFIG_DIRECT_MONITORING = "DirectMonitoring";

	/**
	 * In windows this will be 30 ms as the kmixer latency
	 */
	private static final String CONFIG_OS_LATENCY_MILLIS = "OperatingSystemLatencyMilliseconds"; 
		

	static {
		properties = new Properties();

		userFrinikaDir = new File(System.getProperty("user.home"), "frinika");
		if (!userFrinikaDir.exists()) {
			System.out.println(" Creating frinka user settings directory  "
					+ userFrinikaDir);
			if (!userFrinikaDir.mkdir()) {
				System.err.println(" Failed to create " + userFrinikaDir);
			}
		}
		File f = null;
		try {
			properties.loadFromXML(new FileInputStream(f = new File(
					userFrinikaDir, CONFIG_FILENAME)));

		} catch (InvalidPropertiesFormatException e) {

			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("Can't find file " + f
					+ ". It will be created when you quit the program.");
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static String lastProjectFile() throws Exception {
		
		// PJL decoupled this a bit.
		
		return properties.getProperty(CONFIG_LAST_PROJECT_FILENAME);
				
				
		/*new ProjectFrame(ProjectContainer.loadProject(new File(properties
				.getProperty(CONFIG_LAST_PROJECT_FILENAME))));*/
	}

	public static void setAudioBufferLength(int len) {
		properties.put(CONFIG_AUDIO_BUFFER_LEN, String.valueOf(len));
	}


	
	public static boolean getJackAutoConnect() {
		try {
			return Boolean.parseBoolean(properties.getProperty(CONFIG_JACK_AUTOCONNECT));
		} catch(Exception e) {
			return false;
		}
	}

	
	public static int getAudioBufferLength() {
		try {
			return Integer.parseInt(properties.getProperty(CONFIG_AUDIO_BUFFER_LEN));
		} catch(Exception e) {
			return 512;
		}
	}


	public static void setJackAutoconnect(boolean yes) {
		properties.put(CONFIG_JACK_AUTOCONNECT, String.valueOf(yes));
	}

	public static boolean getJackAutoconnect() {
		try {
			return Boolean.parseBoolean(properties.getProperty(CONFIG_JACK_AUTOCONNECT));
		} catch(Exception e) {
			return false;
		}
	}
	
	public static void setDirectMonitoring(boolean yes) {
		properties.put(CONFIG_DIRECT_MONITORING, String.valueOf(yes));
	}


	public static boolean getDirectMonitoring() {
		try {
			return Boolean.parseBoolean(properties.getProperty(CONFIG_DIRECT_MONITORING));
		} catch(Exception e) {
			return false;
		}
	}


	public static void setLastProjectFilename(String fileName) {
		properties.put(CONFIG_LAST_PROJECT_FILENAME, fileName);
	}

	/**
	 * Get operating system latency in milliseconds. For windows this is the typical 30 ms kmixer latency
	 * @return
	 */
	public static int getOSLatencyMillis()
	{
		Integer osLatencyMillis = null;
		try
		{
			osLatencyMillis = Integer.parseInt((String)properties.get(CONFIG_OS_LATENCY_MILLIS));
		} catch(Exception e) {}
		if(osLatencyMillis == null)
		{
			if(System.getProperty("os.name").contains("Windows"))
			{
				System.out.println("Detected windows - setting operating system latency to 30 ms (KMixer latency) ");
				osLatencyMillis = 30; // Default is KMixer latency - please adjust if you bypass kmixer
			}
			else
				osLatencyMillis = 0;
				
			properties.setProperty(CONFIG_OS_LATENCY_MILLIS, ""+osLatencyMillis);
		}
		return osLatencyMillis;
	}
	
	/**
	 * Set the operating system latency in milliseconds
	 * @param osLatencyMillis
	 */
	public static void setOSLatencyMillis(int osLatencyMillis)
	{
		properties.setProperty(CONFIG_OS_LATENCY_MILLIS, ""+osLatencyMillis);
	}
	
	public static void setMidiInDeviceList(Vector<String> list) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (String o : list) {
			if (!first)
				buf.append(";");
			buf.append(o.toString());
			first = false;
		}
		System.out.println(buf);
		properties.put(CONFIG_MIDIIN_LIST, buf.toString());
	}

	
	/*public static void setAudioInDeviceList(Vector<String> list) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (String o : list) {
			if (!first)
				buf.append(";");
			buf.append(o.toString());
			first = false;
		}
		System.out.println(buf);
		properties.put(CONFIG_AUDIOIN_LIST, buf.toString());
	}*/

	public static Vector<String> getMidiInDeviceList() {
		String buf = properties.getProperty(CONFIG_MIDIIN_LIST);
			if (buf == null)
			buf = "";
		String[] list = buf.split(";");
		Vector<String> vec = new Vector<String>();
		for (String str : list) {
			if (!str.equals(""))
				vec.add(str);
		}
		return vec;
	}

	public static void setProperty(String property,String value) {
		properties.put(property, value);		
	}

	
	public static String getProperty(String property) {
		return (String) properties.get(property);	
	}

	public static void store() {
		try {
			properties
					.storeToXML(new FileOutputStream(new File(userFrinikaDir,
							CONFIG_FILENAME)), "Frinika configuration",
							CONFIG_ENCODING);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeAndQuit() {
		store();
		System.exit(0);
	}

	public static boolean getPropertyBoolean(String string) {
		try {
			return Boolean.parseBoolean(properties.getProperty(string));
		} catch(Exception e) {
			return false;
		}
	}
	
	// !!! ST for AudioServerConfiguration use
	public static Properties getProperties() {
		return properties;
	}
}
