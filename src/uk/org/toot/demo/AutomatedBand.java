// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

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
import uk.org.toot.music.performance.*;
import uk.org.toot.music.*;
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
	
//	private Music music;
	private boolean rendering = false;
	private Key key = new Key();
	private float modulationDensity = 0.5f;
	private float cycleOfFifthsDensity = 1f;
	private float keyReturnProbability = 0.5f;
	private int maxKeys = 5;
//	private float scaleDensity = 0.1f;
//	private List<String> scaleNames;
	
	public AutomatedBand() {
		composers = new java.util.ArrayList<BarComposer>();
		performers = new java.util.ArrayList<Performer>();
		keyList = new java.util.ArrayList<Key>();
//		scaleNames = Scales.getScaleNames();
//		music = new Music();
	}
	
	public void add(BarComposer composer, Performer performer) {
		if ( rendering ) {
			throw new IllegalStateException("Can't add composers while rendering");
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
//		Music.Section section = music.createSection("All", nbars);
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
//		composers.get(0).checkSwing();
		long barTick = 0;
		// for each bar
		for ( int bar = 0; bar < nbars; bar++) {
			barTick += 4 * ppq;
			if ( changeKey() ) {
				// write key to mastertrack somehow
				// and to music section
//				int keyChange = KeyCoding.create(0, key.getRoot(), key.getScale().getIntervalsAsInt());
//				int[] keyChanges = new int[1];
//				keyChanges[0] = keyChange;
//				section.setKeyChanges(bar, keyChanges);
			}
			// for each composer/performer/track
			for ( int c = 0; c < composers.size(); c++) {
				BarComposer composer = composers.get(c);
				Track track = tracks.get(c);
				Performer performer = performers.get(c);
				int[] notes = composer.composeBar(key);
//				section.setNotes(performer.getName(), bar, notes); // !!! TODO
				performer.renderBar(notes, track, barTick, 4 * ppq);
			}
//			System.out.print(bar+"\r");
		}
/*		System.out.println(section.getKeyChangeCount()+ " Key changes");
		for ( int k = 0; k < section.getKeyChangeCount(); k++) {
			int keyChange = section.getKeyChange(k);
			int bar = KeyCoding.getBar(keyChange);
			int beat = KeyCoding.getBeat(keyChange);
			int root = KeyCoding.getRoot(keyChange);
			System.out.println(bar+"."+beat+": "+Pitch.className(root));
		} */
//		music.list();
//		File musicFile = new File(dir, "abdemo.music");
		// save music file
//		try {
//			MusicSerialization.save(music, musicFile);
//		} catch ( Exception e ) {
//			e.printStackTrace();
//		}
		// test reload of music file
/*		try {
			System.out.println("Reloading serialized music file");
			Music musicReload = MusicSerialization.load(musicFile);
			musicReload.list();
		} catch ( Exception e ) {
			e.printStackTrace();			
		} */
		rendering = false;
		return sequence;
	}
	
	protected boolean changeKey() {
		if ( Math.random() > modulationDensity ) return false;
		Key previousKey = key;
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
			System.out.println(key+" reused from "+keyIndex);
			return true;
		}
/*		if ( Math.random() < scaleDensity ) {
			key.setScale(Scales.getScale(scaleNames.get((int)(Math.random()*4))));
		} else */
		// !!! weak indices, what about 8 and more notes scales
		if ( Math.random() < cycleOfFifthsDensity ) {
			if ( Math.random() < 0.5 ) {
				key = new Key(key.getNote(4-1));
				System.out.println(previousKey+" => "+key+" (Sub Dominant)");
			} else {
				key = new Key(key.getNote(5-1));
				System.out.println(previousKey+" => "+key+" (Dominant)");
			}
		} else {
			if ( Math.random() < 0.5 ) {
				key = new Key(key.getNote(7-1));
				System.out.println(previousKey+" => "+key+" (Leading Tone)");
			} else {
				key = new Key(key.getNote(2-1));
				System.out.println(previousKey+" => "+key+" (Supertonic)");
			}
		}
		if ( keyList.contains(key) ) {
			keyIndex = keyList.indexOf(key);
			return true;
		}
		keyList.add(key);
		keyIndex = keyList.size()-1;
		System.out.println(key+" added at "+keyIndex);
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
