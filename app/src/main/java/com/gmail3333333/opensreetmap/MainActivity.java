package com.gmail3333333.opensreetmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gmail3333333.opensreetmap.api.ServerAPI;
import com.gmail3333333.opensreetmap.model.Basslocation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_READ_CONTACTS = 123;
    @BindView(R.id.bntDir)
    Button direction1;
    @BindView(R.id.bntDir2)
    Button direction2;
    @BindView(R.id.btnDir3)
    Button direction3;
    @BindView(R.id.btnStartCar)
    Button startCAr;
    private ScaleBarOverlay mScaleBarOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    @BindView(R.id.ic_center_map)
    ImageButton btCenterMap;
    @BindView(R.id.ic_follow_me)
    ImageButton btFollowMe;
    ImageButton btOrthoMap;
    private MapView map = null;
    private MapController mapController = null;
    private MyLocationNewOverlay locationOverlay;
    private CompassOverlay compassOverlay;
    private RoadManager roadManager;
    private Marker hideMarke;
    private Interpolator interpolator;
    private ArrayList<GeoPoint> geoPoints;
    private long start;
    private long duration;
    private Handler h;

    Handler.Callback hc = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            Log.d("TAG", "loadRoad" + msg);
            animateMarker(map, hideMarke, geoPoints.get(msg.what));
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        h = new Handler();
        h = new Handler(hc);
        geoPoints = new ArrayList<>();
        loadGeo();
        duration = 2500;
        start = SystemClock.uptimeMillis();
        interpolator = new LinearInterpolator();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        checkPermition();
        roadManager = new OSRMRoadManager(this);
        final DisplayMetrics dm = this.getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mRotationGestureOverlay = new RotationGestureOverlay(this, map);
        mRotationGestureOverlay.setEnabled(true);
        hideMarke = new Marker(map);

    }

    @OnClick({R.id.btnStartCar, R.id.bntDir, R.id.bntDir2, R.id.btnDir3, R.id.ic_follow_me, R.id.ic_center_map})
    public void ClickButton(View view) {
        switch (view.getId()) {
            case R.id.btnStartCar:
                hideMarke.setPosition(new GeoPoint(47.496618, 34.649008));
                map.getOverlays().add(hideMarke);
                map.invalidate();
                ServerAPI apiService = ServerAPI.retrofit.create(ServerAPI.class);
                apiService.getBasById(2).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                        .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                                return throwableObservable.take(3).delay(1, TimeUnit.SECONDS);
                            }
                        })
                        .subscribe(new DisposableObserver<Basslocation>() {
                            @Override
                            public void onNext(Basslocation basslocation) {
                                Log.d("TAG", "" + basslocation.toString());
                                animateMarker(map, hideMarke, new GeoPoint(basslocation.getLotittude(),basslocation.getLongittude()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("TAG", "onError --> " + e.toString());
                                dispose();
                            }

                            @Override
                            public void onComplete() {
                                Log.d("TAG", "onComplete");
                            }
                        });
//                moveMarker();
                break;
            case R.id.bntDir:
                lineRedDirection1();
                break;
            case R.id.bntDir2:
                lineRedDirection2();
                break;
            case R.id.btnDir3:
                break;
            case R.id.ic_follow_me:
                Log.d("TAG", "ic_center_map" );
                LocationManager mLocationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.d("TAG", "проверку не прошел" );
                        return;
                    }
                }
                Log.d("TAG", "прошел" );
                Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                GeoPoint myPosition = new GeoPoint(locationGPS.getLatitude(), locationGPS.getLongitude());
                map.getController().animateTo(myPosition);
                break;
            case R.id.ic_center_map:
                Log.d("TAG", "ic_follow_me" );
                if (!locationOverlay.isFollowLocationEnabled()) {
                    locationOverlay.enableFollowLocation();
//                    btFollowMe.setImageResource(R.drawable.ic_follow_me_on);
                } else {
                    locationOverlay.disableFollowLocation();
//                    btFollowMe.setImageResource(R.drawable.ic_follow_me);
                }
                break;
        }
    }

    private void checkPermition() {
        int ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int ACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d("TAG", +ACCESS_COARSE_LOCATION + "" + ACCESS_COARSE_LOCATION);
        if (ACCESS_FINE_LOCATION == PackageManager.PERMISSION_GRANTED
                && ACCESS_COARSE_LOCATION == PackageManager.PERMISSION_GRANTED
                && WRITE_EXTERNAL_STORAGE == PackageManager.PERMISSION_GRANTED) {
//            mapFragment.getMapAsync(this);
            map();
        } else {
            Log.d("TAG", "" + "DONT " + Manifest.permission.ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSION_READ_CONTACTS);
        }
    }


    private void map() {

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);
        map.setBuiltInZoomControls(true);
        map.setFlingEnabled(true);
        map.getOverlays().add(this.mScaleBarOverlay);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        locationOverlay.setDrawAccuracyEnabled(true);
        locationOverlay.setOptionsMenuEnabled(true);


        compassOverlay = new CompassOverlay(getApplicationContext(),
                new InternalCompassOrientationProvider(getApplicationContext()), map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);
        map.getOverlays().add(locationOverlay);

        mapController = (MapController) map.getController();
        mapController.setZoom(16);
        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        items.add(new OverlayItem("Title", "Description", new GeoPoint(0.0d, 0.0d))); // Lat/Lon decimal degrees
        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                map();
            }
        }
    }

    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public static void animateMarker(final MapView map, final Marker marker, final GeoPoint toPosition) {
        Log.d("TAG", "animateMarker "+ toPosition.getLatitude()+ " =---"+ toPosition.getLongitude());
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toPixels(marker.getPosition(), null);
        final IGeoPoint startLatLng = proj.fromPixels(startPoint.x, startPoint.y);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.getLongitude() + (1 - t) * startLatLng.getLongitude();
                double lat = t * toPosition.getLatitude() + (1 - t) * startLatLng.getLatitude();

                marker.setPosition(new GeoPoint(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
                map.postInvalidate();
            }
        });
    }

    private void loadRoad() {
        for (int i = 1; i <= 25; i++) {
            Log.d("TAG", "loadRoad" + i);
            h.sendEmptyMessageDelayed(i, (i * 1000) + 1000);
        }


    }

    private ArrayList<GeoPoint> loadGeo() {
        geoPoints.add(new GeoPoint(47.49686, 34.64888));
        geoPoints.add(new GeoPoint(47.49485, 34.65054));
        geoPoints.add(new GeoPoint(47.49315, 34.65174));
        geoPoints.add(new GeoPoint(47.4907, 34.6535));
        geoPoints.add(new GeoPoint(47.48854, 34.65502));
        geoPoints.add(new GeoPoint(47.48689, 34.65611));
        geoPoints.add(new GeoPoint(47.48664, 34.65616));
        geoPoints.add(new GeoPoint(47.48424, 34.6552));
        geoPoints.add(new GeoPoint(47.48347, 34.65922));
        geoPoints.add(new GeoPoint(47.48295, 34.66191));
        geoPoints.add(new GeoPoint(47.48572, 34.66308));
        geoPoints.add(new GeoPoint(47.48703, 34.6636));
        geoPoints.add(new GeoPoint(47.49016, 34.66105));
        geoPoints.add(new GeoPoint(47.4907, 34.66069));
        geoPoints.add(new GeoPoint(47.49144, 34.66254));
        geoPoints.add(new GeoPoint(47.49351, 34.6608));
        geoPoints.add(new GeoPoint(47.4958, 34.65889));
        geoPoints.add(new GeoPoint(47.49764, 34.65737));
        geoPoints.add(new GeoPoint(47.49896, 34.6563));
        geoPoints.add(new GeoPoint(47.49984, 34.65561));
        geoPoints.add(new GeoPoint(47.49879, 34.65283));
        geoPoints.add(new GeoPoint(47.49719, 34.64867));
        geoPoints.add(new GeoPoint(47.49703, 34.64846));
        geoPoints.add(new GeoPoint(47.49692, 34.64862));
        geoPoints.add(new GeoPoint(47.49694, 34.64879));
        geoPoints.add(new GeoPoint(47.49669, 34.64897));
        return geoPoints;
    }

    private void lineRedDirection1() {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new GeoPoint(47.49686, 34.64888));
        geoPoints.add(new GeoPoint(47.49485, 34.65054));
        geoPoints.add(new GeoPoint(47.49315, 34.65174));
        geoPoints.add(new GeoPoint(47.4907, 34.6535));
        geoPoints.add(new GeoPoint(47.48854, 34.65502));
        geoPoints.add(new GeoPoint(47.48689, 34.65611));
        geoPoints.add(new GeoPoint(47.48664, 34.65616));
        geoPoints.add(new GeoPoint(47.48424, 34.6552));
        geoPoints.add(new GeoPoint(47.48347, 34.65922));
        geoPoints.add(new GeoPoint(47.48295, 34.66191));
        geoPoints.add(new GeoPoint(47.48572, 34.66308));
        geoPoints.add(new GeoPoint(47.48703, 34.6636));
        geoPoints.add(new GeoPoint(47.49016, 34.66105));
        geoPoints.add(new GeoPoint(47.4907, 34.66069));
        geoPoints.add(new GeoPoint(47.49144, 34.66254));
        geoPoints.add(new GeoPoint(47.49351, 34.6608));
        geoPoints.add(new GeoPoint(47.4958, 34.65889));
        geoPoints.add(new GeoPoint(47.49764, 34.65737));
        geoPoints.add(new GeoPoint(47.49896, 34.6563));
        geoPoints.add(new GeoPoint(47.49984, 34.65561));
        geoPoints.add(new GeoPoint(47.49879, 34.65283));
        geoPoints.add(new GeoPoint(47.49719, 34.64867));
        geoPoints.add(new GeoPoint(47.49703, 34.64846));
        geoPoints.add(new GeoPoint(47.49692, 34.64862));
        geoPoints.add(new GeoPoint(47.49694, 34.64879));
        geoPoints.add(new GeoPoint(47.49669, 34.64897));
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setOnClickListener((polyline, mapView, eventPos) -> {
            Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
            return false;
        });
        line.setColor(Color.YELLOW);
        map.getOverlayManager().add(line);
        map.invalidate();
    }

    private void lineRedDirection2() {
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new GeoPoint(47.49686, 34.64888));
        geoPoints.add(new GeoPoint(47.49485, 34.65054));
        geoPoints.add(new GeoPoint(47.49315, 34.65174));
        geoPoints.add(new GeoPoint(47.4907, 34.6535));
        geoPoints.add(new GeoPoint(47.48854, 34.65502));
        geoPoints.add(new GeoPoint(47.48689, 34.65611));
        geoPoints.add(new GeoPoint(47.48664, 34.65616));
        geoPoints.add(new GeoPoint(47.48424, 34.6552));
        geoPoints.add(new GeoPoint(47.48347, 34.65922));
        geoPoints.add(new GeoPoint(47.48295, 34.66191));
        geoPoints.add(new GeoPoint(47.48572, 34.66308));
        geoPoints.add(new GeoPoint(47.48703, 34.6636));
        geoPoints.add(new GeoPoint(47.49016, 34.66105));
        geoPoints.add(new GeoPoint(47.4907, 34.66069));
        geoPoints.add(new GeoPoint(47.49144, 34.66254));
        geoPoints.add(new GeoPoint(47.49351, 34.6608));
        geoPoints.add(new GeoPoint(47.4958, 34.65889));
        geoPoints.add(new GeoPoint(47.49764, 34.65737));
        geoPoints.add(new GeoPoint(47.49896, 34.6563));
        geoPoints.add(new GeoPoint(47.49984, 34.65561));
        geoPoints.add(new GeoPoint(47.49879, 34.65283));
        geoPoints.add(new GeoPoint(47.49719, 34.64867));
        geoPoints.add(new GeoPoint(47.49703, 34.64846));
        geoPoints.add(new GeoPoint(47.49692, 34.64862));
        geoPoints.add(new GeoPoint(47.49694, 34.64879));
        geoPoints.add(new GeoPoint(47.49669, 34.64897));
        ;
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setOnClickListener((polyline, mapView, eventPos) -> {
            Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + " pts was tapped", Toast.LENGTH_LONG).show();
            return false;
        });
        line.setColor(Color.RED);
        map.getOverlayManager().add(line);
        map.invalidate();
    }

    private void moveMarker() {
        hideMarke.setPosition(new GeoPoint(47.496618, 34.649008));
        map.getOverlays().add(hideMarke);
        map.invalidate();
//        animateMarker(map, hideMarke, new GeoPoint(47.49315, 34.65174));
        loadRoad();
    }

}
//47.49686, 34.64888
//47.49485, 34.65054
//47.49315, 34.65174
//47.4907, 34.6535
//47.48854, 34.65502
//47.48689, 34.65611
//47.48664, 34.65616
//47.48424, 34.6552
//47.48347, 34.65922
//47.48295, 34.66191
//47.48572, 34.66308
//47.48703, 34.6636
//47.49016, 34.66105
//47.4907, 34.66069
//47.49144, 34.66254
//47.49351, 34.6608
//47.4958, 34.65889
//47.49764, 34.65737
//47.49896, 34.6563
//47.49984, 34.65561
//47.49879, 34.65283
//47.49719, 34.64867
//47.49703, 34.64846
//47.49692, 34.64862
//47.49694, 34.64879
//47.49669, 34.64897

