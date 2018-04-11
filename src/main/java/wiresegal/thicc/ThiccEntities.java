package wiresegal.thicc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

import static wiresegal.thicc.ThiccEntities.THICC;

/**
 * @author WireSegal
 * Created at 6:13 PM on 4/10/18.
 */
@Mod(modid = THICC, name = "Thicc Entities", version = "GRADLE:VERSION", guiFactory = "wiresegal.thicc.client.ThiccGooeyHandler")
@Mod.EventBusSubscriber
public class ThiccEntities {

    public static final String THICC = "thiccentities";

    public static ThiccConfig config;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderPre(RenderLivingEvent.Pre e) {
        GlStateManager.pushMatrix();
        float scale = getScaleFactor(e.getEntity());
        if (scale > 0 && scale != 1) {
            float scMinusOne = scale - 1;
            GlStateManager.translate(-e.getX() * scMinusOne, -e.getY() * scMinusOne, -e.getZ() * scMinusOne);
            GlStateManager.scale(scale, scale, scale);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderPost(RenderLivingEvent.Post e) {
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void configReloaded(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (e.getModID().equals(THICC))
            config.init();
    }

    private static final String SPEED_ATT = "Thicc Speed";
    private static final String FSPEED_ATT = "Thicc Flight Speed";
    private static final String HEALTH_ATT = "Thicc Health";
    private static final String FOLLOW_ATT = "Thicc Follow";
    private static final String DAMAGE_ATT = "Thicc Damage";
    private static final String ARMOR_ATT = "Thicc Armor";
    private static final String TOUGH_ATT = "Thicc Tough";
    private static final String REACH_ATT = "Thicc Reach";
    private static final UUID SPEED_UUID = UUID.fromString("89194793-81a2-4dd9-854a-cd279dd90db1");
    private static final UUID FSPEED_UUID = UUID.fromString("3bc6f90b-74a5-4d2f-a9fb-56d18ed3eac0");
    private static final UUID HEALTH_UUID = UUID.fromString("3bb25fff-6a9c-47d0-8f07-13bd6a1e676c");
    private static final UUID FOLLOW_UUID = UUID.fromString("1ce8c66a-6c86-4594-9563-a1a8c2147552");
    private static final UUID DAMAGE_UUID = UUID.fromString("1d4d2d55-9b03-4eb1-8dba-caf8ef85440d");
    private static final UUID ARMOR_UUID = UUID.fromString("99f8aa85-0669-4976-819e-55cc4c6fe68e");
    private static final UUID TOUGH_UUID = UUID.fromString("37949588-c60d-4733-976a-c40e15b3f978");
    private static final UUID REACH_UUID = UUID.fromString("21df8e07-e1e5-41a8-9b90-5230b1453055");


    private static void applyAttribute(Multimap<String, AttributeModifier> attributes,
                                       IAttribute attribute, UUID uuid, String name, double value, int operation) {
        attributes.put(attribute.getName(),
                new AttributeModifier(uuid, name, value - (operation != 0 ? 1 : 0), operation).setSaved(false));
    }

    @SubscribeEvent
    public static void entityAttributes(EntityJoinWorldEvent e) {
        Entity ent = e.getEntity();
        if (ent instanceof EntityLivingBase && !(ent instanceof EntityArmorStand)) {
            EntityLivingBase entity = (EntityLivingBase) ent;
            ResourceLocation loc = ent instanceof EntityPlayer ? new ResourceLocation("player") : EntityList.getKey(entity);

            Multimap<String, AttributeModifier> attributes = HashMultimap.create();
            applyAttribute(attributes, SharedMonsterAttributes.MOVEMENT_SPEED, SPEED_UUID, SPEED_ATT, config.getConfigSpeed(loc), 1);
            applyAttribute(attributes, SharedMonsterAttributes.FLYING_SPEED, FSPEED_UUID, FSPEED_ATT, config.getConfigSpeed(loc), 1);
            applyAttribute(attributes, SharedMonsterAttributes.MAX_HEALTH, HEALTH_UUID, HEALTH_ATT, config.getConfigHealth(loc), 1);
            applyAttribute(attributes, SharedMonsterAttributes.FOLLOW_RANGE, FOLLOW_UUID, FOLLOW_ATT, config.getConfigFollow(loc), 1);
            applyAttribute(attributes, SharedMonsterAttributes.ATTACK_DAMAGE, DAMAGE_UUID, DAMAGE_ATT, config.getConfigDamage(loc), 1);
            applyAttribute(attributes, EntityPlayer.REACH_DISTANCE, REACH_UUID, REACH_ATT, config.getConfigReach(loc), 1);
            applyAttribute(attributes, SharedMonsterAttributes.ARMOR, ARMOR_UUID, ARMOR_ATT, config.getConfigArmor(loc), 0);
            applyAttribute(attributes, SharedMonsterAttributes.ARMOR_TOUGHNESS, TOUGH_UUID, TOUGH_ATT, config.getConfigToughness(loc), 0);

            entity.getAttributeMap().applyAttributeModifiers(attributes);
        }
    }

    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent e) {
        e.getEntityLiving().motionY *= config.getConfigJump(EntityList.getKey(e.getEntity()));
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new ThiccConfig(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config.init();
    }

    public static float getScaleFactor(Entity entity) {
        ResourceLocation loc = entity instanceof EntityPlayer ? new ResourceLocation("player") : EntityList.getKey(entity);
        return config.getConfigScale(loc);
    }
}
