// Copyright (C) 2007 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.music.composition.*;
import uk.org.toot.tonality.Scales;
import java.io.File;
import static uk.org.toot.midi.misc.GM.*;

public class AutomatedBandDemo 
{
	private int ppq = 480;
	private AutomatedBand band;
	
	public AutomatedBandDemo() {
		Scales.LydianChromaticConcept.init();
		
		band = new AutomatedBand();
		band.setModulationDensity(0.50f);
		band.setCycleOfFifthsDensity(0.33f);
		band.setMaxKeys(5);
		
		addDrumKitComposer(0, 9);
		addBassComposer(35, 0); // fretless bass
		addKeyboardComposer(0, 2); // acoustic grand piano
	}

	protected void addDrumKitComposer(int program, int channel) {
		RhythmicComposer kickComposer = new RhythmicComposer("Kick", program, channel);
		band.addComposer(kickComposer);
		RhythmicComposer leftHandComposer = new RhythmicComposer("Drums Left", program, channel);
		band.addComposer(leftHandComposer);
		RhythmicComposer rightHandComposer = new RhythmicComposer("Drums Right", program, channel);
		band.addComposer(rightHandComposer);
		
		RhythmicComposer.Context ridmContext = new RhythmicComposer.SingleDrumContext(ACOUSTIC_BASS_DRUM);
		ridmContext.setDensity(0.60f);
		ridmContext.setMinNoteLen(8);
		ridmContext.setJamTiming(Timing.ODD_DOWNBEATS);
		kickComposer.setContext(ridmContext);
		kickComposer.setSwingRatio(1.1f);
		
		ridmContext = new RhythmicComposer.SingleDrumContext(ACOUSTIC_SNARE);
		ridmContext.setDensity(0.40f);
		ridmContext.setMinNoteLen(16);
		ridmContext.setJamTiming(Timing.EVEN_DOWNBEATS);
		ridmContext.setClearTiming(Timing.ODD_DOWNBEATS);
		leftHandComposer.setContext(ridmContext);
		leftHandComposer.setSwingRatio(1.1f);
		
		ridmContext = new RhythmicComposer.DualDrumContext(
				CLOSED_HI_HAT, 0.95f, OPEN_HI_HAT);
		ridmContext.setDensity(0.80f);
		ridmContext.setMinNoteLen(16);
		ridmContext.setJamTiming(Timing.ALL_UPBEATS);
		ridmContext.setAccent(-24);
		ridmContext.setAccentTiming(Timing.ALL_UPBEATS);
		rightHandComposer.setContext(ridmContext);
		rightHandComposer.setSwingRatio(1.1f);
	}

	protected void addBassComposer(int program, int channel) {
		TonalComposer bass = new TonalComposer("Bass", program, channel); // fretless bass
		TonalComposer.Context bassContext = new TonalComposer.Context();
		bassContext.setMelodyProbability(1.0f); // 100% melodic
		bassContext.setRepeatPitchProbability(0.25f);
		bassContext.setMinPitch(30);
		bassContext.setMaxPitch(43);
		bassContext.setMaxPitchChange(5);
		bassContext.setDensity(0.7f);
		bassContext.setMinNoteLen(8);
		bassContext.setLevel(90);
		bassContext.setLegato(0.9f);
		bass.setContext(bassContext);
		bass.setSwingRatio(1.3f);
		band.addComposer(bass);
	}
	
	protected void addKeyboardComposer(int program, int channel) {
		TonalComposer leftHandComposer = new TonalComposer("Keyboard Left", program, channel);
		band.addComposer(leftHandComposer);
		TonalComposer rightHandComposer = new TonalComposer("Keyboard Right", program, channel);
		band.addComposer(rightHandComposer);

		TonalComposer.Context pianoContext = new TonalComposer.Context();
		pianoContext.setRepeatPitchProbability(0.25f);
		pianoContext.setMaxPitchChange(3); // odd numbers work best for tertian intervals
		pianoContext.setDensity(0.3f);
		pianoContext.setMinNoteLen(8);
		pianoContext.setLevel(80);
		pianoContext.setLegato(0.9f);
		pianoContext.setMinPitch(42);
		pianoContext.setMaxPitch(56);
		leftHandComposer.setContext(pianoContext);
		leftHandComposer.setSwingRatio(1.3f);
		
		pianoContext = new TonalComposer.Context();
		pianoContext.setMelodyProbability(0.75f); // melodic probability
		pianoContext.setRepeatPitchProbability(0.25f);
		pianoContext.setMaxPitchChange(5);
		pianoContext.setDensity(0.8f);
		pianoContext.setMinNoteLen(8);
		pianoContext.setLevel(80);
		pianoContext.setLegato(0.9f);
		pianoContext.setMinPitch(62); // slight overlap with left hand 9ths
		pianoContext.setMaxPitch(78);
		rightHandComposer.setContext(pianoContext);
		rightHandComposer.setSwingRatio(1.3f);
	}
	
	/**
	 * Compose nbars of 4/4 music to the specified Standard MIDI File.
	 * @param nbars the number of bars of 4/4 to compose
	 * @param file the standard MIDI file
	 */
	public void compose(int nbars, File file) {
		try {
			band.renderFile(nbars, ppq, file);
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		AutomatedBandDemo demo = new AutomatedBandDemo();
        File homeDir = new File(System.getProperty("user.home"), "toot");
        File baseDir = new File(homeDir, "abdemos");
        baseDir.mkdirs();
        String filename = "abdemo.mid";
        File file = new File(baseDir, filename);
		demo.compose(128, file);
	}
}
