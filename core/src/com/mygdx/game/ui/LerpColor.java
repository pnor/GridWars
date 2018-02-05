package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/**
 * Color that oscillates between two values. The {@link #update(float deltaTime)} method must be called
 * for the interpolation to happen
 * @author Phillip O'Reggio
 */
public class LerpColor extends Color {
    private Color startColor;
    private Color endColor;
    private boolean increasing;
    private float lastDeltaTime;
    private float totalDelta; //for increasing/decreasing

    private float timeToChange = 1f;
    private Interpolation interpolation;

    /**
     * no arg constructor for serialization
     */
    public LerpColor() {}
    
    /**
     * Constructs a LerpColor that changes from its start color to end color in 1 second
     * @param start initial color
     * @param end end color
     */
    public LerpColor(Color start, Color end) {
        startColor = start;
        endColor = end;
    }

    /**
     * Constructs a LerpColor that changes from its start color to end color in a variable amount of time
     * @param start inital color
     * @param end end color
     * @param time time to change from start to end colors
     */
    public LerpColor(Color start, Color end, float time) {
        startColor = start;
        endColor = end;
        timeToChange = time;
    }

    /**
     * Constructs a LerpColor that changes from its start color to end color using a custom {@code Interpolation}
     * @param start inital color
     * @param end end color
     * @param lerp Interpolation used
     */
    public LerpColor(Color start, Color end, Interpolation lerp) {
        startColor = start;
        endColor = end;
        interpolation = lerp;
    }

    /**
     * Constructs a LerpColor that changes from its start color to end color using a custom {@code Interpolation}
     * @param start inital color
     * @param end end color
     * @param lerp Interpolation used
     */
    public LerpColor(Color start, Color end, float time, Interpolation lerp) {
        startColor = start;
        endColor = end;
        timeToChange = time;
        interpolation = lerp;
    }

    @Override
    public float toFloatBits() {

        if (interpolation == null)  //defualt
            return new Color(startColor).lerp(endColor, totalDelta / timeToChange).toFloatBits();
        else {   //custom interpolation
            return new Color(interpolation.apply(startColor.r, endColor.r, totalDelta / timeToChange),
                    interpolation.apply(startColor.g, endColor.g, totalDelta / timeToChange),
                    interpolation.apply(startColor.b, endColor.b, totalDelta / timeToChange),
                    interpolation.apply(startColor.a, endColor.a, totalDelta / timeToChange)).toFloatBits();
        }


    }

    /*
    @Override
    public */

    public void update(float delta) {
        lastDeltaTime = delta;
        if (increasing)
            totalDelta = MathUtils.clamp(totalDelta + delta, 0, timeToChange);
        else
            totalDelta = MathUtils.clamp(totalDelta - delta, 0, timeToChange);

        if (totalDelta >= timeToChange || totalDelta <= 0f)
            increasing = !increasing;

        if (interpolation == null) {
            this.r = MathUtils.lerp(startColor.r, endColor.r, totalDelta / timeToChange);
            this.g = MathUtils.lerp(startColor.g, endColor.g, totalDelta / timeToChange);
            this.b = MathUtils.lerp(startColor.b, endColor.b, totalDelta / timeToChange);
            this.a = MathUtils.lerp(startColor.a, endColor.a, totalDelta / timeToChange);
        } else {
            this.r = interpolation.apply(startColor.r, endColor.r, totalDelta / timeToChange);
            this.g = interpolation.apply(startColor.g, endColor.g, totalDelta / timeToChange);
            this.b = interpolation.apply(startColor.b, endColor.b, totalDelta / timeToChange);
            this.a = interpolation.apply(startColor.a, endColor.a, totalDelta / timeToChange);
        }
        //System.out.println("Lerp Color Balue (of highscores): " + this.highscores);
    }

    /**
     * Checks if the starting and ending color of two {@code LerpColor} objects are the same
     * @param color LerpColor
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object color) {
            return color != null && color instanceof LerpColor &&
                    this.startColor.equals(((LerpColor) color).startColor) && this.endColor.equals(((LerpColor) color).endColor);
    }

    @Override
    public Color lerp(Color color, float progress) {
        return new Color(super.lerp(color, progress));
    }

    @Override
    public Color lerp(float r, float g, float b, float a, float progress) {
        return new Color(super.lerp(r, g, b, a, progress));
    }

    /**
     * @return Copy of this lerpColor.
     */
    @Override
    public LerpColor cpy() {
        return new LerpColor(startColor.cpy(), endColor.cpy(), timeToChange, interpolation);
    }

    public Color getStartColor() {
        return startColor;
    }

    public Color getEndColor() {
        return endColor;
    }

    /**
     * @return Color in the middle of the start and end colors
     */
    public Color getMiddleColor() {
        return new Color((startColor.r + endColor.r) / 2f, (startColor.g + endColor.g) / 2f, (startColor.b + endColor.b) / 2f, (startColor.a + endColor.a) / 2f);
    }
}
