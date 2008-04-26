// Copyright (C) 2008 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import java.io.File;
import uk.org.toot.project.SingleProject;
import uk.org.toot.project.ProjectListener;
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.audio.mixer.automation.MixerControlsMidiFileSnapshotAutomation;

public class ProjectMidiFileSnapshotAutomation extends
    MixerControlsMidiFileSnapshotAutomation
{
    private SingleProject projectManager;
    private ProjectListener projectListener;

    public ProjectMidiFileSnapshotAutomation(
        							MixerControls controls, SingleProject p) {
        super(controls, null);
        projectManager = p;
        projectListener = new ProjectListener() {
            public void open() {
		        snapshotPath = new File(projectManager.getCurrentProjectPath(),
                    											SNAPSHOT_DIR);
        		snapshotPath.mkdirs();
                configure("default");
                recall("default");
            }
            public void save() {
		        snapshotPath = new File(projectManager.getCurrentProjectPath(),
                    											SNAPSHOT_DIR);
        		snapshotPath.mkdirs();
                store("default");
            }
        };
        projectManager.addProjectListener(projectListener); // remove ??? !!! !!!
    }
}
