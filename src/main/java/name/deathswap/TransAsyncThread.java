package name.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


import net.minecraft.world.World;

//import cn.hutool.core.thread.ThreadUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TransAsyncThread extends Thread
{
    private final int ERROR_POS = 1000;
    boolean _loading = true;
    @Override
    public void run()
    {
        System.out.println("TransAsyncThread start");
        System.out.println("Thread Name:"+"\t"+getName());
        System.out.println("Thread ID:"+"\t"+getId());


        ExecutorService executor = Executors.newFixedThreadPool(1);

        Callable<Integer> callableTask = () -> {
            System.out.println("SendMSGAsync start");
            System.out.println("Thread Name:"+"\t"+getName());
            System.out.println("Thread ID:"+"\t"+getId());

            MSGAni("少女祈祷中");

            System.out.println("SendMSGAsync end");
            return 0;
        };

        Future<Integer> future = executor.submit(callableTask);


        /*ThreadUtil.execAsync(() ->
        {
            System.out.println("SendMSGAsync start");
            System.out.println("Thread Name:"+"\t"+getName());
            System.out.println("Thread ID:"+"\t"+getId());

            MSGAni("少女祈祷中");

            System.out.println("SendMSGAsync end");
        });*/


        //Text msg = new LiteralText("☯少女祈祷中。。。。☯").formatted(Formatting.RED);
        //_player.sendMessage(msg,true);


        BlockPos safePos = findSafePos();
        if(safePos.getY() != ERROR_POS)
        {
            _player.teleport(safePos.getX(), safePos.getY() + 1, safePos.getZ());

            LGDeathSwapMod.getInstance().resetPlayer(_player);
            Text gameStart = new LiteralText("游戏开始！").formatted(Formatting.YELLOW);
            _player.sendMessage(gameStart,true);
        }
        _loading = false;
        executor.shutdown();
        System.out.println("TransAsyncThread end");
    }

    private void MSGAni(String _msg)
    {
        StringBuilder dotdot = new StringBuilder();
        String finalMsg;
        int dotCount = 0;
        int dotLength = 4;
        while (_loading)
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
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    private final ServerPlayerEntity _player;
    private final World _world;
    public TransAsyncThread(ServerPlayerEntity player, World world)
    {
        _player = player;
        _world = world;
    }

    private int _findPosCount = 0;

    private Block[] _dangerousBlock = new Block[]{Blocks.LAVA,Blocks.FIRE};
    private boolean isDangerousBlock(Block block)
    {
        for(Block b : _dangerousBlock)
        {
            if(b.is(block))
            {
                return true;
            }
        }
        return false;
    }
    private BlockPos findSafePos()
    {
        if(_findPosCount > 10)
        {
            String str = "尝试十次寻找安全坐标均失败，请检查世界设置,死亡交换游戏启动失败";
            Text msg = new LiteralText(str).formatted(Formatting.YELLOW);
            _player.sendMessage(msg, false);
            LGDeathSwapMod.getInstance().setIsGameStarting(false);
            return new BlockPos(0,ERROR_POS,0);
        }
        Random random = new Random();
        BlockPos blockPos = new BlockPos(random.nextInt(5000),255,random.nextInt(5000));
        Block block = _world.getBlockState(blockPos).getBlock();


        while (block.is(Blocks.AIR)&&blockPos.getY()>=0)
        {
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ());
            block = _world.getBlockState(blockPos).getBlock();

        }
        System.out.println(_player.toString()+" SafePos:"+blockPos);
        System.out.println(_player+" SafeBlock:"+block);
        if(blockPos.getY()<=0 || isDangerousBlock(block))
        {
            _findPosCount++;
            System.out.println(_player + " findPos Failed:"+_findPosCount);
            return findSafePos();
        }
        return blockPos;
    }



}

//LZX completed this code in 2024/12/08
//LZX-TC-2024-03-21-002