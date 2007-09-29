// Copyright (C) 2006 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.swingui.midiui.MidiConnectionMap;
import uk.org.toot.swingui.midixui.controlui.neckui.NecksView;
import uk.org.toot.swingui.projectui.*;
import uk.org.toot.swingui.audioui.mixerui.*;
import uk.org.toot.swingui.audioui.serverui.*;

import javax.swing.JPanel;
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
        // the audio server panel is provided by a service provider
        toolBar.add(new AudioServerUIButton(AudioServerUIServices.createServerUI(realServer, serverConfig)));
        SingleTransportProjectPanel panel = new SingleTransportProjectPanel(project, toolBar);
        if ( hasMultiTrack ) {
        	panel.addTab("MultiTrack", new MultiTrackPanel(multiTrackControls));
        }
        panel.addTab("Audio Mixer", new CompactMixerPanel(mixerControls));
       	frame(panel, "Toot Transport Project");
       	if ( hasSequencer ) {
            JPanel midiConnectionPanel = new MidiConnectionMap(midiSystem);
            JPanel necksPanel = new NecksView(midiSystem);
            frame(midiConnectionPanel, "Midi Connection Map");
            frame(necksPanel, "Necks View");
       		
       	}
        // add the source demo panel as a separate frame
//        frame(new DemoSourcePanel(demoSourceControls), "Source Demo");
		project.openProject("default");
    }

    public static void main(String[] args) {
        (new TransportProjectDemo()).create(args);
    }
}
