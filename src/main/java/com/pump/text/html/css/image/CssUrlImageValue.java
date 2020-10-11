package com.pump.text.html.css.image;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;

import javax.swing.text.Document;
import javax.swing.text.View;

import com.pump.image.ImageLoader;
import com.pump.util.Cache;

public class CssUrlImageValue implements CssImageValue {
	private static final String PROPERTY_IMAGE_CACHE = CssUrlImageValue.class
			.getName() + "#imageCache";

	/**
	 * Create a BufferedImage from a url that begins with something like:
	 * "data:image/png;base64,". This currently throws an exception for any
	 * image that isn't a PNG or JPG. (We could add support for GIFs, but since
	 * that requires supporting animation it's a whole separate project.)
	 * 
	 * @param urlStr
	 *            a URL of base-64 encoded data that begins with something like
	 *            "data:image/png;base64,"
	 */
	public static BufferedImage createImageFromDataUrl(String urlStr) {
		if (!urlStr.startsWith("data:"))
			throw new IllegalArgumentException();

		int i1 = urlStr.indexOf(";", 5);
		int i2 = urlStr.indexOf(",", i1 + 1);
		String mimeType = urlStr.substring(5, i1);
		String encodingType = urlStr.substring(i1 + 1, i2);
		String data = urlStr.substring(i2 + 1);

		if (!mimeType.startsWith("image/"))
			throw new IllegalArgumentException(
					"unsupported mime type: " + mimeType);
		String fileFormat = mimeType.substring("image/".length());

		byte[] bytes;
		if (encodingType.equals("base64")) {
			bytes = Base64.getDecoder().decode(data);
		} else {
			throw new IllegalArgumentException(
					"Unsupported encoding type: " + encodingType);
		}

		if (fileFormat.equals("jpg") || fileFormat.equals("jpeg")
				|| fileFormat.equals("png")) {
			BufferedImage img = ImageLoader.createImage(
					Toolkit.getDefaultToolkit().createImage(bytes));
			return img;
		} else {
			// regarding gifs: we could support them, we just haven't
			// bothered yet.
			throw new UnsupportedOperationException(
					"This decoder does not support bas64 encoded " + fileFormat
							+ ".");
		}
	}

	/**
	 * This wrapper contains a BufferedImage, or null if an error occurred
	 * trying to load that image.
	 *
	 */
	private static class LoadedImage {
		/**
		 * This may be null if an error occurred trying to load this URL.
		 */
		private BufferedImage bi;

		private LoadedImage(BufferedImage bi) {
			this.bi = bi;
		}
	}

	private final String cssStr, urlStr;

	public CssUrlImageValue(String cssStr, String urlStr) {
		Objects.requireNonNull(cssStr);
		this.cssStr = cssStr;
		this.urlStr = urlStr;
	}

	@Override
	public void paintRectangle(Graphics2D g, View view, int x, int y, int width,
			int height) {
		Cache<String, LoadedImage> imageCache = getCache(view.getDocument());
		LoadedImage img = imageCache.get(cssStr);
		if (img == null) {
			img = new LoadedImage(createBufferedImage());
			imageCache.put(cssStr, img);
		}
		if (img.bi != null) {
			g.drawImage(img.bi, x, y, width, height, null);
		} else {
			// this probably means an error occurred loading the image
		}
	}

	protected BufferedImage createBufferedImage() {
		BufferedImage bi = null;
		try {
			if (urlStr.startsWith("data:")) {
				bi = createImageFromDataUrl(urlStr);
			} else {
				URL url = new URL(urlStr);
				bi = ImageLoader.createImage(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bi;
	}

	@SuppressWarnings("unchecked")
	protected Cache<String, LoadedImage> getCache(Document doc) {
		Cache<String, LoadedImage> cache = (Cache<String, LoadedImage>) doc
				.getProperty(PROPERTY_IMAGE_CACHE);
		if (cache == null) {
			cache = new Cache<>(100, 10000, 500);
			doc.putProperty(PROPERTY_IMAGE_CACHE, cache);
		}
		return cache;
	}

	@Override
	public String toString() {
		return "CssUrlImageValue[ " + toCSSString() + "]";
	}

	@Override
	public String toCSSString() {
		return cssStr;
	}

}