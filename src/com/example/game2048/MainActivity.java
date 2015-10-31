package com.example.game2048;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements Game2048Layout.Game2048Listener{
	private Game2048Layout mGame2048Layout;  //·½¿éÈÝÆ÷
	private TextView mScore;  //·ÖÊýÈÝÆ÷
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mScore = (TextView)findViewById(R.id.id_score);
		mGame2048Layout = (Game2048Layout)findViewById(R.id.id_game2048);
		mGame2048Layout.setOnGame2048Listener(this);  //ÉèÖÃ¼àÌýÆ÷
	}

	@Override
	public void onScoreChange(int score) {
		// TODO Auto-generated method stub
		mScore.setText(score + "");
	}

	@Override
	public void onGameOver() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this).setTitle("GAME OVER")
				.setMessage("YOU HAVE GOT " + mScore.getText())
				.setPositiveButton("RESTART", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mGame2048Layout.reStart();
					}
				}).setNegativeButton("EXIT", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				}).show();
	}
}
