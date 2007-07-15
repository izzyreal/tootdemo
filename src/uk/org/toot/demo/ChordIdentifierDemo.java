package uk.org.toot.demo;

import javax.swing.JPanel;


import uk.org.toot.swingui.tonalityui.ChordIdentifierPanel;
import uk.org.toot.tonality.Chords;

public class ChordIdentifierDemo extends AbstractDemo 
{
	public ChordIdentifierDemo(String[] args) {
		super(args);
	}
	
	@Override
	protected void create(String[] args) {
		//Scales.LydianChromaticConcept.init();
		Chords.checkIdentifiability();
		createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new ChordIdentifierPanel();
        frame(panel, "Chord Identifier");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ChordIdentifierDemo(args);
	}

}
