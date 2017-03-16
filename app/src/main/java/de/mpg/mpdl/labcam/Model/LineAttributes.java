package de.mpg.mpdl.labcam.Model;

/**
 * Created by pfingsta on 09.01.2017.
 */

public class LineAttributes {
    public float left;
    public float top;
    public float right;
    public float bottom;
    public String text;
    public float height;
    public boolean headline;

    public LineAttributes(float l, float t, float r, float b, String txt, float h, boolean hdl) {
        left = l;
        top = t;
        right = r;
        bottom = b;
        text = txt;
        height = h;
        headline = hdl;
    }

    public float getTop() {
        return this.top;
    }

    public float getLeft() {
        return this.left;
    }

    public float getRight() {
        return this.right;
    }

    public float getBottom() {
        return this.bottom;
    }

    public String getText() {
        return this.text;
    }

    public float getHeight() {
        return this.height;
    }

    public boolean isHeadline(){
        return this.headline;
    }

    public void setIsHeadline(Boolean hl){
        this.headline = hl;
    }
}