/**
 * This software is released as part of the Pumpernickel project.
 * 
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 * 
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package com.pump.swing;

import java.awt.Color;

import javax.swing.UIManager;

import com.pump.blog.Blurb;
import com.pump.blog.ResourceSample;
import com.pump.plaf.ColorPaletteUI;

/** This is a <code>JComponent</code> that presents a selection of colors to the user.
 * 
 * @see com.pump.plaf.ColorPaletteUI
 * @see com.pump.plaf.ColorSet
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://raw.githubusercontent.com/mickleness/pumpernickel/master/pump-release/resources/samples/ColorPalette/sample.png" alt="new&#160;com.bric.swing.ColorPalette(&#160;java.awt.Color.blue&#160;)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@Blurb (
imageName = "ColorPalette.png",
title = "Colors: a ColorPalette",
releaseDate = "January 2010",
summary = "This is an isolated GUI component from <a href=\"http://javagraphics.blogspot.com/2010/01/colors-good-gui-for-selecting-colors.html\">this article</a>.\n"+
"<p>This app shows the progression of several different concepts for ColorPalettes.  (Hopefully each one is an improvement over the previous...)"
)
@ResourceSample( sample = { "new com.bric.swing.ColorPalette( java.awt.Color.blue )"} )
public class ColorPalette extends ColorComponent {
	private static final long serialVersionUID = 1L;
	private static final String uiClassID = "ColorPaletteUI";
	

	public ColorPalette(Color c) {
		this();
		setColor(color);
	}
	
	public ColorPalette() {
        updateUI();
        setRequestFocusEnabled(true);
        setFocusable(true);
	}
	
    @Override
	public String getUIClassID() {
        return uiClassID;
    }
	
    @Override
	public void updateUI() {
    	if(UIManager.getDefaults().get(uiClassID)==null) {
    		UIManager.getDefaults().put(uiClassID, "com.pump.plaf.HSLColorPaletteUI");
    	}
    	setUI((ColorPaletteUI)UIManager.getUI(this));
    }
	
	public void setUI(ColorPaletteUI ui) {
        super.setUI(ui);
	}
	
	public ColorPaletteUI getUI() {
		return (ColorPaletteUI)ui;
	}
}