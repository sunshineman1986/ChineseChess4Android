package com.onezeros.chinesechess;

import cn.domob.android.ads.DomobAdView;

import com.android.chinesechess.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ChineseChessActivity extends Activity {
	ChessboardView mChessboardView;
	RelativeLayout mMainLayout;
	LinearLayout mMenuLayout;
	Button mNewGameButton;
	Button mContinueButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mMainLayout = (RelativeLayout)findViewById(R.id.mainview);
        mMenuLayout = (LinearLayout)findViewById(R.id.menu_view);
        mNewGameButton = (Button)findViewById(R.id.new_game_btn);
        mContinueButton = (Button)findViewById(R.id.restore_game_btn);
        
        mChessboardView = (ChessboardView)findViewById(R.id.chessboard);
        
        mNewGameButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mChessboardView.newGame();
				switchViewTo(mMainLayout);
			}
		});
        mContinueButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mChessboardView.restoreGameStatus();
				switchViewTo(mMainLayout);
			}
		});
        
        // domob ad
        LinearLayout layout = (LinearLayout)findViewById(R.id.AdLinearLayout);
        DomobAdView adView = new DomobAdView(this,"56OJyOeouMzH2P6sIM",DomobAdView.INLINE_SIZE_320X50);
        layout.addView(adView);
    }

    void switchViewTo(View v) {
    	if (v == mMainLayout) {
			mMenuLayout.setVisibility(View.INVISIBLE);
			mMainLayout.setVisibility(View.VISIBLE);
		}else if (v == mMenuLayout) {
			mMenuLayout.setVisibility(View.VISIBLE);
			mMainLayout.setVisibility(View.INVISIBLE);
		}
    }
	@Override
	public void onBackPressed() {
		if (mMainLayout.getVisibility() == View.VISIBLE) {
			switchViewTo(mMenuLayout);
		}
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		mChessboardView.saveGameStatus();
	}

	@Override
	protected void onResume() {		
		super.onResume();
		MobclickAgent.onResume(this);
	}
}