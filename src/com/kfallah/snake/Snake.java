package com.kfallah.snake;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Snake extends Canvas {
	
	//SETTINGS
	final int PIXELS_PER_SQUARE = 10;
	final int TICK = 40; // in milliseconds
	final int PIECES_PER_FOOD = 5;
	final boolean WRAP = false;
	//SETTINGS

	public static void main(String[] args) {
		Frame g = new Frame("Snake - Kion Fallah");
		g.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		g.add(new Snake());
		g.pack();
		g.setVisible(true);
	}
	
	Snake() {
		setSize(400,400);
		 ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(4);
		 scheduledPool.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				render();
			}
		 }, 200, TICK, TimeUnit.MILLISECONDS);
		 KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && !running) {
					startGame();
				} else if(running) {
					if(e.getKeyCode() == KeyEvent.VK_UP) {
						if(snake.size() <= 1 || (snake.get(0).getY() != snake.get(1).getY() + 1))
							dir = Direction.UP;
					} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						if(snake.size() <= 1 || (snake.get(0).getX() != snake.get(1).getX() + 1))
							dir = Direction.LEFT;	
					}else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(snake.size() <= 1 || (snake.get(0).getX() + 1 != snake.get(1).getX()))
							dir = Direction.RIGHT;
					} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						if(snake.size() <= 1 || (snake.get(0).getY() != snake.get(1).getY() - 1))
							dir = Direction.DOWN;
					}
				}				
				return false;
			}		 
		 });
	}
	
	void drawSquare(Graphics g, Point p, Color c) {
		g.setColor(c);
		g.fillRect((int)p.getX() * PIXELS_PER_SQUARE, (int)p.getY() * PIXELS_PER_SQUARE, PIXELS_PER_SQUARE, PIXELS_PER_SQUARE);
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect((int)p.getX() * PIXELS_PER_SQUARE, (int)p.getY() * PIXELS_PER_SQUARE, PIXELS_PER_SQUARE, PIXELS_PER_SQUARE);
	}
	
	void render() {
		System.out.println("Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(2);
			render();
			return;
		} 		
		Graphics g = bs.getDrawGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0,0,getWidth(),getHeight());	
		if(running && snake != null) {			
			// paint food
			drawSquare(g, food, Color.RED);
			
			// paint current setup for snake
			for(Point p : snake) {
				drawSquare(g, p, Color.BLACK);
			}
			
			// check for loss and wrap
			for(int i = 1; i < snake.size(); i++) {
				if(WRAP) {
					wrapPoint(snake.get(i));
				}
				
				if(snake.size() < 4 ||	
					(snake.get(i).getX() == snake.get(0).getX()
					&& snake.get(i).getY() == snake.get(0).getY())) {
					running = false;
					update(bs);
					return;
				}
			}
			
			// reset snake
			if(snake.size() == 1) {
				snake = null;
				update(bs);
				return;
			}
			
			// check for consume
			if(food.getX() == snake.get(0).getX()
					&& food.getY() == snake.get(0).getY()) {
				pieces = PIECES_PER_FOOD;
				generateFood();
			}
			
			snake.add(0, snake.get(0).getLocation());
			if(pieces == 0) 
				snake.remove(snake.size() - 1);
			else
				pieces--;		
			// move snake head
			switch(dir) {	
				case UP:
					snake.get(0).translate(0, -1);
					break;			
				case DOWN:
					snake.get(0).translate(0, 1);
					break;				
				case LEFT:
					snake.get(0).translate(-1, 0);				
					break;				
				case RIGHT:
					snake.get(0).translate(1, 0);
					break;	
			}		
			if(wrapPoint(snake.get(0)) && !WRAP) {
				running = false;
				pieces = (6000 / TICK);
			} 		
		} else if(!running && pieces > 0) {
			g.setColor(Color.RED);
			g.drawString("YOU HAVE DIED!", 200, 200);
			pieces--;
		} else {
			g.setColor(Color.RED);
			g.drawString("PRESS ENTER TO START", 200, 200);
		}		
		update(bs);
	}
	
	public boolean wrapPoint(Point p) {
		if(p.getX() < 0) {
			p.translate(getWidth() / PIXELS_PER_SQUARE, 0);
			return true;
		} else if(p.getX() > getWidth() / PIXELS_PER_SQUARE) {
			p.translate(-1 * getWidth() / PIXELS_PER_SQUARE, 0);
			return true;
		} else if(p.getY() < 0) {
			p.translate(0, getWidth() / PIXELS_PER_SQUARE);
			return true;
		} else if(p.getY() > getWidth() / PIXELS_PER_SQUARE) {
			p.translate(0, -1 * getWidth() / PIXELS_PER_SQUARE);
			return true;
		}
		return false;
	}
	
	void update(BufferStrategy bs) {
		bs.getDrawGraphics().dispose();
		bs.show();
	}
	
	void startGame() {
		dir = Direction.UP;
		snake = new ArrayList<Point>();
		for(int i = 4; i > 0; i--) 
			snake.add(new Point((getWidth()/ PIXELS_PER_SQUARE / 2), (getWidth()/ PIXELS_PER_SQUARE / 2) - i));
		generateFood();
		pieces = 0;
		running = true;
	}
	
	void generateFood() {
		food = new Point((int)(Math.random() * getWidth()/ PIXELS_PER_SQUARE), (int)(Math.random() * getHeight()/ PIXELS_PER_SQUARE));
	}
	
	enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN;
	}
	
	List<Point> snake;
	Point food;
	Direction dir;
	boolean running = false;
	int pieces;
}
