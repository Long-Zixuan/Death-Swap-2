package name.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;

public class SendMSGThread extends Thread
{
    private final int SLEEP_TIME = 100;//0.1s
    private String _msg;
    private ServerPlayerEntity _player;

    private boolean _running = true;
    public void stopThread()
    {
        _running = false;
    }
    public SendMSGThread(String msg, ServerPlayerEntity player)
    {
        _msg = msg;
        _player = player;
    }

    public void run()
    {
        System.out.println("SendMSGThread start");
        System.out.println("Thread Name:"+"\t"+getName());
        System.out.println("Thread ID:"+"\t"+getId());
        ThreadMainLogic();
        System.out.println("SendMSGThread end");
    }

    private void ThreadMainLogic()
    {
        StringBuilder dotdot = new StringBuilder();
        String finalMsg;
        int dotCount = 0;
        int dotLength = 6;
        while (_running)
        {
            dotdot = new StringBuilder();
            for(int i=0;i<dotLength;i++)
            {
                if(i <= dotCount)
                {
                    dotdot.append("。");
                }
                else
                {
                    dotdot.append(" ");
                }
            }
            finalMsg = "☯"+_msg+dotdot+"☯";
            Text txt = new LiteralText(finalMsg).formatted(Formatting.RED);
            _player.sendMessage(txt,true);
            dotCount++;
            dotCount = dotCount % dotLength;
            try
            {
                Thread.sleep(SLEEP_TIME);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

}

//LZX completed this code on 2024-12-11
//LZX-IDEA2023.2-2024-12-11-001

