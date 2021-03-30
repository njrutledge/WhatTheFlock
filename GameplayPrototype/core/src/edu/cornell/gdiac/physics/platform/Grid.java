package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Grid {

    private final int ROWS = 50;
    private final int COLS = 60;

    private float cell_width;
    private float cell_height;

    private Tile[][] grid;

    /**
     * Initializes the grid
     *
     * @param width     the width of the canvas
     * @param height    the height of the canvas
     */
    public Grid(float width, float height) {
        cell_width = width/COLS;
        cell_height = height/ROWS;

        grid = new Tile[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = new Tile(i, j);
            }
        }
    }

    /**
     * Transforms a tile's row and col into x and y world-coordinates
     *
     * @param row   The row of the given tile
     * @param col   The col of the given tile
     * @return      X and Y coordinates of the tile at [row,col]
     */
    public Vector2 getPosition(float row, float col) {
        return new Vector2((row*cell_height)-(0.5f*cell_height),
                (col*cell_width)-(0.5f*cell_width));
    }

    /**
     * Returns whether (x1, y1) and (x2, y2) lands in the same tile
     *
     * @param x1    X world coordinate of first position
     * @param y1    Y world coordinate of first position
     * @param x2    X world coordinate of second position
     * @param y2    Y world coordinate of second position
     * @return
     */
    public boolean sameTile(float x1, float y1, float x2, float y2) {
        return getTile(x1, y1) ==  getTile(x2, y2);
    }

    /**
     * Returns whether (x1, y1) and grid[row][col] is the same tile
     *
     * @param x1    X world coordinate of first position
     * @param y1    Y world coordinate of first position
     * @param row   row of second position
     * @param col   col of second position
     * @return
     */
    public boolean sameTile(float x1, float y1, int row, int col) {
        return getTile(x1, y1) ==  getTile(row, col);
    }

    /**
     * Sets the tile to be an obstacle
     *
     * @param x     X world coordinate of tile
     * @param y     Y world coordinate of tile
     */
    public void setObstacle(float x, float y) { getTile(x,y).setObstacle(); }

    /** Set visited attribute for tile at grid[row][col] to be true
     *
     * @param row   Row of the tile
     * @param col   Col of the tile
     */
    public void setVisited(int row, int col) { grid[row][col].setVisited(true);}

    /**
     * Whether the tile at grid[row][col] has been visited
     *
     * @param row     Row of the tile
     * @param col     Col of the tile
     * @return      Whether the tile has been visited
     */
    public boolean isVisited(int row, int col) { return grid[row][col].isVisited(); }

    /**
     * Whether the tile at grid[row][col] is an obstacle
     *
     * @param row     Row of the tile
     * @param col     Col of the tile
     * @return      Whether the tile is an obstacle
     */
    public boolean isObstacle(int row, int col) { return grid[row][col].isObstacle(); }

    /** Whether the tile at grid[row][col] is in bounds
     *
     * @param row   The row of the tile in question
     * @param col   The col of the tile in question
     * @return      Whether the tile is in bounds
     */
    public boolean inBounds(int row, int col) { return col >= 0 && col < COLS && row >= 0 && row < ROWS; }

    /** Set all tiles.visited to false */
    public void resetVisited() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j].setVisited(false);
            }
        }
    }

    /** Return the row and the col of the tile at (x,y).
     *
     * @param x     X world coordinate
     * @param y     Y world coordinate
     * @return      Row and col, with respect to the grid, of the tile at (x,y)
     */
    public Vector2 getTileRowCol(float x, float y) {
        Vector2 tile = new Vector2();
        tile.x = MathUtils.clamp((int)(x/cell_width),0,ROWS-1);
        tile.y = MathUtils.clamp((int)(y/cell_height),0,COLS-1);
        return tile;
    }

    /**
     * Gets the tile at grid[row][col]
     *
     * @param row   The row of the given tile
     * @param col   The col of the given tile
     * @return      Tile at grid[row][col]
     */
    private Tile getTile(int row, int col) {
        return grid[row][col];
    }

    /**
     * Gets the tile at the x and y world coordinates
     *
     * @param x     X world coordinate of desired location
     * @param y     Y world coordinate of desired location
     * @return      Tile at (x,y)
     */
    private Tile getTile(float x, float y) {
        return grid[MathUtils.clamp((int)(x/cell_width),0,ROWS-1)]
                [MathUtils.clamp((int)(y/cell_height),0,COLS-1)];
    }

    private class Tile {
        protected int row;
        protected int col;
        private boolean obstacle;
        private boolean visited;

        /** Initializes a tile
         *
         * @param row   The row of the tile with respect to the grid
         * @param col   The col of the tile with respect to the grid
         */
        private Tile(int row, int col) {
            this.row = row;
            this.col = col;
            obstacle = false;
            visited = false;
        }

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
         * Set obstacle to true
         */
        public void setObstacle() { obstacle = true; }

        /**
         * Set visited to value
         */
        public void setVisited(boolean value) { visited = value; }

        /**
         * Whether or not the tile has been visited in this search
         *
         * @return visited?
         */
        private boolean isVisited() { return visited; }

        /**
         * Whether or not this tile is contains a wall or an obstacle
         *
         * @return  obstacle?
         */
        private boolean isObstacle() { return obstacle; }
    }
}
