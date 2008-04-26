package uk.org.toot.demo;

import javax.swing.JPanel;

import uk.org.toot.project.ProjectListener;
import uk.org.toot.project.SingleTransportProject;
import uk.org.toot.midi.core.MidiSystem;
import uk.org.toot.swingui.midiui.sequenceui.GridEditor;
import uk.org.toot.swingui.midiui.sequenceui.OpenSequenceUI;
import uk.org.toot.swingui.midiui.sequenceui.SpiralEditor;
import uk.org.toot.swingui.midiui.sequenceui.WorkSpace;

public class MidiSequencerPanel extends JPanel
{
	private ProjectMidiSequencer sequencer;
    private SingleTransportProject project;
    private ProjectListener projectListener;
    private OpenSequenceUI openSeqUI;
    private WorkSpace workspace;
    
	public MidiSequencerPanel(ProjectMidiSequencer seq, final MidiSystem midiSystem) {
		sequencer = seq;
		project = sequencer.getProject();
		projectListener = new ProjectListener() {
            public void open() {
            	// ProjectMidiSequencer.setMidiSequence() has just been called
            	if ( openSeqUI != null) {
            		openSeqUI.close();
            	}
            	MidiSequencerPanel.this.removeAll();
            	if ( sequencer.getMidiSequence() == null ) return;
            	openSeqUI = new OpenSequenceUI(sequencer);
    			workspace = new WorkSpace(openSeqUI, midiSystem) ;
                workspace.getTabbedPane().addTab("Grid", new GridEditor(openSeqUI, midiSystem));
                workspace.getTabbedPane().addTab("Helix", new SpiralEditor(openSeqUI, midiSystem));
                MidiSequencerPanel.this.add(workspace);
                MidiSequencerPanel.this.revalidate();
            }
            public void save() {
            	
            }
		};
		project.addProjectListener(projectListener);
	}
}
