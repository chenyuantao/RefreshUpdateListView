package com.cyt.mylistview;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

/**
 * 可跟随手指的进度条 
 * @author Tao
 */
public class ProgressBar extends View {

	private int width;
	private int height;
	private int progress; // 0-100
	private boolean isDoing;
	private Thread thread;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				// 通知控件重绘
				invalidate();
			}
		}
	};
	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			try {
				while (isDoing) {
					Thread.sleep(10);
					progress += 1;
					if (progress > 100) {
						progress = 0;
					}
					handler.sendEmptyMessage(1);

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	};

	public ProgressBar(Context context) {
		super(context);
		init(context);
	}

	public ProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ProgressBar(Context context, AttributeSet attrs,
					   int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		progress = 0;
		isDoing = false;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		this.invalidate();
	}

	public void startAnimation() {
		if (isDoing) {
			return;
		}
		progress = 0;
		if (thread == null) {
			thread = new Thread(runnable);
			thread.start();
		}
		isDoing = true;
	}

	public void stopAnimation() {
		if (!isDoing) {
			return;
		}
		progress = 0;
		if (thread != null) {
			thread = null;
		}
		isDoing = false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 初始化画笔
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// 设置进度条色
		paint.setARGB(255,39, 188, 136);
		if (isDoing) {
			RectF rectProgress = new RectF(-width / 3 + width * 4 * progress
					/ 300, 0, width * 4 * progress / 300, height);
			// 画移动进度条
			canvas.drawRect(rectProgress, paint);
		} else {
			// 左进度条方框
			RectF rectLeft = new RectF(0, 0, width / 200 * progress, height);
			// 右进度条方框
			RectF rectRight = new RectF(width - width / 200 * progress, 0,
					width, height);
			// 画左进度条
			canvas.drawRect(rectLeft, paint);
			// 画右进度条
			canvas.drawRect(rectRight, paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		if (widthSpecMode == MeasureSpec.EXACTLY
				|| widthSpecMode == MeasureSpec.AT_MOST) {
			width = widthSpecSize;
		} else {
			width = 0;
		}
		if (heightSpecMode == MeasureSpec.AT_MOST
				|| heightSpecMode == MeasureSpec.UNSPECIFIED) {
			height = dipToPx(15);
		} else {
			height = heightSpecSize;
		}
		setMeasuredDimension(width, height);
	}

	private int dipToPx(int dip) {
		float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

}
