package uk.org.toot.demo;

import javax.swing.JPanel;

import uk.org.toot.pitch.Scales;

import uk.org.toot.swingui.pitchui.ChordChooserPanel;

public class ChordChooserDemo extends AbstractDemo 
{
	public ChordChooserDemo(String[] args) {
		super(args);
	}
	
	@Override
	protected void create(String[] args) {
		Scales.LydianChromaticConcept.init();
		createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new ChordChooserPanel();
        frame(panel, "Chord Chooser");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ChordChooserDemo(args);
	}

}
