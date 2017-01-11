package com.czp.pianotiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.czp.pianotiles.element.Block;
import com.czp.pianotiles.element.Score;
import com.czp.pianotiles.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by caizepeng on 17/1/11.
 */

public class PianoTilesView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final int LINE = 4;
    private static final int COL = 4;
    /**
     *   方块的滚动速度
     */
    private static final int REFRESH_DELAYED = 750;

    private SurfaceHolder mHolder;
    /**
     *
     * 与SurfaceHolder绑定的Canvas
     */
    private Canvas mCanvas;
    /**
     * 用于绘制的线程
     */
    private Thread mDrawThread;
    /**
     * 线程的控制开关
     */
    private boolean mIsRuning;
    /**
     * 保存所有方块
     */
    private List<Block> mBlockData;
    /**
     * 绘制方块的画笔
     */
    private Paint mBlockPaint;
    /**
     * 绘制得分的画笔
     */
    private Paint mScorePaint;
    /**
     * 分割线大小
     */
    private float mBorderSize;

    private float mBlockWidth;
    private float mBlockHeight;
    /**
     * 用于更新方块数据
     */
    private ScheduledExecutorService mScheduled;
    /**
     * 记录点击的行
     */
    private int mUnLine = LINE;
    /**
     * 得分
     */
    private Score mScore;

    private GameListener mGameListener;

    public void restart() {
        initBlock();
        // 开启线程
        mIsRuning = true;
        mDrawThread = new Thread(this);
        mDrawThread.start();
    }

    interface GameListener{
        public void gameEnd(String number);
    }
    public PianoTilesView(Context context) {
        this(context, null);
    }
    public PianoTilesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {



        initBlock();


        mBlockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScorePaint.setTextSize(DensityUtil.sp2px(getContext(),mScore.getTextSize()));
        mScorePaint.setColor(mScore.getTextColor());
        mScorePaint.setStrokeWidth(DensityUtil.px2dip(getContext(),mScore.getStrokeWidth()));

        // 开启线程
        mIsRuning = true;
        mDrawThread = new Thread(this);
        mDrawThread.start();
    }
    public void initBlock() {
        mScore = new Score();
        mUnLine = LINE;
        mIsRuning = true;
        mBlockData = new ArrayList<>();
        mBorderSize = DensityUtil.dip2px(getContext(), 1);
        mBlockWidth = (getMeasuredWidth() - mBorderSize * (COL - 1.0f)) / COL;
        mBlockHeight = (getMeasuredHeight() - mBorderSize * (LINE - 1.0f)) / LINE;
        Random random = new Random();
        for (int y = 0; y < LINE; y++) {
            int target = random.nextInt(4);
            for (int x = 0; x < COL; x++) {
                float left = x * mBlockWidth + x * mBorderSize;
                float right = left + mBlockWidth;
                float top = y * mBlockHeight + y * mBorderSize;
                float bottom = mBlockHeight + top;
                RectF rectF = new RectF(left, top, right, bottom);
                /**
                 * 初始化的时候,最后一行不留黑色方块
                 */
                int status = target == x && y != LINE - 1 ? Block.STATE_ACTIVE : Block.STATE_STANDARD;
                Block mBlock = new Block(mBlockHeight, mBlockWidth, mBorderSize, rectF);
                mBlock.setState(status);
                mBlockData.add(mBlock);
            }
        }
//        for(int i = 0; i < count;i++){
//            int index = i % COL;
//            int col = i / COL % LINE;
//            float left = index * mBlockWidth + index * mBorderSize;
//            float right = left + mBlockWidth;
//            float top = col * mBlockHeight + col * mBorderSize;
//            float bottom = mBlockHeight + top;
//            RectF rectF = new RectF(left,top,right,bottom);
//            Log.e("DEMO","index == "+index+"  target == "+target);
//            Block mBlock = new Block(mBlockHeight,mBlockWidth,color,mBorderSize,rectF);
//            mBlockData.add(mBlock);
//        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float rx = event.getX();
            float ry = event.getY();
            for (int i = 0; i < mBlockData.size(); i++) {
                Block block = mBlockData.get(i);
                int col = i / COL % LINE;
                if (col == (mUnLine - 1)) {
                    if (block.isClickRange(rx, ry)) {
                        mUnLine = col;
                        if (block.isActive()) {
                            block.toggleVisited();
                            mScore.update();
                            break;
                        }
                        if (block.isStandard()) {
                            block.toggleError();
                            endGame();
                            break;
                        }
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }
    public void startGame() {
        setZOrderOnTop(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                refreshBlock();
            }
        };
        mScheduled = Executors.newScheduledThreadPool(1);
        mScheduled.scheduleAtFixedRate(task, 2000, REFRESH_DELAYED, TimeUnit.MILLISECONDS);
    }
    /**
     * 游戏结束
     */
    public void endGame() {
        mScheduled.shutdown();
        if(mGameListener!=null){
            mGameListener.gameEnd(mScore.getNumber());
        }
        //1秒后再关闭绘制线程
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsRuning = false;
            }
        },1000);
    }

    /**
     * 刷新面板
     * <p>
     * 本来位置的计算是放到这里的,但是移动的时候位置要重新计算,有些麻烦
     */
    private void refreshBlock() {
        Random random = new Random();
        int target = random.nextInt(COL);
        boolean isEnd = false;
        for (int i = 0; i < COL; i++) {
            Block block = mBlockData.remove(mBlockData.size() - 1);
            if (block.isActive()) {
                isEnd = true;
            }
        }
        for (int i = 0; i < COL; i++) {
            int statue = target == i ? Block.STATE_ACTIVE : Block.STATE_STANDARD;
            Block block = new Block(mBlockHeight, mBlockWidth, mBorderSize, null);
            block.setState(statue);
            mBlockData.add(0, block);
        }
        mUnLine++;
        mUnLine = Math.min(LINE, mUnLine);
        if (isEnd) {
            endGame();
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 通知关闭线程
        mIsRuning = false;
        mScheduled.shutdown();
        if(mGameListener!=null){
            mGameListener.gameEnd(mScore.getNumber());
        }
    }
    @Override
    public void run() {
        while (mIsRuning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            try {
                if (end - start < 10) {
                    Thread.sleep(10 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.BLACK);
            if (mCanvas != null) {
                for (int i = 0; i < mBlockData.size(); i++) {
                    Block block = mBlockData.get(i);
                    int index = i % COL;
                    int col = i / COL % LINE;
                    float left = index * block.getWidth() + index * block.getBorderSize();
                    float right = left + block.getWidth();
                    float top = col * block.getHeight() + col * block.getBorderSize();
                    float bottom = block.getHeight() + top;
                    RectF rectF = new RectF(left, top, right, bottom);
                    block.setRectF(rectF);
                    mBlockPaint.setColor(block.getBgColor());
                    float xText = getMeasuredWidth()/2 - mScorePaint.measureText(mScore.getNumber())/2;
                    mCanvas.drawText(mScore.getNumber(),xText,150,mScorePaint);
                    mCanvas.drawRect(block.getRectF(), mBlockPaint);
                }
            }
        } catch (Exception e) {

        } finally {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mScheduled!=null){
            mScheduled.shutdown();
        }
    }
    public GameListener getGameListener() {
        return mGameListener;
    }
    public void setGameListener(GameListener gameListener) {
        mGameListener = gameListener;
    }
}