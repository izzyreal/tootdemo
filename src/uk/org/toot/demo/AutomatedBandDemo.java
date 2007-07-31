// Copyright (C) 2007 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.music.composition.*;
import uk.org.toot.tonality.Scales;
import java.io.File;

public class AutomatedBandDemo 
{
	private AutomatedBand band;
	private DrumKitComposer kit;
	private TonalComposer bass;
	private KeyboardComposer piano;
	
	private int ppq = 480;
	
	public AutomatedBandDemo() {
		Scales.LydianChromaticConcept.init();
		band = new AutomatedBand();
		band.setModulationDensity(0.50f);
		band.setCycleOfFifthsDensity(0.33f);
		
		kit = new DrumKitComposer("Drums", 0, 9);
		band.addComposer(kit);
		AbstractComposer ridm = kit.getKickComposer();
		ridm.setDensity(0.60f);
		ridm.setMinNoteLen(8);
		ridm.setJamTiming(Timing.ODD_DOWNBEATS);
		ridm = kit.getLeftHandComposer();
		ridm.setDensity(0.40f);
		ridm.setMinNoteLen(16);
		ridm.setJamTiming(Timing.EVEN_DOWNBEATS);
		ridm.setClearTiming(Timing.ODD_DOWNBEATS);
		ridm = kit.getRightHandComposer();
		ridm.setDensity(0.50f);
		ridm.setMinNoteLen(16);
		ridm.setJamTiming(Timing.ALL_UPBEATS);
		ridm.setClearTiming(1);
		
		bass = new TonalComposer("Bass", 35, 0, 30, 43); // fretless bass
		bass.setMelodyProbability(1.0f); // 100% melodic
		bass.setRepeatPitchProbability(0.25f);
		bass.setSwingRatio(1.3f);
		bass.setMaxPitchChange(5);
		bass.setDensity(0.7f);
		bass.setMinNoteLen(8);
		bass.setLevel(90);
		bass.setLegato(0.9f);
		band.addComposer(bass);
		
		piano = new KeyboardComposer("Piano", 0, 2); // acoustic grand piano
		TonalComposer pianoLeftHand = piano.getLeftHandComposer();
		pianoLeftHand.setRepeatPitchProbability(0.25f);
		pianoLeftHand.setSwingRatio(1.3f);
		pianoLeftHand.setMaxPitchChange(3); // odd numbers work best for tertian intervals
		pianoLeftHand.setDensity(0.3f);
		pianoLeftHand.setMinNoteLen(8);
		pianoLeftHand.setLevel(80);
		pianoLeftHand.setLegato(0.9f);
		pianoLeftHand.setMinPitch(42);
		pianoLeftHand.setMaxPitch(56);
		TonalComposer pianoRightHand = piano.getRightHandComposer();
		pianoRightHand.setMelodyProbability(0.75f); // melodic probability
		pianoLeftHand.setRepeatPitchProbability(0.25f);
		pianoRightHand.setSwingRatio(1.3f);
		pianoRightHand.setMaxPitchChange(5);
		pianoRightHand.setDensity(0.8f);
		pianoRightHand.setMinNoteLen(8);
		pianoRightHand.setLevel(80);
		pianoRightHand.setLegato(0.9f);
		pianoRightHand.setMinPitch(62); // slight overlap with left hand 9ths
		pianoRightHand.setMaxPitch(78);
		band.addComposer(piano);
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
