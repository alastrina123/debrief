/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package MWC.GUI.Tools.Operations;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import MWC.GUI.CanvasType;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Plottable;
import MWC.GUI.PlottableSelection;
import MWC.GUI.Tools.Action;
import MWC.GUI.Tools.Chart.RightClickEdit;

public class RightClickPasteAdaptor implements RightClickEdit.PlottableMenuCreator
{
  ///////////////////////////////////
  // member variables
  //////////////////////////////////
  private static Clipboard _clipboard;


  ///////////////////////////////////
  // constructor
  //////////////////////////////////
  public RightClickPasteAdaptor(final Clipboard clipboard)
  {
    _clipboard = clipboard;
  }

  public RightClickPasteAdaptor()
  {
  }



  ///////////////////////////////////
  // member functions
  //////////////////////////////////


  ///////////////////////////////////
  // nested classes
  //////////////////////////////////
  public void createMenu(final javax.swing.JPopupMenu menu,
                         final Editable destination,
                         final java.awt.Point thePoint,
                         final CanvasType theCanvas,
                         final MWC.GUI.Properties.PropertiesPanel thePanel,
                         final Layer theParent,
                         final Layers theLayers, final Layer updateLayer)
  {
    // is the plottable a layer
    if ((destination instanceof MWC.GUI.Layer) || (destination == null))
    {

      final Transferable tr = _clipboard.getContents(this);
      // see if there is currently a plottable on the clipboard
      if (tr != null)
      {
        if (tr.isDataFlavorSupported(PlottableSelection.PlottableFlavor))
        {
          // we're off!

          try
          {

            // extract the plottable
            final Plottable theData = (Plottable) tr.getTransferData(PlottableSelection.PlottableFlavor);

            PasteItem paster = null;

            if (tr instanceof PlottableSelection)
            {
              final PlottableSelection ps = (PlottableSelection) tr;

              final boolean isCopy = ps.isACopy();

              // see if it is a layer or not
              if (theData instanceof MWC.GUI.Layer)
              {

                final MWC.GUI.Layer clipLayer = (MWC.GUI.Layer) theData;

                // create the menu items
                paster = new PasteLayer(clipLayer,
                                        _clipboard,
                                        (Layer) destination,
                                        theCanvas,
                                        theLayers,
                                        isCopy);
              }
              else
              {
                // just check that there isn't a null destination
                if (destination != null)
                {
                  // create the menu items
                  paster = new PasteItem(theData,
                                         _clipboard,
                                         (Layer) destination,
                                         theCanvas,
                                         theLayers,
                                         isCopy);
                }
              }


              if (paster != null)
              {
                // add to the menu
                menu.addSeparator();
                menu.add(paster);
              }

            }
            else
            {
              System.err.println("WRONG TYPE OF PLOTTABLE");
            }
          }
          catch (final Exception e)
          {
            MWC.Utilities.Errors.Trace.trace(e);
          }
        }
      }

    }
  }


  public class PasteItem extends javax.swing.JMenuItem implements Action, ActionListener, ClipboardOwner
  {
    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Plottable _data;
    Clipboard _myClipboard;
    Layer _theDestination;
    CanvasType _theCanvas;
    Layers _theLayers;
    boolean _isACopy;

    public PasteItem(final Plottable data,
                     final Clipboard clipboard,
                     final Layer theDestination,
                     final CanvasType theCanvas,
                     final Layers theLayers,
                     final boolean isACopy)
    {
      // formatting
      super.setText("Paste " + data.getName());

      // remember stuff
      // try to take a fresh clone of the data item
      _data = cloneThis(data);
      _myClipboard = clipboard;
      _theDestination = theDestination;
      _theCanvas = theCanvas;
      _theLayers = theLayers;
      _isACopy = isACopy;

      // and process event
      this.addActionListener(this);
    }

    public boolean isUndoable()
    {
      return true;
    }

    public boolean isRedoable()
    {
      return true;
    }

    public String toString()
    {
      return "Paste " + _data.getName();
    }

    public void undo()
    {
      // remove the item from it's new parent
      _theDestination.removeElement(_data);

      _theLayers.fireModified((Layer) _data);
    }

    public void execute()
    {

      // paste the new data in it's Destination
      _theDestination.add(_data);

      if (!_isACopy)
      {
        // clear the clipboard
        // No, let's not bother, so that we can make multiple copies
        //		_myClipboard.setContents(null, null);
      }

      // inform the listeners
      _theLayers.fireExtended();

      // and update the chart
      _theCanvas.updateMe();
    }

    public void actionPerformed(final ActionEvent p1)
    {
      // do it
      execute();
    }

    public void lostOwnership(final Clipboard p1, final Transferable p2)
    {
      // don't bother
    }
  }

  //////////////////////////////////////////////
  //	clone items, using "Serializable" interface
  /////////////////////////////////////////////////
  static public Plottable cloneThis(final Plottable item)
  {
    Plottable res = null;
    try
    {
      final java.io.ByteArrayOutputStream bas = new ByteArrayOutputStream();
      final java.io.ObjectOutputStream oos = new ObjectOutputStream(bas);
      oos.writeObject(item);
      // get closure
      oos.close();
      bas.close();

      // now get the item
      final byte[] bt = bas.toByteArray();

      // and read it back in as a new item
      final java.io.ByteArrayInputStream bis = new ByteArrayInputStream(bt);

      // create the reader
      final java.io.ObjectInputStream iis = new ObjectInputStream(bis);

      // and read it in
      final Object oj = iis.readObject();

      // get more closure
      bis.close();
      iis.close();

      if (oj instanceof Plottable)
      {
        res = (Plottable) oj;
      }
    }
    catch (final Exception e)
    {
      MWC.Utilities.Errors.Trace.trace(e);
    }
    return res;
  }


  public class PasteLayer extends PasteItem
  {

    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PasteLayer(final Layer data,
                      final Clipboard clipboard,
                      final Layer theDestination,
                      final CanvasType theCanvas,
                      final Layers theLayers,
                      final boolean isACopy)
    {
      super(data, clipboard, theDestination, theCanvas, theLayers, isACopy);
    }

    public String toString()
    {
      return "Paste Layer:" + _data.getName();
    }

    public void undo()
    {
      // remove the item from it's new parent
      // do we have a destination layer?
      if (super._theDestination != null)
      {
        // add it to this layer
        _theDestination.removeElement(_data);
        _theLayers.fireModified(_theDestination);
      }
      else
      {
        // just remove it from the top level
        _theLayers.removeThisLayer((Layer) _data);
        _theLayers.fireModified((Layer) _data);
      }

    }

    public void execute()
    {
      // do we have a destination layer?
      if (super._theDestination != null)
      // add it to this layer
        _theDestination.add(_data);
      else
      {
        // see if there is already a track of this name at the top level
        if (_theLayers.findLayer(_data.getName()) == null)
        {
          // just add it
          _theLayers.addThisLayerDoNotResize((Layer) _data);
        }
        else
        {
          // adjust the name
          final Layer newLayer = (Layer) _data;

          final String theName = newLayer.getName();

          // does the layer end in a digit?
          final char id = theName.charAt(theName.length() - 1);
          final String idStr = new String("" + id);
          int val = 1;

          String newName = null;
          try
          {
            val = Integer.parseInt(idStr);
            newName = theName.substring(0, theName.length() - 2) + " " + val;

            while (_theLayers.findLayer(newName) != null)
            {
              val++;
              newName = theName.substring(0, theName.length() - 2) + " " + val;
            }
          }
          catch (final java.lang.NumberFormatException f)
          {
            newName = theName + " " + val;
            while (_theLayers.findLayer(newName) != null)
            {
              val++;
              newName = theName + " " + val;
            }
          }

          // ignore, there isn't a number, just add a 1
          newLayer.setName(newName);

          // just drop it in at the top level
          _theLayers.addThisLayerDoNotResize((Layer) _data);
        }
      }

      if (!_isACopy)
      {
        // clear the clipboard
        _myClipboard.setContents(null, null);
      }

      _theLayers.fireModified(null);

      // and update the chart
      _theCanvas.updateMe();
    }

  }

}
