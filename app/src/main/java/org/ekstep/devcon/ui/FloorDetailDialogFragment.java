package org.ekstep.devcon.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ekstep.devcon.R;
import org.ekstep.devcon.telemetry.ImpressionType;
import org.ekstep.devcon.telemetry.TelemetryBuilder;
import org.ekstep.devcon.telemetry.TelemetryHandler;

/**
 * @author vinayagasundar
 */

public class FloorDetailDialogFragment extends DialogFragment {

    private static final String BUNDLE_TITLE = "title";
    private static final String BUNDLE_WEB_URL = "web_url";
    private static final String BUNDLE_COLOR = "color";
    private static final String BUNDLE_X = "x";
    private static final String BUNDLE_Y = "Y";

    private String mTitle;
    private String mWebUrl;
    private int mColorValue;
    private float mX;
    private float mY;


    public static FloorDetailDialogFragment newInstance(String title, String webUrl, int color, float x, float y) {
        FloorDetailDialogFragment fragment = new FloorDetailDialogFragment();

        Bundle args = new Bundle();

        args.putString(BUNDLE_TITLE, title);
        args.putString(BUNDLE_WEB_URL, webUrl);
        args.putInt(BUNDLE_COLOR, color);
        args.putFloat(BUNDLE_X, x);
        args.putFloat(BUNDLE_Y, y);

        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTitle = getArguments().getString(BUNDLE_TITLE);
            mWebUrl = getArguments().getString(BUNDLE_WEB_URL);
            mColorValue = getArguments().getInt(BUNDLE_COLOR);
            mX = getArguments().getFloat(BUNDLE_X);
            mY = getArguments().getFloat(BUNDLE_Y);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_floor_detail, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //Set the dialog to immersive
        dialog.getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility());

        return dialog;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(mTitle, ImpressionType.DETAIL,null));

        View closeIcon = view.findViewById(R.id.close_icon);
        TextView title = view.findViewById(R.id.title);
        WebView webView = view.findViewById(R.id.webview);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        title.setText(mTitle);
        title.setBackgroundColor(mColorValue);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

                // From google play console we received a mail about Security flaw in this
                // implementation. So before proceeding to the page ask user opinion
                // http://stackoverflow.com/questions/36050741/webview-avoid-security-alert-from-google-play-upon-implementation-of-onreceiveds/36147896#36147896

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Invalid SSL Are you sure you want to continue?");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        webView.loadUrl(mWebUrl);
    }

    @Override
    public void onStart() {
        super.onStart();

        final View decorView = getDialog()
                .getWindow()
                .getDecorView();

        decorView.setPivotX(mX);
        decorView.setPivotY(mY);

        PropertyValuesHolder pvhPivotX = PropertyValuesHolder.ofFloat("pivotX", mX);
        PropertyValuesHolder pvhPivotY = PropertyValuesHolder.ofFloat("pivotY", mY);

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(decorView,
                pvhPivotX,
                pvhPivotY,
                PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f),
                PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f));
        scaleDown.setDuration(750);
        scaleDown.start();
    }
}
