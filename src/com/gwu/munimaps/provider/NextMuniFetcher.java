package com.gwu.munimaps.provider;

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
import android.util.Log;
import android.util.Xml;

import com.gwu.munimaps.Direction;
import com.gwu.munimaps.Path;
import com.gwu.munimaps.Point;
import com.gwu.munimaps.RouteInfo;
import com.gwu.munimaps.RouteListing;
import com.gwu.munimaps.Stop;

/**
 * Fetches NextMuni data from the web API via HTTP.  Thread-safe.
 */
public class NextMuniFetcher {
	protected static final String ROUTES_URL =
		"http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=sf-muni";
	protected static final String ROUTE_CONFIG_URL =
		"http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni&r=%s";
	
	public NextMuniFetcher() {
	}

	public List<RouteListing> fetchRoutes() {
		InputStream inputStream;
		try {
			inputStream = openUrl(ROUTES_URL);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		final List<RouteListing> routes = new ArrayList<RouteListing>();
		RootElement rootElement = new RootElement("body");
		Element routeElement = rootElement.getChild("route");
		routeElement.setStartElementListener(new StartElementListener() {
			@Override
			public void start(Attributes attributes) {
				String tag = attributes.getValue("tag");
				String title = attributes.getValue("title");
				String shortTitle = attributes.getValue("shortTitle");
				routes.add(new RouteListing(tag, title, shortTitle));
			}
		});
        try {
            Xml.parse(inputStream, Xml.Encoding.UTF_8, rootElement.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		
        return routes;
	}
	
	public RouteInfo fetchRouteDetail(String tag) {
		Log.i("Fetching xml", tag);
		InputStream inputStream;
		try {
			inputStream = openUrl(String.format(ROUTE_CONFIG_URL, tag));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final RouteInfo routeDetail = new RouteInfo(tag);
		RootElement rootElement = new RootElement("body");
		Element routeElement = rootElement.getChild("route");
		routeElement.setStartElementListener(new StartElementListener() {
			@Override
			public void start(Attributes attributes) {
				Log.i("Color", "foo");
				routeDetail.setLineColor(attributes.getValue("color"));
				routeDetail.setTextColor(attributes.getValue("oppositeColor"));
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
		pathElement.setStartElementListener(new StartElementListener() {
			@Override
			public void start(Attributes attributes) {
				Log.i("Fetched path", "path");
			}
		});
		final Path path = new Path();
		Element pointElement = pathElement.getChild("point");
		pointElement.setStartElementListener(new StartElementListener() {
			@Override
			public void start(Attributes attributes) {
				double lat = Double.parseDouble(attributes.getValue("lat"));
				double lon = Double.parseDouble(attributes.getValue("lon"));
				Point point = new Point(lat, lon);
				Log.i("Fetched point", "point");
				path.addPoint(point);
			}
		});
		pathElement.setEndElementListener(new EndElementListener() {
			@Override
			public void end() {
				routeDetail.addPath(path.clone());
				path.getPoints().clear();
			}
		});
        try {
            Xml.parse(inputStream, Xml.Encoding.UTF_8, rootElement.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        Log.i("done fetching url", "");
        Log.i("paths found:", Integer.toString(routeDetail.getPaths().size()));
		
        return routeDetail;
	}
	
	/**
	 * Opens a URL as an input stream.
	 * @param url the URL to open.
	 * @return the input stream.
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected InputStream openUrl(String url) throws MalformedURLException, IOException {
		return new URL(url).openConnection().getInputStream();
	}
}
