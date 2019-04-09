package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import java.lang.IllegalArgumentException;

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
    private String serializationString;

    /**
     * no arg constructor for serialization
     */
    public LerpColor() { }
    
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

        // Check recursively
        if (startColor instanceof LerpColor) ((LerpColor) startColor).update(delta);
        if (endColor instanceof LerpColor) ((LerpColor) endColor).update(delta);

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
        Color start, end = null;
        // Recursively find start and end Colors
        if (startColor instanceof LerpColor) {
           start = ((LerpColor) startColor).getMiddleColor(); 
        } else {
            start = startColor;
        }
        if (endColor instanceof LerpColor) {
            end = ((LerpColor) endColor).getMiddleColor();
        } else {
            end = endColor;
        }
        return new Color(
            (start.r + end.r) / 2f,
            (start.g + end.g) / 2f,
            (start.b + end.b) / 2f,
            (start.a + end.a) / 2f
        );
    }

    /**
     * Removes the {@code Interpolation} from this object. Used to allow this class to be saved without crashing.
     * @return self for chaining
     */
    public LerpColor removeInterpolation() {
        interpolation = null;
        if (startColor instanceof LerpColor) { ((LerpColor) startColor).removeInterpolation(); }
        if (endColor instanceof LerpColor) { ((LerpColor) endColor).removeInterpolation(); }
        
        return this;
    }

    /**
     * Sets this object's serialization string. Should be called before this object is <br>
     * serialized in order to save the Interpolation Information.
     */
    public void readyForSerialization() {
        if (interpolation != null) {
            serializationString = getStringFromInterpolation(interpolation);
            removeInterpolation();
        }

        if (startColor instanceof LerpColor) { ((LerpColor) startColor).readyForSerialization(); }
        if (endColor instanceof LerpColor) { ((LerpColor) endColor).readyForSerialization(); }
    }

    /**
     * Sets this object's interpolation using its serialization string.
     */
    public void setInterpolationFromSerializationString() {
        if (serializationString != null) {
            interpolation = getInterpolationFromString(serializationString);
        }

        if (startColor instanceof LerpColor) { ((LerpColor) startColor).setInterpolationFromSerializationString(); }
        if (endColor instanceof LerpColor) { ((LerpColor) endColor).setInterpolationFromSerializationString(); }
    }

    /**
     * Get a String from an interpolation object so it can be serialized. 
     * @precondition: interpolation is not null and is one of the built in ones
     */
    public String getStringFromInterpolation(Interpolation interpolation) {
        if (interpolation == Interpolation.bounce) {
            return "bounce";
        } else if (interpolation == Interpolation.bounceIn) {
            return "bounceIn";
        } else if (interpolation == Interpolation.bounceOut) {
            return "bounceOut";
        } else if (interpolation == Interpolation.circle) {
            return "circle";
        } else if (interpolation == Interpolation.circleIn) {
            return "circleIn";
        } else if (interpolation == Interpolation.circleOut) {
            return "circleOut";
        } else if (interpolation == Interpolation.elastic) {
            return "elastic";
        } else if (interpolation == Interpolation.elasticIn) {
            return "elasticIn";
        } else if (interpolation == Interpolation.elasticOut) {
            return "elasticOut";
        } else if (interpolation == Interpolation.exp5) {
            return "exp5";
        } else if (interpolation == Interpolation.exp5In) {
            return "exp5In";
        } else if (interpolation == Interpolation.exp5Out) {
            return "exp5Out";
        } else if (interpolation == Interpolation.exp10) {
            return "exp10";
        } else if (interpolation == Interpolation.exp10In) {
            return "exp10In";
        } else if (interpolation == Interpolation.exp10Out) {
            return "exp10Out";
        } else if (interpolation == Interpolation.fade) {
            return "fade";
        } else if (interpolation == Interpolation.linear) {
            return "linear";
        } else if (interpolation == Interpolation.pow2) {
            return "pow2";
        } else if (interpolation == Interpolation.pow2In) {
            return "pow2In";
        } else if (interpolation == Interpolation.pow2Out) {
            return "pow2Out";
        } else if (interpolation == Interpolation.pow3) {
            return "pow3";
        } else if (interpolation == Interpolation.pow3In) {
            return "pow3In";
        } else if (interpolation == Interpolation.pow3Out) {
            return "pow3Out";
        } else if (interpolation == Interpolation.pow4) {
            return "pow4";
        } else if (interpolation == Interpolation.pow4In) {
            return "pow4In";
        } else if (interpolation == Interpolation.pow4Out) {
            return "pow4Out";
        } else if (interpolation == Interpolation.pow5) {
            return "pow5";
        } else if (interpolation == Interpolation.pow5In) {
            return "pow5In";
        } else if (interpolation == Interpolation.pow5Out) {
            return "pow5Out";
        } else if (interpolation == Interpolation.sine) {
            return "sine";
        } else if (interpolation == Interpolation.sineIn) {
            return "sineIn";
        } else if (interpolation == Interpolation.sineOut) {
            return "sineOut";
        } else if (interpolation == Interpolation.swing) {
            return "swing";
        } else if (interpolation == Interpolation.swingIn) {
            return "swingIn";
        } else if (interpolation == Interpolation.swingOut) {
            return "swingOut";
        }
        throw new IllegalArgumentException("Cannot get String from Interpolation");
    }

    /**
     * Gets an {@link Interpolation} object from a serialization string. 
     * @precondition: s is not null and is one of the Strings returned from <br>
     * {@code getStringFromInterpolation()}.
     */
    public Interpolation getInterpolationFromString(String s) {
        if (s.equals("bounce")) {
            return Interpolation.bounce;
        } else if (s.equals("bounceIn")) {
            return Interpolation.bounceIn;
        } else if (s.equals("bounceOut")) {
            return Interpolation.bounceOut;
        } else if (s.equals("circle")) {
            return Interpolation.circle;
        } else if (s.equals("circleIn")) {
            return Interpolation.circleIn;
        } else if (s.equals("circleOut")) {
            return Interpolation.circleOut;
        } else if (s.equals("elastic")) {
            return Interpolation.elastic;
        } else if (s.equals("elasticIn")) {
            return Interpolation.elasticIn;
        } else if (s.equals("elasticOut")) {
            return Interpolation.elasticOut;
        } else if (s.equals("exp5")) {
            return Interpolation.exp5;
        } else if (s.equals("exp5In")) {
            return Interpolation.exp5In;
        } else if (s.equals("exp5Out")) {
            return Interpolation.exp5Out;
        } else if (s.equals("exp10")) {
            return Interpolation.exp10;
        } else if (s.equals("exp10In")) {
            return Interpolation.exp10In;
        } else if (s.equals("exp10Out")) {
            return Interpolation.exp10Out;
        } else if (s.equals("fade")) {
            return Interpolation.fade;
        } else if (s.equals("linear")) {
            return Interpolation.linear;
        } else if (s.equals("pow2")) {
            return Interpolation.pow2;
        } else if (s.equals("pow2In")) {
            return Interpolation.pow2In;
        } else if (s.equals("pow2Out")) {
            return Interpolation.pow2Out;
        } else if (s.equals("pow3")) {
            return Interpolation.pow3;
        } else if (s.equals("pow3In")) {
            return Interpolation.pow3In;
        } else if (s.equals("pow3Out")) {
            return Interpolation.pow3Out;
        } else if (s.equals("pow4")) {
            return Interpolation.pow4;
        } else if (s.equals("pow4In")) {
            return Interpolation.pow4In;
        } else if (s.equals("pow4Out")) {
            return Interpolation.pow4Out;
        } else if (s.equals("pow5")) {
            return Interpolation.pow5;
        } else if (s.equals("pow5In")) {
            return Interpolation.pow5In;
        } else if (s.equals("pow5Out")) {
            return Interpolation.pow5Out;
        } else if (s.equals("sine")) {
            return Interpolation.sine;
        } else if (s.equals("sineIn")) {
            return Interpolation.sineIn;
        } else if (s.equals("sineOut")) {
            return Interpolation.sineOut;
        } else if (s.equals("swing")) {
            return Interpolation.swing;
        } else if (s.equals("swingIn")) {
            return Interpolation.swingIn;
        } else if (s.equals("swingOut")) {
            return Interpolation.swingOut;
        }
        throw new IllegalArgumentException("Cannot get Interpolation from String");
    }
}
