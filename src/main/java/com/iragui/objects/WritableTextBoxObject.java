package com.iragui.objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

import java.awt.Color;
import java.awt.Font;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.glfw.GLFW;

import com.iragui.GUI;
import com.iragui.util.ObjectUtils;

/**
 * A writable text box GUI component that supports user input, cursor movement,
 * multi-line editing, and different editing modes.
 * <p>
 * Extends {@link TextBoxObject} by adding keyboard and mouse interaction,
 * cursor rendering, and text-editing logic (insert, backspace, newline, etc.).
 * </p>
 *
 * <h2>Supported Modes</h2>
 * <ul>
 *   <li>{@link #CONSOLE_MODE} - Appends text at the end, typical console input behavior.</li>
 *   <li>{@link #FREE_MODE} - Allows free multi-line editing.</li>
 *   <li>{@link #TEXT_EDITOR_MODE} - Full text editor behavior with insert, newline, and merge logic.</li>
 * </ul>
 *
 * <h2>Line Limit</h2>
 * You may set a line limit using {@link #setLineLimit(int)}.  
 * If set to {@link #NO_LINE_LIMIT}, unlimited lines are allowed.
 *
 * <h2>Cursor</h2>
 * A {@link TextObject} cursor is maintained internally and updated during editing.
 *
 * <h2>Usage</h2>
 * Example:
 * <pre>
 * WritableTextBoxObject textBox = new WritableTextBoxObject(
 *     "chatBox", 1, gui,
 *     50, 50, 400, 300,
 *     false, true, 4,
 *     mySubWindow, myFont, Color.WHITE, new Color(0,0,0,0), true
 * );
 * textBox.setMode(WritableTextBoxObject.TEXT_EDITOR_MODE);
 * </pre>
 */
public class WritableTextBoxObject extends TextBoxObject{
	
	 /** Whether this text box is currently focused for input. */
	public boolean focused = false;
	
	 /** Optional parent sub-window object that controls focus behavior. */
	private SubWindowObject parent = null;
	
	private int letterIndex = 0;
	private int currentLine = 0;
	
	/** Cursor object rendered as a text glyph (e.g., "|"). */
	public TextObject cursor;
	private Font font;
	private Color textColor,backgroundColor;
	private boolean antiAliasing;
	
	/** Console mode: appends text at the end. */
	public static int CONSOLE_MODE = 0;
	
	/** Free mode: allows arbitrary line navigation and editing. */
	public static int FREE_MODE = 1;
	
	  /** Text editor mode: supports full editing logic (newlines, merging lines, etc.). */
	public static int TEXT_EDITOR_MODE = 2;
	
	  /** Text editor mode: supports full editing logic (newlines, merging lines, etc.). */
	public static int NO_LINE_LIMIT = -1;
	
	/** The current editing mode (default: {@link #CONSOLE_MODE}). */
	private int mode = CONSOLE_MODE;
	
	/** Maximum number of lines allowed (or {@link #NO_LINE_LIMIT}). */
	private int lineLimit = NO_LINE_LIMIT;
	
	 /**
     * Sets the maximum line count for this text box.
     *
     * @param l line limit, or {@link #NO_LINE_LIMIT} for unlimited lines
     */
	public void setLineLimit(int l) {
		this.lineLimit=l;
	}
	
	/**
     * @return the maximum line count, or {@link #NO_LINE_LIMIT} if unlimited
     */
	public int getLineLimit() {
		return this.lineLimit;
	}
	
	 /**
     * Sets the editing mode for this text box.
     *
     * @param mode one of {@link #CONSOLE_MODE}, {@link #FREE_MODE}, {@link #TEXT_EDITOR_MODE}
     */
	public void setMode(int mode) {
		this.mode=mode;
	}
	
	/**
     * @return the current editing mode
     */
	public int getMode() {
		return this.mode;
	}

	/**
     * Creates a writable text box object.
     *
     * @param name           object name
     * @param layer          rendering layer
     * @param gui            GUI manager
     * @param x              x position
     * @param y              y position
     * @param sizeX          width
     * @param sizeY          height
     * @param nearestFilter  true to use nearest-neighbor scaling
     * @param rgba           whether RGBA is enabled
     * @param lineSpacing    line spacing in pixels
     * @param window         parent sub-window object (optional, can be null)
     * @param font           font for rendering text
     * @param textColor      foreground text color
     * @param backgroundColor background color
     * @param antiAliasing   whether to enable anti-aliasing
     */
	public WritableTextBoxObject(String name, 
			int layer,
			GUI gui, 
			int x, 
			int y, 
			int sizeX, 
			int sizeY, 
			boolean nearestFilter,
			boolean rgba, 
			int lineSpacing,
			SubWindowObject window,
			Font font,
			Color textColor,
			Color backgroundColor,
			boolean antiAliasing) {
		super(name, layer, gui, x, y, sizeX, sizeY, nearestFilter, rgba, lineSpacing);
		this.parent=window;
		
		this.font=font;
		this.textColor=textColor;
		this.backgroundColor=backgroundColor;
		this.antiAliasing=antiAliasing;
		
		gui.getWindow().getKeyListener().add(this);
		gui.getWindow().getMouseListener().add(this);
		
		cursor = new TextObject(name+":cursor",layer, gui, 0, 0, nearestFilter, rgba,"|", 
				font, 
				textColor, 
				backgroundColor, 
				antiAliasing);
		cursor.setVisible();
		this.focused=true;
	}
	
	/**
     * Appends a new line of text to the text box.
     *
     * @param text line text to append
     */
	public void appendLine(String text) {

		int line = lines.size();
		TextObject tO = new TextObject(name+":textObject:",
				this.getLayer(),
				this.gui,
				0, 
				0, 
				this.isNearestFilter(), 
				rgba, 
				text, 
				font, 
				this.textColor, 
				this.backgroundColor, 
				this.antiAliasing);
		tO.setVisible();
		lines.put(line,tO);
		currentLine++;
		if(currentLine>lines.size()-1) {
			currentLine = lines.size()-1;
		}
	}
	
	/**
     * Handles a keyboard key event.
     *
     * @param key    GLFW key code
     * @param action GLFW action (press, release, repeat)
     */
	@Override
	public void sendKey(int key, int action) {
		if(parent!=null) {
			if(focused && parent.isFocused()) {
				key(key,action);
			}
		} else {
			if(focused) {
				key(key,action);
			}
		}
	}
	
	private long lastPos = 0;
	
	
	@Override
	public void destroyObject() {
		cursor.destroyObject();
		super.destroyObject();
	}
	
	private void enterLogic() {
		if(mode == TEXT_EDITOR_MODE) {
			String fullText = lines.get(currentLine).getText();
	        
	        // Split at caret position
	        String beforeCaret = fullText.substring(0, letterIndex);
	        String afterCaret = fullText.substring(letterIndex);

	        // Update current line to be text before caret
	        lines.get(currentLine).setText(beforeCaret);

	        int targetIndex = currentLine + 1;

	        // Shift lines downward by one (if needed)
	        if (lines.size() > targetIndex) {
	            // Need to shift all following lines down
	            for (int i = lines.size() - 1; i >= targetIndex; i--) {
	                String content = lines.get(i).getText();

	                // Ensure the next index exists, create if not
	                if (i + 1 >= lines.size()) {
	                    this.appendLine("");
	                }

	                lines.get(i + 1).setText(content);
	            }
	        } else {
	            // Line after current didn't exist, create it
	            this.appendLine("");
	        }

	        // Set new line with the text after the caret
	        lines.get(targetIndex).setText(afterCaret);

	        // Update caret position
	        currentLine = targetIndex;
	        letterIndex = 0;
		} else if(lineLimit==NO_LINE_LIMIT || lines.size()<lineLimit) {
			if(currentLine==lines.size()-1 || mode == CONSOLE_MODE) {
				this.appendLine("",this.font,this.textColor,this.backgroundColor, this.rgba,this.isNearestFilter(),true);
			}
			letterIndex=0;
			currentLine++;
			if(currentLine<0) {
				currentLine=0;
			}
		}
	}
	
	/**
     * Updates the text box state (cursor position, pending writes, etc.).
     *
     * @param showFrame true if the GUI should be refreshed
     */
	@Override
	public void update(boolean showFrame) {
		
		if(lines.containsKey(currentLine)) {
			if(letterIndex>lines.get(currentLine).getText().length()) {
				letterIndex = lines.get(currentLine).getText().length();
			}
		}
		if(letterIndex<0) {
			letterIndex=0;
		}
		
		if(backSpace) {
			backSpace=false;
			try {
			backSpace();
			} catch(Exception e) {}
		} else if(enter) {
			enter=false;
			try {
				enterLogic();
			} catch (Exception e) {}
		} else {
			writeCurrent();
		}
		
		cursor.setY(lines.get(currentLine).getY());
		
		int px = parent.getX();
		int py = parent.getY();
		int pTopSize = parent.getTopSize();
		int psx = parent.getSizeX();
		int psy = parent.getSizeY();
		
		cursor.setMinX(px>parent.getMinX()?px:parent.getMinX());
		cursor.setMinY(py>parent.getMinY()?py:parent.getMinY());
		cursor.setLimitX((px+psx)<parent.getLimitX()?(px+psx):parent.getLimitX());
		cursor.setLimitY((py+(psy-pTopSize))<parent.getLimitY()?(py+(psy-pTopSize)):parent.getLimitY());
		
		String subString = lines.get(currentLine).getText();
		int offsetX = 0;
		
		if(subString.contentEquals("")) {
			letterIndex=0;
		}
		
		if(letterIndex>0 && letterIndex<=subString.length()) {
			subString = subString.substring(0,letterIndex);
			offsetX = lines.get(currentLine).getTextWidth(subString);
		} else if(letterIndex>subString.length()) {
			offsetX = lines.get(currentLine).getTextWidth(subString);
		}
		offsetX += lines.get(currentLine).getX();
		cursor.setX(offsetX - (lines.get(currentLine).getFont().getSize()/4));
		
		long pos = com.iragui.util.ObjectUtils.getLongFromInts(cursor.getX(),cursor.getY());
		if(pos!=lastPos) {
			lastPos=pos;
			gui.showNextFrame();
		}
	}
	
	private boolean shifting=false,controling=false;
	private ConcurrentHashMap<Long,String> writes = new ConcurrentHashMap<>();
	private void write(String string) {
		writes.put(System.currentTimeMillis(),string);
	}
	
	private void writeCurrent() {
	    for (Long t : writes.keySet()) {
	        String string = writes.get(t);

	        // Ensure at least one line exists
	        if (lines.size() == 0) {
	            this.appendLine(
	                "",
	                new Font("Lucida Console", Font.PLAIN, 22),
	                Color.WHITE,
	                new Color(0, 0, 0, 0),
	                this.rgba,
	                this.isNearestFilter(),
	                true
	            );
	        }

	        char[] currentText = lines.get(currentLine).getText().toCharArray();
	        char[] toInsert = string.toCharArray();

	        char[] result;

	        if (letterIndex >= currentText.length) {
	            // APPEND
	            result = new char[currentText.length + toInsert.length];
	            System.arraycopy(currentText, 0, result, 0, currentText.length);
	            System.arraycopy(toInsert, 0, result, currentText.length, toInsert.length);
	        } else if (letterIndex > 0) {
	            // INSERT
	            result = new char[currentText.length + toInsert.length];
	            System.arraycopy(currentText, 0, result, 0, letterIndex);
	            System.arraycopy(toInsert, 0, result, letterIndex, toInsert.length);
	            System.arraycopy(currentText, letterIndex, result, letterIndex + toInsert.length, currentText.length - letterIndex);
	        } else {
	            // PREPEND
	            result = new char[currentText.length + toInsert.length];
	            System.arraycopy(toInsert, 0, result, 0, toInsert.length);
	            System.arraycopy(currentText, 0, result, toInsert.length, currentText.length);
	        }

	        // Convert char[] to String only once, here
	        lines.get(currentLine).setText(new String(result));
	        letterIndex += toInsert.length;
	    }

	    writes.clear();
	}
	
	private void copy() {
		
	}
	
	private boolean enter = false;
	private void enter() {
		enter=true;
	}
	
	private boolean backSpace = false;
	private void backSpace() {
		
		if(mode==TEXT_EDITOR_MODE) {
			if (currentLine > lines.firstKey() && letterIndex == 0) {
				TextObject prevLine = lines.get(currentLine - 1);
				TextObject currLine = lines.get(currentLine);

				int futureLetterIndex = prevLine.getText().length();
				String mergedText = prevLine.getText() + currLine.getText();

				prevLine.setText(mergedText);

				for (int i = currentLine + 1; i < lines.size(); i++) {
					lines.get(i - 1).setText(lines.get(i).getText());
				}

				lines.get(lines.lastKey()).setText("");

				currentLine--;
				letterIndex = futureLetterIndex;
				return;
			}
		}
		
		if(letterIndex==0) {
			return;
		}
		
		if(lines.size()==0) {
			this.appendLine("",font,this.textColor,this.backgroundColor, this.rgba,this.isNearestFilter(),true);
		}
		String originalText = this.lines.get(currentLine).getText();
		
		if(letterIndex>originalText.length()) {
			letterIndex=originalText.length();
		}
		
		char[] chars = originalText.toCharArray();
		
		if(chars.length>1) {
			char[] newChars = new char[chars.length-1];
			
			int j=0;
			for(int i=0;i<chars.length;i++) {
				if(i!=letterIndex-1) {
					newChars[j]=chars[i];
					j++;
				}
			}
			
			this.lines.get(currentLine).setText(String.valueOf(newChars));
			
			letterIndex--;
			if(letterIndex<0) {
				letterIndex=0;
			}
			
		} else if(chars.length==1) {
			this.lines.get(currentLine).setText("");
		}
		
		backSpace=false;
	}
	
	private void key(int key, int action) {
		if(action==GLFW_PRESS || action==GLFW.GLFW_REPEAT) {
			switch(key) {
			case GLFW_KEY_TAB:
				this.write("     ");
				break;
			case GLFW_KEY_LEFT_CONTROL:
				controling=true;
				break;
			case GLFW_KEY_RIGHT_CONTROL:
				controling=true;
				break;
			
			case GLFW_KEY_LEFT_SHIFT:
				shifting=true;
				break;
			case GLFW.GLFW_KEY_RIGHT_SHIFT:
				shifting=true;
				break;
			case GLFW.GLFW_KEY_A:
				if(shifting) {
					this.write("A");
				} else {
					this.write("a");
				}
				break;
			case GLFW_KEY_B:
                if (shifting) {
                    this.write("B");
                } else {
                    this.write("b");
                }
                break;
            case GLFW_KEY_C:
            	
            	if(controling) {
            		copy();
            	} else {
            		if (shifting) {
                    	this.write("C");
                	} else {
                		this.write("c");
                	}
            	}
                break;
            case GLFW_KEY_D:
                if (shifting) {
                    this.write("D");
                } else {
                    this.write("d");
                }
                break;
            case GLFW_KEY_E:
                if (shifting) {
                    this.write("E");
                } else {
                    this.write("e");
                }
                break;
            case GLFW_KEY_F:
                if (shifting) {
                    this.write("F");
                } else {
                    this.write("f");
                }
                break;
            case GLFW_KEY_G:
                if (shifting) {
                    this.write("G");
                } else {
                    this.write("g");
                }
                break;
            case GLFW_KEY_H:
                if (shifting) {
                    this.write("H");
                } else {
                    this.write("h");
                }
                break;
            case GLFW_KEY_I:
                if (shifting) {
                    this.write("I");
                } else {
                    this.write("i");
                }
                break;
            case GLFW_KEY_J:
                if (shifting) {
                    this.write("J");
                } else {
                    this.write("j");
                }
                break;
            case GLFW_KEY_K:
                if (shifting) {
                    this.write("K");
                } else {
                    this.write("k");
                }
                break;
            case GLFW_KEY_L:
                if (shifting) {
                    this.write("L");
                } else {
                    this.write("l");
                }
                break;
            case GLFW_KEY_M:
                if (shifting) {
                    this.write("M");
                } else {
                    this.write("m");
                }
                break;
            case GLFW_KEY_N:
                if (shifting) {
                    this.write("N");
                } else {
                    this.write("n");
                }
                break;
            case GLFW_KEY_O:
                if (shifting) {
                    this.write("O");
                } else {
                    this.write("o");
                }
                break;
            case GLFW_KEY_P:
                if (shifting) {
                    this.write("P");
                } else {
                    this.write("p");
                }
                break;
            case GLFW_KEY_Q:
                if (shifting) {
                    this.write("Q");
                } else {
                    this.write("q");
                }
                break;
            case GLFW_KEY_R:
                if (shifting) {
                    this.write("R");
                } else {
                    this.write("r");
                }
                break;
            case GLFW_KEY_S:
                if (shifting) {
                    this.write("S");
                } else {
                    this.write("s");
                }
                break;
            case GLFW_KEY_T:
                if (shifting) {
                    this.write("T");
                } else {
                    this.write("t");
                }
                break;
            case GLFW_KEY_U:
                if (shifting) {
                    this.write("U");
                } else {
                    this.write("u");
                }
                break;
            case GLFW_KEY_V:
            	
            	if(controling) {
            		String clipboard = ObjectUtils.getClipboard();
            		String[] split = clipboard.split("\n");
            		
            		for(int i=0;i<split.length;i++) {
            			this.write(split[i]);
            			if(i<split.length-1) {
            				this.enter();
            			}
            		}
            	} else {
            		if (shifting) {
            			this.write("V");
            		} else {
            			this.write("v");
            		}
            	}
                break;
            case GLFW_KEY_W:
                if (shifting) {
                    this.write("W");
                } else {
                    this.write("w");
                }
                break;
            case GLFW_KEY_X:
                if (shifting) {
                    this.write("X");
                } else {
                    this.write("x");
                }
                break;
            case GLFW_KEY_Y:
                if (shifting) {
                    this.write("Y");
                } else {
                    this.write("y");
                }
                break;
            case GLFW_KEY_Z:
                if (shifting) {
                    this.write("Z");
                } else {
                    this.write("z");
                }
                break;
            case GLFW_KEY_RIGHT:
            	letterIndex++;
            	try {
            		if(letterIndex>lines.get(currentLine).getText().length()) {
            			letterIndex = lines.get(currentLine).getText().length();
            		}
            	} catch (Exception e) {
            		letterIndex = 0;
            	}
            	break;
            case GLFW_KEY_LEFT:
            	letterIndex--;
            	break;
            case GLFW_KEY_DOWN:
            	currentLine++;
            	
            	
            	if(currentLine>=lines.size()) {
            		currentLine=lines.size()-1;
            		if(mode==FREE_MODE) {
            			enter=true;
            		}
            	}
            	break;
            case GLFW_KEY_UP:
            	
            	if(this.mode!=CONSOLE_MODE) {
            		currentLine--;
            		if(currentLine<0) {
            			currentLine=0;
            		}
            	}
            	break;
			case GLFW.GLFW_KEY_BACKSPACE:
				backSpace=true;
				break;
			case GLFW.GLFW_KEY_ENTER:
				this.enter();
				break;
			case GLFW_KEY_SPACE:
				this.write(" ");
				break;
			case GLFW.GLFW_KEY_PERIOD:
				if(shifting) {
					this.write(">");
				} else {
					this.write(".");
				}
				break;
			case GLFW.GLFW_KEY_KP_DECIMAL:
				this.write(".");
				break;
			case GLFW.GLFW_KEY_COMMA:
				if(shifting) {
					this.write("<");
				} else {
					this.write(",");
				}
				break;
			case GLFW.GLFW_KEY_SLASH:
				if(shifting) {
					this.write("?");
				} else {
					this.write("/");
				}
				break;
			case GLFW.GLFW_KEY_SEMICOLON:
				if(shifting) {
					this.write(":");
				} else {
					this.write(";");
				}
				break;
			case GLFW.GLFW_KEY_APOSTROPHE:
				if(shifting) {
					this.write("\"");
				} else {
					this.write("'");
				}
				break;
			case GLFW.GLFW_KEY_LEFT_BRACKET:
				if(shifting) {
					this.write("{");
				} else {
					this.write("[");
				}
				break;
			case GLFW.GLFW_KEY_RIGHT_BRACKET:
				if(shifting) {
					this.write("}");
				} else {
					this.write("]");
				}
				break;
			case GLFW.GLFW_KEY_BACKSLASH:
				if(shifting) {
					this.write("|");
				} else {
					this.write("\\");
				}
				break;
			case GLFW.GLFW_KEY_EQUAL:
				if(shifting) {
					this.write("+");
				} else {
					this.write("=");
				}
				break;
			case GLFW.GLFW_KEY_MINUS:
				if(shifting) {
					this.write("_");
				} else {
					this.write("-");
				}
				break;
			case GLFW.GLFW_KEY_1:
				if(shifting) {
					this.write("!");
				} else {
					this.write("1");
				}
				break;
			case GLFW.GLFW_KEY_2:
				if(shifting) {
					this.write("@");
				} else {
					this.write("2");
				}
				break;
			case GLFW.GLFW_KEY_3:
				if(shifting) {
					this.write("#");
				} else {
					this.write("3");
				}
				break;
			case GLFW.GLFW_KEY_4:
				if(shifting) {
					this.write("$");
				} else {
					this.write("4");
				}
				break;
			case GLFW.GLFW_KEY_5:
				if(shifting) {
					this.write("%");
				} else {
					this.write("5");
				}
				break;
			case GLFW.GLFW_KEY_6:
				if(shifting) {
					this.write("^");
				} else {
					this.write("6");
				}
				break;
			case GLFW.GLFW_KEY_7:
				if(shifting) {
					this.write("&");
				} else {
					this.write("7");
				}
				break;
			case GLFW.GLFW_KEY_8:
				if(shifting) {
					this.write("*");
				} else {
					this.write("8");
				}
				break;
			case GLFW.GLFW_KEY_9:
				if(shifting) {
					this.write("(");
				} else {
					this.write("9");
				}
				break;
			case GLFW.GLFW_KEY_0:
				if(shifting) {
					this.write(")");
				} else {
					this.write("0");
				}
				break;
			case GLFW.GLFW_KEY_KP_0:
				this.write("0");
				break;
			case GLFW.GLFW_KEY_KP_1:
				this.write("1");
				break;
			case GLFW.GLFW_KEY_KP_2:
				this.write("2");
				break;
			case GLFW.GLFW_KEY_KP_3:
				this.write("3");
				break;
			case GLFW.GLFW_KEY_KP_4:
				this.write("4");
				break;
			case GLFW.GLFW_KEY_KP_5:
				this.write("5");
				break;
			case GLFW.GLFW_KEY_KP_6:
				this.write("6");
				break;
			case GLFW.GLFW_KEY_KP_7:
				this.write("7");
				break;
			case GLFW.GLFW_KEY_KP_8:
				this.write("8");
				break;
			case GLFW.GLFW_KEY_KP_9:
				this.write("9");
				break;
			}
			
		} else if(action==GLFW.GLFW_RELEASE) {
			switch(key) {
			case GLFW.GLFW_KEY_LEFT_SHIFT:
				shifting=false;
				break;
			case GLFW.GLFW_KEY_RIGHT_SHIFT:
				shifting=false;
				break;
			case GLFW_KEY_LEFT_CONTROL:
				controling=false;
				break;
			case GLFW_KEY_RIGHT_CONTROL:
				controling=false;
				break;
			}
		}
	}
	
	@Override
	public void sendMousePos(long window, double xPos, double yPos) {
		
	}
	
	@Override
	public void setLayer(int layer) {
		super.setLayer(layer);
		this.cursor.setLayer(layer);
	}

	@Override
	public void sendMouseButton(long window, int button, int action, int mods) {
		
	}
	
	@Override
	public void setX(int x) {
		super.setX(x);
	}
	
	@Override
	public void setY(int y) {
		super.setY(y);
	}

	  /**
     * @return the lines in this text box
     */
	public TreeMap<Integer, TextObject> getLines() {
		return this.lines;
	}
	
}
