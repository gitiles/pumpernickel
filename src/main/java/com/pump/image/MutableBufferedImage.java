/**
 * This software is released as part of the Pumpernickel project.
 * 
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://github.com/mickleness/pumpernickel/raw/master/License.txt
 * 
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package com.pump.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This is a <code>BufferedImage</code> that offers a <code>setProperty()</code>
 * method, is serializable, and supports
 * {@link #equals(Object)}/{@link #hashCode()}.
 */
public class MutableBufferedImage extends BufferedImage
		implements Externalizable {

	/**
	 * Return a map of all known properties of a BufferedImage.
	 */
	public static Map<String, Object> getProperties(BufferedImage bi) {
		Map<String, Object> returnValue = new HashMap<>();
		if (bi != null) {
			synchronized (bi) {
				String[] propNames = bi.getPropertyNames();
				if (propNames != null) {
					for (String key : propNames) {
						returnValue.put(key, bi.getProperty(key));
					}
				}
			}
		}
		return returnValue;
	}

	private static Hashtable<String, Object> getPropertiesHashtable(
			BufferedImage bi) {
		Map<String, Object> map = getProperties(bi);
		Hashtable<String, Object> returnValue = new Hashtable<>(map.size());
		returnValue.putAll(map);
		return returnValue;
	}

	Map<String, Object> extraProperties = null;

	/**
	 * This constructor should not be used. It is only available for
	 * deserialization.
	 * 
	 * @deprecated use any other constructor
	 */
	public MutableBufferedImage() {
		super(1, 1, BufferedImage.TYPE_INT_RGB);
	}

	public MutableBufferedImage(ColorModel cm, WritableRaster r,
			boolean premultiplied, Hashtable<String, Object> properties) {
		super(cm, r, premultiplied, properties);
	}

	public MutableBufferedImage(int width, int height, int imageType,
			IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public MutableBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	/**
	 * Create a MutableBufferedImage that stores data in the same raster as the
	 * argument. If you modify this image or the argument: the other will also
	 * be modified.
	 */
	public MutableBufferedImage(BufferedImage bi) {
		this(bi.getColorModel(), bi.getRaster(), bi.isAlphaPremultiplied(),
				getPropertiesHashtable(bi));
		setAccelerationPriority(bi.getAccelerationPriority());
	}

	@Override
	public synchronized Object getProperty(String name,
			ImageObserver observer) {
		if (extraProperties != null && extraProperties.containsKey(name)) {
			return extraProperties.get(name);
		}
		return super.getProperty(name, observer);
	}

	@Override
	public synchronized Object getProperty(String name) {
		if (extraProperties != null && extraProperties.containsKey(name)) {
			return extraProperties.get(name);
		}
		return super.getProperty(name);
	}

	@Override
	public synchronized String[] getPropertyNames() {
		Collection<String> returnValue = new LinkedHashSet<String>();
		String[] superNames = super.getPropertyNames();
		if (superNames != null)
			returnValue.addAll(Arrays.asList(superNames));
		if (extraProperties != null) {
			returnValue.addAll(extraProperties.keySet());
		}
		return returnValue.toArray(new String[0]);
	}

	/**
	 * Assign a property value.
	 */
	public synchronized void setProperty(String propertyName, Object value) {
		if (extraProperties == null)
			extraProperties = new HashMap<String, Object>();
		extraProperties.put(propertyName, value);
	}

	/**
	 * Assign multiple property values.
	 */
	public synchronized void setProperties(Map<String, Object> properties) {
		if (extraProperties == null)
			extraProperties = new HashMap<String, Object>();
		extraProperties.putAll(properties);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof BufferedImage))
			return false;

		int w = getWidth();
		int h = getHeight();

		BufferedImage other = (BufferedImage) o;
		if (w != other.getWidth() || h != other.getHeight()
				|| getType() != other.getType())
			return false;
		if (!getColorModel().equals(other.getColorModel()))
			return false;
		if (!getRaster().getClass().equals(other.getRaster().getClass()))
			return false;

		Object row1 = null;
		Object row2 = null;
		for (int y = 0; y < h; y++) {
			row1 = getRaster().getDataElements(0, y, w, 1, row1);
			row2 = other.getRaster().getDataElements(0, y, w, 1, row2);

			if (row1 instanceof int[]) {
				if (!Arrays.equals((int[]) row1, (int[]) row2))
					return false;
			} else if (row1 instanceof byte[]) {
				if (!Arrays.equals((byte[]) row1, (byte[]) row2))
					return false;
			} else if (row1 instanceof short[]) {
				if (!Arrays.equals((short[]) row1, (short[]) row2))
					return false;
			} else {
				// I'm not sure if this can ever happen? If it does we can scale
				// this method up
				throw new IllegalStateException(row1.getClass().getName());
			}
		}

		if (!getProperties(this).equals(getProperties(other)))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int w = getWidth();
		int h = getHeight();
		int returnValue = (getWidth() << 14) + (getHeight() << 4) + getType();

		for (Point p : new Point[] { new Point(0, 0), new Point(w - 1, 0),
				new Point(0, h - 1), new Point(w - 1, h - 1),
				new Point(w / 2, h / 2) }) {
			int argb = getRGB(p.x, p.y);
			returnValue ^= argb;
		}

		return returnValue;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ Integer.toHexString(hashCode()) + ", type = " + getType()
				+ ", colorModel = " + getColorModel() + ", raster = "
				+ getRaster() + ", properties = " + getProperties(this) + "]";
	}

	///////// serialization:

	/**
	 * Return true if this object can be serialized, or false if attempting to
	 * serialize this image will throw an IOException.
	 * <p>
	 * For example: if {@link #getType()} returns
	 * {@link BufferedImage#TYPE_CUSTOM} then this method will return false.
	 */
	public boolean isSerializationSupported() {
		return getSerializationUnsupportedReason() != null;
	}

	/**
	 * Return a String for an IOException explaining why this image shouldn't be
	 * serialized, or null if this image is serializable.
	 */
	private String getSerializationUnsupportedReason() {
		switch (getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
		case BufferedImage.TYPE_BYTE_GRAY:
		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_INT_ARGB_PRE:
		case BufferedImage.TYPE_INT_BGR:
		case BufferedImage.TYPE_INT_RGB:
			return null;

		case BufferedImage.TYPE_BYTE_BINARY:
		case BufferedImage.TYPE_BYTE_INDEXED:
			// IndexColorModels COULD be supported, I just haven't gotten around
			// to it:
			return "This MutableBufferedImage is not serializable. IndexColorModels are not supported.";

		case BufferedImage.TYPE_CUSTOM:
		default:
			return "This MutableBufferedImage is not serializable. This image type ("
					+ getType() + ") is not supported.";

		}
	}

	private transient MutableBufferedImage replacementImage;

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		String msg = getSerializationUnsupportedReason();
		if (msg != null)
			throw new IOException(msg);

		out.writeInt(0);
		out.writeInt(getWidth());
		out.writeInt(getHeight());
		out.writeInt(getType());
		out.writeFloat(getAccelerationPriority());
		out.writeObject(getProperties(this));
		out.writeObject(getRaster().getDataElements(0, 0, getWidth(),
				getHeight(), null));
	}

	@Override
	public void readExternal(ObjectInput in)
			throws IOException, ClassNotFoundException {
		int internalVersion = in.readInt();
		if (internalVersion == 0) {
			int width = in.readInt();
			int height = in.readInt();
			int type = in.readInt();
			float accelerationPriority = in.readFloat();
			Map<String, Object> properties = (Map<String, Object>) in
					.readObject();
			Object rasterData = in.readObject();

			// This approach is a little bit wasteful, because we have a large
			// primitive array and now we're about to create an array of the
			// same size and populate it.

			// If this waste comes up on profilers we can save some short-term
			// memory usage and probably get a little speed increase if create
			// our own DataBuffer from the array and create our own
			// WritableRaster. But for now I don't plan on using this in a hot
			// loop so I'm not sure that optimization is necessary.

			replacementImage = new MutableBufferedImage(width, height, type);
			replacementImage.setAccelerationPriority(accelerationPriority);
			replacementImage.getRaster().setDataElements(0, 0, width, height,
					rasterData);
			replacementImage.setProperties(properties);
		}
	}

	private Object readResolve() {
		if (replacementImage != null) {
			return replacementImage;
		}
		return this;
	}
}