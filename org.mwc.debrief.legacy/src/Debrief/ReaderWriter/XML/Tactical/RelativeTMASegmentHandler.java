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
package Debrief.ReaderWriter.XML.Tactical;

/**
 * Title:        Debrief 2000
 * Description:  Debrief 2000 Track Analysis Software
 * Copyright:    Copyright (c) 2000
 * Company:      MWC
 * @author Ian Mayo
 * @version 1.0
 */

import org.w3c.dom.Element;

import Debrief.Wrappers.Track.RelativeTMASegment;
import Debrief.Wrappers.Track.TrackSegment;
import MWC.GUI.Layers;
import MWC.GenericData.WatchableList;
import MWC.GenericData.WorldVector;
import MWC.Utilities.ReaderWriter.XML.Util.WorldVectorHandler;

abstract public class RelativeTMASegmentHandler extends CoreTMASegmentHandler
{
	private static final String TMA_SEGMENT = "RelativeTMASegment";
	public static final String HOST = "HostTrack";
	public static final String OFFSET = "Offset";

	protected String _host = null;
	protected WorldVector _offset = null;
	private final Layers _theLayers;

	public RelativeTMASegmentHandler(final Layers theLayers)
	{
		// inform our parent what type of class we are
		super(TMA_SEGMENT);

		_theLayers = theLayers;

		addAttributeHandler(new HandleAttribute(HOST)
		{
			@Override
			public void setValue(final String name, final String val)
			{
				_host = val;
			}
		});

		addHandler(new WorldVectorHandler(OFFSET)
		{

			@Override
			public void setWorldVector(final WorldVector res)
			{
				_offset = res;
			}
		});

	}

	@Override
	protected TrackSegment createTrack()
	{
		if(_offset == null)
		{
			// duff data file, declare it
			throw new RuntimeException("Offset data missing for TMA segment on " + _host);
		}
		
		final RelativeTMASegment res = new RelativeTMASegment(_courseDegs, _speed,
				_offset, _theLayers);
		res.setBaseFrequency(_baseFrequency);
		res.setHostName(_host);
		return res;
	}

	public static void exportThisTMASegment(final org.w3c.dom.Document doc,
			final Element trk, final RelativeTMASegment seg)
	{

		final Element segE = CoreTrackSegmentHandler.exportThisSegment(doc, seg,
				TMA_SEGMENT);

		// export the start bits
		CoreTMASegmentHandler.exportThisTMASegment(doc, seg, segE);

		// sort out the remaining attributes
		final WatchableList refTrack = seg.getReferenceTrack();
		if (refTrack != null)
		{
			segE.setAttribute(HOST, refTrack.getName());
		}

		// and the offset vector
		final WorldVector theOffset = seg.getOffset();
		
		// now we must have an offset. Throw a wobbly if we don't
		WorldVectorHandler.exportVector(OFFSET, theOffset, segE, doc);

		trk.appendChild(segE);

	}

}