package com.example.tonyjia.camerademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button, R.id.button2, R.id.button3, R.id.button4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
                FaceCameraViewActivity.startActivity(this);
                break;
            case R.id.button2:
                SurfaceCameraViewActivity.startActivity(this);
                break;
            case R.id.button3:
                TextureCameraViewActivity.startActivity(this);
                break;
            case R.id.button4:


                break;
        }
    }
}
