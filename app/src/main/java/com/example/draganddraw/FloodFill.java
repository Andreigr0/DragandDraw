package com.example.draganddraw;

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.LinkedList;
import java.util.Queue;

public class FloodFill {
    public void floodFill(Bitmap image, Point node, int targetColor, int replacementColor) {
        int width = image.getWidth();
        int height = image.getHeight();
            Queue<Point> queue = new LinkedList<Point>();
            do {

                int x = node.x;
                int y = node.y;
                while (x > 0 && image.getPixel(x - 1, y) == targetColor) {
                    x--;

                }
                boolean spanUp = false;
                boolean spanDown = false;

                while (x < width && image.getPixel(x, y) == targetColor) {

                    image.setPixel(x, y, replacementColor);


                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == targetColor) {
                        queue.add(new Point(x, y - 1));
                        spanUp = true;
                    }


                    else if (spanUp && image.getPixel(x, y - 1) != targetColor) {
                        spanUp = false;
                    }


                    if (!spanDown && y < height - 1
                            && image.getPixel(x, y + 1) == targetColor) {
                        queue.add(new Point(x, y + 1));
                        spanDown = true;
                    }

                    else if (spanDown && y < height - 1
                            && image.getPixel(x, y + 1) != targetColor) {
                        spanDown = false;
                    }
                    x++;
                }
            } while ((node = queue.poll()) != null);
    }
}