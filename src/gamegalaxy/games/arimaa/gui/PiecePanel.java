package gamegalaxy.games.arimaa.gui;

import gamegalaxy.games.arimaa.data.PieceData;
import gamegalaxy.tools.ResourceLoader;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * Contains all of the information and code necessary for displaying a single playing piece
 * in the game Arimaa.  Should eventually implement the native Java Drag and Drop API.
 */
@SuppressWarnings("serial")
public class PiecePanel extends JPanel
{
	//Store the image affiliated with this particular piece.
	private Image pieceImage;

	//Store the object that is currently holding this piece.
	private PieceHolder	holder;

	private PieceData	data;
	
	public PiecePanel(final ArimaaUI gui, PieceData data, ResourceLoader loader)
	{
		//Store the information about this piece.
		this.data = data;
		
		//Create the mouse listener to listen for mouse events.
		MouseInputAdapter ma = new MouseInputAdapter()
		{
			//Stores the position that we started dragging from, in case we don't land anywhere.
			private int	dragXStart;
			private int	dragYStart;
			
			//Stores the last known position to update delta values against.
			private int	lastDragX;
			private int	lastDragY;

			public void mousePressed(MouseEvent me)
			{	
				//Identify where we're starting to drag.
				dragXStart = ((PiecePanel)me.getSource()).getX();
				dragYStart = ((PiecePanel)me.getSource()).getY();
					
				//Store the last known position.
				Point relativePoint = new Point(me.getX(), me.getY());
				SwingUtilities.convertPointToScreen(relativePoint, (PiecePanel)me.getSource());
				
				lastDragX = relativePoint.x;
				lastDragY = relativePoint.y;
			}
			
			public void mouseReleased(MouseEvent me)
			{
				PiecePanel piecePanel = (PiecePanel)me.getSource();
				
				/*
				 * Determine where we're dragging to, if anywhere.
				 * Note that we are using the center point to determine this.
				 * It makes it drag and drop more nicely.
				 */
				PieceHolder dropPanel = gui.getHolderAt(getX() + getWidth() / 2, getY() + getHeight() / 2);
				
				if(dropPanel == null)
				{
					//We didn't go anywhere.  Just reset the position of the object.
					piecePanel.setLocation(dragXStart, dragYStart);
				}
				else
				{
					/*
					 * We managed to drop it on somethin that was listening!
					 * Now go find out what it wants to do about you.
					 * 
					 * Until the engine is in place, this just moves the piece without
					 * validating anything.
					 */
					holder.removePiece(piecePanel);
					dropPanel.dropPiece(piecePanel);
				}
			}
			
			public void mouseDragged(MouseEvent me)
			{
				//Determine the updated position from the deltas.
				Point relativePoint = new Point(me.getX(), me.getY());
				SwingUtilities.convertPointToScreen(relativePoint, (PiecePanel)me.getSource());
				
				int myX = relativePoint.x;
				int myY = relativePoint.y;
				
				int newX = getX() + (myX - lastDragX);
				int newY = getY() + (myY - lastDragY);
				
				//Reset the position to update against in the future.
				lastDragX = myX;
				lastDragY = myY;

				//Set the new location
				setLocation(newX, newY);
			}
		};
		
		//Actually add the mouse listener to the panel.
		addMouseListener(ma);
		addMouseMotionListener(ma);
		
		//Load the image related to this piece.
		String pieceName = data.getColorString() + data.getNameString();
		System.out.println("Loading piece image for " + pieceName);
		pieceImage = loader.getResource(pieceName);
		
		//configure the size of this panel
		setSize(pieceImage.getWidth(this), pieceImage.getHeight(this));
	}
	
	/**
	 * 
	 * Draws the image associated with this piece, with a transparent background.
	 *
	 * @param g The graphics context for this paint operation.
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		g.drawImage(pieceImage, 0, 0, this);
	}

	/**
	 * Set the object that is currently holding this piece.
	 *
	 * @param pieceHolder The object that is holding this piece.
	 */
	public void setHolder(PieceHolder pieceHolder)
	{
		holder = pieceHolder;
	}

	/**
	 * Remove this piece from its holder.  Nothing is holding it right now.
	 *
	 */
	public void removeHolder()
	{
		holder = null;
	}

	/**
	 * TODO: Describe method
	 *
	 * @return
	 */
	public PieceData getData()
	{
		return data;
	}
}
