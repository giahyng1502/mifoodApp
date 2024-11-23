package com.fpoly.nhom2.mifoodapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import FPT.PRO1122.Nhom3.DuAn1.R;

public class DeliveryAddress extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 10;
    private ImageView btn_back;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        // Thiết lập thanh điều hướng
        btn_back = findViewById(R.id.btn_back_delivery);
        btn_back.setOnClickListener(v -> startActivity(new Intent(DeliveryAddress.this, Profile.class)));

        // Khởi tạo bản đồ
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Cài đặt giao diện
        setupEdgeToEdge();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Cài đặt các tùy chọn cho bản đồ
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Kiểm tra quyền truy cập vị trí và yêu cầu quyền nếu cần
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
            return;
        }

        // Nếu đã cấp quyền, bật tính năng hiển thị vị trí hiện tại
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Thêm đánh dấu mẫu trên bản đồ
        LatLng MiFood = new LatLng(21, 106);
        mMap.addMarker(new MarkerOptions().position(MiFood).title("Mi Food"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MiFood, 10));

        // Lấy vị trí hiện tại của người dùng
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 20, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                getAddressFromLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(@NonNull String provider) {}
            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0); // Địa chỉ chi tiết
                Toast.makeText(this, "Current Location: " + addressString, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No address found for current location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to get address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, bật tính năng hiển thị vị trí hiện tại
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Quyền bị từ chối, thông báo cho người dùng
                Toast.makeText(this, "Permission denied. Unable to access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
