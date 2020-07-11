package com.pump.image.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * This mimics a {@link GaussianShadowRenderer} by applying multiple iterations
 * of a {@link FastShadowRenderer}. This class is preloaded with information
 * about how to best combine FastShadowRenderers to resemble a
 * GaussianShadowRenderer for kernel radii of up to 120 pixels.
 * <p>
 * (If you attempt to use this class for a kernel radius of over 120 or less
 * than 1: then this object immediately defers to a GaussianShadowRenderer.f)
 * <p>
 * The GaussianShadowRenderer offers a unique blur with infinite precision as
 * the radius changes. (For example: a radius of 3.31 and 3.45 will produce
 * different results.f) This renderer increases its blur in discrete chunks.
 * Usually this corresponds to an integer increase in the radius, but there are
 * a couple of cases where even two consecutive integers yield the same blur. In
 * general this effect will only really be noticeable if you're animating the
 * shadow, though. For the most part the user will observe that as the radius
 * increases the blur increases.
 * <p>
 * This renderer only allows up to a maximum of 4 FastShadowRenderer iterations.
 * And even if you allow up to 4 iterations: usually only 1 or 2 are used. The
 * preloaded information picks which iterations to use based on how closely they
 * resemble the actual Gaussian blur, and often 1 or 2 iterations provides the
 * best match.
 * <p>
 * Warning: the kernel returned by {@link #getKernel(ShadowAttributes)} is an
 * approximation.
 */
public class CompositeShadowRenderer implements ShadowRenderer {

	static TreeMap<Number, Combo> lutTwos = new TreeMap<>();
	static TreeMap<Number, Combo> lutThrees = new TreeMap<>();
	static TreeMap<Number, Combo> lutFours = new TreeMap<>();

	static {
		initialize();
		initialize2();
		initialize3();
	}

	private static void initialize() {
		lutTwos.put(Float.valueOf(1.0f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.1f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.2f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.3f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.4f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.5f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.6f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.7f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(1.8f), new Combo(1));
		lutTwos.put(Float.valueOf(1.9f), new Combo(1));
		lutTwos.put(Float.valueOf(2.0f), new Combo(1));
		lutTwos.put(Float.valueOf(2.1f), new Combo(1));
		lutTwos.put(Float.valueOf(2.2f), new Combo(1));
		lutTwos.put(Float.valueOf(2.3f), new Combo(1));
		lutTwos.put(Float.valueOf(2.4f), new Combo(1));
		lutTwos.put(Float.valueOf(2.5f), new Combo(1));
		lutTwos.put(Float.valueOf(2.6f), new Combo(1));
		lutTwos.put(Float.valueOf(2.7f), new Combo(1));
		lutTwos.put(Float.valueOf(2.8f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(2.9f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.0f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.1f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.2f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.3f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.4f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.5f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.6f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.7f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.8f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(3.9f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.0f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.1f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.2f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.3f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.4f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.5f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.6f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.7f), new Combo(1, 1));
		lutTwos.put(Float.valueOf(4.8f), new Combo(2));
		lutTwos.put(Float.valueOf(4.9f), new Combo(2));
		lutTwos.put(Float.valueOf(5.0f), new Combo(2));
		lutTwos.put(Float.valueOf(5.1f), new Combo(2));
		lutTwos.put(Float.valueOf(5.2f), new Combo(2));
		lutTwos.put(Float.valueOf(5.3f), new Combo(2));
		lutTwos.put(Float.valueOf(5.4f), new Combo(2));
		lutTwos.put(Float.valueOf(5.5f), new Combo(2));
		lutTwos.put(Float.valueOf(5.6f), new Combo(1));
		lutTwos.put(Float.valueOf(5.7f), new Combo(1));
		lutTwos.put(Float.valueOf(5.8f), new Combo(1));
		lutTwos.put(Float.valueOf(5.9f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.0f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.1f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.2f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.3f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.4f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.5f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.6f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.7f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.8f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(6.9f), new Combo(2, 2));
		lutTwos.put(Float.valueOf(7.0f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.1f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.2f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.3f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.4f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.5f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.6f), new Combo(1, 3));
		lutTwos.put(Float.valueOf(7.7f), new Combo(1));
		lutTwos.put(Float.valueOf(7.8f), new Combo(1));
		lutTwos.put(Float.valueOf(7.9f), new Combo(1));
		lutTwos.put(Float.valueOf(8.0f), new Combo(1));
		lutTwos.put(Float.valueOf(8.1f), new Combo(1));
		lutTwos.put(Float.valueOf(8.2f), new Combo(1));
		lutTwos.put(Float.valueOf(8.3f), new Combo(1));
		lutTwos.put(Float.valueOf(8.4f), new Combo(1));
		lutTwos.put(Float.valueOf(8.5f), new Combo(1));
		lutTwos.put(Float.valueOf(8.6f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(8.7f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(8.8f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(8.9f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.0f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.1f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.2f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.3f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.4f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.5f), new Combo(3, 3));
		lutTwos.put(Float.valueOf(9.6f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(9.7f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(9.8f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(9.9f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.0f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.1f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.2f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.3f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.4f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.5f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.6f), new Combo(2, 4));
		lutTwos.put(Float.valueOf(10.7f), new Combo(1, 5));
		lutTwos.put(Float.valueOf(10.8f), new Combo(1, 5));
		lutTwos.put(Float.valueOf(10.9f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.0f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.1f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.2f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.3f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.4f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.5f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.6f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.7f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.8f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(11.9f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(12.0f), new Combo(4, 4));
		lutTwos.put(Float.valueOf(12.1f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.2f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.3f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.4f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.5f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.6f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.7f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.8f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(12.9f), new Combo(3, 5));
		lutTwos.put(Float.valueOf(13.0f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.1f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.2f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.3f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.4f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.5f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.6f), new Combo(2, 6));
		lutTwos.put(Float.valueOf(13.7f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(13.8f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(13.9f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.0f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.1f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.2f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.3f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.4f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.5f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.6f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.7f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.8f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(14.9f), new Combo(5, 5));
		lutTwos.put(Float.valueOf(15.0f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.1f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.2f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.3f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.4f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.5f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.6f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.7f), new Combo(4, 6));
		lutTwos.put(Float.valueOf(15.8f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(15.9f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(16.0f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(16.1f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(16.2f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(16.3f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(16.4f), new Combo(3, 7));
		lutTwos.put(Float.valueOf(16.5f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(16.6f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(16.7f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(16.8f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(16.9f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(17.0f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(17.1f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(17.2f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(17.3f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(17.4f), new Combo(6, 6));
		lutTwos.put(Float.valueOf(17.5f), new Combo(5, 7));
		lutTwos.put(Float.valueOf(17.6f), new Combo(5, 7));
		lutTwos.put(Float.valueOf(17.7f), new Combo(5, 7));
		lutTwos.put(Float.valueOf(17.8f), new Combo(5, 7));
		lutTwos.put(Float.valueOf(17.9f), new Combo(5, 7));
		lutTwos.put(Float.valueOf(18.0f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.1f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.2f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.3f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.4f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.5f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.6f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.7f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.8f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(18.9f), new Combo(4, 8));
		lutTwos.put(Float.valueOf(19.0f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.1f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.2f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.3f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.4f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.5f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.6f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.7f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.8f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(19.9f), new Combo(7, 7));
		lutTwos.put(Float.valueOf(20.0f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.1f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.2f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.3f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.4f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.5f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.6f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.7f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.8f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(20.9f), new Combo(6, 8));
		lutTwos.put(Float.valueOf(21.0f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.1f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.2f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.3f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.4f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.5f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.6f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.7f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.8f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(21.9f), new Combo(5, 9));
		lutTwos.put(Float.valueOf(22.0f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.1f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.2f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.3f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.4f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.5f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.6f), new Combo(8, 8));
		lutTwos.put(Float.valueOf(22.7f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(22.8f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(22.9f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(23.0f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(23.1f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(23.2f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(23.3f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(23.4f), new Combo(7, 9));
		lutTwos.put(Float.valueOf(23.5f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(23.6f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(23.7f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(23.8f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(23.9f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.0f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.1f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.2f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.3f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.4f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.5f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.6f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.7f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.8f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(24.9f), new Combo(6, 10));
		lutTwos.put(Float.valueOf(25.0f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.1f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.2f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.3f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.4f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.5f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.6f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.7f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.8f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(25.9f), new Combo(8, 10));
		lutTwos.put(Float.valueOf(26.0f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.1f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.2f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.3f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.4f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.5f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.6f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.7f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.8f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(26.9f), new Combo(7, 11));
		lutTwos.put(Float.valueOf(27.0f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.1f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.2f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.3f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.4f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.5f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.6f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.7f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.8f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(27.9f), new Combo(6, 12));
		lutTwos.put(Float.valueOf(28.0f), new Combo(10, 10));
		lutTwos.put(Float.valueOf(28.1f), new Combo(10, 10));
		lutTwos.put(Float.valueOf(28.2f), new Combo(10, 10));
		lutTwos.put(Float.valueOf(28.3f), new Combo(10, 10));
		lutTwos.put(Float.valueOf(28.4f), new Combo(10, 10));
		lutTwos.put(Float.valueOf(28.5f), new Combo(9, 11));
		lutTwos.put(Float.valueOf(28.6f), new Combo(9, 11));
		lutTwos.put(Float.valueOf(28.7f), new Combo(9, 11));
		lutTwos.put(Float.valueOf(28.8f), new Combo(9, 11));
		lutTwos.put(Float.valueOf(28.9f), new Combo(9, 11));
		lutTwos.put(Float.valueOf(29.0f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.1f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.2f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.3f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.4f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.5f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.6f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.7f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.8f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(29.9f), new Combo(8, 12));
		lutTwos.put(Float.valueOf(30.0f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.1f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.2f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.3f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.4f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.5f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.6f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.7f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.8f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(30.9f), new Combo(7, 13));
		lutTwos.put(Float.valueOf(31.0f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.1f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.2f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.3f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.4f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.5f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.6f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.7f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.8f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(31.9f), new Combo(10, 12));
		lutTwos.put(Float.valueOf(32.0f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.1f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.2f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.3f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.4f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.5f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.6f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.7f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.8f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(32.9f), new Combo(9, 13));
		lutTwos.put(Float.valueOf(33.0f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.1f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.2f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.3f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.4f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.5f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.6f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.7f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.8f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(33.9f), new Combo(8, 14));
		lutTwos.put(Float.valueOf(34.0f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.1f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.2f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.3f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.4f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.5f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.6f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.7f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.8f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(34.9f), new Combo(11, 13));
		lutTwos.put(Float.valueOf(35.0f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.1f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.2f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.3f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.4f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.5f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.6f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.7f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.8f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(35.9f), new Combo(10, 14));
		lutTwos.put(Float.valueOf(36.0f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.1f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.2f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.3f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.4f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.5f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.6f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.7f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.8f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(36.9f), new Combo(9, 15));
		lutTwos.put(Float.valueOf(37.0f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.1f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.2f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.3f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.4f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.5f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.6f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.7f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.8f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(37.9f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.0f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.1f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.2f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.3f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.4f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.5f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.6f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.7f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.8f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(38.9f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(39.0f), new Combo(11, 15));
		lutTwos.put(Float.valueOf(39.1f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.2f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.3f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.4f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.5f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.6f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.7f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.8f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(39.9f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.0f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.1f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.2f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.3f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.4f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.5f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.6f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.7f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.8f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(40.9f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(41.0f), new Combo(10, 16));
		lutTwos.put(Float.valueOf(41.1f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.2f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.3f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.4f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.5f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.6f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.7f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.8f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(41.9f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(42.0f), new Combo(12, 16));
		lutTwos.put(Float.valueOf(42.1f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.2f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.3f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.4f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.5f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.6f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.7f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.8f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(42.9f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.0f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.1f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.2f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.3f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.4f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.5f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.6f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.7f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.8f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(43.9f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(44.0f), new Combo(11, 17));
		lutTwos.put(Float.valueOf(44.1f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.2f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.3f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.4f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.5f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.6f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.7f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.8f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(44.9f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.0f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.1f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.2f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.3f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.4f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.5f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.6f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.7f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.8f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(45.9f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.0f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.1f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.2f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.3f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.4f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.5f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.6f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.7f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.8f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(46.9f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(47.0f), new Combo(13, 17));
		lutTwos.put(Float.valueOf(47.1f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.2f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.3f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.4f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.5f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.6f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.7f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.8f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(47.9f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.0f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.1f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.2f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.3f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.4f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.5f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.6f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.7f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.8f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(48.9f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(49.0f), new Combo(14, 18));
		lutTwos.put(Float.valueOf(49.1f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.2f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.3f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.4f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.5f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.6f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.7f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.8f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(49.9f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(50.0f), new Combo(13, 19));
		lutTwos.put(Float.valueOf(50.1f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.2f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.3f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.4f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.5f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.6f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.7f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.8f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(50.9f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(51.0f), new Combo(12, 20));
		lutTwos.put(Float.valueOf(51.1f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.2f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.3f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.4f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.5f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.6f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.7f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.8f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(51.9f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(52.0f), new Combo(11, 21));
		lutTwos.put(Float.valueOf(52.1f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.2f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.3f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.4f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.5f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.6f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.7f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.8f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(52.9f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.0f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.1f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.2f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.3f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.4f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.5f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.6f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.7f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.8f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(53.9f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(54.0f), new Combo(15, 19));
		lutTwos.put(Float.valueOf(54.1f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.2f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.3f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.4f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.5f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.6f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.7f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.8f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(54.9f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(55.0f), new Combo(13, 21));
		lutTwos.put(Float.valueOf(55.1f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.2f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.3f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.4f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.5f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.6f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.7f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.8f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(55.9f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(56.0f), new Combo(17, 19));
		lutTwos.put(Float.valueOf(56.1f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.2f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.3f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.4f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.5f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.6f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.7f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.8f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(56.9f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(57.0f), new Combo(12, 22));
		lutTwos.put(Float.valueOf(57.1f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.2f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.3f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.4f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.5f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.6f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.7f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.8f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(57.9f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(58.0f), new Combo(15, 21));
		lutTwos.put(Float.valueOf(58.1f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.2f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.3f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.4f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.5f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.6f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.7f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.8f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(58.9f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.0f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.1f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.2f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.3f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.4f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.5f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.6f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.7f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.8f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(59.9f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(60.0f), new Combo(14, 22));
		lutTwos.put(Float.valueOf(60.1f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.2f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.3f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.4f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.5f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.6f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.7f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.8f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(60.9f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.0f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.1f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.2f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.3f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.4f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.5f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.6f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.7f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.8f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(61.9f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(62.0f), new Combo(19, 19));
		lutTwos.put(Float.valueOf(62.1f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.2f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.3f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.4f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.5f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.6f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.7f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.8f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(62.9f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(63.0f), new Combo(12, 24));
		lutTwos.put(Float.valueOf(63.1f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.2f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.3f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.4f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.5f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.6f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.7f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.8f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(63.9f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(64.0f), new Combo(11, 25));
		lutTwos.put(Float.valueOf(64.1f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.2f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.3f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.4f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.5f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.6f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.7f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.8f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(64.9f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.0f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.1f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.2f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.3f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.4f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.5f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.6f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.7f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.8f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(65.9f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(66.0f), new Combo(13, 25));
		lutTwos.put(Float.valueOf(66.1f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.2f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.3f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.4f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.5f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.6f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.7f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.8f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(66.9f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(67.0f), new Combo(14, 24));
		lutTwos.put(Float.valueOf(67.1f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.2f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.3f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.4f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.5f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.6f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.7f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.8f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(67.9f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.0f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.1f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.2f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.3f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.4f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.5f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.6f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.7f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.8f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(68.9f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(69.0f), new Combo(18, 22));
		lutTwos.put(Float.valueOf(69.1f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.2f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.3f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.4f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.5f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.6f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.7f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.8f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(69.9f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(70.0f), new Combo(19, 21));
		lutTwos.put(Float.valueOf(70.1f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.2f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.3f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.4f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.5f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.6f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.7f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.8f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(70.9f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(71.0f), new Combo(17, 23));
		lutTwos.put(Float.valueOf(71.1f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.2f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.3f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.4f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.5f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.6f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.7f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.8f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(71.9f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(72.0f), new Combo(21, 21));
		lutTwos.put(Float.valueOf(72.1f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.2f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.3f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.4f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.5f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.6f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.7f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.8f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(72.9f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.0f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.1f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.2f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.3f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.4f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.5f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.6f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.7f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.8f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(73.9f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(74.0f), new Combo(12, 28));
		lutTwos.put(Float.valueOf(74.1f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.2f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.3f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.4f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.5f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.6f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.7f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.8f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(74.9f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.0f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.1f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.2f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.3f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.4f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.5f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.6f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.7f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.8f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(75.9f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(76.0f), new Combo(14, 28));
		lutTwos.put(Float.valueOf(76.1f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.2f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.3f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.4f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.5f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.6f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.7f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.8f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(76.9f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.0f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.1f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.2f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.3f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.4f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.5f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.6f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.7f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.8f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(77.9f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.0f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.1f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.2f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.3f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.4f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.5f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.6f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.7f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.8f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(78.9f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(79.0f), new Combo(15, 27));
		lutTwos.put(Float.valueOf(79.1f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.2f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.3f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.4f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.5f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.6f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.7f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.8f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(79.9f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.0f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.1f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.2f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.3f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.4f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.5f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.6f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.7f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.8f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(80.9f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.0f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.1f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.2f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.3f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.4f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.5f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.6f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.7f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.8f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(81.9f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(82.0f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(82.1f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.2f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.3f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.4f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.5f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.6f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.7f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.8f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(82.9f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(83.0f), new Combo(18, 24));
		lutTwos.put(Float.valueOf(83.1f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.2f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.3f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.4f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.5f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.6f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.7f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.8f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(83.9f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(84.0f), new Combo(17, 25));
		lutTwos.put(Float.valueOf(84.1f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.2f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.3f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.4f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.5f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.6f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.7f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.8f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(84.9f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.0f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.1f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.2f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.3f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.4f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.5f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.6f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.7f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.8f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(85.9f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(86.0f), new Combo(22, 22));
		lutTwos.put(Float.valueOf(86.1f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.2f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.3f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.4f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.5f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.6f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.7f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.8f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(86.9f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(87.0f), new Combo(9, 29));
		lutTwos.put(Float.valueOf(87.1f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.2f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.3f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.4f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.5f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.6f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.7f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.8f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(87.9f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(88.0f), new Combo(5, 31));
		lutTwos.put(Float.valueOf(88.1f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.2f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.3f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.4f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.5f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.6f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.7f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.8f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(88.9f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(89.0f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(89.1f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.2f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.3f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.4f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.5f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.6f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.7f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.8f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(89.9f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.0f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.1f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.2f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.3f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.4f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.5f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.6f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.7f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.8f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(90.9f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.0f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.1f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.2f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.3f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.4f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.5f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.6f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.7f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.8f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(91.9f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.0f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.1f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.2f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.3f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.4f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.5f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.6f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.7f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.8f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(92.9f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(93.0f), new Combo(10, 32));
		lutTwos.put(Float.valueOf(93.1f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.2f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.3f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.4f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.5f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.6f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.7f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.8f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(93.9f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.0f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.1f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.2f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.3f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.4f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.5f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.6f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.7f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.8f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(94.9f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.0f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.1f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.2f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.3f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.4f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.5f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.6f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.7f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.8f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(95.9f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.0f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.1f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.2f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.3f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.4f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.5f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.6f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.7f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.8f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(96.9f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(97.0f), new Combo(4, 32));
		lutTwos.put(Float.valueOf(97.1f), new Combo(32));
		lutTwos.put(Float.valueOf(97.2f), new Combo(32));
		lutTwos.put(Float.valueOf(97.3f), new Combo(32));
		lutTwos.put(Float.valueOf(97.4f), new Combo(32));
		lutTwos.put(Float.valueOf(97.5f), new Combo(32));
		lutTwos.put(Float.valueOf(97.6f), new Combo(32));
		lutTwos.put(Float.valueOf(97.7f), new Combo(32));
		lutTwos.put(Float.valueOf(97.8f), new Combo(32));
		lutTwos.put(Float.valueOf(97.9f), new Combo(32));
		lutTwos.put(Float.valueOf(98.0f), new Combo(32));
		lutTwos.put(Float.valueOf(98.1f), new Combo(32));
		lutTwos.put(Float.valueOf(98.2f), new Combo(32));
		lutTwos.put(Float.valueOf(98.3f), new Combo(32));
		lutTwos.put(Float.valueOf(98.4f), new Combo(32));
		lutTwos.put(Float.valueOf(98.5f), new Combo(32));
		lutTwos.put(Float.valueOf(98.6f), new Combo(32));
		lutTwos.put(Float.valueOf(98.7f), new Combo(32));
		lutTwos.put(Float.valueOf(98.8f), new Combo(32));
		lutTwos.put(Float.valueOf(98.9f), new Combo(32));
		lutTwos.put(Float.valueOf(99.0f), new Combo(32));
		lutTwos.put(Float.valueOf(99.1f), new Combo(32));
		lutTwos.put(Float.valueOf(99.2f), new Combo(32));
		lutTwos.put(Float.valueOf(99.3f), new Combo(32));
		lutTwos.put(Float.valueOf(99.4f), new Combo(32));
		lutTwos.put(Float.valueOf(99.5f), new Combo(32));
		lutTwos.put(Float.valueOf(99.6f), new Combo(32));
		lutTwos.put(Float.valueOf(99.7f), new Combo(32));
		lutTwos.put(Float.valueOf(99.8f), new Combo(32));
		lutTwos.put(Float.valueOf(99.9f), new Combo(32));
		lutTwos.put(Float.valueOf(100.0f), new Combo(32));
		lutTwos.put(Float.valueOf(100.1f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.2f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.3f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.4f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.5f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.6f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.7f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.8f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(100.9f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.0f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.1f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.2f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.3f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.4f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.5f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.6f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.7f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.8f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(101.9f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(102.0f), new Combo(1, 31));
		lutTwos.put(Float.valueOf(102.1f), new Combo(30));
		lutTwos.put(Float.valueOf(102.2f), new Combo(30));
		lutTwos.put(Float.valueOf(102.3f), new Combo(30));
		lutTwos.put(Float.valueOf(102.4f), new Combo(30));
		lutTwos.put(Float.valueOf(102.5f), new Combo(30));
		lutTwos.put(Float.valueOf(102.6f), new Combo(30));
		lutTwos.put(Float.valueOf(102.7f), new Combo(30));
		lutTwos.put(Float.valueOf(102.8f), new Combo(30));
		lutTwos.put(Float.valueOf(102.9f), new Combo(30));
		lutTwos.put(Float.valueOf(103.0f), new Combo(30));
		lutTwos.put(Float.valueOf(103.1f), new Combo(30));
		lutTwos.put(Float.valueOf(103.2f), new Combo(30));
		lutTwos.put(Float.valueOf(103.3f), new Combo(30));
		lutTwos.put(Float.valueOf(103.4f), new Combo(30));
		lutTwos.put(Float.valueOf(103.5f), new Combo(30));
		lutTwos.put(Float.valueOf(103.6f), new Combo(30));
		lutTwos.put(Float.valueOf(103.7f), new Combo(30));
		lutTwos.put(Float.valueOf(103.8f), new Combo(30));
		lutTwos.put(Float.valueOf(103.9f), new Combo(30));
		lutTwos.put(Float.valueOf(104.0f), new Combo(30));
		lutTwos.put(Float.valueOf(104.1f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.2f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.3f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.4f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.5f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.6f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.7f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.8f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(104.9f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.0f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.1f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.2f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.3f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.4f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.5f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.6f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.7f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.8f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(105.9f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(106.0f), new Combo(1, 29));
		lutTwos.put(Float.valueOf(106.1f), new Combo(28));
		lutTwos.put(Float.valueOf(106.2f), new Combo(28));
		lutTwos.put(Float.valueOf(106.3f), new Combo(28));
		lutTwos.put(Float.valueOf(106.4f), new Combo(28));
		lutTwos.put(Float.valueOf(106.5f), new Combo(28));
		lutTwos.put(Float.valueOf(106.6f), new Combo(28));
		lutTwos.put(Float.valueOf(106.7f), new Combo(28));
		lutTwos.put(Float.valueOf(106.8f), new Combo(28));
		lutTwos.put(Float.valueOf(106.9f), new Combo(28));
		lutTwos.put(Float.valueOf(107.0f), new Combo(28));
		lutTwos.put(Float.valueOf(107.1f), new Combo(28));
		lutTwos.put(Float.valueOf(107.2f), new Combo(28));
		lutTwos.put(Float.valueOf(107.3f), new Combo(28));
		lutTwos.put(Float.valueOf(107.4f), new Combo(28));
		lutTwos.put(Float.valueOf(107.5f), new Combo(28));
		lutTwos.put(Float.valueOf(107.6f), new Combo(28));
		lutTwos.put(Float.valueOf(107.7f), new Combo(28));
		lutTwos.put(Float.valueOf(107.8f), new Combo(28));
		lutTwos.put(Float.valueOf(107.9f), new Combo(28));
		lutTwos.put(Float.valueOf(108.0f), new Combo(28));
		lutTwos.put(Float.valueOf(108.1f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.2f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.3f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.4f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.5f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.6f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.7f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.8f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(108.9f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.0f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.1f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.2f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.3f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.4f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.5f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.6f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.7f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.8f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(109.9f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(110.0f), new Combo(1, 27));
		lutTwos.put(Float.valueOf(110.1f), new Combo(26));
		lutTwos.put(Float.valueOf(110.2f), new Combo(26));
		lutTwos.put(Float.valueOf(110.3f), new Combo(26));
		lutTwos.put(Float.valueOf(110.4f), new Combo(26));
		lutTwos.put(Float.valueOf(110.5f), new Combo(26));
		lutTwos.put(Float.valueOf(110.6f), new Combo(26));
		lutTwos.put(Float.valueOf(110.7f), new Combo(26));
		lutTwos.put(Float.valueOf(110.8f), new Combo(26));
		lutTwos.put(Float.valueOf(110.9f), new Combo(26));
		lutTwos.put(Float.valueOf(111.0f), new Combo(26));
		lutTwos.put(Float.valueOf(111.1f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.2f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.3f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.4f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.5f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.6f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.7f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.8f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(111.9f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(112.0f), new Combo(1, 25));
		lutTwos.put(Float.valueOf(112.1f), new Combo(24));
		lutTwos.put(Float.valueOf(112.2f), new Combo(24));
		lutTwos.put(Float.valueOf(112.3f), new Combo(24));
		lutTwos.put(Float.valueOf(112.4f), new Combo(24));
		lutTwos.put(Float.valueOf(112.5f), new Combo(24));
		lutTwos.put(Float.valueOf(112.6f), new Combo(24));
		lutTwos.put(Float.valueOf(112.7f), new Combo(24));
		lutTwos.put(Float.valueOf(112.8f), new Combo(24));
		lutTwos.put(Float.valueOf(112.9f), new Combo(24));
		lutTwos.put(Float.valueOf(113.0f), new Combo(24));
		lutTwos.put(Float.valueOf(113.1f), new Combo(24));
		lutTwos.put(Float.valueOf(113.2f), new Combo(24));
		lutTwos.put(Float.valueOf(113.3f), new Combo(24));
		lutTwos.put(Float.valueOf(113.4f), new Combo(24));
		lutTwos.put(Float.valueOf(113.5f), new Combo(24));
		lutTwos.put(Float.valueOf(113.6f), new Combo(24));
		lutTwos.put(Float.valueOf(113.7f), new Combo(24));
		lutTwos.put(Float.valueOf(113.8f), new Combo(24));
		lutTwos.put(Float.valueOf(113.9f), new Combo(24));
		lutTwos.put(Float.valueOf(114.0f), new Combo(24));
		lutTwos.put(Float.valueOf(114.1f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.2f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.3f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.4f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.5f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.6f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.7f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.8f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(114.9f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(115.0f), new Combo(1, 23));
		lutTwos.put(Float.valueOf(115.1f), new Combo(22));
		lutTwos.put(Float.valueOf(115.2f), new Combo(22));
		lutTwos.put(Float.valueOf(115.3f), new Combo(22));
		lutTwos.put(Float.valueOf(115.4f), new Combo(22));
		lutTwos.put(Float.valueOf(115.5f), new Combo(22));
		lutTwos.put(Float.valueOf(115.6f), new Combo(22));
		lutTwos.put(Float.valueOf(115.7f), new Combo(22));
		lutTwos.put(Float.valueOf(115.8f), new Combo(22));
		lutTwos.put(Float.valueOf(115.9f), new Combo(22));
		lutTwos.put(Float.valueOf(116.0f), new Combo(22));
		lutTwos.put(Float.valueOf(116.1f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.2f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.3f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.4f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.5f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.6f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.7f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.8f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(116.9f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(117.0f), new Combo(1, 21));
		lutTwos.put(Float.valueOf(117.1f), new Combo(20));
		lutTwos.put(Float.valueOf(117.2f), new Combo(20));
		lutTwos.put(Float.valueOf(117.3f), new Combo(20));
		lutTwos.put(Float.valueOf(117.4f), new Combo(20));
		lutTwos.put(Float.valueOf(117.5f), new Combo(20));
		lutTwos.put(Float.valueOf(117.6f), new Combo(20));
		lutTwos.put(Float.valueOf(117.7f), new Combo(20));
		lutTwos.put(Float.valueOf(117.8f), new Combo(20));
		lutTwos.put(Float.valueOf(117.9f), new Combo(20));
		lutTwos.put(Float.valueOf(118.0f), new Combo(20));
		lutTwos.put(Float.valueOf(118.1f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.2f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.3f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.4f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.5f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.6f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.7f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.8f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(118.9f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(119.0f), new Combo(1, 19));
		lutTwos.put(Float.valueOf(119.1f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.2f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.3f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.4f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.5f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.6f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.7f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.8f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(119.9f), new Combo(1, 17));
		lutTwos.put(Float.valueOf(120.0f), new Combo(1, 17));
	}

	private static void initialize2() {
	}

	private static void initialize3() {
	}

	static class Combo implements Comparable<Combo> {

		List<Integer> sortedRadii;
		int radiiSum;

		Combo(int... fastKernelRadii) {
			radiiSum = 0;
			sortedRadii = new ArrayList<>(fastKernelRadii.length);
			for (int radius : fastKernelRadii) {
				if (radius != 0)
					sortedRadii.add(radius);
				radiiSum += radius;

			}
			Collections.sort(sortedRadii);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Combo[ ");
			sb.append("radii = {");
			sb.append(sortedRadii.get(0));
			for (int a = 1; a < sortedRadii.size(); a++) {
				sb.append(",");
				sb.append(sortedRadii.get(a));
			}
			sb.append("}]");
			return sb.toString();
		}

		@Override
		public int compareTo(Combo o) {
			int max = Math.max(sortedRadii.size(), o.sortedRadii.size());
			for (int a = 0; a < max; a++) {
				int v1 = a < sortedRadii.size() ? sortedRadii.get(a) : -1;
				int v2 = a < o.sortedRadii.size() ? o.sortedRadii.get(a) : -1;
				int k = Integer.compare(v1, v2);
				if (k != 0)
					return k;
			}
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Combo))
				return false;
			Combo other = (Combo) obj;
			return compareTo(other) == 0;
		}

		@Override
		public int hashCode() {
			return sortedRadii.hashCode();
		}
	}

	int maxComposites;

	/**
	 * Create a new CompositeShadowRenderer that applies up to 3 iterations.
	 */
	public CompositeShadowRenderer() {
		this(3);
	}

	/**
	 * 
	 * @param maxComposites
	 *            the maximum number of renderings we'll use to generate a
	 *            shadow. This must be 2, 3 or 4. The higher the value the more
	 *            accurate and more expensive the result.
	 */
	public CompositeShadowRenderer(int maxComposites) {
		if (maxComposites < 2 || maxComposites > 4)
			throw new IllegalArgumentException("maxComposites (" + maxComposites
					+ ") should be a value between 2 and 4");
		this.maxComposites = maxComposites;
	}

	@Override
	public ARGBPixels createShadow(ARGBPixels srcImage, ARGBPixels destImage,
			ShadowAttributes attr) {
		float kernelRadius = attr.getShadowKernelRadius();
		if (kernelRadius < 1 || kernelRadius > 120) {
			GaussianShadowRenderer r = new GaussianShadowRenderer();
			return r.createShadow(srcImage, destImage, attr);
		}
		TreeMap<Number, Combo> map = getComboMap();
		Combo combo = map.floorEntry(kernelRadius).getValue();

		int dstWidth = srcImage.getWidth() + 2 * combo.radiiSum;
		int dstHeight = srcImage.getHeight() + 2 * combo.radiiSum;

		if (destImage == null) {
			destImage = new ARGBPixels(dstWidth, dstHeight);
		} else {
			if (destImage.getWidth() < dstWidth)
				throw new IllegalArgumentException(
						"The destination width (" + destImage.getWidth()
								+ ") must be " + dstWidth + " or greater");
			if (destImage.getHeight() < dstHeight)
				throw new IllegalArgumentException(
						"The destination height (" + destImage.getHeight()
								+ ") must be " + dstHeight + " or greater");
		}

		FastShadowRenderer r = new FastShadowRenderer();
		int x = combo.radiiSum;
		int y = combo.radiiSum;
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();

		for (int a = 0; a < combo.sortedRadii.size(); a++) {
			ShadowAttributes attr2 = new ShadowAttributes(
					combo.sortedRadii.get(a), 1);
			if (a == combo.sortedRadii.size() - 1)
				attr2.setShadowOpacity(attr.getShadowOpacity());

			if (a == 0) {
				r.createShadow(srcImage, destImage, x, y, attr2);
			} else {
				r.applyShadow(destImage, x, y, width, height, attr2);
			}

			x -= combo.sortedRadii.get(a);
			y -= combo.sortedRadii.get(a);
			width += combo.sortedRadii.get(a);
			height += combo.sortedRadii.get(a);
		}

		return destImage;
	}

	private TreeMap<Number, Combo> getComboMap() {
		if (maxComposites == 2)
			return lutTwos;
		if (maxComposites == 3)
			return lutThrees;
		if (maxComposites == 4)
			return lutFours;

		// our constructor prevent you from getting in this state:
		throw new IllegalStateException(
				"Unsupported max composites: " + maxComposites);
	}

	@Override
	public GaussianKernel getKernel(ShadowAttributes attr) {
		float kernelRadius = attr.getShadowKernelRadius();
		if (kernelRadius < 1 || kernelRadius > 120) {
			GaussianShadowRenderer r = new GaussianShadowRenderer();
			return r.getKernel(attr);
		}
		TreeMap<Number, Combo> map = getComboMap();
		Combo combo = map.floorEntry(kernelRadius).getValue();

		double[] kernel = createCombinedKernel(combo);
		int[] intKernel = new int[kernel.length];
		for (int a = 0; a < kernel.length; a++) {
			intKernel[a] = (int) (0x10000 * kernel[a]);
		}
		return new GaussianKernel(intKernel);
	}

	private double[] createCombinedKernel(Combo combo) {
		double[] t = new double[] { 1 };
		for (int fastKernelRadius : combo.sortedRadii) {
			double[] k = createSingleKernel(fastKernelRadius);
			t = distributeKernel(t, k);
		}
		return t;
	}

	private double[] distributeKernel(double[] data, double[] kernel) {
		int r = (kernel.length - 1) / 2;
		double[] returnValue = new double[data.length + 2 * r];

		for (int dataIndex = 0; dataIndex < data.length; dataIndex++) {
			for (int kernelIndex = 0; kernelIndex < kernel.length; kernelIndex++) {
				returnValue[dataIndex + kernelIndex] += data[dataIndex]
						* kernel[kernelIndex];
			}
		}

		return returnValue;
	}

	private double[] createSingleKernel(int fastKernelRadius) {
		double[] returnValue = new double[fastKernelRadius * 2 + 1];
		double z = 1.0 / ((double) returnValue.length);
		Arrays.fill(returnValue, z);
		return returnValue;
	}

}
