// Copyright (C) 2006 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import java.io.File;
import java.util.List;
import java.util.Collections;
import java.util.Observable;
import uk.org.toot.control.*;
import uk.org.toot.audio.core.*;
import uk.org.toot.audio.meter.*;
import uk.org.toot.audio.server.AudioClient;
import uk.org.toot.audio.system.AudioDevice;
import uk.org.toot.audio.system.AudioInput;
import uk.org.toot.audio.system.AudioOutput;
import uk.org.toot.transport.*;

/**
 * MultiTrackPlayer is an experimental multi-track player that has been
 * generalised from the Release 1 demo code.
 */
public class MultiTrackPlayer extends Observable implements AudioClient, AudioDevice
{
    private Transport transport;
    private TransportListener transportListener;
    private List<PlayerProcess> filePlayers; // !!!
    private int trackLimit = 0;
    private boolean playing = false;
    private boolean playRequest = false;
    private boolean stopRequest = false;
//    private long locateRequest = -1;
	private boolean enabled = true;

    /**
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    protected MultiTrackControls multiTrackControls = null;

    public MultiTrackPlayer(Transport t) {
        transport = t;
		transportListener = new TransportListener() {
            public void play() {
                if ( enabled ) {
                    playRequest = true;
                } else {
                    playing = true;
                }
            }
			public void stop() {
                if ( enabled ) {
	                stopRequest = true;
                } else {
                    playing = false;
                }
            }
			public void record(boolean rec) {
            }
            public void locate(long microseconds) {
                MultiTrackPlayer.this.locate(microseconds);
            }
        };
        transport.addTransportListener(transportListener);
        filePlayers = new java.util.ArrayList<PlayerProcess>();
    }

    public MultiTrackPlayer(Transport t, MultiTrackControls mtc) {
        this(t);
        multiTrackControls = mtc;
        Control[] controls = multiTrackControls.getMemberControls();
        trackLimit = controls.length;
        for ( int i = 0; i < trackLimit; i++ ) {
            filePlayers.add(new PlayerProcess((AudioControlsChain)controls[i], "Tape "+(1+i)));
        }
//		filePlayers.get(0).debug = true;
    }

    public void work(int nFrames) {
        if ( !enabled ) return;
        // sync transport with dsp
        if ( stopRequest ) {
            playing = false;
        } else if ( playRequest ) {
            playing = true;
        }
        stopRequest = false;
        playRequest = false;
    }

    public void setEnabled(boolean enable) {
        enabled = enable;
    }

    protected void locate(long microseconds) {
//        System.out.println("Locate "+microseconds);
        for ( PlayerProcess player : filePlayers ) {
            player.locate(microseconds);
        }
    }

    public void setTrack(int trk, File f, String name) {
        filePlayers.get(trk).setFile(f, name);
    }

    public int getTrackLimit() { return trackLimit; }

    /**
     * PlayerProcess extends AudioFilePlayerProcess with a K-System meter and
     * for efficiency avoids metering if there is no audio connected.
     */
    public class PlayerProcess extends AudioFilePlayerProcess implements AudioOutput
    {
        private AudioControlsChain chain;
        private MeterProcess meter;
        private AudioBuffer.MetaInfo metaInfo = null;

        public PlayerProcess(AudioControlsChain controls, String location) {
        	super(location);
            chain = controls;
            // find MeterControls in controls
            for ( Control c : controls.getControls() ) {
                if ( c instanceof MeterControls ) {
		            meter = new MeterProcess((MeterControls)c);
                }
            }
        }

        // !!! !!! may need resetting on setFile ???
        private int prevPlayRet = AUDIO_DISCONNECT;

        public int processAudio(AudioBuffer buffer) {
            int ret = prevPlayRet;
            if ( playing ) {
                ret = super.processAudio(buffer); // may be AUDIO_DISCONNECT !
                prevPlayRet = ret;
            } else { //if ( prevPlayRet != AUDIO_DISCONNECT ) {
                // if we are stopped and we were previously playing
                // we need to return silence to allow effects decays!
                buffer.makeSilence();
		        attachMetaInfo(buffer);
            }

            if ( ret != AUDIO_DISCONNECT ) {
	            if ( metaInfo != buffer.getMetaInfo() ) {
   		            metaInfo = buffer.getMetaInfo();
     		        if ( metaInfo != null ) { // !!! !!! why necessary ???
           		    	chain.setMetaInfo(metaInfo);
//                        System.out.println(metaInfo.getSourceLabel());
             		}
	        	}
	            if ( meter != null ) {
    	   	        meter.processAudio(buffer);
        	   	}
            }

            return ret;
        }

        public String getLocation() {
            return location;
        }

        public String getName() {
            return location; // !!!
        }
    }

    public void closeAudio() {
        // TODO Auto-generated method stub       
    }

    public List<AudioInput> getAudioInputs() {
        return Collections.emptyList();
    }

    public List<AudioOutput> getAudioOutputs() {
        return Collections.<AudioOutput>unmodifiableList(filePlayers);
    }

    public String getName() {
        return "MultiTrack";
    }
}
