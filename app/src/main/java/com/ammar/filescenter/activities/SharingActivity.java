package com.ammar.filescenter.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.recyclers.DownloadablesDialogAdapter;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.objects.Downloadable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

public class SharingActivity extends AppCompatActivity {
    static {
        System.loadLibrary("NativeQRCodeGen");
    }

    private final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private native byte[] encodeTextToQR(String text);

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MYLOG", "Received!!!!!");
            String action = intent.getAction();
            if (action != null)
                switch (action) {
                    case NetworkService.ACTION_GET_SERVER_STATUS:
                        boolean serverRunning = intent.getBooleanExtra(NetworkService.EXTRA_SERVER_STATUS, false);
                        if (serverButton == null) return;
                        IndicateServerOn(serverRunning);
                        break;
                    case NetworkService.ACTION_GET_DOWNLOADABLE:
                        LinkedList<Downloadable> downloadables = (LinkedList<Downloadable>) intent.getSerializableExtra(NetworkService.EXTRA_DOWNLOADABLES_ARRAY);
                        showDownloadablesDialog(downloadables);
                        break;
                    default:
                        break;
                }
        }
    };
    private Button serverButton;
    private Button qrCodeButton;
    private Button downloadablesDialogButton;
    private TextView serverStatusText;
    private TextView serverAddressText;
    private TextView updateIpAddressText;
    private ImageView qrCodeImage;
    private AlertDialog qrCodeDialog;
    private Button addFiles;
    private Button addApps;
    private final ActivityResultLauncher<Intent> mGetFiles = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent resultIntent = result.getData();
            ArrayList<String> selectedFilePaths = resultIntent.getStringArrayListExtra(AddFilesActivity.EXTRA_INTENT_PATHS);

            // intent to be sent to service
            Intent intent = new Intent(SharingActivity.this, NetworkService.class);
            intent.setAction(NetworkService.ACTION_ADD_DOWNLOADABLE);
            intent.putExtra(NetworkService.EXTRA_FILE_PATHS, selectedFilePaths);
            startService(intent);
        }
    });

    private final ActivityResultLauncher<Intent> mGetApps = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {

    });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set local broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkService.ACTION_GET_SERVER_STATUS);
        filter.addAction(NetworkService.ACTION_GET_DOWNLOADABLE);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        // set layout
        setContentView(R.layout.activity_share);

        // init UI elements
        InitElements();

        // handle permissions
        handlePermissions();


        // onclick start server
        addElementListeners();
        requestStorageAccess();
    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }


    private void InitElements() {
        serverButton = findViewById(R.id.B_ServerButton);
        qrCodeButton = findViewById(R.id.B_QRCodeButton);
        downloadablesDialogButton = findViewById(R.id.B_ShowDownloads);
        serverStatusText = findViewById(R.id.TV_ServerStatus);
        serverAddressText = findViewById(R.id.TV_ServerAddress);
        updateIpAddressText = findViewById(R.id.TV_UpdateIpAddressText);
        addFiles = findViewById(R.id.B_AddFiles);
        addApps  = findViewById(R.id.B_AddِApps);
        View qrDialogView = getLayoutInflater().inflate(R.layout.dialog_qrcode, null, false);
        qrCodeDialog = new AlertDialog.Builder(this)
                .setView(qrDialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })
                .create();
        qrCodeImage = qrDialogView.findViewById(R.id.IV_QRCodeImage);


        // init state
        Intent intent = new Intent(this, NetworkService.class);
        intent.setAction(NetworkService.ACTION_GET_SERVER_STATUS);
        startService(intent);
        ShowServerAddressAndPort();
    }

    private void addElementListeners() {

        serverButton.setOnClickListener((button) -> {
            Log.d("MYLOG", "Button pressed toggle server");
            Intent intent = new Intent(this, NetworkService.class);
            intent.setAction(NetworkService.ACTION_TOGGLE_SERVER);
            startService(intent);
            ShowServerAddressAndPort();
        });

        qrCodeButton.setOnClickListener((button) -> {
            qrCodeDialog.show();
            setupQrCode();
        });

        updateIpAddressText.setOnClickListener((view) -> {
            ShowServerAddressAndPort();

            Intent intent = new Intent(this, NetworkService.class);
            intent.setAction(NetworkService.ACTION_UPDATE_NOTIFICATION_TEXT);
            startService(intent);
        });

        addFiles.setOnClickListener((button) -> {
            mGetFiles.launch(new Intent(this, AddFilesActivity.class));
        });

        addApps.setOnClickListener((button) -> {
            mGetApps.launch(new Intent(this, AddAppsActivity.class));
        });
        downloadablesDialogButton.setOnClickListener((button) -> {
            Intent intent = new Intent(this, NetworkService.class);
            intent.setAction(NetworkService.ACTION_GET_DOWNLOADABLE);
            startService(intent);
        });

    }

    private void IndicateServerOn(boolean status) {
        if (status) {
            serverButton.setText(R.string.on);
            serverStatusText.setText(R.string.on);
            serverStatusText.setTextColor(getResources().getColor(R.color.status_on));
        } else {
            serverButton.setText(R.string.off);
            serverStatusText.setText(R.string.off);
            serverStatusText.setTextColor(getResources().getColor(R.color.status_off));
        }
    }

    @SuppressLint("SetTextI18n")
    private void ShowServerAddressAndPort() {
        String serverAddress = NetworkService.getIpAddress();
        int port = NetworkService.PORT_NUMBER;

        if (serverAddress != null) {
            serverAddressText.setText(String.format(Locale.ENGLISH, "%s:%d", serverAddress, port));
        } else {
            serverAddressText.setText("0.0.0.0:" + port);
        }
    }


    private Bitmap QrCodeArrayToBitmap(byte[] qrCodeBytes) {
        int qrColor;
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                qrColor = Color.WHITE;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                qrColor = Color.BLACK;
                break;
        }


        int size = (int) Math.sqrt(qrCodeBytes.length);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int pixel = qrCodeBytes[size * j + i] == 1 ? qrColor : Color.TRANSPARENT;
                bitmap.setPixel(j, i, pixel);
            }
        }

        return getResizedBitmap(bitmap, 5000, 5000);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }


    private void setupQrCode() {
        byte[] qrCodeBytes = encodeTextToQR("http://" + NetworkService.getIpAddress() + ":" + NetworkService.PORT_NUMBER);
        Bitmap qrCodeBitmap = QrCodeArrayToBitmap(qrCodeBytes);
        // Display the bitmap in an ImageView or any other suitable view
        qrCodeImage.setImageBitmap(qrCodeBitmap);
    }

    private void removeQrCode() {
        if (qrCodeImage != null)
            qrCodeImage.setImageDrawable(null);
    }

    final int notificationRequestCode = 1;

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = new String[]{Manifest.permission.POST_NOTIFICATIONS};
            requestPermissions(permissions, notificationRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == notificationRequestCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.notification_refused_message, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {

        }
    }


    private void requestStorageAccess() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()) {
                Uri uri = Uri.parse(String.format(Locale.ENGLISH,"package:%s", getApplicationContext().getPackageName()));
                startActivity(
                        new Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                uri
                        )
                );

            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_CODE_STORAGE_PERMISSION);
            }
        }
    }


    private void showDownloadablesDialog(LinkedList<Downloadable> downloadableList) {
        View downloadablesDialogView = getLayoutInflater().inflate(R.layout.dialog_downloadables, null, false);
        RecyclerView downloadablesRecycler = downloadablesDialogView.findViewById(R.id.RV_DownloadablesDialogRecycler);
        DownloadablesDialogAdapter adapter = new DownloadablesDialogAdapter(this, downloadableList);
        downloadablesRecycler.setAdapter(adapter);
        downloadablesRecycler.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(downloadablesDialogView)
                .setPositiveButton(R.string.ok, (dialogView, which) -> {
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if( window != null ) {
                View decorView = window.getDecorView();
                decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ViewGroup.LayoutParams layoutParams = downloadablesRecycler.getLayoutParams();
                        layoutParams.width = decorView.getWidth();
                        downloadablesRecycler.setLayoutParams(layoutParams);
                    }
                });
            }
        });
        dialog.show();

    }
}
