package uk.org.toot.demo;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.UIManager;


import uk.org.toot.swingui.tonalityui.ChordChooserPanel;
import uk.org.toot.music.tonality.Scales;

public class ChordChooserDemo extends AbstractDemo 
{
	@Override
	protected void create(String[] args) {
		Scales.LydianChromaticConcept.init();
		createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new ChordChooserPanel();
        frame(panel, "Chord Chooser");
        UIManager.put("ToolTip.background", new Color(255, 255, 225));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new ChordChooserDemo()).create(args);
	}

}
