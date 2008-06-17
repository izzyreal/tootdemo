// Copyright (C) 2007 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.music.MeterCoding;
import uk.org.toot.music.composition.*;
import uk.org.toot.music.performance.*;
import uk.org.toot.music.timing.*;
import uk.org.toot.music.tonality.Scales;
import java.io.File;

public class AutomatedBandDemo 
{
	private int ppq = 480;
	private AutomatedBand band;
	
	public AutomatedBandDemo() {
		Scales.LydianChromaticConcept.init();
		
		band = createBand();
		
		addKeyboardComposer(2, 0, 0); // acoustic grand piano
		addBassComposer(0, 35, 0); // fretless bass
	}

	protected AutomatedBand createBand() {
		AutomatedBand band = new AutomatedBand();
		band.setModulationDensity(0.5f);
		band.setCycleOfFifthsDensity(0.33f);
		band.setMaxKeys(5);
		band.getBarContext().setMeter(MeterCoding.createMeter(4, 4));
		return band;
	}
	
	protected void addBassComposer(int channel, int program, int variation) {
		Instrument instrument = new Instrument(channel, program);
		TonalComposer bassComposer = new TonalComposer("Bass");
		Performer bass = new Performer("Bass", instrument);
		TonalComposer.Context bassContext = new TonalComposer.Context();
		bassContext.setTimingStrategy(
				new ConventionalTimingStrategy(0.66f, Timing.EIGHTH_NOTE, 0.66f));
		bassContext.setMelodyProbability(1.0f); // 100% melodic
		bassContext.setMinPitch(30);
		bassContext.setMaxPitch(43);
		bassContext.setMaxPitchChange(7);
		bassContext.setLevel(90);
		bassContext.setLevelDeviation(8);
		bassContext.setLegato(0.9f);
		bassComposer.setContext(bassContext);
		bass.setSwingRatio(1.3f);
		band.add(bassComposer, bass);
	}
	
	protected void addKeyboardComposer(int channel, int program, int variation) {
		Instrument instrument = new Instrument(channel, program);
		TonalComposer leftHandComposer = new TonalComposer("Keys Left");
		Performer left = new Performer("Keys Left", instrument);
		band.add(leftHandComposer, left);
		TonalComposer rightHandComposer = new TonalComposer("Keys Right");
		Performer right = new Performer("Keys Right", instrument);
		band.add(rightHandComposer, right);

		TonalComposer.Context pianoContext = new TonalComposer.Context();
		pianoContext.setTimingStrategy(
				new ConventionalTimingStrategy(0.25f, Timing.QUARTER_NOTE, 0.50f));
		pianoContext.setTertianProbability(0.5f);
		pianoContext.setMaxPitchChange(3); // odd numbers work best for tertian intervals
		pianoContext.setLevel(75);
		pianoContext.setLevelDeviation(8);
		pianoContext.setLegato(0.9f);
		pianoContext.setMinPitch(42);
		pianoContext.setMaxPitch(56);
		leftHandComposer.setContext(pianoContext);
		left.setSwingRatio(1.2f);
		
		pianoContext = new TonalComposer.Context();
		JazzyTimingStrategy.Context timingContext = new JazzyTimingStrategy.Context(0.33f);
		timingContext.setProbability(Timing.SIXTEENTH_NOTE, 0f); // no sixteenths
		pianoContext.setTimingStrategy(
				new JazzyTimingStrategy(timingContext));
		pianoContext.setMaxPoly(3);
		pianoContext.setMelodyProbability(0.8f); // melodic probability, was 0.75f
		pianoContext.setMaxPitchChange(5);
		pianoContext.setLevel(80);
		pianoContext.setLevelDeviation(12);
		pianoContext.setLegato(0.8f);
		pianoContext.setMinPitch(62); // slight overlap with left hand 9ths
		pianoContext.setMaxPitch(78);
		rightHandComposer.setContext(pianoContext);
		right.setSwingRatio(1.3f);
	}
	
	/**
	 * Compose nbars of 4/4 music to the specified Standard MIDI File.
	 * @param nbars the number of bars of 4/4 to compose
	 * @param file the standard MIDI file
	 */
	public void compose(int nbars, File file, File dir) {
		try {
			band.renderFile(nbars, ppq, file, dir);
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
		demo.compose(256, file, baseDir);
	}
}
