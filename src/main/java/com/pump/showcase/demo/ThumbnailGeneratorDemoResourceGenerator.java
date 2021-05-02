package com.pump.showcase.demo;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.pump.graphics.vector.VectorImage;
import com.pump.image.thumbnail.generator.BasicThumbnailGenerator;
import com.pump.image.thumbnail.generator.ThumbnailGenerator;
import com.pump.io.IOUtils;
import com.pump.showcase.chart.BarChartRenderer;

public class ThumbnailGeneratorDemoResourceGenerator
		extends DemoResourceGenerator {

	public static void main(String[] args) throws Exception {
		ThumbnailGeneratorDemoResourceGenerator g = new ThumbnailGeneratorDemoResourceGenerator();
		g.run();
	}

	String prefix = getClass().getSimpleName() + ": ";

	public void run() throws Exception {
		File srcFile = getFile("bridge3.jpg");
		System.out
				.println(prefix + "source file: " + srcFile.getAbsolutePath());

		Thread.sleep(3000);

		Map<String, Map<String, Long>> chartData = new HashMap<>();
		chartData.put("Time", new LinkedHashMap<>());
		for (ThumbnailGenerator gs : ThumbnailGeneratorDemo.GENERATORS) {
			if (gs instanceof BasicThumbnailGenerator)
				continue;

			System.gc();
			System.runFinalization();
			System.gc();
			System.runFinalization();
			long[] times = new long[10];
			try {
				for (int a = 0; a < times.length; a++) {
					times[a] = System.currentTimeMillis();
					for (int b = 0; b < 10; b++) {
						gs.createThumbnail(srcFile, -1);
					}
					times[a] = System.currentTimeMillis() - times[a];
				}
				Arrays.sort(times);
				long time = times[times.length / 2];
				System.out.println(
						prefix + gs.getClass().getSimpleName() + " " + time);
				chartData.get("Time").put(gs.getClass().getSimpleName(), time);
			} catch (Throwable t) {
				t.printStackTrace();
				System.out.println(
						prefix + gs.getClass().getSimpleName() + " failed");
			}
		}

		BarChartRenderer renderer = new BarChartRenderer(chartData);
		VectorImage vi = new VectorImage();
		renderer.paint(vi.createGraphics(), new Dimension(300, 100));

		File jvgFile = new File("ThumbnailGeneratorDemo.jvg");
		try (FileOutputStream fileOut = new FileOutputStream(jvgFile)) {
			vi.save(fileOut);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(prefix + "chart: " + jvgFile.getAbsolutePath());

		// make a PNG version just for quick human reference:
		BufferedImage bi = renderer.paint(new Dimension(300, 100));
		File pngFile = new File("ThumbnailGeneratorDemo.png");
		ImageIO.write(bi, "png", pngFile);
		System.out.println(prefix + "chart: " + pngFile.getAbsolutePath());

		byte[] bytes = IOUtils.readBytes(jvgFile);
		String str = new String(Base64.getEncoder().encode(bytes));
		System.out.println(prefix + "base64 encoding of jvg:");
		System.out.println(str);
	}
}
