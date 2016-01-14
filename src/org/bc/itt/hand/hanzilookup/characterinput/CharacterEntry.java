/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.bc.itt.hand.hanzilookup.characterinput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.bc.itt.hand.hanzilookup.ui.CharacterCanvas;
import org.bc.itt.hand.hanzilookup.ui.WrittenCharacter;
import org.bc.itt.hand.hanzilookup.ui.WrittenCharacter.SubStrokeDescriptor;
import org.bc.itt.hand.hanzilookup.ui.WrittenCharacter.WrittenPoint;
import org.bc.itt.hand.hanzilookup.ui.WrittenCharacter.WrittenStroke;
import org.bc.itt.hand.kiang.chinese.font.ChineseFontFinder;


/**
 * An app to generate character stroke data for use by HanziDict.
 * You can trace load a character by typing its unicode (i.e. 4e00)
 * in the box and hitting load.  That loads a character in the background
 * that you can use to trace.  Hit analyze to generate the substroke
 * segments.  These are displayed in the box.  Check that each segment
 * is a unique substroke and makes sense.  When done it outlook.
 * The stroke data is spit into the given OutputStream.  It can be copied
 * into a strokes text file.
 */
public class CharacterEntry extends JPanel {
	
	private StrokeEntryCanvas strokeCanvas;
	
	private JTextField unicodeEntryField;
	
	private JButton loadCharButton;
	private JButton analyzeButton;
	private JButton outputButton;
	
	private PrintWriter out;
	
	public CharacterEntry(Font bgFont, OutputStream out) {
		this.initUI(bgFont);
		this.out = new PrintWriter(out);
	}
	
	private void initUI(Font bgFont) {
		bgFont = bgFont.deriveFont(250f);
		
		this.strokeCanvas = new StrokeEntryCanvas();
		this.strokeCanvas.setPreferredSize(new Dimension(250, 250));
		this.strokeCanvas.setForeground(Color.LIGHT_GRAY);
		this.strokeCanvas.setFont(bgFont);
		this.strokeCanvas.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.unicodeEntryField = new JTextField(4);

		this.loadCharButton = new JButton("load");
		this.analyzeButton = new JButton("analyze");
		this.outputButton = new JButton("output");
		
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object eventSource = e.getSource();
				
				if(eventSource == CharacterEntry.this.loadCharButton) {
					CharacterEntry.this.loadChar();
				} else if(eventSource == CharacterEntry.this.analyzeButton) {
					CharacterEntry.this.analyzeAndMark();
				} else {
					CharacterEntry.this.output();
				}
			}
		};
		
		this.loadCharButton.addActionListener(buttonListener);
		this.analyzeButton.addActionListener(buttonListener);
		this.outputButton.addActionListener(buttonListener);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(this.unicodeEntryField);
		buttonPanel.add(this.loadCharButton);
		buttonPanel.add(this.analyzeButton);
		buttonPanel.add(this.outputButton);
	
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.CENTER, this.strokeCanvas);
		this.add(BorderLayout.SOUTH, buttonPanel);
	}
	
	private void loadChar() {
		String unicodeText = this.unicodeEntryField.getText();
		try {
			this.strokeCanvas.setText(Character.toString((char)Integer.parseInt(unicodeText, 16)));		
		} catch(NumberFormatException nfe) {
			this.strokeCanvas.setText("");
			Toolkit.getDefaultToolkit().beep();
		}
		
		this.strokeCanvas.clear();
		this.strokeCanvas.repaint();
	}
	
	private void analyzeAndMark() {
		this.strokeCanvas.getCharacter().analyzeAndMark();
		this.strokeCanvas.repaint();
	}
	
	private void output() {
		WrittenCharacter character = this.strokeCanvas.getCharacter();
		
		DecimalFormat hundredths = new DecimalFormat("0.00");
		
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(this.unicodeEntryField.getText());
		for(Iterator strokeIter = character.getStrokeList().iterator(); strokeIter.hasNext();) {
			sbuf.append(" | ");
			
			WrittenStroke stroke = (WrittenStroke)strokeIter.next();
			Iterator subStrokesIter = stroke.getSubStrokes().iterator();
			if(subStrokesIter.hasNext()) {
				while(true) {
					SubStrokeDescriptor subStroke = (SubStrokeDescriptor)subStrokesIter.next();
					
					String direction = hundredths.format(subStroke.getDirection());
					String length = hundredths.format(subStroke.getLength());
					
					sbuf.append("(").append(direction).append(", ").append(length).append(")");
				
					if(subStrokesIter.hasNext()) {
						sbuf.append(" # ");
					} else {
						break;
					}
				}
			}
		}
		
		this.out.println(sbuf.toString());
		this.out.flush();
	}
	
	static private class StrokeEntryCanvas extends CharacterCanvas {
		
		static private final int POINT_RADIUS = 3;
		
		protected void paintStroke(WrittenStroke stroke, Graphics g) {
			super.paintStroke(stroke, g);
			
			for(Iterator pointIter = stroke.getPointList().iterator(); pointIter.hasNext();) {
				WrittenPoint nextPoint = (WrittenPoint)pointIter.next();
				
				Color previousColor = g.getColor();
				g.setColor(Color.RED);
				
				if(nextPoint.isPivot()) {
					this.paintPoint(nextPoint, g);
				}
				
				g.setColor(previousColor);
				
			}
		}
		
		private void paintPoint(WrittenPoint point, Graphics g) {
			double x = point.getX();
			double y = point.getY();
			
			g.fillOval((int)x - POINT_RADIUS, (int)y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
		}
	}
	
	static public void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Font chineseFont = ChineseFontFinder.getChineseFont();
		frame.getContentPane().add(new CharacterEntry(chineseFont, System.out));
		
		frame.pack();
		frame.setVisible(true);
	}
}
