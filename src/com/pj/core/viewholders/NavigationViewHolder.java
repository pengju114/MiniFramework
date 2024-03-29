package com.pj.core.viewholders;

import java.util.ArrayList;
import java.util.HashSet;

import com.pj.core.BaseActivity;
import com.pj.core.managers.LogManager;
import com.pj.core.transition.AnimationFactory;
import com.pj.core.utilities.DimensionUtility;

import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 导航视图持有器
 * pj-framework-1.2
 * @author lzw
 * 2014年3月23日 下午4:13:53
 * email: pengju114@163.com
 */
public class NavigationViewHolder extends ViewHolder{
	private int navigationBarID;
	private int navigationContainerID; 
	
	private RelativeLayout navigationBarLayout;
	
	private boolean animating;
	private ArrayList<ViewHolder> pageHolders;
	private HashSet<View> dirtySet;

	private Interpolator  alphaInInterpolator  = new AccelerateInterpolator(1.4f);
	private Interpolator  alphaOutInterpolator = new DecelerateInterpolator(2.0f);
	
	private Interpolator  contentInterpolator  = new AccelerateDecelerateInterpolator();
	
	
	private int animationCount;
	private NavigationAnimationListener animationCounterListener=new NavigationAnimationListener();
	private View.OnClickListener gobackClickListener=new View.OnClickListener() {
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			NavigationViewHolder.this.pop(true);
		}
	};

	public NavigationViewHolder(ViewHolder firstViewHolder) {
		super(firstViewHolder.getActivity());
		// TODO Auto-generated constructor stub
		setLayoutResource(com.pj.core.R.layout.c_navigation_view);
		
		firstViewHolder.setDuplicateParentState(false);
		push(firstViewHolder, false);
	}
	
	
	public RelativeLayout getNavigationBarView() {
		// TODO Auto-generated method stub
		return navigationBarLayout;
	}
	
	public FrameLayout getNavigationContainerView() {
		return (FrameLayout) find(navigationContainerID);
	}
	
	public ViewHolder getCurrentTopHolder(){
		return getTop();
	}
	
	
	protected void initialize(BaseActivity activity, View view) {
		// TODO Auto-generated method stub
		navigationBarID=com.pj.core.R.id.c_navigation_bar;
		navigationContainerID=com.pj.core.R.id.c_navigation_container;
		
		animating=false;
		pageHolders=new ArrayList<ViewHolder>(8);
		dirtySet=new HashSet<View>();
		
		super.initialize(activity, view);
	}

	
	protected void onApplyView(View view) {
		// TODO Auto-generated method stub
		navigationBarLayout=(RelativeLayout) find(navigationBarID);
	}
	
	
	public void postAnimation(View view,Animation animation,boolean removedWhenFinish){
		if (view!=null && animation!=null) {
			if (removedWhenFinish) {
				dirtySet.add(view);
			}
			view.clearAnimation();
			enhanceAnimation(animation, view);
			view.startAnimation(animation);
		}
	}
	
	private ViewHolder getTop() {
		// TODO Auto-generated method stub
		if (pageHolders.size()>0) {
			return pageHolders.get(pageHolders.size()-1);
		}
		return null;
	}
	
	
	public boolean push(ViewHolder holder,boolean animate) {
		if (animating || pageHolders.contains(holder)) {
			return false;
		}
		onTransitionStart();
		
		holder.navigationViewHolder=this;
		
		NavigationBar bar=holder.getNavigationBar();
		View leftView=bar.getNavigationLeftView();
		ViewHolder topViewHolder=getTop();
		pageHolders.add(holder);
		
		if (leftView==null && topViewHolder!=null) {
			Button back=bar.getDefaultGobackButton();
			back.setText(topViewHolder.getNavigationBar().getTitle());
			back.setOnClickListener(gobackClickListener);
			leftView=back;
		}
		
		View centerView=bar.getNavigationCenterView();
		if (centerView==null) {
			centerView=bar.getTitleView();
		}
		
		View rightView=bar.getNavigationRightView();
		
		if (!animate) {
			navigationBarLayout.removeAllViews();
		}
		
		if (leftView!=null) {
			RelativeLayout.LayoutParams leftItemParams=bar.getNavigationLeftItemLayoutParams();
			//先度量试图尺寸，否则getMeasuredWidth会返回0
			leftView.measure(leftItemParams.width, leftItemParams.height);
			forceAddView(navigationBarLayout, leftView, leftItemParams);
		}
		
		RelativeLayout.LayoutParams centerItemParams=bar.getNavigationCenterItemLayoutParams();
		centerView.measure(centerItemParams.width, centerItemParams.height);
		forceAddView(navigationBarLayout, centerView, centerItemParams);
		
		if (rightView!=null) {
			
			RelativeLayout.LayoutParams rightItemParams=bar.getNavigationRightItemLayoutParams();
			forceAddView(navigationBarLayout, rightView, rightItemParams);
		}
		
		
		if (animate) {
			
			if (topViewHolder!=null) {
				NavigationBar preBar=topViewHolder.getNavigationBar();
				View preLeftItemView=preBar.getNavigationLeftView();
				if (preLeftItemView==null && preBar.defaultGobackButton!=null) {
					preLeftItemView=preBar.getDefaultGobackButton();
				}
				
				View preCenterView=preBar.getNavigationCenterView();
				if (preCenterView==null) {
					preCenterView=preBar.getTitleView();
				}
				
				View preRightView=preBar.getNavigationRightView();
				
				
				if (preLeftItemView!=null) {
					postAnimation(preLeftItemView, getLeftItemPushoutAnimation(preLeftItemView), true);
				}
				
				if (preCenterView!=null) {
					postAnimation(preCenterView, getCenterItemPushoutAnimation(preCenterView), true);
				}
				
				if (preRightView!=null) {
					postAnimation(preRightView, getRightItemPushoutAnimation(preRightView), true);
				}
			}
			
			if (leftView!=null) {
				postAnimation(leftView, getLeftItemPushAnimation(leftView), false);
			}
			
			//centerView是肯定有的
			postAnimation(centerView, getCenterItemPushAnimation(centerView), false);
			
			if (rightView!=null) {
				postAnimation(rightView, getRightItemPushAnimation(rightView), false);
			}
		}
		
		if (topViewHolder!=null) {
			if (animate) {
				removeChild(getContentPushoutAnimation(topViewHolder), topViewHolder);
			}else {
				removeChild(topViewHolder);
			}
		}
		if (animate) {
			addChild(getContentPushAnimation(holder),navigationContainerID, holder);
		}else {
			addChild(navigationContainerID,holder);
			onTransitionEnd();
		}
		
		return true;
	}
	
	
	protected int getNavigationBarWidth(){
		return navigationBarLayout.getMeasuredWidth();
	}

	private Animation getLeftItemPushAnimation(View leftView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		alphaAnimation.setInterpolator(alphaInInterpolator);
		
		int Xtype=Animation.ABSOLUTE;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float fromXValue=(getNavigationBarWidth()-leftView.getMeasuredWidth())*0.5f-navigationBarLayout.getPaddingLeft();
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, 0, Ytype, 0, Ytype, 0);
		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(false);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	
	private Animation getCenterItemPushAnimation(View centerView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
//		alphaAnimation.setInterpolator(alphaInInterpolator);
		
		int Xtype=Animation.RELATIVE_TO_PARENT;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float fromXValue=0.6f;
		float toXValue=0f;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype, 0);
//		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	
	private Animation getRightItemPushAnimation(View rightView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		initAnimations(alphaAnimation);
		return alphaAnimation;
	}
	
	private Animation getContentPushAnimation(ViewHolder holder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_RIGHT_IN));
	}


	private Animation getContentPushoutAnimation(ViewHolder topViewHolder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_LEFT_OUT));
	}
	
	private Animation getContentPopinAnimation(ViewHolder holder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_LEFT_IN));
	}


	private Animation getContentPopoutAnimation(ViewHolder topViewHolder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_RIGHT_OUT));
	}

	
	private Animation getLeftItemPushoutAnimation(View leftItemView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
		alphaAnimation.setInterpolator(alphaOutInterpolator);
		
		int Xtype=Animation.ABSOLUTE;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float toXValue=navigationBarLayout.getPaddingLeft()-leftItemView.getMeasuredWidth()-DimensionUtility.dp2px(30);;
		float fromXValue=0;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype, 0);
		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(false);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}

	private Animation getRightItemPushoutAnimation(View preRightView) {
		// TODO Auto-generated method stub
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_F_FADE_OUT));
	}

	private Animation getCenterItemPushoutAnimation(View centerView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
		alphaAnimation.setInterpolator(alphaOutInterpolator);
		
		int Xtype=Animation.ABSOLUTE;
		int Ytype=Animation.RELATIVE_TO_SELF;
		
		float toXValue=navigationBarLayout.getPaddingLeft()-(getNavigationBarWidth()-centerView.getMeasuredWidth())*0.5f;
		float fromXValue=0;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype,0);
		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	

	public boolean pop(boolean animate){
		if (animating || pageHolders.size()<2) {
			return false;
		}
		onTransitionStart();
		
		ViewHolder currTopHolder=getTop();
		ViewHolder gettingShowHolder=pageHolders.get(pageHolders.size()-2);
		pageHolders.remove(pageHolders.size()-1);
				
		View gsLeftItem=gettingShowHolder.getNavigationBar().getNavigationLeftView();
		if (gsLeftItem==null && gettingShowHolder.getNavigationBar().defaultGobackButton!=null) {
			gsLeftItem=gettingShowHolder.getNavigationBar().getDefaultGobackButton();
		}
		
		View gsCenterItem=gettingShowHolder.getNavigationBar().getNavigationCenterView();
		if (gsCenterItem==null) {
			gsCenterItem=gettingShowHolder.getNavigationBar().getTitleView();
		}
		
		View gsRightItem=gettingShowHolder.getNavigationBar().getNavigationRightView();
		
		if (!animate) {
			navigationBarLayout.removeAllViews();
		}
		
		if (gsLeftItem!=null) {
			forceAddView(navigationBarLayout, gsLeftItem, gettingShowHolder.getNavigationBar().getNavigationLeftItemLayoutParams());
		}
		forceAddView(navigationBarLayout, gsCenterItem, gettingShowHolder.getNavigationBar().getNavigationCenterItemLayoutParams());
		
		if (gsRightItem!=null) {
			forceAddView(navigationBarLayout, gsRightItem, gettingShowHolder.getNavigationBar().getNavigationRightItemLayoutParams());
		}
		
		if (animate) {
			View currLeftItem=currTopHolder.getNavigationBar().getNavigationLeftView();
			if (currLeftItem==null && currTopHolder.getNavigationBar().defaultGobackButton!=null) {
				currLeftItem=currTopHolder.getNavigationBar().getDefaultGobackButton();
			}
			
			View currCenterItem=currTopHolder.getNavigationBar().getNavigationCenterView();
			if (currCenterItem==null) {
				currCenterItem=currTopHolder.getNavigationBar().getTitleView();
			}
			
			View currRightItem=currTopHolder.getNavigationBar().getNavigationRightView();
			
			if (currLeftItem!=null) {
				postAnimation(currLeftItem, getLeftItemPopoutAnimation(currLeftItem), true);
			}
			
			if (currCenterItem!=null) {
				postAnimation(currCenterItem, getCenterItemPopoutAnimation(currCenterItem), true);
			}
			
			if (currRightItem!=null) {
				postAnimation(currRightItem, getRightItemPopoutAnimation(currRightItem), true);
			}
			
			//上一个holder
			if (gsLeftItem!=null) {
				postAnimation(gsLeftItem, getLeftItemPopinAnimation(gsLeftItem), false);
			}
			
			if (gsCenterItem!=null) {
				postAnimation(gsCenterItem, getCenterItemPopinAnimation(gsCenterItem), false);
			}
			
			if (gsRightItem!=null) {
				postAnimation(gsRightItem, getRightItemPopinAnimation(gsRightItem), false);
			}
		}
		//内容部分
		if (animate) {
			removeChild(getContentPopoutAnimation(currTopHolder), currTopHolder);
			addChild(getContentPopinAnimation(gettingShowHolder), navigationContainerID, gettingShowHolder);
		}else {
			removeChild(currTopHolder);
			addChild(navigationContainerID, gettingShowHolder);
			onTransitionEnd();
		}
		
		currTopHolder.navigationViewHolder=null;
		return true;
	}
	
	public boolean pop(int count,boolean animate) {
		if (animating) {
			return false;
		}
		
		count--;
		if (count>=(pageHolders.size()-1)) {
			count=pageHolders.size()-2;
		}
		
		for (int i = 0; i < count; i++) {
			ViewHolder holder=pageHolders.remove(pageHolders.size()-2);
			holder.navigationViewHolder=null;
		}
		
		return pop(animate);
	}
	
	public boolean popTo(ViewHolder holder,boolean animate) {
		if (animating) {
			return false;
		}
		
		int index=pageHolders.indexOf(holder);
		if (index<0) {
			return false;
		}
		return popToIndex(index, animate);
	}
	
	public boolean popTo(Class<? extends ViewHolder> clazz,boolean animate) {
		if (animating) {
			return false;
		}
		for (int i = 0; i < pageHolders.size(); i++) {
			ViewHolder holder=pageHolders.get(i);
			if (holder.getClass().equals(clazz)) {
				return popToIndex(i, animate);
			}
		}
		return false;
	}
	
	public boolean setHolder(ViewHolder holder,boolean animate){
		holder.navigationViewHolder=this;
		pageHolders.add(0, holder);
		return popToIndex(0, animate);
	}
	
	
	private boolean popToIndex(int index,boolean animate){
		int pos=pageHolders.size()-2;
		while (pos>index) {
			ViewHolder holder=pageHolders.remove(pos--);
			holder.navigationViewHolder.navigationViewHolder=null;
		}
		return pop(animate);
	}
	
	
	protected Animation getLeftItemPopoutAnimation(View leftView){
		Animation alphaAnimation=new AlphaAnimation(1, 0);
		
		int xtype=Animation.ABSOLUTE;
		int ytype=Animation.RELATIVE_TO_SELF;
		
		float toXValue=(getNavigationBarWidth()-leftView.getMeasuredWidth())*0.5f-navigationBarLayout.getPaddingLeft();
		Animation transAnimation=new TranslateAnimation(xtype, 0, xtype, toXValue, ytype, 0, ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(transAnimation);
		
		return initAnimations(animationSet);
	}
	
	protected Animation getCenterItemPopoutAnimation(View centerItem){
		AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
		int Xtype=Animation.RELATIVE_TO_PARENT;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float fromXValue=0f;
		float toXValue=0.6f;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype, 0);
		
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	
	protected Animation getRightItemPopoutAnimation(View rightItem){
		return initAnimations(new AlphaAnimation(1, 0));
	}
	
	protected Animation getLeftItemPopinAnimation(View leftItem){
		if (leftItem.getMeasuredWidth()==0 && leftItem.getLayoutParams()!=null) {
			leftItem.measure(leftItem.getLayoutParams().width, leftItem.getLayoutParams().height);
		}
		
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		
		int xtype=Animation.ABSOLUTE;
		int ytype=Animation.RELATIVE_TO_SELF;
		
		float fromXValue=navigationBarLayout.getPaddingLeft()-leftItem.getMeasuredWidth()-DimensionUtility.dp2px(30);
		float toXValue  =0;
		
		TranslateAnimation translateAnimation=new TranslateAnimation(xtype, fromXValue, xtype, toXValue, ytype, 0, ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		
		return initAnimations(animationSet);
	}
	
	protected Animation getCenterItemPopinAnimation(View centerView){
		if (centerView.getMeasuredWidth()==0 && centerView.getLayoutParams()!=null) {
			centerView.measure(centerView.getLayoutParams().width, centerView.getLayoutParams().height);
		}
		
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		
		int xtype=Animation.ABSOLUTE;
		int ytype=Animation.RELATIVE_TO_SELF;
		
		float fromXValue=navigationBarLayout.getPaddingLeft()-(getNavigationBarWidth()-centerView.getMeasuredWidth())*0.5f;
		float toXValue  =0;
		
		TranslateAnimation translateAnimation=new TranslateAnimation(xtype, fromXValue, xtype, toXValue, ytype, 0, ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		
		return initAnimations(animationSet);
	}
	
	protected Animation getRightItemPopinAnimation(View rightItem){
		return initAnimations(new AlphaAnimation(0, 1));
	}
	
	
	protected Animation initAnimations(Animation animation) {
		// TODO Auto-generated method stub
		animation.setAnimationListener(animationCounterListener);
		return super.initAnimations(animation);
	}
	
	private void onTransitionStart(){
		animating=true;
		LogManager.i(getClass().getSimpleName(), "onTransitionStart");
	}
	
	private void onTransitionEnd(){
		LogManager.i(getClass().getSimpleName(), dirtySet);
		for (View view : dirtySet) {
			if (view!=null) {
				view.clearAnimation();
				navigationBarLayout.removeView(view);
			}
		}
		dirtySet.clear();
		animating=false;
		
		if (pageHolders.size()==1) {
			getCurrentTopHolder().setDuplicateParentState(true);
		}
		LogManager.i(getClass().getSimpleName(), "onTransitionEnd");
	}
	
	
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		 boolean h=super.onKeyDown(keyCode, event);
		
		if (!h && keyCode==KeyEvent.KEYCODE_BACK) {
			return animating?true:pop(true);
		}
		return h;
	}
	
	private class NavigationAnimationListener implements AnimationListener{
		
		public NavigationAnimationListener(){
		}

		
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			NavigationViewHolder.this.animationCount++;
		}

		
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			NavigationViewHolder.this.animationCount--;
			if (NavigationViewHolder.this.animationCount<=0) {
				onTransitionEnd();
			}
		}

		
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
	}
}