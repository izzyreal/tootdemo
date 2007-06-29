package uk.org.toot.demo;

import javax.swing.JPanel;

//import uk.org.toot.pitch.Scales;

import uk.org.toot.swingui.pitchui.ChordIdentifierPanel;

public class ChordIdentifierDemo extends AbstractDemo 
{
	public ChordIdentifierDemo(String[] args) {
		super(args);
	}
	
	@Override
	protected void create(String[] args) {
		//Scales.LydianChromaticConcept.init();
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
