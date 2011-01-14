package com.gwu.munimaps;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

/**
 * Fetches NextMuni data from the web API via HTTP.  Thread-safe.
 */
public class NextMuniFetcher {
	private static final String ROUTES_URL =
		"http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=sf-muni";
	private static final String ROUTE_CONFIG_URL =
		"http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni&r=%s";
	
	public NextMuniFetcher() {
	}

	public List<Route> fetchRoutes() {
		try {
			URL routesUrl = new URL(ROUTES_URL);
			try {
				InputStream inputStream = routesUrl.openConnection().getInputStream();
				
				final List<Route> routes = new ArrayList<Route>();
				RootElement rootElement = new RootElement("body");
				Element routeElement = rootElement.getChild("route");
				routeElement.setStartElementListener(new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						String tag = attributes.getValue("tag");
						String title = attributes.getValue("title");
						String shortTitle = attributes.getValue("shortTitle");
						routes.add(new Route(tag, title, shortTitle));
					}
				});
		        try {
		            Xml.parse(inputStream, Xml.Encoding.UTF_8, rootElement.getContentHandler());
		        } catch (Exception e) {
		            throw new RuntimeException(e);
		        }
				
		        return routes;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RouteDetail fetchRouteDetail(String tag) {
		try {
			URL routeUrl = new URL(String.format(ROUTE_CONFIG_URL, tag));
			try {
				InputStream inputStream = routeUrl.openConnection().getInputStream();
				
				final RouteDetail routeDetail = new RouteDetail(tag);
				RootElement rootElement = new RootElement("body");
				Element routeElement = rootElement.getChild("route");
				routeElement.setStartElementListener(new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						routeDetail.mLineColor = attributes.getValue("color");
						routeDetail.mTextColor = attributes.getValue("oppositeColor");
					}
				});
				Element stopElement = routeElement.getChild("stop");
				stopElement.setStartElementListener(new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						Stop stop = new Stop(attributes.getValue("tag"));
						stop.mTitle = attributes.getValue("title");
						stop.mLat = Double.parseDouble(attributes.getValue("lat"));
						stop.mLon = Double.parseDouble(attributes.getValue("lon"));
						routeDetail.addStop(stop);
					}
				});
				Element directionElement = routeElement.getChild("direction");
				final Direction direction = new Direction();
				directionElement.setStartElementListener(new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						direction.mTag = attributes.getValue("tag");
						direction.mTitle = attributes.getValue("title");
					}
				});
				Element directionPoint = directionElement.getChild("stop");
				directionPoint.setStartElementListener(new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						direction.addStop(attributes.getValue("tag"));
					}
				});
				directionElement.setEndElementListener(new EndElementListener() {
					@Override
					public void end() {
						routeDetail.addDirection(direction.clone());
					}
				});
				Element pathElement = routeElement.getChild("path");
				final Path path = new Path();
				Element pointElement = pathElement.getChild("point");
				pointElement.setStartElementListener(new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						double lat = Double.parseDouble(attributes.getValue("lat"));
						double lon = Double.parseDouble(attributes.getValue("lon"));
						Point point = new Point(lat, lon);
						path.addPoint(point);
					}
				});
				pathElement.setEndElementListener(new EndElementListener() {
					@Override
					public void end() {
						routeDetail.addPath(path.clone());
						path.mPoints.clear();
					}
				});
		        try {
		            Xml.parse(inputStream, Xml.Encoding.UTF_8, rootElement.getContentHandler());
		        } catch (Exception e) {
		            throw new RuntimeException(e);
		        }
				
		        return routeDetail;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
