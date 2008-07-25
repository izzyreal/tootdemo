//Copyright (C) 2006 Steve Taylor.
//Distributed under under the terms of the GNU General Public License as
//published by the Free Software Foundation; either version 2 of the License,
//or (at your option) any later version.

package uk.org.toot.demo;

import java.util.Observable;
import java.util.Observer;
import uk.org.toot.control.*;
import uk.org.toot.swingui.audioui.serverui.*;
import uk.org.toot.synth.example1.*;
import uk.org.toot.synth.example2.*;
import uk.org.toot.audio.mixer.*;
import uk.org.toot.audio.server.*;
import uk.org.toot.midi.core.ConnectedMidiSystem;
import uk.org.toot.midi.core.LegacyDevices;
import uk.org.toot.synth.*;
import uk.org.toot.project.*;
import uk.org.toot.transport.*;
import java.io.File;
import uk.org.toot.audio.core.*;
import javax.swing.*;
import java.awt.Color;

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

	/**
	 * @link aggregationByValue
	 * @supplierCardinality 1 
	 */
	protected MultiTrackPlayer multiTrack;
	protected boolean hasMultiTrack = true;

	/**
	 * @link aggregationByValue 
	 * @supplierCardinality 1
	 */
	protected MultiTrackControls multiTrackControls;
	protected MixerControls mixerControls;

	protected ProjectMidiSequencer sequencer;
	protected boolean hasMidi = true;
	protected boolean hasAudio = true;

	protected ConnectedMidiSystem midiSystem;

	protected SynthRack synthRack;
	protected SynthRackControls synthRackControls;
	protected ExampleSynthControls exampleSynthControls;
	protected Example2SynthControls example2SynthControls;
	
//	protected DemoSourceControls demoSourceControls;

	/*
	 * Each creational property is obtained from one of several sources,
	 * tried in the following order.
	 * 1. java -D command line properties
	 * 2. demo.properties file
	 * 3. coded defaults
	 *
	 * i.e. the properties file overrides the defaults and the command line
	 * overrides everything.
	 * AudioServerChooser allows server properties to be reviewed/changed
	 * prior to use.
	 * If the server starts without exception properties should probably be saved.
	 */
	protected void create(String[] args) {
		try {
			AudioMixer mixer = null;
			int nSources = 0;
			// create the shared transport
			transport = new DefaultTransport();
			// create the shared project 'manager'
			project = new SingleTransportProject(transport);
			// load the demo properties
			properties = new DemoProperties(project.getApplicationPath());
			if ( hasAudio ) {
				// choose and configure and audio server
				// modifies server and sample.rate properties
				AudioServerChooser.showDialog(properties);
				// create the audio server
				realServer = AudioServerServices.createServer(property("server"));
				realServer.setSampleRate((float)intProperty("sample.rate", 44100));
				// hook it for non-real-time
				server = new NonRealTimeAudioServer(realServer);
				// hack the non real time audio server into the project 'manager'
				if ( server instanceof NonRealTimeAudioServer ) {
					project.setNonRealTimeAudioServer((NonRealTimeAudioServer)server);
				}
				serverConfig = AudioServerServices.createServerConfiguration(realServer);
				serverConfig.addObserver(new Observer() {
					public void update(Observable obs, Object obj) {
						serverConfig.mergeInto(properties);
						properties.store();
					}
				});
				serverConfig.applyProperties(properties);

				/*        	System.out.println("Inputs:");
        	for ( String in : server.getAvailableInputNames() ) {
        		System.out.println(in);
        	}
        	System.out.println("Outputs:");
        	for ( String in : server.getAvailableOutputNames() ) {
        		System.out.println(in);
        	}
				 */
			}

			// set the projects root
			String projectsRoot = property("projects.root");
			if ( projectsRoot != null ) {
				project.setProjectsRoot(projectsRoot);
			}

			if ( hasMidi ) {
				sequencer = new ProjectMidiSequencer(project);
				// ProjectMidiSystem must be created after the sequencer
				// so that it can open connections after the sequencer
				// has updated its ports for a new project
				midiSystem = new ProjectMidiSystem(project);
				LegacyDevices.installPlatformPorts(midiSystem);
				midiSystem.addMidiDevice(sequencer);

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
				MixerControlsSnapshotAutomation snapshotAutomation =
					new ProjectMidiFileSnapshotAutomation(mixerControls, project);
				mixerControls.setSnapshotAutomation(snapshotAutomation);
				// add dynamic automation of the mixer controls
//				MixerControlsDynamicAutomation dynamicAutomation =
//				new TestMixerControlsMidiDynamicAutomation(mixerControls);
				// create the automated mixer
				mixer = new AudioMixer(mixerControls, server);
				
				int s = connect(mixer);
				
				if ( hasMidi ) {
					synthRack = new SynthRack(midiSystem);

					// add an example midi synth for testing
					MidiSynth midiSynth1 = new MidiSynth("Synth 1");
					synthRack.addMidiSynth(midiSynth1); // adds the MIDI input
					
					// needs synths adding first
					synthRackControls = new SynthRackControls(synthRack);

					// and example synth channel controls for testing
					exampleSynthControls = new ExampleSynthControls("ExSyn1");
					// add to the first synth, first channel, using SPI for synth channel
					synthRackControls.add(0, 0, exampleSynthControls);
					// get the provided synth channel
					SynthChannel synthChannel = synthRack.getMidiSynths().get(0).getChannels()[0];
					// connect it to the next unsused mixer channel
					mixer.getStrip(String.valueOf(s++)).setInputProcess(synthChannel);

					// and example synth channel controls for testing
					example2SynthControls = new Example2SynthControls("ExSyn2");
					// add to the first synth, first channel, using SPI for synth channel
					synthRackControls.add(0, 1, example2SynthControls);
					// get the provided synth channel
					synthChannel = synthRack.getMidiSynths().get(0).getChannels()[1];
					// connect it to the next unsused mixer channel
					mixer.getStrip(String.valueOf(s++)).setInputProcess(synthChannel);
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
			waitForKeypress();
		}
	}

	// the opposite of create()
	protected void dispose() {
		if ( hasMidi ) {
			midiSystem.close(); // close all open midi devices
		}
		
		if ( hasAudio ) {
			server.stop();
//			server.close(); // close all open audio devices
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

		// create a demo source connected to the next available strip
		/*            String demoSourceStripName = String.valueOf(s++);
            demoSourceControls = new DemoSourceControls(mixerControls, demoSourceStripName, "A");
            DemoSourceProcess dsp = new DemoSourceProcess(demoSourceControls);
            mixer.getStrip(demoSourceStripName).setInputProcess(dsp); */

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
