package name.deathswap;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


import net.minecraft.world.World;
//import org.lwjgl.system.CallbackI;

//import cn.hutool.core.thread.ThreadUtil;

import java.util.concurrent.*;


public class TransAsyncThread implements Runnable
{
    private final String loadingKey = "lds.loading";//少女祈祷中
    private final String error1Key = "lds.error1";//尝试十次寻找安全坐标均失败，请检查世界设置,死亡交换游戏启动失败
    private final String gameStartKey = "lds.start";//游戏开始！

    private final int ERROR_POS = 1000;
    boolean _loading = true;
    @Override
    public void run()
    {
        System.out.println("TransAsyncThread start");
        System.out.println("Thread Name:"+"\t"+Thread.currentThread().getName());
        System.out.println("Thread ID:"+"\t"+Thread.currentThread().getId());

        //CompletableFuture<Void> future =
        CompletableFuture.runAsync(() -> {
            System.out.println("SendMSGAsync start");
            System.out.println("Thread Name:"+"\t"+Thread.currentThread().getName());
            System.out.println("Thread ID:"+"\t"+Thread.currentThread().getId());

            MSGAni(I18n.translate(loadingKey));

            System.out.println("SendMSGAsync end");
        });

        BlockPos safePos = findSafePos();
        if(safePos.getY() != ERROR_POS)
        {
            _player.teleport(safePos.getX(), safePos.getY() + 1, safePos.getZ());

            LGDeathSwapMod.getInstance().resetPlayer(_player);
            Text gameStart = new LiteralText(I18n.translate(gameStartKey)).formatted(Formatting.YELLOW);
            _player.sendMessage(gameStart,true);
        }
        _loading = false;
        //executor.shutdown();
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
                //TimeUnit.SECONDS.sleep(1);
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
            String str = I18n.translate(error1Key);
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