// Copyright (C) 2007 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package uk.org.toot.demo;

import java.awt.Container;
import java.io.IOException;

import uk.org.toot.swingui.miscui.SwingApplication;

public abstract class AbstractDemo
{
    protected DemoProperties properties;

	public AbstractDemo(String[] args) {
        create(args);
    }

    protected abstract void create(String[] args);

    protected void waitForKeypress() {
        try {
            System.in.read();
		} catch ( IOException ioe ) {};
    }

    protected void frame(Container panel, String title) {
		SwingApplication.createFrame(panel, title);
    }

    protected String property(String key) {
        return properties.getProperty(key);
    }

    protected int intProperty(String key) {
        return Integer.parseInt(property(key));
    }

    protected int intProperty(String key, int def) {
        String prop = property(key);
        return prop == null ? def : Integer.parseInt(prop);
    }

    protected boolean booleanProperty(String key, boolean def) {
        String prop = property(key);
        return prop == null ? def : Boolean.parseBoolean(prop);
    }
}
