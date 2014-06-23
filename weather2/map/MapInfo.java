package weather2.map;

import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class MapInfo
{
    /** Reference for EntityPlayer object in MapInfo */
    public final EntityPlayer entityplayerObj;
    public int[] field_76209_b;
    public int[] field_76210_c;

    /**
     * updated by x = mod(x*11,128) +1  x-1 is used to index field_76209_b and field_76210_c
     */
    private int currentRandomNumber;
    private int ticksUntilPlayerLocationMapUpdate;

    /**
     * a cache of the result from getPlayersOnMap so that it is not resent when nothing changes
     */
    private byte[] lastPlayerLocationOnMap;
    public int field_82569_d;
    private boolean field_82570_i;

    /** reference in MapInfo to MapData object */
    final MapData mapDataObj;

    public MapInfo(MapData par1MapData, EntityPlayer par2EntityPlayer)
    {
        this.mapDataObj = par1MapData;
        this.field_76209_b = new int[128];
        this.field_76210_c = new int[128];
        this.entityplayerObj = par2EntityPlayer;

        for (int i = 0; i < this.field_76209_b.length; ++i)
        {
            this.field_76209_b[i] = 0;
            this.field_76210_c[i] = 127;
        }
    }

    /**
     * returns a 1+players*3 array, of x,y, and color . the name of this function may be partially wrong, as there is a
     * second branch to the code here
     */
    public byte[] getPlayersOnMap(ItemStack par1ItemStack)
    {
        byte[] abyte;

        if (!this.field_82570_i)
        {
            abyte = new byte[] {(byte)2, this.mapDataObj.scale};
            this.field_82570_i = true;
            return abyte;
        }
        else
        {
            int i;
            int j;

            if (--this.ticksUntilPlayerLocationMapUpdate < 0)
            {
                this.ticksUntilPlayerLocationMapUpdate = 4;
                abyte = new byte[this.mapDataObj.playersVisibleOnMap.size() * 3 + 1];
                abyte[0] = 1;
                i = 0;

                for (Iterator iterator = this.mapDataObj.playersVisibleOnMap.values().iterator(); iterator.hasNext(); ++i)
                {
                    MapCoord mapcoord = (MapCoord)iterator.next();
                    abyte[i * 3 + 1] = (byte)(mapcoord.iconSize << 4 | mapcoord.iconRotation & 15);
                    abyte[i * 3 + 2] = mapcoord.centerX;
                    abyte[i * 3 + 3] = mapcoord.centerZ;
                }

                boolean flag = !par1ItemStack.isOnItemFrame();

                if (this.lastPlayerLocationOnMap != null && this.lastPlayerLocationOnMap.length == abyte.length)
                {
                    for (j = 0; j < abyte.length; ++j)
                    {
                        if (abyte[j] != this.lastPlayerLocationOnMap[j])
                        {
                            flag = false;
                            break;
                        }
                    }
                }
                else
                {
                    flag = false;
                }

                if (!flag)
                {
                    this.lastPlayerLocationOnMap = abyte;
                    return abyte;
                }
            }

            for (int k = 0; k < 1; ++k)
            {
                i = this.currentRandomNumber++ * 11 % 128;

                if (this.field_76209_b[i] >= 0)
                {
                    int l = this.field_76210_c[i] - this.field_76209_b[i] + 1;
                    j = this.field_76209_b[i];
                    byte[] abyte1 = new byte[l + 3];
                    abyte1[0] = 0;
                    abyte1[1] = (byte)i;
                    abyte1[2] = (byte)j;

                    for (int i1 = 0; i1 < abyte1.length - 3; ++i1)
                    {
                        abyte1[i1 + 3] = this.mapDataObj.colors[(i1 + j) * 128 + i];
                    }

                    this.field_76210_c[i] = -1;
                    this.field_76209_b[i] = -1;
                    return abyte1;
                }
            }

            return null;
        }
    }
}
