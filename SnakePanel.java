package finalproject;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.*;

public class SnakePanel extends JPanel implements ActionListener {

	// Instance variables that will remain constant throughout the game
	final int WINDOW_WIDTH = 1000; 
	final int WINDOW_HEIGHT = 600; 
	final int PLAYER_SIZE = 100;
	final int MONSTER_SIZE = 60;
	final int PLAYER_SPEED = 40;
	final int MONSTER_SPEED = 10;
	final int SPACE_BETWEEN_FOLLOWERS = 60;
	final int GAME_UNITS = (WINDOW_WIDTH*WINDOW_HEIGHT)/PLAYER_SIZE;
	final int DELAY = 100;
	final int playerX[] = new int[GAME_UNITS];
	final int playerY[] = new int[GAME_UNITS];
	
	
	// Number of people attach to the snake line
	int numOfPeople = 1;
	
	// Counts the number of monsters defeated by the snake line
	int monstersDefeated;
	
	// Holds the X and Y coordinates of the moving monster
	int monsterX;
	int monsterY;
	
	// Holds the X and Y coordinates of the fire being cast by the player, should follow the snake leader
	int fireX;
	int fireY;
	
	// Game starts off with player moving to the right
	char direction = 'R';
	
	// Boolean value signifies if the game should be running or if it's over
	boolean running = false;
	
	// Timer object to call a repaint() method every frame
	Timer gameTimer;
	
	// Allows the newMonster() method to return new random coordinates every time the monster is defeated
	Random random;
	
	// Initializes the image of the player walking
	ImageIcon iconWalk;
	Image playerWalk;
	
	// Credit of image: https://www.deviantart.com/isa-draws/art/Pixel-Art-Background-591383173
	
	// Creates the background image for the game, since it isn't going to change, it's created as final
	final ImageIcon backgroundIcon = new ImageIcon("DirtBackground.jpeg");
	final Image backgroundImg = backgroundIcon.getImage();
	
	// Credit of image: https://www.spriters-resource.com/game_boy_advance/thelegendofzeldatheminishcap/sheet/6528/?source=genre
	
	// Initializes the image of the monster
	ImageIcon monsterIcon; 
	Image monsterImg;
	
	// Initializes the image of the fire spell
	ImageIcon fireIcon;
	Image fireImg;
	
	// This is the "frame," or the counter that increases every time the Timer object sends an ActionEvent
	// This is kept as a reference to switch images of the player to create an animation
	int renderFrame = 0;
	
	// This is another "frame" variable specific to the monster, since the monster only has 2 frames
	// This is kept to help animate the monster sprite
	int monsterFrame;
	
	// This is also another "frame" variable to animate the player attacking the monster
	// The "attack" only happens when attackActivate is 1, so it initialized as 0
	int attackActivate = 0;
	
	
	// This creates the health bar for the monster. Health bar starts at 100
	JProgressBar healthBar = new JProgressBar(0, 100);
	int monsterHealth = 100;

	
	// Normal constructor sets up the dimensions, adds the KeyAdapter, and healthBar 
	SnakePanel() {
		random = new Random();
		this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		this.add(healthBar);
		startGame();
	}
	
	// The game starts with a new instance of the monster, and starts the Timer object 
	public void startGame() {
		newMonster();
		running = true;
		gameTimer = new Timer(DELAY, this);
		gameTimer.start();
	}
	
	// Overrides the paintComponent method of the JPanel class to draw the background image, and calls the draw() method
	// The paint() method of the JPanel class gets called every time the JPanel is instantiated, so the paintComponent and draw methods in this class will
	// be called at every instantiation or repaint()
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(backgroundImg, 0,0,null);
		draw(g);
	}
	
	// Get drawn every the object is instantiated or repaint() is called
	public void draw(Graphics g) {
		
		// Only draws if the game is running and the player hasn't died yet
		if(running) {
			
			// Checks if attackActivate is 1 or 0
			// Program will animate the normal monster walking image if 0
			// Program will animate the monster flinching images if 1
			if (attackActivate == 0) {
				monsterImg = animator("Monster", monsterFrame);
			}
			else {
				monsterImg = animator("MonsterFlinch", attackActivate);
			}
			g.drawImage(monsterImg, monsterX, monsterY, MONSTER_SIZE, MONSTER_SIZE, null);
			
			// For loop draws the snake line, the player is drawn at position 0, while the followers are drawn at the positions that follow
			for(int i = 0; i < numOfPeople; i++) {
				// Checks first if attackActivate is set to 0 or 1 to see if it should animate the walking or attacking animation
				if (i == 0) {
					if (attackActivate == 0) {
						// Credit of image: https://www.spriters-resource.com/pc_computer/lostruins/sheet/165739/
						playerWalk = animator("Player", renderFrame);
					}
					else {
						playerWalk = animator("PlayerAttack", attackActivate);
						
						// Credit of image: https://www.spriters-resource.com/game_boy_advance/thelegendofzeldatheminishcap/sheet/6452/?source=genre
						
						// Whenever the player attacks, this fire animation will also be set off, to show the player casting a fire spell
						fireImg = animator("Fire", attackActivate);
						g.drawImage(fireImg, playerX[i] - 30, playerY[i], 80, 80, null);
					}
					
					g.drawImage(playerWalk, playerX[i], playerY[i], PLAYER_SIZE, PLAYER_SIZE, null);
				}
				else {
					g.drawImage(monsterImg, playerX[i] + 35, playerY[i] + 35, MONSTER_SIZE, MONSTER_SIZE, null);
				}
				
			}
		
			// This creates the score board shown at the top of the game
			g.setColor(Color.blue);
			g.setFont(new Font("Comic Sans MS",Font.BOLD, 40));
			FontMetrics metrics = getFontMetrics(g.getFont());
			g.drawString("Score: " + monstersDefeated, (WINDOW_WIDTH - metrics.stringWidth("Score: " + monstersDefeated))/2, g.getFont().getSize());
		
		}

		// If the game isn't running, or if player dies, then run the gameOver method
		else {
			gameOver(g);
		}
			
	}
	

	
	// Changes image for the draw() to display the image
	public Image animator(String fileName, int currentFrame) {
		ImageIcon inputIcon = new ImageIcon(fileName + currentFrame + ".png");
		Image newImage = inputIcon.getImage();
		return newImage;
	}
		
	// Creates a new instance of the monster at a random X and Y
	public void newMonster() {
		monsterX = random.nextInt((int)(WINDOW_WIDTH/PLAYER_SIZE))*PLAYER_SIZE;
		monsterY = random.nextInt((int)(WINDOW_HEIGHT/PLAYER_SIZE))*PLAYER_SIZE;
	}
	
	// Move the player according to what arrow key was pressed
	public void move() {
		// Moves each member of the snake line forward
		for(int i = numOfPeople; i > 0; i--) {
			playerX[i] = playerX[i - 1];
			playerY[i] = playerY[i - 1];
		} 
		
		// Moves the player (head of snake) towards the direction the arrow key was pressed
		switch(direction) {
		case 'U':
			playerY[0] = playerY[0] - PLAYER_SPEED;
			break;
		case 'D':
			playerY[0] = playerY[0] + PLAYER_SPEED;
			break;
		case 'L':
			playerX[0] = playerX[0] - PLAYER_SPEED;
			break;
		case 'R':
			playerX[0] = playerX[0] + PLAYER_SPEED;
			break;
		
		}
		
	}
	
	
	// Moves the monster towards the position of the player
	public void monsterMove() {
		
		if (monsterX - playerX[0] > 0) {
			monsterX = monsterX - MONSTER_SPEED;
		}
		else{
			monsterX = monsterX + MONSTER_SPEED;
		}
		
		
		if (monsterX < -MONSTER_SIZE) {
			monsterX = monsterX + MONSTER_SPEED * 10;
		}
		
		if (monsterX > WINDOW_WIDTH + MONSTER_SIZE) {
			monsterX = monsterX - MONSTER_SPEED * 10;
		}
	
		
		if (monsterY - playerY[0] > 0) {
			monsterY = monsterY - MONSTER_SPEED;
		}
		else {
			monsterY = monsterY + MONSTER_SPEED;
		}
		
		if (monsterY < -MONSTER_SIZE) {
			monsterY = monsterY + MONSTER_SPEED * 10;
		}
		
		if (monsterY > WINDOW_HEIGHT + MONSTER_SIZE) {
			monsterY = monsterY - MONSTER_SPEED * 10;
		}
		
		
	}
	
	
	
	// Starts the attack animation sequence
	public void startAttack() {
		attackActivate = 1;
	}
	
	// The monster gets pushed back every time the player gets close enough to attack it
	public void flinch() {

		if (monsterX - playerX[0] < 0) {
			monsterX = monsterX - MONSTER_SIZE * 2;
		}
		else {
			monsterX = monsterX + MONSTER_SIZE * 2;
		}
		
		
		if (monsterY - playerY[0] < 0) {
			monsterY = monsterY - MONSTER_SIZE * 2;
		}
		else {
			monsterY = monsterY + MONSTER_SIZE * 2;
		}


	}
	
	// Method updates the health bar to a value entered into the parameter
	public void updateHealth(int currentHealth) {
		healthBar.setValue(currentHealth);
		healthBar.setBounds(monsterX,monsterY - MONSTER_SIZE/2,60,50);
		healthBar.setForeground(Color.red);
	}
	
	// If the monster is within a near distance to the player, or the members of the snake line, then it will be attacked
	public void checkMonster() {
		if((Math.abs(playerX[0] - monsterX) < PLAYER_SIZE/2) && (Math.abs(playerY[0] - monsterY) < PLAYER_SIZE/2)) {
			monsterHealth = monsterHealth - 25;
			startAttack();
		}
		for (int i = numOfPeople; i > 0; i--) {
			if ((Math.abs(playerX[i] - monsterX) < PLAYER_SIZE/2) && (Math.abs(playerY[i] - monsterY) < PLAYER_SIZE/2)) {
				monsterHealth = monsterHealth - 25;
				startAttack();
				flinch();
			}
		}
	}
	
	// Checks if the health of the monster ever goes to 0 and if it does, score is updated, snake line increases, and a new monster is created
	public void checkHealth() {
		if (monsterHealth <= 0) {
			numOfPeople++;
			monstersDefeated++;
			newMonster();
			monsterHealth = 100;
		}
	}
	
	
	// Checks to see if the player hits the borders of the screen or if touches the snake line
	public void checkCollisions() {
		// Checks collides with body
		for (int i = numOfPeople; i > 0; i--) {
			if ((playerX[0] == playerX[i] && playerY[0] == playerY[i])) {
				running = false;
			}
		}
		//Check touches left border
		if (playerX[0] < 0) {
			running = false;
		}
		//Check touches right border
		if (playerX[0] > WINDOW_WIDTH - PLAYER_SIZE/2) {
			running = false;
		}
		//Check touches top border
		if (playerY[0] < 0) {
			running = false;
		}
		//Check touches bottom border
		if (playerY[0] > WINDOW_HEIGHT - PLAYER_SIZE/2) {
			running = false;
		}
		
		if(!running) {
			gameTimer.stop();
		}
		
		
	}

	// If the player dies, the gameOver method is called, where the "Game Over" text is displayed with the score
	public void gameOver(Graphics g) {

		playerWalk = animator("PlayerDead", 2);
		g.drawImage(playerWalk, playerX[0], playerY[0], PLAYER_SIZE,PLAYER_SIZE,null);
		
		g.setColor(Color.blue);
		g.setFont(new Font("Comic Sans MS",Font.BOLD, 75));
		FontMetrics metrics1 = getFontMetrics(g.getFont());
		g.drawString("Game Over", (WINDOW_WIDTH - metrics1.stringWidth("Game Over"))/2, WINDOW_HEIGHT/2);
	
		g.setColor(Color.blue);
		g.setFont(new Font("Comic Sans MS",Font.BOLD, 40));
		FontMetrics metrics2 = getFontMetrics(g.getFont());
		g.drawString("Score: " + monstersDefeated, (WINDOW_WIDTH - metrics2.stringWidth("Score: " + monstersDefeated))/2, g.getFont().getSize());
		
	}
	
	
	// Gets called after every time the Timer sets off an ActionEvent
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(running) {
			move();
			if (attackActivate == 0) {
				monsterMove();
			}
			checkMonster();
			checkCollisions();
			updateHealth(monsterHealth);
			checkHealth();
		}
		if(renderFrame == 38) {
			renderFrame = 0;
		}
		else {
			renderFrame++;
		}
		
		monsterFrame = renderFrame % 2;
		
		if (attackActivate >= 7 || attackActivate == 0) {
			attackActivate = 0;
		}
		else {
			attackActivate++;
		}
		
		repaint();
	}
	
	// Checks arrow keys to register changes in the direction of the player
	public class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				if (direction != 'R') {
					direction = 'L';
				}
				break;
			case KeyEvent.VK_RIGHT:
				if(direction != 'L') {
					direction = 'R';
				}
				break;
			case KeyEvent.VK_UP:
				if (direction != 'D') {
					direction = 'U';
				}
				break;
			case KeyEvent.VK_DOWN:
				if (direction != 'U') {
					direction = 'D';
				}
				break;
				
			}
		}
		
	}
	
	
	
	
	
	
}
	
	

