package de.mpg.mpdl.labcam.code.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;

import butterknife.ButterKnife;

@SuppressLint("NewApi")
public abstract class BaseCompatActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(getLayoutId());
        ButterKnife.bind(this);
        initContentView(savedInstanceState);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * this activity layout res
     * set layout resource file
     *
     * @return res layout xml id
     */
    protected abstract int getLayoutId();

    protected abstract void initContentView(Bundle savedInstanceState);


}
