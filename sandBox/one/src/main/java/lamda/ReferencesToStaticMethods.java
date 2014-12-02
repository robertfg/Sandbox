package lamda;

public class ReferencesToStaticMethods
{
   public static void main(String[] args)
   {
      new Thread(ReferencesToStaticMethods::doWork).start(); 
      new Thread(() -> doWork()).start();
      new Thread(new Runnable()
                 {
                    @Override
                    public void run()
                    {
                       doWork();
                    }
                 }).start();
   }

   static void doWork()
   {
      String name = Thread.currentThread().getName();
      for (int i = 0; i < 50; i++)
      {
         System.out.printf("%s: %d%n", name, i);
         try
         {
            Thread.sleep((int) (Math.random()*50));
         }
         catch (InterruptedException ie)
         {
         }
      }
   }
}