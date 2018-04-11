package wiresegal.thicc;

import com.google.common.collect.Sets;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WireSegal
 * Created at 10:13 PM on 4/10/18.
 */
public class ThiccConfig {

    public Configuration configInstance;

    private final Map<ResourceLocation, Double> SCALE_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> SPEED_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> DAMAGE_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> FOLLOW_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> HEALTH_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> ARMOR_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> TOUGH_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> REACH_CONFIG = new HashMap<>();
    private final Map<ResourceLocation, Double> JUMP_CONFIG = new HashMap<>();


    public float getConfigScale(ResourceLocation name) {
        return SCALE_CONFIG.getOrDefault(name, 1.0).floatValue();
    }

    public double getConfigSpeed(ResourceLocation name) {
        return SPEED_CONFIG.getOrDefault(name, 1.0);
    }

    public double getConfigDamage(ResourceLocation name) {
        return DAMAGE_CONFIG.getOrDefault(name, 1.0);
    }

    public double getConfigFollow(ResourceLocation name) {
        return FOLLOW_CONFIG.getOrDefault(name, 1.0);
    }

    public double getConfigHealth(ResourceLocation name) {
        return HEALTH_CONFIG.getOrDefault(name, 1.0);
    }

    public double getConfigArmor(ResourceLocation name) {
        return ARMOR_CONFIG.getOrDefault(name, 0.0);
    }

    public double getConfigToughness(ResourceLocation name) {
        return TOUGH_CONFIG.getOrDefault(name, 0.0);
    }

    public double getConfigReach(ResourceLocation name) {
        return REACH_CONFIG.getOrDefault(name, 1.0);
    }

    public double getConfigJump(ResourceLocation name) {
        return JUMP_CONFIG.getOrDefault(name, 1.0);
    }

    private final File configFile;

    public ThiccConfig(File configFile) {
        this.configFile = configFile;
    }

    public void init() {
        if (configInstance == null)
            configInstance = new Configuration(configFile, true);
        else
            configInstance.load();

        ResourceLocation player = new ResourceLocation("player");

        for (ResourceLocation location : Sets.union(EntityList.getEntityNameList(), Sets.newHashSet(player))) {
            Class<? extends Entity> entityClass = location == player ? EntityPlayer.class : EntityList.getClass(location);
            if (entityClass != null && entityClass != EntityArmorStand.class &&
                    EntityLivingBase.class.isAssignableFrom(entityClass) &&
                    !IEntityMultiPart.class.isAssignableFrom(entityClass)) {

                SCALE_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "scale", 1.0,
                        "The factor to scale the entity by. Default: 1")
                        .setRequiresWorldRestart(true).getDouble(1));
                SPEED_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "speed", 1.0,
                        "The factor to scale the entity's speed by. Default: 1")
                        .setRequiresWorldRestart(true).getDouble(1));
                DAMAGE_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "damage", 1.0,
                        "The factor to scale the entity's damage by. Default: 1")
                        .setRequiresWorldRestart(true).getDouble(1));
                HEALTH_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "health", 1.0,
                        "The factor to scale the entity's max health by. Default: 1")
                        .setRequiresWorldRestart(true).getDouble(1));
                JUMP_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "jump", 1.0,
                        "The factor to scale the player's jump strength by. Default: 1")
                        .setRequiresWorldRestart(true).getDouble(1.0));

                ARMOR_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "armor", 0.0,
                        "The amount of extra armor to give the entity. Default: 0")
                        .setRequiresWorldRestart(true).getDouble(0.0));
                TOUGH_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                        "toughness", 0.0,
                        "The amount of extra armor toughness to give the entity. Default: 0")
                        .setRequiresWorldRestart(true).getDouble(0.0));

                if (EntityLiving.class.isAssignableFrom(entityClass))
                    FOLLOW_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                            "followRange", 1.0,
                            "The factor to scale the entity's follow range by. Default: 1")
                            .setRequiresWorldRestart(true).getDouble(1));
                if (EntityPlayer.class.isAssignableFrom(entityClass))
                    REACH_CONFIG.put(location, configInstance.get(location.getResourceDomain() + "." + location.getResourcePath(),
                            "reach", 1.0,
                            "The factor to scale the entity's reach distance by. Default: 1")
                            .setRequiresWorldRestart(true).getDouble(1.0));
            }

        }

        configInstance.save();
    }
}
