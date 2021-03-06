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
package de.kupzog.ktable;

/**
 * @author Friederich Kupzog
 */
public interface KTableCellResizeListener {

	/**
	 * Is called when a row is resized. (but not when first row is resized!)
	 */
	public void rowResized(int row, int newHeight);

	/**
	 * Is called when a column is resized.
	 */
	public void columnResized(int col, int newWidth);
}
