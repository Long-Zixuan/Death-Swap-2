package name.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;

import static name.deathswap.LGDeathSwapMod.LOGGER;

public class SwapPosAsync extends Thread {

    @Override
    public void run()
    {
        System.out.println("SwapPosAsync start");
        System.out.println("Thread Name:"+"\t"+getName());
        System.out.println("Thread ID:"+"\t"+getId());
        swapPos();
        System.out.println("SwapPosAsync end");
    }
    List<ServerPlayerEntity> _players;
    public SwapPosAsync(List<ServerPlayerEntity> players)
    {
        _players = players;
    }

    void swapPos()
    {
        List<ServerPlayerEntity> alivePlayers = new ArrayList<ServerPlayerEntity>();
        for(ServerPlayerEntity player : _players)
        {
            if (player.interactionManager.getGameMode() == GameMode.SURVIVAL && player.getHealth()>0)
            {
                alivePlayers.add(player);
            }
        }

        if (alivePlayers.size() < 2)
        {
            LOGGER.info("没有足够的玩家进行游戏");
            Text msg = new LiteralText("没有足够的玩家进行游戏").formatted(Formatting.YELLOW);
            _players.get(0).sendMessage(msg,false);
            return;
        }


        ServerPlayerEntity player1 = alivePlayers.get(0);
        double tempX = player1.getX();
        double tempY = player1.getY();
        double tempZ = player1.getZ();

        ServerWorld tmpWorld = player1.getServerWorld();
        //World tmpWorld = player1.world;
        System.out.println("Debug:"+player1+"            "+tmpWorld.toString());

        float tmpYaw = player1.getYaw(0);
        float tmpPitch = player1.getPitch(0);

        for(int i = 0; i < alivePlayers.size()-1; i++)
        {
            ServerPlayerEntity tmpPlayer = alivePlayers.get(i);
            //tmpPlayer.setWorld(alivePlayers.get(i+1).world);
            try
            {
                tmpPlayer.teleport(alivePlayers.get(i+1).getServerWorld(),alivePlayers.get(i+1).getX(), alivePlayers.get(i+1).getY(), alivePlayers.get(i+1).getZ(),alivePlayers.get(i+1).getYaw(0),alivePlayers.get(i+1).getPitch(0));
            }
            catch (Exception e)
            {
                System.out.println("SwapPosAsync Exception:"+e.toString());
            }
            Text msg = new LiteralText("你和玩家：" + alivePlayers.get(i+1).getGameProfile().getName()+"交换了位置").formatted(Formatting.YELLOW);
            tmpPlayer.sendMessage(msg,false);
            System.out.println("Debug:"+tmpPlayer.toString());
        }
        ServerPlayerEntity lastPlayer = alivePlayers.get(alivePlayers.size()-1);
        Swap2:System.out.println("Lastplayer1:"+lastPlayer.toString());
        try
        {
            lastPlayer.teleport(tmpWorld,tempX, tempY, tempZ,tmpYaw,tmpPitch);
        }
        catch (Exception e)
        {
            System.out.println("SwapPosAsync Exception:"+e.toString());
        }

        System.out.println("Lastplayer2:"+lastPlayer.toString());
        Text msg = new LiteralText("你和玩家：" + player1.getGameProfile().getName()+"交换了位置").formatted(Formatting.YELLOW);
        lastPlayer.sendMessage(msg,false);
        alivePlayers.clear();
    }







}

//LZX completed this code in 2024/12/07
//LZX-TC-2024-03-21-003