package weather2;

import weather2.volcano.VolcanoObject;
import weather2.weathersystem.WeatherManagerBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;

public class CommandWeather2 extends CommandBase {

	@Override
	public String getCommandName() {
		return "weather2";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equals("volcano")) {
					if (var2[1].equals("create")) {
						if (player.worldObj.provider.dimensionId == 0) {
							WeatherManagerBase wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
							VolcanoObject vo = new VolcanoObject(wm);
							vo.pos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
							vo.initFirstTime();
							wm.addStormObject(vo);
							vo.initPost();
							var1.sendChatToPlayer(new ChatMessageComponent().addText("volcano created"));
						} else {
							var1.sendChatToPlayer(new ChatMessageComponent().addText("can only make volcanos on main overworld"));
						}
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling Weather2 command");
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
