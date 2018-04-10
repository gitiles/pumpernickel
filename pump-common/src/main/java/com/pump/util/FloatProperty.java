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
package com.pump.util;

public class FloatProperty extends Property<Float> {
	final float min, max;

	public FloatProperty(String name, float minValue, float maxValue,
			float defaultValue) {
		super(name);
		min = minValue;
		max = maxValue;
		if (max < min)
			throw new IllegalArgumentException("the max (" + max
					+ ") is less than the min (" + min + ")");

		setValue(defaultValue);
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}

	public void setValue(float f) {
		setValue(new Float(f));
	}

	@Override
	protected void validateValue(Float value) {
		if (value < min)
			throw new IllegalArgumentException("the value (" + value
					+ ") is less than the min (" + min + ")");
		if (value > max)
			throw new IllegalArgumentException("the value (" + value
					+ ") is greater than the max (" + max + ")");
	}
}