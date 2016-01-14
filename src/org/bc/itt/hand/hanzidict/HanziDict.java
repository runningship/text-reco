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
package org.bc.itt.hand.hanzidict;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.metal.MetalIconFactory;

import org.bc.itt.hand.hanzidict.CEDICTCharacterDictionary.CEDICTStreamProvider;
import org.bc.itt.hand.hanzidict.CharacterDictionary.Entry.Definition;
import org.bc.itt.hand.hanzilookup.HanziLookup;
import org.bc.itt.hand.hanzilookup.HanziLookup.CharacterSelectionEvent;
import org.bc.itt.hand.hanzilookup.i18n.HanziLookupBundleKeys;
import org.bc.itt.hand.hanzilookup.ui.HanziLookupUIBuilder;
import org.bc.itt.hand.kiang.chinese.font.ChineseFontFinder;

/**
 * A messy adaptation of a previous dictionary app to work with
 * a newer version of the lookup engine.
 * 
 * Avert your eyes and don't look at this code.
 */
public class HanziDict extends JApplet implements HanziLookup.CharacterSelectionListener {

	static private final String RESOURCE_DICTIONARY_PREF_KEY = "resource_dictionary_pref";
	static private final String FILE_DICTIONARY_PREF_KEY = "file_dictionary_pref";
	static private final String USING_RESOURCE_DICTIONARY_PREF_KEY = "using_resource_dictionary_pref";
	static private final String FONT_PREF_KEY = "font";
	static private final String SAVE_OPTIONS_PREF = "save_options";
	
	static private final String DEFAULT_RESOURCE_DICTIONARY_PATH = "cedict_ts.u8";
	//static private final String DEFAULT_RESOURCE_DICTIONARY_PATH = "handedict.u8";
	static private final String STROKE_DATA	= "/strokes.dat";
	//static private final String STROKE_DATA	= "/strokes-extended.dat";
	
    private HanziLookup lookupPanel;
	private JEditorPane definitionTextPane;          // pane to render the definition text
	
	private CharacterDictionary dictionary;
	
	private String resourceDictionaryPath = DEFAULT_RESOURCE_DICTIONARY_PATH;
	private String fileDictionaryPath = "";
	private boolean usingResourceDictionary = true;
	
	private Font font;
	private Preferences prefs;
	
	private ResourceBundle bundle = HanziLookupBundleKeys.DEFAULT_ENGLISH_BUNDLE;
	
    public void init() {
    	if(null != this.prefs) {
    		// If there are defined preferences, then we load the dictionary
    		// options from there.
    		this.loadDictionaryOptionsFromPreferences(this.prefs);
    	}
    	
    	if(null == this.font) {
    		// If the preferences weren't defined, or didn't include a font,
    		// then we try to find a Font capable of displaying Chinese.
    		this.font = ChineseFontFinder.getChineseFont();
    	}
    	
        this.lookupPanel = this.buildLookupPanel(this.font);
        
        if(null != this.prefs) {
        	// Now prime the lookup panel with any preferences that may be populated.
        	this.lookupPanel.loadOptionsFromPreferences(this.prefs);
        }
       
        JComponent definitionPane = this.buildDefinitionPane(this.font);
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.lookupPanel, BorderLayout.WEST);
        contentPane.add(definitionPane, BorderLayout.CENTER);
         
        try {
	        if(this.usingResourceDictionary) {
	        	this.loadResourceDictionary(this.resourceDictionaryPath, null);
	        } else {
	        	this.loadFileDictionary(this.fileDictionaryPath, null);
	        }
        } catch(IOException ioe) {
        	JOptionPane.showMessageDialog(this, ioe.getMessage());
        }
        
        this.setJMenuBar(this.buildMenuBar());   
    }
    
    /**
     * Loads the dictionary preferences (dictionary paths, whether to use a resource or a file, font)
     * from the given preferences.
     * @param prefs
     */
    private void loadDictionaryOptionsFromPreferences(Preferences prefs) {
    	this.resourceDictionaryPath = prefs.get(RESOURCE_DICTIONARY_PREF_KEY, DEFAULT_RESOURCE_DICTIONARY_PATH);
    	
    	InputStream testStream = HanziLookup.class.getResourceAsStream(this.resourceDictionaryPath);
    	if(null == testStream) {
    		// test if we can read the stream.
    		// if we can then ok, otherwise revert to the default.
    		this.resourceDictionaryPath = DEFAULT_RESOURCE_DICTIONARY_PATH;
    	} else {
    		try {
    			testStream.close();
    		} catch(IOException ioe) {
    		}
    	}
    	
    	
		this.fileDictionaryPath = prefs.get(FILE_DICTIONARY_PREF_KEY, "");
		this.usingResourceDictionary = prefs.getBoolean(USING_RESOURCE_DICTIONARY_PREF_KEY, true);
		
		String fontName = prefs.get(FONT_PREF_KEY, null);
		if(null != fontName) {
			// Here we rely on the fact that this constructor doesn't blow up even
			// when you give it a bogus font name.
			this.font = new Font(fontName, Font.PLAIN, ChineseFontFinder.FONT_SIZE);
		}
    }
    
    /**
     * Write all preferences including the dictionary prefs and the internal HanziLookup prefs
     * to the given Preferences.
     * @param prefs
     */
    private void writeOptionsToPreferences(Preferences prefs) {
    	prefs.put(RESOURCE_DICTIONARY_PREF_KEY, this.resourceDictionaryPath);
    	prefs.put(FILE_DICTIONARY_PREF_KEY, this.fileDictionaryPath);
    	prefs.putBoolean(USING_RESOURCE_DICTIONARY_PREF_KEY, this.usingResourceDictionary);
    	prefs.put(FONT_PREF_KEY, this.lookupPanel.getFont().getName());
    	
    	this.lookupPanel.writeOptionsToPreferences(prefs);
    }
    
    /**
     * Load dictionary data from the given resource path.
     * The given ChangeListener (if non-null) will have stateChanged fired with
     * every new character loaded with a source of the CharacterDictionary.
     * The listener can then check the dictionary's current size.
     * This is useful for progress bars when loading.
     * 
     * @param resourcePath
     * @param progressListener
     * @throws IOException
     */
    private void loadResourceDictionary(String resourcePath, ChangeListener progressListener) throws IOException {
    	final URL resourceURL = this.getClass().getResource(resourcePath);
		if(null == resourceURL) {
			throw new MissingResourceException("Can't find resource: " + resourcePath, this.getClass().getName(), resourcePath);
		} else {
			this.loadDictionary(new CEDICTStreamProvider() {
				public InputStream getCEDICTStream() throws IOException {
					return resourceURL.openStream();
				}
			}, progressListener);
			
			HanziDict.this.resourceDictionaryPath = resourcePath;
			HanziDict.this.usingResourceDictionary = true;
		}
    }
    
    /**
     * Load the dictionary data from the given file on disk.
     * @param filePath
     * @param progressListener
     * @throws IOException
     */
    private void loadFileDictionary(String filePath, ChangeListener progressListener) throws IOException {
    	final File file = new File(filePath);
		if(!file.canRead()) {
			throw new IOException("Can't read from the specified file: " + filePath);
		} else {
			this.loadDictionary(new CEDICTStreamProvider() {
				public InputStream getCEDICTStream() throws IOException {
					return new FileInputStream(file);
				}
			}, progressListener);
			
			HanziDict.this.fileDictionaryPath = filePath;
			HanziDict.this.usingResourceDictionary = false;
		}
    }
    
    private void loadDictionary(CEDICTStreamProvider streamProvider, ChangeListener progressListener) throws IOException {
    	this.dictionary = new CEDICTCharacterDictionary(streamProvider, progressListener);
    }
    
    /**
     * @param font
     * @return the HanziLookup panel that shows the left side of the app.
     */
    private HanziLookup buildLookupPanel(Font font) {
        try {
        	HanziLookup lookup = new HanziLookup(STROKE_DATA, font);
        	
        	lookup.addCharacterReceiver(this);
    
        	return lookup;
        
        } catch(IOException ioe) {
        	ioe.printStackTrace();
        	JOptionPane.showMessageDialog(this, "Error reading in strokes data!", "Error", JOptionPane.ERROR_MESSAGE);
        	System.exit(1);
        }
        
        // unreachable
        return null;
    }
    
    /**
     * @param font
     * @return the definition pane that goes on the right side.
     */
    private JComponent buildDefinitionPane(Font font) {
		// textPane displays the character definitions
		this.definitionTextPane = new JEditorPane();
		this.definitionTextPane.setContentType("text/html; charset=UTF-8");
		this.definitionTextPane.setEditable(false);
		
		// wrap the textPane in a scroll pane so that it can scroll if necessary
		JScrollPane textPaneScrollPane = new JScrollPane(this.definitionTextPane);
		textPaneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textPaneScrollPane.setPreferredSize(new Dimension(300, 300));
		textPaneScrollPane.setMinimumSize(new Dimension(10, 10));
		textPaneScrollPane.setBorder(BorderFactory.createTitledBorder("Info"));
		
		return textPaneScrollPane;
    }
    
    /**
     * @return the menu bar
     */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        List containers = new ArrayList(1);
        containers.add(this);
        
        JMenu optionsMenu = HanziLookupUIBuilder.buildOptionsMenu(this.lookupPanel, containers, this.bundle);  
        
        try {
	        JMenuItem dictionaryChooserMenuItem = this.buildDictionaryChooserMenuItem();
	        optionsMenu.add(dictionaryChooserMenuItem);
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
        menuBar.add(optionsMenu);
        return menuBar;
    }
    
    /**
     * @return JMenuItem for selecting your dictionary
     */
    private JMenuItem buildDictionaryChooserMenuItem() {
    	JMenuItem menuItem = new JMenuItem("Choose Dictionary");
    	menuItem.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			JDialog dictionaryChooser = HanziDict.this.buildDictionaryChooserDialog();
    			HanziLookupUIBuilder.setChildComponentPosition(HanziDict.this, dictionaryChooser);
    			dictionaryChooser.setVisible(true);
    		}
    	});
    	
    	return menuItem;
    }
    
    /**
     * @return dialog for selecting either a resource or a file for reading in a separate CEDICT dictionary
     */
    private JDialog buildDictionaryChooserDialog() {
    	// Uses GridBagLayout
    	// [resource radio][resource text field]
    	// [file radio    ][file text field    ][file chooser]
    	//                 [ok][cancel]
    	
    	final JDialog dialog = new JDialog();
    	
    	dialog.setTitle("Choose Dictionary");
    	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    	
    	Container cp = dialog.getContentPane();
    	dialog.setLayout(new GridBagLayout());
    	
    	GridBagConstraints c = new GridBagConstraints();
    	
    	ButtonGroup radioGroup = new ButtonGroup();
    	
    	final JRadioButton resourceRadio = new JRadioButton("Resource:");
    	final JTextField resourcePathField = new JTextField(20);
    	resourcePathField.setText(this.resourceDictionaryPath);
    	
    	resourceRadio.setSelected(this.usingResourceDictionary);
    	radioGroup.add(resourceRadio);
    	c.weightx = 0.0;
    	c.gridx = 0;
    	c.gridy = 0;
    	c.gridwidth = 1;
    	c.fill = GridBagConstraints.NONE;
    	cp.add(resourceRadio, c);

    	
    	c.weightx = 1.0;
    	c.gridx = 1;
    	c.gridy = 0;
    	c.gridwidth = 2;
    	c.fill = GridBagConstraints.HORIZONTAL;
    	cp.add(resourcePathField, c);
    	
    	final JRadioButton fileRadio = new JRadioButton("File:");
    	final JTextField filePathField = new JTextField(20);
    	filePathField.setText(this.fileDictionaryPath);
    	
    	// Check if we can read from the file system.
    	// We first check the specified directory, then fall back on the home directory.
    	// If we can read either one then we enable reading from the file system.
    	boolean enableFileAccess = false;
    	try {
    		System.getSecurityManager().checkRead(this.fileDictionaryPath);
    		enableFileAccess = true;
    	} catch(Exception e1) {
    		try {
    			enableFileAccess = FileSystemView.getFileSystemView().getHomeDirectory().canRead();
    		} catch(Exception e2) {
    			// enableFileAccess remains false
    		}
    	}
    		
    	fileRadio.setSelected(!this.usingResourceDictionary);
    	fileRadio.setEnabled(enableFileAccess);
    	radioGroup.add(fileRadio);
    	c.weightx = 0.0;
    	c.gridx = 0;
    	c.gridy = 1;
    	c.gridwidth = 1;
    	c.fill = GridBagConstraints.HORIZONTAL;
    	cp.add(fileRadio, c);
    	
    	
    	filePathField.setText(this.fileDictionaryPath);
    	filePathField.setEnabled(enableFileAccess);
    	c.weightx = 1.0;
    	c.gridx = 1;
    	c.gridy = 1;
    	c.gridwidth = 2;
    	c.fill = GridBagConstraints.HORIZONTAL;
    	cp.add(filePathField, c);
    	
    	JButton fileChooserButton = new JButton(MetalIconFactory.getTreeFolderIcon());
    	fileChooserButton.setEnabled(enableFileAccess);
    	fileChooserButton.setMargin(new Insets(0, 0, 0, 0));
    	
    	if(enableFileAccess) {
	    	fileChooserButton.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent ae) {
	    			JFileChooser fileChooser = new JFileChooser(filePathField.getText());
	    			fileChooser.setFileHidingEnabled(false);
	    			fileChooser.showOpenDialog(fileChooser);
	    			
	    			File selectedFile = fileChooser.getSelectedFile();
	    			if(null != selectedFile) {
	    				filePathField.setText(selectedFile.getAbsolutePath());
	    			}
	    		}
	    	});
    	}
    	c.weightx = 0.0;
    	c.gridx = 3;
    	c.gridy = 1;
    	c.gridwidth = 1;
    	c.fill = GridBagConstraints.NONE;
    	cp.add(fileChooserButton, c);
    
    	JButton loadOKButton = new JButton("OK");
    	loadOKButton.addActionListener(new ActionListener() {
    		// When the OK button is clicked we try to read in a new dictionary from the selected path.
    		
            public void actionPerformed(ActionEvent ae) {
            	
        		final boolean usingResource = resourceRadio.isSelected();
        		final String dictionaryPath = usingResource ? resourcePathField.getText() : filePathField.getText();
        		
        		if(null == dictionaryPath || dictionaryPath.length() == 0) {
        			JOptionPane.showMessageDialog(dialog, "No path entered!");
        		} else {
        			final JDialog progressDialog = new JDialog();
        			progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                	
               		final JProgressBar progressBar = new JProgressBar();
               		
               		// we're reading from a stream of unknown length, so there
               		// isn't really a way of telling how long it will take.
               		// so we set the bar to indeterminate.
               		progressBar.setIndeterminate(true);
            		
            		final String progressMessage = " characters found";
            		final JLabel progressLabel = new JLabel(0 + progressMessage);
            		
            		final JButton progressOKButton = new JButton("OK");
            		progressOKButton.setEnabled(false);
            		progressOKButton.addActionListener(new ActionListener() {
            			public void actionPerformed(ActionEvent ae) {
            				progressDialog.dispose();
            			}
            		});
            		
            		progressDialog.setLayout(new BoxLayout(progressDialog.getContentPane(), BoxLayout.Y_AXIS));
            		progressDialog.add(progressBar);
            		progressDialog.add(progressLabel);
            		progressDialog.add(progressOKButton);
            		progressDialog.pack();
            		HanziLookupUIBuilder.setChildComponentPosition(HanziDict.this, progressDialog);
            		progressDialog.setVisible(true);
        			
        			final ChangeListener progressLabelUpdater = new ChangeListener() {
            			public void stateChanged(ChangeEvent ce) {
            				CEDICTCharacterDictionary dict = (CEDICTCharacterDictionary)ce.getSource();
            				int charCount = dict.getSize();
            				
            				progressLabel.setText(charCount + progressMessage);
            				progressBar.setValue(charCount);
            			}
            		};
            		
        			Thread loaderThread = new Thread() {
        				// We run the loading in a separate thread.
        				// This is so it doesn't interfere with the progress bar
        				// that shows in the event dispatch thread.
        				
        				public void run() {
	        				try {
	        					if(usingResource) {
	        						HanziDict.this.loadResourceDictionary(dictionaryPath, progressLabelUpdater);
	        					} else {
	        						HanziDict.this.loadFileDictionary(dictionaryPath, progressLabelUpdater);
	        					}
	        					
	        					// once we're done we set the bar's max to the current
	        					// level and set it to determinate.  this stops
	        					// the indeterminate animation.
	        					progressBar.setMaximum(progressBar.getValue());
	        					progressBar.setIndeterminate(false);
	        					progressOKButton.setEnabled(true);
	        					dialog.dispose();
	        					
	        				} catch(Exception e) {
	                			progressDialog.dispose();
	                			JOptionPane.showMessageDialog(dialog, "Error reading dictionary: " + dictionaryPath, "Error", JOptionPane.ERROR_MESSAGE);
	                		}
        				}
        			};
        			loaderThread.start();
        		}
            }
         });
    	
    	c.weightx = 0.0;
    	c.gridx = 1;
    	c.gridy = 2;
    	c.anchor = GridBagConstraints.LINE_END;
    	c.fill = GridBagConstraints.NONE;
    	cp.add(loadOKButton, c);

    	JButton cancelButton = new JButton("Cancel");
    	cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {    	
            	dialog.dispose();
            }
         });
    	c.weightx = 0.0;
    	c.gridx = 2;
    	c.gridy = 2;
    	c.anchor = GridBagConstraints.LINE_START;
    	c.fill = GridBagConstraints.NONE;
    	cp.add(cancelButton, c);
    	
    	dialog.pack();
    	
    	return dialog;
    }
    
    /**
     * Load the given dictionary entry data.
     * @param selectedChar the character
     * @param dictEntry the definition data
     */
    private void loadDefinitionData(char selectedChar, CharacterDictionary.Entry dictEntry) {
		char tradChar = dictEntry.getTraditional();
		char simpChar = dictEntry.getSimplified();
		
		char primaryChar;
		char secondaryChar;
		
		if(selectedChar == tradChar) {
			primaryChar = tradChar;
			secondaryChar = simpChar;
		} else {
			primaryChar = simpChar;
			secondaryChar = tradChar;
		}
    	
		StringBuffer paneText = new StringBuffer();
		
		paneText.append("<html>\n");
		paneText.append("\t<head>\n");
		paneText.append("\t\t<style type=\"text/css\">\n");
		
		Font font = this.getFont();
		if(null != font) {
			paneText.append("\t\tbody {font-family: ").append(this.getFont().getFamily()).append("; font-size: ").append(this.getFont().getSize()).append("}\n");
		}
		
		paneText.append("\t\t.characters {font-size: 150%}\n");
		paneText.append("\t\t</style>\n");
    	paneText.append("\t</head>\n");
    	paneText.append("\t<body>\n");
		
		paneText.append("<h1 class=\"characters\">").append(primaryChar);
		if(secondaryChar != primaryChar) {
			paneText.append("(").append(secondaryChar).append(")");
		}
		paneText.append("</h1>\n");
		paneText.append("<br>\n\n");
		
		Definition[] defs = dictEntry.getDefinitions();
		
		// display the data in an html list
		paneText.append("<ol>\n");
		// cycle through pronunciations
		for(int i = 0; i < defs.length; i++) {
			String pinyinString = Pinyinifier.pinyinify(defs[i].getPinyin());
			
			/* canDisplayUpTo not reliable, apparently
			if(this.getFont().canDisplayUpTo(pinyinString) > -1) {
				// preferably show pinyin tones with accented chars,
				// but if the font can't do that, then revert to tone digits appended.
				pinyinString = defs[i].getPinyin();
			}
			*/
			
			paneText.append("<li><b>").append(pinyinString).append("</b><br>\n");
			
			String[] translations = defs[i].getTranslations();
			
			// cycle through the definitions for this pronunciation
			for(int j = 0; j < translations.length; j++) {
				paneText.append(translations[j]);
				if(j < translations.length - 1) {
					paneText.append("; ");
				}
			}
			paneText.append("\n");
		}
		
		paneText.append("</ol>\n");
		paneText.append("\t</body>\n");
		paneText.append("</html>");
	
		this.definitionTextPane.setText(paneText.toString());	
		
		// make sure scroll centered at the top
		this.definitionTextPane.setCaretPosition(0);
    }
   
    /**
     * @see Component#getFont()
     */
    public Font getFont() {
    	return this.font;
    }
    
    /**
     * @see Component#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
    	this.font = font;
    	
    	if(null != this.lookupPanel) {
    		this.lookupPanel.setFont(font);
    	}
    
    	super.setFont(font);
    }
	
    /**
     * Load the definition pane with a blank definition for when there is no data available.
     * @param character
     */
	private void loadEmptyDefinition(char character) {
		this.definitionTextPane.setText("<html>\n<body>\nNo definition found.\n</body>\n</html>");
	}
	
	public void characterSelected(CharacterSelectionEvent e) {
		char selectedChar = e.getSelectedCharacter();
	    CharacterDictionary.Entry entry = this.dictionary.lookup(selectedChar);
	    
	    if(null != entry) {
	        this.loadDefinitionData(selectedChar, entry);
	    } else {
	        this.loadEmptyDefinition(selectedChar);
	    }
	}

	/**
	 * Main method for running as a stand-alone program.
	 * Brings up a new Frame with the program running in it.
	 */
	static public void main(String[] args) {
		// generate a new frame to load the applet into it
		final JFrame frame = new JFrame("HanziDict");
		final HanziDict hanziDict = new HanziDict();
		
		// It's running as app, so we attempt to load
		// preferences from the Java prefs api.
		final Preferences prefs = retrievePreferences();
		if(null != prefs) {
			// If we successfully retrieved some prefs then we load the options state from there.
			hanziDict.prefs = prefs;
			
			// Add a listener so that when they shut down the program we save the preferences.
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					
					try {
						// We want to ask them if they want to have preferences saved to disk.
						// Check if they have already elected to save preferences previously.
						// If they have, then we don't ask them if they want to save again
						// and just save automatically.  If they haven't (or hit no last time)
						// then we ask them again.  This means we keep asking them every time
						// if they keep hitting no.
						if(prefs.getBoolean(SAVE_OPTIONS_PREF, false) ||
							JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, "Okay to save options to disk?  Options will be saved according to the Java Preferences API in a system-dependent location.", "Save options?", JOptionPane.YES_NO_OPTION)) {
							
							// They have elected to save options.
							// Refresh the prefs state and flush it.
							
							hanziDict.writeOptionsToPreferences(prefs);
							prefs.putBoolean(SAVE_OPTIONS_PREF, true);
							prefs.flush();
						} else {
							// If they chose not to save, then we delete the node.
							// Note that this node actually already exists due to how Java retrieves
							// nodes.  This is kind of annoying since they didn't elect to have anything
							// saved to disk, and we're saving something and then deleting it if they
							// didn't want it.  Oh well, seems the best we can do.
							
							prefs.removeNode();
						}
					} catch(Exception e) {
						JOptionPane.showMessageDialog(frame, "Unexpected error handling prferences.", "Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			});
		}
		
		// init as if it were an applet.
		// by manually invoking init instead of using the constructor
		// we unify the cases of stand-alone program and applet.
		hanziDict.init();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(hanziDict);

		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Would prefer if this method didn't actually create a node if it didn't already exist.
	 * Would like instead to give them an option of whether or not to save the node.
	 * We'll settle for deleting the node created here after the fact if they choose not to save.
	 * 
	 * @return preferences loaded using this class
	 */
	static private Preferences retrievePreferences() {
		Preferences prefs = null;
		
		try {
			prefs = Preferences.userNodeForPackage(HanziDict.class);
		} catch(Exception e) {
			// if for any reason we can't load preferences, we just return
			// null and expect the app to use the defaults.
			
			System.err.println("Unable to read preferences, loading defaults.");
			e.printStackTrace();
		}
		
		return prefs;
	}
}
