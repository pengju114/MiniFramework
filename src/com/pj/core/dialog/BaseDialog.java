package com.pj.core.dialog;

import com.pj.core.datamodel.DataWrapper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

public abstract class BaseDialog extends Dialog implements CacheableDialog, DialogInterface.OnCancelListener,DialogInterface.OnDismissListener{
	private DataWrapper dataWrapper;
	private DialogListener    listener;
	
	private OnCancelListener outsideCancelListener;
	private OnDismissListener outsideDismissListener;
	
	private int requestCode;

	public BaseDialog(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}
	public BaseDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	protected void init() {
		// TODO Auto-generated method stub
		
		Window window=getWindow();
		ColorDrawable drawable=new ColorDrawable(Color.TRANSPARENT);
		window.setBackgroundDrawable(drawable);
		
		dataWrapper=new DataWrapper();
		setCancelable(true);
		setCanceledOnTouchOutside(false);
		
		setOnCancelListener(this);
		setOnDismissListener(this);
	}
	
	
	public void setObject(String key, Object value) {
		// TODO Auto-generated method stub
		dataWrapper.setObject(key, value);
	}
	
	public Object getObject(String key) {
		// TODO Auto-generated method stub
		return dataWrapper.get(key);
	}
	
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	public int getRequestCode() {
		return requestCode;
	}

	
	public void setDialogListener(DialogListener listener) {
		// TODO Auto-generated method stub
		this.listener=listener;
	}
	
	public void setDialogData(DataWrapper wrapper) {
		// TODO Auto-generated method stub
		setTitle(wrapper.getString(KEY_TITLE));
		setMessage(wrapper.getString(KEY_MESSAGE));
		setObject(KEY_DATA, wrapper.getObject(KEY_DATA));
		setDialogListener((DialogListener) wrapper.getObject(KEY_LISTENER));
		setRequestCode(wrapper.getInt(KEY_REQUEST_ID).intValue());
	}
	
	protected void onDialogClose(int trigger) {
		if (listener!=null) {
			listener.onDialogClose(requestCode, this, trigger, getObject(KEY_DATA));
		}
	}
	
	protected void reset() {
		// TODO Auto-generated method stub
		dataWrapper.clear();
		listener=null;
		setDialogData(dataWrapper);
	}
	
	
	
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		onDialogClose(DialogListener.BTN_UNKNOWN);
		if (outsideCancelListener!=null) {
			outsideCancelListener.onCancel(dialog);
		}
	}

	public void setOutsideCancelListener(OnCancelListener outsideCancelListener) {
		this.outsideCancelListener = outsideCancelListener;
	}
	
	
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		if (outsideDismissListener!=null) {
			outsideDismissListener.onDismiss(dialog);
		}
	}
	public void setOutsideDismissListener(
			OnDismissListener outsideDismissListener) {
		this.outsideDismissListener = outsideDismissListener;
	}
}
