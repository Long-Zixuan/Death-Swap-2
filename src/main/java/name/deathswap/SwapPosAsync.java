package name.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;

import static name.deathswap.LGDeathSwapMod.LOGGER;

import net.minecraft.text.TranslatableText;
import net.minecraft.client.resource.language.I18n;

public class SwapPosAsync extends Thread {

    private final static String noEnoughPlayerKey = "lds.no.enough.player";//没有足够的玩家进行游戏
    private final static String youChangeWithKey = "lds.you.change.with";//你和玩家：
    private final static String youChangeWithKey2 = "lds.you.change.with2";//交换了位置;


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
            Text msg = new LiteralText(I18n.translate(noEnoughPlayerKey)).formatted(Formatting.YELLOW);
            _players.get(0).sendMessage(msg,false);
            return;
        }


        ServerPlayerEntity player1 = alivePlayers.get(0);
        double tempX = player1.getX();
        double tempY = player1.getY();
        double tempZ = player1.getZ();

        ServerWorld tmpWorld = player1.getServerWorld();
        float tmpYaw = player1.getYaw(0);
        float tmpPitch = player1.getPitch(0);

        for(int i = 0; i < alivePlayers.size()-1; i++)
        {
            ServerPlayerEntity tmpPlayer = alivePlayers.get(i);
            try
            {
                tmpPlayer.teleport(alivePlayers.get(i+1).getServerWorld(),alivePlayers.get(i+1).getX(), alivePlayers.get(i+1).getY(), alivePlayers.get(i+1).getZ(),alivePlayers.get(i+1).getYaw(0),alivePlayers.get(i+1).getPitch(0));
                alivePlayers.get(i+1).getServerWorld().tryLoadEntity(tmpPlayer);
            }
            catch (Exception e)
            {
                System.out.println("SwapPosAsync Exception:"+e.toString());
            }
            Text msg = new LiteralText(I18n.translate(youChangeWithKey) + alivePlayers.get(i+1).getGameProfile().getName()+I18n.translate(youChangeWithKey2)).formatted(Formatting.YELLOW);
            tmpPlayer.sendMessage(msg,false);
            System.out.println("Debug:"+tmpPlayer.toString());
        }
        ServerPlayerEntity lastPlayer = alivePlayers.get(alivePlayers.size()-1);
        try
        {
            lastPlayer.teleport(tmpWorld,tempX, tempY, tempZ,tmpYaw,tmpPitch);
            tmpWorld.tryLoadEntity(lastPlayer);
        }
        catch (Exception e)
        {
            System.out.println("SwapPosAsync Exception:"+e.toString()+" ["+tempX+","+tempY+","+tempZ+","+tmpWorld+"]");
        }
        Text msg = new LiteralText(I18n.translate(youChangeWithKey) + player1.getGameProfile().getName()+I18n.translate(youChangeWithKey2)).formatted(Formatting.YELLOW);
        lastPlayer.sendMessage(msg,false);
        alivePlayers.clear();
    }







}

//LZX completed this code in 2024/12/07
//LZX-TC-2024-03-21-003