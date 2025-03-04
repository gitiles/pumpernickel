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
package com.pump.showcase.demo;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.pump.inspector.Inspector;
import com.pump.plaf.CircularProgressBarUI;
import com.pump.swing.JColorWell;
import com.pump.swing.popover.JPopover;

/**
 * This demos the CircularProgressBarUI.
 * <p>
 * Here is a sample screenshot of this showcase demo:
 * <p>
 * <img src=
 * "https://github.com/mickleness/pumpernickel/raw/master/resources/showcase/CircularProgressBarUIDemo.png"
 * alt="A screenshot of the CircularProgressBarUIDemo.">
 */
public class CircularProgressBarUIDemo extends ShowcaseExampleDemo {
	private static final long serialVersionUID = 1L;

	JSlider sizeSlider = new ShowcaseSlider(10, 120, 90);
	JRadioButton indeterminateButton = new JRadioButton("Indeterminate", false);
	JRadioButton determinateButton = new JRadioButton("Determinate", true);
	JProgressBar progressBar = new JProgressBar(0, 100);
	JSpinner progressSpinner = new JSpinner(
			new SpinnerNumberModel(0, 0, 100, 5));
	JRadioButton animateOnButton = new JRadioButton("On");
	JRadioButton animateOffButton = new JRadioButton("Off", true);
	JColorWell foregroundColor = new JColorWell(
			CircularProgressBarUI.COLOR_DEFAULT_FOREGROUND);
	JColorWell backgroundColor = new JColorWell(
			CircularProgressBarUI.COLOR_DEFAULT_BACKGROUND);
	JRadioButton stringOnButton = new JRadioButton("On", true);
	JRadioButton stringOffButton = new JRadioButton("Off", false);
	JCheckBox pulseCheckBox = new JCheckBox("Pulse", true);
	JCheckBox sparkCheckBox = new JCheckBox("Spark", true);
	JCheckBox transitionCheckBox = new JCheckBox("Transition", true);
	JCheckBox accelerateCheckBox = new JCheckBox("Accelerate", false);
	JSlider strokeSlider = new ShowcaseSlider(1, 50, 4);
	JCheckBox strokeCheckBox = new JCheckBox("Custom Stroke Width:");

	ChangeListener sizeListener = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			int i = sizeSlider.getValue();
			progressBar.setPreferredSize(new Dimension(i, i));
			progressBar.getParent().revalidate();
		}

	};

	ActionListener determinateListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			progressBar.setIndeterminate(indeterminateButton.isSelected());
		}

	};

	ActionListener stringListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			progressBar.setStringPainted(stringOnButton.isSelected());
		}

	};

	ActionListener effectsListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			progressBar.putClientProperty(
					CircularProgressBarUI.PROPERTY_PULSE_COMPLETION_ACTIVE,
					pulseCheckBox.isSelected());
			progressBar.putClientProperty(
					CircularProgressBarUI.PROPERTY_SPARK_ACTIVE,
					sparkCheckBox.isSelected());
			progressBar.putClientProperty(
					CircularProgressBarUI.PROPERTY_ACCELERATE,
					accelerateCheckBox.isSelected());
			progressBar.putClientProperty(
					CircularProgressBarUI.PROPERTY_TRANSITION,
					transitionCheckBox.isSelected());
		}

	};

	ActionListener animateListener = new ActionListener() {

		/**
		 * This increments the progress bar until we reach 100%, then we wait 2
		 * seconds and restart.
		 */
		Timer timer = new Timer(15, new ActionListener() {
			long timeWhen100Reached = 0;

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = ((Number) progressSpinner.getValue()).intValue();
				if (i == 100) {
					if (timeWhen100Reached == 0) {
						timeWhen100Reached = System.currentTimeMillis();
					} else {
						long elapsed = System.currentTimeMillis()
								- timeWhen100Reached;
						if (elapsed > 2000) {
							timeWhen100Reached = 0;
							i = 0;
						}
					}
				} else {
					i += 1;
				}
				progressSpinner.setValue(i);
			}

		});

		@Override
		public void actionPerformed(ActionEvent e) {
			if (animateOnButton.isSelected()) {
				if (!timer.isRunning())
					timer.start();
			} else {
				if (timer.isRunning())
					timer.stop();
			}
		}
	};

	ChangeListener spinnerListener = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			int i = ((Number) progressSpinner.getValue()).intValue();
			progressBar.setValue(i);
		}

	};

	ChangeListener colorListener = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			progressBar.setForeground(foregroundColor.getColorSelectionModel()
					.getSelectedColor());
			progressBar.setBackground(backgroundColor.getColorSelectionModel()
					.getSelectedColor());
		}

	};

	ChangeListener strokeSliderListener = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			Number w;
			if (strokeCheckBox.isSelected()) {
				w = strokeSlider.getValue();
			} else {
				w = null;
			}
			progressBar.putClientProperty(
					CircularProgressBarUI.PROPERTY_STROKE_WIDTH, w);
		}

	};

	ActionListener strokeCheckboxListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			strokeSlider.setEnabled(strokeCheckBox.isSelected());
			strokeSliderListener.stateChanged(null);
		}

	};

	public CircularProgressBarUIDemo() {
		JPopover.add(sizeSlider, " pixels");
		JPopover.add(strokeSlider, " pixels");

		Inspector layout = new Inspector(configurationPanel);
		layout.addRow(new JLabel("Size:"), sizeSlider, true);
		layout.addRow(new JLabel("Style:"), indeterminateButton,
				determinateButton);
		layout.addRow(new JLabel("Value:"), progressSpinner, false);
		layout.addRow(new JLabel("String Painted:"), stringOnButton,
				stringOffButton);
		layout.addRow(new JLabel("Animate Value:"), animateOnButton,
				animateOffButton);
		layout.addRow(new JLabel("Foreground:"), foregroundColor, false);
		layout.addRow(new JLabel("Background:"), backgroundColor, false);
		layout.addRow(new JLabel("Effects:"), pulseCheckBox, transitionCheckBox,
				sparkCheckBox, accelerateCheckBox);
		layout.addRow(strokeCheckBox, strokeSlider, true);

		ButtonGroup g1 = new ButtonGroup();
		g1.add(indeterminateButton);
		g1.add(determinateButton);

		ButtonGroup g2 = new ButtonGroup();
		g2.add(animateOnButton);
		g2.add(animateOffButton);

		ButtonGroup g3 = new ButtonGroup();
		g3.add(stringOnButton);
		g3.add(stringOffButton);

		examplePanel.add(progressBar);

		progressBar.setUI(new CircularProgressBarUI());
		progressBar.setValue(33);

		sizeSlider.addChangeListener(sizeListener);
		sizeListener.stateChanged(null);

		indeterminateButton.addActionListener(determinateListener);
		determinateButton.addActionListener(determinateListener);
		determinateListener.actionPerformed(null);

		animateOnButton.addActionListener(animateListener);
		animateOffButton.addActionListener(animateListener);
		animateListener.actionPerformed(null);

		stringOnButton.addActionListener(stringListener);
		stringOffButton.addActionListener(stringListener);
		stringListener.actionPerformed(null);

		backgroundColor.getColorSelectionModel()
				.addChangeListener(colorListener);
		foregroundColor.getColorSelectionModel()
				.addChangeListener(colorListener);
		colorListener.stateChanged(null);

		strokeSlider.addChangeListener(strokeSliderListener);
		strokeCheckBox.addActionListener(strokeCheckboxListener);
		strokeCheckboxListener.actionPerformed(null);

		pulseCheckBox.addActionListener(effectsListener);
		sparkCheckBox.addActionListener(effectsListener);
		accelerateCheckBox.addActionListener(effectsListener);
		transitionCheckBox.addActionListener(effectsListener);
		// do NOT call actionPerformed(null) here; prove in the demo that
		// the
		// defaults are interpreted as true when undefined.
		// effectsListener.actionPerformed(null);

		progressSpinner.addChangeListener(spinnerListener);

		pulseCheckBox
				.setToolTipText("Pulse the width of the arc on completion.");
		sparkCheckBox.setToolTipText(
				"Trace a a small highlight after a few seconds of inactivity.");
		accelerateCheckBox
				.setToolTipText("Render an arc that appears to accelerate.");
		transitionCheckBox.setToolTipText("Always animate to new values.");
	}

	@Override
	public String getTitle() {
		return "CircularProgressBarUI Demo";
	}

	@Override
	public String getSummary() {
		return "This demonstrates a new ProgressBarUI that renders circular arcs.";
	}

	@Override
	public URL getHelpURL() {
		return getClass().getResource("circularProgressBarUIDemo.html");
	}

	@Override
	public String[] getKeywords() {
		return new String[] { "ux", "ui", "gui", "progress", "bar",
				"determinate", "indeterminate", "progress", "feedback",
				"Swing" };
	}

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { CircularProgressBarUI.class, JProgressBar.class };
	}
}