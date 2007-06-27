package uk.org.toot.demo;

import javax.swing.JPanel;

import uk.org.toot.pitch.LydianChromaticConceptScales;

import uk.org.toot.swingui.pitchui.ChordChooser;

public class ChordChooserDemo extends AbstractDemo 
{
	public ChordChooserDemo(String[] args) {
		super(args);
	}
	
	@Override
	protected void create(String[] args) {
		LydianChromaticConceptScales.init();
		createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new ChordChooser();
        frame(panel, "Chord Chooser");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ChordChooserDemo(args);
	}

}
