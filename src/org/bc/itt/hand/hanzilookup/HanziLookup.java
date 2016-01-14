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

package org.bc.itt.hand.hanzilookup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.bc.itt.hand.hanzilookup.data.CharacterDescriptor;
import org.bc.itt.hand.hanzilookup.data.CharacterTypeRepository;
import org.bc.itt.hand.hanzilookup.data.MatcherThread;
import org.bc.itt.hand.hanzilookup.data.MatcherThread.ResultsHandler;
import org.bc.itt.hand.hanzilookup.data.MemoryStrokesStreamProvider;
import org.bc.itt.hand.hanzilookup.data.ResourceStrokesStreamProvider;
import org.bc.itt.hand.hanzilookup.data.StrokesDataSource;
import org.bc.itt.hand.hanzilookup.data.StrokesMatcher;
import org.bc.itt.hand.hanzilookup.data.StrokesParser;
import org.bc.itt.hand.hanzilookup.ui.CharacterCanvas;
import org.bc.itt.hand.hanzilookup.ui.CharacterCanvas.StrokeEvent;
import org.bc.itt.hand.hanzilookup.ui.CharacterCanvas.StrokesListener;
import org.bc.itt.hand.hanzilookup.ui.WrittenCharacter;
import org.bc.itt.hand.kiang.swing.JClickableList;
import org.bc.itt.hand.kiang.swing.JClickableList.ListClickedEvent;
import org.bc.itt.hand.kiang.swing.JClickableList.ListClickedListener;

/**
 * A Swing Panel that encapsulates a handwriting recognition widget for Chinese characters.
 * Contained in the panel is an input area, lookup buttons, and a JList of candidate matches.
 */
public class HanziLookup extends JPanel {
	
	static public final String SEARCH_TYPE_PREF_KEY = "search_type";
	static public final String LOOSENESS_PREF_KEY = "looseness";
	static public final String AUTOLOOKUP_PREF_KEY = "autolookup";
	static public final String MATCH_COUNT_PREF_KEY = "match_count";
	
	static public final String LOOKUP_MACRO_PREF_CODE_KEY = "lookup_macro_code";
	static public final String LOOKUP_MACRO_PREF_MODIFIERS_KEY = "lookup_macro_modifiers";
	
	static public final String CLEAR_MACRO_PREF_CODE_KEY = "clear_macro_code";
	static public final String CLEAR_MACRO_PREF_MODIFIERS_KEY = "clear_macro_modifiers";
	
	static public final String UNDO_MACRO_PREF_KEY = "undo_macro_code";
	static public final String UNDO_MACRO_PREF_MODIFIERS = "undo_macro_modifiers";
	
	// The current type of character we're looking for; set by the type radio buttons.
	// Should be one of the type constants defined in CharacterTypeRepository.
	private int searchType;
	
	// The component on which the handwriting input is done.
	private CharacterCanvas inputCanvas;
	
	// Buttons for lookup and clear functions, respectively. Undo added by enj ^_^
	private JButton lookupButton;
	private JButton clearButton;
	private JButton undoButton;
	
	// KeyStroke macros for triggering lookup and clear events
	// requested by somebody so they could program their stylus.
	private KeyStroke lookupMacro;
	private KeyStroke clearMacro;
	private KeyStroke undoMacro;
	
	// Selection list for choosing the best matches to the input character.
	private JList candidatesList;

	// StrokesDataSource abstracts access to a strokes data stream.
	private StrokesDataSource strokesDataSource;
	
	// Some settings with default initial values
	private boolean autoLookup	= true;		// whether to run a lookup automatically with each new stroke
	private double looseness 	= 0.25;		// the "looseness" of lookup, 0-1, higher == looser, looser more computationally intensive
	private int numResults		= 15;		// maximum number of results to return with each lookup
	
	// The matching Thread instance for running comparisons in a separate Thread so not to
	// lock up the event dispatch Thread.  We reuse a single Thread instance over the liftime
	// of the app, putting it to sleep when there's no comparison to be done.
	private MatcherThread matcherThread;
	
	// List of components that receive the results of this lookup widget.
	// Use a LinkedHashSet since it iterates in order.
	private Set characterHandlers = new LinkedHashSet();
	
	// Several constructors supplied.
	// Most are for legacy, the last one is preferred.
	
	/**
	 * Instantiate a new HanziLookup panel.
	 * Takes stream arguments so that the client decide how to provide access to the files (ie might be different if appletized).
	 * The two streams are plain text data files.
	 * The font should be a font capable of displaying simplified and traditional chinese characters.
	 * Usually around 20 point is appropriate, but this may vary depending on the font.
	 * 
	 * This constructor takes raw text data files and parses them, which can be slow.
	 * This is the original way of doing, you may want to try one of the other constructors.
	 * If you have a compiled version of the strokes data use one of the other constructors
	 * for faster load.  If you need a compiled version, see StrokesParser's main method.
	 *
	 * @param strokesIn an input stream to the strokes data file
	 * @param typesIn an input stream to the types data file
	 * @param font the font to use when rendering the list of closest candidates
	 * @throws IOException on io problem reading data streams
	 */
	public HanziLookup(InputStream strokesIn, InputStream typesIn, Font font) throws IOException {
		this(new StrokesDataSource(new MemoryStrokesStreamProvider(StrokesParser.getStrokeBytes(strokesIn, typesIn))), font);
	}
	
	/**
	 * A constructor to use when using a pre-compiled data file for quicker load.
	 * Like the three argument constructor, all the data is loaded into memory.
	 * 
	 * @param compiledData
	 * @param font
	 * @throws IOException
	 */
	public HanziLookup(InputStream compiledData, Font font) throws IOException {
		this(new StrokesDataSource(new MemoryStrokesStreamProvider(compiledData)), font);
	}
	
	/**
	 * A constructor to use when you don't want the strokes data file to be loaded into memory.
	 * Instead, the file will be read from the provided path as needed.  Use this to minimize
	 * the memory footprint.  The resource will be read relative to this class, so you may
	 * want to use an absolute path (starting with /) if placing your strokes resource elsewhere.
	 * 
	 * @param font
	 */
	public HanziLookup(String compiledDataResourcePath, Font font) throws IOException {
		this(new StrokesDataSource(new ResourceStrokesStreamProvider(compiledDataResourcePath)), font);
	}
	
	/**
	 * This most generic constructor.
	 * 
	 * @param strokesDataSource
	 * @param font
	 */
	public HanziLookup(StrokesDataSource strokesDataSource, Font font) {
		this.strokesDataSource = strokesDataSource;
		
		this.initMatcherThread();
		this.initUI(font);
	}
	
	/**
	 * Init the Thread that does comparisons.
	 * The thread sits idle waiting until it needs to do a comparison.
	 */
	private void initMatcherThread() {
		this.matcherThread = new MatcherThread();
		this.matcherThread.addResultsHandler(new ResultsHandler() {
			public void handleResults(Character[] results) {
				HanziLookup.this.handleResults(results);
			}
		});
		
		// no sense in holding up app shutdown, so make it a daemon Thread.
		this.matcherThread.setDaemon(true);
		// NORM_PRIORITY so it doesn't compete with event dispatch
		this.matcherThread.setPriority(Thread.NORM_PRIORITY);
		
		// Start it up.  It will immediately go to sleep waiting for a comparison.
		this.matcherThread.start();
	}
	
	private void initUI(Font font) {
		// inputPanel encapsulates the handwriting panel, lookup/clear button, and the type radio options.
		JPanel inputPanel = this.buildInputPanel();
		
		// selectPanel encapsulates the selection window of closest options.
		JPanel selectPanel = this.buildSelectionPanel();
		
		this.setFont(font);
		this.setLayout(new BorderLayout());
		this.add(inputPanel, BorderLayout.NORTH);
		this.add(selectPanel, BorderLayout.CENTER);
		
		// register default macros
		KeyStroke lookupMacro = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_MASK);
		KeyStroke clearMacro = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK);
		KeyStroke undoMacro = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK);
		this.registerLookupMacro(lookupMacro);
		this.registerClearMacro(clearMacro);
		this.registerUndoMacro(undoMacro);
	}
	
	/////////////////////
	// listener methods
	
	/**
	 * Add a CharacterSelectionListener that handles selections from this lookup component
	 * @param the listener to add
	 */
	public void addCharacterReceiver(CharacterSelectionListener listener) {
	    synchronized(this.characterHandlers) {
	        this.characterHandlers.add(listener);
	    }
	}
	
	/**
	 * Remove a CharacterSelectionListener from the currently set set of listener.
	 * @param receiver the listener to remove
	 */
	public void removeCharacterReceiver(CharacterSelectionListener listener) {
	    synchronized(this.characterHandlers) {
	        this.characterHandlers.remove(listener);
	    }
	}
	
	/**
	 * Invokes the handleCharacter method on all the current CharacterHandlers
	 * @param result the char to pass as the result
	 */
	private void notifyReceivers(char result) {
	    synchronized(this.characterHandlers) {
		    for(Iterator receiverIter = this.characterHandlers.iterator(); receiverIter.hasNext();) {
		        CharacterSelectionListener listener = (CharacterSelectionListener)receiverIter.next();
		        listener.characterSelected(new CharacterSelectionEvent(this, result));
		    }
	    }
	}
	
	public void loadOptionsFromPreferences(Preferences prefs) {
		this.searchType = prefs.getInt(SEARCH_TYPE_PREF_KEY, this.searchType);
		this.looseness = prefs.getDouble(LOOSENESS_PREF_KEY, this.looseness);
		this.autoLookup = prefs.getBoolean(AUTOLOOKUP_PREF_KEY, this.autoLookup);
		this.numResults = prefs.getInt(MATCH_COUNT_PREF_KEY, this.numResults);
		
		int lookupMacroCode = prefs.getInt(LOOKUP_MACRO_PREF_CODE_KEY, this.lookupMacro.getKeyCode());
		int lookupMacroModifiers = prefs.getInt(LOOKUP_MACRO_PREF_MODIFIERS_KEY, this.lookupMacro.getModifiers());
		this.registerLookupMacro(KeyStroke.getKeyStroke(lookupMacroCode, lookupMacroModifiers));
		
		int clearMacroCode = prefs.getInt(CLEAR_MACRO_PREF_CODE_KEY, this.clearMacro.getKeyCode());
		int clearMacroModifiers = prefs.getInt(CLEAR_MACRO_PREF_MODIFIERS_KEY, this.clearMacro.getModifiers());
		this.registerClearMacro(KeyStroke.getKeyStroke(clearMacroCode, clearMacroModifiers));
		
		int undoMacroCode = prefs.getInt(UNDO_MACRO_PREF_KEY, this.undoMacro.getKeyCode());
		int undoMacroModifiers = prefs.getInt(UNDO_MACRO_PREF_MODIFIERS, this.undoMacro.getModifiers());
		this.registerUndoMacro(KeyStroke.getKeyStroke(undoMacroCode, undoMacroModifiers));
	}
	
	public void writeOptionsToPreferences(Preferences prefs) {
		prefs.putInt(SEARCH_TYPE_PREF_KEY, this.searchType);
		prefs.putDouble(LOOSENESS_PREF_KEY, this.looseness);
		prefs.putBoolean(AUTOLOOKUP_PREF_KEY, this.autoLookup);
		prefs.putInt(MATCH_COUNT_PREF_KEY, this.numResults);
		
		prefs.putInt(LOOKUP_MACRO_PREF_CODE_KEY, this.lookupMacro.getKeyCode());
		prefs.putInt(LOOKUP_MACRO_PREF_MODIFIERS_KEY, this.lookupMacro.getModifiers());
		
		prefs.putInt(CLEAR_MACRO_PREF_CODE_KEY, this.clearMacro.getKeyCode());
		prefs.putInt(CLEAR_MACRO_PREF_MODIFIERS_KEY, this.clearMacro.getModifiers());
		
		prefs.putInt(UNDO_MACRO_PREF_KEY, this.undoMacro.getKeyCode());
		prefs.putInt(UNDO_MACRO_PREF_MODIFIERS, this.undoMacro.getModifiers());
	}
	
	/////////////////////
	
	/**
	 * Run matching in the separate matching Thread.
	 * The thread will load the results into the results window if it finishes before interrupted.
	 */
	void runLookup() {
		synchronized(this.candidatesList) {
			this.candidatesList.setModel(new DefaultListModel());
		}
	    
		WrittenCharacter writtenCharacter = this.inputCanvas.getCharacter();
		if(writtenCharacter.getStrokeList().size() == 0) {
			// Don't bother doing anything if nothing has been input yet (number of strokes == 0).
			this.handleResults(new Character[0]);
			return;
		}
			
		CharacterDescriptor inputDescriptor = writtenCharacter.buildCharacterDescriptor();
		
    	boolean searchTraditional = this.searchType == CharacterTypeRepository.GENERIC_TYPE || this.searchType == CharacterTypeRepository.TRADITIONAL_TYPE;
        boolean searchSimplified = this.searchType  == CharacterTypeRepository.GENERIC_TYPE || this.searchType == CharacterTypeRepository.SIMPLIFIED_TYPE;
    	
        StrokesMatcher matcher = new StrokesMatcher(inputDescriptor,
	            							     searchTraditional,
	            							     searchSimplified,
	            							     this.looseness,
	            							     this.numResults,
	            							     this.strokesDataSource);
	    
        // If the Thread is currently running, setting a new StrokesMatcher
        // will cause the Thread to fall out of its current matching loop
        // discarding any accumulated results.  It will then start processing
        // the newly set StrokesMatcher.
        this.matcherThread.setStrokesMatcher(matcher);
	}
	
	/**
	 * Load the given characters into the selection window.
	 * @param results the results to load
	 */
	private void handleResults(final Character[] results) {
		// invokeLater ensures that the JList updated on the
		// event dispatch Thread.  Touching it in a separate
		// Thread can lead to issues.
		SwingUtilities.invokeLater(new Runnable() {
	   		public void run() {
	    		HanziLookup.this.candidatesList.setListData(results);
	    		HanziLookup.this.lookupButton.setEnabled(true);
	   		}
		});
	}
	
	/**
	 * Resets the state of the panel.
	 */
	private void clear() {
		// Wipes the handwritten input.
		this.inputCanvas.clear();
		this.inputCanvas.repaint();
		
		// Clears the closest candidates.
		synchronized(this.candidatesList) {
			this.candidatesList.setListData(new Object[0]);
			this.candidatesList.repaint();
		}
	}
	
	private void undo() {
		// Wipes the handwritten input.
		this.inputCanvas.undo();

		if(this.autoLookup && this.inputCanvas.getCharacter().getStrokeList().size() > 0) {
			// if auto lookup enabled and the character still has some strokes
			// then run an lookup after the removed stroke
			this.runLookup();
		} else {
			// if not then just blank any current results
			this.handleResults(new Character[0]);
		}
		
		this.inputCanvas.repaint();
	}
	
	/**
	 * Trigger character lookup.
	 */
	private void lookup() {
		this.lookupButton.setEnabled(false);
		this.runLookup();
	}
	
	
	// Size of the handwriting canvas.
	static final private Dimension CANVAS_DIMENSION = new Dimension(235, 235);
	
	/**
	 * @return a new JPanel encapsulating the handwriting panel, lookup/clear button, and the type radio options.
	 */
	private JPanel buildInputPanel() {
	    this.inputCanvas = new CharacterCanvas();
		this.inputCanvas.setPreferredSize(CANVAS_DIMENSION);
		this.inputCanvas.addStrokesListener(new StrokesListener() {
			public void strokeFinished(StrokeEvent e) {
				HanziLookup.this.strokeFinished(e);
			}
		});
		
		// The lookup/clear buttons.
		JPanel canvasButtonPanel = this.buildCanvasButtons();
		
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		inputPanel.add(this.inputCanvas, BorderLayout.CENTER);
		inputPanel.add(canvasButtonPanel, BorderLayout.SOUTH);
		inputPanel.setBorder(BorderFactory.createTitledBorder("Enter character"));
		
		return inputPanel;
	}
	

	/**
	 * Builds the buttons associated with the input character and canvas (ie "Lookup" and "Clear").
	 * Also configures the actions to take when these buttons are pressed.
	 * 
	 * @return a JPanel enclosing the buttons
	 */
	private JPanel buildCanvasButtons() {
		// Have to make them final to make them accessible to the anonymous listener.
		this.lookupButton = new JButton("Lookup");
		this.clearButton = new JButton("Clear");
		this.undoButton = new JButton("Undo");
		
		// Anonymous listener handles button clicks.
		ActionListener canvasButtonListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(ae.getSource() == HanziLookup.this.lookupButton) {
					    HanziLookup.this.lookup();
					} else if (ae.getSource() == HanziLookup.this.clearButton){
						// by default must be that ae.getSource() == clearButton
						HanziLookup.this.clear();
					} else if (ae.getSource() == HanziLookup.this.undoButton){
						// by default must be that ae.getSource() == clearButton
						HanziLookup.this.undo();
					}
				}
		};
		
		this.lookupButton.addActionListener(canvasButtonListener);
		this.clearButton.addActionListener(canvasButtonListener);
		this.undoButton.addActionListener(canvasButtonListener);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));	// equally space the buttons
		buttonPanel.add(this.lookupButton);
		buttonPanel.add(this.undoButton);
		buttonPanel.add(this.clearButton);
		
		return buttonPanel;
	}
	
	// Size of the window that we select the closest options from.
	static final private Dimension SELECTION_DIMENSION = new Dimension(275, 130);
	
	/**
	 * The selection panel contains the options selection list and the selection button.
	 * 
	 * @return the selection JPanel
	 */
	private JPanel buildSelectionPanel() {
		
		this.candidatesList = this.buildCandidatesList();
		
		// Scroll pane encloses the options list in case there are more options than displayable.
		JScrollPane candidatesPane = new JScrollPane(this.candidatesList);
		candidatesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());	// BorderLayout, list in center, select button on the left
		selectionPanel.add(candidatesPane, BorderLayout.CENTER);
		selectionPanel.setPreferredSize(SELECTION_DIMENSION);
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Select character"));
		
		return selectionPanel;
	}
	
	/**
	 * @return a JList for displaying the closest matches
	 */
	private JList buildCandidatesList() {
	    final JClickableList candidatesList = new JClickableList();
		candidatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		candidatesList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		candidatesList.setVisibleRowCount(-1);	// -1 sets to maximum displayable
		((DefaultListCellRenderer)candidatesList.getCellRenderer()).setHorizontalAlignment(SwingConstants.CENTER);		
		
		candidatesList.addListClickedListener(new ListClickedListener() {
		    public void listClicked(ListClickedEvent e) {
		        Character selectedChar = (Character)candidatesList.getSelectedValue();
		        if(null != selectedChar) {
		        	HanziLookup.this.notifyReceivers(selectedChar.charValue());
		        }
		    }
		});
		
		return candidatesList;
	}
	
	/**
	 * Sets the Font on the components that compose the lookup component, including the selection window.
	 * @para m Font the font to set
	 */
	public void setFont(Font font) {
	    super.setFont(font);
	    if(null != font && null != this.candidatesList) {
	        // setFont is called once by the Swing framework before everything is set up.
	        // candidatesList hasn't been initialized yet, so need to do a null check.
	        this.candidatesList.setFont(font);
	        
	        // a change in font size means we need to redermine the JList cell size.
	        this.resetListCellWidth(font);
	    }
	}
	
	/**
	 * @see Container#validateTree()
	 * 
	 * This is a bit of hack.  The selection JList's cell spacing
	 * depends on actual size of the component.  So we need to stuff a call
	 * to resize the cells after the component has been packed.  This method
	 * appears to do that, so we override and append a resize call to it.
	 */
	protected void validateTree() {
	    super.validateTree();
	    this.resetListCellWidth(this.candidatesList.getFont());
	}
	
	/**
	 * We want the selection options JList to have characters spaced out.
	 * The spacing depends on the component width and the size of the set Font.
	 * @param font the set Font
	 */
	private void resetListCellWidth(Font font) {
	    // API says that getFontMetrics is deprecated, but I don't see another way
	    // to get one without already having a Graphics object in hand?
	    FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
	    
	    // If this method is invoked before the component is packed, then the width will be 0.
	    // In this case we use the preferred size instead, otherwise use the actual size.
	    int listWidth = this.candidatesList.getWidth();
	    listWidth = listWidth == 0 ? (int)this.candidatesList.getPreferredSize().getWidth() : listWidth;
	    
	    // A cell should be twice as wide as a character.
	    // We use \u4e00 here, but any character should pretty much have the same width.
	    int cellWidth = 2 * metrics.charWidth('\u4e00');
        this.candidatesList.setFixedCellWidth(cellWidth);
	}
	
	/**
	 * Set the type of characters that the component will compare against.
	 * @param searchType should be one of CharacterTypeRepository's constants: GENERIC_TYPE, SIMPLIFIED_TYPE, TRADITIONAL_TYPE
	 */
	public void setSearchType(int searchType) {
	    if(searchType != CharacterTypeRepository.GENERIC_TYPE 		&&
	       searchType != CharacterTypeRepository.SIMPLIFIED_TYPE	&&
	       searchType != CharacterTypeRepository.TRADITIONAL_TYPE) {   
		    throw new IllegalArgumentException("searchType invalid!");
	    }
	    
	    int previousSearchType = this.searchType;
	    this.searchType = searchType;
        
        if(this.autoLookup && previousSearchType != searchType) {
            // If the lookup type is changed, go ahead and rerun the comparison.
            this.runLookup();
        }
	}
	
	/**
	 * Gets the type of character that the component is currently looking up.
	 * Result is one of CharacterTypeRepository's constants: GENERIC_TYPE, SIMPLIFIED_TYPE, TRADITIONAL_TYPE
	 * @return the search type
	 */
	public int getSearchType() {
	    return this.searchType;
	}
	
	/**
	 * @param numResults the number of characters to return as matches
	 */
	public void setNumResults(int numResults) {
	    if(numResults < 1) {
	        throw new IllegalArgumentException("numResults must be at least 1!");
	    }
	   
	    this.numResults = numResults;
	}
	
	/**
	 * @return the set number of characters to return as matches
	 */
	public int getNumResults() {
	    return this.numResults;
	}
	
	/**
	 * Sets whether the lookup component automatically runs a comparison after every stroke input.
	 * @param autoLookup true for autolookup, false otherwise
	 */
	public void setAutoLookup(boolean autoLookup) {
	    this.autoLookup = autoLookup;
	}

	/**
	 * @return true if set to autolookup after stroke input, false otherwise
	 */
	public boolean getAutoLookup() {
	    return this.autoLookup;
	}
	
	/**
	 * Sets how loosely or strictly written stroke input needs to be.
	 * Higher levels of looseness take longer, but will give more complete results for rougher input.
	 * Lower levels are faster, but require more precise input
	 * @param looseness the looseness, 0-1 exclusive
	 */
	public void setLooseness(double looseness) {
	    if(looseness < 0.0 || looseness > 1.0) {
	        throw new IllegalArgumentException("looseness must be between 0.0 and 1.0!");
	    }
	    
	    this.looseness = looseness;
	}
	
	/**
	 * @return how loosely or strictly the comparison this component is set to, 0-1 exclusive
	 */
	public double getLooseness() {
	    return this.looseness;
	}
	
	private void strokeFinished(StrokeEvent e) {
	    if(this.autoLookup) {
	        this.runLookup();
	    }
	}
	
	public KeyStroke getLookupMacro() {
		return this.lookupMacro;
	}
	
	public KeyStroke getUndoMacro() {
		return this.undoMacro;
	}
	
	public KeyStroke getClearMacro() {
		return this.clearMacro;
	}
	
	/**
	 * Register the keyStroke as the macro for the lookup button
	 * @param keyStroke
	 */
	public void registerLookupMacro(KeyStroke keyStroke) {
		final String LOOKUP_COMMAND_KEY = "hanzilookup.lookup";
		Action lookupAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				HanziLookup.this.lookupButton.doClick();
			}
		};
		this.registerMacro(keyStroke, this.lookupMacro, LOOKUP_COMMAND_KEY, lookupAction);
		this.lookupMacro = keyStroke;
	}
	
	/**
	 * Register the keyStroke as the macro for the clear button
	 * @param keyStroke
	 */
	public void registerClearMacro(KeyStroke keyStroke) {
		final String CLEAR_COMMAND_KEY = "hanzilookup.clear";
		Action clearAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				HanziLookup.this.clearButton.doClick();
			}
		};
		this.registerMacro(keyStroke, this.clearMacro, CLEAR_COMMAND_KEY, clearAction);
		this.clearMacro = keyStroke;
	}
	
	/**
	 * Register the keyStroke as the macro for the undo button
	 * @param keyStroke
	 */
	public void registerUndoMacro(KeyStroke keyStroke) {
		final String CLEAR_COMMAND_KEY = "hanzilookup.undo";
		Action undoAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				HanziLookup.this.undoButton.doClick();
			}
		};
		this.registerMacro(keyStroke, this.undoMacro, CLEAR_COMMAND_KEY, undoAction);
		this.undoMacro = keyStroke;
	}
	
	private void registerMacro(KeyStroke newKeyStroke, KeyStroke oldKeyStroke, String command, Action action) {
		InputMap inputMap = this.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		synchronized(inputMap) {
			if(null != oldKeyStroke) {
				inputMap.remove(oldKeyStroke);
			}
			
			inputMap.put(newKeyStroke, command);
		}
		
		ActionMap actionMap = this.getActionMap();
		synchronized(actionMap) {
			actionMap.put(command, action);
		}
	}
	
	/**
	 * An interface that the client using this lookup component will need to implement somewhere.
	 * Whenever a character is selected, an event will be passed to the registered listeners.
	 */
	static public interface CharacterSelectionListener {
	    
	    /**
	     * A character has been selected.
	     * The event contains the selected character.
	     * @param e the event
	     */
	    public void characterSelected(CharacterSelectionEvent e);
	}
	
	/**
	 * An event that gets passed to CharacterSelectionListeners
	 * whenever an character is selcted.  Contains the character.
	 */
	static public class CharacterSelectionEvent extends EventObject {
	    private char character;
	    
	    private CharacterSelectionEvent(Object source, char character) {
	        super(source);
	        this.character = character;
	    }
	    
	    /**
	     * @return the selected character
	     */
	    public char getSelectedCharacter() {
	        return this.character;
	    }
	}
}
