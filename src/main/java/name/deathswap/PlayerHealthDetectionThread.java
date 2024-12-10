package name.deathswap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.minecraft.world.GameMode;


import java.util.List;

import static name.deathswap.LGDeathSwapMod.LOGGER;

public class PlayerHealthDetectionThread implements Runnable
{
    private static PlayerHealthDetectionThread _instance = null;
    public static PlayerHealthDetectionThread getInstance()
    {
        if(_instance==null)
        {
            if(LGDeathSwapMod.getInstance().getMinecraftServer() == null)
            {
                LOGGER.error("MinecraftServer is null");
                return null;
            }
            _instance = new PlayerHealthDetectionThread();
        }
        return _instance;
    }

    Thread _thread;
    public void startThread()
    {
        _thread = new Thread(this,"PlayerHealthDetection Thread");
        _thread.start();
        System.out.println("PlayerHealthDetectionThread");
        System.out.println("Thread Name"+"\t"+_thread.getName());
        System.out.println("Thread ID"+"\t"+_thread.getId());
    }

    String _winText = "No Winner";
    private volatile boolean _running = true;//CSDN说加个volatile比较好，那我就加咯，嘻嘻
    private final int SLEEP_TIME = 500;//半秒

    //private LGDeathSwapMod lgDeathSwapModInstance = LGDeathSwapMod.getInstance();
    public void stopTread()
    {
        _running = false;
        _instance = null;
    }

    private void TreadMainLogic()
    {
        while(_running)
        {
            //System.out.println("PlayerHealthDetectionAsync is running \nisGameStarting:"+LGDeathSwapMod.getInstance().getIsGameStarting());
            playerHealthDetection();
            try
            {
                //System.out.println("PlayerHealthDetectionAsync is running");
                Thread.sleep(SLEEP_TIME);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run()
    {
        TreadMainLogic();
    }

    private PlayerHealthDetectionThread()
    {
        super();
    }




    private void playerHealthDetection()
    {
        List<ServerPlayerEntity> players = LGDeathSwapMod.getInstance().getMinecraftServer().getPlayerManager().getPlayerList();
        if(!LGDeathSwapMod.getInstance().getIsGameStarting())
        {
            if(players.size()>LGDeathSwapMod.getInstance().getPlayerNum())
            {
                players.get(players.size()-1).sendMessage(new LiteralText(LGDeathSwapMod.MOD_INFO[0]+":欢迎加入死亡交换游戏！").formatted(Formatting.YELLOW),false);
            }
            LGDeathSwapMod.getInstance().setPlayerNum(players.size());
            return;
        }
        else
        {
            if(players.size()>LGDeathSwapMod.getInstance().getPlayerNum())
            {
                players.get(players.size()-1).sendMessage(new LiteralText(LGDeathSwapMod.MOD_INFO[0]+":欢迎加入死亡交换游戏！游戏已经开始，你现在处于旁观模式").formatted(Formatting.YELLOW),false);
                players.get(players.size()-1).setGameMode(GameMode.SPECTATOR);
            }
            LGDeathSwapMod.getInstance().setPlayerNum(players.size());
            for (ServerPlayerEntity player : players)
            {
                if (player.getHealth() <= 0)
                {
                    onPlayerDeath(player);
                }

            }
            onPlayerWin();
        }

    }
    private void onPlayerDeath(ServerPlayerEntity player)
    {
        System.out.println(player.getGameProfile().getName()+" Death");
        player.setGameMode(GameMode.SPECTATOR);
        Text msg = new LiteralText("You Death").formatted(Formatting.YELLOW);
        player.sendMessage(msg,true);
        //player.removeStatusEffect(StatusEffects.);
        double tmpX = player.getX();
        double tmpZ = player.getZ();
        double tmpY = player.getY();
        float tmpYaw = player.getYaw(0);
        float tmpPitch = player.getPitch(0);
        ServerWorld tmpWorld = player.getServerWorld();

        player.setHealth(20);
        player.teleport(tmpWorld,tmpX,tmpY,tmpZ,tmpYaw,tmpPitch);
    }

    private void onPlayerWin()
    {
        if(!LGDeathSwapMod.getInstance().getIsGameStarting())
        {
            return;
        }
        int SurvalPlayerNum=0;
        ServerPlayerEntity tmpPlayer = null;
        List<ServerPlayerEntity> players = LGDeathSwapMod.getInstance().getMinecraftServer().getPlayerManager().getPlayerList();
        for(ServerPlayerEntity player : players)
        {
            if (player.interactionManager.getGameMode() == GameMode.SURVIVAL)
            {
                SurvalPlayerNum++;
                tmpPlayer = player;
            }
        }

        if(SurvalPlayerNum==0)
        {
            System.out.println("No Winner");
            _winText = "No winner";
            for(ServerPlayerEntity player : players)
            {
                Text msg2 = new LiteralText(_winText).formatted(Formatting.YELLOW);
                player.sendMessage(msg2,true);
            }
            LGDeathSwapMod.getInstance().setIsGameStarting(false);
        }
        if(SurvalPlayerNum==1&&players.size()!=1)
        {
            System.out.println(tmpPlayer.getGameProfile().getName()+" Win");
            Text msg = new LiteralText("You Win").formatted(Formatting.YELLOW);
            tmpPlayer.sendMessage(msg,false);
            tmpPlayer.setGameMode(GameMode.SPECTATOR);
            _winText = "Winner is:" + tmpPlayer.getGameProfile().getName();
            for(ServerPlayerEntity player : players)
            {
                Text msg2 = new LiteralText("胜利者是:" + tmpPlayer.getGameProfile().getName()).formatted(Formatting.YELLOW);
                player.sendMessage(msg2,true);
                LGDeathSwapMod.getInstance().playAnvilFallSound(player, SoundEvents.ENTITY_PLAYER_LEVELUP);
            }
            LGDeathSwapMod.getInstance().setIsGameStarting(false);
        }


    }

}

//LZX completed this code in 2024/12/08
//LZX-TC-2024-03-21-002