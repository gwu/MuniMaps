package com.gwu.munimaps;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

/**
 * Fetches NextMuni data from the web API via HTTP.  Thread-safe.
 */
public class NextMuniFetcher {
	private static final String ROUTES_URL =
		"http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=sf-muni";
	
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
}
