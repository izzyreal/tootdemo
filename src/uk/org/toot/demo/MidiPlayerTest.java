package uk.org.toot.demo;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.JFileChooser;

import uk.org.toot.midi.seqng.MTCMidiPlayer;
//import uk.org.toot.midi.seqng.MidiPlayer;
import uk.org.toot.midi.seqng.MidiSource;
import uk.org.toot.midi.seqng.SequenceMidiSource;

public class MidiPlayerTest
{
	private MTCMidiPlayer midiPlayer = new MTCMidiPlayer();
	
	public MidiPlayerTest() {
	    JFileChooser chooser = new JFileChooser() {
	    	public boolean accept(File file) {
	    		if ( file.isDirectory() ) return true;
	    		String name = file.getName(); 
	    		return name.endsWith("MID") || name.endsWith("mid");
	    	}
	    };
	    int returnVal = chooser.showOpenDialog(null);
	    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
	        File midiFile = chooser.getSelectedFile();
	        try {
				Sequence sequence = MidiSystem.getSequence(midiFile);
				MidiSource midiSource = new SequenceMidiSource(sequence);
				midiPlayer.setMTCEnabled(true);
				midiPlayer.setMidiSource(midiSource);
				midiPlayer.addObserver(new Observer() {
					public void update(Observable arg0, Object arg1) {
						System.out.println(midiPlayer.isRunning()? "\nRunning" : "\nStopped");
					}					
				});
				midiPlayer.play();
				delay(60);
				midiPlayer.stop();
				while ( midiPlayer.isRunning() ) {
					try {
						Thread.sleep((int) (1000));
					} catch (InterruptedException ie) {

					}					
				}
				midiPlayer.returnToZero();
				delay(10);
				midiPlayer.play();
				delay(60);
				System.in.read();
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}

	protected void delay(float seconds) {
		for ( int i = 0; i < seconds; i++ ) {
//			System.out.print((int)(midiPlayer.getTickPosition())+" ");
			System.out.print("  "+(int)(midiPlayer.getMillisecondPosition()/1000)+" ");
			if ( i > 0 && (i % 10) == 0 ) System.out.println();
			try {
				Thread.sleep((int) (1000));
			} catch (InterruptedException ie) {

			}
		}
	}
	
    public static void main(String[] args) {
        new MidiPlayerTest();
    }

}
