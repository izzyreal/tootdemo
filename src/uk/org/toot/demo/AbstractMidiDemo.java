package uk.org.toot.demo;

import uk.org.toot.midi.core.*;
import uk.org.toot.swingui.midiui.MidiConnectionMap;
import javax.swing.JPanel;

public class AbstractMidiDemo extends AbstractDemo 
{
	private ConnectedMidiSystem system;
	
	public AbstractMidiDemo(String[] args) {
		super(args);
	}
	
	protected void create(String[] args) {
		system = new DefaultConnectedMidiSystem();
		LegacyDevices.installPlatformPorts(system);

        createUI(args);
	}

	protected void createUI(String[] args) {
        JPanel panel = new MidiConnectionMap(system);
        frame(panel, "Midi Connection Map");
	}

    public static void main(String[] args) {
        new AbstractMidiDemo(args);
    }
}
