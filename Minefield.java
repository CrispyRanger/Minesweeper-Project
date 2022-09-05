import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import javax.swing.border.Border;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Minefield extends JFrame implements Serializable
{
   private Object[] options = {"Easy","Normal", "Hard"};
   private int difficulty = JOptionPane.showOptionDialog(Minefield.this, "Choose difficulty", "Minesweeper",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Easy");
   
   private int ROWS = 9;
   private int COLS = 9;
   private int BOMBS = 10;
   private int WIN;
   private final static int SIZE = 10;
   
   private int revealed = 0;
   private int flaggedBomb = 0;
   private int numFlags = 0;
   private static int seconds = 0;
   private boolean firstBomb = true;
   private boolean winCondition = false;
   
   private static ObjectOutputStream output;
   private static ObjectInputStream input;
   
   private MSCell[][] field;
   private JButton[][] grid;
   
   private static int[] easyScores;
   
   private JButton restart;
   
   private JLabel timerLabel = new JLabel(seconds/60 + ":0" + seconds%60);
   private JLabel bombLabel;
   
   private GridBagConstraints buttonConstraints = new GridBagConstraints();
   
   private JPanel buttonGrid = new JPanel();
   private JPanel staticPanel = new JPanel();
   
   private Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
   
   private Icon bombIcon = new ImageIcon(getClass().getResource("bomb.png"));
   private Icon flagIcon = new ImageIcon(getClass().getResource("flag.png"));
   private Icon normalIcon = new ImageIcon(getClass().getResource("normal.png"));
   private Icon winIcon = new ImageIcon(getClass().getResource("win.png"));
   private Icon loseIcon = new ImageIcon(getClass().getResource("zombie.png"));
   private Icon pressIcon = new ImageIcon(getClass().getResource("pressing.png"));
   
   public Minefield()
   {
      super("Minesweeper");
      
      if(difficulty == 0)
      {
         ROWS = 9;
         COLS = 9;
         BOMBS= 10;
      }
      else if(difficulty == 1)
      {
         ROWS = 16;
         COLS = 16;
         BOMBS = 40;
      }
      else if(difficulty == 2)
      {
         ROWS = 16;
         COLS = 16;
         BOMBS = 60;
      }
      
      WIN = ROWS*COLS-BOMBS;
      
      grid = new JButton[ROWS+2][COLS+2];
      
      BorderLayout fieldGUI = new BorderLayout();
      setLayout(fieldGUI);
      
      GridBagLayout buttonLayout = new GridBagLayout();
      GridBagLayout headerGUI = new GridBagLayout();
      
      buttonGrid.setLayout(buttonLayout);
      staticPanel.setLayout(headerGUI);
      
      for(int i=1; i<ROWS+1;i++)
      {
         for(int j=1; j<COLS+1;j++)
         {
            buttonConstraints.gridx = j;
            buttonConstraints.gridy = i;
            
            grid[i][j] = new JButton(i+" "+j);
            
            grid[i][j].setPreferredSize(new Dimension(45,45));
            
            buttonGrid.add(grid[i][j],buttonConstraints);
            
            grid[i][j].addMouseListener(new MouseHandler());
         }
      }
      add(buttonGrid, BorderLayout.CENTER);
      
      buttonConstraints.weightx = 0.5;
      buttonConstraints.gridx = 0;
      
      
      timerLabel.setPreferredSize(new Dimension(100,45));
      timerLabel.setOpaque(true);
      timerLabel.setFont(timerLabel.getFont().deriveFont(30f));
      timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
      timerLabel.setForeground(Color.WHITE);
      timerLabel.setBackground(Color.BLACK);
      staticPanel.add(timerLabel, buttonConstraints);
      
      buttonConstraints.weightx = 1;
      buttonConstraints.gridx = 1;
      
      restart = new JButton("");
      restart.setIcon(normalIcon);
      restart.setPreferredSize(new Dimension(45,45));
      staticPanel.add(restart, buttonConstraints);
      restart.addMouseListener(new MouseHandler());
      
      buttonConstraints.weightx = 0.5;
      buttonConstraints.gridx = 2;
      
      bombLabel = new JLabel(BOMBS-numFlags+"/"+BOMBS);
      bombLabel.setPreferredSize(new Dimension(100,45));
      bombLabel.setOpaque(true);
      bombLabel.setFont(bombLabel.getFont().deriveFont(30f));
      bombLabel.setHorizontalAlignment(SwingConstants.CENTER);
      bombLabel.setForeground(Color.WHITE);
      bombLabel.setBackground(Color.BLACK);
      staticPanel.add(bombLabel, buttonConstraints);
      
      add(staticPanel, BorderLayout.NORTH);
      
      field = new MSCell[ROWS+2][COLS+2];
      initialiseField();
		plantBombs();
		displayField();
      
      easyScores = new int[SIZE];
      
      readRecords();      
   }
   
   Timer timer = new Timer();
   TimerTask task = new TimerTask()
   {
      public void run()
      {
         if(seconds%60<10)
            timerLabel.setText(seconds/60+":0"+seconds%60);
         else
            timerLabel.setText(seconds/60+":"+seconds%60);
         seconds++;
      }
   };
   
   public void initialiseField()
   {
      for (int i=0; i<ROWS+2;i++)
		{
			for (int j=0; j<COLS+2; j++)
			{
				field[i][j] = new MSCell();
            if((j==0)||(i==0)||(j==COLS+1)||(i==ROWS+1))
            {
               field[i][j].setValue(-1);
            }
			}
		}
   }
   
   public void plantBombs()
   {
      Random randomNumbers = new Random();
      
      int bombsPlanted = 0;
      int bombRow = 0;
      int bombCol =0;
      while(bombsPlanted<BOMBS)
      {
         bombRow = randomNumbers.nextInt(ROWS+1);
			bombCol = randomNumbers.nextInt(COLS+1);
         
         if((!field[bombRow][bombCol].isBomb())&&(bombRow>=1)&&(bombCol>=1)) 
         {
            field[bombRow][bombCol].setBomb();
            bombsPlanted++;
            
            //Calculates values for grid arround bomb when it's created does not add values to borders
            for(int i = bombRow - 1; i<=bombRow+1; i++)
            {
               for(int j = bombCol - 1; j<=bombCol+1; j++)
               {
                  if((i>=1)&&(j>=1)&&(i<ROWS+1)&&(j<COLS+1))
                  {
                     int tempValue = field[i][j].getValue() + 1;
                     field[i][j].setValue(tempValue);
                  }
               }
            }
         }
      }
   }
   
   public void clearZeros(int i, int j)
   {
      int cellValue = field[i][j].getValue();
      
      if((cellValue!=-1)&&(!(field[i][j].isRevealed())&&(!(field[i][j].isFlagged()))))
      {
         buttonConstraints.gridx = j;
         buttonConstraints.gridy = i;
         
         JLabel buttonPessedLabel = new JLabel();
         
         grid[i][j].setVisible(false);
         
         if(field[i][j].isBomb())
         {            
            if(firstBomb)
            {
               firstBomb = false;
               buttonPessedLabel.setOpaque(true);
               buttonPessedLabel.setBackground(Color.RED);
            }
            buttonPessedLabel.setIcon(bombIcon);
         }
         else
            buttonPessedLabel.setText(field[i][j].toString());
         
         buttonPessedLabel.setHorizontalAlignment(SwingConstants.CENTER);
         buttonPessedLabel.setBorder(border);
         
         buttonPessedLabel.setPreferredSize(new Dimension(45, 45));
         
         if(cellValue == 1)
            buttonPessedLabel.setForeground(Color.BLUE);
         else if(cellValue == 2)
            buttonPessedLabel.setForeground(Color.GREEN.darker());
         else if(cellValue == 3)
            buttonPessedLabel.setForeground(Color.RED);
         else if(cellValue == 4)
            buttonPessedLabel.setForeground(Color.MAGENTA);
         else if(cellValue == 5)
            buttonPessedLabel.setForeground(Color.YELLOW);
         else if(cellValue == 6)
            buttonPessedLabel.setForeground(Color.CYAN.darker());
         else if(cellValue == 7)
            buttonPessedLabel.setForeground(Color.BLACK);
         else if(cellValue == 8)
            buttonPessedLabel.setForeground(Color.PINK.darker());
         
         buttonGrid.add(buttonPessedLabel, buttonConstraints);
         buttonGrid.revalidate();
         buttonGrid.repaint();
      }
      if(cellValue==-1){}
      else if(field[i][j].isRevealed()){}
      else if(field[i][j].isFlagged()){}
      else if((field[i][j].getValue()!=0))
      {
         field[i][j].setRevealed();
         revealed++; 
      }
      else
      {
         field[i][j].setRevealed();
         revealed++;
         
         for(int k = i-1;k<=i+1;k++)
         {
            for(int m = j-1;m<=j+1;m++)
            {
               clearZeros(k,m);
            }
         }
      }
   }
   
   public void restartMS()
   {
      if(!winCondition)
         timer.cancel();
      seconds = 0;
      timer = new Timer();
      Minefield nextFrame = new Minefield();
      nextFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      nextFrame.pack();
      nextFrame.setLocationRelativeTo(null);
      dispose();
      nextFrame.setVisible(true);
   }
   
   public void displayField()
   {
      for (int i=1; i<ROWS+1;i++)
		{
			for (int j=1; j<COLS+1; j++)
			{  
            grid[i][j].setText("");
			}
			repaint();
			revalidate();
		}
   }
   
   public static void openFileOutput()
   {
      try
      {
         output = new ObjectOutputStream(new FileOutputStream("easy.ser"));
      }
      catch(IOException e)
      {
         System.err.println("Error operning file");
         System.exit(1);
      }
   }
   
   public static void openFileInput()
   {
      try
      {
         input = new ObjectInputStream(new FileInputStream("easy.ser"));
      }
      catch(IOException e)
      {
         System.out.println("Can't open file"+e);
      }
   }
   
   
   public static void addRecord()
   {
      openFileOutput();
      try
      {
         int i = 0;
         int temp = 0;
         int secTemp = seconds-1;
         while(i<SIZE)
         {
            if((easyScores[i]>=secTemp)&&(easyScores[i]!=0))
            {
               temp = easyScores[i];
               easyScores[i] = secTemp;
               secTemp = temp;
            }
            else if(easyScores[i] == 0)
            {
               temp = easyScores[i];
               easyScores[i] = secTemp;
               secTemp = temp;
            }
            i++;
         }
         output.writeObject(easyScores);
      }
      catch(IOException e)
      {
         System.err.println();
      }
   }
   
   public static void readRecords()
   {
      openFileInput();
      try
      {
         while(true)
         {
            int[] temp = (int[]) input.readObject();
            for(int i = 10;i<SIZE;i++)
            {
               System.out.println(temp[i]);
               easyScores[i] = temp[i];
            }
         }
      }
      
      catch (EOFException e)
      {
         System.err.println("ouch"+e);
      }
      catch (ClassNotFoundException e)
      {
         System.err.println("oof");
      }
      catch (IOException e)
      {
         System.err.println("why you do dis" + e);
      }
   }
   
   public static void closeFile()
   {
      try
      {
         if((input != null)&&(output != null))
         {
            input.close();
            output.close();
         }
      }
      catch(IOException e)
      {
         System.err.println();
         System.exit(1);
      }
   }
   
   private class MouseHandler implements MouseListener
   {
      public void mouseClicked(MouseEvent me)
      {
         if(seconds==0)
            timer.scheduleAtFixedRate(task,0,1000);
         
         if((restart == me.getSource())&&(me.getButton() == me.BUTTON1))
         {
            restartMS();
         }
         
         if(!winCondition)
         {
            for(int i = 1;i<ROWS+1;i++)
            {
               for(int j = 1;j<COLS+1;j++)
               {
                  buttonConstraints.gridx = j;
                  buttonConstraints.gridy = i;
               
                  if((grid[i][j] == me.getSource())&&(me.getButton() == me.BUTTON1)&&(!(field[i][j].isFlagged())))
                  {              
                     clearZeros(i,j);
                  
                     if(field[i][j].isRevealed()&&field[i][j].isBomb())
                     {
                        for(int x = 1;x<ROWS+1;x++)
                        {
                           for(int y = 1;y<COLS+1;y++)
                           {
                              if(field[x][y].isBomb())
                              {
                                 clearZeros(x,y);
                              }
                           }
                        }
                        timer.cancel();
                        restart.setIcon(loseIcon);
                        winCondition = true;
                        JOptionPane.showMessageDialog(Minefield.this,"You Lose");
                        closeFile();
                     }
                     else
                     {
                        if(WIN==revealed)
                        {
                           for(int x = 1;x<ROWS+1;x++)
                           {
                              for(int y = 1;y<COLS+1;y++)
                              {
                                 if((!(field[x][y].isFlagged()))&&(!(field[x][y].isRevealed())))
                                 {
                                    field[x][y].setFlagged(true);
                                    grid[x][y].setIcon(flagIcon);
                                    grid[x][y].setText(field[x][y].toString());
                                 }
                              }
                           }
                           timer.cancel();
                           addRecord();
                           for(int k=0;k<10;k++)
                              System.out.println(easyScores[k]);
                           restart.setIcon(winIcon);
                           winCondition = true;
                           JOptionPane.showMessageDialog(Minefield.this,"You Win!");
                           closeFile();
                        }
                     }
                  }
               
                  if((grid[i][j] == me.getSource())&&(me.getButton() == me.BUTTON3))
                  {
                     if(field[i][j].isFlagged())
                     {
                        numFlags--;
                        if(field[i][j].isBomb())
                           flaggedBomb--;
                        else
                           flaggedBomb++;
                        field[i][j].setFlagged(false);
                        grid[i][j].setIcon(null);
                     }
                     else
                     {
                        numFlags++;
                        if(field[i][j].isBomb())
                           flaggedBomb++;
                        else
                           flaggedBomb--;
                        field[i][j].setFlagged(true);
                        grid[i][j].setIcon(flagIcon);
                     }
                     grid[i][j].setText(field[i][j].toString());
                     bombLabel.setText(BOMBS-numFlags+"/"+BOMBS);
                  
                     if(BOMBS==flaggedBomb)
                     {
                        timer.cancel();
                        addRecord();
                        restart.setIcon(winIcon);
                        winCondition = true;
                        JOptionPane.showMessageDialog(Minefield.this,"You Win!");
                        closeFile();
                     }
                  }   
               }
            }
         }
      }
      public void mousePressed(MouseEvent me)
      {
         if(!winCondition)
            restart.setIcon(pressIcon);
      }
      public void mouseExited(MouseEvent me){}
      public void mouseEntered(MouseEvent me){}
      public void mouseReleased(MouseEvent me)
      {
         if(!winCondition)
            restart.setIcon(normalIcon);
      }
   }
}