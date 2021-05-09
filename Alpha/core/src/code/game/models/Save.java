package code.game.models;

import com.badlogic.gdx.utils.JsonValue;

public class Save {
    public int screen_width;
    public int screen_height;
    public boolean auto_cook;
    public int furthest_level;
    public static final String file = "save.json";

    public Save(JsonValue save) {
        screen_width = save.getInt("screen_width",1280);
        screen_height = save.getInt("screen_height",720);
        auto_cook = save.getBoolean("auto_cook",true);
        furthest_level = save.getInt("furthest_level", 0);
    }

    public Save(int w, int h, boolean ac, int fl) {
        screen_width = w;
        screen_height = h;
        auto_cook = ac;
        furthest_level = fl;

    }

    public Save(Save s, int fl) {
        this(s.screen_width, s.screen_height, s.auto_cook, fl);
    }
}
