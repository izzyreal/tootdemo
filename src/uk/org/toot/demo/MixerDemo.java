// Copyright (C) 2006 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import uk.org.toot.audio.core.*;
import uk.org.toot.audio.mixer.AudioMixer;

/**
 * MixerDemo simply displays the automated mixer and starts the (unstoppable)
 * transport after a short delay, if you pass any command line arguments the
 * so-called FullMixerPanel will be used instead of the preferred
 * CompactMixerPanel.
 */
public class MixerDemo extends AbstractAudioDemo 
{
	public MixerDemo() {
        transport.play();
    }

    protected void create(String[] args) {
        hasMultiTrack = false;
        super.create(args);
    }

    protected int connect(AudioMixer mixer) throws Exception {
        super.connect(mixer);
        int s = 2;
        AudioProcess p;
		int nsilence = 30;
        System.out.println("Creating "+nsilence+" Silent Inputs");
   	    for ( int i = 0; i < nsilence; i++ ) {
    		p = new SilentInputAudioProcess(ChannelFormat.STEREO, "S"+(1+i));
       	    mixer.getStrip(String.valueOf(s++)).setInputProcess(p);
        }
   	    return s;
    }

    protected void createUI(String[] args) {
        super.createUI(args);
/*	    // pass an arg for full miser, nothing for compact mixer!
        JPanel panel;
        if ( args.length > 0 ) { // !!!
      		panel = new FullMixerPanel(mixerControls);
        } else {
       		panel = new CompactMixerPanel(mixerControls);
        }
       	frame(panel, "Toot Mixer"); */
    }

    public static void main(String[] args) {
        (new MixerDemo()).create(args);
    }
}
