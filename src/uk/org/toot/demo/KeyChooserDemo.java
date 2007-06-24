package uk.org.toot.demo;

import javax.swing.JPanel;

import uk.org.toot.pitch.LydianChromaticConceptScales;

import uk.org.toot.swingui.pitchui.KeyChooser;

public class KeyChooserDemo extends AbstractDemo 
{
	public KeyChooserDemo(String[] args) {
		super(args);
	}
	
	@Override
	protected void create(String[] args) {
		LydianChromaticConceptScales.init();
		createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new KeyChooser();
        frame(panel, "Key Chooser");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new KeyChooserDemo(args);
	}

}
