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
package ASSET.Models.Movement;

import MWC.GenericData.WorldAcceleration;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldSpeed;

/**
 * Created by IntelliJ IDEA.
 * User: Ian.Mayo
 * Date: 13-Aug-2003
 * Time: 15:14:31
 * To change this template use Options | File Templates.
 */
public class SurfaceMovementCharacteristics extends MovementCharacteristics
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
   * turning circle
   */
  protected WorldDistance _turningCircle;


  /**
   */
  public static SurfaceMovementCharacteristics generateDebug(String myName, double accelRate,
                                        double decelRate, double fuel_usage_rate,
                                        double maxSpeed, double minSpeed, double turnCircle)
  {
    return new SurfaceMovementCharacteristics(myName, new WorldAcceleration(accelRate, WorldAcceleration.M_sec_sec),
         new WorldAcceleration(decelRate, WorldAcceleration.M_sec_sec),
         fuel_usage_rate, new WorldSpeed(maxSpeed, WorldSpeed.M_sec),
         new WorldSpeed(minSpeed, WorldSpeed.M_sec), new WorldDistance(turnCircle, WorldDistance.METRES));
  }


  public SurfaceMovementCharacteristics(String myName, WorldAcceleration accelRate,
                                        WorldAcceleration decelRate, double fuel_usage_rate,
                                        WorldSpeed maxSpeed, WorldSpeed minSpeed, WorldDistance turnCircle)
  {
    super(myName, accelRate, decelRate,
          fuel_usage_rate, maxSpeed, minSpeed);
    _turningCircle = turnCircle;
  }


  //////////////////////////////////////////////////
  // member methods
  //////////////////////////////////////////////////

  /**
   * get the turning circle diameter (m) at this speed (in m/sec)
   */
  public double getTurningCircleDiameter(double m_sec)
  {
    return _turningCircle.getValueIn(WorldDistance.METRES);
  }


  public static MovementCharacteristics getSampleChars()
  {
    MovementCharacteristics chars = new SurfaceMovementCharacteristics("sample",
                                                                       new WorldAcceleration(4, WorldAcceleration.Kts_sec),
                                                                       new WorldAcceleration(3, WorldAcceleration.Kts_sec),
                                                                       1,
                                                                       new WorldSpeed(20, WorldSpeed.M_sec),
                                                                       new WorldSpeed(0, WorldSpeed.Kts),
                                                                       new WorldDistance(400, WorldDistance.METRES));
    return chars;
  }
}
