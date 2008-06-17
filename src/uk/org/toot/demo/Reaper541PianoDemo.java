package uk.org.toot.demo;

import java.io.File;

import uk.org.toot.music.tonality.Key;
import uk.org.toot.music.tonality.Pitch;

public class Reaper541PianoDemo extends AutomatedBandDemo
{
	private Key dflat = new Key(Pitch.classValue("Db"));
	private boolean keySet = false;
	
	protected AutomatedBand createBand() {
		AutomatedBand band = new AutomatedBand() {
			// should set keyChanges and keyChangeTimes for the next bar
			protected boolean changeKey() {
				if ( !keySet ) {
					keyChangeTimes = new int[1];
					keyChangeTimes[0] = 0; //Timing.ONE;
					keyChanges = new Key[1];
					keyChanges[0] = dflat;
					keySet = true;
					return true;
				}
				return false;
			}
		};
		return band;
	}
	
	protected void addDrumKitComposer(int channel, int program) {
	}
	
	protected void addBassComposer(int channel, int program) {
	}
	
	public static void main(String[] args) {
		Reaper541PianoDemo demo = new Reaper541PianoDemo();
        File homeDir = new File(System.getProperty("user.home"), "toot");
        File baseDir = new File(homeDir, "abdemos");
        baseDir.mkdirs();
        String filename = "541PianoDb.mid";
        File file = new File(baseDir, filename);
		demo.compose(128, file, baseDir);
	}

}
