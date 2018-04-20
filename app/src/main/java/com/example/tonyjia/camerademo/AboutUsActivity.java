package com.example.tonyjia.camerademo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 关于我们
 */
public class AboutUsActivity extends AppCompatActivity {
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AboutUsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        /**
         @BindView(R.id.version) TextView mVersionTextView;
         @BindView(R.id.about_list) QMUIGroupListView mAboutGroupListView;
         @BindView(R.id.copyright) TextView mCopyrightTextView;

         mVersionTextView.setText(QMUIPackageHelper.getAppVersion(getContext()));

         QMUIGroupListView.newSection(getContext())
         .addItemView(mAboutGroupListView.createItemView(getResources().getString(R.string.about_item_homepage)), new View.OnClickListener() {
         @Override public void onClick(View v) {
         String url = "http://qmuiteam.com/android/page/index.html";
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse(url));
         startActivity(intent);
         }
         })
         .addItemView(mAboutGroupListView.createItemView(getResources().getString(R.string.about_item_github)), new View.OnClickListener() {
         @Override public void onClick(View v) {
         String url = "https://github.com/QMUI/QMUI_Android";
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse(url));
         startActivity(intent);
         }
         })
         .addTo(mAboutGroupListView);

         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.CHINA);
         String currentYear = dateFormat.format(new java.util.Date());
         mCopyrightTextView.setText(String.format(getResources().getString(R.string.about_copyright), currentYear));






         */
    }
}
