package com.example.game2048;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.R.integer;
import android.content.Context;
import android.inputmethodservice.Keyboard.Row;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class Game2048Layout extends RelativeLayout {
	//设置Item的数量n*n,默认为4
	private int mColumn = 4;
	//存放所有的Item
	private Game2048Item[] mGame2048Items;
	//Item横向与纵向的间距gap
	private int mMargin = 10;
	//面板的padding
	private int mPadding;
	//检测用户的手势
	private GestureDetector mGestureDetector;
	
	//用于确认是否需要生成一个新的值
	private boolean isMergeHappen = true;
	private boolean isMoveHappen = true;
	//记录分数
	private int mScore;
	//是否是第一次初始化界面,每次只需要重绘Item
	private boolean once;
	//设置一个监听器
	Game2048Listener mGame2048Listener = null;	
	
	public Game2048Layout(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		mMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				mMargin, getResources().getDisplayMetrics());
		//设置Layout的内边距，设置为四个内边距中的最小值
		mPadding = min(getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
		
		mGestureDetector = new GestureDetector(context, new MyGestureDetector());
	}
	
	public Game2048Layout(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		this(context, attrs, 0);
	}
	
	public Game2048Layout(Context context) {
		// TODO Auto-generated constructor stub
		this(context, null);
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		//获取正方形容器的边长
		int length = Math.min(getMeasuredHeight(), getMeasuredWidth());
		//计算每个Item的边长
		int childWidth = (length - mPadding*2 - mMargin*(mColumn-1))/mColumn;
		
		if(!once){
			if(mGame2048Items == null){
				mGame2048Items = new Game2048Item[mColumn*mColumn];			
			}
			//放置Item,进行布局
			for(int i = 0; i < mGame2048Items.length; i++){
				Game2048Item item = new Game2048Item(getContext());
				
				mGame2048Items[i] = item;
				item.setId(i+1);
				RelativeLayout.LayoutParams lParams = new LayoutParams(childWidth, childWidth);
				
				//设置横向边距,不是最后一列
				if((i+1)%mColumn != 0){
					lParams.rightMargin = mMargin;
				}
				//如果不是第一列
				if(i%mColumn != 0){
					lParams.addRule(RelativeLayout.RIGHT_OF, mGame2048Items[i-1].getId());
				}
				//如果不是第一行 ,设置纵向边距,非最后一行
				if((i+1)>mColumn){
					lParams.topMargin = mMargin;
					lParams.addRule(RelativeLayout.BELOW, mGame2048Items[i-mColumn].getId());
				}
				addView(item, lParams);
			}
			//初始化界面,产生随机数
			generateNum();
		}
		once = true;
		
		setMeasuredDimension(length, length);
	}
	
	//触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event){
		mGestureDetector.onTouchEvent(event);  //传递给GestureDetector类
		return true;
	}	
	
	
	//根据用户手势,处理滑动操作
	private void dealAction(ACTION action){
		//遍历行
		for(int i = 0; i < mColumn; i++){
			//新建临时存储空间,进行移动及合并操作
			List<Game2048Item> row = new ArrayList<Game2048Item>();
			//遍历列
			for(int j = 0; j < mColumn; j++){
				int index = getIndexByAction(action,i,j);
				
				Game2048Item item = mGame2048Items[index];
				if(item.getNumber() != 0){
					row.add(item);
				}
			}
			
			//处理
			for(int j = 0; j < mColumn && j < row.size(); j++){
				int index = getIndexByAction(action, i, j);
				Game2048Item item = mGame2048Items[index];
				
				//发生了移动操作,中间的0没有加入链表
				if(item.getNumber() != row.get(j).getNumber()){
					isMoveHappen = true;
				}
			}
			//合并
			mergeItem(row);
			//拷贝回去
			for(int j = 0; j < mColumn; j++){
				int index = getIndexByAction(action, i, j);
				if(row.size() > j){
					mGame2048Items[index].setNumber(row.get(j).getNumber());
				}
				else{
					mGame2048Items[index].setNumber(0);
				}
			}
		}
		//生成新的随机数
		generateNum();
	}
	
	//根据运动方向获得数组的索引
	private int getIndexByAction(ACTION action, int i, int j){
		int index = -1;
		switch (action) {
		case LEFT:
			index = i*mColumn+j;
			break;
		case RIGHT:
			index = i*mColumn+mColumn-j-1;
			break;
		case UP:
			index = i+j*mColumn;
			break;
		case DOWN:
			index = i+(mColumn-1-j)*mColumn;
			break;
		default:
			break;
		}
		return index;
	}
	
	//合并相同的数字
	private void mergeItem(List<Game2048Item> row){
		if(row.size() < 2)
			return;
		
		for(int j = 0; j < row.size()-1; j++){
			Game2048Item item1 = row.get(j);
			Game2048Item item2 = row.get(j+1);
			
			//相同的数字
			if(item1.getNumber() == item2.getNumber()){
				isMergeHappen = true;  //发生了合并操作
				
				int val = item1.getNumber() + item2.getNumber();
				item1.setNumber(val);
				
				//加分,更新界面
				mScore += val;
				if(mGame2048Listener != null){
					mGame2048Listener.onScoreChange(mScore);
				}
				
				for(int k = j+1; k < row.size()-1; k++){
					row.get(k).setNumber(row.get(k+1).getNumber());
				}
				row.get(row.size()-1).setNumber(0);
				
				return;  //只会合并一次,退出
			}
		}
	}
	
	//产生一个随机位置的2或4
	private void generateNum(){
		if(checkOver()){
			Log.e("TAG", "GAME OVER");
			if(mGame2048Listener != null){
				mGame2048Listener.onGameOver();
			}
			return;
		}
		
		//还没有填满
		if(!isFull()){
			//发生了移动操作
			if(isMoveHappen || isMergeHappen){
				Random random = new Random();
				int next = random.nextInt(16);
				Game2048Item item = mGame2048Items[next];
				
				//直到扔出的位置上数字为0为止
				while(item.getNumber() != 0){
					next = random.nextInt(16);
					item = mGame2048Items[next];
				}
				//根据概率大小生成4或2
				item.setNumber(Math.random()>0.75?4:2);
				
				//清除标记
				isMergeHappen = isMoveHappen = false;
			}
		}
	}
	//检测当前所有位置都有数字,且相邻的都没有相同的数字
	private boolean checkOver(){
		//检测是否所有位置都有数字
		if(!isFull()){
			return false;
		}
		for(int i = 0; i < mColumn; i++){
			for(int j = 0; j < mColumn; j++){
				int index = i*mColumn + j;
				
				//当前Item
				Game2048Item item = mGame2048Items[index];
				//检测右边
				if((index+1)%mColumn != 0){
					Log.e("TAG", "RIGHT");
					//右边的Item
					Game2048Item itemR = mGame2048Items[index+1];
					if(item.getNumber() == itemR.getNumber())
						return false;
				}
				//检测下边
				if(index+mColumn < mColumn*mColumn){
					Log.e("TAG", "DOWN");
					Game2048Item itemD = mGame2048Items[index+mColumn];
					if(item.getNumber() == itemD.getNumber())
						return false;
				}
				//检测左边
				if(index%mColumn != 0){
					Log.e("TAG", "LEFT");
					Game2048Item itemL = mGame2048Items[index-1];
					if(item.getNumber() == itemL.getNumber())
						return false;
				}
				//检测上边
				if(index+1 > mColumn){
					Log.e("TAG", "UP");
					Game2048Item itemU = mGame2048Items[index-mColumn];
					if(item.getNumber() == itemU.getNumber())
						return false;
				}
			}
		}
		
		return true;
	}
	
	//检测是否填满数字
	private boolean isFull(){
		for(int i = 0; i < mGame2048Items.length; i++){
			if(mGame2048Items[i].getNumber() == 0)
				return false;
		}
		return true;
	}
	
	//重新开始
	public void reStart(){
		for(Game2048Item item : mGame2048Items)
			item.setNumber(0);
		
		mScore = 0;
		if(mGame2048Listener != null){
			mGame2048Listener.onScoreChange(mScore);
		}
		isMoveHappen = isMergeHappen = true;
		generateNum();
	}
	
	//定义内部类,实现对滑动手势的监听
	class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
		final int FLING_MIN_DISTANCE = 20;
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY){
			float x = e2.getX() - e1.getX();
			float y = e2.getY() - e1.getY();
			
			if(x > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)){
				dealAction(ACTION.RIGHT);
			}
			else if(x < -FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)){
				dealAction(ACTION.LEFT);
			}
			else if(y > FLING_MIN_DISTANCE && Math.abs(velocityX) < Math.abs(velocityY)){
				dealAction(ACTION.DOWN);
			}
			else if(y < -FLING_MIN_DISTANCE && Math.abs(velocityX) < Math.abs(velocityY)){
				dealAction(ACTION.UP);
			}
			
			return true;
		}
	}
	
	//设置监听器方法
	public void setOnGame2048Listener(Game2048Listener mListener){
		mGame2048Listener = mListener;
	}
	
	//定义一个接口
	public interface Game2048Listener{
		//分数发生了改变
		public void onScoreChange(int score);
		//游戏结束了
		public void onGameOver();
	}
	
	//枚举方向
	private enum ACTION{
		LEFT,RIGHT,UP,DOWN
	}
	
	//求最小值
	private int min(int... params){
		int min = params[0];
		
		for(int param:params){
			if(min > param)
				min = param;
		}
		
		return min;
	}
}
