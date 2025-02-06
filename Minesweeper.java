
/**
 * @author Mallikarjuna rao yarapathineni
 * 999902486
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Mallikarjuna rao yarapathineni
 */
public class Minesweeper extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	Object lostMsg = "You detonated Mine. Game Over. Better Luck Next Time!";
	Object timeoutmsg = "Time Up. Better Luck Next Time!";
	Object wonMsg = "You Won the Game!\nTime: ";
	Object restartMsg = "Are you sure you want to Restart Game with new settings?";
	String[] imagenames = { "", "flag.png", "question_mark.png", "cross.png", "mine.png", "mine1.png", "clock.png" };
	public Icon[] upperImages;
	Image mine_image;
	int numberOfRows, numberOfCols, numberOfMines, timeElapsed, timeRemaining;
	boolean isGameOver;
	Dimension size;
	JMenu optionsmenu, thememenu;
	JMenuItem aboutitem;
	JMenuBar menubar;
	String[] optionsString = { "Beginner(10 mines, 7 x 9)", "Intermediate(35 mines, 13 x 18)",
			"Advanced(91 mines, 22 x 25)" };
	ButtonGroup optionsGroup = new ButtonGroup();
	JRadioButtonMenuItem[] options = new JRadioButtonMenuItem[3];
	MyJButton[][] button;
	Point[] mine;
	JPanel gamePanel = new JPanel(), scorePanel;
	JLabel timer, flag;
	TextField timerText, flagText;
	Timer clock;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Minesweeper();
			}
		});
	}

	public Minesweeper() {
		super("Minesweeper");
		upperImages = new Icon[7];
		for (int i = 0; i < upperImages.length; i++) {
			upperImages[i] = new ImageIcon(getClass().getResource("/resources/" + imagenames[i]));
		}
		mine_image = new ImageIcon(getClass().getResource("/resources/" + imagenames[5])).getImage();
		this.setIconImage(mine_image);
		numberOfRows = 7;
		numberOfCols = 9;
		numberOfMines = 10;
		timeRemaining = 60;
		size = new Dimension(numberOfRows * 45, numberOfCols * 35);
		setPreferredSize(size);
		menubar = new JMenuBar();
		optionsmenu = new JMenu("Change Level");
		options[0] = new JRadioButtonMenuItem(optionsString[0], true);
		options[0].setMnemonic(KeyEvent.VK_B);
		options[1] = new JRadioButtonMenuItem(optionsString[1], false);
		options[1].setMnemonic(KeyEvent.VK_I);
		options[2] = new JRadioButtonMenuItem(optionsString[2], false);
		options[2].setMnemonic(KeyEvent.VK_A);
		for (JRadioButtonMenuItem option : options) {
			optionsGroup.add(option);
			option.addActionListener(this);
			optionsmenu.add(option);
		}
		optionsmenu.setMnemonic(KeyEvent.VK_O);
		aboutitem = new JMenuItem("About", KeyEvent.VK_U);
		aboutitem.addActionListener(this);
		optionsmenu.add(aboutitem);
		menubar.add(optionsmenu);
		setJMenuBar(menubar);
		startGame();
		setVisible(true);
		requestFocusInWindow();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * Opens up the game with default level of the game.
	 */
	public void startGame() {
		this.setPreferredSize(size);
		isGameOver = false;
		timeElapsed = 0;
		MyJButton.flagCount = 0;
		setLayout(new BorderLayout());
		scorePanel = new JPanel();
		scorePanel.setBorder(new EmptyBorder(0, 20, 0, 20));
		timer = new JLabel(upperImages[6]);
		timerText = new TextField(Integer.toString(timeRemaining), 1);
		timerText.setEditable(false);
		scorePanel.add(timer);
		scorePanel.add(timerText);
		flag = new JLabel(upperImages[1]);
		flagText = new TextField(1);
		flagText.setEditable(false);
		scorePanel.add(flag);
		scorePanel.add(flagText);
		add(scorePanel, BorderLayout.NORTH);
		gamePanel.setBorder(new EmptyBorder(0, 20, 20, 20));
		add(gamePanel, BorderLayout.CENTER);
		gamePanel.setLayout(new GridLayout(numberOfRows, numberOfCols));
		createButtons();
		clock = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				timerText.setText(Integer.toString(--timeRemaining));
				if (timeRemaining == 0) {
					timeUp();
				}
			}
		});
		paint();
		pack();
	}

	public void paint() {
		flagText.setText(Integer.toString(numberOfMines - MyJButton.flagCount));
	}

	/**
	 * Creates the buttons based on the level selected by the user
	 */
	private void createButtons() {
		button = new MyJButton[numberOfRows][numberOfCols];
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfCols; j++) {
				button[i][j] = new MyJButton(i, j, this);
				gamePanel.add(button[i][j]);
			}
		}
	}

	/**
	 * Randomly generates the mines
	 * 
	 * @param firstButtonOpened
	 */
	private void generateMines(Point firstButtonOpened) {
		mine = new Point[numberOfMines];
		Random r = new Random();
		for (int i = 0; i < numberOfMines; i++) {
			mine[i] = new Point(r.nextInt(numberOfRows), r.nextInt(numberOfCols));
			if (replaceMine(mine[i], firstButtonOpened)) {
				i--;
				continue;
			}
			for (int j = 0; j < i; j++) {
				if (mine[i].equals(mine[j])) {
					i--;
					break;
				}
			}
		}
	}

	/**
	 * Replaces the Mines
	 * 
	 * @param mine
	 * @param firstButtonOpened
	 * @return
	 */
	private boolean replaceMine(Point mine, Point firstButtonOpened) {
		int x = firstButtonOpened.x, y = firstButtonOpened.y;
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (mine.equals(new Point(i, j))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Places the mines in the JFrame
	 */
	private void plantMines() {
		for (int i = 0; i < numberOfMines; i++) {
			button[mine[i].x][mine[i].y].lower_limit = -1;
		}
	}

	/**
	 * Clears the surrounding buttons when user clicks on a button
	 */
	private void surroundMines() {
		for (int i = 0; i < numberOfMines; i++) {
			for (int j = mine[i].x - 1; j <= mine[i].x + 1; j++) {
				if (j >= 0 && j < numberOfRows) {
					for (int k = mine[i].y - 1; k <= mine[i].y + 1; k++) {
						if (k >= 0 && k < numberOfCols) {
							if (button[j][k].lower_limit >= 0) {
								button[j][k].lower_limit += 1;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Opens the button when user clicks on it to revil the mines or numbers
	 * 
	 * @param b
	 */
	public void openButton(MyJButton b) {
		if (b.isEnabled() && b.upper_limit == 0) {
			switch (b.lower_limit) {
			case -1:
				b.setIcon(upperImages[4]);
				if (!isGameOver && timeRemaining != 0) {
					String pathname = "resources/Explosion2.wav";
					try {
						AudioInputStream audioInputStream = AudioSystem
								.getAudioInputStream(new File(pathname).getAbsoluteFile());
						Clip clip = AudioSystem.getClip();
						clip.open(audioInputStream);
						clip.start();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (UnsupportedAudioFileException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (LineUnavailableException e) {
						e.printStackTrace();
					}
					isGameOver = true;
					gameLost();
				}
				break;
			case 0:
				b.setEnabled(false);
				b.setText("");
				if (timeElapsed == 0) {
					clock.start();
					timeElapsed++;
					generateMines(new Point(b.row, b.col));
					plantMines();
					surroundMines();
				}
				for (int i = b.row - 1; i <= b.row + 1; i++) {
					if (i >= 0 && i < numberOfRows) {
						for (int j = b.col - 1; j <= b.col + 1; j++) {
							if (j >= 0 && j < numberOfCols) {
								openButton(button[i][j]);
							}
						}
					}
				}
				break;
			default:
				b.setEnabled(false);
				b.setText(Integer.toString(b.lower_limit));
				break;
			}
		}
	}

	/*
	 * Sets the buttons in the JFrame based on the level of the game
	 */
	private void openAllButtons() {
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfCols; j++) {
				if (button[i][j].lower_limit != -1 && button[i][j].upper_limit >= 1) {
					button[i][j].setIcon(upperImages[3]);
				}
				openButton(button[i][j]);
			}
		}
	}

	/**
	 * Checks if the player won the game and calls gameWon() method if player wins
	 * the game
	 */
	public void checkIfWon() {
		boolean gameWonFlag = true;
		for (int i = 0; i < numberOfRows && gameWonFlag; i++) {
			for (int j = 0; j < numberOfCols; j++) {
				if (button[i][j].lower_limit != -1 && button[i][j].isEnabled()) {
					gameWonFlag = false;
					break;
				}
			}
		}
		if (gameWonFlag) {
			isGameOver = true;
			gameWon();
		}
	}

	/**
	 * Opens a JOptionPane to display the Game Won Message to the players when he
	 * wins the game
	 */
	private void gameWon() {
		clock.stop();
		openAllButtons();
		Object[] options = { "Play Again", "Exit" };
		int selection = JOptionPane.showOptionDialog(this, wonMsg + timerText.getText() + " seconds", "You Won",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (selection == JOptionPane.YES_OPTION) {
			size = getSize();
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
		} else {
			System.exit(0);
		}
	}

	/**
	 * Opens a JOptionPane to display the Game Lost Message to the players when the
	 * time completes to finish the level
	 */
	public void timeUp() {
		clock.stop();
		openAllButtons();
		Object[] options = { "Play Again", "Exit" };
		int selection = JOptionPane.showOptionDialog(this, timeoutmsg, "You Lost", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (selection == JOptionPane.YES_OPTION) {
			size = getSize();
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
		} else {
			System.exit(0);
		}
	}

	/**
	 * Opens a JOptionPane to display the Game Lost Message to the players when he
	 * clicks on the mine or time completes
	 */
	public void gameLost() {
		clock.stop();
		openAllButtons();
		Object[] options = { "Play Again", "Exit" };
		int selection = JOptionPane.showOptionDialog(this, lostMsg, "You Lost", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (selection == JOptionPane.YES_OPTION) {
			size = getSize();
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
		} else {
			System.exit(0);
		}
	}

	/**
	 * This Method restarts the game by setting the rows, columns, time of that
	 * particular level
	 * 
	 * @param arg0
	 */
	private void restartGame(ActionEvent arg0) {
		int selection = JOptionPane.showConfirmDialog(this, restartMsg);
		if (selection == JOptionPane.YES_OPTION) {
			clock.stop();
			if (arg0.getSource() == options[0]) {
				numberOfRows = 7;
				numberOfCols = 9;
				numberOfMines = 10;
				timeRemaining = 60;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[1]) {
				numberOfRows = 13;
				numberOfCols = 18;
				numberOfMines = 35;
				timeRemaining = 180;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[2]) {
				numberOfRows = 22;
				numberOfCols = 25;
				numberOfMines = 91;
				timeRemaining = 600;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			}
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
			setLocationRelativeTo(null);
		} else {
			switch (numberOfMines) {
			case 10:
				options[0].setSelected(true);
				break;
			case 35:
				options[1].setSelected(true);
				break;
			case 91:
				options[2].setSelected(true);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == aboutitem) {
			JOptionPane.showMessageDialog(this, "MineSweeper Game!", getTitle(), JOptionPane.INFORMATION_MESSAGE);
		} else {
			restartGame(arg0);
		}
	}

}

/**
 * This class has access to all the variables and methods in the parent class
 * 
 * @author Mallikarjuna rao yarapathineni
 * 
 */
class restartGame extends Minesweeper {
	private static final long serialVersionUID = 1L;

	restartGame(ActionEvent arg0) {
		int selection = JOptionPane.showConfirmDialog(this, restartMsg);
		if (selection == JOptionPane.YES_OPTION) {
			clock.stop();
			if (arg0.getSource() == options[0]) {
				numberOfRows = 7;
				numberOfCols = 9;
				numberOfMines = 10;
				timeRemaining = 60;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[1]) {
				numberOfRows = 13;
				numberOfCols = 18;
				numberOfMines = 35;
				timeRemaining = 180;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[2]) {
				numberOfRows = 22;
				numberOfCols = 25;
				numberOfMines = 91;
				timeRemaining = 600;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			}
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
			setLocationRelativeTo(null);
		} else {
			switch (numberOfMines) {
			case 10:
				options[0].setSelected(true);
				break;
			case 35:
				options[1].setSelected(true);
				break;
			case 91:
				options[2].setSelected(true);
				break;
			default:
				break;
			}
		}

	}
}

class surroundMines extends Minesweeper {
	private static final long serialVersionUID = 0;

	public surroundMines() {
		for (int i = 0; i < numberOfMines; i++) {
			for (int j = mine[i].x - 1; j <= mine[i].x + 1; j++) {
				if (j >= 0 && j < numberOfRows) {
					for (int k = mine[i].y - 1; k <= mine[i].y + 1; k++) {
						if (k >= 0 && k < numberOfCols) {
							if (button[j][k].lower_limit >= 0) {
								button[j][k].lower_limit += 1;
							}
						}
					}
				}
			}
		}

	}
}