package src;

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
import java.util.ArrayList;

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
	private int opposingPlayer = 2;

	/**
	 * This array contains all of the intersections on the game board. It exists
	 * so that they can be accessed by all methods of the program.
	 */
	private Intersection[][] intersections;

	/**
	 * This ArrayList contains the board state of every previous turn. The most
	 * recent previous board state is stored in index 0, with each previous turn
	 * in increasing order. If undo is pressed, the most recent prior board
	 * state replaces the current one and is removed from this ArrayList
	 */
	private ArrayList<int[][]> allPreviousAllegiances = new ArrayList<int[][]>();

	/**
	 * pass stores the number of times a turn has been passed consecutively. It
	 * is reset to 0 once a player places a piece and is incremented by one if a
	 * player passes. Should it reach 2, the game ends as per the rules of Go.
	 */
	private int pass = 0;

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

	/** testing stuff */
	public void run() {

	}

	/**
	 * createBoard is a simple method that draws all the lines that comprise the
	 * Go game board. It determines the locations of the lines based on the
	 * constants that define how many lines there are and how large the
	 * application window should be
	 */
	private void createBoard() {
		for (int i = 1; i < NUM_LINES + 1; i++) {
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
						add(intersections[i][j].getPiece());

						pass = 0;
						capturePieces(i, j);
						nextPlayer();
						checkNeighbors(i, j);

					}

				}

			}
		}
		if (breakingKo()) {
			undo();
			System.out
					.println("It is illegal to make a move that repeats the board state of your previous move.");
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
		opposingPlayer++;
		if (opposingPlayer > 2) {
			opposingPlayer = 1;
		}
	}

	/**
	 * undo is a method that reverts the previous move made by a player. If undo
	 * is chosen after a pass, it will only change the turn of the current
	 * player. There is no longer any limit on the number of possible undos
	 */
	private void undo() {
		if (allPreviousAllegiances.size() > 1) {
			resetBoard();
			overwriteIntersections();
			restoreBoardState();
			nextPlayer();
		}
	}

	/**
	 * resetBoard is a void method that completely clears the board. It exists
	 * to make the undo method simpler. It allows the current board to be
	 * replaced with a previous board, which is done by the restoreBoardState
	 * method.
	 */
	private void resetBoard() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				if (intersections[i][j].getAllegiance() == 1
						|| intersections[i][j].getAllegiance() == 2) {
					remove(intersections[i][j].getPiece());
				}
			}
		}
	}

	/**
	 * restoreBoardState is a void method that adds every piece that should be
	 * on the board to the board according to the first element of the
	 * allPreviousAllegiances array ArrayList. It assumes that the board is
	 * currently empty and is only called after resetBoard and
	 * overwriteIntersections
	 */
	private void restoreBoardState() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				if (intersections[i][j].getAllegiance() == 1
						|| intersections[i][j].getAllegiance() == 2) {
					add(intersections[i][j].getPiece());
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
	 * value in intersections with the corresponding value from index 0 of
	 * allPreviousAllegiances. Additionally, it removes that particular board
	 * state from the ArrayList, now that the game board has been reverted to
	 * that state. It exists to simplify the Undo method, and is called
	 * immediately after every existing piece is removed from the board with
	 * resetBoard.
	 */
	private void overwriteIntersections() {
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				intersections[i][j]
						.setAllegiance(allPreviousAllegiances.get(0)[i][j]);
			}
		}
		allPreviousAllegiances.remove(0);
	}

	/**
	 * overwritePreviousAllegiances is a void method that stores in an ArrayList
	 * the arrays that contain the allegiance of each piece on the board for
	 * every single previous board state. It is called immediately before a
	 * piece is placed or after a player passes their turn, as well as at the
	 * beginning of the game to store the empty board as the first board state.
	 */
	private void overwritePreviousAllegiances() {
		int[][] previousAllegiances = new int[NUM_LINES][NUM_LINES];
		for (int i = 0; i < NUM_LINES; i++) {
			for (int j = 0; j < NUM_LINES; j++) {
				previousAllegiances[i][j] = intersections[i][j].getAllegiance();
			}
		}
		allPreviousAllegiances.add(0, previousAllegiances);
	}

	/**
	 * Ko is a rule in go that prevents a player from making a move that results
	 * in the board state of their previous turn being repeated. This means that
	 * if array of allegiances moves ago is the same as the move being made,
	 * then the move being made is invalid. This method returns false if there
	 * have not been enough moves in the game for Ko to matter, or if the
	 * previous turn of that player did not have the same board state.
	 * 
	 * @return true if the player has made a move that repeats the board state
	 *         of their previous move
	 */
	private boolean breakingKo() {
		if (allPreviousAllegiances.size() < 4) {
			return false;
		}
		if (allPreviousAllegiances.get(1).equals(allPreviousAllegiances.get(3))) {
			return true;
		}
		return false;
	}

	/**
	 * capturePieces is a void method that checks to see if any of the pieces
	 * adjacent to the most recently placed piece are part of a group that
	 * should be captured, then removes any group as appropriate. Each call of
	 * checkNeighbors checks a different neighboring intersection for opposing
	 * allegiance pieces.
	 * 
	 * checkNeighbors only needs to be called 4 times since captures can only be
	 * performed on chains of pieces that are touching the last placed piece.
	 * However, since the capture methods only operate on pieces not of the
	 * allegiance of the current player, self capture doesn't happen, the pieces
	 * that would be self captured are not adjacent to the piece that is next
	 * played after the current turn ends and so can never be captured. This is
	 * currently an fault in the program.
	 * 
	 * @param x
	 *            the x index of the piece just placed
	 * @param y
	 *            the y index of the piece just placed
	 */
	private void capturePieces(int x, int y) {

		checkNeighbors(x, y - 1);
		checkNeighbors(x + 1, y);
		checkNeighbors(x - 1, y);
		checkNeighbors(x, y + 1);

	}

	/**
	 * checkNeighbors is a method that performs some of the capture
	 * functionality. It receives the indices of one of the four intersections
	 * adjacent to the most recently placed piece. If the intersection has a
	 * piece of the opposite player's allegiance, then the recursive
	 * markedForCapture method is called to determine if it should be removed.
	 * The arrayList chain is altered to store every piece of the same color
	 * attached to the aforementioned piece, and modifies the marked boolean to
	 * true for any completely surrounded piece. After, if any piece in the
	 * arrayList is unmarked, then none of the pieces are captured, whereas if
	 * every piece is marked, the pieces are all removed.
	 * 
	 * checkNeighbors is additionally called once more on the last played piece
	 * itself after the current player allegiance has switched but before that
	 * player's turn starts in order to check for self capture. Self-capture is
	 * when a player makes a move that causes a chain of their own pieces to be
	 * captured. It always occurs after any sort of regular capture, and does
	 * not occur if the pieces causing the self-capture would be removed by
	 * regular capture.
	 * 
	 * @param x
	 *            the x index of the adjacent piece
	 * @param y
	 *            the y index of the adjacent piece
	 */
	private void checkNeighbors(int x, int y) {
		if (y >= 0 && y < NUM_LINES && x >= 0 && x < NUM_LINES) {
			if (intersections[x][y].getAllegiance() == opposingPlayer) {
				ArrayList<Intersection> chain = new ArrayList<Intersection>();
				markedForCapture(x, y, chain);

				for (int i = 0; i < chain.size(); i++) {
					if (!chain.get(i).getMarked()) {
						for (int j = 0; j < chain.size(); j++) {
							chain.get(j).setMarked(false);
						}
						break;
					}
				}
				if (chain.get(0).getMarked()) {
					for (int i = 0; i < chain.size(); i++) {
						remove(chain.get(i).getPiece());
						chain.get(i).setAllegiance(0);
					}
				}

			}
		}

	}

	/**
	 * markedForCapture is a recursive method that determines if a piece that is
	 * part of a group should be "marked", which is a boolean that is true if
	 * the piece has no empty spaces adjacent to it. It modifies the arrayList
	 * chain to store every intersection that is part of this group of
	 * same-color pieces, and marks all pieces that should be marked.
	 * 
	 * Base case: all edges are either walls, opposing allegiances or previously
	 * checked pieces. Alternatively, any edge is a space. Recursive step, check
	 * adjacent pieces to see if they meet the base case, add them to the
	 * arrayList of previously checked pieces and 'mark' them as appropriate
	 * 
	 * 
	 * @param x
	 *            the x index of the piece being checked
	 * @param y
	 *            the y index of the piece being checked
	 * @param chain
	 *            the arrayList that stores the checked intersections of the
	 *            chain of pieces
	 * @return whether or not the previous piece should be marked
	 */
	private boolean markedForCapture(int x, int y, ArrayList<Intersection> chain) {
		/*
		 * Capture rules, for reference: if a piece has no 'liberties' that is,
		 * empty spaces around it, then it is slated to be captured however, if
		 * any of its liberties are occupied by a piece of the same color, then
		 * it will not be captured. Again however, if those pieces of the same
		 * color have no liberties they are marked for capture under the same
		 * conditions and all pieces will be captured.
		 * 
		 * Recursion plan: recursion 4 directions. Base case: all edges are
		 * either walls, opposing allegiances or previously checked pieces.
		 * Alternatively, any edge is a space Recursive step, check adjacent
		 * pieces to see if they meet the base case, add them to the arrayList
		 * as appropriate
		 */

		if (x < 0 || y < 0 || y >= NUM_LINES || x >= NUM_LINES) {
			return true;
		} else if (intersections[x][y].getAllegiance() == currentPlayer) {
			return true;
		}
		if (intersections[x][y].getAllegiance() == 0) {
			return false;
		}
		if (chain.contains(intersections[x][y])) {
			intersections[x][y].setMarked(true);
			return true;
		}
		chain.add(intersections[x][y]);
		if (markedForCapture(x, y - 1, chain)
				&& markedForCapture(x - 1, y, chain)
				&& markedForCapture(x + 1, y, chain)
				&& markedForCapture(x, y + 1, chain)) {
			intersections[x][y].setMarked(true);
			return true;
		}
		intersections[x][y].setMarked(false);
		return false;

	}

	/*
	 * Possible additions label 1-19, a-s, accommodate less than 19x19 board
	 * sizes with this addition
	 */
}