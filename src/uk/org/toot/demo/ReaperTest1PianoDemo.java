package uk.org.toot.demo;

import java.io.File;
import java.util.List;

import uk.org.toot.music.timing.Timing;
import uk.org.toot.music.tonality.Key;
import uk.org.toot.music.tonality.Keys;
import uk.org.toot.music.tonality.Pitch;

public class ReaperTest1PianoDemo extends AutomatedBandDemo
{
	protected AutomatedBand createBand() {
		// setup the notes we find Keys from
		int[] oneNotes = new int[2];
		oneNotes[0] = Pitch.value("E", 0);
		oneNotes[1] = Pitch.value("F", 0);
		int[] threeNotes = new int[2];
		threeNotes[0] = Pitch.value("Ab", 0);
		threeNotes[1] = Pitch.value("Db", 0);
		// setup the key lists
		final List<Key> oneKeys = Keys.withNotes(oneNotes);
		final List<Key> threeKeys = Keys.withNotes(threeNotes);
		
		AutomatedBand band = new AutomatedBand() {
			// should set keyChanges and keyChangeTimes for the next bar
			protected boolean changeKey() {
				keyChangeTimes = new int[2];
				keyChangeTimes[0] = 0; //Timing.ONE;
				keyChangeTimes[1] = Timing.HALF_NOTE;
				keyChanges = new Key[2];
				keyChanges[0] = chooseKey(oneKeys);
				keyChanges[0] = chooseKey(threeKeys);
				return true;
			}
		};
		return band;
	}
	
	protected Key chooseKey(List<Key> keys) {
		int n = (int)(Math.random() * keys.size());
		Key key = keys.get(n);
		System.out.println(key);
		return key;
	}
	
	protected void addDrumKitComposer(int channel, int program) {
	}
	
	protected void addBassComposer(int channel, int program) {
	}
	
	public static void main(String[] args) {
		ReaperTest1PianoDemo demo = new ReaperTest1PianoDemo();
        File homeDir = new File(System.getProperty("user.home"), "toot");
        File baseDir = new File(homeDir, "abdemos");
        baseDir.mkdirs();
        String filename = "reaper1.mid";
        File file = new File(baseDir, filename);
		demo.compose(128, file, baseDir);
	}

}
