package com.gwu.munimaps;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MuniRoutesOverlay extends Overlay {
	private RouteData mRouteData;
	private MuniMapsPrefs mPrefs;

	public MuniRoutesOverlay(RouteData routeData, MuniMapsPrefs prefs) {
		mRouteData = routeData;
		mPrefs = prefs;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			// TODO: draw shadow if needed.
			return;
		}
		
		for (String routeTag : mPrefs.getSelectedRouteTags()) {
			if (mRouteData.isRouteInfoCached(routeTag)) {
				RouteInfo routeInfo = mRouteData.getRouteInfo(routeTag);
				drawPath(canvas, mapView, routeInfo);
			}
		}
	}
	
	/**
	 * Draw the path for a route.
	 * @param canvas
	 * @param route
	 */
	private void drawPath(Canvas canvas, MapView mapView, RouteInfo route) {
		Projection mapProjection = mapView.getProjection();
		for (Path path : route.getPaths()) {
			Log.i("Drawing path", path.toString());
			android.graphics.Path pathLine = new android.graphics.Path();
			boolean pathStarted = false;
			android.graphics.Point canvasPoint = new android.graphics.Point();
			for (Point point : path.getPoints()) {
				GeoPoint geoPoint = new GeoPoint((int) (point.mLat * 1e6), (int) (point.mLon * 1e6));
				mapProjection.toPixels(geoPoint, canvasPoint);
				if (!pathStarted) {
					pathLine.moveTo(canvasPoint.x, canvasPoint.y);
					pathStarted = true;
				} else {
					pathLine.lineTo(canvasPoint.x, canvasPoint.y);
				}
			}
			
			int lineColor = 0xFF0000;
			try {
				lineColor = Integer.parseInt(route.getLineColor(), 16);
			} catch (NumberFormatException e) {
				// Oh well, we'll use the default color.
			}
			// Full opacity on the line color.
			lineColor |= 0xFF000000;
			
			Paint paint = new Paint();
			paint.setColor(lineColor);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(3);
			paint.setStrokeCap(Cap.ROUND);
			paint.setAntiAlias(true);
			canvas.drawPath(pathLine, paint);
		}
	}
}
