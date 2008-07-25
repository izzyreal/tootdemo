// Copyright (C) 2006 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.swingui.midiui.MidiConnectionView;
import uk.org.toot.swingui.midixui.controlui.neckui.NecksView;
import uk.org.toot.swingui.projectui.*;
import uk.org.toot.swingui.synthui.SynthRackPanel;
import uk.org.toot.swingui.audioui.mixerui.*;
import uk.org.toot.swingui.audioui.serverui.*;
import uk.org.toot.swingui.controlui.CompoundControlPanel;
import uk.org.toot.swingui.controlui.ControlPanelFactory;

//import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * TransportProjectDemo displays a full user interface over the underlying
 * demonstration problem domain model, with the multi-track player and automated
 * mixer in separate tabs.
 */
public class TransportProjectDemo extends AbstractAudioDemo 
{
    private JToolBar toolBar;

    protected void createUI(String[] args) {
        toolBar = new JToolBar();
        super.createUI(args);
        if ( hasAudio ) {
        // the audio server panel is provided by a service provider
        toolBar.add(new AudioServerUIButton(AudioServerUIServices.createServerUI(realServer, serverConfig)));
        }
        SingleTransportProjectPanel panel = new SingleTransportProjectPanel(project, toolBar) {
        	protected void dispose() {
        		super.dispose();
        		TransportProjectDemo.this.dispose();
        	}
        };

        if ( hasMidi ) {
            panel.addTab("Necks", new NecksView(midiSystem));
            panel.addTab("MIDI Patchbay", new MidiConnectionView(midiSystem));
            panel.addTab("Sequencer", new MidiSequencerPanel(sequencer, midiSystem));
       	}
        if ( hasAudio ) {
        	if ( hasMidi ) {
        		panel.addTab("Synths", new SynthRackPanel(synthRackControls));
        	}
        	if ( hasMultiTrack ) {
        		panel.addTab("MultiTrack", new MultiTrackPanel(multiTrackControls));
        	}
        	panel.addTab("Audio Mixer", new CompactMixerPanel(mixerControls));
        	if ( hasMidi ) {
        		panel.addTab("Synth", 
        				new CompoundControlPanel(exampleSynthControls, 1, null,
        						new ControlPanelFactory() {
        					protected boolean canEdit() { return true; }
        				}, true, true)
        		);
        		panel.addTab("Synth 2", 
        				new CompoundControlPanel(example2SynthControls, 1, null,
        						new ControlPanelFactory() {
        					protected boolean canEdit() { return true; }
        				}, true, true)
        		);
        	}
        // add the source demo panel as a separate frame
//        frame(new DemoSourcePanel(demoSourceControls), "Source Demo");
        }
       	frame(panel, "Toot Transport Project");
		project.openProject("default");
    }

    public static void main(String[] args) {
        (new TransportProjectDemo()).create(args);
    }
}
