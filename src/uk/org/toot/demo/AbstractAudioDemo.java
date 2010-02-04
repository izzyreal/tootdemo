//Copyright (C) 2006 Steve Taylor.
//Distributed under under the terms of the GNU General Public License as
//published by the Free Software Foundation; either version 2 of the License,
//or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.control.*;
import uk.org.toot.control.automation.MidiFileSnapshotAutomation;
import uk.org.toot.audio.core.*;
import uk.org.toot.audio.mixer.*;
import uk.org.toot.audio.mixer.automation.MixerControlsMidiSequenceSnapshotAutomation;
import uk.org.toot.audio.server.*;
import uk.org.toot.audio.system.MixerConnectedAudioSystem;
import uk.org.toot.midi.core.ConnectedMidiSystem;
import uk.org.toot.midi.core.LegacyDevices;
import uk.org.toot.misc.plugin.Plugin;
import uk.org.toot.misc.plugin.TootPluginSupport;
import uk.org.toot.synth.*;
import uk.org.toot.synth.automation.SynthRackControlsMidiSequenceSnapshotAutomation;
import uk.org.toot.project.*;
import uk.org.toot.project.audio.ProjectAudioSystem;
import uk.org.toot.project.automation.ProjectMidiFileSnapshotAutomation;
import uk.org.toot.project.midi.ProjectMidiSystem;
import uk.org.toot.transport.*;
import uk.org.toot.swingui.audioui.serverui.*;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;
import java.io.File;

import javax.swing.*;

/**
 * AbstractAudioDemo creates a problem domain containing an automated mixer and
 * multi-track player with common transport which is extended by MixerDemo
 * and TransportProjectDemo to provide different user interfaces of the same
 * problem domain.
 */
abstract public class AbstractAudioDemo extends AbstractDemo 
{
	protected Transport transport;
	protected SingleTransportProject project;
	protected AudioServer realServer;
	protected AudioServer server;
	protected AudioServerConfiguration serverConfig;

	protected AudioMixer mixer;
	
	/**
	 * @link aggregationByValue
	 * @supplierCardinality 1 
	 */
	protected MultiTrackPlayer multiTrack;
	protected boolean hasMultiTrack = false;

	/**
	 * @link aggregationByValue 
	 * @supplierCardinality 1
	 */
	protected MultiTrackControls multiTrackControls;
	protected MixerControls mixerControls;

	protected ProjectMidiPlayer player;
	protected ProjectMidiSequencer sequencer;
	protected boolean hasMidiPlayer = true;
	protected boolean hasSequencer = false;
	protected boolean hasMidi = true;
	protected boolean hasAudio = true;

	protected ConnectedMidiSystem midiSystem;
	protected MixerConnectedAudioSystem audioSystem;

	protected SynthRack synthRack;
	protected SynthRackControls synthRackControls;
	protected SynthRackControlsMidiSequenceSnapshotAutomation synthRackControlsSnapshotAutomation;
	
	/*
	 * Each creational property is obtained from one of several sources,
	 * tried in the following order.
	 * 1. java -D command line properties
	 * 2. demo.properties file
	 * 3. coded defaults
	 *
	 * i.e. the properties file overrides the defaults and the command line
	 * overrides everything.
	 * If the server starts without exception properties should probably be saved.
	 */
	protected void create(String[] args) {
		try {
			int nSources = 1; // line in
			// create the shared transport
			transport = new DefaultTransport();
			Plugin.setPluginSupport(new TootPluginSupport(transport));
			// create the shared project 'manager'
			project = new SingleTransportProject(transport);
			// load the demo properties
			properties = new DemoProperties(project.getApplicationPath());
			if ( hasAudio ) {
				// choose an audio server
				String serverName = AudioServerChooser.showDialog(property("server"));
				if ( serverName == null ) dispose();
				// remember it for next time
				properties.put("server", serverName);
				properties.store();
				// create the audio server
				realServer = AudioServerServices.createServer(serverName);
				// hook in the setup ui config if available
				final AudioServerConfiguration serverSetup = AudioServerServices.createServerSetup(realServer);
				if ( serverSetup != null ) {
					serverSetup.applyProperties(properties);
					serverSetup.addObserver(new Observer() {
						public void update(Observable obs, Object obj) {
							serverSetup.mergeInto(properties);
							properties.store();
						}
					});
				}
				// show setup ui for sample rate etc.
				AudioServerUIServices.showSetupDialog(realServer, serverSetup);
				// hook it for non-real-time
				server = new NonRealTimeAudioServer(realServer);
				// hack the non real time audio server into the project 'manager'
				if ( server instanceof NonRealTimeAudioServer ) {
					project.setNonRealTimeAudioServer((NonRealTimeAudioServer)server);
				}
				serverConfig = AudioServerServices.createServerConfiguration(realServer);
				if ( serverConfig != null ) {
					serverConfig.applyProperties(properties);
					serverConfig.addObserver(new Observer() {
						public void update(Observable obs, Object obj) {
							serverConfig.mergeInto(properties);
							properties.store();
						}
					});
				}
			}

			// set the projects root
			String projectsRoot = property("projects.root");
			if ( projectsRoot != null ) {
				project.setProjectsRoot(projectsRoot);
			}

			if ( hasAudio ) {
				// create the multitrack player controls
				if ( hasMultiTrack ) {
					int nTapeTracks = intProperty("tape.tracks", 24);
					nSources += nTapeTracks;
					multiTrackControls =
						new MultiTrackControls(nTapeTracks);
					// create the multitrack player
					multiTrack = new ProjectMultiTrackPlayer(project, multiTrackControls);
				}
				
			}
			
			if ( hasMidi ) {
				if ( hasMidiPlayer ) {
					player = new ProjectMidiPlayer(project);
				} else if ( hasSequencer ) {
					sequencer = new ProjectMidiSequencer(project);
				}
				if ( hasAudio ) {
					synthRackControls = new SynthRackControls(8);
					synthRackControlsSnapshotAutomation =
						new SynthRackControlsMidiSequenceSnapshotAutomation(synthRackControls);
					new ProjectMidiFileSnapshotAutomation(
						new MidiFileSnapshotAutomation(
							synthRackControlsSnapshotAutomation, ".synths-snapshot")
						, project);
				}
				// ProjectMidiSystem must be created after the sequencer and synth rack
				// so that it can open connections after the
				// ports have been updated for a new project
				midiSystem = new ProjectMidiSystem(project);
				LegacyDevices.installPlatformPorts(midiSystem);
				if ( hasMidiPlayer ) {
					midiSystem.addMidiDevice(player);
				} else if ( hasSequencer ) {
					midiSystem.addMidiDevice(sequencer);
				}
				nSources += 32 + 8; // TODO synthrack (2 multis, others single)
			}

			if ( hasAudio ) {
				// create the mixer controls
				int nMixerChans = intProperty("mixer.chans", 32);
				if ( nMixerChans < nSources ) nMixerChans = nSources; // for sanity
				mixerControls = new MixerControls("Mixer");
//				MixerControlsFactory.createBusses(mixerControls,
//				intProperty("mixer.fx", 3), intProperty("mixer.aux", 1));
				mixerControls.createFxBusControls("FX#1", null);
				mixerControls.createFxBusControls("FX#2", null);
				mixerControls.createFxBusControls("FX#3", null);
				mixerControls.createAuxBusControls("Aux#1", ChannelFormat.MONO);
				mixerControls.createAuxBusControls("Aux#2", ChannelFormat.QUAD);
				MixerControlsFactory.createBusStrips(mixerControls, "L-R", ChannelFormat.STEREO,
						intProperty("mixer.returns", 2));
				MixerControlsFactory.createGroupStrips(mixerControls,
						intProperty("mixer.groups", 2));
				MixerControlsFactory.createChannelStrips(mixerControls, nMixerChans);
				// add snapshot automation of the mixer controls
				MidiFileSnapshotAutomation snapshotAutomation =
					new MidiFileSnapshotAutomation(
						new MixerControlsMidiSequenceSnapshotAutomation(mixerControls)
						, ".mixer-snapshot");
				
				new ProjectMidiFileSnapshotAutomation(snapshotAutomation, project);
				mixerControls.setSnapshotAutomation(snapshotAutomation);
				// add dynamic automation of the mixer controls
//				MixerControlsDynamicAutomation dynamicAutomation =
//				new TestMixerControlsMidiDynamicAutomation(mixerControls);
				// create the automated mixer
				mixer = new AudioMixer(mixerControls, server);
				
				/*int s =*/ connect(mixer);
				
				// audio system must be after mixer automation and synth automation
				audioSystem = new ProjectAudioSystem(project, mixer);
				if ( hasMidi ) {
					// synth automation needs to disable audiosystem autoconnect
					synthRackControlsSnapshotAutomation.setAudioSystem(audioSystem);
					synthRack = new SynthRack(synthRackControls, midiSystem, audioSystem);
				}
			}

			// add module persistence to ~/toot/presets/<domain>/
			CompoundControl.setPersistence(
					new CompoundControlMidiPersistence(
							new File(System.getProperty("user.home"),
									"toot"+File.separator+"presets")
					)
			);

			if ( hasAudio ) {
				// the multitrack and the mizer are clients of the server
				CompoundAudioClient compoundAudioClient = new CompoundAudioClient();
				compoundAudioClient.add(multiTrack);
				compoundAudioClient.add(mixer);
				server.setClient(compoundAudioClient);
			}

			createUI(args);
			try {
				Thread.sleep(1000);
			} catch ( InterruptedException ie ) {
			}
			if ( hasAudio ) {
				server.start();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			dispose();
		}
	}

	// the opposite of create()
	protected void dispose() {
		System.out.println("Resource Disposal");
		if ( hasAudio && server != null ) {
			server.stop();
//			server.close(); // close all open audio devices
		}

		// close audioSystem? TODO
		
		if ( hasMidi && midiSystem != null ) {
			midiSystem.close(); // close all open midi devices
		}
		
		if ( hasMidi && synthRack != null ) {
			synthRack.close();
		}
		
		if ( hasAudio && mixer != null ) {
			mixer.close();
		}
		
		System.exit(0);
	}
	
	protected int connect(AudioMixer mixer) throws Exception {
		
		// connect an output to the main mixer bus
		AudioProcess output = server.openAudioOutput(property("main.output"), "Line Out");
		// hook it for WAV export although not currently possible to export
//		output = new TransportExportAudioProcessAdapter(output, format, "Mixer Main Bus", transport);
		mixer.getMainBus().setOutputProcess(output);
//		mixer.getBus("Aux#2").setOutputProcess(new NullAudioProcess());
		int s = 1;

		if ( hasMultiTrack ) {
			// connect multitrack outputs 1..n to mixer inputs 1..n
			for ( AudioProcess p : multiTrack.getProcesses() ) {
				mixer.getStrip(String.valueOf(s++)).setInputProcess(p);
			}
		}

		// create an input connected to the next available strip
		String lineName = "Line In";
		try {
			String inputStripName = String.valueOf(s++);
			mixer.getStrip(inputStripName).setInputProcess(
					server.openAudioInput(property("main.input"), lineName));
		} catch ( Exception e ) {
			System.err.println("Failed to open "+lineName);
		}
		return s;
	}

	protected void createUI(String[] args) {
		UIManager.put("ToolTip.background", new Color(255, 255, 225));
	};
}
