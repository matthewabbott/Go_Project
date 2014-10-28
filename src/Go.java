/**
 * File: Go.java
 * ----------------
 * Written by Matthew Abbott
 * 
 * This file runs a game of Go
 * The people playing take turns using the mouse to select their next move
 * Since pieces are never moved, I opted instead to represent each intersection where a piece might go as an object
 * that object is further elaborated on in the Intersection.java file
 * The rules of Go (or at least the ruleset that this file is implementing) are listed below this comment.
 */

/*	The Rules of Go, for reference
 * See: http://en.wikipedia.org/wiki/Rules_of_Go
 * 
 * The board is empty at the onset of the game (unless players agree to place a handicap).
 * Black makes the first move, after which White and Black alternate.
 * A move consists of placing one stone of one's own color on an empty intersection on the board.
 * A player may pass his turn at any time.
 * A stone or solidly connected group of stones of one color is captured and removed from the board when all the intersections directly adjacent to it are occupied by the enemy. (Capture of the enemy takes precedence over self-capture.)
 * No stone may be played so as to recreate a former board position.
 * Two consecutive passes end the game.
 * A player's territory consists of all the points the player has either occupied or surrounded.
 * The player with more territory wins.
 */

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import javax.swing.*;

public class Go extends GraphicsProgram {

	/**
	 * Width and height of application window in pixels. Note: this is how large
	 * the application window should be. However, these parameters must be
	 * specified when this program is being run as a Java applet. In eclipse,
	 * which I wrote this program in, the run configuration must be modified
	 * such that the width and height of the window reflects these values
	 */
	public static final int APPLICATION_WIDTH = 800;
	public static final int APPLICATION_HEIGHT = 800;

	/**
	 * Number of vertical and horizontal lines that comprise the game board.
	 * Note: the board is always a square and will have NUM_LINES^2
	 * intersections. For a proper game of Go, there should be 19 lines. The
	 * existence of this constant also accommodates other board setups, such as
	 * the common beginner 9x9 board.
	 */
	public static final int NUM_LINES = 19;

	/**
	 * These values represent the separation between each line, vertically and
	 * horizontally, respectively. They also represent the distance between
	 * adjacent intersections, in pixels.
	 */
	public static final int VERT_LINE_SEP = APPLICATION_WIDTH / (NUM_LINES + 1);
	public static final int HORIZ_LINE_SEP = APPLICATION_HEIGHT
			/ (NUM_LINES + 1);

	/**
	 * Width of one game piece. Uses vertical line separation to determine size,
	 * because the program is simpler without using horizontal line separation
	 * to determine the height of a piece. If the window were not a square, the
	 * pieces would not conform to its warped shape and would instead be circles
	 * diameters based on the separation between vertical lines. I have opted
	 * for this because it is simpler and because it maintains said circular
	 * shape of pieces even under strange window conditions.
	 */
	public static final double PIECE_DIAMETER = VERT_LINE_SEP / 2;

	/** This integer represents the player whose turn it currently is */
	private int currentPlayer = 1;

	/**
	 * This array contains all of the intersections on the game board. It exists
	 * so that they can be accessed by all methods of the program.
	 */
	private Intersection[][] intersections;

	/**
	 * This array contains the board state of the previous turn. If undo is
	 * pressed, this board state replaces the existing board state
	 */
	private int[][] previousAllegiances = new int[NUM_LINES][NUM_LINES];

	/**
	 * pass stores the number of times a turn has been passed consecutively. It
	 * is reset to 0 once a player places a piece and is incremented by one if a
	 * player passes. Should it reach 2, the game ends as per the rules of Go.
	 */
	private int pass = 0;

	/**
	 * numUndos counts the number of consecutive undos, if that number is
	 * greater than or equal to the number of previous saved board states, then
	 * no more undos can happen. If they are attempted, the board wouldn't
	 * change but the player whose turn it is would, which is unacceptable.
	 * 
	 * In the current implementation of this program, only 1 previous board
	 * state is saved, but in the future the program could be modified to
	 * accommodate multiple undos. A simple solution would be to simply create
	 * more previousAllegiances arrays, then use this variable to determine
	 * which to access, however this seems like a waste of resources for little
	 * benefit.
	 */
	private int numUndos = 1;

	public static void main(String[] args) {

	}

	public void init() {
		createBoard();
		initializeIntersections();
		overwritePreviousAllegiances();

		addMouseListeners();
		add(new JButton("Undo"), NORTH);
		add(new JButton("Pass"), NORTH);
		add(new JButton("End Game"), NORTH);
		addActionListeners();
	}

	/** runs the Go game */
	public void run() {
		// finalGameStats();
	}

	/**
	 * createBoard is a simple method that draws all the lines that comprise the
	 * Go game board. It determines the locations of the lines based on the
	 * constants that define how many lines there are and how large the
	 * application window should be
	 */
	private void createBoard() {
		for (int i = 1; i <= NUM_LINES + 1; i++) {
			GLine vertLine = new GLine(VERT_LINE_SEP * i, HORIZ_LINE_SEP,
					VERT_LINE_SEP * i, APPLICATION_HEIGHT - HORIZ_LINE_SEP);
			GLine horizLine = new GLine(VERT_LINE_SEP, HORIZ_LINE_SEP * i,
					APPLICATION_WIDTH - VERT_LINE_SEP, HORIZ_LINE_SEP * i);
			horizLine.setColor(Color.BLACK);
			vertLine.setColor(Color.BLACK);
			add(vertLine);
			add(horizLine);
		}

	}

	/**
	 * initializeIntersections is a private method that creates all of the
	 * intersections and stores each of them in the appropriate index of a
	 * NUM_LINES x NUM_LINES array. The x locations and y locations of each
	 * intersection are determined by using the separation between vertical
	 * lines and horizontal lines as well as i and j, respectively. The for
	 * loops store each new intersection in the array. The indices of each
	 * intersection represent the x and y number of each intersection. That is,
	 * index 0,0 is the leftmost topmost intersection, while 0,1 is the
	 * intersection directly below it
	 */
	private void initializeIntersections() {
		intersections = new Intersection[NUM_LINES][NUM_LINES];
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				intersections[i][j] = new Intersection(VERT_LINE_SEP * (i + 1),
						HORIZ_LINE_SEP * (j + 1), PIECE_DIAMETER);
			}
		}
	}

	/**
	 * playerTurn is a private void method that allows a player to take his/her
	 * turn. They use the mouse to click an intersection, at which point their
	 * piece will be placed there. Note: when the player clicks, their piece
	 * will be placed on the intersection they are close enough to, or if they
	 * are not close to any intersections the game will wait until they have
	 * clicked clicked again indefinitely. Alternatively, the player may click
	 * the pass button and their turn will be skipped. The method then checks to
	 * see if any piece has been captured. After this happens blackTurn returns
	 * a boolean that states whether or not both players have passed their
	 * turns. If they have, the game ends. When a player's turn comes up,
	 * current player is changed such that it no longer corresponds to the
	 * previous player.
	 */
	private void playerTurn() {
		currentPlayer++;
		if (currentPlayer > 2) {
			currentPlayer = 1;
		}
	}

	/**
	 * mouseClicked responds to a player clicking the board somewhere, calling
	 * playerMoved to place a piece if necessary.
	 */
	public void mouseClicked(MouseEvent e) {
		playerMoved(e);
	}

	/**
	 * playerMoved is a void method that activates when the mouse is clicked,
	 * meaning that a player has tried to make a move. If an intersection was
	 * clicked, then a piece will be placed there and it will become the next
	 * player's turn, otherwise nothing will happen and the game will wait for
	 * another click or button press.
	 */
	private void playerMoved(MouseEvent e) {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {

				if (intersectionClicked(intersections[i][j].getX(),
						intersections[i][j].getY(), e.getX(), e.getY())) {

					if (intersections[i][j].getAllegiance() != 1
							&& intersections[i][j].getAllegiance() != 2) {

						overwritePreviousAllegiances();

						intersections[i][j].setAllegiance(currentPlayer);
						add(intersections[i][j].piece);

						pass = 0;
						capturePieces();
						nextPlayer();
					}

				}

			}
		}

	}

	/**
	 * intersectionClicked is a private method that receives the x and y
	 * coordinates of an intersection and the x and y coordinates of the mouse
	 * (after a click) and checks whether the intersection was selected or not.
	 * This is determined by the radius of a game piece. If the click was less
	 * than or equal to the radius of a game piece distance from the
	 * intersection, then that intersection is considered to have been clicked
	 * In that scenario, this method returns true, otherwise it returns false
	 * This method exists to reduce clutter in the mouseClicked method.
	 * 
	 * @param x1
	 *            x coordinate of the intersection
	 * @param y1
	 *            y coordinate of the intersection
	 * @param x2
	 *            x coordinate of the mouse
	 * @param y2
	 *            y coordinate of the mouse
	 * @return true if click was close enough to the intersection, otherwise
	 *         false
	 */
	private boolean intersectionClicked(double x1, double y1, double x2,
			double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) <= PIECE_DIAMETER / 2;
	}

	/**
	 * The actionPerformed method responds to a button press by either player.
	 * Undo reverts the previous move. Pass passes a player's turn. End Game
	 * ends the game and causes the score to be evaluated without both players
	 * having to pass, as is normal. This button simulates the ability to
	 * concede and still have the score totaled.
	 */
	public void actionPerformed(ActionEvent e) {
		if ("Pass".equals(e.getActionCommand())) {
			pass++;
			if (pass >= 2) {
				endGame();
			}
			overwritePreviousAllegiances();
			nextPlayer();
		}
		if ("Undo".equals(e.getActionCommand())) {
			undo();
		}
		if ("End Game".equals(e.getActionCommand())) {
			endGame();
		}
	}

	/**
	 * nextPlayer is a void method that changes the current player to the other
	 * player and resets the undo counter, which prevents undo from being called
	 * multiple times in a row. It is called in response to a piece being placed
	 * or to a turn being passed.
	 */
	private void nextPlayer() {
		currentPlayer++;
		if (currentPlayer > 2) {
			currentPlayer = 1;
		}
		numUndos = 0;
	}

	/**
	 * undo is a method that reverts the previous move made by a player. If undo
	 * is chosen after a pass, it will only change the turn of the current
	 * player.
	 */
	private void undo() {
		if (numUndos < 1) {
			resetBoard();
			overwriteIntersections();
			restoreBoardState();
			nextPlayer();
			numUndos++;
		}
	}

	/**
	 * resetBoard is a void method that completely clears the board. It exists
	 * to make the undo method simpler. It allows the current board to be
	 * replaced with the previous board, which is done by the restoreBoardState
	 * method.
	 */
	private void resetBoard() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				if (intersections[i][j].getAllegiance() == 1
						|| intersections[i][j].getAllegiance() == 2) {
					remove(intersections[i][j].piece);
				}
			}
		}
	}

	private void restoreBoardState() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				if (intersections[i][j].getAllegiance() == 1
						|| intersections[i][j].getAllegiance() == 2) {
					add(intersections[i][j].piece);
				}
			}
		}
	}

	/**
	 * the endGame method is a void method called in response to the game
	 * ending. It causes the score to be tallied and determines the winner.
	 */
	private void endGame() {

	}

	/**
	 * overwriteIntersections is a void method that replaces every allegiance
	 * value in intersections with the corresponding value from
	 * previousAllegiances. It exists to simplify the Undo method, and is called
	 * immediately after every existing piece is removed from the board with
	 * resetBoard.
	 */
	private void overwriteIntersections() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				intersections[i][j].setAllegiance(previousAllegiances[i][j]);
			}
		}
	}

	/**
	 * overwritePreviousBoard is a void method that stores in an array the
	 * allegiance of each piece on the board immediately prior the move that was
	 * just made by a player. Aside from when the game is initialized, this
	 * always happens before the game state is changed somehow. It is called
	 * immediately before a piece is placed or after a player passes their turn.
	 */
	private void overwritePreviousAllegiances() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				previousAllegiances[i][j] = intersections[i][j].getAllegiance();
			}
		}
	}

	private void capturePieces() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				if (markedForCapture(i, j)) {
					remove(intersections[i][j].piece);
					intersections[i][j].setAllegiance(currentPlayer + 2);
				}
			}
		}
	}

	private boolean markedForCapture(int xIndex, int yIndex) {

		return false;
	}

	/* Possible additions
	 * label 1-19, a-s, accommodate less than 19x19 board sizes with this addition
	 */

}