package gamegalaxy.games.arimaa.gui;

import gamegalaxy.games.arimaa.data.PiecePosition;
import gamegalaxy.games.arimaa.data.GameConstants;
import gamegalaxy.games.arimaa.data.PieceData;
import gamegalaxy.games.arimaa.engine.ArimaaEngine;
import gamegalaxy.tools.ResourceLoader;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * The main UI for the Arimaa game implementation within the galaxy of games.
 * 
 * Eventually, this should be a subclass of a generic UI type of system.  For now,
 * it's a class unto itself.
 */
@SuppressWarnings("serial")
public class ArimaaUI extends JPanel
{
	private BoardPanel		boardPanel;
	private BucketPanel		goldBucketPanel;
	private BucketPanel		silverBucketPanel;
	
	//Store the background image that we want to draw.
	private Image 				backgroundImage;
	private List<PiecePanel>	piecePanels;
	private TurnPanel			turnPanel;
	private ArimaaEngine		engine;
	
	private HighlightPanel		highlight;
	
	/**
	 * 
	 * Construct the UI, load any files needed for it, set the layout, etc.
	 * @param loader 
	 *
	 */
	public ArimaaUI(ArimaaEngine engine, ResourceLoader loader)
	{	
		//Link the engine and GUI.
		this.engine = engine;
		engine.linkGUI(this);
		
		//Configure this panel
		setLayout(null);

		//Create the background image.
		backgroundImage = loader.getResource("AppBackground");
		
		createPieces(engine, loader);
		
		//Create highlight panel.
		highlight = new HighlightPanel(loader);
		add(highlight);
		
		//Create other components
		boardPanel = new BoardPanel(loader);
		add(boardPanel);
		boardPanel.setLocation(248, 120);
		
		//Create buckets
		goldBucketPanel = new BucketPanel(loader);
		add(goldBucketPanel);
		goldBucketPanel.setLocation(48, 132);
		
		silverBucketPanel = new BucketPanel(loader);
		add(silverBucketPanel);
		silverBucketPanel.setLocation(822, 132);
		
		turnPanel = new TurnPanel(this, loader);
		add(turnPanel);
		turnPanel.setLocation(282, 659);
		
		setPreferredSize(new Dimension(1024, 768));
		
		//Start with the pieces in the buckets.
		initializePieceLocations();
		turnPanel.setTurnState(GameConstants.GOLD);
	}
	
	/**
	 * TODO: Describe method
	 *
	 */
	private void initializePieceLocations()
	{
		Iterator<PiecePanel> piecePanelIterator = piecePanels.iterator();
		while(piecePanelIterator.hasNext())
		{
			PiecePanel tempPiece = piecePanelIterator.next();
			if(tempPiece.getData().getColor() == GameConstants.GOLD)
			{
				goldBucketPanel.dropPiece(tempPiece);
			}
			else
			{
				silverBucketPanel.dropPiece(tempPiece);
			}
		}
	}

	/**
	 * TODO: Describe method
	 *
	 * @param engine
	 * @param loader
	 */
	private void createPieces(ArimaaEngine engine, ResourceLoader loader)
	{
		List<PieceData> pieceDataList = engine.getPieces(); 
		Iterator<PieceData> iterator = pieceDataList.iterator();
		piecePanels = new Vector<PiecePanel>(pieceDataList.size());
		while(iterator.hasNext())
		{
			PieceData pieceData = iterator.next();
			PiecePanel tempPiece = new PiecePanel(this, pieceData, loader);
			add(tempPiece);
			piecePanels.add(tempPiece);
		}
	}

	/**
	 * 
	 * Draw the background image for this panel.  Do not draw anything else.
	 *
	 * @param g The graphics context for drawing on this panel.
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		//Paint the background
		g.drawImage(backgroundImage, 0, 0, this);
	}

	/**
	 * Since this is the main UI class, it is responsible for figuring out what
	 * objects at the top level want to respond to a drag and drop event.
	 * 
	 * This should be replaced when we refactor this to use Java's built in
	 * Drag-n-drop functionality.
	 *
	 * @param x The x coordinate, relative to the UI, of the place we are interested
	 * 			in looking for a drop target.
	 * @param y The y coordinate, relative to the UI, of the place we are interested
	 * 			in looking for a drop target.
	 * @return The drop target associated with this location. 
	 */
	private PieceHolder getHolderAt(int x, int y)
	{
		Component[] components = getComponents();
		for(int i = 0; i < components.length; i++)
		{
			//Store a link to the component in question.
			Component component = components[i];
			Rectangle bounds = component.getBounds();
			
			if(x >= bounds.getMinX() 
				&& x <= bounds.getMaxX() 
				&& y >= bounds.getMinY()
				&& y <= bounds.getMaxY())
			{
				if(component instanceof PieceHolder)
				{
					return (PieceHolder)component;
				}
			}
		}
	
		//We couldn't find anything.
		return null;
	}
	
	public boolean canDragPiece(PiecePanel piecePanel)
	{
		return engine.isPieceSelectable(piecePanel.getData());
	}

	/**
	 * Notifies the gui as pieces are being dragged, so that it can respond
	 * with real-time board highlighting.
	 *
	 * @param piecePanel 	The piece that is currently being dragged.
	 * @param holder 		The holder where this piece originated from.
	 * @param dragLocation  The (x,y) coordinates of the piece center.
	 */
	public void draggedPiece(PiecePanel piecePanel, PieceHolder holder,
			Point dragLocation)
	{
		//Find out where we're coming from
		PieceHolder dragOverPanel = getHolderAt(dragLocation.x, dragLocation.y); 	

		if(dragOverPanel == boardPanel)
		{
			//Determine what square we're over:
			int relativeDragX = dragLocation.x - boardPanel.getX();
			int relativeDragY = dragLocation.y - boardPanel.getY();
			PiecePosition dragOverPosition = boardPanel.identifyBoardPosition(relativeDragX, relativeDragY);
			
			//Determine where we're moving over.
			PiecePosition originalLocation = getPiecePositionAt(piecePanel.getOriginalLocation());
			
			//Set the highlight.
			if(engine.isValidMove(piecePanel.getData(), originalLocation, dragOverPosition))
			{
				highlight.setColor(HighlightPanel.BLUE);
				
				//get coords of upper-left corner of this square:
				Point coords = boardPanel.identifyCoordinates(dragOverPosition);
				
				//place our highlighter over this square:
				highlight.setLocation(coords.x + 248, coords.y + 120);
			}
			else
			{
				highlight.setColor(HighlightPanel.OFF);
			}
		}
		else
		{
			highlight.setColor(HighlightPanel.OFF);
		}
	}
	
	/**
	 * Notifies the gui as a dragged piece is dropped.  The gui will check with
	 * the game engine to see if this is a valid piece placement, and updates the
	 * piece location if so.
	 *
	 * @param piecePanel 	The piece that is being dropped.
	 * @param holder		The holder where this piece originated from.
	 * @param dropLocation	The (x,y) coordinates of the piece center.
	 */
	public void droppedPiece(PiecePanel piecePanel, PieceHolder holder,
			Point dropLocation)
	{
		//Turn off highlighting.
		highlight.setColor(HighlightPanel.OFF);
		
		//Determine where we're going to
		PiecePosition dropPosition = getPiecePositionAt(dropLocation);
		
		//Determine where we're coming from.
		PiecePosition originalPosition = getPiecePositionAt(piecePanel.getOriginalLocation());
		
		//Now see if it makes sense to move this piece.
		if(engine.isValidMove(piecePanel.getData(), originalPosition, dropPosition))
		{
			engine.movePiece(piecePanel.getData(), originalPosition, dropPosition);
			
			//Remove the piece from its previous location
			holder.removePiece(piecePanel);
			
			//Place the piece in its new location;
			if(dropPosition.isOnBoard())
			{
				boardPanel.placePiece(piecePanel, dropPosition);
			}
			else if(dropPosition.getBucketColor() == GameConstants.GOLD)
			{
				goldBucketPanel.dropPiece(piecePanel);
			}
			else
			{
				silverBucketPanel.dropPiece(piecePanel);
			}
		}
		else
		{
			piecePanel.resetPosition();
		}
	}

	/**
	 * TODO: Describe method
	 *
	 * @param dropLocation
	 * @return
	 */
	private PiecePosition getPiecePositionAt(Point location)
	{
		PieceHolder dropPanel = getHolderAt(location.x, location.y); 
		if(dropPanel == boardPanel)
		{	
			//Determine what square we dropped this on:
			int relativeDropX = location.x - boardPanel.getX();
			int relativeDropY = location.y - boardPanel.getY();
			return boardPanel.identifyBoardPosition(relativeDropX, relativeDropY);
		}
		else if(dropPanel == goldBucketPanel)
		{
			return PiecePosition.GOLD_BUCKET;
		}
		else if(dropPanel == silverBucketPanel)
		{
			return PiecePosition.SILVER_BUCKET;
		}
		return null;
	}

	/**
	 * TODO: Describe method
	 *
	 * @param b
	 */
	public void setEndofTurn(boolean b)
	{
		turnPanel.showEndTurnButton(b);
	}

	/**
	 * TODO: Describe method
	 *
	 */
	public void endTurn()
	{
		engine.endTurn();
	}

	/**
	 * TODO: Describe method
	 *
	 * @param playerTurn
	 */
	public void setTurnState(int playerTurn)
	{
		turnPanel.setTurnState(playerTurn);
	}
}
