// Copyright (C) 2007 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

//import java.beans.XMLEncoder;
//import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.Track;
import javax.sound.midi.Sequence;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.InvalidMidiDataException;
import uk.org.toot.midi.message.*;
import uk.org.toot.music.composition.BarComposer;
import uk.org.toot.music.composition.BarContext;
import uk.org.toot.music.performance.*;
import uk.org.toot.music.timing.Timing;
import uk.org.toot.music.tonality.*;

/**
 * An AutomatedBand is a composition of BarComposer / Performer pairings.
 * It can render a Sequence and a MIDI FIle.
 * @author st
 *
 */
public class AutomatedBand 
{
    /**
     * @link aggregation
     * @supplierCardinality 1..* 
     */
    /*#BarComposer lnkBarComposer;*/
	private List<BarComposer> composers;
	private List<Performer> performers;
	
	private List<Key> keyList;
	private int keyIndex = -1;
	
	// used by local changeKey() implementation
	private Key key = new Key();
	protected Key[] keyChanges = { key };
	protected int[] keyChangeTimes = new int[1];
	
	private BarContext barContext;
	
	private boolean rendering = false;
	private float modulationDensity = 0.5f;
	private float cycleOfFifthsDensity = 1f;
	private float keyReturnProbability = 0.5f;
	private int maxKeys = 6;
//	private float scaleDensity = 0.1f;
//	private List<String> scaleNames;
	
	public AutomatedBand() {
		composers = new java.util.ArrayList<BarComposer>();
		performers = new java.util.ArrayList<Performer>();
		keyList = new java.util.ArrayList<Key>();
		barContext = new BarContext();
		barContext.setKeys(keyChanges);
		barContext.setKeyTimes(keyChangeTimes);
//		scaleNames = Scales.getScaleNames();
	}
	
	public BarContext getBarContext() {
		return barContext;
	}
	
	public void add(BarComposer composer, Performer performer) {
		if ( rendering ) {
			throw new IllegalStateException("Can't add composers/performers while rendering");
		}
		composers.add(composer);
		performers.add(performer);
	}
	
	public boolean isRendering() {
		return rendering;
	}
	
	public void renderFile(int nbars, int ppq, File file, File dir) 
		throws IOException, InvalidMidiDataException {
		Sequence sequence = renderSequence(nbars, ppq, dir);
		MidiSystem.write(sequence, 1, file);
	}
	
	public Sequence renderSequence(int nbars, int ppq, File dir) 
		throws InvalidMidiDataException {
		rendering = true;
		Sequence sequence = new Sequence(Sequence.PPQ, ppq);
		sequence.createTrack(); // master track
		// create a Track for each composer
		List<Track> tracks = new java.util.ArrayList<Track>();
		Track masterTrack = sequence.createTrack();
		tracks.add(masterTrack);
		for ( int c = 0; c < performers.size(); c++) {
			Performer performer = performers.get(c);
			Track t = sequence.createTrack();
			Instrument instrument = performer.getInstrument(); 
			MidiMessage msg = 
				MetaMsg.createMeta(MetaMsg.TRACK_NAME, performer.getName());
			t.add(new MidiEvent(msg, 0));
			msg = ChannelMsg.createChannel(ChannelMsg.PROGRAM_CHANGE, 
					instrument.getChannel(), instrument.getProgram());
			t.add(new MidiEvent(msg, 0));
			tracks.add(t);
		}
		long barTick = 0;
		// for each bar
		for ( int bar = 0; bar < nbars; bar++) {
			if ( changeKey() ) {
				barContext.setKeys(keyChanges);
				barContext.setKeyTimes(keyChangeTimes);
				// write key to mastertrack somehow
				System.out.println((1+bar)+": "+keyChanges[0]);
			}
			// for each composer/performer/track
			for ( int c = 0; c < composers.size(); c++) {
				BarComposer composer = composers.get(c);
				Track track = tracks.get(c+1);
				Performer performer = performers.get(c);
				int[] notes = composer.composeBar(barContext);
				performer.renderBar(notes, track, barTick, ppq);
			}
			barTick += ppq * barContext.getMeter() / Timing.QUARTER_NOTE;
//			System.out.print(bar+"\r");
		}
		rendering = false;
		return sequence;
	}
	
	// should set keyChanges and keyChangeTimes for the next bar
	protected boolean changeKey() {
		if ( Math.random() > modulationDensity ) return false;
		// we only do one key change per bar
		keyChangeTimes = new int[1];
		keyChangeTimes[0] = 0;
		keyChanges = new Key[1];
//		Key previousKey = key;
		if ( (Math.random() < keyReturnProbability && keyList.size() > 1) ||
				keyList.size() >= maxKeys ) {
			if ( keyIndex == 0 ) {
				keyIndex += 1;
			} else if ( keyIndex == keyList.size()-1 ) {
				keyIndex -= 1;
			} else {
				keyIndex += Math.random() > 0.5f ? 1 : -1;
			}
			key = keyList.get(keyIndex);
//			System.out.println(key+" reused from "+keyIndex);
			keyChanges[0] = key;
			return true;
		}
/*		if ( Math.random() < scaleDensity ) {
			key.setScale(Scales.getScale(scaleNames.get((int)(Math.random()*4))));
		} else */
		// !!! weak indices, what about 8 and more notes scales
		if ( Math.random() < cycleOfFifthsDensity ) {
			if ( Math.random() < 0.5 ) {
				key = new Key(key.getNote(4-1));
//				System.out.println(previousKey+" => "+key+" (Sub Dominant)");
			} else {
				key = new Key(key.getNote(5-1));
//				System.out.println(previousKey+" => "+key+" (Dominant)");
			}
		} else {
			if ( Math.random() < 0.5 ) {
				key = new Key(key.getNote(7-1));
//				System.out.println(previousKey+" => "+key+" (Leading Tone)");
			} else {
				key = new Key(key.getNote(2-1));
//				System.out.println(previousKey+" => "+key+" (Supertonic)");
			}
		}
		if ( keyList.contains(key) ) {
			keyIndex = keyList.indexOf(key);
			keyChanges[0] = key;
			return true;
		}
		keyList.add(key);
		keyIndex = keyList.size()-1;
//		System.out.println(key+" added at "+keyIndex);
		keyChanges[0] = key;
		return true;
	}

	/**
	 * @return the cycleOfFifthsDensity
	 */
	public float getCycleOfFifthsDensity() {
		return cycleOfFifthsDensity;
	}

	/**
	 * @param cycleOfFifthsDensity the cycleOfFifthsDensity to set
	 */
	public void setCycleOfFifthsDensity(float cycleOfFifthsDensity) {
		this.cycleOfFifthsDensity = cycleOfFifthsDensity;
	}

	/**
	 * @return the modulationDensity
	 */
	public float getModulationDensity() {
		return modulationDensity;
	}

	/**
	 * @param modulationDensity the modulationDensity to set
	 */
	public void setModulationDensity(float modulationDensity) {
		this.modulationDensity = modulationDensity;
	}

	/**
	 * @return the keyReturnProbability
	 */
	public float getKeyReturnProbability() {
		return keyReturnProbability;
	}

	/**
	 * @param keyReturnProbability the keyReturnProbability to set
	 */
	public void setKeyReturnProbability(float keyReturnProbability) {
		this.keyReturnProbability = keyReturnProbability;
	}

	/**
	 * @return the maxKeys
	 */
	public int getMaxKeys() {
		return maxKeys;
	}

	/**
	 * @param maxKeys the maxKeys to set
	 */
	public void setMaxKeys(int maxKeys) {
		this.maxKeys = maxKeys;
	}
}
