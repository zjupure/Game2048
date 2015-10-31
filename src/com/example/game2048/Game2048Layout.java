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
	//����Item������n*n,Ĭ��Ϊ4
	private int mColumn = 4;
	//������е�Item
	private Game2048Item[] mGame2048Items;
	//Item����������ļ��gap
	private int mMargin = 10;
	//����padding
	private int mPadding;
	//����û�������
	private GestureDetector mGestureDetector;
	
	//����ȷ���Ƿ���Ҫ����һ���µ�ֵ
	private boolean isMergeHappen = true;
	private boolean isMoveHappen = true;
	//��¼����
	private int mScore;
	//�Ƿ��ǵ�һ�γ�ʼ������,ÿ��ֻ��Ҫ�ػ�Item
	private boolean once;
	//����һ��������
	Game2048Listener mGame2048Listener = null;	
	
	public Game2048Layout(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		mMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				mMargin, getResources().getDisplayMetrics());
		//����Layout���ڱ߾࣬����Ϊ�ĸ��ڱ߾��е���Сֵ
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
		
		//��ȡ�����������ı߳�
		int length = Math.min(getMeasuredHeight(), getMeasuredWidth());
		//����ÿ��Item�ı߳�
		int childWidth = (length - mPadding*2 - mMargin*(mColumn-1))/mColumn;
		
		if(!once){
			if(mGame2048Items == null){
				mGame2048Items = new Game2048Item[mColumn*mColumn];			
			}
			//����Item,���в���
			for(int i = 0; i < mGame2048Items.length; i++){
				Game2048Item item = new Game2048Item(getContext());
				
				mGame2048Items[i] = item;
				item.setId(i+1);
				RelativeLayout.LayoutParams lParams = new LayoutParams(childWidth, childWidth);
				
				//���ú���߾�,�������һ��
				if((i+1)%mColumn != 0){
					lParams.rightMargin = mMargin;
				}
				//������ǵ�һ��
				if(i%mColumn != 0){
					lParams.addRule(RelativeLayout.RIGHT_OF, mGame2048Items[i-1].getId());
				}
				//������ǵ�һ�� ,��������߾�,�����һ��
				if((i+1)>mColumn){
					lParams.topMargin = mMargin;
					lParams.addRule(RelativeLayout.BELOW, mGame2048Items[i-mColumn].getId());
				}
				addView(item, lParams);
			}
			//��ʼ������,���������
			generateNum();
		}
		once = true;
		
		setMeasuredDimension(length, length);
	}
	
	//�����¼�
	@Override
	public boolean onTouchEvent(MotionEvent event){
		mGestureDetector.onTouchEvent(event);  //���ݸ�GestureDetector��
		return true;
	}	
	
	
	//�����û�����,����������
	private void dealAction(ACTION action){
		//������
		for(int i = 0; i < mColumn; i++){
			//�½���ʱ�洢�ռ�,�����ƶ����ϲ�����
			List<Game2048Item> row = new ArrayList<Game2048Item>();
			//������
			for(int j = 0; j < mColumn; j++){
				int index = getIndexByAction(action,i,j);
				
				Game2048Item item = mGame2048Items[index];
				if(item.getNumber() != 0){
					row.add(item);
				}
			}
			
			//����
			for(int j = 0; j < mColumn && j < row.size(); j++){
				int index = getIndexByAction(action, i, j);
				Game2048Item item = mGame2048Items[index];
				
				//�������ƶ�����,�м��0û�м�������
				if(item.getNumber() != row.get(j).getNumber()){
					isMoveHappen = true;
				}
			}
			//�ϲ�
			mergeItem(row);
			//������ȥ
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
		//�����µ������
		generateNum();
	}
	
	//�����˶����������������
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
	
	//�ϲ���ͬ������
	private void mergeItem(List<Game2048Item> row){
		if(row.size() < 2)
			return;
		
		for(int j = 0; j < row.size()-1; j++){
			Game2048Item item1 = row.get(j);
			Game2048Item item2 = row.get(j+1);
			
			//��ͬ������
			if(item1.getNumber() == item2.getNumber()){
				isMergeHappen = true;  //�����˺ϲ�����
				
				int val = item1.getNumber() + item2.getNumber();
				item1.setNumber(val);
				
				//�ӷ�,���½���
				mScore += val;
				if(mGame2048Listener != null){
					mGame2048Listener.onScoreChange(mScore);
				}
				
				for(int k = j+1; k < row.size()-1; k++){
					row.get(k).setNumber(row.get(k+1).getNumber());
				}
				row.get(row.size()-1).setNumber(0);
				
				return;  //ֻ��ϲ�һ��,�˳�
			}
		}
	}
	
	//����һ�����λ�õ�2��4
	private void generateNum(){
		if(checkOver()){
			Log.e("TAG", "GAME OVER");
			if(mGame2048Listener != null){
				mGame2048Listener.onGameOver();
			}
			return;
		}
		
		//��û������
		if(!isFull()){
			//�������ƶ�����
			if(isMoveHappen || isMergeHappen){
				Random random = new Random();
				int next = random.nextInt(16);
				Game2048Item item = mGame2048Items[next];
				
				//ֱ���ӳ���λ��������Ϊ0Ϊֹ
				while(item.getNumber() != 0){
					next = random.nextInt(16);
					item = mGame2048Items[next];
				}
				//���ݸ��ʴ�С����4��2
				item.setNumber(Math.random()>0.75?4:2);
				
				//������
				isMergeHappen = isMoveHappen = false;
			}
		}
	}
	//��⵱ǰ����λ�ö�������,�����ڵĶ�û����ͬ������
	private boolean checkOver(){
		//����Ƿ�����λ�ö�������
		if(!isFull()){
			return false;
		}
		for(int i = 0; i < mColumn; i++){
			for(int j = 0; j < mColumn; j++){
				int index = i*mColumn + j;
				
				//��ǰItem
				Game2048Item item = mGame2048Items[index];
				//����ұ�
				if((index+1)%mColumn != 0){
					Log.e("TAG", "RIGHT");
					//�ұߵ�Item
					Game2048Item itemR = mGame2048Items[index+1];
					if(item.getNumber() == itemR.getNumber())
						return false;
				}
				//����±�
				if(index+mColumn < mColumn*mColumn){
					Log.e("TAG", "DOWN");
					Game2048Item itemD = mGame2048Items[index+mColumn];
					if(item.getNumber() == itemD.getNumber())
						return false;
				}
				//������
				if(index%mColumn != 0){
					Log.e("TAG", "LEFT");
					Game2048Item itemL = mGame2048Items[index-1];
					if(item.getNumber() == itemL.getNumber())
						return false;
				}
				//����ϱ�
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
	
	//����Ƿ���������
	private boolean isFull(){
		for(int i = 0; i < mGame2048Items.length; i++){
			if(mGame2048Items[i].getNumber() == 0)
				return false;
		}
		return true;
	}
	
	//���¿�ʼ
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
	
	//�����ڲ���,ʵ�ֶԻ������Ƶļ���
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
	
	//���ü���������
	public void setOnGame2048Listener(Game2048Listener mListener){
		mGame2048Listener = mListener;
	}
	
	//����һ���ӿ�
	public interface Game2048Listener{
		//���������˸ı�
		public void onScoreChange(int score);
		//��Ϸ������
		public void onGameOver();
	}
	
	//ö�ٷ���
	private enum ACTION{
		LEFT,RIGHT,UP,DOWN
	}
	
	//����Сֵ
	private int min(int... params){
		int min = params[0];
		
		for(int param:params){
			if(min > param)
				min = param;
		}
		
		return min;
	}
}
