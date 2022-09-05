public class MSCell
{
   private boolean revealed;
   private boolean bomb;
   private int value;
   private boolean flagged;
   
   public MSCell()
   {
      revealed = false;
      flagged = false;
      bomb = false;
      value = 0;
   }
   
   //mutators
   public void setRevealed()
   {
      revealed = true;
   }
   public void setFlagged(boolean flag)
   {
      flagged = flag;
   }
   public void setBomb()
   {
      bomb = true;
   }
   public void setValue(int val)
   {
      value = val;
   }
   
   //Accessors
   public boolean isRevealed()
   {
      return revealed;
   }
   public boolean isBomb()
   {
      return bomb;
   }
   public boolean isFlagged()
   {
      return flagged;
   }
   public int getValue()
   {
      return value;
   }
   
   public String toString()
   {
      String s="";
         if (flagged)
            s = "";
			else if (bomb)
				s = "B";
         else if(value == 0)
            s = "";
			else
				s += value;
			return s;
   }
}