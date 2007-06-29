package uk.org.toot.demo;

import javax.swing.JPanel;

import uk.org.toot.pitch.Scales;

import uk.org.toot.swingui.pitchui.KeyChooserPanel;

public class KeyChooserDemo extends AbstractDemo 
{
	public KeyChooserDemo(String[] args) {
		super(args);
	}
	
	@Override
	protected void create(String[] args) {
		Scales.LydianChromaticConcept.init();
		createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new KeyChooserPanel();
        frame(panel, "Key Chooser");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new KeyChooserDemo(args);
	}

}
