package com.ammar.filescenter.activities.MainActivity.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity;
import com.ammar.filescenter.activities.AddFilesActivity;
import com.ammar.filescenter.activities.MainActivity.adapters.SendAdapter;
import com.ammar.filescenter.activities.MainActivity.dialogs.ChosenFilesDialog;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.utils.Utils;

import java.util.ArrayList;


public class SendFragment extends Fragment {
    private View v;
    private Toolbar toolbar;
    private ImageButton addAppsB;
    private ImageButton addFilesB;
    private ImageButton showSelected;
    private RecyclerView filesSendRV;
    private SendAdapter adapter;
    private ImageButton QRCodeB;
    private AlertDialog QRDialogAD;
    private ImageView QRImageIV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_send, container, false);
        initItems();
        setItemsListener();
        initObservers();
        return v;
    }


    private void initItems() {
        toolbar = v.findViewById(R.id.TB_Toolbar);
        toolbar.setTitle("Share");

        addAppsB = v.findViewById(R.id.B_AddApps);
        addFilesB = v.findViewById(R.id.B_AddFiles);
        showSelected = v.findViewById(R.id.B_ShowSelected);

        filesSendRV = v.findViewById(R.id.RV_FilesSend);
        adapter = new SendAdapter();
        filesSendRV.setAdapter(adapter);
        filesSendRV.setLayoutManager(new LinearLayoutManager(getContext()));
        filesSendRV.setItemAnimator(null);
        filesSendRV.setHasFixedSize(true);
        QRCodeB = v.findViewById(R.id.B_ShowQRCode);
        View QRDialogView = getLayoutInflater().inflate(R.layout.dialog_qrcode, null, false);
        QRDialogAD = new AlertDialog.Builder(requireContext())
                .setView(QRDialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })
                .create();
        QRImageIV = QRDialogView.findViewById(R.id.IV_QRCodeImage);

    }

    private void setItemsListener() {

        addAppsB.setOnClickListener((button) -> launcher.launch(new Intent(getActivity(), AddAppsActivity.class)));

        addFilesB.setOnClickListener((button) -> mGetContent.launch("*/*"));

        showSelected.setOnClickListener(button -> {
            ChosenFilesDialog dialog = new ChosenFilesDialog();
            dialog.show(requireActivity().getSupportFragmentManager(), ChosenFilesDialog.TAG);
        });

        QRCodeB.setOnClickListener( button -> {
            QRDialogAD.show();
            setupQrCode();
        });

    }

    private void initObservers() {
        NetworkService.filesListObserver.observe(getViewLifecycleOwner(), data -> {
        });

        NetworkService.filesSendNotifier.observe( requireActivity(), info -> {
            char action = info.getChar("action");
            int index = info.getInt("index");

            switch (action) {
                case 'P':
                    adapter.notifyItemChanged(index);
                    break;
                case 'R':
                    adapter.notifyItemRemoved(index);
                    break;
                case 'A':
                    adapter.notifyItemInserted(index);
                    break;
            }


        });

    }


    private void setupQrCode() {
        byte[] qrCodeBytes = Utils.encodeTextToQR("http://" + NetworkService.getIpAddress() + ":" + NetworkService.PORT_NUMBER);
        Bitmap qrCodeBitmap = Utils.QrCodeArrayToBitmap(qrCodeBytes);
        // Display the bitmap in an ImageView or any other suitable view
        QRImageIV.setImageBitmap(qrCodeBitmap);
    }


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getAction() != null) {
            Intent data = result.getData();
            Intent intent = new Intent(requireContext(), NetworkService.class);

            if (data.getAction().equals(AddFilesActivity.ACTION_ADD_FILES)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(AddFilesActivity.EXTRA_INTENT_PATHS);
                // intent to be sent to service
                intent.setAction(NetworkService.ACTION_ADD_DOWNLOADS);
                intent.putExtra(NetworkService.EXTRA_FILE_PATHS, selectedFilePaths);
            } else if (data.getAction().equals(AddAppsActivity.ACTION_ADD_APPS)) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsActivity.EXTRA_INTENT_APPS);

                intent.setAction(NetworkService.ACTION_ADD_APPS_DOWNLOADS);
                intent.putExtra(NetworkService.EXTRA_APPS_NAMES, selectedApps);
            }
            requireContext().startService(intent);

        }
    });


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), (result) -> {
        ArrayList<String> files = new ArrayList<>(result.size());

        for( Uri i : result ) {
            String path = Utils.getPathFromUri(requireContext(), i);
            files.add(path);
        }

        Intent intent = new Intent(requireContext(), NetworkService.class);

        // intent to be sent to service
        intent.setAction(NetworkService.ACTION_ADD_DOWNLOADS);
        intent.putExtra(NetworkService.EXTRA_FILE_PATHS, files);
        requireContext().startService(intent);
    });
}