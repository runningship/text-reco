/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.bc.itt.hand.hanziinput;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;

import org.bc.itt.hand.hanzilookup.HanziLookup;
import org.bc.itt.hand.hanzilookup.data.CharacterTypeRepository;
import org.bc.itt.hand.hanzilookup.i18n.HanziLookupBundleKeys;
import org.bc.itt.hand.hanzilookup.ui.HanziLookupUIBuilder;
import org.bc.itt.hand.kiang.chinese.font.ChineseFontFinder;


/**
 * A Chinese character handwriting input app making use of the HanziLookup component.
 * Can be used as an applet or run as a stand-alone Java app.
 */
public class HanziInput extends JApplet implements HanziLookup.CharacterSelectionListener {

	static private final String DATA_PATH = "/strokes.dat";

    private HanziLookup lookupPanel;
    private JTextArea textArea;
    
    private ResourceBundle bundle = HanziLookupBundleKeys.DEFAULT_ENGLISH_BUNDLE;
    
    /**
     * @see Applet#init()
     * 
     * Invoked directly by static main to unify applet and program cases.
     * Sets everything up in the place of a real constructor.
     */
    public void init() {
        // Try to find a decent Chinese font to start with.
        Font initialFont = ChineseFontFinder.getChineseFont();
        
        // Lookup panel on the left.
        this.lookupPanel = this.buildLookupPanel(initialFont);
        if(null != initialFont) {
        	boolean isSimplifiedFont = ChineseFontFinder.isSimplifiedFont(initialFont);
        	boolean isTraditionalFont = ChineseFontFinder.isTraditionalFont(initialFont);
        	
        	if(isSimplifiedFont && !isTraditionalFont) {
        		this.lookupPanel.setSearchType(CharacterTypeRepository.SIMPLIFIED_TYPE);
        	} else if(isTraditionalFont && !isSimplifiedFont) {
        		this.lookupPanel.setSearchType(CharacterTypeRepository.TRADITIONAL_TYPE);
        	}
        }
        
        // Text area on the right.
        this.textArea = this.buildTextArea(initialFont);
        JScrollPane textScrollPane = new JScrollPane(this.textArea);
        textScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        textScrollPane.setPreferredSize(new Dimension(300, 300));
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.lookupPanel, BorderLayout.WEST);
        contentPane.add(textScrollPane, BorderLayout.CENTER);
        
        Collection containers = new ArrayList();
        containers.add(this.textArea);
        
        // Pass along the text area so that its Font will also be set when Font is changed.
        this.setJMenuBar(this.buildMenuBar(containers));
    }
    
    /**
     * Build a HanziLookup component starting out with the given Font.
     * This goes on the left.
     * 
     * @param font the font
     * @return the lookup component
     */
    private HanziLookup buildLookupPanel(Font font) {
        
    	try {
    		InputStream compiledIn = this.getClass().getResourceAsStream(DATA_PATH);

    		HanziLookup lookup = new HanziLookup(compiledIn, font);
    		lookup.addCharacterReceiver(this);
    		
        	return lookup;
        
        } catch(IOException ioe) {
        	System.err.println("Error reading in strokes data!");
        	ioe.printStackTrace();
        	System.exit(1);
        }
        
        // unreachable
        return null;
    }
    
    /**
     * Build a text area to use as the scratch output area.
     * @param font the font to use initially
     * @return the text area.
     */
    private JTextArea buildTextArea(Font font) {
        JTextArea textArea = new JTextArea();

        textArea.setFont(font);
        textArea.setColumns(30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        return textArea;
    }
    
    /**
     * Build a menu bar containing the menu items for this app
     * @param containers a list of Containers who should also be updated when the Font is changed
     * @return the menu bar
     */
    private JMenuBar buildMenuBar(Collection containers) {
       JMenuBar menuBar = new JMenuBar();
       
       JMenu optionsMenu	= HanziLookupUIBuilder.buildOptionsMenu(this.lookupPanel, containers, this.bundle);
       JMenu editMenu		= this.buildEditMenu();
       
       menuBar.add(optionsMenu);
       menuBar.add(editMenu);
       
       return menuBar;
    }
    
    /**
     * @return the standard edit menu (cut, copy, paste).
     */
    private JMenu buildEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText("Cut");
        cutItem.setMnemonic(KeyEvent.VK_T);
        editMenu.add(cutItem);
        
        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText("Copy");
        copyItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(copyItem);
        
        JMenuItem pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteItem.setText("Paste");
        pasteItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(pasteItem);

        return editMenu;
    }
    
    /**
     * @see HanziLookup.CharacterSelectionListener
     */
    public void characterSelected(HanziLookup.CharacterSelectionEvent e) {
        try {
            // Insert the character at the caret position
            int caretPosition = this.textArea.getCaretPosition();
            this.textArea.getDocument().insertString(caretPosition, Character.toString(e.getSelectedCharacter()), null);
        } catch(BadLocationException ble) {
            ble.printStackTrace();
        }
    }
    
    /**
     * Run the app.
     * Need a main method so that it can be run stand-alone and not just as an Applet.
     * @param args
     */
    static public void main(String[] args) {
		HanziInput inputApp = new HanziInput();
		inputApp.init();	// init as if it were an applet
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(inputApp);
		
		frame.pack();
		frame.setVisible(true);
    }
}
