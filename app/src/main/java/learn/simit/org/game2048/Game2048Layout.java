package learn.simit.org.game2048;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by liuchun on 2016/3/25.
 */
public class Game2048Layout extends ViewGroup{
    private static final int DEFAULT_COLUMN = 4;
    private static final int DEFAULT_MARGIN = 16;
    public static final int ACTION_UP_SWIPE = 0;
    public static final int ACTION_DOWN_SWIPE = 1;
    public static final int ACTION_LEFT_SWIPE = 2;
    public static final int ACTION_RIGHT_SWIPE = 3;
    public static final int MIN_FLING_DIS = 50;
    private static final String[] NUM_COLOR = {"#CDC1B4","#EEE4DA","#EDE0C8","#F2B179","#F59563",
    	"#F67C5F","#F65E3B","#EDCF72","#EDCC61","#EDC850","#EEC340","#EEC22E","#FF3D3D","#FF1C1E",
            "#FF1E20","#FF1D1F"};
    // columns
    private int mColumn = DEFAULT_COLUMN;
    // margin
    private int mMargin = DEFAULT_MARGIN;
    // Game Items
    private TextView[] mGameItems;
    // Numbers in the label
    private int[] mNumbers;
    // Number of Movements
    private int mMove = 0;
    // Scores
    private int mScore = 0;
    // Gesture detector
    private GestureDetectorCompat mGesture;
    // GameChange listener
    private OnGameChangeListener mGameListener;

    public Game2048Layout(Context context) {
        this(context, null);
    }

    public Game2048Layout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Game2048Layout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //
        TypedArray a = getResources().obtainAttributes(attrs, R.styleable.Game2048Layout);
        mColumn = a.getInt(R.styleable.Game2048Layout_column_count, DEFAULT_COLUMN);
        a.recycle();
        //
        mNumbers = new int[mColumn*mColumn];
        mGameItems = new TextView[mColumn*mColumn];
        for(int i = 0; i < mColumn*mColumn; i++){
            mNumbers[i] = 0;
            mGameItems[i] = new TextView(context);
            mGameItems[i].setText("");
            mGameItems[i].setTextSize(24);
            mGameItems[i].setTextColor(Color.parseColor("#776E65"));
            mGameItems[i].setGravity(Gravity.CENTER);
            mGameItems[i].setBackgroundColor(Color.parseColor(NUM_COLOR[0]));
            addView(mGameItems[i]);
        }
        // initial the game data
        generateRandom();
        // Gesture Detector
        mGesture = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                if(dx > MIN_FLING_DIS && Math.abs(velocityX) > Math.abs(velocityY)){
                    // right
                    dealWithSwipe(ACTION_RIGHT_SWIPE);
                }else if(dx < -MIN_FLING_DIS && Math.abs(velocityX) > Math.abs(velocityY)){
                    // left
                    dealWithSwipe(ACTION_LEFT_SWIPE);
                }else if(dy > MIN_FLING_DIS && Math.abs(velocityY) > Math.abs(velocityX)){
                    // down
                    dealWithSwipe(ACTION_DOWN_SWIPE);
                }else if(dy < -MIN_FLING_DIS && Math.abs(velocityY) > Math.abs(velocityX) ){
                    // up
                    dealWithSwipe(ACTION_UP_SWIPE);
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        //
        int childSize = (size - mMargin*(mColumn+1))/mColumn;
        int childSizeSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY);
        for(int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            child.measure(childSizeSpec, childSizeSpec);
        }
        //
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = mMargin, childTop = mMargin;
        for(int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            if(child.getVisibility() == GONE){
                continue;
            }
            //
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            child.layout(childLeft, childTop, childLeft+childWidth, childTop+childHeight);
            //
            if(i%mColumn == (mColumn-1)){
                childLeft = mMargin;
                childTop += childHeight + mMargin;
            }else{
                childLeft += childWidth + mMargin;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // delivery event to the Gesture Detector
        mGesture.onTouchEvent(event);
        return true;
    }

    /** 设置监听 */
    public void setOnGameChangeListener(OnGameChangeListener listener){
        mGameListener = listener;
    }

    /** 处理滑动操作 */
    public void dealWithSwipe(int action){
        int index = 0;
        boolean isMerge = false;
        List<Integer> row = new ArrayList<>();
        // line by line
        for(int i = 0; i < mColumn; i++){
            row.clear();
            for(int j = 0; j < mColumn; j++){
                index = getNumberIndex(action, i, j);
                if(mNumbers[index] > 0){
                    row.add(mNumbers[index]);
                }
            }
            // merge the same value
            int k = 0;
            for(int j = 0; j < row.size(); j++){
                index = getNumberIndex(action, i, k);
                if(j+1 < row.size() && row.get(j).equals(row.get(j+1))){
                    mNumbers[index] = row.get(j) << 1;  // x2
                    updateLabel(index);
                    mScore += mNumbers[index];
                    if(mGameListener != null){
                        mGameListener.onScoreChanged(true, mScore);
                    }
                    //
                    isMerge = true;
                    j++;  // skip the next pos
                }else{
                    mNumbers[index] = row.get(j);
                    updateLabel(index);
                }
                k++;
            }
            while(k < mColumn){
                index = getNumberIndex(action, i, k);
                mNumbers[index] = 0;
                updateLabel(index);
                k++;
            }
        }
        //
        if(isMerge || !isFull()){
            generateRandom();
            mMove++;
            if(mGameListener != null){
                mGameListener.onMovement(mMove);
            }
        }else if(!isMergeable()){
            //gameover
            if(mGameListener != null){
                mGameListener.onGameOver(mScore);
            }
            // reset
            //reset();
        }
    }

    /** 方格是否已满 */
    private boolean isFull(){
        //
        for(int i = 0; i < mColumn*mColumn; i++){
            if(mNumbers[i] == 0){
                return false;
            }
        }
        return true;
    }

    /** 方格是否存在可以合并 */
    private boolean isMergeable(){
        for(int i = 0; i < mColumn; i++){
            //
            for(int j = 0; j < mColumn; j++){
                if(mNumbers[i*mColumn+j] == 0){
                    continue;
                }
                // right block is equal to left
                if(j+1 < mColumn && mNumbers[i*mColumn+j] == mNumbers[i*mColumn+j+1]){
                    return true;
                }
                if(i+1 < mColumn && mNumbers[i*mColumn+j] == mNumbers[(i+1)*mColumn+j]){
                    return true;
                }
            }
        }
        return false;
    }

    /** reset the layout*/
    public void reset(){
        //
        mScore = 0; mMove = 0;
        for(int i = 0; i < mColumn*mColumn; i++){
            mNumbers[i] = 0;
            updateLabel(i);
        }
        generateRandom();
    }

    /** 空白位置随机产生一个2或4 */
    private void generateRandom(){
        Random random = new Random();
        int pos = random.nextInt(mColumn*mColumn);
        int value = Math.random() > 0.75 ? 4 : 2;

        while(mNumbers[pos] != 0){
            pos = random.nextInt(mColumn*mColumn);
        }
        mNumbers[pos] = value;
        updateLabel(pos);
    }

    /** 更新下标index对应的TextView */
    private void updateLabel(int index){
        if(mNumbers[index] == 0){
            mGameItems[index].setText("");
        }else{
            mGameItems[index].setText(String.format("%d", mNumbers[index]));
        }
        int colorIndex = getColorIndex(mNumbers[index]);
        mGameItems[index].setBackgroundColor(Color.parseColor(NUM_COLOR[colorIndex]));
    }

    /** 计算第i行j列的下标 */
    private int getNumberIndex(int action, int i, int j){
        int index = 0;
        switch (action){
            case ACTION_LEFT_SWIPE:
                index = i*mColumn + j;
                break;
            case ACTION_RIGHT_SWIPE:
                index = i*mColumn + (mColumn-1-j);
                break;
            case ACTION_UP_SWIPE:
                index = j*mColumn + i;
                break;
            case ACTION_DOWN_SWIPE:
                index = (mColumn-1-j)*mColumn + i;
                break;
            default:break;
        }
        return index;
    }

    /** Log2(n)*/
    private int getColorIndex(int n){
        int x = 0;
        if(n == 0)
            return 0;

        if((n & 0xffff) == 0){x += 16; n >>= 16;}
        if((n & 0xff) == 0){x += 8; n >>=8;}
        if((n & 0x0f) == 0){x += 4; n >>=4;}
        if((n & 0x03) == 0){x += 2; n >>=2;}
        if((n & 0x01) == 0){x += 1; n >>=1;}

        return (x > 15) ? 15 : x;
    }

    public interface OnGameChangeListener{
        void onScoreChanged(boolean changed, int score);
        void onMovement(int move);
        void onGameOver(int score);
    }
}
