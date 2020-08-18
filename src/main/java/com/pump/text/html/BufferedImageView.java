package com.pump.text.html;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.text.AbstractDocument.AbstractElement;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.ImageView;

/**
 * This View shows a BufferedImage.
 */
class BufferedImageView extends ImageView {

	// So... I'm not crazy about extending ImageView. But it appears to have
	// some nice code in it related to painting highlights and borders (and
	// maybe other stuff), so unless I do a bigger read-through of everything it
	// does: it's nice to try to automatically inherit that behavior. But it
	// also comes with some weird baggage about image loaders and almost
	// everything I want to touch is private. (If they just updated most of the
	// fields/methods to be protected I'd be more comfortable with it.)

	protected BufferedImage image;

	public BufferedImageView(Element elem, BufferedImage img) {
		super(elem);
		image = img;

		// I don't like this casting approach, but I don't see any good
		// options in the superclass to inject the behavior we want
		if (elem.getAttributes().getAttribute(HTML.Attribute.WIDTH) == null)
			((AbstractElement) elem).addAttribute(HTML.Attribute.WIDTH,
					Integer.toString(img.getWidth()));
		if (elem.getAttributes().getAttribute(HTML.Attribute.HEIGHT) == null)
			((AbstractElement) elem).addAttribute(HTML.Attribute.HEIGHT,
					Integer.toString(img.getHeight()));

		// instead of assigning dimensions, we might also (?) get
		// this to work if we use the "imageCache" property in the
		// ImageView class?
	}

	@Override
	public Image getImage() {
		return image;
	}
}
