package name.deathswap;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
////////////
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;

import net.minecraft.server.command.ServerCommandSource;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import net.minecraft.world.GameMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.time.Instant;

import java.util.List;




public class LGDeathSwapMod implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("death-swap-mod");

	private static LGDeathSwapMod _instance = null;

	public static LGDeathSwapMod getInstance()
	{
		return _instance;
	}

	private static final String MOD_AUTHOR = "LoongLy";
	private static final String MOD_NAME = "DeathSwap";
	private static final String MOD_VERSION = "2.1";
	private static final String MOD_LAST_EDIT_TIME = "2024/12/10";
	public static final String []MOD_INFO = {MOD_AUTHOR,MOD_NAME,MOD_VERSION,MOD_LAST_EDIT_TIME};

	int _deathSwapTime = 300;

	private boolean _isGameStarting = false;
	public boolean getIsGameStarting()
	{
		return _isGameStarting;
	}
	public void setIsGameStarting(boolean value)
	{
		_isGameStarting = value;
	}

	long _startTime = 0;

	private int _playerNum = 0;
	public int getPlayerNum()
	{
		return _playerNum;
	}
	public void setPlayerNum(int value)
	{
		_playerNum = value;
	}


	private MinecraftServer _server = null;

	public MinecraftServer getMinecraftServer()
	{
		return _server;
	}


	@Override
	public void onInitialize()
	{

		LOGGER.info("Init instance of LGDeathSwapMod");
		if(_instance != null)
		{
			LOGGER.error("LGDeathSwapMod has been initialized");
			return;
		}
		_instance = this;
		System.out.println("init PlayerHealthDetectionAsync");

		ServerLifecycleEvents.SERVER_STARTED.register(this::initPlayerHealthDetectionAsync);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::stopPlayerHealthDetectionAsync);
		System.out.println("init onServerTick");
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

		CommandRegistrationCallback.EVENT.register(this::editSwapTime);
		CommandRegistrationCallback.EVENT.register(this::editGameMode);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("startdeathswap")
					.executes(context -> {
						// 在指令执行时开始操作
						startGame(context.getSource());
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("startdeathswap2")
					.executes(context -> {
						// 在指令执行时开始操作
						StartGame2(context.getSource());
						return 1;
					}));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("aboutdeathswap")
					.executes(context -> {
						// 在指令执行时开始操作
						AboutMod(context.getSource());
						return 1;
					}));
		});

		LOGGER.info("I am LZX(LoongLy),Hello Fabric world!  Initialize DeathSwap mod "+ MOD_VERSION +" completed!");
	}
	private void initPlayerHealthDetectionAsync(MinecraftServer server)
	{
		System.out.println("Init PlayerHealthDetectionThread");
		//PlayerHealthDetectionThread.initInstance(server);
		_server = server;
		try
		{
			PlayerHealthDetectionThread.getInstance().startThread();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void stopPlayerHealthDetectionAsync(MinecraftServer server)
	{
		System.out.println("Stop PlayerHealthDetectionThread");
		_isGameStarting = false;
		try
		{
			PlayerHealthDetectionThread.getInstance().stopTread();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.gc();
	}




	private void editSwapTime(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
	{
		dispatcher.register(CommandManager.literal("setswaptime")
				.then(CommandManager.argument("value", IntegerArgumentType.integer())
						.executes(context -> {
							if(_isGameStarting)
							{
								context.getSource().sendFeedback(new LiteralText("游戏正在进行，不能更改时间"), false);
								return 1;
							}
							// 获取命令参数中的值
							int swaptime = IntegerArgumentType.getInteger(context, "value");
							if(swaptime<40)
							{
								context.getSource().sendFeedback(new LiteralText("交换时间不得小于40秒"), false);
								return 1;
							}
							// 更新变量的值
							_deathSwapTime = swaptime;
							// 发送消息给执行命令的玩家
							context.getSource().sendFeedback(new LiteralText("交换时间更新为： " + swaptime), false);
							return 1;
						})));
	}


	private void editGameMode(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
	{
		dispatcher.register(CommandManager.literal("gamemode")
				.then(CommandManager.argument("gamemodeValue", StringArgumentType.word())
						.executes(context -> {
							if(_isGameStarting)
							{
								context.getSource().sendFeedback(new LiteralText("Game is starting, can't change game mode to "), false);
								return 1;
							}

							String gamemodeValue = StringArgumentType.getString(context, "gamemodeValue");

							if(gamemodeValue.equals("survival"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.SURVIVAL);
							}
							else if(gamemodeValue.equals("creative"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.CREATIVE);
							}
							else if(gamemodeValue.equals("adventure"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.ADVENTURE);
							}
							else if(gamemodeValue.equals("spectator"))
							{
								context.getSource().getPlayer().setGameMode(GameMode.SPECTATOR);
							}
							else
							{
								context.getSource().sendFeedback(new LiteralText("Game mode not found"), false);
								return 1;
							}
							// 发送消息给执行命令的玩家
							context.getSource().sendFeedback(new LiteralText("您的游戏模式已更新"), false);
							return 1;
						})));
	}


	private void AboutMod(ServerCommandSource source)
	{
		Text msg = new LiteralText("Death Swap 版本:"+MOD_VERSION+" 作者:"+MOD_AUTHOR+"(Lagging_Warrior)  最后一次更新时间"+MOD_LAST_EDIT_TIME).formatted(Formatting.YELLOW);
		source.sendFeedback(msg, false);
		Text msg2 = new LiteralText("下载链接(更新中): https://mod.3dmgame.com/mod/207510").styled(style->style
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mod.3dmgame.com/mod/207510"))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to jump to: " + "https://mod.3dmgame.com/mod/207510")))
			.withColor(Formatting.UNDERLINE));
		source.sendFeedback(msg2, false);
		Text gitmsg = new LiteralText("github链接: https://github.com/Long-Zixuan/Dealth-Swap").styled(style->style
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Long-Zixuan/Dealth-Swap"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to jump to: " + "https://github.com/Long-Zixuan/Dealth-Swap")))
				.withColor(Formatting.UNDERLINE));
		source.sendFeedback(gitmsg, false);
	}

	private void initStartGame(boolean needTransPos,ServerCommandSource source)
	{
		_startTime = Instant.now().getEpochSecond();
		MinecraftServer server = source.getMinecraftServer();
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		_playerNum = players.size();
		if (players.size() < 2)
		{
			LOGGER.info("没有足够的玩家参与游戏");
			Text msg = new LiteralText("没有足够的玩家参与游戏").formatted(Formatting.YELLOW);
			players.get(0).sendMessage(msg,true);
			return;
		}
		for (ServerPlayerEntity player : players)
		{
			//Text msg = new LiteralText("☯少女祈祷中。。。。☯").formatted(Formatting.RED);
			//player.sendMessage(msg,true);
			player.setInvulnerable(true);
			if(needTransPos)
			{
				World world = player.world;
				TransAsyncThread asyncThread = new TransAsyncThread(player,world);
				asyncThread.start();
			}
			else
			{
				player.setInvulnerable(false);
				Text msg = new LiteralText("游戏开始!").formatted(Formatting.YELLOW);
				player.sendMessage(msg,true);
			}
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.setAir(300);
			player.getHungerManager().setFoodLevel(20);
			player.getHungerManager().setSaturationLevel(1.0F);
			player.inventory.clear();
			//msg = new LiteralText("游戏开始!").formatted(Formatting.YELLOW);
			//player.sendMessage(msg,true);
		}
		_isGameStarting = true;
	}
	private void startGame(ServerCommandSource source)
	{
		initStartGame(true,source);
	}

	private void StartGame2(ServerCommandSource source)
	{
		initStartGame(false,source);
	}

	public void playAnvilFallSound(ServerPlayerEntity player,SoundEvent soundEvent)
	{
		World world = player.getEntityWorld();

		// 在指定位置播放音效
		world.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}

	public void sendMSGForEveryPlayer(MinecraftServer server,String str)
	{
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		for (ServerPlayerEntity player : players)
		{
			Text msg = new LiteralText(str).formatted(Formatting.YELLOW);
			player.sendMessage(msg, false);
			//player.playSound(SoundEvents.BLOCK_ANVIL_FALL, 5.0F, 5.0F);
			playAnvilFallSound(player,SoundEvents.BLOCK_ANVIL_LAND);

		}
	}

	boolean shouldSendMSG = true;
	private void onServerTick(MinecraftServer minecraftServer)
	{
		if(!_isGameStarting)
		{
			return;
		}
		//LOGGER.info("server tick running");
		long nowUnixTime = Instant.now().getEpochSecond();
		long deltaTime = nowUnixTime - _startTime;
		//以下是屎山代码
		if (deltaTime==_deathSwapTime-1&&shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在1秒后开始");
			shouldSendMSG = false;
		}
		else if (deltaTime==_deathSwapTime-2&&!shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在2秒后开始");
			shouldSendMSG = true;
		}
		else if (deltaTime==_deathSwapTime-3&&shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在3秒后开始");
			shouldSendMSG = false;
		}
		else if (deltaTime==_deathSwapTime-4&&!shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在4秒后开始");
			shouldSendMSG = true;
		}
		else if (deltaTime==_deathSwapTime-5&&shouldSendMSG)
		{
			sendMSGForEveryPlayer(minecraftServer,"交换将在5秒后开始");
			shouldSendMSG = false;
		}

		if(deltaTime== _deathSwapTime)
		{
			System.out.println("Swap");
			LOGGER.info("Swap");
			_startTime = Instant.now().getEpochSecond();
			swapPlayerPos(minecraftServer.getPlayerManager().getPlayerList());
			shouldSendMSG = true;
		}
	}
	private void swapPlayerPos(List<ServerPlayerEntity> players)
	{
		SwapPosAsync swapPosAsync = new SwapPosAsync(players);
		swapPosAsync.start();
	}
}

//LZX completed this code in 2024/12/08
//LZX-TC-2024-03-21-001