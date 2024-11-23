package com.fpoly.nhom2.mifoodapp.implement;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    int buttonWidth;
    RecyclerView recyclerView;
    ArrayList<MyButton> buttonList;
    GestureDetector gestureDetector;
    private int swipePositon = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<MyButton>> buttonBuffer;
    private Queue<Integer> removeQueue;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            for (MyButton button : buttonList) {
                if (button.onClick(e.getX(), e.getY())) {
                    return true;
                }
            }
            return false;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (swipePositon < 0) return false;
            Point point = new Point((int) event.getRawX(), (int) event.getRawY());
            RecyclerView.ViewHolder swipeViewHolder = recyclerView.findViewHolderForAdapterPosition(swipePositon);
            if (swipeViewHolder == null) return false;
            View swipedItem = swipeViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y) {
                    gestureDetector.onTouchEvent(event);
                } else {
                    removeQueue.add(swipePositon);
                    swipePositon = -1;
                    recoverSwipeItem();
                }
            }
            return false; // Changed to false to allow swiping back and forth
        }
    };

    public SwipeToDeleteCallback(Context context, RecyclerView recyclerView, int buttonWidth) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
        this.buttonList = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.buttonBuffer = new HashMap<>();
        this.recyclerView.setOnTouchListener(onTouchListener);
        this.buttonWidth = buttonWidth;

        removeQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer integer) {
                if (contains(integer)) {
                    return false;
                } else {
                    return super.add(integer);
                }
            }
        };
        attachSwipe();
    }

    private void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public synchronized void recoverSwipeItem() {
        while (!removeQueue.isEmpty()) {
            int pos = removeQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAbsoluteAdapterPosition();
        if (swipePositon != pos) {
            removeQueue.add(swipePositon);
        }
        swipePositon = pos;
        if (buttonBuffer.containsKey(swipePositon)) {
            buttonList = new ArrayList<>(buttonBuffer.get(swipePositon));
        } else {
            buttonList.clear();
        }
        buttonBuffer.clear();
        swipeThreshold = 0.5f * buttonList.size() * buttonWidth;
        recoverSwipeItem();
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAbsoluteAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0) {
            swipePositon = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                List<MyButton> buffer = new ArrayList<>();
                if (!buttonBuffer.containsKey(pos)) {
                    instantiateMyButton(viewHolder, buffer);
                    buttonBuffer.put(pos, buffer);
                } else {
                    buffer = buttonBuffer.get(pos);
                }
                translationX = dX * buffer.size() * buttonWidth / itemView.getWidth();
                drawButton(c, itemView, buffer, pos, translationX);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private void drawButton(Canvas c, View itemView, List<MyButton> buffer, int pos, float translationX) {
        float right = itemView.getRight();
        float dbuttonWidth = -1 * translationX / buffer.size();
        for (MyButton button : buffer) {
            float left = right - dbuttonWidth;
            button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()), pos);
            right = left;
        }
    }

    public abstract void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buffer);

    public class MyButton {
        private String txt;
        private int imageResID, textSize, color, pos;
        private RectF clickRegion;
        private MyButtonClickListener listener;
        private Context context;
        private Resources resource;

        public MyButton(Context context, String txt, int textSize, int imageResID, int color, MyButtonClickListener listener) {
            this.txt = txt;
            this.imageResID = imageResID;
            this.textSize = textSize;
            this.color = color;
            this.listener = listener;
            this.context = context;
            this.resource = context.getResources();
        }

        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                listener.onclick(pos);
                return true;
            }
            return false;
        }

        private void onDraw(Canvas c, RectF rectF, int pos) {
            Paint p = new Paint();
            p.setColor(color);
            c.drawRect(rectF, p);

            // Vẽ văn bản hoặc hình ảnh
            p.setColor(Color.WHITE);
            p.setTextSize(textSize);
            Rect r = new Rect();
            float cHeight = rectF.height();
            float cWidth = rectF.width();
            p.setTextAlign(Paint.Align.LEFT);
            float x, y;

            if (imageResID == 0) {
                p.getTextBounds(txt, 0, txt.length(), r);
                x = cWidth / 2f - r.width() / 2f - r.left;
                y = cHeight / 2f + r.height() / 2f - r.bottom;
                c.drawText(txt, rectF.left + x, rectF.top + y, p);
            } else {
                Drawable d = ContextCompat.getDrawable(context, imageResID);
                Bitmap bitmap = drawableToBitmap(d);
                float bitmapX = (rectF.left + rectF.right) / 2 - bitmap.getWidth() / 2;
                float bitmapY = (rectF.top + rectF.bottom) / 2 - bitmap.getHeight() / 2;
                c.drawBitmap(bitmap, bitmapX, bitmapY, p);
            }

            // Đặt vùng nhấp chuột
            RectF toado = null;
            if (rectF != null) {
                toado = rectF;
            }
            clickRegion = toado;
            this.pos = pos;
        }

    }

    private Bitmap drawableToBitmap(Drawable d) {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bitmap;
    }



}
