package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MuniPathsOverlay extends Overlay {
	public List<RouteDetail> mRoutes;

	public MuniPathsOverlay() {
		mRoutes = new ArrayList<RouteDetail>();
	}

	public void addRoute(RouteDetail route) {
		mRoutes.add(route);
	}
	
	public void clearRoutes() {
		mRoutes.clear();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			// TODO: draw shadow if needed.
			return;
		}
		
		for (RouteDetail route : mRoutes) {
			drawPath(canvas, mapView, route);
		}
	}
	
	/**
	 * Draw the path for a route.
	 * @param canvas
	 * @param route
	 */
	private void drawPath(Canvas canvas, MapView mapView, RouteDetail route) {
		Projection mapProjection = mapView.getProjection();
		for (Path path : route.mPaths) {
			android.graphics.Path pathLine = new android.graphics.Path();
			boolean pathStarted = false;
			android.graphics.Point canvasPoint = new android.graphics.Point();
			for (Point point : path.mPoints) {
				GeoPoint geoPoint = new GeoPoint((int) (point.mLat * 1e6), (int) (point.mLon * 1e6)); 
				mapProjection.toPixels(geoPoint, canvasPoint);
				if (!pathStarted) {
					pathLine.moveTo(canvasPoint.x, canvasPoint.y);
					pathStarted = true;
				} else {
					pathLine.lineTo(canvasPoint.x, canvasPoint.y);
				}
			}
			Paint paint = new Paint();
			paint.setColor(0xFFFF0000);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(3);
			paint.setStrokeCap(Cap.ROUND);
			paint.setAntiAlias(true);
			canvas.drawPath(pathLine, paint);
		}
	}
}
