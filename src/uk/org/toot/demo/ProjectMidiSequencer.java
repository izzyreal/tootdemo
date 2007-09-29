// Copyright (C) 2006 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.project.*;
import uk.org.toot.midi.sequencer.MidiSequencer;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.*;

public class ProjectMidiSequencer extends MidiSequencer
{
    private SingleTransportProject project;
    private ProjectListener projectListener;

    public ProjectMidiSequencer(SingleTransportProject p) {
        project = p;
        projectListener = new ProjectListener() {
            public void open() {
            	try {
            		File path = new File(project.getCurrentProjectPath(), "midi");
            		File seqfile = new File(path, "sequence.mid");
            		if ( !seqfile.exists() ) return;
//            		System.out.println(seqfile.getPath());
            		Sequence sequence = MidiSystem.getSequence(seqfile);
            		setSequence(sequence);
            	} catch ( Exception e) {
            		e.printStackTrace();
            		System.out.println("Failed to open project sequence");
            	}
            }
            public void save() {
                // ???
            }
        };
        // TODO figure out if and when these should be removed
        project.addProjectListener(projectListener);
        project.getTransport().addTransportListener(this);
        try {
        	open();
        } catch ( Exception e ) {
        	e.printStackTrace();
        	System.err.println("Failed to open Sequencer");
        }
    }
}
