package name.deathswap;

import name.deathswap.LGDeathSwapMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class PlayerHealthDetectionAsync implements Runnable
{
    private static PlayerHealthDetectionAsync _instance = null;
    public static PlayerHealthDetectionAsync initInstance(MinecraftServer server)
    {
        if(_instance!=null)
        {
            _instance.stopTread();
            _instance = null;
            System.gc();
        }
        _instance = new PlayerHealthDetectionAsync(server);
        return _instance;
    }

    public static PlayerHealthDetectionAsync getInstance()
    {
        if(_instance==null)
        {
            LGDeathSwapMod.getInstance().LOGGER.error("PlayerHealthDetectionAsync haven't been initialized");
        }
        return _instance;
    }

    Thread _thread;
    public void startThread()
    {
        _thread = new Thread(this);
        _thread.start();
    }

    String winText = "No Winner";
    private volatile boolean _running = true;//CSDN说加个volatile，那我就加咯，嘻嘻
    private final int SLEEP_TIME = 500;//半秒
    public void stopTread()
    {
        _running = false;
        _instance = null;
    }

    private void TreadMainLogic()
    {
        while(_running)
        {
            System.out.println("PlayerHealthDetectionAsync is running \nisGameStarting:"+LGDeathSwapMod.getInstance().isGameStarting);
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

    /*void initMinecraftServer(MinecraftServer server)
    {
        _server = server;
    }*/
    @Override
    public void run()
    {
        TreadMainLogic();
    }
    private MinecraftServer _server;

    //private String[] _modInfo;

    private PlayerHealthDetectionAsync(MinecraftServer server)
    {
        super();
        _server = server;
        //ServerLifecycleEvents.SERVER_STARTING.register(this::initMinecraftServer);
    }




    private void playerHealthDetection()
    {
        List<ServerPlayerEntity> players = _server.getPlayerManager().getPlayerList();
        if(!LGDeathSwapMod.getInstance().isGameStarting)
        {

            if(players.size()>LGDeathSwapMod.getInstance().playerNum)
            {
                players.get(players.size()-1).sendMessage(new LiteralText(LGDeathSwapMod.getInstance().getModInfo()[0]+":欢迎加入死亡交换游戏！").formatted(Formatting.YELLOW),false);
            }
            LGDeathSwapMod.getInstance().playerNum = players.size();
            return;
        }
        else
        {
            if(players.size()>LGDeathSwapMod.getInstance().playerNum)
            {
                players.get(players.size()-1).sendMessage(new LiteralText(LGDeathSwapMod.getInstance().getModInfo()[0]+":欢迎加入死亡交换游戏！游戏已经开始，你现在处于旁观模式").formatted(Formatting.YELLOW),false);
                players.get(players.size()-1).setGameMode(GameMode.SPECTATOR);
            }
            LGDeathSwapMod.getInstance().playerNum = players.size();
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
        //player.stopUsingItem();
        //DeathScreen tmpScreen = new DeathScreen(new LiteralText("You Death"),false);

        //player.closeScreenHandler();
        player.setHealth(20);
        player.teleport(tmpWorld,tmpX,tmpY,tmpZ,tmpYaw,tmpPitch);
    }

    private void onPlayerWin()
    {
        if(!LGDeathSwapMod.getInstance().isGameStarting)
        {
            return;
        }
        int SurvalPlayerNum=0;
        ServerPlayerEntity tmpPlayer = null;
        List<ServerPlayerEntity> players = _server.getPlayerManager().getPlayerList();
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
            winText = "No winner";
            for(ServerPlayerEntity player : players)
            {
                Text msg2 = new LiteralText(winText).formatted(Formatting.YELLOW);
                player.sendMessage(msg2,true);
            }
            LGDeathSwapMod.getInstance().isGameStarting = false;
        }
        if(SurvalPlayerNum==1&&players.size()!=1)
        {

            Text msg = new LiteralText("You Win").formatted(Formatting.YELLOW);
            tmpPlayer.sendMessage(msg,false);
            tmpPlayer.setGameMode(GameMode.SPECTATOR);
            winText = "Winner is:" + tmpPlayer.getGameProfile().getName().toString();
            for(ServerPlayerEntity player : players)
            {
                Text msg2 = new LiteralText("胜利者是:" + tmpPlayer.getGameProfile().getName().toString()).formatted(Formatting.YELLOW);
                player.sendMessage(msg2,true);
                //player.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                LGDeathSwapMod.getInstance().playAnvilFallSound(player, SoundEvents.ENTITY_PLAYER_LEVELUP);
                //player.sendMessage(new LiteralText("Winner is " + tmpPlayer.getGameProfile().getName().toString()), true);
                //ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
                //ServerTickEvents.END_SERVER_TICK.register(this::playerHealthDetection);


            }
            LGDeathSwapMod.getInstance().isGameStarting = false;
        }


    }

}

//LZX completed this code in 2024/12/07
//LZX-TC-2024-03-21-002