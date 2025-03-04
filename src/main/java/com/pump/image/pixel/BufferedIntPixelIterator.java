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
package com.pump.image.pixel;

/**
 * This iterates over an int array of pixels.
 */
public class BufferedIntPixelIterator implements IntPixelIterator {
	int[] data;
	int dataIndex, row;
	final int width, height, pixelType, scanSize;

	/**
	 * Create a BufferedIntPixelIterator.
	 * 
	 * @param data
	 *            the data to iterate over.
	 * @param width
	 *            the width of each row.
	 * @param height
	 *            the number of rows to iterate over.
	 * @param offset
	 *            the offset in the original data to start reading pixel data.
	 * @param scanSize
	 *            the distance from one row of pixels to the next in the pixels
	 *            array. This must be the width or larger.
	 * @param pixelType
	 *            a constant like BufferedImage.TYPE_INT_ARGB
	 */
	public BufferedIntPixelIterator(int[] data, int width, int height,
			int offset, int scanSize, int pixelType) {
		if (width <= 0)
			throw new IllegalArgumentException(
					"width must be greater than zero. (width = " + width + ")");
		if (height <= 0)
			throw new IllegalArgumentException(
					"height must be greater than zero. (height = " + height
							+ ")");

		if (scanSize < width)
			throw new IllegalArgumentException(
					"scanSize should be equal to or greater than the width (scanSize = "
							+ scanSize + ", width = " + width + ")");

		int lastIndex = offset + scanSize * height;
		if (lastIndex > data.length)
			throw new IllegalArgumentException("this array is too short");

		this.data = data;
		this.pixelType = pixelType;
		this.dataIndex = offset;
		this.width = width;
		this.height = height;
		this.scanSize = scanSize;
		row = 0;
	}

	@Override
	public int getType() {
		return pixelType;
	}

	@Override
	public boolean isDone() {
		return row >= height;
	}

	@Override
	public boolean isTopDown() {
		return true;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getMinimumArrayLength() {
		return width;
	}

	@Override
	public void skip() {
		dataIndex += scanSize;
	}

	@Override
	public void next(int[] dest) {
		System.arraycopy(data, dataIndex, dest, 0, width);
		dataIndex += scanSize;
		row++;
	}

}