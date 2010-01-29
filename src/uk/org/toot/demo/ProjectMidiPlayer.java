// Copyright (C) 2009 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.project.*;
import uk.org.toot.transport.TransportListener;
import uk.org.toot.midi.seqng.SequenceMidiSource;
import uk.org.toot.midi.sequence.Midi;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.*;

public class ProjectMidiPlayer extends TootMidiPlayer implements TransportListener
{
    private SingleTransportProject project;
    private ProjectListener projectListener;
    private SequenceMidiSource sequenceSource;

    public ProjectMidiPlayer(SingleTransportProject p) {
        project = p;
        projectListener = new ProjectListener() {
            public void open() {
            	try {
            		File path = new File(project.getCurrentProjectPath(), "midi");
            		File seqfile = new File(path, "sequence.mid");
            		if ( !seqfile.exists() ) return;
            		Sequence sequence = MidiSystem.getSequence(seqfile);
            		sequenceSource = new SequenceMidiSource(Midi.unMix(sequence));
            		setMidiSource(sequenceSource);
            	} catch ( Exception e) {
            		e.printStackTrace();
            		System.out.println("Failed to open project sequence");
            	}
            }
            public void save() {
            	try {
            		if ( sequenceSource == null ) return;
        			Sequence sequence = sequenceSource.getSequence();
            		if ( sequence != null ) {
            			File path = new File(project.getCurrentProjectPath(), "midi");
            			File seqfile = new File(path, "sequence.mid");
//            			if ( seqfile.exists() ) return;
            			MidiSystem.write(sequence, 1, seqfile);
            		}
            	} catch ( Exception e) {
            		e.printStackTrace();
            		System.out.println("Failed to save project sequence");
            	}
            }
        };
        // TODO figure out if and when these should be removed
        project.addProjectListener(projectListener);
        project.getTransport().addTransportListener(this);
/*        try {
        	open();
        } catch ( Exception e ) {
        	e.printStackTrace();
        	System.err.println("Failed to open Sequencer");
        } */
    }
    
    /**
     * Called when the transport record mode changes.
     */
	public void record(boolean rec) {}

    /**
     * Called when the transport locates to a new microsecond time.
     */
    public void locate(long microseconds) {
    	if ( microseconds == 0 ) {
    		returnToZero();
    	} else {
    		System.err.println("ProjectMidiPlayer doesn't support locate!");
    	}
    }

    public SingleTransportProject getProject() {
    	return project;
    }
}
