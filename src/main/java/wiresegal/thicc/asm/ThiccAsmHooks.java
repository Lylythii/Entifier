package wiresegal.thicc.asm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import wiresegal.thicc.ThiccEntities;

/**
 * @author WireSegal
 * Created at 6:17 PM on 4/10/18.
 */
@SuppressWarnings("unused")
public class ThiccAsmHooks {
    public static float rescale(Entity entity, float size) {
        if (!(entity instanceof EntityLivingBase) || entity instanceof IEntityMultiPart)
            return size;
        float scaleFactor = ThiccEntities.getScaleFactor(entity);
        if (scaleFactor > 0 && scaleFactor != 1f)
            return size * scaleFactor;
        return size;
    }

    public static float descale(Entity entity, float size) {
        if (!(entity instanceof EntityLivingBase) || entity instanceof IEntityMultiPart)
            return size;
        float scaleFactor = ThiccEntities.getScaleFactor(entity);
        if (scaleFactor > 0 && scaleFactor != 1f)
            return size / scaleFactor;
        return size;
    }


    public static float rescale(float size, Entity entity) {
        return rescale(entity, size);
    }

    public static float descale(float size, Entity entity) {
        return descale(entity, size);
    }
}
