package org.mwc.debrief.core.editors.painters.snail;

// Copyright MWC 1999, Debrief 3 Project
// $RCSfile$
// @author $Author$
// @version $Revision$
// $Log$
// Revision 1.3  2007-03-08 12:01:04  ian.mayo
// Lots of tidying,refactoring = esp to snail & range highlighters
//
// Revision 1.2  2006/05/16 08:40:44  Ian.Mayo
// Add categories for properties
//
// Revision 1.1  2005/07/04 07:45:51  Ian.Mayo
// Initial snail implementation
//


import java.awt.*;
import java.beans.PropertyDescriptor;

import org.mwc.debrief.core.editors.painters.SnailHighlighter;
import org.mwc.debrief.core.editors.painters.SnailHighlighter.drawSWTHighLight;
import org.mwc.debrief.core.editors.painters.highlighters.*;

import Debrief.Tools.Tote.*;
import Debrief.Wrappers.*;
import MWC.GUI.*;
import MWC.GUI.Properties.*;
import MWC.GenericData.*;


public final class SnailDrawSWTFix implements drawSWTHighLight, Editable
{

	/** keep a copy of the track plotter we are using
	 */
	private final SnailDrawSWTTrack _trackPlotter = new SnailDrawSWTTrack();

	/** keep a copy of the requested point size
	 */
  private int _pointSize;

	/** do we draw in the track/vessel name?
	 */
  private boolean _plotName;

	/** the 'stretch' factor to put on the speed vector (factor to apply to the speed vector, in pixels)
	 */
  private double _vectorStretch;

  /** our editor
   */
  transient private Editable.EditorType _myEditor = null;

  /** the name we display when shown in an editor
   *  (which may initially be Snail or Relative)
   */
  private final String _myName;

  /*******************************************************
   * constructor
   ******************************************************/
  public SnailDrawSWTFix(final String name)
  {
    _myName = name;
  }

  ///////////////////////////////////
  // member functions
  //////////////////////////////////
	public final java.awt.Rectangle drawMe(final MWC.Algorithms.PlainProjection proj,
																	 final CanvasType dest,
																	 final WatchableList list,
																	 final Watchable watch,
																	 final SnailHighlighter parent,
																	 final HiResDate dtg,
                                   final java.awt.Color backColor)
	{
    Rectangle thisR = null;

 //   dest.setXORMode(backColor);

    // get a pointer to the fix
		final FixWrapper fix = (FixWrapper)watch;

		// get the colour of the track
		final Color col = fix.getColor();
		dest.setColor(col);

    // produce the centre point
    final Point p = new Point(proj.toScreen(fix.getLocation()));

    // see if we are in symbol plotting mode
    final SWTPlotHighlighter thisHighlighter = parent.getCurrentPrimaryHighlighter ();
    if(thisHighlighter instanceof SWTSymbolHighlighter)
    {
      // just plot away!
      thisHighlighter.highlightIt(proj, dest, list, watch);

      // work out the area covered
      final WorldArea wa = watch.getBounds();
      final WorldLocation tl = wa.getTopLeft();
      final WorldLocation br = wa.getBottomRight();
      final Point pTL = new Point(proj.toScreen(tl));
      final Point pBR = new Point(proj.toScreen(br));
      final Rectangle thisArea = new java.awt.Rectangle(pTL);
      thisArea.add(pBR);
      if(thisR == null)
        thisR = thisArea;
      else
        thisR.add(thisArea);

    }
    else
    {
      // plot the pointy vector thingy

      // get the current area of the watchable
      final WorldArea wa = watch.getBounds();
      // convert to screen coordinates
      final Point tl = new Point(proj.toScreen(wa.getTopLeft()));
      final Point br = new Point(proj.toScreen(wa.getBottomRight()));

      final int mySize = _pointSize;

      // get the width
      final int x = tl.x - mySize;
      final int y = tl.y - mySize;
      final int wid = (br.x - tl.x) + mySize * 2;
      final int ht = (br.y - tl.y) + mySize * 2;

      // represent this area as a rectangle
      thisR = new Rectangle(x, y, wid, ht);

      // plot the rectangle anyway
 //     dest.drawOval(x , y, wid, ht);

      // get the fix to draw itself

      // create our own canvas object (don't bother - do it all from the Track, so we know
      // the correct size of the resulting object
//      final CanvasAdaptor cad = new CanvasAdaptor(proj, dest);

      // and do the paint
   //   fix.paintMe(cad);

      // and now plot the vector
      final double crse = watch.getCourse();
      final double spd = watch.getSpeed();

      //
      final int dx = (int)(Math.sin(crse) * mySize * spd * _vectorStretch);
      final int dy = (int)(Math.cos(crse) * mySize * spd * _vectorStretch);


      // produce the end of the stick (just to establish the length in data units)
      final Point p2 = new Point(p.x + dx, p.y - dy);

      // how long is the stalk in data units?
      final WorldLocation w3 = proj.toWorld(p2);
      final double len = w3.rangeFrom(fix.getLocation());

      // now sort out the real end of this stalk
      final WorldLocation stalkEnd = fix.getLocation().add(new WorldVector(crse, len, 0));
      // and get this in screen coordinates
      final Point pStalkEnd = proj.toScreen(stalkEnd);

      // and plot the stalk itself
      dest.drawLine(p.x, p.y, pStalkEnd.x, pStalkEnd.y);

      // extend the area covered to include the stick
      thisR.add(p2);

    }

		// draw the trailing dots
		final java.awt.Rectangle dotsArea = _trackPlotter.drawMe(proj,
																											 dest,
																											 watch,
																											 parent,
																											 dtg,
                                                       backColor);

		// extend the rectangle, if necesary
		if(dotsArea != null)
			thisR.add(dotsArea);

		// plot the track name
		if(_plotName)
		{
			final String msg = fix.getTrackWrapper().getName();

			// shift the centre point across a bit
			p.translate(5, 0);

			// and draw the text
			dest.drawText(msg, p.x, p.y);

			// somehow we need to include this extended area
			final int sWid = msg.length() * 6;

			// shift from the start of the string
			p.translate(sWid, 0);

			// and add to the limits rectangle
			thisR.add(p);
		}

    // set the width
//    if(dest instanceof CanvasType)
//    {
//      CanvasType ct = (CanvasType)dest;
//      ct.setLineWidth(1);
//    }
//    if(dest instanceof Graphics2D)
//    {
//      Graphics2D g2 = (Graphics2D)dest;
//      BasicStroke bs = new BasicStroke(1);
//    }

		return thisR;
	}

	public final boolean canPlot(final Watchable wt)
	{
		boolean res = false;

		if(wt instanceof Debrief.Wrappers.FixWrapper)
		{
			res = true;
		}
		return res;
	}

  protected static final void highlightContact(final MWC.Algorithms.PlainProjection proj,
																	final Graphics dest,
																	final ContactWrapper contact,
																	final int mySize,
																	Rectangle areaCovered)
	{
		// set the highlight colour
		dest.setColor(Color.white);

		final WorldLocation start = contact.getStart();
		final WorldLocation end = contact.getEnd();

		// convert to screen coordinates
		final Point tl = new Point(proj.toScreen(start));
		final Point br = new Point(proj.toScreen(end));
		// get the width
		final int x = tl.x - mySize;
		final int y = tl.y - mySize;
		final int wid = (br.x - tl.x) + mySize * 2;
		final int ht = (br.y - tl.y) + mySize * 2;

		// represent this area as a rectangle
		final java.awt.Rectangle thisR = new Rectangle(x, y, wid, ht);

		// keep track of the area covered
		if(areaCovered == null)
			areaCovered = thisR;
		else
			areaCovered.add(thisR);

		// plot the rectangle
		dest.drawLine(tl.x+1, tl.y+1, br.x+1, br.y+1);

	}

  public final String getName()
  {
    return _myName;
  }

  public final String toString()
  {
    return getName();
  }

  public final boolean hasEditor()
  {
    return true;
  }

  public final Editable.EditorType getInfo()
  {
    if(_myEditor == null)
      _myEditor = new SnailFixPainterInfo(this);

    return _myEditor;
  }

  //////////////////////////////////////////////////////////
  // accessors for editable parameters
  /////////////////////////////////////////////////////////


  public final void setLinkPositions(final boolean val)
  {
    _trackPlotter.setJoinPositions(val);
  }

  public final boolean getLinkPositions()
  {
    return  _trackPlotter.getJoinPositions();
  }

  public final void setFadePoints(final boolean val)
  {
    _trackPlotter.setFadePoints(val);
  }

  public final boolean getFadePoints()
  {
    return _trackPlotter.getFadePoints();
  }

  /** point size of symbols (pixels)
   */
  public final BoundedInteger getPointSize()
  {
    return new BoundedInteger(_trackPlotter.getPointSize(),
															1,
															20);
  }

  /** length of trail to plot
   */
  public final Duration getTrailLength()
  {
    return new Duration(_trackPlotter.getTrailLength().longValue(), Duration.MICROSECONDS);
  }

  /** size of points to draw (pixels)
   */
  public final void setPointSize(final BoundedInteger val)
  {

    _trackPlotter.setPointSize(val.getCurrent());
		_pointSize = val.getCurrent();
  }

  /** size of points to draw (pixels) - convenience method used for XML persistence
   */
  public final void setPointSize(final int val)
  {

    _trackPlotter.setPointSize(val);
		_pointSize = val;
  }

  /** length of trail to draw
   */
  public final void setTrailLength(final Duration len)
  {
    _trackPlotter.setTrailLength(new Long((long)len.getValueIn(Duration.MICROSECONDS)));
  }

	/** whether to plot in the name of the vessel
	 */
	public final boolean getPlotTrackName()
	{
		return _plotName;
	}

	/** whether to plot in the name of the vessel
	 */
	public final void setPlotTrackName(final boolean val)
	{
		_plotName = val;
	}

	/** how much to stretch the vector
	 */
	public final void setVectorStretch(final double val)
	{
		_vectorStretch = val;
	}

	/** how much to stretch the vector
	 */
	public final double getVectorStretch()
	{
		return _vectorStretch;
	}


  //////////////////////////////////////////////////////////
  // nested editable class
  /////////////////////////////////////////////////////////

  public static final class SnailFixPainterInfo extends Editable.EditorType
  {

    public SnailFixPainterInfo(final SnailDrawSWTFix data)
    {
      super(data, "Snail Painter", "");
    }

    public final PropertyDescriptor[] getPropertyDescriptors()
    {
      try{
        final PropertyDescriptor[] res={
          prop("LinkPositions", "whether to join the points in the trail", Editable.EditorType.VISIBILITY),
          prop("PlotTrackName", "whether to plot the name of the track", Editable.EditorType.VISIBILITY),
          prop("FadePoints", "whether the trails should fade to black", Editable.EditorType.FORMAT),
          prop("PointSize", "the size of the points in the trail", Editable.EditorType.FORMAT),
          prop("TrailLength", "the length of trail to draw", Editable.EditorType.TEMPORAL),
          prop("VectorStretch", "how far to stretch the speed vector (pixels per knot)", Editable.EditorType.FORMAT),
        };

        res[5].setPropertyEditorClass(FractionPropertyEditor.class);

        return res;
      }
      catch(Exception e)
      {
        MWC.Utilities.Errors.Trace.trace(e);
        return super.getPropertyDescriptors();
      }

    }

	}

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // testing for this class
  //////////////////////////////////////////////////////////////////////////////////////////////////
  static public final class testMe extends junit.framework.TestCase
  {
    static public final String TEST_ALL_TEST_TYPE  = "UNIT";
    public testMe(final String val)
    {
      super(val);
    }
    public final void testMyParams()
    {
      Editable ed = new SnailDrawSWTFix("testing");
      Editable.editableTesterSupport.testParams(ed, this);
      ed = null;
    }
  }
}

