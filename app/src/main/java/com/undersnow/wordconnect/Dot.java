package com.undersnow.wordconnect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import burgerrain.R;


/**
 * Created by alex on 4/30/2017.
 */

public class Dot extends Drawable {
    private char value;
    private boolean touched = false;
    private Context context;
    private  int wordId;

    public Dot(Context context, float x, float y, Bitmap scaled, char value, int wordId) {
        //makes bitmap and calls superclass

        super(scaled, x, y);
        this.value = value;
        this.context = context;
        this.wordId = wordId;
    }

    private static int getDrawable(int i) {

        return MainActivity.drawables.getOrDefault((char) i, R.drawable.alpha_d);
    }

    public Dot(Context context, int type) {
        //makes bitmap and calls superclass
        super(BitmapFactory.decodeResource(context.getResources(), type), 0, 0);

        this.context = context;
    }

    // moves down the dot when called
    public void update(float delta) {
        this.y += delta;
    }

    public int getWordId() {
        return wordId;
    }

    @Override
    public boolean isClicked(double mX, double mY) {
        if (touched) return false;
        return issClicked(mX, mY);
    }

    public void wasClicked() {

        Bitmap b = BitmapFactory.decodeResource(context.getResources(), getDrawable(value
                + ('A' - 'a')));
           bitmap = Bitmap.createScaledBitmap(b, b.getWidth()/3, b.getHeight()/3, true);

    }

    public boolean isOutOfBounds() {
        return this.getY() > MainGamePanel.SCREEN_HEIGHT && !this.isTouched();
    }

    public static Dot generateRandomDot(Context context, float position, boolean firstTime, int wordId) {
        int value = 'a'+ (int) (Math.random() * 27);

        Bitmap b = BitmapFactory.decodeResource(context.getResources(), getDrawable(value));
        Bitmap  scaled = Bitmap.createScaledBitmap(b, b.getWidth()/3, b.getHeight()/3, true);
        Dot d = new Dot(context, 0, 0,scaled , (char) (value), wordId);
        // probably better way to do this
        float dotX = (float) Math.floor(position * (MainGamePanel.getScreenWidth() - d.getBitmap().getWidth())); // random x location within screen
        return new Dot(context, dotX, firstTime ? 0 - d.getBitmap().getHeight() + MainGamePanel.getScreenHeight() / 2 : 0 - d.getBitmap().getHeight(), scaled, (char) ( value) , wordId );
    }

    public boolean isTouched() {
        return touched;
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }

    public boolean issClicked(double mX, double mY) {
        if (mX >= this.x && mX < this.x + bitmap.getWidth() && mY >= this.y && mY < this.y + bitmap.getHeight()) {
            return true;
        } else {
            return false;
        }
    }
}