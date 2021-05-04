package code.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import code.game.views.GameCanvas;

import java.util.ArrayList;
import java.util.Comparator;

public class Grid {

    private final int ROWS = 27;
    private final int COLS = 48;

    private float cell_width;
    private float cell_height;

    private float canvas_width;
    private float canvas_height;

    private Tile[][] grid;
    private TileComparator comparator;

    /**
     * Initializes the grid
     *
     * @param width     the width of the canvas
     * @param height    the height of the canvas
     */
    public Grid(float width, float height, Vector2 scale) {
        cell_width  = width/scale.x/COLS;
        cell_height = height/scale.y/ROWS;

        canvas_width  = width;
        canvas_height = height;

        comparator = new TileComparator();

        grid = new Tile[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = new Tile(i, j);
            }
        }
        populate();
    }


    public void clearObstacles(){
        for (int ii = 0; ii < ROWS; ii++){
            for (int jj = 0; jj < COLS; jj++){
                getTile(ii,jj).clearObstacle();
            }
        }
    }

    /** Resets all costs for every tile to the initial starting values */
    public void clearCosts() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Tile tile = grid[i][j];
                tile.setFcost(10000);
                tile.setGcost(10000);
                tile.setHcost(10000);
                tile.setParent(null);
            }
        }
    }

    /** Returns whether the tile at (x,y) is an obstacle
     *
     * @param x     the x coordinate on the canvas?
     * @param y     the y coordinate on the canvas?
     *
     * @return whether the tile at (x,y) is an obstacle
     */
    public boolean isObstacleAt(float x, float y) {
        return getTile(x,y).isObstacle();
    }

    /** Returns whether the tile at [row,col] is an obstacle
     *
     * @param row       the row in the grid
     * @param col       the column in the grid
     * @return whether the tile at [row,col] is an obstacle
     */
    public boolean isObstacleTile(int row, int col){
        return grid[row][col].isObstacle();
    }

    /** Returns the comparator used for comparing Tile FCosts.
     *
     * @return  comparator
     */
    public TileComparator getComparator() {
        return comparator;
    }

    /**
     * Transforms a tile's row and col into x and y world-coordinates
     *
     * @param row   The row of the given tile
     * @param col   The col of the given tile
     * @return      X and Y coordinates of the tile at [row,col]
     */
    public Vector2 getPosition(float row, float col) {
        return new Vector2((col*cell_width)+(0.5f*cell_width),(row*cell_height)+(0.5f*cell_height));
    }

    /**
     * Sets the tile to be an obstacle
     *
     * @param x     X world coordinate of tile
     * @param y     Y world coordinate of tile
     */
    public void setObstacle(float x, float y) { getTile(x,y).setObstacle(); }

    /** Whether the tile at grid[row][col] is in bounds
     *
     * @param row   The row of the tile in question
     * @param col   The col of the tile in question
     * @return      Whether the tile is in bounds
     */
    public boolean inBounds(int row, int col) { return col >= 0 && col < COLS && row >= 0 && row < ROWS; }

    /**
     * Gets the tile at the x and y world coordinates
     *
     * @param x     X world coordinate of desired location
     * @param y     Y world coordinate of desired location
     * @return      Tile at (x,y)
     */
    public Tile getTile(float x, float y) {
        return grid[MathUtils.clamp((int)(y/cell_height),0,ROWS-1)]
                [MathUtils.clamp((int)(x/cell_width),0,COLS-1)];
    }

    private void populate(Tile tile) {
        int curr_row = tile.getRow();
        int curr_col = tile.getCol();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (inBounds(curr_row+i, curr_col+j)) {
                    if (!(i == 0 && j == 0)) { tile.addNeighbor(grid[curr_row+i][curr_col+j]); }
                }
            }
        }
    }

    private void populate() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                populate(grid[i][j]);
            }
        }
    }

    /**
     * Returns the row of the tile with respect to the grid
     *
     * @return row
     */
    public int getRowCount() { return ROWS; }

    /**
     * Returns the col of the tile with respect to the grid
     *
     * @return col
     */
    public int getColCount() { return COLS; }

    /**
     * Returns the gcost of the tile
     *
     * @return gcost
     */

    /**
     * Draws the outline of the grid.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        // canvas cell width (unscaled)
        float ccw = canvas_width/COLS;
        // canvas cell height (unscaled)
        float cch = canvas_height/ROWS;
        for (int i = 1; i < ROWS; i++) {
            canvas.drawLine(new Vector2(0,cch*i), new Vector2(ccw*COLS,cch*i));
        }
        for (int j = 1; j < COLS; j++) {
            canvas.drawLine(new Vector2(ccw*j,0), new Vector2(ccw*j, cch*ROWS));
        }
        BitmapFont pFont = new BitmapFont();
        pFont.setColor(Color.RED);
        canvas.endDebug();
        canvas.begin();
        for(Tile[] r : grid){
            for (Tile t : r){
                if (t.isObstacle()){
                    canvas.drawText("X",pFont, ccw*t.getCol(), cch*(t.getRow()+1));
                }
            }
        }
        canvas.end();
        canvas.beginDebug();
    }

    public class Tile {
        protected int row;
        protected int col;
        private boolean obstacle;

        private ArrayList<Tile> neighbors;

        // Distance from starting node
        private float gcost;
        // Distance from end node
        private float hcost;
        // GCost + HCost
        private float fcost;

        private Tile parent;

        /** Initializes a tile
         *
         * @param row   The row of the tile with respect to the grid
         * @param col   The col of the tile with respect to the grid
         */
        private Tile(int row, int col) {
            this.row = row;
            this.col = col;
            obstacle = false;

            this.gcost = 10000;
            this.hcost = 10000;
            this.fcost = 10000;
            this.parent = null;

            neighbors = new ArrayList<>();
        }

        public ArrayList<Tile> getNeighbors() { return neighbors; }

        /**
         * Returns the row of the tile with respect to the grid
         *
         * @return row
         */
        public int getRow() { return row; }

        /**
         * Returns the col of the tile with respect to the grid
         *
         * @return col
         */
        public int getCol() { return col; }

        /**
         * Returns the gcost of the tile
         *
         * @return gcost
         */
        public float getGcost() { return gcost; }

        /**
         * Returns the hcost of the tile
         *
         * @return hcost
         */
        public float getHcost() { return hcost; }

        /**
         * Returns the fcost of the tile
         *
         * @return fcost
         */
        public float getFcost() { return fcost; }

        /**
         * Returns the parent of the tile
         *
         * @return parent
         */
        public Tile getParent() { return parent; }

        /** Add tile to this tile's list of neighbors
         *
         * @param tile  the tile to be added as a neighbor
         */
        public void addNeighbor(Tile tile) { neighbors.add(tile); }

        /**
         * Sets gcost to cost
         *
         * @param cost  Value to set gcost to
         */
        public void setGcost(float cost) { gcost = cost; }

        /**
         * Sets hcost to cost
         *
         * @param cost  Value to set hcost to
         */
        public void setHcost(float cost) { hcost = cost; }

        /**
         * Sets fcost to cost
         *
         * @param cost  Value to set fcost to
         */
        public void setFcost(float cost) { fcost = cost; }

        /**
         * Sets the parent of the tile to parent
         *
         * @param parent The tile to set as this tile's parent
         */
        public void setParent(Tile parent) { this.parent = parent; }

        /**
         * Set obstacle to true
         */
        public void setObstacle() { obstacle = true; }
        /**
         * Set obstacle to false
         */
        public void clearObstacle() { obstacle = false; }

        /**
         * Whether or not this tile is contains a wall or an obstacle
         *
         * @return  obstacle?
         */
        public boolean isObstacle() { return obstacle; }
    }

    /**
     * Comparator for Tile class that have the following rules:
     *
     * If Tile1's FCost < Tile2's FCost : -1
     * If Tile1's FCost > Tile2's FCost : 1
     * Otherwise, 0
     */
    public class TileComparator implements Comparator<Tile> {
        @Override
        public int compare(Tile tile1, Tile tile2) {
            return tile1.getFcost() < tile2.getFcost() ? -1: tile1.getFcost() > tile2.getFcost()? 1: 0;
        }
    }
}
