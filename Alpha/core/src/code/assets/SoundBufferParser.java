/*
 * SoundBufferParser.java
 *
 * This is an interface for parsing a JSON entry into a SoundBuffer asset. SoundBuffer
 * assets do not have any properties other than the file name (because all properties)
 * are stored in the file.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package code.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import code.audio.SoundBuffer;

/**
 * This class parses a JSON entry into a {@link SoundBuffer}.
 *
 * SoundBuffer assets do not have any properties other than the file name (because 
 * all properties) are stored in the file.
 */
public class SoundBufferParser implements AssetParser<SoundBuffer> {
    /** The current font entry in the JSON directory */
    private JsonValue root;

    /** The separating character between the file name and the alias */
    public static char ALIAS_SEP = ':';
    /** The substitution character for when an alias separation is in the file */
    private static char ALIAS_MASK = '∀';
    /**
     * Returns the asset type generated by this parser
     *
     * @return the asset type generated by this parser
     */
    public Class<SoundBuffer> getType() {
        return SoundBuffer.class;
    }

    /**
     * Resets the parser iterator for the given directory.
     *
     * The value directory is assumed to be the root of a larger JSON structure.
     * The individual assets are defined by subtrees in this structure.
     *
     * @param directory    The JSON representation of the asset directory
     */
    public void reset(JsonValue directory) {
        root = directory;
        root = root.getChild( "sounds" );
    }

    /**
     * Returns true if there are still assets left to generate
     *
     * @return true if there are still assets left to generate
     */
    public boolean hasNext() {
        return root != null;
    }

    /**
     * Processes the next available sound buffer, loading it into the asset manager
     *
     * {@link SoundBuffer} objects have no additional loader properties.  They are
     * specified key : filename.
     *
     * This method fails silently if there are no available assets to process.
     *
     * @param manager    The asset manager to load an asset
     * @param keymap    The mapping of JSON keys to asset file names
     */
    public void processNext(AssetManager manager, ObjectMap<String,String> keymap) {
        String file = root.asString();
        String asset = ParserUtils.safeConcatenate(file,root.name(),ALIAS_SEP,ALIAS_MASK);
        keymap.put(root.name(), asset);
        manager.load( asset, SoundBuffer.class, null );
        root = root.next();
    }

    /**
     * Returns true if o is another SoundBufferParser
     *
     * @return true if o is another SoundBufferParser
     */
    public boolean equals(Object o) {
        return o instanceof SoundBufferParser;
    }

}