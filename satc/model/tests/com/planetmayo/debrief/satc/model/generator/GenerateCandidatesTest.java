package com.planetmayo.debrief.satc.model.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.planetmayo.debrief.satc.model.ModelTestBase;
import com.planetmayo.debrief.satc.model.contributions.BearingMeasurementContribution;
import com.planetmayo.debrief.satc.model.contributions.CourseForecastContribution;
import com.planetmayo.debrief.satc.model.states.BaseRange.IncompatibleStateException;
import com.planetmayo.debrief.satc.model.states.BoundedState;
import com.planetmayo.debrief.satc.model.states.LocationRange;
import com.planetmayo.debrief.satc.model.states.Route;
import com.planetmayo.debrief.satc.model.states.SpeedRange;
import com.planetmayo.debrief.satc.model.states.State;
import com.planetmayo.debrief.satc.model.states.StraightLeg;
import com.planetmayo.debrief.satc.support.TestSupport;
import com.planetmayo.debrief.satc.util.GeoSupport;
import com.planetmayo.debrief.satc.util.MakeGrid;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@SuppressWarnings("deprecation")
public class GenerateCandidatesTest extends ModelTestBase
{

	private IBoundsManager boundsManager;
	private BearingMeasurementContribution bearingMeasurementContribution;
	private CourseForecastContribution courseForecastContribution;

	@Before
	public void prepareBoundsManager()
	{
		bearingMeasurementContribution = new BearingMeasurementContribution();
		bearingMeasurementContribution.loadFrom(TestSupport.getShortData());

		courseForecastContribution = new CourseForecastContribution();
		courseForecastContribution.setStartDate(new Date(110, 0, 12, 12, 15, 0));
		courseForecastContribution.setFinishDate(new Date(110, 0, 12, 12, 20, 0));
		courseForecastContribution.setMinCourse(50d);
		courseForecastContribution.setMaxCourse(100d);

		boundsManager = new BoundsManager();
		boundsManager.addContribution(bearingMeasurementContribution);
		boundsManager.addContribution(courseForecastContribution);
	}

	@Test
	public void testGridding() throws ParseException
	{

		WKTReader wkt = new WKTReader();
		Geometry geom = wkt
				.read("POLYGON ((0.0 3.0, 2.0 4.0, 4.0 4.0, 2.0 3.0, 0.0 3.0))");

		// how many points?
		final int num = 100;

		// ok, try the tesselate function
		long start = System.currentTimeMillis();
		ArrayList<Point> pts = MakeGrid.ST_Tile(geom, num, 6);
		System.out.println("elapsed:" + (System.currentTimeMillis() - start));
		assertNotNull("something returned", pts);
		assertEquals("correct num", 98, pts.size());
		Iterator<Point> iter = pts.iterator();
		while (iter.hasNext())
		{
			Point po = iter.next();
			// check the point is in the area
			assertEquals("point is in area", true, geom.contains(po));

			// send out for debug
			// System.out.println(po.getX() + "\t" + po.getY());
		}
	}

	@Test
	public void testRouteSegmentation1()
	{
		Date startD = new Date(2012, 5, 5, 12, 0, 0);
		Date endD = new Date(2012, 5, 5, 17, 0, 0);
		Point startP = GeoSupport.getFactory().createPoint(new Coordinate(0, 0));
		Point endP = GeoSupport.getFactory().createPoint(new Coordinate(1, 0));
		Route testR = new Route("1", startP, startD, endP, endD);

		assertNull("no states, yet", testR.getStates());

		// ok, generate some times
		ArrayList<BoundedState> theTimes = new ArrayList<BoundedState>();
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 11, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 12, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 14, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 15, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 17, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 18, 0, 0)));

		testR.generateSegments(theTimes);

		ArrayList<State> states = testR.getStates();
		assertNotNull("have some states", states);
		assertEquals("correct num states", 4, states.size());
	}

	@Test
	public void testRouteSegmentation2()
	{
		Date startD = new Date(2012, 5, 5, 12, 0, 0);
		Date endD = new Date(2012, 5, 5, 12, 0, 0);
		Point startP = GeoSupport.getFactory().createPoint(new Coordinate(0, 0));
		Point endP = GeoSupport.getFactory().createPoint(new Coordinate(1, 0));
		Route testR = new Route("1", startP, startD, endP, endD);

		assertNull("no states, yet", testR.getStates());

		// ok, generate some times
		ArrayList<BoundedState> theTimes = new ArrayList<BoundedState>();
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 11, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 12, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 14, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 15, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 17, 0, 0)));
		theTimes.add(new BoundedState(new Date(2012, 5, 5, 18, 0, 0)));

		testR.generateSegments(theTimes);

		ArrayList<State> states = testR.getStates();
		assertNotNull("have some states", states);
		assertEquals("correct num states", 1, states.size());
	}

	@Test
	public void testRouteCourseAndSpeed()
	{
		Date startD = new Date(2012, 5, 5, 12, 0, 0);
		Date endD = new Date(2012, 5, 5, 17, 0, 0);
		Point startP = GeoSupport.getFactory().createPoint(new Coordinate(0, 0));
		Point endP = GeoSupport.getFactory().createPoint(new Coordinate(1, 0));
		Route testR = new Route("1", startP, startD, endP, endD);

		assertEquals("correct course", 0, testR.getCourse(), EPS);
		assertEquals("correct speed", GeoSupport.kts2MSec(12), testR.getSpeed(),
				0.01);

		startP = GeoSupport.getFactory().createPoint(new Coordinate(0, 0));
		endP = GeoSupport.getFactory().createPoint(new Coordinate(1, 1));
		testR = new Route("1", startP, startD, endP, endD);

		assertEquals("correct course", Math.toRadians(45), testR.getCourse(), EPS);
		assertEquals("correct speed", GeoSupport.kts2MSec(16.97), testR.getSpeed(),
				0.01);

		startP = GeoSupport.getFactory().createPoint(new Coordinate(0, 0));
		endP = GeoSupport.getFactory().createPoint(new Coordinate(1, -1));
		testR = new Route("1", startP, startD, endP, endD);

		assertEquals("correct course", Math.toRadians(-45), testR.getCourse(), EPS);
		assertEquals("correct speed", GeoSupport.kts2MSec(16.97), testR.getSpeed(),
				0.01);

		startP = GeoSupport.getFactory().createPoint(new Coordinate(1, -1));
		endP = GeoSupport.getFactory().createPoint(new Coordinate(1, -1));
		testR = new Route("1", startP, startD, endP, endD);

		assertEquals("correct course", Math.toRadians(0), testR.getCourse(), EPS);
		assertEquals("correct speed", 0, testR.getSpeed(), 0.01);

	}

	@Test
	public void testMatrixStorage() throws ParseException
	{
		WKTReader wkt = new WKTReader();
		Geometry leg1Start = wkt.read("POLYGON ((0 3, 2 4, 4 4, 2 3, 0 3))");
		Geometry leg1End = wkt.read("POLYGON ((5 1, 5.5 2,6 2,6 1, 5 1))");

		ArrayList<Point> startP = MakeGrid.ST_Tile(leg1Start, 10, 6);
		ArrayList<Point> endP = MakeGrid.ST_Tile(leg1End, 10, 6);

		assertNotNull("produced start", startP);
		assertNotNull("produced end", endP);

		int startLen = startP.size();
		int endLen = endP.size();

		Route[][] leg1 = new Route[startLen][endLen];

		Date tStart = new Date(2012, 1, 1, 11, 0, 0);
		Date tEnd = new Date(2012, 1, 1, 15, 0, 0);

		int ctr = 0;
		for (int i = 0; i < startLen; i++)
		{
			for (int j = 0; j < endLen; j++)
			{
				leg1[i][j] = new Route("1", startP.get(i), tStart, endP.get(j), tEnd);
				ctr++;
			}
		}

		// check we have the correct nubmer of points
		assertEquals("correct number of points", startLen * endLen, ctr);

	}

	@Test
	public void testLegCreation() throws ParseException,
			IncompatibleStateException
	{
		ArrayList<BoundedState> sList1 = createStates(3, 36, false);
		ArrayList<BoundedState> sList2 = createStates(3, 29, true);

		StraightLeg s1 = new StraightLeg("Straight_1", sList1, 12);
		StraightLeg s2 = new StraightLeg("Straight_2", sList2, 8);

		assertNotNull("created leg", s1);

		// check we're still achievable
		assertEquals("all still achievable", 180, s1.getNumAchievable());
		assertEquals("all still achievable", 96, s2.getNumAchievable());

		// generate the routes
		// ok, check what's achievable
		s1.decideAchievableRoutes();
		s2.decideAchievableRoutes();

		// check some knocked off
		assertEquals("fewer achievable", 65, s1.getNumAchievable());
		assertEquals("fewer achievable", 26, s2.getNumAchievable());

//		writeMatrix("s1",s1.getRoutes());
//		System.out.println("==========");
//		writeMatrix("s2", s2.getRoutes());

		// now multiply them together
		int[][] leg1Arr = s1.asMatrix();
		int[][] leg2Arr = s2.asMatrix();
		int[][] legRes = s2.multiply(leg1Arr, leg2Arr);

//		writeMatrix("l1", leg1Arr);
//		writeMatrix("l2", leg2Arr);
//		writeMatrix("l res", legRes);
//		
		// double check that the answer is of the correct size
		assertEquals("correct rows", s1.getRoutes().length, legRes.length);
		assertEquals("correct rows", s2.getRoutes()[0].length, legRes[0].length);
		
		// work through the matrix multiplication style thing.

		// have a look at the achievable routes
		// RouteOperator writePossible = new RouteOperator()
		// {
		//
		// @Override
		// public void process(Route theRoute)
		// {
		// if (theRoute.isPossible())
		// {
		// Coordinate coord = theRoute.first().getLocation().getCoordinate();
		// System.out.println(coord.x + "\t" + coord.y);
		// coord = theRoute.last().getLocation().getCoordinate();
		// System.out.println(" " + coord.x + "\t" + coord.y);
		// }
		// }
		// };
		// sl.applyToRoutes(writePossible);
		//
		// System.out.println("=====================");
		//
		// RouteOperator writeimPossible = new RouteOperator()
		// {
		//
		// @Override
		// public void process(Route theRoute)
		// {
		// if (!theRoute.isPossible())
		// {
		// Coordinate coord = theRoute.first().getLocation().getCoordinate();
		// System.out.println(coord.x + "\t" + coord.y);
		// coord = theRoute.last().getLocation().getCoordinate();
		// System.out.println(" " + coord.x + "\t" + coord.y);
		// }
		// }
		// };
		// sl.applyToRoutes(writeimPossible);

	}
	
	@Test
	public void testMult()
	{
		int[][] m1 = new int[][]{{2,3},{1,2},{1,1}};
		int[][] m2 = new int[][]{{0,2,3},{1,2,0}};
		int[][] res = StraightLeg.multiply(m1, m2);
		writeMatrix("res", res);
	}

	private static void writeMatrix(String name, Route[][] routes)
	{
		System.out.println("== " + name + " ==");
		for (int x = 0; x < routes.length; x++)
		{
			for (int y = 0; y < routes[0].length; y++)
			{
				Route thisR = routes[x][y];
				if (thisR.isPossible())
					System.out.print("1 ");
				else
					System.out.print("0 ");

			}
			System.out.println();
		}
	}

	private static void writeMatrix(String name, int[][] routes)
	{
		System.out.println("== " + name + " ==");
		for (int x = 0; x < routes.length; x++)
		{
			for (int y = 0; y < routes[0].length; y++)
			{
				System.out.print(routes[x][y] + " ");
			}
			System.out.println();
		}
	}

	private ArrayList<BoundedState> createStates(double minS, double maxS,
			boolean reverseOrder) throws ParseException, IncompatibleStateException
	{
		Date startA = new Date(2012, 5, 5, 12, 0, 0);
		Date startB = new Date(2012, 5, 5, 14, 0, 0);
		Date startC = new Date(2012, 5, 5, 15, 0, 0);
		Date startD = new Date(2012, 5, 5, 17, 0, 0);
		BoundedState bA = new BoundedState(startA);
		BoundedState bB = new BoundedState(startB);
		BoundedState bC = new BoundedState(startC);
		BoundedState bD = new BoundedState(startD);

		// apply location bounds
		WKTReader wkt = new WKTReader();
		LocationRange locA = new LocationRange(
				wkt.read("POLYGON ((0 3, 2 4, 4 4, 2 3, 0 3))"));
		LocationRange locB = new LocationRange(
				wkt.read("POLYGON ((2.63 2.56, 3.5 3.16, 4.11 3.42, 3.33 2.3, 2.63 2.56))"));
		LocationRange locC = new LocationRange(
				wkt.read("POLYGON ((3.32 1.99,3.93 2.71,4.64 2.87,3.81 1.78, 3.32 1.99))"));
		LocationRange locD = new LocationRange(
				wkt.read("POLYGON ((5 1, 5.5 2, 6 2, 6 1, 5 1))"));

		if (!reverseOrder)
		{
			bA.constrainTo(locA);
			bB.constrainTo(locB);
			bC.constrainTo(locC);
			bD.constrainTo(locD);
		}
		else
		{
			bA.constrainTo(locD);
			bB.constrainTo(locC);
			bC.constrainTo(locB);
			bD.constrainTo(locA);

		}

		// apply speed bounds
		SpeedRange sr = new SpeedRange(minS, maxS);
		bA.constrainTo(sr);
		bD.constrainTo(sr);

		ArrayList<BoundedState> sList = new ArrayList<BoundedState>();
		sList.add(bA);
		sList.add(bB);
		sList.add(bC);
		sList.add(bD);
		return sList;
	}

}
