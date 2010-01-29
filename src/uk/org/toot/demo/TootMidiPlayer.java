package uk.org.toot.demo;

import java.util.Hashtable;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

import uk.org.toot.midi.core.AbstractMidiDevice;
import uk.org.toot.midi.core.DefaultMidiOutput;
//import uk.org.toot.midi.seqng.MTCMidiPlayer;
import uk.org.toot.midi.seqng.MidiPlayer;
import uk.org.toot.midi.seqng.MidiSource;

import static uk.org.toot.midi.misc.Controller.ALL_NOTES_OFF;
import static uk.org.toot.midi.message.ChannelMsg.*;

/**
 * This class integrates a MidiPlayer into the Toot midi system.
 * @author st
 *
 */
public class TootMidiPlayer extends AbstractMidiDevice
{
	private OurMidiPlayer player = new OurMidiPlayer();
	
	public TootMidiPlayer() {
		super("MidiPlayer");
	}

	public void setMidiSource(MidiSource source) {
		player.setMidiSource(source);
	}
	
	public void play() {
		try {
			player.play();
		} catch ( IllegalStateException ise ) {
		}
	}
	
	public void stop() {
		player.stop();
	}
	
	public void returnToZero() {
		try { 
			player.returnToZero();
		} catch ( IllegalStateException ise ) {
		}
	}
	
	public boolean isRunning() {
		return player.isRunning();
	}
	
	public long getTickPosition() {
		return player.getTickPosition();
	}
	
	public long getMillisecondPosition() {
		return player.getMillisecondPosition();
	}
	
	public float getBeatsPerMinute() {
		return player.getBeatsPerMinute();
	}
	
	public void closeMidi() {
		// TODO Auto-generated method stub		
	}

	public class OurMidiPlayer extends MidiPlayer
	{
		private Hashtable<MidiSource.EventSource, DefaultMidiOutput> outtable =
			new Hashtable<MidiSource.EventSource, DefaultMidiOutput>();

		@Override
		protected void transport(MidiMessage msg, MidiSource.EventSource src, int idx) {
			DefaultMidiOutput output = getOutput(src);
			output.transport(msg, -1);
		}
		
		/*
		 * This is called when the source is set so it gives us a chance to
		 * build the initial hashtable off the real-time thread.
		 * (non-Javadoc)
		 * @see uk.org.toot.midi.seqng.MidiPlayer#notesOff()
		 */
		@Override
		protected void notesOff() {
			for ( MidiSource.EventSource src : eventSources() ) {
				DefaultMidiOutput output = getOutput(src);
				for ( int i = 0; i < 16; i++ ) {
					try {
						MidiMessage msg = createChannel(
							CONTROL_CHANGE, i, ALL_NOTES_OFF, 0);
						output.transport(msg, -1);
					} catch ( InvalidMidiDataException imde ) {
						System.err.println(imde.getMessage());
					}
				}
			}
		}
		
		protected DefaultMidiOutput getOutput(MidiSource.EventSource src) {
			DefaultMidiOutput output = outtable.get(src);
			if ( output == null ) {
				output = new DefaultMidiOutput(src.getName());
				addMidiOutput(output); // sends notifications !!!
				outtable.put(src, output);
			}
			return output;
		}
	}
}
