package src;

/**
 * File: Intersection.java
 * -----------------------
 * Written by Matthew Abbott
 * 
 * An intersection is an class created for the Go program
 * Intersections store the position and allegiance of an intersection
 * The allegiance of an intersection is a variable that represents who owns the intersection
 */

import java.awt.*;
import acm.graphics.*;

public class Intersection {
	
	private double xLocation;
	private double yLocation;
	
	
	/** Color of the intersection
	 * 0 means there is no piece on the intersection
	 * 1 means there is a black piece on the intersection
	 * 2 means there is a white piece on the intersection
	 * 3 means the intersection is part of black territory but has no piece
	 * 4 means the intersection is part of white territory but has no piece
	 * 5 means the intersection is neutral territory and has been checked
	 */
	private int allegiance;
	
//	/** determines the diameter of the game piece associated with the intersection */
//	private double pieceDiameter;
	
	/** This oval is the game piece associated with the given intersection
	 * It is drawn by the Go.java program when a player has chosen the intersection.
	 * Its color is concurrent with that of the player who owns it, and is modified by the setAllegiance method
	 * It is removed by the Go.java program when it is captured
	 */
	private GOval piece;
	
	private boolean marked = false;
	
	public Intersection(double xPos, double yPos, double pieceDiameter) {
		xLocation = xPos;
		yLocation = yPos;
		piece = new GOval(xLocation - pieceDiameter/2, yLocation - pieceDiameter/2, pieceDiameter, pieceDiameter);
		allegiance = 0;
	}
	
	public int getAllegiance() {
		return allegiance;
	}
	
	/** setAllegiance is a public method that, when called, alters the allegiance of the intersection according
	 * to the input player
	 * it also changes the color of the game piece associated with the intersection
	 * black if it's player 1, and white if it's player 2
	 * 
	 * @param player: the player who is taking the given intersection
	 */
	public void setAllegiance(int player) {
		allegiance = player;
		piece.setFilled(true);
		if (player == 1) {
			piece.setFillColor(Color.BLACK);
		} else if (player == 2) {
			piece.setFillColor(Color.WHITE);
		}
	}
	
	public double getX() {
		return xLocation;
	}
	
	public double getY() {
		return yLocation;
	}
	
	public boolean getMarked() {
		return marked;
	}
	public void setMarked(boolean b) {
		marked = b;
	}
	
	public GOval getPiece(){
		return piece;
	}
}