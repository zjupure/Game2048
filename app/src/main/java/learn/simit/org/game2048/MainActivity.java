package learn.simit.org.game2048;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView scoreTv;
    private TextView bestScoreTv;
    private TextView moveTv;
    private Game2048Layout games;
    private int bestScore;
    private DialogFragment diagFragment;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setNavigationIcon(R.mipmap.ic_launcher);
        setSupportActionBar(mToolbar);
        //
        games = (Game2048Layout) findViewById(R.id.game2048);
        games.setOnGameChangeListener(new Game2048Layout.OnGameChangeListener() {
            @Override
            public void onScoreChanged(boolean changed, int score) {
                scoreTv.setText(String.format("%d", score));
            }

            @Override
            public void onMovement(int move) {
                moveTv.setText(String.format("%d MOVE", move));
            }

            @Override
            public void onGameOver(int score) {
                if(score > bestScore){
                    setBestScore(score);
                    bestScoreTv.setText(String.format("%d", score));
                }
                diagFragment.show(getSupportFragmentManager(), "Game Over");
            }
        });
        scoreTv = (TextView) findViewById(R.id.cur_score);
        bestScoreTv = (TextView) findViewById(R.id.best_score);
        moveTv = (TextView) findViewById(R.id.movement);
        //
        bestScore = getBestScore();
        bestScoreTv.setText(String.format("%d", bestScore));
        //
        diagFragment = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.gameover_dialog_layout, null);
                Button restart = (Button) view.findViewById(R.id.restart);
                builder.setView(view);

                restart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diagFragment.dismiss();
                        games.reset();
                        scoreTv.setText(String.format("%d", 0));
                        moveTv.setText(String.format("%d MOVE", 0));
                    }
                });

                return builder.create();
            }
        };

    }

    private int getBestScore(){
        SharedPreferences preferences = getSharedPreferences("scores", Context.MODE_PRIVATE);

        return preferences.getInt("best_score", 0);
    }

    private void setBestScore(int score){
        bestScore = score;
        SharedPreferences preferences = getSharedPreferences("scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("best_score", score);
        editor.apply();
    }
}
