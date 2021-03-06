package com.atakmap.map;

import android.graphics.PointF;

import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoCalculations;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.projection.Projection;
import com.atakmap.math.MathUtils;
import com.atakmap.math.Plane;
import com.atakmap.math.PointD;
import com.atakmap.math.Vector3D;

public final class CameraController {
    private final static String TAG = "CameraController";

    /**
     * Pan the map a given number of pixels.  The map will not pan past the
     * extremities defined by AtakMapView.
     *
     * @param x         Horizontal pixels to pan
     * @param y         Vertical pixels to pan
     * @param animate   Pan smoothly if true; immediately if false
     */
    public static void panBy(MapRenderer2 renderer, float x, float y, boolean animate) {
        final RenderSurface surface = renderer.getRenderContext().getRenderSurface();
        final MapSceneModel currentModel = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);

        GeoPoint mapCenter = GeoPoint.createMutable();
        currentModel.mapProjection.inverse(currentModel.camera.target, mapCenter);
        final float focusPoint_x = surface.getWidth() / 2 + renderer.getFocusPointOffsetX();
        final float focusPoint_y = surface.getHeight() / 2 + renderer.getFocusPointOffsetY();

        GeoPoint tgt = GeoPoint.createMutable();
        currentModel.inverse (new PointF(focusPoint_x + x, focusPoint_y + y), tgt);

        if(true) { // XXX - continuous scroll enabled
            if(tgt.getLongitude() < -180d)
                tgt.set(tgt.getLatitude(), tgt.getLongitude()+360d, tgt.getAltitude(), tgt.getAltitudeReference(), tgt.getCE(), tgt.getLE());
            else if(tgt.getLongitude() > 180d)
                tgt.set(tgt.getLatitude(), tgt.getLongitude()-360d, tgt.getAltitude(), tgt.getAltitudeReference(), tgt.getCE(), tgt.getLE());
        }

        if (!tgt.isValid())
            return;

        renderer.lookAt(tgt,
                currentModel.gsd,
                currentModel.camera.azimuth,
                90d+currentModel.camera.elevation,
                animate);
    }

    public static void panTo(MapRenderer2 renderer, GeoPoint focus, boolean animate) {
        final MapSceneModel sm = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        panToImpl(renderer, sm, focus, sm.focusx, sm.focusy, animate);
    }
    public static void panTo(MapRenderer2 renderer, GeoPoint focus, float x, float y, boolean animate) {
        final MapSceneModel sm = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        panToImpl(renderer, sm, focus, x, y, animate);
    }

    private static void panToImpl(MapRenderer2 renderer, MapSceneModel sm, GeoPoint focus, float x, float y, boolean animate) {
        if(!sm.camera.perspective) {
            PointF xy = sm.forward(focus, (PointF)null);
            if(xy == null)
                return;
            panBy(renderer, xy.x-sm.focusx, xy.y-sm.focusy, animate);
            panBy(renderer, sm.focusx-x, sm.focusy-y, animate);
            return;
        }

        if(Double.isNaN(focus.getAltitude()))
            focus = new GeoPoint(focus.getLatitude(), focus.getLongitude(), 0d);

        // create plane at press location
        PointD startProj = sm.mapProjection.forward(focus, null);
        PointD startProjUp = sm.mapProjection.forward(new GeoPoint(focus.getLatitude(), focus.getLongitude(), focus.getAltitude()+100d), null);

        // compute the normal at the start point
        Vector3D startNormal = new Vector3D(startProjUp.x-startProj.x, startProjUp.y-startProj.y, startProjUp.z-startProj.z);
        startNormal.X *= sm.displayModel.projectionXToNominalMeters;
        startNormal.Y *= sm.displayModel.projectionYToNominalMeters;
        startNormal.Z *= sm.displayModel.projectionZToNominalMeters;
        final double startNormalLen = MathUtils.distance(startNormal.X, startNormal.Y, startNormal.Z, 0d, 0d, 0d);
        startNormal.X /= startNormalLen;
        startNormal.Y /= startNormalLen;
        startNormal.Z /= startNormalLen;

        final Plane panPlane = new Plane(startNormal, startProj);

        // intersect the end point with the plane to determine the translation vector in WCS
        GeoPoint endgeo = GeoPoint.createMutable();
        if (sm.inverse(new PointF(x, y), endgeo, panPlane) == null)
            return;

        PointD endWCS = new PointD(0d, 0d, 0d);
        sm.mapProjection.forward(endgeo, endWCS);

        // compute translation of focus point on model surface to end point on plane
        final double tx = endWCS.x - sm.camera.target.x;
        final double ty = endWCS.y - sm.camera.target.y;
        final double tz = endWCS.z - sm.camera.target.z;

        // new focus is the desired location minus the translation
        GeoPoint newFocus = sm.mapProjection.inverse(new PointD(startProj.x - tx, startProj.y - ty, startProj.z - tz), null);
        renderer.lookAt(newFocus, sm.gsd, sm.camera.azimuth, 90d + sm.camera.elevation, false);
    }

    /**
     * Sets the map to the specified rotation (animate)
     *
     * @param rotation  The new rotation of the map
     * @param animate   Rotate smoothly if true; immediately if false
     */
    public static void rotateTo (MapRenderer2 renderer,
                                 double rotation,
                                 boolean animate) {

        final MapSceneModel sm = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        renderer.lookAt(sm.mapProjection.inverse(sm.camera.target, null), sm.gsd, rotation, 90d+sm.camera.elevation, animate);
    }


    public static void zoomBy(MapRenderer2 renderer, double scaleFactor, float focusx, float focusy, boolean animate) {
        final RenderSurface surface = renderer.getRenderContext().getRenderSurface();
        final MapSceneModel model = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        double newScale = Globe.getMapScale(surface.getDpi(), model.gsd) * scaleFactor;

        // Don't zoom to NaN
        if (Double.isNaN (newScale))
            return;

        updateBy(renderer,
                surface,
                model,
                newScale,
                model.camera.azimuth,
                90d+model.camera.elevation,
                focusx, focusy,
                animate);

    }

    public static void tiltTo(MapRenderer2 renderer,
                              double tilt,
                              boolean animate) {
        final MapSceneModel currentModel = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        renderer.lookAt (
                currentModel.mapProjection.inverse(currentModel.camera.target, null),
                currentModel.gsd,
                currentModel.camera.azimuth,
                tilt,
                animate);
    }

    public static void rotateBy(MapRenderer2 renderer,
                                double theta,
                                GeoPoint focus,
                                boolean animate) {

        final MapSceneModel currentModel = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        rotateToImpl(renderer, currentModel, currentModel.camera.azimuth + theta, focus, animate);
    }

    public static void rotateTo(MapRenderer2 renderer,
                                double theta,
                                GeoPoint focus,
                                boolean animate) {

        final MapSceneModel currentModel = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);

        rotateToImpl(renderer, currentModel, theta, focus, animate);
    }

    private static void rotateToImpl(MapRenderer2 renderer,
                                     MapSceneModel currentModel,
                                     double theta,
                                     GeoPoint focus,
                                     boolean animate) {

        final RenderSurface surface = renderer.getRenderContext().getRenderSurface();
        double newRot = theta;

        // Don't zoom to NaN
        if (Double.isNaN (newRot))
            return;

        GeoPoint mapCenter = GeoPoint.createMutable();
        currentModel.mapProjection.inverse(currentModel.camera.target, mapCenter);
        final float focusPoint_x = surface.getWidth() / 2 + renderer.getFocusPointOffsetX();
        final float focusPoint_y = surface.getHeight() / 2 + renderer.getFocusPointOffsetY();

        MapSceneModel nadirSurface = new MapSceneModel(surface.getDpi(),
                surface.getWidth(), surface.getHeight(),
                currentModel.mapProjection,
                new GeoPoint(focus.getLatitude(), focus.getLongitude()),
                focusPoint_x, focusPoint_y,
                0d,
                0d,
                currentModel.gsd,
                true);

        // compute cam->tgt range
        double rangeSurface = MathUtils.distance(
                nadirSurface.camera.location.x, nadirSurface.camera.location.y, nadirSurface.camera.location.z,
                nadirSurface.camera.target.x, nadirSurface.camera.target.y, nadirSurface.camera.target.z
        );


        PointD point2Proj = nadirSurface.mapProjection.forward(focus, null);

        double rangeElevated = MathUtils.distance(
                nadirSurface.camera.location.x, nadirSurface.camera.location.y, nadirSurface.camera.location.z,
                point2Proj.x, point2Proj.y, point2Proj.z
        );

        // scale resolution by cam->'point' distance
        final double resolutionAtElevated = currentModel.gsd * (rangeSurface / rangeElevated);

        PointD focusBy = new PointD(0d, 0d, 0d);
        currentModel.forward(focus, focusBy);

        // construct model to 'point' at altitude with scaled resolution with rotate/tilt
        MapSceneModel scene = new MapSceneModel(surface.getDpi(),
                surface.getWidth(),
                surface.getHeight(),
                currentModel.mapProjection,
                focus,
                focusPoint_x,
                focusPoint_y,
                newRot,
                90d+currentModel.camera.elevation,
                resolutionAtElevated,
                true
        );

        // obtain new center
        GeoPoint focusGeo2 =  scene.inverse(new PointF(focusPoint_x, focusPoint_y), null);
        if(focusGeo2 == null) {
            Log.w(TAG, "Unable to compute new rotation center: focus=" + focus + " new rotation=" + newRot);
            return;
        }

        renderer.lookAt(focusGeo2, currentModel.gsd, newRot, 90d+currentModel.camera.elevation, animate);
    }

    public static void tiltBy(MapRenderer2 renderer,
                              double theta,
                              GeoPoint focus,
                              boolean animate) {

        final MapSceneModel currentModel = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);
        tiltToImpl(renderer, currentModel, 90d+currentModel.camera.elevation + theta, focus, animate);
    }

    public static void tiltTo(MapRenderer2 renderer,
                              double theta,
                              GeoPoint focus,
                              boolean animate) {

        final MapSceneModel currentModel = renderer.getMapSceneModel(false, MapRenderer2.DisplayOrigin.UpperLeft);

        tiltToImpl(renderer, currentModel, theta, focus, animate);
    }

    private static void tiltToImpl(MapRenderer2 renderer,
                                   MapSceneModel currentModel,
                                   double newTilt,
                                   GeoPoint focus,
                                   boolean animate) {

        newTilt = MathUtils.clamp(newTilt, 0, 85);

        final RenderSurface surface = renderer.getRenderContext().getRenderSurface();

        // Don't zoom to NaN
        if (Double.isNaN (newTilt))
            return;

        GeoPoint mapCenter = GeoPoint.createMutable();
        currentModel.mapProjection.inverse(currentModel.camera.target, mapCenter);
        final float focusPoint_x = surface.getWidth() / 2 + renderer.getFocusPointOffsetX();
        final float focusPoint_y = surface.getHeight() / 2 + renderer.getFocusPointOffsetY();

        MapSceneModel nadirSurface = new MapSceneModel(surface.getDpi(),
                surface.getWidth(), surface.getHeight(),
                currentModel.mapProjection,
                new GeoPoint(focus.getLatitude(), focus.getLongitude()),
                focusPoint_x, focusPoint_y,
                0d,
                0d,
                currentModel.gsd,
                true);

        // compute cam->tgt range
        double rangeSurface = MathUtils.distance(
                nadirSurface.camera.location.x, nadirSurface.camera.location.y, nadirSurface.camera.location.z,
                nadirSurface.camera.target.x, nadirSurface.camera.target.y, nadirSurface.camera.target.z
        );


        PointD point2Proj = nadirSurface.mapProjection.forward(focus, null);

        double rangeElevated = MathUtils.distance(
                nadirSurface.camera.location.x, nadirSurface.camera.location.y, nadirSurface.camera.location.z,
                point2Proj.x, point2Proj.y, point2Proj.z
        );

        // scale resolution by cam->'point' distance
        final double resolutionAtElevated = currentModel.gsd * (rangeSurface / rangeElevated);

        PointD focusBy = new PointD(0d, 0d, 0d);
        currentModel.forward(focus, focusBy);

        // construct model to 'point' at altitude with scaled resolution with rotate/tilt
        MapSceneModel scene = new MapSceneModel(surface.getDpi(),
                surface.getWidth(),
                surface.getHeight(),
                currentModel.mapProjection,
                focus,
                focusPoint_x,
                focusPoint_y,
                currentModel.camera.azimuth,
                newTilt,
                resolutionAtElevated,
                true
        );

        // obtain new center
        GeoPoint focusGeo2 =  scene.inverse(new PointF(focusPoint_x, focusPoint_y), null);
        if(focusGeo2 == null) {
            Log.w(TAG, "Unable to compute new tilt center: focus=" + focus + " new tilt=" + newTilt);
            return;
        }

        renderer.lookAt(focusGeo2, currentModel.gsd, currentModel.camera.azimuth, newTilt, animate);
    }


    private static void updateBy(MapRenderer2 renderer,
                                 RenderSurface surface,
                                 MapSceneModel currentModel,
                                 double scale,
                                 double rotation,
                                 double tilt,
                                 float xpos,
                                 float ypos,
                                 boolean animate) {

        GeoPoint mapCenter = GeoPoint.createMutable();
        currentModel.mapProjection.inverse(currentModel.camera.target, mapCenter);
        final float focusPoint_x = surface.getWidth() / 2 + renderer.getFocusPointOffsetX();
        final float focusPoint_y = surface.getHeight() / 2 + renderer.getFocusPointOffsetY();

        final Projection proj = currentModel.mapProjection;

        MapSceneModel sm = new MapSceneModel(
                surface.getDpi(),
                renderer.getRenderContext().getRenderSurface().getWidth(),
                renderer.getRenderContext().getRenderSurface().getWidth(),
                proj,
                mapCenter,
                focusPoint_x, focusPoint_y,
                rotation,
                tilt,
                Globe.getMapResolution(surface.getDpi(), scale),
                true);

        GeoPoint focusLatLng = currentModel.inverse(new PointF(xpos, ypos), null);
        GeoPoint focusLatLng2 = sm.inverse(new PointF(xpos, ypos), null, true);

        if (focusLatLng != null && focusLatLng2 != null) {
            double focusLng2 = focusLatLng2.getLongitude();
            if (true) // XXX - continuous scroll enabled
                focusLng2 = GeoCalculations.wrapLongitude(focusLng2);
            double latDiff = focusLatLng2.getLatitude() - focusLatLng.getLatitude();
            double lonDiff = focusLng2 - focusLatLng.getLongitude();
            double lng = mapCenter.getLongitude() - lonDiff;
            if (true) // XXX - continuous scroll enabled
                lng = GeoCalculations.wrapLongitude(lng);

            renderer.lookAt(
                    new GeoPoint(mapCenter.getLatitude() - latDiff, lng),
                    Globe.getMapResolution(renderer.getRenderContext().getRenderSurface().getDpi(), scale),
                    rotation,
                    tilt,
                    animate);
        }
    }

    private static void updateBy(MapRenderer2 renderer,
                                 RenderSurface surface,
                                 MapSceneModel currentModel,
                                 double scale,
                                 double rotation,
                                 double tilt,
                                 GeoPoint pos,
                                 boolean animate) {

        if (pos == null)
            return;

        GeoPoint mapCenter = GeoPoint.createMutable();
        currentModel.mapProjection.inverse(currentModel.camera.target, mapCenter);
        final float focusPoint_x = surface.getWidth() / 2 + renderer.getFocusPointOffsetX();
        final float focusPoint_y = surface.getHeight() / 2 + renderer.getFocusPointOffsetY();

        final Projection proj = currentModel.mapProjection;

        MapSceneModel sm = new MapSceneModel(
                surface.getDpi(),
                renderer.getRenderContext().getRenderSurface().getWidth(),
                renderer.getRenderContext().getRenderSurface().getWidth(),
                proj,
                mapCenter,
                focusPoint_x, focusPoint_y,
                rotation,
                tilt,
                Globe.getMapResolution(surface.getDpi(), scale),
                true);

        GeoPoint focusLatLng = pos;
        PointF xypos = currentModel.forward(focusLatLng, (PointF)null);
        GeoPoint focusLatLng2 = sm.inverse(xypos, null, true);

        if (focusLatLng != null && focusLatLng2 != null) {
            double focusLng2 = focusLatLng2.getLongitude();
            if (true) // XXX - continuous scroll enabled
                focusLng2 = GeoCalculations.wrapLongitude(focusLng2);
            double latDiff = focusLatLng2.getLatitude() - focusLatLng.getLatitude();
            double lonDiff = focusLng2 - focusLatLng.getLongitude();
            double lng = mapCenter.getLongitude() - lonDiff;
            if (true) // XXX - continuous scroll enabled
                lng = GeoCalculations.wrapLongitude(lng);

            renderer.lookAt(
                    new GeoPoint(mapCenter.getLatitude() - latDiff, lng),
                    Globe.getMapResolution(renderer.getRenderContext().getRenderSurface().getDpi(), scale),
                    rotation,
                    tilt,
                    animate);
        }
    }

}
