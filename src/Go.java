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
import java.util.*;
import java.lang.*;

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import javax.swing.*;

public class Go extends GraphicsProgram {

	/** the extra vertical space above the board, in pixels */
	private static final int EXTRA_HEIGHT = 50;

	/**
	 * Width and height of application window in pixels. Note: this is how large
	 * the application window should be. However, these parameters must be
	 * specified when this program is being run as a Java applet. In eclipse,
	 * which I wrote this program in, the run configuration must be modified
	 * such that the width and height of the window reflects these values
	 */
	public static final int APPLICATION_WIDTH = 800;
	public static final int APPLICATION_HEIGHT = 800 + EXTRA_HEIGHT;

	/**
	 * Number of vertical and horizontal lines that comprise the game board.
	 * Note: the board is always a square and will have numLines^2
	 * intersections. For a proper game of Go, there should be 19 lines. The
	 * existence of this constant also accommodates other board setups, such as
	 * the common beginner 9x9 board.
	 */
	private int numLines;

	/**
	 * These values represent the separation between each line, vertically and
	 * horizontally, respectively. They also represent the distance between
	 * adjacent intersections, in pixels.
	 */
	private int vertLineSep;
	private int horizLineSep;

	/**
	 * Width of one game piece. Uses vertical line separation to determine size,
	 * because the program is simpler without using horizontal line separation
	 * to determine the height of a piece. If the window were not a square, the
	 * pieces would not conform to its warped shape and would instead be circles
	 * diameters based on the separation between vertical lines. I have opted
	 * for this because it is simpler and because it maintains said circular
	 * shape of pieces even under strange window conditions.
	 */
	private double pieceDiameter;

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

	/**
	 * Determines whether the game should use Ko or Superko rules. Superko is a
	 * rule that states that no previous board state can ever be repeated. Ko
	 * means that only the board state of the previous turn cannot be repeated.
	 */
	private boolean usingKo;

	/** If the game is over, gameOver is true */
	private boolean gameOver = false;

	/**
	 * The white player recieves extra points at the end of the game based on
	 * options chosen by the players at the beginning.
	 */
	private int whiteDisadvantageBonus = 0;

	/**
	 * The white player will win in the event that both players have the same
	 * score at the end of the game if this is true. Players choose at the start
	 * of the game whether this is true or not.
	 */
	private boolean whiteWinsTies = false;

	/** The user inputs a previous board number to undo the game to in here */
	private JTextField undoField;

	/** the current turn number of the game */
	private int currentTurn = 1;

	private GLabel turnLabel;
	private GOval currentPlayerPiece;

	public void init() {

		GoOptionMenu menu = new GoOptionMenu();
		menu.setVisible(true);
		while (!menu.isCompleted())
			pause(20);
		menu.setVisible(false);

		assignAllOptions(menu);

		createBoard();
		initializeIntersections();
		overwritePreviousAllegiances();
		addTurnInformation();

		addMouseListeners();

		addJComponents();

		addActionListeners();

	}

	private void assignAllOptions(GoOptionMenu menu) {
		usingKo = menu.getUsingKo();
		whiteDisadvantageBonus = menu.getWhiteDisadvantageBonus();
		initializeBoardProperties(menu);
	}

	private void initializeBoardProperties(GoOptionMenu menu) {
		numLines = menu.getBoardSize();
		vertLineSep = APPLICATION_WIDTH / (numLines + 1);
		horizLineSep = (APPLICATION_HEIGHT - EXTRA_HEIGHT) / (numLines + 1);
		pieceDiameter = vertLineSep / 2;
		
	}

	/**
	 * koDialogResponse is an int method that returns the integer corresponding
	 * the the response a user chooses after being prompted with a JOptionPane.
	 * Ko is n == 0, Superko is 1. If the user clicks the option asking what ko
	 * and superko are, n = 2, another dialog window pops up explaining the
	 * rules, and the while loop restarts, asking the user again.
	 * 
	 * @return the user's response, 0 if ko, 1 if superko
	 */
	private int koDialogResponse() {
		String[] options = { "Ko", "Superko", "What are these things?" };
		int n = -1;
		while (n == -1) { // n is -1 if the user doesn't choose an option
			n = JOptionPane.showOptionDialog(this,
					"Are you using Ko or Superko for this game?", "Rules",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 2) { // user asks what Ko and Superko are
				n = -1;
				JOptionPane
						.showMessageDialog(
								this,
								"Under Ko, you cannot make a move that would cause the board "
										+ "\nto be layed out the same way it was after your previous move."
										+ " \nUnder Superko, no previous board layout can ever be repeated.",
								"Rules", JOptionPane.PLAIN_MESSAGE);
			}
		}
		return n;
	}

	/** testing stuff */
	public void run() {

	}

	/**
	 * createBoard is a simple method that draws all the lines that comprise the
	 * Go game board. It determines the locations of the lines based on the
	 * constants that define how many lines there are, how large the application
	 * window should be, and how much space there is above the board
	 */
	private void createBoard() {
		for (int i = 1; i < numLines + 1; i++) {
			GLine vertLine = new GLine(vertLineSep * i, horizLineSep
					+ EXTRA_HEIGHT, vertLineSep * i, APPLICATION_HEIGHT
					- horizLineSep);

			GLine horizLine = new GLine(vertLineSep, EXTRA_HEIGHT
					+ horizLineSep * i, APPLICATION_WIDTH - vertLineSep,
					EXTRA_HEIGHT + horizLineSep * i);

			horizLine.setColor(Color.BLACK);
			vertLine.setColor(Color.BLACK);
			add(vertLine);
			add(horizLine);
		}

	}

	/**
	 * initializeIntersections is a private method that creates all of the
	 * intersections and stores each of them in the appropriate index of a
	 * numLines x numLines array. The x locations and y locations of each
	 * intersection are determined by using the separation between vertical
	 * lines and horizontal lines as well as i and j, respectively. The for
	 * loops store each new intersection in the array. The indices of each
	 * intersection represent the x and y number of each intersection. That is,
	 * index 0,0 is the leftmost topmost intersection, while 0,1 is the
	 * intersection directly below it
	 */
	private void initializeIntersections() {
		intersections = new Intersection[numLines][numLines];
		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				intersections[i][j] = new Intersection(vertLineSep * (i + 1),
						EXTRA_HEIGHT + horizLineSep * (j + 1), pieceDiameter);
			}

		}
	}

	/**
	 * addTurnInformation is a void method that adds a GLabel that says
	 * "Current player:", as well as a separate GLabel that has the current turn
	 * number on it. It also adds a GOval that is the size of a normal piece
	 * with that is the color of the black player. This piece is changed by the
	 * displayTurnInformation method, along with the turn count GLabel, which is
	 * called after any player makes a move.
	 */
	private void addTurnInformation() {
		GLabel currentPlayerLabel = new GLabel("Current Player:", 0,
				EXTRA_HEIGHT);
		currentPlayerLabel.setColor(Color.BLACK);
		currentPlayerLabel.setFont("Plain-*-18");
		add(currentPlayerLabel);

		currentPlayerPiece = new GOval(currentPlayerLabel.getWidth()
				+ pieceDiameter / 2, EXTRA_HEIGHT - pieceDiameter / 3
				- currentPlayerLabel.getAscent() / 2, pieceDiameter,
				pieceDiameter);
		currentPlayerPiece.setFilled(true);
		add(currentPlayerPiece);

		turnLabel = new GLabel("Current Turn: 1", 0, EXTRA_HEIGHT / 2);
		turnLabel.setColor(Color.BLACK);
		turnLabel.setFont("Plain-*-18");
		add(turnLabel);

	}

	/**
	 * addJComponents is a void method that add ass of the salient javax
	 * components to the game. It exists to make the init method easier to read
	 */
	private void addJComponents() {
		add(new JLabel("How many turns back do you want to go?:"), NORTH);
		undoField = new JTextField(2);
		undoField.setActionCommand("Undo");
		add(undoField, NORTH);
		undoField.setText("1");

		add(new JButton("Undo"), NORTH);

		add(new JButton("Pass"), NORTH);
		add(new JButton("End Game"), NORTH);
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
		if (!gameOver) {

			for (int i = 0; i < numLines; i++) {
				for (int j = 0; j < numLines; j++) {

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
							displayTurnInformation();
							checkNeighbors(i, j);

						}

					}

				}
			}

			koCheck();
		}
	}

	/**
	 * koCheck is a void method that checks if the move the player made breaks
	 * the Ko or Superko rule, depending on which is being used in this game. If
	 * the player is breaking either rule, a quick JOptionPane message dialog
	 * will pop up mentioning that they repeated a/the previous board state
	 * depending on whether superko or ko are being used, respectively.
	 */
	private void koCheck() {
		if (usingKo) {

			if (breakingKo()) {
				undo(-1);
				JOptionPane
						.showMessageDialog(
								this,
								"It is illegal to make a move that repeats the board state of your previous move.",
								"Rules", JOptionPane.PLAIN_MESSAGE);
			}

		} else {
			if (breakingSuperko()) {
				undo(-1);
				JOptionPane
						.showMessageDialog(
								this,
								"It is illegal to make a move that repeats the board state of any previous move.",
								"Rules", JOptionPane.PLAIN_MESSAGE);
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
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) <= pieceDiameter / 2;
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

			if (allPreviousAllegiances.size() > 1) {

				pass++;
				overwritePreviousAllegiances();
				nextPlayer();
				displayTurnInformation();

				if (pass >= 2) {
					endGame();
				}

			} else {
				JOptionPane
						.showMessageDialog(
								this,
								"Your pass has been undone, surely you can think of a better move.",
								"I'm assuming that was a mistake.",
								JOptionPane.PLAIN_MESSAGE);
			}
		}

		if ("Undo".equals(e.getActionCommand())) {
			try {
				int numTurns = Integer.parseInt(undoField.getText());

				if (numTurns > 0 && numTurns <= allPreviousAllegiances.size()) {
					undo(numTurns);
				} else {
					undo(1);
				}
			} catch (NullPointerException playerInputInvalid) {
				nonIntegerInputMessage();
				undo(1);

			} catch (NumberFormatException playerInputInvalid) {
				nonIntegerInputMessage();
				undo(1);
			}

		}

		if ("End Game".equals(e.getActionCommand())) {

			if (allPreviousAllegiances.size() > 2) {
				endGame();

			} else {

				JOptionPane.showMessageDialog(this,
						"Are you honestly going to end the game like this?",
						"What a strange decision.", JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	/**
	 * If the player inputs a non integer valuu (or no value at all) into the
	 * undofield, they are notified with a dialog window
	 */
	private void nonIntegerInputMessage() {
		JOptionPane.showMessageDialog(this,
				"Please input integer values for this field"
						+ "\n1 turn has been undone", "Error",
				JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * nextPlayer is a void method that changes the current player to the other
	 * player and vice versa. It is called in response to a piece being placed
	 * or to a turn being passed. It also calls the displayCurrentPlayerMethod,
	 * which shows whose turn it is at any given time.
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

	private void displayTurnInformation() {
		currentTurn++;
		if (currentTurn % 2 == 0) {
			currentPlayerPiece.setFilled(false);
		} else {
			currentPlayerPiece.setFilled(true);
		}

		remove(turnLabel);
		turnLabel = new GLabel("Current Turn: " + currentTurn, 0,
				EXTRA_HEIGHT / 2);
		turnLabel.setColor(Color.BLACK);
		turnLabel.setFont("Plain-*-18");
		add(turnLabel);

	}

	/**
	 * undo is a method that reverts the previous move made by a player. If undo
	 * is chosen after a pass, it will only change the turn of the current
	 * player. There is no longer any limit on the number of possible undos
	 */
	private void undo(int numTurns) {
		if (pass > 0) {
			pass--;
		}

		if (allPreviousAllegiances.size() > 1) {

			resetBoard();
			overwriteIntersections(numTurns - 1);
			restoreBoardState();

			currentTurn -= numTurns + 1;
			for (int i = 0; i < numTurns; i++) {
				nextPlayer();
			}
			displayTurnInformation();

		}

		gameOver = false;
	}

	/**
	 * resetBoard is a void method that completely clears the board. It exists
	 * to make the undo method simpler. It allows the current board to be
	 * replaced with a previous board, which is done by the restoreBoardState
	 * method.
	 */
	private void resetBoard() {
		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

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
		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				if (intersections[i][j].getAllegiance() == 1
						|| intersections[i][j].getAllegiance() == 2) {

					add(intersections[i][j].getPiece());

				}

			}
		}
	}

	/**
	 * overwriteIntersections is a void method that replaces every allegiance
	 * value in intersections with the corresponding value from the board at
	 * index boardIndex of allPreviousAllegiances. Additionally, it removes that
	 * particular board state from the ArrayList and every board state that came
	 * after it (ie earlier indices), since the game board has been reverted to
	 * that state. It exists to simplify the Undo method, and is called
	 * immediately after every existing piece is removed from the board with
	 * resetBoard.
	 * 
	 * @param boardIndex
	 *            the index of the board the player has chosen to revert to when
	 *            the press the undo button. It is 0 if they are reverting the
	 *            last move
	 */
	private void overwriteIntersections(int boardIndex) {

		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				intersections[i][j].setAllegiance(allPreviousAllegiances
						.get(boardIndex)[i][j]);

			}
		}

		for (int i = 0; i <= boardIndex; i++) {
			allPreviousAllegiances.remove(0);
		}
	}

	/**
	 * overwritePreviousAllegiances is a void method that stores in an ArrayList
	 * the arrays that contain the allegiance of each piece on the board for
	 * every single previous board state. It is called immediately before a
	 * piece is placed or after a player passes their turn, as well as at the
	 * beginning of the game to store the empty board as the first board state.
	 */
	private void overwritePreviousAllegiances() {

		int[][] previousAllegiances = new int[numLines][numLines];

		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				previousAllegiances[i][j] = intersections[i][j].getAllegiance();

			}
		}

		allPreviousAllegiances.add(0, previousAllegiances);
	}

	/**
	 * breakingKo is a boolean method helps enforce the Ko rule in the koCheck
	 * method. Ko is a rule in go that prevents a player from making a move that
	 * results in the board state of their previous turn being repeated. This
	 * means that if the array of allegiances 2 moves ago is the same as that of
	 * th move being made, then the move being made is invalid. This method
	 * returns false if there have not been enough moves in the game for Ko to
	 * matter, or if the previous turn of that player did not have the same
	 * board state.
	 * 
	 * @return true if the player has made a move that repeats the board state
	 *         of their previous move
	 */
	private boolean breakingKo() {

		if (allPreviousAllegiances.size() < 2) {
			return false;
		}

		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				if (intersections[i][j].getAllegiance() != allPreviousAllegiances
						.get(1)[i][j]) {
					return false;
				}

			}
		}
		return true;
	}

	/**
	 * breakingSuperko is the same as breakingKo, except it checks for every
	 * previous board rather than just the board from the previous move.
	 * 
	 * @return true if the player has made a move that repeats the board state
	 *         of any previous move
	 */
	private boolean breakingSuperko() {

		if (allPreviousAllegiances.size() < 2) {
			return false;
		}

		for (int i = 0; i < allPreviousAllegiances.size(); i++) {
			boolean arraysSame = true;

			for (int j = 0; j < numLines; j++) {
				for (int k = 0; k < numLines; k++) {

					if (intersections[j][k].getAllegiance() != allPreviousAllegiances
							.get(i)[j][k]) {
						arraysSame = false;
					}

				}
			}

			if (arraysSame) {
				return true;
			}
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

		if (y >= 0 && y < numLines && x >= 0 && x < numLines) {

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

		if (x < 0 || y < 0 || y >= numLines || x >= numLines) {
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

	/**
	 * the endGame method is a void method called in response to the game
	 * ending. It causes the score to be tallied and determines the winner. It
	 * then creates a message dialog that states the winner. After the winner
	 * has been chosen, players can no longer place pieces on the board, but if
	 * they press undo, the gameOver variable is reset to false and the players
	 * can play again from that point.
	 */
	private void endGame() {
		gameOver = true;

		int lastPlayer = currentPlayer; // stores the current player

		String gameWinner = determineWinner();
		JOptionPane
				.showMessageDialog(
						this,
						"The game is over and "
								+ gameWinner
								+ " has won. \nHowever, you can still click undo to keep the game going",
						("Victory for " + gameWinner),
						JOptionPane.PLAIN_MESSAGE);

		/*
		 * Returns currentPlayer and opposingPlayer to normal so players can
		 * keep playing if they press undo.
		 */
		currentPlayer = lastPlayer;
		opposingPlayer = currentPlayer + 1;
		if (opposingPlayer == 3) {
			opposingPlayer = 1;
		}
	}

	/**
	 * determineWinner is a String method that returns a string with the name of
	 * the color of the player that won. It checks every intersection in the
	 * game to see what territory it belongs to. If the intersection has been
	 * previously checked or was already claimed by a player (allegiance == 1 or
	 * 2, the intersection has a piece on it) it is ignored, otherwise it is
	 * checked by setColorTerritory and is changed to the corresponding
	 * allegiance of the player that owns it, along with every other
	 * intersection that was connected to it. (allegiance 3 is black territory,
	 * 4 is white, 5 is unaligned/neutral)
	 * 
	 * @return the color of the winner
	 */
	private String determineWinner() {

		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				if (intersections[i][j].getAllegiance() == 0) {
					setColorTerritory(i, j);
				}

			}
		}

		currentPlayer = 1;
		int blackScore = tallyScore();
		System.out.println("Black has " + blackScore + " points.");

		currentPlayer = 2;
		int whiteScore = tallyScore();
		System.out.println("White has " + whiteScore + " points.");

		if (blackScore > whiteScore) {
			return "black";
		} else if (whiteScore > blackScore) {
			return "white";
		} else if (whiteWinsTies) {
			return "white";
		} else {
			return "neither player";
		}

	}

	/**
	 * setColorTerritory is a method that helps determine the winner by changing
	 * the allegiance of a piece that currently has allegiance 0 to the the
	 * allegiance of the territory it should be. If the piece is surrounded by
	 * black pieces, it is allegiance 3. White, allegiance 4, and if it is
	 * connected to both colors, it is of allegiance 5: neutral.
	 * setColorTerritory changes the allegiance of every empty space touching
	 * the space at indices x and y to its appropriate value by using the
	 * checkCurrentPlayerTerritory method to see if the piece is in the
	 * territory of the current player. After checking both black and white, if
	 * the piece was not part of either player's territory, then its allegiance
	 * is changed to 5 (neutral).
	 * 
	 * @param x
	 *            the x index of the space
	 * @param y
	 *            the y index of the space
	 * @param space
	 *            the ArrayList storing every space that has been checked and
	 *            has had its allegiance changed to a number from 3 to 5
	 */
	private void setColorTerritory(int x, int y) {

		ArrayList<Intersection> chain = new ArrayList<Intersection>();

		currentPlayer = 1;
		opposingPlayer = 2;
		if (!checkCurrentPlayerTerritory(x, y, chain)) {

			chain.clear();
			currentPlayer = 2;
			opposingPlayer = 1;
			if (!checkCurrentPlayerTerritory(x, y, chain)) {
				/*
				 * if the intersections were touching both white and black (or
				 * no color for some reason), the pieces are now guaranteed to
				 * be neutral
				 */
				for (int i = 0; i < chain.size(); i++) {
					chain.get(i).setAllegiance(5);
				}
			}
		}

	}

	/**
	 * checkCurrentPlayerTerritory is a boolean that receives the x and y index
	 * of a space that needs to be checked and an ArrayList chain which is used
	 * to store every space connected to that space. If currentPlayer is 1
	 * (black), it will use determineTerritory to check the space and every
	 * connected space to see if those spaces are black territory. Those pieces
	 * will be marked by determineTerritory if they are. Then it will check to
	 * see if any piece is unmarked, because that would mean that every piece is
	 * not a part of black territory. If so, every piece is unmarked, the
	 * allegiances are not changed and false is returned. If the pieces are part
	 * of black territory then true is returned and setColorTerritory will also
	 * terminate. If every piece is marked, then their allegiances are changed
	 * to currentPlayer + 2, the corresponding territory allegiance to
	 * currentPlayer, which is 3 for black.
	 * 
	 * If false was returned, setColorTerritory will alter current and opposing
	 * player such that 2 (white) is the current player and this method will be
	 * called again. It will do for white, this time, what it did for black,
	 * returning true if the pieces are white territory and false otherwise.
	 * 
	 * Note: currentPlayer is set to 1 when this method is being called to
	 * determine black territory and 2 when it is being called to determine
	 * white territory. opposingPlayer is also appropriately changed.
	 * 
	 * @param x
	 *            the x index of the space being checked
	 * @param y
	 *            the y index of the space being checked
	 * @param chain
	 *            the ArrayList containing every space connected to the one at
	 *            (x, y)
	 * @return whether or not the piece is part of the current player's
	 *         territory
	 */
	private boolean checkCurrentPlayerTerritory(int x, int y,
			ArrayList<Intersection> chain) {
		determineTerritory(x, y, chain);
		/*
		 * everything in chain is now marked if it was touching only pieces of
		 * currentPlayer's color
		 */
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
				chain.get(i).setAllegiance(currentPlayer + 2);
				chain.get(i).setMarked(false);
			}
			return true;
		}
		return false;
	}

	/**
	 * determineTerritory is a modified version of markedForCapture that
	 * recursively checks each space to see if it is surrounded by pieces owned
	 * by the current player. It changes the marked variable of every space that
	 * is surrounded by previously checked spaces, walls or pieces of the
	 * allegiance of the current player to true. If any space in the ArrayList
	 * chain is unmarked, that means it was connected to a piece of the opposing
	 * player's color. This means that in the checkCurrentPlayerTerritory
	 * method, every space will be unmarked and their allegiances will not be
	 * changed to the territory of the current player
	 */
	private boolean determineTerritory(int x, int y,
			ArrayList<Intersection> chain) {

		if (x < 0 || y < 0 || y >= numLines || x >= numLines) {
			return true;
		} else if (intersections[x][y].getAllegiance() == currentPlayer) {
			return true;
		}
		if (intersections[x][y].getAllegiance() == opposingPlayer) {
			return false;
		}
		if (chain.contains(intersections[x][y])) {
			intersections[x][y].setMarked(true);
			return true;
		}
		chain.add(intersections[x][y]);
		if (determineTerritory(x, y - 1, chain)
				&& determineTerritory(x - 1, y, chain)
				&& determineTerritory(x + 1, y, chain)
				&& determineTerritory(x, y + 1, chain)) {
			intersections[x][y].setMarked(true);
			return true;
		}
		intersections[x][y].setMarked(false);
		return false;

	}

	/**
	 * tallyScore is an integer method that returns the total score of the
	 * current player. It does so by checking every intersection and
	 * incrementing the variable totalCurrentPlayerScore by 1 for every
	 * intersection that is of the allegiance or corresponding territory
	 * allegiance of the current player. That is, if the current player is 1
	 * (black), then any space/piece with allegiance 1 or 3 increases black's
	 * score by 1. Additionally, if there is a bonus for white for going second,
	 * white's total score will also increase.
	 * 
	 * @return currentPlayer's total score
	 */
	private int tallyScore() {

		int totalCurrentPlayerScore = 0;
		if (currentPlayer == 2) {
			totalCurrentPlayerScore = whiteDisadvantageBonus;
		}

		for (int i = 0; i < numLines; i++) {
			for (int j = 0; j < numLines; j++) {

				if (intersections[i][j].getAllegiance() == currentPlayer
						|| intersections[i][j].getAllegiance() == currentPlayer + 2) {

					totalCurrentPlayerScore++;
				}

			}
		}

		return totalCurrentPlayerScore;
	}

	/*
	 * Display: current turn number and current player color using Glabels and a
	 * circle Undo a certain number of terms.
	 * 
	 * Option to enter the number of a previous board state to revert to. Option
	 * to view a previous board state without having to revert to it.
	 * 
	 * Display the color of the current player somewhere
	 * 
	 * JFrame for options ko vs superko, handicap, black gets extra moves
	 * scoring handicap, white gets + x.5 points at the end, where x is input by
	 * the players board size, defaulting to 19x19
	 * 
	 * Label 1-19, a-s, must accommodate less than 19x19 board sizes with this
	 * addition
	 */
}
