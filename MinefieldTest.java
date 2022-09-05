import javax.swing.JFrame;

public class MinefieldTest
{
   public static void main(String[] args)
   {
      Minefield test = new Minefield();
      test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      test.pack();
      test.setLocationRelativeTo(null);
      test.setVisible(true);
   }
}